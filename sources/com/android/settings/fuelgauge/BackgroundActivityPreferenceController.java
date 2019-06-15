package com.android.settings.fuelgauge;

import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.AppInfo.Builder;
import com.android.settings.fuelgauge.batterytip.BatteryTipDialogFragment;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import com.android.settings.fuelgauge.batterytip.tips.UnrestrictAppTip;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;

public class BackgroundActivityPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    @VisibleForTesting
    static final String KEY_BACKGROUND_ACTIVITY = "background_activity";
    private static final String TAG = "BgActivityPrefContr";
    private final AppOpsManager mAppOpsManager;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    DevicePolicyManager mDpm;
    private InstrumentedPreferenceFragment mFragment;
    private PowerWhitelistBackend mPowerWhitelistBackend;
    private String mTargetPackage;
    private final int mUid;
    private final UserManager mUserManager;

    public BackgroundActivityPreferenceController(Context context, InstrumentedPreferenceFragment fragment, int uid, String packageName) {
        this(context, fragment, uid, packageName, PowerWhitelistBackend.getInstance(context));
    }

    @VisibleForTesting
    BackgroundActivityPreferenceController(Context context, InstrumentedPreferenceFragment fragment, int uid, String packageName, PowerWhitelistBackend backend) {
        super(context);
        this.mPowerWhitelistBackend = backend;
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mDpm = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mUid = uid;
        this.mFragment = fragment;
        this.mTargetPackage = packageName;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public void updateState(Preference preference) {
        int mode = this.mAppOpsManager.checkOpNoThrow(78, this.mUid, this.mTargetPackage);
        if (this.mPowerWhitelistBackend.isWhitelisted(this.mTargetPackage) || mode == 2 || Utils.isProfileOrDeviceOwner(this.mUserManager, this.mDpm, this.mTargetPackage)) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
        updateSummary(preference);
    }

    public boolean isAvailable() {
        return this.mTargetPackage != null;
    }

    public String getPreferenceKey() {
        return KEY_BACKGROUND_ACTIVITY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_BACKGROUND_ACTIVITY.equals(preference.getKey())) {
            boolean restricted = true;
            if (this.mAppOpsManager.checkOpNoThrow(78, this.mUid, this.mTargetPackage) != 1) {
                restricted = false;
            }
            showDialog(restricted);
        }
        return false;
    }

    public void updateSummary(Preference preference) {
        if (this.mPowerWhitelistBackend.isWhitelisted(this.mTargetPackage)) {
            preference.setSummary((int) R.string.background_activity_summary_whitelisted);
            return;
        }
        int mode = this.mAppOpsManager.checkOpNoThrow(78, this.mUid, this.mTargetPackage);
        if (mode == 2) {
            preference.setSummary((int) R.string.background_activity_summary_disabled);
        } else {
            int i;
            boolean restricted = true;
            if (mode != 1) {
                restricted = false;
            }
            if (restricted) {
                i = R.string.restricted_true_label;
            } else {
                i = R.string.restricted_false_label;
            }
            preference.setSummary(i);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showDialog(boolean restricted) {
        BatteryTip tip;
        AppInfo appInfo = new Builder().setUid(this.mUid).setPackageName(this.mTargetPackage).build();
        if (restricted) {
            tip = new UnrestrictAppTip(0, appInfo);
        } else {
            tip = new RestrictAppTip(0, appInfo);
        }
        BatteryTipDialogFragment dialogFragment = BatteryTipDialogFragment.newInstance(tip, this.mFragment.getMetricsCategory());
        dialogFragment.setTargetFragment(this.mFragment, 0);
        dialogFragment.show(this.mFragment.getFragmentManager(), TAG);
    }
}
