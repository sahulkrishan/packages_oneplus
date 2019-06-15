package com.oneplus.settings.timer.timepower;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.R;

public class OPShutdownActivity extends Activity {
    private static final int DIALOG = 1;
    private static final String TAG = "ShutdownActivity";
    public static CountDownTimer sCountDownTimer = null;
    private String mMessage;
    private int mSecondsCountdown;
    private TelephonyManager mTelephonyManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService("power");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("screen is on ? ----- ");
        stringBuilder.append(pm.isScreenOn());
        Log.d(str, stringBuilder.toString());
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        getWindow().addFlags(4718592);
        if (savedInstanceState == null) {
            this.mSecondsCountdown = 11;
        } else {
            this.mSecondsCountdown = savedInstanceState.getInt("lefttime");
            this.mMessage = savedInstanceState.getString("message");
        }
        sCountDownTimer = new CountDownTimer((long) (this.mSecondsCountdown * 1000), 1000) {
            public void onTick(long millisUntilFinished) {
                OPShutdownActivity.this.mSecondsCountdown = (int) (millisUntilFinished / 1000);
                if (OPShutdownActivity.this.mSecondsCountdown > 1) {
                    OPShutdownActivity.this.mMessage = OPShutdownActivity.this.getString(R.string.oneplus_shutdown_message, new Object[]{Integer.valueOf(OPShutdownActivity.this.mSecondsCountdown)});
                } else {
                    OPShutdownActivity.this.mMessage = OPShutdownActivity.this.getString(R.string.oneplus_shutdown_message_second, new Object[]{Integer.valueOf(OPShutdownActivity.this.mSecondsCountdown)});
                }
                String str = OPShutdownActivity.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("showDialog time = ");
                stringBuilder.append(millisUntilFinished / 1000);
                Log.d(str, stringBuilder.toString());
                OPShutdownActivity.this.showDialog(1);
            }

            public void onFinish() {
                if (OPShutdownActivity.this.mTelephonyManager.getCallState() != 0) {
                    Log.d(OPShutdownActivity.TAG, "phone is incall, countdown end");
                    OPShutdownActivity.this.finish();
                    return;
                }
                Log.d(OPShutdownActivity.TAG, "count down timer arrived, shutdown phone");
                OPShutdownActivity.this.fireShutDown();
                OPShutdownActivity.sCountDownTimer = null;
            }
        };
        Log.d(TAG, "ShutdonwActivity onCreate");
        if (sCountDownTimer == null) {
            finish();
        } else {
            sCountDownTimer.start();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lefttime", this.mSecondsCountdown);
        outState.putString("message", this.mMessage);
    }

    private void cancelCountDownTimer() {
        if (sCountDownTimer != null) {
            Log.d(TAG, "cancel sCountDownTimer");
            sCountDownTimer.cancel();
            sCountDownTimer = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public Dialog onCreateDialog(int id) {
        Log.d(TAG, "onCreateDialog");
        return new Builder(this).setCancelable(false).setIcon(17301543).setTitle("power off").setMessage(this.mMessage).setPositiveButton(17039379, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                OPShutdownActivity.this.cancelCountDownTimer();
                OPShutdownActivity.this.fireShutDown();
            }
        }).setNegativeButton(17039369, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                OPShutdownActivity.this.cancelCountDownTimer();
                OPShutdownActivity.this.finish();
            }
        }).create();
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialog(int id, Dialog dialog) {
        ((AlertDialog) dialog).setMessage(this.mMessage);
    }

    private void fireShutDown() {
        if (!SystemProperties.getBoolean("sys.debug.watchdog", false)) {
            Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(8388608);
            intent.setFlags(268435456);
            startActivity(intent);
        }
    }
}
