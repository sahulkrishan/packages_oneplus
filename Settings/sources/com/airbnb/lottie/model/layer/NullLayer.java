package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import com.airbnb.lottie.LottieDrawable;

public class NullLayer extends BaseLayer {
    NullLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        super(lottieDrawable, layerModel);
    }

    /* Access modifiers changed, original: 0000 */
    public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        super.getBounds(outBounds, parentMatrix);
        outBounds.set(0.0f, 0.0f, 0.0f, 0.0f);
    }
}
