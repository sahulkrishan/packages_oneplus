package com.android.settings.applications;

import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManageDomainUrls$agHbI5vf9m7UaPnJCYH2ithkZhk implements OnPreferenceClickListener {
    private final /* synthetic */ ManageDomainUrls f$0;
    private final /* synthetic */ Intent f$1;

    public /* synthetic */ -$$Lambda$ManageDomainUrls$agHbI5vf9m7UaPnJCYH2ithkZhk(ManageDomainUrls manageDomainUrls, Intent intent) {
        this.f$0 = manageDomainUrls;
        this.f$1 = intent;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return ManageDomainUrls.lambda$onRebuildComplete$0(this.f$0, this.f$1, preference);
    }
}
