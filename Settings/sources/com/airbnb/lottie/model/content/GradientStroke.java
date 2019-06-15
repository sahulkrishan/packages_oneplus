package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientStrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.ShapeStroke.LineCapType;
import com.airbnb.lottie.model.content.ShapeStroke.LineJoinType;
import com.airbnb.lottie.model.layer.BaseLayer;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class GradientStroke implements ContentModel {
    private final LineCapType capType;
    @Nullable
    private final AnimatableFloatValue dashOffset;
    private final AnimatablePointValue endPoint;
    private final AnimatableGradientColorValue gradientColor;
    private final GradientType gradientType;
    private final LineJoinType joinType;
    private final List<AnimatableFloatValue> lineDashPattern;
    private final String name;
    private final AnimatableIntegerValue opacity;
    private final AnimatablePointValue startPoint;
    private final AnimatableFloatValue width;

    static class Factory {
        private Factory() {
        }

        static GradientStroke newInstance(JSONObject json, LottieComposition composition) {
            AnimatableFloatValue offset;
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            String name = jSONObject.optString("nm");
            JSONObject jsonColor = jSONObject.optJSONObject("g");
            if (jsonColor != null && jsonColor.has("k")) {
                jsonColor = jsonColor.optJSONObject("k");
            }
            JSONObject jsonColor2 = jsonColor;
            AnimatableGradientColorValue color = null;
            if (jsonColor2 != null) {
                color = com.airbnb.lottie.model.animatable.AnimatableGradientColorValue.Factory.newInstance(jsonColor2, lottieComposition);
            }
            AnimatableGradientColorValue color2 = color;
            JSONObject jsonOpacity = jSONObject.optJSONObject("o");
            AnimatableIntegerValue opacity = null;
            if (jsonOpacity != null) {
                opacity = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(jsonOpacity, lottieComposition);
            }
            AnimatableIntegerValue opacity2 = opacity;
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
            AnimatablePointValue endPoint = startPoint;
            AnimatableFloatValue width = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("w"), lottieComposition);
            LineCapType capType = LineCapType.values()[jSONObject.optInt("lc") - 1];
            LineJoinType joinType = LineJoinType.values()[jSONObject.optInt("lj") - 1];
            List<AnimatableFloatValue> lineDashPattern = new ArrayList();
            if (jSONObject.has("d")) {
                JSONArray dashesJson = jSONObject.optJSONArray("d");
                AnimatableFloatValue offset2 = null;
                AnimatableFloatValue offset3 = null;
                while (offset3 < dashesJson.length()) {
                    JSONObject dashJson = dashesJson.optJSONObject(offset3);
                    String n = dashJson.optString("n");
                    if (n.equals("o")) {
                        offset2 = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(dashJson.optJSONObject("v"), lottieComposition);
                    } else if (n.equals("d") || n.equals("g")) {
                        lineDashPattern.add(com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(dashJson.optJSONObject("v"), lottieComposition));
                    }
                    offset3++;
                    jSONObject = json;
                }
                if (lineDashPattern.size() == 1) {
                    lineDashPattern.add(lineDashPattern.get(0));
                }
                offset = offset2;
            } else {
                offset = null;
            }
            return new GradientStroke(name, gradientType, color2, opacity2, startPoint2, endPoint, width, capType, joinType, lineDashPattern, offset);
        }
    }

    private GradientStroke(String name, GradientType gradientType, AnimatableGradientColorValue gradientColor, AnimatableIntegerValue opacity, AnimatablePointValue startPoint, AnimatablePointValue endPoint, AnimatableFloatValue width, LineCapType capType, LineJoinType joinType, List<AnimatableFloatValue> lineDashPattern, @Nullable AnimatableFloatValue dashOffset) {
        this.name = name;
        this.gradientType = gradientType;
        this.gradientColor = gradientColor;
        this.opacity = opacity;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.width = width;
        this.capType = capType;
        this.joinType = joinType;
        this.lineDashPattern = lineDashPattern;
        this.dashOffset = dashOffset;
    }

    public String getName() {
        return this.name;
    }

    public GradientType getGradientType() {
        return this.gradientType;
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

    public AnimatableFloatValue getWidth() {
        return this.width;
    }

    public LineCapType getCapType() {
        return this.capType;
    }

    public LineJoinType getJoinType() {
        return this.joinType;
    }

    public List<AnimatableFloatValue> getLineDashPattern() {
        return this.lineDashPattern;
    }

    @Nullable
    public AnimatableFloatValue getDashOffset() {
        return this.dashOffset;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new GradientStrokeContent(drawable, layer, this);
    }
}
