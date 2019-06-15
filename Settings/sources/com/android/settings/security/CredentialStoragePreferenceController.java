package com.android.settings.security;

import android.content.Context;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import com.android.settings.R;

public class CredentialStoragePreferenceController extends RestrictedEncryptionPreferenceController {
    private static final String KEY_CREDENTIAL_STORAGE_TYPE = "credential_storage_type";
    private final KeyStore mKeyStore = KeyStore.getInstance();

    public CredentialStoragePreferenceController(Context context) {
        super(context, "no_config_credentials");
    }

    public String getPreferenceKey() {
        return KEY_CREDENTIAL_STORAGE_TYPE;
    }

    public void updateState(Preference preference) {
        int i;
        if (this.mKeyStore.isHardwareBacked()) {
            i = R.string.credential_storage_type_hardware;
        } else {
            i = R.string.credential_storage_type_software;
        }
        preference.setSummary(i);
    }
}
