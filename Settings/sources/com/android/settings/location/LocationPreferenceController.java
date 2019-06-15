package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineListPayload;
import com.android.settings.search.ResultPayload;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class LocationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_LOCATION = "location";
    private Context mContext;
    @VisibleForTesting
    BroadcastReceiver mLocationProvidersChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
                LocationPreferenceController.this.updateSummary();
            }
        }
    };
    private Preference mPreference;

    public LocationPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mContext = context;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY_LOCATION);
    }

    public void onResume() {
        if (this.mLocationProvidersChangedReceiver != null) {
            this.mContext.registerReceiver(this.mLocationProvidersChangedReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
        }
    }

    public void onPause() {
        if (this.mLocationProvidersChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mLocationProvidersChangedReceiver);
        }
    }

    public void updateState(Preference preference) {
        preference.setSummary(getLocationSummary(this.mContext));
    }

    public String getPreferenceKey() {
        return KEY_LOCATION;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateSummary() {
        updateState(this.mPreference);
    }

    public static String getLocationSummary(Context context) {
        if (Secure.getInt(context.getContentResolver(), "location_mode", 0) != 0) {
            return context.getString(R.string.location_on_summary);
        }
        return context.getString(R.string.location_off_summary);
    }

    public ResultPayload getResultPayload() {
        return new InlineListPayload("location_mode", 2, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, LocationSettings.class.getName(), KEY_LOCATION, this.mContext.getString(R.string.location_settings_title)), isAvailable(), 4, 0);
    }
}
