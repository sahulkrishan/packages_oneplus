package com.android.settingslib.deviceinfo;

import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public abstract class AbstractImsStatusPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = new String[]{"android.bluetooth.adapter.action.STATE_CHANGED", "android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    @VisibleForTesting
    static final String KEY_IMS_REGISTRATION_STATE = "ims_reg_state";
    private Preference mImsStatus;

    public AbstractImsStatusPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }

    public boolean isAvailable() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService(CarrierConfigManager.class);
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        PersistableBundle config = null;
        if (configManager != null) {
            config = configManager.getConfigForSubId(subId);
        }
        return config != null && config.getBoolean("show_ims_registration_status_bool");
    }

    public String getPreferenceKey() {
        return KEY_IMS_REGISTRATION_STATE;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mImsStatus = screen.findPreference(KEY_IMS_REGISTRATION_STATE);
        updateConnectivity();
    }

    /* Access modifiers changed, original: protected */
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    /* Access modifiers changed, original: protected */
    public void updateConnectivity() {
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (this.mImsStatus != null) {
            TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
            Preference preference = this.mImsStatus;
            int i = (tm == null || !tm.isImsRegistered(subId)) ? R.string.ims_reg_status_not_registered : R.string.ims_reg_status_registered;
            preference.setSummary(i);
        }
    }
}
