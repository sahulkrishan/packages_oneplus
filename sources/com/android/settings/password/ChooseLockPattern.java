package com.android.settings.password;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.android.settings.EncryptionInterstitial;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.notification.RedactionInterstitial;
import com.android.setupwizardlib.GlifLayout;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChooseLockPattern extends SettingsActivity {
    static final int RESULT_FINISHED = 1;
    private static final String TAG = "ChooseLockPattern";

    public static class IntentBuilder {
        private final Intent mIntent;

        public IntentBuilder(Context context) {
            this.mIntent = new Intent(context, ChooseLockPattern.class);
            this.mIntent.putExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, false);
            this.mIntent.putExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, false);
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
        }

        public IntentBuilder setUserId(int userId) {
            this.mIntent.putExtra("android.intent.extra.USER_ID", userId);
            return this;
        }

        public IntentBuilder setChallenge(long challenge) {
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
            return this;
        }

        public IntentBuilder setPattern(String pattern) {
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, pattern);
            return this;
        }

        public IntentBuilder setForFingerprint(boolean forFingerprint) {
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, forFingerprint);
            return this;
        }

        public Intent build() {
            return this.mIntent;
        }
    }

    public static class SaveAndFinishWorker extends SaveChosenLockWorkerBase {
        private List<Cell> mChosenPattern;
        private String mCurrentPattern;
        private boolean mLockVirgin;

        public /* bridge */ /* synthetic */ void onCreate(Bundle bundle) {
            super.onCreate(bundle);
        }

        public /* bridge */ /* synthetic */ void setBlocking(boolean z) {
            super.setBlocking(z);
        }

        public /* bridge */ /* synthetic */ void setListener(Listener listener) {
            super.setListener(listener);
        }

        public void start(LockPatternUtils utils, boolean credentialRequired, boolean hasChallenge, long challenge, List<Cell> chosenPattern, String currentPattern, int userId) {
            prepare(utils, credentialRequired, hasChallenge, challenge, userId);
            this.mCurrentPattern = currentPattern;
            this.mChosenPattern = chosenPattern;
            this.mUserId = userId;
            this.mLockVirgin = this.mUtils.isPatternEverChosen(this.mUserId) ^ 1;
            start();
        }

        /* Access modifiers changed, original: protected */
        public Intent saveAndVerifyInBackground() {
            int userId = this.mUserId;
            this.mUtils.saveLockPattern(this.mChosenPattern, this.mCurrentPattern, userId);
            if (!this.mHasChallenge) {
                return null;
            }
            byte[] token;
            try {
                token = this.mUtils.verifyPattern(this.mChosenPattern, this.mChallenge, userId);
            } catch (RequestThrottledException e) {
                token = null;
            }
            if (token == null) {
                Log.e(ChooseLockPattern.TAG, "critical: no token returned for known good pattern");
            }
            Intent result = new Intent();
            result.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
            return result;
        }

        /* Access modifiers changed, original: protected */
        public void finish(Intent resultData) {
            if (this.mLockVirgin) {
                this.mUtils.setVisiblePatternEnabled(true, this.mUserId);
            }
            super.finish(resultData);
        }
    }

    public static class ChooseLockPatternFragment extends InstrumentedFragment implements OnClickListener, Listener {
        public static final int CONFIRM_EXISTING_REQUEST = 55;
        private static final String FRAGMENT_TAG_SAVE_AND_FINISH = "save_and_finish_worker";
        private static final int ID_EMPTY_MESSAGE = -1;
        static final int INFORMATION_MSG_TIMEOUT_MS = 3000;
        private static final String KEY_CURRENT_PATTERN = "currentPattern";
        private static final String KEY_PATTERN_CHOICE = "chosenPattern";
        private static final String KEY_UI_STAGE = "uiStage";
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;
        private final List<Cell> mAnimatePattern = Collections.unmodifiableList(Lists.newArrayList(new Cell[]{Cell.of(0, 0), Cell.of(0, 1), Cell.of(1, 1), Cell.of(2, 1)}));
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        protected OnPatternListener mChooseNewLockPatternListener = new OnPatternListener() {
            public void onPatternStart() {
                ChooseLockPatternFragment.this.mLockPatternView.removeCallbacks(ChooseLockPatternFragment.this.mClearPatternRunnable);
                patternInProgress();
            }

            public void onPatternCleared() {
                ChooseLockPatternFragment.this.mLockPatternView.removeCallbacks(ChooseLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternDetected(List<Cell> pattern) {
                if (ChooseLockPatternFragment.this.mUiStage == Stage.NeedToConfirm || ChooseLockPatternFragment.this.mUiStage == Stage.ConfirmWrong) {
                    if (ChooseLockPatternFragment.this.mChosenPattern == null) {
                        throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
                    } else if (ChooseLockPatternFragment.this.mChosenPattern.equals(pattern)) {
                        ChooseLockPatternFragment.this.updateStage(Stage.ChoiceConfirmed);
                    } else {
                        ChooseLockPatternFragment.this.updateStage(Stage.ConfirmWrong);
                    }
                } else if (ChooseLockPatternFragment.this.mUiStage != Stage.Introduction && ChooseLockPatternFragment.this.mUiStage != Stage.ChoiceTooShort) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unexpected stage ");
                    stringBuilder.append(ChooseLockPatternFragment.this.mUiStage);
                    stringBuilder.append(" when entering the pattern.");
                    throw new IllegalStateException(stringBuilder.toString());
                } else if (pattern.size() < 4) {
                    ChooseLockPatternFragment.this.updateStage(Stage.ChoiceTooShort);
                } else {
                    ChooseLockPatternFragment.this.mChosenPattern = new ArrayList(pattern);
                    ChooseLockPatternFragment.this.updateStage(Stage.FirstChoiceValid);
                }
            }

            public void onPatternCellAdded(List<Cell> list) {
            }

            private void patternInProgress() {
                ChooseLockPatternFragment.this.mHeaderText.setText(R.string.lockpattern_recording_inprogress);
                if (ChooseLockPatternFragment.this.mDefaultHeaderColorList != null) {
                    ChooseLockPatternFragment.this.mHeaderText.setTextColor(ChooseLockPatternFragment.this.mDefaultHeaderColorList);
                }
                ChooseLockPatternFragment.this.mFooterText.setText("");
                ChooseLockPatternFragment.this.mFooterLeftButton.setEnabled(false);
                ChooseLockPatternFragment.this.mFooterRightButton.setEnabled(false);
                if (ChooseLockPatternFragment.this.mTitleHeaderScrollView != null) {
                    ChooseLockPatternFragment.this.mTitleHeaderScrollView.post(new Runnable() {
                        public void run() {
                            ChooseLockPatternFragment.this.mTitleHeaderScrollView.fullScroll(130);
                        }
                    });
                }
            }
        };
        protected List<Cell> mChosenPattern = null;
        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                ChooseLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private String mCurrentPattern;
        private ColorStateList mDefaultHeaderColorList;
        private TextView mFooterLeftButton;
        private TextView mFooterRightButton;
        protected TextView mFooterText;
        protected boolean mForFingerprint;
        private boolean mHasChallenge;
        protected TextView mHeaderText;
        private boolean mHideDrawer = false;
        private LockPatternUtils mLockPatternUtils;
        protected LockPatternView mLockPatternView;
        protected TextView mMessageText;
        private SaveAndFinishWorker mSaveAndFinishWorker;
        private ScrollView mTitleHeaderScrollView;
        protected TextView mTitleText;
        private Stage mUiStage = Stage.Introduction;
        protected int mUserId;

        enum LeftButtonMode {
            Retry(R.string.lockpattern_retry_button_text, true),
            RetryDisabled(R.string.lockpattern_retry_button_text, false),
            Gone(-1, false);
            
            final boolean enabled;
            final int text;

            private LeftButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }
        }

        enum RightButtonMode {
            Continue(R.string.next_label, true),
            ContinueDisabled(R.string.next_label, false),
            Confirm(R.string.lockpattern_confirm_button_text, true),
            ConfirmDisabled(R.string.lockpattern_confirm_button_text, false),
            Ok(17039370, true);
            
            final boolean enabled;
            final int text;

            private RightButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }
        }

        protected enum Stage {
            Introduction(R.string.lock_settings_picker_fingerprint_added_security_message, R.string.lockpassword_choose_your_pattern_message, R.string.lockpattern_recording_intro_header, LeftButtonMode.Gone, RightButtonMode.ContinueDisabled, -1, true),
            HelpScreen(-1, -1, R.string.lockpattern_settings_help_how_to_record, LeftButtonMode.Gone, RightButtonMode.Ok, -1, false),
            ChoiceTooShort(R.string.lock_settings_picker_fingerprint_added_security_message, R.string.lockpassword_choose_your_pattern_message, R.string.lockpattern_recording_incorrect_too_short, LeftButtonMode.Retry, RightButtonMode.ContinueDisabled, -1, true),
            FirstChoiceValid(R.string.lock_settings_picker_fingerprint_added_security_message, R.string.lockpassword_choose_your_pattern_message, R.string.lockpattern_pattern_entered_header, LeftButtonMode.Retry, RightButtonMode.Continue, -1, false),
            NeedToConfirm(-1, -1, R.string.lockpattern_need_to_confirm, LeftButtonMode.Gone, RightButtonMode.ConfirmDisabled, -1, true),
            ConfirmWrong(-1, -1, R.string.lockpattern_need_to_unlock_wrong, LeftButtonMode.Gone, RightButtonMode.ConfirmDisabled, -1, true),
            ChoiceConfirmed(-1, -1, R.string.lockpattern_pattern_confirmed_header, LeftButtonMode.Gone, RightButtonMode.Confirm, -1, false);
            
            final int footerMessage;
            final int headerMessage;
            final LeftButtonMode leftMode;
            final int message;
            final int messageForFingerprint;
            final boolean patternEnabled;
            final RightButtonMode rightMode;

            private Stage(int messageForFingerprint, int message, int headerMessage, LeftButtonMode leftMode, RightButtonMode rightMode, int footerMessage, boolean patternEnabled) {
                this.headerMessage = headerMessage;
                this.messageForFingerprint = messageForFingerprint;
                this.message = message;
                this.leftMode = leftMode;
                this.rightMode = rightMode;
                this.footerMessage = footerMessage;
                this.patternEnabled = patternEnabled;
            }
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 55) {
                if (resultCode != -1) {
                    getActivity().setResult(1);
                    getActivity().finish();
                } else {
                    this.mCurrentPattern = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                }
                updateStage(Stage.Introduction);
            }
        }

        /* Access modifiers changed, original: protected */
        public void setRightButtonEnabled(boolean enabled) {
            this.mFooterRightButton.setEnabled(enabled);
        }

        /* Access modifiers changed, original: protected */
        public void setRightButtonText(int text) {
            this.mFooterRightButton.setText(text);
        }

        public int getMetricsCategory() {
            return 29;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
            if (getActivity() instanceof ChooseLockPattern) {
                Intent intent = getActivity().getIntent();
                this.mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
                this.mLockPatternUtils = new LockPatternUtils(getActivity());
                if (intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT, false)) {
                    SaveAndFinishWorker w = new SaveAndFinishWorker();
                    boolean required = getActivity().getIntent().getBooleanExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true);
                    String current = intent.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                    w.setBlocking(true);
                    w.setListener(this);
                    w.start(this.mChooseLockSettingsHelper.utils(), required, false, 0, LockPatternUtils.stringToPattern(current), current, this.mUserId);
                }
                this.mHideDrawer = getActivity().getIntent().getBooleanExtra(SettingsActivity.EXTRA_HIDE_DRAWER, false);
                this.mForFingerprint = intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false);
                return;
            }
            throw new SecurityException("Fragment contained in wrong activity");
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            GlifLayout layout = (GlifLayout) inflater.inflate(R.layout.choose_lock_pattern, container, false);
            layout.setHeaderText(getActivity().getTitle());
            if (getResources().getBoolean(R.bool.config_lock_pattern_minimal_ui)) {
                View iconView = layout.findViewById(R.id.suw_layout_icon);
                if (iconView != null) {
                    iconView.setVisibility(8);
                }
            } else if (this.mForFingerprint) {
                layout.setIcon(getActivity().getDrawable(R.drawable.op_ic_lock));
                layout.setHeaderText((int) R.string.lock_settings_picker_fingerprint_message);
            }
            return layout;
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((GlifLayout) view).getHeaderTextView().setTextAppearance(getActivity(), R.style.OnePlusSuwGlifHeaderTitle);
            this.mTitleText = (TextView) view.findViewById(R.id.suw_layout_title);
            this.mHeaderText = (TextView) view.findViewById(R.id.headerText);
            this.mDefaultHeaderColorList = this.mHeaderText.getTextColors();
            this.mMessageText = (TextView) view.findViewById(R.id.message);
            this.mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
            this.mLockPatternView.setOnPatternListener(this.mChooseNewLockPatternListener);
            this.mLockPatternView.setTactileFeedbackEnabled(this.mChooseLockSettingsHelper.utils().isTactileFeedbackEnabled());
            this.mLockPatternView.setFadePattern(false);
            this.mFooterText = (TextView) view.findViewById(R.id.footerText);
            this.mFooterLeftButton = (TextView) view.findViewById(R.id.footerLeftButton);
            this.mFooterRightButton = (TextView) view.findViewById(R.id.footerRightButton);
            this.mTitleHeaderScrollView = (ScrollView) view.findViewById(R.id.scroll_layout_title_header);
            this.mFooterLeftButton.setOnClickListener(this);
            this.mFooterRightButton.setOnClickListener(this);
            ((LinearLayoutWithDefaultTouchRecepient) view.findViewById(R.id.topLayout)).setDefaultTouchRecepient(this.mLockPatternView);
            boolean confirmCredentials = getActivity().getIntent().getBooleanExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, true);
            Intent intent = getActivity().getIntent();
            this.mCurrentPattern = intent.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            this.mHasChallenge = intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
            this.mChallenge = intent.getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
            if (savedInstanceState != null) {
                String patternString = savedInstanceState.getString(KEY_PATTERN_CHOICE);
                if (patternString != null) {
                    this.mChosenPattern = LockPatternUtils.stringToPattern(patternString);
                }
                if (this.mCurrentPattern == null) {
                    this.mCurrentPattern = savedInstanceState.getString(KEY_CURRENT_PATTERN);
                }
                updateStage(Stage.values()[savedInstanceState.getInt(KEY_UI_STAGE)]);
                this.mSaveAndFinishWorker = (SaveAndFinishWorker) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_SAVE_AND_FINISH);
            } else if (confirmCredentials) {
                updateStage(Stage.NeedToConfirm);
                if (!this.mChooseLockSettingsHelper.launchConfirmationActivity(55, getString(R.string.unlock_set_unlock_launch_picker_title), true, this.mUserId)) {
                    updateStage(Stage.Introduction);
                }
            } else {
                updateStage(Stage.Introduction);
            }
        }

        public void onResume() {
            super.onResume();
            updateStage(this.mUiStage);
            if (this.mSaveAndFinishWorker != null) {
                setRightButtonEnabled(false);
                this.mSaveAndFinishWorker.setListener(this);
            }
        }

        public void onPause() {
            super.onPause();
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(null);
            }
        }

        /* Access modifiers changed, original: protected */
        public Intent getRedactionInterstitialIntent(Context context) {
            return RedactionInterstitial.createStartIntent(context, this.mUserId);
        }

        public void handleLeftButton() {
            if (this.mUiStage.leftMode == LeftButtonMode.Retry) {
                this.mChosenPattern = null;
                this.mLockPatternView.clearPattern();
                updateStage(Stage.Introduction);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("left footer button pressed, but stage of ");
            stringBuilder.append(this.mUiStage);
            stringBuilder.append(" doesn't make sense");
            throw new IllegalStateException(stringBuilder.toString());
        }

        public void handleRightButton() {
            StringBuilder stringBuilder;
            if (this.mUiStage.rightMode == RightButtonMode.Continue) {
                if (this.mUiStage == Stage.FirstChoiceValid) {
                    updateStage(Stage.NeedToConfirm);
                    return;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("expected ui stage ");
                stringBuilder.append(Stage.FirstChoiceValid);
                stringBuilder.append(" when button is ");
                stringBuilder.append(RightButtonMode.Continue);
                throw new IllegalStateException(stringBuilder.toString());
            } else if (this.mUiStage.rightMode == RightButtonMode.Confirm) {
                if (this.mUiStage == Stage.ChoiceConfirmed) {
                    startSaveAndFinish();
                    this.mLockPatternUtils.savePINPasswordLength(0, this.mUserId);
                    return;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("expected ui stage ");
                stringBuilder.append(Stage.ChoiceConfirmed);
                stringBuilder.append(" when button is ");
                stringBuilder.append(RightButtonMode.Confirm);
                throw new IllegalStateException(stringBuilder.toString());
            } else if (this.mUiStage.rightMode != RightButtonMode.Ok) {
            } else {
                if (this.mUiStage == Stage.HelpScreen) {
                    this.mLockPatternView.clearPattern();
                    this.mLockPatternView.setDisplayMode(DisplayMode.Correct);
                    updateStage(Stage.Introduction);
                    return;
                }
                stringBuilder = new StringBuilder();
                stringBuilder.append("Help screen is only mode with ok button, but stage is ");
                stringBuilder.append(this.mUiStage);
                throw new IllegalStateException(stringBuilder.toString());
            }
        }

        public void onClick(View v) {
            if (v == this.mFooterLeftButton) {
                handleLeftButton();
            } else if (v == this.mFooterRightButton) {
                handleRightButton();
            }
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == 4 && event.getRepeatCount() == 0 && this.mUiStage == Stage.HelpScreen) {
                updateStage(Stage.Introduction);
                return true;
            } else if (keyCode != 82 || this.mUiStage != Stage.Introduction) {
                return false;
            } else {
                updateStage(Stage.HelpScreen);
                return true;
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(KEY_UI_STAGE, this.mUiStage.ordinal());
            if (this.mChosenPattern != null) {
                outState.putString(KEY_PATTERN_CHOICE, LockPatternUtils.patternToString(this.mChosenPattern));
            }
            if (this.mCurrentPattern != null) {
                outState.putString(KEY_CURRENT_PATTERN, this.mCurrentPattern);
            }
        }

        /* Access modifiers changed, original: protected */
        public void updateStage(Stage stage) {
            Stage previousStage = this.mUiStage;
            this.mUiStage = stage;
            if (stage == Stage.ChoiceTooShort) {
                this.mHeaderText.setText(getResources().getString(stage.headerMessage, new Object[]{Integer.valueOf(4)}));
            } else {
                this.mHeaderText.setText(stage.headerMessage);
            }
            int message = this.mForFingerprint ? stage.messageForFingerprint : stage.message;
            if (message == -1) {
                this.mMessageText.setText("");
            } else {
                this.mMessageText.setText(message);
            }
            if (stage.footerMessage == -1) {
                this.mFooterText.setText("");
            } else {
                this.mFooterText.setText(stage.footerMessage);
            }
            if (stage == Stage.ConfirmWrong || stage == Stage.ChoiceTooShort) {
                getActivity().getTheme().resolveAttribute(R.attr.colorError, new TypedValue(), true);
            } else {
                if (this.mDefaultHeaderColorList != null) {
                    this.mHeaderText.setTextColor(this.mDefaultHeaderColorList);
                }
                if (stage == Stage.NeedToConfirm && this.mForFingerprint) {
                    this.mHeaderText.setText("");
                    this.mTitleText.setText(R.string.lockpassword_draw_your_pattern_again_header);
                }
            }
            updateFooterLeftButton(stage, this.mFooterLeftButton);
            setRightButtonText(stage.rightMode.text);
            setRightButtonEnabled(stage.rightMode.enabled);
            if (stage.patternEnabled) {
                this.mLockPatternView.enableInput();
            } else {
                this.mLockPatternView.disableInput();
            }
            this.mLockPatternView.setDisplayMode(DisplayMode.Correct);
            boolean announceAlways = false;
            switch (this.mUiStage) {
                case Introduction:
                    this.mLockPatternView.clearPattern();
                    break;
                case HelpScreen:
                    this.mLockPatternView.setPattern(DisplayMode.Animate, this.mAnimatePattern);
                    break;
                case ChoiceTooShort:
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    postClearPatternRunnable();
                    announceAlways = true;
                    break;
                case NeedToConfirm:
                    this.mLockPatternView.clearPattern();
                    break;
                case ConfirmWrong:
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    postClearPatternRunnable();
                    announceAlways = true;
                    break;
            }
            if (previousStage != stage || announceAlways) {
                this.mHeaderText.announceForAccessibility(this.mHeaderText.getText());
            }
        }

        /* Access modifiers changed, original: protected */
        public void updateFooterLeftButton(Stage stage, TextView footerLeftButton) {
            if (stage.leftMode == LeftButtonMode.Gone) {
                footerLeftButton.setVisibility(8);
                return;
            }
            footerLeftButton.setVisibility(0);
            footerLeftButton.setText(stage.leftMode.text);
            footerLeftButton.setEnabled(stage.leftMode.enabled);
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 2000);
        }

        private void startSaveAndFinish() {
            if (this.mSaveAndFinishWorker != null) {
                Log.w(ChooseLockPattern.TAG, "startSaveAndFinish with an existing SaveAndFinishWorker.");
                return;
            }
            setRightButtonEnabled(false);
            this.mSaveAndFinishWorker = new SaveAndFinishWorker();
            this.mSaveAndFinishWorker.setListener(this);
            getFragmentManager().beginTransaction().add(this.mSaveAndFinishWorker, FRAGMENT_TAG_SAVE_AND_FINISH).commit();
            getFragmentManager().executePendingTransactions();
            this.mSaveAndFinishWorker.start(this.mChooseLockSettingsHelper.utils(), getActivity().getIntent().getBooleanExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true), this.mHasChallenge, this.mChallenge, this.mChosenPattern, this.mCurrentPattern, this.mUserId);
        }

        public void onChosenLockSaveFinished(boolean wasSecureBefore, Intent resultData) {
            getActivity().setResult(1, resultData);
            if (!wasSecureBefore) {
                Intent intent = getRedactionInterstitialIntent(getActivity());
                if (intent != null) {
                    intent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, this.mHideDrawer);
                    startActivity(intent);
                }
            }
            getActivity().finish();
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, getFragmentClass().getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (ChooseLockPatternFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends Fragment> getFragmentClass() {
        return ChooseLockPatternFragment.class;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false)) {
            i = R.string.oneplus_choose_screen_lock_method;
        } else {
            i = R.string.lockpassword_choose_your_screen_lock_header;
        }
        setTitle(i);
        ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(true);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
