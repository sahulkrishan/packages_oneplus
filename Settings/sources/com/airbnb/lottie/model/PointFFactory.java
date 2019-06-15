package com.airbnb.lottie.model;

import android.graphics.PointF;
import com.airbnb.lottie.model.animatable.AnimatableValue.Factory;
import com.airbnb.lottie.utils.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class PointFFactory implements Factory<PointF> {
    public static final PointFFactory INSTANCE = new PointFFactory();

    private PointFFactory() {
    }

    public PointF valueFromObject(Object object, float scale) {
        if (object instanceof JSONArray) {
            return JsonUtils.pointFromJsonArray((JSONArray) object, scale);
        }
        if (object instanceof JSONObject) {
            return JsonUtils.pointFromJsonObject((JSONObject) object, scale);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unable to parse point from ");
        stringBuilder.append(object);
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
