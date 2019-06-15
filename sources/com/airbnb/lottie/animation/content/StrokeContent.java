package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.ShapeStroke;
import com.airbnb.lottie.model.layer.BaseLayer;

public class StrokeContent extends BaseStrokeContent {
    private final BaseKeyframeAnimation<Integer, Integer> colorAnimation;
    private final String name;

    public StrokeContent(LottieDrawable lottieDrawable, BaseLayer layer, ShapeStroke stroke) {
        super(lottieDrawable, layer, stroke.getCapType().toPaintCap(), stroke.getJoinType().toPaintJoin(), stroke.getOpacity(), stroke.getWidth(), stroke.getLineDashPattern(), stroke.getDashOffset());
        this.name = stroke.getName();
        this.colorAnimation = stroke.getColor().createAnimation();
        this.colorAnimation.addUpdateListener(this);
        layer.addAnimation(this.colorAnimation);
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        this.paint.setColorFilter(colorFilter);
    }

    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        this.paint.setColor(((Integer) this.colorAnimation.getValue()).intValue());
        super.draw(canvas, parentMatrix, parentAlpha);
    }

    public String getName() {
        return this.name;
    }
}
