package com.airbnb.lottie;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Build.VERSION;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.airbnb.lottie.manager.FontAssetManager;
import com.airbnb.lottie.manager.ImageAssetManager;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.model.layer.Layer.Factory;
import com.airbnb.lottie.utils.LottieValueAnimator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LottieDrawable extends Drawable implements Callback {
    private static final String TAG = LottieDrawable.class.getSimpleName();
    private int alpha = 255;
    private final LottieValueAnimator animator = new LottieValueAnimator();
    private final Set<ColorFilterData> colorFilterData = new HashSet();
    private LottieComposition composition;
    @Nullable
    private CompositionLayer compositionLayer;
    private boolean enableMergePaths;
    @Nullable
    FontAssetDelegate fontAssetDelegate;
    @Nullable
    private FontAssetManager fontAssetManager;
    @Nullable
    private ImageAssetDelegate imageAssetDelegate;
    @Nullable
    private ImageAssetManager imageAssetManager;
    @Nullable
    private String imageAssetsFolder;
    private final ArrayList<LazyCompositionTask> lazyCompositionTasks = new ArrayList();
    private final Matrix matrix = new Matrix();
    private boolean performanceTrackingEnabled;
    private float scale = 1.0f;
    private float speed = 1.0f;
    private boolean systemAnimationsAreDisabled;
    @Nullable
    TextDelegate textDelegate;

    private static class ColorFilterData {
        @Nullable
        final ColorFilter colorFilter;
        @Nullable
        final String contentName;
        final String layerName;

        ColorFilterData(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
            this.layerName = layerName;
            this.contentName = contentName;
            this.colorFilter = colorFilter;
        }

        public int hashCode() {
            int hashCode = 17;
            if (this.layerName != null) {
                hashCode = (17 * 31) * this.layerName.hashCode();
            }
            if (this.contentName != null) {
                return (hashCode * 31) * this.contentName.hashCode();
            }
            return hashCode;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ColorFilterData)) {
                return false;
            }
            ColorFilterData other = (ColorFilterData) obj;
            if (!(hashCode() == other.hashCode() && this.colorFilter == other.colorFilter)) {
                z = false;
            }
            return z;
        }
    }

    private interface LazyCompositionTask {
        void run(LottieComposition lottieComposition);
    }

    public LottieDrawable() {
        this.animator.setRepeatCount(0);
        this.animator.setInterpolator(new LinearInterpolator());
        this.animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (LottieDrawable.this.compositionLayer != null) {
                    LottieDrawable.this.compositionLayer.setProgress(LottieDrawable.this.animator.getProgress());
                }
            }
        });
    }

    public boolean hasMasks() {
        return this.compositionLayer != null && this.compositionLayer.hasMasks();
    }

    public boolean hasMatte() {
        return this.compositionLayer != null && this.compositionLayer.hasMatte();
    }

    public boolean enableMergePathsForKitKatAndAbove() {
        return this.enableMergePaths;
    }

    public void enableMergePathsForKitKatAndAbove(boolean enable) {
        if (VERSION.SDK_INT < 19) {
            Log.w(TAG, "Merge paths are not supported pre-Kit Kat.");
            return;
        }
        this.enableMergePaths = enable;
        if (this.composition != null) {
            buildCompositionLayer();
        }
    }

    public void setImagesAssetsFolder(@Nullable String imageAssetsFolder) {
        this.imageAssetsFolder = imageAssetsFolder;
    }

    @Nullable
    public String getImageAssetsFolder() {
        return this.imageAssetsFolder;
    }

    public void recycleBitmaps() {
        if (this.imageAssetManager != null) {
            this.imageAssetManager.recycleBitmaps();
        }
    }

    public boolean setComposition(LottieComposition composition) {
        if (this.composition == composition) {
            return false;
        }
        clearComposition();
        this.composition = composition;
        setSpeed(this.speed);
        setScale(this.scale);
        updateBounds();
        buildCompositionLayer();
        applyColorFilters();
        Iterator<LazyCompositionTask> it = new ArrayList(this.lazyCompositionTasks).iterator();
        while (it.hasNext()) {
            ((LazyCompositionTask) it.next()).run(composition);
            it.remove();
        }
        this.lazyCompositionTasks.clear();
        composition.setPerformanceTrackingEnabled(this.performanceTrackingEnabled);
        this.animator.forceUpdate();
        return true;
    }

    public void setPerformanceTrackingEnabled(boolean enabled) {
        this.performanceTrackingEnabled = enabled;
        if (this.composition != null) {
            this.composition.setPerformanceTrackingEnabled(enabled);
        }
    }

    @Nullable
    public PerformanceTracker getPerformanceTracker() {
        if (this.composition != null) {
            return this.composition.getPerformanceTracker();
        }
        return null;
    }

    private void buildCompositionLayer() {
        this.compositionLayer = new CompositionLayer(this, Factory.newInstance(this.composition), this.composition.getLayers(), this.composition);
    }

    private void applyColorFilters() {
        if (this.compositionLayer != null) {
            for (ColorFilterData data : this.colorFilterData) {
                this.compositionLayer.addColorFilter(data.layerName, data.contentName, data.colorFilter);
            }
        }
    }

    private void clearComposition() {
        recycleBitmaps();
        this.compositionLayer = null;
        this.imageAssetManager = null;
        invalidateSelf();
    }

    public void invalidateSelf() {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        this.alpha = alpha;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Use addColorFilter instead.");
    }

    public void addColorFilterToContent(String layerName, String contentName, @Nullable ColorFilter colorFilter) {
        addColorFilterInternal(layerName, contentName, colorFilter);
    }

    public void addColorFilterToLayer(String layerName, @Nullable ColorFilter colorFilter) {
        addColorFilterInternal(layerName, null, colorFilter);
    }

    public void addColorFilter(ColorFilter colorFilter) {
        addColorFilterInternal(null, null, colorFilter);
    }

    public void clearColorFilters() {
        this.colorFilterData.clear();
        addColorFilterInternal(null, null, null);
    }

    private void addColorFilterInternal(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        ColorFilterData data = new ColorFilterData(layerName, contentName, colorFilter);
        if (colorFilter == null && this.colorFilterData.contains(data)) {
            this.colorFilterData.remove(data);
        } else {
            this.colorFilterData.add(new ColorFilterData(layerName, contentName, colorFilter));
        }
        if (this.compositionLayer != null) {
            this.compositionLayer.addColorFilter(layerName, contentName, colorFilter);
        }
    }

    public int getOpacity() {
        return -3;
    }

    public void draw(@NonNull Canvas canvas) {
        L.beginSection("Drawable#draw");
        if (this.compositionLayer != null) {
            float scale = this.scale;
            float extraScale = 1.0f;
            float maxScale = getMaxScale(canvas);
            if (scale > maxScale) {
                scale = maxScale;
                extraScale = this.scale / scale;
            }
            if (extraScale > 1.0f) {
                canvas.save();
                float halfWidth = ((float) this.composition.getBounds().width()) / 2.0f;
                float halfHeight = ((float) this.composition.getBounds().height()) / 2.0f;
                float scaledHalfWidth = halfWidth * scale;
                float scaledHalfHeight = halfHeight * scale;
                canvas.translate((getScale() * halfWidth) - scaledHalfWidth, (getScale() * halfHeight) - scaledHalfHeight);
                canvas.scale(extraScale, extraScale, scaledHalfWidth, scaledHalfHeight);
            }
            this.matrix.reset();
            this.matrix.preScale(scale, scale);
            this.compositionLayer.draw(canvas, this.matrix, this.alpha);
            L.endSection("Drawable#draw");
            if (extraScale > 1.0f) {
                canvas.restore();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void systemAnimationsAreDisabled() {
        this.systemAnimationsAreDisabled = true;
        this.animator.systemAnimationsAreDisabled();
    }

    public void loop(boolean loop) {
        this.animator.setRepeatCount(loop ? -1 : 0);
    }

    public boolean isLooping() {
        return this.animator.getRepeatCount() == -1;
    }

    public boolean isAnimating() {
        return this.animator.isRunning();
    }

    public void playAnimation() {
        playAnimation(true);
    }

    public void resumeAnimation() {
        boolean z = this.animator.getAnimatedFraction() == this.animator.getMaxProgress() || this.systemAnimationsAreDisabled;
        playAnimation(z);
    }

    private void playAnimation(final boolean resetProgress) {
        if (this.compositionLayer == null) {
            this.lazyCompositionTasks.add(new LazyCompositionTask() {
                public void run(LottieComposition composition) {
                    LottieDrawable.this.playAnimation(resetProgress);
                }
            });
            return;
        }
        if (resetProgress) {
            this.animator.start();
        } else {
            this.animator.resumeAnimation();
        }
    }

    public void playAnimation(final int startFrame, final int endFrame) {
        if (this.composition == null) {
            this.lazyCompositionTasks.add(new LazyCompositionTask() {
                public void run(LottieComposition composition) {
                    LottieDrawable.this.playAnimation(startFrame, endFrame);
                }
            });
        } else {
            playAnimation(((float) startFrame) / this.composition.getDurationFrames(), ((float) endFrame) / this.composition.getDurationFrames());
        }
    }

    public void playAnimation(@FloatRange(from = 0.0d, to = 1.0d) float startProgress, @FloatRange(from = 0.0d, to = 1.0d) float endProgress) {
        this.animator.updateValues(startProgress, endProgress);
        this.animator.setCurrentPlayTime(0);
        setProgress(startProgress);
        playAnimation(false);
    }

    public void resumeReverseAnimation() {
        reverseAnimation(false);
    }

    public void reverseAnimation() {
        float progress = getProgress();
        reverseAnimation(true);
    }

    private void reverseAnimation(final boolean resetProgress) {
        if (this.compositionLayer == null) {
            this.lazyCompositionTasks.add(new LazyCompositionTask() {
                public void run(LottieComposition composition) {
                    LottieDrawable.this.reverseAnimation(resetProgress);
                }
            });
            return;
        }
        float progress = this.animator.getProgress();
        this.animator.reverse();
        if (resetProgress || getProgress() == 1.0f) {
            this.animator.setProgress(this.animator.getMinProgress());
        } else {
            this.animator.setProgress(progress);
        }
    }

    public void setMinFrame(final int minFrame) {
        if (this.composition == null) {
            this.lazyCompositionTasks.add(new LazyCompositionTask() {
                public void run(LottieComposition composition) {
                    LottieDrawable.this.setMinFrame(minFrame);
                }
            });
        } else {
            setMinProgress(((float) minFrame) / this.composition.getDurationFrames());
        }
    }

    public void setMinProgress(float minProgress) {
        this.animator.setMinProgress(minProgress);
    }

    public void setMaxFrame(final int maxFrame) {
        if (this.composition == null) {
            this.lazyCompositionTasks.add(new LazyCompositionTask() {
                public void run(LottieComposition composition) {
                    LottieDrawable.this.setMaxFrame(maxFrame);
                }
            });
        } else {
            setMaxProgress(((float) maxFrame) / this.composition.getDurationFrames());
        }
    }

    public void setMaxProgress(float maxProgress) {
        this.animator.setMaxProgress(maxProgress);
    }

    public void setMinAndMaxFrame(int minFrame, int maxFrame) {
        setMinFrame(minFrame);
        setMaxFrame(maxFrame);
    }

    public void setMinAndMaxProgress(float minProgress, float maxProgress) {
        setMinProgress(minProgress);
        setMaxProgress(maxProgress);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.animator.setIsReversed(speed < 0.0f);
        if (this.composition != null) {
            this.animator.setDuration((long) (((float) this.composition.getDuration()) / Math.abs(speed)));
        }
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        this.animator.setProgress(progress);
        if (this.compositionLayer != null) {
            this.compositionLayer.setProgress(progress);
        }
    }

    public float getProgress() {
        return this.animator.getProgress();
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateBounds();
    }

    public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
        this.imageAssetDelegate = assetDelegate;
        if (this.imageAssetManager != null) {
            this.imageAssetManager.setDelegate(assetDelegate);
        }
    }

    public void setFontAssetDelegate(FontAssetDelegate assetDelegate) {
        this.fontAssetDelegate = assetDelegate;
        if (this.fontAssetManager != null) {
            this.fontAssetManager.setDelegate(assetDelegate);
        }
    }

    public void setTextDelegate(TextDelegate textDelegate) {
        this.textDelegate = textDelegate;
    }

    @Nullable
    public TextDelegate getTextDelegate() {
        return this.textDelegate;
    }

    public boolean useTextGlyphs() {
        return this.textDelegate == null && this.composition.getCharacters().size() > 0;
    }

    public float getScale() {
        return this.scale;
    }

    public LottieComposition getComposition() {
        return this.composition;
    }

    private void updateBounds() {
        if (this.composition != null) {
            float scale = getScale();
            setBounds(0, 0, (int) (((float) this.composition.getBounds().width()) * scale), (int) (((float) this.composition.getBounds().height()) * scale));
        }
    }

    public void cancelAnimation() {
        this.lazyCompositionTasks.clear();
        this.animator.cancel();
    }

    public void addAnimatorUpdateListener(AnimatorUpdateListener updateListener) {
        this.animator.addUpdateListener(updateListener);
    }

    public void removeAnimatorUpdateListener(AnimatorUpdateListener updateListener) {
        this.animator.removeUpdateListener(updateListener);
    }

    public void addAnimatorListener(AnimatorListener listener) {
        this.animator.addListener(listener);
    }

    public void removeAnimatorListener(AnimatorListener listener) {
        this.animator.removeListener(listener);
    }

    public int getIntrinsicWidth() {
        return this.composition == null ? -1 : (int) (((float) this.composition.getBounds().width()) * getScale());
    }

    public int getIntrinsicHeight() {
        return this.composition == null ? -1 : (int) (((float) this.composition.getBounds().height()) * getScale());
    }

    @Nullable
    public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
        ImageAssetManager bm = getImageAssetManager();
        if (bm == null) {
            Log.w(L.TAG, "Cannot update bitmap. Most likely the drawable is not added to a View which prevents Lottie from getting a Context.");
            return null;
        }
        Bitmap ret = bm.updateBitmap(id, bitmap);
        invalidateSelf();
        return ret;
    }

    @Nullable
    public Bitmap getImageAsset(String id) {
        ImageAssetManager bm = getImageAssetManager();
        if (bm != null) {
            return bm.bitmapForId(id);
        }
        return null;
    }

    private ImageAssetManager getImageAssetManager() {
        if (getCallback() == null) {
            return null;
        }
        if (!(this.imageAssetManager == null || this.imageAssetManager.hasSameContext(getContext()))) {
            this.imageAssetManager.recycleBitmaps();
            this.imageAssetManager = null;
        }
        if (this.imageAssetManager == null) {
            this.imageAssetManager = new ImageAssetManager(getCallback(), this.imageAssetsFolder, this.imageAssetDelegate, this.composition.getImages());
        }
        return this.imageAssetManager;
    }

    @Nullable
    public Typeface getTypeface(String fontFamily, String style) {
        FontAssetManager assetManager = getFontAssetManager();
        if (assetManager != null) {
            return assetManager.getTypeface(fontFamily, style);
        }
        return null;
    }

    private FontAssetManager getFontAssetManager() {
        if (getCallback() == null) {
            return null;
        }
        if (this.fontAssetManager == null) {
            this.fontAssetManager = new FontAssetManager(getCallback(), this.fontAssetDelegate);
        }
        return this.fontAssetManager;
    }

    @Nullable
    private Context getContext() {
        Callback callback = getCallback();
        if (callback != null && (callback instanceof View)) {
            return ((View) callback).getContext();
        }
        return null;
    }

    public void invalidateDrawable(@NonNull Drawable who) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    private float getMaxScale(@NonNull Canvas canvas) {
        return Math.min(((float) canvas.getWidth()) / ((float) this.composition.getBounds().width()), ((float) canvas.getHeight()) / ((float) this.composition.getBounds().height()));
    }
}
