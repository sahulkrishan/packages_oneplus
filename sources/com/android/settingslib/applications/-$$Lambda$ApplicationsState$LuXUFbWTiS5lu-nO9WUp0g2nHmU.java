package com.android.settingslib.applications;

import com.android.settingslib.applications.ApplicationsState.AppEntry;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ApplicationsState$LuXUFbWTiS5lu-nO9WUp0g2nHmU implements Runnable {
    private final /* synthetic */ ApplicationsState f$0;
    private final /* synthetic */ AppEntry f$1;
    private final /* synthetic */ String f$2;
    private final /* synthetic */ int f$3;

    public /* synthetic */ -$$Lambda$ApplicationsState$LuXUFbWTiS5lu-nO9WUp0g2nHmU(ApplicationsState applicationsState, AppEntry appEntry, String str, int i) {
        this.f$0 = applicationsState;
        this.f$1 = appEntry;
        this.f$2 = str;
        this.f$3 = i;
    }

    public final void run() {
        ApplicationsState.lambda$requestSize$0(this.f$0, this.f$1, this.f$2, this.f$3);
    }
}
