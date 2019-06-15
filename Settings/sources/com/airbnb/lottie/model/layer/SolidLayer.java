package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;

public class SolidLayer extends BaseLayer {
    private final Layer layerModel;
    private final Paint paint = new Paint();
    private final RectF rect = new RectF();

    SolidLayer(LottieDrawable lottieDrawable, Layer layerModel) {
        super(lottieDrawable, layerModel);
        this.layerModel = layerModel;
        this.paint.setAlpha(0);
        this.paint.setStyle(Style.FILL);
        this.paint.setColor(layerModel.getSolidColor());
    }

    public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        int backgroundAlpha = Color.alpha(this.layerModel.getSolidColor());
        if (backgroundAlpha != 0) {
            int alpha = (int) (((((float) parentAlpha) / 255.0f) * (((((float) backgroundAlpha) / 255.0f) * ((float) ((Integer) this.transform.getOpacity().getValue()).intValue())) / 100.0f)) * 1132396544);
            this.paint.setAlpha(alpha);
            if (alpha > 0) {
                updateRect(parentMatrix);
                canvas.drawRect(this.rect, this.paint);
            }
        }
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        super.getBounds(outBounds, parentMatrix);
        updateRect(this.boundsMatrix);
        outBounds.set(this.rect);
    }

    private void updateRect(Matrix matrix) {
        this.rect.set(0.0f, 0.0f, (float) this.layerModel.getSolidWidth(), (float) this.layerModel.getSolidHeight());
        matrix.mapRect(this.rect);
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        this.paint.setColorFilter(colorFilter);
    }
}
