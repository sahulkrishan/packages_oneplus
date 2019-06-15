package com.android.settings.applications.appinfo;

import android.content.Context;
import com.android.settings.applications.defaultapps.DefaultSmsPreferenceController;

public class DefaultSmsShortcutPreferenceController extends DefaultAppShortcutPreferenceControllerBase {
    private static final String KEY = "default_sms_app";

    public DefaultSmsShortcutPreferenceController(Context context, String packageName) {
        super(context, KEY, packageName);
    }

    /* Access modifiers changed, original: protected */
    public boolean hasAppCapability() {
        return DefaultSmsPreferenceController.hasSmsPreference(this.mPackageName, this.mContext);
    }

    /* Access modifiers changed, original: protected */
    public boolean isDefaultApp() {
        return DefaultSmsPreferenceController.isSmsDefault(this.mPackageName, this.mContext);
    }
}
