package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class PulseNotificationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String TAG = "PulseNotifPrefContr";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri NOTIFICATION_LIGHT_PULSE_URI = System.getUriFor("notification_light_pulse");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.NOTIFICATION_LIGHT_PULSE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.NOTIFICATION_LIGHT_PULSE_URI.equals(uri)) {
                PulseNotificationPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public PulseNotificationPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(KEY_NOTIFICATION_PULSE);
        if (preference != null) {
            this.mSettingObserver = new SettingObserver(preference);
        }
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

    public String getPreferenceKey() {
        return KEY_NOTIFICATION_PULSE;
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(17956988);
    }

    public void updateState(Preference preference) {
        try {
            boolean z = true;
            if (System.getInt(this.mContext.getContentResolver(), "notification_light_pulse") != 1) {
                z = false;
            }
            ((TwoStatePreference) preference).setChecked(z);
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "notification_light_pulse not found");
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return System.putInt(this.mContext.getContentResolver(), "notification_light_pulse", ((Boolean) newValue).booleanValue());
    }
}
