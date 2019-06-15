package com.android.settings.vpn2;

import com.android.internal.util.Preconditions;
import java.util.Objects;

class AppVpnInfo implements Comparable {
    public final String packageName;
    public final int userId;

    public AppVpnInfo(int userId, String packageName) {
        this.userId = userId;
        this.packageName = (String) Preconditions.checkNotNull(packageName);
    }

    public int compareTo(Object other) {
        AppVpnInfo that = (AppVpnInfo) other;
        int result = this.packageName.compareTo(that.packageName);
        if (result == 0) {
            return that.userId - this.userId;
        }
        return result;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof AppVpnInfo)) {
            return false;
        }
        AppVpnInfo that = (AppVpnInfo) other;
        if (this.userId == that.userId && Objects.equals(this.packageName, that.packageName)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.packageName, Integer.valueOf(this.userId)});
    }
}
