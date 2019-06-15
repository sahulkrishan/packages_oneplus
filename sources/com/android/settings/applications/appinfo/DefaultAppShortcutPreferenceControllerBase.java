package com.android.settings.applications.appinfo;

import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.applications.DefaultAppSettings;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;

public abstract class DefaultAppShortcutPreferenceControllerBase extends BasePreferenceController {
    protected final String mPackageName;

    public abstract boolean hasAppCapability();

    public abstract boolean isDefaultApp();

    public DefaultAppShortcutPreferenceControllerBase(Context context, String preferenceKey, String packageName) {
        super(context, preferenceKey);
        this.mPackageName = packageName;
    }

    public int getAvailabilityStatus() {
        if (Utils.isManagedProfile(UserManager.get(this.mContext))) {
            return 3;
        }
        return hasAppCapability() ? 0 : 2;
    }

    public CharSequence getSummary() {
        return this.mContext.getText(isDefaultApp() ? R.string.yes : R.string.no);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(this.mPreferenceKey, preference.getKey())) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY, this.mPreferenceKey);
        new SubSettingLauncher(this.mContext).setDestination(DefaultAppSettings.class.getName()).setArguments(bundle).setTitle((int) R.string.configure_apps).setSourceMetricsCategory(0).launch();
        return true;
    }
}
