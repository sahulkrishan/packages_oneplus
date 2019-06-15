package com.android.settingslib.graph;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.Path.Op;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import com.android.settingslib.R;
import com.android.settingslib.Utils;

public class SignalDrawable extends Drawable {
    private static final float CUT_OUT = 0.32916668f;
    private static final float CUT_WIDTH_DP = 0.083333336f;
    private static final float DIAG_OFFSET_MULTIPLIER = 0.707107f;
    private static final float DOT_CUT_HEIGHT = 0.16666667f;
    private static final float DOT_CUT_WIDTH = 0.5833334f;
    private static final long DOT_DELAY = 1000;
    private static final float DOT_PADDING = 0.041666668f;
    private static final float DOT_SIZE = 0.125f;
    private static final float[] FIT = new float[]{2.26f, -3.02f, 1.76f};
    private static final float INV_TAN = (1.0f / ((float) Math.tan(0.39269908169872414d)));
    private static final int LEVEL_MASK = 255;
    private static final int NUM_DOTS = 3;
    private static final int NUM_LEVEL_MASK = 65280;
    private static final int NUM_LEVEL_SHIFT = 8;
    private static final float PAD = 0.083333336f;
    private static final float RADIUS_RATIO = 0.04411765f;
    private static final int STATE_AIRPLANE = 4;
    private static final int STATE_CARRIER_CHANGE = 3;
    private static final int STATE_CUT = 2;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_MASK = 16711680;
    private static final int STATE_NONE = 0;
    private static final int STATE_SHIFT = 16;
    private static final String TAG = "SignalDrawable";
    private static final float VIEWPORT = 24.0f;
    private static float[][] X_PATH = new float[][]{new float[]{0.91249996f, 0.7083333f}, new float[]{-0.045833334f, -0.045833334f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{-0.079166666f, -0.079166666f}, new float[]{-0.045833334f, 0.045833334f}, new float[]{0.079166666f, 0.079166666f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{0.045833334f, 0.045833334f}, new float[]{0.079166666f, -0.079166666f}, new float[]{0.079166666f, 0.079166666f}, new float[]{0.045833334f, -0.045833334f}, new float[]{-0.079166666f, -0.079166666f}};
    private boolean mAnimating;
    private final float mAppliedCornerInset;
    private final Runnable mChangeDot = new Runnable() {
        public void run() {
            if (SignalDrawable.access$104(SignalDrawable.this) == 3) {
                SignalDrawable.this.mCurrentDot = 0;
            }
            SignalDrawable.this.invalidateSelf();
            SignalDrawable.this.mHandler.postDelayed(SignalDrawable.this.mChangeDot, 1000);
        }
    };
    private int mCurrentDot;
    private final Path mCutPath = new Path();
    private final int mDarkModeBackgroundColor;
    private final int mDarkModeFillColor;
    private final Paint mForegroundPaint = new Paint(1);
    private final Path mForegroundPath = new Path();
    private final Path mFullPath = new Path();
    private final Handler mHandler;
    private int mIntrinsicSize;
    private int mLevel;
    private final int mLightModeBackgroundColor;
    private final int mLightModeFillColor;
    private float mNumLevels = 1.0f;
    private float mOldDarkIntensity = -1.0f;
    private final Paint mPaint = new Paint(1);
    private final SlashArtist mSlash = new SlashArtist(this, null);
    private int mState;
    private final PointF mVirtualLeft = new PointF();
    private final PointF mVirtualTop = new PointF();
    private boolean mVisible;
    private final Path mXPath = new Path();

    private final class SlashArtist {
        private static final float BOTTOM = 1.1195517f;
        private static final float CENTER_X = 10.65f;
        private static final float CENTER_Y = 15.869239f;
        private static final float CORNER_RADIUS = 1.0f;
        private static final float LEFT = 0.40544835f;
        private static final float RIGHT = 0.4820516f;
        private static final float ROTATION = -45.0f;
        private static final float SCALE = 24.0f;
        private static final float SLASH_HEIGHT = 22.0f;
        private static final float SLASH_WIDTH = 1.8384776f;
        private static final float TOP = 0.20288496f;
        private final Path mPath;
        private final RectF mSlashRect;

        private SlashArtist() {
            this.mPath = new Path();
            this.mSlashRect = new RectF();
        }

        /* synthetic */ SlashArtist(SignalDrawable x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: 0000 */
        public void draw(int height, int width, Canvas canvas, Paint paint) {
            Matrix m = new Matrix();
            float radius = scale(1.0f, width);
            updateRect(scale(LEFT, width), scale(TOP, height), scale(RIGHT, width), scale(BOTTOM, height));
            this.mPath.reset();
            this.mPath.addRoundRect(this.mSlashRect, radius, radius, Direction.CW);
            m.setRotate(ROTATION, (float) (width / 2), (float) (height / 2));
            this.mPath.transform(m);
            canvas.drawPath(this.mPath, paint);
            m.setRotate(45.0f, (float) (width / 2), (float) (height / 2));
            this.mPath.transform(m);
            m.setTranslate(this.mSlashRect.width(), 0.0f);
            this.mPath.transform(m);
            this.mPath.addRoundRect(this.mSlashRect, radius, radius, Direction.CW);
            m.setRotate(ROTATION, (float) (width / 2), (float) (height / 2));
            this.mPath.transform(m);
            canvas.clipOutPath(this.mPath);
        }

        /* Access modifiers changed, original: 0000 */
        public void updateRect(float left, float top, float right, float bottom) {
            this.mSlashRect.left = left;
            this.mSlashRect.top = top;
            this.mSlashRect.right = right;
            this.mSlashRect.bottom = bottom;
        }

        private float scale(float frac, int width) {
            return ((float) width) * frac;
        }
    }

    static /* synthetic */ int access$104(SignalDrawable x0) {
        int i = x0.mCurrentDot + 1;
        x0.mCurrentDot = i;
        return i;
    }

    public SignalDrawable(Context context) {
        this.mDarkModeBackgroundColor = Utils.getDefaultColor(context, R.color.dark_mode_icon_color_dual_tone_background);
        this.mDarkModeFillColor = Utils.getDefaultColor(context, R.color.dark_mode_icon_color_dual_tone_fill);
        this.mLightModeBackgroundColor = Utils.getDefaultColor(context, R.color.light_mode_icon_color_dual_tone_background);
        this.mLightModeFillColor = Utils.getDefaultColor(context, R.color.light_mode_icon_color_dual_tone_fill);
        this.mIntrinsicSize = context.getResources().getDimensionPixelSize(R.dimen.signal_icon_size);
        this.mHandler = new Handler();
        setDarkIntensity(0.0f);
        this.mAppliedCornerInset = (float) context.getResources().getDimensionPixelSize(R.dimen.stat_sys_mobile_signal_circle_inset);
    }

    public void setIntrinsicSize(int size) {
        this.mIntrinsicSize = size;
    }

    public int getIntrinsicWidth() {
        return this.mIntrinsicSize;
    }

    public int getIntrinsicHeight() {
        return this.mIntrinsicSize;
    }

    public void setNumLevels(int levels) {
        if (((float) levels) != this.mNumLevels) {
            this.mNumLevels = (float) levels;
            invalidateSelf();
        }
    }

    private void setSignalState(int state) {
        if (state != this.mState) {
            this.mState = state;
            updateAnimation();
            invalidateSelf();
        }
    }

    private void updateAnimation() {
        boolean shouldAnimate = this.mState == 3 && this.mVisible;
        if (shouldAnimate != this.mAnimating) {
            this.mAnimating = shouldAnimate;
            if (shouldAnimate) {
                this.mChangeDot.run();
            } else {
                this.mHandler.removeCallbacks(this.mChangeDot);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean onLevelChange(int state) {
        setNumLevels(getNumLevels(state));
        setSignalState(getState(state));
        int level = getLevel(state);
        if (level != this.mLevel) {
            this.mLevel = level;
            invalidateSelf();
        }
        return true;
    }

    public void setColors(int background, int foreground) {
        this.mPaint.setColor(background);
        this.mForegroundPaint.setColor(foreground);
    }

    public void setDarkIntensity(float darkIntensity) {
        if (darkIntensity != this.mOldDarkIntensity) {
            this.mPaint.setColor(getBackgroundColor(darkIntensity));
            this.mForegroundPaint.setColor(getFillColor(darkIntensity));
            this.mOldDarkIntensity = darkIntensity;
            invalidateSelf();
        }
    }

    private int getFillColor(float darkIntensity) {
        return getColorForDarkIntensity(darkIntensity, this.mLightModeFillColor, this.mDarkModeFillColor);
    }

    private int getBackgroundColor(float darkIntensity) {
        return getColorForDarkIntensity(darkIntensity, this.mLightModeBackgroundColor, this.mDarkModeBackgroundColor);
    }

    private int getColorForDarkIntensity(float darkIntensity, int lightColor, int darkColor) {
        return ((Integer) ArgbEvaluator.getInstance().evaluate(darkIntensity, Integer.valueOf(lightColor), Integer.valueOf(darkColor))).intValue();
    }

    /* Access modifiers changed, original: protected */
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        int i;
        int i2;
        float padding;
        Canvas canvas2 = canvas;
        float width = (float) getBounds().width();
        float height = (float) getBounds().height();
        boolean isRtl = getLayoutDirection() == 1;
        if (isRtl) {
            canvas.save();
            canvas2.translate(width, 0.0f);
            canvas2.scale(-1.0f, 1.0f);
        }
        this.mFullPath.reset();
        this.mFullPath.setFillType(FillType.WINDING);
        float padding2 = (float) Math.round(0.083333336f * width);
        float cornerRadius = RADIUS_RATIO * height;
        float diagOffset = DIAG_OFFSET_MULTIPLIER * cornerRadius;
        this.mFullPath.moveTo(width - padding2, (height - padding2) - cornerRadius);
        this.mFullPath.lineTo(width - padding2, (padding2 + cornerRadius) + this.mAppliedCornerInset);
        this.mFullPath.arcTo((width - padding2) - (2.0f * cornerRadius), padding2 + this.mAppliedCornerInset, width - padding2, (this.mAppliedCornerInset + padding2) + (2.0f * cornerRadius), 0.0f, -135.0f, false);
        this.mFullPath.lineTo(((this.mAppliedCornerInset + padding2) + cornerRadius) - diagOffset, ((height - padding2) - cornerRadius) - diagOffset);
        this.mFullPath.arcTo(padding2 + this.mAppliedCornerInset, (height - padding2) - (2.0f * cornerRadius), (this.mAppliedCornerInset + padding2) + (2.0f * cornerRadius), height - padding2, -135.0f, -135.0f, false);
        this.mFullPath.lineTo((width - padding2) - cornerRadius, height - padding2);
        this.mFullPath.arcTo((width - padding2) - (2.0f * cornerRadius), (height - padding2) - (2.0f * cornerRadius), width - padding2, height - padding2, 90.0f, -90.0f, false);
        if (this.mState == 3) {
            float cutWidth = DOT_CUT_WIDTH * width;
            float cutHeight = DOT_CUT_HEIGHT * width;
            float dotSize = DOT_SIZE * height;
            float dotPadding = DOT_PADDING * height;
            this.mFullPath.moveTo(width - padding2, height - padding2);
            this.mFullPath.rLineTo(-cutWidth, 0.0f);
            this.mFullPath.rLineTo(0.0f, -cutHeight);
            this.mFullPath.rLineTo(cutWidth, 0.0f);
            this.mFullPath.rLineTo(0.0f, cutHeight);
            float dotSpacing = (dotPadding * 2.0f) + dotSize;
            float x = (width - padding2) - dotSize;
            float y = (height - padding2) - dotSize;
            this.mForegroundPath.reset();
            float f = y;
            i = 3;
            i2 = 2;
            float f2 = dotSize;
            padding = padding2;
            drawDot(this.mFullPath, this.mForegroundPath, x, f, f2, 2.8E-45f);
            drawDot(this.mFullPath, this.mForegroundPath, x - dotSpacing, f, f2, 1);
            drawDot(this.mFullPath, this.mForegroundPath, x - (dotSpacing * 2.0f), f, f2, 0);
        } else {
            i = 3;
            padding = padding2;
            i2 = 2;
            if (this.mState == 2) {
                float cut = CUT_OUT * width;
                this.mFullPath.moveTo(width - padding, height - padding);
                this.mFullPath.rLineTo(-cut, 0.0f);
                this.mFullPath.rLineTo(0.0f, -cut);
                this.mFullPath.rLineTo(cut, 0.0f);
                this.mFullPath.rLineTo(0.0f, cut);
            }
        }
        if (this.mState == 1) {
            this.mVirtualTop.set(width - padding, ((padding + cornerRadius) + this.mAppliedCornerInset) - (INV_TAN * cornerRadius));
            this.mVirtualLeft.set(((padding + cornerRadius) + this.mAppliedCornerInset) - (INV_TAN * cornerRadius), height - padding);
            float cutWidth2 = 0.083333336f * height;
            float cutDiagInset = INV_TAN * cutWidth2;
            this.mCutPath.reset();
            this.mCutPath.setFillType(FillType.WINDING);
            this.mCutPath.moveTo((width - padding) - cutWidth2, (height - padding) - cutWidth2);
            this.mCutPath.lineTo((width - padding) - cutWidth2, this.mVirtualTop.y + cutDiagInset);
            this.mCutPath.lineTo(this.mVirtualLeft.x + cutDiagInset, (height - padding) - cutWidth2);
            this.mCutPath.lineTo((width - padding) - cutWidth2, (height - padding) - cutWidth2);
            this.mForegroundPath.reset();
            this.mFullPath.op(this.mCutPath, Op.DIFFERENCE);
        } else if (this.mState == 4) {
            this.mForegroundPath.reset();
            this.mSlash.draw((int) height, (int) width, canvas2, this.mPaint);
        } else if (this.mState != i) {
            this.mForegroundPath.reset();
            this.mForegroundPath.addRect(padding, padding, padding + ((float) Math.round(calcFit(((float) this.mLevel) / (this.mNumLevels - 1.0f)) * (width - (2.0f * padding)))), height - padding, Direction.CW);
            this.mForegroundPath.op(this.mFullPath, Op.INTERSECT);
        }
        canvas2.drawPath(this.mFullPath, this.mPaint);
        canvas2.drawPath(this.mForegroundPath, this.mForegroundPaint);
        if (this.mState == i2) {
            this.mXPath.reset();
            this.mXPath.moveTo(X_PATH[0][0] * width, X_PATH[0][1] * height);
            for (int i3 = 1; i3 < X_PATH.length; i3++) {
                this.mXPath.rLineTo(X_PATH[i3][0] * width, X_PATH[i3][1] * height);
            }
            canvas2.drawPath(this.mXPath, this.mForegroundPaint);
        }
        if (isRtl) {
            canvas.restore();
        }
    }

    private void drawDot(Path fullPath, Path foregroundPath, float x, float y, float dotSize, int i) {
        (i == this.mCurrentDot ? foregroundPath : fullPath).addRect(x, y, x + dotSize, y + dotSize, Direction.CW);
    }

    private float calcFit(float v) {
        float ret = 0.0f;
        float t = v;
        for (float f : FIT) {
            ret += f * t;
            t *= v;
        }
        return ret;
    }

    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
        this.mForegroundPaint.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
        this.mForegroundPaint.setColorFilter(colorFilter);
    }

    public int getOpacity() {
        return 255;
    }

    public boolean setVisible(boolean visible, boolean restart) {
        this.mVisible = visible;
        updateAnimation();
        return super.setVisible(visible, restart);
    }

    public static int getLevel(int fullState) {
        return fullState & 255;
    }

    public static int getState(int fullState) {
        return (STATE_MASK & fullState) >> 16;
    }

    public static int getNumLevels(int fullState) {
        return (65280 & fullState) >> 8;
    }

    public static int getState(int level, int numLevels, boolean cutOut) {
        return (((cutOut ? 2 : 0) << 16) | (numLevels << 8)) | level;
    }

    public static int getCarrierChangeState(int numLevels) {
        return (numLevels << 8) | 196608;
    }

    public static int getEmptyState(int numLevels) {
        return (numLevels << 8) | 65536;
    }

    public static int getAirplaneModeState(int numLevels) {
        return (numLevels << 8) | 262144;
    }
}
