package com.android.settings.applications.manageapplications;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;

public class ResetAppPrefPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnSaveInstanceState {
    private ResetAppsHelper mResetAppsHelper;

    public ResetAppPrefPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mResetAppsHelper = new ResetAppsHelper(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        this.mResetAppsHelper.buildResetDialog();
        return true;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return "reset_app_prefs";
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mResetAppsHelper.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        this.mResetAppsHelper.onSaveInstanceState(outState);
    }
}
