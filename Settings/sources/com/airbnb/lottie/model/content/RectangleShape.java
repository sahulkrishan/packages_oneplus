package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.RectangleContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONObject;

public class RectangleShape implements ContentModel {
    private final AnimatableFloatValue cornerRadius;
    private final String name;
    private final AnimatableValue<PointF, PointF> position;
    private final AnimatablePointValue size;

    static class Factory {
        private Factory() {
        }

        static RectangleShape newInstance(JSONObject json, LottieComposition composition) {
            return new RectangleShape(json.optString("nm"), AnimatablePathValue.createAnimatablePathOrSplitDimensionPath(json.optJSONObject("p"), composition), com.airbnb.lottie.model.animatable.AnimatablePointValue.Factory.newInstance(json.optJSONObject("s"), composition), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("r"), composition));
        }
    }

    private RectangleShape(String name, AnimatableValue<PointF, PointF> position, AnimatablePointValue size, AnimatableFloatValue cornerRadius) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.cornerRadius = cornerRadius;
    }

    public String getName() {
        return this.name;
    }

    public AnimatableFloatValue getCornerRadius() {
        return this.cornerRadius;
    }

    public AnimatablePointValue getSize() {
        return this.size;
    }

    public AnimatableValue<PointF, PointF> getPosition() {
        return this.position;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new RectangleContent(drawable, layer, this);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("RectangleShape{cornerRadius=");
        stringBuilder.append(this.cornerRadius.getInitialValue());
        stringBuilder.append(", position=");
        stringBuilder.append(this.position);
        stringBuilder.append(", size=");
        stringBuilder.append(this.size);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
