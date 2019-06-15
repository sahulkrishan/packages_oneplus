package com.android.settings.accessibility;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public abstract class VibrationIntensityPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop {
    private Preference mPreference;
    private final String mSettingKey;
    private final SettingObserver mSettingsContentObserver;
    protected final Vibrator mVibrator = ((Vibrator) this.mContext.getSystemService(Vibrator.class));

    private static class SettingObserver extends ContentObserver {
        public final Uri uri;

        public SettingObserver(String settingKey) {
            super(new Handler(Looper.getMainLooper()));
            this.uri = System.getUriFor(settingKey);
        }
    }

    public abstract int getDefaultIntensity();

    public VibrationIntensityPreferenceController(Context context, String prefkey, String settingKey) {
        super(context, prefkey);
        this.mSettingKey = settingKey;
        this.mSettingsContentObserver = new SettingObserver(settingKey) {
            public void onChange(boolean selfChange, Uri uri) {
                VibrationIntensityPreferenceController.this.updateState(VibrationIntensityPreferenceController.this.mPreference);
            }
        };
    }

    public void onStart() {
        this.mContext.getContentResolver().registerContentObserver(this.mSettingsContentObserver.uri, false, this.mSettingsContentObserver);
    }

    public void onStop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsContentObserver);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public CharSequence getSummary() {
        return getIntensityString(this.mContext, System.getInt(this.mContext.getContentResolver(), this.mSettingKey, getDefaultIntensity()));
    }

    public static CharSequence getIntensityString(Context context, int intensity) {
        if (context.getResources().getBoolean(R.bool.config_vibration_supports_multiple_intensities)) {
            switch (intensity) {
                case 0:
                    return context.getString(R.string.accessibility_vibration_intensity_off);
                case 1:
                    return context.getString(R.string.accessibility_vibration_intensity_low);
                case 2:
                    return context.getString(R.string.accessibility_vibration_intensity_medium);
                case 3:
                    return context.getString(R.string.accessibility_vibration_intensity_high);
                default:
                    return "";
            }
        } else if (intensity == 0) {
            return context.getString(R.string.switch_off_text);
        } else {
            return context.getString(R.string.switch_on_text);
        }
    }
}
