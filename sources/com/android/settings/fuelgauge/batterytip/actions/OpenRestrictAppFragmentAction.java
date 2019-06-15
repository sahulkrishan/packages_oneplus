package com.android.settings.fuelgauge.batterytip.actions;

import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.fuelgauge.RestrictedAppDetails;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import com.android.settingslib.utils.ThreadUtils;
import java.util.List;

public class OpenRestrictAppFragmentAction extends BatteryTipAction {
    @VisibleForTesting
    BatteryDatabaseManager mBatteryDatabaseManager = BatteryDatabaseManager.getInstance(this.mContext);
    private final InstrumentedPreferenceFragment mFragment;
    private final RestrictAppTip mRestrictAppTip;

    public OpenRestrictAppFragmentAction(InstrumentedPreferenceFragment fragment, RestrictAppTip tip) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mRestrictAppTip = tip;
    }

    public void handlePositiveAction(int metricsKey) {
        this.mMetricsFeatureProvider.action(this.mContext, 1361, metricsKey);
        List<AppInfo> mAppInfos = this.mRestrictAppTip.getRestrictAppList();
        RestrictedAppDetails.startRestrictedAppDetails(this.mFragment, mAppInfos);
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$OpenRestrictAppFragmentAction$EtKh55lPJMI0rxkM0QFArF_zK8E(this, mAppInfos));
    }

    public static /* synthetic */ void lambda$handlePositiveAction$0(OpenRestrictAppFragmentAction openRestrictAppFragmentAction, List mAppInfos) {
        Log.d("RestrictAppFgt", "postOnBackgroundThread handlePositiveAction start");
        openRestrictAppFragmentAction.mBatteryDatabaseManager.updateAnomalies(mAppInfos, 1);
        Log.d("RestrictAppFgt", "postOnBackgroundThread handlePositiveAction end");
    }
}
