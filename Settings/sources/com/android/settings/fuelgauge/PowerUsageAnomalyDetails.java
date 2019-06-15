package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.IconDrawableFactory;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.AnomalyDialogFragment;
import com.android.settings.fuelgauge.anomaly.AnomalyDialogFragment.AnomalyDialogListener;
import com.android.settings.fuelgauge.anomaly.AnomalyPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public class PowerUsageAnomalyDetails extends DashboardFragment implements AnomalyDialogListener {
    @VisibleForTesting
    static final String EXTRA_ANOMALY_LIST = "anomaly_list";
    private static final String KEY_PREF_ANOMALY_LIST = "app_abnormal_list";
    private static final int REQUEST_ANOMALY_ACTION = 0;
    public static final String TAG = "PowerAbnormalUsageDetail";
    @VisibleForTesting
    PreferenceGroup mAbnormalListGroup;
    @VisibleForTesting
    List<Anomaly> mAnomalies;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    IconDrawableFactory mIconDrawableFactory;
    @VisibleForTesting
    PackageManager mPackageManager;

    public static void startBatteryAbnormalPage(SettingsActivity caller, InstrumentedPreferenceFragment fragment, List<Anomaly> anomalies) {
        Bundle args = new Bundle();
        args.putParcelableList(EXTRA_ANOMALY_LIST, anomalies);
        new SubSettingLauncher(caller).setDestination(PowerUsageAnomalyDetails.class.getName()).setTitle((int) R.string.battery_abnormal_details_title).setArguments(args).setSourceMetricsCategory(fragment.getMetricsCategory()).launch();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        this.mAnomalies = getArguments().getParcelableArrayList(EXTRA_ANOMALY_LIST);
        this.mAbnormalListGroup = (PreferenceGroup) findPreference(KEY_PREF_ANOMALY_LIST);
        this.mPackageManager = context.getPackageManager();
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public void onResume() {
        super.onResume();
        refreshUi();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof AnomalyPreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        AnomalyDialogFragment dialogFragment = AnomalyDialogFragment.newInstance(((AnomalyPreference) preference).getAnomaly(), 987);
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(getFragmentManager(), TAG);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.power_abnormal_detail;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    public int getMetricsCategory() {
        return 987;
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshUi() {
        this.mAbnormalListGroup.removeAll();
        int size = this.mAnomalies.size();
        for (int i = 0; i < size; i++) {
            Anomaly anomaly = (Anomaly) this.mAnomalies.get(i);
            Preference pref = new AnomalyPreference(getPrefContext(), anomaly);
            pref.setSummary(this.mBatteryUtils.getSummaryResIdFromAnomalyType(anomaly.type));
            Drawable icon = getBadgedIcon(anomaly.packageName, UserHandle.getUserId(anomaly.uid));
            if (icon != null) {
                pref.setIcon(icon);
            }
            this.mAbnormalListGroup.addPreference(pref);
        }
    }

    public void onAnomalyHandled(Anomaly anomaly) {
        this.mAnomalies.remove(anomaly);
        refreshUi();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Drawable getBadgedIcon(String packageName, int userId) {
        return Utils.getBadgedIcon(this.mIconDrawableFactory, this.mPackageManager, packageName, userId);
    }
}
