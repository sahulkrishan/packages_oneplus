package com.oneplus.settings.timer.timepower;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import com.android.settings.R;
import com.oneplus.settings.ui.OPTimerDialog;

public class OPPowerOffPromptActivity extends Activity {
    private static final int MSG_CANCEL = 1000;
    private static final String TAG = "OPPowerOffPromptActivity";
    private static final int TYPE_NEGATIVE = 2;
    private static final int TYPE_POSITIVE = 1;
    private OPTimerDialog alertDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (OPPowerOffPromptActivity.this.alertDialog != null) {
                Button n = OPPowerOffPromptActivity.this.alertDialog.getNButton();
                Button p = OPPowerOffPromptActivity.this.alertDialog.getPButton();
                int i = msg.what;
                if (i != 1000) {
                    switch (i) {
                        case 1:
                            if (OPPowerOffPromptActivity.this.mPositiveCount > 0) {
                                OPPowerOffPromptActivity.this.mPositiveCount = OPPowerOffPromptActivity.this.mPositiveCount - 1;
                                if (p != null) {
                                    OPPowerOffPromptActivity.this.alertDialog.setMessage(String.format(OPPowerOffPromptActivity.this.getResources().getString(R.string.oneplus_timer_shutdown_summary), new Object[]{Integer.valueOf(OPPowerOffPromptActivity.this.mPositiveCount)}));
                                }
                                if (OPPowerOffPromptActivity.this.mHandler != null) {
                                    OPPowerOffPromptActivity.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                                    return;
                                }
                                return;
                            } else if (p != null && !OPPowerOffPromptActivity.this.mStatus) {
                                if (p.isEnabled()) {
                                    i = OPPowerOffPromptActivity.this.mTelephonyManager.getCallState();
                                    if (i != 0) {
                                        String str = OPPowerOffPromptActivity.TAG;
                                        StringBuilder stringBuilder = new StringBuilder();
                                        stringBuilder.append("Cancel auto shutdown while phone state is:");
                                        stringBuilder.append(i);
                                        Log.d(str, stringBuilder.toString());
                                        OPPowerOffPromptActivity.this.cancel();
                                        return;
                                    }
                                    Log.d(OPPowerOffPromptActivity.TAG, "Perform auto shutdown");
                                    p.performClick();
                                    return;
                                }
                                p.setEnabled(true);
                                return;
                            } else {
                                return;
                            }
                        case 2:
                            if (OPPowerOffPromptActivity.this.mNegativeCount > 0) {
                                OPPowerOffPromptActivity.this.mNegativeCount = OPPowerOffPromptActivity.this.mNegativeCount - 1;
                                if (n != null) {
                                    n.setText(OPPowerOffPromptActivity.this.alertDialog.getTimeText((String) n.getText(), OPPowerOffPromptActivity.this.mNegativeCount));
                                }
                                OPPowerOffPromptActivity.this.mHandler.sendEmptyMessageDelayed(2, 1000);
                                return;
                            } else if (n == null) {
                                return;
                            } else {
                                if (n.isEnabled()) {
                                    n.performClick();
                                    return;
                                } else {
                                    n.setEnabled(true);
                                    return;
                                }
                            }
                        default:
                            return;
                    }
                }
                boolean access$300 = OPPowerOffPromptActivity.this.mResume;
            }
        }
    };
    private WakeLock mLock = null;
    private int mNegativeCount = 0;
    private int mPositiveCount = 60;
    private boolean mResume = false;
    private ProgressDialog mShutdownDialog;
    private boolean mStatus = false;
    private TelephonyManager mTelephonyManager;
    private WakeLock mWakeLock;
    private PowerManager pm = null;

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("time", this.mPositiveCount);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        if (savedInstanceState != null) {
            this.mPositiveCount = savedInstanceState.getInt("time");
        }
        getWindow().addFlags(1574016);
        super.onCreate(savedInstanceState);
        this.pm = (PowerManager) getSystemService("power");
        raiseScreenUp();
        this.alertDialog = new OPTimerDialog(this);
        this.alertDialog.setTitle(getResources().getString(R.string.oneplus_timer_shutdown_title));
        this.alertDialog.setMessage(String.format(getResources().getString(R.string.oneplus_timer_shutdown_summary), new Object[]{Integer.valueOf(this.mPositiveCount)}));
        this.alertDialog.setPositiveButton(getResources().getString(R.string.oneplus_timer_shutdown_position), new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (OPPowerOffPromptActivity.this.alertDialog != null) {
                    OPPowerOffPromptActivity.this.alertDialog.dismiss();
                    OPPowerOffPromptActivity.this.alertDialog = null;
                }
                if (!SystemProperties.getBoolean("sys.debug.watchdog", false)) {
                    OPPowerOffPromptActivity.this.showDialog(OPPowerOffPromptActivity.this);
                    Intent intent = new Intent(SettingsUtil.ACTION_POWER_CONFIRM_OP_OFF);
                    intent.addFlags(285212672);
                    OPPowerOffPromptActivity.this.sendBroadcast(intent);
                }
            }
        }, 60);
        this.alertDialog.setNegativeButton(getResources().getString(R.string.oneplus_timer_shutdown_nagative), new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                OPPowerOffPromptActivity.this.cancel();
            }
        }, 10);
        this.alertDialog.show();
        this.alertDialog.setButtonType(-1, this.mPositiveCount, true);
        this.mHandler.sendEmptyMessageDelayed(1, 200);
        this.mShutdownDialog = new ProgressDialog(this);
        this.mShutdownDialog.setMessage(getString(17040920));
        this.mShutdownDialog.setCancelable(false);
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code skipped:
            return;
     */
    private void showDialog(android.content.Context r2) {
        /*
        r1 = this;
        r0 = r1.isFinishing();
        if (r0 != 0) goto L_0x0017;
    L_0x0006:
        r0 = r1.isDestroyed();
        if (r0 == 0) goto L_0x000d;
    L_0x000c:
        goto L_0x0017;
    L_0x000d:
        r0 = r1.mShutdownDialog;
        if (r0 == 0) goto L_0x0016;
    L_0x0011:
        r0 = r1.mShutdownDialog;
        r0.show();
    L_0x0016:
        return;
    L_0x0017:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.timer.timepower.OPPowerOffPromptActivity.showDialog(android.content.Context):void");
    }

    private void dismissShutdownDialog() {
        if (this.mShutdownDialog != null) {
            this.mShutdownDialog.dismiss();
        }
    }

    private void cancel() {
        Intent intent = new Intent(SettingsUtil.ACTION_POWER_CANCEL_OP_OFF);
        intent.addFlags(285212672);
        sendBroadcast(intent);
        this.mStatus = true;
        this.alertDialog.dismiss();
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        this.mResume = true;
        acquireWakeLock();
    }

    public void finish() {
        super.finish();
        releaseWakeLock();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        this.mResume = false;
        if (this.alertDialog != null && this.alertDialog.isShowing()) {
            this.mHandler.obtainMessage(1000);
        }
        dismissShutdownDialog();
    }

    private void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(536870913, "TimepowerWakeLock");
            this.mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    private void raiseScreenUp() {
        this.pm = (PowerManager) getSystemService("power");
        this.mLock = this.pm.newWakeLock(805306374, "TimepowerWakeLock");
        this.mLock.acquire();
        this.mLock.release();
        this.mLock = null;
    }
}
