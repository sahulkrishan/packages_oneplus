package com.android.settings.fingerprint;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources.Theme;
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
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.password.SetupChooseLockGeneric;
import com.android.settings.password.SetupChooseLockGeneric.SetupChooseLockGenericFragment;
import com.android.settings.password.StorageManagerWrapper;
import com.oneplus.faceunlock.internal.IOPFaceSettingService;
import com.oneplus.faceunlock.internal.IOPFaceSettingService.Stub;
import com.oneplus.settings.faceunlock.OPFaceUnlockModeSettingsActivity;
import com.oneplus.settings.faceunlock.OPFaceUnlockSettings;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class SetupFaceUnlockEnrollIntroduction extends FingerprintEnrollIntroduction {
    private static final int FACE_RESULT_FAIL = 1;
    private static final int FACE_RESULT_NOT_FOUND = 2;
    private static final int FACE_RESULT_OK = 0;
    private static final String KEY_LOCK_SCREEN_PRESENT = "wasLockScreenPresent";
    private static final String TAG = "SetupFaceUnlockEnrollIntroduction";
    private boolean mAlreadyHadLockScreenSetup = false;
    private IOPFaceSettingService mFaceSettingService;
    private ServiceConnection mFaceUnlockConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(SetupFaceUnlockEnrollIntroduction.TAG, "Oneplus face unlock service connected");
            SetupFaceUnlockEnrollIntroduction.this.mFaceSettingService = Stub.asInterface(service);
            if (SetupFaceUnlockEnrollIntroduction.this.isFaceAdded()) {
                SetupFaceUnlockEnrollIntroduction.this.getNextButton().setVisibility(8);
            } else {
                SetupFaceUnlockEnrollIntroduction.this.getNextButton().setVisibility(0);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(SetupFaceUnlockEnrollIntroduction.TAG, "Oneplus face unlock service disconnected");
            SetupFaceUnlockEnrollIntroduction.this.mFaceSettingService = null;
        }
    };

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
    }

    public void onPause() {
        unbindFaceUnlockService();
        super.onPause();
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
    public void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFromSetup = true;
        if (savedInstanceState == null) {
            this.mAlreadyHadLockScreenSetup = isKeyguardSecure();
        } else {
            this.mAlreadyHadLockScreenSetup = savedInstanceState.getBoolean(KEY_LOCK_SCREEN_PRESENT, false);
        }
        setContentView(R.layout.setup_op_face_unlock_introduction);
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
                intent.putExtra("op_legal_notices_type", 10);
                intent.putExtra("key_from_settings", true);
                SetupFaceUnlockEnrollIntroduction.this.startActivity(intent);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.mFromSetup = true;
        Intent intent;
        if (requestCode == 2 && isKeyguardSecure() && !this.mAlreadyHadLockScreenSetup) {
            data = getMetricIntent(data);
            if (!OPUtils.isO2()) {
                try {
                    ComponentName componentName;
                    intent = new Intent();
                    if (OPUtils.isGuestMode()) {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.UserSettingSuccess");
                    } else {
                        componentName = new ComponentName("com.oneplus.provision", "com.oneplus.provision.GesturesActivity");
                    }
                    intent.setComponent(componentName);
                    startActivity(intent);
                    overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
                    finish();
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        } else if (requestCode != 4 || resultCode == 0) {
            if (requestCode == 7 && resultCode == 1) {
                this.mToken = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                launchFaceUnlock();
            } else if (requestCode == 6 && (resultCode == 2 || resultCode == 11 || resultCode == 1 || resultCode == -1)) {
                goToNextPage();
            } else if (requestCode == 13) {
                updatePasswordQuality();
                if (this.mHasPassword && resultCode != 0) {
                    goToNextPage();
                }
            }
        } else if (OPUtils.isSupportCustomFingerprint() && isFaceAdded() && OPUtils.isSupportXCamera()) {
            Intent i = new Intent();
            i.putExtra(OPFaceUnlockModeSettingsActivity.ENTER_FACEUNLOCK_MODE_SETTINGS_FROM_SUW, true);
            i.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
            if (this.mUserId != -10000) {
                i.putExtra("android.intent.extra.USER_ID", this.mUserId);
            }
            i.setClassName("com.android.settings", "com.oneplus.settings.faceunlock.OPFaceUnlockModeSettingsActivity");
            startActivityForResult(i, 13);
        } else {
            intent = new Intent(this, SetupFingerprintEnrollIntroduction.class);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
            if (this.mUserId != -10000) {
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
            }
            startActivityForResult(intent, 6);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void goToNextPage() {
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
        if (!this.mHasPassword) {
            launchChooseLock();
        } else if (!isFaceAdded()) {
            launchFaceUnlock();
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
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.fingerprint.SetupFingerprintEnrollIntroduction"));
            startActivityForResult(intent, 6);
            overridePendingTransition(R.anim.op_slide_in, R.anim.op_slide_out);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            finish();
        }
    }

    private boolean isKeyguardSecure() {
        return ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardSecure();
    }

    public int getMetricsCategory() {
        return 249;
    }

    /* Access modifiers changed, original: protected */
    public boolean isSetupWizard() {
        return true;
    }
}
