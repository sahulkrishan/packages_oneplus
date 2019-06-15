package com.android.settings.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class OPIntelligentlySelectBestWifiPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_INTELLIGENTLY_SELECT_BEST_WIFI = "intelligently_select_best_wifi";
    public static final String WIFI_SHOULD_SWITCH_NETWORK = "wifi_should_switch_network";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri WIFI_SHOULD_SWITCH_NETWORK_ENABLED_URI = System.getUriFor(OPIntelligentlySelectBestWifiPreferenceController.WIFI_SHOULD_SWITCH_NETWORK);
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.WIFI_SHOULD_SWITCH_NETWORK_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.WIFI_SHOULD_SWITCH_NETWORK_ENABLED_URI.equals(uri)) {
                OPIntelligentlySelectBestWifiPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPIntelligentlySelectBestWifiPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_INTELLIGENTLY_SELECT_BEST_WIFI);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_INTELLIGENTLY_SELECT_BEST_WIFI));
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

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_INTELLIGENTLY_SELECT_BEST_WIFI) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        System.putInt(this.mContext.getContentResolver(), WIFI_SHOULD_SWITCH_NETWORK, ((SwitchPreference) preference).isChecked());
        OPUtils.sendAppTrackerForSmartWifiSwitch();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_INTELLIGENTLY_SELECT_BEST_WIFI;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitchNetwork = (SwitchPreference) preference;
            boolean z = true;
            if (System.getInt(this.mContext.getContentResolver(), WIFI_SHOULD_SWITCH_NETWORK, 0) != 1) {
                z = false;
            }
            enableSwitchNetwork.setChecked(z);
        }
    }
}
