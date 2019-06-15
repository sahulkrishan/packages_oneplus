package com.android.settingslib.graph;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.Path.Op;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import com.android.settingslib.R;
import com.android.settingslib.Utils;

public class BatteryMeterDrawableBase extends Drawable {
    private static final float ASPECT_RATIO = 0.58f;
    private static final float BOLT_LEVEL_THRESHOLD = 0.3f;
    private static final int FULL = 96;
    private static final float RADIUS_RATIO = 0.05882353f;
    private static final boolean SINGLE_DIGIT_PERCENT = false;
    public static final String TAG = BatteryMeterDrawableBase.class.getSimpleName();
    protected final Paint mBatteryPaint;
    private final RectF mBoltFrame = new RectF();
    protected final Paint mBoltPaint;
    private final Path mBoltPath = new Path();
    private final float[] mBoltPoints;
    private final RectF mButtonFrame = new RectF();
    protected float mButtonHeightFraction;
    private int mChargeColor;
    private boolean mCharging;
    private final int[] mColors;
    protected final Context mContext;
    private final int mCriticalLevel;
    private final RectF mFrame = new RectF();
    protected final Paint mFramePaint;
    private int mHeight;
    private int mIconTint = -1;
    private final int mIntrinsicHeight;
    private final int mIntrinsicWidth;
    private int mLevel = -1;
    private float mOldDarkIntensity = -1.0f;
    private final Path mOutlinePath = new Path();
    private final Rect mPadding = new Rect();
    private final RectF mPlusFrame = new RectF();
    protected final Paint mPlusPaint;
    private final Path mPlusPath = new Path();
    private final float[] mPlusPoints;
    protected boolean mPowerSaveAsColorError = true;
    private boolean mPowerSaveEnabled;
    protected final Paint mPowersavePaint;
    private final Path mShapePath = new Path();
    private boolean mShowPercent;
    private float mSubpixelSmoothingLeft;
    private float mSubpixelSmoothingRight;
    private float mTextHeight;
    protected final Paint mTextPaint;
    private final Path mTextPath = new Path();
    private String mWarningString;
    private float mWarningTextHeight;
    protected final Paint mWarningTextPaint;
    private int mWidth;

    public BatteryMeterDrawableBase(Context context, int frameColor) {
        this.mContext = context;
        Resources res = context.getResources();
        TypedArray levels = res.obtainTypedArray(R.array.batterymeter_color_levels);
        TypedArray colors = res.obtainTypedArray(R.array.batterymeter_color_values);
        int N = levels.length();
        this.mColors = new int[(2 * N)];
        for (int i = 0; i < N; i++) {
            this.mColors[2 * i] = levels.getInt(i, 0);
            if (colors.getType(i) == 2) {
                this.mColors[(2 * i) + 1] = Utils.getColorAttr(context, colors.getThemeAttributeId(i, 0));
            } else {
                this.mColors[(2 * i) + 1] = colors.getColor(i, 0);
            }
        }
        levels.recycle();
        colors.recycle();
        this.mWarningString = context.getString(R.string.battery_meter_very_low_overlay_symbol);
        this.mCriticalLevel = this.mContext.getResources().getInteger(17694758);
        this.mButtonHeightFraction = context.getResources().getFraction(R.fraction.battery_button_height_fraction, 1, 1);
        this.mSubpixelSmoothingLeft = context.getResources().getFraction(R.fraction.battery_subpixel_smoothing_left, 1, 1);
        this.mSubpixelSmoothingRight = context.getResources().getFraction(R.fraction.battery_subpixel_smoothing_right, 1, 1);
        this.mFramePaint = new Paint(1);
        this.mFramePaint.setColor(frameColor);
        this.mFramePaint.setDither(true);
        this.mFramePaint.setStrokeWidth(0.0f);
        this.mFramePaint.setStyle(Style.FILL_AND_STROKE);
        this.mBatteryPaint = new Paint(1);
        this.mBatteryPaint.setDither(true);
        this.mBatteryPaint.setStrokeWidth(0.0f);
        this.mBatteryPaint.setStyle(Style.FILL_AND_STROKE);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", 1));
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mWarningTextPaint = new Paint(1);
        this.mWarningTextPaint.setTypeface(Typeface.create("sans-serif", 1));
        this.mWarningTextPaint.setTextAlign(Align.CENTER);
        if (this.mColors.length > 1) {
            this.mWarningTextPaint.setColor(this.mColors[1]);
        }
        this.mChargeColor = Utils.getDefaultColor(this.mContext, R.color.meter_consumed_color);
        this.mBoltPaint = new Paint(1);
        this.mBoltPaint.setColor(Utils.getDefaultColor(this.mContext, R.color.batterymeter_bolt_color));
        this.mBoltPoints = loadPoints(res, R.array.batterymeter_bolt_points);
        this.mPlusPaint = new Paint(1);
        this.mPlusPaint.setColor(Utils.getDefaultColor(this.mContext, R.color.batterymeter_plus_color));
        this.mPlusPoints = loadPoints(res, R.array.batterymeter_plus_points);
        this.mPowersavePaint = new Paint(1);
        this.mPowersavePaint.setColor(this.mPlusPaint.getColor());
        this.mPowersavePaint.setStyle(Style.STROKE);
        this.mPowersavePaint.setStrokeWidth((float) context.getResources().getDimensionPixelSize(R.dimen.battery_powersave_outline_thickness));
        this.mIntrinsicWidth = context.getResources().getDimensionPixelSize(R.dimen.battery_width);
        this.mIntrinsicHeight = context.getResources().getDimensionPixelSize(R.dimen.battery_height);
    }

