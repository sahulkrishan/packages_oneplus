package com.android.settings.fingerprint;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$FingerprintStatusPreferenceController$KyR_IBe4qHxX_KvME9NBxSJFxFQ implements OnPreferenceClickListener {
    private final /* synthetic */ int f$0;
    private final /* synthetic */ String f$1;

    public /* synthetic */ -$$Lambda$FingerprintStatusPreferenceController$KyR_IBe4qHxX_KvME9NBxSJFxFQ(int i, String str) {
        this.f$0 = i;
        this.f$1 = str;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return FingerprintStatusPreferenceController.lambda$updateState$0(this.f$0, this.f$1, preference);
    }
}
