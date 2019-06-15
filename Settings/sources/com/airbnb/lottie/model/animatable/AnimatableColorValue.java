package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ColorKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.ColorFactory;
import java.util.List;
import org.json.JSONObject;

public class AnimatableColorValue extends BaseAnimatableValue<Integer, Integer> {

    public static final class Factory {
        private Factory() {
        }

        public static AnimatableColorValue newInstance(JSONObject json, LottieComposition composition) {
            Result<Integer> result = AnimatableValueParser.newInstance(json, 1.0f, composition, ColorFactory.INSTANCE).parseJson();
            return new AnimatableColorValue(result.keyframes, (Integer) result.initialValue);
        }
    }

    private AnimatableColorValue(List<Keyframe<Integer>> keyframes, Integer initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<Integer, Integer> createAnimation() {
        if (hasAnimation()) {
            return new ColorKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("AnimatableColorValue{initialValue=");
        stringBuilder.append(this.initialValue);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
