package com.android.settings.applications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public abstract class AppInfoBase extends SettingsPreferenceFragment implements Callbacks {
    public static final String ARG_PACKAGE_NAME = "package";
    public static final String ARG_PACKAGE_UID = "uid";
    protected static final int DLG_BASE = 0;
    protected static final String TAG = AppInfoBase.class.getSimpleName();
    protected static final boolean localLOGV = false;
    protected AppEntry mAppEntry;
    protected ApplicationFeatureProvider mApplicationFeatureProvider;
    protected EnforcedAdmin mAppsControlDisallowedAdmin;
    protected boolean mAppsControlDisallowedBySystem;
    protected DevicePolicyManager mDpm;
    protected boolean mFinishing;
    protected boolean mListeningToPackageRemove;
    protected PackageInfo mPackageInfo;
    protected String mPackageName;
    protected final BroadcastReceiver mPackageRemovedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (!AppInfoBase.this.mFinishing) {
                if (AppInfoBase.this.mAppEntry == null || AppInfoBase.this.mAppEntry.info == null || TextUtils.equals(AppInfoBase.this.mAppEntry.info.packageName, packageName)) {
                    AppInfoBase.this.onPackageRemoved();
                }
            }
        }
    };
    protected PackageManager mPm;
    protected Session mSession;
    protected ApplicationsState mState;
    protected IUsbManager mUsbManager;
    protected int mUserId;
    protected UserManager mUserManager;

    public static class MyAlertDialogFragment extends InstrumentedDialogFragment {
        private static final String ARG_ID = "id";

        public int getMetricsCategory() {
            return 558;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            Dialog dialog = ((AppInfoBase) getTargetFragment()).createDialog(id, getArguments().getInt("moveError"));
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

    public abstract AlertDialog createDialog(int i, int i2);

    public abstract boolean refreshUi();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFinishing = false;
        Activity activity = getActivity();
        this.mApplicationFeatureProvider = FeatureFactory.getFactory(activity).getApplicationFeatureProvider(activity);
        this.mState = ApplicationsState.getInstance(activity.getApplication());
        this.mSession = this.mState.newSession(this, getLifecycle());
        this.mDpm = (DevicePolicyManager) activity.getSystemService("device_policy");
        this.mUserManager = (UserManager) activity.getSystemService("user");
        this.mPm = activity.getPackageManager();
        this.mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));
        retrieveAppEntry();
        startListeningToPackageRemove();
    }

    public void onResume() {
        super.onResume();
        this.mAppsControlDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_control_apps", this.mUserId);
        this.mAppsControlDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_control_apps", this.mUserId);
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    public void onDestroy() {
        stopListeningToPackageRemove();
        super.onDestroy();
    }

    /* Access modifiers changed, original: protected */
    public String retrieveAppEntry() {
        Bundle args = getArguments();
        this.mPackageName = args != null ? args.getString("package") : null;
        Intent intent = args == null ? getIntent() : (Intent) args.getParcelable(SliceDeepLinkSpringBoard.INTENT);
        if (!(this.mPackageName != null || intent == null || intent.getData() == null)) {
            this.mPackageName = intent.getData().getSchemeSpecificPart();
        }
        if (intent == null || !intent.hasExtra("android.intent.extra.user_handle")) {
            this.mUserId = UserHandle.myUserId();
        } else {
            this.mUserId = ((UserHandle) intent.getParcelableExtra("android.intent.extra.user_handle")).getIdentifier();
        }
        this.mAppEntry = this.mState.getEntry(this.mPackageName, this.mUserId);
        if (this.mAppEntry != null) {
            try {
                this.mPackageInfo = this.mPm.getPackageInfoAsUser(this.mAppEntry.info.packageName, 134222336, this.mUserId);
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
        return this.mPackageName;
    }

    /* Access modifiers changed, original: protected */
    public void setIntentAndFinish(boolean finish, boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra("chg", appChanged);
        ((SettingsActivity) getActivity()).finishPreferencePanel(-1, intent);
        this.mFinishing = true;
    }

    /* Access modifiers changed, original: protected */
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

    public void onPackageSizeChanged(String packageName) {
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

    public static void startAppInfoFragment(Class<?> fragment, int titleRes, String pkg, int uid, Fragment source, int request, int sourceMetricsCategory) {
        Bundle args = new Bundle();
        args.putString("package", pkg);
        args.putInt("uid", uid);
        new SubSettingLauncher(source.getContext()).setDestination(fragment.getName()).setSourceMetricsCategory(sourceMetricsCategory).setTitle(titleRes).setArguments(args).setUserHandle(new UserHandle(UserHandle.getUserId(uid))).setResultListener(source, request).launch();
    }

    /* Access modifiers changed, original: protected */
    public void startListeningToPackageRemove() {
        if (!this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = true;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            getContext().registerReceiver(this.mPackageRemovedReceiver, filter);
        }
    }

    /* Access modifiers changed, original: protected */
    public void stopListeningToPackageRemove() {
        if (this.mListeningToPackageRemove) {
            this.mListeningToPackageRemove = false;
            getContext().unregisterReceiver(this.mPackageRemovedReceiver);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPackageRemoved() {
        getActivity().finishAndRemoveTask();
    }
}
