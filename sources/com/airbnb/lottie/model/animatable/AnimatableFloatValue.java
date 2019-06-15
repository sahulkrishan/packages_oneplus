package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.utils.JsonUtils;
import java.util.List;
import org.json.JSONObject;

public class AnimatableFloatValue extends BaseAnimatableValue<Float, Float> {

    public static final class Factory {
        private Factory() {
        }

        static AnimatableFloatValue newInstance() {
            return new AnimatableFloatValue();
        }

        public static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition) {
            return newInstance(json, composition, true);
        }

        public static AnimatableFloatValue newInstance(JSONObject json, LottieComposition composition, boolean isDp) {
            float scale = isDp ? composition.getDpScale() : 1.0f;
            if (json != null && json.has("x")) {
                composition.addWarning("Lottie doesn't support expressions.");
            }
            Result<Float> result = AnimatableValueParser.newInstance(json, scale, composition, ValueFactory.INSTANCE).parseJson();
            return new AnimatableFloatValue(result.keyframes, (Float) result.initialValue);
        }
    }

    private static class ValueFactory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<Float> {
        static final ValueFactory INSTANCE = new ValueFactory();

        private ValueFactory() {
        }

        public Float valueFromObject(Object object, float scale) {
            return Float.valueOf(JsonUtils.valueFromObject(object) * scale);
        }
    }

    private AnimatableFloatValue() {
        super(Float.valueOf(0.0f));
    }

    private AnimatableFloatValue(List<Keyframe<Float>> keyframes, Float initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<Float, Float> createAnimation() {
        if (hasAnimation()) {
            return new FloatKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }

    public Float getInitialValue() {
        return (Float) this.initialValue;
    }
}
