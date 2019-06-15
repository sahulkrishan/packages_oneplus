package com.android.settings.applications.appinfo;

import android.content.Context;
import com.android.settings.applications.defaultapps.DefaultPhonePreferenceController;

public class DefaultPhoneShortcutPreferenceController extends DefaultAppShortcutPreferenceControllerBase {
    private static final String KEY = "default_phone_app";

    public DefaultPhoneShortcutPreferenceController(Context context, String packageName) {
        super(context, KEY, packageName);
    }

    /* Access modifiers changed, original: protected */
    public boolean hasAppCapability() {
        return DefaultPhonePreferenceController.hasPhonePreference(this.mPackageName, this.mContext);
    }

    /* Access modifiers changed, original: protected */
    public boolean isDefaultApp() {
        return DefaultPhonePreferenceController.isPhoneDefault(this.mPackageName, this.mContext);
    }
}
