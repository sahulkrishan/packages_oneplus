package com.android.settings.password;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternChecker.OnCheckCallback;
import com.android.internal.widget.LockPatternChecker.OnVerifyCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.CellState;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settingslib.animation.AppearAnimationCreator;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.AppearAnimationUtils.RowTranslationScaler;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.oneplus.settings.utils.OPUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfirmLockPattern extends ConfirmDeviceCredentialBaseActivity {

    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }

    public static class ConfirmLockPatternFragment extends ConfirmDeviceCredentialBaseFragment implements AppearAnimationCreator<Object>, Listener {
        private static final String FRAGMENT_TAG_CHECK_LOCK_RESULT = "check_lock_result";
        private AppearAnimationUtils mAppearAnimationUtils;
        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                ConfirmLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private OnPatternListener mConfirmExistingLockPatternListener = new OnPatternListener() {
            public void onPatternStart() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCleared() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<Cell> list) {
            }

            public void onPatternDetected(List<Cell> pattern) {
                if (ConfirmLockPatternFragment.this.mPendingLockCheck == null && !ConfirmLockPatternFragment.this.mDisappearing) {
                    ConfirmLockPatternFragment.this.mLockPatternView.setEnabled(false);
                    boolean verifyChallenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
                    Intent intent = new Intent();
                    if (!verifyChallenge) {
                        startCheckPattern(pattern, intent);
                    } else if (isInternalActivity()) {
                        startVerifyPattern(pattern, intent);
                    } else {
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                    }
                }
            }

            private boolean isInternalActivity() {
                return ConfirmLockPatternFragment.this.getActivity() instanceof InternalActivity;
            }

            private void startVerifyPattern(List<Cell> pattern, Intent intent) {
                AsyncTask verifyPattern;
                ConfirmLockPatternFragment confirmLockPatternFragment;
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                int localUserId = ConfirmLockPatternFragment.this.mUserId;
                long challenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
                final Intent intent2 = intent;
                OnVerifyCallback onVerifyCallback = new OnVerifyCallback() {
                    public void onVerified(byte[] token, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        boolean matched = false;
                        if (token != null) {
                            matched = true;
                            if (ConfirmLockPatternFragment.this.mReturnCredentials) {
                                intent2.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
                            }
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(matched, intent2, timeoutMs, localEffectiveUserId);
                    }
                };
                ConfirmLockPatternFragment confirmLockPatternFragment2 = ConfirmLockPatternFragment.this;
                if (localEffectiveUserId == localUserId) {
                    verifyPattern = LockPatternChecker.verifyPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, challenge, localUserId, onVerifyCallback);
                    confirmLockPatternFragment = confirmLockPatternFragment2;
                } else {
                    confirmLockPatternFragment = confirmLockPatternFragment2;
                    verifyPattern = LockPatternChecker.verifyTiedProfileChallenge(ConfirmLockPatternFragment.this.mLockPatternUtils, LockPatternUtils.patternToString(pattern), true, challenge, localUserId, onVerifyCallback);
                }
                confirmLockPatternFragment.mPendingLockCheck = verifyPattern;
            }

            private void startCheckPattern(final List<Cell> pattern, final Intent intent) {
                if (pattern.size() < 4) {
                    ConfirmLockPatternFragment.this.onPatternChecked(false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId, false);
                    return;
                }
                ConfirmLockPatternFragment.this.mPattenString = LockPatternUtils.patternToString(pattern);
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                ConfirmLockPatternFragment.this.mPendingLockCheck = LockPatternChecker.checkPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, localEffectiveUserId, new OnCheckCallback() {
                    public void onChecked(boolean matched, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        if (matched && AnonymousClass3.this.isInternalActivity() && ConfirmLockPatternFragment.this.mReturnCredentials) {
                            intent.putExtra("type", 2);
                            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, LockPatternUtils.patternToString(pattern));
                        }
                        ConfirmLockPatternFragment.this.mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs, localEffectiveUserId);
                    }
                });
            }
        };
        private CountDownTimer mCountdownTimer;
        private CredentialCheckResultTracker mCredentialCheckResultTracker;
        private CharSequence mDetailsText;
        private TextView mDetailsTextView;
        private DisappearAnimationUtils mDisappearAnimationUtils;
        private boolean mDisappearing = false;
        private CharSequence mHeaderText;
        private TextView mHeaderTextView;
        private View mLeftSpacerLandscape;
        private LockPatternView mLockPatternView;
        private String mPattenString;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private View mRightSpacerLandscape;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view;
            ConfirmLockPattern activity = (ConfirmLockPattern) getActivity();
            if (activity.getConfirmCredentialTheme() == ConfirmCredentialTheme.NORMAL) {
                view = R.layout.confirm_lock_pattern_normal;
            } else {
                view = R.layout.confirm_lock_pattern;
            }
            view = inflater.inflate(view, container, false);
            this.mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            if (this.mHeaderTextView != null && OPUtils.isSupportCustomFingerprint() && isFingerprintNeedShowDarkTheme()) {
                this.mHeaderTextView.setTextColor(activity.getColor(R.color.oneplus_contorl_text_color_primary_dark));
            }
            this.mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
            this.mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
            this.mErrorTextView = (TextView) view.findViewById(R.id.errorText);
            this.mLeftSpacerLandscape = view.findViewById(R.id.leftSpacer);
            this.mRightSpacerLandscape = view.findViewById(R.id.rightSpacer);
            ((LinearLayoutWithDefaultTouchRecepient) view.findViewById(R.id.topLayout)).setDefaultTouchRecepient(this.mLockPatternView);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                this.mHeaderText = intent.getCharSequenceExtra(ConfirmDeviceCredentialBaseFragment.HEADER_TEXT);
                this.mDetailsText = intent.getCharSequenceExtra(ConfirmDeviceCredentialBaseFragment.DETAILS_TEXT);
            }
            this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
            this.mLockPatternView.setInStealthMode(this.mLockPatternUtils.isVisiblePatternEnabled(this.mEffectiveUserId) ^ 1);
            this.mLockPatternView.setOnPatternListener(this.mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);
            if (!(savedInstanceState != null || this.mFrp || this.mLockPatternUtils.isLockPatternEnabled(this.mEffectiveUserId))) {
                getActivity().setResult(-1);
                getActivity().finish();
            }
            this.mAppearAnimationUtils = new AppearAnimationUtils(getContext(), 220, 2.0f, 1.3f, AnimationUtils.loadInterpolator(getContext(), AndroidResources.LINEAR_OUT_SLOW_IN));
            this.mDisappearAnimationUtils = new DisappearAnimationUtils(getContext(), 125, 4.0f, 0.3f, AnimationUtils.loadInterpolator(getContext(), AndroidResources.FAST_OUT_LINEAR_IN), new RowTranslationScaler() {
                public float getRowTranslationScale(int row, int numRows) {
                    return ((float) (numRows - row)) / ((float) numRows);
                }
            });
            setAccessibilityTitle(this.mHeaderTextView.getText());
            this.mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_CHECK_LOCK_RESULT);
            if (this.mCredentialCheckResultTracker == null) {
                this.mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(this.mCredentialCheckResultTracker, FRAGMENT_TAG_CHECK_LOCK_RESULT).commit();
            }
            return view;
        }

        public void onSaveInstanceState(Bundle outState) {
        }

        public void onPause() {
            super.onPause();
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
            }
            this.mCredentialCheckResultTracker.setListener(null);
        }

        public int getMetricsCategory() {
            return 31;
        }

        public void onResume() {
            super.onResume();
            long deadline = this.mLockPatternUtils.getLockoutAttemptDeadline(this.mEffectiveUserId);
            if (deadline != 0) {
                this.mCredentialCheckResultTracker.clearResult();
                handleAttemptLockout(deadline);
            } else if (!this.mLockPatternView.isEnabled()) {
                updateStage(Stage.NeedToUnlock);
            }
            this.mCredentialCheckResultTracker.setListener(this);
        }

        /* Access modifiers changed, original: protected */
        public void onShowError() {
        }

        public void prepareEnterAnimation() {
            super.prepareEnterAnimation();
            this.mHeaderTextView.setAlpha(0.0f);
            this.mCancelButton.setAlpha(0.0f);
            this.mLockPatternView.setAlpha(0.0f);
            this.mDetailsTextView.setAlpha(0.0f);
            this.mFingerprintIcon.setAlpha(0.0f);
        }

        private int getDefaultDetails() {
            if (this.mFrp) {
                return R.string.lockpassword_confirm_your_pattern_details_frp;
            }
            boolean isStrongAuthRequired = isStrongAuthRequired();
            int i;
            if (UserManager.get(getActivity()).isManagedProfile(this.mEffectiveUserId)) {
                if (isStrongAuthRequired) {
                    i = R.string.lockpassword_strong_auth_required_work_pattern;
                } else {
                    i = R.string.lockpassword_confirm_your_pattern_generic_profile;
                }
                return i;
            }
            if (isStrongAuthRequired) {
                i = R.string.lockpassword_strong_auth_required_device_pattern;
            } else {
                i = R.string.lockpassword_confirm_your_pattern_generic;
            }
            return i;
        }

        private Object[][] getActiveViews() {
            ArrayList<ArrayList<Object>> result = new ArrayList();
            result.add(new ArrayList(Collections.singletonList(this.mHeaderTextView)));
            result.add(new ArrayList(Collections.singletonList(this.mDetailsTextView)));
            if (this.mCancelButton.getVisibility() == 0) {
                result.add(new ArrayList(Collections.singletonList(this.mCancelButton)));
            }
            CellState[][] cellStates = this.mLockPatternView.getCellStates();
            for (int i = 0; i < cellStates.length; i++) {
                ArrayList<Object> row = new ArrayList();
                for (Object add : cellStates[i]) {
                    row.add(add);
                }
                result.add(row);
            }
            if (this.mFingerprintIcon.getVisibility() == 0) {
                result.add(new ArrayList(Collections.singletonList(this.mFingerprintIcon)));
            }
            Object[][] resultArr = (Object[][]) Array.newInstance(Object.class, new int[]{result.size(), cellStates[0].length});
            for (int i2 = 0; i2 < result.size(); i2++) {
                ArrayList<Object> row2 = (ArrayList) result.get(i2);
                for (int j = 0; j < row2.size(); j++) {
                    resultArr[i2][j] = row2.get(j);
                }
            }
            return resultArr;
        }

        public void startEnterAnimation() {
            super.startEnterAnimation();
            this.mLockPatternView.setAlpha(1.0f);
            this.mAppearAnimationUtils.startAnimation2d(getActiveViews(), null, this);
        }

        private void updateStage(Stage stage) {
            switch (stage) {
                case NeedToUnlock:
                    if (this.mHeaderText != null) {
                        this.mHeaderTextView.setText(this.mHeaderText);
                    } else {
                        this.mHeaderTextView.setText(getDefaultHeader());
                    }
                    if (this.mDetailsText != null) {
                        this.mDetailsTextView.setText(this.mDetailsText);
                    } else {
                        this.mDetailsTextView.setText(getDefaultDetails());
                    }
                    this.mErrorTextView.setText("");
                    updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    this.mLockPatternView.clearPattern();
                    this.isLockedOutStage = false;
                    break;
                case NeedToUnlockWrong:
                    showError((int) R.string.lockpattern_need_to_unlock_wrong, 3000);
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    this.isLockedOutStage = false;
                    break;
                case LockedOut:
                    this.mLockPatternView.clearPattern();
                    this.mLockPatternView.setEnabled(false);
                    this.isLockedOutStage = true;
                    break;
            }
            this.mHeaderTextView.announceForAccessibility(this.mHeaderTextView.getText());
        }

        private int getDefaultHeader() {
            if (this.mFrp) {
                return R.string.lockpassword_confirm_your_pattern_header_frp;
            }
            return R.string.lockpassword_confirm_your_pattern_header;
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 3000);
        }

        /* Access modifiers changed, original: protected */
        public void authenticationSucceeded() {
            this.mCredentialCheckResultTracker.setResult(true, new Intent(), 0, this.mEffectiveUserId);
        }

        private void startDisappearAnimation(Intent intent) {
            if (!this.mDisappearing) {
                this.mDisappearing = true;
                ConfirmLockPattern activity = (ConfirmLockPattern) getActivity();
                if (activity != null && !activity.isFinishing()) {
                    if (activity.getConfirmCredentialTheme() == ConfirmCredentialTheme.DARK) {
                        this.mLockPatternView.clearPattern();
                        this.mDisappearAnimationUtils.startAnimation2d(getActiveViews(), new -$$Lambda$ConfirmLockPattern$ConfirmLockPatternFragment$5mgp_p2Jjy9apKG7HsLV4Zu-sXo(activity, intent), this);
                    } else {
                        intent.putExtra("power_on_psw", this.mPattenString);
                        activity.setResult(-1, intent);
                        activity.finish();
                    }
                }
            }
        }

        static /* synthetic */ void lambda$startDisappearAnimation$0(ConfirmLockPattern activity, Intent intent) {
            activity.setResult(-1, intent);
            activity.finish();
            activity.overridePendingTransition(R.anim.confirm_credential_close_enter, R.anim.confirm_credential_close_exit);
        }

        public void onFingerprintIconVisibilityChanged(boolean visible) {
            if (this.mLeftSpacerLandscape != null && this.mRightSpacerLandscape != null) {
                int i = 0;
                this.mLeftSpacerLandscape.setVisibility(visible ? 8 : 0);
                View view = this.mRightSpacerLandscape;
                if (visible) {
                    i = 8;
                }
                view.setVisibility(i);
            }
        }

        private void onPatternChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            this.mLockPatternView.setEnabled(true);
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
                updateStage(Stage.NeedToUnlockWrong);
                postClearPatternRunnable();
            }
            if (newResult) {
                reportFailedAttempt();
            }
        }

        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs, int effectiveUserId, boolean newResult) {
            onPatternChecked(matched, intent, timeoutMs, effectiveUserId, newResult);
        }

        /* Access modifiers changed, original: protected */
        public int getLastTryErrorMessage(int userType) {
            switch (userType) {
                case 1:
                    return R.string.lock_last_pattern_attempt_before_wipe_device;
                case 2:
                    return R.string.lock_last_pattern_attempt_before_wipe_profile;
                case 3:
                    return R.string.lock_last_pattern_attempt_before_wipe_user;
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unrecognized user type:");
                    stringBuilder.append(userType);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }

        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            updateStage(Stage.LockedOut);
            this.mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - SystemClock.elapsedRealtime(), 1000) {
                public void onTick(long millisUntilFinished) {
                    int secondsCountdown = (int) (millisUntilFinished / 1000);
                    ConfirmLockPatternFragment.this.mErrorTextView.setText(ConfirmLockPatternFragment.this.getString(R.string.lockpattern_too_many_failed_confirmation_attempts, new Object[]{Integer.valueOf(secondsCountdown)}));
                }

                public void onFinish() {
                    ConfirmLockPatternFragment.this.updateStage(Stage.NeedToUnlock);
                }
            }.start();
        }

        public void createAnimation(Object obj, long delay, long duration, float translationY, boolean appearing, Interpolator interpolator, Runnable finishListener) {
            CellState cellState = obj;
            if (cellState instanceof CellState) {
                this.mLockPatternView.startCellStateAnimation(cellState, 1.0f, appearing ? 1.0f : 0.0f, appearing ? translationY : 0.0f, appearing ? 0.0f : translationY, appearing ? 0.0f : 1.0f, 1.0f, delay, duration, interpolator, finishListener);
                return;
            }
            this.mAppearAnimationUtils.createAnimation((View) cellState, delay, duration, translationY, appearing, interpolator, finishListener);
        }
    }

    public static class InternalActivity extends ConfirmLockPattern {
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, ConfirmLockPatternFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (ConfirmLockPatternFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
