package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;
import java.util.List;
import org.json.JSONObject;

public class AnimatableIntegerValue extends BaseAnimatableValue<Integer, Integer> {

    public static final class Factory {
        private Factory() {
        }

        static AnimatableIntegerValue newInstance() {
            return new AnimatableIntegerValue();
        }

        public static AnimatableIntegerValue newInstance(JSONObject json, LottieComposition composition) {
            if (json != null && json.has("x")) {
                composition.addWarning("Lottie doesn't support expressions.");
            }
            Result<Integer> result = AnimatableValueParser.newInstance(json, 1.0f, composition, ValueFactory.INSTANCE).parseJson();
            return new AnimatableIntegerValue(result.keyframes, result.initialValue);
        }
    }

    private static class ValueFactory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<Integer> {
        private static final ValueFactory INSTANCE = new ValueFactory();

        private ValueFactory() {
        }

        public Integer valueFromObject(Object object, float scale) {
            return Integer.valueOf(Math.round(JsonUtils.valueFromObject(object) * scale));
        }
    }

    private AnimatableIntegerValue() {
        super(Integer.valueOf(100));
    }

    AnimatableIntegerValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
        if (hasAnimation()) {
            return new IntegerKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }

    public Integer getInitialValue() {
        return (Integer) this.initialValue;
    }
}
