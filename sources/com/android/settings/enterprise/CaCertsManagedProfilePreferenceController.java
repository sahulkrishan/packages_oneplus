package com.android.settings.enterprise;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

public class CaCertsManagedProfilePreferenceController extends CaCertsPreferenceControllerBase {
    @VisibleForTesting
    static final String CA_CERTS_MANAGED_PROFILE = "ca_certs_managed_profile";

    public CaCertsManagedProfilePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return CA_CERTS_MANAGED_PROFILE;
    }

    /* Access modifiers changed, original: protected */
    public int getNumberOfCaCerts() {
        return this.mFeatureProvider.getNumberOfOwnerInstalledCaCertsForManagedProfile();
    }
}
