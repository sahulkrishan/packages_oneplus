package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.RepeaterContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;
import org.json.JSONObject;

public class Repeater implements ContentModel {
    private final AnimatableFloatValue copies;
    private final String name;
    private final AnimatableFloatValue offset;
    private final AnimatableTransform transform;

    static final class Factory {
        private Factory() {
        }

        static Repeater newInstance(JSONObject json, LottieComposition composition) {
            return new Repeater(json.optString("nm"), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("c"), composition, false), com.airbnb.lottie.model.animatable.AnimatableFloatValue.Factory.newInstance(json.optJSONObject("o"), composition, false), com.airbnb.lottie.model.animatable.AnimatableTransform.Factory.newInstance(json.optJSONObject("tr"), composition));
        }
    }

    Repeater(String name, AnimatableFloatValue copies, AnimatableFloatValue offset, AnimatableTransform transform) {
        this.name = name;
        this.copies = copies;
        this.offset = offset;
        this.transform = transform;
    }

    public String getName() {
        return this.name;
    }

    public AnimatableFloatValue getCopies() {
        return this.copies;
    }

    public AnimatableFloatValue getOffset() {
        return this.offset;
    }

    public AnimatableTransform getTransform() {
        return this.transform;
    }

    @Nullable
    public Content toContent(LottieDrawable drawable, BaseLayer layer) {
        return new RepeaterContent(drawable, layer, this);
    }
}
