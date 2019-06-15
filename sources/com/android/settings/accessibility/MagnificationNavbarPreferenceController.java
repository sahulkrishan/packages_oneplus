package com.android.settings.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class MagnificationNavbarPreferenceController extends TogglePreferenceController {
    private boolean mIsFromSUW = false;

    public MagnificationNavbarPreferenceController(Context context, String key) {
        super(context, key);
    }

    public boolean isChecked() {
        return MagnificationPreferenceFragment.isChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_navbar_enabled");
    }

    public boolean setChecked(boolean isChecked) {
        return MagnificationPreferenceFragment.setChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_navbar_enabled", isChecked);
    }

    public void setIsFromSUW(boolean fromSUW) {
        this.mIsFromSUW = fromSUW;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            Bundle extras = preference.getExtras();
            extras.putString("preference_key", "accessibility_display_magnification_navbar_enabled");
            extras.putInt("title_res", R.string.accessibility_screen_magnification_navbar_title);
            extras.putInt("summary_res", R.string.accessibility_screen_magnification_navbar_summary);
            extras.putBoolean("checked", isChecked());
            extras.putBoolean("from_suw", this.mIsFromSUW);
        }
        return false;
    }

    public int getAvailabilityStatus() {
        if (MagnificationPreferenceFragment.isApplicable(this.mContext.getResources())) {
            return 0;
        }
        return 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "screen_magnification_navbar_preference_screen");
    }

    public CharSequence getSummary() {
        int resId;
        if (this.mIsFromSUW) {
            resId = R.string.accessibility_screen_magnification_navbar_short_summary;
        } else {
            int i;
            if (isChecked()) {
                i = R.string.accessibility_feature_state_on;
            } else {
                i = R.string.accessibility_feature_state_off;
            }
            resId = i;
        }
        return this.mContext.getText(resId);
    }
}
