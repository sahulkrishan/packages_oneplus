package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.os.UserHandle;
import com.android.settings.Utils;

public class DefaultWorkPhonePreferenceController extends DefaultPhonePreferenceController {
    public static final String KEY = "work_default_phone_app";
    private final UserHandle mUserHandle = Utils.getManagedProfile(this.mUserManager);

    public DefaultWorkPhonePreferenceController(Context context) {
        super(context);
        if (this.mUserHandle != null) {
            this.mUserId = this.mUserHandle.getIdentifier();
        }
    }

    public boolean isAvailable() {
        if (this.mUserHandle == null) {
            return false;
        }
        return super.isAvailable();
    }

    public String getPreferenceKey() {
        return KEY;
    }
}
