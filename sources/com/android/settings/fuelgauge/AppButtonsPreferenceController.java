package com.android.settings.fuelgauge;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.IWebViewUpdateService.Stub;
import com.android.settings.DeviceAdminAdd;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.applications.ApplicationFeatureProvider;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppButtonsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnDestroy, Callbacks {
    public static final String APP_CHG = "chg";
    private static final String KEY_ACTION_BUTTONS = "action_buttons";
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "AppButtonsPrefCtl";
    private final SettingsActivity mActivity;
    @VisibleForTesting
    AppEntry mAppEntry;
    private final ApplicationFeatureProvider mApplicationFeatureProvider;
    private EnforcedAdmin mAppsControlDisallowedAdmin;
    private boolean mAppsControlDisallowedBySystem;
    @VisibleForTesting
    ActionButtonPreference mButtonsPref;
    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean enabled = getResultCode() != 0;
            String str = AppButtonsPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Got broadcast response: Restart status for ");
            stringBuilder.append(AppButtonsPreferenceController.this.mAppEntry.info.packageName);
            stringBuilder.append(" ");
            stringBuilder.append(enabled);
            Log.d(str, stringBuilder.toString());
            AppButtonsPreferenceController.this.updateForceStopButtonInner(enabled);
        }
    };
    @VisibleForTesting
    boolean mDisableAfterUninstall = false;
    private final DevicePolicyManager mDpm;
    private boolean mFinishing = false;
    private final Fragment mFragment;
    @VisibleForTesting
    final HashSet<String> mHomePackages = new HashSet();
    private boolean mListeningToPackageRemove = false;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    @VisibleForTesting
    PackageInfo mPackageInfo;
    @VisibleForTesting
    String mPackageName;
    private final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (!AppButtonsPreferenceController.this.mFinishing && AppButtonsPreferenceController.this.mAppEntry.info.packageName.equals(packageName)) {
                AppButtonsPreferenceController.this.mActivity.finishAndRemoveTask();
            }
        }
    };
    private final PackageManager mPm;
    private final int mRequestRemoveDeviceAdmin;
    private final int mRequestUninstall;
    private Session mSession;
    @VisibleForTesting
    ApplicationsState mState;
    private boolean mUpdatedSysApp = false;
    private final int mUserId;
    private final UserManager mUserManager;

    private class DisableChangerRunnable implements Runnable {
        final String mPackageName;
        final PackageManager mPm;
        final int mState;

        public DisableChangerRunnable(PackageManager pm, String packageName, int state) {
            this.mPm = pm;
            this.mPackageName = packageName;
            this.mState = state;
        }

        public void run() {
            this.mPm.setApplicationEnabledSetting(this.mPackageName, this.mState, 0);
        }
    }

    private class ForceStopButtonListener implements OnClickListener {
        private ForceStopButtonListener() {
        }

        /* synthetic */ ForceStopButtonListener(AppButtonsPreferenceController x0, AnonymousClass1 x1) {
            this();
        }

        public void onClick(View v) {
            if (AppButtonsPreferenceController.this.mAppsControlDisallowedAdmin == null || AppButtonsPreferenceController.this.mAppsControlDisallowedBySystem) {
                AppButtonsPreferenceController.this.showDialogInner(2);
            } else {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(AppButtonsPreferenceController.this.mActivity, AppButtonsPreferenceController.this.mAppsControlDisallowedAdmin);
            }
        }
    }

    private class UninstallAndDisableButtonListener implements OnClickListener {
        private UninstallAndDisableButtonListener() {
        }

        /* synthetic */ UninstallAndDisableButtonListener(AppButtonsPreferenceController x0, AnonymousClass1 x1) {
            this();
        }

        public void onClick(View v) {
            String packageName = AppButtonsPreferenceController.this.mAppEntry.info.packageName;
            if (AppButtonsPreferenceController.this.mDpm.packageHasActiveAdmins(AppButtonsPreferenceController.this.mPackageInfo.packageName)) {
                AppButtonsPreferenceController.this.stopListeningToPackageRemove();
                Intent uninstallDaIntent = new Intent(AppButtonsPreferenceController.this.mActivity, DeviceAdminAdd.class);
                uninstallDaIntent.putExtra(DeviceAdminAdd.EXTRA_DEVICE_ADMIN_PACKAGE_NAME, packageName);
                AppButtonsPreferenceController.this.mMetricsFeatureProvider.action(AppButtonsPreferenceController.this.mActivity, 873, new Pair[0]);
                AppButtonsPreferenceController.this.mFragment.startActivityForResult(uninstallDaIntent, AppButtonsPreferenceController.this.mRequestRemoveDeviceAdmin);
                return;
            }
            EnforcedAdmin admin = RestrictedLockUtils.checkIfUninstallBlocked(AppButtonsPreferenceController.this.mActivity, packageName, AppButtonsPreferenceController.this.mUserId);
            boolean uninstallBlockedBySystem = AppButtonsPreferenceController.this.mAppsControlDisallowedBySystem || RestrictedLockUtils.hasBaseUserRestriction(AppButtonsPreferenceController.this.mActivity, packageName, AppButtonsPreferenceController.this.mUserId);
            if (admin != null && !uninstallBlockedBySystem) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(AppButtonsPreferenceController.this.mActivity, admin);
            } else if ((AppButtonsPreferenceController.this.mAppEntry.info.flags & 1) != 0) {
                if (!AppButtonsPreferenceController.this.mAppEntry.info.enabled || AppButtonsPreferenceController.this.isDisabledUntilUsed()) {
                    int i;
                    MetricsFeatureProvider access$500 = AppButtonsPreferenceController.this.mMetricsFeatureProvider;
                    Context access$400 = AppButtonsPreferenceController.this.mActivity;
                    if (AppButtonsPreferenceController.this.mAppEntry.info.enabled) {
                        i = 874;
                    } else {
                        i = 875;
                    }
                    access$500.action(access$400, i, new Pair[0]);
                    AsyncTask.execute(new DisableChangerRunnable(AppButtonsPreferenceController.this.mPm, AppButtonsPreferenceController.this.mAppEntry.info.packageName, 0));
                } else if (AppButtonsPreferenceController.this.mUpdatedSysApp && AppButtonsPreferenceController.this.isSingleUser()) {
                    AppButtonsPreferenceController.this.showDialogInner(1);
                } else {
                    AppButtonsPreferenceController.this.showDialogInner(0);
                }
            } else if ((AppButtonsPreferenceController.this.mAppEntry.info.flags & 8388608) == 0) {
                AppButtonsPreferenceController.this.uninstallPkg(packageName, true, false);
            } else {
                AppButtonsPreferenceController.this.uninstallPkg(packageName, false, false);
            }
        }
    }

    public AppButtonsPreferenceController(SettingsActivity activity, Fragment fragment, Lifecycle lifecycle, String packageName, ApplicationsState state, DevicePolicyManager dpm, UserManager userManager, PackageManager packageManager, int requestUninstall, int requestRemoveDeviceAdmin) {
        super(activity);
        if (fragment instanceof AppButtonsDialogListener) {
            FeatureFactory factory = FeatureFactory.getFactory(activity);
            this.mMetricsFeatureProvider = factory.getMetricsFeatureProvider();
            this.mApplicationFeatureProvider = factory.getApplicationFeatureProvider(activity);
            this.mState = state;
            this.mDpm = dpm;
            this.mUserManager = userManager;
            this.mPm = packageManager;
            this.mPackageName = packageName;
            this.mActivity = activity;
            this.mFragment = fragment;
            this.mUserId = UserHandle.myUserId();
            this.mRequestUninstall = requestUninstall;
            this.mRequestRemoveDeviceAdmin = requestRemoveDeviceAdmin;
            if (packageName != null) {
                this.mAppEntry = this.mState.getEntry(packageName, this.mUserId);
                this.mSession = this.mState.newSession(this, lifecycle);
                lifecycle.addObserver(this);
                return;
            }
            this.mFinishing = true;
            return;
        }
        throw new IllegalArgumentException("Fragment should implement AppButtonsDialogListener");
    }

    public boolean isAvailable() {
        return (this.mAppEntry == null || AppUtils.isInstant(this.mAppEntry.info)) ? false : true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mButtonsPref = ((ActionButtonPreference) screen.findPreference(KEY_ACTION_BUTTONS)).setButton1Text(R.string.uninstall_text).setButton2Text(R.string.force_stop).setButton1OnClickListener(new UninstallAndDisableButtonListener(this, null)).setButton2OnClickListener(new ForceStopButtonListener(this, null)).setButton1Positive(false).setButton2Positive(false).setButton2Enabled(false);
        }
    }

    public String getPreferenceKey() {
        return KEY_ACTION_BUTTONS;
    }

    public void onResume() {
        if (isAvailable() && !this.mFinishing) {
            this.mAppsControlDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mActivity, "no_control_apps", this.mUserId);
            this.mAppsControlDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mActivity, "no_control_apps", this.mUserId);
            if (!refreshUi()) {
                setIntentAndFinish(true);
            }
        }
    }

    public void onDestroy() {
        stopListeningToPackageRemove();
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.mRequestUninstall) {
            if (this.mDisableAfterUninstall) {
                this.mDisableAfterUninstall = false;
                AsyncTask.execute(new DisableChangerRunnable(this.mPm, this.mAppEntry.info.packageName, 3));
            }
            refreshAndFinishIfPossible();
        } else if (requestCode == this.mRequestRemoveDeviceAdmin) {
            refreshAndFinishIfPossible();
        }
    }

    public void handleDialogClick(int id) {
        switch (id) {
            case 0:
                this.mMetricsFeatureProvider.action(this.mActivity, 874, new Pair[0]);
                AsyncTask.execute(new DisableChangerRunnable(this.mPm, this.mAppEntry.info.packageName, 3));
                return;
            case 1:
                this.mMetricsFeatureProvider.action(this.mActivity, 874, new Pair[0]);
                uninstallPkg(this.mAppEntry.info.packageName, false, true);
                return;
            case 2:
                forceStopPackage(this.mAppEntry.info.packageName);
                return;
            default:
                return;
        }
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
        if (isAvailable()) {
            refreshUi();
        }
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void retrieveAppEntry() {
        if (this.mPackageName != null) {
            this.mAppEntry = this.mState.getEntry(this.mPackageName, this.mUserId);
        }
        if (this.mAppEntry != null) {
            try {
                this.mPackageInfo = this.mPm.getPackageInfo(this.mAppEntry.info.packageName, 4198976);
                this.mPackageName = this.mAppEntry.info.packageName;
                return;
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Exception when retrieving package:");
                stringBuilder.append(this.mAppEntry.info.packageName);
                Log.e(str, stringBuilder.toString(), e);
                this.mPackageInfo = null;
                return;
            }
        }
        this.mPackageInfo = null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateUninstallButton() {
        boolean z = true;
        boolean isBundled = (this.mAppEntry.info.flags & 1) != 0;
        boolean enabled = true;
        if (isBundled) {
            enabled = handleDisableable();
        } else if ((this.mPackageInfo.applicationInfo.flags & 8388608) == 0 && this.mUserManager.getUsers().size() >= 2) {
            enabled = false;
        }
        if (isBundled && this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
            enabled = false;
        }
        if (Utils.isProfileOrDeviceOwner(this.mUserManager, this.mDpm, this.mPackageInfo.packageName)) {
            enabled = false;
        }
        if (com.android.settingslib.Utils.isDeviceProvisioningPackage(this.mContext.getResources(), this.mAppEntry.info.packageName)) {
            enabled = false;
        }
        if (this.mDpm.isUninstallInQueue(this.mPackageName)) {
            enabled = false;
        }
        if (enabled && this.mHomePackages.contains(this.mPackageInfo.packageName)) {
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
                    enabled = 1 ^ this.mPackageInfo.packageName.equals(currentDefaultHome.getPackageName());
                }
            }
        }
        if (this.mAppsControlDisallowedBySystem) {
            enabled = false;
        }
        if (isFallbackPackage(this.mAppEntry.info.packageName)) {
            enabled = false;
        }
        this.mButtonsPref.setButton1Enabled(enabled);
    }

    private void setIntentAndFinish(boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra("chg", appChanged);
        this.mActivity.finishPreferencePanel(-1, intent);
        this.mFinishing = true;
    }

    private void refreshAndFinishIfPossible() {
        if (refreshUi()) {
            startListeningToPackageRemove();
        } else {
            setIntentAndFinish(true);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isFallbackPackage(String packageName) {
        try {
            if (Stub.asInterface(ServiceManager.getService("webviewupdate")).isFallbackPackage(packageName)) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateForceStopButton() {
        if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
            Log.w(TAG, "User can't force stop device admin");
            updateForceStopButtonInner(false);
        } else if ((this.mAppEntry.info.flags & 2097152) == 0) {
            Log.w(TAG, "App is not explicitly stopped");
            updateForceStopButtonInner(true);
        } else {
            Intent intent = new Intent("android.intent.action.QUERY_PACKAGE_RESTART", Uri.fromParts("package", this.mAppEntry.info.packageName, null));
            intent.putExtra("android.intent.extra.PACKAGES", new String[]{this.mAppEntry.info.packageName});
            intent.putExtra("android.intent.extra.UID", this.mAppEntry.info.uid);
            intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(this.mAppEntry.info.uid));
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Sending broadcast to query restart status for ");
            stringBuilder.append(this.mAppEntry.info.packageName);
            Log.d(str, stringBuilder.toString());
            this.mActivity.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mCheckKillProcessesReceiver, null, 0, null, null);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateForceStopButtonInner(boolean enabled) {
        if (this.mAppsControlDisallowedBySystem) {
            this.mButtonsPref.setButton2Enabled(false);
        } else {
            this.mButtonsPref.setButton2Enabled(enabled);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void uninstallPkg(String packageName, boolean allUsers, boolean andDisable) {
        stopListeningToPackageRemove();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package:");
        stringBuilder.append(packageName);
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse(stringBuilder.toString()));
        if (OPUtils.hasMultiApp(this.mContext, packageName)) {
            uninstallIntent.setAction(OPConstants.ONEPLUS_INTENT_ACTION_DELETE);
        }
        uninstallIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", allUsers);
        this.mMetricsFeatureProvider.action(this.mActivity, 872, new Pair[0]);
        this.mFragment.startActivityForResult(uninstallIntent, this.mRequestUninstall);
        this.mDisableAfterUninstall = andDisable;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void forceStopPackage(String pkgName) {
        FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider().action(this.mContext, 807, pkgName, new Pair[0]);
        ActivityManager am = (ActivityManager) this.mActivity.getSystemService("activity");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Stopping package ");
        stringBuilder.append(pkgName);
        Log.d(str, stringBuilder.toString());
        am.forceStopPackage(pkgName);
        int userId = UserHandle.getUserId(this.mAppEntry.info.uid);
        this.mState.invalidatePackage(pkgName, userId);
        AppEntry newEnt = this.mState.getEntry(pkgName, userId);
        if (newEnt != null) {
            this.mAppEntry = newEnt;
        }
        updateForceStopButton();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean handleDisableable() {
        if (this.mHomePackages.contains(this.mAppEntry.info.packageName) || isSystemPackage(this.mActivity.getResources(), this.mPm, this.mPackageInfo)) {
            this.mButtonsPref.setButton1Text(R.string.disable_text).setButton1Positive(false);
            return false;
        } else if (!this.mAppEntry.info.enabled || isDisabledUntilUsed()) {
            this.mButtonsPref.setButton1Text(R.string.enable_text).setButton1Positive(true);
            return true;
        } else {
            this.mButtonsPref.setButton1Text(R.string.disable_text).setButton1Positive(false);
            return this.mApplicationFeatureProvider.getKeepEnabledPackages().contains(this.mAppEntry.info.packageName) ^ 1;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isSystemPackage(Resources resources, PackageManager pm, PackageInfo packageInfo) {
        return com.android.settingslib.Utils.isSystemPackage(resources, pm, packageInfo);
    }

    private boolean isDisabledUntilUsed() {
        return this.mAppEntry.info.enabledSetting == 4;
    }

    private void showDialogInner(int id) {
        ButtonActionDialogFragment newFragment = ButtonActionDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this.mFragment, 0);
        FragmentManager fragmentManager = this.mActivity.getFragmentManager();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dialog ");
        stringBuilder.append(id);
        newFragment.show(fragmentManager, stringBuilder.toString());
    }

    private boolean isSingleUser() {
        int userCount = this.mUserManager.getUserCount();
        if (userCount == 1) {
            return true;
        }
        UserManager userManager = this.mUserManager;
        if (UserManager.isSplitSystemUser() && userCount == 2) {
            return true;
        }
        return false;
    }

    private boolean signaturesMatch(String pkg1, String pkg2) {
        if (!(pkg1 == null || pkg2 == null)) {
            try {
                if (this.mPm.checkSignatures(pkg1, pkg2) >= 0) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean refreshUi() {
        if (this.mPackageName == null) {
            return false;
        }
        retrieveAppEntry();
        if (this.mAppEntry == null || this.mPackageInfo == null) {
            return false;
        }
        List<ResolveInfo> homeActivities = new ArrayList();
        this.mPm.getHomeActivities(homeActivities);
        this.mHomePackages.clear();
        int size = homeActivities.size();
        for (int i = 0; i < size; i++) {
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
        updateUninstallButton();
        updateForceStopButton();
        return true;
    }

    private void startListeningToPackageRemove() {
        if (!this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = true;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            this.mActivity.registerReceiver(this.mPackageRemovedReceiver, filter);
        }
    }

    private void stopListeningToPackageRemove() {
        if (this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = false;
            this.mActivity.unregisterReceiver(this.mPackageRemovedReceiver);
        }
    }
}
