package com.oneplus.settings.faceunlock;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.faceunlock.internal.IOPFaceSettingService;
import com.oneplus.faceunlock.internal.IOPFaceSettingService.Stub;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class OPFaceUnlockSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    protected static final int CHOOSE_LOCK_GENERIC_REQUEST = 3;
    private static final int GOTO_ADD_FACEUNLOCK_REQUEST = 5;
    private static final int GOTO_FACEUNLOCK_PAGE_REQUEST = 4;
    private static final String KEY_ADD_FACE = "key_add_face";
    private static final String KEY_AUTO_FACE_UNLOCK_ENABLE = "key_auto_face_unlock_enable";
    private static final String KEY_FACEUNLOCK_CATEGORY = "key_faceunlock_category";
    private static final String KEY_FACEUNLOCK_MANAGEMENT_CATEGORY = "key_faceunlock_management_category";
    private static final String KEY_FACE_UNLOCK_ASSISTIVE_LIGHTING = "key_face_unlock_assistive_lighting";
    private static final String KEY_FACE_UNLOCK_ENABLE = "key_face_unlock_enable";
    private static final String KEY_FACE_UNLOCK_MODE = "key_face_unlock_mode";
    private static final String KEY_REMOVE_FACE = "key_remove_face";
    public static final String ONEPLUS_AUTO_FACE_UNLOCK_ENABLE = "oneplus_auto_face_unlock_enable";
    private static final String ONEPLUS_FACE_UNLOCK_ASSISTIVE_LIGHTING_ENABLE = "oneplus_face_unlock_assistive_lighting_enable";
    private static final String ONEPLUS_FACE_UNLOCK_ENABLE = "oneplus_face_unlock_enable";
    private static final String ONEPLUS_FACE_UNLOCK_ENROLL_ACTION = "com.oneplus.faceunlock.FaceUnlockActivity";
    private static final int REFRESH_UI = 100;
    private static final int RESULT_FAIL = 1;
    private static final int RESULT_NOT_FOUND = 2;
    private static final int RESULT_OK = 0;
    private static final String TAG = "OPFaceUnlockSettings";
    private Preference mAddFace;
    private SwitchPreference mAutoFaceUnlock;
    private IOPFaceSettingService mFaceSettingService;
    private SwitchPreference mFaceUnlock;
    private SwitchPreference mFaceUnlockAssistiveLighting;
    private PreferenceCategory mFaceUnlockCategory;
    private ServiceConnection mFaceUnlockConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(OPFaceUnlockSettings.TAG, "Oneplus face unlock service connected");
            OPFaceUnlockSettings.this.mFaceSettingService = Stub.asInterface(service);
            OPFaceUnlockSettings.this.mUIHandler.sendEmptyMessage(100);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(OPFaceUnlockSettings.TAG, "Oneplus face unlock service disconnected");
            OPFaceUnlockSettings.this.mFaceSettingService = null;
        }
    };
    private PreferenceCategory mFaceUnlockManagerCategory;
    private Preference mFaceUnlockMode;
    public boolean mPasswordConfirmed = false;
    private AlertDialog mRemoveDialog;
    private Preference mRemoveFace;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                OPFaceUnlockSettings.this.refreshUI();
            }
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_faceunlock_settings);
        initView();
        bindFaceUnlockService();
        launchChooseOrConfirmLock(4);
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

    private void initView() {
        this.mFaceUnlockCategory = (PreferenceCategory) findPreference(KEY_FACEUNLOCK_CATEGORY);
        this.mFaceUnlockManagerCategory = (PreferenceCategory) findPreference(KEY_FACEUNLOCK_MANAGEMENT_CATEGORY);
        this.mAddFace = findPreference(KEY_ADD_FACE);
        this.mAddFace.setOnPreferenceClickListener(this);
        this.mRemoveFace = findPreference(KEY_REMOVE_FACE);
        this.mRemoveFace.setOnPreferenceClickListener(this);
        this.mFaceUnlock = (SwitchPreference) findPreference(KEY_FACE_UNLOCK_ENABLE);
        this.mFaceUnlock.setOnPreferenceChangeListener(this);
        this.mAutoFaceUnlock = (SwitchPreference) findPreference(KEY_AUTO_FACE_UNLOCK_ENABLE);
        this.mAutoFaceUnlock.setOnPreferenceChangeListener(this);
        this.mFaceUnlockAssistiveLighting = (SwitchPreference) findPreference(KEY_FACE_UNLOCK_ASSISTIVE_LIGHTING);
        this.mFaceUnlockAssistiveLighting.setOnPreferenceChangeListener(this);
        this.mFaceUnlockMode = findPreference(KEY_FACE_UNLOCK_MODE);
        if (OPUtils.isSupportXCamera()) {
            this.mAutoFaceUnlock.setVisible(false);
            this.mFaceUnlockAssistiveLighting.setVisible(false);
            return;
        }
        this.mFaceUnlockMode.setVisible(false);
    }

    public void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        if (isFaceAdded()) {
            this.mFaceUnlockCategory.addPreference(this.mRemoveFace);
            this.mFaceUnlockCategory.removePreference(this.mAddFace);
            this.mFaceUnlock.setEnabled(true);
            disableAutoUnlockSettings(this.mFaceUnlock.isChecked());
            disableFaceUnlockAssistiveLightingSettings(this.mFaceUnlock.isChecked());
            disableFaceUnlockModeSettings(this.mFaceUnlock.isChecked());
        } else {
            this.mFaceUnlockCategory.addPreference(this.mAddFace);
            this.mFaceUnlockCategory.removePreference(this.mRemoveFace);
            this.mFaceUnlock.setEnabled(false);
            this.mAutoFaceUnlock.setEnabled(false);
            if (this.mFaceUnlockAssistiveLighting != null) {
                this.mFaceUnlockAssistiveLighting.setEnabled(false);
            }
            this.mFaceUnlockMode.setEnabled(false);
        }
        this.mFaceUnlock.setChecked(isFaceUnlockEnabled());
        this.mAutoFaceUnlock.setChecked(isAutoFaceUnlockEnabled());
        if (this.mFaceUnlockAssistiveLighting != null) {
            this.mFaceUnlockAssistiveLighting.setChecked(isFaceUnlockAssistiveLightingEnabled());
        }
        if (this.mFaceUnlockMode != null) {
            CharSequence string;
            int value = System.getInt(getActivity().getContentResolver(), OPFaceUnlockModeSettings.ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 0);
            Preference preference = this.mFaceUnlockMode;
            if (value == 1) {
                string = getActivity().getString(R.string.oneplus_face_auto_unlock_while_screen_on_title);
            } else {
                string = getActivity().getString(R.string.oneplus_face_unlock_choose_swipe_up_mode);
            }
            preference.setSummary(string);
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        unbindFaceUnlockService();
        super.onDestroy();
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (KEY_ADD_FACE.equals(key)) {
            if (OPUtils.isFaceUnlockEnabled(getActivity())) {
                showDisableAospFaceUnlockDialog();
            } else {
                gotoAddFaceData();
            }
            return true;
        } else if (!KEY_REMOVE_FACE.equals(key)) {
            return false;
        } else {
            showRemoveFaceDataDialog();
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        String key = preference.getKey();
        boolean newValue = ((Boolean) obj).booleanValue();
        if (KEY_FACE_UNLOCK_ENABLE.equals(key)) {
            switchFaceUnlock(newValue);
            return true;
        } else if (KEY_AUTO_FACE_UNLOCK_ENABLE.equals(key)) {
            switchAutoFaceUnlock(newValue);
            return true;
        } else if (!KEY_FACE_UNLOCK_ASSISTIVE_LIGHTING.equals(key)) {
            return false;
        } else {
            switchFaceUnlockAssistiveLighting(newValue);
            return true;
        }
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

    private void removeFaceData() {
        if (this.mFaceSettingService != null) {
            try {
                this.mFaceSettingService.removeFace(0);
                this.mUIHandler.sendEmptyMessage(100);
            } catch (RemoteException re) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Start remove face RemoteException:");
                stringBuilder.append(re);
                Log.i(str, stringBuilder.toString());
            }
        }
    }

    private void gotoAddFaceData() {
        Intent intent = null;
        try {
            intent = new Intent();
            intent.setAction("com.oneplus.settings.FACEUNLOCK_INTRODUCTION");
            startActivityForResult(intent, 5);
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
        }
    }

    public static Intent getSetupFaceUnlockIntent(Context context) {
        Intent intent = new Intent();
        intent.setClassName(OPConstants.PACKAGENAME_FACE_UNLOCK, ONEPLUS_FACE_UNLOCK_ENROLL_ACTION);
        intent.putExtra("FaceUnlockActivity.StartMode", 1);
        intent.putExtra("FaceUnlockActivity.SkipFingerprint", 1);
        intent.putExtra("FaceUnlockActivity.SkipIntroduction", 1);
        return intent;
    }

    public void showDisableAospFaceUnlockDialog() {
        new Builder(getActivity()).setMessage(R.string.oneplus_disable_aosp_face_lock_message).setPositiveButton(R.string.security_settings_fingerprint_enroll_introduction_continue, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                OPUtils.disableAospFaceUnlock(OPFaceUnlockSettings.this.getActivity());
                OPFaceUnlockSettings.this.gotoAddFaceData();
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    public void showRemoveFaceDataDialog() {
        this.mRemoveDialog = new Builder(getActivity()).setTitle(R.string.oneplus_face_unlock_remove_dialog_title).setMessage(R.string.oneplus_face_unlock_remove_dialog_message).setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                OPFaceUnlockSettings.this.removeFaceData();
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        this.mRemoveDialog.show();
    }

    private void disableAutoUnlockSettings(boolean enabled) {
        if (this.mAutoFaceUnlock != null) {
            this.mAutoFaceUnlock.setEnabled(enabled);
        }
    }

    private void disableFaceUnlockAssistiveLightingSettings(boolean enabled) {
        if (this.mFaceUnlockAssistiveLighting != null) {
            this.mFaceUnlockAssistiveLighting.setEnabled(enabled);
        }
    }

    private void disableFaceUnlockModeSettings(boolean enabled) {
        if (this.mFaceUnlockMode != null) {
            this.mFaceUnlockMode.setEnabled(enabled);
        }
    }

    private boolean isFaceUnlockEnabled() {
        return System.getInt(getContentResolver(), ONEPLUS_FACE_UNLOCK_ENABLE, 0) == 1;
    }

    private void switchFaceUnlock(boolean enabled) {
        System.putInt(getContentResolver(), ONEPLUS_FACE_UNLOCK_ENABLE, enabled);
        disableAutoUnlockSettings(enabled);
        disableFaceUnlockAssistiveLightingSettings(enabled);
        disableFaceUnlockModeSettings(enabled);
    }

    private boolean isAutoFaceUnlockEnabled() {
        return System.getInt(getContentResolver(), ONEPLUS_AUTO_FACE_UNLOCK_ENABLE, 0) == 1;
    }

    private void switchAutoFaceUnlock(boolean enabled) {
        System.putInt(getContentResolver(), ONEPLUS_AUTO_FACE_UNLOCK_ENABLE, enabled);
        OPUtils.sendAppTracker("auto_face_unlock", (int) enabled);
    }

    private boolean isFaceUnlockAssistiveLightingEnabled() {
        return System.getInt(getContentResolver(), ONEPLUS_FACE_UNLOCK_ASSISTIVE_LIGHTING_ENABLE, 0) == 1;
    }

    private void switchFaceUnlockAssistiveLighting(boolean enabled) {
        System.putInt(getContentResolver(), ONEPLUS_FACE_UNLOCK_ASSISTIVE_LIGHTING_ENABLE, enabled);
    }

    public static void gotoFaceUnlockSettings(Context context) {
        Intent intent = null;
        try {
            intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$OPFaceUnlockSettings");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No activity found for ");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
        }
    }

    private void launchChooseOrConfirmLock(int requestCode) {
        long challenge = Utils.getFingerprintManagerOrNull(getActivity()).preEnroll();
        Intent intent = new Intent();
        if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(requestCode, getActivity().getString(R.string.op_security_lock_settings_title), null, null, challenge)) {
            intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
            intent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
            intent.putExtra(ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
            startActivityForResult(intent, 3);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (5 == requestCode) {
            if (isFaceAdded() && OPUtils.isSupportXCamera()) {
                Intent i = new Intent();
                i.setClassName("com.android.settings", "com.oneplus.settings.faceunlock.OPFaceUnlockModeSettingsActivity");
                startActivity(i);
            }
            return;
        }
        if (!((requestCode == 4 || requestCode == 3) && (resultCode == 1 || resultCode == -1))) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
