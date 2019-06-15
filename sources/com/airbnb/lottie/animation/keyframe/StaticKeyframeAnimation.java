package com.airbnb.lottie.animation.keyframe;

import android.support.annotation.FloatRange;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import java.util.Collections;

public class StaticKeyframeAnimation<K, A> extends BaseKeyframeAnimation<K, A> {
    private final A initialValue;

    public StaticKeyframeAnimation(A initialValue) {
        super(Collections.emptyList());
        this.initialValue = initialValue;
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
    }

    public void addUpdateListener(AnimationListener listener) {
    }

    public A getValue() {
        return this.initialValue;
    }

    public A getValue(Keyframe<K> keyframe, float keyframeProgress) {
        return this.initialValue;
    }
}
