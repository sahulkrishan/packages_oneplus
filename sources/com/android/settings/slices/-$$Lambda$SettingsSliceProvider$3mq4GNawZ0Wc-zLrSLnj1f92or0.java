package com.android.settings.slices;

import android.net.Uri;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SettingsSliceProvider$3mq4GNawZ0Wc-zLrSLnj1f92or0 implements Runnable {
    private final /* synthetic */ SettingsSliceProvider f$0;
    private final /* synthetic */ Uri f$1;

    public /* synthetic */ -$$Lambda$SettingsSliceProvider$3mq4GNawZ0Wc-zLrSLnj1f92or0(SettingsSliceProvider settingsSliceProvider, Uri uri) {
        this.f$0 = settingsSliceProvider;
        this.f$1 = uri;
    }

    public final void run() {
        SettingsSliceProvider.lambda$loadSliceInBackground$0(this.f$0, this.f$1);
    }
}
