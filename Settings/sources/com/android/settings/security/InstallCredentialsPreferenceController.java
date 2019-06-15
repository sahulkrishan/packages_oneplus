package com.android.settings.security;

import android.content.Context;

public class InstallCredentialsPreferenceController extends RestrictedEncryptionPreferenceController {
    private static final String KEY_CREDENTIALS_INSTALL = "credentials_install";

    public InstallCredentialsPreferenceController(Context context) {
        super(context, "no_config_credentials");
    }

    public String getPreferenceKey() {
        return KEY_CREDENTIALS_INSTALL;
    }
}
