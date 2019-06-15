package com.airbnb.lottie.model;

import org.json.JSONObject;

public class Font {
    private final float ascent;
    private final String family;
    private final String name;
    private final String style;

    public static class Factory {
        public static Font newInstance(JSONObject json) {
            return new Font(json.optString("fFamily"), json.optString("fName"), json.optString("fStyle"), (float) json.optDouble("ascent"));
        }
    }

    Font(String family, String name, String style, float ascent) {
        this.family = family;
        this.name = name;
        this.style = style;
        this.ascent = ascent;
    }

    public String getFamily() {
        return this.family;
    }

    public String getName() {
        return this.name;
    }

    public String getStyle() {
        return this.style;
    }

    /* Access modifiers changed, original: 0000 */
    public float getAscent() {
        return this.ascent;
    }
}
