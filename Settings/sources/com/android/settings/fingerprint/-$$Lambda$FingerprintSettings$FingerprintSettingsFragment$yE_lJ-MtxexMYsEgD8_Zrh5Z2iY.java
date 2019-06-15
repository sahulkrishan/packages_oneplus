package com.android.settings.fingerprint;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$FingerprintSettings$FingerprintSettingsFragment$yE_lJ-MtxexMYsEgD8_Zrh5Z2iY implements OnClickListener {
    private final /* synthetic */ Activity f$0;
    private final /* synthetic */ EnforcedAdmin f$1;

    public /* synthetic */ -$$Lambda$FingerprintSettings$FingerprintSettingsFragment$yE_lJ-MtxexMYsEgD8_Zrh5Z2iY(Activity activity, EnforcedAdmin enforcedAdmin) {
        this.f$0 = activity;
        this.f$1 = enforcedAdmin;
    }

    public final void onClick(View view) {
        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.f$0, this.f$1);
    }
}
