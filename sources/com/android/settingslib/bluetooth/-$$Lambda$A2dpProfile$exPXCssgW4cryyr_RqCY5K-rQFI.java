package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothCodecConfig;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$A2dpProfile$exPXCssgW4cryyr_RqCY5K-rQFI implements Comparator {
    public static final /* synthetic */ -$$Lambda$A2dpProfile$exPXCssgW4cryyr_RqCY5K-rQFI INSTANCE = new -$$Lambda$A2dpProfile$exPXCssgW4cryyr_RqCY5K-rQFI();

    private /* synthetic */ -$$Lambda$A2dpProfile$exPXCssgW4cryyr_RqCY5K-rQFI() {
    }

    public final int compare(Object obj, Object obj2) {
        return (((BluetoothCodecConfig) obj2).getCodecPriority() - ((BluetoothCodecConfig) obj).getCodecPriority());
    }
}
