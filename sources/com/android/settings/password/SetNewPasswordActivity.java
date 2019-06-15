package com.android.settings.password;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.Utils;

public class SetNewPasswordActivity extends Activity implements Ui {
    private static final String TAG = "SetNewPasswordActivity";
    private String mNewPasswordAction;
    private SetNewPasswordController mSetNewPasswordController;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        this.mNewPasswordAction = getIntent().getAction();
        if ("android.app.action.SET_NEW_PASSWORD".equals(this.mNewPasswordAction) || "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(this.mNewPasswordAction)) {
            this.mSetNewPasswordController = SetNewPasswordController.create(this, this, getIntent(), getActivityToken());
            this.mSetNewPasswordController.dispatchSetNewPasswordIntent();
            return;
        }
        Log.e(TAG, "Unexpected action to launch this activity");
        finish();
    }

    public void launchChooseLock(Bundle chooseLockFingerprintExtras) {
        Intent intent;
        if (Utils.isDeviceProvisioned(this) ^ 1) {
            intent = new Intent(this, SetupChooseLockGeneric.class);
        } else {
            intent = new Intent(this, ChooseLockGeneric.class);
        }
        intent.setAction(this.mNewPasswordAction);
        intent.putExtras(chooseLockFingerprintExtras);
        startActivity(intent);
        finish();
    }
}
