package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothAvrcpVersionPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final String BLUETOOTH_AVRCP_VERSION_PROPERTY = "persist.bluetooth.avrcpversion";
    private static final String BLUETOOTH_SELECT_AVRCP_VERSION_KEY = "bluetooth_select_avrcp_version";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public BluetoothAvrcpVersionPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.bluetooth_avrcp_version_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.bluetooth_avrcp_versions);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_AVRCP_VERSION_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(BLUETOOTH_AVRCP_VERSION_PROPERTY, newValue.toString());
        updateState(this.mPreference);
        return true;
    }

    public void updateState(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        String currentValue = SystemProperties.get(BLUETOOTH_AVRCP_VERSION_PROPERTY);
        int index = 0;
        for (int i = 0; i < this.mListValues.length; i++) {
            if (TextUtils.equals(currentValue, this.mListValues[i])) {
                index = i;
                break;
            }
        }
        listPreference.setValue(this.mListValues[index]);
        listPreference.setSummary(this.mListSummaries[index]);
    }
}
