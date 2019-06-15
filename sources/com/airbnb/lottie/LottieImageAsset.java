package com.airbnb.lottie;

import com.android.settingslib.datetime.ZoneGetter;
import org.json.JSONObject;

public class LottieImageAsset {
    private final String fileName;
    private final int height;
    private final String id;
    private final int width;

    static class Factory {
        private Factory() {
        }

        static LottieImageAsset newInstance(JSONObject imageJson) {
            return new LottieImageAsset(imageJson.optInt("w"), imageJson.optInt("h"), imageJson.optString(ZoneGetter.KEY_ID), imageJson.optString("p"));
        }
    }

    private LottieImageAsset(int width, int height, String id, String fileName) {
        this.width = width;
        this.height = height;
        this.id = id;
        this.fileName = fileName;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getId() {
        return this.id;
    }

    public String getFileName() {
        return this.fileName;
    }
}
