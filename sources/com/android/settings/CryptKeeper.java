package com.android.settings;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.android.settings.widget.ImeAwareEditText;
import java.util.List;

public class CryptKeeper extends Activity implements OnEditorActionListener, OnKeyListener, OnTouchListener, TextWatcher {
    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    private static final int COOL_DOWN_ATTEMPTS = 10;
    private static final String DECRYPT_STATE = "trigger_restart_framework";
    private static final String EXTRA_FORCE_VIEW = "com.android.settings.CryptKeeper.DEBUG_FORCE_VIEW";
    private static final int FAKE_ATTEMPT_DELAY = 1000;
    private static final String FORCE_VIEW_ERROR = "error";
    private static final String FORCE_VIEW_PASSWORD = "password";
    private static final String FORCE_VIEW_PROGRESS = "progress";
    private static final int MAX_FAILED_ATTEMPTS = 30;
    private static final int MESSAGE_NOTIFY = 2;
    private static final int MESSAGE_UPDATE_PROGRESS = 1;
    protected static final int MIN_LENGTH_BEFORE_REPORT = 4;
    private static final String ONEPLUS_A3000 = "ONEPLUS A3000";
    private static final String ONEPLUS_A3003 = "ONEPLUS A3003";
    private static final String ONEPLUS_A3010 = "ONEPLUS A3010";
    private static final int RIGHT_PATTERN_CLEAR_TIMEOUT_MS = 500;
    private static final String STATE_COOLDOWN = "cooldown";
    private static final String TAG = "CryptKeeper";
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 1500;
    private static int sWidgetsToDisable = 52887552;
    private AudioManager mAudioManager;
    protected OnPatternListener mChooseNewLockPatternListener = new OnPatternListener() {
        public void onPatternStart() {
            CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
        }

        public void onPatternCleared() {
        }

        public void onPatternDetected(List<Cell> pattern) {
            CryptKeeper.this.mLockPatternView.setEnabled(false);
            if (pattern.size() >= 4) {
                new DecryptTask(CryptKeeper.this, null).execute(new String[]{LockPatternUtils.patternToString(pattern)});
                return;
            }
            CryptKeeper.this.fakeUnlockAttempt(CryptKeeper.this.mLockPatternView);
        }

        public void onPatternCellAdded(List<Cell> list) {
        }
    };
    private final Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.mLockPatternView.clearPattern();
        }
    };
    private boolean mCooldown = false;
    private boolean mCorrupt;
    private boolean mEncryptionGoneBad;
    private final Runnable mFakeUnlockAttemptRunnable = new Runnable() {
        public void run() {
            CryptKeeper.this.handleBadAttempt(Integer.valueOf(1));
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CryptKeeper.this.updateProgress();
                    return;
                case 2:
                    CryptKeeper.this.notifyUser();
                    return;
                default:
                    return;
            }
        }
    };
    private LockPatternView mLockPatternView;
    private int mNotificationCountdown = 0;
    private ImeAwareEditText mPasswordEntry;
    private int mReleaseWakeLockCountdown = 0;
    private StatusBarManager mStatusBar;
    private int mStatusString = R.string.enter_password;
    private boolean mValidationComplete;
    private boolean mValidationRequested;
    WakeLock mWakeLock;

    private class DecryptTask extends AsyncTask<String, Void, Integer> {
        private DecryptTask() {
        }

        /* synthetic */ DecryptTask(CryptKeeper x0, AnonymousClass1 x1) {
            this();
        }

        private void hide(int id) {
            View view = CryptKeeper.this.findViewById(id);
            if (view != null) {
                view.setVisibility(8);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPreExecute() {
            super.onPreExecute();
            CryptKeeper.this.beginAttempt();
        }

        /* Access modifiers changed, original: protected|varargs */
        public Integer doInBackground(String... params) {
            try {
                return Integer.valueOf(CryptKeeper.this.getStorageManager().decryptStorage(params[0]));
            } catch (Exception e) {
                Log.e(CryptKeeper.TAG, "Error while decrypting...", e);
                return Integer.valueOf(-1);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Integer failedAttempts) {
            if (failedAttempts.intValue() == 0) {
                if (CryptKeeper.this.mLockPatternView != null) {
                    CryptKeeper.this.mLockPatternView.removeCallbacks(CryptKeeper.this.mClearPatternRunnable);
                    CryptKeeper.this.mLockPatternView.postDelayed(CryptKeeper.this.mClearPatternRunnable, 500);
                }
                ((TextView) CryptKeeper.this.findViewById(R.id.status)).setText(R.string.starting_android);
                hide(R.id.passwordEntry);
                hide(R.id.switch_ime_button);
                hide(R.id.lockPattern);
                hide(R.id.owner_info);
                hide(R.id.emergencyCallButton);
            } else if (failedAttempts.intValue() == 30) {
                Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                intent.setPackage("android");
                intent.addFlags(268435456);
                intent.putExtra("android.intent.extra.REASON", "CryptKeeper.MAX_FAILED_ATTEMPTS");
                CryptKeeper.this.sendBroadcast(intent);
            } else if (failedAttempts.intValue() == -1) {
                CryptKeeper.this.setContentView(R.layout.crypt_keeper_progress);
                CryptKeeper.this.showFactoryReset(true);
            } else {
                CryptKeeper.this.handleBadAttempt(failedAttempts);
            }
        }
    }

    private static class NonConfigurationInstanceState {
        final WakeLock wakelock;

        NonConfigurationInstanceState(WakeLock _wakelock) {
            this.wakelock = _wakelock;
        }
    }

    private class ValidationTask extends AsyncTask<Void, Void, Boolean> {
        int state;

        private ValidationTask() {
        }

        /* synthetic */ ValidationTask(CryptKeeper x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected|varargs */
        public Boolean doInBackground(Void... params) {
            IStorageManager service = CryptKeeper.this.getStorageManager();
            try {
                Log.d(CryptKeeper.TAG, "Validating encryption state.");
                this.state = service.getEncryptionState();
                if (this.state == 1) {
                    Log.w(CryptKeeper.TAG, "Unexpectedly in CryptKeeper even though there is no encryption.");
                    return Boolean.valueOf(true);
                }
                return Boolean.valueOf(this.state == 0);
            } catch (RemoteException e) {
                Log.w(CryptKeeper.TAG, "Unable to get encryption state properly");
                return Boolean.valueOf(true);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Boolean result) {
            boolean z = true;
            CryptKeeper.this.mValidationComplete = true;
            if (Boolean.FALSE.equals(result)) {
                Log.w(CryptKeeper.TAG, "Incomplete, or corrupted encryption detected. Prompting user to wipe.");
                CryptKeeper.this.mEncryptionGoneBad = true;
                CryptKeeper cryptKeeper = CryptKeeper.this;
                if (this.state != -4) {
                    z = false;
                }
                cryptKeeper.mCorrupt = z;
            } else {
                Log.d(CryptKeeper.TAG, "Encryption state validated. Proceeding to configure UI");
            }
            CryptKeeper.this.setupUi();
        }
    }

    private void beginAttempt() {
        ((TextView) findViewById(R.id.status)).setText(R.string.checking_decryption);
    }

    private void handleBadAttempt(Integer failedAttempts) {
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 1500);
        }
        if (failedAttempts.intValue() % 10 == 0) {
            this.mCooldown = true;
            cooldown();
            return;
        }
        TextView status = (TextView) findViewById(R.id.status);
        if (30 - failedAttempts.intValue() < 10) {
            status.setText(TextUtils.expandTemplate(getText(R.string.crypt_keeper_warn_wipe), new CharSequence[]{Integer.toString(remainingAttempts)}));
        } else {
            int passwordType = 0;
            try {
                passwordType = getStorageManager().getPasswordType();
            } catch (Exception e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error calling mount service ");
                stringBuilder.append(e);
                Log.e(str, stringBuilder.toString());
            }
            if (passwordType == 3) {
                status.setText(R.string.cryptkeeper_wrong_pin);
            } else if (passwordType == 2) {
                status.setText(R.string.cryptkeeper_wrong_pattern);
            } else {
                status.setText(R.string.cryptkeeper_wrong_password);
            }
        }
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            this.mLockPatternView.setEnabled(true);
        }
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setEnabled(true);
            this.mPasswordEntry.scheduleShowSoftInput();
            setBackFunctionality(true);
        }
    }

    private boolean isDebugView() {
        return getIntent().hasExtra(EXTRA_FORCE_VIEW);
    }

    private boolean isDebugView(String viewType) {
        return viewType.equals(getIntent().getStringExtra(EXTRA_FORCE_VIEW));
    }

    private void notifyUser() {
        if (this.mNotificationCountdown > 0) {
            this.mNotificationCountdown--;
        } else if (this.mAudioManager != null) {
            try {
                this.mAudioManager.playSoundEffect(5, 100);
            } catch (Exception e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("notifyUser: Exception while playing sound: ");
                stringBuilder.append(e);
                Log.w(str, stringBuilder.toString());
            }
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 5000);
        if (!this.mWakeLock.isHeld()) {
            return;
        }
        if (this.mReleaseWakeLockCountdown > 0) {
            this.mReleaseWakeLockCountdown--;
        } else {
            this.mWakeLock.release();
        }
    }

    public void onBackPressed() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String state = SystemProperties.get("vold.decrypt");
        if (isDebugView() || !("".equals(state) || DECRYPT_STATE.equals(state))) {
            try {
                if (getResources().getBoolean(R.bool.crypt_keeper_allow_rotation)) {
                    setRequestedOrientation(-1);
                }
            } catch (NotFoundException e) {
            }
            if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A3000) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A3010) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A3003)) {
                sWidgetsToDisable |= 8388608;
            }
            this.mStatusBar = (StatusBarManager) getSystemService("statusbar");
            this.mStatusBar.disable(sWidgetsToDisable);
            if (savedInstanceState != null) {
                this.mCooldown = savedInstanceState.getBoolean(STATE_COOLDOWN);
            }
            setAirplaneModeIfNecessary();
            this.mAudioManager = (AudioManager) getSystemService("audio");
            NonConfigurationInstanceState lastInstance = getLastNonConfigurationInstance();
            if (lastInstance instanceof NonConfigurationInstanceState) {
                this.mWakeLock = lastInstance.wakelock;
                Log.d(TAG, "Restoring wakelock from NonConfigurationInstanceState");
            }
            return;
        }
        disableCryptKeeperComponent(this);
        finish();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_COOLDOWN, this.mCooldown);
    }

    public void onStart() {
        super.onStart();
        setupUi();
    }

    private void setupUi() {
        if (this.mEncryptionGoneBad || isDebugView(FORCE_VIEW_ERROR)) {
            setContentView(R.layout.crypt_keeper_progress);
            showFactoryReset(this.mCorrupt);
            return;
        }
        if (!"".equals(SystemProperties.get("vold.encrypt_progress")) || isDebugView("progress")) {
            setContentView(R.layout.crypt_keeper_progress);
            encryptionProgressInit();
        } else if (this.mValidationComplete || isDebugView("password")) {
            new AsyncTask<Void, Void, Void>() {
                String owner_info;
                int passwordType = 0;
                boolean password_visible;
                boolean pattern_visible;

                public Void doInBackground(Void... v) {
                    try {
                        IStorageManager service = CryptKeeper.this.getStorageManager();
                        this.passwordType = service.getPasswordType();
                        this.owner_info = service.getField("OwnerInfo");
                        this.pattern_visible = "0".equals(service.getField("PatternVisible")) ^ 1;
                        this.password_visible = "0".equals(service.getField("PasswordVisible")) ^ 1;
                    } catch (Exception e) {
                        String str = CryptKeeper.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Error calling mount service ");
                        stringBuilder.append(e);
                        Log.e(str, stringBuilder.toString());
                    }
                    return null;
                }

                public void onPostExecute(Void v) {
                    System.putInt(CryptKeeper.this.getContentResolver(), "show_password", this.password_visible);
                    if (this.passwordType == 3) {
                        CryptKeeper.this.setContentView(R.layout.crypt_keeper_pin_entry);
                        CryptKeeper.this.mStatusString = R.string.enter_pin;
                    } else if (this.passwordType == 2) {
                        CryptKeeper.this.setContentView(R.layout.crypt_keeper_pattern_entry);
                        CryptKeeper.this.setBackFunctionality(false);
                        CryptKeeper.this.mStatusString = R.string.enter_pattern;
                    } else {
                        CryptKeeper.this.setContentView(R.layout.crypt_keeper_password_entry);
                        CryptKeeper.this.mStatusString = R.string.enter_password;
                    }
                    ((TextView) CryptKeeper.this.findViewById(R.id.status)).setText(CryptKeeper.this.mStatusString);
                    TextView ownerInfo = (TextView) CryptKeeper.this.findViewById(R.id.owner_info);
                    ownerInfo.setText(this.owner_info);
                    ownerInfo.setSelected(true);
                    CryptKeeper.this.passwordEntryInit();
                    CryptKeeper.this.findViewById(16908290).setSystemUiVisibility(4194304);
                    if (CryptKeeper.this.mLockPatternView != null) {
                        CryptKeeper.this.mLockPatternView.setInStealthMode(1 ^ this.pattern_visible);
                    }
                    if (CryptKeeper.this.mCooldown) {
                        CryptKeeper.this.setBackFunctionality(false);
                        CryptKeeper.this.cooldown();
                    }
                }
            }.execute(new Void[0]);
        } else if (!this.mValidationRequested) {
            new ValidationTask(this, null).execute((Void[]) null);
            this.mValidationRequested = true;
        }
    }

    public void onStop() {
        super.onStop();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
    }

    public Object onRetainNonConfigurationInstance() {
        NonConfigurationInstanceState state = new NonConfigurationInstanceState(this.mWakeLock);
        Log.d(TAG, "Handing wakelock off to NonConfigurationInstanceState");
        this.mWakeLock = null;
        return state;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mWakeLock != null) {
            Log.d(TAG, "Releasing and destroying wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void encryptionProgressInit() {
        Log.d(TAG, "Encryption progress screen initializing.");
        if (this.mWakeLock == null) {
            Log.d(TAG, "Acquiring wakelock.");
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(26, TAG);
            this.mWakeLock.acquire();
        }
        ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(true);
        setBackFunctionality(false);
        updateProgress();
    }

    private void showFactoryReset(final boolean corrupt) {
        findViewById(R.id.encroid).setVisibility(8);
        Button button = (Button) findViewById(R.id.factory_reset);
        button.setVisibility(0);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.FACTORY_RESET");
                intent.setPackage("android");
                intent.addFlags(268435456);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("CryptKeeper.showFactoryReset() corrupt=");
                stringBuilder.append(corrupt);
                intent.putExtra("android.intent.extra.REASON", stringBuilder.toString());
                CryptKeeper.this.sendBroadcast(intent);
            }
        });
        if (corrupt) {
            ((TextView) findViewById(R.id.title)).setText(R.string.crypt_keeper_data_corrupt_title);
            ((TextView) findViewById(R.id.status)).setText(R.string.crypt_keeper_data_corrupt_summary);
        } else {
            ((TextView) findViewById(R.id.title)).setText(R.string.crypt_keeper_failed_title);
            ((TextView) findViewById(R.id.status)).setText(R.string.crypt_keeper_failed_summary);
        }
        View view = findViewById(R.id.bottom_divider);
        if (view != null) {
            view.setVisibility(0);
        }
    }

    private void updateProgress() {
        String str;
        StringBuilder stringBuilder;
        String state = SystemProperties.get("vold.encrypt_progress");
        if ("error_partially_encrypted".equals(state)) {
            showFactoryReset(false);
            return;
        }
        CharSequence status = getText(R.string.crypt_keeper_setup_description);
        int percent = 0;
        try {
            percent = isDebugView() ? 50 : Integer.parseInt(state);
        } catch (Exception e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Error parsing progress: ");
            stringBuilder.append(e.toString());
            Log.w(str, stringBuilder.toString());
        }
        String progress = Integer.toString(percent);
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Encryption progress: ");
        stringBuilder.append(progress);
        Log.v(str, stringBuilder.toString());
        try {
            int time = Integer.parseInt(SystemProperties.get("vold.encrypt_time_remaining"));
            if (time >= 0) {
                progress = DateUtils.formatElapsedTime((long) (((time + 9) / 10) * 10));
                status = getText(R.string.crypt_keeper_setup_time_remaining);
            }
        } catch (Exception e2) {
        }
        TextView tv = (TextView) findViewById(R.id.status);
        if (tv != null) {
            tv.setText(TextUtils.expandTemplate(status, new CharSequence[]{progress}));
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private void cooldown() {
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setEnabled(false);
        }
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setEnabled(false);
        }
        ((TextView) findViewById(R.id.status)).setText(R.string.crypt_keeper_force_power_cycle);
    }

    private final void setBackFunctionality(boolean isEnabled) {
        if (isEnabled) {
            this.mStatusBar.disable(sWidgetsToDisable);
        } else {
            this.mStatusBar.disable(sWidgetsToDisable | 4194304);
        }
    }

    private void fakeUnlockAttempt(View postingView) {
        beginAttempt();
        postingView.postDelayed(this.mFakeUnlockAttemptRunnable, 1000);
    }

    private void passwordEntryInit() {
        View emergencyCall;
        this.mPasswordEntry = (ImeAwareEditText) findViewById(R.id.passwordEntry);
        if (this.mPasswordEntry != null) {
            this.mPasswordEntry.setOnEditorActionListener(this);
            this.mPasswordEntry.requestFocus();
            this.mPasswordEntry.setOnKeyListener(this);
            this.mPasswordEntry.setOnTouchListener(this);
            this.mPasswordEntry.addTextChangedListener(this);
        }
        this.mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        if (this.mLockPatternView != null) {
            this.mLockPatternView.setOnPatternListener(this.mChooseNewLockPatternListener);
        }
        if (!getTelephonyManager().isVoiceCapable()) {
            emergencyCall = findViewById(R.id.emergencyCallButton);
            if (emergencyCall != null) {
                Log.d(TAG, "Removing the emergency Call button");
                emergencyCall.setVisibility(8);
            }
        }
        emergencyCall = findViewById(R.id.switch_ime_button);
        final InputMethodManager imm = (InputMethodManager) getSystemService("input_method");
        if (emergencyCall != null && hasMultipleEnabledIMEsOrSubtypes(imm, false)) {
            emergencyCall.setVisibility(0);
            emergencyCall.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    imm.showInputMethodPicker(false);
                }
            });
        }
        if (this.mWakeLock == null) {
            Log.d(TAG, "Acquiring wakelock.");
            PowerManager pm = (PowerManager) getSystemService("power");
            if (pm != null) {
                this.mWakeLock = pm.newWakeLock(26, TAG);
                this.mWakeLock.acquire();
                this.mReleaseWakeLockCountdown = 96;
            }
        }
        if (this.mLockPatternView == null && !this.mCooldown) {
            getWindow().setSoftInputMode(5);
            if (this.mPasswordEntry != null) {
                this.mPasswordEntry.scheduleShowSoftInput();
            }
        }
        updateEmergencyCallButtonState();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 120000);
        getWindow().addFlags(4718592);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager imm, boolean shouldIncludeAuxiliarySubtypes) {
        int filteredImisCount = 0;
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (filteredImisCount > 1) {
                return true;
            }
            List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(imi, true);
            if (subtypes.isEmpty()) {
                filteredImisCount++;
            } else {
                int auxCount = 0;
                for (InputMethodSubtype subtype : subtypes) {
                    if (subtype.isAuxiliary()) {
                        auxCount++;
                    }
                }
                if (subtypes.size() - auxCount > 0 || (shouldIncludeAuxiliarySubtypes && auxCount > 1)) {
                    filteredImisCount++;
                }
            }
        }
        boolean z = false;
        if (filteredImisCount > 1 || imm.getEnabledInputMethodSubtypeList(null, false).size() > 1) {
            z = true;
        }
        return z;
    }

    private IStorageManager getStorageManager() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        return null;
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != 0 && actionId != 6) {
            return false;
        }
        String password = v.getText().toString();
        if (TextUtils.isEmpty(password)) {
            return true;
        }
        v.setText(null);
        this.mPasswordEntry.setEnabled(false);
        setBackFunctionality(false);
        if (password.length() >= 4) {
            new DecryptTask(this, null).execute(new String[]{password});
        } else {
            fakeUnlockAttempt(this.mPasswordEntry);
        }
        return true;
    }

    private final void setAirplaneModeIfNecessary() {
        if (!(getTelephonyManager().getLteOnCdmaMode() == 1)) {
            Log.d(TAG, "Going into airplane mode.");
            Global.putInt(getContentResolver(), "airplane_mode_on", 1);
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", true);
            sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void updateEmergencyCallButtonState() {
        Button emergencyCall = (Button) findViewById(R.id.emergencyCallButton);
        if (emergencyCall != null) {
            if (isEmergencyCallCapable()) {
                int textId;
                emergencyCall.setVisibility(0);
                emergencyCall.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        CryptKeeper.this.takeEmergencyCallAction();
                    }
                });
                if (getTelecomManager().isInCall()) {
                    textId = R.string.cryptkeeper_return_to_call;
                } else {
                    textId = R.string.cryptkeeper_emergency_call;
                }
                emergencyCall.setText(textId);
                return;
            }
            emergencyCall.setVisibility(8);
        }
    }

    private boolean isEmergencyCallCapable() {
        return getResources().getBoolean(17957079);
    }

    private void takeEmergencyCallAction() {
        TelecomManager telecomManager = getTelecomManager();
        if (telecomManager.isInCall()) {
            telecomManager.showInCallScreen(false);
        } else {
            launchEmergencyDialer();
        }
    }

    private void launchEmergencyDialer() {
        Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(276824064);
        setBackFunctionality(true);
        startActivity(intent);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) getSystemService("phone");
    }

    private TelecomManager getTelecomManager() {
        return (TelecomManager) getSystemService("telecom");
    }

    private void delayAudioNotification() {
        this.mNotificationCountdown = 20;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        delayAudioNotification();
        return false;
    }

    public boolean onTouch(View v, MotionEvent event) {
        delayAudioNotification();
        return false;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        delayAudioNotification();
    }

    public void afterTextChanged(Editable s) {
    }

    private static void disableCryptKeeperComponent(Context context) {
        PackageManager pm = context.getPackageManager();
        ComponentName name = new ComponentName(context, CryptKeeper.class);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Disabling component ");
        stringBuilder.append(name);
        Log.d(str, stringBuilder.toString());
        pm.setComponentEnabledSetting(name, 2, 1);
    }
}
