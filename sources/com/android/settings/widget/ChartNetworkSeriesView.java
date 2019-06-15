package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R;

public class ChartNetworkSeriesView extends View {
    private static final boolean ESTIMATE_ENABLED = false;
    private static final boolean LOGD = false;
    private static final String TAG = "ChartNetworkSeriesView";
    private long mEnd;
    private long mEndTime;
    private boolean mEstimateVisible;
    private ChartAxis mHoriz;
    private long mMax;
    private long mMaxEstimate;
    private Paint mPaintEstimate;
    private Paint mPaintFill;
    private Paint mPaintFillSecondary;
    private Paint mPaintStroke;
    private Path mPathEstimate;
    private Path mPathFill;
    private Path mPathStroke;
    private boolean mPathValid;
    private int mSafeRegion;
    private boolean mSecondary;
    private long mStart;
    private NetworkStatsHistory mStats;
    private ChartAxis mVert;

    public ChartNetworkSeriesView(Context context) {
        this(context, null, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartNetworkSeriesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mEndTime = Long.MIN_VALUE;
        this.mPathValid = false;
        this.mEstimateVisible = false;
        this.mSecondary = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChartNetworkSeriesView, defStyle, 0);
        int stroke = a.getColor(3, -65536);
        int fill = a.getColor(0, -65536);
        int fillSecondary = a.getColor(1, -65536);
        int safeRegion = a.getDimensionPixelSize(2, 0);
        setChartColor(stroke, fill, fillSecondary);
        setSafeRegion(safeRegion);
        setWillNotDraw(false);
        a.recycle();
        this.mPathStroke = new Path();
        this.mPathFill = new Path();
        this.mPathEstimate = new Path();
    }

    /* Access modifiers changed, original: 0000 */
    public void init(ChartAxis horiz, ChartAxis vert) {
        this.mHoriz = (ChartAxis) Preconditions.checkNotNull(horiz, "missing horiz");
        this.mVert = (ChartAxis) Preconditions.checkNotNull(vert, "missing vert");
    }

    public void setChartColor(int stroke, int fill, int fillSecondary) {
        this.mPaintStroke = new Paint();
        this.mPaintStroke.setStrokeWidth(4.0f * getResources().getDisplayMetrics().density);
        this.mPaintStroke.setColor(stroke);
        this.mPaintStroke.setStyle(Style.STROKE);
        this.mPaintStroke.setAntiAlias(true);
        this.mPaintFill = new Paint();
        this.mPaintFill.setColor(fill);
        this.mPaintFill.setStyle(Style.FILL);
        this.mPaintFill.setAntiAlias(true);
        this.mPaintFillSecondary = new Paint();
        this.mPaintFillSecondary.setColor(fillSecondary);
        this.mPaintFillSecondary.setStyle(Style.FILL);
        this.mPaintFillSecondary.setAntiAlias(true);
        this.mPaintEstimate = new Paint();
        this.mPaintEstimate.setStrokeWidth(3.0f);
        this.mPaintEstimate.setColor(fillSecondary);
        this.mPaintEstimate.setStyle(Style.STROKE);
        this.mPaintEstimate.setAntiAlias(true);
        this.mPaintEstimate.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 1.0f));
    }

    public void setSafeRegion(int safeRegion) {
        this.mSafeRegion = safeRegion;
    }

    public void bindNetworkStats(NetworkStatsHistory stats) {
        this.mStats = stats;
        invalidatePath();
        invalidate();
    }

    public void setBounds(long start, long end) {
        this.mStart = start;
        this.mEnd = end;
    }

    public void setSecondary(boolean secondary) {
        this.mSecondary = secondary;
    }

    public void invalidatePath() {
        this.mPathValid = false;
        this.mMax = 0;
        invalidate();
    }

    private void generatePath() {
        this.mMax = 0;
        this.mPathStroke.reset();
        this.mPathFill.reset();
        this.mPathEstimate.reset();
        this.mPathValid = true;
        if (this.mStats != null && this.mStats.size() >= 2) {
            int start;
            int end;
            boolean started;
            int height;
            int width = getWidth();
            int height2 = getHeight();
            boolean started2 = false;
            float lastY = (float) height2;
            long lastTime = this.mHoriz.convertToValue(0.0f);
            this.mPathStroke.moveTo(0.0f, lastY);
            this.mPathFill.moveTo(0.0f, lastY);
            long totalData = 0;
            Entry entry = null;
            int start2 = this.mStats.getIndexBefore(this.mStart);
            int end2 = this.mStats.getIndexAfter(this.mEnd);
            float lastX = 0.0f;
            int i = start2;
            while (i <= end2) {
                entry = this.mStats.getValues(i, entry);
                long startTime = entry.bucketStart;
                start = start2;
                end = end2;
                long endTime = entry.bucketDuration + startTime;
                int width2 = width;
                width = this.mHoriz.convertToPoint(startTime);
                started = started2;
                float endX = this.mHoriz.convertToPoint(endTime);
                if (endX < 0.0f) {
                    height = height2;
                } else {
                    long endTime2 = endTime;
                    height = height2;
                    float endX2 = endX;
                    totalData += entry.rxBytes + entry.txBytes;
                    float startY = lastY;
                    endX = this.mVert.convertToPoint(totalData);
                    if (lastTime != startTime) {
                        this.mPathStroke.lineTo(width, startY);
                        this.mPathFill.lineTo(width, startY);
                    }
                    float endX3 = endX2;
                    this.mPathStroke.lineTo(endX3, endX);
                    this.mPathFill.lineTo(endX3, endX);
                    lastY = endX;
                    lastTime = endTime2;
                    lastX = endX3;
                }
                i++;
                start2 = start;
                end2 = end;
                width = width2;
                started2 = started;
                height2 = height;
            }
            height = height2;
            started = started2;
            start = start2;
            end = end2;
            if (lastTime < this.mEndTime) {
                lastX = this.mHoriz.convertToPoint(this.mEndTime);
                this.mPathStroke.lineTo(lastX, lastY);
                this.mPathFill.lineTo(lastX, lastY);
            }
            height2 = height;
            this.mPathFill.lineTo(lastX, (float) height2);
            this.mPathFill.lineTo(0.0f, (float) height2);
            this.mMax = totalData;
            invalidate();
        }
    }

    public void setEndTime(long endTime) {
        this.mEndTime = endTime;
    }

    public void setEstimateVisible(boolean estimateVisible) {
        this.mEstimateVisible = false;
        invalidate();
    }

    public long getMaxEstimate() {
        return this.mMaxEstimate;
    }

    public long getMaxVisible() {
        long maxVisible = this.mEstimateVisible ? this.mMaxEstimate : this.mMax;
        if (maxVisible > 0 || this.mStats == null) {
            return maxVisible;
        }
        Entry entry = this.mStats.getValues(this.mStart, this.mEnd, null);
        return entry.rxBytes + entry.txBytes;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        if (!this.mPathValid) {
            generatePath();
        }
        if (this.mEstimateVisible) {
            int save = canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            canvas.drawPath(this.mPathEstimate, this.mPaintEstimate);
            canvas.restoreToCount(save);
        }
        Paint paintFill = this.mSecondary ? this.mPaintFillSecondary : this.mPaintFill;
        int save2 = canvas.save();
        canvas.clipRect(this.mSafeRegion, 0, getWidth(), getHeight() - this.mSafeRegion);
        canvas.drawPath(this.mPathFill, paintFill);
        canvas.restoreToCount(save2);
    }
}
