package com.android.settings.notification;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeMediaPreferenceController extends AbstractZenModePreferenceController implements OnPreferenceChangeListener {
    protected static final String KEY = "zen_mode_media";
    private final ZenModeBackend mBackend;

    public ZenModeMediaPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY, lifecycle);
        this.mBackend = ZenModeBackend.getInstance(context);
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
                pref.setEnabled(false);
                pref.setChecked(false);
                return;
            case 3:
                pref.setEnabled(false);
                pref.setChecked(true);
                return;
            default:
                pref.setEnabled(true);
                pref.setChecked(this.mBackend.isPriorityCategoryEnabled(64));
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean allowMedia = ((Boolean) newValue).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onPrefChange allowMedia=");
            stringBuilder.append(allowMedia);
            Log.d(str, stringBuilder.toString());
        }
        this.mBackend.saveSoundPolicy(64, allowMedia);
        return true;
    }
}