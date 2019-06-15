package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import java.util.List;
import org.json.JSONObject;

public class AnimatableTextFrame extends BaseAnimatableValue<DocumentData, DocumentData> {

    public static final class Factory {
        private Factory() {
        }

        public static AnimatableTextFrame newInstance(JSONObject json, LottieComposition composition) {
            if (json != null && json.has("x")) {
                composition.addWarning("Lottie doesn't support expressions.");
            }
            Result<DocumentData> result = AnimatableValueParser.newInstance(json, 1.0f, composition, ValueFactory.INSTANCE).parseJson();
            return new AnimatableTextFrame(result.keyframes, (DocumentData) result.initialValue);
        }
    }

    private static class ValueFactory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<DocumentData> {
        private static final ValueFactory INSTANCE = new ValueFactory();

        private ValueFactory() {
        }

        public DocumentData valueFromObject(Object object, float scale) {
            return com.airbnb.lottie.model.DocumentData.Factory.newInstance((JSONObject) object);
        }
    }

    AnimatableTextFrame(List<Keyframe<DocumentData>> keyframes, DocumentData initialValue) {
        super(keyframes, initialValue);
    }

    public TextKeyframeAnimation createAnimation() {
        return new TextKeyframeAnimation(this.keyframes);
    }
}
