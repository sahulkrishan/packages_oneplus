package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.layer.BaseLayer;

public class GradientStrokeContent extends BaseStrokeContent {
    private static final int CACHE_STEPS_MS = 32;
    private final RectF boundsRect = new RectF();
    private final int cacheSteps;
    private final BaseKeyframeAnimation<GradientColor, GradientColor> colorAnimation;
    private final BaseKeyframeAnimation<PointF, PointF> endPointAnimation;
    private final LongSparseArray<LinearGradient> linearGradientCache = new LongSparseArray();
    private final String name;
    private final LongSparseArray<RadialGradient> radialGradientCache = new LongSparseArray();
    private final BaseKeyframeAnimation<PointF, PointF> startPointAnimation;
    private final GradientType type;

    public GradientStrokeContent(LottieDrawable lottieDrawable, BaseLayer layer, GradientStroke stroke) {
        super(lottieDrawable, layer, stroke.getCapType().toPaintCap(), stroke.getJoinType().toPaintJoin(), stroke.getOpacity(), stroke.getWidth(), stroke.getLineDashPattern(), stroke.getDashOffset());
        this.name = stroke.getName();
        this.type = stroke.getGradientType();
        this.cacheSteps = (int) (lottieDrawable.getComposition().getDuration() / 32);
        this.colorAnimation = stroke.getGradientColor().createAnimation();
        this.colorAnimation.addUpdateListener(this);
        layer.addAnimation(this.colorAnimation);
        this.startPointAnimation = stroke.getStartPoint().createAnimation();
        this.startPointAnimation.addUpdateListener(this);
        layer.addAnimation(this.startPointAnimation);
        this.endPointAnimation = stroke.getEndPoint().createAnimation();
        this.endPointAnimation.addUpdateListener(this);
        layer.addAnimation(this.endPointAnimation);
    }

    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        getBounds(this.boundsRect, parentMatrix);
        if (this.type == GradientType.Linear) {
            this.paint.setShader(getLinearGradient());
        } else {
            this.paint.setShader(getRadialGradient());
        }
        super.draw(canvas, parentMatrix, parentAlpha);
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
    }

    public String getName() {
        return this.name;
    }

    private LinearGradient getLinearGradient() {
        int gradientHash = getGradientHash();
        LinearGradient gradient = (LinearGradient) this.linearGradientCache.get((long) gradientHash);
        if (gradient != null) {
            return gradient;
        }
        PointF startPoint = (PointF) this.startPointAnimation.getValue();
        PointF endPoint = (PointF) this.endPointAnimation.getValue();
        GradientColor gradientColor = (GradientColor) this.colorAnimation.getValue();
        int x0 = (int) ((this.boundsRect.left + (this.boundsRect.width() / 2.0f)) + startPoint.x);
        int y0 = (int) ((this.boundsRect.top + (this.boundsRect.height() / 2.0f)) + startPoint.y);
        int x1 = (int) ((this.boundsRect.left + (this.boundsRect.width() / 2.0f)) + endPoint.x);
        int y1 = (int) ((this.boundsRect.top + (this.boundsRect.height() / 2.0f)) + endPoint.y);
        float f = (float) y1;
        gradient = new LinearGradient((float) x0, (float) y0, (float) x1, f, gradientColor.getColors(), gradientColor.getPositions(), TileMode.CLAMP);
        this.linearGradientCache.put((long) gradientHash, gradient);
        return gradient;
    }

    private RadialGradient getRadialGradient() {
        int gradientHash = getGradientHash();
        RadialGradient gradient = (RadialGradient) this.radialGradientCache.get((long) gradientHash);
        if (gradient != null) {
            return gradient;
        }
        PointF startPoint = (PointF) this.startPointAnimation.getValue();
        PointF endPoint = (PointF) this.endPointAnimation.getValue();
        GradientColor gradientColor = (GradientColor) this.colorAnimation.getValue();
        int x0 = (int) ((this.boundsRect.left + (this.boundsRect.width() / 2.0f)) + startPoint.x);
        int y0 = (int) ((this.boundsRect.top + (this.boundsRect.height() / 2.0f)) + startPoint.y);
        int x1 = (int) ((this.boundsRect.left + (this.boundsRect.width() / 2.0f)) + endPoint.x);
        int y1 = (int) ((this.boundsRect.top + (this.boundsRect.height() / 2.0f)) + endPoint.y);
        float hypot = (float) Math.hypot((double) (x1 - x0), (double) (y1 - y0));
        float r = hypot;
        gradient = new RadialGradient((float) x0, (float) y0, hypot, gradientColor.getColors(), gradientColor.getPositions(), TileMode.CLAMP);
        this.radialGradientCache.put((long) gradientHash, gradient);
        return gradient;
    }

    private int getGradientHash() {
        int startPointProgress = Math.round(this.startPointAnimation.getProgress() * ((float) this.cacheSteps));
        int endPointProgress = Math.round(this.endPointAnimation.getProgress() * ((float) this.cacheSteps));
        int colorProgress = Math.round(this.colorAnimation.getProgress() * ((float) this.cacheSteps));
        int hash = 17;
        if (startPointProgress != 0) {
            hash = (17 * 31) * startPointProgress;
        }
        if (endPointProgress != 0) {
            hash = (hash * 31) * endPointProgress;
        }
        if (colorProgress != 0) {
            return (hash * 31) * colorProgress;
        }
        return hash;
    }
}
