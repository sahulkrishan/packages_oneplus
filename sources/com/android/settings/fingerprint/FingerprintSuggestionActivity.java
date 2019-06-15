package com.android.settings.fingerprint;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.Utils;

public class FingerprintSuggestionActivity extends SetupFingerprintEnrollIntroduction {
    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        ((Button) findViewById(R.id.fingerprint_cancel_button)).setText(R.string.security_settings_fingerprint_enroll_introduction_cancel);
    }

    public void finish() {
        setResult(0);
        super.finish();
    }

    public static boolean isSuggestionComplete(Context context) {
        return (Utils.hasFingerprintHardware(context) && isFingerprintEnabled(context) && !isNotSingleFingerprintEnrolled(context)) ? false : true;
    }

    private static boolean isNotSingleFingerprintEnrolled(Context context) {
        FingerprintManager manager = Utils.getFingerprintManagerOrNull(context);
        return manager == null || manager.getEnrolledFingerprints().size() != 1;
    }

    static boolean isFingerprintEnabled(Context context) {
        return (((DevicePolicyManager) context.getSystemService("device_policy")).getKeyguardDisabledFeatures(null, context.getUserId()) & 32) == 0;
    }
}
