package com.android.settings.utils;

import android.content.ComponentName;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManagedServiceSettings$qzumG4qfCDX22E2-mvpKDzSZyck implements OnPreferenceChangeListener {
    private final /* synthetic */ ManagedServiceSettings f$0;
    private final /* synthetic */ ComponentName f$1;
    private final /* synthetic */ String f$2;

    public /* synthetic */ -$$Lambda$ManagedServiceSettings$qzumG4qfCDX22E2-mvpKDzSZyck(ManagedServiceSettings managedServiceSettings, ComponentName componentName, String str) {
        this.f$0 = managedServiceSettings;
        this.f$1 = componentName;
        this.f$2 = str;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return this.f$0.setEnabled(this.f$1, this.f$2, ((Boolean) obj).booleanValue());
    }
}
