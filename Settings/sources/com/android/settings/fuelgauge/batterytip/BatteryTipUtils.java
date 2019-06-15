package com.android.settings.fuelgauge.batterytip;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.PendingIntent;
import android.app.StatsManager;
import android.app.StatsManager.StatsUnavailableException;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import com.android.internal.util.CollectionUtils;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.fuelgauge.batterytip.AppInfo.Builder;
import com.android.settings.fuelgauge.batterytip.actions.BatterySaverAction;
import com.android.settings.fuelgauge.batterytip.actions.BatteryTipAction;
import com.android.settings.fuelgauge.batterytip.actions.OpenBatterySaverAction;
import com.android.settings.fuelgauge.batterytip.actions.OpenRestrictAppFragmentAction;
import com.android.settings.fuelgauge.batterytip.actions.RestrictAppAction;
import com.android.settings.fuelgauge.batterytip.actions.SmartBatteryAction;
import com.android.settings.fuelgauge.batterytip.actions.UnrestrictAppAction;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import com.android.settings.fuelgauge.batterytip.tips.UnrestrictAppTip;
import java.util.ArrayList;
import java.util.List;

public class BatteryTipUtils {
    private static final int REQUEST_CODE = 0;

    @NonNull
    public static List<AppInfo> getRestrictedAppsList(AppOpsManager appOpsManager, UserManager userManager) {
        List<UserHandle> userHandles = userManager.getUserProfiles();
        List<PackageOps> packageOpsList = appOpsManager.getPackagesForOps(new int[]{78});
        List<AppInfo> appInfos = new ArrayList();
        int size = CollectionUtils.size(packageOpsList);
        for (int i = 0; i < size; i++) {
            PackageOps packageOps = (PackageOps) packageOpsList.get(i);
            List<OpEntry> entries = packageOps.getOps();
            int entriesSize = entries.size();
            for (int j = 0; j < entriesSize; j++) {
                OpEntry entry = (OpEntry) entries.get(j);
                if (entry.getOp() == 78 && entry.getMode() != 0 && userHandles.contains(new UserHandle(UserHandle.getUserId(packageOps.getUid())))) {
                    appInfos.add(new Builder().setPackageName(packageOps.getPackageName()).setUid(packageOps.getUid()).build());
                }
            }
        }
        return appInfos;
    }

    public static BatteryTipAction getActionForBatteryTip(BatteryTip batteryTip, SettingsActivity settingsActivity, InstrumentedPreferenceFragment fragment) {
        int type = batteryTip.getType();
        if (type == 3 || type == 5) {
            if (batteryTip.getState() == 1) {
                return new OpenBatterySaverAction(settingsActivity);
            }
            return new BatterySaverAction(settingsActivity);
        } else if (type == 7) {
            return new UnrestrictAppAction(settingsActivity, (UnrestrictAppTip) batteryTip);
        } else {
            switch (type) {
                case 0:
                    return new SmartBatteryAction(settingsActivity, fragment);
                case 1:
                    if (batteryTip.getState() == 1) {
                        return new OpenRestrictAppFragmentAction(fragment, (RestrictAppTip) batteryTip);
                    }
                    return new RestrictAppAction(settingsActivity, (RestrictAppTip) batteryTip);
                default:
                    return null;
            }
        }
    }

    public static void uploadAnomalyPendingIntent(Context context, StatsManager statsManager) throws StatsUnavailableException {
        statsManager.setBroadcastSubscriber(PendingIntent.getBroadcast(context, null, new Intent(context, AnomalyDetectionReceiver.class), 134217728), 1, 1);
    }
}
