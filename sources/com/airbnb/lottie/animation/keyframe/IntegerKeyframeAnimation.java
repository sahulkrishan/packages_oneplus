package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.List;

public class IntegerKeyframeAnimation extends KeyframeAnimation<Integer> {
    public IntegerKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
        super(keyframes);
    }

    /* Access modifiers changed, original: 0000 */
    public Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
        if (keyframe.startValue != null && keyframe.endValue != null) {
            return Integer.valueOf(MiscUtils.lerp(((Integer) keyframe.startValue).intValue(), ((Integer) keyframe.endValue).intValue(), keyframeProgress));
        }
        throw new IllegalStateException("Missing values for keyframe.");
    }
}
