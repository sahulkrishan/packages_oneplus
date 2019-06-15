package com.android.setupwizardlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import java.lang.ref.SoftReference;

public class GlifPatternDrawable extends Drawable {
    @SuppressLint({"InlinedApi"})
    private static final int[] ATTRS_PRIMARY_COLOR = new int[]{16843827};
    private static final float COLOR_ALPHA = 0.8f;
    private static final int COLOR_ALPHA_INT = 204;
    private static final float MAX_CACHED_BITMAP_SCALE = 1.5f;
    private static final int NUM_PATHS = 7;
    private static final float SCALE_FOCUS_X = 0.146f;
    private static final float SCALE_FOCUS_Y = 0.228f;
    private static final float VIEWBOX_HEIGHT = 768.0f;
    private static final float VIEWBOX_WIDTH = 1366.0f;
    private static SoftReference<Bitmap> sBitmapCache;
    private static int[] sPatternLightness;
    private static Path[] sPatternPaths;
    private int mColor;
    private Paint mTempPaint = new Paint(1);

    public static GlifPatternDrawable getDefault(Context context) {
        int colorPrimary = 0;
        if (VERSION.SDK_INT >= 21) {
            TypedArray a = context.obtainStyledAttributes(ATTRS_PRIMARY_COLOR);
            colorPrimary = a.getColor(0, ViewCompat.MEASURED_STATE_MASK);
            a.recycle();
        }
        return new GlifPatternDrawable(colorPrimary);
    }

    @VisibleForTesting
    public static void invalidatePattern() {
        sBitmapCache = null;
    }

