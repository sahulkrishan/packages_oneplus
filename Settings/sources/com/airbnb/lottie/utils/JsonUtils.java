package com.airbnb.lottie.utils;

import android.graphics.PointF;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {
    private JsonUtils() {
    }

    public static PointF pointFromJsonObject(JSONObject values, float scale) {
        return new PointF(valueFromObject(values.opt("x")) * scale, valueFromObject(values.opt("y")) * scale);
    }

    public static PointF pointFromJsonArray(JSONArray values, float scale) {
        if (values.length() >= 2) {
            return new PointF(((float) values.optDouble(0, 1.0d)) * scale, ((float) values.optDouble(1, 1.0d)) * scale);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unable to parse point for ");
        stringBuilder.append(values);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static float valueFromObject(Object object) {
        if (object instanceof Float) {
            return ((Float) object).floatValue();
        }
        if (object instanceof Integer) {
            return (float) ((Integer) object).intValue();
        }
        if (object instanceof Double) {
            return (float) ((Double) object).doubleValue();
        }
        if (object instanceof JSONArray) {
            return (float) ((JSONArray) object).optDouble(0);
        }
        return 0.0f;
    }
}
