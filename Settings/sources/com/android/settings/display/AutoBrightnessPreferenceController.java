package com.android.settings.display;

import android.content.Context;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class AutoBrightnessPreferenceController extends TogglePreferenceController {
    private final int DEFAULT_VALUE = 0;
    private final String SYSTEM_KEY = "screen_brightness_mode";

    public AutoBrightnessPreferenceController(Context context, String key) {
        super(context, key);
    }

    public boolean isChecked() {
        return System.getInt(this.mContext.getContentResolver(), "screen_brightness_mode", 0) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        System.putInt(this.mContext.getContentResolver(), "screen_brightness_mode", isChecked);
        return true;
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(17956895)) {
            return 0;
        }
        return 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "auto_brightness");
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("screen_brightness_mode", 1, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, DisplaySettings.class.getName(), getPreferenceKey(), this.mContext.getString(R.string.display_settings)), isAvailable(), 0);
    }
}
