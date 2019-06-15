package com.android.settings.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.UserManager;
import android.security.KeyStore;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.android.settings.ProxySelector;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.ThreadUtils;
import com.android.settingslib.wifi.AccessPoint;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class WifiConfigController implements TextWatcher, OnItemSelectedListener, OnCheckedChangeListener, OnEditorActionListener, OnKeyListener {
    private static final int DHCP = 0;
    public static final int HIDDEN_NETWORK = 1;
    public static final int NOT_HIDDEN_NETWORK = 0;
    public static final int PROXY_NONE = 0;
    public static final int PROXY_PAC = 2;
    public static final int PROXY_STATIC = 1;
    private static final int SAFE_SELECT_8021XEAP = 3;
    private static final int STATIC_IP = 1;
    private static final String SYSTEM_CA_STORE_PATH = "/system/etc/security/cacerts";
    private static final String TAG = "WifiConfigController";
    private static final int[] WAPI_PSK_TYPE = new int[]{0, 1};
    public static final String WAPI_USER_CERTIFICATE = "WAPI_USER_";
    public static final int WIFI_EAP_METHOD_AKA = 5;
    public static final int WIFI_EAP_METHOD_AKA_PRIME = 6;
    public static final int WIFI_EAP_METHOD_PEAP = 0;
    public static final int WIFI_EAP_METHOD_PWD = 3;
    public static final int WIFI_EAP_METHOD_SIM = 4;
    public static final int WIFI_EAP_METHOD_TLS = 1;
    public static final int WIFI_EAP_METHOD_TTLS = 2;
    public static final int WIFI_PEAP_PHASE2_AKA = 4;
    public static final int WIFI_PEAP_PHASE2_AKA_PRIME = 5;
    public static final int WIFI_PEAP_PHASE2_GTC = 2;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2 = 1;
    public static final int WIFI_PEAP_PHASE2_NONE = 0;
    public static final int WIFI_PEAP_PHASE2_SIM = 3;
    private final AccessPoint mAccessPoint;
    private int mAccessPointSecurity;
    private final WifiConfigUiBase mConfigUi;
    private Context mContext;
    private ScrollView mDialogContainer;
    private TextView mDns1View;
    private TextView mDns2View;
    private String mDoNotProvideEapUserCertString;
    private String mDoNotValidateEapServerString;
    private TextView mEapAnonymousView;
    private Spinner mEapCaCertSpinner;
    private TextView mEapDomainView;
    private TextView mEapIdentityView;
    private Spinner mEapMethodSpinner;
    private Spinner mEapUserCertSpinner;
    private TextView mGatewayView;
    private boolean mHaveWapiCert = false;
    private Spinner mHiddenSettingsSpinner;
    private TextView mHiddenWarningView;
    private ProxyInfo mHttpProxy = null;
    private TextView mIpAddressView;
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private Spinner mIpSettingsSpinner;
    private String[] mLevels;
    private Spinner mMeteredSettingsSpinner;
    private int mMode;
    private String mMultipleCertSetString;
    private TextView mNetworkPrefixLengthView;
    private TextView mPasswordView;
    private ArrayAdapter<String> mPhase2Adapter;
    private final ArrayAdapter<String> mPhase2FullAdapter;
    private final ArrayAdapter<String> mPhase2PeapAdapter;
    private Spinner mPhase2Spinner;
    private TextView mProxyExclusionListView;
    private TextView mProxyHostView;
    private TextView mProxyPacView;
    private TextView mProxyPortView;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private Spinner mProxySettingsSpinner;
    private Spinner mSecuritySpinner;
    private CheckBox mShareThisWifiCheckBox;
    private CheckBox mSharedCheckBox;
    private Spinner mSimCardSpinner;
    private ArrayList<String> mSimDisplayNames;
    private TextView mSsidView;
    private StaticIpConfiguration mStaticIpConfiguration = null;
    private SubscriptionManager mSubscriptionManager = null;
    private TelephonyManager mTelephonyManager;
    private String mUnspecifiedCertString;
    private String mUseSystemCertsString;
    private final View mView;
    private Spinner mWapiCertSpinner;
    private Spinner mWapiPskTypeSpinner;
    private WifiManager mWifiManager;
    private int selectedSimCardNumber;

    public WifiConfigController(WifiConfigUiBase parent, View view, AccessPoint accessPoint, int mode) {
        AccessPoint accessPoint2 = accessPoint;
        this.mConfigUi = parent;
        this.mView = view;
        this.mAccessPoint = accessPoint2;
        this.mAccessPointSecurity = accessPoint2 == null ? 0 : accessPoint.getSecurity();
        this.mMode = mode;
        this.mContext = this.mConfigUi.getContext();
        Resources res = this.mContext.getResources();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mSimDisplayNames = new ArrayList();
        this.mLevels = res.getStringArray(R.array.wifi_signal);
        this.mPhase2PeapAdapter = new ArrayAdapter(this.mContext, 17367048, res.getStringArray(R.array.wifi_peap_phase2_entries));
        this.mPhase2PeapAdapter.setDropDownViewResource(17367049);
        this.mPhase2FullAdapter = new ArrayAdapter(this.mContext, 17367048, res.getStringArray(R.array.wifi_phase2_entries));
        this.mPhase2FullAdapter.setDropDownViewResource(17367049);
        this.mUnspecifiedCertString = this.mContext.getString(R.string.wifi_unspecified);
        this.mMultipleCertSetString = this.mContext.getString(R.string.wifi_multiple_cert_added);
        this.mUseSystemCertsString = this.mContext.getString(R.string.wifi_use_system_certs);
        this.mDoNotProvideEapUserCertString = this.mContext.getString(R.string.wifi_do_not_provide_eap_user_cert);
        this.mDoNotValidateEapServerString = this.mContext.getString(R.string.wifi_do_not_validate_eap_server);
        this.mDialogContainer = (ScrollView) this.mView.findViewById(R.id.dialog_scrollview);
        this.mIpSettingsSpinner = (Spinner) this.mView.findViewById(R.id.ip_settings);
        this.mIpSettingsSpinner.setOnItemSelectedListener(this);
        this.mProxySettingsSpinner = (Spinner) this.mView.findViewById(R.id.proxy_settings);
        this.mProxySettingsSpinner.setOnItemSelectedListener(this);
        this.mSharedCheckBox = (CheckBox) this.mView.findViewById(R.id.shared);
        this.mMeteredSettingsSpinner = (Spinner) this.mView.findViewById(R.id.metered_settings);
        this.mHiddenSettingsSpinner = (Spinner) this.mView.findViewById(R.id.hidden_settings);
        this.mHiddenSettingsSpinner.setOnItemSelectedListener(this);
        this.mHiddenWarningView = (TextView) this.mView.findViewById(R.id.hidden_settings_warning);
        this.mHiddenWarningView.setVisibility(this.mHiddenSettingsSpinner.getSelectedItemPosition() == 0 ? 8 : 0);
        this.mShareThisWifiCheckBox = (CheckBox) this.mView.findViewById(R.id.share_this_wifi);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (this.mAccessPoint == null) {
            this.mConfigUi.setTitle((int) R.string.wifi_add_network);
            this.mSsidView = (TextView) this.mView.findViewById(R.id.ssid);
            this.mSsidView.addTextChangedListener(this);
            this.mSecuritySpinner = (Spinner) this.mView.findViewById(R.id.security);
            this.mSecuritySpinner.setOnItemSelectedListener(this);
            this.mView.findViewById(R.id.type).setVisibility(0);
            showIpConfigFields();
            showProxyFields();
            this.mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(0);
            this.mView.findViewById(R.id.hidden_settings_field).setVisibility(0);
            ((CheckBox) this.mView.findViewById(R.id.wifi_advanced_togglebox)).setOnCheckedChangeListener(this);
            this.mConfigUi.setSubmitButton(res.getString(R.string.wifi_save));
        } else {
            if (!(this.mWifiManager.isWifiCoverageExtendFeatureEnabled() && (this.mAccessPoint.getSecurity() == 0 || this.mAccessPoint.getSecurity() == 2))) {
                this.mShareThisWifiCheckBox.setChecked(false);
                this.mShareThisWifiCheckBox.setVisibility(8);
            }
            if (this.mAccessPoint.isPasspointConfig()) {
                this.mConfigUi.setTitle(this.mAccessPoint.getConfigName());
            } else {
                this.mConfigUi.setTitle(this.mAccessPoint.getSsid());
            }
            ViewGroup group = (ViewGroup) this.mView.findViewById(R.id.info);
            boolean showAdvancedFields = false;
            if (this.mAccessPoint.isSaved()) {
                boolean showAdvancedFields2;
                WifiConfiguration config = this.mAccessPoint.getConfig();
                this.mMeteredSettingsSpinner.setSelection(config.meteredOverride);
                this.mHiddenSettingsSpinner.setSelection(config.hiddenSSID ? 1 : 0);
                if (config.getIpAssignment() == IpAssignment.STATIC) {
                    this.mIpSettingsSpinner.setSelection(1);
                    showAdvancedFields = true;
                    StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                    if (!(staticConfig == null || staticConfig.ipAddress == null)) {
                        addRow(group, R.string.wifi_ip_address, staticConfig.ipAddress.getAddress().getHostAddress());
                    }
                } else {
                    this.mIpSettingsSpinner.setSelection(0);
                }
                this.mShareThisWifiCheckBox.setChecked(config.shareThisAp);
                this.mSharedCheckBox.setEnabled(config.shared);
                if (!config.shared) {
                    showAdvancedFields = true;
                }
                if (config.getProxySettings() == ProxySettings.STATIC) {
                    this.mProxySettingsSpinner.setSelection(1);
                    showAdvancedFields2 = true;
                } else if (config.getProxySettings() == ProxySettings.PAC) {
                    this.mProxySettingsSpinner.setSelection(2);
                    showAdvancedFields2 = true;
                } else {
                    this.mProxySettingsSpinner.setSelection(0);
                    if (config != null && config.isPasspoint()) {
                        addRow(group, R.string.passpoint_label, String.format(this.mContext.getString(R.string.passpoint_content), new Object[]{config.providerFriendlyName}));
                    }
                }
                showAdvancedFields = showAdvancedFields2;
                addRow(group, R.string.passpoint_label, String.format(this.mContext.getString(R.string.passpoint_content), new Object[]{config.providerFriendlyName}));
            }
            if (!((this.mAccessPoint.isSaved() || this.mAccessPoint.isActive() || this.mAccessPoint.isPasspointConfig()) && this.mMode == 0)) {
                showSecurityFields();
                showIpConfigFields();
                showProxyFields();
                CheckBox advancedTogglebox = (CheckBox) this.mView.findViewById(R.id.wifi_advanced_togglebox);
                this.mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(this.mAccessPoint.isCarrierAp() ? 8 : 0);
                advancedTogglebox.setOnCheckedChangeListener(this);
                advancedTogglebox.setChecked(showAdvancedFields);
                this.mView.findViewById(R.id.wifi_advanced_fields).setVisibility(showAdvancedFields ? 0 : 8);
                if (this.mAccessPoint.isCarrierAp()) {
                    addRow(group, R.string.wifi_carrier_connect, String.format(this.mContext.getString(R.string.wifi_carrier_content), new Object[]{this.mAccessPoint.getCarrierName()}));
                }
            }
            if (this.mMode == 2) {
                this.mConfigUi.setSubmitButton(res.getString(R.string.wifi_save));
            } else if (this.mMode == 1) {
                this.mConfigUi.setSubmitButton(res.getString(R.string.wifi_connect));
            } else {
                DetailedState state = this.mAccessPoint.getDetailedState();
                String signalLevel = getSignalString();
                if ((state == null || state == DetailedState.DISCONNECTED) && signalLevel != null) {
                    this.mConfigUi.setSubmitButton(res.getString(R.string.wifi_connect));
                } else {
                    String providerFriendlyName;
                    if (state != null) {
                        boolean isEphemeral = this.mAccessPoint.isEphemeral();
                        WifiConfiguration config2 = this.mAccessPoint.getConfig();
                        providerFriendlyName = null;
                        if (config2 != null && config2.isPasspoint()) {
                            providerFriendlyName = config2.providerFriendlyName;
                        }
                        addRow(group, R.string.wifi_status, AccessPoint.getSummary(this.mConfigUi.getContext(), state, isEphemeral, providerFriendlyName));
                    }
                    if (signalLevel != null) {
                        addRow(group, R.string.wifi_signal, signalLevel);
                    }
                    WifiInfo info = this.mAccessPoint.getInfo();
                    if (!(info == null || info.getLinkSpeed() == -1)) {
                        addRow(group, R.string.wifi_speed, String.format(res.getString(R.string.link_speed), new Object[]{Integer.valueOf(info.getLinkSpeed())}));
                    }
                    if (!(info == null || info.getFrequency() == -1)) {
                        int frequency = info.getFrequency();
                        String band = null;
                        if (frequency >= AccessPoint.LOWER_FREQ_24GHZ && frequency < AccessPoint.HIGHER_FREQ_24GHZ) {
                            band = res.getString(R.string.wifi_band_24ghz);
                        } else if (frequency < AccessPoint.LOWER_FREQ_5GHZ || frequency >= AccessPoint.HIGHER_FREQ_5GHZ) {
                            providerFriendlyName = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Unexpected frequency ");
                            stringBuilder.append(frequency);
                            Log.e(providerFriendlyName, stringBuilder.toString());
                        } else {
                            band = res.getString(R.string.wifi_band_5ghz);
                        }
                        if (band != null) {
                            addRow(group, R.string.wifi_frequency, band);
                        }
                    }
                    addRow(group, R.string.wifi_security, this.mAccessPoint.getSecurityString(false));
                    this.mView.findViewById(R.id.ip_fields).setVisibility(8);
                }
                if (this.mAccessPoint.isSaved() || this.mAccessPoint.isActive() || this.mAccessPoint.isPasspointConfig()) {
                    this.mConfigUi.setForgetButton(res.getString(R.string.wifi_forget));
                }
            }
        }
        if (!isSplitSystemUser()) {
            this.mSharedCheckBox.setVisibility(8);
        }
        this.mConfigUi.setCancelButton(res.getString(R.string.wifi_cancel));
        if (this.mConfigUi.getSubmitButton() != null) {
            enableSubmitIfAppropriate();
        }
        this.mView.findViewById(R.id.l_wifidialog).requestFocus();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isSplitSystemUser() {
        this.mContext.getSystemService("user");
        return UserManager.isSplitSystemUser();
    }

    private void addRow(ViewGroup group, int name, String value) {
        View row = this.mConfigUi.getLayoutInflater().inflate(R.layout.wifi_dialog_row, group, false);
        ((TextView) row.findViewById(R.id.name)).setText(name);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        group.addView(row);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getSignalString() {
        String str = null;
        if (!this.mAccessPoint.isReachable()) {
            return null;
        }
        int level = this.mAccessPoint.getLevel();
        if (level > -1 && level < this.mLevels.length) {
            str = this.mLevels[level];
        }
        return str;
    }

    /* Access modifiers changed, original: 0000 */
    public void hideForgetButton() {
        Button forget = this.mConfigUi.getForgetButton();
        if (forget != null) {
            forget.setVisibility(8);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void hideSubmitButton() {
        Button submit = this.mConfigUi.getSubmitButton();
        if (submit != null) {
            submit.setVisibility(8);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void enableSubmitIfAppropriate() {
        Button submit = this.mConfigUi.getSubmitButton();
        if (submit != null) {
            submit.setEnabled(isSubmittable());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isValidPsk(String password) {
        if (password.length() == 64 && password.matches("[0-9A-Fa-f]{64}")) {
            return true;
        }
        if (password.length() < 8 || password.length() > 63) {
            return false;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isSubmittable() {
        boolean enabled;
        boolean passwordInvalid = false;
        if (this.mPasswordView != null && ((this.mAccessPointSecurity == 1 && !isWepPskValid(this.mPasswordView.getText().toString(), this.mPasswordView.length())) || ((this.mAccessPointSecurity == 2 && !isValidPsk(this.mPasswordView.getText().toString())) || (this.mAccessPointSecurity == 4 && !isWapiPskValid())))) {
            passwordInvalid = true;
        }
        if (this.mPasswordView != null && this.mPasswordView.length() == 0 && this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
            passwordInvalid = false;
        }
        if ((this.mSsidView == null || this.mSsidView.length() != 0) && !passwordInvalid) {
            enabled = ipAndProxyFieldsAreValid();
        } else {
            enabled = false;
        }
        if (!(this.mEapCaCertSpinner == null || this.mView.findViewById(R.id.l_ca_cert).getVisibility() == 8)) {
            String caCertSelection = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (caCertSelection.equals(this.mUnspecifiedCertString) && this.mSecuritySpinner != null && this.mSecuritySpinner.getSelectedItemPosition() == 3) {
                enabled = false;
            }
            if (caCertSelection.equals(this.mUseSystemCertsString) && this.mEapDomainView != null && this.mView.findViewById(R.id.l_domain).getVisibility() != 8 && TextUtils.isEmpty(this.mEapDomainView.getText().toString())) {
                enabled = false;
            }
        }
        if (!(this.mEapUserCertSpinner == null || this.mView.findViewById(R.id.l_user_cert).getVisibility() == 8 || !((String) this.mEapUserCertSpinner.getSelectedItem()).equals(this.mUnspecifiedCertString))) {
            enabled = false;
        }
        if (this.mSsidView == null || this.mSsidView.getText() == null || !WifiUtils.isSSIDTooLong(this.mSsidView.getText().toString())) {
            return enabled;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void showWarningMessagesIfAppropriate() {
        this.mView.findViewById(R.id.no_ca_cert_warning).setVisibility(8);
        this.mView.findViewById(R.id.no_domain_warning).setVisibility(8);
        this.mView.findViewById(R.id.ssid_too_long_warning).setVisibility(8);
        if (this.mSsidView != null && WifiUtils.isSSIDTooLong(this.mSsidView.getText().toString())) {
            this.mView.findViewById(R.id.ssid_too_long_warning).setVisibility(0);
        }
        if (this.mEapCaCertSpinner != null && this.mView.findViewById(R.id.l_ca_cert).getVisibility() != 8) {
            String caCertSelection = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (caCertSelection.equals(this.mDoNotValidateEapServerString)) {
                this.mView.findViewById(R.id.no_ca_cert_warning).setVisibility(0);
            }
            if (caCertSelection.equals(this.mUseSystemCertsString) && this.mEapDomainView != null && this.mView.findViewById(R.id.l_domain).getVisibility() != 8 && TextUtils.isEmpty(this.mEapDomainView.getText().toString())) {
                this.mView.findViewById(R.id.no_domain_warning).setVisibility(0);
            }
        }
    }

    private boolean isWapiPskValid() {
        if (this.mPasswordView.length() < 8 || this.mPasswordView.length() > 64) {
            return false;
        }
        String password = this.mPasswordView.getText().toString();
        if (WAPI_PSK_TYPE[this.mWapiPskTypeSpinner.getSelectedItemPosition()] == 1) {
            Log.e(TAG, "isWapiPskValid hex mode");
            if (!(this.mPasswordView.length() % 2 == 0 && password.matches("[0-9A-Fa-f]*"))) {
                return false;
            }
        }
        return true;
    }

    public WifiConfiguration getConfig() {
        if (this.mMode == 0) {
            return null;
        }
        WifiConfiguration config = new WifiConfiguration();
        if (this.mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(this.mSsidView.getText().toString());
            config.hiddenSSID = this.mHiddenSettingsSpinner.getSelectedItemPosition() == 1;
        } else if (!this.mAccessPoint.isSaved()) {
            config.SSID = AccessPoint.convertToQuotedString(this.mAccessPoint.getSsidStr());
        } else if (this.mAccessPoint.getConfig() != null) {
            Log.i(TAG, "Config");
            config.networkId = this.mAccessPoint.getConfig().networkId;
            config.hiddenSSID = this.mAccessPoint.getConfig().hiddenSSID;
        }
        config.shared = this.mSharedCheckBox.isChecked();
        config.shareThisAp = this.mShareThisWifiCheckBox.isChecked();
        String password;
        StringBuilder stringBuilder;
        switch (this.mAccessPointSecurity) {
            case 0:
                config.allowedKeyManagement.set(0);
                break;
            case 1:
                config.allowedKeyManagement.set(0);
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
                if (this.mPasswordView.length() != 0) {
                    int length = this.mPasswordView.length();
                    String password2 = this.mPasswordView.getText().toString();
                    if ((length != 10 && length != 26 && length != 32) || !password2.matches("[0-9A-Fa-f]*")) {
                        String[] strArr = config.wepKeys;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append('\"');
                        stringBuilder2.append(password2);
                        stringBuilder2.append('\"');
                        strArr[0] = stringBuilder2.toString();
                        break;
                    }
                    config.wepKeys[0] = password2;
                    break;
                }
                break;
            case 2:
                config.allowedKeyManagement.set(1);
                if (this.mPasswordView.length() != 0) {
                    password = this.mPasswordView.getText().toString();
                    if (!password.matches("[0-9A-Fa-f]{64}")) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append('\"');
                        stringBuilder.append(password);
                        stringBuilder.append('\"');
                        config.preSharedKey = stringBuilder.toString();
                        break;
                    }
                    config.preSharedKey = password;
                    break;
                }
                break;
            case 3:
                String str;
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
                if (this.mAccessPoint != null && this.mAccessPoint.isFils256Supported()) {
                    config.allowedKeyManagement.set(8);
                }
                if (this.mAccessPoint != null && this.mAccessPoint.isFils384Supported()) {
                    config.allowedKeyManagement.set(9);
                }
                if (this.mAccessPoint != null && this.mAccessPoint.isSuiteBSupported()) {
                    config.allowedKeyManagement.set(13);
                    config.requirePMF = true;
                    config.allowedPairwiseCiphers.set(3);
                    config.allowedGroupCiphers.set(5);
                    config.allowedGroupMgmtCiphers.set(1);
                    config.allowedSuiteBCiphers.set(1);
                }
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = this.mEapMethodSpinner.getSelectedItemPosition();
                int phase2Method = this.mPhase2Spinner.getSelectedItemPosition();
                config.enterpriseConfig.setEapMethod(eapMethod);
                if (eapMethod == 0) {
                    switch (phase2Method) {
                        case 0:
                            config.enterpriseConfig.setPhase2Method(0);
                            break;
                        case 1:
                            config.enterpriseConfig.setPhase2Method(3);
                            break;
                        case 2:
                            config.enterpriseConfig.setPhase2Method(4);
                            break;
                        case 3:
                            config.enterpriseConfig.setPhase2Method(5);
                            break;
                        case 4:
                            config.enterpriseConfig.setPhase2Method(6);
                            break;
                        case 5:
                            config.enterpriseConfig.setPhase2Method(7);
                            break;
                        default:
                            str = TAG;
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("Unknown phase2 method");
                            stringBuilder3.append(phase2Method);
                            Log.e(str, stringBuilder3.toString());
                            break;
                    }
                }
                switch (eapMethod) {
                    case 4:
                    case 5:
                    case 6:
                        this.selectedSimCardNumber = this.mSimCardSpinner.getSelectedItemPosition() + 1;
                        config.enterpriseConfig.setSimNum(this.selectedSimCardNumber);
                        break;
                    default:
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }
                str = (String) this.mEapCaCertSpinner.getSelectedItem();
                config.enterpriseConfig.setCaCertificateAliases(null);
                config.enterpriseConfig.setCaPath(null);
                config.enterpriseConfig.setDomainSuffixMatch(this.mEapDomainView.getText().toString());
                if (!(str.equals(this.mUnspecifiedCertString) || str.equals(this.mDoNotValidateEapServerString))) {
                    if (str.equals(this.mUseSystemCertsString)) {
                        config.enterpriseConfig.setCaPath(SYSTEM_CA_STORE_PATH);
                    } else if (!str.equals(this.mMultipleCertSetString)) {
                        config.enterpriseConfig.setCaCertificateAliases(new String[]{str});
                    } else if (this.mAccessPoint != null) {
                        if (!this.mAccessPoint.isSaved()) {
                            Log.e(TAG, "Multiple certs can only be set when editing saved network");
                        }
                        config.enterpriseConfig.setCaCertificateAliases(this.mAccessPoint.getConfig().enterpriseConfig.getCaCertificateAliases());
                    }
                }
                if (!(config.enterpriseConfig.getCaCertificateAliases() == null || config.enterpriseConfig.getCaPath() == null)) {
                    password = TAG;
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("ca_cert (");
                    stringBuilder4.append(config.enterpriseConfig.getCaCertificateAliases());
                    stringBuilder4.append(") and ca_path (");
                    stringBuilder4.append(config.enterpriseConfig.getCaPath());
                    stringBuilder4.append(") should not both be non-null");
                    Log.e(password, stringBuilder4.toString());
                }
                password = (String) this.mEapUserCertSpinner.getSelectedItem();
                if (password.equals(this.mUnspecifiedCertString) || password.equals(this.mDoNotProvideEapUserCertString)) {
                    password = "";
                }
                config.enterpriseConfig.setClientCertificateAlias(password);
                if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
                    config.enterpriseConfig.setIdentity("");
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else if (eapMethod == 3) {
                    config.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else {
                    config.enterpriseConfig.setIdentity(this.mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity(this.mEapAnonymousView.getText().toString());
                }
                if (!this.mPasswordView.isShown()) {
                    config.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                } else if (this.mPasswordView.length() > 0) {
                    config.enterpriseConfig.setPassword(this.mPasswordView.getText().toString());
                }
                if (this.mAccessPoint != null && (this.mAccessPoint.isFils256Supported() || this.mAccessPoint.isFils384Supported())) {
                    config.enterpriseConfig.setFieldValue("eap_erp", "1");
                    break;
                }
                break;
            case 4:
                config.allowedKeyManagement.set(190);
                config.wapiPskType = WAPI_PSK_TYPE[this.mWapiPskTypeSpinner.getSelectedItemPosition()];
                if (this.mPasswordView.length() != 0) {
                    StringBuilder stringBuilder5 = new StringBuilder();
                    stringBuilder5.append('\"');
                    stringBuilder5.append(this.mPasswordView.getText().toString());
                    stringBuilder5.append('\"');
                    config.wapiPsk = stringBuilder5.toString();
                }
                password = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("wapiPskType: ");
                stringBuilder.append(config.wapiPskType);
                stringBuilder.append(" wapiPsk: ");
                stringBuilder.append(config.wapiPsk);
                Log.d(password, stringBuilder.toString());
                break;
            case 5:
                config.allowedKeyManagement.set(191);
                if (this.mWapiCertSpinner.getSelectedItemPosition() == 0) {
                    config.wapiCertSelMode = 0;
                    config.wapiCertSel = null;
                } else {
                    config.wapiCertSelMode = 1;
                    config.wapiCertSel = (String) this.mWapiCertSpinner.getSelectedItem();
                }
                password = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("wapiCertSelMode: ");
                stringBuilder.append(config.wapiCertSelMode);
                stringBuilder.append(" wapiCertSel: ");
                stringBuilder.append(config.wapiCertSel);
                Log.d(password, stringBuilder.toString());
                break;
            case 6:
                config.allowedKeyManagement.set(10);
                config.requirePMF = true;
                break;
            case 7:
                config.allowedKeyManagement.set(11);
                config.requirePMF = true;
                if (this.mPasswordView.length() != 0) {
                    password = this.mPasswordView.getText().toString();
                    if (!password.matches("[0-9A-Fa-f]{64}")) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append('\"');
                        stringBuilder.append(password);
                        stringBuilder.append('\"');
                        config.preSharedKey = stringBuilder.toString();
                        break;
                    }
                    config.preSharedKey = password;
                    break;
                }
                break;
            case 8:
                config.allowedKeyManagement.set(12);
                config.requirePMF = true;
                break;
            default:
                return null;
        }
        config.setIpConfiguration(new IpConfiguration(this.mIpAssignment, this.mProxySettings, this.mStaticIpConfiguration, this.mHttpProxy));
        if (this.mMeteredSettingsSpinner != null) {
            config.meteredOverride = this.mMeteredSettingsSpinner.getSelectedItemPosition();
        }
        return config;
    }

    private boolean ipAndProxyFieldsAreValid() {
        IpAssignment ipAssignment;
        if (this.mIpSettingsSpinner == null || this.mIpSettingsSpinner.getSelectedItemPosition() != 1) {
            ipAssignment = IpAssignment.DHCP;
        } else {
            ipAssignment = IpAssignment.STATIC;
        }
        this.mIpAssignment = ipAssignment;
        if (this.mIpAssignment == IpAssignment.STATIC) {
            this.mStaticIpConfiguration = new StaticIpConfiguration();
            if (validateIpConfigFields(this.mStaticIpConfiguration) != 0) {
                return false;
            }
        }
        int selectedPosition = this.mProxySettingsSpinner.getSelectedItemPosition();
        this.mProxySettings = ProxySettings.NONE;
        this.mHttpProxy = null;
        if (selectedPosition == 1 && this.mProxyHostView != null) {
            this.mProxySettings = ProxySettings.STATIC;
            String host = this.mProxyHostView.getText().toString();
            String portStr = this.mProxyPortView.getText().toString();
            String exclusionList = this.mProxyExclusionListView.getText().toString();
            int port = 0;
            int result = 0;
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result != 0) {
                return false;
            }
            this.mHttpProxy = new ProxyInfo(host, port, exclusionList);
        } else if (selectedPosition == 2 && this.mProxyPacView != null) {
            this.mProxySettings = ProxySettings.PAC;
            CharSequence uriSequence = this.mProxyPacView.getText();
            if (TextUtils.isEmpty(uriSequence)) {
                return false;
            }
            Uri uri = Uri.parse(uriSequence.toString());
            if (uri == null) {
                return false;
            }
            this.mHttpProxy = new ProxyInfo(uri);
        }
        return true;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (ClassCastException | IllegalArgumentException e) {
            return null;
        }
    }

    private int validateIpConfigFields(StaticIpConfiguration staticIpConfiguration) {
        if (this.mIpAddressView == null) {
            return 0;
        }
        String ipAddr = this.mIpAddressView.getText().toString();
        if (TextUtils.isEmpty(ipAddr)) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }
        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null || inetAddr.equals(Inet4Address.ANY)) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }
        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(this.mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength >= 0) {
                if (networkPrefixLength <= 32) {
                    InetAddress dnsAddr;
                    staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
                    String gateway = this.mGatewayView.getText().toString();
                    if (!TextUtils.isEmpty(gateway) || this.mGatewayView.isFocused()) {
                        InetAddress gatewayAddr = getIPv4Address(gateway);
                        if (gatewayAddr == null || gatewayAddr.isMulticastAddress()) {
                            return R.string.wifi_ip_settings_invalid_gateway;
                        }
                        staticIpConfiguration.gateway = gatewayAddr;
                    } else {
                        try {
                            byte[] addr = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength).getAddress();
                            addr[addr.length - 1] = (byte) 1;
                            this.mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
                        } catch (RuntimeException | UnknownHostException e) {
                        }
                    }
                    String dns = this.mDns1View.getText().toString();
                    if (!TextUtils.isEmpty(dns) || this.mDns1View.isFocused()) {
                        dnsAddr = getIPv4Address(dns);
                        if (dnsAddr == null) {
                            return R.string.wifi_ip_settings_invalid_dns;
                        }
                        staticIpConfiguration.dnsServers.add(dnsAddr);
                    } else {
                        this.mDns1View.setText(this.mConfigUi.getContext().getString(R.string.wifi_dns1_hint));
                    }
                    if (this.mDns2View.length() > 0) {
                        dnsAddr = getIPv4Address(this.mDns2View.getText().toString());
                        if (dnsAddr == null) {
                            return R.string.wifi_ip_settings_invalid_dns;
                        }
                        staticIpConfiguration.dnsServers.add(dnsAddr);
                    }
                    return 0;
                }
            }
            return R.string.wifi_ip_settings_invalid_network_prefix_length;
        } catch (NumberFormatException e2) {
            if (!this.mNetworkPrefixLengthView.isFocused()) {
                this.mNetworkPrefixLengthView.setText(this.mConfigUi.getContext().getString(R.string.wifi_network_prefix_length_hint));
            }
        } catch (IllegalArgumentException e3) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }
    }

    private void showSecurityFields() {
        if (this.mAccessPointSecurity == 0 || this.mAccessPointSecurity == 6 || this.mAccessPointSecurity == 8) {
            this.mView.findViewById(R.id.security_fields).setVisibility(8);
            return;
        }
        String str;
        this.mView.findViewById(R.id.security_fields).setVisibility(0);
        if (this.mPasswordView == null) {
            this.mPasswordView = (TextView) this.mView.findViewById(R.id.password);
            this.mPasswordView.addTextChangedListener(this);
            this.mPasswordView.setOnEditorActionListener(this);
            this.mPasswordView.setOnKeyListener(this);
            ((CheckBox) this.mView.findViewById(R.id.show_password)).setOnCheckedChangeListener(this);
            if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
                this.mPasswordView.setHint(R.string.wifi_unchanged);
            }
        }
        if (this.mAccessPointSecurity != 4) {
            this.mView.findViewById(R.id.wapi_psk).setVisibility(8);
        } else {
            this.mView.findViewById(R.id.wapi_psk).setVisibility(0);
            this.mWapiPskTypeSpinner = (Spinner) this.mView.findViewById(R.id.wapi_psk_type);
            if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
                this.mWapiPskTypeSpinner.setSelection(this.mAccessPoint.getConfig().wapiPskType);
            }
            this.mWapiPskTypeSpinner.setOnItemSelectedListener(this);
        }
        if (this.mAccessPointSecurity != 5) {
            this.mView.findViewById(R.id.wapi_cert).setVisibility(8);
            this.mView.findViewById(R.id.password_layout).setVisibility(0);
            this.mView.findViewById(R.id.show_password_layout).setVisibility(0);
        } else {
            this.mView.findViewById(R.id.password_layout).setVisibility(8);
            this.mView.findViewById(R.id.show_password_layout).setVisibility(8);
            this.mView.findViewById(R.id.wapi_cert).setVisibility(0);
            this.mWapiCertSpinner = (Spinner) this.mView.findViewById(R.id.wapi_cert_select);
            loadWapiCertificates(this.mWapiCertSpinner);
            if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
                WifiConfiguration config = this.mAccessPoint.getConfig();
                str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Read WAPI_CERT sel mode: ");
                stringBuilder.append(config.wapiCertSelMode);
                Log.d(str, stringBuilder.toString());
                if (config.wapiCertSelMode == 0) {
                    this.mWapiCertSpinner.setSelection(0);
                } else {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Read WAPI_CERT sel cert name: ");
                    stringBuilder.append(config.wapiCertSel);
                    Log.d(str, stringBuilder.toString());
                    setSelection(this.mWapiCertSpinner, config.wapiCertSel);
                }
            }
        }
        if (this.mAccessPointSecurity != 3) {
            this.mView.findViewById(R.id.eap).setVisibility(8);
            return;
        }
        this.mView.findViewById(R.id.eap).setVisibility(0);
        if (this.mEapMethodSpinner == null) {
            getSIMInfo();
            this.mEapMethodSpinner = (Spinner) this.mView.findViewById(R.id.method);
            this.mEapMethodSpinner.setOnItemSelectedListener(this);
            if (Utils.isWifiOnly(this.mContext) || !this.mContext.getResources().getBoolean(17956951)) {
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(this.mContext, 17367048, this.mContext.getResources().getStringArray(R.array.eap_method_without_sim_auth));
                spinnerAdapter.setDropDownViewResource(17367049);
                this.mEapMethodSpinner.setAdapter(spinnerAdapter);
            }
            this.mPhase2Spinner = (Spinner) this.mView.findViewById(R.id.phase2);
            this.mPhase2Spinner.setOnItemSelectedListener(this);
            this.mEapCaCertSpinner = (Spinner) this.mView.findViewById(R.id.ca_cert);
            this.mEapCaCertSpinner.setOnItemSelectedListener(this);
            this.mEapDomainView = (TextView) this.mView.findViewById(R.id.domain);
            this.mEapDomainView.addTextChangedListener(this);
            this.mEapUserCertSpinner = (Spinner) this.mView.findViewById(R.id.user_cert);
            this.mEapUserCertSpinner.setOnItemSelectedListener(this);
            this.mSimCardSpinner = (Spinner) this.mView.findViewById(R.id.sim_card);
            this.mEapIdentityView = (TextView) this.mView.findViewById(R.id.identity);
            this.mEapAnonymousView = (TextView) this.mView.findViewById(R.id.anonymous);
            if (this.mAccessPoint != null && this.mAccessPoint.isCarrierAp()) {
                this.mEapMethodSpinner.setSelection(this.mAccessPoint.getCarrierApEapType());
            }
            loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, false, true);
            loadCertificates(this.mEapUserCertSpinner, "USRPKEY_", this.mDoNotProvideEapUserCertString, false, false);
            if (this.mAccessPoint == null || !this.mAccessPoint.isSaved()) {
                this.mPhase2Spinner = (Spinner) this.mView.findViewById(R.id.phase2);
                showEapFieldsByMethod(this.mEapMethodSpinner.getSelectedItemPosition());
            } else {
                WifiEnterpriseConfig enterpriseConfig = this.mAccessPoint.getConfig().enterpriseConfig;
                int eapMethod = enterpriseConfig.getEapMethod();
                int phase2Method = enterpriseConfig.getPhase2Method();
                this.mEapMethodSpinner.setSelection(eapMethod);
                showEapFieldsByMethod(eapMethod);
                if (eapMethod != 0) {
                    switch (eapMethod) {
                        case 4:
                        case 5:
                        case 6:
                            if (enterpriseConfig.getSimNum() != null && !enterpriseConfig.getSimNum().isEmpty()) {
                                this.mSimCardSpinner.setSelection(Integer.parseInt(enterpriseConfig.getSimNum()) - 1);
                                break;
                            }
                            this.mSimCardSpinner.setSelection(0);
                            break;
                        default:
                            this.mPhase2Spinner.setSelection(phase2Method);
                            break;
                    }
                } else if (phase2Method != 0) {
                    switch (phase2Method) {
                        case 3:
                            this.mPhase2Spinner.setSelection(1);
                            break;
                        case 4:
                            this.mPhase2Spinner.setSelection(2);
                            break;
                        case 5:
                            this.mPhase2Spinner.setSelection(3);
                            break;
                        case 6:
                            this.mPhase2Spinner.setSelection(4);
                            break;
                        case 7:
                            this.mPhase2Spinner.setSelection(5);
                            break;
                        default:
                            str = TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Invalid phase 2 method ");
                            stringBuilder2.append(phase2Method);
                            Log.e(str, stringBuilder2.toString());
                            break;
                    }
                } else {
                    this.mPhase2Spinner.setSelection(0);
                }
                if (TextUtils.isEmpty(enterpriseConfig.getCaPath())) {
                    String[] caCerts = enterpriseConfig.getCaCertificateAliases();
                    if (caCerts == null) {
                        setSelection(this.mEapCaCertSpinner, this.mDoNotValidateEapServerString);
                    } else if (caCerts.length == 1) {
                        setSelection(this.mEapCaCertSpinner, caCerts[0]);
                    } else {
                        loadCertificates(this.mEapCaCertSpinner, "CACERT_", this.mDoNotValidateEapServerString, true, true);
                        setSelection(this.mEapCaCertSpinner, this.mMultipleCertSetString);
                    }
                } else {
                    setSelection(this.mEapCaCertSpinner, this.mUseSystemCertsString);
                }
                this.mEapDomainView.setText(enterpriseConfig.getDomainSuffixMatch());
                String userCert = enterpriseConfig.getClientCertificateAlias();
                if (TextUtils.isEmpty(userCert)) {
                    setSelection(this.mEapUserCertSpinner, this.mDoNotProvideEapUserCertString);
                } else {
                    setSelection(this.mEapUserCertSpinner, userCert);
                }
                this.mEapIdentityView.setText(enterpriseConfig.getIdentity());
                this.mEapAnonymousView.setText(enterpriseConfig.getAnonymousIdentity());
            }
        } else {
            showEapFieldsByMethod(this.mEapMethodSpinner.getSelectedItemPosition());
        }
    }

    private void showEapFieldsByMethod(int eapMethod) {
        this.mView.findViewById(R.id.l_method).setVisibility(0);
        this.mView.findViewById(R.id.l_identity).setVisibility(0);
        this.mView.findViewById(R.id.l_domain).setVisibility(0);
        this.mView.findViewById(R.id.l_ca_cert).setVisibility(0);
        this.mView.findViewById(R.id.password_layout).setVisibility(0);
        this.mView.findViewById(R.id.show_password_layout).setVisibility(0);
        Context context = this.mConfigUi.getContext();
        switch (eapMethod) {
            case 0:
                if (this.mPhase2Adapter != this.mPhase2PeapAdapter) {
                    this.mPhase2Adapter = this.mPhase2PeapAdapter;
                    this.mPhase2Spinner.setAdapter(this.mPhase2Adapter);
                }
                this.mView.findViewById(R.id.l_phase2).setVisibility(0);
                this.mView.findViewById(R.id.l_anonymous).setVisibility(0);
                showPeapFields();
                setUserCertInvisible();
                setSimCardInvisible();
                break;
            case 1:
                this.mView.findViewById(R.id.l_user_cert).setVisibility(0);
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setPasswordInvisible();
                setSimCardInvisible();
                break;
            case 2:
                if (this.mPhase2Adapter != this.mPhase2FullAdapter) {
                    this.mPhase2Adapter = this.mPhase2FullAdapter;
                    this.mPhase2Spinner.setAdapter(this.mPhase2Adapter);
                }
                this.mView.findViewById(R.id.l_phase2).setVisibility(0);
                this.mView.findViewById(R.id.l_anonymous).setVisibility(0);
                setUserCertInvisible();
                setSimCardInvisible();
                break;
            case 3:
                setPhase2Invisible();
                setCaCertInvisible();
                setDomainInvisible();
                setAnonymousIdentInvisible();
                setUserCertInvisible();
                setSimCardInvisible();
                break;
            case 4:
            case 5:
            case 6:
                WifiConfiguration config = null;
                if (this.mAccessPoint != null) {
                    config = this.mAccessPoint.getConfig();
                }
                ArrayAdapter<String> eapSimAdapter = new ArrayAdapter(this.mContext, 17367048, (String[]) this.mSimDisplayNames.toArray(new String[this.mSimDisplayNames.size()]));
                eapSimAdapter.setDropDownViewResource(17367049);
                this.mSimCardSpinner.setAdapter(eapSimAdapter);
                this.mView.findViewById(R.id.l_sim_card).setVisibility(0);
                if (config != null) {
                    if (config.enterpriseConfig.getSimNum() == null || config.enterpriseConfig.getSimNum().isEmpty()) {
                        this.mSimCardSpinner.setSelection(0);
                    } else {
                        this.mSimCardSpinner.setSelection(Integer.parseInt(config.enterpriseConfig.getSimNum()) - 1);
                    }
                }
                setPhase2Invisible();
                setAnonymousIdentInvisible();
                setCaCertInvisible();
                setDomainInvisible();
                setUserCertInvisible();
                setPasswordInvisible();
                setIdentityInvisible();
                if (this.mAccessPoint != null && this.mAccessPoint.isCarrierAp()) {
                    setEapMethodInvisible();
                    break;
                }
                break;
        }
        if (this.mView.findViewById(R.id.l_ca_cert).getVisibility() != 8) {
            String eapCertSelection = (String) this.mEapCaCertSpinner.getSelectedItem();
            if (eapCertSelection.equals(this.mDoNotValidateEapServerString) || eapCertSelection.equals(this.mUnspecifiedCertString)) {
                setDomainInvisible();
            }
        }
    }

    private void showPeapFields() {
        int phase2Method = this.mPhase2Spinner.getSelectedItemPosition();
        if (phase2Method == 3 || phase2Method == 4 || phase2Method == 5) {
            this.mEapIdentityView.setText("");
            this.mView.findViewById(R.id.l_identity).setVisibility(8);
            setPasswordInvisible();
            return;
        }
        this.mView.findViewById(R.id.l_identity).setVisibility(0);
        this.mView.findViewById(R.id.l_anonymous).setVisibility(0);
        this.mView.findViewById(R.id.password_layout).setVisibility(0);
        this.mView.findViewById(R.id.show_password_layout).setVisibility(0);
    }

    private void setSimCardInvisible() {
        this.mView.findViewById(R.id.l_sim_card).setVisibility(8);
    }

    private void setIdentityInvisible() {
        this.mView.findViewById(R.id.l_identity).setVisibility(8);
        this.mPhase2Spinner.setSelection(0);
    }

    private void setPhase2Invisible() {
        this.mView.findViewById(R.id.l_phase2).setVisibility(8);
        this.mPhase2Spinner.setSelection(0);
    }

    private void setCaCertInvisible() {
        this.mView.findViewById(R.id.l_ca_cert).setVisibility(8);
        setSelection(this.mEapCaCertSpinner, this.mUnspecifiedCertString);
    }

    private void setDomainInvisible() {
        this.mView.findViewById(R.id.l_domain).setVisibility(8);
        this.mEapDomainView.setText("");
    }

    private void setUserCertInvisible() {
        this.mView.findViewById(R.id.l_user_cert).setVisibility(8);
        setSelection(this.mEapUserCertSpinner, this.mUnspecifiedCertString);
    }

    private void setAnonymousIdentInvisible() {
        this.mView.findViewById(R.id.l_anonymous).setVisibility(8);
        this.mEapAnonymousView.setText("");
    }

    private void setPasswordInvisible() {
        this.mPasswordView.setText("");
        this.mView.findViewById(R.id.password_layout).setVisibility(8);
        this.mView.findViewById(R.id.show_password_layout).setVisibility(8);
    }

    private void setEapMethodInvisible() {
        this.mView.findViewById(R.id.eap).setVisibility(8);
    }

    private void showIpConfigFields() {
        WifiConfiguration config = null;
        this.mView.findViewById(R.id.ip_fields).setVisibility(0);
        if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
            config = this.mAccessPoint.getConfig();
        }
        if (this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
            this.mView.findViewById(R.id.staticip).setVisibility(0);
            if (this.mIpAddressView == null) {
                this.mIpAddressView = (TextView) this.mView.findViewById(R.id.ipaddress);
                this.mIpAddressView.addTextChangedListener(this);
                this.mGatewayView = (TextView) this.mView.findViewById(R.id.gateway);
                this.mGatewayView.addTextChangedListener(this);
                this.mNetworkPrefixLengthView = (TextView) this.mView.findViewById(R.id.network_prefix_length);
                this.mNetworkPrefixLengthView.addTextChangedListener(this);
                this.mDns1View = (TextView) this.mView.findViewById(R.id.dns1);
                this.mDns1View.addTextChangedListener(this);
                this.mDns2View = (TextView) this.mView.findViewById(R.id.dns2);
                this.mDns2View.addTextChangedListener(this);
            }
            if (config != null) {
                StaticIpConfiguration staticConfig = config.getStaticIpConfiguration();
                if (staticConfig != null) {
                    if (staticConfig.ipAddress != null) {
                        this.mIpAddressView.setText(staticConfig.ipAddress.getAddress().getHostAddress());
                        this.mNetworkPrefixLengthView.setText(Integer.toString(staticConfig.ipAddress.getNetworkPrefixLength()));
                    }
                    if (staticConfig.gateway != null) {
                        this.mGatewayView.setText(staticConfig.gateway.getHostAddress());
                    }
                    Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator();
                    if (dnsIterator.hasNext()) {
                        this.mDns1View.setText(((InetAddress) dnsIterator.next()).getHostAddress());
                    }
                    if (dnsIterator.hasNext()) {
                        this.mDns2View.setText(((InetAddress) dnsIterator.next()).getHostAddress());
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        this.mView.findViewById(R.id.staticip).setVisibility(8);
    }

    private void showProxyFields() {
        WifiConfiguration config = null;
        this.mView.findViewById(R.id.proxy_settings_fields).setVisibility(0);
        if (this.mAccessPoint != null && this.mAccessPoint.isSaved()) {
            config = this.mAccessPoint.getConfig();
        }
        ProxyInfo proxyProperties;
        if (this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
            setVisibility(R.id.proxy_warning_limited_support, 0);
            setVisibility(R.id.proxy_fields, 0);
            setVisibility(R.id.proxy_pac_field, 8);
            if (this.mProxyHostView == null) {
                this.mProxyHostView = (TextView) this.mView.findViewById(R.id.proxy_hostname);
                this.mProxyHostView.addTextChangedListener(this);
                this.mProxyPortView = (TextView) this.mView.findViewById(R.id.proxy_port);
                this.mProxyPortView.addTextChangedListener(this);
                this.mProxyExclusionListView = (TextView) this.mView.findViewById(R.id.proxy_exclusionlist);
                this.mProxyExclusionListView.addTextChangedListener(this);
            }
            if (config != null) {
                proxyProperties = config.getHttpProxy();
                if (proxyProperties != null) {
                    this.mProxyHostView.setText(proxyProperties.getHost());
                    this.mProxyPortView.setText(Integer.toString(proxyProperties.getPort()));
                    this.mProxyExclusionListView.setText(proxyProperties.getExclusionListAsString());
                }
            }
        } else if (this.mProxySettingsSpinner.getSelectedItemPosition() == 2) {
            setVisibility(R.id.proxy_warning_limited_support, 8);
            setVisibility(R.id.proxy_fields, 8);
            setVisibility(R.id.proxy_pac_field, 0);
            if (this.mProxyPacView == null) {
                this.mProxyPacView = (TextView) this.mView.findViewById(R.id.proxy_pac);
                this.mProxyPacView.addTextChangedListener(this);
            }
            if (config != null) {
                proxyProperties = config.getHttpProxy();
                if (proxyProperties != null) {
                    this.mProxyPacView.setText(proxyProperties.getPacFileUrl().toString());
                }
            }
        } else {
            setVisibility(R.id.proxy_warning_limited_support, 8);
            setVisibility(R.id.proxy_fields, 8);
            setVisibility(R.id.proxy_pac_field, 8);
        }
    }

    private void setVisibility(int id, int visibility) {
        View v = this.mView.findViewById(id);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public KeyStore getKeyStore() {
        return KeyStore.getInstance();
    }

    private void loadCertificates(Spinner spinner, String prefix, String noCertificateString, boolean showMultipleCerts, boolean showUsePreinstalledCertOption) {
        Context context = this.mConfigUi.getContext();
        ArrayList<String> certs = new ArrayList();
        certs.add(this.mUnspecifiedCertString);
        if (showMultipleCerts) {
            certs.add(this.mMultipleCertSetString);
        }
        if (showUsePreinstalledCertOption) {
            certs.add(this.mUseSystemCertsString);
        }
        try {
            certs.addAll(Arrays.asList(getKeyStore().list(prefix, PointerIconCompat.TYPE_ALIAS)));
        } catch (Exception e) {
            Log.e(TAG, "can't get the certificate list from KeyStore");
        }
        certs.add(noCertificateString);
        ArrayAdapter<String> adapter = new ArrayAdapter(context, 17367048, (String[]) certs.toArray(new String[certs.size()]));
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
    }

    private void loadWapiCertificates(Spinner spinner) {
        Context context = this.mConfigUi.getContext();
        String unspecified = context.getString(R.string.wifi_unspecified);
        String autoSelCertString = context.getString(R.string.wapi_auto_sel_cert);
        ArrayList<String> certAliasList = new ArrayList();
        String[] certs = KeyStore.getInstance().list(WAPI_USER_CERTIFICATE, PointerIconCompat.TYPE_ALIAS);
        if (certs == null || certs.length <= 0) {
            certAliasList.add(unspecified);
        } else {
            certAliasList.add(autoSelCertString);
            for (Object add : certs) {
                certAliasList.add(add);
            }
        }
        if (certAliasList.size() > 1) {
            this.mHaveWapiCert = true;
        } else {
            this.mHaveWapiCert = false;
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context, 17367048, (String[]) certAliasList.toArray(new String[0]));
        adapter.setDropDownViewResource(17367049);
        spinner.setAdapter(adapter);
    }

    private void setSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                if (value.equals(adapter.getItem(i))) {
                    spinner.setSelection(i);
                    return;
                }
            }
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void afterTextChanged(Editable s) {
        ThreadUtils.postOnMainThread(new -$$Lambda$WifiConfigController$zJJNImzldn2IDwntJeWg8KPYIDY(this));
    }

    public static /* synthetic */ void lambda$afterTextChanged$0(WifiConfigController wifiConfigController) {
        wifiConfigController.showWarningMessagesIfAppropriate();
        wifiConfigController.enableSubmitIfAppropriate();
        if (wifiConfigController.mNetworkPrefixLengthView != null && wifiConfigController.mNetworkPrefixLengthView.isFocused()) {
            wifiConfigController.setGatewayByNetworkPrefixLength();
        }
    }

    private void setGatewayByNetworkPrefixLength() {
        try {
            int networkPrefixLength = Integer.parseInt(this.mNetworkPrefixLengthView.getText().toString());
            if (networkPrefixLength >= 0 && networkPrefixLength <= 32) {
                String ipAddr = this.mIpAddressView.getText().toString();
                if (!TextUtils.isEmpty(ipAddr)) {
                    Inet4Address inetAddr = getIPv4Address(ipAddr);
                    if (inetAddr != null && !inetAddr.equals(Inet4Address.ANY)) {
                        byte[] addr = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength).getAddress();
                        addr[addr.length - 1] = (byte) 1;
                        try {
                            if (!TextUtils.equals(InetAddress.getByAddress(addr).getHostAddress(), this.mGatewayView.getText())) {
                                this.mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress());
                            }
                        } catch (RuntimeException | UnknownHostException e) {
                        }
                    }
                }
            }
        } catch (Exception e2) {
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (textView != this.mPasswordView || id != 6 || !isSubmittable()) {
            return false;
        }
        this.mConfigUi.dispatchSubmit();
        return true;
    }

    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (view != this.mPasswordView || keyCode != 66 || !isSubmittable()) {
            return false;
        }
        this.mConfigUi.dispatchSubmit();
        return true;
    }

    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view.getId() == R.id.show_password) {
            int i;
            int pos = this.mPasswordView.getSelectionEnd();
            TextView textView = this.mPasswordView;
            if (isChecked) {
                i = Const.CODE_C1_SPA;
            } else {
                i = 128;
            }
            textView.setInputType(1 | i);
            if (pos >= 0) {
                ((EditText) this.mPasswordView).setSelection(pos);
            }
        } else if (view.getId() == R.id.wifi_advanced_togglebox) {
            int toggleVisibility;
            int stringID;
            View advancedToggle = this.mView.findViewById(R.id.wifi_advanced_toggle);
            if (isChecked) {
                toggleVisibility = 0;
                stringID = R.string.wifi_advanced_toggle_description_expanded;
            } else {
                toggleVisibility = 8;
                stringID = R.string.wifi_advanced_toggle_description_collapsed;
            }
            this.mView.findViewById(R.id.wifi_advanced_fields).setVisibility(toggleVisibility);
            advancedToggle.setContentDescription(this.mContext.getString(stringID));
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int i = 8;
        if (parent == this.mSecuritySpinner) {
            this.mAccessPointSecurity = position;
            if (this.mWifiManager.isWifiCoverageExtendFeatureEnabled() && (this.mAccessPointSecurity == 0 || this.mAccessPointSecurity == 2)) {
                this.mShareThisWifiCheckBox.setVisibility(0);
            } else {
                this.mShareThisWifiCheckBox.setChecked(false);
                this.mShareThisWifiCheckBox.setVisibility(8);
            }
            showSecurityFields();
        } else if (parent == this.mEapMethodSpinner || parent == this.mEapCaCertSpinner) {
            showSecurityFields();
        } else if (parent == this.mPhase2Spinner && this.mEapMethodSpinner.getSelectedItemPosition() == 0) {
            showPeapFields();
        } else if (parent == this.mProxySettingsSpinner) {
            showProxyFields();
        } else if (parent == this.mHiddenSettingsSpinner) {
            TextView textView = this.mHiddenWarningView;
            if (position != 0) {
                i = 0;
            }
            textView.setVisibility(i);
            if (position == 1) {
                this.mDialogContainer.post(new -$$Lambda$WifiConfigController$7dViR1XLVJsqzanUSq-nLAYeTK0(this));
            }
        } else {
            showIpConfigFields();
        }
        showWarningMessagesIfAppropriate();
        enableSubmitIfAppropriate();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void updatePassword() {
        int i;
        TextView passwdView = (TextView) this.mView.findViewById(R.id.password);
        if (((CheckBox) this.mView.findViewById(R.id.show_password)).isChecked()) {
            i = Const.CODE_C1_SPA;
        } else {
            i = 128;
        }
        passwdView.setInputType(i | 1);
    }

    public AccessPoint getAccessPoint() {
        return this.mAccessPoint;
    }

    private boolean isWepPskValid(String psk, int pskLength) {
        if (psk == null || pskLength <= 0) {
            return false;
        }
        if (pskLength == 5 || pskLength == 13 || pskLength == 16 || ((pskLength == 10 || pskLength == 26 || pskLength == 32) && psk.matches("[0-9A-Fa-f]*"))) {
            return true;
        }
        return false;
    }

    private void getSIMInfo() {
        this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
        for (int i = 0; i < this.mTelephonyManager.getSimCount(); i++) {
            String displayname;
            SubscriptionInfo sir = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            if (sir != null) {
                displayname = String.valueOf(sir.getDisplayName());
            } else {
                displayname = this.mContext.getString(R.string.sim_editor_title, new Object[]{Integer.valueOf(i + 1)});
            }
            this.mSimDisplayNames.add(displayname);
        }
    }

    public int getCurSecurity() {
        return this.mAccessPointSecurity;
    }

    public boolean checkWapiParam() {
        String unchanged = this.mConfigUi.getContext().getString(R.string.wifi_unchanged);
        if (this.mAccessPointSecurity == 5) {
            if (this.mHaveWapiCert) {
                return true;
            }
            return false;
        } else if (this.mAccessPointSecurity != 4) {
            return true;
        } else {
            int wapiPskType = WAPI_PSK_TYPE[this.mWapiPskTypeSpinner.getSelectedItemPosition()];
            String password = this.mPasswordView.getText().toString();
            if (unchanged.equals(this.mPasswordView.getHint() != null ? this.mPasswordView.getHint().toString() : "")) {
                return true;
            }
            return checkWapiPassword(password, wapiPskType);
        }
    }

    private boolean checkWapiPassword(String password, int type) {
        if (password.length() == 0) {
            Toast.makeText(this.mConfigUi.getContext(), R.string.wapi_password_empty, 1).show();
            return false;
        } else if (password.length() < 8 || password.length() > 64) {
            Toast.makeText(this.mConfigUi.getContext(), R.string.wapi_password_length_invalid, 1).show();
            return false;
        } else {
            if (type == 1) {
                int ret = checkHexPasswd(password);
                if (ret == -2) {
                    Toast.makeText(this.mConfigUi.getContext(), R.string.wapi_password_hex_format_invalid, 1).show();
                    return false;
                } else if (ret == -1) {
                    Toast.makeText(this.mConfigUi.getContext(), R.string.wapi_password_hex_length_invalid, 1).show();
                    return false;
                }
            }
            return true;
        }
    }

    private static int checkHexPasswd(String key) {
        if (key.length() % 2 == 1) {
            return -1;
        }
        for (int i = key.length() - 1; i >= 0; i--) {
            char c = key.charAt(i);
            if ((c < '0' || c > '9') && ((c < 'A' || c > 'F') && (c < 'a' || c > 'f'))) {
                return -2;
            }
        }
        return 0;
    }
}
