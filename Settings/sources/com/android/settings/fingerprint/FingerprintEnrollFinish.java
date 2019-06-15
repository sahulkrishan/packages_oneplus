package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class FingerprintEnrollFinish extends FingerprintEnrollBase {
    private boolean mLaunchingEnroll;
    private boolean mNeedJumpToFingerprintSettings = false;
    protected byte[] mToken;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_enroll_finish);
        setHeaderText(R.string.security_settings_fingerprint_enroll_finish_title);
        Button addButton = (Button) findViewById(R.id.add_another_button);
        this.mToken = getIntent().getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        this.mNeedJumpToFingerprintSettings = getIntent().getBooleanExtra("needJumpToFingerprintSettings", false);
        if (((FingerprintManager) getSystemService("fingerprint")).getEnrolledFingerprints(this.mUserId).size() >= getResources().getInteger(17694790)) {
            addButton.setVisibility(4);
        } else {
            addButton.setOnClickListener(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        if (this.mNeedJumpToFingerprintSettings) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", FingerprintSettings.class.getName());
            intent.putExtra("needJumpToFingerprintSettings", this.mNeedJumpToFingerprintSettings);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
            intent.putExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
            startActivity(intent);
        }
        setResult(1);
        finish();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.add_another_button) {
            this.mLaunchingEnroll = true;
            Intent intent = getEnrollingIntent();
            intent.addFlags(33554432);
            startActivity(intent);
            finish();
        }
        super.onClick(v);
    }

    public void onResume() {
        this.mLaunchingEnroll = false;
        setFingerprintEnrolling(true);
        super.onResume();
    }

    public void onPause() {
        setFingerprintEnrolling(false);
        super.onPause();
    }

    private void setFingerprintEnrolling(boolean enrolling) {
        if (!this.mLaunchingEnroll && !OPUtils.isSurportBackFingerprint(this)) {
            boolean z = false;
            if (System.getInt(getApplicationContext().getContentResolver(), OPConstants.OEM_ACC_FINGERPRINT_ENROLLING, 0) != 0) {
                z = true;
            }
            if (enrolling != z) {
                System.putInt(getApplicationContext().getContentResolver(), OPConstants.OEM_ACC_FINGERPRINT_ENROLLING, enrolling);
            }
        }
    }

    public int getMetricsCategory() {
        return 242;
    }

    /* Access modifiers changed, original: protected */
    public int applyActionBarTitle() {
        return R.string.oneplus_opfinger_input_completed_title;
    }
}
