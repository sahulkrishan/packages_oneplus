package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class EnterpriseInstalledPackagesPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_NUMBER_ENTERPRISE_INSTALLED_PACKAGES = "number_enterprise_installed_packages";
    private final boolean mAsync;
    private final ApplicationFeatureProvider mFeatureProvider;

    public EnterpriseInstalledPackagesPreferenceController(Context context, boolean async) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getApplicationFeatureProvider(context);
        this.mAsync = async;
    }

    public void updateState(Preference preference) {
        this.mFeatureProvider.calculateNumberOfPolicyInstalledApps(true, new -$$Lambda$EnterpriseInstalledPackagesPreferenceController$ywnQ5T98AEytxQMBHl3WTR7fuAo(this, preference));
    }

    public static /* synthetic */ void lambda$updateState$0(EnterpriseInstalledPackagesPreferenceController enterpriseInstalledPackagesPreferenceController, Preference preference, int num) {
        boolean available;
        if (num == 0) {
            available = false;
        } else {
            available = true;
            preference.setSummary(enterpriseInstalledPackagesPreferenceController.mContext.getResources().getQuantityString(R.plurals.enterprise_privacy_number_packages_lower_bound, num, new Object[]{Integer.valueOf(num)}));
        }
        preference.setVisible(available);
    }

    public boolean isAvailable() {
        if (this.mAsync) {
            return true;
        }
        Boolean[] haveEnterpriseInstalledPackages = new Boolean[]{null};
        this.mFeatureProvider.calculateNumberOfPolicyInstalledApps(false, new -$$Lambda$EnterpriseInstalledPackagesPreferenceController$cz4T-BR7YJ9IEY1tdj7V5o_-Yuo(haveEnterpriseInstalledPackages));
        return haveEnterpriseInstalledPackages[0].booleanValue();
    }

    static /* synthetic */ void lambda$isAvailable$1(Boolean[] haveEnterpriseInstalledPackages, int num) {
        haveEnterpriseInstalledPackages[0] = Boolean.valueOf(num > 0);
    }

    public String getPreferenceKey() {
        return KEY_NUMBER_ENTERPRISE_INSTALLED_PACKAGES;
    }
}
