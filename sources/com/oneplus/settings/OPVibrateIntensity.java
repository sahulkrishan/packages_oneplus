package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.ui.OPListDialog;
import com.oneplus.settings.ui.OPListDialog.OnDialogListItemClickListener;
import com.oneplus.settings.utils.OPUtils;

public class OPVibrateIntensity extends SettingsPreferenceFragment implements OnDialogListItemClickListener, OnPreferenceChangeListener {
    private static final String KEY_INCOMING_CALL_VIBRATE_INTENSITY = "incoming_call_vibrate_intensity";
    private static final String KEY_NOTICE_VIBRATE_INTENSITY = "notice_vibrate_intensity";
    private static final String KEY_VIBRATE_ON_TOUCH_INTENSITY = "vibrate_on_touch_intensity";
    private static final String TAG = "OPVibrateIntensity";
    private static final int TOUCH_TAP_VIBRATE_DELAY = 10;
    private Context mContext;
    private String mCunrrentType = KEY_INCOMING_CALL_VIBRATE_INTENSITY;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 10) {
                View view = null;
                if (OPVibrateIntensity.this.mOPListDialog != null) {
                    view = OPVibrateIntensity.this.mOPListDialog.getRootContainer();
                }
                if (view == null) {
                    view = new View(OPVibrateIntensity.this.mContext);
                }
                view.performHapticFeedback(1);
                System.putInt(OPVibrateIntensity.this.getActivity().getContentResolver(), OPVibrateIntensity.KEY_VIBRATE_ON_TOUCH_INTENSITY, OPVibrateIntensity.this.mTempTouchTapIntensity);
            }
        }
    };
    private Preference mIncomingCallVibrateIntensityPreference;
    private Preference mNoticeVibrateIntensityPreference;
    private OPListDialog mOPListDialog;
    private int mTempTouchTapIntensity;
    private Preference mVibrateOnTouchIntensityPreference;
    private Vibrator mVibrator;
    private long[][] sNoticeVibrateIntensity = new long[][]{new long[]{-1, 0, 100, 150, 100, 1000, 100, 150, 100}, new long[]{-2, 0, 100, 150, 100, 1000, 100, 150, 100}, new long[]{-3, 0, 100, 150, 100, 1000, 100, 150, 100}};
    private long[][] sRepeatVibrateIntensity = new long[][]{new long[]{-1, 500, 1000, 1500, 1000}, new long[]{-2, 500, 1000, 1500, 1000}, new long[]{-3, 500, 1000, 1500, 1000}};
    private long[][] sTouchVibrateIntensity = new long[][]{new long[]{-1, 0, 10, 1000, 10}, new long[]{-2, 0, 10, 1000, 10}, new long[]{-3, 0, 10, 1000, 10}};
    private long[][] sVibrateIntensity = new long[][]{new long[]{-1, 500, 1000, 500}, new long[]{-2, 500, 1000, 500}, new long[]{-3, 500, 1000, 500}};
    private long[][] sVibratePatternrhythm = new long[][]{new long[]{-2, 0, 1000, 1000, 1000}, new long[]{-2, 0, 500, 250, 10, 1000, 500, 250, 10}, new long[]{-2, 0, 300, 400, 300, 400, 300, 1000, 300, 400, 300, 400, 300}, new long[]{-2, 0, 30, 80, 30, 80, 50, 180, 600, 1000, 30, 80, 30, 80, 50, 180, 600}, new long[]{-2, 0, 80, 200, 600, 150, 10, 1000, 80, 200, 600, 150, 10}};

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_vibrate_intensity);
        this.mContext = getActivity();
        this.mVibrator = (Vibrator) getActivity().getSystemService("vibrator");
        if (!(this.mVibrator == null || this.mVibrator.hasVibrator())) {
            this.mVibrator = null;
        }
        this.mIncomingCallVibrateIntensityPreference = findPreference(KEY_INCOMING_CALL_VIBRATE_INTENSITY);
        this.mNoticeVibrateIntensityPreference = findPreference(KEY_NOTICE_VIBRATE_INTENSITY);
        this.mVibrateOnTouchIntensityPreference = findPreference(KEY_VIBRATE_ON_TOUCH_INTENSITY);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrateOnTouchIntensityPreference.setVisible(false);
        }
        updateVibratePreferenceDescription(KEY_INCOMING_CALL_VIBRATE_INTENSITY, System.getInt(getActivity().getContentResolver(), KEY_INCOMING_CALL_VIBRATE_INTENSITY, 0));
        updateVibratePreferenceDescription(KEY_NOTICE_VIBRATE_INTENSITY, System.getInt(getActivity().getContentResolver(), KEY_NOTICE_VIBRATE_INTENSITY, 0));
        int value = System.getInt(getActivity().getContentResolver(), KEY_VIBRATE_ON_TOUCH_INTENSITY, 0);
        this.mTempTouchTapIntensity = value;
        updateVibratePreferenceDescription(KEY_VIBRATE_ON_TOUCH_INTENSITY, value);
    }

    private void updateVibratePreferenceDescription(String key, int value) {
        Preference modePreference = findPreference(key);
        if (modePreference != null) {
            modePreference.setSummary(this.mContext.getResources().getStringArray(R.array.vibrate_intensity)[value]);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        this.mOPListDialog = new OPListDialog(this.mContext, preference.getTitle(), this.mContext.getResources().getStringArray(R.array.vibrate_intensity_values), this.mContext.getResources().getStringArray(R.array.vibrate_intensity));
        this.mOPListDialog.setOnDialogListItemClickListener(this);
        if (KEY_INCOMING_CALL_VIBRATE_INTENSITY.equals(key)) {
            this.mOPListDialog.setVibrateLevelKey(KEY_INCOMING_CALL_VIBRATE_INTENSITY);
            this.mCunrrentType = KEY_INCOMING_CALL_VIBRATE_INTENSITY;
        } else if (KEY_NOTICE_VIBRATE_INTENSITY.equals(key)) {
            this.mOPListDialog.setVibrateLevelKey(KEY_NOTICE_VIBRATE_INTENSITY);
            this.mCunrrentType = KEY_NOTICE_VIBRATE_INTENSITY;
        } else if (KEY_VIBRATE_ON_TOUCH_INTENSITY.equals(key)) {
            this.mOPListDialog.setVibrateLevelKey(KEY_VIBRATE_ON_TOUCH_INTENSITY);
            this.mCunrrentType = KEY_VIBRATE_ON_TOUCH_INTENSITY;
        }
        this.mOPListDialog.show();
        return true;
    }

    public void OnDialogListItemClick(int value) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("OnDialogListItemClick--index:");
        stringBuilder.append(value);
        Log.d(str, stringBuilder.toString());
        if (KEY_INCOMING_CALL_VIBRATE_INTENSITY.equals(this.mCunrrentType) && this.mVibrator != null) {
            int modevalue = System.getInt(getActivity().getContentResolver(), "incoming_call_vibrate_mode", value);
            int i = 0;
            modevalue = modevalue > this.sVibratePatternrhythm.length - 1 ? 0 : modevalue;
            this.mVibrator.cancel();
            if (value == 0) {
                this.sVibratePatternrhythm[modevalue][0] = -1;
            } else if (value == 1) {
                this.sVibratePatternrhythm[modevalue][0] = -2;
            } else if (value == 2) {
                this.sVibratePatternrhythm[modevalue][0] = -3;
            }
            while (true) {
                int i2 = i;
                if (i2 < this.sVibratePatternrhythm[modevalue].length) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("sVibratePatternrhythm [");
                    stringBuilder2.append(modevalue);
                    stringBuilder2.append("][");
                    stringBuilder2.append(i2);
                    stringBuilder2.append("] = ");
                    stringBuilder2.append(this.sVibratePatternrhythm[modevalue][i2]);
                    Log.d(str2, stringBuilder2.toString());
                    i = i2 + 1;
                } else {
                    this.mVibrator.vibrate(this.sVibratePatternrhythm[modevalue], -1);
                    return;
                }
            }
        } else if (KEY_NOTICE_VIBRATE_INTENSITY.equals(this.mCunrrentType) && this.mVibrator != null) {
            this.mVibrator.cancel();
            this.mVibrator.vibrate(this.sNoticeVibrateIntensity[value], -1);
        } else if (KEY_VIBRATE_ON_TOUCH_INTENSITY.equals(this.mCunrrentType) && this.mVibrator != null) {
            if (OPUtils.isSupportZVibrationMotor()) {
                System.putInt(getActivity().getContentResolver(), KEY_VIBRATE_ON_TOUCH_INTENSITY, value);
                this.mHandler.removeMessages(10);
                this.mHandler.sendEmptyMessageDelayed(10, 100);
                return;
            }
            this.mVibrator.cancel();
            this.mVibrator.vibrate(this.sTouchVibrateIntensity[value], -1);
        }
    }

    public void OnDialogListConfirmClick(int value) {
        if (KEY_INCOMING_CALL_VIBRATE_INTENSITY.equals(this.mCunrrentType)) {
            System.putInt(getActivity().getContentResolver(), KEY_INCOMING_CALL_VIBRATE_INTENSITY, value);
            updateVibratePreferenceDescription(KEY_INCOMING_CALL_VIBRATE_INTENSITY, value);
        } else if (KEY_NOTICE_VIBRATE_INTENSITY.equals(this.mCunrrentType) && this.mVibrator != null) {
            System.putInt(getActivity().getContentResolver(), KEY_NOTICE_VIBRATE_INTENSITY, value);
            updateVibratePreferenceDescription(KEY_NOTICE_VIBRATE_INTENSITY, value);
        } else if (KEY_VIBRATE_ON_TOUCH_INTENSITY.equals(this.mCunrrentType) && this.mVibrator != null) {
            System.putInt(getActivity().getContentResolver(), KEY_VIBRATE_ON_TOUCH_INTENSITY, value);
            updateVibratePreferenceDescription(KEY_VIBRATE_ON_TOUCH_INTENSITY, value);
        }
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
    }

    public void OnDialogListCancelClick() {
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
