package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.dashboard.DashboardFragment;

public abstract class PowerUsageBase extends DashboardFragment {
    private static final String KEY_REFRESH_TYPE = "refresh_type";
    @VisibleForTesting
    static final int MENU_STATS_REFRESH = 2;
    private static final String TAG = "PowerUsageBase";
    private BatteryBroadcastReceiver mBatteryBroadcastReceiver;
    protected BatteryStatsHelper mStatsHelper;
    protected UserManager mUm;

    public class PowerLoaderCallback implements LoaderCallbacks<BatteryStatsHelper> {
        private int mRefreshType;

        public Loader<BatteryStatsHelper> onCreateLoader(int id, Bundle args) {
            this.mRefreshType = args.getInt(PowerUsageBase.KEY_REFRESH_TYPE);
            return new BatteryStatsHelperLoader(PowerUsageBase.this.getContext());
        }

        public void onLoadFinished(Loader<BatteryStatsHelper> loader, BatteryStatsHelper statsHelper) {
            PowerUsageBase.this.mStatsHelper = statsHelper;
            PowerUsageBase.this.refreshUi(this.mRefreshType);
        }

        public void onLoaderReset(Loader<BatteryStatsHelper> loader) {
        }
    }

    public abstract void refreshUi(int i);

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mUm = (UserManager) activity.getSystemService("user");
        this.mStatsHelper = new BatteryStatsHelper(activity, true);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mStatsHelper.create(icicle);
        setHasOptionsMenu(true);
        this.mBatteryBroadcastReceiver = new BatteryBroadcastReceiver(getContext());
        this.mBatteryBroadcastReceiver.setBatteryChangedListener(new -$$Lambda$PowerUsageBase$FbH3lnw7c_hajFOBNpt07exnLiM(this));
    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();
        BatteryStatsHelper.dropFile(getActivity(), BatteryHistoryDetail.BATTERY_HISTORY_FILE);
        this.mBatteryBroadcastReceiver.register();
    }

    public void onPause() {
        super.onPause();
        this.mBatteryBroadcastReceiver.unRegister();
    }

    /* Access modifiers changed, original: protected */
    public void restartBatteryStatsLoader(int refreshType) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_REFRESH_TYPE, refreshType);
        try {
            getLoaderManager().restartLoader(0, bundle, new PowerLoaderCallback());
        } catch (IllegalStateException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("E: ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public void updatePreference(BatteryHistoryPreference historyPref) {
        long startTime = System.currentTimeMillis();
        historyPref.setStats(this.mStatsHelper);
        BatteryUtils.logRuntime(TAG, "updatePreference", startTime);
    }
}
