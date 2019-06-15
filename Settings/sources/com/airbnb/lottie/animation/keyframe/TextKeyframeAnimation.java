package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.DocumentData;
import java.util.List;

public class TextKeyframeAnimation extends KeyframeAnimation<DocumentData> {
    public TextKeyframeAnimation(List<? extends Keyframe<DocumentData>> keyframes) {
        super(keyframes);
    }

    /* Access modifiers changed, original: 0000 */
    public DocumentData getValue(Keyframe<DocumentData> keyframe, float keyframeProgress) {
        return (DocumentData) keyframe.startValue;
    }
}
