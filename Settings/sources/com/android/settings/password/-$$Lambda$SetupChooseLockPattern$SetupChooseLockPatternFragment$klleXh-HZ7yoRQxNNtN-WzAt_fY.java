package com.android.settings.password;

import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.password.SetupChooseLockPattern.SetupChooseLockPatternFragment;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$klleXh-HZ7yoRQxNNtN-WzAt_fY implements OnClickListener {
    private final /* synthetic */ SetupChooseLockPatternFragment f$0;

    public /* synthetic */ -$$Lambda$SetupChooseLockPattern$SetupChooseLockPatternFragment$klleXh-HZ7yoRQxNNtN-WzAt_fY(SetupChooseLockPatternFragment setupChooseLockPatternFragment) {
        this.f$0 = setupChooseLockPatternFragment;
    }

    public final void onClick(View view) {
        SetupSkipDialog.newInstance(this.f$0.getActivity().getIntent().getBooleanExtra(SetupSkipDialog.EXTRA_FRP_SUPPORTED, false)).show(this.f$0.getFragmentManager());
    }
}
