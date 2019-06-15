package com.android.settings.applications.appinfo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment.Callback;
import com.android.settings.core.BasePreferenceController;

public abstract class AppInfoPreferenceControllerBase extends BasePreferenceController implements Callback {
    private final Class<? extends SettingsPreferenceFragment> mDetailFragmentClass = getDetailFragmentClass();
    protected AppInfoDashboardFragment mParent;
    protected Preference mPreference;

    public AppInfoPreferenceControllerBase(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), this.mPreferenceKey) || this.mDetailFragmentClass == null) {
            return false;
        }
        AppInfoDashboardFragment.startAppInfoFragment(this.mDetailFragmentClass, -1, getArguments(), this.mParent, this.mParent.getAppEntry());
        return true;
    }

    public void refreshUi() {
        updateState(this.mPreference);
    }

    public void setParentFragment(AppInfoDashboardFragment parent) {
        this.mParent = parent;
        parent.addToCallbackList(this);
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public Bundle getArguments() {
        return null;
    }
}
