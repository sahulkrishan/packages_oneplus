package com.android.settings.applications.appinfo;

import android.content.Context;
import android.os.UserHandle;
import com.android.settings.applications.defaultapps.DefaultBrowserPreferenceController;

public class DefaultBrowserShortcutPreferenceController extends DefaultAppShortcutPreferenceControllerBase {
    private static final String KEY = "default_browser";

    public DefaultBrowserShortcutPreferenceController(Context context, String packageName) {
        super(context, KEY, packageName);
    }

    /* Access modifiers changed, original: protected */
    public boolean hasAppCapability() {
        return DefaultBrowserPreferenceController.hasBrowserPreference(this.mPackageName, this.mContext);
    }

    /* Access modifiers changed, original: protected */
    public boolean isDefaultApp() {
        return new DefaultBrowserPreferenceController(this.mContext).isBrowserDefault(this.mPackageName, UserHandle.myUserId());
    }
}
