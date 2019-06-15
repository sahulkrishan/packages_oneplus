package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.content.ShapeGroup;
import java.util.Collections;

public class ShapeLayer extends BaseLayer {
    private final ContentGroup contentGroup;

    ShapeLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        super(lottieDrawable, layerModel);
        this.contentGroup = new ContentGroup(lottieDrawable, this, new ShapeGroup(layerModel.getName(), layerModel.getShapes()));
        this.contentGroup.setContents(Collections.emptyList(), Collections.emptyList());
    }

    /* Access modifiers changed, original: 0000 */
    public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        this.contentGroup.draw(canvas, parentMatrix, parentAlpha);
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        super.getBounds(outBounds, parentMatrix);
        this.contentGroup.getBounds(outBounds, this.boundsMatrix);
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        this.contentGroup.addColorFilter(layerName, contentName, colorFilter);
    }
}
