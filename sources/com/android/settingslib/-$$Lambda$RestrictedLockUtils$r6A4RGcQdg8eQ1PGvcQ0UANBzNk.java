package com.android.settingslib;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedLockUtils$r6A4RGcQdg8eQ1PGvcQ0UANBzNk implements LockSettingCheck {
    private final /* synthetic */ int f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$RestrictedLockUtils$r6A4RGcQdg8eQ1PGvcQ0UANBzNk(int i, int i2) {
        this.f$0 = i;
        this.f$1 = i2;
    }

    public final boolean isEnforcing(DevicePolicyManager devicePolicyManager, ComponentName componentName, int i) {
        return RestrictedLockUtils.lambda$checkIfKeyguardFeaturesDisabled$0(this.f$0, this.f$1, devicePolicyManager, componentName, i);
    }
}
