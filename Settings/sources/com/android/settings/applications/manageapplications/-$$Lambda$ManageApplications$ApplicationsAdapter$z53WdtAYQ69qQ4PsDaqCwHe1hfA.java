package com.android.settings.applications.manageapplications;

import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManageApplications$ApplicationsAdapter$z53WdtAYQ69qQ4PsDaqCwHe1hfA implements Runnable {
    private final /* synthetic */ ApplicationsAdapter f$0;
    private final /* synthetic */ AppFilter f$1;
    private final /* synthetic */ Comparator f$2;

    public /* synthetic */ -$$Lambda$ManageApplications$ApplicationsAdapter$z53WdtAYQ69qQ4PsDaqCwHe1hfA(ApplicationsAdapter applicationsAdapter, AppFilter appFilter, Comparator comparator) {
        this.f$0 = applicationsAdapter;
        this.f$1 = appFilter;
        this.f$2 = comparator;
    }

    public final void run() {
        ApplicationsAdapter.lambda$rebuild$3(this.f$0, this.f$1, this.f$2);
    }
}