    public int getIntrinsicHeight() {
        return this.mIntrinsicHeight;
    }

    public int getIntrinsicWidth() {
        return this.mIntrinsicWidth;
    }

    public void setShowPercent(boolean show) {
        this.mShowPercent = show;
        postInvalidate();
    }

    public void setCharging(boolean val) {
        this.mCharging = val;
        postInvalidate();
    }

    public boolean getCharging() {
        return this.mCharging;
    }

    public void setBatteryLevel(int val) {
        this.mLevel = val;
        postInvalidate();
    }

    public int getBatteryLevel() {
        return this.mLevel;
    }

    public void setPowerSave(boolean val) {
        this.mPowerSaveEnabled = val;
        postInvalidate();
    }

    /* Access modifiers changed, original: protected */
    public void setPowerSaveAsColorError(boolean asError) {
        this.mPowerSaveAsColorError = asError;
    }

    /* Access modifiers changed, original: protected */
    public void postInvalidate() {
        unscheduleSelf(new -$$Lambda$BatteryMeterDrawableBase$ExJ0HHRzS2_LMtcBJqtFiovbn0w(this));
        scheduleSelf(new -$$Lambda$BatteryMeterDrawableBase$ExJ0HHRzS2_LMtcBJqtFiovbn0w(this), 0);
    }

    private static float[] loadPoints(Resources res, int pointArrayRes) {
        int[] pts = res.getIntArray(pointArrayRes);
        int i = 0;
        int maxY = 0;
        int maxX = 0;
        for (int i2 = 0; i2 < pts.length; i2 += 2) {
            maxX = Math.max(maxX, pts[i2]);
            maxY = Math.max(maxY, pts[i2 + 1]);
        }
        float[] ptsF = new float[pts.length];
        while (i < pts.length) {
            ptsF[i] = ((float) pts[i]) / ((float) maxX);
            ptsF[i + 1] = ((float) pts[i + 1]) / ((float) maxY);
            i += 2;
        }
        return ptsF;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        updateSize();
    }

    private void updateSize() {
        Rect bounds = getBounds();
        this.mHeight = (bounds.bottom - this.mPadding.bottom) - (bounds.top + this.mPadding.top);
        this.mWidth = (bounds.right - this.mPadding.right) - (bounds.left + this.mPadding.left);
        this.mWarningTextPaint.setTextSize(((float) this.mHeight) * 0.75f);
        this.mWarningTextHeight = -this.mWarningTextPaint.getFontMetrics().ascent;
    }

