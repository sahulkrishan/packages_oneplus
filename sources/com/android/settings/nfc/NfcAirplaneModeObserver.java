package com.android.settings.nfc;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;

public class NfcAirplaneModeObserver extends ContentObserver {
    @VisibleForTesting
    static final Uri AIRPLANE_MODE_URI = Global.getUriFor("airplane_mode_on");
    private int mAirplaneMode;
    private final Context mContext;
    private final NfcAdapter mNfcAdapter;
    private final Preference mPreference;

    public NfcAirplaneModeObserver(Context context, NfcAdapter nfcAdapter, Preference preference) {
        super(new Handler(Looper.getMainLooper()));
        this.mContext = context;
        this.mNfcAdapter = nfcAdapter;
        this.mPreference = preference;
        updateNfcPreference();
    }

    public void register() {
        this.mContext.getContentResolver().registerContentObserver(AIRPLANE_MODE_URI, false, this);
    }

    public void unregister() {
        this.mContext.getContentResolver().unregisterContentObserver(this);
    }

    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        updateNfcPreference();
    }

    private void updateNfcPreference() {
        int airplaneMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", this.mAirplaneMode);
        if (airplaneMode != this.mAirplaneMode) {
            this.mAirplaneMode = airplaneMode;
            boolean z = true;
            if (this.mAirplaneMode == 1) {
                z = false;
            }
            boolean toggleable = z;
            if (toggleable) {
                this.mNfcAdapter.enable();
            } else {
                this.mNfcAdapter.disable();
            }
            this.mPreference.setEnabled(toggleable);
        }
    }
}
