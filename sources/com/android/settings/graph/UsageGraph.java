package com.android.settings.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import com.android.settings.R;
import com.android.settings.fuelgauge.BatteryUtils;

public class UsageGraph extends View {
    public static final String LOG_TAG = "UsageGraph";
    private static final int PATH_DELIM = -1;
    private int mAccentColor;
    private final int mCornerRadius;
    private final Drawable mDivider;
    private final int mDividerSize;
    private final Paint mDottedPaint;
    private final Paint mFillPaint;
    private final Paint mLinePaint;
    private final SparseIntArray mLocalPaths = new SparseIntArray();
    private final SparseIntArray mLocalProjectedPaths = new SparseIntArray();
    private float mMaxX = 100.0f;
    private float mMaxY = 100.0f;
    private float mMiddleDividerLoc = 0.5f;
    private int mMiddleDividerTint = -1;
    private final Path mPath = new Path();
    private final SparseIntArray mPaths = new SparseIntArray();
    private final SparseIntArray mProjectedPaths = new SparseIntArray();
    private final Drawable mTintedDivider;
    private Paint mTintedPaint = new Paint();
    private int mTopDividerTint = -1;

    public UsageGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = context.getResources();
        this.mLinePaint = new Paint();
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeCap(Cap.ROUND);
        this.mLinePaint.setStrokeJoin(Join.ROUND);
        this.mLinePaint.setAntiAlias(true);
        this.mCornerRadius = resources.getDimensionPixelSize(R.dimen.usage_graph_line_corner_radius);
        this.mLinePaint.setPathEffect(new CornerPathEffect((float) this.mCornerRadius));
        this.mLinePaint.setStrokeWidth((float) resources.getDimensionPixelSize(R.dimen.usage_graph_line_width));
        this.mFillPaint = new Paint(this.mLinePaint);
        this.mFillPaint.setStyle(Style.FILL);
        this.mDottedPaint = new Paint(this.mLinePaint);
        this.mDottedPaint.setStyle(Style.STROKE);
        float interval = (float) resources.getDimensionPixelSize(R.dimen.usage_graph_dot_interval);
        this.mDottedPaint.setStrokeWidth(3.0f * ((float) resources.getDimensionPixelSize(R.dimen.usage_graph_dot_size)));
        this.mDottedPaint.setPathEffect(new DashPathEffect(new float[]{dots, interval}, 0.0f));
        this.mDottedPaint.setColor(context.getColor(R.color.usage_graph_dots));
        TypedValue v = new TypedValue();
        context.getTheme().resolveAttribute(16843284, v, true);
        this.mDivider = context.getDrawable(v.resourceId);
        this.mTintedDivider = context.getDrawable(v.resourceId);
        this.mDividerSize = resources.getDimensionPixelSize(R.dimen.usage_graph_divider_size);
    }

    /* Access modifiers changed, original: 0000 */
    public void clearPaths() {
        this.mPaths.clear();
        this.mLocalPaths.clear();
        this.mProjectedPaths.clear();
        this.mLocalProjectedPaths.clear();
    }

    /* Access modifiers changed, original: 0000 */
    public void setMax(int maxX, int maxY) {
        long startTime = System.currentTimeMillis();
        this.mMaxX = (float) maxX;
        this.mMaxY = (float) maxY;
        calculateLocalPaths();
        postInvalidate();
        BatteryUtils.logRuntime(LOG_TAG, "setMax", startTime);
    }

    /* Access modifiers changed, original: 0000 */
    public void setDividerLoc(int height) {
        this.mMiddleDividerLoc = 1.0f - (((float) height) / this.mMaxY);
    }

    /* Access modifiers changed, original: 0000 */
    public void setDividerColors(int middleColor, int topColor) {
        this.mMiddleDividerTint = middleColor;
        this.mTopDividerTint = topColor;
    }

    public void addPath(SparseIntArray points) {
        addPathAndUpdate(points, this.mPaths, this.mLocalPaths);
    }

    public void addProjectedPath(SparseIntArray points) {
        addPathAndUpdate(points, this.mProjectedPaths, this.mLocalProjectedPaths);
    }

    private void addPathAndUpdate(SparseIntArray points, SparseIntArray paths, SparseIntArray localPaths) {
        long startTime = System.currentTimeMillis();
        int size = points.size();
        for (int i = 0; i < size; i++) {
            paths.put(points.keyAt(i), points.valueAt(i));
        }
        paths.put(points.keyAt(points.size() - 1) + 1, -1);
        calculateLocalPaths(paths, localPaths);
        postInvalidate();
        BatteryUtils.logRuntime(LOG_TAG, "addPathAndUpdate", startTime);
    }

    /* Access modifiers changed, original: 0000 */
    public void setAccentColor(int color) {
        this.mAccentColor = color;
        this.mLinePaint.setColor(this.mAccentColor);
        updateGradient();
        postInvalidate();
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        long startTime = System.currentTimeMillis();
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradient();
        calculateLocalPaths();
        BatteryUtils.logRuntime(LOG_TAG, "onSizeChanged", startTime);
    }

    private void calculateLocalPaths() {
        calculateLocalPaths(this.mPaths, this.mLocalPaths);
        calculateLocalPaths(this.mProjectedPaths, this.mLocalProjectedPaths);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void calculateLocalPaths(SparseIntArray paths, SparseIntArray localPaths) {
        long startTime = System.currentTimeMillis();
        if (getWidth() != 0) {
            localPaths.clear();
            int lx = 0;
            int ly = -1;
            boolean skippedLastPoint = false;
            for (int i = 0; i < paths.size(); i++) {
                int x = paths.keyAt(i);
                int y = paths.valueAt(i);
                if (y != -1) {
                    lx = getX((float) x);
                    ly = getY((float) y);
                    if (localPaths.size() > 0) {
                        int lastX = localPaths.keyAt(localPaths.size() - 1);
                        int lastY = localPaths.valueAt(localPaths.size() - 1);
                        if (!(lastY == -1 || hasDiff(lastX, lx) || hasDiff(lastY, ly))) {
                            skippedLastPoint = true;
                        }
                    }
                    skippedLastPoint = false;
                    localPaths.put(lx, ly);
                } else if (i == 1) {
                    localPaths.put(getX((float) (x + 1)) - 1, getY(0.0f));
                } else {
                    if (i == paths.size() - 1 && skippedLastPoint) {
                        localPaths.put(lx, ly);
                    }
                    skippedLastPoint = false;
                    localPaths.put(lx + 1, -1);
                }
            }
            BatteryUtils.logRuntime(LOG_TAG, "calculateLocalPaths", startTime);
        }
    }

    private boolean hasDiff(int x1, int x2) {
        return Math.abs(x2 - x1) >= this.mCornerRadius;
    }

    private int getX(float x) {
        return (int) ((x / this.mMaxX) * ((float) getWidth()));
    }

    private int getY(float y) {
        return (int) (((float) getHeight()) * (1.0f - (y / this.mMaxY)));
    }

    private void updateGradient() {
        this.mFillPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) getHeight(), getColor(this.mAccentColor, 0.2f), 0, TileMode.CLAMP));
    }

    private int getColor(int color, float alphaScale) {
        return ((((int) (255.0f * alphaScale)) << 24) | ViewCompat.MEASURED_SIZE_MASK) & color;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        if (this.mMiddleDividerLoc != 0.0f) {
            drawDivider(0, canvas, this.mTopDividerTint);
        }
        drawDivider((int) (((float) (canvas.getHeight() - this.mDividerSize)) * this.mMiddleDividerLoc), canvas, this.mMiddleDividerTint);
        drawDivider(canvas.getHeight() - this.mDividerSize, canvas, -1);
        if (this.mLocalPaths.size() != 0 || this.mLocalProjectedPaths.size() != 0) {
            drawLinePath(canvas, this.mLocalProjectedPaths, this.mDottedPaint);
            drawFilledPath(canvas, this.mLocalPaths, this.mFillPaint);
            drawLinePath(canvas, this.mLocalPaths, this.mLinePaint);
            BatteryUtils.logRuntime(LOG_TAG, "onDraw", startTime);
        }
    }

    private void drawLinePath(Canvas canvas, SparseIntArray localPaths, Paint paint) {
        if (localPaths.size() != 0) {
            this.mPath.reset();
            this.mPath.moveTo((float) localPaths.keyAt(0), (float) localPaths.valueAt(0));
            int i = 1;
            while (i < localPaths.size()) {
                int x = localPaths.keyAt(i);
                int y = localPaths.valueAt(i);
                if (y == -1) {
                    i++;
                    if (i < localPaths.size()) {
                        this.mPath.moveTo((float) localPaths.keyAt(i), (float) localPaths.valueAt(i));
                    }
                } else {
                    this.mPath.lineTo((float) x, (float) y);
                }
                i++;
            }
            canvas.drawPath(this.mPath, paint);
        }
    }

    private void drawFilledPath(Canvas canvas, SparseIntArray localPaths, Paint paint) {
        this.mPath.reset();
        float lastStartX = (float) localPaths.keyAt(0);
        this.mPath.moveTo((float) localPaths.keyAt(0), (float) localPaths.valueAt(0));
        float lastStartX2 = lastStartX;
        int i = 1;
        while (i < localPaths.size()) {
            int x = localPaths.keyAt(i);
            int y = localPaths.valueAt(i);
            if (y == -1) {
                this.mPath.lineTo((float) localPaths.keyAt(i - 1), (float) getHeight());
                this.mPath.lineTo(lastStartX2, (float) getHeight());
                this.mPath.close();
                i++;
                if (i < localPaths.size()) {
                    lastStartX2 = (float) localPaths.keyAt(i);
                    this.mPath.moveTo((float) localPaths.keyAt(i), (float) localPaths.valueAt(i));
                }
            } else {
                this.mPath.lineTo((float) x, (float) y);
            }
            i++;
        }
        canvas.drawPath(this.mPath, paint);
    }

    private void drawDivider(int y, Canvas canvas, int tintColor) {
        if (tintColor == -1) {
            Drawable d = this.mDivider;
            d.setBounds(0, y, canvas.getWidth(), this.mDividerSize + y);
            d.draw(canvas);
            return;
        }
        this.mTintedPaint.setColor(tintColor);
        canvas.drawLine(0.0f, (float) y, (float) canvas.getWidth(), (float) (this.mDividerSize + y), this.mTintedPaint);
    }
}
