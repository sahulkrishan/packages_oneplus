package com.android.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings.System;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import com.android.settings.core.BasePreferenceController;

public class FontSizePreferenceController extends BasePreferenceController {
    public FontSizePreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public CharSequence getSummary() {
        float currentScale = System.getFloat(this.mContext.getContentResolver(), "font_scale", 1.0f);
        Resources res = this.mContext.getResources();
        return res.getStringArray(2130903123)[ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale, res.getStringArray(2130903124))];
    }
}
