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
import android.util.OpFeatures;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class OPNetworkAutoChangePreferenceController extends AbstractPreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_NETWORK_AUTO_CHANGE = "network_auto_change";
    public static final String WIFI_AUTO_CHANGE_TO_MOBILE_DATA = "wifi_auto_change_to_mobile_data";
    public static final String WIFI_SHOULD_SWITCH_NETWORK = "wifi_should_switch_network";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri WIFI_SHOULD_SWITCH_NETWORK_ENABLED_URI = System.getUriFor(OPNetworkAutoChangePreferenceController.WIFI_SHOULD_SWITCH_NETWORK);
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
                OPNetworkAutoChangePreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPNetworkAutoChangePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_NETWORK_AUTO_CHANGE));
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
        if (!TextUtils.equals(preference.getKey(), KEY_NETWORK_AUTO_CHANGE) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        System.putInt(this.mContext.getContentResolver(), WIFI_SHOULD_SWITCH_NETWORK, ((SwitchPreference) preference).isChecked());
        if (OpFeatures.isSupport(new int[]{1})) {
            System.putInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_TO_MOBILE_DATA, ((SwitchPreference) preference).isChecked());
        }
        OPUtils.sendAppTrackerForSmartWifiSwitch();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_NETWORK_AUTO_CHANGE;
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
