package com.android.settings.applications.appinfo;

import android.content.Context;
import com.android.settings.applications.defaultapps.DefaultEmergencyPreferenceController;

public class DefaultEmergencyShortcutPreferenceController extends DefaultAppShortcutPreferenceControllerBase {
    private static final String KEY = "default_emergency_app";

    public DefaultEmergencyShortcutPreferenceController(Context context, String packageName) {
        super(context, KEY, packageName);
    }

    /* Access modifiers changed, original: protected */
    public boolean hasAppCapability() {
        return DefaultEmergencyPreferenceController.hasEmergencyPreference(this.mPackageName, this.mContext);
    }

    /* Access modifiers changed, original: protected */
    public boolean isDefaultApp() {
        return DefaultEmergencyPreferenceController.isEmergencyDefault(this.mPackageName, this.mContext);
    }
}
