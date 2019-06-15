package com.android.settings.enterprise;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;

public class CaCertsCurrentUserPreferenceController extends CaCertsPreferenceControllerBase {
    @VisibleForTesting
    static final String CA_CERTS_CURRENT_USER = "ca_certs_current_user";

    public CaCertsCurrentUserPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return CA_CERTS_CURRENT_USER;
    }

    public void updateState(Preference preference) {
        int i;
        super.updateState(preference);
        if (this.mFeatureProvider.isInCompMode()) {
            i = R.string.enterprise_privacy_ca_certs_personal;
        } else {
            i = R.string.enterprise_privacy_ca_certs_device;
        }
        preference.setTitle(i);
    }

    /* Access modifiers changed, original: protected */
    public int getNumberOfCaCerts() {
        return this.mFeatureProvider.getNumberOfOwnerInstalledCaCertsForCurrentUser();
    }
}
