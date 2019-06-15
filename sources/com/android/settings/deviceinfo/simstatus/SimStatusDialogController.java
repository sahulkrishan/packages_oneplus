package com.android.settings.deviceinfo.simstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.telephony.CarrierConfigManager;
import android.telephony.CellBroadcastMessage;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.euicc.EuiccManager;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class SimStatusDialogController implements LifecycleObserver, OnResume, OnPause {
    private static final String CB_AREA_INFO_RECEIVED_ACTION = "com.android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED";
    @VisibleForTesting
    static final int CELLULAR_NETWORK_STATE = 2131362152;
    private static final String CELL_BROADCAST_RECEIVER_APP = "com.android.cellbroadcastreceiver";
    @VisibleForTesting
    static final int CELL_DATA_NETWORK_TYPE_VALUE_ID = 2131362149;
    @VisibleForTesting
    static final int CELL_VOICE_NETWORK_TYPE_VALUE_ID = 2131363325;
    @VisibleForTesting
    static final int EID_INFO_VALUE_ID = 2131362266;
    private static final String GET_LATEST_CB_AREA_INFO_ACTION = "com.android.cellbroadcastreceiver.GET_LATEST_CB_AREA_INFO";
    @VisibleForTesting
    static final int ICCID_INFO_LABEL_ID = 2131362402;
    @VisibleForTesting
    static final int ICCID_INFO_VALUE_ID = 2131362403;
    @VisibleForTesting
    static final int IMS_REGISTRATION_STATE_LABEL_ID = 2131362436;
    @VisibleForTesting
    static final int IMS_REGISTRATION_STATE_VALUE_ID = 2131362437;
    @VisibleForTesting
    static final int NETWORK_PROVIDER_VALUE_ID = 2131362810;
    @VisibleForTesting
    static final int OPERATOR_INFO_LABEL_ID = 2131362522;
    @VisibleForTesting
    static final int OPERATOR_INFO_VALUE_ID = 2131362523;
    @VisibleForTesting
    static final int PHONE_NUMBER_VALUE_ID = 2131362688;
    @VisibleForTesting
    static final int ROAMING_INFO_VALUE_ID = 2131362982;
    @VisibleForTesting
    static final int SERVICE_STATE_VALUE_ID = 2131363052;
    @VisibleForTesting
    static final int SIGNAL_STRENGTH_LABEL_ID = 2131363074;
    @VisibleForTesting
    static final int SIGNAL_STRENGTH_VALUE_ID = 2131363075;
    private static final String TAG = "SimStatusDialogCtrl";
    private final BroadcastReceiver mAreaInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), SimStatusDialogController.CB_AREA_INFO_RECEIVED_ACTION)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    CellBroadcastMessage cbMessage = (CellBroadcastMessage) extras.get("message");
                    if (cbMessage != null && SimStatusDialogController.this.mSubscriptionInfo.getSubscriptionId() == cbMessage.getSubId()) {
                        SimStatusDialogController.this.mDialog.setText(R.id.latest_area_info_value, cbMessage.getMessageBody());
                    }
                }
            }
        }
    };
    private final CarrierConfigManager mCarrierConfigManager;
    private final Context mContext;
    private final SimStatusDialogFragment mDialog;
    private final EuiccManager mEuiccManager;
    private PhoneStateListener mPhoneStateListener;
    private final Resources mRes;
    private boolean mShowLatestAreaInfo;
    private final SubscriptionInfo mSubscriptionInfo;
    private final TelephonyManager mTelephonyManager;

    public SimStatusDialogController(@NonNull SimStatusDialogFragment dialog, Lifecycle lifecycle, int slotId) {
        this.mDialog = dialog;
        this.mContext = dialog.getContext();
        this.mSubscriptionInfo = getPhoneSubscriptionInfo(slotId);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCarrierConfigManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        this.mEuiccManager = (EuiccManager) this.mContext.getSystemService("euicc");
        this.mRes = this.mContext.getResources();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("slotId = ");
        stringBuilder.append(slotId);
        Log.d(str, stringBuilder.toString());
    }

    public void initialize() {
        updateEid();
        if (this.mSubscriptionInfo != null) {
            this.mPhoneStateListener = getPhoneStateListener();
            ServiceState serviceState = getCurrentServiceState();
            updateNetworkProvider(serviceState);
            updatePhoneNumber();
            updateLatestAreaInfo();
            updateServiceState(serviceState);
            updateSignalStrength(getSignalStrength());
            updateNetworkType();
            updateRoamingStatus(serviceState);
            updateIccidNumber();
            updateImsRegistrationState();
        }
    }

    public void onResume() {
        if (this.mSubscriptionInfo != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 321);
            if (this.mShowLatestAreaInfo) {
                this.mContext.registerReceiver(this.mAreaInfoReceiver, new IntentFilter(CB_AREA_INFO_RECEIVED_ACTION), "android.permission.RECEIVE_EMERGENCY_BROADCAST", null);
                Intent getLatestIntent = new Intent(GET_LATEST_CB_AREA_INFO_ACTION);
                getLatestIntent.setPackage(CELL_BROADCAST_RECEIVER_APP);
                this.mContext.sendBroadcastAsUser(getLatestIntent, UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST");
            }
        }
    }

    public void onPause() {
        if (this.mSubscriptionInfo != null) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            if (this.mShowLatestAreaInfo) {
                this.mContext.unregisterReceiver(this.mAreaInfoReceiver);
            }
        }
    }

    private void updateNetworkProvider(ServiceState serviceState) {
        this.mDialog.setText(R.id.operator_name_value, serviceState.getOperatorAlphaLong());
    }

    private void updatePhoneNumber() {
        this.mDialog.setText(R.id.number_value, BidiFormatter.getInstance().unicodeWrap(getPhoneNumber(), TextDirectionHeuristics.LTR));
    }

    private void updateDataState(int state) {
        String networkStateValue;
        switch (state) {
            case 0:
                networkStateValue = this.mRes.getString(R.string.radioInfo_data_disconnected);
                break;
            case 1:
                networkStateValue = this.mRes.getString(R.string.radioInfo_data_connecting);
                break;
            case 2:
                networkStateValue = this.mRes.getString(R.string.radioInfo_data_connected);
                break;
            case 3:
                networkStateValue = this.mRes.getString(R.string.radioInfo_data_suspended);
                break;
            default:
                networkStateValue = this.mRes.getString(R.string.radioInfo_unknown);
                break;
        }
        this.mDialog.setText(R.id.data_state_value, networkStateValue);
    }

    private void updateLatestAreaInfo() {
        boolean z = Resources.getSystem().getBoolean(17957027) && this.mTelephonyManager.getPhoneType() != 2;
        this.mShowLatestAreaInfo = z;
        if (!this.mShowLatestAreaInfo) {
            this.mDialog.removeSettingFromScreen(R.id.latest_area_info_label);
            this.mDialog.removeSettingFromScreen(R.id.latest_area_info_value);
        }
    }

    private void updateServiceState(ServiceState serviceState) {
        String serviceStateValue;
        int state = serviceState.getState();
        if (state == 1 || state == 3) {
            resetSignalStrength();
        }
        switch (state) {
            case 0:
                serviceStateValue = this.mRes.getString(R.string.radioInfo_service_in);
                break;
            case 1:
            case 2:
                serviceStateValue = this.mRes.getString(R.string.radioInfo_service_out);
                break;
            case 3:
                serviceStateValue = this.mRes.getString(R.string.radioInfo_service_off);
                break;
            default:
                serviceStateValue = this.mRes.getString(R.string.radioInfo_unknown);
                break;
        }
        this.mDialog.setText(R.id.service_state_value, serviceStateValue);
    }

    private void updateSignalStrength(SignalStrength signalStrength) {
        PersistableBundle carrierConfig = this.mCarrierConfigManager.getConfigForSubId(this.mSubscriptionInfo.getSubscriptionId());
        boolean showSignalStrength = true;
        if (carrierConfig != null) {
            showSignalStrength = carrierConfig.getBoolean("show_signal_strength_in_sim_status_bool");
        }
        if (showSignalStrength) {
            int state = getCurrentServiceState().getState();
            if (1 != state && 3 != state) {
                int signalDbm = getDbm(signalStrength);
                int signalAsu = getAsuLevel(signalStrength);
                if (signalDbm == -1) {
                    signalDbm = 0;
                }
                if (signalAsu == -1) {
                    signalAsu = 0;
                }
                this.mDialog.setText(R.id.signal_strength_value, this.mRes.getString(R.string.sim_signal_strength, new Object[]{Integer.valueOf(signalDbm), Integer.valueOf(signalAsu)}));
                return;
            }
            return;
        }
        this.mDialog.removeSettingFromScreen(R.id.signal_strength_label);
        this.mDialog.removeSettingFromScreen(R.id.signal_strength_value);
    }

    private void resetSignalStrength() {
        this.mDialog.setText(R.id.signal_strength_value, "0");
    }

    private void updateNetworkType() {
        TelephonyManager telephonyManager;
        String dataNetworkTypeName = null;
        String voiceNetworkTypeName = null;
        int subId = this.mSubscriptionInfo.getSubscriptionId();
        int actualDataNetworkType = this.mTelephonyManager.getDataNetworkType(subId);
        int actualVoiceNetworkType = this.mTelephonyManager.getVoiceNetworkType(subId);
        if (actualDataNetworkType != 0) {
            telephonyManager = this.mTelephonyManager;
            dataNetworkTypeName = TelephonyManager.getNetworkTypeName(actualDataNetworkType);
        }
        if (actualVoiceNetworkType != 0) {
            telephonyManager = this.mTelephonyManager;
            voiceNetworkTypeName = TelephonyManager.getNetworkTypeName(actualVoiceNetworkType);
        }
        boolean show4GForLTE = false;
        try {
            Context con = this.mContext.createPackageContext(SliceBroadcastRelay.SYSTEMUI_PACKAGE, 0);
            show4GForLTE = con.getResources().getBoolean(con.getResources().getIdentifier("config_show4GForLTE", "bool", SliceBroadcastRelay.SYSTEMUI_PACKAGE));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException for show4GForLTE");
        }
        if (show4GForLTE) {
            if (!OpFeatures.isSupport(new int[]{85})) {
                if ("LTE".equals(dataNetworkTypeName)) {
                    dataNetworkTypeName = "4G";
                }
                if ("LTE".equals(voiceNetworkTypeName)) {
                    voiceNetworkTypeName = "4G";
                }
            }
        }
        this.mDialog.setText(R.id.voice_network_type_value, voiceNetworkTypeName);
        this.mDialog.setText(R.id.data_network_type_value, dataNetworkTypeName);
    }

    private void updateRoamingStatus(ServiceState serviceState) {
        if (serviceState.getRoaming()) {
            this.mDialog.setText(R.id.roaming_state_value, this.mRes.getString(R.string.radioInfo_roaming_in));
        } else {
            this.mDialog.setText(R.id.roaming_state_value, this.mRes.getString(R.string.radioInfo_roaming_not));
        }
    }

    private void updateIccidNumber() {
        int subscriptionId = this.mSubscriptionInfo.getSubscriptionId();
        PersistableBundle carrierConfig = this.mCarrierConfigManager.getConfigForSubId(subscriptionId);
        boolean showIccId = false;
        if (carrierConfig != null) {
            showIccId = carrierConfig.getBoolean("show_iccid_in_sim_status_bool");
        }
        if (OPUtils.isSupportUstMode()) {
            showIccId = true;
        }
        if (showIccId) {
            this.mDialog.setText(R.id.icc_id_value, getSimSerialNumber(subscriptionId));
            return;
        }
        this.mDialog.removeSettingFromScreen(R.id.icc_id_label);
        this.mDialog.removeSettingFromScreen(R.id.icc_id_value);
    }

    private void updateEid() {
        this.mDialog.setText(R.id.esim_id_value, this.mEuiccManager.getEid());
    }

    private void updateImsRegistrationState() {
        boolean showImsRegState;
        int subscriptionId = this.mSubscriptionInfo.getSubscriptionId();
        PersistableBundle carrierConfig = this.mCarrierConfigManager.getConfigForSubId(subscriptionId);
        if (carrierConfig == null) {
            showImsRegState = false;
        } else {
            showImsRegState = carrierConfig.getBoolean("show_ims_registration_status_bool");
        }
        if (showImsRegState) {
            this.mDialog.setText(R.id.ims_reg_state_value, this.mRes.getString(this.mTelephonyManager.isImsRegistered(subscriptionId) ? R.string.ims_reg_status_registered : R.string.ims_reg_status_not_registered));
            return;
        }
        this.mDialog.removeSettingFromScreen(R.id.ims_reg_state_label);
        this.mDialog.removeSettingFromScreen(R.id.ims_reg_state_value);
    }

    private SubscriptionInfo getPhoneSubscriptionInfo(int slotId) {
        List<SubscriptionInfo> subscriptionInfoList = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfoList();
        if (subscriptionInfoList == null) {
            return null;
        }
        for (SubscriptionInfo info : subscriptionInfoList) {
            if (slotId == info.getSimSlotIndex()) {
                return info;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public ServiceState getCurrentServiceState() {
        return this.mTelephonyManager.getServiceStateForSubscriber(this.mSubscriptionInfo.getSubscriptionId());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getDbm(SignalStrength signalStrength) {
        if (signalStrength.getLteLevel() == 0) {
            return signalStrength.getDbm();
        }
        return signalStrength.getLteDbm();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getAsuLevel(SignalStrength signalStrength) {
        return signalStrength.getAsuLevel();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public PhoneStateListener getPhoneStateListener() {
        return new PhoneStateListener(Integer.valueOf(this.mSubscriptionInfo.getSubscriptionId())) {
            public void onDataConnectionStateChanged(int state) {
                SimStatusDialogController.this.updateDataState(state);
                SimStatusDialogController.this.updateNetworkType();
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                SimStatusDialogController.this.updateSignalStrength(signalStrength);
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                SimStatusDialogController.this.updateNetworkProvider(serviceState);
                SimStatusDialogController.this.updateServiceState(serviceState);
                SimStatusDialogController.this.updateRoamingStatus(serviceState);
                SimStatusDialogController.this.updateNetworkType();
            }
        };
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getPhoneNumber() {
        return DeviceInfoUtils.getFormattedPhoneNumber(this.mContext, this.mSubscriptionInfo);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public SignalStrength getSignalStrength() {
        return this.mTelephonyManager.getSignalStrength();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getSimSerialNumber(int subscriptionId) {
        return this.mTelephonyManager.getSimSerialNumber(subscriptionId);
    }
}
