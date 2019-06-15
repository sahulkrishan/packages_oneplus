package com.android.settings.security;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.TogglePreferenceController;

public class LockdownButtonPreferenceController extends TogglePreferenceController {
    private static final String KEY_LOCKDOWN_ENALBED = "security_setting_lockdown_enabled";
    private final LockPatternUtils mLockPatternUtils;

    public LockdownButtonPreferenceController(Context context) {
        super(context, KEY_LOCKDOWN_ENALBED);
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    public int getAvailabilityStatus() {
        if (this.mLockPatternUtils.isSecure(UserHandle.myUserId())) {
            return 0;
        }
        return 3;
    }

    public boolean isChecked() {
        return Secure.getInt(this.mContext.getContentResolver(), "lockdown_in_power_menu", 0) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        Secure.putInt(this.mContext.getContentResolver(), "lockdown_in_power_menu", isChecked);
        return true;
    }
}
