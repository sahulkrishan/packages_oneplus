package com.android.settings.applications;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RecentAppsPreferenceController$benLpqwf0HURWhX82bB7mmwJ8Oo implements OnPreferenceClickListener {
    private final /* synthetic */ RecentAppsPreferenceController f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ AppEntry f$2;

    public /* synthetic */ -$$Lambda$RecentAppsPreferenceController$benLpqwf0HURWhX82bB7mmwJ8Oo(RecentAppsPreferenceController recentAppsPreferenceController, String str, AppEntry appEntry) {
        this.f$0 = recentAppsPreferenceController;
        this.f$1 = str;
        this.f$2 = appEntry;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return AppInfoBase.startAppInfoFragment(AppInfoDashboardFragment.class, R.string.application_info_label, this.f$1, this.f$2.info.uid, this.f$0.mHost, 1001, 748);
    }
}
