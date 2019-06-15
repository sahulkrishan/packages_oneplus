package com.airbnb.lottie.animation.content;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.content.RectangleShape;
import com.airbnb.lottie.model.content.ShapeTrimPath.Type;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.Utils;
import java.util.List;

public class RectangleContent implements PathContent, AnimationListener {
    private final BaseKeyframeAnimation<?, Float> cornerRadiusAnimation;
    private boolean isPathValid;
    private final LottieDrawable lottieDrawable;
    private final String name;
    private final Path path = new Path();
    private final BaseKeyframeAnimation<?, PointF> positionAnimation;
    private final RectF rect = new RectF();
    private final BaseKeyframeAnimation<?, PointF> sizeAnimation;
    @Nullable
    private TrimPathContent trimPath;

    public RectangleContent(LottieDrawable lottieDrawable, BaseLayer layer, RectangleShape rectShape) {
        this.name = rectShape.getName();
        this.lottieDrawable = lottieDrawable;
        this.positionAnimation = rectShape.getPosition().createAnimation();
        this.sizeAnimation = rectShape.getSize().createAnimation();
        this.cornerRadiusAnimation = rectShape.getCornerRadius().createAnimation();
        layer.addAnimation(this.positionAnimation);
        layer.addAnimation(this.sizeAnimation);
        layer.addAnimation(this.cornerRadiusAnimation);
        this.positionAnimation.addUpdateListener(this);
        this.sizeAnimation.addUpdateListener(this);
        this.cornerRadiusAnimation.addUpdateListener(this);
    }

    public String getName() {
        return this.name;
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

    public Path getPath() {
        if (this.isPathValid) {
            return this.path;
        }
        this.path.reset();
        PointF size = (PointF) this.sizeAnimation.getValue();
        float halfWidth = size.x / 2.0f;
        float halfHeight = size.y / 2.0f;
        float radius = this.cornerRadiusAnimation == null ? 0.0f : ((Float) this.cornerRadiusAnimation.getValue()).floatValue();
        float maxRadius = Math.min(halfWidth, halfHeight);
        if (radius > maxRadius) {
            radius = maxRadius;
        }
        PointF position = (PointF) this.positionAnimation.getValue();
        this.path.moveTo(position.x + halfWidth, (position.y - halfHeight) + radius);
        this.path.lineTo(position.x + halfWidth, (position.y + halfHeight) - radius);
        if (radius > 0.0f) {
            this.rect.set((position.x + halfWidth) - (2.0f * radius), (position.y + halfHeight) - (2.0f * radius), position.x + halfWidth, position.y + halfHeight);
            this.path.arcTo(this.rect, 0.0f, 90.0f, false);
        }
        this.path.lineTo((position.x - halfWidth) + radius, position.y + halfHeight);
        if (radius > 0.0f) {
            this.rect.set(position.x - halfWidth, (position.y + halfHeight) - (2.0f * radius), (position.x - halfWidth) + (2.0f * radius), position.y + halfHeight);
            this.path.arcTo(this.rect, 90.0f, 90.0f, false);
        }
        this.path.lineTo(position.x - halfWidth, (position.y - halfHeight) + radius);
        if (radius > 0.0f) {
            this.rect.set(position.x - halfWidth, position.y - halfHeight, (position.x - halfWidth) + (2.0f * radius), (position.y - halfHeight) + (2.0f * radius));
            this.path.arcTo(this.rect, 180.0f, 90.0f, false);
        }
        this.path.lineTo((position.x + halfWidth) - radius, position.y - halfHeight);
        if (radius > 0.0f) {
            this.rect.set((position.x + halfWidth) - (2.0f * radius), position.y - halfHeight, position.x + halfWidth, (position.y - halfHeight) + (2.0f * radius));
            this.path.arcTo(this.rect, 270.0f, 90.0f, false);
        }
        this.path.close();
        Utils.applyTrimPathIfNeeded(this.path, this.trimPath);
        this.isPathValid = true;
        return this.path;
    }
}
