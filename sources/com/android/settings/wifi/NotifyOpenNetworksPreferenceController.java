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
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class NotifyOpenNetworksPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri NETWORKS_AVAILABLE_URI = Global.getUriFor("wifi_networks_available_notification_on");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.NETWORKS_AVAILABLE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.NETWORKS_AVAILABLE_URI.equals(uri)) {
                NotifyOpenNetworksPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public NotifyOpenNetworksPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSettingObserver = new SettingObserver(screen.findPreference(KEY_NOTIFY_OPEN_NETWORKS));
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
        if (!TextUtils.equals(preference.getKey(), KEY_NOTIFY_OPEN_NETWORKS) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        Global.putInt(this.mContext.getContentResolver(), "wifi_networks_available_notification_on", ((SwitchPreference) preference).isChecked());
        return true;
    }

    public String getPreferenceKey() {
        return KEY_NOTIFY_OPEN_NETWORKS;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference notifyOpenNetworks = (SwitchPreference) preference;
            boolean z = true;
            if (Global.getInt(this.mContext.getContentResolver(), "wifi_networks_available_notification_on", 0) != 1) {
                z = false;
            }
            notifyOpenNetworks.setChecked(z);
        }
    }
}
