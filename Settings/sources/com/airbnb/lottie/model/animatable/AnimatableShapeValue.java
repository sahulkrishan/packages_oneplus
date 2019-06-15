package com.airbnb.lottie.model.animatable;

import android.graphics.Path;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ShapeKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.List;
import org.json.JSONObject;

public class AnimatableShapeValue extends BaseAnimatableValue<ShapeData, Path> {
    private final Path convertTypePath;

    public static final class Factory {
        private Factory() {
        }

        public static AnimatableShapeValue newInstance(JSONObject json, LottieComposition composition) {
            Result<ShapeData> result = AnimatableValueParser.newInstance(json, composition.getDpScale(), composition, com.airbnb.lottie.model.content.ShapeData.Factory.INSTANCE).parseJson();
            return new AnimatableShapeValue(result.keyframes, (ShapeData) result.initialValue);
        }
    }

    private AnimatableShapeValue(List<Keyframe<ShapeData>> keyframes, ShapeData initialValue) {
        super(keyframes, initialValue);
        this.convertTypePath = new Path();
    }

    public BaseKeyframeAnimation<ShapeData, Path> createAnimation() {
        if (hasAnimation()) {
            return new ShapeKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(convertType((ShapeData) this.initialValue));
    }

    /* Access modifiers changed, original: 0000 */
    public Path convertType(ShapeData shapeData) {
        this.convertTypePath.reset();
        MiscUtils.getPathFromData(shapeData, this.convertTypePath);
        return this.convertTypePath;
    }
}
