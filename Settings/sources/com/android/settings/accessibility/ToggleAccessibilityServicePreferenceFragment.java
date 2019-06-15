package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.password.ConfirmDeviceCredentialActivity;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.List;

public class ToggleAccessibilityServicePreferenceFragment extends ToggleFeaturePreferenceFragment implements OnClickListener {
    public static final int ACTIVITY_REQUEST_CONFIRM_CREDENTIAL_FOR_WEAKER_ENCRYPTION = 1;
    private static final int DIALOG_ID_DISABLE_WARNING = 2;
    private static final int DIALOG_ID_ENABLE_WARNING = 1;
    private ComponentName mComponentName;
    private LockPatternUtils mLockPatternUtils;
    private final SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            ToggleAccessibilityServicePreferenceFragment.this.updateSwitchBarToggleSwitch();
        }
    };
    private int mShownDialogId;

    public int getMetricsCategory() {
        return 4;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater infalter) {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
    }

    public void onResume() {
        this.mSettingsContentObserver.register(getContentResolver());
        updateSwitchBarToggleSwitch();
        super.onResume();
    }

    public void onPause() {
        this.mSettingsContentObserver.unregister(getContentResolver());
        super.onPause();
    }

    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        AccessibilityUtils.setAccessibilityServiceState(getActivity(), ComponentName.unflattenFromString(preferenceKey), enabled);
    }

    private AccessibilityServiceInfo getAccessibilityServiceInfo() {
        List<AccessibilityServiceInfo> serviceInfos = AccessibilityManager.getInstance(getActivity()).getInstalledAccessibilityServiceList();
        int serviceInfoCount = serviceInfos.size();
        for (int i = 0; i < serviceInfoCount; i++) {
            AccessibilityServiceInfo serviceInfo = (AccessibilityServiceInfo) serviceInfos.get(i);
            ResolveInfo resolveInfo = serviceInfo.getResolveInfo();
            if (this.mComponentName.getPackageName().equals(resolveInfo.serviceInfo.packageName) && this.mComponentName.getClassName().equals(resolveInfo.serviceInfo.name)) {
                return serviceInfo;
            }
        }
        return null;
    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case 1:
                this.mShownDialogId = 1;
                AccessibilityServiceInfo info = getAccessibilityServiceInfo();
                if (info == null) {
                    return null;
                }
                return AccessibilityServiceWarning.createCapabilitiesDialog(getActivity(), info, this);
            case 2:
                this.mShownDialogId = 2;
                if (getAccessibilityServiceInfo() == null) {
                    return null;
                }
                return new Builder(getActivity()).setTitle(getString(R.string.disable_service_title, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())})).setMessage(getString(R.string.disable_service_message, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())})).setCancelable(true).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == 1) {
            return 583;
        }
        return 584;
    }

    private void updateSwitchBarToggleSwitch() {
        this.mSwitchBar.setCheckedInternal(AccessibilityUtils.getEnabledServicesFromSettings(getActivity()).contains(this.mComponentName));
    }

    private boolean isFullDiskEncrypted() {
        return StorageManager.isNonDefaultBlockEncrypted();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            return;
        }
        if (resultCode == -1) {
            handleConfirmServiceEnabled(true);
            if (isFullDiskEncrypted()) {
                this.mLockPatternUtils.clearEncryptionPassword();
                Global.putInt(getContentResolver(), "require_password_to_decrypt", 0);
                return;
            }
            return;
        }
        handleConfirmServiceEnabled(false);
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean checked = false;
        switch (which) {
            case -2:
                if (this.mShownDialogId == 2) {
                    checked = true;
                }
                handleConfirmServiceEnabled(checked);
                return;
            case -1:
                if (this.mShownDialogId != 1) {
                    handleConfirmServiceEnabled(false);
                    return;
                } else if (isFullDiskEncrypted()) {
                    startActivityForResult(ConfirmDeviceCredentialActivity.createIntent(createConfirmCredentialReasonMessage(), null), 1);
                    return;
                } else {
                    handleConfirmServiceEnabled(true);
                    return;
                }
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleConfirmServiceEnabled(boolean confirmed) {
        this.mSwitchBar.setCheckedInternal(confirmed);
        getArguments().putBoolean("checked", confirmed);
        onPreferenceToggled(this.mPreferenceKey, confirmed);
    }

    private String createConfirmCredentialReasonMessage() {
        int resId = R.string.enable_service_password_reason;
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId());
        if (keyguardStoredPasswordQuality == 65536) {
            resId = R.string.enable_service_pattern_reason;
        } else if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) {
            resId = R.string.enable_service_pin_reason;
        }
        return getString(resId, new Object[]{getAccessibilityServiceInfo().getResolveInfo().loadLabel(getPackageManager())});
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
        super.onInstallSwitchBarToggleSwitch();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                if (checked) {
                    ToggleAccessibilityServicePreferenceFragment.this.mSwitchBar.setCheckedInternal(false);
                    ToggleAccessibilityServicePreferenceFragment.this.getArguments().putBoolean("checked", false);
                    ToggleAccessibilityServicePreferenceFragment.this.showDialog(1);
                } else {
                    ToggleAccessibilityServicePreferenceFragment.this.mSwitchBar.setCheckedInternal(true);
                    ToggleAccessibilityServicePreferenceFragment.this.getArguments().putBoolean("checked", true);
                    ToggleAccessibilityServicePreferenceFragment.this.showDialog(2);
                }
                return true;
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        String settingsTitle = arguments.getString("settings_title");
        String settingsComponentName = arguments.getString("settings_component_name");
        if (!(TextUtils.isEmpty(settingsTitle) || TextUtils.isEmpty(settingsComponentName))) {
            Intent settingsIntent = new Intent("android.intent.action.MAIN").setComponent(ComponentName.unflattenFromString(settingsComponentName.toString()));
            if (!getPackageManager().queryIntentActivities(settingsIntent, 0).isEmpty()) {
                this.mSettingsTitle = settingsTitle;
                this.mSettingsIntent = settingsIntent;
                setHasOptionsMenu(true);
            }
        }
        this.mComponentName = (ComponentName) arguments.getParcelable("component_name");
    }
}
