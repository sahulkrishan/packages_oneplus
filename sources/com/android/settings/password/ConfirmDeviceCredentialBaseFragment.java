package com.android.settings.password;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.UserInfo;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.fingerprint.FingerprintUiHelper;
import com.android.settings.fingerprint.FingerprintUiHelper.Callback;
import com.android.settings.password.ConfirmLockPassword.InternalActivity;

public abstract class ConfirmDeviceCredentialBaseFragment extends InstrumentedFragment implements Callback {
    public static final String ALLOW_FP_AUTHENTICATION = "com.android.settings.ConfirmCredentials.allowFpAuthentication";
    protected static final long CLEAR_WRONG_ATTEMPT_TIMEOUT_MS = 3000;
    public static final String DARK_THEME = "com.android.settings.ConfirmCredentials.darkTheme";
    public static final String DETAILS_TEXT = "com.android.settings.ConfirmCredentials.details";
    public static final String HEADER_TEXT = "com.android.settings.ConfirmCredentials.header";
    public static final String PACKAGE = "com.android.settings";
    public static final String SHOW_CANCEL_BUTTON = "com.android.settings.ConfirmCredentials.showCancelButton";
    public static final String SHOW_WHEN_LOCKED = "com.android.settings.ConfirmCredentials.showWhenLocked";
    public static final String TITLE_TEXT = "com.android.settings.ConfirmCredentials.title";
    protected static final int USER_TYPE_MANAGED_PROFILE = 2;
    protected static final int USER_TYPE_PRIMARY = 1;
    protected static final int USER_TYPE_SECONDARY = 3;
    protected boolean isLockedOutStage;
    protected Button mCancelButton;
    protected DevicePolicyManager mDevicePolicyManager;
    protected int mEffectiveUserId;
    protected TextView mErrorTextView;
    protected FingerprintUiHelper mFingerprintHelper;
    protected ImageView mFingerprintIcon;
    protected boolean mFrp;
    private CharSequence mFrpAlternateButtonText;
    protected final Handler mHandler = new Handler();
    protected LockPatternUtils mLockPatternUtils;
    private final Runnable mResetErrorRunnable = new Runnable() {
        public void run() {
            if (!ConfirmDeviceCredentialBaseFragment.this.isLockedOutStage) {
                ConfirmDeviceCredentialBaseFragment.this.mErrorTextView.setText("");
            }
        }
    };
    protected boolean mReturnCredentials = false;
    protected int mUserId;
    protected UserManager mUserManager;

    public static class LastTryDialog extends DialogFragment {
        private static final String ARG_BUTTON = "button";
        private static final String ARG_DISMISS = "dismiss";
        private static final String ARG_MESSAGE = "message";
        private static final String ARG_TITLE = "title";
        private static final String TAG = LastTryDialog.class.getSimpleName();

        static boolean show(FragmentManager from, String title, int message, int button, boolean dismiss) {
            LastTryDialog existent = (LastTryDialog) from.findFragmentByTag(TAG);
            if (existent != null && !existent.isRemoving()) {
                return false;
            }
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putInt(ARG_MESSAGE, message);
            args.putInt(ARG_BUTTON, button);
            args.putBoolean(ARG_DISMISS, dismiss);
            DialogFragment dialog = new LastTryDialog();
            dialog.setArguments(args);
            dialog.show(from, TAG);
            from.executePendingTransactions();
            return true;
        }

        static void hide(FragmentManager from) {
            LastTryDialog dialog = (LastTryDialog) from.findFragmentByTag(TAG);
            if (dialog != null) {
                dialog.dismissAllowingStateLoss();
                from.executePendingTransactions();
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Builder(getActivity()).setTitle(getArguments().getString("title")).setMessage(getArguments().getInt(ARG_MESSAGE)).setPositiveButton(getArguments().getInt(ARG_BUTTON), null).create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (getActivity() != null && getArguments().getBoolean(ARG_DISMISS)) {
                getActivity().finish();
            }
        }
    }

    public abstract void authenticationSucceeded();

    public abstract int getLastTryErrorMessage(int i);

    public abstract void onShowError();

