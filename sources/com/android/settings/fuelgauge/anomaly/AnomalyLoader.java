package com.android.settings.fuelgauge.anomaly;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.fuelgauge.anomaly.Anomaly.Builder;
import com.android.settingslib.utils.AsyncLoader;
import java.util.ArrayList;
import java.util.List;

public class AnomalyLoader extends AsyncLoader<List<Anomaly>> {
    private static final String TAG = "AnomalyLoader";
    private static final boolean USE_FAKE_DATA = false;
    @VisibleForTesting
    AnomalyUtils mAnomalyUtils;
    private BatteryStatsHelper mBatteryStatsHelper;
    private String mPackageName;
    @VisibleForTesting
    AnomalyDetectionPolicy mPolicy;
    private UserManager mUserManager;

    public AnomalyLoader(Context context, BatteryStatsHelper batteryStatsHelper) {
        this(context, batteryStatsHelper, null, new AnomalyDetectionPolicy(context));
    }

    public AnomalyLoader(Context context, String packageName) {
        this(context, null, packageName, new AnomalyDetectionPolicy(context));
    }

    @VisibleForTesting
    AnomalyLoader(Context context, BatteryStatsHelper batteryStatsHelper, String packageName, AnomalyDetectionPolicy policy) {
        super(context);
        this.mBatteryStatsHelper = batteryStatsHelper;
        this.mPackageName = packageName;
        this.mAnomalyUtils = AnomalyUtils.getInstance(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mPolicy = policy;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(List<Anomaly> list) {
    }

    public List<Anomaly> loadInBackground() {
        if (this.mBatteryStatsHelper == null) {
            this.mBatteryStatsHelper = new BatteryStatsHelper(getContext());
            this.mBatteryStatsHelper.create((Bundle) null);
            this.mBatteryStatsHelper.refreshStats(0, this.mUserManager.getUserProfiles());
        }
        return this.mAnomalyUtils.detectAnomalies(this.mBatteryStatsHelper, this.mPolicy, this.mPackageName);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<Anomaly> generateFakeData() {
        List<Anomaly> anomalies = new ArrayList();
        String packageName = "com.android.settings";
        CharSequence displayName = "Settings";
        try {
            int uid = getContext().getPackageManager().getPackageUid("com.android.settings", 0);
            anomalies.add(new Builder().setUid(uid).setType(0).setPackageName("com.android.settings").setDisplayName(displayName).build());
            anomalies.add(new Builder().setUid(uid).setType(1).setPackageName("com.android.settings").setDisplayName(displayName).build());
            anomalies.add(new Builder().setUid(uid).setType(2).setPackageName("com.android.settings").setDisplayName(displayName).build());
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find package by name: com.android.settings", e);
        }
        return anomalies;
    }
}
