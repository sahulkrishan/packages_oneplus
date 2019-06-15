package com.android.settings.fingerprint;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.setupwizardlib.span.LinkSpan;
import com.oneplus.settings.utils.OPUtils;

public class FingerprintEnrollIntroduction extends FingerprintEnrollBase implements OnClickListener, LinkSpan.OnClickListener {
    protected static final int CHOOSE_LOCK_GENERIC_REQUEST = 1;
    protected static final int CHOOSE_LOCK_GENERIC_REQUEST_BY_FACEUNLOCK = 7;
    protected static final int FACE_UNLOCK_SETUP_REQUEST = 4;
    protected static final int FINGERPRINT_FIND_SENSOR_REQUEST = 2;
    protected static final int GO_TO_FACE_UNLOCK_PAGE = 5;
    protected static final int GO_TO_FINGERPRINT_INTRODUCTION_PAGE = 6;
    protected static final int KEY_FACEUNLOCK_FUNCTIONAL_TERMS_TYPE = 10;
    protected static final int KEY_FINGERPRINT_FUNCTIONAL_TERMS_TYPE = 8;
    protected static final String KEY_FROM_SETTINGS = "key_from_settings";
    protected static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    protected static final int LEARN_MORE_REQUEST = 3;
    protected static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    private static final String TAG = "FingerprintEnrollIntroduction";
    private TextView mErrorText;
    private boolean mFingerprintUnlockDisabledByAdmin;
    protected boolean mFromSetup = false;
    private Handler mHandler = new Handler();
    protected boolean mHasPassword;
    private LottieAnimationView mHowToUseTipsAnimView;
    private boolean mNextButtonClicked = false;
    private UserManager mUserManager;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFingerprintUnlockDisabledByAdmin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this, 32, this.mUserId) != null;
        if (OPUtils.isSupportCustomFingerprint()) {
            setContentView(R.layout.op_fingerprint_enroll_introduction);
            this.mHowToUseTipsAnimView = (LottieAnimationView) findViewById(R.id.op_how_to_use_fingerprint_tips_view);
            if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mHowToUseTipsAnimView.setAnimation("op_custom_fingerprint_guide_dark.json");
            } else {
                this.mHowToUseTipsAnimView.setAnimation("op_custom_fingerprint_guide.json");
            }
            this.mHowToUseTipsAnimView.loop(true);
            this.mHowToUseTipsAnimView.playAnimation();
        } else if (OPUtils.isSurportBackFingerprint(this)) {
            setContentView(R.layout.fingerprint_enroll_introduction);
        } else {
            setContentView(R.layout.op_fingerprint_enroll_introduction_hardward_key);
        }
        if (this.mFingerprintUnlockDisabledByAdmin) {
            setHeaderText(R.string.security_settings_fingerprint_enroll_introduction_title_unlock_disabled);
        } else if (OPUtils.isSupportCustomFingerprint()) {
            setHeaderText(R.string.oneplus_fingerprint_findsendor_tilte);
        } else {
            setHeaderText(R.string.security_settings_fingerprint_enroll_introduction_title);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.security_settings_fingerprint_enroll_introduction_title);
        }
        ((Button) findViewById(R.id.fingerprint_cancel_button)).setOnClickListener(this);
        ((TextView) findViewById(R.id.functional_terms)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FingerprintEnrollIntroduction.OPLEGAL_NOTICES_ACTION);
                intent.putExtra(FingerprintEnrollIntroduction.KEY_NOTICES_TYPE, 8);
                intent.putExtra(FingerprintEnrollIntroduction.KEY_FROM_SETTINGS, true);
                FingerprintEnrollIntroduction.this.startActivity(intent);
            }
        });
        this.mErrorText = (TextView) findViewById(R.id.error_text);
        this.mUserManager = UserManager.get(this);
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        if (this.mHowToUseTipsAnimView != null) {
            this.mHowToUseTipsAnimView.playAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        if (this.mHowToUseTipsAnimView != null) {
            this.mHowToUseTipsAnimView.pauseAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mHowToUseTipsAnimView != null) {
            this.mHowToUseTipsAnimView.cancelAnimation();
            this.mHowToUseTipsAnimView = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        updatePasswordQuality();
        FingerprintManager fingerprintManager = Utils.getFingerprintManagerOrNull(this);
        int errorMsg = 0;
        if (fingerprintManager != null) {
            if (fingerprintManager.getEnrolledFingerprints(this.mUserId).size() >= getResources().getInteger(17694790)) {
                errorMsg = R.string.fingerprint_intro_error_max;
            }
        } else {
            errorMsg = R.string.fingerprint_intro_error_unknown;
        }
        if (errorMsg == 0) {
            this.mErrorText.setText(null);
            getNextButton().setVisibility(0);
            return;
        }
        this.mErrorText.setText(errorMsg);
        getNextButton().setVisibility(8);
    }

    private void updatePasswordQuality() {
        this.mHasPassword = new ChooseLockSettingsHelper(this).utils().getActivePasswordQuality(this.mUserManager.getCredentialOwnerProfile(this.mUserId)) != 0;
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return (Button) findViewById(R.id.fingerprint_next_button);
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        if (!this.mNextButtonClicked) {
            this.mNextButtonClicked = true;
            if (this.mHasPassword) {
                launchFindSensor(this.mToken);
            } else {
                launchChooseLock();
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    FingerprintEnrollIntroduction.this.mNextButtonClicked = false;
                }
            }, 200);
        }
    }

    /* Access modifiers changed, original: protected */
    public void launchChooseLock() {
        Intent intent = getChooseLockIntent();
        long challenge = Utils.getFingerprintManagerOrNull(this).preEnroll();
        intent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
        intent.putExtra(ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, true);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(intent, 1);
    }

    /* Access modifiers changed, original: protected */
    public void launchFindSensor(byte[] token) {
        Intent intent = getFindSensorIntent();
        if (token != null) {
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
        }
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        startActivityForResult(intent, 2);
    }

    /* Access modifiers changed, original: protected */
    public Intent getChooseLockIntent() {
        return new Intent(this, ChooseLockGeneric.class);
    }

    /* Access modifiers changed, original: protected */
    public Intent getFindSensorIntent() {
        return new Intent(this, FingerprintEnrollFindSensor.class);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isResultFinished = resultCode == 1;
        int i = 2;
        if (requestCode == 2) {
            if (isResultFinished || resultCode == 2) {
                if (isResultFinished) {
                    i = -1;
                }
                setResult(i, data);
                finish();
                return;
            }
        } else if (requestCode == 1) {
            if (isResultFinished) {
                updatePasswordQuality();
                launchFindSensor(data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN));
                return;
            }
        } else if (requestCode == 3) {
            overridePendingTransition(R.anim.suw_slide_back_in, R.anim.suw_slide_back_out);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.fingerprint_cancel_button) {
            onCancelButtonClick();
        } else {
            super.onClick(v);
        }
    }

    public int getMetricsCategory() {
        return 243;
    }

    /* Access modifiers changed, original: protected */
    public void onCancelButtonClick() {
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        TextView description = (TextView) findViewById(R.id.description_text);
        if (this.mFingerprintUnlockDisabledByAdmin) {
            description.setText(R.string.security_settings_fingerprint_enroll_introduction_message_unlock_disabled);
        }
    }

    public void onClick(LinkSpan span) {
        if ("url".equals(span.getId())) {
            Intent intent = HelpUtils.getHelpIntent(this, getString(R.string.help_url_fingerprint), getClass().getName());
            if (intent == null) {
                Log.w(TAG, "Null help intent.");
                return;
            }
            try {
                startActivityForResult(intent, 3);
            } catch (ActivityNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Activity was not found for intent, ");
                stringBuilder.append(e);
                Log.w(str, stringBuilder.toString());
            }
        }
    }
}
