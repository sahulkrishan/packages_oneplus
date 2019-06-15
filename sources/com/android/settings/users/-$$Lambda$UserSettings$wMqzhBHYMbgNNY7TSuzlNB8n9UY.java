package com.android.settings.users;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$UserSettings$wMqzhBHYMbgNNY7TSuzlNB8n9UY implements OnPreferenceClickListener {
    private final /* synthetic */ UserSettings f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$UserSettings$wMqzhBHYMbgNNY7TSuzlNB8n9UY(UserSettings userSettings, int i) {
        this.f$0 = userSettings;
        this.f$1 = i;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return UserSettings.lambda$updateUserList$0(this.f$0, this.f$1, preference);
    }
}
