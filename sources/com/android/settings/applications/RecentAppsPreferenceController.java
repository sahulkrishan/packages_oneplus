package com.android.settings.applications;

import android.app.Application;
import android.app.Fragment;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
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
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.utils.StringUtil;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecentAppsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, Comparator<UsageStats> {
    @VisibleForTesting
    static final String KEY_DIVIDER = "all_app_info_divider";
    private static final String KEY_PREF_CATEGORY = "recent_apps_category";
    @VisibleForTesting
    static final String KEY_SEE_ALL = "all_app_info";
    private static final int SHOW_RECENT_APP_COUNT = 5;
    private static final Set<String> SKIP_SYSTEM_PACKAGES = new ArraySet();
    private static final String TAG = "RecentAppsCtrl";
    private final ApplicationsState mApplicationsState;
    private Calendar mCal;
    private PreferenceCategory mCategory;
    private Preference mDivider;
    private boolean mHasRecentApps;
    private final Fragment mHost;
    private final IconDrawableFactory mIconDrawableFactory;
    private final PackageManager mPm;
    private Preference mSeeAllPref;
    private List<UsageStats> mStats;
    private final UsageStatsManager mUsageStatsManager;
    private final int mUserId;

    static {
        SKIP_SYSTEM_PACKAGES.addAll(Arrays.asList(new String[]{"android", "com.android.phone", "com.android.settings", SliceBroadcastRelay.SYSTEMUI_PACKAGE, "com.android.providers.calendar", "com.android.providers.media"}));
    }

    public RecentAppsPreferenceController(Context context, Application app, Fragment host) {
        this(context, app == null ? null : ApplicationsState.getInstance(app), host);
    }

    @VisibleForTesting(otherwise = 5)
    RecentAppsPreferenceController(Context context, ApplicationsState appState, Fragment host) {
        super(context);
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        this.mUserId = UserHandle.myUserId();
        this.mPm = context.getPackageManager();
        this.mHost = host;
        this.mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        this.mApplicationsState = appState;
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
        refreshUi(this.mCategory.getContext());
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        refreshUi(this.mCategory.getContext());
        new InstalledAppCounter(this.mContext, -1, new PackageManagerWrapper(this.mContext.getPackageManager())) {
            /* Access modifiers changed, original: protected */
            public void onCountComplete(int num) {
                if (RecentAppsPreferenceController.this.mHasRecentApps) {
                    RecentAppsPreferenceController.this.mSeeAllPref.setTitle(RecentAppsPreferenceController.this.mContext.getString(R.string.see_all_apps_title, new Object[]{Integer.valueOf(num)}));
                    return;
                }
                RecentAppsPreferenceController.this.mSeeAllPref.setSummary(RecentAppsPreferenceController.this.mContext.getString(R.string.apps_summary, new Object[]{Integer.valueOf(num)}));
                RecentAppsPreferenceController.this.mSeeAllPref.setIcon((int) R.drawable.ic_settings_about);
            }
        }.execute(new Void[0]);
    }

    public final int compare(UsageStats a, UsageStats b) {
        return Long.compare(b.getLastTimeUsed(), a.getLastTimeUsed());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void refreshUi(Context prefContext) {
        reloadData();
        List<UsageStats> recentApps = getDisplayableRecentAppList();
        if (recentApps == null || recentApps.isEmpty()) {
            this.mHasRecentApps = false;
            displayOnlyAppInfo();
            return;
        }
        this.mHasRecentApps = true;
        displayRecentApps(prefContext, recentApps);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void reloadData() {
        this.mCal = Calendar.getInstance();
        this.mCal.add(6, -1);
        this.mStats = this.mUsageStatsManager.queryUsageStats(4, this.mCal.getTimeInMillis(), System.currentTimeMillis());
    }

    private void displayOnlyAppInfo() {
        this.mCategory.setTitle(null);
        this.mDivider.setVisible(false);
        this.mSeeAllPref.setTitle((int) R.string.applications_settings);
        this.mSeeAllPref.setIcon(null);
        for (int i = this.mCategory.getPreferenceCount() - 1; i >= 0; i--) {
            Preference pref = this.mCategory.getPreference(i);
            if (!TextUtils.equals(pref.getKey(), KEY_SEE_ALL)) {
                this.mCategory.removePreference(pref);
            }
        }
    }

    private void displayRecentApps(Context prefContext, List<UsageStats> recentApps) {
        int i;
        Context context;
        int recentAppsCount;
        this.mCategory.setTitle((int) R.string.recent_app_category_title);
        this.mDivider.setVisible(true);
        this.mSeeAllPref.setSummary(null);
        this.mSeeAllPref.setIcon((int) R.drawable.ic_chevron_right_24dp);
        ArrayMap appPreferences = new ArrayMap();
        int prefCount = this.mCategory.getPreferenceCount();
        for (i = 0; i < prefCount; i++) {
            Preference pref = this.mCategory.getPreference(i);
            String key = pref.getKey();
            if (!TextUtils.equals(key, KEY_SEE_ALL)) {
                appPreferences.put(key, pref);
            }
        }
        int recentAppsCount2 = recentApps.size();
        i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= recentAppsCount2) {
                break;
            }
            UsageStats stat = (UsageStats) recentApps.get(i2);
            String pkgName = stat.getPackageName();
            AppEntry appEntry = this.mApplicationsState.getEntry(pkgName, this.mUserId);
            if (appEntry == null) {
                context = prefContext;
                recentAppsCount = recentAppsCount2;
            } else {
                CharSequence name;
                boolean rebindPref = true;
                Preference pref2 = (Preference) appPreferences.remove(pkgName);
                if (pref2 == null) {
                    pref2 = new AppPreference(prefContext);
                    rebindPref = false;
                } else {
                    context = prefContext;
                }
                boolean rebindPref2 = rebindPref;
                pref2.setKey(pkgName);
                try {
                    name = this.mPm.getApplicationLabel(this.mPm.getApplicationInfo(pkgName, 128)).toString();
                } catch (NameNotFoundException e) {
                    name = appEntry.label;
                }
                pref2.setTitle(name);
                pref2.setIcon(this.mIconDrawableFactory.getBadgedIcon(appEntry.info));
                recentAppsCount = recentAppsCount2;
                pref2.setSummary(StringUtil.formatRelativeTime(this.mContext, (double) (System.currentTimeMillis() - stat.getLastTimeUsed()), false));
                pref2.setOrder(i2);
                pref2.setOnPreferenceClickListener(new -$$Lambda$RecentAppsPreferenceController$benLpqwf0HURWhX82bB7mmwJ8Oo(this, pkgName, appEntry));
                if (!rebindPref2) {
                    this.mCategory.addPreference(pref2);
                }
            }
            i = i2 + 1;
            recentAppsCount2 = recentAppsCount;
        }
        context = prefContext;
        List<UsageStats> list = recentApps;
        recentAppsCount = recentAppsCount2;
        for (Preference unusedPrefs : appPreferences.values()) {
            this.mCategory.removePreference(unusedPrefs);
        }
    }

    private List<UsageStats> getDisplayableRecentAppList() {
        UsageStats existingStats;
        List<UsageStats> recentApps = new ArrayList();
        Map<String, UsageStats> map = new ArrayMap();
        int statCount = this.mStats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats pkgStats = (UsageStats) this.mStats.get(i);
            if (shouldIncludePkgInRecents(pkgStats)) {
                String pkgName = pkgStats.getPackageName();
                existingStats = (UsageStats) map.get(pkgName);
                if (existingStats == null) {
                    map.put(pkgName, pkgStats);
                } else {
                    existingStats.add(pkgStats);
                }
            }
        }
        List<UsageStats> packageStats = new ArrayList();
        packageStats.addAll(map.values());
        Collections.sort(packageStats, this);
        int count = 0;
        for (UsageStats existingStats2 : packageStats) {
            if (this.mApplicationsState.getEntry(existingStats2.getPackageName(), this.mUserId) != null) {
                recentApps.add(existingStats2);
                count++;
                if (count >= 5) {
                    break;
                }
            }
        }
        return recentApps;
    }

    private boolean shouldIncludePkgInRecents(UsageStats stat) {
        String pkgName = stat.getPackageName();
        String str;
        StringBuilder stringBuilder;
        if (stat.getLastTimeUsed() < this.mCal.getTimeInMillis()) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid timestamp, skipping ");
            stringBuilder.append(pkgName);
            Log.d(str, stringBuilder.toString());
            return false;
        } else if (SKIP_SYSTEM_PACKAGES.contains(pkgName)) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("System package, skipping ");
            stringBuilder.append(pkgName);
            Log.d(str, stringBuilder.toString());
            return false;
        } else {
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
}