    public GlifPatternDrawable(int color) {
        setColor(color);
    }

    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        int drawableWidth = bounds.width();
        int drawableHeight = bounds.height();
        Bitmap bitmap = null;
        if (sBitmapCache != null) {
            bitmap = (Bitmap) sBitmapCache.get();
        }
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (drawableWidth > bitmapWidth && ((float) bitmapWidth) < 2049.0f) {
                bitmap = null;
            } else if (drawableHeight > bitmapHeight && ((float) bitmapHeight) < 1152.0f) {
                bitmap = null;
            }
        }
        if (bitmap == null) {
            this.mTempPaint.reset();
            bitmap = createBitmapCache(drawableWidth, drawableHeight);
            sBitmapCache = new SoftReference(bitmap);
            this.mTempPaint.reset();
        }
        canvas.save();
        canvas.clipRect(bounds);
        scaleCanvasToBounds(canvas, bitmap, bounds);
        canvas.drawColor(ViewCompat.MEASURED_STATE_MASK);
        this.mTempPaint.setColor(-1);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mTempPaint);
        canvas.drawColor(this.mColor);
        canvas.restore();
    }

    @VisibleForTesting
    public Bitmap createBitmapCache(int drawableWidth, int drawableHeight) {
        float scale = Math.min(1.5f, Math.max(((float) drawableWidth) / VIEWBOX_WIDTH, ((float) drawableHeight) / VIEWBOX_HEIGHT));
        Bitmap bitmap = Bitmap.createBitmap((int) (1152040960 * scale), (int) (1145044992 * scale), Config.ALPHA_8);
        renderOnCanvas(new Canvas(bitmap), scale);
        return bitmap;
    }

    private void renderOnCanvas(Canvas canvas, float scale) {
        Canvas canvas2 = canvas;
        canvas.save();
        float f = scale;
        canvas2.scale(f, f);
        this.mTempPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        int i = 0;
        if (sPatternPaths == null) {
            sPatternPaths = new Path[7];
            sPatternLightness = new int[]{10, 40, 51, 66, 91, 112, 130};
            Path[] pathArr = sPatternPaths;
            Path path = new Path();
            pathArr[0] = path;
            Path p = path;
            p.moveTo(1029.4f, 357.5f);
            p.lineTo(VIEWBOX_WIDTH, 759.1f);
            p.lineTo(VIEWBOX_WIDTH, 0.0f);
            p.lineTo(1137.7f, 0.0f);
            p.close();
            Path[] pathArr2 = sPatternPaths;
            Path path2 = new Path();
            pathArr2[1] = path2;
            p = path2;
            p.moveTo(1138.1f, 0.0f);
            p.rLineTo(-144.8f, VIEWBOX_HEIGHT);
            p.rLineTo(372.7f, 0.0f);
            p.rLineTo(0.0f, -524.0f);
            p.cubicTo(1290.7f, 121.6f, 1219.2f, 41.1f, 1178.7f, 0.0f);
            p.close();
            pathArr2 = sPatternPaths;
            Path path3 = new Path();
            pathArr2[2] = path3;
            p = path3;
            p.moveTo(949.8f, VIEWBOX_HEIGHT);
            p.rCubicTo(92.6f, -170.6f, 213.0f, -440.3f, 269.4f, -768.0f);
            p.lineTo(585.0f, 0.0f);
            p.rLineTo(2.1f, 766.0f);
            p.close();
            pathArr2 = sPatternPaths;
            path3 = new Path();
            pathArr2[3] = path3;
            p = path3;
            p.moveTo(471.1f, VIEWBOX_HEIGHT);
            p.rMoveTo(704.5f, 0.0f);
            p.cubicTo(1123.6f, 563.3f, 1027.4f, 275.2f, 856.2f, 0.0f);
            p.lineTo(476.4f, 0.0f);
            p.rLineTo(-5.3f, VIEWBOX_HEIGHT);
            p.close();
            pathArr2 = sPatternPaths;
            path3 = new Path();
            pathArr2[4] = path3;
            p = path3;
            p.moveTo(323.1f, VIEWBOX_HEIGHT);
            p.moveTo(777.5f, VIEWBOX_HEIGHT);
            p.cubicTo(661.9f, 348.8f, 427.2f, 21.4f, 401.2f, 25.4f);
            p.lineTo(323.1f, VIEWBOX_HEIGHT);
            p.close();
            pathArr2 = sPatternPaths;
            path3 = new Path();
            pathArr2[5] = path3;
            p = path3;
            p.moveTo(178.44286f, 766.8571f);
            p.lineTo(308.7f, VIEWBOX_HEIGHT);
            p.cubicTo(381.7f, 604.6f, 481.6f, 344.3f, 562.2f, 0.0f);
            p.lineTo(0.0f, 0.0f);
            p.close();
            pathArr2 = sPatternPaths;
            path3 = new Path();
            pathArr2[6] = path3;
            p = path3;
            p.moveTo(146.0f, 0.0f);
            p.lineTo(0.0f, 0.0f);
            p.lineTo(0.0f, VIEWBOX_HEIGHT);
            p.lineTo(394.2f, VIEWBOX_HEIGHT);
            p.cubicTo(327.7f, 475.3f, 228.5f, 201.0f, 146.0f, 0.0f);
            p.close();
        }
        while (true) {
            int i2 = i;
            if (i2 < 7) {
                this.mTempPaint.setColor(sPatternLightness[i2] << 24);
                canvas2.drawPath(sPatternPaths[i2], this.mTempPaint);
                i = i2 + 1;
            } else {
                canvas.restore();
                this.mTempPaint.reset();
                return;
            }
        }
    }

    @VisibleForTesting
    public void scaleCanvasToBounds(Canvas canvas, Bitmap bitmap, Rect drawableBounds) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleX = ((float) drawableBounds.width()) / ((float) bitmapWidth);
        float scaleY = ((float) drawableBounds.height()) / ((float) bitmapHeight);
        canvas.scale(scaleX, scaleY);
        if (scaleY > scaleX) {
            canvas.scale(scaleY / scaleX, 1.0f, SCALE_FOCUS_X * ((float) bitmapWidth), 0.0f);
        } else if (scaleX > scaleY) {
            canvas.scale(1.0f, scaleX / scaleY, 0.0f, SCALE_FOCUS_Y * ((float) bitmapHeight));
        }
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return 0;
    }

    public void setColor(int color) {
        this.mColor = Color.argb(COLOR_ALPHA_INT, Color.red(color), Color.green(color), Color.blue(color));
        invalidateSelf();
    }

    public int getColor() {
        return Color.argb(255, Color.red(this.mColor), Color.green(this.mColor), Color.blue(this.mColor));
    }
}
