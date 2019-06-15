package com.android.settings.applications;

import android.content.Context;
import android.net.Uri;
import android.support.v7.preference.PreferenceCategory;
import android.util.Pair;
import java.util.Set;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DirectoryAccessDetails$K0N0BhiTAIxLxuaXU9qwR-rLnAY implements Consumer {
    private final /* synthetic */ DirectoryAccessDetails f$0;
    private final /* synthetic */ Context f$1;
    private final /* synthetic */ String f$2;
    private final /* synthetic */ Uri f$3;
    private final /* synthetic */ ExternalVolume f$4;
    private final /* synthetic */ PreferenceCategory f$5;
    private final /* synthetic */ Set f$6;

    public /* synthetic */ -$$Lambda$DirectoryAccessDetails$K0N0BhiTAIxLxuaXU9qwR-rLnAY(DirectoryAccessDetails directoryAccessDetails, Context context, String str, Uri uri, ExternalVolume externalVolume, PreferenceCategory preferenceCategory, Set set) {
        this.f$0 = directoryAccessDetails;
        this.f$1 = context;
        this.f$2 = str;
        this.f$3 = uri;
        this.f$4 = externalVolume;
        this.f$5 = preferenceCategory;
        this.f$6 = set;
    }

    public final void accept(Object obj) {
        DirectoryAccessDetails.lambda$refreshUi$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (Pair) obj);
    }
}
