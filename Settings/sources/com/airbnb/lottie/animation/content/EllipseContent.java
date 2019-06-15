package com.airbnb.lottie.animation.content;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.content.CircleShape;
import com.airbnb.lottie.model.content.ShapeTrimPath.Type;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.Utils;
import java.util.List;

public class EllipseContent implements PathContent, AnimationListener {
    private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;
    private boolean isPathValid;
    private final LottieDrawable lottieDrawable;
    private final String name;
    private final Path path = new Path();
    private final BaseKeyframeAnimation<?, PointF> positionAnimation;
    private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
    @Nullable
    private TrimPathContent trimPath;

    public EllipseContent(LottieDrawable lottieDrawable, BaseLayer layer, CircleShape circleShape) {
        this.name = circleShape.getName();
        this.lottieDrawable = lottieDrawable;
        this.sizeAnimation = circleShape.getSize().createAnimation();
        this.positionAnimation = circleShape.getPosition().createAnimation();
        layer.addAnimation(this.sizeAnimation);
        layer.addAnimation(this.positionAnimation);
        this.sizeAnimation.addUpdateListener(this);
        this.positionAnimation.addUpdateListener(this);
    }

    public void onValueChanged() {
        invalidate();
    }

    private void invalidate() {
        this.isPathValid = false;
        this.lottieDrawable.invalidateSelf();
    }

    public void setContents(List<Content> contentsBefore, List<Content> list) {
        for (int i = 0; i < contentsBefore.size(); i++) {
            Content content = (Content) contentsBefore.get(i);
            if ((content instanceof TrimPathContent) && ((TrimPathContent) content).getType() == Type.Simultaneously) {
                this.trimPath = (TrimPathContent) content;
                this.trimPath.addListener(this);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public Path getPath() {
        if (this.isPathValid) {
            return this.path;
        }
        this.path.reset();
        PointF size = (PointF) this.sizeAnimation.getValue();
        float halfWidth = size.x / 2.0f;
        float halfHeight = size.y / 2.0f;
        float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
        float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;
        this.path.reset();
        this.path.moveTo(0.0f, -halfHeight);
        this.path.cubicTo(0.0f + cpW, -halfHeight, halfWidth, 0.0f - cpH, halfWidth, 0.0f);
        this.path.cubicTo(halfWidth, 0.0f + cpH, 0.0f + cpW, halfHeight, 0.0f, halfHeight);
        this.path.cubicTo(0.0f - cpW, halfHeight, -halfWidth, 0.0f + cpH, -halfWidth, 0.0f);
        this.path.cubicTo(-halfWidth, 0.0f - cpH, 0.0f - cpW, -halfHeight, 0.0f, -halfHeight);
        PointF position = (PointF) this.positionAnimation.getValue();
        this.path.offset(position.x, position.y);
        this.path.close();
        Utils.applyTrimPathIfNeeded(this.path, this.trimPath);
        this.isPathValid = true;
        return this.path;
    }
}
