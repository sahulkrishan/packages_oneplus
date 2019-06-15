package com.airbnb.lottie;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View.BaseSavedState;
import com.airbnb.lottie.LottieComposition.Factory;
import com.airbnb.lottie.utils.Utils;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class LottieAnimationView extends AppCompatImageView {
    private static final Map<String, LottieComposition> STRONG_REF_CACHE = new HashMap();
    private static final String TAG = LottieAnimationView.class.getSimpleName();
    private static final Map<String, WeakReference<LottieComposition>> WEAK_REF_CACHE = new HashMap();
    private String animationName;
    private boolean autoPlay = false;
    @Nullable
    private LottieComposition composition;
    @Nullable
    private Cancellable compositionLoader;
    private CacheStrategy defaultCacheStrategy;
    private final OnCompositionLoadedListener loadedListener = new OnCompositionLoadedListener() {
        public void onCompositionLoaded(@Nullable LottieComposition composition) {
            if (composition != null) {
                LottieAnimationView.this.setComposition(composition);
            }
            LottieAnimationView.this.compositionLoader = null;
        }
    };
    private final LottieDrawable lottieDrawable = new LottieDrawable();
    private boolean useHardwareLayer = false;
    private boolean wasAnimatingWhenDetached = false;

    public enum CacheStrategy {
        None,
        Weak,
        Strong
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String animationName;
        String imageAssetsFolder;
        boolean isAnimating;
        boolean isLooping;
        float progress;

        /* synthetic */ SavedState(Parcel x0, AnonymousClass1 x1) {
            this(x0);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.animationName = in.readString();
            this.progress = in.readFloat();
            boolean z = false;
            this.isAnimating = in.readInt() == 1;
            if (in.readInt() == 1) {
                z = true;
            }
            this.isLooping = z;
            this.imageAssetsFolder = in.readString();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.animationName);
            out.writeFloat(this.progress);
            out.writeInt(this.isAnimating);
            out.writeInt(this.isLooping);
            out.writeString(this.imageAssetsFolder);
        }
    }

    public LottieAnimationView(Context context) {
        super(context);
        init(null);
    }

    public LottieAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LottieAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LottieAnimationView);
        this.defaultCacheStrategy = CacheStrategy.values()[ta.getInt(R.styleable.LottieAnimationView_lottie_cacheStrategy, CacheStrategy.Weak.ordinal())];
        String fileName = ta.getString(R.styleable.LottieAnimationView_lottie_fileName);
        if (!(isInEditMode() || fileName == null)) {
            setAnimation(fileName);
        }
        if (ta.getBoolean(R.styleable.LottieAnimationView_lottie_autoPlay, false)) {
            this.lottieDrawable.playAnimation();
            this.autoPlay = true;
        }
        this.lottieDrawable.loop(ta.getBoolean(R.styleable.LottieAnimationView_lottie_loop, false));
        setImageAssetsFolder(ta.getString(R.styleable.LottieAnimationView_lottie_imageAssetsFolder));
        setProgress(ta.getFloat(R.styleable.LottieAnimationView_lottie_progress, 0.0f));
        enableMergePathsForKitKatAndAbove(ta.getBoolean(R.styleable.LottieAnimationView_lottie_enableMergePathsForKitKatAndAbove, false));
        if (ta.hasValue(R.styleable.LottieAnimationView_lottie_colorFilter)) {
            addColorFilter(new SimpleColorFilter(ta.getColor(R.styleable.LottieAnimationView_lottie_colorFilter, 0)));
        }
        if (ta.hasValue(R.styleable.LottieAnimationView_lottie_scale)) {
            this.lottieDrawable.setScale(ta.getFloat(R.styleable.LottieAnimationView_lottie_scale, 1.0f));
        }
        ta.recycle();
        if (Utils.getAnimationScale(getContext()) == 0.0f) {
            this.lottieDrawable.systemAnimationsAreDisabled();
        }
        enableOrDisableHardwareLayer();
    }

    public void setImageResource(int resId) {
        recycleBitmaps();
        cancelLoaderTask();
        super.setImageResource(resId);
    }

    public void setImageDrawable(Drawable drawable) {
        if (drawable != this.lottieDrawable) {
            recycleBitmaps();
        }
        cancelLoaderTask();
        super.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        recycleBitmaps();
        cancelLoaderTask();
        super.setImageBitmap(bm);
    }

    public void addColorFilterToContent(String layerName, String contentName, @Nullable ColorFilter colorFilter) {
        this.lottieDrawable.addColorFilterToContent(layerName, contentName, colorFilter);
    }

    public void addColorFilterToLayer(String layerName, @Nullable ColorFilter colorFilter) {
        this.lottieDrawable.addColorFilterToLayer(layerName, colorFilter);
    }

    public void addColorFilter(@Nullable ColorFilter colorFilter) {
        this.lottieDrawable.addColorFilter(colorFilter);
    }

    public void clearColorFilters() {
        this.lottieDrawable.clearColorFilters();
    }

    public void invalidateDrawable(@NonNull Drawable dr) {
        if (getDrawable() == this.lottieDrawable) {
            super.invalidateDrawable(this.lottieDrawable);
        } else {
            super.invalidateDrawable(dr);
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.animationName = this.animationName;
        ss.progress = this.lottieDrawable.getProgress();
        ss.isAnimating = this.lottieDrawable.isAnimating();
        ss.isLooping = this.lottieDrawable.isLooping();
        ss.imageAssetsFolder = this.lottieDrawable.getImageAssetsFolder();
        return ss;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            this.animationName = ss.animationName;
            if (!TextUtils.isEmpty(this.animationName)) {
                setAnimation(this.animationName);
            }
            setProgress(ss.progress);
            loop(ss.isLooping);
            if (ss.isAnimating) {
                playAnimation();
            }
            this.lottieDrawable.setImagesAssetsFolder(ss.imageAssetsFolder);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.autoPlay && this.wasAnimatingWhenDetached) {
            playAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        if (isAnimating()) {
            cancelAnimation();
            this.wasAnimatingWhenDetached = true;
        }
        recycleBitmaps();
        super.onDetachedFromWindow();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void recycleBitmaps() {
        if (this.lottieDrawable != null) {
            this.lottieDrawable.recycleBitmaps();
        }
    }

    public void enableMergePathsForKitKatAndAbove(boolean enable) {
        this.lottieDrawable.enableMergePathsForKitKatAndAbove(enable);
    }

    @Deprecated
    public void useExperimentalHardwareAcceleration() {
        useHardwareAcceleration(true);
    }

    @Deprecated
    public void useExperimentalHardwareAcceleration(boolean use) {
        useHardwareAcceleration(use);
    }

    public void useHardwareAcceleration() {
        useHardwareAcceleration(true);
    }

    public void useHardwareAcceleration(boolean use) {
        this.useHardwareLayer = use;
        enableOrDisableHardwareLayer();
    }

    public void setAnimation(String animationName) {
        setAnimation(animationName, this.defaultCacheStrategy);
    }

    public void setAnimation(final String animationName, final CacheStrategy cacheStrategy) {
        this.animationName = animationName;
        if (WEAK_REF_CACHE.containsKey(animationName)) {
            LottieComposition ref = (LottieComposition) ((WeakReference) WEAK_REF_CACHE.get(animationName)).get();
            if (ref != null) {
                setComposition(ref);
                return;
            }
        } else if (STRONG_REF_CACHE.containsKey(animationName)) {
            setComposition((LottieComposition) STRONG_REF_CACHE.get(animationName));
            return;
        }
        this.animationName = animationName;
        this.lottieDrawable.cancelAnimation();
        cancelLoaderTask();
        this.compositionLoader = Factory.fromAssetFileName(getContext(), animationName, new OnCompositionLoadedListener() {
            public void onCompositionLoaded(LottieComposition composition) {
                if (cacheStrategy == CacheStrategy.Strong) {
                    LottieAnimationView.STRONG_REF_CACHE.put(animationName, composition);
                } else if (cacheStrategy == CacheStrategy.Weak) {
                    LottieAnimationView.WEAK_REF_CACHE.put(animationName, new WeakReference(composition));
                }
                LottieAnimationView.this.setComposition(composition);
            }
        });
    }

    public void setAnimation(JSONObject json) {
        cancelLoaderTask();
        this.compositionLoader = Factory.fromJson(getResources(), json, this.loadedListener);
    }

    private void cancelLoaderTask() {
        if (this.compositionLoader != null) {
            this.compositionLoader.cancel();
            this.compositionLoader = null;
        }
    }

    public void setComposition(@NonNull LottieComposition composition) {
        this.lottieDrawable.setCallback(this);
        boolean isNewComposition = this.lottieDrawable.setComposition(composition);
        enableOrDisableHardwareLayer();
        if (isNewComposition) {
            setImageDrawable(null);
            setImageDrawable(this.lottieDrawable);
            this.composition = composition;
            requestLayout();
        }
    }

    public boolean hasMasks() {
        return this.lottieDrawable.hasMasks();
    }

    public boolean hasMatte() {
        return this.lottieDrawable.hasMatte();
    }

    public void addAnimatorUpdateListener(AnimatorUpdateListener updateListener) {
        this.lottieDrawable.addAnimatorUpdateListener(updateListener);
    }

    public void removeUpdateListener(AnimatorUpdateListener updateListener) {
        this.lottieDrawable.removeAnimatorUpdateListener(updateListener);
    }

    public void addAnimatorListener(AnimatorListener listener) {
        this.lottieDrawable.addAnimatorListener(listener);
    }

    public void removeAnimatorListener(AnimatorListener listener) {
        this.lottieDrawable.removeAnimatorListener(listener);
    }

    public void loop(boolean loop) {
        this.lottieDrawable.loop(loop);
    }

    public boolean isAnimating() {
        return this.lottieDrawable.isAnimating();
    }

    public void playAnimation() {
        this.lottieDrawable.playAnimation();
        enableOrDisableHardwareLayer();
    }

    public void resumeAnimation() {
        this.lottieDrawable.resumeAnimation();
        enableOrDisableHardwareLayer();
    }

    public void playAnimation(int startFrame, int endFrame) {
        this.lottieDrawable.playAnimation(startFrame, endFrame);
    }

    public void playAnimation(@FloatRange(from = 0.0d, to = 1.0d) float startProgress, @FloatRange(from = 0.0d, to = 1.0d) float endProgress) {
        this.lottieDrawable.playAnimation(startProgress, endProgress);
    }

    public void reverseAnimation() {
        this.lottieDrawable.reverseAnimation();
        enableOrDisableHardwareLayer();
    }

    public void setMinFrame(int startFrame) {
        this.lottieDrawable.setMinFrame(startFrame);
    }

    public void setMinProgress(float startProgress) {
        this.lottieDrawable.setMinProgress(startProgress);
    }

    public void setMaxFrame(int endFrame) {
        this.lottieDrawable.setMaxFrame(endFrame);
    }

    public void setMaxProgress(float endProgress) {
        this.lottieDrawable.setMaxProgress(endProgress);
    }

    public void setMinAndMaxFrame(int minFrame, int maxFrame) {
        this.lottieDrawable.setMinAndMaxFrame(minFrame, maxFrame);
    }

    public void setMinAndMaxProgress(float minProgress, float maxProgress) {
        this.lottieDrawable.setMinAndMaxProgress(minProgress, maxProgress);
    }

    public void resumeReverseAnimation() {
        this.lottieDrawable.resumeReverseAnimation();
        enableOrDisableHardwareLayer();
    }

    public void setSpeed(float speed) {
        this.lottieDrawable.setSpeed(speed);
    }

    public void setImageAssetsFolder(String imageAssetsFolder) {
        this.lottieDrawable.setImagesAssetsFolder(imageAssetsFolder);
    }

    @Nullable
    public String getImageAssetsFolder() {
        return this.lottieDrawable.getImageAssetsFolder();
    }

    @Nullable
    public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
        return this.lottieDrawable.updateBitmap(id, bitmap);
    }

    public void setImageAssetDelegate(ImageAssetDelegate assetDelegate) {
        this.lottieDrawable.setImageAssetDelegate(assetDelegate);
    }

    public void setFontAssetDelegate(FontAssetDelegate assetDelegate) {
        this.lottieDrawable.setFontAssetDelegate(assetDelegate);
    }

    public void setTextDelegate(TextDelegate textDelegate) {
        this.lottieDrawable.setTextDelegate(textDelegate);
    }

    public void setScale(float scale) {
        this.lottieDrawable.setScale(scale);
        if (getDrawable() == this.lottieDrawable) {
            setImageDrawable(null);
            setImageDrawable(this.lottieDrawable);
        }
    }

    public float getScale() {
        return this.lottieDrawable.getScale();
    }

    public void cancelAnimation() {
        this.lottieDrawable.cancelAnimation();
        enableOrDisableHardwareLayer();
    }

    public void pauseAnimation() {
        float progress = getProgress();
        this.lottieDrawable.cancelAnimation();
        setProgress(progress);
        enableOrDisableHardwareLayer();
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        this.lottieDrawable.setProgress(progress);
    }

    @FloatRange(from = 0.0d, to = 1.0d)
    public float getProgress() {
        return this.lottieDrawable.getProgress();
    }

    public long getDuration() {
        return this.composition != null ? this.composition.getDuration() : 0;
    }

    public void setPerformanceTrackingEnabled(boolean enabled) {
        this.lottieDrawable.setPerformanceTrackingEnabled(enabled);
    }

    @Nullable
    public PerformanceTracker getPerformanceTracker() {
        return this.lottieDrawable.getPerformanceTracker();
    }

    private void enableOrDisableHardwareLayer() {
        int i = 1;
        boolean useHardwareLayer = this.useHardwareLayer && this.lottieDrawable.isAnimating();
        if (useHardwareLayer) {
            i = 2;
        }
        setLayerType(i, null);
    }
}
