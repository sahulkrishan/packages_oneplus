package com.android.settings.accounts;

import android.content.SyncStatusObserver;
import com.android.settingslib.utils.ThreadUtils;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AccountPreferenceBase$duCjsGZhZVNysJ2Rj1t7N9PkFAY implements SyncStatusObserver {
    private final /* synthetic */ AccountPreferenceBase f$0;

    public /* synthetic */ -$$Lambda$AccountPreferenceBase$duCjsGZhZVNysJ2Rj1t7N9PkFAY(AccountPreferenceBase accountPreferenceBase) {
        this.f$0 = accountPreferenceBase;
    }

    public final void onStatusChanged(int i) {
        ThreadUtils.postOnMainThread(new -$$Lambda$AccountPreferenceBase$7XBpqCguERDVZFsa_jC8V8rk8o8(this.f$0));
    }
}
