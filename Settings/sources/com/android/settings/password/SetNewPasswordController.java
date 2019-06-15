package com.android.settings.password;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;

final class SetNewPasswordController {
    private final DevicePolicyManager mDevicePolicyManager;
    private final FingerprintManager mFingerprintManager;
    private final PackageManager mPackageManager;
    private final int mTargetUserId;
    private final Ui mUi;

    interface Ui {
        void launchChooseLock(Bundle bundle);
    }

    public static SetNewPasswordController create(Context context, Ui ui, Intent intent, IBinder activityToken) {
        int userId = ActivityManager.getCurrentUser();
        if ("android.app.action.SET_NEW_PASSWORD".equals(intent.getAction())) {
            int callingUserId = Utils.getSecureTargetUser(activityToken, UserManager.get(context), null, intent.getExtras()).getIdentifier();
            if (new LockPatternUtils(context).isSeparateProfileChallengeAllowed(callingUserId)) {
                userId = callingUserId;
            }
        }
        return new SetNewPasswordController(userId, context.getPackageManager(), Utils.getFingerprintManagerOrNull(context), (DevicePolicyManager) context.getSystemService("device_policy"), ui);
    }

    @VisibleForTesting
    SetNewPasswordController(int targetUserId, PackageManager packageManager, FingerprintManager fingerprintManager, DevicePolicyManager devicePolicyManager, Ui ui) {
        this.mTargetUserId = targetUserId;
        this.mPackageManager = (PackageManager) Preconditions.checkNotNull(packageManager);
        this.mFingerprintManager = fingerprintManager;
        this.mDevicePolicyManager = (DevicePolicyManager) Preconditions.checkNotNull(devicePolicyManager);
        this.mUi = (Ui) Preconditions.checkNotNull(ui);
    }

    public void dispatchSetNewPasswordIntent() {
        Bundle extras;
        if (!this.mPackageManager.hasSystemFeature("android.hardware.fingerprint") || this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected() || this.mFingerprintManager.hasEnrolledFingerprints(this.mTargetUserId) || isFingerprintDisabledByAdmin()) {
            extras = new Bundle();
        } else {
            extras = getFingerprintChooseLockExtras();
        }
        extras.putInt("android.intent.extra.USER_ID", this.mTargetUserId);
        this.mUi.launchChooseLock(extras);
    }

    private Bundle getFingerprintChooseLockExtras() {
        Bundle chooseLockExtras = new Bundle();
        long challenge = this.mFingerprintManager.preEnroll();
        chooseLockExtras.putInt(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
        chooseLockExtras.putBoolean(ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
        chooseLockExtras.putBoolean(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
        chooseLockExtras.putLong(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        chooseLockExtras.putBoolean(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, true);
        return chooseLockExtras;
    }

    private boolean isFingerprintDisabledByAdmin() {
        return (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mTargetUserId) & 32) != 0;
    }
}
