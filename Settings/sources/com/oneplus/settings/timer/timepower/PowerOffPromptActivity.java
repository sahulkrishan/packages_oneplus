package com.oneplus.settings.timer.timepower;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;

public class PowerOffPromptActivity extends AlertActivity implements OnClickListener {
    static final String ACTION_TIMER_SHUTDOWN = "com.android.settings.Shutdown";
    static final String ACTION_TIMER_SHUTDOWN_LOCKED = "com.android.settings.ShutdownWhenLocked";
    private static final String EXTRA_IS_MISSED_REQUEST = "isMissedRequest";
    private static final int MSG_SHUTDOWN_NOW = 1;
    private static final String TAG = "PowerOffPromptActivity";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                PowerOffPromptActivity.this.beginShutdown();
            }
        }
    };
    private boolean mIsCurrentLocked = false;
    private WakeLock mLock = null;
    private final Runnable mMoveToFrontRunnable = new Runnable() {
        public void run() {
            ActivityManager am = (ActivityManager) PowerOffPromptActivity.this.getSystemService("activity");
            ComponentName cn = ((RunningTaskInfo) am.getRunningTasks(1).get(0)).topActivity;
            String pkg = cn.getPackageName();
            String cls = cn.getClassName();
            String str = PowerOffPromptActivity.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("pkg:");
            stringBuilder.append(pkg);
            Log.d(str, stringBuilder.toString());
            str = PowerOffPromptActivity.TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("cls:");
            stringBuilder.append(cls);
            Log.d(str, stringBuilder.toString());
            str = PowerOffPromptActivity.TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("taskId:");
            stringBuilder.append(PowerOffPromptActivity.this.getTaskId());
            Log.d(str, stringBuilder.toString());
            if (!cls.equals(NewStylePowerOffPromptActivity.class.getName())) {
                am.moveTaskToFront(PowerOffPromptActivity.this.getTaskId(), 1);
            }
        }
    };
    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                PowerOffPromptActivity.this.mHandler.sendEmptyMessageDelayed(1, 60000);
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING) || state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                PowerOffPromptActivity.this.mHandler.removeMessages(1);
            }
        }
    };
    private PowerManager pm = null;

    public static class NewStylePowerOffPromptActivity extends PowerOffPromptActivity {
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        if (ACTION_TIMER_SHUTDOWN.equals(action)) {
            this.mIsCurrentLocked = false;
        } else if (ACTION_TIMER_SHUTDOWN_LOCKED.equals(action)) {
            this.mIsCurrentLocked = true;
        }
        if (this.mIsCurrentLocked) {
            lightScreen();
        }
        this.pm = (PowerManager) getSystemService("power");
        this.mLock = this.pm.newWakeLock(805306369, "TimepowerWakeLock");
        this.mLock.acquire();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SettingsUtil.ACTION_PHONE_STATE);
        registerReceiver(this.mStateReceiver, filter);
        AlertParams params = this.mAlertParams;
        params.mTitle = "Power off";
        params.mPositiveButtonText = getString(17039370);
        params.mPositiveButtonListener = this;
        params.mNegativeButtonText = getString(17039360);
        params.mNegativeButtonListener = this;
        params.mMessage = getString(R.string.shutdown_confirm);
        setupAlert();
        getWindow().setCloseOnTouchOutside(false);
        this.mHandler.sendEmptyMessageDelayed(1, 60000);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                boolean isMissedRequest = false;
                Intent intent = getIntent();
                if (intent.hasExtra(EXTRA_IS_MISSED_REQUEST)) {
                    isMissedRequest = Boolean.valueOf(intent.getBooleanExtra(EXTRA_IS_MISSED_REQUEST, false)).booleanValue();
                }
                if (isMissedRequest) {
                    System.exit(0);
                }
                finish();
                return;
            case -1:
                beginShutdown();
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        Intent intent = new Intent(SettingsUtil.ACTION_SET_CHANGED);
        intent.addFlags(285212672);
        sendBroadcast(intent);
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacks(this.mMoveToFrontRunnable);
        if (this.mLock != null) {
            this.mLock.release();
            this.mLock = null;
        }
        if (this.mStateReceiver != null) {
            unregisterReceiver(this.mStateReceiver);
        }
    }

    private void beginShutdown() {
        if (this.mLock != null) {
            this.mLock.release();
            this.mLock = null;
        }
        this.mLock = this.pm.newWakeLock(268435466, "TimepowerWakeLock");
        this.mLock.acquire();
        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(268435456);
        startActivity(intent);
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onUserLeaveHint() {
        Log.i(TAG, "onUserLeaveHint");
        super.onUserLeaveHint();
        this.mHandler.removeCallbacks(this.mMoveToFrontRunnable);
        this.mHandler.postDelayed(this.mMoveToFrontRunnable, 5000);
    }

    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        this.mHandler.removeCallbacks(this.mMoveToFrontRunnable);
        this.mHandler.postDelayed(this.mMoveToFrontRunnable, 5000);
    }

    private void lightScreen() {
        getWindow().setAttributes(new LayoutParams(2004, 7865472));
        getWindow().setCloseOnTouchOutside(false);
        if (this.mIsCurrentLocked) {
            getWindow().setBackgroundDrawableResource(17170445);
        }
    }
}
