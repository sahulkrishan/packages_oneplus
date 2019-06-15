package com.android.settings.network;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import com.android.settingslib.RestrictedLockUtils;

public class NetworkResetRestrictionChecker {
    private final Context mContext;
    private final UserManager mUserManager;

    public NetworkResetRestrictionChecker(Context context) {
        this.mContext = context;
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting(otherwise = 2)
    public boolean hasUserBaseRestriction() {
        return RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_network_reset", UserHandle.myUserId());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting(otherwise = 2)
    public boolean isRestrictionEnforcedByAdmin() {
        return RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_network_reset", UserHandle.myUserId()) != null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasUserRestriction() {
        return !this.mUserManager.isAdminUser() || hasUserBaseRestriction();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasRestriction() {
        return hasUserRestriction() || isRestrictionEnforcedByAdmin();
    }
}
