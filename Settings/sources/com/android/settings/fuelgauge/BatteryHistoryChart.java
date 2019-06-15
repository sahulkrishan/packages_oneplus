package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.BatteryStats;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.android.internal.R;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class BatteryHistoryChart extends View {
    static final int CHART_DATA_BIN_MASK = -65536;
    static final int CHART_DATA_BIN_SHIFT = 16;
    static final int CHART_DATA_X_MASK = 65535;
    static final boolean DEBUG = false;
    static final int MONOSPACE = 3;
    static final int NUM_PHONE_SIGNALS = 7;
    static final int SANS = 1;
    static final int SERIF = 2;
    static final String TAG = "BatteryHistoryChart";
    final Path mBatCriticalPath = new Path();
    final Path mBatGoodPath = new Path();
    int mBatHigh;
    final Path mBatLevelPath = new Path();
    int mBatLow;
    final Path mBatWarnPath = new Path();
    final Paint mBatteryBackgroundPaint = new Paint(1);
    Intent mBatteryBroadcast;
    int mBatteryCriticalLevel = this.mContext.getResources().getInteger(17694758);
    final Paint mBatteryCriticalPaint = new Paint(1);
    final Paint mBatteryGoodPaint = new Paint(1);
    int mBatteryWarnLevel = this.mContext.getResources().getInteger(17694806);
    final Paint mBatteryWarnPaint = new Paint(1);
    Bitmap mBitmap;
    String mCameraOnLabel;
    int mCameraOnOffset;
    final Paint mCameraOnPaint = new Paint();
    final Path mCameraOnPath = new Path();
    Canvas mCanvas;
    String mChargeDurationString;
    int mChargeDurationStringWidth;
    int mChargeLabelStringWidth;
    String mChargingLabel;
    int mChargingOffset;
    final Paint mChargingPaint = new Paint();
    final Path mChargingPath = new Path();
    int mChartMinHeight;
    String mCpuRunningLabel;
    int mCpuRunningOffset;
    final Paint mCpuRunningPaint = new Paint();
    final Path mCpuRunningPath = new Path();
    final ArrayList<DateLabel> mDateLabels = new ArrayList();
    final Paint mDateLinePaint = new Paint();
    final Path mDateLinePath = new Path();
    final Paint mDebugRectPaint = new Paint();
    String mDrainString;
    int mDrainStringWidth;
    String mDurationString;
    int mDurationStringWidth;
    long mEndDataWallTime;
    long mEndWallTime;
    String mFlashlightOnLabel;
    int mFlashlightOnOffset;
    final Paint mFlashlightOnPaint = new Paint();
    final Path mFlashlightOnPath = new Path();
    String mGpsOnLabel;
    int mGpsOnOffset;
    final Paint mGpsOnPaint = new Paint();
    final Path mGpsOnPath = new Path();
    boolean mHaveCamera;
    boolean mHaveFlashlight;
    boolean mHaveGps;
    boolean mHavePhoneSignal;
    boolean mHaveWifi;
    int mHeaderHeight;
    int mHeaderTextAscent;
    int mHeaderTextDescent;
    final TextPaint mHeaderTextPaint = new TextPaint(1);
    long mHistDataEnd;
    long mHistEnd;
    long mHistStart;
    BatteryInfo mInfo;
    boolean mLargeMode;
    int mLastHeight = -1;
    int mLastWidth = -1;
    int mLevelBottom;
    int mLevelLeft;
    int mLevelOffset;
    int mLevelRight;
    int mLevelTop;
    int mLineWidth;
    String mMaxPercentLabelString;
    int mMaxPercentLabelStringWidth;
    String mMinPercentLabelString;
    int mMinPercentLabelStringWidth;
    int mNumHist;
    final ChartData mPhoneSignalChart = new ChartData();
    String mPhoneSignalLabel;
    int mPhoneSignalOffset;
    String mScreenOnLabel;
    int mScreenOnOffset;
    final Paint mScreenOnPaint = new Paint();
    final Path mScreenOnPath = new Path();
    long mStartWallTime;
    BatteryStats mStats;
    long mStatsPeriod;
    int mTextAscent;
    int mTextDescent;
    final TextPaint mTextPaint = new TextPaint(1);
    int mThinLineWidth = ((int) TypedValue.applyDimension(1, 2.0f, getResources().getDisplayMetrics()));
    final ArrayList<TimeLabel> mTimeLabels = new ArrayList();
    final Paint mTimeRemainPaint = new Paint(1);
    final Path mTimeRemainPath = new Path();
    String mWifiRunningLabel;
    int mWifiRunningOffset;
    final Paint mWifiRunningPaint = new Paint();
    final Path mWifiRunningPath = new Path();

    static class ChartData {
        int[] mColors;
        int mLastBin;
        int mNumTicks;
        Paint[] mPaints;
        int[] mTicks;

        ChartData() {
        }

        /* Access modifiers changed, original: 0000 */
        public void setColors(int[] colors) {
            this.mColors = colors;
            this.mPaints = new Paint[colors.length];
            for (int i = 0; i < colors.length; i++) {
                this.mPaints[i] = new Paint();
                this.mPaints[i].setColor(colors[i]);
                this.mPaints[i].setStyle(Style.FILL);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void init(int width) {
            if (width > 0) {
                this.mTicks = new int[(width * 2)];
            } else {
                this.mTicks = null;
            }
            this.mNumTicks = 0;
            this.mLastBin = 0;
        }

        /* Access modifiers changed, original: 0000 */
        public void addTick(int x, int bin) {
            if (bin != this.mLastBin && this.mNumTicks < this.mTicks.length) {
                this.mTicks[this.mNumTicks] = (65535 & x) | (bin << 16);
                this.mNumTicks++;
                this.mLastBin = bin;
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void finish(int width) {
            if (this.mLastBin != 0) {
                addTick(width, 0);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void draw(Canvas canvas, int top, int height) {
            int i = top;
            int lastBin = 0;
            int lastX = 0;
            int bottom = i + height;
            for (int i2 = 0; i2 < this.mNumTicks; i2++) {
                int tick = this.mTicks[i2];
                int x = 65535 & tick;
                int bin = (-65536 & tick) >> 16;
                if (lastBin != 0) {
                    canvas.drawRect((float) lastX, (float) i, (float) x, (float) bottom, this.mPaints[lastBin]);
                }
                lastBin = bin;
                lastX = x;
            }
        }
    }

    static class DateLabel {
        final String label;
        final int width;
        final int x;

        DateLabel(TextPaint paint, int x, Calendar cal, boolean dayFirst) {
            this.x = x;
            this.label = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), dayFirst ? "dM" : "Md"), cal).toString();
            this.width = (int) paint.measureText(this.label);
        }
    }

    static class TextAttrs {
        int styleIndex = -1;
        ColorStateList textColor = null;
        int textSize = 15;
        int typefaceIndex = -1;

        TextAttrs() {
        }

        /* Access modifiers changed, original: 0000 */
        public void retrieve(Context context, TypedArray from, int index) {
            TypedArray appearance = null;
            int ap = from.getResourceId(index, -1);
            if (ap != -1) {
                appearance = context.obtainStyledAttributes(ap, R.styleable.TextAppearance);
            }
            if (appearance != null) {
                int n = appearance.getIndexCount();
                for (int i = 0; i < n; i++) {
                    int attr = appearance.getIndex(i);
                    switch (attr) {
                        case 0:
                            this.textSize = appearance.getDimensionPixelSize(attr, this.textSize);
                            break;
                        case 1:
                            this.typefaceIndex = appearance.getInt(attr, -1);
                            break;
                        case 2:
                            this.styleIndex = appearance.getInt(attr, -1);
                            break;
                        case 3:
                            this.textColor = appearance.getColorStateList(attr);
                            break;
                        default:
                            break;
                    }
                }
                appearance.recycle();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void apply(Context context, TextPaint paint) {
            paint.density = context.getResources().getDisplayMetrics().density;
            paint.setCompatibilityScaling(context.getResources().getCompatibilityInfo().applicationScale);
            paint.setColor(this.textColor.getDefaultColor());
            paint.setTextSize((float) this.textSize);
            Typeface tf = null;
            switch (this.typefaceIndex) {
                case 1:
                    tf = Typeface.SANS_SERIF;
                    break;
                case 2:
                    tf = Typeface.SERIF;
                    break;
                case 3:
                    tf = Typeface.MONOSPACE;
                    break;
            }
            setTypeface(paint, tf, this.styleIndex);
        }

        public void setTypeface(TextPaint paint, Typeface tf, int style) {
            float f = 0.0f;
            boolean z = false;
            if (style > 0) {
                if (tf == null) {
                    tf = Typeface.defaultFromStyle(style);
                } else {
                    tf = Typeface.create(tf, style);
                }
                paint.setTypeface(tf);
                int need = (~(tf != null ? tf.getStyle() : 0)) & style;
                if ((need & 1) != 0) {
                    z = true;
                }
                paint.setFakeBoldText(z);
                if ((need & 2) != 0) {
                    f = -0.25f;
                }
                paint.setTextSkewX(f);
                return;
            }
            paint.setFakeBoldText(false);
            paint.setTextSkewX(0.0f);
            paint.setTypeface(tf);
        }
    }

    static class TimeLabel {
        final String label;
        final int width;
        final int x;

        TimeLabel(TextPaint paint, int x, Calendar cal, boolean use24hr) {
            this.x = x;
            this.label = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), use24hr ? "km" : "ha"), cal).toString();
            this.width = (int) paint.measureText(this.label);
        }
    }

    public BatteryHistoryChart(Context context, AttributeSet attrs) {
        Context context2 = context;
        super(context, attrs);
        int accentColor = Utils.getColorAccent(this.mContext);
        this.mBatteryBackgroundPaint.setColor(accentColor);
        this.mBatteryBackgroundPaint.setStyle(Style.FILL);
        this.mBatteryGoodPaint.setARGB(128, 0, 128, 0);
        this.mBatteryGoodPaint.setStyle(Style.STROKE);
        this.mBatteryWarnPaint.setARGB(128, 128, 128, 0);
        this.mBatteryWarnPaint.setStyle(Style.STROKE);
        this.mBatteryCriticalPaint.setARGB(192, 128, 0, 0);
        this.mBatteryCriticalPaint.setStyle(Style.STROKE);
        this.mTimeRemainPaint.setColor(-3221573);
        this.mTimeRemainPaint.setStyle(Style.FILL);
        this.mChargingPaint.setStyle(Style.STROKE);
        this.mScreenOnPaint.setStyle(Style.STROKE);
        this.mGpsOnPaint.setStyle(Style.STROKE);
        this.mCameraOnPaint.setStyle(Style.STROKE);
        this.mFlashlightOnPaint.setStyle(Style.STROKE);
        this.mWifiRunningPaint.setStyle(Style.STROKE);
        this.mCpuRunningPaint.setStyle(Style.STROKE);
        this.mPhoneSignalChart.setColors(com.android.settings.Utils.BADNESS_COLORS);
        this.mDebugRectPaint.setARGB(255, 255, 0, 0);
        this.mDebugRectPaint.setStyle(Style.STROKE);
        this.mScreenOnPaint.setColor(accentColor);
        this.mGpsOnPaint.setColor(accentColor);
        this.mCameraOnPaint.setColor(accentColor);
        this.mFlashlightOnPaint.setColor(accentColor);
        this.mWifiRunningPaint.setColor(accentColor);
        this.mCpuRunningPaint.setColor(accentColor);
        this.mChargingPaint.setColor(accentColor);
        TypedArray a = context2.obtainStyledAttributes(attrs, com.android.settings.R.styleable.BatteryHistoryChart, 0, 0);
        TextAttrs mainTextAttrs = new TextAttrs();
        TextAttrs headTextAttrs = new TextAttrs();
        mainTextAttrs.retrieve(context2, a, 0);
        headTextAttrs.retrieve(context2, a, 12);
        int n = a.getIndexCount();
        float r = 0.0f;
        float dy = 0.0f;
        float dx = 0.0f;
        int shadowcolor = 0;
        int i = 0;
        while (i < n) {
            int accentColor2;
            int attr = a.getIndex(i);
            switch (attr) {
                case 1:
                    accentColor2 = accentColor;
                    mainTextAttrs.textSize = a.getDimensionPixelSize(attr, mainTextAttrs.textSize);
                    headTextAttrs.textSize = a.getDimensionPixelSize(attr, headTextAttrs.textSize);
                    break;
                case 2:
                    accentColor2 = accentColor;
                    mainTextAttrs.typefaceIndex = a.getInt(attr, mainTextAttrs.typefaceIndex);
                    headTextAttrs.typefaceIndex = a.getInt(attr, headTextAttrs.typefaceIndex);
                    break;
                case 3:
                    accentColor2 = accentColor;
                    mainTextAttrs.styleIndex = a.getInt(attr, mainTextAttrs.styleIndex);
                    headTextAttrs.styleIndex = a.getInt(attr, headTextAttrs.styleIndex);
                    break;
                case 4:
                    accentColor2 = accentColor;
                    mainTextAttrs.textColor = a.getColorStateList(attr);
                    headTextAttrs.textColor = a.getColorStateList(attr);
                    break;
                case 5:
                    accentColor2 = accentColor;
                    shadowcolor = a.getInt(attr, 0);
                    break;
                case 6:
                    accentColor2 = accentColor;
                    dx = a.getFloat(attr, 0.0f);
                    break;
                case 7:
                    accentColor2 = accentColor;
                    dy = a.getFloat(attr, 0.0f);
                    break;
                case 8:
                    accentColor2 = accentColor;
                    r = a.getFloat(attr, 0.0f);
                    break;
                case 9:
                    accentColor2 = accentColor;
                    this.mTimeRemainPaint.setColor(a.getInt(attr, 0));
                    break;
                case 10:
                    accentColor2 = accentColor;
                    this.mBatteryBackgroundPaint.setColor(a.getInt(attr, 0));
                    this.mScreenOnPaint.setColor(a.getInt(attr, 0));
                    this.mGpsOnPaint.setColor(a.getInt(attr, 0));
                    this.mCameraOnPaint.setColor(a.getInt(attr, 0));
                    this.mFlashlightOnPaint.setColor(a.getInt(attr, 0));
                    this.mWifiRunningPaint.setColor(a.getInt(attr, 0));
                    this.mCpuRunningPaint.setColor(a.getInt(attr, 0));
                    this.mChargingPaint.setColor(a.getInt(attr, 0));
                    break;
                case 11:
                    this.mChartMinHeight = a.getDimensionPixelSize(attr, 0);
                    accentColor2 = accentColor;
                    break;
                default:
                    accentColor2 = accentColor;
                    break;
            }
            i++;
            accentColor = accentColor2;
        }
        a.recycle();
        mainTextAttrs.apply(context2, this.mTextPaint);
        headTextAttrs.apply(context2, this.mHeaderTextPaint);
        this.mDateLinePaint.set(this.mTextPaint);
        this.mDateLinePaint.setStyle(Style.STROKE);
        accentColor = this.mThinLineWidth / 2;
        if (accentColor < 1) {
            accentColor = 1;
        }
        this.mDateLinePaint.setStrokeWidth((float) accentColor);
        this.mDateLinePaint.setPathEffect(new DashPathEffect(new float[]{(float) (this.mThinLineWidth * 2), (float) (this.mThinLineWidth * 2)}, 0.0f));
        if (shadowcolor != 0) {
            this.mTextPaint.setShadowLayer(r, dx, dy, shadowcolor);
            this.mHeaderTextPaint.setShadowLayer(r, dx, dy, shadowcolor);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setStats(BatteryStats stats, Intent broadcast) {
        this.mStats = stats;
        this.mBatteryBroadcast = broadcast;
        this.mStatsPeriod = this.mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, 0);
        this.mChargingLabel = getContext().getString(com.android.settings.R.string.battery_stats_charging_label);
        this.mScreenOnLabel = getContext().getString(com.android.settings.R.string.battery_stats_screen_on_label);
        this.mGpsOnLabel = getContext().getString(com.android.settings.R.string.battery_stats_gps_on_label);
        this.mCameraOnLabel = getContext().getString(com.android.settings.R.string.battery_stats_camera_on_label);
        this.mFlashlightOnLabel = getContext().getString(com.android.settings.R.string.battery_stats_flashlight_on_label);
        this.mWifiRunningLabel = getContext().getString(com.android.settings.R.string.battery_stats_wifi_running_label);
        this.mCpuRunningLabel = getContext().getString(com.android.settings.R.string.battery_stats_wake_lock_label);
        this.mPhoneSignalLabel = getContext().getString(com.android.settings.R.string.battery_stats_phone_signal_label);
        this.mMaxPercentLabelString = Utils.formatPercentage(100);
        this.mMinPercentLabelString = Utils.formatPercentage(0);
        BatteryInfo.getBatteryInfo(getContext(), new -$$Lambda$BatteryHistoryChart$O1ddbx0WxFm_LlbjYiwVyWtFoUA(this, stats), this.mStats, false);
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0094  */
    public static /* synthetic */ void lambda$setStats$0(com.android.settings.fuelgauge.BatteryHistoryChart r24, android.os.BatteryStats r25, com.android.settings.fuelgauge.BatteryInfo r26) {
        /*
        r0 = r24;
        r1 = r26;
        r0.mInfo = r1;
        r2 = "";
        r0.mDrainString = r2;
        r2 = "";
        r0.mChargeDurationString = r2;
        r2 = r0.mInfo;
        r2 = r2.chargeLabel;
        r0.setContentDescription(r2);
        r2 = 0;
        r3 = 0;
        r4 = -1;
        r5 = 0;
        r0.mBatLow = r5;
        r6 = 100;
        r0.mBatHigh = r6;
        r6 = 0;
        r0.mStartWallTime = r6;
        r0.mEndDataWallTime = r6;
        r0.mEndWallTime = r6;
        r0.mHistStart = r6;
        r0.mHistEnd = r6;
        r8 = 0;
        r10 = 0;
        r12 = 0;
        r13 = 0;
        r14 = 1;
        r15 = r25.startIteratingHistoryLocked();
        if (r15 == 0) goto L_0x00c2;
    L_0x0038:
        r15 = new android.os.BatteryStats$HistoryItem;
        r15.<init>();
    L_0x003d:
        r5 = r25;
        r16 = r5.getNextHistoryLocked(r15);
        if (r16 == 0) goto L_0x00bd;
    L_0x0045:
        r2 = r2 + 1;
        if (r14 == 0) goto L_0x004e;
    L_0x0049:
        r14 = 0;
        r6 = r15.time;
        r0.mHistStart = r6;
    L_0x004e:
        r6 = r15.cmd;
        r7 = 5;
        if (r6 == r7) goto L_0x0060;
    L_0x0053:
        r6 = r15.cmd;
        r7 = 7;
        if (r6 != r7) goto L_0x0059;
    L_0x0058:
        goto L_0x0060;
    L_0x0059:
        r22 = r13;
        r23 = r14;
        r6 = 0;
        goto L_0x009c;
    L_0x0060:
        r6 = r15.currentTime;
        r18 = 15552000000; // 0x39ef8b000 float:-2.6330813E-20 double:7.683708924E-314;
        r18 = r8 + r18;
        r6 = (r6 > r18 ? 1 : (r6 == r18 ? 0 : -1));
        if (r6 > 0) goto L_0x0080;
    L_0x006d:
        r6 = r15.time;
        r20 = r8;
        r8 = r0.mHistStart;
        r18 = 300000; // 0x493e0 float:4.2039E-40 double:1.482197E-318;
        r8 = r8 + r18;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 >= 0) goto L_0x007d;
    L_0x007c:
        goto L_0x0082;
    L_0x007d:
        r6 = 0;
        goto L_0x0086;
    L_0x0080:
        r20 = r8;
    L_0x0082:
        r6 = 0;
        r0.mStartWallTime = r6;
    L_0x0086:
        r8 = r15.currentTime;
        r10 = r15.time;
        r22 = r13;
        r23 = r14;
        r13 = r0.mStartWallTime;
        r13 = (r13 > r6 ? 1 : (r13 == r6 ? 0 : -1));
        if (r13 != 0) goto L_0x009c;
    L_0x0094:
        r13 = r0.mHistStart;
        r13 = r10 - r13;
        r13 = r8 - r13;
        r0.mStartWallTime = r13;
    L_0x009c:
        r13 = r15.isDeltaData();
        if (r13 == 0) goto L_0x00b8;
    L_0x00a2:
        r13 = r15.batteryLevel;
        if (r13 != r4) goto L_0x00a9;
    L_0x00a6:
        r13 = 1;
        if (r2 != r13) goto L_0x00ab;
    L_0x00a9:
        r4 = r15.batteryLevel;
    L_0x00ab:
        r3 = r2;
        r13 = r15.time;
        r0.mHistDataEnd = r13;
        r13 = r15.states;
        r12 = r12 | r13;
        r13 = r15.states2;
        r13 = r22 | r13;
        goto L_0x00ba;
    L_0x00b8:
        r13 = r22;
    L_0x00ba:
        r14 = r23;
        goto L_0x003d;
    L_0x00bd:
        r20 = r8;
        r22 = r13;
        goto L_0x00c6;
    L_0x00c2:
        r5 = r25;
        r20 = r8;
    L_0x00c6:
        r6 = r0.mHistDataEnd;
        r8 = r0.mInfo;
        r8 = r8.remainingTimeUs;
        r16 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r8 = r8 / r16;
        r6 = r6 + r8;
        r0.mHistEnd = r6;
        r6 = r0.mHistDataEnd;
        r6 = r20 + r6;
        r6 = r6 - r10;
        r0.mEndDataWallTime = r6;
        r6 = r0.mEndDataWallTime;
        r8 = r0.mInfo;
        r8 = r8.remainingTimeUs;
        r8 = r8 / r16;
        r6 = r6 + r8;
        r0.mEndWallTime = r6;
        r0.mNumHist = r3;
        r6 = 536870912; // 0x20000000 float:1.0842022E-19 double:2.652494739E-315;
        r7 = r12 & r6;
        if (r7 == 0) goto L_0x00ef;
    L_0x00ed:
        r7 = 1;
        goto L_0x00f0;
    L_0x00ef:
        r7 = 0;
    L_0x00f0:
        r0.mHaveGps = r7;
        r7 = 134217728; // 0x8000000 float:3.85186E-34 double:6.63123685E-316;
        r7 = r7 & r13;
        if (r7 == 0) goto L_0x00f9;
    L_0x00f7:
        r7 = 1;
        goto L_0x00fa;
    L_0x00f9:
        r7 = 0;
    L_0x00fa:
        r0.mHaveFlashlight = r7;
        r7 = 2097152; // 0x200000 float:2.938736E-39 double:1.0361308E-317;
        r7 = r7 & r13;
        if (r7 == 0) goto L_0x0103;
    L_0x0101:
        r7 = 1;
        goto L_0x0104;
    L_0x0103:
        r7 = 0;
    L_0x0104:
        r0.mHaveCamera = r7;
        r6 = r6 & r13;
        if (r6 != 0) goto L_0x0111;
    L_0x0109:
        r6 = 402718720; // 0x18010000 float:1.667286E-24 double:1.989694845E-315;
        r6 = r6 & r12;
        if (r6 == 0) goto L_0x010f;
    L_0x010e:
        goto L_0x0111;
    L_0x010f:
        r6 = 0;
        goto L_0x0112;
    L_0x0111:
        r6 = 1;
    L_0x0112:
        r0.mHaveWifi = r6;
        r6 = r24.getContext();
        r6 = com.android.settingslib.Utils.isWifiOnly(r6);
        if (r6 != 0) goto L_0x0121;
    L_0x011e:
        r6 = 1;
        r0.mHavePhoneSignal = r6;
    L_0x0121:
        r6 = r0.mHistEnd;
        r8 = r0.mHistStart;
        r6 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r6 > 0) goto L_0x0130;
    L_0x0129:
        r6 = r0.mHistStart;
        r8 = 1;
        r6 = r6 + r8;
        r0.mHistEnd = r6;
    L_0x0130:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.BatteryHistoryChart.lambda$setStats$0(com.android.settings.fuelgauge.BatteryHistoryChart, android.os.BatteryStats, com.android.settings.fuelgauge.BatteryInfo):void");
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mMaxPercentLabelStringWidth = (int) this.mTextPaint.measureText(this.mMaxPercentLabelString);
        this.mMinPercentLabelStringWidth = (int) this.mTextPaint.measureText(this.mMinPercentLabelString);
        this.mDrainStringWidth = (int) this.mHeaderTextPaint.measureText(this.mDrainString);
        this.mChargeLabelStringWidth = (int) this.mHeaderTextPaint.measureText(this.mInfo.chargeLabel.toString());
        this.mChargeDurationStringWidth = (int) this.mHeaderTextPaint.measureText(this.mChargeDurationString);
        this.mTextAscent = (int) this.mTextPaint.ascent();
        this.mTextDescent = (int) this.mTextPaint.descent();
        this.mHeaderTextAscent = (int) this.mHeaderTextPaint.ascent();
        this.mHeaderTextDescent = (int) this.mHeaderTextPaint.descent();
        this.mHeaderHeight = ((this.mHeaderTextDescent - this.mHeaderTextAscent) * 2) - this.mTextAscent;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(this.mChartMinHeight + this.mHeaderHeight, heightMeasureSpec));
    }

    /* Access modifiers changed, original: 0000 */
    public void finishPaths(int w, int h, int levelh, int startX, int y, Path curLevelPath, int lastX, boolean lastCharging, boolean lastScreenOn, boolean lastGpsOn, boolean lastFlashlightOn, boolean lastCameraOn, boolean lastWifiRunning, boolean lastCpuRunning, Path lastPath) {
        int i = w;
        int i2 = y;
        Path path = curLevelPath;
        int i3 = lastX;
        Path path2 = lastPath;
        if (path != null) {
            if (i3 >= 0 && i3 < i) {
                if (path2 != null) {
                    path2.lineTo((float) i, (float) i2);
                }
                path.lineTo((float) i, (float) i2);
            }
            path.lineTo((float) i, (float) (this.mLevelTop + levelh));
            path.lineTo((float) startX, (float) (this.mLevelTop + levelh));
            curLevelPath.close();
        } else {
            int i4 = startX;
        }
        if (lastCharging) {
            this.mChargingPath.lineTo((float) i, (float) (h - this.mChargingOffset));
        }
        if (lastScreenOn) {
            this.mScreenOnPath.lineTo((float) i, (float) (h - this.mScreenOnOffset));
        }
        if (lastGpsOn) {
            this.mGpsOnPath.lineTo((float) i, (float) (h - this.mGpsOnOffset));
        }
        if (lastFlashlightOn) {
            this.mFlashlightOnPath.lineTo((float) i, (float) (h - this.mFlashlightOnOffset));
        }
        if (lastCameraOn) {
            this.mCameraOnPath.lineTo((float) i, (float) (h - this.mCameraOnOffset));
        }
        if (lastWifiRunning) {
            this.mWifiRunningPath.lineTo((float) i, (float) (h - this.mWifiRunningOffset));
        }
        if (lastCpuRunning) {
            this.mCpuRunningPath.lineTo((float) i, (float) (h - this.mCpuRunningOffset));
        }
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.finish(i);
        }
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getContext());
    }

    private boolean isDayFirst() {
        String value = LocaleData.get(getResources().getConfiguration().locale).getDateFormat(3);
        return value.indexOf(77) > value.indexOf(100);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x07b2  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x075e  */
    /* JADX WARNING: Removed duplicated region for block: B:279:0x090e  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x08f3  */
    public void onSizeChanged(int r86, int r87, int r88, int r89) {
        /*
        r85 = this;
        r15 = r85;
        r14 = r86;
        r13 = r87;
        super.onSizeChanged(r86, r87, r88, r89);
        r0 = r15.mLastWidth;
        if (r0 != r14) goto L_0x0012;
    L_0x000d:
        r0 = r15.mLastHeight;
        if (r0 != r13) goto L_0x0012;
    L_0x0011:
        return;
    L_0x0012:
        r0 = r15.mLastWidth;
        if (r0 == 0) goto L_0x0915;
    L_0x0016:
        r0 = r15.mLastHeight;
        if (r0 != 0) goto L_0x001c;
    L_0x001a:
        goto L_0x0915;
    L_0x001c:
        r15.mLastWidth = r14;
        r15.mLastHeight = r13;
        r12 = 0;
        r15.mBitmap = r12;
        r15.mCanvas = r12;
        r0 = r15.mTextDescent;
        r1 = r15.mTextAscent;
        r16 = r0 - r1;
        r0 = r16 * 10;
        r1 = r15.mChartMinHeight;
        r0 = r0 + r1;
        r11 = 1;
        r10 = 0;
        if (r13 <= r0) goto L_0x0044;
    L_0x0034:
        r15.mLargeMode = r11;
        r0 = r16 * 15;
        if (r13 <= r0) goto L_0x003f;
    L_0x003a:
        r0 = r16 / 2;
        r15.mLineWidth = r0;
        goto L_0x004a;
    L_0x003f:
        r0 = r16 / 3;
        r15.mLineWidth = r0;
        goto L_0x004a;
    L_0x0044:
        r15.mLargeMode = r10;
        r0 = r15.mThinLineWidth;
        r15.mLineWidth = r0;
    L_0x004a:
        r0 = r15.mLineWidth;
        if (r0 > 0) goto L_0x0050;
    L_0x004e:
        r15.mLineWidth = r11;
    L_0x0050:
        r0 = r15.mHeaderHeight;
        r15.mLevelTop = r0;
        r0 = r15.mMaxPercentLabelStringWidth;
        r1 = r15.mThinLineWidth;
        r9 = 3;
        r1 = r1 * r9;
        r0 = r0 + r1;
        r15.mLevelLeft = r0;
        r15.mLevelRight = r14;
        r0 = r15.mLevelRight;
        r1 = r15.mLevelLeft;
        r8 = r0 - r1;
        r0 = r15.mTextPaint;
        r1 = r15.mThinLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mBatteryGoodPaint;
        r1 = r15.mThinLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mBatteryWarnPaint;
        r1 = r15.mThinLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mBatteryCriticalPaint;
        r1 = r15.mThinLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mChargingPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mScreenOnPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mGpsOnPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mCameraOnPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mFlashlightOnPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mWifiRunningPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mCpuRunningPaint;
        r1 = r15.mLineWidth;
        r1 = (float) r1;
        r0.setStrokeWidth(r1);
        r0 = r15.mDebugRectPaint;
        r1 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r0.setStrokeWidth(r1);
        r0 = r15.mLineWidth;
        r17 = r16 + r0;
        r0 = r15.mLargeMode;
        r7 = 2;
        if (r0 == 0) goto L_0x0136;
    L_0x00cd:
        r0 = r15.mLineWidth;
        r15.mChargingOffset = r0;
        r0 = r15.mChargingOffset;
        r0 = r0 + r17;
        r15.mScreenOnOffset = r0;
        r0 = r15.mScreenOnOffset;
        r0 = r0 + r17;
        r15.mCpuRunningOffset = r0;
        r0 = r15.mCpuRunningOffset;
        r0 = r0 + r17;
        r15.mWifiRunningOffset = r0;
        r0 = r15.mWifiRunningOffset;
        r1 = r15.mHaveWifi;
        if (r1 == 0) goto L_0x00ec;
    L_0x00e9:
        r1 = r17;
        goto L_0x00ed;
    L_0x00ec:
        r1 = r10;
    L_0x00ed:
        r0 = r0 + r1;
        r15.mGpsOnOffset = r0;
        r0 = r15.mGpsOnOffset;
        r1 = r15.mHaveGps;
        if (r1 == 0) goto L_0x00f9;
    L_0x00f6:
        r1 = r17;
        goto L_0x00fa;
    L_0x00f9:
        r1 = r10;
    L_0x00fa:
        r0 = r0 + r1;
        r15.mFlashlightOnOffset = r0;
        r0 = r15.mFlashlightOnOffset;
        r1 = r15.mHaveFlashlight;
        if (r1 == 0) goto L_0x0106;
    L_0x0103:
        r1 = r17;
        goto L_0x0107;
    L_0x0106:
        r1 = r10;
    L_0x0107:
        r0 = r0 + r1;
        r15.mCameraOnOffset = r0;
        r0 = r15.mCameraOnOffset;
        r1 = r15.mHaveCamera;
        if (r1 == 0) goto L_0x0113;
    L_0x0110:
        r1 = r17;
        goto L_0x0114;
    L_0x0113:
        r1 = r10;
    L_0x0114:
        r0 = r0 + r1;
        r15.mPhoneSignalOffset = r0;
        r0 = r15.mPhoneSignalOffset;
        r1 = r15.mHavePhoneSignal;
        if (r1 == 0) goto L_0x0120;
    L_0x011d:
        r1 = r17;
        goto L_0x0121;
    L_0x0120:
        r1 = r10;
    L_0x0121:
        r0 = r0 + r1;
        r1 = r15.mLineWidth;
        r1 = r1 * r7;
        r0 = r0 + r1;
        r1 = r15.mLineWidth;
        r1 = r1 / r7;
        r0 = r0 + r1;
        r15.mLevelOffset = r0;
        r0 = r15.mHavePhoneSignal;
        if (r0 == 0) goto L_0x0157;
    L_0x0130:
        r0 = r15.mPhoneSignalChart;
        r0.init(r14);
        goto L_0x0157;
    L_0x0136:
        r15.mPhoneSignalOffset = r10;
        r15.mChargingOffset = r10;
        r15.mCpuRunningOffset = r10;
        r15.mWifiRunningOffset = r10;
        r15.mFlashlightOnOffset = r10;
        r15.mCameraOnOffset = r10;
        r15.mGpsOnOffset = r10;
        r15.mScreenOnOffset = r10;
        r0 = r15.mThinLineWidth;
        r0 = r0 * 4;
        r0 = r17 + r0;
        r15.mLevelOffset = r0;
        r0 = r15.mHavePhoneSignal;
        if (r0 == 0) goto L_0x0157;
    L_0x0152:
        r0 = r15.mPhoneSignalChart;
        r0.init(r10);
    L_0x0157:
        r0 = r15.mBatLevelPath;
        r0.reset();
        r0 = r15.mBatGoodPath;
        r0.reset();
        r0 = r15.mBatWarnPath;
        r0.reset();
        r0 = r15.mTimeRemainPath;
        r0.reset();
        r0 = r15.mBatCriticalPath;
        r0.reset();
        r0 = r15.mScreenOnPath;
        r0.reset();
        r0 = r15.mGpsOnPath;
        r0.reset();
        r0 = r15.mFlashlightOnPath;
        r0.reset();
        r0 = r15.mCameraOnPath;
        r0.reset();
        r0 = r15.mWifiRunningPath;
        r0.reset();
        r0 = r15.mCpuRunningPath;
        r0.reset();
        r0 = r15.mChargingPath;
        r0.reset();
        r0 = r15.mTimeLabels;
        r0.clear();
        r0 = r15.mDateLabels;
        r0.clear();
        r5 = r15.mStartWallTime;
        r0 = r15.mEndWallTime;
        r0 = (r0 > r5 ? 1 : (r0 == r5 ? 0 : -1));
        if (r0 <= 0) goto L_0x01a9;
    L_0x01a5:
        r0 = r15.mEndWallTime;
        r0 = r0 - r5;
        goto L_0x01ab;
    L_0x01a9:
        r0 = 1;
    L_0x01ab:
        r18 = r0;
        r0 = r15.mStartWallTime;
        r2 = 0;
        r4 = r15.mBatLow;
        r12 = r15.mBatHigh;
        r7 = r15.mBatLow;
        r22 = r12 - r7;
        r7 = r15.mLevelOffset;
        r7 = r13 - r7;
        r12 = r15.mLevelTop;
        r23 = r7 - r12;
        r7 = r15.mLevelTop;
        r7 = r7 + r23;
        r15.mLevelBottom = r7;
        r7 = r15.mLevelLeft;
        r12 = 0;
        r9 = r15.mLevelLeft;
        r25 = -1;
        r26 = -1;
        r27 = 0;
        r28 = 0;
        r29 = 0;
        r30 = 0;
        r31 = 0;
        r32 = 0;
        r33 = 0;
        r34 = 0;
        r35 = 0;
        r36 = 0;
        r37 = 0;
        r38 = 0;
        r11 = r15.mNumHist;
        r41 = r11;
        r10 = r15.mEndDataWallTime;
        r42 = r0;
        r0 = r15.mStartWallTime;
        r0 = (r10 > r0 ? 1 : (r10 == r0 ? 0 : -1));
        if (r0 <= 0) goto L_0x06b6;
    L_0x01f6:
        r0 = r15.mStats;
        r0 = r0.startIteratingHistoryLocked();
        if (r0 == 0) goto L_0x06b6;
    L_0x01fe:
        r0 = new android.os.BatteryStats$HistoryItem;
        r0.<init>();
        r10 = r25;
        r1 = r31;
        r11 = r32;
        r14 = r33;
        r45 = r34;
        r46 = r35;
        r47 = r37;
        r48 = r38;
        r25 = r7;
        r7 = r29;
        r82 = r27;
        r27 = r9;
        r9 = r26;
        r26 = r12;
        r12 = r82;
        r83 = r2;
        r3 = r28;
        r28 = r83;
        r2 = r30;
    L_0x0229:
        r49 = r14;
        r14 = r15.mStats;
        r14 = r14.getNextHistoryLocked(r0);
        if (r14 == 0) goto L_0x0672;
    L_0x0233:
        r14 = r41;
        if (r12 >= r14) goto L_0x064d;
    L_0x0237:
        r30 = r0.isDeltaData();
        if (r30 == 0) goto L_0x0508;
    L_0x023d:
        r51 = r11;
        r50 = r12;
        r11 = r0.time;
        r11 = r11 - r28;
        r42 = r42 + r11;
        r11 = r0.time;
        r52 = r11;
        r11 = r15.mLevelLeft;
        r28 = r42 - r5;
        r54 = r5;
        r5 = (long) r8;
        r28 = r28 * r5;
        r5 = r28 / r18;
        r5 = (int) r5;
        r11 = r11 + r5;
        if (r11 >= 0) goto L_0x025c;
    L_0x025a:
        r5 = 0;
        r11 = r5;
    L_0x025c:
        r5 = r15.mLevelTop;
        r5 = r5 + r23;
        r6 = r0.batteryLevel;
        r6 = r6 - r4;
        r12 = r23 + -1;
        r6 = r6 * r12;
        r6 = r6 / r22;
        r5 = r5 - r6;
        if (r10 == r11) goto L_0x02ba;
    L_0x026b:
        if (r9 == r5) goto L_0x02ba;
    L_0x026d:
        r6 = r0.batteryLevel;
        r12 = r15.mBatteryCriticalLevel;
        if (r6 > r12) goto L_0x0276;
    L_0x0273:
        r12 = r15.mBatCriticalPath;
    L_0x0275:
        goto L_0x027e;
    L_0x0276:
        r12 = r15.mBatteryWarnLevel;
        if (r6 > r12) goto L_0x027d;
    L_0x027a:
        r12 = r15.mBatWarnPath;
        goto L_0x0275;
    L_0x027d:
        r12 = 0;
    L_0x027e:
        if (r12 == r7) goto L_0x029a;
    L_0x0280:
        if (r7 == 0) goto L_0x028c;
    L_0x0282:
        r56 = r4;
        r4 = (float) r11;
        r57 = r6;
        r6 = (float) r5;
        r7.lineTo(r4, r6);
        goto L_0x0290;
    L_0x028c:
        r56 = r4;
        r57 = r6;
    L_0x0290:
        if (r12 == 0) goto L_0x0297;
    L_0x0292:
        r4 = (float) r11;
        r6 = (float) r5;
        r12.moveTo(r4, r6);
    L_0x0297:
        r4 = r12;
        r7 = r4;
        goto L_0x02a5;
    L_0x029a:
        r56 = r4;
        r57 = r6;
        if (r12 == 0) goto L_0x02a5;
    L_0x02a0:
        r4 = (float) r11;
        r6 = (float) r5;
        r12.lineTo(r4, r6);
    L_0x02a5:
        if (r3 != 0) goto L_0x02b2;
    L_0x02a7:
        r3 = r15.mBatLevelPath;
        r4 = (float) r11;
        r6 = (float) r5;
        r3.moveTo(r4, r6);
        r4 = r11;
        r27 = r4;
        goto L_0x02b7;
    L_0x02b2:
        r4 = (float) r11;
        r6 = (float) r5;
        r3.lineTo(r4, r6);
    L_0x02b7:
        r10 = r11;
        r9 = r5;
        goto L_0x02bc;
    L_0x02ba:
        r56 = r4;
    L_0x02bc:
        r4 = r15.mLargeMode;
        if (r4 == 0) goto L_0x04d7;
    L_0x02c0:
        r4 = r0.states;
        r6 = 524288; // 0x80000 float:7.34684E-40 double:2.590327E-318;
        r4 = r4 & r6;
        if (r4 == 0) goto L_0x02c9;
    L_0x02c7:
        r4 = 1;
        goto L_0x02ca;
    L_0x02c9:
        r4 = 0;
    L_0x02ca:
        if (r4 == r2) goto L_0x02eb;
    L_0x02cc:
        if (r4 == 0) goto L_0x02dc;
    L_0x02ce:
        r6 = r15.mChargingPath;
        r12 = (float) r11;
        r58 = r2;
        r2 = r15.mChargingOffset;
        r2 = r13 - r2;
        r2 = (float) r2;
        r6.moveTo(r12, r2);
        goto L_0x02e9;
    L_0x02dc:
        r58 = r2;
        r2 = r15.mChargingPath;
        r6 = (float) r11;
        r12 = r15.mChargingOffset;
        r12 = r13 - r12;
        r12 = (float) r12;
        r2.lineTo(r6, r12);
    L_0x02e9:
        r2 = r4;
        goto L_0x02ed;
    L_0x02eb:
        r58 = r2;
    L_0x02ed:
        r6 = r0.states;
        r12 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;
        r6 = r6 & r12;
        if (r6 == 0) goto L_0x02f6;
    L_0x02f4:
        r6 = 1;
        goto L_0x02f7;
    L_0x02f6:
        r6 = 0;
    L_0x02f7:
        if (r6 == r1) goto L_0x031c;
    L_0x02f9:
        if (r6 == 0) goto L_0x030b;
    L_0x02fb:
        r12 = r15.mScreenOnPath;
        r59 = r1;
        r1 = (float) r11;
        r60 = r2;
        r2 = r15.mScreenOnOffset;
        r2 = r13 - r2;
        r2 = (float) r2;
        r12.moveTo(r1, r2);
        goto L_0x031a;
    L_0x030b:
        r59 = r1;
        r60 = r2;
        r1 = r15.mScreenOnPath;
        r2 = (float) r11;
        r12 = r15.mScreenOnOffset;
        r12 = r13 - r12;
        r12 = (float) r12;
        r1.lineTo(r2, r12);
    L_0x031a:
        r1 = r6;
        goto L_0x0320;
    L_0x031c:
        r59 = r1;
        r60 = r2;
    L_0x0320:
        r2 = r0.states;
        r12 = 536870912; // 0x20000000 float:1.0842022E-19 double:2.652494739E-315;
        r2 = r2 & r12;
        if (r2 == 0) goto L_0x0329;
    L_0x0327:
        r2 = 1;
        goto L_0x032a;
    L_0x0329:
        r2 = 0;
    L_0x032a:
        r12 = r51;
        if (r2 == r12) goto L_0x0356;
    L_0x032e:
        if (r2 == 0) goto L_0x0342;
    L_0x0330:
        r61 = r1;
        r1 = r15.mGpsOnPath;
        r62 = r3;
        r3 = (float) r11;
        r63 = r4;
        r4 = r15.mGpsOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r1.moveTo(r3, r4);
        goto L_0x0353;
    L_0x0342:
        r61 = r1;
        r62 = r3;
        r63 = r4;
        r1 = r15.mGpsOnPath;
        r3 = (float) r11;
        r4 = r15.mGpsOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r1.lineTo(r3, r4);
    L_0x0353:
        r1 = r2;
        r12 = r1;
        goto L_0x035c;
    L_0x0356:
        r61 = r1;
        r62 = r3;
        r63 = r4;
    L_0x035c:
        r1 = r0.states2;
        r3 = 134217728; // 0x8000000 float:3.85186E-34 double:6.63123685E-316;
        r1 = r1 & r3;
        if (r1 == 0) goto L_0x0365;
    L_0x0363:
        r1 = 1;
        goto L_0x0366;
    L_0x0365:
        r1 = 0;
    L_0x0366:
        r4 = r49;
        if (r1 == r4) goto L_0x038f;
    L_0x036a:
        if (r1 == 0) goto L_0x037c;
    L_0x036c:
        r3 = r15.mFlashlightOnPath;
        r64 = r2;
        r2 = (float) r11;
        r65 = r4;
        r4 = r15.mFlashlightOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r3.moveTo(r2, r4);
        goto L_0x038b;
    L_0x037c:
        r64 = r2;
        r65 = r4;
        r2 = r15.mFlashlightOnPath;
        r3 = (float) r11;
        r4 = r15.mFlashlightOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r2.lineTo(r3, r4);
    L_0x038b:
        r2 = r1;
        r65 = r2;
        goto L_0x0393;
    L_0x038f:
        r64 = r2;
        r65 = r4;
    L_0x0393:
        r2 = r0.states2;
        r3 = 2097152; // 0x200000 float:2.938736E-39 double:1.0361308E-317;
        r2 = r2 & r3;
        if (r2 == 0) goto L_0x039c;
    L_0x039a:
        r2 = 1;
        goto L_0x039d;
    L_0x039c:
        r2 = 0;
    L_0x039d:
        r4 = r45;
        if (r2 == r4) goto L_0x03c6;
    L_0x03a1:
        if (r2 == 0) goto L_0x03b3;
    L_0x03a3:
        r3 = r15.mCameraOnPath;
        r66 = r1;
        r1 = (float) r11;
        r67 = r4;
        r4 = r15.mCameraOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r3.moveTo(r1, r4);
        goto L_0x03c2;
    L_0x03b3:
        r66 = r1;
        r67 = r4;
        r1 = r15.mCameraOnPath;
        r3 = (float) r11;
        r4 = r15.mCameraOnOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r1.lineTo(r3, r4);
    L_0x03c2:
        r1 = r2;
        r45 = r1;
        goto L_0x03cc;
    L_0x03c6:
        r66 = r1;
        r67 = r4;
        r45 = r67;
    L_0x03cc:
        r1 = r0.states2;
        r1 = r1 & 15;
        r3 = 0;
        r1 = r1 >> r3;
        r4 = r48;
        if (r4 == r1) goto L_0x03f5;
    L_0x03d6:
        r3 = r1;
        switch(r1) {
            case 0: goto L_0x03e9;
            case 1: goto L_0x03e9;
            case 2: goto L_0x03e9;
            case 3: goto L_0x03e9;
            default: goto L_0x03da;
        };
    L_0x03da:
        switch(r1) {
            case 11: goto L_0x03e9;
            case 12: goto L_0x03e9;
            default: goto L_0x03dd;
        };
    L_0x03dd:
        r30 = 1;
        r4 = r30;
        r36 = r30;
        r48 = r3;
        r31 = 0;
        goto L_0x03fd;
    L_0x03e9:
        r30 = 1;
        r31 = 0;
        r4 = r31;
        r36 = r31;
        r48 = r3;
        goto L_0x03fd;
    L_0x03f5:
        r30 = 1;
        r31 = 0;
        r48 = r4;
        r4 = r36;
    L_0x03fd:
        r3 = r36;
        r68 = r1;
        r1 = r0.states;
        r25 = 402718720; // 0x18010000 float:1.667286E-24 double:1.989694845E-315;
        r1 = r1 & r25;
        if (r1 == 0) goto L_0x040a;
    L_0x0409:
        r3 = 1;
    L_0x040a:
        r1 = r46;
        if (r3 == r1) goto L_0x0437;
    L_0x040e:
        if (r3 == 0) goto L_0x0422;
    L_0x0410:
        r69 = r1;
        r1 = r15.mWifiRunningPath;
        r70 = r2;
        r2 = (float) r11;
        r71 = r4;
        r4 = r15.mWifiRunningOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r1.moveTo(r2, r4);
        goto L_0x0433;
    L_0x0422:
        r69 = r1;
        r70 = r2;
        r71 = r4;
        r1 = r15.mWifiRunningPath;
        r2 = (float) r11;
        r4 = r15.mWifiRunningOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r1.lineTo(r2, r4);
    L_0x0433:
        r1 = r3;
        r46 = r1;
        goto L_0x043f;
    L_0x0437:
        r69 = r1;
        r70 = r2;
        r71 = r4;
        r46 = r69;
    L_0x043f:
        r1 = r0.states;
        r2 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r1 = r1 & r2;
        if (r1 == 0) goto L_0x0449;
    L_0x0446:
        r1 = r30;
        goto L_0x044b;
    L_0x0449:
        r1 = r31;
    L_0x044b:
        r2 = r47;
        if (r1 == r2) goto L_0x0474;
    L_0x044f:
        if (r1 == 0) goto L_0x0461;
    L_0x0451:
        r4 = r15.mCpuRunningPath;
        r72 = r2;
        r2 = (float) r11;
        r73 = r3;
        r3 = r15.mCpuRunningOffset;
        r3 = r13 - r3;
        r3 = (float) r3;
        r4.moveTo(r2, r3);
        goto L_0x0470;
    L_0x0461:
        r72 = r2;
        r73 = r3;
        r2 = r15.mCpuRunningPath;
        r3 = (float) r11;
        r4 = r15.mCpuRunningOffset;
        r4 = r13 - r4;
        r4 = (float) r4;
        r2.lineTo(r3, r4);
    L_0x0470:
        r2 = r1;
        r47 = r2;
        goto L_0x047a;
    L_0x0474:
        r72 = r2;
        r73 = r3;
        r47 = r72;
    L_0x047a:
        r2 = r15.mLargeMode;
        if (r2 == 0) goto L_0x04ac;
    L_0x047e:
        r2 = r15.mHavePhoneSignal;
        if (r2 == 0) goto L_0x04ac;
    L_0x0482:
        r2 = r0.states;
        r2 = r2 & 448;
        r3 = 6;
        r2 = r2 >> r3;
        r3 = 3;
        if (r2 != r3) goto L_0x0491;
    L_0x048b:
        r2 = 0;
    L_0x048c:
        r21 = 2;
        r24 = 3;
        goto L_0x04a6;
    L_0x0491:
        r2 = r0.states;
        r3 = 2097152; // 0x200000 float:2.938736E-39 double:1.0361308E-317;
        r2 = r2 & r3;
        if (r2 == 0) goto L_0x049a;
    L_0x0498:
        r2 = 1;
        goto L_0x048c;
    L_0x049a:
        r2 = r0.states;
        r2 = r2 & 56;
        r24 = 3;
        r2 = r2 >> 3;
        r21 = 2;
        r2 = r2 + 2;
    L_0x04a6:
        r3 = r15.mPhoneSignalChart;
        r3.addTick(r11, r2);
        goto L_0x04b0;
    L_0x04ac:
        r21 = 2;
        r24 = 3;
    L_0x04b0:
        r34 = r0;
        r26 = r5;
        r76 = r8;
        r25 = r11;
        r11 = r12;
        r51 = r14;
        r49 = r24;
        r77 = r31;
        r72 = r47;
        r4 = r48;
        r20 = r50;
        r28 = r52;
        r47 = r54;
        r41 = r56;
        r2 = r60;
        r1 = r61;
        r3 = r62;
        r14 = r65;
        r36 = r71;
        goto L_0x0637;
    L_0x04d7:
        r59 = r1;
        r58 = r2;
        r62 = r3;
        r67 = r45;
        r69 = r46;
        r72 = r47;
        r4 = r48;
        r65 = r49;
        r12 = r51;
        r21 = 2;
        r24 = 3;
        r34 = r0;
        r26 = r5;
        r76 = r8;
        r25 = r11;
        r11 = r12;
        r51 = r14;
        r49 = r24;
        r20 = r50;
        r28 = r52;
        r47 = r54;
        r41 = r56;
        r14 = r65;
        r77 = 0;
        goto L_0x0637;
    L_0x0508:
        r59 = r1;
        r58 = r2;
        r56 = r4;
        r54 = r5;
        r50 = r12;
        r67 = r45;
        r69 = r46;
        r72 = r47;
        r4 = r48;
        r65 = r49;
        r21 = 2;
        r24 = 3;
        r30 = 1;
        r31 = 0;
        r12 = r11;
        r32 = r42;
        r1 = r0.cmd;
        r2 = 5;
        if (r1 == r2) goto L_0x0537;
    L_0x052c:
        r1 = r0.cmd;
        r5 = 7;
        if (r1 != r5) goto L_0x0532;
    L_0x0531:
        goto L_0x0537;
    L_0x0532:
        r74 = r3;
        r75 = r4;
        goto L_0x0558;
    L_0x0537:
        r5 = r0.currentTime;
        r74 = r3;
        r2 = r15.mStartWallTime;
        r1 = (r5 > r2 ? 1 : (r5 == r2 ? 0 : -1));
        if (r1 < 0) goto L_0x0548;
    L_0x0541:
        r1 = r0.currentTime;
        r42 = r1;
        r75 = r4;
        goto L_0x0554;
    L_0x0548:
        r1 = r15.mStartWallTime;
        r5 = r0.time;
        r75 = r4;
        r3 = r15.mHistStart;
        r5 = r5 - r3;
        r1 = r1 + r5;
        r42 = r1;
    L_0x0554:
        r1 = r0.time;
        r28 = r1;
    L_0x0558:
        r1 = r0.cmd;
        r11 = 6;
        if (r1 == r11) goto L_0x05f9;
    L_0x055d:
        r1 = r0.cmd;
        r2 = 5;
        if (r1 != r2) goto L_0x059a;
    L_0x0562:
        r1 = r32 - r42;
        r1 = java.lang.Math.abs(r1);
        r3 = 3600000; // 0x36ee80 float:5.044674E-39 double:1.7786363E-317;
        r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1));
        if (r1 <= 0) goto L_0x0570;
    L_0x056f:
        goto L_0x059a;
    L_0x0570:
        r34 = r0;
        r21 = r7;
        r76 = r8;
        r30 = r12;
        r51 = r14;
        r49 = r24;
        r77 = r31;
        r20 = r50;
        r47 = r54;
        r41 = r56;
        r38 = r58;
        r35 = r59;
        r44 = r65;
        r45 = r67;
        r37 = r69;
        r39 = r72;
        r40 = r74;
        r46 = r75;
        r24 = r9;
        r31 = r10;
        goto L_0x0621;
    L_0x059a:
        if (r74 == 0) goto L_0x05f9;
    L_0x059c:
        r1 = r25 + 1;
        r34 = r0;
        r0 = r15;
        r35 = r59;
        r37 = r69;
        r38 = r58;
        r39 = r72;
        r2 = r13;
        r40 = r74;
        r3 = r23;
        r41 = r56;
        r44 = r65;
        r45 = r67;
        r46 = r75;
        r4 = r27;
        r47 = r54;
        r5 = r9;
        r6 = r40;
        r21 = r7;
        r7 = r10;
        r76 = r8;
        r8 = r38;
        r49 = r24;
        r24 = r9;
        r9 = r35;
        r77 = r31;
        r31 = r10;
        r10 = r12;
        r51 = r14;
        r14 = r30;
        r30 = r12;
        r12 = r11;
        r11 = r44;
        r20 = r50;
        r12 = r45;
        r13 = r37;
        r14 = r39;
        r15 = r21;
        r0.finishPaths(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15);
        r0 = -1;
        r9 = r0;
        r10 = r0;
        r3 = 0;
        r7 = 0;
        r0 = r77;
        r45 = r77;
        r14 = r77;
        r11 = r77;
        r1 = r77;
        r2 = r77;
        r72 = r0;
        goto L_0x0633;
    L_0x05f9:
        r34 = r0;
        r21 = r7;
        r76 = r8;
        r30 = r12;
        r51 = r14;
        r49 = r24;
        r77 = r31;
        r20 = r50;
        r47 = r54;
        r41 = r56;
        r38 = r58;
        r35 = r59;
        r44 = r65;
        r45 = r67;
        r37 = r69;
        r39 = r72;
        r40 = r74;
        r46 = r75;
        r24 = r9;
        r31 = r10;
    L_0x0621:
        r7 = r21;
        r9 = r24;
        r11 = r30;
        r10 = r31;
        r1 = r35;
        r2 = r38;
        r72 = r39;
        r3 = r40;
        r14 = r44;
    L_0x0633:
        r4 = r46;
        r46 = r37;
    L_0x0637:
        r12 = r20 + 1;
        r13 = r87;
        r0 = r34;
        r5 = r47;
        r47 = r72;
        r8 = r76;
        r15 = r85;
        r48 = r4;
        r4 = r41;
        r41 = r51;
        goto L_0x0229;
    L_0x064d:
        r34 = r0;
        r35 = r1;
        r38 = r2;
        r40 = r3;
        r41 = r4;
        r21 = r7;
        r76 = r8;
        r24 = r9;
        r31 = r10;
        r30 = r11;
        r20 = r12;
        r51 = r14;
        r37 = r46;
        r39 = r47;
        r46 = r48;
        r44 = r49;
        r77 = 0;
        r47 = r5;
        goto L_0x0696;
    L_0x0672:
        r34 = r0;
        r35 = r1;
        r38 = r2;
        r40 = r3;
        r21 = r7;
        r76 = r8;
        r24 = r9;
        r31 = r10;
        r30 = r11;
        r20 = r12;
        r51 = r41;
        r37 = r46;
        r39 = r47;
        r46 = r48;
        r44 = r49;
        r77 = 0;
        r41 = r4;
        r47 = r5;
    L_0x0696:
        r15 = r85;
        r0 = r15.mStats;
        r0.finishIteratingHistoryLocked();
        r7 = r25;
        r12 = r26;
        r33 = r28;
        r32 = r30;
        r25 = r31;
        r31 = r35;
        r35 = r37;
        r30 = r38;
        r37 = r39;
        r28 = r40;
        r29 = r21;
        r26 = r24;
        goto L_0x06cc;
    L_0x06b6:
        r47 = r5;
        r76 = r8;
        r51 = r41;
        r77 = 0;
        r41 = r4;
        r20 = r27;
        r44 = r33;
        r45 = r34;
        r46 = r38;
        r33 = r2;
        r27 = r9;
    L_0x06cc:
        if (r26 < 0) goto L_0x06ea;
    L_0x06ce:
        if (r25 >= 0) goto L_0x06d3;
    L_0x06d0:
        r14 = r76;
        goto L_0x06ec;
    L_0x06d3:
        r0 = r15.mLevelLeft;
        r1 = r15.mEndDataWallTime;
        r1 = r1 - r47;
        r14 = r76;
        r3 = (long) r14;
        r1 = r1 * r3;
        r1 = r1 / r18;
        r1 = (int) r1;
        r0 = r0 + r1;
        if (r0 >= 0) goto L_0x06e4;
    L_0x06e3:
        r0 = 0;
    L_0x06e4:
        r13 = r0;
        r21 = r12;
        r12 = r26;
        goto L_0x0732;
    L_0x06ea:
        r14 = r76;
    L_0x06ec:
        r0 = r15.mLevelLeft;
        r1 = r0;
        r2 = r15.mLevelTop;
        r2 = r2 + r23;
        r3 = r15.mInfo;
        r3 = r3.batteryLevel;
        r3 = r3 - r41;
        r4 = r23 + -1;
        r3 = r3 * r4;
        r3 = r3 / r22;
        r2 = r2 - r3;
        r3 = r2;
        r12 = r2;
        r2 = r15.mInfo;
        r2 = r2.batteryLevel;
        r2 = (byte) r2;
        r4 = r15.mBatteryCriticalLevel;
        if (r2 > r4) goto L_0x070d;
    L_0x070a:
        r4 = r15.mBatCriticalPath;
    L_0x070c:
        goto L_0x0715;
    L_0x070d:
        r4 = r15.mBatteryWarnLevel;
        if (r2 > r4) goto L_0x0714;
    L_0x0711:
        r4 = r15.mBatWarnPath;
        goto L_0x070c;
    L_0x0714:
        r4 = 0;
    L_0x0715:
        if (r4 == 0) goto L_0x071e;
    L_0x0717:
        r5 = (float) r0;
        r6 = (float) r12;
        r4.moveTo(r5, r6);
        r29 = r4;
    L_0x071e:
        r5 = r15.mBatLevelPath;
        r6 = (float) r0;
        r7 = (float) r12;
        r5.moveTo(r6, r7);
        r5 = r15.mBatLevelPath;
        r0 = r86;
        r13 = r0;
        r25 = r1;
        r28 = r5;
        r21 = r12;
        r12 = r3;
    L_0x0732:
        r0 = r15;
        r1 = r13;
        r2 = r87;
        r3 = r23;
        r4 = r27;
        r5 = r12;
        r6 = r28;
        r7 = r25;
        r8 = r30;
        r9 = r31;
        r10 = r32;
        r11 = r44;
        r78 = r12;
        r12 = r45;
        r79 = r13;
        r13 = r35;
        r24 = r14;
        r14 = r37;
        r15 = r29;
        r0.finishPaths(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15);
        r0 = r86;
        r1 = r79;
        if (r1 >= r0) goto L_0x07b2;
    L_0x075e:
        r2 = r85;
        r3 = r2.mTimeRemainPath;
        r4 = (float) r1;
        r5 = r78;
        r6 = (float) r5;
        r3.moveTo(r4, r6);
        r3 = r2.mLevelTop;
        r3 = r3 + r23;
        r4 = 100 - r41;
        r6 = r23 + -1;
        r4 = r4 * r6;
        r4 = r4 / r22;
        r3 = r3 - r4;
        r4 = r2.mLevelTop;
        r4 = r4 + r23;
        r6 = r77;
        r10 = 0 - r41;
        r7 = r23 + -1;
        r10 = r10 * r7;
        r10 = r10 / r22;
        r4 = r4 - r10;
        r7 = r2.mInfo;
        r7 = r7.discharging;
        if (r7 == 0) goto L_0x0793;
    L_0x0789:
        r7 = r2.mTimeRemainPath;
        r8 = r2.mLevelRight;
        r8 = (float) r8;
        r9 = (float) r4;
        r7.lineTo(r8, r9);
        goto L_0x07a5;
    L_0x0793:
        r7 = r2.mTimeRemainPath;
        r8 = r2.mLevelRight;
        r8 = (float) r8;
        r9 = (float) r3;
        r7.lineTo(r8, r9);
        r7 = r2.mTimeRemainPath;
        r8 = r2.mLevelRight;
        r8 = (float) r8;
        r9 = (float) r4;
        r7.lineTo(r8, r9);
    L_0x07a5:
        r7 = r2.mTimeRemainPath;
        r8 = (float) r1;
        r9 = (float) r4;
        r7.lineTo(r8, r9);
        r7 = r2.mTimeRemainPath;
        r7.close();
        goto L_0x07b8;
    L_0x07b2:
        r6 = r77;
        r5 = r78;
        r2 = r85;
    L_0x07b8:
        r3 = r2.mStartWallTime;
        r7 = 0;
        r3 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r3 <= 0) goto L_0x08e8;
    L_0x07c0:
        r3 = r2.mEndWallTime;
        r7 = r2.mStartWallTime;
        r3 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1));
        if (r3 <= 0) goto L_0x08e8;
    L_0x07c8:
        r3 = r85.is24Hour();
        r4 = java.util.Calendar.getInstance();
        r7 = r2.mStartWallTime;
        r4.setTimeInMillis(r7);
        r7 = 14;
        r4.set(r7, r6);
        r8 = 13;
        r4.set(r8, r6);
        r9 = 12;
        r4.set(r9, r6);
        r10 = r4.getTimeInMillis();
        r12 = r2.mStartWallTime;
        r12 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1));
        r13 = 11;
        if (r12 >= 0) goto L_0x07fe;
    L_0x07f0:
        r12 = r4.get(r13);
        r14 = 1;
        r12 = r12 + r14;
        r4.set(r13, r12);
        r10 = r4.getTimeInMillis();
        goto L_0x07ff;
    L_0x07fe:
        r14 = 1;
    L_0x07ff:
        r12 = java.util.Calendar.getInstance();
        r13 = r2.mEndWallTime;
        r12.setTimeInMillis(r13);
        r12.set(r7, r6);
        r12.set(r8, r6);
        r12.set(r9, r6);
        r13 = r12.getTimeInMillis();
        r15 = (r10 > r13 ? 1 : (r10 == r13 ? 0 : -1));
        r38 = 2;
        if (r15 >= 0) goto L_0x0864;
    L_0x081b:
        r15 = r2.mLevelLeft;
        r9 = r2.mLevelRight;
        r2.addTimeLabel(r4, r15, r9, r3);
        r9 = java.util.Calendar.getInstance();
        r6 = r2.mStartWallTime;
        r80 = r9;
        r8 = r2.mEndWallTime;
        r81 = r1;
        r0 = r2.mStartWallTime;
        r8 = r8 - r0;
        r8 = r8 / r38;
        r6 = r6 + r8;
        r0 = r80;
        r0.setTimeInMillis(r6);
        r1 = 0;
        r6 = 14;
        r0.set(r6, r1);
        r6 = 13;
        r0.set(r6, r1);
        r6 = 12;
        r0.set(r6, r1);
        r6 = r0.getTimeInMillis();
        r1 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1));
        if (r1 <= 0) goto L_0x085c;
    L_0x0851:
        r1 = (r6 > r13 ? 1 : (r6 == r13 ? 0 : -1));
        if (r1 >= 0) goto L_0x085c;
    L_0x0855:
        r1 = r2.mLevelLeft;
        r8 = r2.mLevelRight;
        r2.addTimeLabel(r0, r1, r8, r3);
    L_0x085c:
        r1 = r2.mLevelLeft;
        r8 = r2.mLevelRight;
        r2.addTimeLabel(r12, r1, r8, r3);
        goto L_0x0866;
    L_0x0864:
        r81 = r1;
    L_0x0866:
        r0 = 6;
        r1 = r4.get(r0);
        r6 = r12.get(r0);
        if (r1 != r6) goto L_0x087c;
    L_0x0871:
        r1 = 1;
        r6 = r4.get(r1);
        r7 = r12.get(r1);
        if (r6 == r7) goto L_0x08ea;
    L_0x087c:
        r1 = r85.isDayFirst();
        r6 = 0;
        r7 = 11;
        r4.set(r7, r6);
        r6 = r4.getTimeInMillis();
        r8 = r2.mStartWallTime;
        r8 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r8 >= 0) goto L_0x089d;
    L_0x0890:
        r8 = r4.get(r0);
        r9 = 1;
        r8 = r8 + r9;
        r4.set(r0, r8);
        r6 = r4.getTimeInMillis();
    L_0x089d:
        r0 = 0;
        r8 = 11;
        r12.set(r8, r0);
        r8 = r12.getTimeInMillis();
        r0 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r0 >= 0) goto L_0x08e0;
    L_0x08ab:
        r0 = r2.mLevelLeft;
        r10 = r2.mLevelRight;
        r2.addDateLabel(r4, r0, r10, r1);
        r0 = java.util.Calendar.getInstance();
        r10 = r8 - r6;
        r10 = r10 / r38;
        r10 = r10 + r6;
        r13 = 7200000; // 0x6ddd00 float:1.0089349E-38 double:3.5572727E-317;
        r10 = r10 + r13;
        r0.setTimeInMillis(r10);
        r10 = 0;
        r11 = 11;
        r0.set(r11, r10);
        r11 = 12;
        r0.set(r11, r10);
        r10 = r0.getTimeInMillis();
        r13 = (r10 > r6 ? 1 : (r10 == r6 ? 0 : -1));
        if (r13 <= 0) goto L_0x08e0;
    L_0x08d5:
        r13 = (r10 > r8 ? 1 : (r10 == r8 ? 0 : -1));
        if (r13 >= 0) goto L_0x08e0;
    L_0x08d9:
        r13 = r2.mLevelLeft;
        r14 = r2.mLevelRight;
        r2.addDateLabel(r0, r13, r14, r1);
    L_0x08e0:
        r0 = r2.mLevelLeft;
        r10 = r2.mLevelRight;
        r2.addDateLabel(r12, r0, r10, r1);
        goto L_0x08ea;
    L_0x08e8:
        r81 = r1;
    L_0x08ea:
        r0 = r2.mTimeLabels;
        r0 = r0.size();
        r1 = 2;
        if (r0 >= r1) goto L_0x090e;
    L_0x08f3:
        r0 = r85.getContext();
        r3 = r2.mEndWallTime;
        r6 = r2.mStartWallTime;
        r3 = r3 - r6;
        r0 = android.text.format.Formatter.formatShortElapsedTime(r0, r3);
        r2.mDurationString = r0;
        r0 = r2.mTextPaint;
        r1 = r2.mDurationString;
        r0 = r0.measureText(r1);
        r0 = (int) r0;
        r2.mDurationStringWidth = r0;
        goto L_0x0914;
    L_0x090e:
        r0 = 0;
        r2.mDurationString = r0;
        r0 = 0;
        r2.mDurationStringWidth = r0;
    L_0x0914:
        return;
    L_0x0915:
        r2 = r15;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.BatteryHistoryChart.onSizeChanged(int, int, int, int):void");
    }

    /* Access modifiers changed, original: 0000 */
    public void addTimeLabel(Calendar cal, int levelLeft, int levelRight, boolean is24hr) {
        long walltimeStart = this.mStartWallTime;
        this.mTimeLabels.add(new TimeLabel(this.mTextPaint, ((int) (((cal.getTimeInMillis() - walltimeStart) * ((long) (levelRight - levelLeft))) / (this.mEndWallTime - walltimeStart))) + levelLeft, cal, is24hr));
    }

    /* Access modifiers changed, original: 0000 */
    public void addDateLabel(Calendar cal, int levelLeft, int levelRight, boolean isDayFirst) {
        long walltimeStart = this.mStartWallTime;
        this.mDateLabels.add(new DateLabel(this.mTextPaint, ((int) (((cal.getTimeInMillis() - walltimeStart) * ((long) (levelRight - levelLeft))) / (this.mEndWallTime - walltimeStart))) + levelLeft, cal, isDayFirst));
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas, getWidth(), getHeight());
    }

    /* Access modifiers changed, original: 0000 */
    public void buildBitmap(int width, int height) {
        if (this.mBitmap == null || width != this.mBitmap.getWidth() || height != this.mBitmap.getHeight()) {
            this.mBitmap = Bitmap.createBitmap(getResources().getDisplayMetrics(), width, height, Config.ARGB_8888);
            this.mCanvas = new Canvas(this.mBitmap);
            drawChart(this.mCanvas, width, height);
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0460  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x045c  */
    public void drawChart(android.graphics.Canvas r34, int r35, int r36) {
        /*
        r33 = this;
        r0 = r33;
        r7 = r34;
        r8 = r35;
        r10 = r33.isLayoutRtl();
        if (r10 == 0) goto L_0x000e;
    L_0x000c:
        r1 = r8;
        goto L_0x000f;
    L_0x000e:
        r1 = 0;
    L_0x000f:
        r12 = r1;
        if (r10 == 0) goto L_0x0014;
    L_0x0012:
        r1 = 0;
        goto L_0x0015;
    L_0x0014:
        r1 = r8;
    L_0x0015:
        r13 = r1;
        if (r10 == 0) goto L_0x001b;
    L_0x0018:
        r1 = android.graphics.Paint.Align.RIGHT;
        goto L_0x001d;
    L_0x001b:
        r1 = android.graphics.Paint.Align.LEFT;
    L_0x001d:
        r14 = r1;
        if (r10 == 0) goto L_0x0023;
    L_0x0020:
        r1 = android.graphics.Paint.Align.LEFT;
        goto L_0x0025;
    L_0x0023:
        r1 = android.graphics.Paint.Align.RIGHT;
    L_0x0025:
        r15 = r1;
        r1 = r0.mBatLevelPath;
        r2 = r0.mBatteryBackgroundPaint;
        r7.drawPath(r1, r2);
        r1 = r0.mTimeRemainPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x003c;
    L_0x0035:
        r1 = r0.mTimeRemainPath;
        r2 = r0.mTimeRemainPaint;
        r7.drawPath(r1, r2);
    L_0x003c:
        r1 = r0.mTimeLabels;
        r1 = r1.size();
        r6 = 1;
        if (r1 <= r6) goto L_0x016c;
    L_0x0045:
        r1 = r0.mLevelBottom;
        r2 = r0.mTextAscent;
        r1 = r1 - r2;
        r2 = r0.mThinLineWidth;
        r2 = r2 * 4;
        r5 = r1 + r2;
        r1 = r0.mLevelBottom;
        r2 = r0.mThinLineWidth;
        r1 = r1 + r2;
        r2 = r0.mThinLineWidth;
        r2 = r2 / 2;
        r4 = r1 + r2;
        r1 = r0.mTextPaint;
        r2 = android.graphics.Paint.Align.LEFT;
        r1.setTextAlign(r2);
        r1 = 0;
        r16 = r1;
        r1 = 0;
    L_0x0066:
        r3 = r1;
        r1 = r0.mTimeLabels;
        r1 = r1.size();
        if (r3 >= r1) goto L_0x0165;
    L_0x006f:
        r1 = r0.mTimeLabels;
        r1 = r1.get(r3);
        r2 = r1;
        r2 = (com.android.settings.fuelgauge.BatteryHistoryChart.TimeLabel) r2;
        if (r3 != 0) goto L_0x00c4;
    L_0x007a:
        r1 = r2.x;
        r6 = r2.width;
        r6 = r6 / 2;
        r1 = r1 - r6;
        if (r1 >= 0) goto L_0x0084;
    L_0x0083:
        r1 = 0;
    L_0x0084:
        r6 = r1;
        r1 = r2.label;
        r11 = (float) r6;
        r19 = r3;
        r3 = (float) r5;
        r20 = r5;
        r5 = r0.mTextPaint;
        r7.drawText(r1, r11, r3, r5);
        r1 = r2.x;
        r3 = (float) r1;
        r5 = (float) r4;
        r1 = r2.x;
        r11 = (float) r1;
        r1 = r0.mThinLineWidth;
        r1 = r1 + r4;
        r1 = (float) r1;
        r21 = r6;
        r6 = r0.mTextPaint;
        r22 = r1;
        r1 = r7;
        r9 = r2;
        r2 = r3;
        r23 = r13;
        r13 = r19;
        r3 = r5;
        r5 = r4;
        r4 = r11;
        r24 = r15;
        r11 = r20;
        r15 = r5;
        r5 = r22;
        r17 = r21;
        r19 = 1;
        r1.drawLine(r2, r3, r4, r5, r6);
        r1 = r9.width;
        r6 = r17 + r1;
    L_0x00c0:
        r16 = r6;
        goto L_0x0159;
    L_0x00c4:
        r9 = r2;
        r11 = r5;
        r19 = r6;
        r23 = r13;
        r24 = r15;
        r13 = r3;
        r15 = r4;
        r1 = r0.mTimeLabels;
        r1 = r1.size();
        r1 = r1 + -1;
        if (r13 >= r1) goto L_0x0126;
    L_0x00d8:
        r1 = r9.x;
        r2 = r9.width;
        r2 = r2 / 2;
        r6 = r1 - r2;
        r1 = r0.mTextAscent;
        r1 = r16 + r1;
        if (r6 >= r1) goto L_0x00e8;
    L_0x00e6:
        goto L_0x0159;
    L_0x00e8:
        r1 = r0.mTimeLabels;
        r3 = r13 + 1;
        r1 = r1.get(r3);
        r5 = r1;
        r5 = (com.android.settings.fuelgauge.BatteryHistoryChart.TimeLabel) r5;
        r1 = r5.width;
        r1 = r8 - r1;
        r2 = r0.mTextAscent;
        r1 = r1 - r2;
        if (r6 <= r1) goto L_0x00fd;
    L_0x00fc:
        goto L_0x0159;
    L_0x00fd:
        r1 = r9.label;
        r2 = (float) r6;
        r3 = (float) r11;
        r4 = r0.mTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r9.x;
        r2 = (float) r1;
        r3 = (float) r15;
        r1 = r9.x;
        r4 = (float) r1;
        r1 = r0.mThinLineWidth;
        r1 = r1 + r15;
        r1 = (float) r1;
        r25 = r6;
        r6 = r0.mTextPaint;
        r17 = r1;
        r1 = r7;
        r20 = r5;
        r5 = r17;
        r17 = r25;
        r1.drawLine(r2, r3, r4, r5, r6);
        r1 = r9.width;
        r6 = r17 + r1;
        goto L_0x00c0;
    L_0x0126:
        r1 = r9.x;
        r2 = r9.width;
        r2 = r2 / 2;
        r1 = r1 - r2;
        r2 = r9.width;
        r2 = r2 + r1;
        if (r2 < r8) goto L_0x0138;
    L_0x0132:
        r2 = r8 + -1;
        r3 = r9.width;
        r1 = r2 - r3;
    L_0x0138:
        r6 = r1;
        r1 = r9.label;
        r2 = (float) r6;
        r3 = (float) r11;
        r4 = r0.mTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r9.x;
        r2 = (float) r1;
        r3 = (float) r15;
        r1 = r9.x;
        r4 = (float) r1;
        r1 = r0.mThinLineWidth;
        r1 = r1 + r15;
        r5 = (float) r1;
        r1 = r0.mTextPaint;
        r17 = r1;
        r1 = r7;
        r20 = r6;
        r6 = r17;
        r1.drawLine(r2, r3, r4, r5, r6);
    L_0x0159:
        r1 = r13 + 1;
        r5 = r11;
        r4 = r15;
        r6 = r19;
        r13 = r23;
        r15 = r24;
        goto L_0x0066;
    L_0x0165:
        r19 = r6;
        r23 = r13;
        r24 = r15;
        goto L_0x019f;
    L_0x016c:
        r19 = r6;
        r23 = r13;
        r24 = r15;
        r1 = r0.mDurationString;
        if (r1 == 0) goto L_0x019f;
    L_0x0176:
        r1 = r0.mLevelBottom;
        r2 = r0.mTextAscent;
        r1 = r1 - r2;
        r2 = r0.mThinLineWidth;
        r2 = r2 * 4;
        r1 = r1 + r2;
        r2 = r0.mTextPaint;
        r3 = android.graphics.Paint.Align.LEFT;
        r2.setTextAlign(r3);
        r2 = r0.mDurationString;
        r3 = r0.mLevelLeft;
        r4 = r0.mLevelRight;
        r5 = r0.mLevelLeft;
        r4 = r4 - r5;
        r4 = r4 / 2;
        r3 = r3 + r4;
        r4 = r0.mDurationStringWidth;
        r4 = r4 / 2;
        r3 = r3 - r4;
        r3 = (float) r3;
        r4 = (float) r1;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x019f:
        r1 = r0.mHeaderTextAscent;
        r1 = -r1;
        r2 = r0.mHeaderTextDescent;
        r3 = r0.mHeaderTextAscent;
        r2 = r2 - r3;
        r2 = r2 / 3;
        r9 = r1 + r2;
        r1 = r0.mHeaderTextPaint;
        r1.setTextAlign(r14);
        r1 = r0.mInfo;
        r1 = r1.chargeLabel;
        r1 = r1.toString();
        r2 = (float) r12;
        r3 = (float) r9;
        r4 = r0.mHeaderTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r0.mChargeDurationStringWidth;
        r1 = r1 / 2;
        if (r10 == 0) goto L_0x01c6;
    L_0x01c5:
        r1 = -r1;
    L_0x01c6:
        r11 = r1;
        r1 = r0.mChargeDurationStringWidth;
        r1 = r8 - r1;
        r2 = r0.mDrainStringWidth;
        r1 = r1 - r2;
        r1 = r1 / 2;
        if (r10 == 0) goto L_0x01d5;
    L_0x01d2:
        r2 = r0.mDrainStringWidth;
        goto L_0x01d7;
    L_0x01d5:
        r2 = r0.mChargeLabelStringWidth;
    L_0x01d7:
        r13 = r1 + r2;
        r1 = r0.mChargeDurationString;
        r2 = r13 - r11;
        r2 = (float) r2;
        r3 = (float) r9;
        r4 = r0.mHeaderTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r0.mHeaderTextPaint;
        r15 = r24;
        r1.setTextAlign(r15);
        r1 = r0.mDrainString;
        r6 = r23;
        r2 = (float) r6;
        r3 = (float) r9;
        r4 = r0.mHeaderTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r0.mBatGoodPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0205;
    L_0x01fe:
        r1 = r0.mBatGoodPath;
        r2 = r0.mBatteryGoodPaint;
        r7.drawPath(r1, r2);
    L_0x0205:
        r1 = r0.mBatWarnPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0214;
    L_0x020d:
        r1 = r0.mBatWarnPath;
        r2 = r0.mBatteryWarnPaint;
        r7.drawPath(r1, r2);
    L_0x0214:
        r1 = r0.mBatCriticalPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0223;
    L_0x021c:
        r1 = r0.mBatCriticalPath;
        r2 = r0.mBatteryCriticalPaint;
        r7.drawPath(r1, r2);
    L_0x0223:
        r1 = r0.mHavePhoneSignal;
        if (r1 == 0) goto L_0x0237;
    L_0x0227:
        r1 = r0.mPhoneSignalOffset;
        r1 = r36 - r1;
        r2 = r0.mLineWidth;
        r2 = r2 / 2;
        r1 = r1 - r2;
        r2 = r0.mPhoneSignalChart;
        r3 = r0.mLineWidth;
        r2.draw(r7, r1, r3);
    L_0x0237:
        r1 = r0.mScreenOnPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0246;
    L_0x023f:
        r1 = r0.mScreenOnPath;
        r2 = r0.mScreenOnPaint;
        r7.drawPath(r1, r2);
    L_0x0246:
        r1 = r0.mChargingPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0255;
    L_0x024e:
        r1 = r0.mChargingPath;
        r2 = r0.mChargingPaint;
        r7.drawPath(r1, r2);
    L_0x0255:
        r1 = r0.mHaveGps;
        if (r1 == 0) goto L_0x0268;
    L_0x0259:
        r1 = r0.mGpsOnPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x0268;
    L_0x0261:
        r1 = r0.mGpsOnPath;
        r2 = r0.mGpsOnPaint;
        r7.drawPath(r1, r2);
    L_0x0268:
        r1 = r0.mHaveFlashlight;
        if (r1 == 0) goto L_0x027b;
    L_0x026c:
        r1 = r0.mFlashlightOnPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x027b;
    L_0x0274:
        r1 = r0.mFlashlightOnPath;
        r2 = r0.mFlashlightOnPaint;
        r7.drawPath(r1, r2);
    L_0x027b:
        r1 = r0.mHaveCamera;
        if (r1 == 0) goto L_0x028e;
    L_0x027f:
        r1 = r0.mCameraOnPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x028e;
    L_0x0287:
        r1 = r0.mCameraOnPath;
        r2 = r0.mCameraOnPaint;
        r7.drawPath(r1, r2);
    L_0x028e:
        r1 = r0.mHaveWifi;
        if (r1 == 0) goto L_0x02a1;
    L_0x0292:
        r1 = r0.mWifiRunningPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x02a1;
    L_0x029a:
        r1 = r0.mWifiRunningPath;
        r2 = r0.mWifiRunningPaint;
        r7.drawPath(r1, r2);
    L_0x02a1:
        r1 = r0.mCpuRunningPath;
        r1 = r1.isEmpty();
        if (r1 != 0) goto L_0x02b0;
    L_0x02a9:
        r1 = r0.mCpuRunningPath;
        r2 = r0.mCpuRunningPaint;
        r7.drawPath(r1, r2);
    L_0x02b0:
        r1 = r0.mLargeMode;
        if (r1 == 0) goto L_0x0358;
    L_0x02b4:
        r1 = r0.mTextPaint;
        r1 = r1.getTextAlign();
        r2 = r0.mTextPaint;
        r2.setTextAlign(r14);
        r2 = r0.mHavePhoneSignal;
        if (r2 == 0) goto L_0x02d3;
    L_0x02c3:
        r2 = r0.mPhoneSignalLabel;
        r3 = (float) r12;
        r4 = r0.mPhoneSignalOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x02d3:
        r2 = r0.mHaveGps;
        if (r2 == 0) goto L_0x02e7;
    L_0x02d7:
        r2 = r0.mGpsOnLabel;
        r3 = (float) r12;
        r4 = r0.mGpsOnOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x02e7:
        r2 = r0.mHaveFlashlight;
        if (r2 == 0) goto L_0x02fb;
    L_0x02eb:
        r2 = r0.mFlashlightOnLabel;
        r3 = (float) r12;
        r4 = r0.mFlashlightOnOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x02fb:
        r2 = r0.mHaveCamera;
        if (r2 == 0) goto L_0x030f;
    L_0x02ff:
        r2 = r0.mCameraOnLabel;
        r3 = (float) r12;
        r4 = r0.mCameraOnOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x030f:
        r2 = r0.mHaveWifi;
        if (r2 == 0) goto L_0x0323;
    L_0x0313:
        r2 = r0.mWifiRunningLabel;
        r3 = (float) r12;
        r4 = r0.mWifiRunningOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
    L_0x0323:
        r2 = r0.mCpuRunningLabel;
        r3 = (float) r12;
        r4 = r0.mCpuRunningOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
        r2 = r0.mChargingLabel;
        r3 = (float) r12;
        r4 = r0.mChargingOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
        r2 = r0.mScreenOnLabel;
        r3 = (float) r12;
        r4 = r0.mScreenOnOffset;
        r4 = r36 - r4;
        r5 = r0.mTextDescent;
        r4 = r4 - r5;
        r4 = (float) r4;
        r5 = r0.mTextPaint;
        r7.drawText(r2, r3, r4, r5);
        r2 = r0.mTextPaint;
        r2.setTextAlign(r1);
    L_0x0358:
        r1 = r0.mLevelLeft;
        r2 = r0.mThinLineWidth;
        r1 = r1 - r2;
        r2 = (float) r1;
        r1 = r0.mLevelTop;
        r3 = (float) r1;
        r1 = r0.mLevelLeft;
        r4 = r0.mThinLineWidth;
        r1 = r1 - r4;
        r4 = (float) r1;
        r1 = r0.mLevelBottom;
        r5 = r0.mThinLineWidth;
        r5 = r5 / 2;
        r1 = r1 + r5;
        r5 = (float) r1;
        r1 = r0.mTextPaint;
        r17 = r1;
        r1 = r7;
        r20 = r6;
        r6 = r17;
        r1.drawLine(r2, r3, r4, r5, r6);
        r1 = r0.mLargeMode;
        if (r1 == 0) goto L_0x03c4;
    L_0x037f:
        r18 = 0;
    L_0x0381:
        r6 = r18;
        r1 = 10;
        if (r6 >= r1) goto L_0x03c4;
    L_0x0387:
        r2 = r0.mLevelTop;
        r3 = r0.mThinLineWidth;
        r3 = r3 / 2;
        r2 = r2 + r3;
        r3 = r0.mLevelBottom;
        r4 = r0.mLevelTop;
        r3 = r3 - r4;
        r3 = r3 * r6;
        r3 = r3 / r1;
        r5 = r2 + r3;
        r1 = r0.mLevelLeft;
        r2 = r0.mThinLineWidth;
        r2 = r2 * 2;
        r1 = r1 - r2;
        r2 = r0.mThinLineWidth;
        r2 = r2 / 2;
        r1 = r1 - r2;
        r2 = (float) r1;
        r3 = (float) r5;
        r1 = r0.mLevelLeft;
        r4 = r0.mThinLineWidth;
        r1 = r1 - r4;
        r4 = r0.mThinLineWidth;
        r4 = r4 / 2;
        r1 = r1 - r4;
        r4 = (float) r1;
        r1 = (float) r5;
        r26 = r6;
        r6 = r0.mTextPaint;
        r17 = r1;
        r1 = r7;
        r18 = r5;
        r5 = r17;
        r17 = r26;
        r1.drawLine(r2, r3, r4, r5, r6);
        r18 = r17 + 1;
        goto L_0x0381;
    L_0x03c4:
        r1 = r0.mMaxPercentLabelString;
        r2 = 0;
        r3 = r0.mLevelTop;
        r3 = (float) r3;
        r4 = r0.mTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r0.mMinPercentLabelString;
        r2 = r0.mMaxPercentLabelStringWidth;
        r3 = r0.mMinPercentLabelStringWidth;
        r2 = r2 - r3;
        r2 = (float) r2;
        r3 = r0.mLevelBottom;
        r4 = r0.mThinLineWidth;
        r3 = r3 - r4;
        r3 = (float) r3;
        r4 = r0.mTextPaint;
        r7.drawText(r1, r2, r3, r4);
        r1 = r0.mLevelLeft;
        r1 = r1 / 2;
        r2 = (float) r1;
        r1 = r0.mLevelBottom;
        r3 = r0.mThinLineWidth;
        r1 = r1 + r3;
        r3 = (float) r1;
        r4 = (float) r8;
        r1 = r0.mLevelBottom;
        r5 = r0.mThinLineWidth;
        r1 = r1 + r5;
        r5 = (float) r1;
        r6 = r0.mTextPaint;
        r1 = r7;
        r1.drawLine(r2, r3, r4, r5, r6);
        r1 = r0.mDateLabels;
        r1 = r1.size();
        if (r1 <= 0) goto L_0x049d;
    L_0x0402:
        r1 = r0.mLevelTop;
        r2 = r0.mTextAscent;
        r1 = r1 + r2;
        r2 = r0.mLevelBottom;
        r3 = r0.mLevelRight;
        r4 = r0.mTextPaint;
        r5 = android.graphics.Paint.Align.LEFT;
        r4.setTextAlign(r5);
        r4 = r0.mDateLabels;
        r4 = r4.size();
        r4 = r4 + -1;
    L_0x041a:
        if (r4 < 0) goto L_0x049d;
    L_0x041c:
        r5 = r0.mDateLabels;
        r5 = r5.get(r4);
        r5 = (com.android.settings.fuelgauge.BatteryHistoryChart.DateLabel) r5;
        r6 = r5.x;
        r8 = r0.mThinLineWidth;
        r6 = r6 - r8;
        r8 = r5.x;
        r27 = r6;
        r6 = r0.mThinLineWidth;
        r6 = r6 * 2;
        r8 = r8 + r6;
        r6 = r5.width;
        r6 = r6 + r8;
        if (r6 < r3) goto L_0x0452;
    L_0x0437:
        r6 = r5.x;
        r28 = r8;
        r8 = r0.mThinLineWidth;
        r8 = r8 * 2;
        r6 = r6 - r8;
        r8 = r5.width;
        r8 = r6 - r8;
        r6 = r0.mThinLineWidth;
        r6 = r8 - r6;
        if (r6 < r3) goto L_0x0456;
        r32 = r1;
        r29 = r3;
    L_0x044f:
        r31 = r9;
        goto L_0x0491;
    L_0x0452:
        r28 = r8;
        r6 = r27;
    L_0x0456:
        r29 = r3;
        r3 = r0.mLevelLeft;
        if (r6 >= r3) goto L_0x0460;
        r32 = r1;
        goto L_0x044f;
    L_0x0460:
        r3 = r0.mDateLinePath;
        r3.reset();
        r3 = r0.mDateLinePath;
        r30 = r6;
        r6 = r5.x;
        r6 = (float) r6;
        r31 = r9;
        r9 = (float) r1;
        r3.moveTo(r6, r9);
        r3 = r0.mDateLinePath;
        r6 = r5.x;
        r6 = (float) r6;
        r9 = (float) r2;
        r3.lineTo(r6, r9);
        r3 = r0.mDateLinePath;
        r6 = r0.mDateLinePaint;
        r7.drawPath(r3, r6);
        r3 = r5.label;
        r6 = (float) r8;
        r9 = r0.mTextAscent;
        r9 = r1 - r9;
        r9 = (float) r9;
        r32 = r1;
        r1 = r0.mTextPaint;
        r7.drawText(r3, r6, r9, r1);
    L_0x0491:
        r4 = r4 + -1;
        r3 = r29;
        r9 = r31;
        r1 = r32;
        r8 = r35;
        goto L_0x041a;
    L_0x049d:
        r31 = r9;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.BatteryHistoryChart.drawChart(android.graphics.Canvas, int, int):void");
    }
}
