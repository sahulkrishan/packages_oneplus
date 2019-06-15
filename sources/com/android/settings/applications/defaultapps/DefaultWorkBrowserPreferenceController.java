package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.os.UserHandle;
import com.android.settings.Utils;

public class DefaultWorkBrowserPreferenceController extends DefaultBrowserPreferenceController {
    public static final String KEY = "work_default_browser";
    private final UserHandle mUserHandle = Utils.getManagedProfile(this.mUserManager);

    public DefaultWorkBrowserPreferenceController(Context context) {
        super(context);
        if (this.mUserHandle != null) {
            this.mUserId = this.mUserHandle.getIdentifier();
        }
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        if (this.mUserHandle == null) {
            return false;
        }
        return super.isAvailable();
    }
}
