package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.AnomalyDetectionPolicy;
import com.android.settings.fuelgauge.batterytip.BatteryTipLoader;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController.BatteryTipListener;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.utils.PowerUtil;
import com.android.settingslib.utils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PowerUsageSummary extends PowerUsageBase implements OnLongClickListener, BatteryTipListener {
    @VisibleForTesting
    static final int BATTERY_INFO_LOADER = 1;
    @VisibleForTesting
    static final int BATTERY_TIP_LOADER = 2;
    private static final boolean DEBUG = false;
    public static final int DEBUG_INFO_LOADER = 3;
    private static final String KEY_BATTERY_HEADER = "battery_header";
    private static final String KEY_BATTERY_SAVER_SUMMARY = "battery_saver_summary";
    private static final String KEY_BATTERY_TIP = "battery_tip";
    private static final String KEY_SCREEN_USAGE = "screen_usage";
    private static final String KEY_TIME_SINCE_LAST_FULL_CHARGE = "last_full_charge";
    @VisibleForTesting
    static final int MENU_ADVANCED_BATTERY = 2;
    @VisibleForTesting
    static final int MENU_STATS_TYPE = 1;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.power_usage_summary;
            return Collections.singletonList(sir);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> niks = super.getNonIndexableKeys(context);
            niks.add(PowerUsageSummary.KEY_BATTERY_SAVER_SUMMARY);
            return niks;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };
    static final String TAG = "PowerUsageSummary";
    @VisibleForTesting
    SparseArray<List<Anomaly>> mAnomalySparseArray;
    @VisibleForTesting
    BatteryHeaderPreferenceController mBatteryHeaderPreferenceController;
    @VisibleForTesting
    BatteryInfo mBatteryInfo;
    LoaderCallbacks<List<BatteryInfo>> mBatteryInfoDebugLoaderCallbacks = new LoaderCallbacks<List<BatteryInfo>>() {
        public Loader<List<BatteryInfo>> onCreateLoader(int i, Bundle bundle) {
            return new DebugEstimatesLoader(PowerUsageSummary.this.getContext(), PowerUsageSummary.this.mStatsHelper);
        }

        public void onLoadFinished(Loader<List<BatteryInfo>> loader, List<BatteryInfo> batteryInfos) {
            PowerUsageSummary.this.updateViews(batteryInfos);
        }

        public void onLoaderReset(Loader<List<BatteryInfo>> loader) {
        }
    };
    @VisibleForTesting
    LoaderCallbacks<BatteryInfo> mBatteryInfoLoaderCallbacks = new LoaderCallbacks<BatteryInfo>() {
        public Loader<BatteryInfo> onCreateLoader(int i, Bundle bundle) {
            return new BatteryInfoLoader(PowerUsageSummary.this.getContext(), PowerUsageSummary.this.mStatsHelper);
        }

        public void onLoadFinished(Loader<BatteryInfo> loader, BatteryInfo batteryInfo) {
            PowerUsageSummary.this.mBatteryHeaderPreferenceController.updateHeaderPreference(batteryInfo);
            PowerUsageSummary.this.mBatteryInfo = batteryInfo;
            PowerUsageSummary.this.updateLastFullChargePreference();
        }

        public void onLoaderReset(Loader<BatteryInfo> loader) {
        }
    };
    @VisibleForTesting
    LayoutPreference mBatteryLayoutPref;
    @VisibleForTesting
    BatteryTipPreferenceController mBatteryTipPreferenceController;
    private LoaderCallbacks<List<BatteryTip>> mBatteryTipsCallbacks = new LoaderCallbacks<List<BatteryTip>>() {
        public Loader<List<BatteryTip>> onCreateLoader(int id, Bundle args) {
            return new BatteryTipLoader(PowerUsageSummary.this.getContext(), PowerUsageSummary.this.mStatsHelper);
        }

        public void onLoadFinished(Loader<List<BatteryTip>> loader, List<BatteryTip> data) {
            PowerUsageSummary.this.mBatteryTipPreferenceController.updateBatteryTips(data);
        }

        public void onLoaderReset(Loader<List<BatteryTip>> loader) {
        }
    };
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    PowerGaugePreference mLastFullChargePref;
    @VisibleForTesting
    boolean mNeedUpdateBatteryTip;
    @VisibleForTesting
    PowerUsageFeatureProvider mPowerFeatureProvider;
    @VisibleForTesting
    PowerGaugePreference mScreenUsagePref;
    private int mStatsType = 0;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final BatteryBroadcastReceiver mBatteryBroadcastReceiver;
        private final Context mContext;
        private final SummaryLoader mLoader;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
            this.mBatteryBroadcastReceiver = new BatteryBroadcastReceiver(this.mContext);
            this.mBatteryBroadcastReceiver.setBatteryChangedListener(new -$$Lambda$PowerUsageSummary$SummaryProvider$kRfOu1vb_I8hwLBBDAS0-xe6-pM(this));
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mBatteryBroadcastReceiver.register();
            } else {
                this.mBatteryBroadcastReceiver.unRegister();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateViews(List<BatteryInfo> batteryInfos) {
        BatteryMeterView batteryView = (BatteryMeterView) this.mBatteryLayoutPref.findViewById(R.id.battery_header_icon);
        TextView summary1 = (TextView) this.mBatteryLayoutPref.findViewById(R.id.summary1);
        TextView summary2 = (TextView) this.mBatteryLayoutPref.findViewById(R.id.summary2);
        BatteryInfo oldInfo = (BatteryInfo) batteryInfos.get(0);
        BatteryInfo newInfo = (BatteryInfo) batteryInfos.get(1);
        ((TextView) this.mBatteryLayoutPref.findViewById(R.id.battery_percent)).setText(Utils.formatPercentage(oldInfo.batteryLevel));
        summary1.setText(this.mPowerFeatureProvider.getOldEstimateDebugString(Formatter.formatShortElapsedTime(getContext(), PowerUtil.convertUsToMs(oldInfo.remainingTimeUs))));
        summary2.setText(this.mPowerFeatureProvider.getEnhancedEstimateDebugString(Formatter.formatShortElapsedTime(getContext(), PowerUtil.convertUsToMs(newInfo.remainingTimeUs))));
        batteryView.setBatteryLevel(oldInfo.batteryLevel);
        batteryView.setCharging(1 ^ oldInfo.discharging);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setAnimationAllowed(true);
        initFeatureProvider();
        this.mBatteryLayoutPref = (LayoutPreference) findPreference(KEY_BATTERY_HEADER);
        ((Button) this.mBatteryLayoutPref.findViewById(R.id.btn_show_stats)).setOnClickListener(new -$$Lambda$PowerUsageSummary$00ln8-VbkueS9HRjA2L4UZ9tGr0(this));
        this.mScreenUsagePref = (PowerGaugePreference) findPreference(KEY_SCREEN_USAGE);
        this.mLastFullChargePref = (PowerGaugePreference) findPreference(KEY_TIME_SINCE_LAST_FULL_CHARGE);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.battery_footer_summary);
        this.mBatteryUtils = BatteryUtils.getInstance(getContext());
        this.mAnomalySparseArray = new SparseArray();
        restartBatteryInfoLoader();
        this.mBatteryTipPreferenceController.restoreInstanceState(icicle);
        updateBatteryTipFlag(icicle);
    }

    public static /* synthetic */ void lambda$onCreate$0(PowerUsageSummary powerUsageSummary, View view) {
        new SubSettingLauncher(powerUsageSummary.getContext()).setDestination(PowerUsageAdvanced.class.getName()).setSourceMetricsCategory(powerUsageSummary.getMetricsCategory()).setTitle((int) R.string.advanced_battery_title).launch();
        Log.d(TAG, "advanced -> launch");
    }

    public int getMetricsCategory() {
        return 1263;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.power_usage_summary;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        Lifecycle lifecycle = getLifecycle();
        SettingsActivity activity = (SettingsActivity) getActivity();
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mBatteryHeaderPreferenceController = new BatteryHeaderPreferenceController(context, activity, this, lifecycle);
        controllers.add(this.mBatteryHeaderPreferenceController);
        this.mBatteryTipPreferenceController = new BatteryTipPreferenceController(context, KEY_BATTERY_TIP, (SettingsActivity) getActivity(), this, this);
        controllers.add(this.mBatteryTipPreferenceController);
        controllers.add(new OPPowerOptimizePreferenceController(context));
        return controllers;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public int getHelpResource() {
        return R.string.help_url_battery;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (this.mStatsType == 0) {
                    this.mStatsType = 2;
                } else {
                    this.mStatsType = 0;
                }
                refreshUi(0);
                return true;
            case 2:
                new SubSettingLauncher(getContext()).setDestination(PowerUsageAdvanced.class.getName()).setSourceMetricsCategory(getMetricsCategory()).setTitle((int) R.string.advanced_battery_title).launch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Access modifiers changed, original: protected */
    public void refreshUi(int refreshType) {
        if (getContext() != null) {
            if (!this.mNeedUpdateBatteryTip || refreshType == 1) {
                this.mNeedUpdateBatteryTip = true;
            } else {
                restartBatteryTipLoader();
            }
            restartBatteryInfoLoader();
            updateLastFullChargePreference();
            this.mScreenUsagePref.setSubtitle(StringUtil.formatElapsedTime(getContext(), (double) this.mBatteryUtils.calculateScreenUsageTime(this.mStatsHelper), false));
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void restartBatteryTipLoader() {
        getLoaderManager().restartLoader(2, Bundle.EMPTY, this.mBatteryTipsCallbacks);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setBatteryLayoutPreference(LayoutPreference layoutPreference) {
        this.mBatteryLayoutPref = layoutPreference;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AnomalyDetectionPolicy getAnomalyDetectionPolicy() {
        return new AnomalyDetectionPolicy(getContext());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateLastFullChargePreference() {
        if (this.mBatteryInfo == null || this.mBatteryInfo.averageTimeToDischarge == -1) {
            long lastFullChargeTime = this.mBatteryUtils.calculateLastFullChargeTime(this.mStatsHelper, System.currentTimeMillis());
            this.mLastFullChargePref.setTitle((int) R.string.battery_last_full_charge);
            this.mLastFullChargePref.setSubtitle(StringUtil.formatRelativeTime(getContext(), (double) lastFullChargeTime, false));
            return;
        }
        this.mLastFullChargePref.setTitle((int) R.string.battery_full_charge_last);
        this.mLastFullChargePref.setSubtitle(StringUtil.formatElapsedTime(getContext(), (double) this.mBatteryInfo.averageTimeToDischarge, false));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showBothEstimates() {
        Context context = getContext();
        if (context != null && this.mPowerFeatureProvider.isEnhancedBatteryPredictionEnabled(context)) {
            getLoaderManager().restartLoader(3, Bundle.EMPTY, this.mBatteryInfoDebugLoaderCallbacks);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initFeatureProvider() {
        Context context = getContext();
        this.mPowerFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateAnomalySparseArray(List<Anomaly> anomalies) {
        this.mAnomalySparseArray.clear();
        for (Anomaly anomaly : anomalies) {
            if (this.mAnomalySparseArray.get(anomaly.uid) == null) {
                this.mAnomalySparseArray.append(anomaly.uid, new ArrayList());
            }
            ((List) this.mAnomalySparseArray.get(anomaly.uid)).add(anomaly);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void restartBatteryInfoLoader() {
        getLoaderManager().restartLoader(1, Bundle.EMPTY, this.mBatteryInfoLoaderCallbacks);
        if (this.mPowerFeatureProvider.isEstimateDebugEnabled()) {
            this.mBatteryLayoutPref.findViewById(R.id.summary1).setOnLongClickListener(this);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateBatteryTipFlag(Bundle icicle) {
        boolean z = icicle == null || this.mBatteryTipPreferenceController.needUpdate();
        this.mNeedUpdateBatteryTip = z;
    }

    public boolean onLongClick(View view) {
        showBothEstimates();
        view.setOnLongClickListener(null);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void restartBatteryStatsLoader(int refreshType) {
        super.restartBatteryStatsLoader(refreshType);
        this.mBatteryHeaderPreferenceController.quickUpdateHeaderPreference();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mBatteryTipPreferenceController.saveInstanceState(outState);
    }

    public void onBatteryTipHandled(BatteryTip batteryTip) {
        restartBatteryTipLoader();
    }

    @VisibleForTesting
    static CharSequence getDashboardLabel(Context context, BatteryInfo info) {
        BidiFormatter formatter = BidiFormatter.getInstance();
        if (info.remainingLabel == null) {
            return info.batteryPercentString;
        }
        return context.getString(R.string.power_remaining_settings_home_page, new Object[]{formatter.unicodeWrap(info.batteryPercentString), formatter.unicodeWrap(info.remainingLabel)});
    }
}
