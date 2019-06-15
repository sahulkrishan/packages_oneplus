package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PointKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.PointFFactory;
import java.util.List;
import org.json.JSONObject;

public class AnimatablePointValue extends BaseAnimatableValue<PointF, PointF> {

    public static final class Factory {
        private Factory() {
        }

        public static AnimatablePointValue newInstance(JSONObject json, LottieComposition composition) {
            Result<PointF> result = AnimatableValueParser.newInstance(json, composition.getDpScale(), composition, PointFFactory.INSTANCE).parseJson();
            return new AnimatablePointValue(result.keyframes, (PointF) result.initialValue);
        }
    }

    private AnimatablePointValue(List<Keyframe<PointF>> keyframes, PointF initialValue) {
        super(keyframes, initialValue);
    }

    public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
        if (hasAnimation()) {
            return new PointKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialValue);
    }
}
