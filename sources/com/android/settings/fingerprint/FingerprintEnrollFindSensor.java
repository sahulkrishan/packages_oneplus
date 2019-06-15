package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.utils.OPUtils;

public class FingerprintEnrollFindSensor extends FingerprintEnrollBase {
    private static final int CONFIRM_REQUEST = 1;
    private static final int ENROLLING = 2;
    public static final String EXTRA_KEY_LAUNCHED_CONFIRM = "launched_confirm_lock";
    protected static final int KEY_FINGERPRINT_FUNCTIONAL_TERMS_TYPE = 8;
    protected static final String KEY_FROM_SETTINGS = "key_from_settings";
    protected static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    protected static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    private static final String TAG = "Settings_FindSensor";
    protected TextView functionalTermsButton;
    private FingerprintFindSensorAnimation mAnimation;
    private boolean mCanProceedToEnrolling = true;
    protected LottieAnimationView mEnrollTipsAnimView;
    private boolean mLaunchedConfirmLock;
    private boolean mNeedJumpToFingerprintSettings = true;
    private boolean mNextClicked;
    private FingerprintEnrollSidecar mSidecar;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        if (OPUtils.isSupportCustomFingerprint()) {
            setHeaderText(R.string.oneplus_enroll_fingerprint_tips_title);
            this.mEnrollTipsAnimView = (LottieAnimationView) findViewById(R.id.op_fingerprint_enroll_tips_view);
            if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mEnrollTipsAnimView.setAnimation("op_fingerprint_enroll_tips_dark.json");
            } else {
                this.mEnrollTipsAnimView.setAnimation("op_fingerprint_enroll_tips_light.json");
            }
            this.mEnrollTipsAnimView.loop(true);
            this.mEnrollTipsAnimView.playAnimation();
            this.mNeedJumpToFingerprintSettings = getIntent().getBooleanExtra("needJumpToFingerprintSettings", true);
        } else {
            setHeaderText(R.string.security_settings_fingerprint_enroll_find_sensor_title);
        }
        if (savedInstanceState != null) {
            this.mLaunchedConfirmLock = savedInstanceState.getBoolean("launched_confirm_lock");
            this.mToken = savedInstanceState.getByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        }
        if (this.mToken == null && !this.mLaunchedConfirmLock) {
            launchConfirmLock();
        } else if (this.mToken != null) {
            startLookingForFingerprint();
        }
        this.mAnimation = (FingerprintFindSensorAnimation) findViewById(R.id.fingerprint_sensor_location_animation);
        this.functionalTermsButton = (TextView) findViewById(R.id.functional_terms);
        if (this.functionalTermsButton != null) {
            this.functionalTermsButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(FingerprintEnrollFindSensor.OPLEGAL_NOTICES_ACTION);
                    intent.putExtra(FingerprintEnrollFindSensor.KEY_NOTICES_TYPE, 8);
                    intent.putExtra(FingerprintEnrollFindSensor.KEY_FROM_SETTINGS, true);
                    FingerprintEnrollFindSensor.this.startActivity(intent);
                }
            });
        }
    }

    /* Access modifiers changed, original: protected */
    public int getContentView() {
        if (OPUtils.isSupportCustomFingerprint()) {
            return R.layout.oneplus_custom_fingerprint_enroll_find_sensor_base;
        }
        if (OPUtils.isSurportBackFingerprint(this)) {
            return R.layout.oneplus_back_fingerprint_enroll_find_sensor_base;
        }
        return R.layout.fingerprint_enroll_find_sensor;
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        if (this.mAnimation != null) {
            this.mAnimation.startAnimation();
        }
        if (this.mEnrollTipsAnimView != null) {
            this.mEnrollTipsAnimView.playAnimation();
        }
    }

    private void startLookingForFingerprint() {
        if (!OPUtils.isSupportCustomFingerprint()) {
            this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag("sidecar");
            if (this.mSidecar == null) {
                this.mSidecar = new FingerprintEnrollSidecar();
                getFragmentManager().beginTransaction().add(this.mSidecar, "sidecar").commit();
            }
            this.mSidecar.setListener(new Listener() {
                public void onEnrollmentProgressChange(int steps, int remaining) {
                    FingerprintEnrollFindSensor.this.mNextClicked = true;
                    String str = FingerprintEnrollFindSensor.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onEnrollmentProgressChange mSidecar = ");
                    stringBuilder.append(FingerprintEnrollFindSensor.this.mSidecar);
                    Log.d(str, stringBuilder.toString());
                    if (FingerprintEnrollFindSensor.this.mSidecar != null && !FingerprintEnrollFindSensor.this.mSidecar.cancelEnrollment()) {
                        FingerprintEnrollFindSensor.this.proceedToEnrolling();
                    }
                }

                public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
                }

                public void onEnrollmentError(int errMsgId, CharSequence errString) {
                    String str = FingerprintEnrollFindSensor.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onEnrollmentError mNextClicked = ");
                    stringBuilder.append(FingerprintEnrollFindSensor.this.mNextClicked);
                    stringBuilder.append(" errMsgId = ");
                    stringBuilder.append(errMsgId);
                    Log.d(str, stringBuilder.toString());
                    if (FingerprintEnrollFindSensor.this.mNextClicked && errMsgId == 5) {
                        FingerprintEnrollFindSensor.this.mNextClicked = false;
                        FingerprintEnrollFindSensor.this.proceedToEnrolling();
                    }
                }
            });
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        if (this.mAnimation != null) {
            this.mAnimation.pauseAnimation();
        }
        if (this.mEnrollTipsAnimView != null) {
            this.mEnrollTipsAnimView.pauseAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mAnimation != null) {
            this.mAnimation.stopAnimation();
        }
        if (this.mEnrollTipsAnimView != null) {
            this.mEnrollTipsAnimView.cancelAnimation();
            this.mEnrollTipsAnimView = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("launched_confirm_lock", this.mLaunchedConfirmLock);
        outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        if (this.mToken == null) {
            finish();
            return;
        }
        this.mNextClicked = true;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onNextButtonClick: mSidecar = ");
        stringBuilder.append(this.mSidecar);
        Log.d(str, stringBuilder.toString());
        if (this.mSidecar == null || !(this.mSidecar == null || this.mSidecar.cancelEnrollment())) {
            proceedToEnrolling();
        }
    }

    private void proceedToEnrolling() {
        if (this.mCanProceedToEnrolling) {
            this.mCanProceedToEnrolling = false;
            if (this.mSidecar != null) {
                getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
                this.mSidecar = null;
            }
            Intent intent = getEnrollingIntent();
            intent.putExtra("needJumpToFingerprintSettings", this.mNeedJumpToFingerprintSettings);
            startActivityForResult(intent, 2);
            overridePendingTransition(R.anim.op_activity_fingeprint_open_enter, R.anim.op_activity_fingeprint_close_exit);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                if (data != null) {
                    this.mToken = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                }
                overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
                getIntent().putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
                startLookingForFingerprint();
                return;
            }
            finish();
        } else if (requestCode != 2) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == 1) {
            setResult(1);
            finish();
        } else if (resultCode == 2) {
            setResult(2);
            finish();
        } else if (resultCode == 3) {
            setResult(3);
            finish();
        } else if (((FingerprintManager) getSystemService(FingerprintManager.class)).getEnrolledFingerprints().size() >= getResources().getInteger(17694790)) {
            finish();
        } else {
            startLookingForFingerprint();
        }
    }

    private void launchConfirmLock() {
        boolean launchedConfirmationActivity;
        long challenge = ((FingerprintManager) getSystemService(FingerprintManager.class)).preEnroll();
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
        if (this.mUserId == -10000) {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, getString(R.string.security_settings_fingerprint_preference_title), null, null, challenge);
        } else {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, (CharSequence) getString(R.string.security_settings_fingerprint_preference_title), null, null, challenge, this.mUserId);
        }
        if (launchedConfirmationActivity) {
            this.mLaunchedConfirmLock = true;
        } else {
            finish();
        }
    }

    public int getMetricsCategory() {
        return 241;
    }

    /* Access modifiers changed, original: protected */
    public int applyActionBarTitle() {
        if (OPUtils.isSupportCustomFingerprint()) {
            return R.string.oneplus_enroll_fingerprint_tips_title;
        }
        return R.string.security_settings_fingerprint_enroll_find_sensor_title;
    }

    public void onResume() {
        super.onResume();
        this.mCanProceedToEnrolling = true;
    }
}
