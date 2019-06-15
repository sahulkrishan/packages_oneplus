package com.android.settings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class DefaultIndicatorSeekBar extends SeekBar {
    private int mDefaultProgress = -1;

    public DefaultIndicatorSeekBar(Context context) {
        super(context);
    }

    public DefaultIndicatorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultIndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultIndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* Access modifiers changed, original: protected */
    public void drawTickMarks(Canvas canvas) {
        if (isEnabled() && this.mDefaultProgress <= getMax() && this.mDefaultProgress >= getMin()) {
            Drawable defaultIndicator = getTickMark();
            int w = defaultIndicator.getIntrinsicWidth();
            int h = defaultIndicator.getIntrinsicHeight();
            int halfH = 1;
            int halfW = w >= 0 ? w / 2 : 1;
            if (h >= 0) {
                halfH = h / 2;
            }
            defaultIndicator.setBounds(-halfW, -halfH, halfW, halfH);
            int availableWidth = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
            int range = getMax() - getMin();
            float f = 0.0f;
            if (((float) range) > 0.0f) {
                f = ((float) this.mDefaultProgress) / ((float) range);
            }
            int offset = (int) ((((float) availableWidth) * f) + 1056964608);
            int indicatorPosition = (isLayoutRtl() && getMirrorForRtl()) ? (availableWidth - offset) + this.mPaddingRight : this.mPaddingLeft + offset;
            int saveCount = canvas.save();
            canvas.translate((float) indicatorPosition, (float) (getHeight() / 2));
            defaultIndicator.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }

    public void setDefaultProgress(int defaultProgress) {
        if (this.mDefaultProgress != defaultProgress) {
            this.mDefaultProgress = defaultProgress;
            invalidate();
        }
    }

    public int getDefaultProgress() {
        return this.mDefaultProgress;
    }
}
