package com.android.settings.notification;

import android.app.NotificationChannelGroup;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AppNotificationSettings$KKPiatF9s2jsC7BTjM3YfK_E8S4 implements OnPreferenceClickListener {
    private final /* synthetic */ AppNotificationSettings f$0;
    private final /* synthetic */ NotificationChannelGroup f$1;

    public /* synthetic */ -$$Lambda$AppNotificationSettings$KKPiatF9s2jsC7BTjM3YfK_E8S4(AppNotificationSettings appNotificationSettings, NotificationChannelGroup notificationChannelGroup) {
        this.f$0 = appNotificationSettings;
        this.f$1 = notificationChannelGroup;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return AppNotificationSettings.lambda$populateGroupToggle$0(this.f$0, this.f$1, preference);
    }
}
