package com.android.settings.development;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class VerifyAppsOverUsbPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, AdbOnChangeListener, PreferenceControllerMixin {
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String VERIFY_APPS_OVER_USB_KEY = "verify_apps_over_usb";
    private final PackageManagerWrapper mPackageManager;
    private final RestrictedLockUtilsDelegate mRestrictedLockUtils = new RestrictedLockUtilsDelegate();

    @VisibleForTesting
    class RestrictedLockUtilsDelegate {
        RestrictedLockUtilsDelegate() {
        }

        public EnforcedAdmin checkIfRestrictionEnforced(Context context, String userRestriction, int userId) {
            return RestrictedLockUtils.checkIfRestrictionEnforced(context, userRestriction, userId);
        }
    }

    public VerifyAppsOverUsbPreferenceController(Context context) {
        super(context);
        this.mPackageManager = new PackageManagerWrapper(context.getPackageManager());
    }

    public boolean isAvailable() {
        return Global.getInt(this.mContext.getContentResolver(), "verifier_setting_visible", 1) > 0;
    }

    public String getPreferenceKey() {
        return VERIFY_APPS_OVER_USB_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "verifier_verify_adb_installs", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        RestrictedSwitchPreference restrictedPreference = (RestrictedSwitchPreference) preference;
        boolean checked = false;
        if (shouldBeEnabled()) {
            EnforcedAdmin enforcingAdmin = this.mRestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "ensure_verify_apps", UserHandle.myUserId());
            if (enforcingAdmin != null) {
                restrictedPreference.setChecked(true);
                restrictedPreference.setDisabledByAdmin(enforcingAdmin);
                return;
            }
            restrictedPreference.setEnabled(true);
            if (Global.getInt(this.mContext.getContentResolver(), "verifier_verify_adb_installs", 1) != 0) {
                checked = true;
            }
            restrictedPreference.setChecked(checked);
            return;
        }
        restrictedPreference.setChecked(false);
        restrictedPreference.setDisabledByAdmin(null);
        restrictedPreference.setEnabled(false);
    }

    public void onAdbSettingChanged() {
        if (isAvailable()) {
            updateState(this.mPreference);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        super.onDeveloperOptionsSwitchEnabled();
        updateState(this.mPreference);
    }

    private boolean shouldBeEnabled() {
        ContentResolver cr = this.mContext.getContentResolver();
        if (Global.getInt(cr, "adb_enabled", 0) == 0 || Global.getInt(cr, "package_verifier_enable", 1) == 0) {
            return false;
        }
        Intent verification = new Intent("android.intent.action.PACKAGE_NEEDS_VERIFICATION");
        verification.setType(PACKAGE_MIME_TYPE);
        verification.addFlags(1);
        if (this.mPackageManager.queryBroadcastReceivers(verification, 0).size() == 0) {
            return false;
        }
        return true;
    }
}
