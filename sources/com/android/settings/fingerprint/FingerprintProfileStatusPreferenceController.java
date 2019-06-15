package com.android.settings.fingerprint;

import android.content.Context;

public class FingerprintProfileStatusPreferenceController extends FingerprintStatusPreferenceController {
    public static final String KEY_FINGERPRINT_SETTINGS = "fingerprint_settings_profile";

    public FingerprintProfileStatusPreferenceController(Context context) {
        super(context, KEY_FINGERPRINT_SETTINGS);
    }

    /* Access modifiers changed, original: protected */
    public boolean isUserSupported() {
        return this.mProfileChallengeUserId != -10000 && this.mLockPatternUtils.isSeparateProfileChallengeAllowed(this.mProfileChallengeUserId);
    }

    /* Access modifiers changed, original: protected */
    public int getUserId() {
        return this.mProfileChallengeUserId;
    }
}
