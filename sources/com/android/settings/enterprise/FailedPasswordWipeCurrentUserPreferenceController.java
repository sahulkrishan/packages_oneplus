package com.android.settings.enterprise;

import android.content.Context;

public class FailedPasswordWipeCurrentUserPreferenceController extends FailedPasswordWipePreferenceControllerBase {
    private static final String KEY_FAILED_PASSWORD_WIPE_CURRENT_USER = "failed_password_wipe_current_user";

    public FailedPasswordWipeCurrentUserPreferenceController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public int getMaximumFailedPasswordsBeforeWipe() {
        return this.mFeatureProvider.getMaximumFailedPasswordsBeforeWipeInCurrentUser();
    }

    public String getPreferenceKey() {
        return KEY_FAILED_PASSWORD_WIPE_CURRENT_USER;
    }
}
