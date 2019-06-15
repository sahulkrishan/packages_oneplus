package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.BatteryMeterDrawableBase;

public class BatteryMeterView extends ImageView {
    @VisibleForTesting
    ColorFilter mAccentColorFilter;
    @VisibleForTesting
    BatteryMeterDrawable mDrawable;
    @VisibleForTesting
    ColorFilter mErrorColorFilter;

    public static class BatteryMeterDrawable extends BatteryMeterDrawableBase {
        private final int mIntrinsicHeight;
        private final int mIntrinsicWidth;

        public BatteryMeterDrawable(Context context, int frameColor) {
            super(context, frameColor);
            this.mIntrinsicWidth = context.getResources().getDimensionPixelSize(R.dimen.battery_meter_width);
            this.mIntrinsicHeight = context.getResources().getDimensionPixelSize(R.dimen.battery_meter_height);
        }

        public int getIntrinsicWidth() {
            return this.mIntrinsicWidth;
        }

        public int getIntrinsicHeight() {
            return this.mIntrinsicHeight;
        }

        public void setWarningColorFilter(ColorFilter colorFilter) {
            this.mWarningTextPaint.setColorFilter(colorFilter);
        }

        public void setBatteryColorFilter(ColorFilter colorFilter) {
            this.mFramePaint.setColorFilter(colorFilter);
            this.mBatteryPaint.setColorFilter(colorFilter);
            this.mBoltPaint.setColorFilter(colorFilter);
        }
    }

    public BatteryMeterView(Context context) {
        this(context, null, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int frameColor = context.getColor(R.color.meter_background_color);
        this.mAccentColorFilter = new PorterDuffColorFilter(Utils.getColorAttr(context, 16843829), Mode.SRC_IN);
        this.mErrorColorFilter = new PorterDuffColorFilter(context.getColor(R.color.battery_icon_color_error), Mode.SRC_IN);
        this.mDrawable = new BatteryMeterDrawable(context, frameColor);
        this.mDrawable.setShowPercent(false);
        this.mDrawable.setBatteryColorFilter(this.mAccentColorFilter);
        this.mDrawable.setWarningColorFilter(new PorterDuffColorFilter(-1, Mode.SRC_IN));
        setImageDrawable(this.mDrawable);
    }

    public void setBatteryLevel(int level) {
        this.mDrawable.setBatteryLevel(level);
        if (level < this.mDrawable.getCriticalLevel()) {
            this.mDrawable.setBatteryColorFilter(this.mErrorColorFilter);
        } else {
            this.mDrawable.setBatteryColorFilter(this.mAccentColorFilter);
        }
    }

    public int getBatteryLevel() {
        return this.mDrawable.getBatteryLevel();
    }

    public void setCharging(boolean charging) {
        this.mDrawable.setCharging(charging);
        postInvalidate();
    }

    public boolean getCharging() {
        return this.mDrawable.getCharging();
    }
}
