package android.support.v17.leanback.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.IntProperty;
import android.util.Property;

public class FitWidthBitmapDrawable extends Drawable {
    public static final Property<FitWidthBitmapDrawable, Integer> PROPERTY_VERTICAL_OFFSET;
    BitmapState mBitmapState;
    final Rect mDest;
    boolean mMutated;

    static class BitmapState extends ConstantState {
        Bitmap mBitmap;
        final Rect mDefaultSource;
        int mOffset;
        Paint mPaint;
        Rect mSource;

        BitmapState() {
            this.mDefaultSource = new Rect();
            this.mPaint = new Paint();
        }

        BitmapState(BitmapState other) {
            this.mDefaultSource = new Rect();
            this.mBitmap = other.mBitmap;
            this.mPaint = new Paint(other.mPaint);
            this.mSource = other.mSource != null ? new Rect(other.mSource) : null;
            this.mDefaultSource.set(other.mDefaultSource);
            this.mOffset = other.mOffset;
        }

        @NonNull
        public Drawable newDrawable() {
            return new FitWidthBitmapDrawable(this);
        }

        public int getChangingConfigurations() {
            return 0;
        }
    }

    public FitWidthBitmapDrawable() {
        this.mDest = new Rect();
        this.mMutated = false;
        this.mBitmapState = new BitmapState();
    }

    FitWidthBitmapDrawable(BitmapState state) {
        this.mDest = new Rect();
        this.mMutated = false;
        this.mBitmapState = state;
    }

    public ConstantState getConstantState() {
        return this.mBitmapState;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mBitmapState = new BitmapState(this.mBitmapState);
            this.mMutated = true;
        }
        return this;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmapState.mBitmap = bitmap;
        if (bitmap != null) {
            this.mBitmapState.mDefaultSource.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        } else {
            this.mBitmapState.mDefaultSource.set(0, 0, 0, 0);
        }
        this.mBitmapState.mSource = null;
    }

    public Bitmap getBitmap() {
        return this.mBitmapState.mBitmap;
    }

    public void setSource(Rect source) {
        this.mBitmapState.mSource = source;
    }

    public Rect getSource() {
        return this.mBitmapState.mSource;
    }

    public void setVerticalOffset(int offset) {
        this.mBitmapState.mOffset = offset;
        invalidateSelf();
    }

    public int getVerticalOffset() {
        return this.mBitmapState.mOffset;
    }

    public void draw(Canvas canvas) {
        if (this.mBitmapState.mBitmap != null) {
            Rect bounds = getBounds();
            this.mDest.left = 0;
            this.mDest.top = this.mBitmapState.mOffset;
            this.mDest.right = bounds.width();
            Rect source = validateSource();
            float scale = ((float) bounds.width()) / ((float) source.width());
            this.mDest.bottom = this.mDest.top + ((int) (((float) source.height()) * scale));
            int i = canvas.save();
            canvas.clipRect(bounds);
            canvas.drawBitmap(this.mBitmapState.mBitmap, source, this.mDest, this.mBitmapState.mPaint);
            canvas.restoreToCount(i);
        }
    }

    public void setAlpha(int alpha) {
        if (alpha != this.mBitmapState.mPaint.getAlpha()) {
            this.mBitmapState.mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return this.mBitmapState.mPaint.getAlpha();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mBitmapState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    public int getOpacity() {
        Bitmap bitmap = this.mBitmapState.mBitmap;
        return (bitmap == null || bitmap.hasAlpha() || this.mBitmapState.mPaint.getAlpha() < 255) ? -3 : -1;
    }

    private Rect validateSource() {
        if (this.mBitmapState.mSource == null) {
            return this.mBitmapState.mDefaultSource;
        }
        return this.mBitmapState.mSource;
    }

    static {
        if (VERSION.SDK_INT >= 24) {
            PROPERTY_VERTICAL_OFFSET = getVerticalOffsetIntProperty();
        } else {
            PROPERTY_VERTICAL_OFFSET = new Property<FitWidthBitmapDrawable, Integer>(Integer.class, "verticalOffset") {
                public void set(FitWidthBitmapDrawable object, Integer value) {
                    object.setVerticalOffset(value.intValue());
                }

                public Integer get(FitWidthBitmapDrawable object) {
                    return Integer.valueOf(object.getVerticalOffset());
                }
            };
        }
    }

    @RequiresApi(24)
    static IntProperty<FitWidthBitmapDrawable> getVerticalOffsetIntProperty() {
        return new IntProperty<FitWidthBitmapDrawable>("verticalOffset") {
            public void setValue(FitWidthBitmapDrawable fitWidthBitmapDrawable, int value) {
                fitWidthBitmapDrawable.setVerticalOffset(value);
            }

            public Integer get(FitWidthBitmapDrawable fitWidthBitmapDrawable) {
                return Integer.valueOf(fitWidthBitmapDrawable.getVerticalOffset());
            }
        };
    }
}
