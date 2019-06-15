package com.oneplus.settings.ringtone;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.Ringtone;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserManager;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.Utils;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.utils.OPVibrateUtils;

public abstract class OPRingtoneBaseActivity extends PreferenceActivity implements Runnable {
    public static final String ALARM_NEED_VIBRATE = "needVibrate";
    private static final int CHECK_RINGTONE_PLAY_STATUS = 1;
    public static final int DELAY_MS_SELECTION_PLAYED = 300;
    private static final int DELAY_PLAY_RINGTONE = 2;
    public static final String KEY_SELECTED_ITEM_URI = "key_selected_item_uri";
    private static final String TAG = "RingtoneBaseActivity";
    private boolean isFirst = true;
    private boolean isPlaying = false;
    protected boolean isSelectedNone = false;
    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == -2 && OPRingtoneBaseActivity.this.isPlaying) {
                OPRingtoneBaseActivity.this.stopAnyPlayingRingtone();
            }
        }
    };
    private AudioManager mAudioManager;
    public boolean mContactsRingtone = false;
    private Ringtone mDefaultRingtone;
    public Uri mDefualtUri;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (OPRingtoneBaseActivity.this.mDefaultRingtone == null) {
                        return;
                    }
                    if (OPRingtoneBaseActivity.this.mDefaultRingtone.isPlaying()) {
                        sendEmptyMessageDelayed(1, 1000);
                        return;
                    }
                    OPRingtoneBaseActivity.this.stopVibrate();
                    Log.d(OPRingtoneBaseActivity.TAG, "Ringtone play stoped, stop vibrate");
                    return;
                case 2:
                    Log.d(OPRingtoneBaseActivity.TAG, "OPRingtoneBaseActivity play ringtone delay");
                    if (OPRingtoneBaseActivity.this.mDefaultRingtone != null) {
                        OPRingtoneBaseActivity.this.mDefaultRingtone.stop();
                        OPRingtoneBaseActivity.this.mDefaultRingtone.play();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    public boolean mHasDefaultItem;
    public boolean mIsAlarmNeedVibrate = false;
    private final Runnable mLookupRingtoneNames = new Runnable() {
        public void run() {
            if (OPRingtoneBaseActivity.this.mSimid == 1) {
                OPRingtoneBaseActivity.this.mUriForDefaultItem = OPRingtoneManager.getActualRingtoneUriBySubId(OPRingtoneBaseActivity.this.getApplicationContext(), 0);
            } else if (OPRingtoneBaseActivity.this.mSimid == 2) {
                OPRingtoneBaseActivity.this.mUriForDefaultItem = OPRingtoneManager.getActualRingtoneUriBySubId(OPRingtoneBaseActivity.this.getApplicationContext(), 1);
            } else {
                OPRingtoneBaseActivity.this.mUriForDefaultItem = OPRingtoneManager.getActualDefaultRingtoneUri(OPRingtoneBaseActivity.this.getUserContext(), OPRingtoneBaseActivity.this.mType);
            }
            OPRingtoneBaseActivity.this.mHandler.post(new Runnable() {
                public void run() {
                    OPRingtoneBaseActivity.this.updateSelected();
                }
            });
        }
    };
    private PhoneCallListener mPhoneCallListener;
    public OPRingtoneManager mRingtoneManager;
    private int mSimid = 0;
    private TelephonyManager mTelephonyManager;
    public int mType = 1;
    public Uri mUriForDefaultItem;
    private Context mUserContext;
    private Vibrator mVibrator;

    class PhoneCallListener extends PhoneStateListener {
        PhoneCallListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 1:
                    Log.d(OPRingtoneBaseActivity.TAG, "PhoneCallListener-CALL_STATE_RINGING--stopAnyPlayingRingtone");
                    OPRingtoneBaseActivity.this.stopAnyPlayingRingtone();
                    return;
                default:
                    return;
            }
        }
    }

    public abstract void updateSelected();

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle bundle) {
        if (this.mUriForDefaultItem != null) {
            bundle.putString(KEY_SELECTED_ITEM_URI, this.mUriForDefaultItem.toString());
        }
        super.onSaveInstanceState(bundle);
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Bundle bundle) {
        String mUriForDefaultItemStr = bundle.getString(KEY_SELECTED_ITEM_URI);
        if (mUriForDefaultItemStr != null) {
            this.mUriForDefaultItem = Uri.parse(mUriForDefaultItemStr);
        }
        super.onRestoreInstanceState(bundle);
    }

    public Context getUserContext() {
        return this.mUserContext;
    }

    public boolean isProfileId() {
        try {
            return Utils.isManagedProfile(UserManager.get(getApplicationContext()), this.mUserContext.getUserId());
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isManagedProfile :");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        String mUriForDefaultItemStr;
        super.onCreate(savedInstanceState);
        OPUtils.setLightNavigationBar(getWindow(), OPUtils.getThemeMode(getContentResolver()));
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        Intent intent = getIntent();
        this.mUserContext = Utils.createPackageContextAsUser(getApplicationContext(), intent.getIntExtra("CURRENT_USER_ID", 0));
        if (this.mUserContext == null) {
            Log.w(TAG, "use application context instead");
            this.mUserContext = getApplicationContext();
        }
        this.mType = intent.getIntExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, 1);
        this.mIsAlarmNeedVibrate = intent.getBooleanExtra(ALARM_NEED_VIBRATE, false);
        this.mHasDefaultItem = intent.getBooleanExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        this.mContactsRingtone = intent.getBooleanExtra(OPRingtoneManager.EXTRA_RINGTONE_FOR_CONTACTS, false);
        this.mDefualtUri = (Uri) intent.getParcelableExtra(OPRingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        if (this.mUriForDefaultItem == null) {
            if (savedInstanceState != null) {
                mUriForDefaultItemStr = savedInstanceState.getString(KEY_SELECTED_ITEM_URI);
                if (mUriForDefaultItemStr != null) {
                    this.mUriForDefaultItem = Uri.parse(mUriForDefaultItemStr);
                }
            }
            if (this.mUriForDefaultItem == null && !this.isSelectedNone) {
                this.mUriForDefaultItem = (Uri) intent.getParcelableExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
            }
        }
        mUriForDefaultItemStr = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mDefualtUri:");
        stringBuilder.append(this.mDefualtUri);
        Log.d(mUriForDefaultItemStr, stringBuilder.toString());
        mUriForDefaultItemStr = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("mUriForDefaultItem:");
        stringBuilder.append(this.mUriForDefaultItem);
        Log.d(mUriForDefaultItemStr, stringBuilder.toString());
        mUriForDefaultItemStr = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("mHasDefaultItem:");
        stringBuilder.append(this.mHasDefaultItem);
        Log.d(mUriForDefaultItemStr, stringBuilder.toString());
        CharSequence title = intent.getCharSequenceExtra(OPRingtoneManager.EXTRA_RINGTONE_TITLE);
        this.mSimid = intent.getIntExtra(OPRingtoneManager.EXTRA_RINGTONE_SIMID, 0);
        if (title == null) {
            if (this.mSimid == 1) {
                title = getString(R.string.oneplus_sim1_ringtone_switch);
            } else if (this.mSimid == 2) {
                title = getString(R.string.oneplus_sim2_ringtone_switch);
            } else {
                title = getString(17040835);
            }
        }
        this.mRingtoneManager = new OPRingtoneManager((Activity) this);
        this.mRingtoneManager.setType(this.mType);
        setVolumeControlStream(this.mRingtoneManager.inferStreamType());
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mPhoneCallListener = new PhoneCallListener();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void playRingtone(int delayMs, Uri uri) {
        this.mHandler.removeCallbacks(this);
        this.mUriForDefaultItem = uri;
        this.mHandler.postDelayed(this, (long) delayMs);
    }

    public void run() {
        stopAnyPlayingRingtone2();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mUriForDefaultItem:");
        stringBuilder.append(this.mUriForDefaultItem);
        OPMyLog.d(str, stringBuilder.toString());
        if (this.mUriForDefaultItem != null) {
            try {
                this.mDefaultRingtone = OPRingtoneManager.getRingtone(this, this.mUriForDefaultItem);
                this.mDefaultRingtone.setStreamType(this.mRingtoneManager.inferStreamType());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.mDefaultRingtone != null) {
                if (!this.isPlaying) {
                    this.isPlaying = true;
                    this.mAudioManager.requestAudioFocus(this.mAudioFocusChangeListener, this.mRingtoneManager.inferStreamType(), 2);
                }
                startPreview();
            }
        }
    }

    private void startPreview() {
        startVibrate(this.mUriForDefaultItem);
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 200);
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        this.mHandler.removeCallbacks(this);
        this.mHandler.removeMessages(2);
        stopAnyPlayingRingtone();
        this.mTelephonyManager.listen(this.mPhoneCallListener, 0);
        this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
    }

    private int getVibrateLevel(int levelValue) {
        switch (levelValue) {
            case 0:
                return -1;
            case 1:
                return -2;
            case 2:
                return -3;
            default:
                return -2;
        }
    }

    /* Access modifiers changed, original: protected */
    public void startVibrate(Uri ringtoneUri) {
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
        int i = this.mType;
        if (i != 4) {
            if (i != 8) {
                switch (i) {
                    case 1:
                        OPVibrateUtils.startVibrateForRingtone(this.mUserContext, ringtoneUri, this.mVibrator);
                        break;
                    case 2:
                        OPVibrateUtils.startVibrateForNotification(this.mUserContext, ringtoneUri, this.mVibrator);
                        break;
                }
            }
            OPVibrateUtils.startVibrateForSms(this.mUserContext, ringtoneUri, this.mVibrator);
        } else if (this.mIsAlarmNeedVibrate) {
            OPVibrateUtils.startVibrateForAlarm(this.mUserContext, ringtoneUri, this.mVibrator);
        }
        if (OPVibrateUtils.isThreeKeyRingMode(getApplicationContext())) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, 500);
        }
    }

    /* Access modifiers changed, original: protected */
    public void stopVibrate() {
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator.cancel();
        }
    }

    /* Access modifiers changed, original: protected */
    public void stopAnyPlayingRingtone() {
        stopAnyPlayingRingtone2();
        stopVibrate();
        this.isPlaying = false;
        this.mAudioManager.abandonAudioFocus(null);
    }

    private void stopAnyPlayingRingtone2() {
        String name = OPRingtoneBaseActivity.class.getName();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("stopAnyPlayingRingtone2 mDefaultRingtone = ");
        stringBuilder.append(this.mDefaultRingtone);
        Log.v(name, stringBuilder.toString());
        if (this.mDefaultRingtone != null) {
            this.mDefaultRingtone.stop();
            this.mDefaultRingtone = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        if (!this.isFirst) {
            lookupRingtoneNames();
        }
        this.isFirst = false;
        this.mTelephonyManager.listen(this.mPhoneCallListener, 32);
    }

    private void lookupRingtoneNames() {
        if (!isThreePart() && !this.mContactsRingtone) {
            AsyncTask.execute(this.mLookupRingtoneNames);
        }
    }

    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_PICKED_URI, this.mUriForDefaultItem);
        setResult(-1, resultIntent);
        super.onBackPressed();
    }

    public boolean isThreePart() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mHasDefaultItem:");
        stringBuilder.append(this.mHasDefaultItem);
        stringBuilder.append(" mType:");
        stringBuilder.append(this.mType);
        OPMyLog.d("", stringBuilder.toString());
        return this.mType == 4 || this.mHasDefaultItem;
    }

    public boolean isMultiSimEnabled() {
        return this.mTelephonyManager.isMultiSimEnabled();
    }

    public int getSimId() {
        return this.mSimid;
    }

    public int getCountOfSim() {
        int count = 0;
        if (!isMultiSimEnabled()) {
            return 1;
        }
        if (this.mTelephonyManager.hasIccCard(0)) {
            count = 0 + 1;
        }
        if (this.mTelephonyManager.hasIccCard(1)) {
            count++;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getCountOfSim:");
        stringBuilder.append(count);
        OPMyLog.d(str, stringBuilder.toString());
        return count;
    }

    public boolean getSim1Enable() {
        return this.mTelephonyManager.hasIccCard(0);
    }

    public boolean getSim2Enable() {
        return this.mTelephonyManager.hasIccCard(1);
    }
}
