package com.oneplus.settings.notification;

import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.NotificationBackend;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OPNotificationAppList extends SettingsPreferenceFragment {
    private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES");
    private static final boolean DEBUG = true;
    private static final String EXTRA_HAS_SETTINGS_INTENT = "has_settings_intent";
    private static final String EXTRA_SETTINGS_INTENT = "settings_intent";
    private static final String KEY_ALLOW_LED_APPS = "op_notification_allow_led";
    private static final String KEY_NOT_ALLOW_LED_APPS = "op_notification_not_allow_led";
    private static long PROGRESS_MIN_SHOW_TIME = 500;
    private static final String TAG = "OPNotificationAppList";
    private static long WILL_SHOW_PROGRESS_TIME = 300;
    private static final Comparator<AppRow> mRowComparator = new Comparator<AppRow>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppRow lhs, AppRow rhs) {
            return this.sCollator.compare(lhs.label, rhs.label);
        }
    };
    private View emptyView;
    private PreferenceCategory mAllowLEDApps;
    private Backend mBackend = new Backend();
    private final Runnable mCollectAppsRunnable = new Runnable() {
        public void run() {
            synchronized (OPNotificationAppList.this.mRows) {
                String str;
                StringBuilder stringBuilder;
                long start = SystemClock.uptimeMillis();
                Log.d(OPNotificationAppList.TAG, "Collecting apps...");
                OPNotificationAppList.this.mRows.clear();
                OPNotificationAppList.this.mSortedRows.clear();
                List<ApplicationInfo> appInfos = new ArrayList();
                List<LauncherActivityInfo> lais = OPNotificationAppList.this.mLauncherApps.getActivityList(null, UserHandle.OWNER);
                Log.d(OPNotificationAppList.TAG, "  launchable activities:");
                for (LauncherActivityInfo lai : lais) {
                    String str2 = OPNotificationAppList.TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("oneplus- ");
                    stringBuilder2.append(lai.getComponentName().toString());
                    Log.d(str2, stringBuilder2.toString());
                    appInfos.add(lai.getApplicationInfo());
                }
                List<ResolveInfo> resolvedConfigActivities = OPNotificationAppList.queryNotificationConfigActivities(OPNotificationAppList.this.mPM);
                Log.d(OPNotificationAppList.TAG, "  config activities:");
                for (ResolveInfo ri : resolvedConfigActivities) {
                    str = OPNotificationAppList.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("oneplus-");
                    stringBuilder.append(ri.activityInfo.packageName);
                    stringBuilder.append("/");
                    stringBuilder.append(ri.activityInfo.name);
                    Log.d(str, stringBuilder.toString());
                    appInfos.add(ri.activityInfo.applicationInfo);
                }
                for (ApplicationInfo info : appInfos) {
                    str = info.packageName;
                    if (!OPNotificationAppList.this.mRows.containsKey(str)) {
                        OPNotificationAppList.this.mRows.put(str, OPNotificationAppList.loadAppRow(OPNotificationAppList.this.mPM, info, OPNotificationAppList.this.mBackend));
                    }
                }
                OPNotificationAppList.applyConfigActivities(OPNotificationAppList.this.mPM, OPNotificationAppList.this.mRows, resolvedConfigActivities);
                OPNotificationAppList.this.mSortedRows.addAll(OPNotificationAppList.this.mRows.values());
                Collections.sort(OPNotificationAppList.this.mSortedRows, OPNotificationAppList.mRowComparator);
                OPNotificationAppList.this.mHandler.post(OPNotificationAppList.this.mRefreshAppsListRunnable);
                long elapsed = SystemClock.uptimeMillis() - start;
                str = OPNotificationAppList.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("oneplus-Collected ");
                stringBuilder.append(OPNotificationAppList.this.mRows.size());
                stringBuilder.append(" apps in ");
                stringBuilder.append(elapsed);
                stringBuilder.append("ms");
                Log.d(str, stringBuilder.toString());
            }
        }
    };
    private Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler mHandler1 = new Handler(Looper.getMainLooper());
    private boolean mHasShowProgress;
    private LauncherApps mLauncherApps;
    private Parcelable mListViewState;
    private PreferenceCategory mNotAllowLEDApps;
    private NotificationBackend mNotificationBackend = new NotificationBackend();
    private PackageManager mPM;
    private final Runnable mRefreshAppsListRunnable = new Runnable() {
        public void run() {
            OPNotificationAppList.this.refreshDisplayedItems(OPNotificationAppList.this.mSortedRows);
        }
    };
    private PreferenceScreen mRoot;
    private final ArrayMap<String, AppRow> mRows = new ArrayMap();
    private boolean mShowAllowLEDApps = false;
    private boolean mShowNotAllowLEDApps = false;
    private Runnable mShowPromptRunnable;
    private long mShowPromptTime;
    private final ArrayList<AppRow> mSortedRows = new ArrayList();
    private Signature[] mSystemSignature;
    private UserManager mUM;

    public static class AppRow {
        public boolean banned;
        public boolean first;
        public Drawable icon;
        public CharSequence label;
        public boolean ledDisabled;
        public String pkg;
        public boolean priority;
        public boolean sensitive;
        public Intent settingsIntent;
        public int uid;
    }

    public static class Backend {
        static INotificationManager sINM = Stub.asInterface(ServiceManager.getService("notification"));

        public boolean setNotificationsBanned(String pkg, int uid, boolean banned) {
            try {
                sINM.setNotificationsEnabledForPackage(pkg, uid, banned ^ 1);
                return true;
            } catch (Exception e) {
                Log.w(OPNotificationAppList.TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getNotificationsBanned(String pkg, int uid) {
            boolean z = false;
            try {
                if (!sINM.areNotificationsEnabledForPackage(pkg, uid)) {
                    z = true;
                }
                return z;
            } catch (Exception e) {
                Log.w(OPNotificationAppList.TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean getHighPriority(String pkg, int uid) {
            return true;
        }

        public boolean setHighPriority(String pkg, int uid, boolean highPriority) {
            return true;
        }

        public boolean getSensitive(String pkg, int uid) {
            return true;
        }

        public boolean setSensitive(String pkg, int uid, boolean sensitive) {
            return true;
        }

        public boolean getLedDisabled(String pkg) {
            try {
                return sINM.isNotificationLedEnabled(pkg) ^ 1;
            } catch (Exception e) {
                Log.w(OPNotificationAppList.TAG, "Error calling NoMan", e);
                return false;
            }
        }

        public boolean setLedDisabled(String pkg, boolean ledDisabled) {
            try {
                sINM.setNotificationLedStatus(pkg, ledDisabled);
                return true;
            } catch (Exception e) {
                Log.w(OPNotificationAppList.TAG, "Error calling NoMan", e);
                return false;
            }
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPreExecute() {
        this.mHasShowProgress = false;
        this.mShowPromptRunnable = new Runnable() {
            public void run() {
                OPNotificationAppList.this.mHasShowProgress = true;
                if (OPNotificationAppList.this.emptyView != null) {
                    OPNotificationAppList.this.setEmptyView(OPNotificationAppList.this.emptyView);
                }
                OPNotificationAppList.this.mShowPromptTime = System.currentTimeMillis();
            }
        };
        this.mHandler1.postDelayed(this.mShowPromptRunnable, WILL_SHOW_PROGRESS_TIME);
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPostExecute() {
        if (this.mHasShowProgress) {
            long remainShowTime = PROGRESS_MIN_SHOW_TIME - (System.currentTimeMillis() - this.mShowPromptTime);
            if (remainShowTime > 0) {
                this.mHandler1.postDelayed(new Runnable() {
                    public void run() {
                        if (OPNotificationAppList.this.emptyView != null) {
                            OPNotificationAppList.this.emptyView.setVisibility(8);
                        }
                        OPNotificationAppList.this.loadAppsList();
                    }
                }, remainShowTime);
                return;
            }
            if (this.emptyView != null) {
                this.emptyView.setVisibility(8);
            }
            loadAppsList();
            return;
        }
        if (this.emptyView != null) {
            this.emptyView.setVisibility(8);
        }
        loadAppsList();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_app_notification_list_settings);
        this.mContext = getActivity();
        this.mUM = UserManager.get(this.mContext);
        this.mPM = this.mContext.getPackageManager();
        this.mLauncherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        this.mRoot = getPreferenceScreen();
        this.mAllowLEDApps = (PreferenceCategory) findPreference(KEY_ALLOW_LED_APPS);
        this.mNotAllowLEDApps = (PreferenceCategory) findPreference(KEY_NOT_ALLOW_LED_APPS);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        this.emptyView = getActivity().getLayoutInflater().inflate(R.layout.loading_container, contentRoot, false);
        this.emptyView.setVisibility(0);
        contentRoot.addView(this.emptyView);
        onPreExecute();
        resetUI();
        onPostExecute();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private void loadAppsList() {
        AsyncTask.execute(this.mCollectAppsRunnable);
    }

    public static AppRow loadAppRow(PackageManager pm, ApplicationInfo app, Backend backend) {
        AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error loading application label for ");
            stringBuilder.append(row.pkg);
            Log.e(str, stringBuilder.toString(), t);
            row.label = row.pkg;
        }
        row.icon = app.loadIcon(pm);
        row.banned = backend.getNotificationsBanned(row.pkg, row.uid);
        row.priority = backend.getHighPriority(row.pkg, row.uid);
        row.sensitive = backend.getSensitive(row.pkg, row.uid);
        row.ledDisabled = backend.getLedDisabled(row.pkg);
        return row;
    }

    public static List<ResolveInfo> queryNotificationConfigActivities(PackageManager pm) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("APP_NOTIFICATION_PREFS_CATEGORY_INTENT is ");
        stringBuilder.append(APP_NOTIFICATION_PREFS_CATEGORY_INTENT);
        Log.d(str, stringBuilder.toString());
        return pm.queryIntentActivities(APP_NOTIFICATION_PREFS_CATEGORY_INTENT, 0);
    }

    public static void collectConfigActivities(PackageManager pm, ArrayMap<String, AppRow> rows) {
        applyConfigActivities(pm, rows, queryNotificationConfigActivities(pm));
    }

    public static void applyConfigActivities(PackageManager pm, ArrayMap<String, AppRow> rows, List<ResolveInfo> resolveInfos) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Found ");
        stringBuilder.append(resolveInfos.size());
        stringBuilder.append(" preference activities");
        stringBuilder.append(resolveInfos.size() == 0 ? " ;_;" : "");
        Log.d(str, stringBuilder.toString());
        for (ResolveInfo ri : resolveInfos) {
            ActivityInfo activityInfo = ri.activityInfo;
            AppRow row = (AppRow) rows.get(activityInfo.applicationInfo.packageName);
            String str2;
            StringBuilder stringBuilder2;
            if (row == null) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Ignoring notification preference activity (");
                stringBuilder2.append(activityInfo.name);
                stringBuilder2.append(") for unknown package ");
                stringBuilder2.append(activityInfo.packageName);
                Log.v(str2, stringBuilder2.toString());
            } else if (row.settingsIntent != null) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Ignoring duplicate notification preference activity (");
                stringBuilder2.append(activityInfo.name);
                stringBuilder2.append(") for package ");
                stringBuilder2.append(activityInfo.packageName);
                Log.v(str2, stringBuilder2.toString());
            } else {
                row.settingsIntent = new Intent(APP_NOTIFICATION_PREFS_CATEGORY_INTENT).setClassName(activityInfo.packageName, activityInfo.name);
            }
        }
    }

    private void resetUI() {
        this.mAllowLEDApps.removeAll();
        this.mNotAllowLEDApps.removeAll();
        this.mRoot.removePreference(this.mNotAllowLEDApps);
        this.mRoot.removePreference(this.mAllowLEDApps);
        this.mShowNotAllowLEDApps = false;
        this.mShowAllowLEDApps = false;
    }

    private void removeCatagoryIfNoneApp() {
        if (this.mShowNotAllowLEDApps) {
            this.mRoot.addPreference(this.mNotAllowLEDApps);
        } else {
            this.mRoot.removePreference(this.mNotAllowLEDApps);
        }
        if (this.mShowAllowLEDApps) {
            this.mRoot.addPreference(this.mAllowLEDApps);
        } else {
            this.mRoot.removePreference(this.mAllowLEDApps);
        }
    }

    private void refreshDisplayedItems(ArrayList<AppRow> sortedRows) {
        resetUI();
        for (int i = 0; i < sortedRows.size(); i++) {
            final AppRow row = (AppRow) sortedRows.get(i);
            final int position = i;
            SwitchPreference itemPref = new SwitchPreference(this.mContext);
            if (!(OPConstants.PACKAGENAME_DESKCOLCK.equals(row.pkg) || OPConstants.PACKAGENAME_INCALLUI.equals(row.pkg) || "com.google.android.calendar".equals(row.pkg) || "com.oneplus.calendar".equals(row.pkg) || OPConstants.PACKAGENAME_DIALER.equals(row.pkg))) {
                itemPref.setKey(row.pkg);
                itemPref.setTitle(row.label);
                itemPref.setIcon(row.icon);
                itemPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enable = ((Boolean) newValue).booleanValue();
                        row.ledDisabled = enable ^ 1;
                        OPNotificationAppList.this.mNotificationBackend.setLedEnabled(row.pkg, enable);
                        OPNotificationAppList.this.mSortedRows.set(position, row);
                        OPNotificationAppList.this.mHandler.post(OPNotificationAppList.this.mRefreshAppsListRunnable);
                        return true;
                    }
                });
                if (row.ledDisabled) {
                    this.mShowNotAllowLEDApps = true;
                    this.mNotAllowLEDApps.addPreference(itemPref);
                } else {
                    this.mShowAllowLEDApps = true;
                    this.mAllowLEDApps.addPreference(itemPref);
                }
                itemPref.setChecked(row.ledDisabled ^ 1);
            }
        }
        removeCatagoryIfNoneApp();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Refreshed ");
        stringBuilder.append(this.mSortedRows.size());
        stringBuilder.append(" displayed items");
        Log.d(str, stringBuilder.toString());
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
