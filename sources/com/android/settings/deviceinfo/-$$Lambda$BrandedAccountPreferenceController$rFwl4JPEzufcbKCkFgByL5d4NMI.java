package com.android.settings.deviceinfo;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.accounts.AccountFeatureProvider;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BrandedAccountPreferenceController$rFwl4JPEzufcbKCkFgByL5d4NMI implements OnPreferenceClickListener {
    private final /* synthetic */ BrandedAccountPreferenceController f$0;
    private final /* synthetic */ AccountFeatureProvider f$1;

    public /* synthetic */ -$$Lambda$BrandedAccountPreferenceController$rFwl4JPEzufcbKCkFgByL5d4NMI(BrandedAccountPreferenceController brandedAccountPreferenceController, AccountFeatureProvider accountFeatureProvider) {
        this.f$0 = brandedAccountPreferenceController;
        this.f$1 = accountFeatureProvider;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return BrandedAccountPreferenceController.lambda$displayPreference$0(this.f$0, this.f$1, preference);
    }
}
