package com.oneplus.settings.ui;

import android.content.Context;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.service.vr.IVrManager.Stub;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.settings.R;
import com.android.settingslib.display.BrightnessUtils;

public class OPBrightnessSeekbarPreferenceCategory extends OPSeekbarPreferenceCategory implements OnSeekBarChangeListener {
    private static final String TAG = "OPBrightnessSeekbarPreferenceCategory";
    private boolean isManuallyTouchingSeekbar;
    private int mBrightness;
    private OPCallbackBrightness mCallback;
    private Context mContext;
    private int mDefaultBacklight;
    private int mDefaultBacklightForVr;
    private int mMaximumBacklight;
    private int mMaximumBacklightForVr;
    private int mMinimumBacklight;
    private int mMinimumBacklightForVr;
    private SeekBar mSeekBar;
    private int max;
    private int min;

    public interface OPCallbackBrightness {
        void onOPBrightValueChanged(int i, int i2);

        void onOPBrightValueStartTrackingTouch(int i);

        void saveBrightnessDataBase(int i);
    }

    public OPBrightnessSeekbarPreferenceCategory(Context context) {
        super(context);
        initView(context);
    }

    public OPBrightnessSeekbarPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPBrightnessSeekbarPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isInVrMode() {
        try {
            return Stub.asInterface(ServiceManager.getService("vrmanager")).getVrModeState();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check vr mode!", e);
            return false;
        }
    }

    private void initView(Context context) {
        int val;
        this.mContext = context;
        PowerManager pm = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
        this.mDefaultBacklight = pm.getDefaultScreenBrightnessSetting();
        this.mMinimumBacklightForVr = pm.getMinimumScreenBrightnessForVrSetting();
        this.mMaximumBacklightForVr = pm.getMaximumScreenBrightnessForVrSetting();
        this.mDefaultBacklightForVr = pm.getDefaultScreenBrightnessForVrSetting();
        boolean inVrMode = isInVrMode();
        if (inVrMode) {
            val = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_for_vr", this.mDefaultBacklightForVr, -2);
        } else {
            val = System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mDefaultBacklight, -2);
        }
        if (inVrMode) {
            this.min = this.mMinimumBacklightForVr;
            this.max = this.mMaximumBacklightForVr;
        } else {
            this.min = this.mMinimumBacklight;
            this.max = this.mMaximumBacklight;
        }
        this.mBrightness = BrightnessUtils.convertLinearToGamma(val, this.min, this.max);
    }

    public void setCallback(OPCallbackBrightness callback) {
        this.mCallback = callback;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSeekBar = (SeekBar) view.findViewById(R.id.opseekbar);
        this.mSeekBar.setMax(BrightnessUtils.GAMMA_SPACE_MAX);
        this.mSeekBar.setProgress(this.mBrightness);
        this.mSeekBar.setOnSeekBarChangeListener(this);
        view.setDividerAllowedAbove(false);
    }

    public void setBrightness(int brightness) {
        this.mBrightness = brightness;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("seekbar brightness after set : ");
        stringBuilder.append(this.mBrightness);
        Log.d(str, stringBuilder.toString());
        notifyChanged();
    }

    public int getBrightness() {
        return this.mBrightness;
    }

    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("seekbar progress arg1 : ");
        stringBuilder.append(arg1);
        stringBuilder.append(" mSeekBar.getProgress : ");
        stringBuilder.append(this.mSeekBar.getProgress());
        Log.d(str, stringBuilder.toString());
        if (this.isManuallyTouchingSeekbar) {
            this.mCallback.onOPBrightValueChanged(0, this.mSeekBar.getProgress());
            this.mBrightness = this.mSeekBar.getProgress();
        }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
        Log.d(TAG, "start tracking seekbar");
        this.isManuallyTouchingSeekbar = true;
        if (this.mCallback != null && this.mSeekBar != null) {
            this.mCallback.onOPBrightValueStartTrackingTouch(this.mSeekBar.getProgress());
        }
    }

    public void onStopTrackingTouch(SeekBar arg0) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("stop tracking seekbar ");
        stringBuilder.append(this.mSeekBar.getProgress());
        Log.d(str, stringBuilder.toString());
        this.isManuallyTouchingSeekbar = false;
        if (this.mCallback != null && this.mSeekBar != null) {
            this.mCallback.saveBrightnessDataBase(this.mSeekBar.getProgress());
        }
    }
}
