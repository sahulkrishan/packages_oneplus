package com.android.settings.password;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.PasswordMetrics;
import android.content.Context;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.settings.EncryptionInterstitial;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.notification.RedactionInterstitial;
import com.android.settings.widget.ImeAwareEditText;
import com.android.setupwizardlib.GlifLayout;
import com.google.common.base.Ascii;
import java.util.ArrayList;
import java.util.List;

public class ChooseLockPassword extends SettingsActivity {
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";
    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MIN_LETTERS_KEY = "lockscreen.password_min_letters";
    public static final String PASSWORD_MIN_LOWERCASE_KEY = "lockscreen.password_min_lowercase";
    public static final String PASSWORD_MIN_NONLETTER_KEY = "lockscreen.password_min_nonletter";
    public static final String PASSWORD_MIN_NUMERIC_KEY = "lockscreen.password_min_numeric";
    public static final String PASSWORD_MIN_SYMBOLS_KEY = "lockscreen.password_min_symbols";
    public static final String PASSWORD_MIN_UPPERCASE_KEY = "lockscreen.password_min_uppercase";
    private static final String TAG = "ChooseLockPassword";

    public static class IntentBuilder {
        private final Intent mIntent;

        public IntentBuilder(Context context) {
            this.mIntent = new Intent(context, ChooseLockPassword.class);
            this.mIntent.putExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, false);
            this.mIntent.putExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, false);
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
        }

        public IntentBuilder setPasswordQuality(int quality) {
            this.mIntent.putExtra("lockscreen.password_type", quality);
            return this;
        }

        public IntentBuilder setPasswordLengthRange(int min, int max) {
            this.mIntent.putExtra(ChooseLockPassword.PASSWORD_MIN_KEY, min);
            this.mIntent.putExtra(ChooseLockPassword.PASSWORD_MAX_KEY, max);
            return this;
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

        public IntentBuilder setPassword(String password) {
            this.mIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, password);
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
        private String mChosenPassword;
        private String mCurrentPassword;
        private int mRequestedQuality;

        public /* bridge */ /* synthetic */ void onCreate(Bundle bundle) {
            super.onCreate(bundle);
        }

        public /* bridge */ /* synthetic */ void setBlocking(boolean z) {
            super.setBlocking(z);
        }

        public /* bridge */ /* synthetic */ void setListener(Listener listener) {
            super.setListener(listener);
        }

        public void start(LockPatternUtils utils, boolean required, boolean hasChallenge, long challenge, String chosenPassword, String currentPassword, int requestedQuality, int userId) {
            prepare(utils, required, hasChallenge, challenge, userId);
            this.mChosenPassword = chosenPassword;
            this.mCurrentPassword = currentPassword;
            this.mRequestedQuality = requestedQuality;
            this.mUserId = userId;
            start();
        }

        /* Access modifiers changed, original: protected */
        public Intent saveAndVerifyInBackground() {
            this.mUtils.saveLockPassword(this.mChosenPassword, this.mCurrentPassword, this.mRequestedQuality, this.mUserId);
            if (!this.mHasChallenge) {
                return null;
            }
            byte[] token;
            try {
                token = this.mUtils.verifyPassword(this.mChosenPassword, this.mChallenge, this.mUserId);
            } catch (RequestThrottledException e) {
                token = null;
            }
            if (token == null) {
                Log.e(ChooseLockPassword.TAG, "critical: no token returned for known good password.");
            }
            Intent result = new Intent();
            result.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
            return result;
        }
    }

    public static class ChooseLockPasswordFragment extends InstrumentedFragment implements OnClickListener, OnEditorActionListener, TextWatcher, Listener {
        private static final int CONFIRM_EXISTING_REQUEST = 58;
        static final int CONTAIN_INVALID_CHARACTERS = 1;
        static final int CONTAIN_NON_DIGITS = 8;
        static final int CONTAIN_SEQUENTIAL_DIGITS = 16;
        private static final String FRAGMENT_TAG_SAVE_AND_FINISH = "save_and_finish_worker";
        private static final String KEY_CURRENT_PASSWORD = "current_password";
        private static final String KEY_FIRST_PIN = "first_pin";
        private static final String KEY_UI_STAGE = "ui_stage";
        private static final int MIN_LETTER_IN_PASSWORD = 0;
        private static final int MIN_LOWER_LETTERS_IN_PASSWORD = 2;
        private static final int MIN_NON_LETTER_IN_PASSWORD = 5;
        private static final int MIN_NUMBER_IN_PASSWORD = 4;
        private static final int MIN_SYMBOLS_IN_PASSWORD = 3;
        private static final int MIN_UPPER_LETTERS_IN_PASSWORD = 1;
        static final int NOT_ENOUGH_DIGITS = 512;
        static final int NOT_ENOUGH_LETTER = 64;
        static final int NOT_ENOUGH_LOWER_CASE = 256;
        static final int NOT_ENOUGH_NON_LETTER = 2048;
        static final int NOT_ENOUGH_SYMBOLS = 1024;
        static final int NOT_ENOUGH_UPPER_CASE = 128;
        static final int NO_ERROR = 0;
        static final int RECENTLY_USED = 32;
        static final int RESULT_FINISHED = 1;
        static final int TOO_LONG = 4;
        static final int TOO_SHORT = 2;
        protected CheckBox mAutoCheckPinLengthCheckBox;
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private String mChosenPassword;
        private Button mClearButton;
        private String mCurrentPassword;
        private String mFirstPin;
        protected boolean mForFingerprint;
        private boolean mHasChallenge;
        private boolean mHideDrawer = false;
        protected boolean mIsAlphaMode;
        private GlifLayout mLayout;
        private LockPatternUtils mLockPatternUtils;
        private TextView mMessage;
        private Button mNextButton;
        private ImeAwareEditText mPasswordEntry;
        private TextViewInputDisabler mPasswordEntryInputDisabler;
        private byte[] mPasswordHistoryHashFactor;
        private int mPasswordMaxLength = 16;
        private int mPasswordMinLength = 4;
        private int mPasswordMinLengthToFulfillAllPolicies = 0;
        private int mPasswordMinLetters = 0;
        private int mPasswordMinLowerCase = 0;
        private int mPasswordMinNonLetter = 0;
        private int mPasswordMinNumeric = 0;
        private int mPasswordMinSymbols = 0;
        private int mPasswordMinUpperCase = 0;
        private PasswordRequirementAdapter mPasswordRequirementAdapter;
        private int[] mPasswordRequirements;
        private RecyclerView mPasswordRestrictionView;
        private int mRequestedQuality = 131072;
        private SaveAndFinishWorker mSaveAndFinishWorker;
        protected Button mSkipButton;
        private TextChangedHandler mTextChangedHandler;
        protected Stage mUiStage = Stage.Introduction;
        protected int mUserId;

        protected enum Stage {
            Introduction(R.string.lockpassword_choose_your_screen_lock_header, R.string.lock_settings_picker_fingerprint_message, R.string.lockpassword_choose_your_screen_lock_header, R.string.lock_settings_picker_fingerprint_message, R.string.lockpassword_choose_your_password_message, R.string.lock_settings_picker_fingerprint_added_security_message, R.string.lockpassword_choose_your_pin_message, R.string.lock_settings_picker_fingerprint_added_security_message, R.string.next_label),
            NeedToConfirm(R.string.lockpassword_confirm_your_password_header, R.string.lockpassword_confirm_your_password_header, R.string.lockpassword_confirm_your_pin_header, R.string.lockpassword_confirm_your_pin_header, 0, 0, 0, 0, R.string.lockpassword_confirm_label),
            ConfirmWrong(R.string.lockpassword_confirm_passwords_dont_match, R.string.lockpassword_confirm_passwords_dont_match, R.string.lockpassword_confirm_pins_dont_match, R.string.lockpassword_confirm_pins_dont_match, 0, 0, 0, 0, R.string.lockpassword_confirm_label);
            
            public final int alphaHint;
            public final int alphaHintForFingerprint;
            public final int alphaMessage;
            public final int alphaMessageForFingerprint;
            public final int buttonText;
            public final int numericHint;
            public final int numericHintForFingerprint;
            public final int numericMessage;
            public final int numericMessageForFingerprint;

            private Stage(int hintInAlpha, int hintInAlphaForFingerprint, int hintInNumeric, int hintInNumericForFingerprint, int messageInAlpha, int messageInAlphaForFingerprint, int messageInNumeric, int messageInNumericForFingerprint, int nextButtonText) {
                this.alphaHint = hintInAlpha;
                this.alphaHintForFingerprint = hintInAlphaForFingerprint;
                this.numericHint = hintInNumeric;
                this.numericHintForFingerprint = hintInNumericForFingerprint;
                this.alphaMessage = messageInAlpha;
                this.alphaMessageForFingerprint = messageInAlphaForFingerprint;
                this.numericMessage = messageInNumeric;
                this.numericMessageForFingerprint = messageInNumericForFingerprint;
                this.buttonText = nextButtonText;
            }

            @StringRes
            public int getHint(boolean isAlpha, boolean isFingerprint) {
                if (isAlpha) {
                    return isFingerprint ? this.alphaHintForFingerprint : this.alphaHint;
                }
                return isFingerprint ? this.numericHintForFingerprint : this.numericHint;
            }

            @StringRes
            public int getMessage(boolean isAlpha, boolean isFingerprint) {
                if (isAlpha) {
                    return isFingerprint ? this.alphaMessageForFingerprint : this.alphaMessage;
                }
                return isFingerprint ? this.numericMessageForFingerprint : this.numericMessage;
            }
        }

        class TextChangedHandler extends Handler {
            private static final int DELAY_IN_MILLISECOND = 100;
            private static final int ON_TEXT_CHANGED = 1;

            TextChangedHandler() {
            }

            private void notifyAfterTextChanged() {
                removeMessages(1);
                sendEmptyMessageDelayed(1, 100);
            }

            public void handleMessage(Message msg) {
                if (ChooseLockPasswordFragment.this.getActivity() != null && msg.what == 1) {
                    ChooseLockPasswordFragment.this.updateUi();
                }
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            Intent intent = getActivity().getIntent();
            if (getActivity() instanceof ChooseLockPassword) {
                this.mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
                this.mForFingerprint = intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false);
                processPasswordRequirements(intent);
                this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
                this.mHideDrawer = getActivity().getIntent().getBooleanExtra(SettingsActivity.EXTRA_HIDE_DRAWER, false);
                if (intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT, false)) {
                    SaveAndFinishWorker w = new SaveAndFinishWorker();
                    boolean required = getActivity().getIntent().getBooleanExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true);
                    String current = intent.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                    w.setBlocking(true);
                    w.setListener(this);
                    w.start(this.mChooseLockSettingsHelper.utils(), required, false, 0, current, current, this.mRequestedQuality, this.mUserId);
                }
                this.mTextChangedHandler = new TextChangedHandler();
                return;
            }
            throw new SecurityException("Fragment contained in wrong activity");
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.choose_lock_password, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            int i;
            super.onViewCreated(view, savedInstanceState);
            this.mLayout = (GlifLayout) view;
            this.mLayout.getHeaderTextView().setTextAppearance(getActivity(), R.style.OnePlusSuwGlifHeaderTitle);
            ((ViewGroup) view.findViewById(R.id.password_container)).setOpticalInsets(Insets.NONE);
            this.mSkipButton = (Button) view.findViewById(R.id.skip_button);
            this.mSkipButton.setOnClickListener(this);
            this.mNextButton = (Button) view.findViewById(R.id.next_button);
            this.mNextButton.setOnClickListener(this);
            this.mClearButton = (Button) view.findViewById(R.id.clear_button);
            this.mClearButton.setOnClickListener(this);
            this.mAutoCheckPinLengthCheckBox = (CheckBox) view.findViewById(R.id.auto_check_pin_length);
            this.mAutoCheckPinLengthCheckBox.setOnClickListener(this);
            if (this.mAutoCheckPinLengthCheckBox != null) {
                this.mAutoCheckPinLengthCheckBox.setChecked(this.mLockPatternUtils.getPINPasswordLength(this.mUserId) != 0);
            }
            this.mMessage = (TextView) view.findViewById(R.id.message);
            if (this.mForFingerprint) {
                this.mLayout.setIcon(getActivity().getDrawable(R.drawable.op_ic_lock));
            }
            boolean z = 262144 == this.mRequestedQuality || 327680 == this.mRequestedQuality || 393216 == this.mRequestedQuality;
            this.mIsAlphaMode = z;
            if (this.mIsAlphaMode) {
                this.mAutoCheckPinLengthCheckBox.setVisibility(8);
            }
            setupPasswordRequirementsView(view);
            this.mPasswordRestrictionView.setLayoutManager(new LinearLayoutManager(getActivity()));
            this.mPasswordEntry = (ImeAwareEditText) view.findViewById(R.id.password_entry);
            this.mPasswordEntry.setOnEditorActionListener(this);
            this.mPasswordEntry.addTextChangedListener(this);
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntryInputDisabler = new TextViewInputDisabler(this.mPasswordEntry);
            Activity activity = getActivity();
            int currentType = this.mPasswordEntry.getInputType();
            ImeAwareEditText imeAwareEditText = this.mPasswordEntry;
            if (this.mIsAlphaMode) {
                i = currentType;
            } else {
                i = 18;
            }
            imeAwareEditText.setInputType(i);
            this.mPasswordEntry.setTypeface(Typeface.create(getContext().getString(17039704), 0));
            Intent intent = getActivity().getIntent();
            boolean confirmCredentials = intent.getBooleanExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, true);
            this.mCurrentPassword = intent.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            this.mHasChallenge = intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
            this.mChallenge = intent.getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
            if (savedInstanceState == null) {
                updateStage(Stage.Introduction);
                if (confirmCredentials) {
                    this.mChooseLockSettingsHelper.launchConfirmationActivity(58, getString(R.string.unlock_set_unlock_launch_picker_title), true, this.mUserId);
                }
            } else {
                this.mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
                String state = savedInstanceState.getString(KEY_UI_STAGE);
                if (state != null) {
                    this.mUiStage = Stage.valueOf(state);
                    updateStage(this.mUiStage);
                }
                if (this.mCurrentPassword == null) {
                    this.mCurrentPassword = savedInstanceState.getString(KEY_CURRENT_PASSWORD);
                }
                this.mSaveAndFinishWorker = (SaveAndFinishWorker) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_SAVE_AND_FINISH);
            }
            if (activity instanceof SettingsActivity) {
                SettingsActivity sa = (SettingsActivity) activity;
                int title = Stage.Introduction.getHint(this.mIsAlphaMode, this.mForFingerprint);
                sa.setTitle(R.string.oneplus_choose_screen_lock_method);
                this.mLayout.setHeaderText(title);
            }
        }

        private void setupPasswordRequirementsView(View view) {
            List<Integer> passwordRequirements = new ArrayList();
            List<String> requirementDescriptions = new ArrayList();
            if (this.mPasswordMinUpperCase > 0) {
                passwordRequirements.add(Integer.valueOf(1));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_uppercase, this.mPasswordMinUpperCase, new Object[]{Integer.valueOf(this.mPasswordMinUpperCase)}));
            }
            if (this.mPasswordMinLowerCase > 0) {
                passwordRequirements.add(Integer.valueOf(2));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_lowercase, this.mPasswordMinLowerCase, new Object[]{Integer.valueOf(this.mPasswordMinLowerCase)}));
            }
            if (this.mPasswordMinLetters > 0 && this.mPasswordMinLetters > this.mPasswordMinUpperCase + this.mPasswordMinLowerCase) {
                passwordRequirements.add(Integer.valueOf(0));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_letters, this.mPasswordMinLetters, new Object[]{Integer.valueOf(this.mPasswordMinLetters)}));
            }
            if (this.mPasswordMinNumeric > 0) {
                passwordRequirements.add(Integer.valueOf(4));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_numeric, this.mPasswordMinNumeric, new Object[]{Integer.valueOf(this.mPasswordMinNumeric)}));
            }
            if (this.mPasswordMinSymbols > 0) {
                passwordRequirements.add(Integer.valueOf(3));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_symbols, this.mPasswordMinSymbols, new Object[]{Integer.valueOf(this.mPasswordMinSymbols)}));
            }
            if (this.mPasswordMinNonLetter > 0 && this.mPasswordMinNonLetter > this.mPasswordMinNumeric + this.mPasswordMinSymbols) {
                passwordRequirements.add(Integer.valueOf(5));
                requirementDescriptions.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_nonletter, this.mPasswordMinNonLetter, new Object[]{Integer.valueOf(this.mPasswordMinNonLetter)}));
            }
            this.mPasswordRequirements = passwordRequirements.stream().mapToInt(-$$Lambda$ChooseLockPassword$ChooseLockPasswordFragment$WFCgmpRIhPOiOzVHNaBhMh5zoJI.INSTANCE).toArray();
            this.mPasswordRestrictionView = (RecyclerView) view.findViewById(R.id.password_requirements_view);
            this.mPasswordRestrictionView.setLayoutManager(new LinearLayoutManager(getActivity()));
            this.mPasswordRequirementAdapter = new PasswordRequirementAdapter();
            this.mPasswordRestrictionView.setAdapter(this.mPasswordRequirementAdapter);
        }

        public int getMetricsCategory() {
            return 28;
        }

        public void onResume() {
            super.onResume();
            updateStage(this.mUiStage);
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(this);
                return;
            }
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntry.scheduleShowSoftInput();
        }

        public void onPause() {
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(null);
            }
            super.onPause();
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_UI_STAGE, this.mUiStage.name());
            outState.putString(KEY_FIRST_PIN, this.mFirstPin);
            outState.putString(KEY_CURRENT_PASSWORD, this.mCurrentPassword);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 58) {
                if (resultCode != -1) {
                    getActivity().setResult(1);
                    getActivity().finish();
                    return;
                }
                this.mCurrentPassword = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            }
        }

        /* Access modifiers changed, original: protected */
        public Intent getRedactionInterstitialIntent(Context context) {
            return RedactionInterstitial.createStartIntent(context, this.mUserId);
        }

        /* Access modifiers changed, original: protected */
        public void updateStage(Stage stage) {
            Stage previousStage = this.mUiStage;
            this.mUiStage = stage;
            updateUi();
            if (previousStage != stage) {
                this.mLayout.announceForAccessibility(this.mLayout.getHeaderText());
            }
        }

        private void processPasswordRequirements(Intent intent) {
            int dpmPasswordQuality = this.mLockPatternUtils.getRequestedPasswordQuality(this.mUserId);
            this.mRequestedQuality = Math.max(intent.getIntExtra("lockscreen.password_type", this.mRequestedQuality), dpmPasswordQuality);
            this.mPasswordMinLength = Math.max(Math.max(4, intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_KEY, this.mPasswordMinLength)), this.mLockPatternUtils.getRequestedMinimumPasswordLength(this.mUserId));
            this.mPasswordMaxLength = intent.getIntExtra(ChooseLockPassword.PASSWORD_MAX_KEY, this.mPasswordMaxLength);
            this.mPasswordMinLetters = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_LETTERS_KEY, this.mPasswordMinLetters), this.mLockPatternUtils.getRequestedPasswordMinimumLetters(this.mUserId));
            this.mPasswordMinUpperCase = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_UPPERCASE_KEY, this.mPasswordMinUpperCase), this.mLockPatternUtils.getRequestedPasswordMinimumUpperCase(this.mUserId));
            this.mPasswordMinLowerCase = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_LOWERCASE_KEY, this.mPasswordMinLowerCase), this.mLockPatternUtils.getRequestedPasswordMinimumLowerCase(this.mUserId));
            this.mPasswordMinNumeric = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_NUMERIC_KEY, this.mPasswordMinNumeric), this.mLockPatternUtils.getRequestedPasswordMinimumNumeric(this.mUserId));
            this.mPasswordMinSymbols = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_SYMBOLS_KEY, this.mPasswordMinSymbols), this.mLockPatternUtils.getRequestedPasswordMinimumSymbols(this.mUserId));
            this.mPasswordMinNonLetter = Math.max(intent.getIntExtra(ChooseLockPassword.PASSWORD_MIN_NONLETTER_KEY, this.mPasswordMinNonLetter), this.mLockPatternUtils.getRequestedPasswordMinimumNonLetter(this.mUserId));
            if (dpmPasswordQuality != 262144) {
                if (dpmPasswordQuality == 327680) {
                    if (this.mPasswordMinLetters == 0) {
                        this.mPasswordMinLetters = 1;
                    }
                    if (this.mPasswordMinNumeric == 0) {
                        this.mPasswordMinNumeric = 1;
                    }
                } else if (dpmPasswordQuality != 393216) {
                    this.mPasswordMinNumeric = 0;
                    this.mPasswordMinLetters = 0;
                    this.mPasswordMinUpperCase = 0;
                    this.mPasswordMinLowerCase = 0;
                    this.mPasswordMinSymbols = 0;
                    this.mPasswordMinNonLetter = 0;
                }
            } else if (this.mPasswordMinLetters == 0) {
                this.mPasswordMinLetters = 1;
            }
            this.mPasswordMinLengthToFulfillAllPolicies = getMinLengthToFulfillAllPolicies();
        }

        private int validatePassword(String password) {
            int errorCode = 0;
            PasswordMetrics metrics = PasswordMetrics.computeForPassword(password);
            if (password.length() < this.mPasswordMinLength) {
                if (this.mPasswordMinLength > this.mPasswordMinLengthToFulfillAllPolicies) {
                    errorCode = 0 | 2;
                }
            } else if (password.length() > this.mPasswordMaxLength) {
                errorCode = 0 | 4;
            } else {
                if (this.mLockPatternUtils.getRequestedPasswordQuality(this.mUserId) == 196608 && metrics.numeric == password.length() && PasswordMetrics.maxLengthSequence(password) > 3) {
                    errorCode = 0 | 16;
                }
                if (this.mLockPatternUtils.checkPasswordHistory(password, getPasswordHistoryHashFactor(), this.mUserId)) {
                    errorCode |= 32;
                }
            }
            int i = 0;
            for (int i2 = 0; i2 < password.length(); i2++) {
                char c = password.charAt(i2);
                if (c < ' ' || c > Ascii.MAX) {
                    errorCode |= 1;
                    break;
                }
            }
            if ((this.mRequestedQuality == 131072 || this.mRequestedQuality == 196608) && (metrics.letters > 0 || metrics.symbols > 0)) {
                errorCode |= 8;
            }
            while (i < this.mPasswordRequirements.length) {
                switch (this.mPasswordRequirements[i]) {
                    case 0:
                        if (metrics.letters >= this.mPasswordMinLetters) {
                            break;
                        }
                        errorCode |= 64;
                        break;
                    case 1:
                        if (metrics.upperCase >= this.mPasswordMinUpperCase) {
                            break;
                        }
                        errorCode |= 128;
                        break;
                    case 2:
                        if (metrics.lowerCase >= this.mPasswordMinLowerCase) {
                            break;
                        }
                        errorCode |= 256;
                        break;
                    case 3:
                        if (metrics.symbols >= this.mPasswordMinSymbols) {
                            break;
                        }
                        errorCode |= 1024;
                        break;
                    case 4:
                        if (metrics.numeric >= this.mPasswordMinNumeric) {
                            break;
                        }
                        errorCode |= 512;
                        break;
                    case 5:
                        if (metrics.nonLetter >= this.mPasswordMinNonLetter) {
                            break;
                        }
                        errorCode |= 2048;
                        break;
                    default:
                        break;
                }
                i++;
            }
            return errorCode;
        }

        private byte[] getPasswordHistoryHashFactor() {
            if (this.mPasswordHistoryHashFactor == null) {
                this.mPasswordHistoryHashFactor = this.mLockPatternUtils.getPasswordHistoryHashFactor(this.mCurrentPassword, this.mUserId);
            }
            return this.mPasswordHistoryHashFactor;
        }

        public void handleNext() {
            if (this.mSaveAndFinishWorker == null) {
                this.mChosenPassword = this.mPasswordEntry.getText().toString();
                if (!TextUtils.isEmpty(this.mChosenPassword)) {
                    if (this.mUiStage == Stage.Introduction) {
                        if (validatePassword(this.mChosenPassword) == 0) {
                            this.mFirstPin = this.mChosenPassword;
                            this.mPasswordEntry.setText("");
                            updateStage(Stage.NeedToConfirm);
                        }
                    } else if (this.mUiStage == Stage.NeedToConfirm) {
                        if (this.mFirstPin.equals(this.mChosenPassword)) {
                            startSaveAndFinish();
                            long j = 0;
                            if (this.mIsAlphaMode) {
                                this.mLockPatternUtils.savePINPasswordLength(0, this.mUserId);
                            } else {
                                LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
                                if (this.mAutoCheckPinLengthCheckBox.isChecked()) {
                                    j = (long) this.mChosenPassword.length();
                                }
                                lockPatternUtils.savePINPasswordLength(j, this.mUserId);
                            }
                        } else {
                            CharSequence tmp = this.mPasswordEntry.getText();
                            if (tmp != null) {
                                Selection.setSelection((Spannable) tmp, 0, tmp.length());
                            }
                            updateStage(Stage.ConfirmWrong);
                        }
                    }
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void setNextEnabled(boolean enabled) {
            this.mNextButton.setEnabled(enabled);
        }

        /* Access modifiers changed, original: protected */
        public void setNextText(int text) {
            this.mNextButton.setText(text);
        }

        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.clear_button) {
                this.mPasswordEntry.setText("");
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

        /* Access modifiers changed, original: 0000 */
        public String[] convertErrorCodeToMessages(int errorCode) {
            int i;
            List<String> messages = new ArrayList();
            if ((errorCode & 1) > 0) {
                messages.add(getString(R.string.lockpassword_illegal_character));
            }
            if ((errorCode & 8) > 0) {
                messages.add(getString(R.string.lockpassword_pin_contains_non_digits));
            }
            if ((errorCode & 128) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_uppercase, this.mPasswordMinUpperCase, new Object[]{Integer.valueOf(this.mPasswordMinUpperCase)}));
            }
            if ((errorCode & 256) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_lowercase, this.mPasswordMinLowerCase, new Object[]{Integer.valueOf(this.mPasswordMinLowerCase)}));
            }
            if ((errorCode & 64) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_letters, this.mPasswordMinLetters, new Object[]{Integer.valueOf(this.mPasswordMinLetters)}));
            }
            if ((errorCode & 512) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_numeric, this.mPasswordMinNumeric, new Object[]{Integer.valueOf(this.mPasswordMinNumeric)}));
            }
            if ((errorCode & 1024) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_symbols, this.mPasswordMinSymbols, new Object[]{Integer.valueOf(this.mPasswordMinSymbols)}));
            }
            if ((errorCode & 2048) > 0) {
                messages.add(getResources().getQuantityString(R.plurals.lockpassword_password_requires_nonletter, this.mPasswordMinNonLetter, new Object[]{Integer.valueOf(this.mPasswordMinNonLetter)}));
            }
            if ((errorCode & 2) > 0) {
                if (this.mIsAlphaMode) {
                    i = R.string.lockpassword_password_too_short;
                } else {
                    i = R.string.lockpassword_pin_too_short;
                }
                messages.add(getString(i, new Object[]{Integer.valueOf(this.mPasswordMinLength)}));
            }
            if ((errorCode & 4) > 0) {
                if (this.mIsAlphaMode) {
                    i = R.string.lockpassword_password_too_long;
                } else {
                    i = R.string.lockpassword_pin_too_long;
                }
                messages.add(getString(i, new Object[]{Integer.valueOf(this.mPasswordMaxLength + 1)}));
            }
            if ((errorCode & 16) > 0) {
                messages.add(getString(R.string.lockpassword_pin_no_sequential_digits));
            }
            if ((errorCode & 32) > 0) {
                if (this.mIsAlphaMode) {
                    i = R.string.lockpassword_password_recently_used;
                } else {
                    i = R.string.lockpassword_pin_recently_used;
                }
                messages.add(getString(i));
            }
            return (String[]) messages.toArray(new String[0]);
        }

        private int getMinLengthToFulfillAllPolicies() {
            return Math.max(this.mPasswordMinLetters, this.mPasswordMinUpperCase + this.mPasswordMinLowerCase) + Math.max(this.mPasswordMinNonLetter, this.mPasswordMinSymbols + this.mPasswordMinNumeric);
        }

        /* Access modifiers changed, original: protected */
        public void updateUi() {
            int errorCode;
            boolean z = true;
            boolean canInput = this.mSaveAndFinishWorker == null;
            String password = this.mPasswordEntry.getText().toString();
            int length = password.length();
            if (this.mUiStage == Stage.Introduction) {
                this.mPasswordRestrictionView.setVisibility(0);
                errorCode = validatePassword(password);
                this.mPasswordRequirementAdapter.setRequirements(convertErrorCodeToMessages(errorCode));
                setNextEnabled(errorCode == 0);
            } else {
                this.mPasswordRestrictionView.setVisibility(8);
                setHeaderText(getString(this.mUiStage.getHint(this.mIsAlphaMode, this.mForFingerprint)));
                boolean z2 = canInput && length >= this.mPasswordMinLength;
                setNextEnabled(z2);
                Button button = this.mClearButton;
                boolean z3 = canInput && length > 0;
                button.setEnabled(z3);
            }
            errorCode = this.mUiStage.getMessage(this.mIsAlphaMode, this.mForFingerprint);
            if (errorCode != 0) {
                this.mMessage.setVisibility(0);
                this.mMessage.setText(errorCode);
            } else {
                this.mMessage.setVisibility(4);
            }
            Button button2 = this.mClearButton;
            if (this.mUiStage == Stage.Introduction) {
                z = false;
            }
            button2.setVisibility(toVisibility(z));
            setNextText(this.mUiStage.buttonText);
            this.mPasswordEntryInputDisabler.setInputEnabled(canInput);
        }

        private int toVisibility(boolean visibleOrGone) {
            return visibleOrGone ? 0 : 8;
        }

        private void setHeaderText(String text) {
            if (TextUtils.isEmpty(this.mLayout.getHeaderText()) || !this.mLayout.getHeaderText().toString().equals(text)) {
                this.mLayout.setHeaderText((CharSequence) text);
            }
        }

        public void afterTextChanged(Editable s) {
            if (this.mUiStage == Stage.ConfirmWrong) {
                this.mUiStage = Stage.NeedToConfirm;
            }
            this.mTextChangedHandler.notifyAfterTextChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        private void startSaveAndFinish() {
            if (this.mSaveAndFinishWorker != null) {
                Log.w(ChooseLockPassword.TAG, "startSaveAndFinish with an existing SaveAndFinishWorker.");
                return;
            }
            this.mPasswordEntryInputDisabler.setInputEnabled(false);
            setNextEnabled(false);
            this.mSaveAndFinishWorker = new SaveAndFinishWorker();
            this.mSaveAndFinishWorker.setListener(this);
            getFragmentManager().beginTransaction().add(this.mSaveAndFinishWorker, FRAGMENT_TAG_SAVE_AND_FINISH).commit();
            getFragmentManager().executePendingTransactions();
            this.mSaveAndFinishWorker.start(this.mLockPatternUtils, getActivity().getIntent().getBooleanExtra(EncryptionInterstitial.EXTRA_REQUIRE_PASSWORD, true), this.mHasChallenge, this.mChallenge, this.mChosenPassword, this.mCurrentPassword, this.mRequestedQuality, this.mUserId);
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
        if (ChooseLockPasswordFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends Fragment> getFragmentClass() {
        return ChooseLockPasswordFragment.class;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        CharSequence msg;
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false)) {
            msg = R.string.oneplus_choose_screen_lock_method;
        } else {
            msg = R.string.lockpassword_choose_your_screen_lock_header;
        }
        setTitle(getText(msg));
        ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(true);
    }
}
