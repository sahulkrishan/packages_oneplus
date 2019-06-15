package com.android.settings.fuelgauge;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.conditional.BatterySaverCondition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.fuelgauge.BatterySaverReceiver.BatterySaverListener;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class BatterySaverController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, BatterySaverListener {
    private static final String KEY_BATTERY_SAVER = "battery_saver_summary";
    private Preference mBatterySaverPref;
    private final BatterySaverReceiver mBatteryStateChangeReceiver;
    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange) {
            BatterySaverController.this.updateSummary();
        }
    };
    private final PowerManager mPowerManager = ((PowerManager) this.mContext.getSystemService("power"));

    public BatterySaverController(Context context) {
        super(context, KEY_BATTERY_SAVER);
        this.mBatteryStateChangeReceiver = new BatterySaverReceiver(context);
        this.mBatteryStateChangeReceiver.setBatterySaverListener(this);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_BATTERY_SAVER;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mBatterySaverPref = screen.findPreference(KEY_BATTERY_SAVER);
    }

    public void onStart() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("low_power_trigger_level"), true, this.mObserver);
        this.mBatteryStateChangeReceiver.setListening(true);
        updateSummary();
    }

    public void onStop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
        this.mBatteryStateChangeReceiver.setListening(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void refreshConditionManager() {
        ((BatterySaverCondition) ConditionManager.get(this.mContext).getCondition(BatterySaverCondition.class)).refreshState();
    }

    public CharSequence getSummary() {
        boolean isPowerSaveOn = this.mPowerManager.isPowerSaveMode();
        int percent = Global.getInt(this.mContext.getContentResolver(), "low_power_trigger_level", 0);
        if (isPowerSaveOn) {
            return this.mContext.getString(R.string.battery_saver_on_summary);
        }
        if (percent == 0) {
            return this.mContext.getString(R.string.battery_saver_off_summary);
        }
        return this.mContext.getString(R.string.battery_saver_off_scheduled_summary, new Object[]{Utils.formatPercentage(percent)});
    }

    private void updateSummary() {
        this.mBatterySaverPref.setSummary(getSummary());
    }

    public void onPowerSaveModeChanged() {
        updateSummary();
    }

    public void onBatteryChanged(boolean pluggedIn) {
    }
}