    public boolean getPadding(Rect padding) {
        if (this.mPadding.left == 0 && this.mPadding.top == 0 && this.mPadding.right == 0 && this.mPadding.bottom == 0) {
            return super.getPadding(padding);
        }
        padding.set(this.mPadding);
        return true;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        this.mPadding.left = left;
        this.mPadding.top = top;
        this.mPadding.right = right;
        this.mPadding.bottom = bottom;
        updateSize();
    }

    /* Access modifiers changed, original: protected */
    public int getColorForLevel(int percent) {
        int color = 0;
        int i = 0;
        while (i < this.mColors.length) {
            int thresh = this.mColors[i];
            color = this.mColors[i + 1];
            if (percent > thresh) {
                i += 2;
            } else if (i == this.mColors.length - 2) {
                return this.mIconTint;
            } else {
                return color;
            }
        }
        return color;
    }

    public void setColors(int fillColor, int backgroundColor) {
        this.mIconTint = fillColor;
        this.mFramePaint.setColor(backgroundColor);
        this.mBoltPaint.setColor(fillColor);
        this.mChargeColor = fillColor;
        invalidateSelf();
    }

    /* Access modifiers changed, original: protected */
    public int batteryColorForLevel(int level) {
        if (this.mCharging || (this.mPowerSaveEnabled && this.mPowerSaveAsColorError)) {
            return this.mChargeColor;
        }
        return getColorForLevel(level);
    }

