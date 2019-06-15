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

public class DebugNonRectClipOperationsPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String SHOW_NON_RECTANGULAR_CLIP_KEY = "show_non_rect_clip";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public DebugNonRectClipOperationsPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.show_non_rect_clip_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.show_non_rect_clip_entries);
    }

    public String getPreferenceKey() {
        return SHOW_NON_RECTANGULAR_CLIP_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeShowNonRectClipOptions(newValue);
        updateShowNonRectClipOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateShowNonRectClipOptions();
    }

    private void writeShowNonRectClipOptions(Object newValue) {
        SystemProperties.set("debug.hwui.show_non_rect_clip", newValue == null ? "" : newValue.toString());
        SystemPropPoker.getInstance().poke();
    }

    private void updateShowNonRectClipOptions() {
        String value = SystemProperties.get("debug.hwui.show_non_rect_clip", "hide");
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
