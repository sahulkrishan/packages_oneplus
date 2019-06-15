package com.oneplus.settings.faceunlock;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.android.settings.fingerprint.SetupFingerprintEnrollIntroduction;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.utils.OPUtils;

public class OPFaceUnlockModeSettingsActivity extends BaseActivity implements OnClickListener {
    public static final String ENTER_FACEUNLOCK_MODE_SETTINGS_FROM_SUW = "enter_faceunlock_mode_settings_from_suw";
    private static final String ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE = "oneplus_face_unlock_powerkey_recognize_enable";
    private static final int SWIPE_UP_MODE = 0;
    private static final int USE_POWER_BUTTON_MODE = 1;
    private Button mDoneButton;
    private boolean mFromSetupWizard = false;
    private LottieAnimationView mLottieAnim;
    private View mPressPowerkey;
    private RadioButton mPressPowerkeyButton;
    private RelativeLayout mRootView;
    private View mSwipeUp;
    private RadioButton mSwipeUpButton;
    protected byte[] mToken;
    protected int mUserId;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFromSetupWizard = getIntent().getBooleanExtra(ENTER_FACEUNLOCK_MODE_SETTINGS_FROM_SUW, false);
        this.mToken = getIntent().getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        if (this.mFromSetupWizard) {
            setTheme(R.style.SetupOnePlusPasswordTheme);
        }
        setContentView(R.layout.op_faceunlock_mode_set_activity);
        getWindow().getDecorView().setSystemUiVisibility(0);
        this.mRootView = (RelativeLayout) findViewById(R.id.op_faceunlock_mode_content);
        if (this.mFromSetupWizard) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            LayoutParams mTextViewLP = new LayoutParams(-2, -2);
            mTextViewLP.setMargins(0, getResources().getDimensionPixelSize(R.dimen.oneplus_fod_setup_fingerprint_enroll_top_margin), 0, 0);
            this.mRootView.setLayoutParams(mTextViewLP);
        }
        this.mSwipeUp = findViewById(R.id.key_faceunlock_swipe_up_mode);
        this.mSwipeUp.setOnClickListener(this);
        this.mPressPowerkey = findViewById(R.id.key_faceunlock_use_power_button_mode);
        this.mPressPowerkey.setOnClickListener(this);
        this.mSwipeUpButton = (RadioButton) findViewById(R.id.key_faceunlock_swipe_up_mode_radiobutton);
        this.mPressPowerkeyButton = (RadioButton) findViewById(R.id.key_faceunlock_use_power_button_mode_radiobutton);
        this.mLottieAnim = (LottieAnimationView) findViewById(R.id.op_single_lottie_view);
        this.mDoneButton = (Button) findViewById(R.id.done_button);
        this.mDoneButton.setOnClickListener(this);
        if (this.mFromSetupWizard) {
            this.mDoneButton.setText(R.string.suw_next_button_label);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        setViewType(getUnlockMode());
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        stopAnim();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        releaseAnim();
    }

    private void setModeSelected() {
        if (getUnlockMode() == 0) {
            this.mSwipeUpButton.setChecked(true);
            this.mPressPowerkeyButton.setChecked(false);
            return;
        }
        this.mSwipeUpButton.setChecked(false);
        this.mPressPowerkeyButton.setChecked(true);
    }

    private void setAnimationResource() {
        if (getUnlockMode() == 0) {
            this.mLottieAnim.setAnimation("op_face_unlock_by_swipe_up_dark.json");
        } else {
            this.mLottieAnim.setAnimation("op_face_unlock_by_use_power_key_dark.json");
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id != R.id.done_button) {
            if (id == R.id.key_faceunlock_swipe_up_mode) {
                setViewType(0);
            } else if (id == R.id.key_faceunlock_use_power_button_mode) {
                setViewType(1);
            }
        } else if (this.mFromSetupWizard) {
            Intent intent = new Intent(this, SetupFingerprintEnrollIntroduction.class);
            if (this.mToken != null) {
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
            }
            if (this.mUserId != -10000) {
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
            }
            startActivityForResult(intent, 6);
        } else {
            finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6) {
            if (resultCode != 0) {
                setResult(resultCode);
            }
            finish();
        }
    }

    public int getUnlockMode() {
        return System.getInt(getContentResolver(), "oneplus_face_unlock_powerkey_recognize_enable", 0);
    }

    public void setViewType(int type) {
        System.putInt(getContentResolver(), "oneplus_face_unlock_powerkey_recognize_enable", type);
        OPUtils.sendAppTracker("pop_up_face_unlock", type);
        setModeSelected();
        stopAnim();
        setAnimationResource();
        this.mLottieAnim.playAnimation();
    }

    public void stopAnim() {
        if (this.mLottieAnim != null) {
            this.mLottieAnim.cancelAnimation();
        }
    }

    public void releaseAnim() {
        if (this.mLottieAnim != null) {
            this.mLottieAnim.cancelAnimation();
            this.mLottieAnim = null;
        }
    }
}
