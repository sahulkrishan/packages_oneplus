package com.android.settings.development;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class WaitForDebuggerPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin, OnActivityResultListener {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String WAIT_FOR_DEBUGGER_KEY = "wait_for_debugger";

    public WaitForDebuggerPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return WAIT_FOR_DEBUGGER_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeDebuggerAppOptions(Global.getString(this.mContext.getContentResolver(), "debug_app"), ((Boolean) newValue).booleanValue(), true);
        return true;
    }

    public void updateState(Preference preference) {
        updateState(this.mPreference, Global.getString(this.mContext.getContentResolver(), "debug_app"));
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != -1) {
            return false;
        }
        updateState(this.mPreference, data.getAction());
        return true;
    }

    private void updateState(Preference preference, String debugApp) {
        SwitchPreference switchPreference = (SwitchPreference) preference;
        boolean z = false;
        if (Global.getInt(this.mContext.getContentResolver(), WAIT_FOR_DEBUGGER_KEY, 0) != 0) {
            z = true;
        }
        boolean debuggerEnabled = z;
        writeDebuggerAppOptions(debugApp, debuggerEnabled, true);
        switchPreference.setChecked(debuggerEnabled);
        switchPreference.setEnabled(1 ^ TextUtils.isEmpty(debugApp));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeDebuggerAppOptions(null, false, false);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public IActivityManager getActivityManagerService() {
        return ActivityManager.getService();
    }

    private void writeDebuggerAppOptions(String packageName, boolean waitForDebugger, boolean persistent) {
        try {
            getActivityManagerService().setDebugApp(packageName, waitForDebugger, persistent);
        } catch (RemoteException e) {
        }
    }
}
