package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation.AnimationListener;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.content.Repeater;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class RepeaterContent implements DrawingContent, PathContent, GreedyContent, AnimationListener {
    private ContentGroup contentGroup;
    private final BaseKeyframeAnimation<Float, Float> copies;
    private final BaseLayer layer;
    private final LottieDrawable lottieDrawable;
    private final Matrix matrix = new Matrix();
    private final String name;
    private final BaseKeyframeAnimation<Float, Float> offset;
    private final Path path = new Path();
    private final TransformKeyframeAnimation transform;

    public RepeaterContent(LottieDrawable lottieDrawable, BaseLayer layer, Repeater repeater) {
        this.lottieDrawable = lottieDrawable;
        this.layer = layer;
        this.name = repeater.getName();
        this.copies = repeater.getCopies().createAnimation();
        layer.addAnimation(this.copies);
        this.copies.addUpdateListener(this);
        this.offset = repeater.getOffset().createAnimation();
        layer.addAnimation(this.offset);
        this.offset.addUpdateListener(this);
        this.transform = repeater.getTransform().createAnimation();
        this.transform.addAnimationsToLayer(layer);
        this.transform.addListener(this);
    }

    public void absorbContent(ListIterator<Content> contentsIter) {
        if (this.contentGroup == null) {
            while (contentsIter.hasPrevious() && contentsIter.previous() != this) {
            }
            List<Content> contents = new ArrayList();
            while (contentsIter.hasPrevious()) {
                contents.add(contentsIter.previous());
                contentsIter.remove();
            }
            Collections.reverse(contents);
            this.contentGroup = new ContentGroup(this.lottieDrawable, this.layer, "Repeater", contents, null);
        }
    }

    public String getName() {
        return this.name;
    }

    public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
        this.contentGroup.setContents(contentsBefore, contentsAfter);
    }

    public Path getPath() {
        Path contentPath = this.contentGroup.getPath();
        this.path.reset();
        float copies = ((Float) this.copies.getValue()).floatValue();
        float offset = ((Float) this.offset.getValue()).floatValue();
        for (int i = ((int) copies) - 1; i >= 0; i--) {
            this.matrix.set(this.transform.getMatrixForRepeater(((float) i) + offset));
            this.path.addPath(contentPath, this.matrix);
        }
        return this.path;
    }

    public void draw(Canvas canvas, Matrix parentMatrix, int alpha) {
        float copies = ((Float) this.copies.getValue()).floatValue();
        float offset = ((Float) this.offset.getValue()).floatValue();
        float startOpacity = ((Float) this.transform.getStartOpacity().getValue()).floatValue() / 100.0f;
        float endOpacity = ((Float) this.transform.getEndOpacity().getValue()).floatValue() / 100.0f;
        for (int i = ((int) copies) - 1; i >= 0; i--) {
            this.matrix.set(parentMatrix);
            this.matrix.preConcat(this.transform.getMatrixForRepeater(((float) i) + offset));
            this.contentGroup.draw(canvas, this.matrix, (int) (((float) alpha) * MiscUtils.lerp(startOpacity, endOpacity, ((float) i) / copies)));
        }
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        this.contentGroup.getBounds(outBounds, parentMatrix);
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        this.contentGroup.addColorFilter(layerName, contentName, colorFilter);
    }

    public void onValueChanged() {
        this.lottieDrawable.invalidateSelf();
    }
}
