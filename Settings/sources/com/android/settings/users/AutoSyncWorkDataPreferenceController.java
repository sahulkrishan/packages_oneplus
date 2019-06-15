package com.android.settings.users;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.settings.Utils;
import java.util.List;

public class AutoSyncWorkDataPreferenceController extends AutoSyncPersonalDataPreferenceController {
    public static final String KEY_AUTO_SYNC_WORK_ACCOUNT = "auto_sync_work_account_data";
    private static final String TAG = "AutoSyncWorkData";

    public AutoSyncWorkDataPreferenceController(Context context, Fragment parent) {
        super(context, parent);
        this.mUserHandle = Utils.getManagedProfileWithDisabled(this.mUserManager);
    }

    public String getPreferenceKey() {
        return KEY_AUTO_SYNC_WORK_ACCOUNT;
    }

    public boolean isAvailable() {
        List<UserInfo> profiles = this.mUserManager.getProfiles(UserHandle.myUserId());
        int profilesCount = profiles.size();
        boolean isMultiAppEnable = Utils.isMultiAppEnable(profiles);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("profile size:");
        stringBuilder.append(profilesCount);
        stringBuilder.append(", isMultiAppEnable:");
        stringBuilder.append(isMultiAppEnable);
        Log.i("wils-debug", stringBuilder.toString());
        if (!(Utils.isManagedProfile(this.mUserManager) || this.mUserManager.isLinkedUser())) {
            if (isMultiAppEnable) {
                if (profilesCount > 2) {
                    return true;
                }
            } else if (profilesCount > 1) {
                return true;
            }
        }
        return false;
    }
}
