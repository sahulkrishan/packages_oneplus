package com.android.settings.notification;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeRepeatCallersPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceChangeListener {
    protected static final String KEY = "zen_mode_repeat_callers";
    private final ZenModeBackend mBackend;
    private final int mRepeatCallersThreshold;

    public ZenModeRepeatCallersPreferenceController(Context context, Lifecycle lifecycle, int repeatCallersThreshold) {
        super(context, KEY, lifecycle);
        this.mRepeatCallersThreshold = repeatCallersThreshold;
        this.mBackend = ZenModeBackend.getInstance(context);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        setRepeatCallerSummary(screen.findPreference(KEY));
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
                boolean anyCallersCanBypassDnd;
                if (this.mBackend.isPriorityCategoryEnabled(8) && this.mBackend.getPriorityCallSenders() == 0) {
                    anyCallersCanBypassDnd = true;
                } else {
                    anyCallersCanBypassDnd = false;
                }
                if (anyCallersCanBypassDnd) {
                    pref.setEnabled(false);
                    pref.setChecked(true);
                    return;
                }
                pref.setEnabled(true);
                pref.setChecked(this.mBackend.isPriorityCategoryEnabled(16));
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean allowRepeatCallers = ((Boolean) newValue).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onPrefChange allowRepeatCallers=");
            stringBuilder.append(allowRepeatCallers);
            Log.d(str, stringBuilder.toString());
        }
        this.mMetricsFeatureProvider.action(this.mContext, 171, allowRepeatCallers);
        this.mBackend.saveSoundPolicy(16, allowRepeatCallers);
        return true;
    }

    private void setRepeatCallerSummary(Preference preference) {
        preference.setSummary(this.mContext.getString(R.string.zen_mode_repeat_callers_summary, new Object[]{Integer.valueOf(this.mRepeatCallersThreshold)}));
    }
}
