package com.android.settings.enterprise;

import android.content.Context;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.applications.EnterpriseDefaultApps;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.users.UserFeatureProvider;
import com.android.settingslib.core.AbstractPreferenceController;

public class EnterpriseSetDefaultAppsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_DEFAULT_APPS = "number_enterprise_set_default_apps";
    private final ApplicationFeatureProvider mApplicationFeatureProvider;
    private final UserFeatureProvider mUserFeatureProvider;

    public EnterpriseSetDefaultAppsPreferenceController(Context context) {
        super(context);
        FeatureFactory factory = FeatureFactory.getFactory(context);
        this.mApplicationFeatureProvider = factory.getApplicationFeatureProvider(context);
        this.mUserFeatureProvider = factory.getUserFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        int num = getNumberOfEnterpriseSetDefaultApps();
        preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.enterprise_privacy_number_packages, num, new Object[]{Integer.valueOf(num)}));
    }

    public boolean isAvailable() {
        return getNumberOfEnterpriseSetDefaultApps() > 0;
    }

    public String getPreferenceKey() {
        return KEY_DEFAULT_APPS;
    }

    private int getNumberOfEnterpriseSetDefaultApps() {
        int num = 0;
        for (UserHandle user : this.mUserFeatureProvider.getUserProfiles()) {
            for (EnterpriseDefaultApps app : EnterpriseDefaultApps.values()) {
                num += this.mApplicationFeatureProvider.findPersistentPreferredActivities(user.getIdentifier(), app.getIntents()).size();
            }
        }
        return num;
    }
}
