package com.android.settings.notification;

import android.provider.Settings.System;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SoundSettings$3WFktXqTAuUwKBOCajSDKQ9QhQc implements Runnable {
    private final /* synthetic */ SoundSettings f$0;

    public /* synthetic */ -$$Lambda$SoundSettings$3WFktXqTAuUwKBOCajSDKQ9QhQc(SoundSettings soundSettings) {
        this.f$0 = soundSettings;
    }

    public final void run() {
        System.putInt(this.f$0.getContentResolver(), SoundSettings.KEY_VIBRATE_WHEN_RINGING, this.f$0.mVibrateWhenRingingValue);
    }
}
