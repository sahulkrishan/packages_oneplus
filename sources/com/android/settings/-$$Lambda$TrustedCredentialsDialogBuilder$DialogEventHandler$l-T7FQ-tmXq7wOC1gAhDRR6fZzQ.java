package com.android.settings;

import java.util.function.IntConsumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$TrustedCredentialsDialogBuilder$DialogEventHandler$l-T7FQ-tmXq7wOC1gAhDRR6fZzQ implements IntConsumer {
    private final /* synthetic */ DialogEventHandler f$0;

    public /* synthetic */ -$$Lambda$TrustedCredentialsDialogBuilder$DialogEventHandler$l-T7FQ-tmXq7wOC1gAhDRR6fZzQ(DialogEventHandler dialogEventHandler) {
        this.f$0 = dialogEventHandler;
    }

    public final void accept(int i) {
        this.f$0.onCredentialConfirmed(i);
    }
}
