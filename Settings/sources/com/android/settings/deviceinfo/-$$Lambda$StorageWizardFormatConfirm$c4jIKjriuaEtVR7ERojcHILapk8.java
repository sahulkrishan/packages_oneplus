package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$StorageWizardFormatConfirm$c4jIKjriuaEtVR7ERojcHILapk8 implements OnClickListener {
    private final /* synthetic */ Context f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ String f$2;
    private final /* synthetic */ boolean f$3;

    public /* synthetic */ -$$Lambda$StorageWizardFormatConfirm$c4jIKjriuaEtVR7ERojcHILapk8(Context context, String str, String str2, boolean z) {
        this.f$0 = context;
        this.f$1 = str;
        this.f$2 = str2;
        this.f$3 = z;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        StorageWizardFormatConfirm.lambda$onCreateDialog$0(this.f$0, this.f$1, this.f$2, this.f$3, dialogInterface, i);
    }
}
