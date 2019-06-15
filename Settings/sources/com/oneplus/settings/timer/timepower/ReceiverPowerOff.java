package com.oneplus.settings.timer.timepower;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.oneplus.settings.utils.OPConstants;

public class ReceiverPowerOff extends BroadcastReceiver {
    private static final String TAG = "ReceiverPowerOff";
    private static boolean mIsCalling = false;
    private static boolean mIsPoweroff = false;
    private Context mContext = null;
    private final Handler mHandler = new Handler();
    private final Runnable mPowerOffPromptRunnable = new Runnable() {
        public void run() {
            if (ReceiverPowerOff.this.mContext == null || ReceiverPowerOff.this.mPoweroffAction == null) {
                String str = ReceiverPowerOff.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("mContext = ");
                stringBuilder.append(ReceiverPowerOff.this.mContext);
                stringBuilder.append(" mPoweroffAction = ");
                stringBuilder.append(ReceiverPowerOff.this.mPoweroffAction);
                Log.e(str, stringBuilder.toString());
                return;
            }
            ComponentName cn = ((RunningTaskInfo) ((ActivityManager) ReceiverPowerOff.this.mContext.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity;
            String pkg = cn.getPackageName();
            String cls = cn.getClassName();
            String str2 = ReceiverPowerOff.TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("pkg:");
            stringBuilder2.append(pkg);
            Log.d(str2, stringBuilder2.toString());
            str2 = ReceiverPowerOff.TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("cls:");
            stringBuilder2.append(cls);
            Log.d(str2, stringBuilder2.toString());
            if (pkg.equals(OPConstants.PACKAGENAME_INCALLUI) && cls.equals("com.android.incallui.OppoInCallActivity")) {
                ReceiverPowerOff.this.mHandler.removeCallbacks(ReceiverPowerOff.this.mPowerOffPromptRunnable);
                ReceiverPowerOff.this.mHandler.postDelayed(ReceiverPowerOff.this.mPowerOffPromptRunnable, 500);
                return;
            }
            Intent intent = new Intent(ReceiverPowerOff.this.mPoweroffAction);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setFlags(268435456);
            ReceiverPowerOff.this.mContext.startActivity(intent);
        }
    };
    private String mPoweroffAction = null;

    public void onReceive(Context context, Intent arg) {
        String action = arg.getAction();
        if (action != null) {
            String poweroffAction = "com.android.settings.Shutdown";
            if (((KeyguardManager) context.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
                poweroffAction = "com.android.settings.ShutdownWhenLocked";
            }
            if (action.equals(SettingsUtil.ACTION_POWER_OFF)) {
                if (System.currentTimeMillis() - arg.getExtras().getLong(SettingsUtil.TRIGGER_TIME) < 60000) {
                    if (mIsCalling) {
                        mIsPoweroff = true;
                        return;
                    }
                    Toast.makeText(context, "phone want to turn off now !", 0).show();
                    if (isUsingTheme(context)) {
                        Log.i(TAG, "time to shutdown when changing theme, so delay shutdown");
                        rememberShutdownRequestMissed(context);
                        return;
                    }
                    Intent intent = new Intent(poweroffAction);
                    intent.setFlags(268435456);
                    context.startActivity(intent);
                }
            } else if (action.equals(SettingsUtil.ACTION_PHONE_STATE)) {
                String state = arg.getStringExtra("state");
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK) || state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                    mIsCalling = true;
                }
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                    mIsCalling = false;
                    if (mIsPoweroff) {
                        mIsPoweroff = false;
                        this.mContext = context;
                        this.mPoweroffAction = poweroffAction;
                        this.mHandler.removeCallbacks(this.mPowerOffPromptRunnable);
                        this.mHandler.postDelayed(this.mPowerOffPromptRunnable, 500);
                    }
                }
            }
        }
    }

    private boolean isUsingTheme(Context context) {
        return System.getInt(context.getContentResolver(), "oem_is_using_theme", 0) == 1;
    }

    private void rememberShutdownRequestMissed(Context context) {
        System.putInt(context.getContentResolver(), "oem_shutdown_request_missed", 1);
    }
}
