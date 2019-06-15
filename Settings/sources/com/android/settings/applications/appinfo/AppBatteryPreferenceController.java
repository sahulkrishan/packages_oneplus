package com.android.settings.applications.appinfo;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.fuelgauge.AdvancedPowerUsageDetail;
import com.android.settings.fuelgauge.BatteryEntry;
import com.android.settings.fuelgauge.BatteryStatsHelperLoader;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.ArrayList;
import java.util.List;

public class AppBatteryPreferenceController extends BasePreferenceController implements LoaderCallbacks<BatteryStatsHelper>, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_BATTERY = "battery";
    @VisibleForTesting
    BatteryStatsHelper mBatteryHelper;
    private String mBatteryPercent;
    @VisibleForTesting
    BatteryUtils mBatteryUtils = BatteryUtils.getInstance(this.mContext);
    private final String mPackageName;
    private final AppInfoDashboardFragment mParent;
    private Preference mPreference;
    @VisibleForTesting
    BatterySipper mSipper;

    public AppBatteryPreferenceController(Context context, AppInfoDashboardFragment parent, String packageName, Lifecycle lifecycle) {
        super(context, KEY_BATTERY);
        this.mParent = parent;
        this.mPackageName = packageName;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_app_info_settings_battery)) {
            return 0;
        }
        return 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
        this.mPreference.setEnabled(false);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_BATTERY.equals(preference.getKey())) {
            return false;
        }
        if (isBatteryStatsAvailable()) {
            BatteryEntry entry = new BatteryEntry(this.mContext, null, (UserManager) this.mContext.getSystemService("user"), this.mSipper);
            entry.defaultPackageName = this.mPackageName;
            AdvancedPowerUsageDetail.startBatteryDetailPage((SettingsActivity) this.mParent.getActivity(), this.mParent, this.mBatteryHelper, 0, entry, this.mBatteryPercent, null);
        } else {
            AdvancedPowerUsageDetail.startBatteryDetailPage((SettingsActivity) this.mParent.getActivity(), this.mParent, this.mPackageName);
        }
        return true;
    }

    public void onResume() {
        LoaderManager loaderManager = this.mParent.getLoaderManager();
        AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
        loaderManager.restartLoader(4, Bundle.EMPTY, this);
    }

    public void onPause() {
        LoaderManager loaderManager = this.mParent.getLoaderManager();
        AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
        loaderManager.destroyLoader(4);
    }

    public Loader<BatteryStatsHelper> onCreateLoader(int id, Bundle args) {
        return new BatteryStatsHelperLoader(this.mContext);
    }

    public void onLoadFinished(Loader<BatteryStatsHelper> loader, BatteryStatsHelper batteryHelper) {
        this.mBatteryHelper = batteryHelper;
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if (packageInfo != null) {
            this.mSipper = findTargetSipper(batteryHelper, packageInfo.applicationInfo.uid);
            if (this.mParent.getActivity() != null) {
                updateBattery();
            }
        }
    }

    public void onLoaderReset(Loader<BatteryStatsHelper> loader) {
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateBattery() {
        this.mPreference.setEnabled(true);
        if (isBatteryStatsAvailable()) {
            int dischargeAmount = this.mBatteryHelper.getStats().getDischargeAmount(0);
            this.mBatteryPercent = Utils.formatPercentage((int) this.mBatteryUtils.calculateBatteryPercent(this.mSipper.totalPowerMah, this.mBatteryHelper.getTotalPower(), this.mBatteryUtils.removeHiddenBatterySippers(new ArrayList(this.mBatteryHelper.getUsageList())), dischargeAmount));
            this.mPreference.setSummary(this.mContext.getString(R.string.battery_summary, new Object[]{this.mBatteryPercent}));
            return;
        }
        this.mPreference.setSummary(this.mContext.getString(R.string.no_battery_summary));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isBatteryStatsAvailable() {
        return (this.mBatteryHelper == null || this.mSipper == null) ? false : true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public BatterySipper findTargetSipper(BatteryStatsHelper batteryHelper, int uid) {
        List<BatterySipper> usageList = batteryHelper.getUsageList();
        int size = usageList.size();
        for (int i = 0; i < size; i++) {
            BatterySipper sipper = (BatterySipper) usageList.get(i);
            if (sipper.getUid() == uid) {
                return sipper;
            }
        }
        return null;
    }
}
