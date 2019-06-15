package com.android.settings.notification;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeDurationPreferenceController extends AbstractZenModePreferenceController implements PreferenceControllerMixin, OnPreferenceClickListener {
    protected static final String KEY = "zen_mode_duration_settings";
    private static final String TAG = "ZenModeDurationDialog";
    private FragmentManager mFragment;

    public ZenModeDurationPreferenceController(Context context, Lifecycle lifecycle, FragmentManager fragment) {
        super(context, KEY, lifecycle);
        this.mFragment = fragment;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        screen.findPreference(KEY).setOnPreferenceClickListener(this);
    }

    public void updateState(Preference preference) {
        CharSequence summary;
        super.updateState(preference);
        String summary2 = "";
        int zenDuration = getZenDuration();
        if (zenDuration < 0) {
            summary = this.mContext.getString(R.string.zen_mode_duration_summary_always_prompt);
        } else if (zenDuration == 0) {
            summary = this.mContext.getString(R.string.zen_mode_duration_summary_forever);
        } else if (zenDuration >= 60) {
            int hours = zenDuration / 60;
            summary = this.mContext.getResources().getQuantityString(R.plurals.zen_mode_duration_summary_time_hours, hours, new Object[]{Integer.valueOf(hours)});
        } else {
            summary = this.mContext.getResources().getString(R.string.zen_mode_duration_summary_time_minutes, new Object[]{Integer.valueOf(zenDuration)});
        }
        preference.setSummary(summary);
    }

    public boolean onPreferenceClick(Preference preference) {
        new SettingsZenDurationDialog().show(this.mFragment, TAG);
        return true;
    }
}
