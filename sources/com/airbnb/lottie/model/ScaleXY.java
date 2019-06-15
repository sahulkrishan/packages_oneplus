package com.airbnb.lottie.model;

import org.json.JSONArray;

public class ScaleXY {
    private final float scaleX;
    private final float scaleY;

    public static class Factory implements com.airbnb.lottie.model.animatable.AnimatableValue.Factory<ScaleXY> {
        public static final Factory INSTANCE = new Factory();

        private Factory() {
        }

        public ScaleXY valueFromObject(Object object, float scale) {
            JSONArray array = (JSONArray) object;
            return new ScaleXY((((float) array.optDouble(0, 1.0d)) / 100.0f) * scale, (((float) array.optDouble(1, 1.0d)) / 100.0f) * scale);
        }
    }

    public ScaleXY(float sx, float sy) {
        this.scaleX = sx;
        this.scaleY = sy;
    }

    public ScaleXY() {
        this(1.0f, 1.0f);
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getScaleX());
        stringBuilder.append("x");
        stringBuilder.append(getScaleY());
        return stringBuilder.toString();
    }
}
