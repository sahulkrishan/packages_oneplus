package com.android.settings.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class OPWifiScanAlwaysAvailablePreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_available";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri WIFI_SCAN_ALWAYS_AVAILABLE_ENABLED_URI = Global.getUriFor("wifi_scan_always_enabled");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.WIFI_SCAN_ALWAYS_AVAILABLE_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.WIFI_SCAN_ALWAYS_AVAILABLE_ENABLED_URI.equals(uri)) {
                OPWifiScanAlwaysAvailablePreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPWifiScanAlwaysAvailablePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_SCAN_ALWAYS_AVAILABLE));
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
        if (!TextUtils.equals(preference.getKey(), KEY_SCAN_ALWAYS_AVAILABLE) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", ((SwitchPreference) preference).isChecked());
        if (!((SwitchPreference) preference).isChecked()) {
            setWifiWakeupEnabled(false);
        }
        return true;
    }

    private void setWifiWakeupEnabled(boolean enabled) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_wakeup_enabled", enabled);
    }

    public String getPreferenceKey() {
        return KEY_SCAN_ALWAYS_AVAILABLE;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitchNetwork = (SwitchPreference) preference;
            boolean z = true;
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
                z = false;
            }
            enableSwitchNetwork.setChecked(z);
        }
    }
}
