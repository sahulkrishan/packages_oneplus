package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.Utils;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class VibrateWhenRingPreferenceController extends TogglePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    private final int DEFAULT_VALUE = 0;
    private final int NOTIFICATION_VIBRATE_WHEN_RINGING = 1;
    private SettingObserver mSettingObserver;

    private final class SettingObserver extends ContentObserver {
        private final Uri VIBRATE_WHEN_RINGING_URI = System.getUriFor(VibrateWhenRingPreferenceController.KEY_VIBRATE_WHEN_RINGING);
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(boolean register) {
            ContentResolver cr = VibrateWhenRingPreferenceController.this.mContext.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.VIBRATE_WHEN_RINGING_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                VibrateWhenRingPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public VibrateWhenRingPreferenceController(Context context, String key) {
        super(context, key);
    }

    public boolean isChecked() {
        return System.getInt(this.mContext.getContentResolver(), KEY_VIBRATE_WHEN_RINGING, 0) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        return System.putInt(this.mContext.getContentResolver(), KEY_VIBRATE_WHEN_RINGING, isChecked);
    }

    public int getAvailabilityStatus() {
        return Utils.isVoiceCapable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_VIBRATE_WHEN_RINGING);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(KEY_VIBRATE_WHEN_RINGING);
        if (preference != null) {
            this.mSettingObserver = new SettingObserver(preference);
            preference.setPersistent(false);
        }
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(true);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(false);
        }
    }
}
