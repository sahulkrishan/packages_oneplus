package com.android.settings.security;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.settings.core.BasePreferenceController;
import java.util.List;

public class SimLockPreferenceController extends BasePreferenceController {
    private static final String KEY_SIM_LOCK = "sim_lock_settings";
    private final CarrierConfigManager mCarrierConfigManager = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config"));
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;

    public SimLockPreferenceController(Context context) {
        super(context, KEY_SIM_LOCK);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
    }

    public int getAvailabilityStatus() {
        PersistableBundle b = this.mCarrierConfigManager.getConfig();
        if (this.mUserManager.isAdminUser() && isSimIccReady() && !b.getBoolean("hide_sim_lock_settings_bool")) {
            return 0;
        }
        return 3;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setEnabled(isSimReady());
        }
    }

    private boolean isSimReady() {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                int simState = this.mTelephonyManager.getSimState(subInfo.getSimSlotIndex());
                if (simState != 1 && simState != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSimIccReady() {
        List<SubscriptionInfo> subInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            for (SubscriptionInfo subInfo : subInfoList) {
                if (this.mTelephonyManager.hasIccCard(subInfo.getSimSlotIndex())) {
                    return true;
                }
            }
        }
        return false;
    }
}
