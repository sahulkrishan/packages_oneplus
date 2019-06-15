package com.android.settings.users;

import android.app.Fragment;
import android.content.Context;
import android.os.UserHandle;

public class AutoSyncPersonalDataPreferenceController extends AutoSyncDataPreferenceController {
    private static final String KEY_AUTO_SYNC_PERSONAL_ACCOUNT = "auto_sync_personal_account_data";
    private static final String TAG = "AutoSyncPersonalData";

    public AutoSyncPersonalDataPreferenceController(Context context, Fragment parent) {
        super(context, parent);
    }

    public boolean isAvailable() {
        if (this.mUserManager.isManagedProfile() || this.mUserManager.isLinkedUser() || this.mUserManager.getProfiles(UserHandle.myUserId()).size() <= 1) {
            return false;
        }
        return true;
    }

    public String getPreferenceKey() {
        return KEY_AUTO_SYNC_PERSONAL_ACCOUNT;
    }
}
