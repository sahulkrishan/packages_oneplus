package com.airbnb.lottie.model.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.airbnb.lottie.LottieDrawable;

public class ImageLayer extends BaseLayer {
    private final float density;
    private final Rect dst = new Rect();
    private final Paint paint = new Paint(3);
    private final Rect src = new Rect();

    ImageLayer(LottieDrawable lottieDrawable, Layer layerModel, float density) {
        super(lottieDrawable, layerModel);
        this.density = density;
    }

    public void drawLayer(@NonNull Canvas canvas, Matrix parentMatrix, int parentAlpha) {
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            this.paint.setAlpha(parentAlpha);
            canvas.save();
            canvas.concat(parentMatrix);
            this.src.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            this.dst.set(0, 0, (int) (((float) bitmap.getWidth()) * this.density), (int) (((float) bitmap.getHeight()) * this.density));
            canvas.drawBitmap(bitmap, this.src, this.dst, this.paint);
            canvas.restore();
        }
    }

    public void getBounds(RectF outBounds, Matrix parentMatrix) {
        super.getBounds(outBounds, parentMatrix);
        Bitmap bitmap = getBitmap();
        if (bitmap != null) {
            outBounds.set(outBounds.left, outBounds.top, Math.min(outBounds.right, (float) bitmap.getWidth()), Math.min(outBounds.bottom, (float) bitmap.getHeight()));
            this.boundsMatrix.mapRect(outBounds);
        }
    }

    @Nullable
    private Bitmap getBitmap() {
        return this.lottieDrawable.getImageAsset(this.layerModel.getRefId());
    }

    public void addColorFilter(@Nullable String layerName, @Nullable String contentName, @Nullable ColorFilter colorFilter) {
        this.paint.setColorFilter(colorFilter);
    }
}
