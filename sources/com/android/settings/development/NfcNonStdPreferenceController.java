package com.android.settings.development;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class NfcNonStdPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String NXP_NON_STD_KEY = "nxp_non_std_card";
    static final String NXP_NON_STD_PROP_NAME = "persist.vendor.nfc.nonstdenable";
    @VisibleForTesting
    static final String SETTING_VALUE_OFF = "false";
    @VisibleForTesting
    static final String SETTING_VALUE_ON = "true";
    private Context mContext;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.nfc.action.ADAPTER_STATE_CHANGED") && intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", -1) == 1) {
                NfcAdapter.getDefaultAdapter(NfcNonStdPreferenceController.this.mContext).enable();
                NfcNonStdPreferenceController.this.mContext.unregisterReceiver(NfcNonStdPreferenceController.this.mReceiver);
            }
        }
    };

    public NfcNonStdPreferenceController(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPreferenceKey() {
        return NXP_NON_STD_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(NXP_NON_STD_PROP_NAME, ((Boolean) newValue).booleanValue() ? SETTING_VALUE_ON : SETTING_VALUE_OFF);
        resetNfcService();
        return true;
    }

    public void updateState(Preference preference) {
        String value = SystemProperties.get(NXP_NON_STD_PROP_NAME, SETTING_VALUE_OFF);
        ((SwitchPreference) this.mPreference).setChecked(value.equals(SETTING_VALUE_ON));
        Log.d("NfcNonStdPreferenceController", value);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(NXP_NON_STD_PROP_NAME, SETTING_VALUE_OFF);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    private void resetNfcService() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, null);
            nfcAdapter.disable();
        }
    }
}
