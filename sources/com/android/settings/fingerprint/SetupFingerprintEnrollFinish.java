package com.android.settings.fingerprint;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.utils.OPUtils;

public class SetupFingerprintEnrollFinish extends FingerprintEnrollFinish {
    private static final int FACE_RESULT_FAIL = 1;
    private static final int FACE_RESULT_NOT_FOUND = 2;
    private static final int FACE_RESULT_OK = 0;
    protected static final int FACE_UNLOCK_SETUP_REQUEST = 4;
    private static final String TAG = "SetupFingerprintEnrollFinish";

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.isSetupPage = true;
        super.onCreate(savedInstanceState);
        if (OPUtils.isSupportCustomFingerprint()) {
            setContentView(R.layout.op_fod_setup_fingerprint_enroll_finish);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            TextView message = (TextView) findViewById(R.id.message);
            message.setText(R.string.security_settings_fingerprint_enroll_finish_title);
            message.setTextColor(getColor(R.color.oneplus_contorl_text_color_primary_dark));
            TextView messageSecond = (TextView) findViewById(R.id.message_secondary);
            messageSecond.setText(R.string.oneplus_reenroll_fingerprint_tips);
            messageSecond.setTextColor(getColor(R.color.oneplus_contorl_text_color_primary_dark));
        }
        Button addButton = (Button) findViewById(R.id.add_another_button);
        if (((FingerprintManager) getSystemService("fingerprint")).getEnrolledFingerprints(this.mUserId).size() >= getResources().getInteger(17694790)) {
            addButton.setVisibility(4);
        } else {
            addButton.setOnClickListener(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public Intent getEnrollingIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollEnrolling.class);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        if (OPUtils.isSupportCustomFingerprint()) {
            resid = R.style.f944Theme.Oneplus.SetupWizardTheme.Black;
        } else {
            resid = SetupWizardUtils.getTheme(getIntent());
        }
        super.onApplyThemeResource(theme, resid, first);
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        setResult(1);
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 4) {
            if (OPUtils.isO2()) {
                if (resultCode == 2 || resultCode == 1 || resultCode == 11) {
                    setResult(1);
                    finish();
                }
            } else if (resultCode == 2 || resultCode == 1 || resultCode == 11) {
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
                    setResult(1);
                    finish();
                }
            } else if (resultCode == 11) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        ((Button) findViewById(R.id.next_button)).setText(R.string.next_label);
    }

    public int getMetricsCategory() {
        return 248;
    }
}
