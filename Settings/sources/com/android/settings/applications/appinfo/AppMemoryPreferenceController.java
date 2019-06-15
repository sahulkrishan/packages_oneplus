package com.android.settings.applications.appinfo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.ProcStatsData;
import com.android.settings.applications.ProcStatsEntry;
import com.android.settings.applications.ProcStatsPackageEntry;
import com.android.settings.applications.ProcessStatsBase;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import java.util.Iterator;

public class AppMemoryPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume {
    private static final String KEY_MEMORY = "memory";
    private final AppInfoDashboardFragment mParent;
    private Preference mPreference;
    private ProcStatsPackageEntry mStats;
    private ProcStatsData mStatsManager;

    private class MemoryUpdater extends AsyncTask<Void, Void, ProcStatsPackageEntry> {
        private MemoryUpdater() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public ProcStatsPackageEntry doInBackground(Void... params) {
            Activity activity = AppMemoryPreferenceController.this.mParent.getActivity();
            if (activity == null) {
                return null;
            }
            PackageInfo packageInfo = AppMemoryPreferenceController.this.mParent.getPackageInfo();
            if (packageInfo == null) {
                return null;
            }
            if (AppMemoryPreferenceController.this.mStatsManager == null) {
                AppMemoryPreferenceController.this.mStatsManager = new ProcStatsData(activity, false);
                AppMemoryPreferenceController.this.mStatsManager.setDuration(ProcessStatsBase.sDurations[0]);
            }
            AppMemoryPreferenceController.this.mStatsManager.refreshStats(true);
            for (ProcStatsPackageEntry pkgEntry : AppMemoryPreferenceController.this.mStatsManager.getEntries()) {
                Iterator it = pkgEntry.getEntries().iterator();
                while (it.hasNext()) {
                    if (((ProcStatsEntry) it.next()).getUid() == packageInfo.applicationInfo.uid) {
                        pkgEntry.updateMetrics();
                        return pkgEntry;
                    }
                }
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(ProcStatsPackageEntry entry) {
            if (AppMemoryPreferenceController.this.mParent.getActivity() != null) {
                if (entry != null) {
                    AppMemoryPreferenceController.this.mStats = entry;
                    AppMemoryPreferenceController.this.mPreference.setEnabled(true);
                    double amount = Math.max(entry.getRunWeight(), entry.getBgWeight()) * AppMemoryPreferenceController.this.mStatsManager.getMemInfo().getWeightToRam();
                    AppMemoryPreferenceController.this.mPreference.setSummary(AppMemoryPreferenceController.this.mContext.getString(R.string.memory_use_summary, new Object[]{Formatter.formatShortFileSize(AppMemoryPreferenceController.this.mContext, (long) amount)}));
                } else {
                    AppMemoryPreferenceController.this.mPreference.setEnabled(false);
                    AppMemoryPreferenceController.this.mPreference.setSummary(AppMemoryPreferenceController.this.mContext.getString(R.string.no_memory_use_summary));
                }
            }
        }
    }

    public AppMemoryPreferenceController(Context context, AppInfoDashboardFragment parent, Lifecycle lifecycle) {
        super(context, KEY_MEMORY);
        this.mParent = parent;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        if (!this.mContext.getResources().getBoolean(R.bool.config_show_app_info_settings_memory)) {
            return 2;
        }
        return DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mContext) ? 0 : 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_MEMORY.equals(preference.getKey())) {
            return false;
        }
        ProcessStatsBase.launchMemoryDetail((SettingsActivity) this.mParent.getActivity(), this.mStatsManager.getMemInfo(), this.mStats, false);
        return true;
    }

    public void onResume() {
        if (isAvailable()) {
            new MemoryUpdater().execute(new Void[0]);
        }
    }
}
