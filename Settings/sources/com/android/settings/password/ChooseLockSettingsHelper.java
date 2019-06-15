package com.android.settings.password;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.UserManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.password.ConfirmLockPattern.InternalActivity;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.google.common.primitives.Ints;

public final class ChooseLockSettingsHelper {
    public static final String EXTRA_ALLOW_ANY_USER = "allow_any_user";
    public static final String EXTRA_KEY_CHALLENGE = "challenge";
    public static final String EXTRA_KEY_CHALLENGE_TOKEN = "hw_auth_token";
    public static final String EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT = "for_cred_req_boot";
    public static final String EXTRA_KEY_FOR_FINGERPRINT = "for_fingerprint";
    public static final String EXTRA_KEY_HAS_CHALLENGE = "has_challenge";
    public static final String EXTRA_KEY_PASSWORD = "password";
    public static final String EXTRA_KEY_RETURN_CREDENTIALS = "return_credentials";
    public static final String EXTRA_KEY_TYPE = "type";
    private Activity mActivity;
    private Fragment mFragment;
    @VisibleForTesting
    LockPatternUtils mLockPatternUtils;

    public ChooseLockSettingsHelper(Activity activity) {
        this.mActivity = activity;
        this.mLockPatternUtils = new LockPatternUtils(activity);
    }

    public ChooseLockSettingsHelper(Activity activity, Fragment fragment) {
        this(activity);
        this.mFragment = fragment;
    }

    public LockPatternUtils utils() {
        return this.mLockPatternUtils;
    }

    public boolean launchConfirmationActivity(int request, CharSequence title) {
        return launchConfirmationActivity(request, title, null, null, false, false);
    }

    public boolean launchConfirmationActivity(int request, CharSequence title, boolean returnCredentials) {
        return launchConfirmationActivity(request, title, null, null, returnCredentials, false);
    }

    public boolean launchConfirmationActivity(int request, CharSequence title, boolean returnCredentials, int userId) {
        return launchConfirmationActivity(request, title, null, null, returnCredentials, false, false, 0, Utils.enforceSameOwner(this.mActivity, userId));
    }

