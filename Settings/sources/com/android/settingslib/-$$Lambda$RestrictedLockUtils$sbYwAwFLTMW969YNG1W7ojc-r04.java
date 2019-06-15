package com.android.settingslib;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedLockUtils$sbYwAwFLTMW969YNG1W7ojc-r04 implements LockSettingCheck {
    public static final /* synthetic */ -$$Lambda$RestrictedLockUtils$sbYwAwFLTMW969YNG1W7ojc-r04 INSTANCE = new -$$Lambda$RestrictedLockUtils$sbYwAwFLTMW969YNG1W7ojc-r04();

    private /* synthetic */ -$$Lambda$RestrictedLockUtils$sbYwAwFLTMW969YNG1W7ojc-r04() {
    }

    public final boolean isEnforcing(DevicePolicyManager devicePolicyManager, ComponentName componentName, int i) {
        return RestrictedLockUtils.lambda$checkIfMaximumTimeToLockIsSet$2(devicePolicyManager, componentName, i);
    }
}
