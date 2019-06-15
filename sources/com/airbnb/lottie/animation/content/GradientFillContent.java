package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.layer.BaseLayer;
import java.util.ArrayList;
import java.util.List;

public class GradientFillContent implements DrawingContent, AnimationListener {
    private static final int CACHE_STEPS_MS = 32;
    private final RectF boundsRect = new RectF();
    private final int cacheSteps;
    private final BaseKeyframeAnimation<GradientColor, GradientColor> colorAnimation;
    private final BaseKeyframeAnimation<PointF, PointF> endPointAnimation;
    private final LongSparseArray<LinearGradient> linearGradientCache = new LongSparseArray();
    private final LottieDrawable lottieDrawable;
    private final String name;
    private final BaseKeyframeAnimation<Integer, Integer> opacityAnimation;
    private final Paint paint = new Paint(1);
    private final Path path = new Path();
    private final List<PathContent> paths = new ArrayList();
    private final LongSparseArray<RadialGradient> radialGradientCache = new LongSparseArray();
    private final Matrix shaderMatrix = new Matrix();
    private final BaseKeyframeAnimation<PointF, PointF> startPointAnimation;
    private final GradientType type;

    public GradientFillContent(LottieDrawable lottieDrawable, BaseLayer layer, GradientFill fill) {
        this.name = fill.getName();
        this.lottieDrawable = lottieDrawable;
        this.type = fill.getGradientType();
        this.path.setFillType(fill.getFillType());
        this.cacheSteps = (int) (lottieDrawable.getComposition().getDuration() / 32);
        this.colorAnimation = fill.getGradientColor().createAnimation();
        this.colorAnimation.addUpdateListener(this);
        layer.addAnimation(this.colorAnimation);
        this.opacityAnimation = fill.getOpacity().createAnimation();
        this.opacityAnimation.addUpdateListener(this);
        layer.addAnimation(this.opacityAnimation);
        this.startPointAnimation = fill.getStartPoint().createAnimation();
        this.startPointAnimation.addUpdateListener(this);
        layer.addAnimation(this.startPointAnimation);
        this.endPointAnimation = fill.getEndPoint().createAnimation();
        this.endPointAnimation.addUpdateListener(this);
        layer.addAnimation(this.endPointAnimation);
    }

    public void onValueChanged() {
        this.lottieDrawable.invalidateSelf();
    }

    public void setContents(List<Content> list, List<Content> contentsAfter) {
        for (int i = 0; i < contentsAfter.size(); i++) {
            Content content = (Content) contentsAfter.get(i);
            if (content instanceof PathContent) {
                this.paths.add((PathContent) content);
            }
        }
    }

    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        Shader shader;
        L.beginSection("GradientFillContent#draw");
        this.path.reset();
        for (int i = 0; i < this.paths.size(); i++) {
            this.path.addPath(((PathContent) this.paths.get(i)).getPath(), parentMatrix);
        }
        this.path.computeBounds(this.boundsRect, false);
        if (this.type == GradientType.Linear) {
            shader = getLinearGradient();
        } else {
            shader = getRadialGradient();
        }
        this.shaderMatrix.set(parentMatrix);
        shader.setLocalMatrix(this.shaderMatrix);
        this.paint.setShader(shader);
        this.paint.setAlpha((int) ((((((float) parentAlpha) / 255.0f) * ((float) ((Integer) this.opacityAnimation.getValue()).intValue())) / 100.0f) * 1132396544));
        canvas.drawPath(this.path, this.paint);
        L.endSection("GradientFillContent#draw");
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        this.path.reset();
        for (int i = 0; i < this.paths.size(); i++) {
            this.path.addPath(((PathContent) this.paths.get(i)).getPath(), parentMatrix);
        }
        this.path.computeBounds(outBounds, false);
        outBounds.set(outBounds.left - 1.0f, outBounds.top - 1.0f, outBounds.right + 1.0f, outBounds.bottom + 1.0f);
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
        int[] colors = gradientColor.getColors();
        gradient = new LinearGradient(startPoint.x, startPoint.y, endPoint.x, endPoint.y, colors, gradientColor.getPositions(), TileMode.CLAMP);
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
        int[] colors = gradientColor.getColors();
        float[] positions = gradientColor.getPositions();
        float x0 = startPoint.x;
        float y0 = startPoint.y;
        float x1 = endPoint.x;
        float y1 = endPoint.y;
        float hypot = (float) Math.hypot((double) (x1 - x0), (double) (y1 - y0));
        float r = hypot;
        gradient = new RadialGradient(x0, y0, hypot, colors, positions, TileMode.CLAMP);
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
