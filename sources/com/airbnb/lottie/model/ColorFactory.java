package com.airbnb.lottie.model;

import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import com.airbnb.lottie.model.animatable.AnimatableValue.Factory;
import org.json.JSONArray;

public class ColorFactory implements Factory<Integer> {
    public static final ColorFactory INSTANCE = new ColorFactory();

    public Integer valueFromObject(Object object, float scale) {
        JSONArray colorArray = (JSONArray) object;
        if (colorArray.length() != 4) {
            return Integer.valueOf(ViewCompat.MEASURED_STATE_MASK);
        }
        boolean shouldUse255 = true;
        for (int i = 0; i < colorArray.length(); i++) {
            if (colorArray.optDouble(i) > 1.0d) {
                shouldUse255 = false;
            }
        }
        float multiplier = shouldUse255 ? 255.0f : 1.0f;
        return Integer.valueOf(Color.argb((int) (colorArray.optDouble(3) * ((double) multiplier)), (int) (colorArray.optDouble(0) * ((double) multiplier)), (int) (colorArray.optDouble(1) * ((double) multiplier)), (int) (colorArray.optDouble(2) * ((double) multiplier))));
    }
}
