package com.android.settings.applications.appinfo;

import android.content.Context;
import com.android.settings.applications.defaultapps.DefaultHomePreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class DefaultHomeShortcutPreferenceController extends DefaultAppShortcutPreferenceControllerBase {
    private static final String KEY = "default_home";

    public DefaultHomeShortcutPreferenceController(Context context, String packageName) {
        super(context, KEY, packageName);
    }

    /* Access modifiers changed, original: protected */
    public boolean hasAppCapability() {
        return DefaultHomePreferenceController.hasHomePreference(this.mPackageName, this.mContext);
    }

    /* Access modifiers changed, original: protected */
    public boolean isDefaultApp() {
        return DefaultHomePreferenceController.isHomeDefault(this.mPackageName, new PackageManagerWrapper(this.mContext.getPackageManager()));
    }
}
