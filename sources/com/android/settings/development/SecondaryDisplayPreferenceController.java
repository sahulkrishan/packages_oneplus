package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class SecondaryDisplayPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String OVERLAY_DISPLAY_DEVICES_KEY = "overlay_display_devices";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public SecondaryDisplayPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.overlay_display_devices_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.overlay_display_devices_entries);
    }

    public String getPreferenceKey() {
        return OVERLAY_DISPLAY_DEVICES_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeSecondaryDisplayDevicesOption(newValue.toString());
        return true;
    }

    public void updateState(Preference preference) {
        updateSecondaryDisplayDevicesOptions();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeSecondaryDisplayDevicesOption(null);
    }

    private void updateSecondaryDisplayDevicesOptions() {
        String value = Global.getString(this.mContext.getContentResolver(), OVERLAY_DISPLAY_DEVICES_KEY);
        int index = 0;
        for (int i = 0; i < this.mListValues.length; i++) {
            if (TextUtils.equals(value, this.mListValues[i])) {
                index = i;
                break;
            }
        }
        ListPreference listPreference = this.mPreference;
        listPreference.setValue(this.mListValues[index]);
        listPreference.setSummary(this.mListSummaries[index]);
    }

    private void writeSecondaryDisplayDevicesOption(String newValue) {
        Global.putString(this.mContext.getContentResolver(), OVERLAY_DISPLAY_DEVICES_KEY, newValue);
        updateSecondaryDisplayDevicesOptions();
    }
}
