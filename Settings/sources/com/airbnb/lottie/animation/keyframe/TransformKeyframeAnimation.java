package com.airbnb.lottie.animation.keyframe;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.ScaleXY;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;

public class TransformKeyframeAnimation {
    private final BaseKeyframeAnimation<PointF, PointF> anchorPoint;
    @Nullable
    private final BaseKeyframeAnimation<?, Float> endOpacity;
    private final Matrix matrix = new Matrix();
    private final BaseKeyframeAnimation<Integer, Integer> opacity;
    private final BaseKeyframeAnimation<?, PointF> position;
    private final BaseKeyframeAnimation<Float, Float> rotation;
    private final BaseKeyframeAnimation<ScaleXY, ScaleXY> scale;
    @Nullable
    private final BaseKeyframeAnimation<?, Float> startOpacity;

    public TransformKeyframeAnimation(AnimatableTransform animatableTransform) {
        this.anchorPoint = animatableTransform.getAnchorPoint().createAnimation();
        this.position = animatableTransform.getPosition().createAnimation();
        this.scale = animatableTransform.getScale().createAnimation();
        this.rotation = animatableTransform.getRotation().createAnimation();
        this.opacity = animatableTransform.getOpacity().createAnimation();
        if (animatableTransform.getStartOpacity() != null) {
            this.startOpacity = animatableTransform.getStartOpacity().createAnimation();
        } else {
            this.startOpacity = null;
        }
        if (animatableTransform.getEndOpacity() != null) {
            this.endOpacity = animatableTransform.getEndOpacity().createAnimation();
        } else {
            this.endOpacity = null;
        }
    }

    public void addAnimationsToLayer(BaseLayer layer) {
        layer.addAnimation(this.anchorPoint);
        layer.addAnimation(this.position);
        layer.addAnimation(this.scale);
        layer.addAnimation(this.rotation);
        layer.addAnimation(this.opacity);
        if (this.startOpacity != null) {
            layer.addAnimation(this.startOpacity);
        }
        if (this.endOpacity != null) {
            layer.addAnimation(this.endOpacity);
        }
    }

    public void addListener(AnimationListener listener) {
        this.anchorPoint.addUpdateListener(listener);
        this.position.addUpdateListener(listener);
        this.scale.addUpdateListener(listener);
        this.rotation.addUpdateListener(listener);
        this.opacity.addUpdateListener(listener);
        if (this.startOpacity != null) {
            this.startOpacity.addUpdateListener(listener);
        }
        if (this.endOpacity != null) {
            this.endOpacity.addUpdateListener(listener);
        }
    }

    public BaseKeyframeAnimation<?, Integer> getOpacity() {
        return this.opacity;
    }

    @Nullable
    public BaseKeyframeAnimation<?, Float> getStartOpacity() {
        return this.startOpacity;
    }

    @Nullable
    public BaseKeyframeAnimation<?, Float> getEndOpacity() {
        return this.endOpacity;
    }

    public Matrix getMatrix() {
        this.matrix.reset();
        PointF position = (PointF) this.position.getValue();
        if (!(position.x == 0.0f && position.y == 0.0f)) {
            this.matrix.preTranslate(position.x, position.y);
        }
        float rotation = ((Float) this.rotation.getValue()).floatValue();
        if (rotation != 0.0f) {
            this.matrix.preRotate(rotation);
        }
        ScaleXY scaleTransform = (ScaleXY) this.scale.getValue();
        if (!(scaleTransform.getScaleX() == 1.0f && scaleTransform.getScaleY() == 1.0f)) {
            this.matrix.preScale(scaleTransform.getScaleX(), scaleTransform.getScaleY());
        }
        PointF anchorPoint = (PointF) this.anchorPoint.getValue();
        if (!(anchorPoint.x == 0.0f && anchorPoint.y == 0.0f)) {
            this.matrix.preTranslate(-anchorPoint.x, -anchorPoint.y);
        }
        return this.matrix;
    }

    public Matrix getMatrixForRepeater(float amount) {
        PointF position = (PointF) this.position.getValue();
        PointF anchorPoint = (PointF) this.anchorPoint.getValue();
        ScaleXY scale = (ScaleXY) this.scale.getValue();
        float rotation = ((Float) this.rotation.getValue()).floatValue();
        this.matrix.reset();
        this.matrix.preTranslate(position.x * amount, position.y * amount);
        this.matrix.preScale((float) Math.pow((double) scale.getScaleX(), (double) amount), (float) Math.pow((double) scale.getScaleY(), (double) amount));
        this.matrix.preRotate(rotation * amount, anchorPoint.x, anchorPoint.y);
        return this.matrix;
    }
}
