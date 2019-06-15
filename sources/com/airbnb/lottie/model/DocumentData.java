package com.airbnb.lottie.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import org.json.JSONArray;
import org.json.JSONObject;

public class DocumentData {
    @ColorInt
    public int color;
    public String fontName;
    int justification;
    double lineHeight;
    public int size;
    @ColorInt
    public int strokeColor;
    public boolean strokeOverFill;
    public int strokeWidth;
    public String text;
    public int tracking;

    public static final class Factory {
        private Factory() {
        }

        public static DocumentData newInstance(JSONObject json) {
            int strokeColor;
            JSONObject jSONObject = json;
            String text = jSONObject.optString("t");
            String fontName = jSONObject.optString("f");
            int size = jSONObject.optInt("s");
            int justification = jSONObject.optInt("j");
            int tracking = jSONObject.optInt("tr");
            double lineHeight = jSONObject.optDouble("lh");
            JSONArray colorArray = jSONObject.optJSONArray("fc");
            int color = Color.argb(255, (int) (colorArray.optDouble(0) * 255.0d), (int) (colorArray.optDouble(1) * 255.0d), (int) (colorArray.optDouble(2) * 255.0d));
            JSONArray strokeArray = jSONObject.optJSONArray("sc");
            if (strokeArray != null) {
                strokeColor = Color.argb(255, (int) (strokeArray.optDouble(0) * 255.0d), (int) (strokeArray.optDouble(1) * 255.0d), (int) (strokeArray.optDouble(2) * 255.0d));
            } else {
                strokeColor = 0;
            }
            return new DocumentData(text, fontName, size, justification, tracking, lineHeight, color, strokeColor, jSONObject.optInt("sw"), jSONObject.optBoolean("of"));
        }
    }

    DocumentData(String text, String fontName, int size, int justification, int tracking, double lineHeight, @ColorInt int color, @ColorInt int strokeColor, int strokeWidth, boolean strokeOverFill) {
        this.text = text;
        this.fontName = fontName;
        this.size = size;
        this.justification = justification;
        this.tracking = tracking;
        this.lineHeight = lineHeight;
        this.color = color;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.strokeOverFill = strokeOverFill;
    }

    /* Access modifiers changed, original: 0000 */
    public void set(DocumentData documentData) {
        this.text = documentData.text;
        this.fontName = documentData.fontName;
        this.size = documentData.size;
        this.justification = documentData.justification;
        this.tracking = documentData.tracking;
        this.lineHeight = documentData.lineHeight;
        this.color = documentData.color;
    }

    public int hashCode() {
        int result = (31 * ((31 * ((31 * ((31 * this.text.hashCode()) + this.fontName.hashCode())) + this.size)) + this.justification)) + this.tracking;
        long temp = Double.doubleToLongBits(this.lineHeight);
        return (31 * ((31 * result) + ((int) ((temp >>> 32) ^ temp)))) + this.color;
    }
}
