package com.android.settings.accessibility;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class MagnificationGesturesPreferenceController extends TogglePreferenceController {
    private boolean mIsFromSUW = false;

    public MagnificationGesturesPreferenceController(Context context, String key) {
        super(context, key);
    }

    public boolean isChecked() {
        return MagnificationPreferenceFragment.isChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled");
    }

    public boolean setChecked(boolean isChecked) {
        return MagnificationPreferenceFragment.setChecked(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", isChecked);
    }

    public void setIsFromSUW(boolean fromSUW) {
        this.mIsFromSUW = fromSUW;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey())) {
            Bundle extras = preference.getExtras();
            populateMagnificationGesturesPreferenceExtras(extras, this.mContext);
            extras.putBoolean("checked", isChecked());
            extras.putBoolean("from_suw", this.mIsFromSUW);
        }
        return false;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "screen_magnification_gestures_preference_screen");
    }

    public CharSequence getSummary() {
        int resId;
        if (this.mIsFromSUW) {
            resId = R.string.accessibility_screen_magnification_short_summary;
        } else {
            int i;
            if (isChecked()) {
                i = R.string.accessibility_feature_state_on;
            } else {
                i = R.string.accessibility_feature_state_off;
            }
            resId = i;
        }
        return this.mContext.getString(resId);
    }

    static void populateMagnificationGesturesPreferenceExtras(Bundle extras, Context context) {
        extras.putString("preference_key", "accessibility_display_magnification_enabled");
        extras.putInt("title_res", R.string.accessibility_screen_magnification_gestures_title);
        extras.putInt("summary_res", R.string.accessibility_screen_magnification_summary);
        extras.putInt("video_resource", R.raw.accessibility_screen_magnification);
    }
}
