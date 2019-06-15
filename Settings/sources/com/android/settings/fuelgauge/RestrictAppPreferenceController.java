package com.android.settings.fuelgauge;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.BatteryTipUtils;
import java.util.List;

public class RestrictAppPreferenceController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_RESTRICT_APP = "restricted_app";
    @VisibleForTesting
    List<AppInfo> mAppInfos;
    private AppOpsManager mAppOpsManager;
    private InstrumentedPreferenceFragment mPreferenceFragment;
    private UserManager mUserManager;

    public RestrictAppPreferenceController(Context context) {
        super(context, KEY_RESTRICT_APP);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    public RestrictAppPreferenceController(InstrumentedPreferenceFragment preferenceFragment) {
        this(preferenceFragment.getContext());
        this.mPreferenceFragment = preferenceFragment;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mAppInfos = BatteryTipUtils.getRestrictedAppsList(this.mAppOpsManager, this.mUserManager);
        int num = this.mAppInfos.size();
        preference.setVisible(num > 0);
        preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.restricted_app_summary, num, new Object[]{Integer.valueOf(num)}));
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        RestrictedAppDetails.startRestrictedAppDetails(this.mPreferenceFragment, this.mAppInfos);
        return true;
    }
}