    private boolean isInternalActivity() {
        return (getActivity() instanceof InternalActivity) || (getActivity() instanceof ConfirmLockPattern.InternalActivity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFrpAlternateButtonText = getActivity().getIntent().getCharSequenceExtra("android.app.extra.ALTERNATE_BUTTON_LABEL");
        boolean z = false;
        this.mReturnCredentials = getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_RETURN_CREDENTIALS, false);
        this.mUserId = Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras(), isInternalActivity());
        if (this.mUserId == -9999) {
            z = true;
        }
        this.mFrp = z;
        this.mUserManager = UserManager.get(getActivity());
        this.mEffectiveUserId = this.mUserManager.getCredentialOwnerProfile(this.mUserId);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        this.mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService("device_policy");
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mCancelButton = (Button) view.findViewById(R.id.cancelButton);
        this.mFingerprintIcon = (ImageView) view.findViewById(R.id.fingerprintIcon);
        this.mFingerprintHelper = new FingerprintUiHelper(this.mFingerprintIcon, (TextView) view.findViewById(R.id.errorText), this, this.mEffectiveUserId);
        int i = 0;
        boolean showCancelButton = getActivity().getIntent().getBooleanExtra(SHOW_CANCEL_BUTTON, false);
        final boolean hasAlternateButton = this.mFrp && !TextUtils.isEmpty(this.mFrpAlternateButtonText);
        Button button = this.mCancelButton;
        if (!(showCancelButton || hasAlternateButton)) {
            i = 8;
        }
        button.setVisibility(i);
        if (hasAlternateButton) {
            this.mCancelButton.setText(this.mFrpAlternateButtonText);
        }
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (hasAlternateButton) {
                    ConfirmDeviceCredentialBaseFragment.this.getActivity().setResult(1);
                }
                ConfirmDeviceCredentialBaseFragment.this.getActivity().finish();
            }
        });
        i = Utils.getCredentialOwnerUserId(getActivity(), Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras(), isInternalActivity()));
        if (this.mUserManager.isManagedProfile(i)) {
            setWorkChallengeBackground(view, i);
        }
    }

    private boolean isFingerprintDisabledByAdmin() {
        return (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mEffectiveUserId) & 32) != 0;
    }

    /* Access modifiers changed, original: protected */
    public boolean isStrongAuthRequired() {
        return (!this.mFrp && this.mLockPatternUtils.isFingerprintAllowedForUser(this.mEffectiveUserId) && this.mUserManager.isUserUnlocked(this.mUserId)) ? false : true;
    }

    /* Access modifiers changed, original: protected */
    public boolean isFingerprintNeedShowDarkTheme() {
        return getActivity().getIntent().getBooleanExtra(ALLOW_FP_AUTHENTICATION, false);
    }

    /* Access modifiers changed, original: protected */
    public boolean isFingerprintAllowed() {
        if (this.mReturnCredentials || !getActivity().getIntent().getBooleanExtra(ALLOW_FP_AUTHENTICATION, false) || isStrongAuthRequired() || isFingerprintDisabledByAdmin()) {
            return false;
        }
        return true;
    }

    public void onResume() {
        super.onResume();
        refreshLockScreen();
    }

    /* Access modifiers changed, original: protected */
    public void refreshLockScreen() {
        if (isFingerprintAllowed()) {
            this.mFingerprintHelper.startListening();
        } else if (this.mFingerprintHelper.isListening()) {
            this.mFingerprintHelper.stopListening();
        }
        updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
    }

    /* Access modifiers changed, original: protected */
    public void setAccessibilityTitle(CharSequence supplementalText) {
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            CharSequence titleText = intent.getCharSequenceExtra(TITLE_TEXT);
            if (supplementalText != null) {
                if (titleText == null) {
                    getActivity().setTitle(supplementalText);
                } else {
                    String accessibilityTitle = new StringBuilder(titleText);
                    accessibilityTitle.append(",");
                    accessibilityTitle.append(supplementalText);
                    getActivity().setTitle(Utils.createAccessibleSequence(titleText, accessibilityTitle.toString()));
                }
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mFingerprintHelper.isListening()) {
            this.mFingerprintHelper.stopListening();
        }
    }

    public void onAuthenticated() {
        if (getActivity() != null && getActivity().isResumed()) {
            ((TrustManager) getActivity().getSystemService("trust")).setDeviceLockedForUser(this.mEffectiveUserId, false);
            authenticationSucceeded();
            checkForPendingIntent();
        }
    }

    public void onFingerprintIconVisibilityChanged(boolean visible) {
    }

    public void prepareEnterAnimation() {
    }

    public void startEnterAnimation() {
    }

    /* Access modifiers changed, original: protected */
    public void checkForPendingIntent() {
        int taskId = getActivity().getIntent().getIntExtra("android.intent.extra.TASK_ID", -1);
        if (taskId != -1) {
            try {
                ActivityManager.getService().startActivityFromRecents(taskId, ActivityOptions.makeBasic().toBundle());
                return;
            } catch (RemoteException e) {
            }
        }
        IntentSender intentSender = (IntentSender) getActivity().getIntent().getParcelableExtra("android.intent.extra.INTENT");
        if (intentSender != null) {
            try {
                getActivity().startIntentSenderForResult(intentSender, -1, null, 0, 0, 0);
            } catch (SendIntentException e2) {
            }
        }
    }

    private void setWorkChallengeBackground(View baseView, int userId) {
        View mainContent = getActivity().findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setPadding(0, 0, 0, 0);
        }
        baseView.setBackground(new ColorDrawable(this.mDevicePolicyManager.getOrganizationColorForUser(userId)));
        ImageView imageView = (ImageView) baseView.findViewById(R.id.background_image);
        if (imageView != null) {
            Drawable image = getResources().getDrawable(R.drawable.work_challenge_background);
            image.setColorFilter(getResources().getColor(R.color.confirm_device_credential_transparent_black), Mode.DARKEN);
            imageView.setImageDrawable(image);
            Point screenSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
            imageView.setLayoutParams(new LayoutParams(-1, screenSize.y));
        }
    }

    /* Access modifiers changed, original: protected */
    public void reportSuccessfulAttempt() {
        this.mLockPatternUtils.reportSuccessfulPasswordAttempt(this.mEffectiveUserId);
        if (this.mUserManager.isManagedProfile(this.mEffectiveUserId)) {
            this.mLockPatternUtils.userPresent(this.mEffectiveUserId);
        }
    }

    /* Access modifiers changed, original: protected */
    public void reportFailedAttempt() {
        updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId) + 1);
        this.mLockPatternUtils.reportFailedPasswordAttempt(this.mEffectiveUserId);
    }

    /* Access modifiers changed, original: protected */
    public void updateErrorMessage(int numAttempts) {
        int maxAttempts = this.mLockPatternUtils.getMaximumFailedPasswordsForWipe(this.mEffectiveUserId);
        if (maxAttempts > 0 && numAttempts > 0) {
            if (this.mErrorTextView != null) {
                showError(getActivity().getString(R.string.lock_failed_attempts_before_wipe, new Object[]{Integer.valueOf(numAttempts), Integer.valueOf(maxAttempts)}), 0);
            }
            int remainingAttempts = maxAttempts - numAttempts;
            if (remainingAttempts <= 1) {
                FragmentManager fragmentManager = getChildFragmentManager();
                int userType = getUserTypeForWipe();
                if (remainingAttempts == 1) {
                    LastTryDialog.show(fragmentManager, getActivity().getString(R.string.lock_last_attempt_before_wipe_warning_title), getLastTryErrorMessage(userType), 17039370, false);
                } else {
                    LastTryDialog.show(fragmentManager, null, getWipeMessage(userType), R.string.lock_failed_attempts_now_wiping_dialog_dismiss, true);
                }
            }
        }
    }

    private int getUserTypeForWipe() {
        UserInfo userToBeWiped = this.mUserManager.getUserInfo(this.mDevicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(this.mEffectiveUserId));
        if (userToBeWiped == null || userToBeWiped.isPrimary()) {
            return 1;
        }
        if (userToBeWiped.isManagedProfile()) {
            return 2;
        }
        return 3;
    }

    private int getWipeMessage(int userType) {
        switch (userType) {
            case 1:
                return R.string.lock_failed_attempts_now_wiping_device;
            case 2:
                return R.string.lock_failed_attempts_now_wiping_profile;
            case 3:
                return R.string.lock_failed_attempts_now_wiping_user;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unrecognized user type:");
                stringBuilder.append(userType);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public void showError(CharSequence msg, long timeout) {
        this.mErrorTextView.setText(msg);
        onShowError();
        this.mHandler.removeCallbacks(this.mResetErrorRunnable);
        if (timeout != 0) {
            this.mHandler.postDelayed(this.mResetErrorRunnable, timeout);
        }
    }

    /* Access modifiers changed, original: protected */
    public void showError(int msg, long timeout) {
        showError(getText(msg), timeout);
    }
}
