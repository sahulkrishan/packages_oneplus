package com.android.settings.fingerprint;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.widget.Button;
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
        super.onCreate(savedInstanceState);
        if (OPUtils.isO2()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(8192);
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
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
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
