package com.android.settings.wifi;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.utils.AnnotationSpan;
import com.android.settings.utils.AnnotationSpan.LinkInfo;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WifiWakeupPreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_ENABLE_WIFI_WAKEUP = "enable_wifi_wakeup";
    private static final String TAG = "WifiWakeupPrefController";
    private final Fragment mFragment;
    private Lifecycle mLifecycle;
    @VisibleForTesting
    LocationManager mLocationManager;
    @VisibleForTesting
    SwitchPreference mPreference;
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri WIFI_WAKE_UP_ENABLED_URI = Global.getUriFor("wifi_wakeup_enabled");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.WIFI_WAKE_UP_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.WIFI_WAKE_UP_ENABLED_URI.equals(uri)) {
                WifiWakeupPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public WifiWakeupPreferenceController(Context context, DashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
        this.mLocationManager = (LocationManager) context.getSystemService("location");
    }

    public WifiWakeupPreferenceController(Context context, DashboardFragment fragment, Lifecycle lifecycle) {
        super(context);
        this.mFragment = fragment;
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        lifecycle.addObserver(this);
        this.mLifecycle = lifecycle;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (SwitchPreference) screen.findPreference(KEY_ENABLE_WIFI_WAKEUP);
        if (this.mLifecycle != null) {
            this.mSettingObserver = new SettingObserver(this.mPreference);
        }
        updateState(this.mPreference);
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), false);
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_ENABLE_WIFI_WAKEUP) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        if (!this.mLocationManager.isLocationEnabled()) {
            this.mFragment.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        } else if (getWifiWakeupEnabled()) {
            setWifiWakeupEnabled(false);
        } else if (getWifiScanningEnabled()) {
            setWifiWakeupEnabled(true);
        } else {
            showScanningDialog();
        }
        updateState(this.mPreference);
        return true;
    }

    public String getPreferenceKey() {
        return KEY_ENABLE_WIFI_WAKEUP;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableWifiWakeup = (SwitchPreference) preference;
            boolean z = getWifiWakeupEnabled() && getWifiScanningEnabled() && this.mLocationManager.isLocationEnabled();
            enableWifiWakeup.setChecked(z);
            if (this.mLocationManager.isLocationEnabled()) {
                preference.setSummary((int) R.string.wifi_wakeup_summary);
            } else {
                preference.setSummary(getNoLocationSummary());
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getNoLocationSummary() {
        LinkInfo linkInfo = new LinkInfo(LinkInfo.DEFAULT_ANNOTATION, null);
        return AnnotationSpan.linkify(this.mContext.getText(R.string.wifi_wakeup_summary_no_location), linkInfo);
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE) {
            if (this.mLocationManager.isLocationEnabled()) {
                setWifiWakeupEnabled(true);
            }
            updateState(this.mPreference);
        }
    }

    private boolean getWifiScanningEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
    }

    private void showScanningDialog() {
        WifiScanningRequiredFragment dialogFragment = WifiScanningRequiredFragment.newInstance();
        dialogFragment.setTargetFragment(this.mFragment, ConfigureWifiSettings.WIFI_WAKEUP_REQUEST_CODE);
        dialogFragment.show(this.mFragment.getFragmentManager(), TAG);
    }

    private boolean getWifiWakeupEnabled() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_wakeup_enabled", 0) == 1;
    }

    private void setWifiWakeupEnabled(boolean enabled) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_wakeup_enabled", enabled);
    }
}
