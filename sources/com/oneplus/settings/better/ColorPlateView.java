package com.oneplus.settings.better;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class ColorPlateView extends View {
    private static final int sDefaultHeight = 656;
    private static final int sDefaultWidth = 864;
    private Bitmap mBmpColorPlate;
    private OnColorChangeListener mColorChangeListener;
    private int mColorPlateMargin;
    private int mCurrentXProgress;
    private int mCurrentYProgress;
    private int mHeightSelectBox;
    private int mIntrinsicHeightColorPlate;
    private int mIntrinsicWidthColorPlate;
    int mLastXProgress;
    int mLastYProgress;
    private int mMaxXProgress = 100;
    private int mMaxYProgress = 100;
    private Paint mPaintColorPlate;
    private Paint mPaintSelectBox;
    private Rect mRectColorPlate = new Rect();
    private RectF mRectSelectBox = new RectF();
    private float mSelectBoxCornerRadius;
    private int mWidthSelectBox;

    public interface OnColorChangeListener {
        void colorChanged(int i, int i2, int i3, int i4);

        void onStartTrackingTouch(int i, int i2, int i3, int i4);

        void onStopTrackingTouch(int i, int i2, int i3, int i4);
    }

    public ColorPlateView(Context context) {
        super(context);
        init();
    }

    public ColorPlateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPlateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.mBmpColorPlate = drawableToBitmap(getResources().getDrawable(R.drawable.op_img_color_plate));
        this.mSelectBoxCornerRadius = getResources().getDimension(R.dimen.indicator_corner_radius);
        this.mIntrinsicWidthColorPlate = this.mBmpColorPlate.getWidth();
        this.mIntrinsicHeightColorPlate = this.mBmpColorPlate.getHeight();
        this.mWidthSelectBox = (int) getResources().getDimension(R.dimen.hue_indicator_width);
        this.mHeightSelectBox = this.mWidthSelectBox;
        this.mColorPlateMargin = (int) (((float) this.mWidthSelectBox) * 0.88f);
        this.mPaintColorPlate = new Paint();
        this.mPaintColorPlate.setAntiAlias(true);
        this.mPaintColorPlate.setFilterBitmap(true);
        this.mPaintSelectBox = new Paint();
        this.mPaintSelectBox.setColor(getResources().getColor(R.color.indicator_border_color));
        this.mPaintSelectBox.setStyle(Style.STROKE);
        this.mPaintSelectBox.setStrokeWidth(getResources().getDimension(R.dimen.indicator_border_width));
        this.mPaintSelectBox.setAntiAlias(true);
        this.mPaintSelectBox.setShadowLayer(getResources().getDimension(R.dimen.indicator_shadow_radius), 0.0f, 0.0f, -7829368);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(this.mBmpColorPlate, new Rect(0, 0, this.mBmpColorPlate.getWidth(), this.mBmpColorPlate.getHeight()), this.mRectColorPlate, this.mPaintColorPlate);
        canvas.drawRoundRect(this.mRectSelectBox, this.mSelectBoxCornerRadius, this.mSelectBoxCornerRadius, this.mPaintSelectBox);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = (this.mIntrinsicHeightColorPlate * width) / this.mIntrinsicWidthColorPlate;
        setMeasuredDimension(width, height);
        this.mRectColorPlate.left = this.mColorPlateMargin;
        this.mRectColorPlate.top = this.mColorPlateMargin;
        this.mRectColorPlate.right = width - this.mColorPlateMargin;
        this.mRectColorPlate.bottom = height - this.mColorPlateMargin;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x < this.mRectColorPlate.left) {
            x = this.mRectColorPlate.left;
        }
        if (x > this.mRectColorPlate.right) {
            x = this.mRectColorPlate.right;
        }
        if (y < this.mRectColorPlate.top) {
            y = this.mRectColorPlate.top;
        }
        if (y > this.mRectColorPlate.bottom) {
            y = this.mRectColorPlate.bottom;
        }
        this.mRectSelectBox.left = (float) (x - (this.mWidthSelectBox / 2));
        this.mRectSelectBox.top = (float) (y - (this.mHeightSelectBox / 2));
        this.mRectSelectBox.right = (float) ((this.mWidthSelectBox / 2) + x);
        this.mRectSelectBox.bottom = (float) ((this.mHeightSelectBox / 2) + y);
        invalidate();
        this.mLastXProgress = this.mCurrentXProgress;
        this.mLastYProgress = this.mCurrentYProgress;
        this.mCurrentXProgress = calculateXProgress(x);
        this.mCurrentYProgress = calculateYProgress(y);
        switch (event.getAction()) {
            case 0:
                if (this.mColorChangeListener != null) {
                    this.mColorChangeListener.onStartTrackingTouch(this.mCurrentXProgress, this.mMaxXProgress, this.mCurrentYProgress, this.mMaxYProgress);
                    break;
                }
                break;
            case 1:
                if (this.mColorChangeListener != null) {
                    this.mColorChangeListener.onStopTrackingTouch(this.mCurrentXProgress, this.mMaxXProgress, this.mCurrentYProgress, this.mMaxYProgress);
                    break;
                }
                break;
            case 2:
                if (this.mColorChangeListener != null && (Math.abs(this.mLastXProgress - this.mCurrentXProgress) >= 1 || Math.abs(this.mLastYProgress - this.mCurrentYProgress) >= 1)) {
                    this.mColorChangeListener.colorChanged(this.mCurrentXProgress, this.mMaxXProgress, this.mCurrentYProgress, this.mMaxYProgress);
                    break;
                }
        }
        return true;
    }

    private int calculateXProgress(int x) {
        return (int) ((((float) (x - this.mRectColorPlate.left)) / ((float) this.mRectColorPlate.width())) * ((float) this.mMaxXProgress));
    }

    private int calculateYProgress(int y) {
        return (int) ((((float) (y - this.mRectColorPlate.top)) / ((float) this.mRectColorPlate.height())) * ((float) this.mMaxYProgress));
    }

    private int measureWidth(int widthMeasureSpec) {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(sDefaultWidth, specSize);
        }
        if (specMode == 0) {
            return sDefaultWidth;
        }
        if (specMode != Ints.MAX_POWER_OF_TWO) {
            return sDefaultWidth;
        }
        return specSize;
    }

    private int measureHeight(int heightMeasureSpec) {
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(sDefaultHeight, specSize);
        }
        if (specMode == 0) {
            return sDefaultHeight;
        }
        if (specMode != Ints.MAX_POWER_OF_TWO) {
            return sDefaultHeight;
        }
        return specSize;
    }

    public OnColorChangeListener getColorChangeListener() {
        return this.mColorChangeListener;
    }

    public void setColorChangeListener(OnColorChangeListener colorChangedListener) {
        this.mColorChangeListener = colorChangedListener;
    }

    public void setProgress(final int xProgress, final int yProgress) {
        this.mCurrentXProgress = xProgress;
        this.mCurrentYProgress = yProgress;
        if (this.mRectColorPlate.width() <= 0) {
            postDelayed(new Runnable() {
                public void run() {
                    ColorPlateView.this.updateSelectBox(xProgress, yProgress);
                }
            }, 400);
        } else {
            updateSelectBox(xProgress, yProgress);
        }
    }

    public int[] getXYProgress() {
        return new int[]{this.mCurrentXProgress, this.mCurrentYProgress};
    }

    private void updateSelectBox(int xProgress, int yProgress) {
        int x = (int) (((((float) xProgress) / ((float) this.mMaxXProgress)) * ((float) this.mRectColorPlate.width())) + ((float) this.mRectColorPlate.left));
        int y = (int) (((((float) yProgress) / ((float) this.mMaxYProgress)) * ((float) this.mRectColorPlate.height())) + ((float) this.mRectColorPlate.top));
        this.mRectSelectBox.left = (float) (x - (this.mWidthSelectBox / 2));
        this.mRectSelectBox.top = (float) (y - (this.mHeightSelectBox / 2));
        this.mRectSelectBox.right = (float) ((this.mWidthSelectBox / 2) + x);
        this.mRectSelectBox.bottom = (float) ((this.mHeightSelectBox / 2) + y);
        postInvalidate();
    }

    public int getMaxXProgress() {
        return this.mMaxXProgress;
    }

    public void setMaxXProgress(int maxXProgress) {
        this.mMaxXProgress = maxXProgress;
    }

    public int getMaxYProgress() {
        return this.mMaxYProgress;
    }

    public void setMaxYProgress(int maxYProgress) {
        this.mMaxYProgress = maxYProgress;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
