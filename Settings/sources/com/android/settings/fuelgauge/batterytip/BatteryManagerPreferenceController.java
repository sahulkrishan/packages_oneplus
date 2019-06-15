package com.android.settings.fuelgauge.batterytip;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.fuelgauge.PowerUsageFeatureProvider;
import com.android.settings.overlay.FeatureFactory;

public class BatteryManagerPreferenceController extends BasePreferenceController {
    private static final String KEY_BATTERY_MANAGER = "smart_battery_manager";
    private static final int ON = 1;
    private AppOpsManager mAppOpsManager;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;
    private UserManager mUserManager;

    public BatteryManagerPreferenceController(Context context) {
        super(context, KEY_BATTERY_MANAGER);
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        String setting;
        super.updateState(preference);
        int num = BatteryTipUtils.getRestrictedAppsList(this.mAppOpsManager, this.mUserManager).size();
        if (this.mPowerUsageFeatureProvider.isSmartBatterySupported()) {
            setting = "adaptive_battery_management_enabled";
        } else {
            setting = "app_auto_restriction_enabled";
        }
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), setting, 1) != 1) {
            z = false;
        }
        updateSummary(preference, z, num);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateSummary(Preference preference, boolean featureOn, int num) {
        if (num > 0) {
            preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.battery_manager_app_restricted, num, new Object[]{Integer.valueOf(num)}));
        } else if (featureOn) {
            preference.setSummary((int) R.string.battery_manager_on);
        } else {
            preference.setSummary((int) R.string.battery_manager_off);
        }
    }
}
