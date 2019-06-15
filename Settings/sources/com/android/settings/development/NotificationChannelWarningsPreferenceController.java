package com.android.settings.development;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class NotificationChannelWarningsPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int DEBUGGING_DISABLED = 0;
    @VisibleForTesting
    static final int DEBUGGING_ENABLED = 1;
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String SHOW_NOTIFICATION_CHANNEL_WARNINGS_KEY = "show_notification_channel_warnings";

    public NotificationChannelWarningsPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHOW_NOTIFICATION_CHANNEL_WARNINGS_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), SHOW_NOTIFICATION_CHANNEL_WARNINGS_KEY, ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(Global.getInt(this.mContext.getContentResolver(), SHOW_NOTIFICATION_CHANNEL_WARNINGS_KEY, isDebuggable()) != 0);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), SHOW_NOTIFICATION_CHANNEL_WARNINGS_KEY, 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isDebuggable() {
        return Build.IS_DEBUGGABLE;
    }
}
