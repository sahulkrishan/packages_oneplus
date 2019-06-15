package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class AirplaneModeEnabler {
    private static final int EVENT_SERVICE_STATE_CHANGED = 3;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange) {
            AirplaneModeEnabler.this.onAirplaneModeChanged();
        }
    };
    private final Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 3) {
                AirplaneModeEnabler.this.onAirplaneModeChanged();
            }
        }
    };
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private OnAirplaneModeChangedListener mOnAirplaneModeChangedListener;
    private PhoneStateIntentReceiver mPhoneStateReceiver;

    public interface OnAirplaneModeChangedListener {
        void onAirplaneModeChanged(boolean z);
    }

    public AirplaneModeEnabler(Context context, MetricsFeatureProvider metricsFeatureProvider, OnAirplaneModeChangedListener listener) {
        this.mContext = context;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mOnAirplaneModeChangedListener = listener;
        this.mPhoneStateReceiver = new PhoneStateIntentReceiver(this.mContext, this.mHandler);
        this.mPhoneStateReceiver.notifyServiceState(3);
    }

    public void resume() {
        this.mPhoneStateReceiver.registerIntent();
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
    }

    public void pause() {
        this.mPhoneStateReceiver.unregisterIntent();
        this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void setAirplaneModeOn(boolean enabling) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", enabling);
        if (this.mOnAirplaneModeChangedListener != null) {
            this.mOnAirplaneModeChangedListener.onAirplaneModeChanged(enabling);
        }
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", enabling);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void onAirplaneModeChanged() {
        if (this.mOnAirplaneModeChangedListener != null) {
            this.mOnAirplaneModeChangedListener.onAirplaneModeChanged(isAirplaneModeOn());
        }
    }

    public void setAirplaneMode(boolean isAirplaneModeOn) {
        if (!Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
            this.mMetricsFeatureProvider.action(this.mContext, 177, isAirplaneModeOn);
            setAirplaneModeOn(isAirplaneModeOn);
        }
    }

    public void setAirplaneModeInECM(boolean isECMExit, boolean isAirplaneModeOn) {
        if (isECMExit) {
            setAirplaneModeOn(isAirplaneModeOn);
        } else {
            onAirplaneModeChanged();
        }
    }

    public boolean isAirplaneModeOn() {
        return WirelessUtils.isAirplaneModeOn(this.mContext);
    }
}
