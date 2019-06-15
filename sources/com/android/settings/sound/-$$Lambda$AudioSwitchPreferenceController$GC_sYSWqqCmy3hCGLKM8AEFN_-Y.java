package com.android.settings.sound;

import com.android.settings.bluetooth.Utils;
import java.util.concurrent.Callable;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AudioSwitchPreferenceController$GC_sYSWqqCmy3hCGLKM8AEFN_-Y implements Callable {
    private final /* synthetic */ AudioSwitchPreferenceController f$0;

    public /* synthetic */ -$$Lambda$AudioSwitchPreferenceController$GC_sYSWqqCmy3hCGLKM8AEFN_-Y(AudioSwitchPreferenceController audioSwitchPreferenceController) {
        this.f$0 = audioSwitchPreferenceController;
    }

    public final Object call() {
        return Utils.getLocalBtManager(this.f$0.mContext);
    }
}
