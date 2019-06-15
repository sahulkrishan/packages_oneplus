package com.android.settings.nfc;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;

public class NfcEnabler extends BaseNfcEnabler {
    private final SwitchPreference mPreference;

    public NfcEnabler(Context context, SwitchPreference preference) {
        super(context);
        this.mPreference = preference;
    }

    /* Access modifiers changed, original: protected */
    public void handleNfcStateChanged(int newState) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("newState = ");
        stringBuilder.append(newState);
        Log.d("NfcEnabler", stringBuilder.toString());
        switch (newState) {
            case 1:
                this.mPreference.setChecked(false);
                this.mPreference.setEnabled(true);
                return;
            case 2:
                this.mPreference.setChecked(true);
                this.mPreference.setEnabled(false);
                return;
            case 3:
                this.mPreference.setChecked(true);
                this.mPreference.setEnabled(true);
                return;
            case 4:
                this.mPreference.setChecked(false);
                this.mPreference.setEnabled(false);
                return;
            default:
                return;
        }
    }
}
