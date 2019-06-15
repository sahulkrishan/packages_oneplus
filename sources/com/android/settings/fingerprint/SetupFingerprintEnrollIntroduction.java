package com.android.settings.fingerprint;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.SetupChooseLockGeneric;
import com.android.settings.password.SetupChooseLockGeneric.SetupChooseLockGenericFragment;
import com.android.settings.password.StorageManagerWrapper;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.settings.utils.OPUtils;

public class SetupFingerprintEnrollIntroduction extends FingerprintEnrollIntroduction implements OnClickListener {
    private static final String KEY_LOCK_SCREEN_PRESENT = "wasLockScreenPresent";
    private boolean mAlreadyHadLockScreenSetup = false;
    private LottieAnimationView mHowToUseTipsAnimView;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFromSetup = true;
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            setContentView(R.layout.op_setup_fingerprint_enroll_introduction);
            setHeaderText(R.string.oneplus_fingerprint_findsendor_tilte);
            this.mHowToUseTipsAnimView = (LottieAnimationView) findViewById(R.id.op_how_to_use_fingerprint_tips_view);
            if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                this.mHowToUseTipsAnimView.setAnimation("op_custom_fingerprint_guide.json");
            } else if (OPUtils.isBlackModeOn(getContentResolver())) {
                this.mHowToUseTipsAnimView.setAnimation("op_custom_fingerprint_guide_dark.json");
            } else {
                this.mHowToUseTipsAnimView.setAnimation("op_custom_fingerprint_guide.json");
            }
            this.mHowToUseTipsAnimView.loop(true);
            this.mHowToUseTipsAnimView.playAnimation();
        }
        if (savedInstanceState == null) {
            this.mAlreadyHadLockScreenSetup = isKeyguardSecure();
        } else {
            this.mAlreadyHadLockScreenSetup = savedInstanceState.getBoolean(KEY_LOCK_SCREEN_PRESENT, false);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.fingerprint_cancel_button) {
            onCancelButtonClick();
        } else {
            super.onClick(v);
        }
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
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOCK_SCREEN_PRESENT, this.mAlreadyHadLockScreenSetup);
    }

    /* Access modifiers changed, original: protected */
    public Intent getChooseLockIntent() {
        Intent intent = new Intent(this, SetupChooseLockGeneric.class);
        if (StorageManagerWrapper.isFileEncryptedNativeOrEmulated()) {
            intent.putExtra("lockscreen.password_type", 131072);
            intent.putExtra(ChooseLockGenericFragment.EXTRA_SHOW_OPTIONS_BUTTON, true);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public Intent getFindSensorIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFindSensor.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        TextView description = (TextView) findViewById(R.id.description_text);
        description.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_light));
        if (OPUtils.isSupportCustomFingerprint()) {
            description.setText(R.string.oneplus_how_to_use_fingerprint_summary_no_note);
        } else {
            description.setText(R.string.security_settings_fingerprint_enroll_introduction_message);
        }
        getNextButton().setText(R.string.security_settings_fingerprint_enroll_introduction_continue_setup);
        Button cancelButton = (Button) findViewById(R.id.fingerprint_cancel_button);
        cancelButton.setOnClickListener(this);
        cancelButton.setText(R.string.security_settings_fingerprint_enroll_introduction_cancel_setup);
        TextView functionalTermsButton = (TextView) findViewById(R.id.functional_terms);
        if (OPUtils.isO2()) {
            functionalTermsButton.setTextColor(getResources().getColor(R.color.op_setupwizard_oxygen_accent_color));
        } else {
            functionalTermsButton.setTextColor(getResources().getColor(R.color.op_setupwizard_hydrogen_accent_color));
        }
        functionalTermsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.oem.intent.action.OP_LEGAL");
                intent.putExtra("op_legal_notices_type", 8);
                intent.putExtra("key_from_settings", true);
                SetupFingerprintEnrollIntroduction.this.startActivity(intent);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && isKeyguardSecure() && !this.mAlreadyHadLockScreenSetup) {
            data = getMetricIntent(data);
            skipToNextPage();
        } else if (requestCode == 11 && resultCode == 1) {
            if (OPUtils.isSupportXCamera()) {
                skipToNextPage();
            } else {
                setResult(1);
                finish();
            }
        } else if (requestCode == 1 && resultCode == 11) {
            if (OPUtils.isSupportXCamera()) {
                skipToNextPage();
            } else {
                setResult(1);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Intent getMetricIntent(Intent data) {
        if (data == null) {
            data = new Intent();
        }
        data.putExtra(SetupChooseLockGenericFragment.EXTRA_PASSWORD_QUALITY, new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
        return data;
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        goSkipOrNext(true);
    }

    private void goSkipOrNext(boolean next) {
        this.mForFingerprint = next;
        if (!this.mNextButtonClicked) {
            this.mNextButtonClicked = true;
            if (!this.mHasPassword) {
                launchChooseLock();
            } else if (OPUtils.isSupportCustomFingerprint()) {
                launchConfirmLock();
            } else {
                launchFindSensor(this.mToken);
            }
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    SetupFingerprintEnrollIntroduction.this.mNextButtonClicked = false;
                }
            }, 200);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCancelButtonClick() {
        if (this.mHasPassword) {
            if (!OPUtils.isO2()) {
                try {
                    ComponentName componentName;
                    Intent intent = new Intent();
                    if (OPUtils.isGuestMode()) {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.UserSettingSuccess");
                    } else {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.GesturesActivity");
                    }
                    intent.setComponent(componentName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
            } else if (isKeyguardSecure()) {
                Intent intent2 = null;
                if (!this.mAlreadyHadLockScreenSetup) {
                    intent2 = getMetricIntent(null);
                }
                setResult(2, intent2);
                finish();
            } else {
                setResult(11);
                finish();
            }
            return;
        }
        goSkipOrNext(false);
    }

    public void onBackPressed() {
        if (!this.mAlreadyHadLockScreenSetup && isKeyguardSecure()) {
            setResult(0, getMetricIntent(null));
        }
        super.onBackPressed();
    }

    private boolean isKeyguardSecure() {
        return ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardSecure();
    }

    public int getMetricsCategory() {
        return 249;
    }
}
