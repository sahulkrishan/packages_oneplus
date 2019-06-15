package com.android.settingslib;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedLockUtils$ZGpdJ-Goya42TrXyPazgpDXw5os implements LockSettingCheck {
    public static final /* synthetic */ -$$Lambda$RestrictedLockUtils$ZGpdJ-Goya42TrXyPazgpDXw5os INSTANCE = new -$$Lambda$RestrictedLockUtils$ZGpdJ-Goya42TrXyPazgpDXw5os();

    private /* synthetic */ -$$Lambda$RestrictedLockUtils$ZGpdJ-Goya42TrXyPazgpDXw5os() {
    }

    public final boolean isEnforcing(DevicePolicyManager devicePolicyManager, ComponentName componentName, int i) {
        return RestrictedLockUtils.lambda$checkIfPasswordQualityIsSet$1(devicePolicyManager, componentName, i);
    }
}
