package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.widget.RestrictedAppPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.List;

public class LocationServicePreferenceController extends LocationBasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    @VisibleForTesting
    static final IntentFilter INTENT_FILTER_INJECTED_SETTING_CHANGED = new IntentFilter("android.location.InjectedSettingChanged");
    private static final String KEY_LOCATION_SERVICES = "location_services";
    private static final String TAG = "LocationServicePrefCtrl";
    private PreferenceCategory mCategoryLocationServices;
    private final LocationSettings mFragment;
    @VisibleForTesting
    BroadcastReceiver mInjectedSettingsReceiver;
    private final SettingsInjector mInjector;

    public LocationServicePreferenceController(Context context, LocationSettings fragment, Lifecycle lifecycle) {
        this(context, fragment, lifecycle, new SettingsInjector(context));
    }

    @VisibleForTesting
    LocationServicePreferenceController(Context context, LocationSettings fragment, Lifecycle lifecycle, SettingsInjector injector) {
        super(context, lifecycle);
        this.mFragment = fragment;
        this.mInjector = injector;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public String getPreferenceKey() {
        return KEY_LOCATION_SERVICES;
    }

    public boolean isAvailable() {
        return this.mInjector.hasInjectedSettings(this.mLocationEnabler.isManagedProfileRestrictedByBase() ? UserHandle.myUserId() : -2);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mCategoryLocationServices = (PreferenceCategory) screen.findPreference(KEY_LOCATION_SERVICES);
    }

    public void updateState(Preference preference) {
        this.mCategoryLocationServices.removeAll();
        List<Preference> prefs = getLocationServices();
        for (Preference pref : prefs) {
            if (pref instanceof RestrictedAppPreference) {
                ((RestrictedAppPreference) pref).checkRestrictionAndSetDisabled();
            }
        }
        LocationSettings.addPreferencesSorted(prefs, this.mCategoryLocationServices);
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
        this.mInjector.reloadStatusMessages();
    }

    public void onResume() {
        if (this.mInjectedSettingsReceiver == null) {
            this.mInjectedSettingsReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (Log.isLoggable(LocationServicePreferenceController.TAG, 3)) {
                        String str = LocationServicePreferenceController.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Received settings change intent: ");
                        stringBuilder.append(intent);
                        Log.d(str, stringBuilder.toString());
                    }
                    LocationServicePreferenceController.this.mInjector.reloadStatusMessages();
                }
            };
        }
        this.mContext.registerReceiver(this.mInjectedSettingsReceiver, INTENT_FILTER_INJECTED_SETTING_CHANGED);
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mInjectedSettingsReceiver);
    }

    private List<Preference> getLocationServices() {
        return this.mInjector.getInjectedSettings(this.mFragment.getPreferenceManager().getContext(), this.mLocationEnabler.isManagedProfileRestrictedByBase() ? UserHandle.myUserId() : -2);
    }
}
