package com.android.settings.fuelgauge;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.fuelgauge.anomaly.Anomaly;
import com.android.settings.fuelgauge.anomaly.AnomalyDialogFragment.AnomalyDialogListener;
import com.android.settings.fuelgauge.anomaly.AnomalyLoader;
import com.android.settings.fuelgauge.anomaly.AnomalySummaryPreferenceController;
import com.android.settings.fuelgauge.anomaly.AnomalyUtils;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController.BatteryTipListener;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.Utils;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public class AdvancedPowerUsageDetail extends DashboardFragment implements AppButtonsDialogListener, AnomalyDialogListener, LoaderCallbacks<List<Anomaly>>, BatteryTipListener {
    private static final int ANOMALY_LOADER = 0;
    public static final String EXTRA_ANOMALY_LIST = "extra_anomaly_list";
    public static final String EXTRA_BACKGROUND_TIME = "extra_background_time";
    public static final String EXTRA_FOREGROUND_TIME = "extra_foreground_time";
    public static final String EXTRA_ICON_ID = "extra_icon_id";
    public static final String EXTRA_LABEL = "extra_label";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    public static final String EXTRA_POWER_USAGE_AMOUNT = "extra_power_usage_amount";
    public static final String EXTRA_POWER_USAGE_PERCENT = "extra_power_usage_percent";
    public static final String EXTRA_UID = "extra_uid";
    private static final String KEY_PREF_BACKGROUND = "app_usage_background";
    private static final String KEY_PREF_FOREGROUND = "app_usage_foreground";
    private static final String KEY_PREF_HEADER = "header_view";
    private static final int REQUEST_REMOVE_DEVICE_ADMIN = 1;
    private static final int REQUEST_UNINSTALL = 0;
    public static final String TAG = "AdvancedPowerDetail";
    private List<Anomaly> mAnomalies;
    @VisibleForTesting
    AnomalySummaryPreferenceController mAnomalySummaryPreferenceController;
    private AppButtonsPreferenceController mAppButtonsPreferenceController;
    @VisibleForTesting
    AppEntry mAppEntry;
    private BackgroundActivityPreferenceController mBackgroundActivityPreferenceController;
    @VisibleForTesting
    Preference mBackgroundPreference;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private DevicePolicyManager mDpm;
    @VisibleForTesting
    Preference mForegroundPreference;
    @VisibleForTesting
    LayoutPreference mHeaderPreference;
    private PackageManager mPackageManager;
    private String mPackageName;
    @VisibleForTesting
    ApplicationsState mState;
    private UserManager mUserManager;

    @VisibleForTesting
    static void startBatteryDetailPage(Activity caller, BatteryUtils batteryUtils, InstrumentedPreferenceFragment fragment, BatteryStatsHelper helper, int which, BatteryEntry entry, String usagePercent, List<Anomaly> anomalies) {
        long foregroundTimeMs;
        long backgroundTimeMs;
        BatteryUtils batteryUtils2 = batteryUtils;
        int i = which;
        BatteryEntry batteryEntry = entry;
        helper.getStats();
        Bundle args = new Bundle();
        BatterySipper sipper = batteryEntry.sipper;
        Uid uid = sipper.uidObj;
        boolean isTypeApp = sipper.drainType == DrainType.APP;
        if (isTypeApp) {
            foregroundTimeMs = batteryUtils2.getProcessTimeMs(1, uid, i);
        } else {
            foregroundTimeMs = sipper.usageTimeMs;
        }
        if (isTypeApp) {
            backgroundTimeMs = batteryUtils2.getProcessTimeMs(2, uid, i);
        } else {
            backgroundTimeMs = 0;
        }
        if (ArrayUtils.isEmpty(sipper.mPackages)) {
            args.putString(EXTRA_LABEL, entry.getLabel());
            args.putInt(EXTRA_ICON_ID, batteryEntry.iconId);
            args.putString(EXTRA_PACKAGE_NAME, null);
        } else {
            String str;
            String str2 = EXTRA_PACKAGE_NAME;
            if (batteryEntry.defaultPackageName != null) {
                str = batteryEntry.defaultPackageName;
            } else {
                str = sipper.mPackages[0];
            }
            args.putString(str2, str);
        }
        args.putInt(EXTRA_UID, sipper.getUid());
        args.putLong(EXTRA_BACKGROUND_TIME, backgroundTimeMs);
        args.putLong(EXTRA_FOREGROUND_TIME, foregroundTimeMs);
        args.putString(EXTRA_POWER_USAGE_PERCENT, usagePercent);
        args.putInt(EXTRA_POWER_USAGE_AMOUNT, (int) sipper.totalPowerMah);
        args.putParcelableList(EXTRA_ANOMALY_LIST, anomalies);
        new SubSettingLauncher(caller).setDestination(AdvancedPowerUsageDetail.class.getName()).setTitle((int) R.string.battery_details_title).setArguments(args).setSourceMetricsCategory(fragment.getMetricsCategory()).setUserHandle(new UserHandle(getUserIdToLaunchAdvancePowerUsageDetail(sipper))).launch();
    }

    private static int getUserIdToLaunchAdvancePowerUsageDetail(BatterySipper bs) {
        if (bs.drainType == DrainType.USER) {
            return ActivityManager.getCurrentUser();
        }
        return UserHandle.getUserId(bs.getUid());
    }

    public static void startBatteryDetailPage(Activity caller, InstrumentedPreferenceFragment fragment, BatteryStatsHelper helper, int which, BatteryEntry entry, String usagePercent, List<Anomaly> anomalies) {
        startBatteryDetailPage(caller, BatteryUtils.getInstance(caller), fragment, helper, which, entry, usagePercent, anomalies);
    }

    public static void startBatteryDetailPage(Activity caller, InstrumentedPreferenceFragment fragment, String packageName) {
        Bundle args = new Bundle(3);
        PackageManager packageManager = caller.getPackageManager();
        args.putString(EXTRA_PACKAGE_NAME, packageName);
        args.putString(EXTRA_POWER_USAGE_PERCENT, Utils.formatPercentage(0));
        try {
            args.putInt(EXTRA_UID, packageManager.getPackageUid(packageName, 0));
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find package: ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
        }
        new SubSettingLauncher(caller).setDestination(AdvancedPowerUsageDetail.class.getName()).setTitle((int) R.string.battery_details_title).setArguments(args).setSourceMetricsCategory(fragment.getMetricsCategory()).launch();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mState = ApplicationsState.getInstance(getActivity().getApplication());
        this.mDpm = (DevicePolicyManager) activity.getSystemService("device_policy");
        this.mUserManager = (UserManager) activity.getSystemService("user");
        this.mPackageManager = activity.getPackageManager();
        this.mBatteryUtils = BatteryUtils.getInstance(getContext());
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPackageName = getArguments().getString(EXTRA_PACKAGE_NAME);
        this.mAnomalySummaryPreferenceController = new AnomalySummaryPreferenceController((SettingsActivity) getActivity(), this);
        this.mForegroundPreference = findPreference(KEY_PREF_FOREGROUND);
        this.mBackgroundPreference = findPreference(KEY_PREF_BACKGROUND);
        this.mHeaderPreference = (LayoutPreference) findPreference(KEY_PREF_HEADER);
        if (this.mPackageName != null) {
            this.mAppEntry = this.mState.getEntry(this.mPackageName, UserHandle.myUserId());
            initAnomalyInfo();
        }
    }

    public void onResume() {
        super.onResume();
        initHeader();
        initPreference();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initAnomalyInfo() {
        this.mAnomalies = getArguments().getParcelableArrayList(EXTRA_ANOMALY_LIST);
        if (this.mAnomalies == null) {
            getLoaderManager().initLoader(0, Bundle.EMPTY, this);
        } else if (this.mAnomalies != null) {
            this.mAnomalySummaryPreferenceController.updateAnomalySummaryPreference(this.mAnomalies);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initHeader() {
        View appSnippet = this.mHeaderPreference.findViewById(R.id.entity_header);
        Activity context = getActivity();
        Bundle bundle = getArguments();
        EntityHeaderController controller = EntityHeaderController.newInstance(context, this, appSnippet).setRecyclerView(getListView(), getLifecycle()).setButtonActions(0, 0);
        if (this.mAppEntry == null) {
            controller.setLabel(bundle.getString(EXTRA_LABEL));
            if (bundle.getInt(EXTRA_ICON_ID, 0) == 0) {
                controller.setIcon(context.getPackageManager().getDefaultActivityIcon());
            } else {
                controller.setIcon(context.getDrawable(bundle.getInt(EXTRA_ICON_ID)));
            }
        } else {
            this.mState.ensureIcon(this.mAppEntry);
            controller.setLabel(this.mAppEntry);
            controller.setIcon(this.mAppEntry);
            CharSequence summary = AppUtils.isInstant(this.mAppEntry.info) ? null : getString(com.android.settings.Utils.getInstallationStatus(this.mAppEntry.info));
            controller.setIsInstantApp(AppUtils.isInstant(this.mAppEntry.info));
            controller.setSummary(summary);
        }
        controller.done(context, true);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initPreference() {
        Bundle bundle = getArguments();
        Context context = getContext();
        long foregroundTimeMs = bundle.getLong(EXTRA_FOREGROUND_TIME);
        long backgroundTimeMs = bundle.getLong(EXTRA_BACKGROUND_TIME);
        String usagePercent = bundle.getString(EXTRA_POWER_USAGE_PERCENT);
        int powerMah = bundle.getInt(EXTRA_POWER_USAGE_AMOUNT);
        this.mForegroundPreference.setSummary(TextUtils.expandTemplate(getText(R.string.battery_used_for), new CharSequence[]{StringUtil.formatElapsedTime(context, (double) foregroundTimeMs, false)}));
        this.mBackgroundPreference.setSummary(TextUtils.expandTemplate(getText(R.string.battery_active_for), new CharSequence[]{StringUtil.formatElapsedTime(context, (double) backgroundTimeMs, false)}));
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), AnomalySummaryPreferenceController.ANOMALY_KEY)) {
            return super.onPreferenceTreeClick(preference);
        }
        this.mAnomalySummaryPreferenceController.onPreferenceTreeClick(preference);
        return true;
    }

    public int getMetricsCategory() {
        return 53;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.power_usage_detail;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        ArrayList controllers = new ArrayList();
        Bundle bundle = getArguments();
        int uid = bundle.getInt(EXTRA_UID, 0);
        String packageName = bundle.getString(EXTRA_PACKAGE_NAME);
        this.mBackgroundActivityPreferenceController = new BackgroundActivityPreferenceController(context, this, uid, packageName);
        controllers.add(this.mBackgroundActivityPreferenceController);
        controllers.add(new BatteryOptimizationPreferenceController((SettingsActivity) getActivity(), this, packageName));
        AppButtonsPreferenceController appButtonsPreferenceController = r0;
        AppButtonsPreferenceController appButtonsPreferenceController2 = new AppButtonsPreferenceController((SettingsActivity) getActivity(), this, getLifecycle(), packageName, this.mState, this.mDpm, this.mUserManager, this.mPackageManager, 0, 1);
        this.mAppButtonsPreferenceController = appButtonsPreferenceController;
        controllers.add(this.mAppButtonsPreferenceController);
        return controllers;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mAppButtonsPreferenceController != null) {
            this.mAppButtonsPreferenceController.handleActivityResult(requestCode, resultCode, data);
        }
    }

    public void handleDialogClick(int id) {
        if (this.mAppButtonsPreferenceController != null) {
            this.mAppButtonsPreferenceController.handleDialogClick(id);
        }
    }

    public void onAnomalyHandled(Anomaly anomaly) {
        this.mAnomalySummaryPreferenceController.hideHighUsagePreference();
    }

    public Loader<List<Anomaly>> onCreateLoader(int id, Bundle args) {
        return new AnomalyLoader(getContext(), this.mPackageName);
    }

    public void onLoadFinished(Loader<List<Anomaly>> loader, List<Anomaly> data) {
        AnomalyUtils.getInstance(getContext()).logAnomalies(this.mMetricsFeatureProvider, data, 53);
        this.mAnomalySummaryPreferenceController.updateAnomalySummaryPreference(data);
    }

    public void onLoaderReset(Loader<List<Anomaly>> loader) {
    }

    public void onBatteryTipHandled(BatteryTip batteryTip) {
        this.mBackgroundActivityPreferenceController.updateSummary(findPreference(this.mBackgroundActivityPreferenceController.getPreferenceKey()));
    }
}
