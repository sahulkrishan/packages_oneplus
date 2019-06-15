package com.android.settings.development;

import android.content.Context;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class ClearAdbKeysPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private static final String CLEAR_ADB_KEYS = "clear_adb_keys";
    @VisibleForTesting
    static final String RO_ADB_SECURE_PROPERTY_KEY = "ro.adb.secure";
    private static final String TAG = "ClearAdbPrefCtrl";
    private final DevelopmentSettingsDashboardFragment mFragment;
    private final IUsbManager mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));

    public ClearAdbKeysPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public boolean isAvailable() {
        return SystemProperties.getBoolean(RO_ADB_SECURE_PROPERTY_KEY, false);
    }

    public String getPreferenceKey() {
        return CLEAR_ADB_KEYS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (this.mPreference != null && !isAdminUser()) {
            this.mPreference.setEnabled(false);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (Utils.isMonkeyRunning() || !TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        ClearAdbKeysWarningDialog.show(this.mFragment);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        if (isAdminUser()) {
            this.mPreference.setEnabled(true);
        }
    }

    public void onClearAdbKeysConfirmed() {
        try {
            this.mUsbManager.clearUsbDebuggingKeys();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to clear adb keys", e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isAdminUser() {
        return ((UserManager) this.mContext.getSystemService("user")).isAdminUser();
    }
}
