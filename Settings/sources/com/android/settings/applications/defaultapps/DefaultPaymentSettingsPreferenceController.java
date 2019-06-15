package com.android.settings.applications.defaultapps;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.nfc.PaymentBackend;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import com.android.settingslib.core.AbstractPreferenceController;

public class DefaultPaymentSettingsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private Fragment mFragment;
    private final NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
    private final PackageManager mPackageManager;
    private PaymentBackend mPaymentBackend;
    private final UserManager mUserManager;

    public DefaultPaymentSettingsPreferenceController(Context context) {
        super(context);
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public boolean isAvailable() {
        return this.mPackageManager.hasSystemFeature("android.hardware.nfc") && this.mPackageManager.hasSystemFeature("android.hardware.nfc.hce") && this.mUserManager.isAdminUser() && this.mNfcAdapter != null;
    }

    public void updateState(Preference preference) {
        if (this.mPaymentBackend == null) {
            if (this.mNfcAdapter != null) {
                this.mPaymentBackend = new PaymentBackend(this.mContext);
                if (this.mFragment != null) {
                    this.mPaymentBackend.setFragment(this.mFragment);
                }
            } else {
                this.mPaymentBackend = null;
            }
        }
        if (this.mPaymentBackend != null) {
            this.mPaymentBackend.refresh();
            PaymentAppInfo app = this.mPaymentBackend.getDefaultApp();
            String defaultComponentString = Secure.getString(this.mContext.getContentResolver(), "nfc_payment_default_component");
            if (app != null) {
                CharSequence defaultAppName = app.label;
                if (!TextUtils.isEmpty(defaultAppName)) {
                    preference.setSummary(defaultAppName);
                }
            } else {
                preference.setSummary((int) R.string.app_list_preference_none);
            }
        }
    }

    public String getPreferenceKey() {
        return "default_payment_app";
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }
}