    /* Access modifiers changed, original: 0000 */
    public boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, boolean returnCredentials, boolean external) {
        return launchConfirmationActivity(request, title, header, description, returnCredentials, external, false, 0, Utils.getCredentialOwnerUserId(this.mActivity));
    }

    /* Access modifiers changed, original: 0000 */
    public boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, boolean returnCredentials, boolean external, int userId) {
        return launchConfirmationActivity(request, title, header, description, returnCredentials, external, false, 0, Utils.enforceSameOwner(this.mActivity, userId));
    }

    public boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, long challenge) {
        return launchConfirmationActivity(request, title, header, description, true, false, true, challenge, Utils.getCredentialOwnerUserId(this.mActivity));
    }

    public boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description, true, false, true, challenge, Utils.enforceSameOwner(this.mActivity, userId));
    }

    public boolean launchConfirmationActivityWithExternalAndChallenge(int request, CharSequence title, CharSequence header, CharSequence description, boolean external, long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description, false, external, true, challenge, Utils.enforceSameOwner(this.mActivity, userId));
    }

    public boolean launchConfirmationActivityForAnyUser(int request, CharSequence title, CharSequence header, CharSequence description, int userId) {
        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_ALLOW_ANY_USER, true);
        return launchConfirmationActivity(request, title, header, description, false, false, true, 0, userId, extras);
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, boolean returnCredentials, boolean external, boolean hasChallenge, long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description, returnCredentials, external, hasChallenge, challenge, userId, null, null);
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, boolean returnCredentials, boolean external, boolean hasChallenge, long challenge, int userId, Bundle extras) {
        return launchConfirmationActivity(request, title, header, description, returnCredentials, external, hasChallenge, challenge, userId, null, extras);
    }

    public boolean launchFrpConfirmationActivity(int request, CharSequence header, CharSequence description, CharSequence alternateButton) {
        return launchConfirmationActivity(request, null, header, description, false, true, false, 0, -9999, alternateButton, null);
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence description, boolean returnCredentials, boolean external, boolean hasChallenge, long challenge, int userId, CharSequence alternateButton, Bundle extras) {
        int i = userId;
        int effectiveUserId = UserManager.get(this.mActivity).getCredentialOwnerProfile(i);
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(effectiveUserId);
        Class cls;
        if (keyguardStoredPasswordQuality == 65536) {
            if (returnCredentials || hasChallenge) {
                cls = InternalActivity.class;
            } else {
                cls = ConfirmLockPattern.class;
            }
            return launchConfirmationActivity(request, title, header, description, cls, returnCredentials, external, hasChallenge, challenge, userId, alternateButton, extras);
        } else if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608 || keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
            if (returnCredentials || hasChallenge) {
                cls = ConfirmLockPassword.InternalActivity.class;
            } else {
                cls = ConfirmLockPassword.class;
            }
            return launchConfirmationActivity(request, title, header, description, cls, returnCredentials, external, hasChallenge, challenge, i, alternateButton, extras);
        } else {
            int i2 = effectiveUserId;
            return false;
        }
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header, CharSequence message, Class<?> activityClass, boolean returnCredentials, boolean external, boolean hasChallenge, long challenge, int userId, CharSequence alternateButton, Bundle extras) {
        int i = request;
        boolean z = external;
        Bundle bundle = extras;
        Intent intent = new Intent();
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.TITLE_TEXT, title);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.HEADER_TEXT, header);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.DETAILS_TEXT, message);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.ALLOW_FP_AUTHENTICATION, z);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.DARK_THEME, false);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.SHOW_CANCEL_BUTTON, false);
        intent.putExtra(ConfirmDeviceCredentialBaseFragment.SHOW_WHEN_LOCKED, z);
        intent.putExtra(EXTRA_KEY_RETURN_CREDENTIALS, returnCredentials);
        intent.putExtra(EXTRA_KEY_HAS_CHALLENGE, hasChallenge);
        intent.putExtra(EXTRA_KEY_CHALLENGE, challenge);
        intent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, true);
        intent.putExtra("android.intent.extra.USER_ID", userId);
        intent.putExtra("android.app.extra.ALTERNATE_BUTTON_LABEL", alternateButton);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setClassName("com.android.settings", activityClass.getName());
        if (z) {
            intent.addFlags(33554432);
            if (this.mFragment != null) {
                copyOptionalExtras(this.mFragment.getActivity().getIntent(), intent);
                this.mFragment.startActivity(intent);
            } else {
                copyOptionalExtras(this.mActivity.getIntent(), intent);
                this.mActivity.startActivity(intent);
            }
        } else if (this.mFragment != null) {
            copyInternalExtras(this.mFragment.getActivity().getIntent(), intent);
            this.mFragment.startActivityForResult(intent, i);
        } else {
            copyInternalExtras(this.mActivity.getIntent(), intent);
            this.mActivity.startActivityForResult(intent, i);
        }
        return true;
    }

    private void copyOptionalExtras(Intent inIntent, Intent outIntent) {
        IntentSender intentSender = (IntentSender) inIntent.getParcelableExtra("android.intent.extra.INTENT");
        if (intentSender != null) {
            outIntent.putExtra("android.intent.extra.INTENT", intentSender);
        }
        int taskId = inIntent.getIntExtra("android.intent.extra.TASK_ID", -1);
        if (taskId != -1) {
            outIntent.putExtra("android.intent.extra.TASK_ID", taskId);
        }
        if (intentSender != null || taskId != -1) {
            outIntent.addFlags(8388608);
            outIntent.addFlags(Ints.MAX_POWER_OF_TWO);
        }
    }

    private void copyInternalExtras(Intent inIntent, Intent outIntent) {
        String theme = inIntent.getStringExtra(WizardManagerHelper.EXTRA_THEME);
        if (theme != null) {
            outIntent.putExtra(WizardManagerHelper.EXTRA_THEME, theme);
        }
    }
}
