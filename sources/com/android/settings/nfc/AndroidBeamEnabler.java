package com.android.settings.nfc;

import android.content.Context;
import android.os.UserHandle;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;

public class AndroidBeamEnabler extends BaseNfcEnabler {
    private final boolean mBeamDisallowedBySystem;
    private final RestrictedPreference mPreference;

    public AndroidBeamEnabler(Context context, RestrictedPreference preference) {
        super(context);
        this.mPreference = preference;
        this.mBeamDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(context, "no_outgoing_beam", UserHandle.myUserId());
        if (isNfcAvailable()) {
            if (this.mBeamDisallowedBySystem) {
                this.mPreference.setEnabled(false);
            }
            return;
        }
        this.mPreference.setEnabled(false);
    }

    /* Access modifiers changed, original: protected */
    public void handleNfcStateChanged(int newState) {
        switch (newState) {
            case 1:
                this.mPreference.setEnabled(false);
                this.mPreference.setSummary((int) R.string.android_beam_disabled_summary);
                return;
            case 2:
                this.mPreference.setEnabled(false);
                return;
            case 3:
                if (this.mBeamDisallowedBySystem) {
                    this.mPreference.setDisabledByAdmin(null);
                    this.mPreference.setEnabled(false);
                } else {
                    this.mPreference.checkRestrictionAndSetDisabled("no_outgoing_beam");
                }
                if (this.mNfcAdapter.isNdefPushEnabled() && this.mPreference.isEnabled()) {
                    this.mPreference.setSummary((int) R.string.android_beam_on_summary);
                    return;
                } else {
                    this.mPreference.setSummary((int) R.string.android_beam_off_summary);
                    return;
                }
            case 4:
                this.mPreference.setEnabled(false);
                return;
            default:
                return;
        }
    }
}
