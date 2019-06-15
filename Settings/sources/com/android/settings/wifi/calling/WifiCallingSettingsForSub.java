package com.android.settings.wifi.calling;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.android.ims.ImsManager;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

public class WifiCallingSettingsForSub extends SettingsPreferenceFragment implements OnSwitchChangeListener, OnPreferenceChangeListener {
    private static final String BUTTON_WFC_MODE = "wifi_calling_mode";
    private static final String BUTTON_WFC_ROAMING_MODE = "wifi_calling_roaming_mode";
    public static final String EXTRA_LAUNCH_CARRIER_APP = "EXTRA_LAUNCH_CARRIER_APP";
    protected static final String FRAGMENT_BUNDLE_SUBID = "subId";
    public static final int LAUCH_APP_ACTIVATE = 0;
    public static final int LAUCH_APP_UPDATE = 1;
    private static final String PREFERENCE_EMERGENCY_ADDRESS = "emergency_address_key";
    private static final int REQUEST_CHECK_WFC_EMERGENCY_ADDRESS = 1;
    private static final String TAG = "WifiCallingForSub";
    private ListPreference mButtonWfcMode;
    private ListPreference mButtonWfcRoamingMode;
    private boolean mEditableWfcMode = true;
    private boolean mEditableWfcRoamingMode = true;
    private TextView mEmptyView;
    private ImsManager mImsManager;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.android.ims.REGISTRATION_ERROR")) {
                setResultCode(0);
                WifiCallingSettingsForSub.this.showAlert(intent);
            }
        }
    };
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            SettingsActivity activity = (SettingsActivity) WifiCallingSettingsForSub.this.getActivity();
            boolean isNonTtyOrTtyOnVolteEnabled = WifiCallingSettingsForSub.this.mImsManager.isNonTtyOrTtyOnVolteEnabled();
            boolean z = false;
            boolean isWfcEnabled = WifiCallingSettingsForSub.this.mSwitchBar.isChecked() && isNonTtyOrTtyOnVolteEnabled;
            SwitchBar access$100 = WifiCallingSettingsForSub.this.mSwitchBar;
            boolean z2 = state == 0 && isNonTtyOrTtyOnVolteEnabled;
            access$100.setEnabled(z2);
            boolean isWfcModeEditable = true;
            z2 = false;
            CarrierConfigManager configManager = (CarrierConfigManager) activity.getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(this.mSubId.intValue());
                if (b != null) {
                    isWfcModeEditable = b.getBoolean("editable_wfc_mode_bool");
                    z2 = b.getBoolean("editable_wfc_roaming_mode_bool");
                }
            }
            Preference pref = WifiCallingSettingsForSub.this.getPreferenceScreen().findPreference(WifiCallingSettingsForSub.BUTTON_WFC_MODE);
            if (pref != null) {
                boolean z3 = isWfcEnabled && isWfcModeEditable && state == 0;
                pref.setEnabled(z3);
            }
            Preference pref_roam = WifiCallingSettingsForSub.this.getPreferenceScreen().findPreference(WifiCallingSettingsForSub.BUTTON_WFC_ROAMING_MODE);
            if (pref_roam != null) {
                if (isWfcEnabled && isWfcRoamingModeEditable && state == 0) {
                    z = true;
                }
                pref_roam.setEnabled(z);
            }
        }
    };
    private int mSubId = -1;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private Preference mUpdateAddress;
    private final OnPreferenceClickListener mUpdateAddressListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            Intent carrierAppIntent = WifiCallingSettingsForSub.this.getCarrierActivityIntent();
            if (carrierAppIntent != null) {
                carrierAppIntent.putExtra(WifiCallingSettingsForSub.EXTRA_LAUNCH_CARRIER_APP, 1);
                WifiCallingSettingsForSub.this.startActivity(carrierAppIntent);
            }
            return true;
        }
    };
    private boolean mValidListener = false;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mEmptyView = (TextView) getView().findViewById(16908292);
        setEmptyView(this.mEmptyView);
        String emptyViewText = new StringBuilder();
        emptyViewText.append(activity.getString(R.string.wifi_calling_off_explanation));
        emptyViewText.append(activity.getString(R.string.wifi_calling_off_explanation_2));
        this.mEmptyView.setText(emptyViewText.toString());
        this.mSwitchBar = (SwitchBar) getView().findViewById(R.id.switch_bar);
        this.mSwitchBar.show();
        this.mSwitch = this.mSwitchBar.getSwitch();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
    }

    private void showAlert(Intent intent) {
        Context context = getActivity();
        CharSequence title = intent.getCharSequenceExtra("alertTitle");
        CharSequence message = intent.getCharSequenceExtra("alertMessage");
        Builder builder = new Builder(context);
        builder.setMessage(message).setTitle(title).setIcon(17301543).setPositiveButton(17039370, null);
        builder.create().show();
    }

    public int getMetricsCategory() {
        return 1230;
    }

    public int getHelpResource() {
        return 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_calling_settings);
        if (getArguments() != null && getArguments().containsKey(FRAGMENT_BUNDLE_SUBID)) {
            this.mSubId = getArguments().getInt(FRAGMENT_BUNDLE_SUBID);
        } else if (savedInstanceState != null) {
            this.mSubId = savedInstanceState.getInt(FRAGMENT_BUNDLE_SUBID, -1);
        }
        this.mImsManager = ImsManager.getInstance(getActivity(), SubscriptionManager.getPhoneId(this.mSubId));
        this.mButtonWfcMode = (ListPreference) findPreference(BUTTON_WFC_MODE);
        this.mButtonWfcMode.setOnPreferenceChangeListener(this);
        this.mButtonWfcRoamingMode = (ListPreference) findPreference(BUTTON_WFC_ROAMING_MODE);
        this.mButtonWfcRoamingMode.setOnPreferenceChangeListener(this);
        this.mUpdateAddress = findPreference(PREFERENCE_EMERGENCY_ADDRESS);
        this.mUpdateAddress.setOnPreferenceClickListener(this.mUpdateAddressListener);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("com.android.ims.REGISTRATION_ERROR");
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(FRAGMENT_BUNDLE_SUBID, this.mSubId);
        super.onSaveInstanceState(outState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_calling_settings_preferences, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        return view;
    }

    private void updateBody() {
        CarrierConfigManager configManager = (CarrierConfigManager) getSystemService("carrier_config");
        boolean isWifiOnlySupported = true;
        if (configManager != null) {
            PersistableBundle b = configManager.getConfigForSubId(this.mSubId);
            if (b != null) {
                this.mEditableWfcMode = b.getBoolean("editable_wfc_mode_bool");
                this.mEditableWfcRoamingMode = b.getBoolean("editable_wfc_roaming_mode_bool");
                isWifiOnlySupported = b.getBoolean("carrier_wfc_supports_wifi_only_bool", true);
            }
        }
        if (!isWifiOnlySupported) {
            this.mButtonWfcMode.setEntries((int) R.array.wifi_calling_mode_choices_without_wifi_only);
            this.mButtonWfcMode.setEntryValues((int) R.array.wifi_calling_mode_values_without_wifi_only);
            this.mButtonWfcRoamingMode.setEntries((int) R.array.wifi_calling_mode_choices_v2_without_wifi_only);
            this.mButtonWfcRoamingMode.setEntryValues((int) R.array.wifi_calling_mode_values_without_wifi_only);
        }
        boolean wfcEnabled = this.mImsManager.isWfcEnabledByUser() && this.mImsManager.isNonTtyOrTtyOnVolteEnabled();
        this.mSwitch.setChecked(wfcEnabled);
        int wfcMode = this.mImsManager.getWfcMode(false);
        int wfcRoamingMode = this.mImsManager.getWfcMode(true);
        this.mButtonWfcMode.setValue(Integer.toString(wfcMode));
        this.mButtonWfcRoamingMode.setValue(Integer.toString(wfcRoamingMode));
        updateButtonWfcMode(wfcEnabled, wfcMode, wfcRoamingMode);
    }

    public void onResume() {
        super.onResume();
        Context context = getActivity();
        updateBody();
        if (this.mImsManager.isWfcEnabledByPlatform()) {
            ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 32);
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
        context.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        Intent intent = getActivity().getIntent();
        if (intent.getBooleanExtra("alertShow", false)) {
            showAlert(intent);
        }
    }

    public void onPause() {
        super.onPause();
        Context context = getActivity();
        if (this.mValidListener) {
            this.mValidListener = false;
            ((TelephonyManager) getSystemService("phone")).listen(this.mPhoneStateListener, 0);
            this.mSwitchBar.removeOnSwitchChangeListener(this);
        }
        context.unregisterReceiver(this.mIntentReceiver);
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onSwitchChanged(");
        stringBuilder.append(isChecked);
        stringBuilder.append(")");
        Log.d(str, stringBuilder.toString());
        if (isChecked) {
            Intent carrierAppIntent = getCarrierActivityIntent();
            if (carrierAppIntent != null) {
                carrierAppIntent.putExtra(EXTRA_LAUNCH_CARRIER_APP, 0);
                startActivityForResult(carrierAppIntent, 1);
            } else {
                updateWfcMode(true);
            }
            return;
        }
        updateWfcMode(false);
    }

    private Intent getCarrierActivityIntent() {
        CarrierConfigManager configManager = (CarrierConfigManager) getActivity().getSystemService(CarrierConfigManager.class);
        if (configManager == null) {
            return null;
        }
        PersistableBundle bundle = configManager.getConfigForSubId(this.mSubId);
        if (bundle == null) {
            return null;
        }
        String carrierApp = bundle.getString("wfc_emergency_address_carrier_app_string");
        if (TextUtils.isEmpty(carrierApp)) {
            return null;
        }
        ComponentName componentName = ComponentName.unflattenFromString(carrierApp);
        if (componentName == null) {
            return null;
        }
        Intent intent = new Intent();
        intent.setComponent(componentName);
        return intent;
    }

    private void updateWfcMode(boolean wfcEnabled) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("updateWfcMode(");
        stringBuilder.append(wfcEnabled);
        stringBuilder.append(")");
        Log.i(str, stringBuilder.toString());
        this.mImsManager.setWfcSetting(wfcEnabled);
        int wfcMode = this.mImsManager.getWfcMode(false);
        updateButtonWfcMode(wfcEnabled, wfcMode, this.mImsManager.getWfcMode(true));
        if (wfcEnabled) {
            this.mMetricsFeatureProvider.action(getActivity(), getMetricsCategory(), wfcMode);
        } else {
            this.mMetricsFeatureProvider.action(getActivity(), getMetricsCategory(), -1);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getActivity();
        if (requestCode == 1) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("WFC emergency address activity result = ");
            stringBuilder.append(resultCode);
            Log.d(str, stringBuilder.toString());
            if (resultCode == -1) {
                updateWfcMode(true);
            }
        }
    }

    private void updateButtonWfcMode(boolean wfcEnabled, int wfcMode, int wfcRoamingMode) {
        this.mButtonWfcMode.setSummary(getWfcModeSummary(wfcMode));
        ListPreference listPreference = this.mButtonWfcMode;
        boolean updateAddressEnabled = false;
        boolean z = wfcEnabled && this.mEditableWfcMode;
        listPreference.setEnabled(z);
        listPreference = this.mButtonWfcRoamingMode;
        z = wfcEnabled && this.mEditableWfcRoamingMode;
        listPreference.setEnabled(z);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (getCarrierActivityIntent() != null) {
            updateAddressEnabled = true;
        }
        if (wfcEnabled) {
            if (this.mEditableWfcMode) {
                preferenceScreen.addPreference(this.mButtonWfcMode);
            } else {
                preferenceScreen.removePreference(this.mButtonWfcMode);
            }
            if (this.mEditableWfcRoamingMode) {
                preferenceScreen.addPreference(this.mButtonWfcRoamingMode);
            } else {
                preferenceScreen.removePreference(this.mButtonWfcRoamingMode);
            }
            if (updateAddressEnabled) {
                preferenceScreen.addPreference(this.mUpdateAddress);
                return;
            } else {
                preferenceScreen.removePreference(this.mUpdateAddress);
                return;
            }
        }
        preferenceScreen.removePreference(this.mButtonWfcMode);
        preferenceScreen.removePreference(this.mButtonWfcRoamingMode);
        preferenceScreen.removePreference(this.mUpdateAddress);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int buttonMode;
        if (preference == this.mButtonWfcMode) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onPreferenceChange mButtonWfcMode ");
            stringBuilder.append(newValue);
            Log.d(str, stringBuilder.toString());
            this.mButtonWfcMode.setValue((String) newValue);
            buttonMode = Integer.valueOf((String) newValue).intValue();
            if (buttonMode != this.mImsManager.getWfcMode(false)) {
                this.mImsManager.setWfcMode(buttonMode, false);
                this.mButtonWfcMode.setSummary(getWfcModeSummary(buttonMode));
                this.mMetricsFeatureProvider.action(getActivity(), getMetricsCategory(), buttonMode);
            }
            if (!(this.mEditableWfcRoamingMode || buttonMode == this.mImsManager.getWfcMode(true))) {
                this.mImsManager.setWfcMode(buttonMode, true);
            }
        } else if (preference == this.mButtonWfcRoamingMode) {
            this.mButtonWfcRoamingMode.setValue((String) newValue);
            buttonMode = Integer.valueOf((String) newValue).intValue();
            if (buttonMode != this.mImsManager.getWfcMode(true)) {
                this.mImsManager.setWfcMode(buttonMode, true);
                this.mMetricsFeatureProvider.action(getActivity(), getMetricsCategory(), buttonMode);
            }
        }
        return true;
    }

    private int getWfcModeSummary(int wfcMode) {
        if (!this.mImsManager.isWfcEnabledByUser()) {
            return 17041152;
        }
        switch (wfcMode) {
            case 0:
                return 17041118;
            case 1:
                return 17041117;
            case 2:
                return 17041119;
            default:
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unexpected WFC mode value: ");
                stringBuilder.append(wfcMode);
                Log.e(str, stringBuilder.toString());
                return 17041152;
        }
    }
}
