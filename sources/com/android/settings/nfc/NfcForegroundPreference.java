package com.android.settings.nfc;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.nfc.PaymentBackend.Callback;

public class NfcForegroundPreference extends DropDownPreference implements Callback, OnPreferenceChangeListener {
    private final PaymentBackend mPaymentBackend;

    public NfcForegroundPreference(Context context, PaymentBackend backend) {
        super(context);
        this.mPaymentBackend = backend;
        this.mPaymentBackend.registerCallback(this);
        setTitle((CharSequence) getContext().getString(R.string.nfc_payment_use_default));
        setEntries(new CharSequence[]{getContext().getString(R.string.nfc_payment_favor_open), getContext().getString(R.string.nfc_payment_favor_default)});
        setEntryValues(new CharSequence[]{"1", "0"});
        refresh();
        setOnPreferenceChangeListener(this);
    }

    public void onPaymentAppsChanged() {
        refresh();
    }

    /* Access modifiers changed, original: 0000 */
    public void refresh() {
        if (this.mPaymentBackend.isForegroundMode()) {
            setValue("1");
        } else {
            setValue("0");
        }
        setSummary(getEntry());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String newValueString = (String) newValue;
        setSummary(getEntries()[findIndexOfValue(newValueString)]);
        this.mPaymentBackend.setForegroundMode(Integer.parseInt(newValueString) != 0);
        return true;
    }
}
