package com.android.settings.fuelgauge;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class InactiveApps extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final CharSequence[] SETTABLE_BUCKETS_NAMES = new CharSequence[]{"ACTIVE", "WORKING_SET", "FREQUENT", "RARE"};
    private static final CharSequence[] SETTABLE_BUCKETS_VALUES = new CharSequence[]{Integer.toString(10), Integer.toString(20), Integer.toString(30), Integer.toString(40)};
    private UsageStatsManager mUsageStats;

    public int getMetricsCategory() {
        return 238;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUsageStats = (UsageStatsManager) getActivity().getSystemService(UsageStatsManager.class);
        addPreferencesFromResource(R.xml.inactive_apps);
    }

    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        PreferenceGroup screen = getPreferenceScreen();
        screen.removeAll();
        screen.setOrderingAsAdded(false);
        Context context = getActivity();
        PackageManager pm = context.getPackageManager();
        context.getSystemService(UsageStatsManager.class);
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory("android.intent.category.LAUNCHER");
        for (ResolveInfo app : pm.queryIntentActivities(launcherIntent, 0)) {
            String packageName = app.activityInfo.applicationInfo.packageName;
            ListPreference p = new ListPreference(getPrefContext());
            p.setTitle(app.loadLabel(pm));
            p.setIcon(app.loadIcon(pm));
            p.setKey(packageName);
            p.setEntries(SETTABLE_BUCKETS_NAMES);
            p.setEntryValues(SETTABLE_BUCKETS_VALUES);
            updateSummary(p);
            p.setOnPreferenceChangeListener(this);
            screen.addPreference(p);
        }
    }

    static String bucketToName(int bucket) {
        if (bucket == 5) {
            return "EXEMPTED";
        }
        if (bucket == 10) {
            return "ACTIVE";
        }
        if (bucket == 20) {
            return "WORKING_SET";
        }
        if (bucket == 30) {
            return "FREQUENT";
        }
        if (bucket == 40) {
            return "RARE";
        }
        if (bucket != 50) {
            return "";
        }
        return "NEVER";
    }

    private void updateSummary(ListPreference p) {
        Resources res = getActivity().getResources();
        int appBucket = this.mUsageStats.getAppStandbyBucket(p.getKey());
        boolean changeable = true;
        p.setSummary(res.getString(R.string.standby_bucket_summary, new Object[]{bucketToName(appBucket)}));
        if (appBucket < 10 || appBucket > 40) {
            changeable = false;
        }
        if (changeable) {
            p.setValue(Integer.toString(appBucket));
        }
        p.setEnabled(changeable);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mUsageStats.setAppStandbyBucket(preference.getKey(), Integer.parseInt((String) newValue));
        updateSummary((ListPreference) preference);
        return false;
    }
}
