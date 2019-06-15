package com.airbnb.lottie.model.animatable;

import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.PathKeyframe;
import com.airbnb.lottie.animation.keyframe.PathKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.StaticKeyframeAnimation;
import com.airbnb.lottie.model.animatable.AnimatableValue.Factory;
import com.airbnb.lottie.utils.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnimatablePathValue implements AnimatableValue<PointF, PointF> {
    private PointF initialPoint;
    private final List<PathKeyframe> keyframes;

    private static class ValueFactory implements Factory<PointF> {
        private static final Factory<PointF> INSTANCE = new ValueFactory();

        private ValueFactory() {
        }

        public PointF valueFromObject(Object object, float scale) {
            return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
        }
    }

    public static AnimatableValue<PointF, PointF> createAnimatablePathOrSplitDimensionPath(JSONObject json, LottieComposition composition) {
        if (json.has("k")) {
            return new AnimatablePathValue(json.opt("k"), composition);
        }
        return new AnimatableSplitDimensionPathValue(AnimatableFloatValue.Factory.newInstance(json.optJSONObject("x"), composition), AnimatableFloatValue.Factory.newInstance(json.optJSONObject("y"), composition));
    }

    AnimatablePathValue() {
        this.keyframes = new ArrayList();
        this.initialPoint = new PointF(0.0f, 0.0f);
    }

    AnimatablePathValue(Object json, LottieComposition composition) {
        this.keyframes = new ArrayList();
        if (hasKeyframes(json)) {
            JSONArray jsonArray = (JSONArray) json;
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                this.keyframes.add(PathKeyframe.Factory.newInstance(jsonArray.optJSONObject(i), composition, ValueFactory.INSTANCE));
            }
            Keyframe.setEndFrames(this.keyframes);
            return;
        }
        this.initialPoint = JsonUtils.pointFromJsonArray((JSONArray) json, composition.getDpScale());
    }

    private boolean hasKeyframes(Object json) {
        boolean z = false;
        if (!(json instanceof JSONArray)) {
            return false;
        }
        Object firstObject = ((JSONArray) json).opt(0);
        if ((firstObject instanceof JSONObject) && ((JSONObject) firstObject).has("t")) {
            z = true;
        }
        return z;
    }

    public BaseKeyframeAnimation<PointF, PointF> createAnimation() {
        if (hasAnimation()) {
            return new PathKeyframeAnimation(this.keyframes);
        }
        return new StaticKeyframeAnimation(this.initialPoint);
    }

    public boolean hasAnimation() {
        return this.keyframes.isEmpty() ^ 1;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("initialPoint=");
        stringBuilder.append(this.initialPoint);
        return stringBuilder.toString();
    }
}
