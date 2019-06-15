package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.view.animation.Interpolator;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.utils.JsonUtils;
import com.airbnb.lottie.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

public class PathKeyframe extends Keyframe<PointF> {
    @Nullable
    private Path path;

    public static class Factory {
        private Factory() {
        }

        public static PathKeyframe newInstance(JSONObject json, LottieComposition composition, com.airbnb.lottie.model.animatable.AnimatableValue.Factory<PointF> valueFactory) {
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            Keyframe<PointF> keyframe = com.airbnb.lottie.animation.Keyframe.Factory.newInstance(jSONObject, lottieComposition, composition.getDpScale(), valueFactory);
            PointF cp1 = null;
            PointF cp2 = null;
            JSONArray tiJson = jSONObject.optJSONArray("ti");
            JSONArray toJson = jSONObject.optJSONArray("to");
            if (!(tiJson == null || toJson == null)) {
                cp1 = JsonUtils.pointFromJsonArray(toJson, composition.getDpScale());
                cp2 = JsonUtils.pointFromJsonArray(tiJson, composition.getDpScale());
            }
            PointF cp12 = cp1;
            PointF cp22 = cp2;
            PathKeyframe pathKeyframe = new PathKeyframe(lottieComposition, (PointF) keyframe.startValue, (PointF) keyframe.endValue, keyframe.interpolator, keyframe.startFrame, keyframe.endFrame);
            boolean equals = (keyframe.endValue == null || keyframe.startValue == null || !((PointF) keyframe.startValue).equals(((PointF) keyframe.endValue).x, ((PointF) keyframe.endValue).y)) ? false : true;
            if (!(pathKeyframe.endValue == null || equals)) {
                pathKeyframe.path = Utils.createPath((PointF) keyframe.startValue, (PointF) keyframe.endValue, cp12, cp22);
            }
            return pathKeyframe;
        }
    }

    private PathKeyframe(LottieComposition composition, @Nullable PointF startValue, @Nullable PointF endValue, @Nullable Interpolator interpolator, float startFrame, @Nullable Float endFrame) {
        super(composition, startValue, endValue, interpolator, startFrame, endFrame);
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public Path getPath() {
        return this.path;
    }
}
