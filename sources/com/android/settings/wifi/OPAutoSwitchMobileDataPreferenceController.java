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

public class OPAutoSwitchMobileDataPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_AUTO_SWITCH_MOBILE_DATA = "auto_switch_mobile_data";
    public static final String WIFI_AUTO_CHANGE_TO_MOBILE_DATA = "wifi_auto_change_to_mobile_data";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri WIFI_AUTO_CHANGE_TO_MOBILE_DATA_URI = System.getUriFor(OPAutoSwitchMobileDataPreferenceController.WIFI_AUTO_CHANGE_TO_MOBILE_DATA);
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.WIFI_AUTO_CHANGE_TO_MOBILE_DATA_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.WIFI_AUTO_CHANGE_TO_MOBILE_DATA_URI.equals(uri)) {
                OPAutoSwitchMobileDataPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public OPAutoSwitchMobileDataPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_AUTO_SWITCH_MOBILE_DATA);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_AUTO_SWITCH_MOBILE_DATA));
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
        if (!TextUtils.equals(preference.getKey(), KEY_AUTO_SWITCH_MOBILE_DATA) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        System.putInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_TO_MOBILE_DATA, ((SwitchPreference) preference).isChecked());
        OPUtils.sendAppTrackerForDataAutoSwitch();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_AUTO_SWITCH_MOBILE_DATA;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitchNetwork = (SwitchPreference) preference;
            boolean z = true;
            if (System.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_TO_MOBILE_DATA, 0) != 1) {
                z = false;
            }
            enableSwitchNetwork.setChecked(z);
        }
    }
}
