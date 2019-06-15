package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.support.v17.leanback.R;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

public class HorizontalGridView extends BaseGridView {
    private boolean mFadingHighEdge;
    private boolean mFadingLowEdge;
    private LinearGradient mHighFadeShader;
    private int mHighFadeShaderLength;
    private int mHighFadeShaderOffset;
    private LinearGradient mLowFadeShader;
    private int mLowFadeShaderLength;
    private int mLowFadeShaderOffset;
    private Bitmap mTempBitmapHigh;
    private Bitmap mTempBitmapLow;
    private Paint mTempPaint;
    private Rect mTempRect;

    public HorizontalGridView(Context context) {
        this(context, null);
    }

    public HorizontalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTempPaint = new Paint();
        this.mTempRect = new Rect();
        this.mLayoutManager.setOrientation(0);
        initAttributes(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void initAttributes(Context context, AttributeSet attrs) {
        initBaseGridViewAttributes(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.lbHorizontalGridView);
        setRowHeight(a);
        setNumRows(a.getInt(R.styleable.lbHorizontalGridView_numberOfRows, 1));
        a.recycle();
        updateLayerType();
        this.mTempPaint = new Paint();
        this.mTempPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }

    /* Access modifiers changed, original: 0000 */
    public void setRowHeight(TypedArray array) {
        if (array.peekValue(R.styleable.lbHorizontalGridView_rowHeight) != null) {
            setRowHeight(array.getLayoutDimension(R.styleable.lbHorizontalGridView_rowHeight, 0));
        }
    }

    public void setNumRows(int numRows) {
        this.mLayoutManager.setNumRows(numRows);
        requestLayout();
    }

    public void setRowHeight(int height) {
        this.mLayoutManager.setRowHeight(height);
        requestLayout();
    }

    public final void setFadingLeftEdge(boolean fading) {
        if (this.mFadingLowEdge != fading) {
            this.mFadingLowEdge = fading;
            if (!this.mFadingLowEdge) {
                this.mTempBitmapLow = null;
            }
            invalidate();
            updateLayerType();
        }
    }

    public final boolean getFadingLeftEdge() {
        return this.mFadingLowEdge;
    }

    public final void setFadingLeftEdgeLength(int fadeLength) {
        if (this.mLowFadeShaderLength != fadeLength) {
            this.mLowFadeShaderLength = fadeLength;
            if (this.mLowFadeShaderLength != 0) {
                this.mLowFadeShader = new LinearGradient(0.0f, 0.0f, (float) this.mLowFadeShaderLength, 0.0f, 0, ViewCompat.MEASURED_STATE_MASK, TileMode.CLAMP);
            } else {
                this.mLowFadeShader = null;
            }
            invalidate();
        }
    }

    public final int getFadingLeftEdgeLength() {
        return this.mLowFadeShaderLength;
    }

    public final void setFadingLeftEdgeOffset(int fadeOffset) {
        if (this.mLowFadeShaderOffset != fadeOffset) {
            this.mLowFadeShaderOffset = fadeOffset;
            invalidate();
        }
    }

    public final int getFadingLeftEdgeOffset() {
        return this.mLowFadeShaderOffset;
    }

    public final void setFadingRightEdge(boolean fading) {
        if (this.mFadingHighEdge != fading) {
            this.mFadingHighEdge = fading;
            if (!this.mFadingHighEdge) {
                this.mTempBitmapHigh = null;
            }
            invalidate();
            updateLayerType();
        }
    }

    public final boolean getFadingRightEdge() {
        return this.mFadingHighEdge;
    }

    public final void setFadingRightEdgeLength(int fadeLength) {
        if (this.mHighFadeShaderLength != fadeLength) {
            this.mHighFadeShaderLength = fadeLength;
            if (this.mHighFadeShaderLength != 0) {
                this.mHighFadeShader = new LinearGradient(0.0f, 0.0f, (float) this.mHighFadeShaderLength, 0.0f, ViewCompat.MEASURED_STATE_MASK, 0, TileMode.CLAMP);
            } else {
                this.mHighFadeShader = null;
            }
            invalidate();
        }
    }

    public final int getFadingRightEdgeLength() {
        return this.mHighFadeShaderLength;
    }

    public final void setFadingRightEdgeOffset(int fadeOffset) {
        if (this.mHighFadeShaderOffset != fadeOffset) {
            this.mHighFadeShaderOffset = fadeOffset;
            invalidate();
        }
    }

    public final int getFadingRightEdgeOffset() {
        return this.mHighFadeShaderOffset;
    }

    private boolean needsFadingLowEdge() {
        if (!this.mFadingLowEdge) {
            return false;
        }
        int c = getChildCount();
        for (int i = 0; i < c; i++) {
            if (this.mLayoutManager.getOpticalLeft(getChildAt(i)) < getPaddingLeft() - this.mLowFadeShaderOffset) {
                return true;
            }
        }
        return false;
    }

    private boolean needsFadingHighEdge() {
        if (!this.mFadingHighEdge) {
            return false;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (this.mLayoutManager.getOpticalRight(getChildAt(i)) > (getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset) {
                return true;
            }
        }
        return false;
    }

    private Bitmap getTempBitmapLow() {
        if (!(this.mTempBitmapLow != null && this.mTempBitmapLow.getWidth() == this.mLowFadeShaderLength && this.mTempBitmapLow.getHeight() == getHeight())) {
            this.mTempBitmapLow = Bitmap.createBitmap(this.mLowFadeShaderLength, getHeight(), Config.ARGB_8888);
        }
        return this.mTempBitmapLow;
    }

    private Bitmap getTempBitmapHigh() {
        if (!(this.mTempBitmapHigh != null && this.mTempBitmapHigh.getWidth() == this.mHighFadeShaderLength && this.mTempBitmapHigh.getHeight() == getHeight())) {
            this.mTempBitmapHigh = Bitmap.createBitmap(this.mHighFadeShaderLength, getHeight(), Config.ARGB_8888);
        }
        return this.mTempBitmapHigh;
    }

    public void draw(Canvas canvas) {
        Canvas canvas2 = canvas;
        boolean needsFadingLow = needsFadingLowEdge();
        boolean needsFadingHigh = needsFadingHighEdge();
        if (!needsFadingLow) {
            this.mTempBitmapLow = null;
        }
        if (!needsFadingHigh) {
            this.mTempBitmapHigh = null;
        }
        if (needsFadingLow || needsFadingHigh) {
            int highEdge;
            int lowEdge = this.mFadingLowEdge ? (getPaddingLeft() - this.mLowFadeShaderOffset) - this.mLowFadeShaderLength : 0;
            if (this.mFadingHighEdge) {
                highEdge = ((getWidth() - getPaddingRight()) + this.mHighFadeShaderOffset) + this.mHighFadeShaderLength;
            } else {
                highEdge = getWidth();
            }
            int save = canvas.save();
            canvas2.clipRect((this.mFadingLowEdge ? this.mLowFadeShaderLength : 0) + lowEdge, 0, highEdge - (this.mFadingHighEdge ? this.mHighFadeShaderLength : 0), getHeight());
            super.draw(canvas);
            canvas2.restoreToCount(save);
            Canvas tmpCanvas = new Canvas();
            this.mTempRect.top = 0;
            this.mTempRect.bottom = getHeight();
            float f = 0.0f;
            if (needsFadingLow && this.mLowFadeShaderLength > 0) {
                Bitmap tempBitmap = getTempBitmapLow();
                tempBitmap.eraseColor(0);
                tmpCanvas.setBitmap(tempBitmap);
                int tmpSave = tmpCanvas.save();
                tmpCanvas.clipRect(0, 0, this.mLowFadeShaderLength, getHeight());
                tmpCanvas.translate((float) (-lowEdge), 0.0f);
                super.draw(tmpCanvas);
                tmpCanvas.restoreToCount(tmpSave);
                this.mTempPaint.setShader(this.mLowFadeShader);
                Bitmap tempBitmap2 = tempBitmap;
                tmpCanvas.drawRect(0.0f, 0.0f, (float) this.mLowFadeShaderLength, (float) getHeight(), this.mTempPaint);
                this.mTempRect.left = 0;
                this.mTempRect.right = this.mLowFadeShaderLength;
                f = 0.0f;
                canvas2.translate((float) lowEdge, 0.0f);
                canvas2.drawBitmap(tempBitmap2, this.mTempRect, this.mTempRect, null);
                canvas2.translate((float) (-lowEdge), 0.0f);
            }
            if (needsFadingHigh && this.mHighFadeShaderLength > 0) {
                Bitmap tempBitmap3 = getTempBitmapHigh();
                tempBitmap3.eraseColor(0);
                tmpCanvas.setBitmap(tempBitmap3);
                int tmpSave2 = tmpCanvas.save();
                tmpCanvas.clipRect(0, 0, this.mHighFadeShaderLength, getHeight());
                tmpCanvas.translate((float) (-(highEdge - this.mHighFadeShaderLength)), f);
                super.draw(tmpCanvas);
                tmpCanvas.restoreToCount(tmpSave2);
                this.mTempPaint.setShader(this.mHighFadeShader);
                tmpCanvas.drawRect(0.0f, 0.0f, (float) this.mHighFadeShaderLength, (float) getHeight(), this.mTempPaint);
                this.mTempRect.left = 0;
                this.mTempRect.right = this.mHighFadeShaderLength;
                canvas2.translate((float) (highEdge - this.mHighFadeShaderLength), 0.0f);
                canvas2.drawBitmap(tempBitmap3, this.mTempRect, this.mTempRect, null);
                canvas2.translate((float) (-(highEdge - this.mHighFadeShaderLength)), 0.0f);
            }
            return;
        }
        super.draw(canvas);
    }

    private void updateLayerType() {
        if (this.mFadingLowEdge || this.mFadingHighEdge) {
            setLayerType(2, null);
            setWillNotDraw(false);
            return;
        }
        setLayerType(0, null);
        setWillNotDraw(true);
    }
}
