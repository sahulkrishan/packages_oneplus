package com.android.settings.applications.manageapplications;

import java.util.ArrayList;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManageApplications$ApplicationsAdapter$u5dJjouyXSv-EkAyCVJkIO0SEV0 implements Runnable {
    private final /* synthetic */ ApplicationsAdapter f$0;
    private final /* synthetic */ ArrayList f$1;

    public /* synthetic */ -$$Lambda$ManageApplications$ApplicationsAdapter$u5dJjouyXSv-EkAyCVJkIO0SEV0(ApplicationsAdapter applicationsAdapter, ArrayList arrayList) {
        this.f$0 = applicationsAdapter;
        this.f$1 = arrayList;
    }

    public final void run() {
        this.f$0.onRebuildComplete(this.f$1);
    }
}
