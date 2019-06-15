package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ShapeContent;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONObject;

public class ShapePath implements ContentModel {
    private final int index;
    private final String name;
    private final AnimatableShapeValue shapePath;

    static class Factory {
        private Factory() {
        }

        static ShapePath newInstance(JSONObject json, LottieComposition composition) {
            return new ShapePath(json.optString("nm"), json.optInt("ind"), com.airbnb.lottie.model.animatable.AnimatableShapeValue.Factory.newInstance(json.optJSONObject("ks"), composition));
        }
    }

    private ShapePath(String name, int index, AnimatableShapeValue shapePath) {
        this.name = name;
        this.index = index;
        this.shapePath = shapePath;
    }

    public String getName() {
        return this.name;
    }

    public AnimatableShapeValue getShapePath() {
        return this.shapePath;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new ShapeContent(drawable, layer, this);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ShapePath{name=");
        stringBuilder.append(this.name);
        stringBuilder.append(", index=");
        stringBuilder.append(this.index);
        stringBuilder.append(", hasAnimation=");
        stringBuilder.append(this.shapePath.hasAnimation());
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
