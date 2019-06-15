package com.android.settings.fingerprint;

import android.app.ActionBar;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SetupWizardUtils;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.password.StorageManagerWrapper;
import com.oneplus.faceunlock.internal.IOPFaceSettingService;
import com.oneplus.faceunlock.internal.IOPFaceSettingService.Stub;
import com.oneplus.settings.faceunlock.OPFaceUnlockSettings;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class FaceUnlockEnrollIntroduction extends FingerprintEnrollBase {
    protected static final int CHOOSE_LOCK_GENERIC_REQUEST = 1;
    protected static final int CHOOSE_LOCK_GENERIC_REQUEST_BY_FACEUNLOCK = 7;
    private static final int FACE_RESULT_FAIL = 1;
    private static final int FACE_RESULT_NOT_FOUND = 2;
    private static final int FACE_RESULT_OK = 0;
    protected static final int FACE_UNLOCK_SETUP_REQUEST = 4;
    protected static final int FINGERPRINT_FIND_SENSOR_REQUEST = 2;
    protected static final int GO_TO_FACE_UNLOCK_PAGE = 5;
    protected static final int GO_TO_FINGERPRINT_INTRODUCTION_PAGE = 6;
    protected static final int KEY_FACEUNLOCK_FUNCTIONAL_TERMS_TYPE = 10;
    protected static final int KEY_FINGERPRINT_FUNCTIONAL_TERMS_TYPE = 8;
    protected static final String KEY_FROM_SETTINGS = "key_from_settings";
    private static final String KEY_LOCK_SCREEN_PRESENT = "wasLockScreenPresent";
    protected static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    protected static final int LEARN_MORE_REQUEST = 3;
    protected static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    private static final String TAG = "FaceUnlockIntroduction";
    private boolean mAlreadyHadLockScreenSetup = false;
    private TextView mErrorText;
    private IOPFaceSettingService mFaceSettingService;
    private ServiceConnection mFaceUnlockConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(FaceUnlockEnrollIntroduction.TAG, "Oneplus face unlock service connected");
            FaceUnlockEnrollIntroduction.this.mFaceSettingService = Stub.asInterface(service);
            FaceUnlockEnrollIntroduction.this.getNextButton().setText(R.string.security_settings_fingerprint_enroll_introduction_continue_setup);
            if (FaceUnlockEnrollIntroduction.this.isFaceAdded()) {
                FaceUnlockEnrollIntroduction.this.getNextButton().setVisibility(8);
            } else {
                FaceUnlockEnrollIntroduction.this.getNextButton().setVisibility(0);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(FaceUnlockEnrollIntroduction.TAG, "Oneplus face unlock service disconnected");
            FaceUnlockEnrollIntroduction.this.mFaceSettingService = null;
        }
    };
    private boolean mFingerprintUnlockDisabledByAdmin;
    protected boolean mFromSetup = false;
    protected boolean mHasPassword;
    private boolean mNextButtonClicked = false;

    private void bindFaceUnlockService() {
        try {
            Intent intent = new Intent();
            intent.setClassName(OPConstants.PACKAGENAME_FACE_UNLOCK, "com.oneplus.faceunlock.FaceSettingService");
            bindService(intent, this.mFaceUnlockConnection, 1);
            Log.i(TAG, "Start bind oneplus face unlockservice");
        } catch (Exception e) {
            Log.i(TAG, "Bind oneplus face unlockservice exception");
        }
    }

    private void unbindFaceUnlockService() {
        Log.i(TAG, "Start unbind oneplus face unlockservice");
        unbindService(this.mFaceUnlockConnection);
    }

    private boolean isFaceAdded() {
        boolean z = false;
        if (this.mFaceSettingService == null) {
            return false;
        }
        int addState = 2;
        try {
            addState = this.mFaceSettingService.checkState(0);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Start check face state:");
            stringBuilder.append(addState);
            Log.i(str, stringBuilder.toString());
        } catch (RemoteException re) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Start check face State RemoteException:");
            stringBuilder2.append(re);
            Log.i(str2, stringBuilder2.toString());
        }
        if (addState == 0) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        bindFaceUnlockService();
        if (isFaceAdded()) {
            finish();
        }
    }

    public void onPause() {
        unbindFaceUnlockService();
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public Intent getChooseLockIntent() {
        Intent intent = new Intent(this, ChooseLockGeneric.class);
        if (StorageManagerWrapper.isFileEncryptedNativeOrEmulated()) {
            intent.putExtra("lockscreen.password_type", 131072);
            intent.putExtra(ChooseLockGenericFragment.EXTRA_SHOW_OPTIONS_BUTTON, true);
        }
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            this.mAlreadyHadLockScreenSetup = isKeyguardSecure();
        } else {
            this.mAlreadyHadLockScreenSetup = savedInstanceState.getBoolean(KEY_LOCK_SCREEN_PRESENT, false);
        }
        setContentView(R.layout.op_face_unlock_introduction);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.oneplus_face_unlock_add_title);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOCK_SCREEN_PRESENT, this.mAlreadyHadLockScreenSetup);
    }

    public boolean checkIfNeedPassword() {
        int passwordQuality = new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId());
        if (passwordQuality == 65536 || passwordQuality == 131072 || passwordQuality == 196608 || passwordQuality == 262144 || passwordQuality == 327680 || passwordQuality == 393216) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        super.initViews();
        TextView description = (TextView) findViewById(R.id.description_text);
        description.setText(R.string.security_settings_fingerprint_enroll_introduction_message_setup);
        Button nextButton = getNextButton();
        if (OPUtils.isSupportXCamera()) {
            setHeaderText(R.string.oneplus_face_unlock_add_title);
            description.setText(R.string.oneplus_faceunlock_introduction_title);
            findViewById(R.id.rich_warning_text).setVisibility(0);
        } else if (OPUtils.isSupportCustomFingerprint()) {
            setHeaderText(R.string.oneplus_use_faceunlock_and_fingerprint_settings_title);
            description.setText(R.string.oneplus_use_faceunlock_and_fingerprint_settings_summary);
        } else {
            setHeaderText(R.string.oneplus_face_setup_unlock_settings_title);
            description.setText(R.string.oneplus_face_setup_unlock_settings_summary);
        }
        nextButton.setText(R.string.oneplus_face_unlock_add_title);
        Button cancelButton = (Button) findViewById(R.id.fingerprint_cancel_button);
        cancelButton.setOnClickListener(this);
        cancelButton.setText(R.string.oneplus_password_cancel);
        ((TextView) findViewById(R.id.functional_terms)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FaceUnlockEnrollIntroduction.OPLEGAL_NOTICES_ACTION);
                intent.putExtra(FaceUnlockEnrollIntroduction.KEY_NOTICES_TYPE, 10);
                intent.putExtra(FaceUnlockEnrollIntroduction.KEY_FROM_SETTINGS, true);
                FaceUnlockEnrollIntroduction.this.startActivity(intent);
            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.fingerprint_cancel_button) {
            onCancelButtonClick();
        } else {
            super.onClick(v);
        }
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return (Button) findViewById(R.id.fingerprint_next_button);
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
        Intent intent = null;
        try {
            intent = new Intent();
            intent.setClassName(OPConstants.PACKAGENAME_FACE_UNLOCK, "com.oneplus.faceunlock.FaceUnlockActivity");
            intent.putExtra("FaceUnlockActivity.StartMode", 0);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.d("OPFaceUnlockSettings", stringBuilder.toString());
        }
    }

    private void launchFaceUnlock() {
        try {
            startActivityForResult(OPFaceUnlockSettings.getSetupFaceUnlockIntent(this), 4);
            overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
        } catch (ActivityNotFoundException e) {
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
        startActivityForResult(intent, 7);
    }

    /* Access modifiers changed, original: protected */
    public void onCancelButtonClick() {
        finish();
    }

    private boolean isKeyguardSecure() {
        return ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardSecure();
    }

    public int getMetricsCategory() {
        return 249;
    }

    /* Access modifiers changed, original: protected */
    public boolean isSetupWizard() {
        return false;
    }
}
