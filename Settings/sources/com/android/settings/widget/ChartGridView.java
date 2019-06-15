package com.android.settings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout.Builder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.Utils;

public class ChartGridView extends View {
    private Drawable mBorder;
    private ChartAxis mHoriz;
    private int mLabelColor;
    private Layout mLabelEnd;
    private Layout mLabelMid;
    private int mLabelSize;
    private Layout mLabelStart;
    private Drawable mPrimary;
    private Drawable mSecondary;
    private ChartAxis mVert;

    public ChartGridView(Context context) {
        this(context, null, 0);
    }

    public ChartGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChartGridView, defStyle, 0);
        this.mPrimary = a.getDrawable(3);
        this.mSecondary = a.getDrawable(4);
        this.mBorder = a.getDrawable(2);
        TypedArray ta = context.obtainStyledAttributes(a.getResourceId(0, -1), com.android.internal.R.styleable.TextAppearance);
        this.mLabelSize = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
        this.mLabelColor = a.getColorStateList(1).getDefaultColor();
        a.recycle();
    }

    /* Access modifiers changed, original: 0000 */
    public void init(ChartAxis horiz, ChartAxis vert) {
        this.mHoriz = (ChartAxis) Preconditions.checkNotNull(horiz, "missing horiz");
        this.mVert = (ChartAxis) Preconditions.checkNotNull(vert, "missing vert");
    }

    /* Access modifiers changed, original: 0000 */
    public void setBounds(long start, long end) {
        Context context = getContext();
        long mid = (start + end) / 2;
        this.mLabelStart = makeLabel(Utils.formatDateRange(context, start, start));
        this.mLabelMid = makeLabel(Utils.formatDateRange(context, mid, mid));
        this.mLabelEnd = makeLabel(Utils.formatDateRange(context, end, end));
        invalidate();
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        int save;
        int intrinsicHeight;
        int save2;
        int width = getWidth();
        int height = getHeight() - getPaddingBottom();
        Drawable secondary = this.mSecondary;
        int padding = 0;
        if (secondary != null) {
            int secondaryHeight = secondary.getIntrinsicHeight();
            for (float y : this.mVert.getTickPoints()) {
                secondary.setBounds(0, (int) y, width, (int) Math.min(((float) secondaryHeight) + y, (float) height));
                secondary.draw(canvas);
            }
        }
        Drawable primary = this.mPrimary;
        if (primary != null) {
            int primaryWidth = primary.getIntrinsicWidth();
            intrinsicHeight = primary.getIntrinsicHeight();
            for (float x : this.mHoriz.getTickPoints()) {
                primary.setBounds((int) x, 0, (int) Math.min(((float) primaryWidth) + x, (float) width), height);
                primary.draw(canvas);
            }
        }
        this.mBorder.setBounds(0, 0, width, height);
        this.mBorder.draw(canvas);
        if (this.mLabelStart != null) {
            padding = this.mLabelStart.getHeight() / 8;
        }
        Layout start = this.mLabelStart;
        if (start != null) {
            intrinsicHeight = canvas.save();
            canvas.translate(0.0f, (float) (height + padding));
            start.draw(canvas);
            canvas.restoreToCount(intrinsicHeight);
        }
        Layout mid = this.mLabelMid;
        if (mid != null) {
            save = canvas.save();
            canvas.translate((float) ((width - mid.getWidth()) / 2), (float) (height + padding));
            mid.draw(canvas);
            canvas.restoreToCount(save);
        }
        Layout end = this.mLabelEnd;
        if (end != null) {
            save2 = canvas.save();
            canvas.translate((float) (width - end.getWidth()), (float) (height + padding));
            end.draw(canvas);
            canvas.restoreToCount(save2);
        }
    }

    private Layout makeLabel(CharSequence text) {
        Resources res = getResources();
        TextPaint paint = new TextPaint(1);
        paint.density = res.getDisplayMetrics().density;
        paint.setCompatibilityScaling(res.getCompatibilityInfo().applicationScale);
        paint.setColor(this.mLabelColor);
        paint.setTextSize((float) this.mLabelSize);
        return Builder.obtain(text, 0, text.length(), paint, (int) Math.ceil((double) Layout.getDesiredWidth(text, paint))).setUseLineSpacingFromFallbacks(true).build();
    }
}
