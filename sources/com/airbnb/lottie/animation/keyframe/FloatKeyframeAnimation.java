package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.List;

public class FloatKeyframeAnimation extends KeyframeAnimation<Float> {
    public FloatKeyframeAnimation(List<Keyframe<Float>> keyframes) {
        super(keyframes);
    }

    /* Access modifiers changed, original: 0000 */
    public Float getValue(Keyframe<Float> keyframe, float keyframeProgress) {
        if (keyframe.startValue != null && keyframe.endValue != null) {
            return Float.valueOf(MiscUtils.lerp(((Float) keyframe.startValue).floatValue(), ((Float) keyframe.endValue).floatValue(), keyframeProgress));
        }
        throw new IllegalStateException("Missing values for keyframe.");
    }
}
