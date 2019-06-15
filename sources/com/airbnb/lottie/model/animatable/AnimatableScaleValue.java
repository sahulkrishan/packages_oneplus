package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ScaleKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.ScaleXY;
import java.util.List;
import org.json.JSONObject;

public class AnimatableScaleValue extends BaseAnimatableValue<ScaleXY, ScaleXY> {

    static final class Factory {
        private Factory() {
        }

        static AnimatableScaleValue newInstance(JSONObject json, LottieComposition composition) {
            Result<ScaleXY> result = AnimatableValueParser.newInstance(json, 1.0f, composition, com.airbnb.lottie.model.ScaleXY.Factory.INSTANCE).parseJson();
            return new AnimatableScaleValue(result.keyframes, (ScaleXY) result.initialValue);
        }

        static AnimatableScaleValue newInstance() {
            return new AnimatableScaleValue();
        }
    }

    private AnimatableScaleValue() {
        super(new ScaleXY());
    }

    AnimatableScaleValue(List<Keyframe<ScaleXY>> keyframes, ScaleXY initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<ScaleXY, ScaleXY> createAnimation() {
        if (hasAnimation()) {
            return new ScaleKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }
}
