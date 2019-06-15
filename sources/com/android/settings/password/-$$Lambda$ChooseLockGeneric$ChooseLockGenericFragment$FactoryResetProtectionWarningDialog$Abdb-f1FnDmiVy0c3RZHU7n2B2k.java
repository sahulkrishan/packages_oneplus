package com.android.settings.password;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment.FactoryResetProtectionWarningDialog;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$Abdb-f1FnDmiVy0c3RZHU7n2B2k implements OnClickListener {
    private final /* synthetic */ FactoryResetProtectionWarningDialog f$0;
    private final /* synthetic */ Bundle f$1;

    public /* synthetic */ -$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$Abdb-f1FnDmiVy0c3RZHU7n2B2k(FactoryResetProtectionWarningDialog factoryResetProtectionWarningDialog, Bundle bundle) {
        this.f$0 = factoryResetProtectionWarningDialog;
        this.f$1 = bundle;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        ((ChooseLockGenericFragment) this.f$0.getParentFragment()).setUnlockMethod(this.f$1.getString(FactoryResetProtectionWarningDialog.ARG_UNLOCK_METHOD_TO_SET));
    }
}
