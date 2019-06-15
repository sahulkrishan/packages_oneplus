package com.android.settings.deviceinfo.simstatus;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.deviceinfo.AbstractSimStatusImeiInfoPreferenceController;
import com.oneplus.settings.utils.OPSNSUtils;
import java.util.ArrayList;
import java.util.List;

public class SimStatusPreferenceController extends AbstractSimStatusImeiInfoPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_SIM_STATUS = "sim_status";
    private final Fragment mFragment;
    private final List<Preference> mPreferenceList = new ArrayList();
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    public SimStatusPreferenceController(Context context, Fragment fragment) {
        super(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
        this.mFragment = fragment;
    }

    public String getPreferenceKey() {
        return KEY_SIM_STATUS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (isAvailable() && preference != null && preference.isVisible()) {
            this.mPreferenceList.add(preference);
            int simStatusOrder = preference.getOrder();
            for (int simSlotNumber = 1; simSlotNumber < this.mTelephonyManager.getPhoneCount(); simSlotNumber++) {
                Preference multiSimPreference = createNewPreference(screen.getContext());
                multiSimPreference.setOrder(simStatusOrder + simSlotNumber);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(KEY_SIM_STATUS);
                stringBuilder.append(simSlotNumber);
                multiSimPreference.setKey(stringBuilder.toString());
                screen.addPreference(multiSimPreference);
                this.mPreferenceList.add(multiSimPreference);
            }
        }
    }

    public void updateState(Preference preference) {
        for (int simSlotNumber = 0; simSlotNumber < this.mPreferenceList.size(); simSlotNumber++) {
            Preference simStatusPreference = (Preference) this.mPreferenceList.get(simSlotNumber);
            simStatusPreference.setTitle(getPreferenceTitle(simSlotNumber));
            simStatusPreference.setSummary(OPSNSUtils.getSimName(this.mContext, simSlotNumber));
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        int simSlot = this.mPreferenceList.indexOf(preference);
        if (simSlot == -1) {
            return false;
        }
        SimStatusDialogFragment.show(this.mFragment, simSlot, getPreferenceTitle(simSlot));
        return true;
    }

    private String getPreferenceTitle(int simSlot) {
        if (this.mTelephonyManager.getPhoneCount() <= 1) {
            return this.mContext.getString(R.string.sim_status_title);
        }
        return this.mContext.getString(R.string.sim_status_title_sim_slot, new Object[]{Integer.valueOf(simSlot + 1)});
    }

    private CharSequence getCarrierName(int simSlot) {
        List<SubscriptionInfo> subscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null) {
            for (SubscriptionInfo info : subscriptionInfoList) {
                if (info.getSimSlotIndex() == simSlot) {
                    return info.getCarrierName();
                }
            }
        }
        return this.mContext.getText(R.string.device_info_not_available);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Preference createNewPreference(Context context) {
        return new Preference(context);
    }
}
