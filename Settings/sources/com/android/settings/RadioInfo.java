package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.QueuedWork;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings.Global;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v4.os.EnvironmentCompat;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.DataConnectionRealTimeInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.PhysicalChannelConfig;
import android.telephony.PreciseCallState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.android.ims.ImsConfig;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import java.io.IOException;
import java.util.List;

public class RadioInfo extends Activity {
    private static final int CELL_INFO_LIST_RATE_DISABLED = Integer.MAX_VALUE;
    private static final int CELL_INFO_LIST_RATE_MAX = 0;
    private static final int EAB_PROVISIONED_CONFIG_ID = 25;
    private static final int EVENT_CFI_CHANGED = 302;
    private static final int EVENT_QUERY_PREFERRED_TYPE_DONE = 1000;
    private static final int EVENT_QUERY_SMSC_DONE = 1005;
    private static final int EVENT_SET_PREFERRED_TYPE_DONE = 1001;
    private static final int EVENT_UPDATE_SMSC_DONE = 1006;
    private static final int IMS_VOLTE_PROVISIONED_CONFIG_ID = 10;
    private static final int IMS_VT_PROVISIONED_CONFIG_ID = 11;
    private static final int IMS_WFC_PROVISIONED_CONFIG_ID = 28;
    private static final int MENU_ITEM_GET_IMS_STATUS = 4;
    private static final int MENU_ITEM_SELECT_BAND = 0;
    private static final int MENU_ITEM_TOGGLE_DATA = 5;
    private static final int MENU_ITEM_VIEW_ADN = 1;
    private static final int MENU_ITEM_VIEW_FDN = 2;
    private static final int MENU_ITEM_VIEW_SDN = 3;
    private static final String TAG = "RadioInfo";
    private static final String[] mCellInfoRefreshRateLabels = new String[]{"Disabled", "Immediate", "Min 5s", "Min 10s", "Min 60s"};
    private static final int[] mCellInfoRefreshRates = new int[]{Integer.MAX_VALUE, 0, 5000, MediaPlayerGlue.FAST_FORWARD_REWIND_STEP, SettingsUtil.TIMEOUT_MILLIS};
    private static final String[] mPreferredNetworkLabels = new String[]{"WCDMA preferred", "GSM only", "WCDMA only", "GSM auto (PRL)", "CDMA auto (PRL)", "CDMA only", "EvDo only", "Global auto (PRL)", "LTE/CDMA auto (PRL)", "LTE/UMTS auto (PRL)", "LTE/CDMA/UMTS auto (PRL)", "LTE only", "LTE/WCDMA", "TD-SCDMA only", "TD-SCDMA/WCDMA", "LTE/TD-SCDMA", "TD-SCDMA/GSM", "TD-SCDMA/UMTS", "LTE/TD-SCDMA/WCDMA", "LTE/TD-SCDMA/UMTS", "TD-SCDMA/CDMA/UMTS", "Global/TD-SCDMA", "Unknown"};
    private TextView callState;
    private Button carrierProvisioningButton;
    private Button cellInfoRefreshRateButton;
    private Spinner cellInfoRefreshRateSpinner;
    private TextView dBm;
    private TextView dataNetwork;
    private TextView dnsCheckState;
    private Button dnsCheckToggleButton;
    private Switch eabProvisionedSwitch;
    private TextView gprsState;
    private TextView gsmState;
    private Switch imsVolteProvisionedSwitch;
    private Switch imsVtProvisionedSwitch;
    private Switch imsWfcProvisionedSwitch;
    OnClickListener mCarrierProvisioningButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.android.settings.CARRIER_PROVISIONING");
            intent.setComponent(ComponentName.unflattenFromString("com.android.omadm.service/.DMIntentReceiver"));
            RadioInfo.this.sendBroadcast(intent);
        }
    };
    private TextView mCellInfo;
    OnItemSelectedListener mCellInfoRefreshRateHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            RadioInfo.this.mCellInfoRefreshRateIndex = pos;
            RadioInfo.this.mTelephonyManager.setCellInfoListRate(RadioInfo.mCellInfoRefreshRates[pos]);
            RadioInfo.this.updateAllCellInfo();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };
    private int mCellInfoRefreshRateIndex;
    private List<CellInfo> mCellInfoResult = null;
    private CellLocation mCellLocationResult = null;
    private TextView mCfi;
    private boolean mCfiValue = false;
    private ConnectivityManager mConnectivityManager;
    private TextView mDcRtInfoTv;
    private final NetworkRequest mDefaultNetworkRequest = new Builder().addTransportType(0).addCapability(12).build();
    private TextView mDeviceId;
    OnClickListener mDnsCheckButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.phone.disableDnsCheck(RadioInfo.this.phone.isDnsCheckDisabled() ^ 1);
            RadioInfo.this.updateDnsCheckState();
        }
    };
    private TextView mDownlinkKbps;
    OnCheckedChangeListener mEabCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo.this.setEabProvisionedState(isChecked);
        }
    };
    private OnMenuItemClickListener mGetImsStatus = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            String imsRegString;
            boolean isImsRegistered = RadioInfo.this.phone.isImsRegistered();
            boolean availableVolte = RadioInfo.this.phone.isVolteEnabled();
            boolean availableWfc = RadioInfo.this.phone.isWifiCallingEnabled();
            boolean availableVt = RadioInfo.this.phone.isVideoEnabled();
            boolean availableUt = RadioInfo.this.phone.isUtEnabled();
            if (isImsRegistered) {
                imsRegString = RadioInfo.this.getString(R.string.radio_info_ims_reg_status_registered);
            } else {
                imsRegString = RadioInfo.this.getString(R.string.radio_info_ims_reg_status_not_registered);
            }
            String available = RadioInfo.this.getString(R.string.radio_info_ims_feature_status_available);
            String unavailable = RadioInfo.this.getString(R.string.radio_info_ims_feature_status_unavailable);
            String imsStatus = RadioInfo.this;
            Object[] objArr = new Object[5];
            objArr[0] = imsRegString;
            objArr[1] = availableVolte ? available : unavailable;
            objArr[2] = availableWfc ? available : unavailable;
            objArr[3] = availableVt ? available : unavailable;
            objArr[4] = availableUt ? available : unavailable;
            new AlertDialog.Builder(RadioInfo.this).setMessage(imsStatus.getString(R.string.radio_info_ims_reg_status, objArr)).setTitle(RadioInfo.this.getString(R.string.radio_info_ims_reg_status_title)).create().show();
            return true;
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case 1000:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null || ar.result == null) {
                        RadioInfo.this.updatePreferredNetworkType(RadioInfo.mPreferredNetworkLabels.length - 1);
                        return;
                    } else {
                        RadioInfo.this.updatePreferredNetworkType(((int[]) ar.result)[0]);
                        return;
                    }
                case 1001:
                    if (((AsyncResult) msg.obj).exception != null) {
                        RadioInfo.this.log("Set preferred network type failed.");
                        return;
                    }
                    return;
                case 1005:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        RadioInfo.this.smsc.setText("refresh error");
                        return;
                    } else {
                        RadioInfo.this.smsc.setText((String) ar.result);
                        return;
                    }
                case 1006:
                    RadioInfo.this.updateSmscButton.setEnabled(true);
                    if (msg.obj.exception != null) {
                        RadioInfo.this.smsc.setText("update error");
                        return;
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private TextView mHttpClientTest;
    private String mHttpClientTestResult;
    private ImsManager mImsManager = null;
    OnCheckedChangeListener mImsVolteCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo.this.setImsVolteProvisionedState(isChecked);
        }
    };
    OnCheckedChangeListener mImsVtCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo.this.setImsVtProvisionedState(isChecked);
        }
    };
    OnCheckedChangeListener mImsWfcCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo.this.setImsWfcProvisionedState(isChecked);
        }
    };
    private TextView mLocation;
    private TextView mMwi;
    private boolean mMwiValue = false;
    private List<NeighboringCellInfo> mNeighboringCellResult = null;
    private TextView mNeighboringCids;
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onCapabilitiesChanged(Network n, NetworkCapabilities nc) {
            RadioInfo.this.updateBandwidths(nc.getLinkDownstreamBandwidthKbps(), nc.getLinkUpstreamBandwidthKbps());
        }
    };
    OnClickListener mOemInfoButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            try {
                RadioInfo.this.startActivity(new Intent("com.android.settings.OEM_RADIO_INFO"));
            } catch (ActivityNotFoundException ex) {
                RadioInfo radioInfo = RadioInfo.this;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("OEM-specific Info/Settings Activity Not Found : ");
                stringBuilder.append(ex);
                radioInfo.log(stringBuilder.toString());
            }
        }
    };
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataConnectionStateChanged(int state) {
            RadioInfo.this.updateDataState();
            RadioInfo.this.updateNetworkType();
        }

        public void onDataActivity(int direction) {
            RadioInfo.this.updateDataStats2();
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            RadioInfo.this.updateNetworkType();
            RadioInfo.this.updatePhoneState(state);
        }

        public void onPreciseCallStateChanged(PreciseCallState preciseState) {
            RadioInfo.this.updateNetworkType();
        }

        public void onCellLocationChanged(CellLocation location) {
            RadioInfo.this.updateLocation(location);
        }

        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            RadioInfo.this.mMwiValue = mwi;
            RadioInfo.this.updateMessageWaiting();
        }

        public void onCallForwardingIndicatorChanged(boolean cfi) {
            RadioInfo.this.mCfiValue = cfi;
            RadioInfo.this.updateCallRedirect();
        }

        public void onCellInfoChanged(List<CellInfo> arrayCi) {
            RadioInfo radioInfo = RadioInfo.this;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onCellInfoChanged: arrayCi=");
            stringBuilder.append(arrayCi);
            radioInfo.log(stringBuilder.toString());
            RadioInfo.this.mCellInfoResult = arrayCi;
            RadioInfo.this.updateCellInfo(RadioInfo.this.mCellInfoResult);
        }

        public void onDataConnectionRealTimeInfoChanged(DataConnectionRealTimeInfo dcRtInfo) {
            RadioInfo radioInfo = RadioInfo.this;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onDataConnectionRealTimeInfoChanged: dcRtInfo=");
            stringBuilder.append(dcRtInfo);
            radioInfo.log(stringBuilder.toString());
            RadioInfo.this.updateDcRtInfoTv(dcRtInfo);
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            RadioInfo radioInfo = RadioInfo.this;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onSignalStrengthChanged: SignalStrength=");
            stringBuilder.append(signalStrength);
            radioInfo.log(stringBuilder.toString());
            RadioInfo.this.updateSignalStrength(signalStrength);
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            RadioInfo radioInfo = RadioInfo.this;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onServiceStateChanged: ServiceState=");
            stringBuilder.append(serviceState);
            radioInfo.log(stringBuilder.toString());
            RadioInfo.this.updateServiceState(serviceState);
            RadioInfo.this.updateRadioPowerState();
            RadioInfo.this.updateNetworkType();
            RadioInfo.this.updateImsProvisionedState();
        }

        public void onPhysicalChannelConfigurationChanged(List<PhysicalChannelConfig> configs) {
            RadioInfo.this.updatePhysicalChannelConfiguration(configs);
        }
    };
    private TextView mPhyChanConfig;
    OnClickListener mPingButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.updatePingState();
        }
    };
    private String mPingHostnameResultV4;
    private String mPingHostnameResultV6;
    private TextView mPingHostnameV4;
    private TextView mPingHostnameV6;
    OnItemSelectedListener mPreferredNetworkHandler = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View v, int pos, long id) {
            if (RadioInfo.this.mPreferredNetworkTypeResult != pos && pos >= 0 && pos <= RadioInfo.mPreferredNetworkLabels.length - 2) {
                StringBuilder stringBuilder;
                RadioInfo.this.mPreferredNetworkTypeResult = pos;
                int subId = RadioInfo.this.phone.getSubId();
                if (SubscriptionManager.isUsableSubIdValue(subId)) {
                    ContentResolver contentResolver = RadioInfo.this.phone.getContext().getContentResolver();
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("preferred_network_mode");
                    stringBuilder.append(subId);
                    Global.putInt(contentResolver, stringBuilder.toString(), RadioInfo.this.mPreferredNetworkTypeResult);
                }
                RadioInfo radioInfo = RadioInfo.this;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Calling setPreferredNetworkType(");
                stringBuilder.append(RadioInfo.this.mPreferredNetworkTypeResult);
                stringBuilder.append(")");
                radioInfo.log(stringBuilder.toString());
                RadioInfo.this.phone.setPreferredNetworkType(RadioInfo.this.mPreferredNetworkTypeResult, RadioInfo.this.mHandler.obtainMessage(1001));
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };
    private int mPreferredNetworkTypeResult;
    OnCheckedChangeListener mRadioPowerOnChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RadioInfo radioInfo = RadioInfo.this;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("toggle radio power: currently ");
            stringBuilder.append(RadioInfo.this.isRadioOn() ? "on" : "off");
            radioInfo.log(stringBuilder.toString());
            RadioInfo.this.phone.setRadioPower(isChecked);
        }
    };
    OnClickListener mRefreshSmscButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.refreshSmsc();
        }
    };
    private OnMenuItemClickListener mSelectBandCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent();
            intent.setClass(RadioInfo.this, BandMode.class);
            RadioInfo.this.startActivity(intent);
            return true;
        }
    };
    private TextView mSubscriberId;
    private TelephonyManager mTelephonyManager;
    private OnMenuItemClickListener mToggleData = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            int state = RadioInfo.this.mTelephonyManager.getDataState();
            if (state == 0) {
                RadioInfo.this.phone.setUserDataEnabled(true);
            } else if (state == 2) {
                RadioInfo.this.phone.setUserDataEnabled(false);
            }
            return true;
        }
    };
    OnClickListener mTriggerCarrierProvisioningButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("com.android.settings.TRIGGER_CARRIER_PROVISIONING");
            intent.setComponent(ComponentName.unflattenFromString("com.android.omadm.service/.DMIntentReceiver"));
            RadioInfo.this.sendBroadcast(intent);
        }
    };
    OnClickListener mUpdateSmscButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            RadioInfo.this.updateSmscButton.setEnabled(false);
            RadioInfo.this.phone.setSmscAddress(RadioInfo.this.smsc.getText().toString(), RadioInfo.this.mHandler.obtainMessage(1006));
        }
    };
    private TextView mUplinkKbps;
    private OnMenuItemClickListener mViewADNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW");
            try {
                intent.setClassName("com.android.phone", "com.android.phone.SimContacts");
                RadioInfo.this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
            }
            return true;
        }
    };
    private OnMenuItemClickListener mViewFDNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setClassName("com.android.phone", "com.android.phone.settings.fdn.FdnList");
            RadioInfo.this.startActivity(intent);
            return true;
        }
    };
    private OnMenuItemClickListener mViewSDNCallback = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("content://icc/sdn"));
            try {
                intent.setClassName("com.android.phone", "com.android.phone.ADNList");
                RadioInfo.this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
            }
            return true;
        }
    };
    private TextView number;
    private Button oemInfoButton;
    private TextView operatorName;
    private Phone phone = null;
    private Button pingTestButton;
    private Spinner preferredNetworkType;
    private Switch radioPowerOnSwitch;
    private TextView received;
    private Button refreshSmscButton;
    private TextView roamingState;
    private TextView sent;
    private EditText smsc;
    private Button triggercarrierProvisioningButton;
    private Button updateSmscButton;
    private TextView voiceNetwork;

    private void log(String s) {
        Log.d(TAG, s);
    }

    private void updatePhysicalChannelConfiguration(List<PhysicalChannelConfig> configs) {
        StringBuilder sb = new StringBuilder();
        String div = "";
        sb.append("{");
        if (configs != null) {
            for (PhysicalChannelConfig c : configs) {
                sb.append(div);
                sb.append(c);
                div = ",";
            }
        }
        sb.append("}");
        this.mPhyChanConfig.setText(sb.toString());
    }

    private void updatePreferredNetworkType(int type) {
        if (type >= mPreferredNetworkLabels.length || type < 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("EVENT_QUERY_PREFERRED_TYPE_DONE: unknown type=");
            stringBuilder.append(type);
            log(stringBuilder.toString());
            type = mPreferredNetworkLabels.length - 1;
        }
        this.mPreferredNetworkTypeResult = type;
        this.preferredNetworkType.setSelection(this.mPreferredNetworkTypeResult, true);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (Process.myUserHandle().isSystem()) {
            setContentView(R.layout.radio_info);
            log("Started onCreate");
            this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
            this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
            this.phone = PhoneFactory.getDefaultPhone();
            this.mImsManager = ImsManager.getInstance(getApplicationContext(), SubscriptionManager.getDefaultVoicePhoneId());
            this.mDeviceId = (TextView) findViewById(R.id.imei);
            this.number = (TextView) findViewById(R.id.number);
            this.mSubscriberId = (TextView) findViewById(R.id.imsi);
            this.callState = (TextView) findViewById(R.id.call);
            this.operatorName = (TextView) findViewById(R.id.operator);
            this.roamingState = (TextView) findViewById(R.id.roaming);
            this.gsmState = (TextView) findViewById(R.id.gsm);
            this.gprsState = (TextView) findViewById(R.id.gprs);
            this.voiceNetwork = (TextView) findViewById(R.id.voice_network);
            this.dataNetwork = (TextView) findViewById(R.id.data_network);
            this.dBm = (TextView) findViewById(R.id.dbm);
            this.mMwi = (TextView) findViewById(R.id.mwi);
            this.mCfi = (TextView) findViewById(R.id.cfi);
            this.mLocation = (TextView) findViewById(R.id.location);
            this.mNeighboringCids = (TextView) findViewById(R.id.neighboring);
            this.mCellInfo = (TextView) findViewById(R.id.cellinfo);
            this.mDcRtInfoTv = (TextView) findViewById(R.id.dcrtinfo);
            this.sent = (TextView) findViewById(R.id.sent);
            this.received = (TextView) findViewById(R.id.received);
            this.smsc = (EditText) findViewById(R.id.smsc);
            this.dnsCheckState = (TextView) findViewById(R.id.dnsCheckState);
            this.mPingHostnameV4 = (TextView) findViewById(R.id.pingHostnameV4);
            this.mPingHostnameV6 = (TextView) findViewById(R.id.pingHostnameV6);
            this.mHttpClientTest = (TextView) findViewById(R.id.httpClientTest);
            this.mPhyChanConfig = (TextView) findViewById(R.id.phy_chan_config);
            this.preferredNetworkType = (Spinner) findViewById(R.id.preferredNetworkType);
            ArrayAdapter<String> adapter = new ArrayAdapter(this, 17367048, mPreferredNetworkLabels);
            adapter.setDropDownViewResource(17367049);
            this.preferredNetworkType.setAdapter(adapter);
            this.cellInfoRefreshRateSpinner = (Spinner) findViewById(R.id.cell_info_rate_select);
            ArrayAdapter<String> cellInfoAdapter = new ArrayAdapter(this, 17367048, mCellInfoRefreshRateLabels);
            cellInfoAdapter.setDropDownViewResource(17367049);
            this.cellInfoRefreshRateSpinner.setAdapter(cellInfoAdapter);
            this.imsVolteProvisionedSwitch = (Switch) findViewById(R.id.volte_provisioned_switch);
            this.imsVtProvisionedSwitch = (Switch) findViewById(R.id.vt_provisioned_switch);
            this.imsWfcProvisionedSwitch = (Switch) findViewById(R.id.wfc_provisioned_switch);
            this.eabProvisionedSwitch = (Switch) findViewById(R.id.eab_provisioned_switch);
            this.radioPowerOnSwitch = (Switch) findViewById(R.id.radio_power);
            this.mDownlinkKbps = (TextView) findViewById(R.id.dl_kbps);
            this.mUplinkKbps = (TextView) findViewById(R.id.ul_kbps);
            updateBandwidths(0, 0);
            this.pingTestButton = (Button) findViewById(R.id.ping_test);
            this.pingTestButton.setOnClickListener(this.mPingButtonHandler);
            this.updateSmscButton = (Button) findViewById(R.id.update_smsc);
            this.updateSmscButton.setOnClickListener(this.mUpdateSmscButtonHandler);
            this.refreshSmscButton = (Button) findViewById(R.id.refresh_smsc);
            this.refreshSmscButton.setOnClickListener(this.mRefreshSmscButtonHandler);
            this.dnsCheckToggleButton = (Button) findViewById(R.id.dns_check_toggle);
            this.dnsCheckToggleButton.setOnClickListener(this.mDnsCheckButtonHandler);
            this.carrierProvisioningButton = (Button) findViewById(R.id.carrier_provisioning);
            this.carrierProvisioningButton.setOnClickListener(this.mCarrierProvisioningButtonHandler);
            this.triggercarrierProvisioningButton = (Button) findViewById(R.id.trigger_carrier_provisioning);
            this.triggercarrierProvisioningButton.setOnClickListener(this.mTriggerCarrierProvisioningButtonHandler);
            this.oemInfoButton = (Button) findViewById(R.id.oem_info);
            this.oemInfoButton.setOnClickListener(this.mOemInfoButtonHandler);
            if (getPackageManager().queryIntentActivities(new Intent("com.android.settings.OEM_RADIO_INFO"), 0).size() == 0) {
                this.oemInfoButton.setEnabled(false);
            }
            this.mCellInfoRefreshRateIndex = 0;
            this.mPreferredNetworkTypeResult = mPreferredNetworkLabels.length - 1;
            this.phone.getPreferredNetworkType(this.mHandler.obtainMessage(1000));
            restoreFromBundle(icicle);
            return;
        }
        Log.e(TAG, "Not run from system user, don't do anything.");
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        log("Started onResume");
        updateMessageWaiting();
        updateCallRedirect();
        updateDataState();
        updateDataStats2();
        updateRadioPowerState();
        updateImsProvisionedState();
        updateProperties();
        updateDnsCheckState();
        updateNetworkType();
        updateNeighboringCids(this.mNeighboringCellResult);
        updateLocation(this.mCellLocationResult);
        updateCellInfo(this.mCellInfoResult);
        this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
        this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        this.cellInfoRefreshRateSpinner.setOnItemSelectedListener(this.mCellInfoRefreshRateHandler);
        this.cellInfoRefreshRateSpinner.setSelection(this.mCellInfoRefreshRateIndex);
        this.preferredNetworkType.setSelection(this.mPreferredNetworkTypeResult, true);
        this.preferredNetworkType.setOnItemSelectedListener(this.mPreferredNetworkHandler);
        this.radioPowerOnSwitch.setOnCheckedChangeListener(this.mRadioPowerOnChangeListener);
        this.imsVolteProvisionedSwitch.setOnCheckedChangeListener(this.mImsVolteCheckedChangeListener);
        this.imsVtProvisionedSwitch.setOnCheckedChangeListener(this.mImsVtCheckedChangeListener);
        this.imsWfcProvisionedSwitch.setOnCheckedChangeListener(this.mImsWfcCheckedChangeListener);
        this.eabProvisionedSwitch.setOnCheckedChangeListener(this.mEabCheckedChangeListener);
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1050109);
        this.mConnectivityManager.registerNetworkCallback(this.mDefaultNetworkRequest, this.mNetworkCallback, this.mHandler);
        this.smsc.clearFocus();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        log("onPause: unregister phone & data intents");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        this.mTelephonyManager.setCellInfoListRate(Integer.MAX_VALUE);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    private void restoreFromBundle(Bundle b) {
        if (b != null) {
            this.mPingHostnameResultV4 = b.getString("mPingHostnameResultV4", "");
            this.mPingHostnameResultV6 = b.getString("mPingHostnameResultV6", "");
            this.mHttpClientTestResult = b.getString("mHttpClientTestResult", "");
            this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
            this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
            this.mHttpClientTest.setText(this.mHttpClientTestResult);
            this.mPreferredNetworkTypeResult = b.getInt("mPreferredNetworkTypeResult", mPreferredNetworkLabels.length - 1);
            this.mCellInfoRefreshRateIndex = b.getInt("mCellInfoRefreshRateIndex", 0);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("mPingHostnameResultV4", this.mPingHostnameResultV4);
        outState.putString("mPingHostnameResultV6", this.mPingHostnameResultV6);
        outState.putString("mHttpClientTestResult", this.mHttpClientTestResult);
        outState.putInt("mPreferredNetworkTypeResult", this.mPreferredNetworkTypeResult);
        outState.putInt("mCellInfoRefreshRateIndex", this.mCellInfoRefreshRateIndex);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 0, R.string.radioInfo_menu_viewADN).setOnMenuItemClickListener(this.mViewADNCallback);
        menu.add(1, 2, 0, R.string.radioInfo_menu_viewFDN).setOnMenuItemClickListener(this.mViewFDNCallback);
        menu.add(1, 3, 0, R.string.radioInfo_menu_viewSDN).setOnMenuItemClickListener(this.mViewSDNCallback);
        menu.add(1, 4, 0, R.string.radioInfo_menu_getIMS).setOnMenuItemClickListener(this.mGetImsStatus);
        menu.add(1, 5, 0, R.string.radio_info_data_connection_disable).setOnMenuItemClickListener(this.mToggleData);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(5);
        int state = this.mTelephonyManager.getDataState();
        boolean visible = true;
        if (state != 0) {
            switch (state) {
                case 2:
                case 3:
                    item.setTitle(R.string.radio_info_data_connection_disable);
                    break;
                default:
                    visible = false;
                    break;
            }
        }
        item.setTitle(R.string.radio_info_data_connection_enable);
        item.setVisible(visible);
        return true;
    }

    private void updateDnsCheckState() {
        this.dnsCheckState.setText(this.phone.isDnsCheckDisabled() ? "0.0.0.0 allowed" : "0.0.0.0 not allowed");
    }

    private void updateBandwidths(int dlbw, int ulbw) {
        int i = -1;
        int i2 = (dlbw < 0 || dlbw == Integer.MAX_VALUE) ? -1 : dlbw;
        dlbw = i2;
        if (ulbw >= 0 && ulbw != Integer.MAX_VALUE) {
            i = ulbw;
        }
        ulbw = i;
        this.mDownlinkKbps.setText(String.format("%-5d", new Object[]{Integer.valueOf(dlbw)}));
        this.mUplinkKbps.setText(String.format("%-5d", new Object[]{Integer.valueOf(ulbw)}));
    }

    private final void updateSignalStrength(SignalStrength signalStrength) {
        Resources r = getResources();
        int signalDbm = signalStrength.getDbm();
        int signalAsu = signalStrength.getAsuLevel();
        if (-1 == signalAsu) {
            signalAsu = 0;
        }
        TextView textView = this.dBm;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(signalDbm));
        stringBuilder.append(" ");
        stringBuilder.append(r.getString(R.string.radioInfo_display_dbm));
        stringBuilder.append("   ");
        stringBuilder.append(String.valueOf(signalAsu));
        stringBuilder.append(" ");
        stringBuilder.append(r.getString(R.string.radioInfo_display_asu));
        textView.setText(stringBuilder.toString());
    }

    private final void updateLocation(CellLocation location) {
        Resources r = getResources();
        int lac;
        int cid;
        if (location instanceof GsmCellLocation) {
            GsmCellLocation loc = (GsmCellLocation) location;
            lac = loc.getLac();
            cid = loc.getCid();
            TextView textView = this.mLocation;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(r.getString(R.string.radioInfo_lac));
            stringBuilder.append(" = ");
            stringBuilder.append(lac == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(lac));
            stringBuilder.append("   ");
            stringBuilder.append(r.getString(R.string.radioInfo_cid));
            stringBuilder.append(" = ");
            stringBuilder.append(cid == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(cid));
            textView.setText(stringBuilder.toString());
        } else if (location instanceof CdmaCellLocation) {
            CdmaCellLocation loc2 = (CdmaCellLocation) location;
            lac = loc2.getBaseStationId();
            cid = loc2.getSystemId();
            int nid = loc2.getNetworkId();
            int lat = loc2.getBaseStationLatitude();
            int lon = loc2.getBaseStationLongitude();
            TextView textView2 = this.mLocation;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("BID = ");
            stringBuilder2.append(lac == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(lac));
            stringBuilder2.append("   SID = ");
            stringBuilder2.append(cid == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(cid));
            stringBuilder2.append("   NID = ");
            stringBuilder2.append(nid == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(nid));
            stringBuilder2.append("\nLAT = ");
            stringBuilder2.append(lat == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(lat));
            stringBuilder2.append("   LONG = ");
            stringBuilder2.append(lon == -1 ? EnvironmentCompat.MEDIA_UNKNOWN : Integer.toHexString(lon));
            textView2.setText(stringBuilder2.toString());
        } else {
            this.mLocation.setText(EnvironmentCompat.MEDIA_UNKNOWN);
        }
    }

    private final void updateNeighboringCids(List<NeighboringCellInfo> cids) {
        StringBuilder sb = new StringBuilder();
        if (cids == null) {
            sb.append(EnvironmentCompat.MEDIA_UNKNOWN);
        } else if (cids.isEmpty()) {
            sb.append("no neighboring cells");
        } else {
            for (NeighboringCellInfo cell : cids) {
                sb.append(cell.toString());
                sb.append(" ");
            }
        }
        this.mNeighboringCids.setText(sb.toString());
    }

    private final String getCellInfoDisplayString(int i) {
        return i != Integer.MAX_VALUE ? Integer.toString(i) : "";
    }

    private final String getCellInfoDisplayString(long i) {
        return i != Long.MAX_VALUE ? Long.toString(i) : "";
    }

    private final String getConnectionStatusString(CellInfo ci) {
        String regStr = "";
        String connStatStr = "";
        String connector = "";
        if (ci.isRegistered()) {
            regStr = "R";
        }
        int cellConnectionStatus = ci.getCellConnectionStatus();
        if (cellConnectionStatus != Integer.MAX_VALUE) {
            switch (cellConnectionStatus) {
                case 0:
                    connStatStr = "N";
                    break;
                case 1:
                    connStatStr = "P";
                    break;
                case 2:
                    connStatStr = "S";
                    break;
            }
        }
        if (!(TextUtils.isEmpty(regStr) || TextUtils.isEmpty(connStatStr))) {
            connector = "+";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(regStr);
        stringBuilder.append(connector);
        stringBuilder.append(connStatStr);
        return stringBuilder.toString();
    }

    private final String buildCdmaInfoString(CellInfoCdma ci) {
        CellIdentityCdma cidCdma = ci.getCellIdentity();
        CellSignalStrengthCdma ssCdma = ci.getCellSignalStrength();
        return String.format("%-3.3s %-5.5s %-5.5s %-5.5s %-6.6s %-6.6s %-6.6s %-6.6s %-5.5s", new Object[]{getConnectionStatusString(ci), getCellInfoDisplayString(cidCdma.getSystemId()), getCellInfoDisplayString(cidCdma.getNetworkId()), getCellInfoDisplayString(cidCdma.getBasestationId()), getCellInfoDisplayString(ssCdma.getCdmaDbm()), getCellInfoDisplayString(ssCdma.getCdmaEcio()), getCellInfoDisplayString(ssCdma.getEvdoDbm()), getCellInfoDisplayString(ssCdma.getEvdoEcio()), getCellInfoDisplayString(ssCdma.getEvdoSnr())});
    }

    private final String buildGsmInfoString(CellInfoGsm ci) {
        CellIdentityGsm cidGsm = ci.getCellIdentity();
        CellSignalStrengthGsm ssGsm = ci.getCellSignalStrength();
        return String.format("%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-4.4s %-4.4s\n", new Object[]{getConnectionStatusString(ci), getCellInfoDisplayString(cidGsm.getMcc()), getCellInfoDisplayString(cidGsm.getMnc()), getCellInfoDisplayString(cidGsm.getLac()), getCellInfoDisplayString(cidGsm.getCid()), getCellInfoDisplayString(cidGsm.getArfcn()), getCellInfoDisplayString(cidGsm.getBsic()), getCellInfoDisplayString(ssGsm.getDbm())});
    }

    private final String buildLteInfoString(CellInfoLte ci) {
        CellIdentityLte cidLte = ci.getCellIdentity();
        CellSignalStrengthLte ssLte = ci.getCellSignalStrength();
        return String.format("%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-3.3s %-6.6s %-2.2s %-4.4s %-4.4s %-2.2s\n", new Object[]{getConnectionStatusString(ci), getCellInfoDisplayString(cidLte.getMcc()), getCellInfoDisplayString(cidLte.getMnc()), getCellInfoDisplayString(cidLte.getTac()), getCellInfoDisplayString(cidLte.getCi()), getCellInfoDisplayString(cidLte.getPci()), getCellInfoDisplayString(cidLte.getEarfcn()), getCellInfoDisplayString(cidLte.getBandwidth()), getCellInfoDisplayString(ssLte.getDbm()), getCellInfoDisplayString(ssLte.getRsrq()), getCellInfoDisplayString(ssLte.getTimingAdvance())});
    }

    private final String buildWcdmaInfoString(CellInfoWcdma ci) {
        CellIdentityWcdma cidWcdma = ci.getCellIdentity();
        CellSignalStrengthWcdma ssWcdma = ci.getCellSignalStrength();
        return String.format("%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-3.3s %-4.4s\n", new Object[]{getConnectionStatusString(ci), getCellInfoDisplayString(cidWcdma.getMcc()), getCellInfoDisplayString(cidWcdma.getMnc()), getCellInfoDisplayString(cidWcdma.getLac()), getCellInfoDisplayString(cidWcdma.getCid()), getCellInfoDisplayString(cidWcdma.getUarfcn()), getCellInfoDisplayString(cidWcdma.getPsc()), getCellInfoDisplayString(ssWcdma.getDbm())});
    }

    private final String buildCellInfoString(List<CellInfo> arrayCi) {
        String value = new String();
        StringBuilder cdmaCells = new StringBuilder();
        StringBuilder gsmCells = new StringBuilder();
        StringBuilder lteCells = new StringBuilder();
        StringBuilder wcdmaCells = new StringBuilder();
        if (arrayCi != null) {
            StringBuilder stringBuilder;
            for (CellInfo ci : arrayCi) {
                if (ci instanceof CellInfoLte) {
                    lteCells.append(buildLteInfoString((CellInfoLte) ci));
                } else if (ci instanceof CellInfoWcdma) {
                    wcdmaCells.append(buildWcdmaInfoString((CellInfoWcdma) ci));
                } else if (ci instanceof CellInfoGsm) {
                    gsmCells.append(buildGsmInfoString((CellInfoGsm) ci));
                } else if (ci instanceof CellInfoCdma) {
                    cdmaCells.append(buildCdmaInfoString((CellInfoCdma) ci));
                }
            }
            if (lteCells.length() != 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(String.format("LTE\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-3.3s %-6.6s %-2.2s %-4.4s %-4.4s %-2.2s\n", new Object[]{"SRV", "MCC", "MNC", "TAC", "CID", "PCI", "EARFCN", "BW", "RSRP", "RSRQ", "TA"}));
                value = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(lteCells.toString());
                value = stringBuilder.toString();
            }
            if (wcdmaCells.length() != 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(String.format("WCDMA\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-3.3s %-4.4s\n", new Object[]{"SRV", "MCC", "MNC", "LAC", "CID", "UARFCN", "PSC", "RSCP"}));
                value = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(wcdmaCells.toString());
                value = stringBuilder.toString();
            }
            if (gsmCells.length() != 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(String.format("GSM\n%-3.3s %-3.3s %-3.3s %-5.5s %-5.5s %-6.6s %-4.4s %-4.4s\n", new Object[]{"SRV", "MCC", "MNC", "LAC", "CID", "ARFCN", "BSIC", "RSSI"}));
                value = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(gsmCells.toString());
                value = stringBuilder.toString();
            }
            if (cdmaCells.length() != 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(String.format("CDMA/EVDO\n%-3.3s %-5.5s %-5.5s %-5.5s %-6.6s %-6.6s %-6.6s %-6.6s %-5.5s\n", new Object[]{"SRV", "SID", "NID", "BSID", "C-RSSI", "C-ECIO", "E-RSSI", "E-ECIO", "E-SNR"}));
                value = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append(value);
                stringBuilder.append(cdmaCells.toString());
                value = stringBuilder.toString();
            }
        } else {
            value = EnvironmentCompat.MEDIA_UNKNOWN;
        }
        return value.toString();
    }

    private final void updateCellInfo(List<CellInfo> arrayCi) {
        this.mCellInfo.setText(buildCellInfoString(arrayCi));
    }

    private final void updateDcRtInfoTv(DataConnectionRealTimeInfo dcRtInfo) {
        this.mDcRtInfoTv.setText(dcRtInfo.toString());
    }

    private final void updateMessageWaiting() {
        this.mMwi.setText(String.valueOf(this.mMwiValue));
    }

    private final void updateCallRedirect() {
        this.mCfi.setText(String.valueOf(this.mCfiValue));
    }

    private final void updateServiceState(ServiceState serviceState) {
        int state = serviceState.getState();
        Resources r = getResources();
        String display = r.getString(R.string.radioInfo_unknown);
        switch (state) {
            case 0:
                display = r.getString(R.string.radioInfo_service_in);
                break;
            case 1:
            case 2:
                display = r.getString(R.string.radioInfo_service_emergency);
                break;
            case 3:
                display = r.getString(R.string.radioInfo_service_off);
                break;
        }
        this.gsmState.setText(display);
        if (serviceState.getRoaming()) {
            this.roamingState.setText(R.string.radioInfo_roaming_in);
        } else {
            this.roamingState.setText(R.string.radioInfo_roaming_not);
        }
        this.operatorName.setText(serviceState.getOperatorAlphaLong());
    }

    private final void updatePhoneState(int state) {
        Resources r = getResources();
        String display = r.getString(R.string.radioInfo_unknown);
        switch (state) {
            case 0:
                display = r.getString(R.string.radioInfo_phone_idle);
                break;
            case 1:
                display = r.getString(R.string.radioInfo_phone_ringing);
                break;
            case 2:
                display = r.getString(R.string.radioInfo_phone_offhook);
                break;
        }
        this.callState.setText(display);
    }

    private final void updateDataState() {
        int state = this.mTelephonyManager.getDataState();
        Resources r = getResources();
        String display = r.getString(R.string.radioInfo_unknown);
        switch (state) {
            case 0:
                display = r.getString(R.string.radioInfo_data_disconnected);
                break;
            case 1:
                display = r.getString(R.string.radioInfo_data_connecting);
                break;
            case 2:
                display = r.getString(R.string.radioInfo_data_connected);
                break;
            case 3:
                display = r.getString(R.string.radioInfo_data_suspended);
                break;
        }
        this.gprsState.setText(display);
    }

    private final void updateNetworkType() {
        if (this.phone != null) {
            ServiceState ss = this.phone.getServiceState();
            this.dataNetwork.setText(ServiceState.rilRadioTechnologyToString(this.phone.getServiceState().getRilDataRadioTechnology()));
            this.voiceNetwork.setText(ServiceState.rilRadioTechnologyToString(this.phone.getServiceState().getRilVoiceRadioTechnology()));
        }
    }

    private final void updateProperties() {
        Resources r = getResources();
        String s = this.phone.getDeviceId();
        if (s == null) {
            s = r.getString(R.string.radioInfo_unknown);
        }
        this.mDeviceId.setText(s);
        s = this.phone.getSubscriberId();
        if (s == null) {
            s = r.getString(R.string.radioInfo_unknown);
        }
        this.mSubscriberId.setText(s);
        s = this.phone.getLine1Number();
        if (s == null) {
            s = r.getString(R.string.radioInfo_unknown);
        }
        this.number.setText(s);
    }

    private final void updateDataStats2() {
        Resources r = getResources();
        long txPackets = TrafficStats.getMobileTxPackets();
        long rxPackets = TrafficStats.getMobileRxPackets();
        long txBytes = TrafficStats.getMobileTxBytes();
        long rxBytes = TrafficStats.getMobileRxBytes();
        String packets = r.getString(R.string.radioInfo_display_packets);
        String bytes = r.getString(R.string.radioInfo_display_bytes);
        TextView textView = this.sent;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(txPackets);
        stringBuilder.append(" ");
        stringBuilder.append(packets);
        stringBuilder.append(", ");
        stringBuilder.append(txBytes);
        stringBuilder.append(" ");
        stringBuilder.append(bytes);
        textView.setText(stringBuilder.toString());
        textView = this.received;
        stringBuilder = new StringBuilder();
        stringBuilder.append(rxPackets);
        stringBuilder.append(" ");
        stringBuilder.append(packets);
        stringBuilder.append(", ");
        stringBuilder.append(rxBytes);
        stringBuilder.append(" ");
        stringBuilder.append(bytes);
        textView.setText(stringBuilder.toString());
    }

    private final void pingHostname() {
        try {
            if (Runtime.getRuntime().exec("ping -c 1 www.google.com").waitFor() == 0) {
                this.mPingHostnameResultV4 = "Pass";
            } else {
                this.mPingHostnameResultV4 = String.format("Fail(%d)", new Object[]{Integer.valueOf(Runtime.getRuntime().exec("ping -c 1 www.google.com").waitFor())});
            }
        } catch (IOException e) {
            try {
                this.mPingHostnameResultV4 = "Fail: IOException";
            } catch (InterruptedException e2) {
                String str = "Fail: InterruptedException";
                this.mPingHostnameResultV6 = str;
                this.mPingHostnameResultV4 = str;
                return;
            }
        }
        try {
            if (Runtime.getRuntime().exec("ping6 -c 1 www.google.com").waitFor() == 0) {
                this.mPingHostnameResultV6 = "Pass";
                return;
            }
            this.mPingHostnameResultV6 = String.format("Fail(%d)", new Object[]{Integer.valueOf(Runtime.getRuntime().exec("ping6 -c 1 www.google.com").waitFor())});
        } catch (IOException e3) {
            this.mPingHostnameResultV6 = "Fail: IOException";
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    private void httpClientTest() {
        /*
        r4 = this;
        r0 = 0;
        r1 = new java.net.URL;	 Catch:{ IOException -> 0x003b }
        r2 = "https://www.google.com";
        r1.<init>(r2);	 Catch:{ IOException -> 0x003b }
        r2 = r1.openConnection();	 Catch:{ IOException -> 0x003b }
        r2 = (java.net.HttpURLConnection) r2;	 Catch:{ IOException -> 0x003b }
        r0 = r2;
        r2 = r0.getResponseCode();	 Catch:{ IOException -> 0x003b }
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        if (r2 != r3) goto L_0x001c;
    L_0x0017:
        r2 = "Pass";
        r4.mHttpClientTestResult = r2;	 Catch:{ IOException -> 0x003b }
        goto L_0x0033;
    L_0x001c:
        r2 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x003b }
        r2.<init>();	 Catch:{ IOException -> 0x003b }
        r3 = "Fail: Code: ";
        r2.append(r3);	 Catch:{ IOException -> 0x003b }
        r3 = r0.getResponseMessage();	 Catch:{ IOException -> 0x003b }
        r2.append(r3);	 Catch:{ IOException -> 0x003b }
        r2 = r2.toString();	 Catch:{ IOException -> 0x003b }
        r4.mHttpClientTestResult = r2;	 Catch:{ IOException -> 0x003b }
    L_0x0033:
        if (r0 == 0) goto L_0x0043;
    L_0x0035:
        r0.disconnect();
        goto L_0x0043;
    L_0x0039:
        r1 = move-exception;
        goto L_0x0044;
    L_0x003b:
        r1 = move-exception;
        r2 = "Fail: IOException";
        r4.mHttpClientTestResult = r2;	 Catch:{ all -> 0x0039 }
        if (r0 == 0) goto L_0x0043;
    L_0x0042:
        goto L_0x0035;
    L_0x0043:
        return;
    L_0x0044:
        if (r0 == 0) goto L_0x0049;
    L_0x0046:
        r0.disconnect();
    L_0x0049:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.RadioInfo.httpClientTest():void");
    }

    private void refreshSmsc() {
        this.phone.getSmscAddress(this.mHandler.obtainMessage(1005));
    }

    private final void updateAllCellInfo() {
        this.mCellInfo.setText("");
        this.mNeighboringCids.setText("");
        this.mLocation.setText("");
        final Runnable updateAllCellInfoResults = new Runnable() {
            public void run() {
                RadioInfo.this.updateNeighboringCids(RadioInfo.this.mNeighboringCellResult);
                RadioInfo.this.updateLocation(RadioInfo.this.mCellLocationResult);
                RadioInfo.this.updateCellInfo(RadioInfo.this.mCellInfoResult);
            }
        };
        new Thread() {
            public void run() {
                RadioInfo.this.mCellInfoResult = RadioInfo.this.mTelephonyManager.getAllCellInfo();
                RadioInfo.this.mCellLocationResult = RadioInfo.this.mTelephonyManager.getCellLocation();
                RadioInfo.this.mNeighboringCellResult = RadioInfo.this.mTelephonyManager.getNeighboringCellInfo();
                RadioInfo.this.mHandler.post(updateAllCellInfoResults);
            }
        }.start();
    }

    private final void updatePingState() {
        this.mPingHostnameResultV4 = getResources().getString(R.string.radioInfo_unknown);
        this.mPingHostnameResultV6 = getResources().getString(R.string.radioInfo_unknown);
        this.mHttpClientTestResult = getResources().getString(R.string.radioInfo_unknown);
        this.mPingHostnameV4.setText(this.mPingHostnameResultV4);
        this.mPingHostnameV6.setText(this.mPingHostnameResultV6);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        final Runnable updatePingResults = new Runnable() {
            public void run() {
                RadioInfo.this.mPingHostnameV4.setText(RadioInfo.this.mPingHostnameResultV4);
                RadioInfo.this.mPingHostnameV6.setText(RadioInfo.this.mPingHostnameResultV6);
                RadioInfo.this.mHttpClientTest.setText(RadioInfo.this.mHttpClientTestResult);
            }
        };
        new Thread() {
            public void run() {
                RadioInfo.this.pingHostname();
                RadioInfo.this.mHandler.post(updatePingResults);
            }
        }.start();
        new Thread() {
            public void run() {
                RadioInfo.this.httpClientTest();
                RadioInfo.this.mHandler.post(updatePingResults);
            }
        }.start();
    }

    private boolean isRadioOn() {
        return this.phone.getServiceState().getState() != 3;
    }

    private void updateRadioPowerState() {
        this.radioPowerOnSwitch.setOnCheckedChangeListener(null);
        this.radioPowerOnSwitch.setChecked(isRadioOn());
        this.radioPowerOnSwitch.setOnCheckedChangeListener(this.mRadioPowerOnChangeListener);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImsVolteProvisionedState(boolean state) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setImsVolteProvisioned state: ");
        stringBuilder.append(state ? "on" : "off");
        Log.d(str, stringBuilder.toString());
        setImsConfigProvisionedState(10, state);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImsVtProvisionedState(boolean state) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setImsVtProvisioned() state: ");
        stringBuilder.append(state ? "on" : "off");
        Log.d(str, stringBuilder.toString());
        setImsConfigProvisionedState(11, state);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImsWfcProvisionedState(boolean state) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setImsWfcProvisioned() state: ");
        stringBuilder.append(state ? "on" : "off");
        Log.d(str, stringBuilder.toString());
        setImsConfigProvisionedState(28, state);
    }

    /* Access modifiers changed, original: 0000 */
    public void setEabProvisionedState(boolean state) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setEabProvisioned() state: ");
        stringBuilder.append(state ? "on" : "off");
        Log.d(str, stringBuilder.toString());
        setImsConfigProvisionedState(25, state);
    }

    /* Access modifiers changed, original: 0000 */
    public void setImsConfigProvisionedState(final int configItem, final boolean state) {
        if (this.phone != null && this.mImsManager != null) {
            QueuedWork.queue(new Runnable() {
                public void run() {
                    try {
                        RadioInfo.this.mImsManager.getConfigInterface().setProvisionedValue(configItem, state);
                    } catch (ImsException e) {
                        Log.e(RadioInfo.TAG, "setImsConfigProvisioned() exception:", e);
                    }
                }
            }, false);
        }
    }

    private boolean isImsVolteProvisioned() {
        boolean z = false;
        if (this.phone == null || this.mImsManager == null) {
            return false;
        }
        ImsManager imsManager = this.mImsManager;
        if (ImsManager.isVolteEnabledByPlatform(this.phone.getContext())) {
            imsManager = this.mImsManager;
            if (ImsManager.isVolteProvisionedOnDevice(this.phone.getContext())) {
                z = true;
            }
        }
        return z;
    }

    private boolean isImsVtProvisioned() {
        boolean z = false;
        if (this.phone == null || this.mImsManager == null) {
            return false;
        }
        ImsManager imsManager = this.mImsManager;
        if (ImsManager.isVtEnabledByPlatform(this.phone.getContext())) {
            imsManager = this.mImsManager;
            if (ImsManager.isVtProvisionedOnDevice(this.phone.getContext())) {
                z = true;
            }
        }
        return z;
    }

    private boolean isImsWfcProvisioned() {
        boolean z = false;
        if (this.phone == null || this.mImsManager == null) {
            return false;
        }
        ImsManager imsManager = this.mImsManager;
        if (ImsManager.isWfcEnabledByPlatform(this.phone.getContext())) {
            imsManager = this.mImsManager;
            if (ImsManager.isWfcProvisionedOnDevice(this.phone.getContext())) {
                z = true;
            }
        }
        return z;
    }

    private boolean isEabProvisioned() {
        return isFeatureProvisioned(25, false);
    }

    private boolean isFeatureProvisioned(int featureId, boolean defaultValue) {
        boolean provisioned = defaultValue;
        if (this.mImsManager != null) {
            try {
                ImsConfig imsConfig = this.mImsManager.getConfigInterface();
                if (imsConfig != null) {
                    boolean z = true;
                    if (imsConfig.getProvisionedValue(featureId) != 1) {
                        z = false;
                    }
                    provisioned = z;
                }
            } catch (ImsException ex) {
                Log.e(TAG, "isFeatureProvisioned() exception:", ex);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isFeatureProvisioned() featureId=");
        stringBuilder.append(featureId);
        stringBuilder.append(" provisioned=");
        stringBuilder.append(provisioned);
        log(stringBuilder.toString());
        return provisioned;
    }

    private static boolean isEabEnabledByPlatform(Context context) {
        if (context != null) {
            CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
            if (configManager != null && configManager.getConfig().getBoolean("use_rcs_presence_bool")) {
                return true;
            }
        }
        return false;
    }

    private void updateImsProvisionedState() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("updateImsProvisionedState isImsVolteProvisioned()=");
        stringBuilder.append(isImsVolteProvisioned());
        log(stringBuilder.toString());
        this.imsVolteProvisionedSwitch.setOnCheckedChangeListener(null);
        this.imsVolteProvisionedSwitch.setChecked(isImsVolteProvisioned());
        this.imsVolteProvisionedSwitch.setOnCheckedChangeListener(this.mImsVolteCheckedChangeListener);
        Switch switchR = this.imsVolteProvisionedSwitch;
        ImsManager imsManager = this.mImsManager;
        switchR.setEnabled(ImsManager.isVolteEnabledByPlatform(this.phone.getContext()));
        this.imsVtProvisionedSwitch.setOnCheckedChangeListener(null);
        this.imsVtProvisionedSwitch.setChecked(isImsVtProvisioned());
        this.imsVtProvisionedSwitch.setOnCheckedChangeListener(this.mImsVtCheckedChangeListener);
        switchR = this.imsVtProvisionedSwitch;
        imsManager = this.mImsManager;
        switchR.setEnabled(ImsManager.isVtEnabledByPlatform(this.phone.getContext()));
        this.imsWfcProvisionedSwitch.setOnCheckedChangeListener(null);
        this.imsWfcProvisionedSwitch.setChecked(isImsWfcProvisioned());
        this.imsWfcProvisionedSwitch.setOnCheckedChangeListener(this.mImsWfcCheckedChangeListener);
        switchR = this.imsWfcProvisionedSwitch;
        imsManager = this.mImsManager;
        switchR.setEnabled(ImsManager.isWfcEnabledByPlatform(this.phone.getContext()));
        this.eabProvisionedSwitch.setOnCheckedChangeListener(null);
        this.eabProvisionedSwitch.setChecked(isEabProvisioned());
        this.eabProvisionedSwitch.setOnCheckedChangeListener(this.mEabCheckedChangeListener);
        this.eabProvisionedSwitch.setEnabled(isEabEnabledByPlatform(this.phone.getContext()));
    }
}
