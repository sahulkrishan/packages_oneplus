package com.android.settings.password;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.List;

public class ChooseLockGenericController {
    private final Context mContext;
    private DevicePolicyManager mDpm;
    private ManagedLockPasswordProvider mManagedPasswordProvider;
    private final int mUserId;

    public ChooseLockGenericController(Context context, int userId) {
        this(context, userId, (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class), ManagedLockPasswordProvider.get(context, userId));
    }

    @VisibleForTesting
    ChooseLockGenericController(Context context, int userId, DevicePolicyManager dpm, ManagedLockPasswordProvider managedLockPasswordProvider) {
        this.mContext = context;
        this.mUserId = userId;
        this.mManagedPasswordProvider = managedLockPasswordProvider;
        this.mDpm = dpm;
    }

    public int upgradeQuality(int quality) {
        return Math.max(quality, this.mDpm.getPasswordQuality(null, this.mUserId));
    }

    public boolean isScreenLockVisible(ScreenLockType type) {
        boolean z = true;
        switch (type) {
            case NONE:
                return this.mContext.getResources().getBoolean(R.bool.config_hide_none_security_option) ^ 1;
            case SWIPE:
                if (this.mContext.getResources().getBoolean(R.bool.config_hide_swipe_security_option) || this.mUserId != UserHandle.myUserId()) {
                    z = false;
                }
                return z;
            case MANAGED:
                return this.mManagedPasswordProvider.isManagedPasswordChoosable();
            default:
                return true;
        }
    }

    public boolean isScreenLockEnabled(ScreenLockType type, int quality) {
        return type.maxQuality >= quality;
    }

    public boolean isScreenLockDisabledByAdmin(ScreenLockType type, int adminEnforcedQuality) {
        boolean z = false;
        boolean disabledByAdmin = type.maxQuality < adminEnforcedQuality;
        if (type != ScreenLockType.MANAGED) {
            return disabledByAdmin;
        }
        if (disabledByAdmin || !this.mManagedPasswordProvider.isManagedPasswordChoosable()) {
            z = true;
        }
        return z;
    }

    public CharSequence getTitle(ScreenLockType type) {
        switch (type) {
            case NONE:
                return this.mContext.getText(R.string.unlock_set_unlock_off_title);
            case SWIPE:
                return this.mContext.getText(R.string.unlock_set_unlock_none_title);
            case MANAGED:
                return this.mManagedPasswordProvider.getPickerOptionTitle(false);
            case PATTERN:
                return this.mContext.getText(R.string.unlock_set_unlock_pattern_title);
            case PIN:
                return this.mContext.getText(R.string.unlock_set_unlock_pin_title);
            case PASSWORD:
                return this.mContext.getText(R.string.unlock_set_unlock_password_title);
            default:
                return null;
        }
    }

    @NonNull
    public List<ScreenLockType> getVisibleScreenLockTypes(int quality, boolean includeDisabled) {
        int upgradedQuality = upgradeQuality(quality);
        List<ScreenLockType> locks = new ArrayList();
        for (ScreenLockType lock : ScreenLockType.values()) {
            if (isScreenLockVisible(lock) && (includeDisabled || isScreenLockEnabled(lock, upgradedQuality))) {
                locks.add(lock);
            }
        }
        return locks;
    }
}
