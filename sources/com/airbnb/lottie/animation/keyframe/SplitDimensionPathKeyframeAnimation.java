package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import java.util.Collections;

public class SplitDimensionPathKeyframeAnimation extends BaseKeyframeAnimation<PointF, PointF> {
    private final PointF point = new PointF();
    private final BaseKeyframeAnimation<Float, Float> xAnimation;
    private final BaseKeyframeAnimation<Float, Float> yAnimation;

    public SplitDimensionPathKeyframeAnimation(BaseKeyframeAnimation<Float, Float> xAnimation, BaseKeyframeAnimation<Float, Float> yAnimation) {
        super(Collections.emptyList());
        this.xAnimation = xAnimation;
        this.yAnimation = yAnimation;
    }

    public void setProgress(float progress) {
        this.xAnimation.setProgress(progress);
        this.yAnimation.setProgress(progress);
        this.point.set(((Float) this.xAnimation.getValue()).floatValue(), ((Float) this.yAnimation.getValue()).floatValue());
        for (int i = 0; i < this.listeners.size(); i++) {
            ((AnimationListener) this.listeners.get(i)).onValueChanged();
        }
    }

    public PointF getValue() {
        return getValue(null, 0.0f);
    }

    /* Access modifiers changed, original: 0000 */
    public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
        return this.point;
    }
}
