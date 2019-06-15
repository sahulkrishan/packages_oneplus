package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;

public class DebugGpuOverdrawPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String DEBUG_HW_OVERDRAW_KEY = "debug_hw_overdraw";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public DebugGpuOverdrawPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.debug_hw_overdraw_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.debug_hw_overdraw_entries);
    }

    public String getPreferenceKey() {
        return DEBUG_HW_OVERDRAW_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeDebugHwOverdrawOptions(newValue);
        updateDebugHwOverdrawOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateDebugHwOverdrawOptions();
    }

    private void writeDebugHwOverdrawOptions(Object newValue) {
        SystemProperties.set("debug.hwui.overdraw", newValue == null ? "" : newValue.toString());
        SystemPropPoker.getInstance().poke();
    }

    private void updateDebugHwOverdrawOptions() {
        String value = SystemProperties.get("debug.hwui.overdraw", "");
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
}
