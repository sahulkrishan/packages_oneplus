package com.airbnb.lottie.model.content;

import android.graphics.Path.FillType;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.FillContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONObject;

public class ShapeFill implements ContentModel {
    @Nullable
    private final AnimatableColorValue color;
    private final boolean fillEnabled;
    private final FillType fillType;
    private final String name;
    @Nullable
    private final AnimatableIntegerValue opacity;

    static class Factory {
        private Factory() {
        }

        static ShapeFill newInstance(JSONObject json, LottieComposition composition) {
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            AnimatableColorValue color = null;
            AnimatableIntegerValue opacity = null;
            String name = jSONObject.optString("nm");
            JSONObject jsonColor = jSONObject.optJSONObject("c");
            if (jsonColor != null) {
                color = com.airbnb.lottie.model.animatable.AnimatableColorValue.Factory.newInstance(jsonColor, lottieComposition);
            }
            JSONObject jsonOpacity = jSONObject.optJSONObject("o");
            if (jsonOpacity != null) {
                opacity = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(jsonOpacity, lottieComposition);
            }
            return new ShapeFill(name, jSONObject.optBoolean("fillEnabled"), jSONObject.optInt("r", 1) == 1 ? FillType.WINDING : FillType.EVEN_ODD, color, opacity);
        }
    }

    private ShapeFill(String name, boolean fillEnabled, FillType fillType, @Nullable AnimatableColorValue color, @Nullable AnimatableIntegerValue opacity) {
        this.name = name;
        this.fillEnabled = fillEnabled;
        this.fillType = fillType;
        this.color = color;
        this.opacity = opacity;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public AnimatableColorValue getColor() {
        return this.color;
    }

    @Nullable
    public AnimatableIntegerValue getOpacity() {
        return this.opacity;
    }

    public FillType getFillType() {
        return this.fillType;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new FillContent(drawable, layer, this);
    }

    public String toString() {
        String str;
        Object obj;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ShapeFill{color=");
        if (this.color == null) {
            str = "null";
        } else {
            str = Integer.toHexString(((Integer) this.color.getInitialValue()).intValue());
        }
        stringBuilder.append(str);
        stringBuilder.append(", fillEnabled=");
        stringBuilder.append(this.fillEnabled);
        stringBuilder.append(", opacity=");
        if (this.opacity == null) {
            obj = "null";
        } else {
            obj = this.opacity.getInitialValue();
        }
        stringBuilder.append(obj);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
