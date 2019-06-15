package com.android.settings.network;

import android.content.ContentValues;
import android.net.Uri;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ApnEditor$1vSLgWOnd4pMuFU2qFaSz0HXNw8 implements Runnable {
    private final /* synthetic */ ApnEditor f$0;
    private final /* synthetic */ Uri f$1;
    private final /* synthetic */ ContentValues f$2;

    public /* synthetic */ -$$Lambda$ApnEditor$1vSLgWOnd4pMuFU2qFaSz0HXNw8(ApnEditor apnEditor, Uri uri, ContentValues contentValues) {
        this.f$0 = apnEditor;
        this.f$1 = uri;
        this.f$2 = contentValues;
    }

    public final void run() {
        ApnEditor.lambda$updateApnDataToDatabase$0(this.f$0, this.f$1, this.f$2);
    }
}
