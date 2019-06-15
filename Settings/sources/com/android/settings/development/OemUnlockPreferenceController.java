package com.android.settings.development;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.oemlock.OemLockManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockService;
import com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockService.Stub;
import com.qualcomm.qti.remoteSimlock.IUimRemoteSimlockServiceCallback;
import java.util.List;

public class OemUnlockPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin, OnActivityResultListener {
    private static final String PREFERENCE_KEY = "oem_unlock_enable";
    private static final String TAG = "OemUnlockPreferenceController";
    private static final int UIM_REMOTE_SIMLOCK_STATE_LOCKED = 0;
    private static final int UIM_REMOTE_SIMLOCK_STATE_PERMANENT_UNLOCK = 2;
    private static final int UIM_REMOTE_SIMLOCK_STATE_TEMPORARY_UNLOCK = 1;
    private static final String simLockStateAppPackage = "com.qualcomm.qti.uim";
    private static final String simLockStateAppService = "com.qualcomm.qti.uim.RemoteSimLockService";
    private boolean isUimLocked = true;
    private Activity mActivity;
    private final ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private final DevelopmentSettingsDashboardFragment mFragment;
    private final OemLockManager mOemLockManager;
    private RestrictedSwitchPreference mPreference;
    private ServiceConnection mSimlockConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(OemUnlockPreferenceController.TAG, "mSimlockConnection service connected");
            OemUnlockPreferenceController.this.uimRemoteSimlockService = Stub.asInterface(service);
            try {
                OemUnlockPreferenceController.this.uimRemoteSimlockService.registerCallback(OemUnlockPreferenceController.this.uimRemoteSimlockServiceCallback);
                OemUnlockPreferenceController.this.uimRemoteSimlockService.uimRemoteSimlockGetSimlockStatus(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(OemUnlockPreferenceController.TAG, "mSimlockConnection service disconnected");
        }
    };
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;
    private IUimRemoteSimlockService uimRemoteSimlockService;
    private IUimRemoteSimlockServiceCallback uimRemoteSimlockServiceCallback = new IUimRemoteSimlockServiceCallback.Stub() {
        public void uimRemoteSimlockGetSimlockStatusResponse(int token, int responseCode, int unlockStatus, long unlockTime) throws RemoteException {
            String str = OemUnlockPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uimRemoteSimlockGetSimlockStatusResponse token:");
            stringBuilder.append(token);
            stringBuilder.append(", responseCode:");
            stringBuilder.append(responseCode);
            stringBuilder.append(", unlockStatus:");
            stringBuilder.append(unlockStatus);
            stringBuilder.append(", unlockTime:");
            stringBuilder.append(unlockTime);
            Log.i(str, stringBuilder.toString());
            if (unlockStatus == 0 || unlockStatus == 1) {
                OemUnlockPreferenceController.this.isUimLocked = true;
            } else {
                OemUnlockPreferenceController.this.isUimLocked = false;
            }
            OemUnlockPreferenceController.this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    OemUnlockPreferenceController.this.updateState(OemUnlockPreferenceController.this.mPreference);
                }
            });
        }

        public void uimRemoteSimlockGetVersionResponse(int token, int responseCode, int majorVersion, int minorVersion) throws RemoteException {
            String str = OemUnlockPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uimRemoteSimlockGetVersionResponse token:");
            stringBuilder.append(token);
            stringBuilder.append(", responseCode:");
            stringBuilder.append(responseCode);
            stringBuilder.append(", majorVersion:");
            stringBuilder.append(majorVersion);
            stringBuilder.append(", minorVersion:");
            stringBuilder.append(minorVersion);
            Log.i(str, stringBuilder.toString());
        }

        public void uimRemoteSimlockProcessSimlockDataResponse(int token, int responseCode, byte[] simlockResponse) throws RemoteException {
            String str = OemUnlockPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uimRemoteSimlockProcessSimlockDataResponse token:");
            stringBuilder.append(token);
            stringBuilder.append(", responseCode:");
            stringBuilder.append(responseCode);
            Log.i(str, stringBuilder.toString());
        }

        public void uimRemoteSimlockGetSharedKeyResponse(int token, int responseCode, byte[] encryptedKey) throws RemoteException {
            String str = OemUnlockPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uimRemoteSimlockProcessSimlockDataResponse token:");
            stringBuilder.append(token);
            stringBuilder.append(", responseCode:");
            stringBuilder.append(responseCode);
            Log.i(str, stringBuilder.toString());
        }

        public void uimRemoteSimlockGenerateHMACResponse(int token, int responseCode, byte[] hmacData) throws RemoteException {
            String str = OemUnlockPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uimRemoteSimlockGenerateHMACResponse token:");
            stringBuilder.append(token);
            stringBuilder.append(", responseCode:");
            stringBuilder.append(responseCode);
            Log.i(str, stringBuilder.toString());
        }
    };

    public OemUnlockPreferenceController(Context context, Activity activity, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mOemLockManager = (OemLockManager) context.getSystemService("oem_lock");
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mFragment = fragment;
        if (activity == null && this.mFragment == null) {
            this.mChooseLockSettingsHelper = null;
        } else {
            this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(activity, this.mFragment);
        }
        this.mActivity = activity;
        if (isUimLockServiceEnable(context)) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(simLockStateAppPackage, simLockStateAppService));
            try {
                boolean ret = context.bindService(intent, this.mSimlockConnection, 1);
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("bind RemoteSimLockService ret = ");
                stringBuilder.append(ret);
                Log.d(str, stringBuilder.toString());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Log.d(TAG, "isSimLockEnable not exist.");
        this.isUimLocked = false;
    }

    public void unBindSimlockConnection() {
        try {
            if (this.uimRemoteSimlockService != null) {
                this.uimRemoteSimlockService.deregisterCallback(this.uimRemoteSimlockServiceCallback);
                this.mContext.unbindService(this.mSimlockConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "unbindService mSimlockConnection.");
    }

    public boolean isAvailable() {
        return this.mOemLockManager != null;
    }

    public String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (RestrictedSwitchPreference) screen.findPreference(getPreferenceKey());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!((Boolean) newValue).booleanValue()) {
            this.mOemLockManager.setOemUnlockAllowedByUser(false);
            OemLockInfoDialog.show(this.mFragment);
        } else if (!showKeyguardConfirmation(this.mContext.getResources(), 0)) {
            confirmEnableOemUnlock();
        }
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(isOemUnlockedAllowed());
        updateOemUnlockSettingDescription();
        this.mPreference.setDisabledByAdmin(null);
        this.mPreference.setEnabled(enableOemUnlockPreference());
        if (this.mPreference.isEnabled()) {
            this.mPreference.checkRestrictionAndSetDisabled("no_factory_reset");
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 0) {
            return false;
        }
        if (resultCode == -1) {
            if (this.mPreference.isChecked()) {
                confirmEnableOemUnlock();
            } else {
                this.mOemLockManager.setOemUnlockAllowedByUser(false);
            }
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        handleDeveloperOptionsToggled();
    }

    public void onOemUnlockConfirmed() {
        this.mOemLockManager.setOemUnlockAllowedByUser(true);
    }

    public void onOemUnlockDismissed() {
        if (this.mPreference != null) {
            updateState(this.mPreference);
        }
    }

    private void handleDeveloperOptionsToggled() {
        this.mPreference.setEnabled(enableOemUnlockPreference());
        if (this.mPreference.isEnabled()) {
            this.mPreference.checkRestrictionAndSetDisabled("no_factory_reset");
        }
    }

    private void updateOemUnlockSettingDescription() {
        int oemUnlockSummary = R.string.oem_unlock_enable_summary;
        if (isBootloaderUnlocked()) {
            oemUnlockSummary = R.string.oem_unlock_enable_disabled_summary_bootloader_unlocked;
        } else if (isSimLockedDevice()) {
            oemUnlockSummary = R.string.oem_unlock_enable_disabled_summary_sim_locked_device;
        } else if (!isOemUnlockAllowedByUserAndCarrier()) {
            oemUnlockSummary = R.string.oem_unlock_enable_disabled_summary_connectivity_or_locked;
        }
        this.mPreference.setSummary((CharSequence) this.mContext.getResources().getString(oemUnlockSummary));
    }

    private boolean isSimLockedDevice() {
        int phoneCount = this.mTelephonyManager.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            if (this.mTelephonyManager.getAllowedCarriers(i).size() > 0) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isBootloaderUnlocked() {
        return this.mOemLockManager.isDeviceOemUnlocked();
    }

    private boolean enableOemUnlockPreference() {
        return (isBootloaderUnlocked() || !isOemUnlockAllowedByUserAndCarrier() || this.isUimLocked) ? false : true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean showKeyguardConfirmation(Resources resources, int requestCode) {
        return this.mChooseLockSettingsHelper.launchConfirmationActivity(requestCode, resources.getString(R.string.oem_unlock_enable));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void confirmEnableOemUnlock() {
        EnableOemUnlockSettingWarningDialog.show(this.mFragment);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isOemUnlockAllowedByUserAndCarrier() {
        return this.mOemLockManager.isOemUnlockAllowedByCarrier() && !this.mUserManager.hasBaseUserRestriction("no_factory_reset", UserHandle.of(UserHandle.myUserId()));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isOemUnlockedAllowed() {
        return this.mOemLockManager.isOemUnlockAllowed();
    }

    public static boolean isUimLockServiceEnable(Context context) {
        String platform = SystemProperties.get("ro.board.platform", null);
        if ("msm8998".equals(platform) || "msm8996".equals(platform)) {
            return false;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(simLockStateAppPackage, simLockStateAppService));
        List<ResolveInfo> matches = context.getPackageManager().queryIntentServices(intent, 0);
        if (matches == null || matches.size() <= 0) {
            return false;
        }
        return true;
    }
}
