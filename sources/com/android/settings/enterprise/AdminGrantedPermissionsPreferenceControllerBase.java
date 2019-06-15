package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class AdminGrantedPermissionsPreferenceControllerBase extends AbstractPreferenceController implements PreferenceControllerMixin {
    private final boolean mAsync;
    private final ApplicationFeatureProvider mFeatureProvider;
    private boolean mHasApps = false;
    private final String[] mPermissions;

    public AdminGrantedPermissionsPreferenceControllerBase(Context context, boolean async, String[] permissions) {
        super(context);
        this.mPermissions = permissions;
        this.mFeatureProvider = FeatureFactory.getFactory(context).getApplicationFeatureProvider(context);
        this.mAsync = async;
    }

    public void updateState(Preference preference) {
        this.mFeatureProvider.calculateNumberOfAppsWithAdminGrantedPermissions(this.mPermissions, true, new -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$8oa0oEjJK2SZdXqZGB2HrMlBk_0(this, preference));
    }

    public static /* synthetic */ void lambda$updateState$0(AdminGrantedPermissionsPreferenceControllerBase adminGrantedPermissionsPreferenceControllerBase, Preference preference, int num) {
        if (num == 0) {
            adminGrantedPermissionsPreferenceControllerBase.mHasApps = false;
        } else {
            preference.setSummary(adminGrantedPermissionsPreferenceControllerBase.mContext.getResources().getQuantityString(R.plurals.enterprise_privacy_number_packages_lower_bound, num, new Object[]{Integer.valueOf(num)}));
            adminGrantedPermissionsPreferenceControllerBase.mHasApps = true;
        }
        preference.setVisible(adminGrantedPermissionsPreferenceControllerBase.mHasApps);
    }

    public boolean isAvailable() {
        if (this.mAsync) {
            return true;
        }
        Boolean[] haveAppsWithAdminGrantedPermissions = new Boolean[]{null};
        this.mFeatureProvider.calculateNumberOfAppsWithAdminGrantedPermissions(this.mPermissions, false, new -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$4ZAcP8cSJJvD_RXkeJP9Rdjuu0k(haveAppsWithAdminGrantedPermissions));
        this.mHasApps = haveAppsWithAdminGrantedPermissions[0].booleanValue();
        return this.mHasApps;
    }

    static /* synthetic */ void lambda$isAvailable$1(Boolean[] haveAppsWithAdminGrantedPermissions, int num) {
        haveAppsWithAdminGrantedPermissions[0] = Boolean.valueOf(num > 0);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (getPreferenceKey().equals(preference.getKey()) && this.mHasApps) {
            return super.handlePreferenceTreeClick(preference);
        }
        return false;
    }
}
