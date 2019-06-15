package com.android.settings.password;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternChecker.OnCheckCallback;
import com.android.internal.widget.LockPatternChecker.OnVerifyCallback;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.widget.ImeAwareEditText;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;

public class ConfirmLockPassword extends ConfirmDeviceCredentialBaseActivity {
    private static final int[] DETAIL_TEXTS = new int[]{R.string.lockpassword_confirm_your_pin_generic, R.string.lockpassword_confirm_your_password_generic, R.string.lockpassword_confirm_your_pin_generic_profile, R.string.lockpassword_confirm_your_password_generic_profile, R.string.lockpassword_strong_auth_required_device_pin, R.string.lockpassword_strong_auth_required_device_password, R.string.lockpassword_strong_auth_required_work_pin, R.string.lockpassword_strong_auth_required_work_password};

    public static class ConfirmLockPasswordFragment extends ConfirmDeviceCredentialBaseFragment implements OnClickListener, OnEditorActionListener, Listener, OnGlobalLayoutListener {
        private static final String FRAGMENT_TAG_CHECK_LOCK_RESULT = "check_lock_result";
        private AppearAnimationUtils mAppearAnimationUtils;
        private CountDownTimer mCountdownTimer;
        private CredentialCheckResultTracker mCredentialCheckResultTracker;
        private TextView mDetailsTextView;
        private DisappearAnimationUtils mDisappearAnimationUtils;
        private boolean mDisappearing = false;
        protected boolean mHasWindowFocus = false;
        private TextView mHeaderTextView;
        protected Runnable mHideFodIconRunnable = new Runnable() {
            public void run() {
                ConfirmLockPasswordFragment.this.mFingerprintHelper.startListening();
            }
        };
        private InputMethodManager mImm;
        private boolean mIsAlpha;
        protected int mLastKeypadHeight = 0;
        protected boolean mOnViewCreated = false;
        private ImeAwareEditText mPasswordEntry;
        private TextViewInputDisabler mPasswordEntryInputDisabler;
        private String mPattenString;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private boolean mUsingFingerprint = false;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (OPUtils.isSupportCustomFingerprint()) {
                getActivity().getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (OPUtils.isSupportCustomFingerprint()) {
                getActivity().getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view;
            int i;
            int storedQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mEffectiveUserId);
            if (((ConfirmLockPassword) getActivity()).getConfirmCredentialTheme() == ConfirmCredentialTheme.NORMAL) {
                view = R.layout.confirm_lock_password_normal;
            } else {
                view = R.layout.confirm_lock_password;
            }
            view = inflater.inflate(view, container, false);
            this.mPasswordEntry = (ImeAwareEditText) view.findViewById(R.id.password_entry);
            this.mPasswordEntry.setOnEditorActionListener(this);
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntryInputDisabler = new TextViewInputDisabler(this.mPasswordEntry);
            this.mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            if (this.mHeaderTextView == null) {
                this.mHeaderTextView = (TextView) view.findViewById(R.id.suw_layout_title);
            }
            this.mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
            this.mErrorTextView = (TextView) view.findViewById(R.id.errorText);
            boolean z = 262144 == storedQuality || 327680 == storedQuality || 393216 == storedQuality || 524288 == storedQuality;
            this.mIsAlpha = z;
            this.mImm = (InputMethodManager) getActivity().getSystemService("input_method");
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                CharSequence headerMessage = intent.getCharSequenceExtra(ConfirmDeviceCredentialBaseFragment.HEADER_TEXT);
                CharSequence detailsMessage = intent.getCharSequenceExtra(ConfirmDeviceCredentialBaseFragment.DETAILS_TEXT);
                if (TextUtils.isEmpty(headerMessage)) {
                    headerMessage = getString(getDefaultHeader());
                }
                if (TextUtils.isEmpty(detailsMessage)) {
                    detailsMessage = getString(getDefaultDetails());
                }
                this.mHeaderTextView.setText(headerMessage);
                this.mDetailsTextView.setText(detailsMessage);
            }
            int currentType = this.mPasswordEntry.getInputType();
            ImeAwareEditText imeAwareEditText = this.mPasswordEntry;
            if (this.mIsAlpha) {
                i = currentType;
            } else {
                i = 18;
            }
            imeAwareEditText.setInputType(i);
            this.mPasswordEntry.setTypeface(Typeface.create(getContext().getString(17039704), 0));
            this.mAppearAnimationUtils = new AppearAnimationUtils(getContext(), 220, 2.0f, 1.0f, AnimationUtils.loadInterpolator(getContext(), AndroidResources.LINEAR_OUT_SLOW_IN));
            this.mDisappearAnimationUtils = new DisappearAnimationUtils(getContext(), 110, 1.0f, 0.5f, AnimationUtils.loadInterpolator(getContext(), AndroidResources.FAST_OUT_LINEAR_IN));
            setAccessibilityTitle(this.mHeaderTextView.getText());
            this.mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_CHECK_LOCK_RESULT);
            if (this.mCredentialCheckResultTracker == null) {
                this.mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(this.mCredentialCheckResultTracker, FRAGMENT_TAG_CHECK_LOCK_RESULT).commit();
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ConfirmLockPasswordFragment.this.mOnViewCreated = true;
                }
            }, 200);
            return view;
        }

        public void onGlobalLayout() {
            if (needRefreshFod() && OPUtils.isSupportCustomFingerprint() && this.mOnViewCreated && isFingerprintAllowed()) {
                if (isImeShowUp()) {
                    this.mFingerprintHelper.stopListening();
                } else if (this.mHasWindowFocus) {
                    this.mHandler.removeCallbacks(this.mHideFodIconRunnable);
                    this.mHandler.postDelayed(this.mHideFodIconRunnable, 250);
                }
            }
        }

        private boolean isImeShowUp() {
            Rect r = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int screenHeight = getActivity().getWindow().getDecorView().getHeight();
            if (((double) (screenHeight - r.bottom)) > ((double) screenHeight) * 0.15d) {
                return true;
            }
            return false;
        }

        private boolean needRefreshFod() {
            Rect r = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            int keypadHeight = getActivity().getWindow().getDecorView().getHeight() - r.bottom;
            if (this.mLastKeypadHeight == keypadHeight) {
                return false;
            }
            this.mLastKeypadHeight = keypadHeight;
            return true;
        }

        private int getDefaultHeader() {
            int i;
            if (this.mFrp) {
                if (this.mIsAlpha) {
                    i = R.string.lockpassword_confirm_your_password_header_frp;
                } else {
                    i = R.string.lockpassword_confirm_your_pin_header_frp;
                }
                return i;
            }
            if (this.mIsAlpha) {
                i = R.string.lockpassword_confirm_your_password_header;
            } else {
                i = R.string.lockpassword_confirm_your_pin_header;
            }
            return i;
        }

        private int getDefaultDetails() {
            if (this.mFrp) {
                int i;
                if (this.mIsAlpha) {
                    i = R.string.lockpassword_confirm_your_password_details_frp;
                } else {
                    i = R.string.lockpassword_confirm_your_pin_details_frp;
                }
                return i;
            }
            return ConfirmLockPassword.DETAIL_TEXTS[((isStrongAuthRequired() << 2) + (UserManager.get(getActivity()).isManagedProfile(this.mEffectiveUserId) << 1)) + this.mIsAlpha];
        }

        private int getErrorMessage() {
            if (this.mIsAlpha) {
                return R.string.lockpassword_invalid_password;
            }
            return R.string.lockpassword_invalid_pin;
        }

        /* Access modifiers changed, original: protected */
        public int getLastTryErrorMessage(int userType) {
            int i;
            switch (userType) {
                case 1:
                    if (this.mIsAlpha) {
                        i = R.string.lock_last_password_attempt_before_wipe_device;
                    } else {
                        i = R.string.lock_last_pin_attempt_before_wipe_device;
                    }
                    return i;
                case 2:
                    if (this.mIsAlpha) {
                        i = R.string.lock_last_password_attempt_before_wipe_profile;
                    } else {
                        i = R.string.lock_last_pin_attempt_before_wipe_profile;
                    }
                    return i;
                case 3:
                    if (this.mIsAlpha) {
                        i = R.string.lock_last_password_attempt_before_wipe_user;
                    } else {
                        i = R.string.lock_last_pin_attempt_before_wipe_user;
                    }
                    return i;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unrecognized user type:");
                    stringBuilder.append(userType);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }

        public void prepareEnterAnimation() {
            super.prepareEnterAnimation();
            this.mHeaderTextView.setAlpha(0.0f);
            this.mDetailsTextView.setAlpha(0.0f);
            this.mCancelButton.setAlpha(0.0f);
            this.mPasswordEntry.setAlpha(0.0f);
            this.mErrorTextView.setAlpha(0.0f);
            this.mFingerprintIcon.setAlpha(0.0f);
        }

        private View[] getActiveViews() {
            ArrayList<View> result = new ArrayList();
            result.add(this.mHeaderTextView);
            result.add(this.mDetailsTextView);
            if (this.mCancelButton.getVisibility() == 0) {
                result.add(this.mCancelButton);
            }
            result.add(this.mPasswordEntry);
            result.add(this.mErrorTextView);
            if (this.mFingerprintIcon.getVisibility() == 0) {
                result.add(this.mFingerprintIcon);
            }
            return (View[]) result.toArray(new View[0]);
        }

        public void startEnterAnimation() {
            super.startEnterAnimation();
            this.mAppearAnimationUtils.startAnimation(getActiveViews(), new -$$Lambda$ConfirmLockPassword$ConfirmLockPasswordFragment$Myp25CGN_sn9Gs6wDwuZ61aKfg8(this));
        }

        public void onPause() {
            super.onPause();
            if (OPUtils.isSupportCustomFingerprint()) {
                this.mHandler.removeCallbacks(this.mHideFodIconRunnable);
                this.mFingerprintHelper.stopListening();
            }
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
                this.mCountdownTimer = null;
            }
            this.mCredentialCheckResultTracker.setListener(null);
        }

        public int getMetricsCategory() {
            return 30;
        }

        public void onResume() {
            super.onResume();
            long deadline = this.mLockPatternUtils.getLockoutAttemptDeadline(this.mEffectiveUserId);
            if (deadline != 0) {
                this.mCredentialCheckResultTracker.clearResult();
                handleAttemptLockout(deadline);
            } else {
                updatePasswordEntry();
                this.mErrorTextView.setText("");
                updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
            }
            this.mCredentialCheckResultTracker.setListener(this);
        }

        /* Access modifiers changed, original: protected */
        public void authenticationSucceeded() {
            this.mCredentialCheckResultTracker.setResult(true, new Intent(), 0, this.mEffectiveUserId);
        }

        public void onFingerprintIconVisibilityChanged(boolean visible) {
            this.mUsingFingerprint = visible;
        }

        private void updatePasswordEntry() {
            boolean z = true;
            boolean isLockedOut = this.mLockPatternUtils.getLockoutAttemptDeadline(this.mEffectiveUserId) != 0;
            this.mPasswordEntry.setEnabled(!isLockedOut);
            TextViewInputDisabler textViewInputDisabler = this.mPasswordEntryInputDisabler;
            if (isLockedOut) {
                z = false;
            }
            textViewInputDisabler.setInputEnabled(z);
            if (isLockedOut || this.mUsingFingerprint) {
                this.mImm.hideSoftInputFromWindow(this.mPasswordEntry.getWindowToken(), 0);
            } else {
                this.mPasswordEntry.scheduleShowSoftInput();
            }
        }

        public void onWindowFocusChanged(boolean hasFocus) {
            if (OPUtils.isSupportCustomFingerprint() && this.mOnViewCreated && isFingerprintAllowed()) {
                if (!hasFocus) {
                    this.mFingerprintHelper.stopListening();
                } else if (!isImeShowUp()) {
                    this.mFingerprintHelper.startListening();
                }
            }
            this.mHasWindowFocus = hasFocus;
            if (hasFocus) {
                this.mPasswordEntry.post(new -$$Lambda$ConfirmLockPassword$ConfirmLockPasswordFragment$Myp25CGN_sn9Gs6wDwuZ61aKfg8(this));
            }
        }

        private void handleNext() {
            if (this.mPendingLockCheck == null && !this.mDisappearing) {
                String pin = this.mPasswordEntry.getText().toString();
                if (!TextUtils.isEmpty(pin)) {
                    this.mPasswordEntryInputDisabler.setInputEnabled(false);
                    boolean verifyChallenge = getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
                    Intent intent = new Intent();
                    if (!verifyChallenge) {
                        startCheckPassword(pin, intent);
                    } else if (isInternalActivity()) {
                        startVerifyPassword(pin, intent);
                    } else {
                        this.mCredentialCheckResultTracker.setResult(false, intent, 0, this.mEffectiveUserId);
                    }
                }
            }
        }

        private boolean isInternalActivity() {
            return getActivity() instanceof InternalActivity;
        }

        private void startVerifyPassword(String pin, Intent intent) {
            AsyncTask verifyPassword;
            long challenge = getActivity().getIntent().getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
            final int localEffectiveUserId = this.mEffectiveUserId;
            int localUserId = this.mUserId;
            final Intent intent2 = intent;
            OnVerifyCallback onVerifyCallback = new OnVerifyCallback() {
                public void onVerified(byte[] token, int timeoutMs) {
                    ConfirmLockPasswordFragment.this.mPendingLockCheck = null;
                    boolean matched = false;
                    if (token != null) {
                        matched = true;
                        if (ConfirmLockPasswordFragment.this.mReturnCredentials) {
                            intent2.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
                        }
                    }
                    ConfirmLockPasswordFragment.this.mCredentialCheckResultTracker.setResult(matched, intent2, timeoutMs, localEffectiveUserId);
                }
            };
            if (localEffectiveUserId == localUserId) {
                verifyPassword = LockPatternChecker.verifyPassword(this.mLockPatternUtils, pin, challenge, localUserId, onVerifyCallback);
            } else {
                verifyPassword = LockPatternChecker.verifyTiedProfileChallenge(this.mLockPatternUtils, pin, false, challenge, localUserId, onVerifyCallback);
            }
            this.mPendingLockCheck = verifyPassword;
        }

        private void startCheckPassword(final String pin, final Intent intent) {
            final int localEffectiveUserId = this.mEffectiveUserId;
            this.mPattenString = pin;
            this.mPendingLockCheck = LockPatternChecker.checkPassword(this.mLockPatternUtils, pin, localEffectiveUserId, new OnCheckCallback() {
                public void onChecked(boolean matched, int timeoutMs) {
                    ConfirmLockPasswordFragment.this.mPendingLockCheck = null;
                    if (matched && ConfirmLockPasswordFragment.this.isInternalActivity() && ConfirmLockPasswordFragment.this.mReturnCredentials) {
                        int i;
                        Intent intent = intent;
                        String str = "type";
                        if (ConfirmLockPasswordFragment.this.mIsAlpha) {
                            i = 0;
                        } else {
                            i = 2;
                        }
                        intent.putExtra(str, i);
                        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, pin);
                    }
                    ConfirmLockPasswordFragment.this.mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs, localEffectiveUserId);
                }
            });
        }

        private void startDisappearAnimation(Intent intent) {
            if (!this.mDisappearing) {
                this.mDisappearing = true;
                ConfirmLockPassword activity = (ConfirmLockPassword) getActivity();
                if (activity != null && !activity.isFinishing()) {
                    if (activity.getConfirmCredentialTheme() == ConfirmCredentialTheme.DARK) {
                        this.mDisappearAnimationUtils.startAnimation(getActiveViews(), new -$$Lambda$ConfirmLockPassword$ConfirmLockPasswordFragment$hwD4uLqRx_u_wyU3V7MV_afxC5o(activity, intent));
                    } else {
                        intent.putExtra("power_on_psw", this.mPattenString);
                        activity.setResult(-1, intent);
                        activity.finish();
                    }
                }
            }
        }

        static /* synthetic */ void lambda$startDisappearAnimation$0(ConfirmLockPassword activity, Intent intent) {
            activity.setResult(-1, intent);
            activity.finish();
            activity.overridePendingTransition(R.anim.confirm_credential_close_enter, R.anim.confirm_credential_close_exit);
        }

        private void onPasswordChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            this.mPasswordEntryInputDisabler.setInputEnabled(true);
            if (matched) {
                if (newResult) {
                    reportSuccessfulAttempt();
                }
                startDisappearAnimation(intent);
                checkForPendingIntent();
                return;
            }
            if (timeoutMs > 0) {
                refreshLockScreen();
                handleAttemptLockout(this.mLockPatternUtils.setLockoutAttemptDeadline(effectiveUserId, timeoutMs));
            } else {
                showError(getErrorMessage(), 3000);
            }
            if (newResult) {
                reportFailedAttempt();
            }
        }

        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            onPasswordChecked(matched, intent, timeoutMs, effectiveUserId, newResult);
        }

        /* Access modifiers changed, original: protected */
        public void onShowError() {
            this.mPasswordEntry.setText(null);
        }

        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            this.mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - SystemClock.elapsedRealtime(), 1000) {
                public void onTick(long millisUntilFinished) {
                    int secondsCountdown = (int) (millisUntilFinished / 1000);
                    ConfirmLockPasswordFragment.this.showError((CharSequence) ConfirmLockPasswordFragment.this.getString(R.string.lockpattern_too_many_failed_confirmation_attempts, new Object[]{Integer.valueOf(secondsCountdown)}), 0);
                }

                public void onFinish() {
                    ConfirmLockPasswordFragment.this.updatePasswordEntry();
                    ConfirmLockPasswordFragment.this.mErrorTextView.setText("");
                    ConfirmLockPasswordFragment.this.mPasswordEntry.requestFocus();
                    ConfirmLockPasswordFragment.this.updateErrorMessage(ConfirmLockPasswordFragment.this.mLockPatternUtils.getCurrentFailedPasswordAttempts(ConfirmLockPasswordFragment.this.mEffectiveUserId));
                }
            }.start();
            updatePasswordEntry();
        }

        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.cancel_button) {
                getActivity().setResult(0);
                getActivity().finish();
            } else if (id == R.id.next_button) {
                handleNext();
            }
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId != 0 && actionId != 6 && actionId != 5) {
                return false;
            }
            handleNext();
            return true;
        }
    }

    public static class InternalActivity extends ConfirmLockPassword {
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, ConfirmLockPasswordFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (ConfirmLockPasswordFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && (fragment instanceof ConfirmLockPasswordFragment)) {
            ((ConfirmLockPasswordFragment) fragment).onWindowFocusChanged(hasFocus);
        }
    }
}
