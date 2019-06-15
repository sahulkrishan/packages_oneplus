package com.android.settings.vpn2;

import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.net.VpnConfig;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.vpn2.AppDialogFragment.Listener;
import com.android.settings.vpn2.ConfirmLockdownFragment.ConfirmLockdownListener;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.RestrictedSwitchPreference;

public class AppManagementFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, ConfirmLockdownListener {
    private static final String ARG_PACKAGE_NAME = "package";
    private static final String KEY_ALWAYS_ON_VPN = "always_on_vpn";
    private static final String KEY_FORGET_VPN = "forget_vpn";
    private static final String KEY_LOCKDOWN_VPN = "lockdown_vpn";
    private static final String KEY_VERSION = "version";
    private static final String TAG = "AppManagementFragment";
    private ConnectivityManager mConnectivityManager;
    private IConnectivityManager mConnectivityService;
    private final Listener mForgetVpnDialogFragmentListener = new Listener() {
        public void onForget() {
            if (AppManagementFragment.this.isVpnAlwaysOn()) {
                AppManagementFragment.this.setAlwaysOnVpn(false, false);
            }
            AppManagementFragment.this.finish();
        }

        public void onCancel() {
        }
    };
    private PackageInfo mPackageInfo;
    private PackageManager mPackageManager;
    private String mPackageName;
    private RestrictedSwitchPreference mPreferenceAlwaysOn;
    private RestrictedPreference mPreferenceForget;
    private RestrictedSwitchPreference mPreferenceLockdown;
    private Preference mPreferenceVersion;
    private final int mUserId = UserHandle.myUserId();
    private String mVpnLabel;

    public static class CannotConnectFragment extends InstrumentedDialogFragment {
        private static final String ARG_VPN_LABEL = "label";
        private static final String TAG = "CannotConnect";

        public int getMetricsCategory() {
            return 547;
        }

        public static void show(AppManagementFragment parent, String vpnLabel) {
            if (parent.getFragmentManager().findFragmentByTag(TAG) == null) {
                Bundle args = new Bundle();
                args.putString(ARG_VPN_LABEL, vpnLabel);
                DialogFragment frag = new CannotConnectFragment();
                frag.setArguments(args);
                frag.show(parent.getFragmentManager(), TAG);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String vpnLabel = getArguments().getString(ARG_VPN_LABEL);
            return new Builder(getActivity()).setTitle(getActivity().getString(R.string.vpn_cant_connect_title, new Object[]{vpnLabel})).setMessage(getActivity().getString(R.string.vpn_cant_connect_message)).setPositiveButton(R.string.okay, null).create();
        }
    }

    public static void show(Context context, AppPreference pref, int sourceMetricsCategory) {
        Bundle args = new Bundle();
        args.putString("package", pref.getPackageName());
        new SubSettingLauncher(context).setDestination(AppManagementFragment.class.getName()).setArguments(args).setTitle(pref.getLabel()).setSourceMetricsCategory(sourceMetricsCategory).setUserHandle(new UserHandle(pref.getUserId())).launch();
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        addPreferencesFromResource(R.xml.vpn_app_management);
        this.mPackageManager = getContext().getPackageManager();
        this.mConnectivityManager = (ConnectivityManager) getContext().getSystemService(ConnectivityManager.class);
        this.mConnectivityService = Stub.asInterface(ServiceManager.getService("connectivity"));
        this.mPreferenceVersion = findPreference(KEY_VERSION);
        this.mPreferenceAlwaysOn = (RestrictedSwitchPreference) findPreference(KEY_ALWAYS_ON_VPN);
        this.mPreferenceLockdown = (RestrictedSwitchPreference) findPreference(KEY_LOCKDOWN_VPN);
        this.mPreferenceForget = (RestrictedPreference) findPreference(KEY_FORGET_VPN);
        this.mPreferenceAlwaysOn.setOnPreferenceChangeListener(this);
        this.mPreferenceLockdown.setOnPreferenceChangeListener(this);
        this.mPreferenceForget.setOnPreferenceClickListener(this);
    }

