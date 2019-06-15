package com.android.settings.utils;

import com.android.settingslib.applications.ServiceListing.Callback;
import java.util.List;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManagedServiceSettings$6gJSYmD-m4iGVFUdlUroaoAptMw implements Callback {
    private final /* synthetic */ ManagedServiceSettings f$0;

    public /* synthetic */ -$$Lambda$ManagedServiceSettings$6gJSYmD-m4iGVFUdlUroaoAptMw(ManagedServiceSettings managedServiceSettings) {
        this.f$0 = managedServiceSettings;
    }

    public final void onServicesReloaded(List list) {
        this.f$0.updateList(list);
    }
}
