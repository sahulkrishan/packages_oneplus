package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.content.GradientColor;
import java.util.List;

public class GradientColorKeyframeAnimation extends KeyframeAnimation<GradientColor> {
    private final GradientColor gradientColor;

    public GradientColorKeyframeAnimation(List<? extends Keyframe<GradientColor>> keyframes) {
        super(keyframes);
        int size = 0;
        GradientColor startValue = ((Keyframe) keyframes.get(0)).startValue;
        if (startValue != null) {
            size = startValue.getSize();
        }
        this.gradientColor = new GradientColor(new float[size], new int[size]);
    }

    /* Access modifiers changed, original: 0000 */
    public GradientColor getValue(Keyframe<GradientColor> keyframe, float keyframeProgress) {
        this.gradientColor.lerp((GradientColor) keyframe.startValue, (GradientColor) keyframe.endValue, keyframeProgress);
        return this.gradientColor;
    }
}
