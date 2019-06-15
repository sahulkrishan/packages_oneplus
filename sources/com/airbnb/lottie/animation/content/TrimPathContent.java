package com.airbnb.lottie.animation.content;

import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import com.airbnb.lottie.model.content.ShapeTrimPath.Type;
import com.airbnb.lottie.model.layer.BaseLayer;
import java.util.ArrayList;
import java.util.List;

public class TrimPathContent implements Content, AnimationListener {
    private final BaseKeyframeAnimation<?, Float> endAnimation;
    private final List<AnimationListener> listeners = new ArrayList();
    private String name;
    private final BaseKeyframeAnimation<?, Float> offsetAnimation;
    private final BaseKeyframeAnimation<?, Float> startAnimation;
    private final Type type;

    public TrimPathContent(BaseLayer layer, ShapeTrimPath trimPath) {
        this.name = trimPath.getName();
        this.type = trimPath.getType();
        this.startAnimation = trimPath.getStart().createAnimation();
        this.endAnimation = trimPath.getEnd().createAnimation();
        this.offsetAnimation = trimPath.getOffset().createAnimation();
        layer.addAnimation(this.startAnimation);
        layer.addAnimation(this.endAnimation);
        layer.addAnimation(this.offsetAnimation);
        this.startAnimation.addUpdateListener(this);
        this.endAnimation.addUpdateListener(this);
        this.offsetAnimation.addUpdateListener(this);
    }

    public void onValueChanged() {
        for (int i = 0; i < this.listeners.size(); i++) {
            ((AnimationListener) this.listeners.get(i)).onValueChanged();
        }
    }

    public void setContents(List<Content> list, List<Content> list2) {
    }

    public String getName() {
        return this.name;
    }

    /* Access modifiers changed, original: 0000 */
    public void addListener(AnimationListener listener) {
        this.listeners.add(listener);
    }

    /* Access modifiers changed, original: 0000 */
    public Type getType() {
        return this.type;
    }

    public BaseKeyframeAnimation<?, Float> getStart() {
        return this.startAnimation;
    }

    public BaseKeyframeAnimation<?, Float> getEnd() {
        return this.endAnimation;
    }

    public BaseKeyframeAnimation<?, Float> getOffset() {
        return this.offsetAnimation;
    }
}
