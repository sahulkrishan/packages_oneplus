package com.android.settings.fuelgauge;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;

public class AutoRestrictionPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private static final String KEY_SMART_BATTERY = "auto_restriction";
    private static final int OFF = 0;
    private static final int ON = 1;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;

    public AutoRestrictionPreferenceController(Context context) {
        super(context, KEY_SMART_BATTERY);
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
    }

    public int getAvailabilityStatus() {
        if (this.mPowerUsageFeatureProvider.isSmartBatterySupported()) {
            return 2;
        }
        return 0;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "app_auto_restriction_enabled", 1) != 1) {
            z = false;
        }
        ((SwitchPreference) preference).setChecked(z);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "app_auto_restriction_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }
}
