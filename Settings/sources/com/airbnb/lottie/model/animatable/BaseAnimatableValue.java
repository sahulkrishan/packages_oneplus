package com.airbnb.lottie.model.animatable;

import com.airbnb.lottie.animation.Keyframe;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BaseAnimatableValue<V, O> implements AnimatableValue<V, O> {
    final V initialValue;
    final List<Keyframe<V>> keyframes;

    BaseAnimatableValue(V initialValue) {
        this(Collections.emptyList(), initialValue);
    }

    BaseAnimatableValue(List<Keyframe<V>> keyframes, V initialValue) {
        this.keyframes = keyframes;
        this.initialValue = initialValue;
    }

    /* Access modifiers changed, original: 0000 */
    public O convertType(V value) {
        return value;
    }

    public boolean hasAnimation() {
        return this.keyframes.isEmpty() ^ 1;
    }

    public O getInitialValue() {
        return convertType(this.initialValue);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("parseInitialValue=");
        sb.append(this.initialValue);
        if (!this.keyframes.isEmpty()) {
            sb.append(", values=");
            sb.append(Arrays.toString(this.keyframes.toArray()));
        }
        return sb.toString();
    }
}
