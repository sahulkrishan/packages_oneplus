package com.android.settingslib.development;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import com.android.settingslib.core.ConfirmationDialogController;

public abstract class AbstractEnableAdbPreferenceController extends DeveloperOptionsPreferenceController implements ConfirmationDialogController {
    public static final String ACTION_ENABLE_ADB_STATE_CHANGED = "com.android.settingslib.development.AbstractEnableAdbController.ENABLE_ADB_STATE_CHANGED";
    public static final int ADB_SETTING_OFF = 0;
    public static final int ADB_SETTING_ON = 1;
    private static final String KEY_ENABLE_ADB = "enable_adb";
    protected SwitchPreference mPreference;

    public AbstractEnableAdbPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreference = (SwitchPreference) screen.findPreference(KEY_ENABLE_ADB);
        }
    }

    public boolean isAvailable() {
        UserManager um = (UserManager) this.mContext.getSystemService(UserManager.class);
        return um != null && (um.isAdminUser() || um.isDemoUser());
    }

    public String getPreferenceKey() {
        return KEY_ENABLE_ADB;
    }

    private boolean isAdbEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "adb_enabled", 0) != 0;
    }

    public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(isAdbEnabled());
    }

    public void enablePreference(boolean enabled) {
        if (isAvailable()) {
            this.mPreference.setEnabled(enabled);
        }
    }

    public void resetPreference() {
        if (this.mPreference.isChecked()) {
            this.mPreference.setChecked(false);
            handlePreferenceTreeClick(this.mPreference);
        }
    }

    public boolean haveDebugSettings() {
        return isAdbEnabled();
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (isUserAMonkey() || !TextUtils.equals(KEY_ENABLE_ADB, preference.getKey())) {
            return false;
        }
        if (isAdbEnabled()) {
            writeAdbSetting(false);
        } else {
            showConfirmationDialog(preference);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void writeAdbSetting(boolean enabled) {
        Global.putInt(this.mContext.getContentResolver(), "adb_enabled", enabled);
        notifyStateChanged();
    }

    private void notifyStateChanged() {
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent(ACTION_ENABLE_ADB_STATE_CHANGED));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isUserAMonkey() {
        return ActivityManager.isUserAMonkey();
    }
}
