package com.android.settings.security;

import android.content.Context;

public class UserCredentialsPreferenceController extends RestrictedEncryptionPreferenceController {
    private static final String KEY_USER_CREDENTIALS = "user_credentials";

    public UserCredentialsPreferenceController(Context context) {
        super(context, "no_config_credentials");
    }

    public String getPreferenceKey() {
        return KEY_USER_CREDENTIALS;
    }
}
