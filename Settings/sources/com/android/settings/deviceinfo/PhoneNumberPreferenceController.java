package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private final List<Preference> mPreferenceList = new ArrayList();
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    public PhoneNumberPreferenceController(Context context) {
        super(context);
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mSubscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
    }

    public String getPreferenceKey() {
        return KEY_PHONE_NUMBER;
    }

    public boolean isAvailable() {
        return this.mTelephonyManager.isVoiceCapable();
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        this.mPreferenceList.add(preference);
        int phonePreferenceOrder = preference.getOrder();
        for (int simSlotNumber = 1; simSlotNumber < this.mTelephonyManager.getPhoneCount(); simSlotNumber++) {
            Preference multiSimPreference = createNewPreference(screen.getContext());
            multiSimPreference.setOrder(phonePreferenceOrder + simSlotNumber);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(KEY_PHONE_NUMBER);
            stringBuilder.append(simSlotNumber);
            multiSimPreference.setKey(stringBuilder.toString());
            screen.addPreference(multiSimPreference);
            this.mPreferenceList.add(multiSimPreference);
        }
    }

    public void updateState(Preference preference) {
        for (int simSlotNumber = 0; simSlotNumber < this.mPreferenceList.size(); simSlotNumber++) {
            Preference simStatusPreference = (Preference) this.mPreferenceList.get(simSlotNumber);
            simStatusPreference.setTitle(getPreferenceTitle(simSlotNumber));
            if (getSubscriptionInfo(simSlotNumber) == null) {
                simStatusPreference.setSelectable(false);
            } else {
                simStatusPreference.setSelectable(true);
            }
            simStatusPreference.setSummary(getPhoneNumber(simSlotNumber));
        }
    }

    private CharSequence getPhoneNumber(int simSlot) {
        SubscriptionInfo subscriptionInfo = getSubscriptionInfo(simSlot);
        if (subscriptionInfo == null) {
            return this.mContext.getString(R.string.device_info_default);
        }
        return getFormattedPhoneNumber(subscriptionInfo);
    }

    private CharSequence getPreferenceTitle(int simSlot) {
        if (this.mTelephonyManager.getPhoneCount() <= 1) {
            return this.mContext.getString(R.string.status_number);
        }
        return this.mContext.getString(R.string.status_number_sim_slot, new Object[]{Integer.valueOf(simSlot + 1)});
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public SubscriptionInfo getSubscriptionInfo(int simSlot) {
        List<SubscriptionInfo> subscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList != null) {
            for (SubscriptionInfo info : subscriptionInfoList) {
                if (info.getSimSlotIndex() == simSlot) {
                    return info;
                }
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getFormattedPhoneNumber(SubscriptionInfo subscriptionInfo) {
        String phoneNumber = DeviceInfoUtils.getFormattedPhoneNumber(this.mContext, subscriptionInfo);
        if (TextUtils.isEmpty(phoneNumber)) {
            return this.mContext.getString(R.string.device_info_default);
        }
        return BidiFormatter.getInstance().unicodeWrap(phoneNumber, TextDirectionHeuristics.LTR);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Preference createNewPreference(Context context) {
        return new Preference(context);
    }
}
