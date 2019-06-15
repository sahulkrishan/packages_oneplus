package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import com.airbnb.lottie.animation.Keyframe;
import java.util.List;

public class PathKeyframeAnimation extends KeyframeAnimation<PointF> {
    private PathMeasure pathMeasure;
    private PathKeyframe pathMeasureKeyframe;
    private final PointF point = new PointF();
    private final float[] pos = new float[2];

    public PathKeyframeAnimation(List<? extends Keyframe<PointF>> keyframes) {
        super(keyframes);
    }

    public PointF getValue(Keyframe<PointF> keyframe, float keyframeProgress) {
        PathKeyframe pathKeyframe = (PathKeyframe) keyframe;
        Path path = pathKeyframe.getPath();
        if (path == null) {
            return (PointF) keyframe.startValue;
        }
        if (this.pathMeasureKeyframe != pathKeyframe) {
            this.pathMeasure = new PathMeasure(path, false);
            this.pathMeasureKeyframe = pathKeyframe;
        }
        this.pathMeasure.getPosTan(this.pathMeasure.getLength() * keyframeProgress, this.pos, null);
        this.point.set(this.pos[0], this.pos[1]);
        return this.point;
    }
}