    public void draw(Canvas c) {
        Canvas canvas = c;
        int level = this.mLevel;
        Rect bounds = getBounds();
        if (level != -1) {
            float levelTop;
            boolean z;
            float drawFrac = ((float) level) / 100.0f;
            int height = this.mHeight;
            int width = (int) (getAspectRatio() * ((float) this.mHeight));
            int px = (this.mWidth - width) / 2;
            int buttonHeight = Math.round(((float) height) * this.mButtonHeightFraction);
            int left = this.mPadding.left + bounds.left;
            int top = (bounds.bottom - this.mPadding.bottom) - height;
            this.mFrame.set((float) left, (float) top, (float) (width + left), (float) (height + top));
            this.mFrame.offset((float) px, 0.0f);
            this.mButtonFrame.set(this.mFrame.left + ((float) Math.round(((float) width) * 0.28f)), this.mFrame.top, this.mFrame.right - ((float) Math.round(((float) width) * 0.28f)), this.mFrame.top + ((float) buttonHeight));
            RectF rectF = this.mFrame;
            rectF.top += (float) buttonHeight;
            this.mBatteryPaint.setColor(batteryColorForLevel(level));
            if (level >= 96) {
                drawFrac = 1.0f;
            } else if (level <= this.mCriticalLevel) {
                drawFrac = 0.0f;
            }
            if (drawFrac == 1.0f) {
                levelTop = this.mButtonFrame.top;
            } else {
                levelTop = this.mFrame.top + (this.mFrame.height() * (1.0f - drawFrac));
            }
            this.mShapePath.reset();
            this.mOutlinePath.reset();
            float radius = getRadiusRatio() * (this.mFrame.height() + ((float) buttonHeight));
            this.mShapePath.setFillType(FillType.WINDING);
            this.mShapePath.addRoundRect(this.mFrame, radius, radius, Direction.CW);
            this.mShapePath.addRect(this.mButtonFrame, Direction.CW);
            this.mOutlinePath.addRoundRect(this.mFrame, radius, radius, Direction.CW);
            Path p = new Path();
            p.addRect(this.mButtonFrame, Direction.CW);
            this.mOutlinePath.op(p, Op.XOR);
            boolean z2 = true;
            int i;
            int i2;
            float f;
            if (this.mCharging) {
                float bl = (this.mFrame.left + (this.mFrame.width() / 4.0f)) + 1.0f;
                float bt = this.mFrame.top + (this.mFrame.height() / 6.0f);
                float br = (this.mFrame.right - (this.mFrame.width() / 4.0f)) + 1.0f;
                drawFrac = this.mFrame.bottom - (this.mFrame.height() / 10.0f);
                float f2;
                if (this.mBoltFrame.left == bl && this.mBoltFrame.top == bt && this.mBoltFrame.right == br && this.mBoltFrame.bottom == drawFrac) {
                    float f3 = br;
                    f2 = drawFrac;
                    i = px;
                    i2 = buttonHeight;
                    f = radius;
                    z = false;
                } else {
                    this.mBoltFrame.set(bl, bt, br, drawFrac);
                    this.mBoltPath.reset();
                    f2 = drawFrac;
                    this.mBoltPath.moveTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
                    int i3 = 2;
                    while (true) {
                        int i4 = i3;
                        if (i4 >= this.mBoltPoints.length) {
                            break;
                        }
                        f = radius;
                        this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[i4] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[i4 + 1] * this.mBoltFrame.height()));
                        i3 = i4 + 2;
                        radius = f;
                    }
                    z = false;
                    this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
                }
                if (Math.min(Math.max((this.mBoltFrame.bottom - levelTop) / (this.mBoltFrame.bottom - this.mBoltFrame.top), 0.0f), 1.0f) <= 0.3f) {
                    canvas.drawPath(this.mBoltPath, this.mBoltPaint);
                } else {
                    this.mShapePath.op(this.mBoltPath, Op.DIFFERENCE);
                }
            } else {
                int i5 = width;
                i = px;
                i2 = buttonHeight;
                f = radius;
                z = false;
            }
            boolean pctOpaque = false;
            drawFrac = 0.0f;
            float pctY = 0.0f;
            String pctText = null;
            if (!this.mCharging && !this.mPowerSaveEnabled && level > this.mCriticalLevel && this.mShowPercent) {
                this.mTextPaint.setColor(getColorForLevel(level));
                this.mTextPaint.setTextSize(((float) height) * (this.mLevel == 100 ? 0.38f : 0.5f));
                this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
                pctText = String.valueOf(level);
                drawFrac = (((float) this.mWidth) * 0.5f) + ((float) left);
                pctY = ((((float) this.mHeight) + this.mTextHeight) * 0.47f) + ((float) top);
                if (levelTop <= pctY) {
                    z2 = z;
                }
                pctOpaque = z2;
                if (!pctOpaque) {
                    this.mTextPath.reset();
                    this.mTextPaint.getTextPath(pctText, 0, pctText.length(), drawFrac, pctY, this.mTextPath);
                    this.mShapePath.op(this.mTextPath, Op.DIFFERENCE);
                }
            }
            canvas.drawPath(this.mShapePath, this.mFramePaint);
            this.mFrame.top = levelTop;
            c.save();
            canvas.clipRect(this.mFrame);
            canvas.drawPath(this.mShapePath, this.mBatteryPaint);
            c.restore();
            if (!(this.mCharging || this.mPowerSaveEnabled)) {
                if (level <= this.mCriticalLevel) {
                    canvas.drawText(this.mWarningString, (((float) this.mWidth) * 0.5f) + ((float) left), ((((float) this.mHeight) + this.mWarningTextHeight) * 0.48f) + ((float) top), this.mWarningTextPaint);
                } else if (pctOpaque) {
                    canvas.drawText(pctText, drawFrac, pctY, this.mTextPaint);
                }
            }
            if (!this.mCharging && this.mPowerSaveEnabled && this.mPowerSaveAsColorError) {
                canvas.drawPath(this.mOutlinePath, this.mPowersavePaint);
            }
        }
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mFramePaint.setColorFilter(colorFilter);
        this.mBatteryPaint.setColorFilter(colorFilter);
        this.mWarningTextPaint.setColorFilter(colorFilter);
        this.mBoltPaint.setColorFilter(colorFilter);
        this.mPlusPaint.setColorFilter(colorFilter);
    }

    public int getOpacity() {
        return 0;
    }

    public int getCriticalLevel() {
        return this.mCriticalLevel;
    }

    /* Access modifiers changed, original: protected */
    public float getAspectRatio() {
        return ASPECT_RATIO;
    }

    /* Access modifiers changed, original: protected */
    public float getRadiusRatio() {
        return RADIUS_RATIO;
    }

    public int getChargeColor() {
        return this.mChargeColor;
    }
}
