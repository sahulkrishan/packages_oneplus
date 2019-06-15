package com.android.settings.accounts;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AccountPreferenceBase$7XBpqCguERDVZFsa_jC8V8rk8o8 implements Runnable {
    private final /* synthetic */ AccountPreferenceBase f$0;

    public /* synthetic */ -$$Lambda$AccountPreferenceBase$7XBpqCguERDVZFsa_jC8V8rk8o8(AccountPreferenceBase accountPreferenceBase) {
        this.f$0 = accountPreferenceBase;
    }

    public final void run() {
        this.f$0.onSyncStateUpdated();
    }
}
