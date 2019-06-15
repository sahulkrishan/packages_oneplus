package com.android.settings.fuelgauge;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;

public class SmartBatteryPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private static final String KEY_SMART_BATTERY = "smart_battery";
    private static final int OFF = 0;
    private static final int ON = 1;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;

    public SmartBatteryPreferenceController(Context context) {
        super(context, KEY_SMART_BATTERY);
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
    }

    public int getAvailabilityStatus() {
        if (this.mPowerUsageFeatureProvider.isSmartBatterySupported()) {
            return 0;
        }
        return 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_SMART_BATTERY);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1) != 1) {
            z = false;
        }
        ((SwitchPreference) preference).setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }
}
