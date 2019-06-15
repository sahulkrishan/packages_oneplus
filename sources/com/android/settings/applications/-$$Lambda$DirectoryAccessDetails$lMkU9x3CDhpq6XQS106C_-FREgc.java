package com.android.settings.applications;

import android.content.Context;
import android.net.Uri;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import java.util.Set;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DirectoryAccessDetails$lMkU9x3CDhpq6XQS106C_-FREgc implements OnPreferenceChangeListener {
    private final /* synthetic */ DirectoryAccessDetails f$0;
    private final /* synthetic */ Context f$1;
    private final /* synthetic */ Uri f$2;
    private final /* synthetic */ String f$3;
    private final /* synthetic */ String f$4;
    private final /* synthetic */ Set f$5;

    public /* synthetic */ -$$Lambda$DirectoryAccessDetails$lMkU9x3CDhpq6XQS106C_-FREgc(DirectoryAccessDetails directoryAccessDetails, Context context, Uri uri, String str, String str2, Set set) {
        this.f$0 = directoryAccessDetails;
        this.f$1 = context;
        this.f$2 = uri;
        this.f$3 = str;
        this.f$4 = str2;
        this.f$5 = set;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return DirectoryAccessDetails.lambda$newPreference$1(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, preference, obj);
    }
}
