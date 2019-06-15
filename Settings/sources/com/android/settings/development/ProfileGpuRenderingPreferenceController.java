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

public class ProfileGpuRenderingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String TRACK_FRAME_TIME_KEY = "track_frame_time";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public ProfileGpuRenderingPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.track_frame_time_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.track_frame_time_entries);
    }

    public String getPreferenceKey() {
        return TRACK_FRAME_TIME_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeTrackFrameTimeOptions(newValue);
        updateTrackFrameTimeOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateTrackFrameTimeOptions();
    }

    private void writeTrackFrameTimeOptions(Object newValue) {
        SystemProperties.set("debug.hwui.profile", newValue == null ? "" : newValue.toString());
        SystemPropPoker.getInstance().poke();
    }

    private void updateTrackFrameTimeOptions() {
        String value = SystemProperties.get("debug.hwui.profile", "");
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
