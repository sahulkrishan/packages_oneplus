package com.oneplus.settings.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class OPProgressDialog extends ProgressDialog {
    private static final int MSG_DELAYSHOW = 0;
    private static final int MSG_TIMEOUT = 1;
    public static final String TAG = "OPProgressDialog";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    msg.obj.show();
                    return;
                case 1:
                    if (OPProgressDialog.this.mTimeOutListener != null) {
                        OPProgressDialog.this.mTimeOutListener.onTimeOut(OPProgressDialog.this);
                        OPProgressDialog.this.dismiss();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private long mShowDelayTime = 0;
    private long mTimeOut = 0;
    private OnTimeOutListener mTimeOutListener = null;
    private Timer mTimer = null;

    public interface OnTimeOutListener {
        void onTimeOut(OPProgressDialog oPProgressDialog);
    }

    public void showDelay(long delayTime) {
        this.mShowDelayTime = delayTime;
        showDelay();
    }

    public void showDelay() {
        this.mHandler.removeMessages(0);
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = this;
        this.mHandler.sendMessageDelayed(msg, this.mShowDelayTime);
    }

    public void dismiss() {
        this.mHandler.removeMessages(0);
        super.dismiss();
    }

    public OPProgressDialog(Context context) {
        super(context);
    }

    public void setTimeOut(long t, OnTimeOutListener timeOutListener) {
        this.mTimeOut = t;
        if (timeOutListener != null) {
            this.mTimeOutListener = timeOutListener;
        }
    }

    public void setShowDelayTime(long showDelayTime) {
        this.mShowDelayTime = showDelayTime;
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    public void onStart() {
        super.onStart();
        if (this.mTimeOut > 0) {
            this.mTimer = new Timer();
            this.mTimer.schedule(new TimerTask() {
                public void run() {
                    Log.d(OPProgressDialog.TAG, "timerOutTast......");
                    OPProgressDialog.this.mHandler.sendEmptyMessage(1);
                }
            }, this.mTimeOut);
        }
    }
}
