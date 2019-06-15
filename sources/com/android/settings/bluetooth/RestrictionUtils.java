package com.android.settings.bluetooth;

import android.content.Context;
import android.os.UserHandle;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class RestrictionUtils {
    public EnforcedAdmin checkIfRestrictionEnforced(Context context, String restriction) {
        return RestrictedLockUtils.checkIfRestrictionEnforced(context, restriction, UserHandle.myUserId());
    }
}
