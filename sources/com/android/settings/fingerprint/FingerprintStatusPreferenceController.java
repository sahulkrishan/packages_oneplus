package com.android.settings.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import java.util.List;

public class FingerprintStatusPreferenceController extends BasePreferenceController {
    private static final String KEY_FINGERPRINT_SETTINGS = "fingerprint_settings";
    protected final FingerprintManager mFingerprintManager;
    protected final LockPatternUtils mLockPatternUtils;
    protected final int mProfileChallengeUserId;
    protected final UserManager mUm;
    protected final int mUserId;

    public FingerprintStatusPreferenceController(Context context) {
        this(context, KEY_FINGERPRINT_SETTINGS);
    }

    public FingerprintStatusPreferenceController(Context context, String key) {
        super(context, key);
        this.mUserId = UserHandle.myUserId();
        this.mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
        this.mUm = (UserManager) context.getSystemService("user");
        this.mLockPatternUtils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
        this.mProfileChallengeUserId = Utils.getManagedProfileId(this.mUm, this.mUserId);
    }

    public int getAvailabilityStatus() {
        if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
            return 2;
        }
        if (isUserSupported()) {
            return 0;
        }
        return 3;
    }

    public void updateState(Preference preference) {
        if (isAvailable()) {
            String clazz;
            preference.setVisible(true);
            int userId = getUserId();
            List<Fingerprint> items = this.mFingerprintManager.getEnrolledFingerprints(userId);
            int fingerprintCount = items != null ? items.size() : 0;
            if (fingerprintCount > 0) {
                preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.security_settings_fingerprint_preference_summary, fingerprintCount, new Object[]{Integer.valueOf(fingerprintCount)}));
                clazz = FingerprintSettings.class.getName();
            } else {
                preference.setSummary((int) R.string.security_settings_fingerprint_preference_summary_none);
                clazz = FingerprintEnrollIntroduction.class.getName();
            }
            preference.setOnPreferenceClickListener(new -$$Lambda$FingerprintStatusPreferenceController$KyR_IBe4qHxX_KvME9NBxSJFxFQ(userId, clazz));
            return;
        }
        if (preference != null) {
            preference.setVisible(false);
        }
    }

    static /* synthetic */ boolean lambda$updateState$0(int userId, String clazz, Preference target) {
        Context context = target.getContext();
        if (Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), userId)) {
            return false;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", clazz);
        intent.putExtra("android.intent.extra.USER_ID", userId);
        context.startActivity(intent);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public int getUserId() {
        return this.mUserId;
    }

    /* Access modifiers changed, original: protected */
    public boolean isUserSupported() {
        return true;
    }
}
