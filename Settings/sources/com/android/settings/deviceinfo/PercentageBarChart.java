package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.R;
import java.util.Collection;

public class PercentageBarChart extends View {
    private final Paint mEmptyPaint = new Paint();
    private Collection<Entry> mEntries;
    private int mMinTickWidth = 1;

    public static class Entry implements Comparable<Entry> {
        public final int order;
        public final Paint paint;
        public final float percentage;

        protected Entry(int order, float percentage, Paint paint) {
            this.order = order;
            this.percentage = percentage;
            this.paint = paint;
        }

        public int compareTo(Entry another) {
            return this.order - another.order;
        }
    }

    public PercentageBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PercentageBarChart);
        this.mMinTickWidth = a.getDimensionPixelSize(1, 1);
        int emptyColor = a.getColor(0, ViewCompat.MEASURED_STATE_MASK);
        a.recycle();
        this.mEmptyPaint.setColor(emptyColor);
        this.mEmptyPaint.setStyle(Style.FILL);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight();
        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom();
        int width = right - left;
        float nextX;
        float entryWidth;
        float lastX;
        if (isLayoutRtl()) {
            nextX = (float) right;
            if (this.mEntries != null) {
                for (Entry e : this.mEntries) {
                    if (e.percentage == 0.0f) {
                        entryWidth = 0.0f;
                    } else {
                        entryWidth = Math.max((float) this.mMinTickWidth, ((float) width) * e.percentage);
                    }
                    lastX = nextX - entryWidth;
                    if (lastX < ((float) left)) {
                        canvas.drawRect((float) left, (float) top, nextX, (float) bottom, e.paint);
                        return;
                    }
                    canvas.drawRect(lastX, (float) top, nextX, (float) bottom, e.paint);
                    nextX = lastX;
                }
            }
            canvas.drawRect((float) left, (float) top, nextX, (float) bottom, this.mEmptyPaint);
        } else {
            nextX = (float) left;
            if (this.mEntries != null) {
                for (Entry e2 : this.mEntries) {
                    if (e2.percentage == 0.0f) {
                        entryWidth = 0.0f;
                    } else {
                        entryWidth = Math.max((float) this.mMinTickWidth, ((float) width) * e2.percentage);
                    }
                    lastX = nextX + entryWidth;
                    if (lastX > ((float) right)) {
                        Entry entry = e2;
                        canvas.drawRect(nextX, (float) top, (float) right, (float) bottom, e2.paint);
                        return;
                    }
                    Entry entry2 = e2;
                    canvas.drawRect(nextX, (float) top, lastX, (float) bottom, e2.paint);
                    nextX = lastX;
                }
            }
            canvas.drawRect(nextX, (float) top, (float) right, (float) bottom, this.mEmptyPaint);
        }
    }

    public void setBackgroundColor(int color) {
        this.mEmptyPaint.setColor(color);
    }

    public static Entry createEntry(int order, float percentage, int color) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Style.FILL);
        return new Entry(order, percentage, p);
    }

    public void setEntries(Collection<Entry> entries) {
        this.mEntries = entries;
    }
}
