package com.android.settings.security;

import android.content.Context;
import android.security.KeyStore;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class ResetCredentialsPreferenceController extends RestrictedEncryptionPreferenceController implements LifecycleObserver, OnResume {
    private static final String KEY_RESET_CREDENTIALS = "credentials_reset";
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private RestrictedPreference mPreference;

    public ResetCredentialsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "no_config_credentials");
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public String getPreferenceKey() {
        return KEY_RESET_CREDENTIALS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (RestrictedPreference) screen.findPreference(getPreferenceKey());
    }

    public void onResume() {
        if (this.mPreference != null && !this.mPreference.isDisabledByAdmin()) {
            this.mPreference.setEnabled(this.mKeyStore.isEmpty() ^ 1);
        }
    }
}
