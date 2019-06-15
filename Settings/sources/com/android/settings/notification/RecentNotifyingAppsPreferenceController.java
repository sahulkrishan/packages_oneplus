package com.android.settings.notification;

import android.app.Application;
import android.app.Fragment;
import android.app.usage.IUsageStatsManager;
import android.app.usage.IUsageStatsManager.Stub;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.NotifyingApp;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.AppStateNotificationBridge;
import com.android.settings.applications.AppStateNotificationBridge.NotificationsSentState;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.utils.StringUtil;
import com.oneplus.settings.utils.OPConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecentNotifyingAppsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final int DAYS_TO_CHECK = 7;
    @VisibleForTesting
    static final String KEY_DIVIDER = "all_notifications_divider";
    private static final String KEY_PREF_CATEGORY = "recent_notifications_category";
    @VisibleForTesting
    static final String KEY_SEE_ALL = "all_notifications";
    private static final int SHOW_RECENT_APP_COUNT = 5;
    private static final Set<String> SKIP_SYSTEM_PACKAGES = new ArraySet();
    private static final String TAG = "RecentNotisCtrl";
    private final ApplicationsState mApplicationsState;
    private List<NotifyingApp> mApps;
    private PreferenceCategory mCategory;
    private Preference mDivider;
    private Map<String, NotificationsSentState> mEventsMap;
    private final Fragment mHost;
    private final IconDrawableFactory mIconDrawableFactory;
    private final NotificationBackend mNotificationBackend;
    private final PackageManager mPm;
    private Preference mSeeAllPref;
    private IUsageStatsManager mUsageStatsManager;
    private final int mUserId;
    protected List<Integer> mUserIds;
    private UserManager mUserManager;

    private class loadAggregatedUsageEventsTask extends AsyncTask<String, Void, Void> {
        private loadAggregatedUsageEventsTask() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(String... params) {
            RecentNotifyingAppsPreferenceController.this.mEventsMap = RecentNotifyingAppsPreferenceController.this.getAggregatedUsageEvents();
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            RecentNotifyingAppsPreferenceController.this.refreshUi(RecentNotifyingAppsPreferenceController.this.mCategory.getContext());
        }
    }

    static {
        SKIP_SYSTEM_PACKAGES.addAll(Arrays.asList(new String[]{"android", "com.android.phone", "com.android.settings", SliceBroadcastRelay.SYSTEMUI_PACKAGE, "com.android.providers.calendar", "com.android.providers.media"}));
    }

    public RecentNotifyingAppsPreferenceController(Context context, NotificationBackend backend, Application app, Fragment host) {
        this(context, backend, app == null ? null : ApplicationsState.getInstance(app), host);
    }

    @VisibleForTesting(otherwise = 5)
    RecentNotifyingAppsPreferenceController(Context context, NotificationBackend backend, ApplicationsState appState, Fragment host) {
        super(context);
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        this.mUserId = UserHandle.myUserId();
        this.mPm = context.getPackageManager();
        this.mHost = host;
        this.mApplicationsState = appState;
        this.mNotificationBackend = backend;
        this.mUsageStatsManager = Stub.asInterface(ServiceManager.getService("usagestats"));
        this.mUserIds = new ArrayList();
        this.mUserIds.add(Integer.valueOf(context.getUserId()));
        this.mUserManager = UserManager.get(context);
        int workUserId = Utils.getManagedProfileId(this.mUserManager, this.mContext.getUserId());
        if (workUserId != -10000) {
            this.mUserIds.add(Integer.valueOf(workUserId));
        }
    }

    private void loadAggregatedUsageEvents() {
        new loadAggregatedUsageEventsTask().execute(new String[0]);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_PREF_CATEGORY;
    }

    public void updateNonIndexableKeys(List<String> keys) {
        super.updateNonIndexableKeys(keys);
        keys.add(KEY_PREF_CATEGORY);
        keys.add(KEY_DIVIDER);
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mCategory = (PreferenceCategory) screen.findPreference(getPreferenceKey());
        this.mSeeAllPref = screen.findPreference(KEY_SEE_ALL);
        this.mDivider = screen.findPreference(KEY_DIVIDER);
        super.displayPreference(screen);
        loadAggregatedUsageEvents();
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshUi(this.mCategory.getContext());
        this.mSeeAllPref.setTitle(this.mContext.getString(R.string.recent_notifications_see_all_title));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void refreshUi(Context prefContext) {
        reloadData();
        List<NotifyingApp> recentApps = getDisplayableRecentAppList();
        if (recentApps == null || recentApps.isEmpty()) {
            displayOnlyAllAppsLink();
        } else {
            displayRecentApps(prefContext, recentApps);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void reloadData() {
        this.mApps = this.mNotificationBackend.getRecentApps();
    }

    private void displayOnlyAllAppsLink() {
        this.mCategory.setTitle(null);
        this.mDivider.setVisible(false);
        this.mSeeAllPref.setTitle((int) R.string.notifications_title);
        this.mSeeAllPref.setIcon(null);
        for (int i = this.mCategory.getPreferenceCount() - 1; i >= 0; i--) {
            Preference pref = this.mCategory.getPreference(i);
            if (!TextUtils.equals(pref.getKey(), KEY_SEE_ALL)) {
                this.mCategory.removePreference(pref);
            }
        }
    }

    private void displayRecentApps(Context prefContext, List<NotifyingApp> recentApps) {
        int i;
        Context context;
        int recentAppsCount;
        this.mCategory.setTitle((int) R.string.recent_notifications);
        this.mDivider.setVisible(true);
        this.mSeeAllPref.setSummary(null);
        this.mSeeAllPref.setIcon((int) R.drawable.ic_chevron_right_24dp);
        Map<String, NotificationAppPreference> appPreferences = new ArrayMap();
        int prefCount = this.mCategory.getPreferenceCount();
        for (i = 0; i < prefCount; i++) {
            Preference pref = this.mCategory.getPreference(i);
            String key = pref.getKey();
            if (!TextUtils.equals(key, KEY_SEE_ALL)) {
                appPreferences.put(key, (NotificationAppPreference) pref);
            }
        }
        i = recentApps.size();
        int i2 = 0;
        while (i2 < i) {
            NotifyingApp app = (NotifyingApp) recentApps.get(i2);
            String pkgName = app.getPackage();
            AppEntry appEntry = this.mApplicationsState.getEntry(app.getPackage(), this.mUserId);
            if (appEntry == null) {
                context = prefContext;
                recentAppsCount = i;
            } else {
                if (this.mEventsMap != null) {
                    NotificationsSentState stats = (NotificationsSentState) this.mEventsMap.get(getKey(UserHandle.getUserId(appEntry.info.uid), appEntry.info.packageName));
                    calculateAvgSentCounts(stats);
                    addBlockStatus(appEntry, stats);
                    appEntry.extraInfo = stats;
                }
                boolean rebindPref = true;
                NotificationAppPreference pref2 = (NotificationAppPreference) appPreferences.remove(pkgName);
                if (pref2 == null) {
                    pref2 = new NotificationAppPreference(prefContext);
                    rebindPref = false;
                } else {
                    context = prefContext;
                }
                pref2.setKey(pkgName);
                pref2.setTitle((CharSequence) appEntry.label);
                pref2.setIcon(this.mIconDrawableFactory.getBadgedIcon(appEntry.info));
                pref2.setIconSize(2);
                recentAppsCount = i;
                pref2.setSummary(StringUtil.formatRelativeTime(this.mContext, (double) (System.currentTimeMillis() - app.getLastNotified()), true));
                pref2.setOrder(i2);
                Bundle args = new Bundle();
                args.putString("package", pkgName);
                args.putInt("uid", appEntry.info.uid);
                pref2.setIntent(new SubSettingLauncher(this.mHost.getActivity()).setDestination(AppNotificationSettings.class.getName()).setTitle((int) R.string.notifications_title).setArguments(args).setSourceMetricsCategory(Const.CODE_C1_CW5).toIntent());
                pref2.setOnPreferenceChangeListener(new -$$Lambda$RecentNotifyingAppsPreferenceController$7CmRKIepfLY9sZOWQrI97x_3AWA(this, pkgName, appEntry));
                pref2.setChecked(this.mNotificationBackend.getNotificationsBanned(pkgName, appEntry.info.uid) ^ 1);
                if (OPConstants.PACKAGENAME_DESKCOLCK.equals(pkgName) || OPConstants.PACKAGENAME_INCALLUI.equals(pkgName) || "com.google.android.calendar".equals(pkgName) || "com.oneplus.calendar".equals(pkgName) || OPConstants.PACKAGENAME_DIALER.equals(pkgName)) {
                    pref2.setSwitchEnabled(false);
                } else {
                    pref2.setSwitchEnabled(AppStateNotificationBridge.enableSwitch(appEntry));
                }
                if (!rebindPref) {
                    this.mCategory.addPreference(pref2);
                }
            }
            i2++;
            i = recentAppsCount;
        }
        context = prefContext;
        List<NotifyingApp> list = recentApps;
        recentAppsCount = i;
        for (Preference unusedPrefs : appPreferences.values()) {
            this.mCategory.removePreference(unusedPrefs);
        }
    }

    public static /* synthetic */ boolean lambda$displayRecentApps$0(RecentNotifyingAppsPreferenceController recentNotifyingAppsPreferenceController, String pkgName, AppEntry appEntry, Preference preference, Object newValue) {
        recentNotifyingAppsPreferenceController.mNotificationBackend.setNotificationsEnabledForPackage(pkgName, appEntry.info.uid, !(((Boolean) newValue).booleanValue() ^ true));
        return true;
    }

    private void addBlockStatus(AppEntry entry, NotificationsSentState stats) {
        if (stats != null) {
            stats.blocked = this.mNotificationBackend.getNotificationsBanned(entry.info.packageName, entry.info.uid);
            stats.systemApp = this.mNotificationBackend.isSystemApp(this.mContext, entry.info);
            boolean z = !stats.systemApp || (stats.systemApp && stats.blocked);
            stats.blockable = z;
        }
    }

    private void calculateAvgSentCounts(NotificationsSentState stats) {
        if (stats != null) {
            stats.avgSentDaily = Math.round(((float) stats.sentCount) / 7.0f);
            if (stats.sentCount < 7) {
                stats.avgSentWeekly = stats.sentCount;
            }
        }
    }

    protected static String getKey(int userId, String pkg) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userId);
        stringBuilder.append("|");
        stringBuilder.append(pkg);
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public NotificationsSentState getAggregatedUsageEvents(int userId, String pkg) {
        NotificationsSentState stats = null;
        long now = System.currentTimeMillis();
        UsageEvents events = null;
        try {
            events = this.mUsageStatsManager.queryEventsForPackageForUser(now - 604800000, now, userId, pkg, this.mContext.getPackageName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (events != null) {
            Event event = new Event();
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == 12) {
                    if (stats == null) {
                        stats = new NotificationsSentState();
                    }
                    if (event.getTimeStamp() > stats.lastSent) {
                        stats.lastSent = event.getTimeStamp();
                    }
                    stats.sentCount++;
                }
            }
        }
        return stats;
    }

    /* Access modifiers changed, original: protected */
    public Map<String, NotificationsSentState> getAggregatedUsageEvents() {
        ArrayMap<String, NotificationsSentState> aggregatedStats = new ArrayMap();
        long now = System.currentTimeMillis();
        long startTime = now - 604800000;
        for (Integer intValue : this.mUserIds) {
            int userId = intValue.intValue();
            UsageEvents events = null;
            try {
                events = this.mUsageStatsManager.queryEventsForUser(startTime, now, userId, this.mContext.getPackageName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (events != null) {
                Event event = new Event();
                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    NotificationsSentState stats = (NotificationsSentState) aggregatedStats.get(getKey(userId, event.getPackageName()));
                    if (stats == null) {
                        stats = new NotificationsSentState();
                        aggregatedStats.put(getKey(userId, event.getPackageName()), stats);
                    }
                    if (event.getEventType() == 12) {
                        if (event.getTimeStamp() > stats.lastSent) {
                            stats.lastSent = event.getTimeStamp();
                        }
                        stats.sentCount++;
                    }
                }
            }
        }
        return aggregatedStats;
    }

    private List<NotifyingApp> getDisplayableRecentAppList() {
        Collections.sort(this.mApps);
        List<NotifyingApp> displayableApps = new ArrayList(5);
        int count = 0;
        for (NotifyingApp app : this.mApps) {
            if (this.mApplicationsState.getEntry(app.getPackage(), this.mUserId) != null) {
                if (shouldIncludePkgInRecents(app.getPackage())) {
                    displayableApps.add(app);
                    count++;
                    if (count >= 5) {
                        break;
                    }
                }
            }
        }
        return displayableApps;
    }

    private boolean shouldIncludePkgInRecents(String pkgName) {
        if (SKIP_SYSTEM_PACKAGES.contains(pkgName)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("System package, skipping ");
            stringBuilder.append(pkgName);
            Log.d(str, stringBuilder.toString());
            return false;
        }
        if (this.mPm.resolveActivity(new Intent().addCategory("android.intent.category.LAUNCHER").setPackage(pkgName), 0) == null) {
            AppEntry appEntry = this.mApplicationsState.getEntry(pkgName, this.mUserId);
            if (appEntry == null || appEntry.info == null || !AppUtils.isInstant(appEntry.info)) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Not a user visible or instant app, skipping ");
                stringBuilder2.append(pkgName);
                Log.d(str2, stringBuilder2.toString());
                return false;
            }
        }
        return true;
    }
}
