package com.android.settings.applications.appinfo;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.IWebViewUpdateService.Stub;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment.Callback;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppActionButtonPreferenceController extends BasePreferenceController implements Callback {
    private static final String KEY_ACTION_BUTTONS = "action_buttons";
    private static final String KEY_GOOGLE__INPUTMETHOD = "com.google.android.inputmethod.latin";
    private static final String KEY_LATIN_INPUTMETHOD = "com.android.inputmethod.latin";
    private static final String TAG = "AppActionButtonControl";
    @VisibleForTesting
    ActionButtonPreference mActionButtons;
    private final ApplicationFeatureProvider mApplicationFeatureProvider;
    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean enabled = getResultCode() != 0;
            String str = AppActionButtonPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Got broadcast response: Restart status for ");
            stringBuilder.append(AppActionButtonPreferenceController.this.mParent.getAppEntry().info.packageName);
            stringBuilder.append(" ");
            stringBuilder.append(enabled);
            Log.d(str, stringBuilder.toString());
            AppActionButtonPreferenceController.this.updateForceStopButton(enabled);
        }
    };
    private DevicePolicyManager mDpm;
    private final HashSet<String> mHomePackages = new HashSet();
    private final String mPackageName;
    private final AppInfoDashboardFragment mParent;
    private PackageManager mPm;
    private int mUserId;
    private UserManager mUserManager;

    public AppActionButtonPreferenceController(Context context, AppInfoDashboardFragment parent, String packageName) {
        super(context, KEY_ACTION_BUTTONS);
        this.mParent = parent;
        this.mPackageName = packageName;
        this.mUserId = UserHandle.myUserId();
        this.mApplicationFeatureProvider = FeatureFactory.getFactory(context).getApplicationFeatureProvider(context);
    }

    public int getAvailabilityStatus() {
        return AppUtils.isInstant(this.mParent.getPackageInfo().applicationInfo) ? 3 : 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mActionButtons = ((ActionButtonPreference) screen.findPreference(KEY_ACTION_BUTTONS)).setButton2Text(R.string.force_stop).setButton2Positive(false).setButton2Enabled(false);
    }

    public void refreshUi() {
        if (this.mPm == null) {
            this.mPm = this.mContext.getPackageManager();
        }
        if (this.mDpm == null) {
            this.mDpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        }
        if (this.mUserManager == null) {
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        }
        AppEntry appEntry = this.mParent.getAppEntry();
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        List<ResolveInfo> homeActivities = new ArrayList();
        this.mPm.getHomeActivities(homeActivities);
        this.mHomePackages.clear();
        for (int i = 0; i < homeActivities.size(); i++) {
            ResolveInfo ri = (ResolveInfo) homeActivities.get(i);
            String activityPkg = ri.activityInfo.packageName;
            this.mHomePackages.add(activityPkg);
            Bundle metadata = ri.activityInfo.metaData;
            if (metadata != null) {
                String metaPkg = metadata.getString("android.app.home.alternate");
                if (signaturesMatch(metaPkg, activityPkg)) {
                    this.mHomePackages.add(metaPkg);
                }
            }
        }
        checkForceStop(appEntry, packageInfo);
        initUninstallButtons(appEntry, packageInfo);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initUninstallButtons(AppEntry appEntry, PackageInfo packageInfo) {
        boolean enabled;
        boolean z = true;
        boolean isBundled = (appEntry.info.flags & 1) != 0;
        if (isBundled) {
            enabled = handleDisableable(appEntry, packageInfo);
        } else {
            enabled = initUninstallButtonForUserApp();
        }
        if (isBundled && this.mDpm.packageHasActiveAdmins(packageInfo.packageName)) {
            enabled = false;
        }
        if (Utils.isProfileOrDeviceOwner(this.mUserManager, this.mDpm, packageInfo.packageName)) {
            enabled = false;
        }
        if (com.android.settingslib.Utils.isDeviceProvisioningPackage(this.mContext.getResources(), appEntry.info.packageName)) {
            enabled = false;
        }
        if (this.mDpm.isUninstallInQueue(this.mPackageName)) {
            enabled = false;
        }
        if (enabled && this.mHomePackages.contains(packageInfo.packageName)) {
            if (isBundled) {
                enabled = false;
            } else {
                ComponentName currentDefaultHome = this.mPm.getHomeActivities(new ArrayList());
                if (currentDefaultHome == null) {
                    if (this.mHomePackages.size() <= 1) {
                        z = false;
                    }
                    enabled = z;
                } else {
                    enabled = 1 ^ packageInfo.packageName.equals(currentDefaultHome.getPackageName());
                }
            }
        }
        if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_control_apps", this.mUserId)) {
            enabled = false;
        }
        try {
            if (Stub.asInterface(ServiceManager.getService("webviewupdate")).isFallbackPackage(appEntry.info.packageName)) {
                enabled = false;
            }
            if (TextUtils.equals(this.mContext.getString(R.string.oneplus_cannot_disable_package_1), appEntry.info.packageName) || TextUtils.equals(this.mContext.getString(R.string.oneplus_cannot_disable_package_2), appEntry.info.packageName)) {
                enabled = false;
            }
            this.mActionButtons.setButton1Enabled(enabled);
            if (enabled) {
                this.mActionButtons.setButton1OnClickListener(new -$$Lambda$AppActionButtonPreferenceController$Ww2IUjWxdICZ6sY_1SuD__XEpOY(this));
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean initUninstallButtonForUserApp() {
        boolean enabled = true;
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if ((packageInfo.applicationInfo.flags & 8388608) == 0 && this.mUserManager.getUsers().size() >= 2) {
            enabled = false;
        } else if (AppUtils.isInstant(packageInfo.applicationInfo)) {
            enabled = false;
            this.mActionButtons.setButton1Visible(false);
        }
        this.mActionButtons.setButton1Text(R.string.uninstall_text).setButton1Positive(false);
        return enabled;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean handleDisableable(AppEntry appEntry, PackageInfo packageInfo) {
        if (this.mHomePackages.contains(appEntry.info.packageName) || KEY_LATIN_INPUTMETHOD.contains(appEntry.info.packageName) || KEY_GOOGLE__INPUTMETHOD.contains(appEntry.info.packageName) || com.android.settingslib.Utils.isSystemPackage(this.mContext.getResources(), this.mPm, packageInfo)) {
            this.mActionButtons.setButton1Text(R.string.disable_text).setButton1Positive(false);
            return false;
        } else if (!appEntry.info.enabled || appEntry.info.enabledSetting == 4) {
            this.mActionButtons.setButton1Text(R.string.enable_text).setButton1Positive(true);
            return true;
        } else {
            this.mActionButtons.setButton1Text(R.string.disable_text).setButton1Positive(false);
            return this.mApplicationFeatureProvider.getKeepEnabledPackages().contains(appEntry.info.packageName) ^ 1;
        }
    }

    private void updateForceStopButton(boolean enabled) {
        boolean disallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_control_apps", this.mUserId);
        this.mActionButtons.setButton2Enabled(disallowedBySystem ? false : enabled).setButton2OnClickListener(disallowedBySystem ? null : new -$$Lambda$AppActionButtonPreferenceController$oIXjjHquqzr1XuPAGEk55khGTJ0(this));
    }

    /* Access modifiers changed, original: 0000 */
    public void checkForceStop(AppEntry appEntry, PackageInfo packageInfo) {
        AppEntry appEntry2 = appEntry;
        PackageInfo packageInfo2 = packageInfo;
        if (this.mDpm.packageHasActiveAdmins(packageInfo2.packageName)) {
            Log.w(TAG, "User can't force stop device admin");
            updateForceStopButton(false);
        } else if (this.mPm.isPackageStateProtected(packageInfo2.packageName, UserHandle.getUserId(appEntry2.info.uid))) {
            Log.w(TAG, "User can't force stop protected packages");
            updateForceStopButton(false);
        } else if (AppUtils.isInstant(packageInfo2.applicationInfo)) {
            updateForceStopButton(false);
            this.mActionButtons.setButton2Visible(false);
        } else if ((appEntry2.info.flags & 2097152) == 0) {
            Log.w(TAG, "App is not explicitly stopped");
            updateForceStopButton(true);
        } else {
            Intent intent = new Intent("android.intent.action.QUERY_PACKAGE_RESTART", Uri.fromParts("package", appEntry2.info.packageName, null));
            intent.putExtra("android.intent.extra.PACKAGES", new String[]{appEntry2.info.packageName});
            intent.putExtra("android.intent.extra.UID", appEntry2.info.uid);
            intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(appEntry2.info.uid));
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Sending broadcast to query restart status for ");
            stringBuilder.append(appEntry2.info.packageName);
            Log.d(str, stringBuilder.toString());
            if (UserHandle.getUserId(appEntry2.info.uid) == 999) {
                this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(999), null, this.mCheckKillProcessesReceiver, null, 0, null, null);
                return;
            }
            this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mCheckKillProcessesReceiver, null, 0, null, null);
        }
    }

    private boolean signaturesMatch(String pkg1, String pkg2) {
        boolean z = false;
        if (!(pkg1 == null || pkg2 == null)) {
            try {
                if (this.mPm.checkSignatures(pkg1, pkg2) >= 0) {
                    z = true;
                }
                return z;
            } catch (Exception e) {
            }
        }
        return false;
    }
}
