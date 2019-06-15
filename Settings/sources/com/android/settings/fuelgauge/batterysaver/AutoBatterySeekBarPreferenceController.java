package com.android.settings.fuelgauge.batterysaver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class AutoBatterySeekBarPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, OnPreferenceChangeListener {
    @VisibleForTesting
    static final String KEY_AUTO_BATTERY_SEEK_BAR = "battery_saver_seek_bar";
    private static final String TAG = "AutoBatterySeekBarPreferenceController";
    private AutoBatterySaverSettingObserver mContentObserver = new AutoBatterySaverSettingObserver(new Handler(Looper.getMainLooper()));
    private SeekBarPreference mPreference;

    private final class AutoBatterySaverSettingObserver extends ContentObserver {
        private final ContentResolver mContentResolver;
        private final Uri mUri = Global.getUriFor("low_power_trigger_level");

        public AutoBatterySaverSettingObserver(Handler handler) {
            super(handler);
            this.mContentResolver = AutoBatterySeekBarPreferenceController.this.mContext.getContentResolver();
        }

        public void registerContentObserver() {
            this.mContentResolver.registerContentObserver(this.mUri, false, this);
        }

        public void unRegisterContentObserver() {
            this.mContentResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mUri.equals(uri)) {
                AutoBatterySeekBarPreferenceController.this.updatePreference(AutoBatterySeekBarPreferenceController.this.mPreference);
            }
        }
    }

    public AutoBatterySeekBarPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_AUTO_BATTERY_SEEK_BAR);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (SeekBarPreference) screen.findPreference(KEY_AUTO_BATTERY_SEEK_BAR);
        this.mPreference.setContinuousUpdates(true);
        this.mPreference.setAccessibilityRangeInfoType(2);
        updatePreference(this.mPreference);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        updatePreference(preference);
    }

    public void onStart() {
        this.mContentObserver.registerContentObserver();
    }

    public void onStop() {
        this.mContentObserver.unRegisterContentObserver();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "low_power_trigger_level", ((Integer) newValue).intValue());
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updatePreference(Preference preference) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int maxLevel = Global.getInt(contentResolver, "low_power_trigger_level_max", 0);
        if (maxLevel > 0) {
            if (preference instanceof SeekBarPreference) {
                SeekBarPreference seekBarPreference = (SeekBarPreference) preference;
                if (maxLevel < seekBarPreference.getMin()) {
                    Log.e(TAG, "LOW_POWER_MODE_TRIGGER_LEVEL_MAX too low; ignored.");
                } else {
                    seekBarPreference.setMax(maxLevel);
                }
            } else {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unexpected preference class: ");
                stringBuilder.append(preference.getClass());
                Log.e(str, stringBuilder.toString());
            }
        }
        int level = Global.getInt(contentResolver, "low_power_trigger_level", 0);
        if (level == 0) {
            preference.setVisible(false);
            return;
        }
        preference.setVisible(true);
        preference.setTitle(this.mContext.getString(R.string.battery_saver_seekbar_title, new Object[]{Utils.formatPercentage(level)}));
        SeekBarPreference seekBarPreference2 = (SeekBarPreference) preference;
        seekBarPreference2.setProgress(level);
        seekBarPreference2.setSeekBarContentDescription(this.mContext.getString(R.string.battery_saver_turn_on_automatically_title));
    }
}
