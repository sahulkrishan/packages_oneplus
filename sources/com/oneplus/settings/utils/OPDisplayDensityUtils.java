package com.oneplus.settings.utils;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settingslib.display.DisplayDensityUtils;
import com.oneplus.settings.OPScreenResolutionAdjust;

public class OPDisplayDensityUtils {
    public static final int DEFAULT_DENSITY_INDEX = 1;
    public static final String DEFAULT_LARGE_DPI = "560";
    private int mCurrentIndex;
    private int mDefaultDensity;
    private String[] mEntries;
    private int[] mValues = new int[]{380, 420, 480, 500, 540};

    public OPDisplayDensityUtils(Context context) {
        String defaultDpi = SystemProperties.get("ro.sf.lcd_density", "480");
        this.mDefaultDensity = new DisplayDensityUtils(context).getDefaultDensity();
        int value = Global.getInt(context.getContentResolver(), OPScreenResolutionAdjust.ONEPLUS_SCREEN_RESOLUTION_ADJUST, 2);
        if (DEFAULT_LARGE_DPI.equals(defaultDpi) && (value == 0 || value == 2)) {
            this.mValues = context.getResources().getIntArray(R.array.oneplus_screen_dpi_values);
        }
        this.mEntries = new String[]{context.getResources().getString(R.string.screen_zoom_summary_small), context.getResources().getString(R.string.screen_zoom_summary_default), context.getResources().getString(R.string.screen_zoom_summary_large), context.getResources().getString(R.string.screen_zoom_summary_very_large), context.getResources().getString(R.string.screen_zoom_summary_extremely_large)};
        String currentDpi = Secure.getStringForUser(context.getContentResolver(), "display_density_forced", -2);
        if (TextUtils.isEmpty(currentDpi)) {
            this.mCurrentIndex = 1;
        } else {
            for (int i = 0; i < this.mValues.length; i++) {
                if (currentDpi.equals(String.valueOf(this.mValues[i]))) {
                    this.mCurrentIndex = i;
                }
            }
        }
        if (this.mCurrentIndex >= this.mValues.length - 1) {
            this.mCurrentIndex = this.mValues.length - 1;
        }
        if (this.mCurrentIndex <= 0) {
            this.mCurrentIndex = 0;
        }
    }

    public String[] getEntries() {
        return this.mEntries;
    }

    public int[] getValues() {
        return this.mValues;
    }

    public int getCurrentIndex() {
        return this.mCurrentIndex;
    }

    public int getDefaultDensity() {
        return this.mDefaultDensity;
    }
}
