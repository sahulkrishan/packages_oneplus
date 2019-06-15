package com.airbnb.lottie.model.layer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.util.Log;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.DrawingContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.Mask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseLayer implements DrawingContent, AnimationListener {
    private static final int SAVE_FLAGS = 19;
    private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList();
    final Matrix boundsMatrix = new Matrix();
    private final Paint clearPaint = new Paint();
    private final Paint contentPaint = new Paint(1);
    private final String drawTraceName;
    final Layer layerModel;
    final LottieDrawable lottieDrawable;
    @Nullable
    private MaskKeyframeAnimation mask;
    private final RectF maskBoundsRect = new RectF();
    private final Paint maskPaint = new Paint(1);
    private final Matrix matrix = new Matrix();
    private final RectF matteBoundsRect = new RectF();
    @Nullable
    private BaseLayer matteLayer;
    private final Paint mattePaint = new Paint(1);
    @Nullable
    private BaseLayer parentLayer;
    private List<BaseLayer> parentLayers;
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final RectF tempMaskBoundsRect = new RectF();
    final TransformKeyframeAnimation transform;
    private boolean visible = true;

    public abstract void drawLayer(Canvas canvas, Matrix matrix, int i);

    @Nullable
    static BaseLayer forModel(Layer layerModel, LottieDrawable drawable, LottieComposition composition) {
        switch (layerModel.getLayerType()) {
            case Shape:
                return new ShapeLayer(drawable, layerModel);
            case PreComp:
                return new CompositionLayer(drawable, layerModel, composition.getPrecomps(layerModel.getRefId()), composition);
            case Solid:
                return new SolidLayer(drawable, layerModel);
            case Image:
                return new ImageLayer(drawable, layerModel, composition.getDpScale());
            case Null:
                return new NullLayer(drawable, layerModel);
            case Text:
                return new TextLayer(drawable, layerModel);
            default:
                String str = L.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown layer type ");
                stringBuilder.append(layerModel.getLayerType());
                Log.w(str, stringBuilder.toString());
                return null;
        }
    }

    BaseLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        this.lottieDrawable = lottieDrawable;
        this.layerModel = layerModel;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(layerModel.getName());
        stringBuilder.append("#draw");
        this.drawTraceName = stringBuilder.toString();
        this.clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        this.maskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        if (layerModel.getMatteType() == MatteType.Invert) {
            this.mattePaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        } else {
            this.mattePaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        }
        this.transform = layerModel.getTransform().createAnimation();
        this.transform.addListener(this);
        this.transform.addAnimationsToLayer(this);
        if (!(layerModel.getMasks() == null || layerModel.getMasks().isEmpty())) {
            this.mask = new MaskKeyframeAnimation(layerModel.getMasks());
            for (BaseKeyframeAnimation<?, Path> animation : this.mask.getMaskAnimations()) {
                addAnimation(animation);
                animation.addUpdateListener(this);
            }
            for (BaseKeyframeAnimation<Integer, Integer> animation2 : this.mask.getOpacityAnimations()) {
                addAnimation(animation2);
                animation2.addUpdateListener(this);
            }
        }
        setupInOutAnimations();
    }

    public void onValueChanged() {
        invalidateSelf();
    }

    /* Access modifiers changed, original: 0000 */
    public Layer getLayerModel() {
        return this.layerModel;
    }

    /* Access modifiers changed, original: 0000 */
    public void setMatteLayer(@Nullable BaseLayer matteLayer) {
        this.matteLayer = matteLayer;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasMatteOnThisLayer() {
        return this.matteLayer != null;
    }

    /* Access modifiers changed, original: 0000 */
    public void setParentLayer(@Nullable BaseLayer parentLayer) {
        this.parentLayer = parentLayer;
    }

    private void setupInOutAnimations() {
        boolean z = true;
        if (this.layerModel.getInOutKeyframes().isEmpty()) {
            setVisible(true);
            return;
        }
        final FloatKeyframeAnimation inOutAnimation = new FloatKeyframeAnimation(this.layerModel.getInOutKeyframes());
        inOutAnimation.setIsDiscrete();
        inOutAnimation.addUpdateListener(new AnimationListener() {
            public void onValueChanged() {
                BaseLayer.this.setVisible(((Float) inOutAnimation.getValue()).floatValue() == 1.0f);
            }
        });
        if (((Float) inOutAnimation.getValue()).floatValue() != 1.0f) {
            z = false;
        }
        setVisible(z);
        addAnimation(inOutAnimation);
    }

    private void invalidateSelf() {
        this.lottieDrawable.invalidateSelf();
    }

    public void addAnimation(BaseKeyframeAnimation<?, ?> newAnimation) {
        if (!(newAnimation instanceof StaticKeyframeAnimation)) {
            this.animations.add(newAnimation);
        }
    }

    @CallSuper
    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        this.boundsMatrix.set(parentMatrix);
        this.boundsMatrix.preConcat(this.transform.getMatrix());
    }

    @SuppressLint({"WrongConstant"})
    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        L.beginSection(this.drawTraceName);
        if (this.visible) {
            int i;
            buildParentLayerListIfNeeded();
            L.beginSection("Layer#parentMatrix");
            this.matrix.reset();
            this.matrix.set(parentMatrix);
            for (i = this.parentLayers.size() - 1; i >= 0; i--) {
                this.matrix.preConcat(((BaseLayer) this.parentLayers.get(i)).transform.getMatrix());
            }
            L.endSection("Layer#parentMatrix");
            i = (int) ((((((float) parentAlpha) / 255.0f) * ((float) ((Integer) this.transform.getOpacity().getValue()).intValue())) / 100.0f) * 1132396544);
            if (hasMatteOnThisLayer() || hasMasksOnThisLayer()) {
                L.beginSection("Layer#computeBounds");
                this.rect.set(0.0f, 0.0f, 0.0f, 0.0f);
                getBounds(this.rect, this.matrix);
                intersectBoundsWithMatte(this.rect, this.matrix);
                this.matrix.preConcat(this.transform.getMatrix());
                intersectBoundsWithMask(this.rect, this.matrix);
                this.rect.set(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight());
                L.endSection("Layer#computeBounds");
                L.beginSection("Layer#saveLayer");
                canvas.saveLayer(this.rect, this.contentPaint, 31);
                L.endSection("Layer#saveLayer");
                clearCanvas(canvas);
                L.beginSection("Layer#drawLayer");
                drawLayer(canvas, this.matrix, i);
                L.endSection("Layer#drawLayer");
                if (hasMasksOnThisLayer()) {
                    applyMasks(canvas, this.matrix);
                }
                if (hasMatteOnThisLayer()) {
                    L.beginSection("Layer#drawMatte");
                    L.beginSection("Layer#saveLayer");
                    canvas.saveLayer(this.rect, this.mattePaint, 19);
                    L.endSection("Layer#saveLayer");
                    clearCanvas(canvas);
                    this.matteLayer.draw(canvas, parentMatrix, i);
                    L.beginSection("Layer#restoreLayer");
                    canvas.restore();
                    L.endSection("Layer#restoreLayer");
                    L.endSection("Layer#drawMatte");
                }
                L.beginSection("Layer#restoreLayer");
                canvas.restore();
                L.endSection("Layer#restoreLayer");
                recordRenderTime(L.endSection(this.drawTraceName));
                return;
            }
            this.matrix.preConcat(this.transform.getMatrix());
            L.beginSection("Layer#drawLayer");
            drawLayer(canvas, this.matrix, i);
            L.endSection("Layer#drawLayer");
            recordRenderTime(L.endSection(this.drawTraceName));
            return;
        }
        L.endSection(this.drawTraceName);
    }

    private void recordRenderTime(float ms) {
        this.lottieDrawable.getComposition().getPerformanceTracker().recordRenderTime(this.layerModel.getName(), ms);
    }

    private void clearCanvas(Canvas canvas) {
        L.beginSection("Layer#clearLayer");
        canvas.drawRect(this.rect.left - 1.0f, this.rect.top - 1.0f, this.rect.right + 1.0f, this.rect.bottom + 1.0f, this.clearPaint);
        L.endSection("Layer#clearLayer");
    }

    private void intersectBoundsWithMask(RectF rect, Matrix matrix) {
        this.maskBoundsRect.set(0.0f, 0.0f, 0.0f, 0.0f);
        if (hasMasksOnThisLayer()) {
            int size = this.mask.getMasks().size();
            int i = 0;
            while (i < size) {
                Mask mask = (Mask) this.mask.getMasks().get(i);
                this.path.set((Path) ((BaseKeyframeAnimation) this.mask.getMaskAnimations().get(i)).getValue());
                this.path.transform(matrix);
                switch (mask.getMaskMode()) {
                    case MaskModeSubtract:
                        return;
                    case MaskModeIntersect:
                        return;
                    case MaskModeUnknown:
                        return;
                    default:
                        this.path.computeBounds(this.tempMaskBoundsRect, false);
                        if (i == 0) {
                            this.maskBoundsRect.set(this.tempMaskBoundsRect);
                        } else {
                            this.maskBoundsRect.set(Math.min(this.maskBoundsRect.left, this.tempMaskBoundsRect.left), Math.min(this.maskBoundsRect.top, this.tempMaskBoundsRect.top), Math.max(this.maskBoundsRect.right, this.tempMaskBoundsRect.right), Math.max(this.maskBoundsRect.bottom, this.tempMaskBoundsRect.bottom));
                        }
                        i++;
                }
            }
            rect.set(Math.max(rect.left, this.maskBoundsRect.left), Math.max(rect.top, this.maskBoundsRect.top), Math.min(rect.right, this.maskBoundsRect.right), Math.min(rect.bottom, this.maskBoundsRect.bottom));
        }
    }

    private void intersectBoundsWithMatte(RectF rect, Matrix matrix) {
        if (hasMatteOnThisLayer() && this.layerModel.getMatteType() != MatteType.Invert) {
            this.matteLayer.getBounds(this.matteBoundsRect, matrix);
            rect.set(Math.max(rect.left, this.matteBoundsRect.left), Math.max(rect.top, this.matteBoundsRect.top), Math.min(rect.right, this.matteBoundsRect.right), Math.min(rect.bottom, this.matteBoundsRect.bottom));
        }
    }

    @SuppressLint({"WrongConstant"})
    private void applyMasks(Canvas canvas, Matrix matrix) {
        L.beginSection("Layer#drawMask");
        L.beginSection("Layer#saveLayer");
        canvas.saveLayer(this.rect, this.maskPaint, 19);
        L.endSection("Layer#saveLayer");
        clearCanvas(canvas);
        int size = this.mask.getMasks().size();
        for (int i = 0; i < size; i++) {
            Mask mask = (Mask) this.mask.getMasks().get(i);
            this.path.set((Path) ((BaseKeyframeAnimation) this.mask.getMaskAnimations().get(i)).getValue());
            this.path.transform(matrix);
            if (AnonymousClass2.$SwitchMap$com$airbnb$lottie$model$content$Mask$MaskMode[mask.getMaskMode().ordinal()] != 1) {
                this.path.setFillType(FillType.WINDING);
            } else {
                this.path.setFillType(FillType.INVERSE_WINDING);
            }
            BaseKeyframeAnimation<Integer, Integer> opacityAnimation = (BaseKeyframeAnimation) this.mask.getOpacityAnimations().get(i);
            int alpha = this.contentPaint.getAlpha();
            this.contentPaint.setAlpha((int) (((float) ((Integer) opacityAnimation.getValue()).intValue()) * 2.55f));
            canvas.drawPath(this.path, this.contentPaint);
            this.contentPaint.setAlpha(alpha);
        }
        L.beginSection("Layer#restoreLayer");
        canvas.restore();
        L.endSection("Layer#restoreLayer");
        L.endSection("Layer#drawMask");
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasMasksOnThisLayer() {
        return (this.mask == null || this.mask.getMaskAnimations().isEmpty()) ? false : true;
    }

    private void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            invalidateSelf();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        if (this.layerModel.getTimeStretch() != 0.0f) {
            progress /= this.layerModel.getTimeStretch();
        }
        if (this.matteLayer != null) {
            this.matteLayer.setProgress(progress);
        }
        for (int i = 0; i < this.animations.size(); i++) {
            ((BaseKeyframeAnimation) this.animations.get(i)).setProgress(progress);
        }
    }

    private void buildParentLayerListIfNeeded() {
        if (this.parentLayers == null) {
            if (this.parentLayer == null) {
                this.parentLayers = Collections.emptyList();
                return;
            }
            this.parentLayers = new ArrayList();
            for (BaseLayer layer = this.parentLayer; layer != null; layer = layer.parentLayer) {
                this.parentLayers.add(layer);
            }
        }
    }

    public String getName() {
        return this.layerModel.getName();
    }

    public void setContents(List<Content> list, List<Content> list2) {
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
    }
}
