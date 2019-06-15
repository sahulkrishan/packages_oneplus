package com.android.settings.notification;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo.DisplayNameComparator;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.AppSwitchPreference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ZenAccessSettings extends EmptyTextSettings {
    private final String TAG = "ZenAccessSettings";
    private Context mContext;
    private NotificationManager mNoMan;
    private final SettingObserver mObserver = new SettingObserver();
    private PackageManager mPkgMan;

    private final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void onChange(boolean selfChange, Uri uri) {
            ZenAccessSettings.this.reloadList();
        }
    }

    public static class FriendlyWarningDialogFragment extends InstrumentedDialogFragment {
        static final String KEY_LABEL = "l";
        static final String KEY_PKG = "p";

        public int getMetricsCategory() {
            return 555;
        }

        public FriendlyWarningDialogFragment setPkgInfo(String pkg, CharSequence label) {
            Bundle args = new Bundle();
            args.putString(KEY_PKG, pkg);
            args.putString(KEY_LABEL, TextUtils.isEmpty(label) ? pkg : label.toString());
            setArguments(args);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            final String pkg = args.getString(KEY_PKG);
            String label = args.getString(KEY_LABEL);
            return new Builder(getContext()).setMessage(getResources().getString(R.string.zen_access_revoke_warning_dialog_summary)).setTitle(getResources().getString(R.string.zen_access_revoke_warning_dialog_title, new Object[]{label})).setCancelable(true).setPositiveButton(R.string.okay, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ZenAccessSettings.deleteRules(FriendlyWarningDialogFragment.this.getContext(), pkg);
                    ZenAccessSettings.setAccess(FriendlyWarningDialogFragment.this.getContext(), pkg, false);
                }
            }).setNegativeButton(R.string.cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    public static class ScaryWarningDialogFragment extends InstrumentedDialogFragment {
        static final String KEY_LABEL = "l";
        static final String KEY_PKG = "p";

        public int getMetricsCategory() {
            return 554;
        }

        public ScaryWarningDialogFragment setPkgInfo(String pkg, CharSequence label) {
            Bundle args = new Bundle();
            args.putString(KEY_PKG, pkg);
            args.putString(KEY_LABEL, TextUtils.isEmpty(label) ? pkg : label.toString());
            setArguments(args);
            return this;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            final String pkg = args.getString(KEY_PKG);
            String label = args.getString(KEY_LABEL);
            return new Builder(getContext()).setMessage(getResources().getString(R.string.zen_access_warning_dialog_summary)).setTitle(getResources().getString(R.string.zen_access_warning_dialog_title, new Object[]{label})).setCancelable(true).setPositiveButton(R.string.allow, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ZenAccessSettings.setAccess(ScaryWarningDialogFragment.this.getContext(), pkg, true);
                }
            }).setNegativeButton(R.string.deny, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
        }
    }

    public int getMetricsCategory() {
        return 180;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPkgMan = this.mContext.getPackageManager();
        this.mNoMan = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(R.string.zen_access_empty_text);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_access_settings;
    }

    public void onResume() {
        super.onResume();
        if (ActivityManager.isLowRamDeviceStatic()) {
            setEmptyText(R.string.disabled_low_ram_device);
            return;
        }
        reloadList();
        getContentResolver().registerContentObserver(Secure.getUriFor("enabled_notification_policy_access_packages"), false, this.mObserver);
        getContentResolver().registerContentObserver(Secure.getUriFor("enabled_notification_listeners"), false, this.mObserver);
    }

    public void onPause() {
        super.onPause();
        if (!ActivityManager.isLowRamDeviceStatic()) {
            getContentResolver().unregisterContentObserver(this.mObserver);
        }
    }

    private void reloadList() {
        ApplicationInfo app;
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        ArrayList<ApplicationInfo> apps = new ArrayList();
        ArraySet<String> requesting = getPackagesRequestingNotificationPolicyAccess();
        if (!requesting.isEmpty()) {
            List<ApplicationInfo> installed = this.mPkgMan.getInstalledApplications(0);
            if (installed != null) {
                for (ApplicationInfo app2 : installed) {
                    if (requesting.contains(app2.packageName)) {
                        apps.add(app2);
                    }
                }
            }
        }
        ArraySet<String> autoApproved = new ArraySet();
        autoApproved.addAll(this.mNoMan.getEnabledNotificationListenerPackages());
        requesting.addAll(autoApproved);
        Collections.sort(apps, new DisplayNameComparator(this.mPkgMan));
        Iterator it = apps.iterator();
        while (it.hasNext()) {
            app2 = (ApplicationInfo) it.next();
            final String pkg = app2.packageName;
            final CharSequence label = app2.loadLabel(this.mPkgMan);
            SwitchPreference pref = new AppSwitchPreference(getPrefContext());
            pref.setKey(pkg);
            pref.setPersistent(false);
            pref.setIcon(app2.loadIcon(this.mPkgMan));
            pref.setTitle(label);
            pref.setChecked(hasAccess(pkg));
            if (autoApproved.contains(pkg)) {
                pref.setEnabled(false);
                pref.setSummary((CharSequence) getString(R.string.zen_access_disabled_package_warning));
            }
            pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (((Boolean) newValue).booleanValue()) {
                        new ScaryWarningDialogFragment().setPkgInfo(pkg, label).show(ZenAccessSettings.this.getFragmentManager(), "dialog");
                    } else {
                        new FriendlyWarningDialogFragment().setPkgInfo(pkg, label).show(ZenAccessSettings.this.getFragmentManager(), "dialog");
                    }
                    return false;
                }
            });
            screen.addPreference(pref);
        }
    }

    private ArraySet<String> getPackagesRequestingNotificationPolicyAccess() {
        ArraySet<String> requestingPackages = new ArraySet();
        try {
            List<PackageInfo> pkgs = AppGlobals.getPackageManager().getPackagesHoldingPermissions(new String[]{"android.permission.ACCESS_NOTIFICATION_POLICY"}, 0, ActivityManager.getCurrentUser()).getList();
            if (pkgs != null) {
                for (PackageInfo info : pkgs) {
                    requestingPackages.add(info.packageName);
                }
            }
        } catch (RemoteException e) {
            Log.e("ZenAccessSettings", "Cannot reach packagemanager", e);
        }
        return requestingPackages;
    }

    private boolean hasAccess(String pkg) {
        return this.mNoMan.isNotificationPolicyAccessGrantedForPackage(pkg);
    }

    private static void setAccess(final Context context, final String pkg, final boolean access) {
        logSpecialPermissionChange(access, pkg, context);
        AsyncTask.execute(new Runnable() {
            public void run() {
                ((NotificationManager) context.getSystemService(NotificationManager.class)).setNotificationPolicyAccessGranted(pkg, access);
            }
        });
    }

    @VisibleForTesting
    static void logSpecialPermissionChange(boolean enable, String packageName, Context context) {
        int logCategory;
        if (enable) {
            logCategory = 768;
        } else {
            logCategory = 769;
        }
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, logCategory, packageName, new Pair[0]);
    }

    private static void deleteRules(final Context context, final String pkg) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                ((NotificationManager) context.getSystemService(NotificationManager.class)).removeAutomaticZenRules(pkg);
            }
        });
    }
}
