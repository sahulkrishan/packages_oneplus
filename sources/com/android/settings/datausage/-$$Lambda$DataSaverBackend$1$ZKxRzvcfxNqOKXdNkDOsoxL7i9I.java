package com.android.settings.datausage;

import com.android.settings.datausage.DataSaverBackend.AnonymousClass1;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DataSaverBackend$1$ZKxRzvcfxNqOKXdNkDOsoxL7i9I implements Runnable {
    private final /* synthetic */ AnonymousClass1 f$0;
    private final /* synthetic */ int f$1;
    private final /* synthetic */ int f$2;

    public /* synthetic */ -$$Lambda$DataSaverBackend$1$ZKxRzvcfxNqOKXdNkDOsoxL7i9I(AnonymousClass1 anonymousClass1, int i, int i2) {
        this.f$0 = anonymousClass1;
        this.f$1 = i;
        this.f$2 = i2;
    }

    public final void run() {
        DataSaverBackend.this.handleUidPoliciesChanged(this.f$1, this.f$2);
    }
}
