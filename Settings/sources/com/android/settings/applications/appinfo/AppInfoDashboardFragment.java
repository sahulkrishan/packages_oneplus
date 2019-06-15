package com.android.settings.applications.appinfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.DeviceAdminAdd;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppInfoDashboardFragment extends DashboardFragment implements Callbacks {
    public static final String ARG_PACKAGE_NAME = "package";
    public static final String ARG_PACKAGE_UID = "uid";
    private static final int DLG_BASE = 0;
    static final int DLG_CLEAR_INSTANT_APP = 4;
    private static final int DLG_DISABLE = 2;
    private static final int DLG_FORCE_STOP = 1;
    private static final int DLG_SPECIAL_DISABLE = 3;
    static final int INSTALL_INSTANT_APP_MENU = 3;
    static final int LOADER_BATTERY = 4;
    static final int LOADER_CHART_DATA = 2;
    static final int LOADER_STORAGE = 3;
    private static final int REQUEST_REMOVE_DEVICE_ADMIN = 1;
    @VisibleForTesting
    static final int REQUEST_UNINSTALL = 0;
    static final int SUB_INFO_FRAGMENT = 1;
    private static final String TAG = "AppInfoDashboard";
    @VisibleForTesting
    static final int UNINSTALL_ALL_USERS_MENU = 1;
    @VisibleForTesting
    static final int UNINSTALL_UPDATES = 2;
    private static final boolean localLOGV = false;
    private AppActionButtonPreferenceController mAppActionButtonPreferenceController;
    private AppEntry mAppEntry;
    private EnforcedAdmin mAppsControlDisallowedAdmin;
    private boolean mAppsControlDisallowedBySystem;
    private List<Callback> mCallbacks = new ArrayList();
    private boolean mDisableAfterUninstall;
    private DevicePolicyManager mDpm;
    private boolean mFinishing;
    private boolean mInitialized;
    private InstantAppButtonsPreferenceController mInstantAppButtonPreferenceController;
    private boolean mListeningToPackageRemove;
    private PackageInfo mPackageInfo;
    private String mPackageName;
    @VisibleForTesting
    final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (!AppInfoDashboardFragment.this.mFinishing) {
                if (AppInfoDashboardFragment.this.mAppEntry == null || AppInfoDashboardFragment.this.mAppEntry.info == null || TextUtils.equals(AppInfoDashboardFragment.this.mAppEntry.info.packageName, packageName)) {
                    AppInfoDashboardFragment.this.onPackageRemoved();
                }
            }
        }
    };
    private PackageManager mPm;
    private Session mSession;
    private boolean mShowUninstalled;
    private ApplicationsState mState;
    private boolean mUpdatedSysApp = false;
    private int mUserId;
    private UserManager mUserManager;

    public interface Callback {
        void refreshUi();
    }

    private static class DisableChanger extends AsyncTask<Object, Object, Object> {
        final WeakReference<AppInfoDashboardFragment> mActivity;
        final ApplicationInfo mInfo;
        final PackageManager mPm;
        final int mState;

        DisableChanger(AppInfoDashboardFragment activity, ApplicationInfo info, int state) {
            this.mPm = activity.mPm;
            this.mActivity = new WeakReference(activity);
            this.mInfo = info;
            this.mState = state;
        }

        /* Access modifiers changed, original: protected|varargs */
        public Object doInBackground(Object... params) {
            this.mPm.setApplicationEnabledSetting(this.mInfo.packageName, this.mState, 0);
            return null;
        }
    }

    public static class MyAlertDialogFragment extends InstrumentedDialogFragment {
        private static final String ARG_ID = "id";

        public int getMetricsCategory() {
            return 558;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            Dialog dialog = ((AppInfoDashboardFragment) getTargetFragment()).createDialog(id, getArguments().getInt("moveError"));
            if (dialog != null) {
                return dialog;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unknown id ");
            stringBuilder.append(id);
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        public static MyAlertDialogFragment newInstance(int id, int errorCode) {
            MyAlertDialogFragment dialogFragment = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putInt("moveError", errorCode);
            dialogFragment.setArguments(args);
            return dialogFragment;
        }
    }

    private boolean isDisabledUntilUsed() {
        return this.mAppEntry.info.enabledSetting == 4;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        String packageName = getPackageName();
        ((TimeSpentInAppPreferenceController) use(TimeSpentInAppPreferenceController.class)).setPackageName(packageName);
        ((AppDataUsagePreferenceController) use(AppDataUsagePreferenceController.class)).setParentFragment(this);
        AppInstallerInfoPreferenceController installer = (AppInstallerInfoPreferenceController) use(AppInstallerInfoPreferenceController.class);
        installer.setPackageName(packageName);
        installer.setParentFragment(this);
        ((AppInstallerPreferenceCategoryController) use(AppInstallerPreferenceCategoryController.class)).setChildren(Arrays.asList(new AbstractPreferenceController[]{installer}));
        ((AppNotificationPreferenceController) use(AppNotificationPreferenceController.class)).setParentFragment(this);
        ((AppOpenByDefaultPreferenceController) use(AppOpenByDefaultPreferenceController.class)).setParentFragment(this);
        ((AppPermissionPreferenceController) use(AppPermissionPreferenceController.class)).setParentFragment(this);
        ((AppPermissionPreferenceController) use(AppPermissionPreferenceController.class)).setPackageName(packageName);
        ((AppSettingPreferenceController) use(AppSettingPreferenceController.class)).setPackageName(packageName).setParentFragment(this);
        ((AppStoragePreferenceController) use(AppStoragePreferenceController.class)).setParentFragment(this);
        ((AppVersionPreferenceController) use(AppVersionPreferenceController.class)).setParentFragment(this);
        ((InstantAppDomainsPreferenceController) use(InstantAppDomainsPreferenceController.class)).setParentFragment(this);
        ((WriteSystemSettingsPreferenceController) use(WriteSystemSettingsPreferenceController.class)).setParentFragment(this);
        ((DrawOverlayDetailPreferenceController) use(DrawOverlayDetailPreferenceController.class)).setParentFragment(this);
        PictureInPictureDetailPreferenceController pip = (PictureInPictureDetailPreferenceController) use(PictureInPictureDetailPreferenceController.class);
        pip.setPackageName(packageName);
        pip.setParentFragment(this);
        ExternalSourceDetailPreferenceController externalSource = (ExternalSourceDetailPreferenceController) use(ExternalSourceDetailPreferenceController.class);
        externalSource.setPackageName(packageName);
        externalSource.setParentFragment(this);
        ((AdvancedAppInfoPreferenceCategoryController) use(AdvancedAppInfoPreferenceCategoryController.class)).setChildren(Arrays.asList(new AbstractPreferenceController[]{writeSystemSettings, drawOverlay, pip, externalSource}));
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mFinishing = false;
        Activity activity = getActivity();
        this.mDpm = (DevicePolicyManager) activity.getSystemService("device_policy");
        this.mUserManager = (UserManager) activity.getSystemService("user");
        this.mPm = activity.getPackageManager();
        if (ensurePackageInfoAvailable(activity)) {
            startListeningToPackageRemove();
            setHasOptionsMenu(true);
        }
    }

    public void onDestroy() {
        stopListeningToPackageRemove();
        super.onDestroy();
    }

    public int getMetricsCategory() {
        return 20;
    }

    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        this.mAppsControlDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(activity, "no_control_apps", this.mUserId);
        this.mAppsControlDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(activity, "no_control_apps", this.mUserId);
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.app_info_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        retrieveAppEntry();
        if (this.mPackageInfo == null) {
            return null;
        }
        String packageName = getPackageName();
        List<AbstractPreferenceController> controllers = new ArrayList();
        Lifecycle lifecycle = getLifecycle();
        controllers.add(new AppHeaderViewPreferenceController(context, this, packageName, lifecycle));
        this.mAppActionButtonPreferenceController = new AppActionButtonPreferenceController(context, this, packageName);
        controllers.add(this.mAppActionButtonPreferenceController);
        for (AbstractPreferenceController controller : controllers) {
            this.mCallbacks.add((Callback) controller);
        }
        this.mInstantAppButtonPreferenceController = new InstantAppButtonsPreferenceController(context, this, packageName, lifecycle);
        controllers.add(this.mInstantAppButtonPreferenceController);
        controllers.add(new AppBatteryPreferenceController(context, this, packageName, lifecycle));
        controllers.add(new AppMemoryPreferenceController(context, this, lifecycle));
        controllers.add(new DefaultHomeShortcutPreferenceController(context, packageName));
        controllers.add(new DefaultBrowserShortcutPreferenceController(context, packageName));
        controllers.add(new DefaultPhoneShortcutPreferenceController(context, packageName));
        controllers.add(new DefaultEmergencyShortcutPreferenceController(context, packageName));
        controllers.add(new DefaultSmsShortcutPreferenceController(context, packageName));
        return controllers;
    }

    /* Access modifiers changed, original: 0000 */
    public void addToCallbackList(Callback callback) {
        if (callback != null) {
            this.mCallbacks.add(callback);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public AppEntry getAppEntry() {
        return this.mAppEntry;
    }

    /* Access modifiers changed, original: 0000 */
    public void setAppEntry(AppEntry appEntry) {
        this.mAppEntry = appEntry;
    }

    /* Access modifiers changed, original: 0000 */
    public PackageInfo getPackageInfo() {
        return this.mPackageInfo;
    }

    public void onPackageSizeChanged(String packageName) {
        if (TextUtils.equals(packageName, this.mPackageName)) {
            refreshUi();
        } else {
            Log.d(TAG, "Package change irrelevant, skipping");
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean ensurePackageInfoAvailable(Activity activity) {
        if (this.mPackageInfo != null) {
            return true;
        }
        this.mFinishing = true;
        Log.w(TAG, "Package info not available. Is this package already uninstalled?");
        activity.finishAndRemoveTask();
        return false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, 2, 0, R.string.app_factory_reset).setShowAsAction(0);
        menu.add(0, 1, 1, R.string.uninstall_all_users_text).setShowAsAction(0);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (!this.mFinishing) {
            super.onPrepareOptionsMenu(menu);
            boolean z = true;
            menu.findItem(1).setVisible(shouldShowUninstallForAll(this.mAppEntry));
            this.mUpdatedSysApp = (this.mAppEntry.info.flags & 128) != 0;
            MenuItem uninstallUpdatesItem = menu.findItem(2);
            boolean uninstallUpdateDisabled = getContext().getResources().getBoolean(R.bool.config_disable_uninstall_update);
            if (!this.mUpdatedSysApp || this.mAppsControlDisallowedBySystem || uninstallUpdateDisabled) {
                z = false;
            }
            uninstallUpdatesItem.setVisible(z);
            if (uninstallUpdatesItem.isVisible()) {
                RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getActivity(), uninstallUpdatesItem, this.mAppsControlDisallowedAdmin);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                uninstallPkg(this.mAppEntry.info.packageName, true, false);
                return true;
            case 2:
                uninstallPkg(this.mAppEntry.info.packageName, false, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                getActivity().invalidateOptionsMenu();
                if (this.mDisableAfterUninstall) {
                    this.mDisableAfterUninstall = false;
                    new DisableChanger(this, this.mAppEntry.info, 3).execute(new Object[]{null});
                }
                if (refreshUi()) {
                    startListeningToPackageRemove();
                    return;
                } else {
                    onPackageRemoved();
                    return;
                }
            case 1:
                if (refreshUi()) {
                    startListeningToPackageRemove();
                    return;
                } else {
                    setIntentAndFinish(true, true);
                    return;
                }
            default:
                return;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean shouldShowUninstallForAll(AppEntry appEntry) {
        if (this.mUpdatedSysApp) {
            return false;
        }
        if (appEntry == null) {
            return false;
        }
        if ((appEntry.info.flags & 1) != 0) {
            return false;
        }
        if (this.mPackageInfo == null || this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
            return false;
        }
        if (UserHandle.myUserId() != 0) {
            return false;
        }
        if (this.mUserManager.getUsers().size() < 2) {
            return false;
        }
        if (getNumberOfUserWithPackageInstalled(this.mPackageName) < 2 && (appEntry.info.flags & 8388608) != 0) {
            return false;
        }
        if (AppUtils.isInstant(appEntry.info)) {
            return false;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean refreshUi() {
        retrieveAppEntry();
        boolean z = false;
        if (this.mAppEntry == null || this.mPackageInfo == null) {
            return false;
        }
        this.mState.ensureIcon(this.mAppEntry);
        for (Callback callback : this.mCallbacks) {
            callback.refreshUi();
        }
        if (this.mInitialized) {
            try {
                ApplicationInfo ainfo = getActivity().getPackageManager().getApplicationInfo(this.mAppEntry.info.packageName, 4194816);
                if (!this.mShowUninstalled) {
                    if ((8388608 & ainfo.flags) != 0) {
                        z = true;
                    }
                    return z;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }
        this.mInitialized = true;
        if ((this.mAppEntry.info.flags & 8388608) == 0) {
            z = true;
        }
        this.mShowUninstalled = z;
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AlertDialog createDialog(int id, int errorCode) {
        switch (id) {
            case 1:
                return new Builder(getActivity()).setTitle(getActivity().getText(R.string.force_stop_dlg_title)).setMessage(getActivity().getText(R.string.force_stop_dlg_text)).setPositiveButton(R.string.dlg_ok, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppInfoDashboardFragment.this.forceStopPackage(AppInfoDashboardFragment.this.mAppEntry.info.packageName);
                    }
                }).setNegativeButton(R.string.dlg_cancel, null).create();
            case 2:
                return new Builder(getActivity()).setMessage(getActivity().getText(R.string.app_disable_dlg_text)).setPositiveButton(R.string.app_disable_dlg_positive, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppInfoDashboardFragment.this.mMetricsFeatureProvider.action(AppInfoDashboardFragment.this.getContext(), 874, new Pair[0]);
                        new DisableChanger(AppInfoDashboardFragment.this, AppInfoDashboardFragment.this.mAppEntry.info, 3).execute(new Object[]{null});
                    }
                }).setNegativeButton(R.string.dlg_cancel, null).create();
            case 3:
                return new Builder(getActivity()).setMessage(getActivity().getText(R.string.app_disable_dlg_text)).setPositiveButton(R.string.app_disable_dlg_positive, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppInfoDashboardFragment.this.mMetricsFeatureProvider.action(AppInfoDashboardFragment.this.getContext(), 874, new Pair[0]);
                        AppInfoDashboardFragment.this.uninstallPkg(AppInfoDashboardFragment.this.mAppEntry.info.packageName, false, true);
                    }
                }).setNegativeButton(R.string.dlg_cancel, null).create();
            default:
                return this.mInstantAppButtonPreferenceController.createDialog(id);
        }
    }

    private void uninstallPkg(String packageName, boolean allUsers, boolean andDisable) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("uninstallPkg-package: ");
        stringBuilder.append(packageName);
        Log.d(str, stringBuilder.toString());
        stopListeningToPackageRemove();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("package:");
        stringBuilder2.append(packageName);
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse(stringBuilder2.toString()));
        if (OPUtils.hasMultiApp(getContext(), packageName)) {
            uninstallIntent.setAction(OPConstants.ONEPLUS_INTENT_ACTION_DELETE);
        }
        uninstallIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", allUsers);
        this.mMetricsFeatureProvider.action(getContext(), 872, new Pair[0]);
        startActivityForResult(uninstallIntent, 0);
        this.mDisableAfterUninstall = andDisable;
    }

    private void forceStopPackage(String pkgName) {
        this.mMetricsFeatureProvider.action(getContext(), 807, pkgName, new Pair[0]);
        ActivityManager am = (ActivityManager) getActivity().getSystemService("activity");
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
        this.mAppActionButtonPreferenceController.checkForceStop(this.mAppEntry, this.mPackageInfo);
    }

    public static void startAppInfoFragment(Class<?> fragment, int title, Bundle args, SettingsPreferenceFragment caller, AppEntry appEntry) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString("package", appEntry.info.packageName);
        args.putInt("uid", appEntry.info.uid);
        new SubSettingLauncher(caller.getContext()).setDestination(fragment.getName()).setArguments(args).setTitle(title).setResultListener(caller, 1).setSourceMetricsCategory(caller.getMetricsCategory()).launch();
    }

    /* Access modifiers changed, original: 0000 */
    public void handleUninstallButtonClick() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("handleUninstallButtonClick-mAppEntry: ");
        stringBuilder.append(this.mAppEntry);
        Log.d(str, stringBuilder.toString());
        if (this.mAppEntry == null) {
            setIntentAndFinish(true, true);
            return;
        }
        str = this.mAppEntry.info.packageName;
        if (this.mDpm.packageHasActiveAdmins(this.mPackageInfo.packageName)) {
            stopListeningToPackageRemove();
            Context activity = getActivity();
            Intent uninstallDAIntent = new Intent(activity, DeviceAdminAdd.class);
            uninstallDAIntent.putExtra(DeviceAdminAdd.EXTRA_DEVICE_ADMIN_PACKAGE_NAME, this.mPackageName);
            this.mMetricsFeatureProvider.action(activity, 873, new Pair[0]);
            activity.startActivityForResult(uninstallDAIntent, 1);
            return;
        }
        EnforcedAdmin admin = RestrictedLockUtils.checkIfUninstallBlocked(getActivity(), str, this.mUserId);
        boolean uninstallBlockedBySystem = this.mAppsControlDisallowedBySystem || RestrictedLockUtils.hasBaseUserRestriction(getActivity(), str, this.mUserId);
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("handleUninstallButtonClick-admin:");
        stringBuilder2.append(admin);
        stringBuilder2.append(" uninstallBlockedBySystem: ");
        stringBuilder2.append(uninstallBlockedBySystem);
        Log.d(str2, stringBuilder2.toString());
        if (admin != null && !uninstallBlockedBySystem) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), admin);
        } else if ((this.mAppEntry.info.flags & 1) != 0) {
            if (!this.mAppEntry.info.enabled || isDisabledUntilUsed()) {
                this.mMetricsFeatureProvider.action(getActivity(), 875, new Pair[0]);
                new DisableChanger(this, this.mAppEntry.info, 1).execute(new Object[]{null});
            } else if (this.mUpdatedSysApp && isSingleUser()) {
                showDialogInner(3, 0);
            } else {
                showDialogInner(2, 0);
            }
        } else if ((this.mAppEntry.info.flags & 8388608) == 0) {
            uninstallPkg(str, true, false);
        } else {
            uninstallPkg(str, false, false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void handleForceStopButtonClick() {
        if (this.mAppEntry == null) {
            setIntentAndFinish(true, true);
            return;
        }
        if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
            showDialogInner(1, 0);
        } else {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
        }
    }

    private boolean isSingleUser() {
        int userCount = this.mUserManager.getUserCount();
        if (userCount == 1) {
            return true;
        }
        UserManager userManager = this.mUserManager;
        return UserManager.isSplitSystemUser() && userCount == 2;
    }

    private void onPackageRemoved() {
        getActivity().finishActivity(1);
        getActivity().finishAndRemoveTask();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getNumberOfUserWithPackageInstalled(String packageName) {
        int count = 0;
        for (UserInfo userInfo : this.mUserManager.getUsers(true)) {
            try {
                if ((this.mPm.getApplicationInfoAsUser(packageName, 128, userInfo.id).flags & 8388608) != 0) {
                    count++;
                }
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Package: ");
                stringBuilder.append(packageName);
                stringBuilder.append(" not found for user: ");
                stringBuilder.append(userInfo.id);
                Log.e(str, stringBuilder.toString());
            }
        }
        return count;
    }

    private String getPackageName() {
        if (this.mPackageName != null) {
            return this.mPackageName;
        }
        Bundle args = getArguments();
        this.mPackageName = args != null ? args.getString("package") : null;
        if (this.mPackageName == null) {
            Intent intent = args == null ? getActivity().getIntent() : (Intent) args.getParcelable(SliceDeepLinkSpringBoard.INTENT);
            if (intent != null) {
                this.mPackageName = intent.getData().getSchemeSpecificPart();
            }
        }
        return this.mPackageName;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void retrieveAppEntry() {
        Activity activity = getActivity();
        if (activity != null) {
            if (this.mState == null) {
                this.mState = ApplicationsState.getInstance(activity.getApplication());
                this.mSession = this.mState.newSession(this, getLifecycle());
            }
            this.mUserId = UserHandle.myUserId();
            this.mAppEntry = this.mState.getEntry(getPackageName(), UserHandle.myUserId());
            if (this.mAppEntry != null) {
                try {
                    this.mPackageInfo = activity.getPackageManager().getPackageInfo(this.mAppEntry.info.packageName, 4198976);
                } catch (NameNotFoundException e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Exception when retrieving package:");
                    stringBuilder.append(this.mAppEntry.info.packageName);
                    Log.e(str, stringBuilder.toString(), e);
                }
            } else {
                Log.w(TAG, "Missing AppEntry; maybe reinstalling?");
                this.mPackageInfo = null;
            }
        }
    }

    private void setIntentAndFinish(boolean finish, boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra("chg", appChanged);
        ((SettingsActivity) getActivity()).finishPreferencePanel(-1, intent);
        this.mFinishing = true;
    }

    /* Access modifiers changed, original: 0000 */
    public void showDialogInner(int id, int moveErrorCode) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, moveErrorCode);
        newFragment.setTargetFragment(this, 0);
        FragmentManager fragmentManager = getFragmentManager();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dialog ");
        stringBuilder.append(id);
        newFragment.show(fragmentManager, stringBuilder.toString());
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
    }

    public void onPackageListChanged() {
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void startListeningToPackageRemove() {
        if (!this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = true;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            getContext().registerReceiver(this.mPackageRemovedReceiver, filter);
        }
    }

    private void stopListeningToPackageRemove() {
        if (this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = false;
            getContext().unregisterReceiver(this.mPackageRemovedReceiver);
        }
    }
}
