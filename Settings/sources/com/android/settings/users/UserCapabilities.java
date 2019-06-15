package com.android.settings.users;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class UserCapabilities {
    boolean mCanAddGuest;
    boolean mCanAddRestrictedProfile = true;
    boolean mCanAddUser = true;
    boolean mDisallowAddUser;
    boolean mDisallowAddUserSetByAdmin;
    boolean mDisallowSwitchUser;
    boolean mEnabled = true;
    EnforcedAdmin mEnforcedAdmin;
    boolean mIsAdmin;
    boolean mIsGuest;

    private UserCapabilities() {
    }

    public static UserCapabilities create(Context context) {
        UserManager userManager = (UserManager) context.getSystemService("user");
        UserCapabilities caps = new UserCapabilities();
        if (!UserManager.supportsMultipleUsers() || Utils.isMonkeyRunning()) {
            caps.mEnabled = false;
            return caps;
        }
        UserInfo myUserInfo = userManager.getUserInfo(UserHandle.myUserId());
        caps.mIsGuest = myUserInfo.isGuest();
        caps.mIsAdmin = myUserInfo.isAdmin();
        if (((DevicePolicyManager) context.getSystemService("device_policy")).isDeviceManaged() || Utils.isVoiceCapable(context)) {
            caps.mCanAddRestrictedProfile = false;
        }
        caps.updateAddUserCapabilities(context);
        return caps;
    }

    public void updateAddUserCapabilities(Context context) {
        this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_add_user", UserHandle.myUserId());
        boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(context, "no_add_user", UserHandle.myUserId());
        boolean z = false;
        boolean z2 = (this.mEnforcedAdmin == null || hasBaseUserRestriction) ? false : true;
        this.mDisallowAddUserSetByAdmin = z2;
        z2 = this.mEnforcedAdmin != null || hasBaseUserRestriction;
        this.mDisallowAddUser = z2;
        this.mCanAddUser = true;
        if (!this.mIsAdmin || UserManager.getMaxSupportedUsers() < 2 || !UserManager.supportsMultipleUsers() || this.mDisallowAddUser) {
            this.mCanAddUser = false;
        }
        z2 = this.mIsAdmin || Global.getInt(context.getContentResolver(), "add_users_when_locked", 0) == 1;
        if (!(this.mIsGuest || this.mDisallowAddUser || !z2)) {
            z = true;
        }
        this.mCanAddGuest = z;
        this.mDisallowSwitchUser = ((UserManager) context.getSystemService("user")).hasUserRestriction("no_user_switch");
    }

    public boolean isAdmin() {
        return this.mIsAdmin;
    }

    public boolean disallowAddUser() {
        return this.mDisallowAddUser;
    }

    public boolean disallowAddUserSetByAdmin() {
        return this.mDisallowAddUserSetByAdmin;
    }

    public EnforcedAdmin getEnforcedAdmin() {
        return this.mEnforcedAdmin;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UserCapabilities{mEnabled=");
        stringBuilder.append(this.mEnabled);
        stringBuilder.append(", mCanAddUser=");
        stringBuilder.append(this.mCanAddUser);
        stringBuilder.append(", mCanAddRestrictedProfile=");
        stringBuilder.append(this.mCanAddRestrictedProfile);
        stringBuilder.append(", mIsAdmin=");
        stringBuilder.append(this.mIsAdmin);
        stringBuilder.append(", mIsGuest=");
        stringBuilder.append(this.mIsGuest);
        stringBuilder.append(", mCanAddGuest=");
        stringBuilder.append(this.mCanAddGuest);
        stringBuilder.append(", mDisallowAddUser=");
        stringBuilder.append(this.mDisallowAddUser);
        stringBuilder.append(", mEnforcedAdmin=");
        stringBuilder.append(this.mEnforcedAdmin);
        stringBuilder.append(", mDisallowSwitchUser=");
        stringBuilder.append(this.mDisallowSwitchUser);
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
