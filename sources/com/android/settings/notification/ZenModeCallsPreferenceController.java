package com.android.settings.notification;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeCallsPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceChangeListener {
    protected static final String KEY = "zen_mode_calls";
    private final ZenModeBackend mBackend;
    private final String[] mListValues;
    private ListPreference mPreference;

    public ZenModeCallsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY, lifecycle);
        this.mBackend = ZenModeBackend.getInstance(context);
        this.mListValues = context.getResources().getStringArray(R.array.zen_mode_contacts_values);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ListPreference) screen.findPreference(KEY);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        updateFromContactsValue(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object selectedContactsFrom) {
        this.mBackend.saveSenders(8, ZenModeBackend.getSettingFromPrefKey(selectedContactsFrom.toString()));
        updateFromContactsValue(preference);
        return true;
    }

    private void updateFromContactsValue(Preference preference) {
        this.mPreference = (ListPreference) preference;
        switch (getZenMode()) {
            case 2:
            case 3:
                this.mPreference.setEnabled(false);
                this.mPreference.setValue("zen_mode_from_none");
                this.mPreference.setSummary(this.mBackend.getContactsSummary(-1));
                return;
            default:
                preference.setEnabled(true);
                preference.setSummary(this.mBackend.getContactsSummary(8));
                this.mPreference.setValue(this.mListValues[getIndexOfSendersValue(ZenModeBackend.getKeyFromSetting(this.mBackend.getPriorityCallSenders()))]);
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public int getIndexOfSendersValue(String currentVal) {
        for (int i = 0; i < this.mListValues.length; i++) {
            if (TextUtils.equals(currentVal, this.mListValues[i])) {
                return i;
            }
        }
        return 3;
    }
}
