package com.android.settings.datausage;

import com.android.settings.datausage.DataSaverBackend.AnonymousClass1;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DataSaverBackend$1$1851XOwRm2qYDEpp81v4WIVwIHs implements Runnable {
    private final /* synthetic */ AnonymousClass1 f$0;
    private final /* synthetic */ boolean f$1;

    public /* synthetic */ -$$Lambda$DataSaverBackend$1$1851XOwRm2qYDEpp81v4WIVwIHs(AnonymousClass1 anonymousClass1, boolean z) {
        this.f$0 = anonymousClass1;
        this.f$1 = z;
    }

    public final void run() {
        DataSaverBackend.this.handleRestrictBackgroundChanged(this.f$1);
    }
}
