package com.airbnb.lottie.model.content;

import android.graphics.Path.FillType;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientFillContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONException;
import org.json.JSONObject;

public class GradientFill implements ContentModel {
    private final AnimatablePointValue endPoint;
    private final FillType fillType;
    private final AnimatableGradientColorValue gradientColor;
    private final GradientType gradientType;
    @Nullable
    private final AnimatableFloatValue highlightAngle;
    @Nullable
    private final AnimatableFloatValue highlightLength;
    private final String name;
    private final AnimatableIntegerValue opacity;
    private final AnimatablePointValue startPoint;

    static class Factory {
        private Factory() {
        }

        static GradientFill newInstance(JSONObject json, LottieComposition composition) {
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            String name = jSONObject.optString("nm");
            JSONObject jsonColor = jSONObject.optJSONObject("g");
            if (jsonColor != null && jsonColor.has("k")) {
                int points = jsonColor.optInt("p");
                JSONObject jsonColor2 = jsonColor.optJSONObject("k");
                try {
                    jsonColor2.put("p", points);
                } catch (JSONException e) {
                }
                jsonColor = jsonColor2;
            }
            AnimatableGradientColorValue color = null;
            if (jsonColor != null) {
                color = com.airbnb.lottie.model.animatable.AnimatableGradientColorValue.Factory.newInstance(jsonColor, lottieComposition);
            }
            AnimatableGradientColorValue color2 = color;
            JSONObject jsonOpacity = jSONObject.optJSONObject("o");
            AnimatableIntegerValue opacity = null;
            if (jsonOpacity != null) {
                opacity = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(jsonOpacity, lottieComposition);
            }
            AnimatableIntegerValue opacity2 = opacity;
            int fillTypeInt = jSONObject.optInt("r", 1);
            FillType fillType = fillTypeInt == 1 ? FillType.WINDING : FillType.EVEN_ODD;
            int gradientTypeInt = jSONObject.optInt("t", 1);
            GradientType gradientType = gradientTypeInt == 1 ? GradientType.Linear : GradientType.Radial;
            JSONObject jsonStartPoint = jSONObject.optJSONObject("s");
            AnimatablePointValue startPoint = null;
            if (jsonStartPoint != null) {
                startPoint = com.airbnb.lottie.model.animatable.AnimatablePointValue.Factory.newInstance(jsonStartPoint, lottieComposition);
            }
            AnimatablePointValue startPoint2 = startPoint;
            JSONObject jsonEndPoint = jSONObject.optJSONObject("e");
            startPoint = null;
            if (jsonEndPoint != null) {
                startPoint = com.airbnb.lottie.model.animatable.AnimatablePointValue.Factory.newInstance(jsonEndPoint, lottieComposition);
            }
            return new GradientFill(name, gradientType, fillType, color2, opacity2, startPoint2, startPoint, null, null);
        }
    }

    private GradientFill(String name, GradientType gradientType, FillType fillType, AnimatableGradientColorValue gradientColor, AnimatableIntegerValue opacity, AnimatablePointValue startPoint, AnimatablePointValue endPoint, AnimatableFloatValue highlightLength, AnimatableFloatValue highlightAngle) {
        this.gradientType = gradientType;
        this.fillType = fillType;
        this.gradientColor = gradientColor;
        this.opacity = opacity;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.name = name;
        this.highlightLength = highlightLength;
        this.highlightAngle = highlightAngle;
    }

    public String getName() {
        return this.name;
    }

    public GradientType getGradientType() {
        return this.gradientType;
    }

    public FillType getFillType() {
        return this.fillType;
    }

    public AnimatableGradientColorValue getGradientColor() {
        return this.gradientColor;
    }

    public AnimatableIntegerValue getOpacity() {
        return this.opacity;
    }

    public AnimatablePointValue getStartPoint() {
        return this.startPoint;
    }

    public AnimatablePointValue getEndPoint() {
        return this.endPoint;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public AnimatableFloatValue getHighlightLength() {
        return this.highlightLength;
    }

    /* Access modifiers changed, original: 0000 */
    @Nullable
    public AnimatableFloatValue getHighlightAngle() {
        return this.highlightAngle;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new GradientFillContent(drawable, layer, this);
    }
}
