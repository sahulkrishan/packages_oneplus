package com.android.settings.development;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class LocalTerminalPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String ENABLE_TERMINAL_KEY = "enable_terminal";
    @VisibleForTesting
    static final String TERMINAL_APP_PACKAGE = "com.android.terminal";
    private PackageManagerWrapper mPackageManager;
    private UserManager mUserManager;

    public LocalTerminalPreferenceController(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public boolean isAvailable() {
        return isPackageInstalled(TERMINAL_APP_PACKAGE);
    }

    public String getPreferenceKey() {
        return ENABLE_TERMINAL_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPackageManager = getPackageManagerWrapper();
        if (isAvailable() && !isEnabled()) {
            this.mPreference.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mPackageManager.setApplicationEnabledSetting(TERMINAL_APP_PACKAGE, ((Boolean) newValue).booleanValue() ? 1 : 0, 0);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        if (this.mPackageManager.getApplicationEnabledSetting(TERMINAL_APP_PACKAGE) != 1) {
            z = false;
        }
        ((SwitchPreference) this.mPreference).setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        if (isEnabled()) {
            this.mPreference.setEnabled(true);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        this.mPackageManager.setApplicationEnabledSetting(TERMINAL_APP_PACKAGE, 0, 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public PackageManagerWrapper getPackageManagerWrapper() {
        return new PackageManagerWrapper(this.mContext.getPackageManager());
    }

    private boolean isPackageInstalled(String packageName) {
        boolean z = false;
        try {
            if (this.mContext.getPackageManager().getPackageInfo(packageName, 0) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean isEnabled() {
        return this.mUserManager.isAdminUser();
    }
}
