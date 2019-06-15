package com.android.settings.password;

import android.content.Intent;
import com.android.settings.password.ConfirmLockPassword.ConfirmLockPasswordFragment;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ConfirmLockPassword$ConfirmLockPasswordFragment$hwD4uLqRx_u_wyU3V7MV_afxC5o implements Runnable {
    private final /* synthetic */ ConfirmLockPassword f$0;
    private final /* synthetic */ Intent f$1;

    public /* synthetic */ -$$Lambda$ConfirmLockPassword$ConfirmLockPasswordFragment$hwD4uLqRx_u_wyU3V7MV_afxC5o(ConfirmLockPassword confirmLockPassword, Intent intent) {
        this.f$0 = confirmLockPassword;
        this.f$1 = intent;
    }

    public final void run() {
        ConfirmLockPasswordFragment.lambda$startDisappearAnimation$0(this.f$0, this.f$1);
    }
}
