package com.android.settings.enterprise;

import android.content.Context;

public class FailedPasswordWipeManagedProfilePreferenceController extends FailedPasswordWipePreferenceControllerBase {
    private static final String KEY_FAILED_PASSWORD_WIPE_MANAGED_PROFILE = "failed_password_wipe_managed_profile";

    public FailedPasswordWipeManagedProfilePreferenceController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public int getMaximumFailedPasswordsBeforeWipe() {
        return this.mFeatureProvider.getMaximumFailedPasswordsBeforeWipeInManagedProfile();
    }

    public String getPreferenceKey() {
        return KEY_FAILED_PASSWORD_WIPE_MANAGED_PROFILE;
    }
}
