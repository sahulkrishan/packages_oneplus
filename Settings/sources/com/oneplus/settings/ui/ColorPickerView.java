package com.oneplus.settings.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class ColorPickerView extends View {
    private static final boolean DEBUG = false;
    private static final boolean RESTRICT_MODE = false;
    private static final String TAG = "ColorPickerView";
    private RectF mDrawingRect;
    private float mHue;
    private Paint mHueIndicatorPaint;
    private float mHueIndicatorWidth;
    private float mHueInidcatorOffset;
    private Paint mHuePaint;
    private float mHuePanelHeight;
    private RectF mHueRect;
    private Shader mHueShader;
    private RectF mHueTouchRect;
    private float mIndicatorBlurRadius;
    private float mIndicatorBorderWidth;
    private int mIndicatorColor;
    private float mIndicatorCornerRadius;
    private OnColorChangedListener mListener;
    private float mMarginLeft;
    private float mMarginTop;
    private float mMinHeight;
    private float mMinWidth;
    private float mPadding;
    private float mPanelSpacing;
    private float mPanelWidth;
    private Paint mSVIndicatorPaint;
    private float mSVIndicatorWidth;
    private float mSVPanelHeight;
    private float mSat;
    private Paint mSatValPaint;
    private RectF mSatValRect;
    private RectF mSatValTouchRect;
    private Point mTouchPoint;
    private float mVal;
    private Shader mValShader;

    public interface OnColorChangedListener {
        void onColorChanged(int i);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTouchPoint = null;
        this.mHue = 360.0f;
        this.mSat = 0.0f;
        this.mVal = 0.0f;
        init();
    }

    private void init() {
        Resources res = getResources();
        this.mPadding = (this.mHueIndicatorWidth / 2.0f) + this.mIndicatorBorderWidth;
        this.mPanelWidth = res.getDimension(R.dimen.panel_view_width);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("panel width 375dp:");
        stringBuilder.append(this.mPanelWidth);
        Log.d(str, stringBuilder.toString());
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("device width pixels:");
        stringBuilder.append(getResources().getDisplayMetrics().widthPixels);
        Log.d(str, stringBuilder.toString());
        this.mPanelSpacing = res.getDimension(R.dimen.panel_spacing);
        this.mMarginTop = res.getDimension(R.dimen.margin_top2);
        this.mMarginLeft = res.getDimension(R.dimen.margin_left2);
        this.mHuePanelHeight = res.getDimension(R.dimen.hue_panel_height);
        this.mSVPanelHeight = res.getDimension(R.dimen.sat_val_panel_height);
        this.mMinWidth = this.mPanelWidth;
        this.mMinHeight = (this.mHuePanelHeight + this.mSVPanelHeight) + this.mPanelSpacing;
        this.mIndicatorCornerRadius = res.getDimension(R.dimen.indicator_corner_radius);
        this.mIndicatorBlurRadius = res.getDimension(R.dimen.indicator_shadow_radius);
        this.mIndicatorBorderWidth = res.getDimension(R.dimen.indicator_border_width);
        this.mIndicatorColor = res.getColor(R.color.indicator_border_color);
        this.mHueIndicatorWidth = res.getDimension(R.dimen.hue_indicator_width);
        this.mHueInidcatorOffset = res.getDimension(R.dimen.hue_indicator_offset);
        this.mSVIndicatorWidth = res.getDimension(R.dimen.sat_val_indicator_width);
        initPaintTools();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initPaintTools() {
        this.mSatValPaint = new Paint();
        this.mHuePaint = new Paint();
        this.mHueIndicatorPaint = new Paint();
        this.mHueIndicatorPaint.setColor(this.mIndicatorColor);
        this.mHueIndicatorPaint.setStyle(Style.STROKE);
        this.mHueIndicatorPaint.setStrokeWidth(this.mIndicatorBorderWidth);
        this.mHueIndicatorPaint.setAntiAlias(true);
        this.mHueIndicatorPaint.setShadowLayer(this.mIndicatorBlurRadius, 0.0f, 0.0f, -7829368);
        this.mSVIndicatorPaint = new Paint();
        this.mSVIndicatorPaint.setStyle(Style.STROKE);
        this.mSVIndicatorPaint.setColor(this.mIndicatorColor);
        this.mSVIndicatorPaint.setStrokeWidth(this.mIndicatorBorderWidth);
        this.mSVIndicatorPaint.setAntiAlias(true);
        this.mSVIndicatorPaint.setShadowLayer(this.mIndicatorBlurRadius, 0.0f, 0.0f, -7829368);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(isUnspecified(widthMode) ? (int) this.mMinWidth : MeasureSpec.getSize(widthMeasureSpec), isUnspecified(widthMode) ? (int) this.mMinHeight : MeasureSpec.getSize(heightMeasureSpec));
    }

    private static boolean isUnspecified(int mode) {
        return (mode == Ints.MAX_POWER_OF_TWO || mode == Integer.MIN_VALUE) ? false : true;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDrawingRect.width() > 0.0f && this.mDrawingRect.height() > 0.0f) {
            drawSatValPanel(canvas);
            drawHuePanel(canvas);
        }
    }

    private void drawSatValPanel(Canvas canvas) {
        this.mSatValPaint.setShader(generateSVShader());
        canvas.drawRect(this.mSatValRect, this.mSatValPaint);
        Point p = satValToPoint(this.mSat, this.mVal);
        RectF rect = new RectF();
        float halfWidth = this.mSVIndicatorWidth / 2.0f;
        rect.left = ((float) p.x) - halfWidth;
        rect.right = ((float) p.x) + halfWidth;
        rect.top = ((float) p.y) - halfWidth;
        rect.bottom = ((float) p.y) + halfWidth;
        canvas.drawRoundRect(rect, this.mIndicatorCornerRadius, this.mIndicatorCornerRadius, this.mSVIndicatorPaint);
    }

    private ComposeShader generateRestrictedSVShader() {
        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(this.mSatValRect.left, this.mSatValRect.top, this.mSatValRect.left, this.mSatValRect.bottom, -1, -11184811, TileMode.CLAMP);
        }
        return new ComposeShader(this.mValShader, new LinearGradient(this.mSatValRect.left, this.mSatValRect.top, this.mSatValRect.right, this.mSatValRect.top, -5592406, Color.HSVToColor(new float[]{this.mHue, 1065353216, 1065353216}), TileMode.CLAMP), Mode.MULTIPLY);
    }

    private ComposeShader generateSVShader() {
        if (this.mValShader == null) {
            this.mValShader = new LinearGradient(this.mSatValRect.left, this.mSatValRect.top, this.mSatValRect.left, this.mSatValRect.bottom, -1, ViewCompat.MEASURED_STATE_MASK, TileMode.CLAMP);
        }
        return new ComposeShader(this.mValShader, new LinearGradient(this.mSatValRect.left, this.mSatValRect.top, this.mSatValRect.right, this.mSatValRect.top, -1, Color.HSVToColor(new float[]{this.mHue, 1065353216, 1065353216}), TileMode.CLAMP), Mode.MULTIPLY);
    }

    private void drawHuePanel(Canvas canvas) {
        RectF rect = this.mHueRect;
        if (this.mHueShader == null) {
            int[] hue = new int[361];
            int count = 0;
            int i = 0;
            while (i <= hue.length - 1) {
                hue[count] = Color.HSVToColor(new float[]{(float) i, 1.0f, 1.0f});
                i++;
                count++;
            }
            this.mHueShader = new LinearGradient(rect.left, rect.top, rect.right, rect.top, hue, null, TileMode.CLAMP);
            this.mHuePaint.setShader(this.mHueShader);
        }
        canvas.drawRect(rect, this.mHuePaint);
        float halfHueIndicatorWidth = this.mHueIndicatorWidth / 2.0f;
        Point p = hueToPoint(this.mHue);
        RectF r = new RectF();
        r.left = ((float) p.x) - halfHueIndicatorWidth;
        r.right = ((float) p.x) + halfHueIndicatorWidth;
        r.top = rect.top - this.mHueInidcatorOffset;
        r.bottom = rect.bottom + this.mHueInidcatorOffset;
        canvas.drawRoundRect(r, this.mIndicatorCornerRadius, this.mIndicatorCornerRadius, this.mHueIndicatorPaint);
    }

    private Point hueToPoint(float hue) {
        RectF rect = this.mHueRect;
        float width = rect.width();
        Point p = new Point();
        p.y = (int) rect.top;
        p.x = (int) (rect.left + ((hue * width) / 360.0f));
        return p;
    }

    private Point satValToPoint(float sat, float val) {
        float height = this.mSatValRect.height();
        float width = this.mSatValRect.width();
        Point p = new Point();
        p.x = (int) ((sat * width) + this.mSatValRect.left);
        p.y = (int) (((1.0f - val) * height) + this.mSatValRect.top);
        return p;
    }

    private float[] pointToSatValRestrictedMode(float x, float y) {
        RectF rect = this.mSatValRect;
        float[] result = new float[2];
        float width = rect.width();
        float height = rect.height();
        if (x < rect.left) {
            x = 0.0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x -= rect.left;
        }
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        result[0] = (1.0f / width) * x;
        result[1] = 1.0f - ((1.0f / height) * y);
        return result;
    }

    private float[] pointToSatVal(float x, float y) {
        RectF rect = this.mSatValRect;
        float[] result = new float[2];
        float width = rect.width();
        float height = rect.height();
        if (x < rect.left) {
            x = 0.0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x -= rect.left;
        }
        if (y < rect.top) {
            y = 0.0f;
        } else if (y > rect.bottom) {
            y = height;
        } else {
            y -= rect.top;
        }
        result[0] = (1.0f / width) * x;
        result[1] = 1.0f - ((1.0f / height) * y);
        return result;
    }

    private float pointToHue(float x) {
        RectF rect = this.mHueRect;
        float width = rect.width();
        if (x < rect.left) {
            x = 0.0f;
        } else if (x > rect.right) {
            x = width;
        } else {
            x -= rect.left;
        }
        return (360.0f * x) / width;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean isUpdated = false;
        switch (event.getAction()) {
            case 0:
                this.mTouchPoint = new Point((int) event.getX(), (int) event.getY());
                isUpdated = updateIndicatorIfNeeded(event);
                break;
            case 1:
                this.mTouchPoint = null;
                isUpdated = updateIndicatorIfNeeded(event);
                break;
            case 2:
                isUpdated = updateIndicatorIfNeeded(event);
                break;
        }
        if (!isUpdated) {
            return super.onTouchEvent(event);
        }
        if (this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
        return true;
    }

    private boolean updateIndicatorIfNeeded(MotionEvent event) {
        if (this.mTouchPoint == null) {
            return false;
        }
        boolean update = false;
        int startX = this.mTouchPoint.x;
        int startY = this.mTouchPoint.y;
        if (this.mHueTouchRect.contains((float) startX, (float) startY)) {
            this.mHue = pointToHue(event.getX());
            update = true;
        } else if (this.mSatValTouchRect.contains((float) startX, (float) startY)) {
            float[] result = pointToSatVal(event.getX(), event.getY());
            this.mSat = result[0];
            this.mVal = result[1];
            update = true;
        }
        return update;
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = this.mPadding + ((float) getPaddingLeft());
        this.mDrawingRect.right = (((float) w) - this.mPadding) - ((float) getPaddingRight());
        this.mDrawingRect.top = this.mPadding + ((float) getPaddingTop());
        this.mDrawingRect.bottom = (((float) h) - this.mPadding) - ((float) getPaddingBottom());
        setupSatValRect();
        setupHueRect();
    }

    private void setupSatValRect() {
        RectF dRect = this.mDrawingRect;
        this.mSatValRect = new RectF(dRect.left, ((dRect.top + this.mHuePanelHeight) + this.mMarginTop) + this.mPanelSpacing, dRect.right, dRect.bottom - this.mMarginTop);
        this.mSatValTouchRect = new RectF();
        this.mSatValTouchRect.left = this.mSatValRect.left - (this.mSVIndicatorWidth / 2.0f);
        this.mSatValTouchRect.right = this.mSatValRect.right + (this.mSVIndicatorWidth / 2.0f);
        this.mSatValTouchRect.top = this.mSatValRect.top - (this.mSVIndicatorWidth / 4.0f);
        this.mSatValTouchRect.bottom = this.mSatValRect.bottom + (this.mSVIndicatorWidth / 4.0f);
    }

    private void setupHueRect() {
        RectF dRect = this.mDrawingRect;
        float left = dRect.left;
        float top = dRect.top;
        this.mHueRect = new RectF(left, top, dRect.right, this.mHuePanelHeight + top);
        this.mHueTouchRect = new RectF();
        this.mHueTouchRect.left = this.mHueRect.left - (this.mHueIndicatorWidth / 2.0f);
        this.mHueTouchRect.right = this.mHueRect.right + (this.mHueIndicatorWidth / 2.0f);
        this.mHueTouchRect.top = this.mHueRect.top - (this.mHueIndicatorWidth / 2.0f);
        this.mHueTouchRect.bottom = this.mHueRect.bottom + (this.mHueIndicatorWidth / 2.0f);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
        this.mListener.onColorChanged(Color.HSVToColor(new float[]{this.mHue, this.mSat, this.mVal}));
    }

    public int getColor() {
        return Color.HSVToColor(new float[]{this.mHue, this.mSat, this.mVal});
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean callback) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.mHue = hsv[0];
        this.mSat = hsv[1];
        this.mVal = hsv[2];
        if (callback && this.mListener != null) {
            this.mListener.onColorChanged(Color.HSVToColor(new float[]{this.mHue, this.mSat, this.mVal}));
        }
        invalidate();
    }
}
