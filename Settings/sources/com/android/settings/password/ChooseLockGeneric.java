package com.android.settings.password;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.UserInfo;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.security.KeyStore;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.EncryptionInterstitial;
import com.android.settings.EventLogTags;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.fingerprint.FingerprintEnrollFindSensor;
import com.android.settings.password.ChooseLockPassword.IntentBuilder;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.oneplus.faceunlock.internal.IOPFaceSettingService;
import com.oneplus.faceunlock.internal.IOPFaceSettingService.Stub;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.utils.OPConstants;
import java.util.List;

public class ChooseLockGeneric extends SettingsActivity {
    public static final String CONFIRM_CREDENTIALS = "confirm_credentials";

    public static class InternalActivity extends ChooseLockGeneric {
    }

    public static class ChooseLockGenericFragment extends SettingsPreferenceFragment {
        private static final int CHOOSE_LOCK_BEFORE_FINGERPRINT_REQUEST = 103;
        private static final int CHOOSE_LOCK_REQUEST = 102;
        private static final int CONFIRM_EXISTING_REQUEST = 100;
        private static final int ENABLE_ENCRYPTION_REQUEST = 101;
        public static final String ENCRYPT_REQUESTED_DISABLED = "encrypt_requested_disabled";
        public static final String ENCRYPT_REQUESTED_QUALITY = "encrypt_requested_quality";
        public static final String EXTRA_CHOOSE_LOCK_GENERIC_EXTRAS = "choose_lock_generic_extras";
        public static final String EXTRA_SHOW_OPTIONS_BUTTON = "show_options_button";
        public static final String HIDE_DISABLED_PREFS = "hide_disabled_prefs";
        private static final String KEY_SKIP_FINGERPRINT = "unlock_skip_fingerprint";
        public static final String MINIMUM_QUALITY_KEY = "minimum_quality";
        private static final int MIN_PASSWORD_LENGTH = 4;
        private static final String PASSWORD_CONFIRMED = "password_confirmed";
        private static final int SKIP_FINGERPRINT_REQUEST = 104;
        private static final String TAG = "ChooseLockGenericFragment";
        public static final String TAG_FRP_WARNING_DIALOG = "frp_warning_dialog";
        private static final String WAITING_FOR_CONFIRMATION = "waiting_for_confirmation";
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private ChooseLockGenericController mController;
        private ProgressDialog mCryptfsChangepwDefaultProgressDialog;
        private DevicePolicyManager mDPM;
        private boolean mEncryptionRequestDisabled;
        private int mEncryptionRequestQuality;
        private IOPFaceSettingService mFaceSettingService;
        private ServiceConnection mFaceUnlockConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(ChooseLockGenericFragment.TAG, "Oneplus face unlock service connected");
                ChooseLockGenericFragment.this.mFaceSettingService = Stub.asInterface(service);
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.i(ChooseLockGenericFragment.TAG, "Oneplus face unlock service disconnected");
                ChooseLockGenericFragment.this.mFaceSettingService = null;
            }
        };
        private FingerprintManager mFingerprintManager;
        private boolean mForChangeCredRequiredForBoot = false;
        protected boolean mForFingerprint = false;
        private Handler mHandler = new Handler();
        private boolean mHasChallenge = false;
        private boolean mHideDrawer = false;
        private boolean mIsSetNewPassword = false;
        private KeyStore mKeyStore;
        private LockPatternUtils mLockPatternUtils;
        private ManagedLockPasswordProvider mManagedPasswordProvider;
        private boolean mPasswordConfirmed = false;
        private int mUserId;
        private UserManager mUserManager;
        private String mUserPassword;
        private boolean mWaitingForConfirmation = false;

        public static class FactoryResetProtectionWarningDialog extends InstrumentedDialogFragment {
            private static final String ARG_MESSAGE_RES = "messageRes";
            private static final String ARG_TITLE_RES = "titleRes";
            private static final String ARG_UNLOCK_METHOD_TO_SET = "unlockMethodToSet";

            public static FactoryResetProtectionWarningDialog newInstance(int titleRes, int messageRes, String unlockMethodToSet) {
                FactoryResetProtectionWarningDialog frag = new FactoryResetProtectionWarningDialog();
                Bundle args = new Bundle();
                args.putInt(ARG_TITLE_RES, titleRes);
                args.putInt(ARG_MESSAGE_RES, messageRes);
                args.putString(ARG_UNLOCK_METHOD_TO_SET, unlockMethodToSet);
                frag.setArguments(args);
                return frag;
            }

            public void show(FragmentManager manager, String tag) {
                if (manager.findFragmentByTag(tag) == null) {
                    super.show(manager, tag);
                }
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                Bundle args = getArguments();
                return new Builder(getActivity()).setTitle(args.getInt(ARG_TITLE_RES)).setMessage(args.getInt(ARG_MESSAGE_RES)).setPositiveButton(R.string.unlock_disable_frp_warning_ok, new -$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$Abdb-f1FnDmiVy0c3RZHU7n2B2k(this, args)).setNegativeButton(R.string.cancel, new -$$Lambda$ChooseLockGeneric$ChooseLockGenericFragment$FactoryResetProtectionWarningDialog$YUiXVX_8NlQHl0UI000UMbpVL0U(this)).create();
            }

            public int getMetricsCategory() {
                return 528;
            }
        }

        private void removeFaceData() {
            if (this.mFaceSettingService != null) {
                try {
                    this.mFaceSettingService.removeFace(0);
                } catch (RemoteException re) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Start remove face RemoteException:");
                    stringBuilder.append(re);
                    Log.i(str, stringBuilder.toString());
                }
            }
        }

        private void bindFaceUnlockService() {
            try {
                Intent intent = new Intent();
                intent.setClassName(OPConstants.PACKAGENAME_FACE_UNLOCK, "com.oneplus.faceunlock.FaceSettingService");
                getActivity().bindService(intent, this.mFaceUnlockConnection, 1);
                Log.i(TAG, "Start bind oneplus face unlockservice");
            } catch (Exception e) {
                Log.i(TAG, "Bind oneplus face unlockservice exception");
            }
        }

        private void unbindFaceUnlockService() {
            Log.i(TAG, "Start unbind oneplus face unlockservice");
            getActivity().unbindService(this.mFaceUnlockConnection);
        }

        public int getMetricsCategory() {
            return 27;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            if (Utils.isDeviceProvisioned(activity) || canRunBeforeDeviceProvisioned()) {
                bindFaceUnlockService();
                String chooseLockAction = getActivity().getIntent().getAction();
                this.mFingerprintManager = Utils.getFingerprintManagerOrNull(getActivity());
                this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
                this.mKeyStore = KeyStore.getInstance();
                this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
                this.mLockPatternUtils = new LockPatternUtils(getActivity());
                this.mCryptfsChangepwDefaultProgressDialog = new ProgressDialog(getActivity());
                boolean z = false;
                boolean z2 = true;
                boolean z3 = "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(chooseLockAction) || "android.app.action.SET_NEW_PASSWORD".equals(chooseLockAction);
                this.mIsSetNewPassword = z3;
                this.mLockPatternUtils.sanitizePassword();
                z3 = getActivity().getIntent().getBooleanExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, true);
                if (getActivity() instanceof InternalActivity) {
                    this.mPasswordConfirmed = z3 ^ 1;
                    this.mUserPassword = getActivity().getIntent().getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                }
                this.mHideDrawer = getActivity().getIntent().getBooleanExtra(SettingsActivity.EXTRA_HIDE_DRAWER, false);
                this.mHasChallenge = getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
                this.mChallenge = getActivity().getIntent().getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
                this.mForFingerprint = getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, false);
                boolean z4 = getArguments() != null && getArguments().getBoolean(ChooseLockSettingsHelper.EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT);
                this.mForChangeCredRequiredForBoot = z4;
                this.mUserManager = UserManager.get(getActivity());
                if (savedInstanceState != null) {
                    this.mPasswordConfirmed = savedInstanceState.getBoolean(PASSWORD_CONFIRMED);
                    this.mWaitingForConfirmation = savedInstanceState.getBoolean(WAITING_FOR_CONFIRMATION);
                    this.mEncryptionRequestQuality = savedInstanceState.getInt(ENCRYPT_REQUESTED_QUALITY);
                    this.mEncryptionRequestDisabled = savedInstanceState.getBoolean(ENCRYPT_REQUESTED_DISABLED);
                    if (this.mUserPassword == null) {
                        this.mUserPassword = savedInstanceState.getString(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                    }
                }
                this.mUserId = Utils.getSecureTargetUser(getActivity().getActivityToken(), UserManager.get(getActivity()), getArguments(), getActivity().getIntent().getExtras()).getIdentifier();
                this.mController = new ChooseLockGenericController(getContext(), this.mUserId);
                if ("android.app.action.SET_NEW_PASSWORD".equals(chooseLockAction) && UserManager.get(getActivity()).isManagedProfile(this.mUserId) && this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId)) {
                    getActivity().setTitle(R.string.lock_settings_picker_title_profile);
                }
                this.mManagedPasswordProvider = ManagedLockPasswordProvider.get(getActivity(), this.mUserId);
                if (this.mPasswordConfirmed) {
                    if (savedInstanceState == null) {
                        z2 = false;
                    }
                    updatePreferencesOrFinish(z2);
                    if (this.mForChangeCredRequiredForBoot) {
                        maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                    }
                } else if (!this.mWaitingForConfirmation) {
                    ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);
                    boolean managedProfileWithUnifiedLock = UserManager.get(getActivity()).isManagedProfile(this.mUserId) && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mUserId);
                    boolean skipConfirmation = managedProfileWithUnifiedLock && !this.mIsSetNewPassword;
                    if (skipConfirmation || !helper.launchConfirmationActivity(100, getString(R.string.unlock_set_unlock_launch_picker_title), true, this.mUserId)) {
                        this.mPasswordConfirmed = true;
                        if (savedInstanceState != null) {
                            z = true;
                        }
                        updatePreferencesOrFinish(z);
                    } else {
                        this.mWaitingForConfirmation = true;
                    }
                }
                addHeaderView();
                return;
            }
            activity.finish();
        }

        /* Access modifiers changed, original: protected */
        public boolean canRunBeforeDeviceProvisioned() {
            return false;
        }

        /* Access modifiers changed, original: protected */
        public void addHeaderView() {
            if (this.mForFingerprint) {
                setHeaderView((int) R.layout.choose_lock_generic_fingerprint_header);
                if (this.mIsSetNewPassword) {
                    ((TextView) getHeaderView().findViewById(R.id.fingerprint_header_description)).setText(R.string.fingerprint_unlock_title);
                }
            }
        }

        public boolean onPreferenceTreeClick(Preference preference) {
            String key = preference.getKey();
            if (!isUnlockMethodSecure(key) && this.mLockPatternUtils.isSecure(this.mUserId)) {
                showFactoryResetProtectionWarningDialog(key);
                return true;
            } else if (!KEY_SKIP_FINGERPRINT.equals(key)) {
                return setUnlockMethod(key);
            } else {
                Intent chooseLockGenericIntent = new Intent(getActivity(), InternalActivity.class);
                chooseLockGenericIntent.setAction(getIntent().getAction());
                chooseLockGenericIntent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                chooseLockGenericIntent.putExtra(ChooseLockGeneric.CONFIRM_CREDENTIALS, this.mPasswordConfirmed ^ 1);
                if (this.mUserPassword != null) {
                    chooseLockGenericIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, this.mUserPassword);
                }
                startActivityForResult(chooseLockGenericIntent, 104);
                return true;
            }
        }

        private void maybeEnableEncryption(int quality, boolean disabled) {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService("device_policy");
            if (UserManager.get(getActivity()).isAdminUser() && this.mUserId == UserHandle.myUserId() && LockPatternUtils.isDeviceEncryptionEnabled() && !LockPatternUtils.isFileEncryptionEnabled() && !dpm.getDoNotAskCredentialsOnBoot()) {
                int i;
                this.mEncryptionRequestQuality = quality;
                this.mEncryptionRequestDisabled = disabled;
                Intent unlockMethodIntent = getIntentForUnlockMethod(quality);
                unlockMethodIntent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT, this.mForChangeCredRequiredForBoot);
                Context context = getActivity();
                Intent intent = getEncryptionInterstitialIntent(context, quality, this.mLockPatternUtils.isCredentialRequiredToDecrypt(AccessibilityManager.getInstance(context).isEnabled() ^ 1), unlockMethodIntent);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FINGERPRINT, this.mForFingerprint);
                intent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, this.mHideDrawer);
                if (this.mIsSetNewPassword && this.mHasChallenge) {
                    i = 103;
                } else {
                    i = 101;
                }
                startActivityForResult(intent, i);
            } else if (this.mForChangeCredRequiredForBoot) {
                finish();
            } else {
                updateUnlockMethodAndFinish(quality, disabled, false);
            }
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            this.mWaitingForConfirmation = false;
            int i = -1;
            if (requestCode == 100 && resultCode == -1) {
                this.mPasswordConfirmed = true;
                if (data != null) {
                    this.mUserPassword = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                }
                updatePreferencesOrFinish(false);
                if (this.mForChangeCredRequiredForBoot) {
                    if (TextUtils.isEmpty(this.mUserPassword)) {
                        finish();
                    } else {
                        maybeEnableEncryption(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId), false);
                    }
                }
            } else if (requestCode == 102 || requestCode == 101) {
                if (resultCode != 0 || this.mForChangeCredRequiredForBoot) {
                    getActivity().setResult(resultCode, data);
                    finish();
                } else if (getIntent().getIntExtra("lockscreen.password_type", -1) != -1) {
                    getActivity().setResult(0, data);
                    finish();
                }
            } else if (requestCode == 103 && resultCode == 1) {
                Intent intent = getFindSensorIntent(getActivity());
                if (data != null) {
                    intent.putExtras(data.getExtras());
                }
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                startActivity(intent);
                finish();
            } else if (requestCode != 104) {
                getActivity().setResult(0);
                finish();
            } else if (resultCode != 0) {
                Activity activity = getActivity();
                if (resultCode != 1) {
                    i = resultCode;
                }
                activity.setResult(i, data);
                finish();
            }
            if (requestCode == 0 && this.mForChangeCredRequiredForBoot) {
                finish();
            }
        }

        /* Access modifiers changed, original: protected */
        public Intent getFindSensorIntent(Context context) {
            return new Intent(context, FingerprintEnrollFindSensor.class);
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(PASSWORD_CONFIRMED, this.mPasswordConfirmed);
            outState.putBoolean(WAITING_FOR_CONFIRMATION, this.mWaitingForConfirmation);
            outState.putInt(ENCRYPT_REQUESTED_QUALITY, this.mEncryptionRequestQuality);
            outState.putBoolean(ENCRYPT_REQUESTED_DISABLED, this.mEncryptionRequestDisabled);
            if (this.mUserPassword != null) {
                outState.putString(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, this.mUserPassword);
            }
        }

        private void updatePreferencesOrFinish(boolean isRecreatingActivity) {
            Intent intent = getActivity().getIntent();
            int quality = intent.getIntExtra("lockscreen.password_type", -1);
            if (quality == -1) {
                quality = this.mController.upgradeQuality(intent.getIntExtra(MINIMUM_QUALITY_KEY, -1));
                boolean hideDisabledPrefs = intent.getBooleanExtra(HIDE_DISABLED_PREFS, false);
                PreferenceScreen prefScreen = getPreferenceScreen();
                if (prefScreen != null) {
                    prefScreen.removeAll();
                }
                addPreferences();
                disableUnusablePreferences(quality, hideDisabledPrefs);
                updatePreferenceText();
                updateCurrentPreference();
                updatePreferenceSummaryIfNeeded();
            } else if (!isRecreatingActivity) {
                updateUnlockMethodAndFinish(quality, false, true);
            }
        }

        /* Access modifiers changed, original: protected */
        public void addPreferences() {
            addPreferencesFromResource(R.xml.security_settings_picker);
            findPreference(ScreenLockType.NONE.preferenceKey).setViewId(R.id.lock_none);
            findPreference(KEY_SKIP_FINGERPRINT).setViewId(R.id.lock_none);
            findPreference(ScreenLockType.PIN.preferenceKey).setViewId(R.id.lock_pin);
            findPreference(ScreenLockType.PASSWORD.preferenceKey).setViewId(R.id.lock_password);
        }

        private void updatePreferenceText() {
            if (this.mForFingerprint) {
                setPreferenceTitle(ScreenLockType.PATTERN, (int) R.string.fingerprint_unlock_set_unlock_pattern);
                setPreferenceTitle(ScreenLockType.PIN, (int) R.string.fingerprint_unlock_set_unlock_pin);
                setPreferenceTitle(ScreenLockType.PASSWORD, (int) R.string.fingerprint_unlock_set_unlock_password);
            }
            if (this.mManagedPasswordProvider.isSettingManagedPasswordSupported()) {
                setPreferenceTitle(ScreenLockType.MANAGED, this.mManagedPasswordProvider.getPickerOptionTitle(this.mForFingerprint));
            } else {
                removePreference(ScreenLockType.MANAGED.preferenceKey);
            }
            if (!this.mForFingerprint || !this.mIsSetNewPassword) {
                removePreference(KEY_SKIP_FINGERPRINT);
            }
        }

        private void setPreferenceTitle(ScreenLockType lock, @StringRes int title) {
            Preference preference = findPreference(lock.preferenceKey);
            if (preference != null) {
                preference.setTitle(title);
            }
        }

        private void setPreferenceTitle(ScreenLockType lock, CharSequence title) {
            Preference preference = findPreference(lock.preferenceKey);
            if (preference != null) {
                preference.setTitle(title);
            }
        }

        private void setPreferenceSummary(ScreenLockType lock, @StringRes int summary) {
            Preference preference = findPreference(lock.preferenceKey);
            if (preference != null) {
                preference.setSummary(summary);
            }
        }

        private void updateCurrentPreference() {
            Preference preference = findPreference(getKeyForCurrent());
            if (preference != null) {
                preference.setSummary((int) R.string.current_screen_lock);
            }
        }

        private String getKeyForCurrent() {
            int credentialOwner = UserManager.get(getContext()).getCredentialOwnerProfile(this.mUserId);
            if (this.mLockPatternUtils.isLockScreenDisabled(credentialOwner)) {
                return ScreenLockType.NONE.preferenceKey;
            }
            ScreenLockType lock = ScreenLockType.fromQuality(this.mLockPatternUtils.getKeyguardStoredPasswordQuality(credentialOwner));
            return lock != null ? lock.preferenceKey : null;
        }

        /* Access modifiers changed, original: protected */
        public void disableUnusablePreferences(int quality, boolean hideDisabledPrefs) {
            disableUnusablePreferencesImpl(quality, hideDisabledPrefs);
        }

        /* Access modifiers changed, original: protected */
        public void disableUnusablePreferencesImpl(int quality, boolean hideDisabled) {
            int i;
            PreferenceScreen entries = getPreferenceScreen();
            int adminEnforcedQuality = this.mDPM.getPasswordQuality(null, this.mUserId);
            EnforcedAdmin enforcedAdmin = RestrictedLockUtils.checkIfPasswordQualityIsSet(getActivity(), this.mUserId);
            for (ScreenLockType lock : ScreenLockType.values()) {
                Preference pref = findPreference(lock.preferenceKey);
                if (pref instanceof RestrictedPreference) {
                    boolean visible = this.mController.isScreenLockVisible(lock);
                    boolean enabled = this.mController.isScreenLockEnabled(lock, quality);
                    boolean disabledByAdmin = this.mController.isScreenLockDisabledByAdmin(lock, adminEnforcedQuality);
                    if (hideDisabled) {
                        boolean z = visible && enabled;
                        visible = z;
                    }
                    if (!visible) {
                        entries.removePreference(pref);
                    } else if (disabledByAdmin && enforcedAdmin != null) {
                        ((RestrictedPreference) pref).setDisabledByAdmin(enforcedAdmin);
                    } else if (enabled) {
                        ((RestrictedPreference) pref).setDisabledByAdmin(null);
                    } else {
                        ((RestrictedPreference) pref).setDisabledByAdmin(null);
                        pref.setSummary((int) R.string.unlock_set_unlock_disabled_summary);
                        pref.setEnabled(false);
                    }
                } else {
                    i = quality;
                }
            }
            i = quality;
        }

        private void updatePreferenceSummaryIfNeeded() {
            if (StorageManager.isBlockEncrypted() && !StorageManager.isNonDefaultBlockEncrypted() && !AccessibilityManager.getInstance(getActivity()).getEnabledAccessibilityServiceList(-1).isEmpty()) {
                setPreferenceSummary(ScreenLockType.PATTERN, R.string.secure_lock_encryption_warning);
                setPreferenceSummary(ScreenLockType.PIN, R.string.secure_lock_encryption_warning);
                setPreferenceSummary(ScreenLockType.PASSWORD, R.string.secure_lock_encryption_warning);
                setPreferenceSummary(ScreenLockType.MANAGED, R.string.secure_lock_encryption_warning);
            }
        }

        /* Access modifiers changed, original: protected */
        public Intent getLockManagedPasswordIntent(String password) {
            return this.mManagedPasswordProvider.createIntent(false, password);
        }

        /* Access modifiers changed, original: protected */
        public Intent getLockPasswordIntent(int quality, int minLength, int maxLength) {
            IntentBuilder builder = new IntentBuilder(getContext()).setPasswordQuality(quality).setPasswordLengthRange(minLength, maxLength).setForFingerprint(this.mForFingerprint).setUserId(this.mUserId);
            if (this.mHasChallenge) {
                builder.setChallenge(this.mChallenge);
            }
            if (this.mUserPassword != null) {
                builder.setPassword(this.mUserPassword);
            }
            return builder.build();
        }

        /* Access modifiers changed, original: protected */
        public Intent getLockPatternIntent() {
            ChooseLockPattern.IntentBuilder builder = new ChooseLockPattern.IntentBuilder(getContext()).setForFingerprint(this.mForFingerprint).setUserId(this.mUserId);
            if (this.mHasChallenge) {
                builder.setChallenge(this.mChallenge);
            }
            if (this.mUserPassword != null) {
                builder.setPattern(this.mUserPassword);
            }
            return builder.build();
        }

        /* Access modifiers changed, original: protected */
        public Intent getEncryptionInterstitialIntent(Context context, int quality, boolean required, Intent unlockMethodIntent) {
            return EncryptionInterstitial.createStartIntent(context, quality, required, unlockMethodIntent);
        }

        /* Access modifiers changed, original: 0000 */
        public void updateUnlockMethodAndFinish(int quality, boolean disabled, boolean chooseLockSkipped) {
            if (this.mPasswordConfirmed) {
                quality = this.mController.upgradeQuality(quality);
                Intent intent = getIntentForUnlockMethod(quality);
                if (intent != null) {
                    int i;
                    if (getIntent().getBooleanExtra(EXTRA_SHOW_OPTIONS_BUTTON, false)) {
                        intent.putExtra(EXTRA_SHOW_OPTIONS_BUTTON, chooseLockSkipped);
                    }
                    intent.putExtra(EXTRA_CHOOSE_LOCK_GENERIC_EXTRAS, getIntent().getExtras());
                    if (this.mIsSetNewPassword && this.mHasChallenge) {
                        i = 103;
                    } else {
                        i = 102;
                    }
                    startActivityForResult(intent, i);
                    return;
                }
                if (quality == 0) {
                    this.mChooseLockSettingsHelper.utils().clearLock(this.mUserPassword, this.mUserId);
                    this.mChooseLockSettingsHelper.utils().setLockScreenDisabled(disabled, this.mUserId);
                    getActivity().setResult(-1);
                    removeAllFingerprintForUserAndFinish(this.mUserId);
                } else {
                    removeAllFingerprintForUserAndFinish(this.mUserId);
                }
                return;
            }
            throw new IllegalStateException("Tried to update password without confirming it");
        }

        private Intent getIntentForUnlockMethod(int quality) {
            Intent intent = null;
            if (quality >= 524288) {
                intent = getLockManagedPasswordIntent(this.mUserPassword);
            } else if (quality >= 131072) {
                int minLength = this.mDPM.getPasswordMinimumLength(null, this.mUserId);
                if (minLength < 4) {
                    minLength = 4;
                }
                intent = getLockPasswordIntent(quality, minLength, this.mDPM.getPasswordMaximumLength(quality));
            } else if (quality == 65536) {
                intent = getLockPatternIntent();
            }
            if (intent != null) {
                intent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, this.mHideDrawer);
            }
            return intent;
        }

        private void removeAllFingerprintForUserAndFinish(final int userId) {
            removeFaceData();
            OPGestureUtils.set0(SettingsBaseApplication.mApplication, 15);
            if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
                showResetPasswordDefaultDialog();
                Intent intent = new Intent("com.android.settings.action.DISMISS_APPLOCKER");
                intent.putExtra("applocker_package_name", "");
                intent.putExtra("applocker_dismiss_all", true);
                SettingsBaseApplication.mApplication.sendBroadcast(intent);
            } else if (this.mFingerprintManager.hasEnrolledFingerprints(userId)) {
                this.mFingerprintManager.setActiveUser(userId);
                this.mFingerprintManager.remove(new Fingerprint(null, userId, 0, 0), userId, new RemovalCallback() {
                    public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                        Log.e(ChooseLockGenericFragment.TAG, String.format("Can't remove fingerprint %d in group %d. Reason: %s", new Object[]{Integer.valueOf(fp.getFingerId()), Integer.valueOf(fp.getGroupId()), errString}));
                    }

                    public void onRemovalSucceeded(Fingerprint fp, int remaining) {
                        if (remaining == 0) {
                            ChooseLockGenericFragment.this.removeManagedProfileFingerprintsAndFinishIfNecessary(userId);
                        }
                    }
                });
            } else {
                removeManagedProfileFingerprintsAndFinishIfNecessary(userId);
            }
        }

        /* JADX WARNING: Missing block: B:13:0x004a, code skipped:
            return;
     */
        private void showResetPasswordDefaultDialog() {
            /*
            r5 = this;
            r0 = r5.mLockPatternUtils;
            r1 = r5.mUserId;
            r2 = 0;
            r0.savePINPasswordLength(r2, r1);
            r0 = r5.getActivity();
            if (r0 == 0) goto L_0x004a;
        L_0x000f:
            r1 = r0.isFinishing();
            if (r1 == 0) goto L_0x0016;
        L_0x0015:
            goto L_0x004a;
        L_0x0016:
            r1 = r5.mCryptfsChangepwDefaultProgressDialog;
            if (r1 == 0) goto L_0x0049;
        L_0x001a:
            r1 = r5.mCryptfsChangepwDefaultProgressDialog;
            r2 = 0;
            r1.setCancelable(r2);
            r1 = r5.mCryptfsChangepwDefaultProgressDialog;
            r2 = r5.getResources();
            r3 = 2131889435; // 0x7f120d1b float:1.9413533E38 double:1.0532933306E-314;
            r2 = r2.getString(r3);
            r1.setMessage(r2);
            if (r0 == 0) goto L_0x003d;
        L_0x0032:
            r1 = r0.isDestroyed();
            if (r1 != 0) goto L_0x003d;
        L_0x0038:
            r1 = r5.mCryptfsChangepwDefaultProgressDialog;
            r1.show();
        L_0x003d:
            r1 = r5.mHandler;
            r2 = new com.android.settings.password.ChooseLockGeneric$ChooseLockGenericFragment$3;
            r2.<init>(r0);
            r3 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
            r1.postDelayed(r2, r3);
        L_0x0049:
            return;
        L_0x004a:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.password.ChooseLockGeneric$ChooseLockGenericFragment.showResetPasswordDefaultDialog():void");
        }

        private void removeManagedProfileFingerprintsAndFinishIfNecessary(int parentUserId) {
            if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected()) {
                this.mFingerprintManager.setActiveUser(UserHandle.myUserId());
            }
            boolean hasChildProfile = false;
            if (!this.mUserManager.getUserInfo(parentUserId).isManagedProfile()) {
                List<UserInfo> profiles = this.mUserManager.getProfiles(parentUserId);
                int profilesSize = profiles.size();
                for (int i = 0; i < profilesSize; i++) {
                    UserInfo userInfo = (UserInfo) profiles.get(i);
                    if (userInfo.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userInfo.id)) {
                        removeAllFingerprintForUserAndFinish(userInfo.id);
                        hasChildProfile = true;
                        break;
                    }
                }
            }
            if (!hasChildProfile) {
                showResetPasswordDefaultDialog();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (this.mLockPatternUtils != null) {
                this.mLockPatternUtils.sanitizePassword();
            }
            unbindFaceUnlockService();
        }

        public int getHelpResource() {
            return R.string.help_url_choose_lockscreen;
        }

        private int getResIdForFactoryResetProtectionWarningTitle() {
            if (UserManager.get(getActivity()).isManagedProfile(this.mUserId)) {
                return R.string.unlock_disable_frp_warning_title_profile;
            }
            return R.string.unlock_disable_frp_warning_title;
        }

        private int getResIdForFactoryResetProtectionWarningMessage() {
            boolean hasFingerprints;
            if (this.mFingerprintManager == null || !this.mFingerprintManager.isHardwareDetected()) {
                hasFingerprints = false;
            } else {
                hasFingerprints = this.mFingerprintManager.hasEnrolledFingerprints(this.mUserId);
            }
            boolean isProfile = UserManager.get(getActivity()).isManagedProfile(this.mUserId);
            int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId);
            if (keyguardStoredPasswordQuality != 65536) {
                if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) {
                    if (hasFingerprints && isProfile) {
                        return R.string.unlock_disable_frp_warning_content_pin_fingerprint_profile;
                    }
                    if (hasFingerprints && !isProfile) {
                        return R.string.unlock_disable_frp_warning_content_pin_fingerprint;
                    }
                    if (isProfile) {
                        return R.string.unlock_disable_frp_warning_content_pin_profile;
                    }
                    return R.string.unlock_disable_frp_warning_content_pin;
                } else if (keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
                    if (hasFingerprints && isProfile) {
                        return R.string.unlock_disable_frp_warning_content_password_fingerprint_profile;
                    }
                    if (hasFingerprints && !isProfile) {
                        return R.string.unlock_disable_frp_warning_content_password_fingerprint;
                    }
                    if (isProfile) {
                        return R.string.unlock_disable_frp_warning_content_password_profile;
                    }
                    return R.string.unlock_disable_frp_warning_content_password;
                } else if (hasFingerprints && isProfile) {
                    return R.string.unlock_disable_frp_warning_content_unknown_fingerprint_profile;
                } else {
                    if (hasFingerprints && !isProfile) {
                        return R.string.unlock_disable_frp_warning_content_unknown_fingerprint;
                    }
                    if (isProfile) {
                        return R.string.unlock_disable_frp_warning_content_unknown_profile;
                    }
                    return R.string.unlock_disable_frp_warning_content_unknown;
                }
            } else if (hasFingerprints && isProfile) {
                return R.string.unlock_disable_frp_warning_content_pattern_fingerprint_profile;
            } else {
                if (hasFingerprints && !isProfile) {
                    return R.string.unlock_disable_frp_warning_content_pattern_fingerprint;
                }
                if (isProfile) {
                    return R.string.unlock_disable_frp_warning_content_pattern_profile;
                }
                return R.string.unlock_disable_frp_warning_content_pattern;
            }
        }

        private boolean isUnlockMethodSecure(String unlockMethod) {
            return (ScreenLockType.SWIPE.preferenceKey.equals(unlockMethod) || ScreenLockType.NONE.preferenceKey.equals(unlockMethod)) ? false : true;
        }

        private boolean setUnlockMethod(String unlockMethod) {
            EventLog.writeEvent(EventLogTags.LOCK_SCREEN_TYPE, unlockMethod);
            ScreenLockType lock = ScreenLockType.fromKey(unlockMethod);
            if (lock != null) {
                switch (lock) {
                    case NONE:
                    case SWIPE:
                        updateUnlockMethodAndFinish(lock.defaultQuality, lock == ScreenLockType.NONE, false);
                        return true;
                    case PATTERN:
                    case PIN:
                    case PASSWORD:
                    case MANAGED:
                        maybeEnableEncryption(lock.defaultQuality, false);
                        return true;
                }
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Encountered unknown unlock method to set: ");
            stringBuilder.append(unlockMethod);
            Log.e(str, stringBuilder.toString());
            return false;
        }

        private void showFactoryResetProtectionWarningDialog(String unlockMethodToSet) {
            FactoryResetProtectionWarningDialog.newInstance(getResIdForFactoryResetProtectionWarningTitle(), getResIdForFactoryResetProtectionWarningMessage(), unlockMethodToSet).show(getChildFragmentManager(), TAG_FRP_WARNING_DIALOG);
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, getFragmentClass().getName());
        String action = modIntent.getAction();
        if ("android.app.action.SET_NEW_PASSWORD".equals(action) || "android.app.action.SET_NEW_PARENT_PROFILE_PASSWORD".equals(action)) {
            modIntent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, true);
        }
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (ChooseLockGenericFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends Fragment> getFragmentClass() {
        return ChooseLockGenericFragment.class;
    }
}
