package com.android.settings.network;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.util.List;

public class MobileNetworkPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {
    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
    private BroadcastReceiver mAirplanModeChangedReceiver;
    private final boolean mIsSecondaryUser;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            MobileNetworkPreferenceController.this.updateDisplayName();
            MobileNetworkPreferenceController.this.updateState(MobileNetworkPreferenceController.this.mPreference);
        }
    };
    @VisibleForTesting
    PhoneStateListener mPhoneStateListener;
    private Preference mPreference;
    private SubscriptionManager mSubscriptionManager;
    private String mSummary;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;

    public MobileNetworkPreferenceController(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mIsSecondaryUser = this.mUserManager.isAdminUser() ^ 1;
        this.mAirplanModeChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                MobileNetworkPreferenceController.this.updateDisplayName();
                MobileNetworkPreferenceController.this.updateState(MobileNetworkPreferenceController.this.mPreference);
            }
        };
        this.mSubscriptionManager = SubscriptionManager.from(context);
    }

    public boolean isAvailable() {
        return (isUserRestricted() || Utils.isWifiOnly(this.mContext)) ? false : true;
    }

    public boolean isUserRestricted() {
        return this.mIsSecondaryUser || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_mobile_networks", UserHandle.myUserId());
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_MOBILE_NETWORK_SETTINGS;
    }

    public void onStart() {
        if (this.mSubscriptionManager != null) {
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        }
        if (isAvailable()) {
            if (this.mPhoneStateListener == null) {
                this.mPhoneStateListener = new PhoneStateListener() {
                    public void onServiceStateChanged(ServiceState serviceState) {
                        MobileNetworkPreferenceController.this.updateDisplayName();
                        MobileNetworkPreferenceController.this.updateState(MobileNetworkPreferenceController.this.mPreference);
                    }
                };
            }
            this.mTelephonyManager.listen(this.mPhoneStateListener, 1);
        }
        if (this.mAirplanModeChangedReceiver != null) {
            this.mContext.registerReceiver(this.mAirplanModeChangedReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        }
    }

    private void updateDisplayName() {
        if (this.mPreference != null) {
            List<SubscriptionInfo> list = this.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (list == null || list.isEmpty()) {
                this.mSummary = this.mTelephonyManager.getNetworkOperatorName();
                return;
            }
            boolean useSeparator = false;
            StringBuilder builder = new StringBuilder();
            for (SubscriptionInfo subInfo : list) {
                if (isSubscriptionInService(subInfo.getSubscriptionId())) {
                    if (useSeparator) {
                        builder.append(", ");
                    }
                    builder.append(this.mTelephonyManager.getNetworkOperatorName(subInfo.getSubscriptionId()));
                    useSeparator = true;
                }
            }
            this.mSummary = builder.toString();
        }
    }

    private boolean isSubscriptionInService(int subId) {
        if (this.mTelephonyManager == null || this.mTelephonyManager.getServiceStateForSubscriber(subId).getState() != 0) {
            return false;
        }
        return true;
    }

    public void onStop() {
        if (this.mPhoneStateListener != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        if (this.mAirplanModeChangedReceiver != null) {
            this.mContext.unregisterReceiver(this.mAirplanModeChangedReceiver);
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!(preference instanceof RestrictedPreference) || !((RestrictedPreference) preference).isDisabledByAdmin()) {
            boolean z = false;
            if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
                z = true;
            }
            preference.setEnabled(z);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_MOBILE_NETWORK_SETTINGS.equals(preference.getKey()) || !com.android.settings.Utils.isNetworkSettingsApkAvailable()) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.qualcomm.qti.networksetting", "com.qualcomm.qti.networksetting.MobileNetworkSettings"));
        this.mContext.startActivity(intent);
        return true;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }
}
