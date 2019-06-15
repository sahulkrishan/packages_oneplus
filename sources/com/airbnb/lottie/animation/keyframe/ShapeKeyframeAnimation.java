package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.List;

public class ShapeKeyframeAnimation extends BaseKeyframeAnimation<ShapeData, Path> {
    private final Path tempPath = new Path();
    private final ShapeData tempShapeData = new ShapeData();

    public ShapeKeyframeAnimation(List<Keyframe<ShapeData>> keyframes) {
        super(keyframes);
    }

    public Path getValue(Keyframe<ShapeData> keyframe, float keyframeProgress) {
        this.tempShapeData.interpolateBetween(keyframe.startValue, keyframe.endValue, keyframeProgress);
        MiscUtils.getPathFromData(this.tempShapeData, this.tempPath);
        return this.tempPath;
    }
}
