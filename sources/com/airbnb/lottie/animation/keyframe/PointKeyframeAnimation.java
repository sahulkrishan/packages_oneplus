package com.airbnb.lottie.animation.keyframe;

import android.graphics.PointF;
import com.airbnb.lottie.animation.Keyframe;
import java.util.List;

public class PointKeyframeAnimation extends KeyframeAnimation<PointF> {
    private final PointF point = new PointF();

    public PointKeyframeAnimation(List<Keyframe<PointF>> keyframes) {
        super(keyframes);
    }

    public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
        if (keyframe.startValue == null || keyframe.endValue == null) {
            throw new IllegalStateException("Missing values for keyframe.");
        }
        PointF startPoint = keyframe.startValue;
        PointF endPoint = keyframe.endValue;
        this.point.set(startPoint.x + ((endPoint.x - startPoint.x) * keyframeProgress), startPoint.y + ((endPoint.y - startPoint.y) * keyframeProgress));
        return this.point;
    }
}
