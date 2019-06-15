package com.android.settings.notification;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeRemindersPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceChangeListener {
    protected static final String KEY = "zen_mode_reminders";

    public ZenModeRemindersPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY, lifecycle);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        SwitchPreference pref = (SwitchPreference) preference;
        switch (getZenMode()) {
            case 2:
            case 3:
                pref.setEnabled(false);
                pref.setChecked(false);
                return;
            default:
                pref.setEnabled(true);
                pref.setChecked(this.mBackend.isPriorityCategoryEnabled(1));
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean allowReminders = ((Boolean) newValue).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onPrefChange allowReminders=");
            stringBuilder.append(allowReminders);
            Log.d(str, stringBuilder.toString());
        }
        this.mMetricsFeatureProvider.action(this.mContext, 167, allowReminders);
        this.mBackend.saveSoundPolicy(1, allowReminders);
        return true;
    }
}
