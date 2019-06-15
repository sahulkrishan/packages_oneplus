package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.utils.GammaEvaluator;
import java.util.List;

public class ColorKeyframeAnimation extends KeyframeAnimation<Integer> {
    public ColorKeyframeAnimation(List<Keyframe<Integer>> keyframes) {
        super(keyframes);
    }

    public Integer getValue(Keyframe<Integer> keyframe, float keyframeProgress) {
        if (keyframe.startValue != null && keyframe.endValue != null) {
            return Integer.valueOf(GammaEvaluator.evaluate(keyframeProgress, ((Integer) keyframe.startValue).intValue(), ((Integer) keyframe.endValue).intValue()));
        }
        throw new IllegalStateException("Missing values for keyframe.");
    }
}
