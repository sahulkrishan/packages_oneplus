package com.android.settings.datausage;

import android.app.Activity;
import android.content.Intent;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import android.util.Log;
import android.util.RecurrenceRule;
import com.android.internal.util.CollectionUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import java.util.List;

public class DataUsageSummaryPreferenceController extends BasePreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart {
    private static final String KEY = "status_header";
    private static final long PETA = 1000000000000000L;
    private static final float RELATIVE_SIZE_LARGE = 1.5625f;
    private static final float RELATIVE_SIZE_SMALL = 0.64f;
    private static final String TAG = "DataUsageController";
    private final Activity mActivity;
    private CharSequence mCarrierName;
    private long mCycleEnd;
    private long mCycleStart;
    private long mDataBarSize;
    private final DataUsageInfoController mDataInfoController;
    private final DataUsageController mDataUsageController;
    private final DataUsageSummary mDataUsageSummary;
    private final int mDataUsageTemplate;
    private int mDataplanCount;
    private long mDataplanSize;
    private long mDataplanUse;
    private final NetworkTemplate mDefaultTemplate;
    private final EntityHeaderController mEntityHeaderController;
    private final boolean mHasMobileData;
    private final Lifecycle mLifecycle;
    private Intent mManageSubscriptionIntent;
    private final NetworkPolicyEditor mPolicyEditor;
    private long mSnapshotTime;
    private final SubscriptionManager mSubscriptionManager;

    public DataUsageSummaryPreferenceController(Activity activity, Lifecycle lifecycle, DataUsageSummary dataUsageSummary) {
        super(activity, KEY);
        this.mActivity = activity;
        this.mEntityHeaderController = EntityHeaderController.newInstance(activity, dataUsageSummary, null);
        this.mLifecycle = lifecycle;
        this.mDataUsageSummary = dataUsageSummary;
        int defaultSubId = DataUsageUtils.getDefaultSubscriptionId(activity);
        this.mDefaultTemplate = DataUsageUtils.getDefaultTemplate(activity, defaultSubId);
        this.mPolicyEditor = new NetworkPolicyEditor(NetworkPolicyManager.from(activity));
        boolean z = DataUsageUtils.hasMobileData(activity) && defaultSubId != -1;
        this.mHasMobileData = z;
        this.mDataUsageController = new DataUsageController(activity);
        this.mDataInfoController = new DataUsageInfoController();
        if (this.mHasMobileData) {
            this.mDataUsageTemplate = R.string.cell_data_template;
        } else if (DataUsageUtils.hasWifiRadio(activity)) {
            this.mDataUsageTemplate = R.string.wifi_data_template;
        } else {
            this.mDataUsageTemplate = R.string.ethernet_data_template;
        }
        this.mSubscriptionManager = (SubscriptionManager) this.mContext.getSystemService("telephony_subscription_service");
    }

    @VisibleForTesting
    DataUsageSummaryPreferenceController(DataUsageController dataUsageController, DataUsageInfoController dataInfoController, NetworkTemplate defaultTemplate, NetworkPolicyEditor policyEditor, int dataUsageTemplate, boolean hasMobileData, SubscriptionManager subscriptionManager, Activity activity, Lifecycle lifecycle, EntityHeaderController entityHeaderController, DataUsageSummary dataUsageSummary) {
        super(activity, KEY);
        this.mDataUsageController = dataUsageController;
        this.mDataInfoController = dataInfoController;
        this.mDefaultTemplate = defaultTemplate;
        this.mPolicyEditor = policyEditor;
        this.mDataUsageTemplate = dataUsageTemplate;
        this.mHasMobileData = hasMobileData;
        this.mSubscriptionManager = subscriptionManager;
        this.mActivity = activity;
        this.mLifecycle = lifecycle;
        this.mEntityHeaderController = entityHeaderController;
        this.mDataUsageSummary = dataUsageSummary;
    }