    public void onResume() {
        super.onResume();
        if (loadInfo()) {
            this.mPreferenceVersion.setTitle(getPrefContext().getString(R.string.vpn_version, new Object[]{this.mPackageInfo.versionName}));
            updateUI();
            return;
        }
        finish();
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        boolean z = (key.hashCode() == -591389790 && key.equals(KEY_FORGET_VPN)) ? false : true;
        if (!z) {
            return onForgetVpnClick();
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unknown key is clicked: ");
        stringBuilder.append(key);
        Log.w(str, stringBuilder.toString());
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0047  */
    public boolean onPreferenceChange(android.support.v7.preference.Preference r5, java.lang.Object r6) {
        /*
        r4 = this;
        r0 = r5.getKey();
        r1 = r0.hashCode();
        r2 = -2008102204; // 0xffffffff884ecac4 float:-6.222922E-34 double:NaN;
        r3 = 0;
        if (r1 == r2) goto L_0x001e;
    L_0x000e:
        r2 = -1808701950; // 0xffffffff94316602 float:-8.956334E-27 double:NaN;
        if (r1 == r2) goto L_0x0014;
    L_0x0013:
        goto L_0x0028;
    L_0x0014:
        r1 = "lockdown_vpn";
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0028;
    L_0x001c:
        r0 = 1;
        goto L_0x0029;
    L_0x001e:
        r1 = "always_on_vpn";
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0028;
    L_0x0026:
        r0 = r3;
        goto L_0x0029;
    L_0x0028:
        r0 = -1;
    L_0x0029:
        switch(r0) {
            case 0: goto L_0x0059;
            case 1: goto L_0x0047;
            default: goto L_0x002c;
        };
    L_0x002c:
        r0 = "AppManagementFragment";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "unknown key is clicked: ";
        r1.append(r2);
        r2 = r5.getKey();
        r1.append(r2);
        r1 = r1.toString();
        android.util.Log.w(r0, r1);
        return r3;
    L_0x0047:
        r0 = r4.mPreferenceAlwaysOn;
        r0 = r0.isChecked();
        r1 = r6;
        r1 = (java.lang.Boolean) r1;
        r1 = r1.booleanValue();
        r0 = r4.onAlwaysOnVpnClick(r0, r1);
        return r0;
    L_0x0059:
        r0 = r6;
        r0 = (java.lang.Boolean) r0;
        r0 = r0.booleanValue();
        r1 = r4.mPreferenceLockdown;
        r1 = r1.isChecked();
        r0 = r4.onAlwaysOnVpnClick(r0, r1);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.vpn2.AppManagementFragment.onPreferenceChange(android.support.v7.preference.Preference, java.lang.Object):boolean");
    }

    public int getMetricsCategory() {
        return 100;
    }

    private boolean onForgetVpnClick() {
        updateRestrictedViews();
        if (!this.mPreferenceForget.isEnabled()) {
            return false;
        }
        AppDialogFragment.show(this, this.mForgetVpnDialogFragmentListener, this.mPackageInfo, this.mVpnLabel, true, true);
        return true;
    }

    private boolean onAlwaysOnVpnClick(boolean alwaysOnSetting, boolean lockdown) {
        boolean replacing = isAnotherVpnActive();
        boolean wasLockdown = VpnUtils.isAnyLockdownActive(getActivity());
        if (!ConfirmLockdownFragment.shouldShow(replacing, wasLockdown, lockdown)) {
            return setAlwaysOnVpnByUI(alwaysOnSetting, lockdown);
        }
        ConfirmLockdownFragment.show(this, replacing, alwaysOnSetting, wasLockdown, lockdown, null);
        return false;
    }

    public void onConfirmLockdown(Bundle options, boolean isEnabled, boolean isLockdown) {
        setAlwaysOnVpnByUI(isEnabled, isLockdown);
    }

    private boolean setAlwaysOnVpnByUI(boolean isEnabled, boolean isLockdown) {
        updateRestrictedViews();
        if (!this.mPreferenceAlwaysOn.isEnabled()) {
            return false;
        }
        if (this.mUserId == 0) {
            VpnUtils.clearLockdownVpn(getContext());
        }
        boolean success = setAlwaysOnVpn(isEnabled, isLockdown);
        if (!isEnabled || (success && isVpnAlwaysOn())) {
            updateUI();
        } else {
            CannotConnectFragment.show(this, this.mVpnLabel);
        }
        return success;
    }

    private boolean setAlwaysOnVpn(boolean isEnabled, boolean isLockdown) {
        return this.mConnectivityManager.setAlwaysOnVpnPackageForUser(this.mUserId, isEnabled ? this.mPackageName : null, isLockdown);
    }

    private void updateUI() {
        if (isAdded()) {
            boolean alwaysOn = isVpnAlwaysOn();
            boolean lockdown = alwaysOn && VpnUtils.isAnyLockdownActive(getActivity());
            this.mPreferenceAlwaysOn.setChecked(alwaysOn);
            this.mPreferenceLockdown.setChecked(lockdown);
            updateRestrictedViews();
        }
    }

    private void updateRestrictedViews() {
        if (isAdded()) {
            this.mPreferenceAlwaysOn.checkRestrictionAndSetDisabled("no_config_vpn", this.mUserId);
            this.mPreferenceLockdown.checkRestrictionAndSetDisabled("no_config_vpn", this.mUserId);
            this.mPreferenceForget.checkRestrictionAndSetDisabled("no_config_vpn", this.mUserId);
            if (this.mConnectivityManager.isAlwaysOnVpnPackageSupportedForUser(this.mUserId, this.mPackageName)) {
                this.mPreferenceAlwaysOn.setSummary((int) R.string.vpn_always_on_summary);
                return;
            }
            this.mPreferenceAlwaysOn.setEnabled(false);
            this.mPreferenceLockdown.setEnabled(false);
            this.mPreferenceAlwaysOn.setSummary((int) R.string.vpn_always_on_summary_not_supported);
        }
    }

    private String getAlwaysOnVpnPackage() {
        return this.mConnectivityManager.getAlwaysOnVpnPackageForUser(this.mUserId);
    }

    private boolean isVpnAlwaysOn() {
        return this.mPackageName.equals(getAlwaysOnVpnPackage());
    }

    private boolean loadInfo() {
        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "empty bundle");
            return false;
        }
        this.mPackageName = args.getString("package");
        if (this.mPackageName == null) {
            Log.e(TAG, "empty package name");
            return false;
        }
        try {
            this.mPackageInfo = this.mPackageManager.getPackageInfo(this.mPackageName, 0);
            this.mVpnLabel = VpnConfig.getVpnLabel(getPrefContext(), this.mPackageName).toString();
            if (this.mPackageInfo.applicationInfo == null) {
                Log.e(TAG, "package does not include an application");
                return false;
            } else if (appHasVpnPermission(getContext(), this.mPackageInfo.applicationInfo)) {
                return true;
            } else {
                Log.e(TAG, "package didn't register VPN profile");
                return false;
            }
        } catch (NameNotFoundException nnfe) {
            Log.e(TAG, "package not found", nnfe);
            return false;
        }
    }

    @VisibleForTesting
    static boolean appHasVpnPermission(Context context, ApplicationInfo application) {
        return ArrayUtils.isEmpty(((AppOpsManager) context.getSystemService("appops")).getOpsForPackage(application.uid, application.packageName, new int[]{47})) ^ 1;
    }

    private boolean isAnotherVpnActive() {
        boolean z = false;
        try {
            VpnConfig config = this.mConnectivityService.getVpnConfig(this.mUserId);
            if (!(config == null || TextUtils.equals(config.user, this.mPackageName))) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Failure to look up active VPN", e);
            return false;
        }
    }
}
