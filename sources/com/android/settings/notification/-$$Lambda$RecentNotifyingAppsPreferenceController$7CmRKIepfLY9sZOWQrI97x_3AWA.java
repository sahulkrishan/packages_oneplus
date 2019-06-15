package com.android.settings.notification;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RecentNotifyingAppsPreferenceController$7CmRKIepfLY9sZOWQrI97x_3AWA implements OnPreferenceChangeListener {
    private final /* synthetic */ RecentNotifyingAppsPreferenceController f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ AppEntry f$2;

    public /* synthetic */ -$$Lambda$RecentNotifyingAppsPreferenceController$7CmRKIepfLY9sZOWQrI97x_3AWA(RecentNotifyingAppsPreferenceController recentNotifyingAppsPreferenceController, String str, AppEntry appEntry) {
        this.f$0 = recentNotifyingAppsPreferenceController;
        this.f$1 = str;
        this.f$2 = appEntry;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return RecentNotifyingAppsPreferenceController.lambda$displayRecentApps$0(this.f$0, this.f$1, this.f$2, preference, obj);
    }
}
