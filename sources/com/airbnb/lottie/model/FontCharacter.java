package com.airbnb.lottie.model;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.ShapeGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class FontCharacter {
    private final char character;
    private final String fontFamily;
    private final List<ShapeGroup> shapes;
    private final int size;
    private final String style;
    private final double width;

    public static class Factory {
        public static FontCharacter newInstance(JSONObject json, LottieComposition composition) {
            JSONObject jSONObject = json;
            int i = 0;
            char character = jSONObject.optString("ch").charAt(0);
            int size = jSONObject.optInt("size");
            double width = jSONObject.optDouble("w");
            String style = jSONObject.optString("style");
            String fontFamily = jSONObject.optString("fFamily");
            JSONObject data = jSONObject.optJSONObject("data");
            List<ShapeGroup> shapes = Collections.emptyList();
            if (data != null) {
                JSONArray shapesJson = data.optJSONArray("shapes");
                if (shapesJson != null) {
                    shapes = new ArrayList(shapesJson.length());
                    while (i < shapesJson.length()) {
                        shapes.add((ShapeGroup) ShapeGroup.shapeItemWithJson(shapesJson.optJSONObject(i), composition));
                        i++;
                    }
                }
            }
            LottieComposition lottieComposition = composition;
            return new FontCharacter(shapes, character, size, width, style, fontFamily);
        }
    }

    public static int hashFor(char character, String fontFamily, String style) {
        return (31 * ((31 * ((31 * 0) + character)) + fontFamily.hashCode())) + style.hashCode();
    }

    FontCharacter(List<ShapeGroup> shapes, char character, int size, double width, String style, String fontFamily) {
        this.shapes = shapes;
        this.character = character;
        this.size = size;
        this.width = width;
        this.style = style;
        this.fontFamily = fontFamily;
    }

    public List<ShapeGroup> getShapes() {
        return this.shapes;
    }

    /* Access modifiers changed, original: 0000 */
    public int getSize() {
        return this.size;
    }

    public double getWidth() {
        return this.width;
    }

    /* Access modifiers changed, original: 0000 */
    public String getStyle() {
        return this.style;
    }

    public int hashCode() {
        return hashFor(this.character, this.fontFamily, this.style);
    }
}
