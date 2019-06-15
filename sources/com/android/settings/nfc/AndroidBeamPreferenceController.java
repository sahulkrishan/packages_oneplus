package com.android.settings.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AndroidBeamPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    public static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private NfcAirplaneModeObserver mAirplaneModeObserver;
    private AndroidBeamEnabler mAndroidBeamEnabler;
    private final NfcAdapter mNfcAdapter;

    public AndroidBeamPreferenceController(Context context, String key) {
        super(context, key);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            RestrictedPreference restrictedPreference = (RestrictedPreference) screen.findPreference(getPreferenceKey());
            this.mAndroidBeamEnabler = new AndroidBeamEnabler(this.mContext, restrictedPreference);
            if (!NfcPreferenceController.isToggleableInAirplaneMode(this.mContext)) {
                this.mAirplaneModeObserver = new NfcAirplaneModeObserver(this.mContext, this.mNfcAdapter, restrictedPreference);
            }
            return;
        }
        this.mAndroidBeamEnabler = null;
    }

    public int getAvailabilityStatus() {
        if (this.mNfcAdapter != null) {
            return 0;
        }
        return 2;
    }

    public void onResume() {
        if (this.mAirplaneModeObserver != null) {
            this.mAirplaneModeObserver.register();
        }
        if (this.mAndroidBeamEnabler != null) {
            this.mAndroidBeamEnabler.resume();
        }
    }

    public void onPause() {
        if (this.mAirplaneModeObserver != null) {
            this.mAirplaneModeObserver.unregister();
        }
        if (this.mAndroidBeamEnabler != null) {
            this.mAndroidBeamEnabler.pause();
        }
    }
}
