package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.IconDrawableFactory;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.BatteryTipDialogFragment;
import com.android.settings.fuelgauge.batterytip.BatteryTipPreferenceController.BatteryTipListener;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import com.android.settings.fuelgauge.batterytip.tips.UnrestrictAppTip;
import com.android.settings.widget.AppCheckBoxPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.FooterPreferenceMixin;
import java.util.List;

public class RestrictedAppDetails extends DashboardFragment implements BatteryTipListener {
    @VisibleForTesting
    static final String EXTRA_APP_INFO_LIST = "app_info_list";
    private static final String KEY_PREF_RESTRICTED_APP_LIST = "restrict_app_list";
    public static final String TAG = "RestrictedAppDetails";
    @VisibleForTesting
    List<AppInfo> mAppInfos;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    private final FooterPreferenceMixin mFooterPreferenceMixin = new FooterPreferenceMixin(this, getLifecycle());
    @VisibleForTesting
    IconDrawableFactory mIconDrawableFactory;
    @VisibleForTesting
    PackageManager mPackageManager;
    @VisibleForTesting
    PreferenceGroup mRestrictedAppListGroup;

    public static void startRestrictedAppDetails(InstrumentedPreferenceFragment fragment, List<AppInfo> appInfos) {
        Bundle args = new Bundle();
        args.putParcelableList(EXTRA_APP_INFO_LIST, appInfos);
        new SubSettingLauncher(fragment.getContext()).setDestination(RestrictedAppDetails.class.getName()).setArguments(args).setTitle((int) R.string.restricted_app_title).setSourceMetricsCategory(fragment.getMetricsCategory()).launch();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.restricted_app_detail_footer);
        this.mRestrictedAppListGroup = (PreferenceGroup) findPreference(KEY_PREF_RESTRICTED_APP_LIST);
        this.mAppInfos = getArguments().getParcelableArrayList(EXTRA_APP_INFO_LIST);
        this.mPackageManager = context.getPackageManager();
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        refreshUi();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.restricted_apps_detail;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return null;
    }

    public int getMetricsCategory() {
        return 1285;
    }

    public int getHelpResource() {
        return R.string.help_uri_restricted_apps;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void refreshUi() {
        this.mRestrictedAppListGroup.removeAll();
        Context context = getPrefContext();
        int size = this.mAppInfos.size();
        for (int i = 0; i < size; i++) {
            CheckBoxPreference checkBoxPreference = new AppCheckBoxPreference(context);
            AppInfo appInfo = (AppInfo) this.mAppInfos.get(i);
            try {
                ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfoAsUser(appInfo.packageName, 0, UserHandle.getUserId(appInfo.uid));
                checkBoxPreference.setChecked(this.mBatteryUtils.isForceAppStandbyEnabled(appInfo.uid, appInfo.packageName));
                checkBoxPreference.setTitle(this.mPackageManager.getApplicationLabel(applicationInfo));
                checkBoxPreference.setIcon(Utils.getBadgedIcon(this.mIconDrawableFactory, this.mPackageManager, appInfo.packageName, UserHandle.getUserId(appInfo.uid)));
                checkBoxPreference.setKey(getKeyFromAppInfo(appInfo));
                checkBoxPreference.setOnPreferenceChangeListener(new -$$Lambda$RestrictedAppDetails$9OOxuAylZQQH-NDtRgh0ZoFLi_8(this, appInfo));
                this.mRestrictedAppListGroup.addPreference(checkBoxPreference);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static /* synthetic */ boolean lambda$refreshUi$0(RestrictedAppDetails restrictedAppDetails, AppInfo appInfo, Preference pref, Object value) {
        BatteryTipDialogFragment fragment = restrictedAppDetails.createDialogFragment(appInfo, ((Boolean) value).booleanValue());
        fragment.setTargetFragment(restrictedAppDetails, 0);
        fragment.show(restrictedAppDetails.getFragmentManager(), TAG);
        return false;
    }

    public void onBatteryTipHandled(BatteryTip batteryTip) {
        AppInfo appInfo;
        boolean isRestricted = batteryTip instanceof RestrictAppTip;
        if (isRestricted) {
            appInfo = (AppInfo) ((RestrictAppTip) batteryTip).getRestrictAppList().get(0);
        } else {
            appInfo = ((UnrestrictAppTip) batteryTip).getUnrestrictAppInfo();
        }
        CheckBoxPreference preference = (CheckBoxPreference) this.mRestrictedAppListGroup.findPreference(getKeyFromAppInfo(appInfo));
        if (preference != null) {
            preference.setChecked(isRestricted);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public BatteryTipDialogFragment createDialogFragment(AppInfo appInfo, boolean toRestrict) {
        BatteryTip restrictAppTip;
        if (toRestrict) {
            restrictAppTip = new RestrictAppTip(0, appInfo);
        } else {
            restrictAppTip = new UnrestrictAppTip(0, appInfo);
        }
        return BatteryTipDialogFragment.newInstance(restrictAppTip, getMetricsCategory());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getKeyFromAppInfo(AppInfo appInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appInfo.uid);
        stringBuilder.append(",");
        stringBuilder.append(appInfo.packageName);
        return stringBuilder.toString();
    }
}
