package com.airbnb.lottie.model.content;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TrimPathContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONObject;

public class ShapeTrimPath implements ContentModel {
    private final AnimatableFloatValue end;
    private final String name;
    private final AnimatableFloatValue offset;
    private final AnimatableFloatValue start;
    private final Type type;

    static class Factory {
        private Factory() {
        }

        static ShapeTrimPath newInstance(JSONObject json, LottieComposition composition) {
            return new ShapeTrimPath(json.optString("nm"), Type.forId(json.optInt("m", 1)), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("s"), composition, false), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("e"), composition, false), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("o"), composition, false));
        }
    }

    public enum Type {
        Simultaneously,
        Individually;

        static Type forId(int id) {
            switch (id) {
                case 1:
                    return Simultaneously;
                case 2:
                    return Individually;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown trim path type ");
                    stringBuilder.append(id);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }
    }

    private ShapeTrimPath(String name, Type type, AnimatableFloatValue start, AnimatableFloatValue end, AnimatableFloatValue offset) {
        this.name = name;
        this.type = type;
        this.start = start;
        this.end = end;
        this.offset = offset;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public AnimatableFloatValue getEnd() {
        return this.end;
    }

    public AnimatableFloatValue getStart() {
        return this.start;
    }

    public AnimatableFloatValue getOffset() {
        return this.offset;
    }

    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new TrimPathContent(layer, this);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Trim Path: {start: ");
        stringBuilder.append(this.start);
        stringBuilder.append(", end: ");
        stringBuilder.append(this.end);
        stringBuilder.append(", offset: ");
        stringBuilder.append(this.offset);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
