package com.android.settings.password;

import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.password.SetupChooseLockPattern.SetupChooseLockPatternFragment;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$oe1sL-LLbUw3chjlv8P3cpGYEWs implements OnClickListener {
    private final /* synthetic */ SetupChooseLockPatternFragment f$0;

    public /* synthetic */ -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$oe1sL-LLbUw3chjlv8P3cpGYEWs(SetupChooseLockPatternFragment setupChooseLockPatternFragment) {
        this.f$0 = setupChooseLockPatternFragment;
    }

    public final void onClick(View view) {
        ChooseLockTypeDialogFragment.newInstance(this.f$0.mUserId).show(this.f$0.getChildFragmentManager(), null);
    }
}
