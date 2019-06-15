package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeTrimPath.Type;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseStrokeContent implements DrawingContent, AnimationListener {
    private final List<BaseKeyframeAnimation<?, Float>> dashPatternAnimations;
    @Nullable
    private final BaseKeyframeAnimation<?, Float> dashPatternOffsetAnimation;
    private final float[] dashPatternValues;
    private final LottieDrawable lottieDrawable;
    private final BaseKeyframeAnimation<?, Integer> opacityAnimation;
    final Paint paint = new Paint(1);
    private final Path path = new Path();
    private final List<PathGroup> pathGroups = new ArrayList();
    private final PathMeasure pm = new PathMeasure();
    private final RectF rect = new RectF();
    private final Path trimPathPath = new Path();
    private final BaseKeyframeAnimation<?, Float> widthAnimation;

    private static final class PathGroup {
        private final List<PathContent> paths;
        @Nullable
        private final TrimPathContent trimPath;

        private PathGroup(@Nullable TrimPathContent trimPath) {
            this.paths = new ArrayList();
            this.trimPath = trimPath;
        }
    }

    BaseStrokeContent(LottieDrawable lottieDrawable, BaseLayer layer, Cap cap, Join join, AnimatableIntegerValue opacity, AnimatableFloatValue width, List<AnimatableFloatValue> dashPattern, AnimatableFloatValue offset) {
        int i;
        this.lottieDrawable = lottieDrawable;
        this.paint.setStyle(Style.STROKE);
        this.paint.setStrokeCap(cap);
        this.paint.setStrokeJoin(join);
        this.opacityAnimation = opacity.createAnimation();
        this.widthAnimation = width.createAnimation();
        if (offset == null) {
            this.dashPatternOffsetAnimation = null;
        } else {
            this.dashPatternOffsetAnimation = offset.createAnimation();
        }
        this.dashPatternAnimations = new ArrayList(dashPattern.size());
        this.dashPatternValues = new float[dashPattern.size()];
        int i2 = 0;
        for (i = 0; i < dashPattern.size(); i++) {
            this.dashPatternAnimations.add(((AnimatableFloatValue) dashPattern.get(i)).createAnimation());
        }
        layer.addAnimation(this.opacityAnimation);
        layer.addAnimation(this.widthAnimation);
        for (i = 0; i < this.dashPatternAnimations.size(); i++) {
            layer.addAnimation((BaseKeyframeAnimation) this.dashPatternAnimations.get(i));
        }
        if (this.dashPatternOffsetAnimation != null) {
            layer.addAnimation(this.dashPatternOffsetAnimation);
        }
        this.opacityAnimation.addUpdateListener(this);
        this.widthAnimation.addUpdateListener(this);
        while (i2 < dashPattern.size()) {
            ((BaseKeyframeAnimation) this.dashPatternAnimations.get(i2)).addUpdateListener(this);
            i2++;
        }
        if (this.dashPatternOffsetAnimation != null) {
            this.dashPatternOffsetAnimation.addUpdateListener(this);
        }
    }

    public void onValueChanged() {
        this.lottieDrawable.invalidateSelf();
    }

    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        TrimPathContent trimPathContentBefore = null;
        for (int i = contentsBefore.size() - 1; i >= 0; i--) {
            Content content = (Content) contentsBefore.get(i);
            if ((content instanceof TrimPathContent) && ((TrimPathContent) content).getType() == Type.Individually) {
                trimPathContentBefore = (TrimPathContent) content;
            }
        }
        if (trimPathContentBefore != null) {
            trimPathContentBefore.addListener(this);
        }
        PathGroup currentPathGroup = null;
        for (int i2 = contentsAfter.size() - 1; i2 >= 0; i2--) {
            Content content2 = (Content) contentsAfter.get(i2);
            if ((content2 instanceof TrimPathContent) && ((TrimPathContent) content2).getType() == Type.Individually) {
                if (currentPathGroup != null) {
                    this.pathGroups.add(currentPathGroup);
                }
                currentPathGroup = new PathGroup((TrimPathContent) content2);
                ((TrimPathContent) content2).addListener(this);
            } else if (content2 instanceof PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = new PathGroup(trimPathContentBefore);
                }
                currentPathGroup.paths.add((PathContent) content2);
            }
        }
        if (currentPathGroup != null) {
            this.pathGroups.add(currentPathGroup);
        }
    }

    public void draw(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        L.beginSection("StrokeContent#draw");
        this.paint.setAlpha((int) ((((((float) parentAlpha) / 255.0f) * ((float) ((Integer) this.opacityAnimation.getValue()).intValue())) / 100.0f) * 1132396544));
        this.paint.setStrokeWidth(((Float) this.widthAnimation.getValue()).floatValue() * Utils.getScale(parentMatrix));
        if (this.paint.getStrokeWidth() <= 0.0f) {
            L.endSection("StrokeContent#draw");
            return;
        }
        applyDashPatternIfNeeded(parentMatrix);
        for (int i = 0; i < this.pathGroups.size(); i++) {
            PathGroup pathGroup = (PathGroup) this.pathGroups.get(i);
            if (pathGroup.trimPath != null) {
                applyTrimPath(canvas, pathGroup, parentMatrix);
            } else {
                L.beginSection("StrokeContent#buildPath");
                this.path.reset();
                for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
                    this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
                }
                L.endSection("StrokeContent#buildPath");
                L.beginSection("StrokeContent#drawPath");
                canvas.drawPath(this.path, this.paint);
                L.endSection("StrokeContent#drawPath");
            }
        }
        L.endSection("StrokeContent#draw");
    }

    private void applyTrimPath(Canvas canvas, PathGroup pathGroup, Matrix parentMatrix) {
        L.beginSection("StrokeContent#applyTrimPath");
        if (pathGroup.trimPath == null) {
            L.endSection("StrokeContent#applyTrimPath");
            return;
        }
        this.path.reset();
        for (int j = pathGroup.paths.size() - 1; j >= 0; j--) {
            this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
        }
        this.pm.setPath(this.path, false);
        float totalLength = this.pm.getLength();
        while (this.pm.nextContour()) {
            totalLength += this.pm.getLength();
        }
        float offsetLength = (((Float) pathGroup.trimPath.getOffset().getValue()).floatValue() * totalLength) / 360.0f;
        float startLength = ((((Float) pathGroup.trimPath.getStart().getValue()).floatValue() * totalLength) / 100.0f) + offsetLength;
        float endLength = ((((Float) pathGroup.trimPath.getEnd().getValue()).floatValue() * totalLength) / 100.0f) + offsetLength;
        float currentLength = 0.0f;
        for (int j2 = pathGroup.paths.size() - 1; j2 >= 0; j2--) {
            this.trimPathPath.set(((PathContent) pathGroup.paths.get(j2)).getPath());
            this.trimPathPath.transform(parentMatrix);
            this.pm.setPath(this.trimPathPath, false);
            float length = this.pm.getLength();
            float startValue;
            if (endLength > totalLength && endLength - totalLength < currentLength + length && currentLength < endLength - totalLength) {
                if (startLength > totalLength) {
                    startValue = (startLength - totalLength) / length;
                } else {
                    startValue = 0.0f;
                }
                Utils.applyTrimPathIfNeeded(this.trimPathPath, startValue, Math.min((endLength - totalLength) / length, 1.0f), 0.0f);
                canvas.drawPath(this.trimPathPath, this.paint);
            } else if (currentLength + length >= startLength && currentLength <= endLength) {
                if (currentLength + length > endLength || startLength >= currentLength) {
                    float endValue;
                    if (startLength < currentLength) {
                        startValue = 0.0f;
                    } else {
                        startValue = (startLength - currentLength) / length;
                    }
                    if (endLength > currentLength + length) {
                        endValue = 1.0f;
                    } else {
                        endValue = (endLength - currentLength) / length;
                    }
                    Utils.applyTrimPathIfNeeded(this.trimPathPath, startValue, endValue, 0.0f);
                    canvas.drawPath(this.trimPathPath, this.paint);
                } else {
                    canvas.drawPath(this.trimPathPath, this.paint);
                }
            }
            currentLength += length;
        }
        L.endSection("StrokeContent#applyTrimPath");
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        L.beginSection("StrokeContent#getBounds");
        this.path.reset();
        for (int i = 0; i < this.pathGroups.size(); i++) {
            PathGroup pathGroup = (PathGroup) this.pathGroups.get(i);
            for (int j = 0; j < pathGroup.paths.size(); j++) {
                this.path.addPath(((PathContent) pathGroup.paths.get(j)).getPath(), parentMatrix);
            }
        }
        this.path.computeBounds(this.rect, false);
        float width = ((Float) this.widthAnimation.getValue()).floatValue();
        this.rect.set(this.rect.left - (width / 2.0f), this.rect.top - (width / 2.0f), this.rect.right + (width / 2.0f), this.rect.bottom + (width / 2.0f));
        outBounds.set(this.rect);
        outBounds.set(outBounds.left - 1.0f, outBounds.top - 1.0f, outBounds.right + 1.0f, outBounds.bottom + 1.0f);
        L.endSection("StrokeContent#getBounds");
    }

    private void applyDashPatternIfNeeded(Matrix parentMatrix) {
        L.beginSection("StrokeContent#applyDashPattern");
        if (this.dashPatternAnimations.isEmpty()) {
            L.endSection("StrokeContent#applyDashPattern");
            return;
        }
        float scale = Utils.getScale(parentMatrix);
        for (int i = 0; i < this.dashPatternAnimations.size(); i++) {
            this.dashPatternValues[i] = ((Float) ((BaseKeyframeAnimation) this.dashPatternAnimations.get(i)).getValue()).floatValue();
            if (i % 2 == 0) {
                if (this.dashPatternValues[i] < 1.0f) {
                    this.dashPatternValues[i] = 1.0f;
                }
            } else if (this.dashPatternValues[i] < 0.1f) {
                this.dashPatternValues[i] = 0.1f;
            }
            float[] fArr = this.dashPatternValues;
            fArr[i] = fArr[i] * scale;
        }
        this.paint.setPathEffect(new DashPathEffect(this.dashPatternValues, this.dashPatternOffsetAnimation == null ? 0.0f : ((Float) this.dashPatternOffsetAnimation.getValue()).floatValue()));
        L.endSection("StrokeContent#applyDashPattern");
    }
}