    public void onStart() {
        this.mEntityHeaderController.setRecyclerView(this.mDataUsageSummary.getListView(), this.mLifecycle);
        this.mEntityHeaderController.styleActionBar(this.mActivity);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPlanValues(int dataPlanCount, long dataPlanSize, long dataPlanUse) {
        this.mDataplanCount = dataPlanCount;
        this.mDataplanSize = dataPlanSize;
        this.mDataBarSize = dataPlanSize;
        this.mDataplanUse = dataPlanUse;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setCarrierValues(String carrierName, long snapshotTime, long cycleEnd, Intent intent) {
        this.mCarrierName = carrierName;
        this.mSnapshotTime = snapshotTime;
        this.mCycleEnd = cycleEnd;
        this.mManageSubscriptionIntent = intent;
    }

    public int getAvailabilityStatus() {
        return (DataUsageUtils.hasSim(this.mActivity) || DataUsageUtils.hasWifiRadio(this.mContext)) ? 0 : 1;
    }

    public void updateState(Preference preference) {
        DataUsageSummaryPreference summaryPreference = (DataUsageSummaryPreference) preference;
        DataUsageInfo info;
        if (DataUsageUtils.hasSim(this.mActivity)) {
            info = this.mDataUsageController.getDataUsageInfo(this.mDefaultTemplate);
            this.mDataInfoController.updateDataLimit(info, this.mPolicyEditor.getPolicy(this.mDefaultTemplate));
            summaryPreference.setWifiMode(false, null);
            if (this.mSubscriptionManager != null) {
                refreshDataplanInfo(info);
            }
            if (info.warningLevel > 0 && info.limitLevel > 0) {
                summaryPreference.setLimitInfo(TextUtils.expandTemplate(this.mContext.getText(R.string.cell_data_warning_and_limit), new CharSequence[]{DataUsageUtils.formatDataUsage(this.mContext, info.warningLevel), DataUsageUtils.formatDataUsage(this.mContext, info.limitLevel)}).toString());
            } else if (info.warningLevel > 0) {
                summaryPreference.setLimitInfo(TextUtils.expandTemplate(this.mContext.getText(R.string.cell_data_warning), new CharSequence[]{DataUsageUtils.formatDataUsage(this.mContext, info.warningLevel)}).toString());
            } else if (info.limitLevel > 0) {
                summaryPreference.setLimitInfo(TextUtils.expandTemplate(this.mContext.getText(R.string.cell_data_limit), new CharSequence[]{DataUsageUtils.formatDataUsage(this.mContext, info.limitLevel)}).toString());
            } else {
                summaryPreference.setLimitInfo(null);
            }
            summaryPreference.setUsageNumbers(this.mDataplanUse, this.mDataplanSize, this.mHasMobileData);
            if (this.mDataBarSize <= 0) {
                summaryPreference.setChartEnabled(false);
            } else {
                summaryPreference.setChartEnabled(true);
                summaryPreference.setLabels(DataUsageUtils.formatDataUsage(this.mContext, 0), DataUsageUtils.formatDataUsage(this.mContext, this.mDataBarSize));
                summaryPreference.setProgress(((float) this.mDataplanUse) / ((float) this.mDataBarSize));
            }
            summaryPreference.setUsageInfo(this.mCycleEnd, this.mSnapshotTime, this.mCarrierName, this.mDataplanCount, this.mManageSubscriptionIntent);
            return;
        }
        info = this.mDataUsageController.getDataUsageInfo(NetworkTemplate.buildTemplateWifiWildcard());
        summaryPreference.setWifiMode(true, info.period);
        summaryPreference.setLimitInfo(null);
        DataUsageSummaryPreference dataUsageSummaryPreference = summaryPreference;
        dataUsageSummaryPreference.setUsageNumbers(info.usageLevel, -1, true);
        summaryPreference.setChartEnabled(false);
        dataUsageSummaryPreference.setUsageInfo(info.cycleEnd, -1, null, 0, null);
    }

    private void refreshDataplanInfo(DataUsageInfo info) {
        this.mCarrierName = null;
        this.mDataplanCount = 0;
        this.mDataplanSize = -1;
        this.mDataBarSize = this.mDataInfoController.getSummaryLimit(info);
        this.mDataplanUse = info.usageLevel;
        this.mCycleStart = info.cycleStart;
        this.mCycleEnd = info.cycleEnd;
        this.mSnapshotTime = -1;
        int defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
        SubscriptionInfo subInfo = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        if (subInfo != null && this.mHasMobileData) {
            this.mCarrierName = subInfo.getCarrierName();
            List<SubscriptionPlan> plans = this.mSubscriptionManager.getSubscriptionPlans(defaultSubId);
            SubscriptionPlan primaryPlan = getPrimaryPlan(this.mSubscriptionManager, defaultSubId);
            if (primaryPlan != null) {
                this.mDataplanCount = plans.size();
                this.mDataplanSize = primaryPlan.getDataLimitBytes();
                if (unlimited(this.mDataplanSize)) {
                    this.mDataplanSize = -1;
                }
                this.mDataBarSize = this.mDataplanSize;
                this.mDataplanUse = primaryPlan.getDataUsageBytes();
                RecurrenceRule rule = primaryPlan.getCycleRule();
                if (!(rule == null || rule.start == null || rule.end == null)) {
                    this.mCycleStart = rule.start.toEpochSecond() * 1000;
                    this.mCycleEnd = rule.end.toEpochSecond() * 1000;
                }
                this.mSnapshotTime = primaryPlan.getDataUsageTime();
            }
        }
        this.mManageSubscriptionIntent = this.mSubscriptionManager.createManageSubscriptionIntent(defaultSubId);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Have ");
        stringBuilder.append(this.mDataplanCount);
        stringBuilder.append(" plans, dflt sub-id ");
        stringBuilder.append(defaultSubId);
        stringBuilder.append(", intent ");
        stringBuilder.append(this.mManageSubscriptionIntent);
        Log.i(str, stringBuilder.toString());
    }

    public static SubscriptionPlan getPrimaryPlan(SubscriptionManager subManager, int primaryId) {
        List<SubscriptionPlan> plans = subManager.getSubscriptionPlans(primaryId);
        SubscriptionPlan subscriptionPlan = null;
        if (CollectionUtils.isEmpty(plans)) {
            return null;
        }
        SubscriptionPlan plan = (SubscriptionPlan) plans.get(0);
        if (plan.getDataLimitBytes() > 0 && saneSize(plan.getDataUsageBytes()) && plan.getCycleRule() != null) {
            subscriptionPlan = plan;
        }
        return subscriptionPlan;
    }

    private static boolean saneSize(long value) {
        return value >= 0 && value < PETA;
    }

    public static boolean unlimited(long size) {
        return size == Long.MAX_VALUE;
    }
}
