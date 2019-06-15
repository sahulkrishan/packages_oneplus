package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.BatteryMeterDrawableBase;

public class BatterySaverDrawable extends BatteryMeterDrawableBase {
    private static final int MAX_BATTERY = 100;

    public BatterySaverDrawable(Context context, int frameColor) {
        super(context, frameColor);
        setBatteryLevel(100);
        setPowerSave(true);
        setCharging(false);
        setPowerSaveAsColorError(false);
        setColorFilter(new PorterDuffColorFilter(Utils.getColorAttr(context, 16843829), Mode.SRC_IN));
    }
}
