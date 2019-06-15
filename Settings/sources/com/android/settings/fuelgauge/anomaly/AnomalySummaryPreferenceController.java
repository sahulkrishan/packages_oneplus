package com.android.settings.fuelgauge.anomaly;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.PowerUsageAnomalyDetails;
import java.util.List;

public class AnomalySummaryPreferenceController {
    public static final String ANOMALY_KEY = "high_usage";
    private static final int REQUEST_ANOMALY_ACTION = 0;
    private static final String TAG = "HighUsagePreferenceController";
    @VisibleForTesting
    List<Anomaly> mAnomalies;
    @VisibleForTesting
    Preference mAnomalyPreference = this.mFragment.getPreferenceScreen().findPreference(ANOMALY_KEY);
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private InstrumentedPreferenceFragment mFragment;
    private int mMetricsKey;
    private SettingsActivity mSettingsActivity;

    public AnomalySummaryPreferenceController(SettingsActivity activity, InstrumentedPreferenceFragment fragment) {
        this.mFragment = fragment;
        this.mSettingsActivity = activity;
        this.mMetricsKey = fragment.getMetricsCategory();
        this.mBatteryUtils = BatteryUtils.getInstance(activity.getApplicationContext());
        hideHighUsagePreference();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mAnomalies == null || !ANOMALY_KEY.equals(preference.getKey())) {
            return false;
        }
        if (this.mAnomalies.size() == 1) {
            AnomalyDialogFragment dialogFragment = AnomalyDialogFragment.newInstance((Anomaly) this.mAnomalies.get(0), this.mMetricsKey);
            dialogFragment.setTargetFragment(this.mFragment, 0);
            dialogFragment.show(this.mFragment.getFragmentManager(), TAG);
        } else {
            PowerUsageAnomalyDetails.startBatteryAbnormalPage(this.mSettingsActivity, this.mFragment, this.mAnomalies);
        }
        return true;
    }

    public void updateAnomalySummaryPreference(List<Anomaly> anomalies) {
        Context context = this.mFragment.getContext();
        this.mAnomalies = anomalies;
        if (this.mAnomalies.isEmpty()) {
            this.mAnomalyPreference.setVisible(false);
            return;
        }
        CharSequence summary;
        this.mAnomalyPreference.setVisible(true);
        int count = this.mAnomalies.size();
        CharSequence title = context.getResources().getQuantityString(R.plurals.power_high_usage_title, count, new Object[]{((Anomaly) this.mAnomalies.get(0)).displayName});
        if (count > 1) {
            summary = context.getString(R.string.battery_abnormal_apps_summary, new Object[]{Integer.valueOf(count)});
        } else {
            summary = context.getString(this.mBatteryUtils.getSummaryResIdFromAnomalyType(((Anomaly) this.mAnomalies.get(0)).type));
        }
        this.mAnomalyPreference.setTitle(title);
        this.mAnomalyPreference.setSummary(summary);
    }

    public void hideHighUsagePreference() {
        this.mAnomalyPreference.setVisible(false);
    }
}
