package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.PolystarContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.android.settings.Utils;
import org.json.JSONObject;

public class PolystarShape implements ContentModel {
    private final AnimatableFloatValue innerRadius;
    private final AnimatableFloatValue innerRoundedness;
    private final String name;
    private final AnimatableFloatValue outerRadius;
    private final AnimatableFloatValue outerRoundedness;
    private final AnimatableFloatValue points;
    private final AnimatableValue<PointF, PointF> position;
    private final AnimatableFloatValue rotation;
    private final Type type;

    static class Factory {
        private Factory() {
        }

        static PolystarShape newInstance(JSONObject json, LottieComposition composition) {
            AnimatableFloatValue innerRadius;
            AnimatableFloatValue newInstance;
            JSONObject jSONObject = json;
            LottieComposition lottieComposition = composition;
            String name = jSONObject.optString("nm");
            Type type = Type.forValue(jSONObject.optInt("sy"));
            AnimatableFloatValue points = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("pt"), lottieComposition, false);
            AnimatableValue<PointF, PointF> position = AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(jSONObject.optJSONObject("p"), lottieComposition);
            AnimatableFloatValue rotation = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("r"), lottieComposition, false);
            AnimatableFloatValue outerRadius = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("or"), lottieComposition);
            AnimatableFloatValue outerRoundedness = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject(Utils.OS_PKG), lottieComposition, false);
            if (type == Type.Star) {
                innerRadius = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("ir"), lottieComposition);
                newInstance = com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(jSONObject.optJSONObject("is"), lottieComposition, false);
            } else {
                innerRadius = null;
                newInstance = null;
            }
            return new PolystarShape(name, type, points, position, rotation, innerRadius, outerRadius, newInstance, outerRoundedness);
        }
    }

    public enum Type {
        Star(1),
        Polygon(2);
        
        private final int value;

        private Type(int value) {
            this.value = value;
        }

        static Type forValue(int value) {
            for (Type type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }
    }

    private PolystarShape(String name, Type type, AnimatableFloatValue points, AnimatableValue<PointF, PointF> position, AnimatableFloatValue rotation, AnimatableFloatValue innerRadius, AnimatableFloatValue outerRadius, AnimatableFloatValue innerRoundedness, AnimatableFloatValue outerRoundedness) {
        this.name = name;
        this.type = type;
        this.points = points;
        this.position = position;
        this.rotation = rotation;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.innerRoundedness = innerRoundedness;
        this.outerRoundedness = outerRoundedness;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public AnimatableFloatValue getPoints() {
        return this.points;
    }

    public AnimatableValue<PointF, PointF> getPosition() {
        return this.position;
    }

    public AnimatableFloatValue getRotation() {
        return this.rotation;
    }

    public AnimatableFloatValue getInnerRadius() {
        return this.innerRadius;
    }

    public AnimatableFloatValue getOuterRadius() {
        return this.outerRadius;
    }

    public AnimatableFloatValue getInnerRoundedness() {
        return this.innerRoundedness;
    }

    public AnimatableFloatValue getOuterRoundedness() {
        return this.outerRoundedness;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new PolystarContent(drawable, layer, this);
    }
}
