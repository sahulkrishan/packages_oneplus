package com.airbnb.lottie.model.content;

import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShapeStroke implements ContentModel {
    private final LineCapType capType;
    private final AnimatableColorValue color;
    private final LineJoinType joinType;
    private final List<AnimatableFloatValue> lineDashPattern;
    private final String name;
    @Nullable
    private final AnimatableFloatValue offset;
    private final AnimatableIntegerValue opacity;
    private final AnimatableFloatValue width;

    static class Factory {
        private Factory() {
        }

        static ShapeStroke newInstance(JSONObject json, LottieComposition composition) {
            AnimatableFloatValue offset;
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            String name = jSONObject.optString("nm");
            List lineDashPattern = new ArrayList();
            AnimatableColorValue color = com.airbnb.lottie.model.animatable.AnimatableColorValue.Factory.newInstance(jSONObject.optJSONObject("c"), lottieComposition);
            AnimatableFloatValue width = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("w"), lottieComposition);
            AnimatableIntegerValue opacity = com.airbnb.lottie.model.animatable.AnimatableIntegerValue.Factory.newInstance(jSONObject.optJSONObject("o"), lottieComposition);
            LineCapType capType = LineCapType.values()[jSONObject.optInt("lc") - 1];
            LineJoinType joinType = LineJoinType.values()[jSONObject.optInt("lj") - 1];
            if (jSONObject.has("d")) {
                JSONArray dashesJson = jSONObject.optJSONArray("d");
                AnimatableFloatValue offset2 = null;
                for (AnimatableFloatValue offset3 = null; offset3 < dashesJson.length(); offset3++) {
                    JSONObject dashJson = dashesJson.optJSONObject(offset3);
                    String n = dashJson.optString("n");
                    if (n.equals("o")) {
                        offset2 = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(dashJson.optJSONObject("v"), lottieComposition);
                    } else if (n.equals("d") || n.equals("g")) {
                        lineDashPattern.add(com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(dashJson.optJSONObject("v"), lottieComposition));
                    }
                }
                if (lineDashPattern.size() == 1) {
                    lineDashPattern.add(lineDashPattern.get(0));
                }
                offset = offset2;
            } else {
                offset = null;
            }
            return new ShapeStroke(name, offset, lineDashPattern, color, opacity, width, capType, joinType);
        }
    }

    public enum LineCapType {
        Butt,
        Round,
        Unknown;

        public Cap toPaintCap() {
            switch (this) {
                case Butt:
                    return Cap.BUTT;
                case Round:
                    return Cap.ROUND;
                default:
                    return Cap.SQUARE;
            }
        }
    }

    public enum LineJoinType {
        Miter,
        Round,
        Bevel;

        public Join toPaintJoin() {
            switch (this) {
                case Bevel:
                    return Join.BEVEL;
                case Miter:
                    return Join.MITER;
                case Round:
                    return Join.ROUND;
                default:
                    return null;
            }
        }
    }

    private ShapeStroke(String name, @Nullable AnimatableFloatValue offset, List<AnimatableFloatValue> lineDashPattern, AnimatableColorValue color, AnimatableIntegerValue opacity, AnimatableFloatValue width, LineCapType capType, LineJoinType joinType) {
        this.name = name;
        this.offset = offset;
        this.lineDashPattern = lineDashPattern;
        this.color = color;
        this.opacity = opacity;
        this.width = width;
        this.capType = capType;
        this.joinType = joinType;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new StrokeContent(drawable, layer, this);
    }

    public String getName() {
        return this.name;
    }

    public AnimatableColorValue getColor() {
        return this.color;
    }

    public AnimatableIntegerValue getOpacity() {
        return this.opacity;
    }

    public AnimatableFloatValue getWidth() {
        return this.width;
    }

    public List<AnimatableFloatValue> getLineDashPattern() {
        return this.lineDashPattern;
    }

    public AnimatableFloatValue getDashOffset() {
        return this.offset;
    }

    public LineCapType getCapType() {
        return this.capType;
    }

    public LineJoinType getJoinType() {
        return this.joinType;
    }
}
