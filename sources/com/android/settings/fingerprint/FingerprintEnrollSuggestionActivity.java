package com.android.settings.fingerprint;

import android.content.Context;
import com.android.settings.Utils;

public class FingerprintEnrollSuggestionActivity extends FingerprintEnrollIntroduction {
    public static boolean isSuggestionComplete(Context context) {
        if (Utils.hasFingerprintHardware(context) && FingerprintSuggestionActivity.isFingerprintEnabled(context) && Utils.hasFingerprintHardware(context)) {
            return Utils.getFingerprintManagerOrNull(context).hasEnrolledFingerprints();
        }
        return true;
    }
}
