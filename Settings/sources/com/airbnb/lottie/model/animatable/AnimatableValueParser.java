package com.airbnb.lottie.model.animatable;

import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableValue.Factory;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnimatableValueParser<T> {
    private final LottieComposition composition;
    @Nullable
    private final JSONObject json;
    private final float scale;
    private final Factory<T> valueFactory;

    static class Result<T> {
        @Nullable
        final T initialValue;
        final List<Keyframe<T>> keyframes;

        Result(List<Keyframe<T>> keyframes, @Nullable T initialValue) {
            this.keyframes = keyframes;
            this.initialValue = initialValue;
        }
    }

    private AnimatableValueParser(@Nullable JSONObject json, float scale, LottieComposition composition, Factory<T> valueFactory) {
        this.json = json;
        this.scale = scale;
        this.composition = composition;
        this.valueFactory = valueFactory;
    }

    static <T> AnimatableValueParser<T> newInstance(@Nullable JSONObject json, float scale, LottieComposition composition, Factory<T> valueFactory) {
        return new AnimatableValueParser(json, scale, composition, valueFactory);
    }

    /* Access modifiers changed, original: 0000 */
    public Result<T> parseJson() {
        List<Keyframe<T>> keyframes = parseKeyframes();
        return new Result(keyframes, parseInitialValue(keyframes));
    }

    private List<Keyframe<T>> parseKeyframes() {
        if (this.json == null) {
            return Collections.emptyList();
        }
        Object k = this.json.opt("k");
        if (hasKeyframes(k)) {
            return Keyframe.Factory.parseKeyframes((JSONArray) k, this.composition, this.scale, this.valueFactory);
        }
        return Collections.emptyList();
    }

    @Nullable
    private T parseInitialValue(List<Keyframe<T>> keyframes) {
        if (this.json == null) {
            return null;
        }
        if (keyframes.isEmpty()) {
            return this.valueFactory.valueFromObject(this.json.opt("k"), this.scale);
        }
        return ((Keyframe) keyframes.get(0)).startValue;
    }

    private static boolean hasKeyframes(Object json) {
        boolean z = false;
        if (!(json instanceof JSONArray)) {
            return false;
        }
        Object firstObject = ((JSONArray) json).opt(0);
        if ((firstObject instanceof JSONObject) && ((JSONObject) firstObject).has("t")) {
            z = true;
        }
        return z;
    }
}
