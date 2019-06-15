package com.android.settings.applications;

import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.text.TextUtils;
import java.util.Objects;

public class UserAppInfo {
    public final ApplicationInfo appInfo;
    public final UserInfo userInfo;

    public UserAppInfo(UserInfo mUserInfo, ApplicationInfo mAppInfo) {
        this.userInfo = mUserInfo;
        this.appInfo = mAppInfo;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UserAppInfo that = (UserAppInfo) other;
        if (!(that.userInfo.id == this.userInfo.id && TextUtils.equals(that.appInfo.packageName, this.appInfo.packageName))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.userInfo.id), this.appInfo.packageName});
    }
}
