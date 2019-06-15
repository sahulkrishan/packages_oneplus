package com.airbnb.lottie.animation.keyframe;

import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import com.airbnb.lottie.animation.Keyframe;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseKeyframeAnimation<K, A> {
    @Nullable
    private Keyframe<K> cachedKeyframe;
    private boolean isDiscrete = false;
    private final List<? extends Keyframe<K>> keyframes;
    final List<AnimationListener> listeners = new ArrayList();
    private float progress = 0.0f;

    public interface AnimationListener {
        void onValueChanged();
    }

    public abstract A getValue(Keyframe<K> keyframe, float f);

    BaseKeyframeAnimation(List<? extends Keyframe<K>> keyframes) {
        this.keyframes = keyframes;
    }

    public void setIsDiscrete() {
        this.isDiscrete = true;
    }

    public void addUpdateListener(AnimationListener listener) {
        this.listeners.add(listener);
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        if (progress < getStartDelayProgress()) {
            progress = 0.0f;
        } else if (progress > getEndProgress()) {
            progress = 1.0f;
        }
        if (progress != this.progress) {
            this.progress = progress;
            for (int i = 0; i < this.listeners.size(); i++) {
                ((AnimationListener) this.listeners.get(i)).onValueChanged();
            }
        }
    }

    private Keyframe<K> getCurrentKeyframe() {
        if (this.keyframes.isEmpty()) {
            throw new IllegalStateException("There are no keyframes");
        } else if (this.cachedKeyframe != null && this.cachedKeyframe.containsProgress(this.progress)) {
            return this.cachedKeyframe;
        } else {
            int i = 0;
            Keyframe<K> keyframe = (Keyframe) this.keyframes.get(0);
            if (this.progress < keyframe.getStartProgress()) {
                this.cachedKeyframe = keyframe;
                return keyframe;
            }
            while (!keyframe.containsProgress(this.progress) && i < this.keyframes.size()) {
                keyframe = (Keyframe) this.keyframes.get(i);
                i++;
            }
            this.cachedKeyframe = keyframe;
            return keyframe;
        }
    }

    private float getCurrentKeyframeProgress() {
        if (this.isDiscrete) {
            return 0.0f;
        }
        Keyframe<K> keyframe = getCurrentKeyframe();
        if (keyframe.isStatic()) {
            return 0.0f;
        }
        return keyframe.interpolator.getInterpolation((this.progress - keyframe.getStartProgress()) / (keyframe.getEndProgress() - keyframe.getStartProgress()));
    }

    @FloatRange(from = 0.0d, to = 1.0d)
    private float getStartDelayProgress() {
        return this.keyframes.isEmpty() ? 0.0f : ((Keyframe) this.keyframes.get(0)).getStartProgress();
    }

    @FloatRange(from = 0.0d, to = 1.0d)
    private float getEndProgress() {
        return this.keyframes.isEmpty() ? 1.0f : ((Keyframe) this.keyframes.get(this.keyframes.size() - 1)).getEndProgress();
    }

    public A getValue() {
        return getValue(getCurrentKeyframe(), getCurrentKeyframeProgress());
    }

    public float getProgress() {
        return this.progress;
    }
}
