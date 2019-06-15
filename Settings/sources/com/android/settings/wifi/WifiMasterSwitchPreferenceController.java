package com.android.settings.wifi;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.MasterSwitchController;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SummaryUpdater.OnSummaryChangeListener;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class WifiMasterSwitchPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnSummaryChangeListener, LifecycleObserver, OnResume, OnPause, OnStart, OnStop {
    public static final String KEY_TOGGLE_WIFI = "toggle_wifi";
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final WifiSummaryUpdater mSummaryHelper = new WifiSummaryUpdater(this.mContext, this);
    private WifiEnabler mWifiEnabler;
    private MasterSwitchPreference mWifiPreference;

    public WifiMasterSwitchPreferenceController(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        super(context);
        this.mMetricsFeatureProvider = metricsFeatureProvider;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mWifiPreference = (MasterSwitchPreference) screen.findPreference(KEY_TOGGLE_WIFI);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_wifi_settings);
    }

    public String getPreferenceKey() {
        return KEY_TOGGLE_WIFI;
    }

    public void onResume() {
        this.mSummaryHelper.register(true);
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.resume(this.mContext);
        }
    }

    public void onPause() {
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.pause();
        }
        this.mSummaryHelper.register(false);
    }

    public void onStart() {
        this.mWifiEnabler = new WifiEnabler(this.mContext, new MasterSwitchController(this.mWifiPreference), this.mMetricsFeatureProvider);
    }

    public void onStop() {
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.teardownSwitchController();
        }
    }

    public void onSummaryChanged(String summary) {
        if (this.mWifiPreference != null) {
            this.mWifiPreference.setSummary((CharSequence) summary);
        }
    }
}
