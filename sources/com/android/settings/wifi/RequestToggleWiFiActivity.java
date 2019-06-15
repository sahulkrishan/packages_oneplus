package com.android.settings.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.app.AlertActivity;
import com.android.settings.R;

public class RequestToggleWiFiActivity extends AlertActivity implements OnClickListener {
    private static final String LOG_TAG = "RequestToggleWiFiActivity";
    private static final int STATE_DISABLE = 3;
    private static final int STATE_DISABLING = 4;
    private static final int STATE_ENABLE = 1;
    private static final int STATE_ENABLING = 2;
    private static final int STATE_UNKNOWN = -1;
    private static final long TOGGLE_TIMEOUT_MILLIS = 10000;
    @NonNull
    private CharSequence mAppLabel;
    private int mLastUpdateState = -1;
    private final StateChangeReceiver mReceiver = new StateChangeReceiver();
    private int mState = -1;
    private final Runnable mTimeoutCommand = new -$$Lambda$RequestToggleWiFiActivity$PwZgoHTFFBr3iYEQbWj0vZPfHpw(this);
    @NonNull
    private WifiManager mWiFiManager;

    private final class StateChangeReceiver extends BroadcastReceiver {
        private final IntentFilter mFilter;

        private StateChangeReceiver() {
            this.mFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        }

        public void register() {
            RequestToggleWiFiActivity.this.registerReceiver(this, this.mFilter);
        }

        public void unregister() {
            RequestToggleWiFiActivity.this.unregisterReceiver(this);
        }

        public void onReceive(Context context, Intent intent) {
            Activity activity = RequestToggleWiFiActivity.this;
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                int currentState = RequestToggleWiFiActivity.this.mWiFiManager.getWifiState();
                if (currentState != 3) {
                    switch (currentState) {
                        case 0:
                            Toast.makeText(activity, R.string.wifi_error, 0).show();
                            RequestToggleWiFiActivity.this.finish();
                            break;
                        case 1:
                            break;
                    }
                }
                if (RequestToggleWiFiActivity.this.mState == 2 || RequestToggleWiFiActivity.this.mState == 4) {
                    RequestToggleWiFiActivity.this.setResult(-1);
                    RequestToggleWiFiActivity.this.finish();
                }
            }
        }
    }

    public static /* synthetic */ void lambda$new$0(RequestToggleWiFiActivity requestToggleWiFiActivity) {
        if (!requestToggleWiFiActivity.isFinishing() && !requestToggleWiFiActivity.isDestroyed()) {
            requestToggleWiFiActivity.finish();
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x006c  */
    /* JADX WARNING: Missing block: B:14:0x0061, code skipped:
            if (r2.equals("android.net.wifi.action.REQUEST_ENABLE") != false) goto L_0x0065;
     */
    public void onCreate(android.os.Bundle r8) {
        /*
        r7 = this;
        super.onCreate(r8);
        r0 = android.net.wifi.WifiManager.class;
        r0 = r7.getSystemService(r0);
        r0 = (android.net.wifi.WifiManager) r0;
        r7.mWiFiManager = r0;
        r0 = 0;
        r7.setResult(r0);
        r1 = r7.getIntent();
        r2 = "android.intent.extra.PACKAGE_NAME";
        r1 = r1.getStringExtra(r2);
        r2 = android.text.TextUtils.isEmpty(r1);
        if (r2 == 0) goto L_0x0025;
    L_0x0021:
        r7.finish();
        return;
    L_0x0025:
        r2 = r7.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0074 }
        r2 = r2.getApplicationInfo(r1, r0);	 Catch:{ NameNotFoundException -> 0x0074 }
        r3 = r7.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0074 }
        r3 = r2.loadSafeLabel(r3);	 Catch:{ NameNotFoundException -> 0x0074 }
        r7.mAppLabel = r3;	 Catch:{ NameNotFoundException -> 0x0074 }
        r2 = r7.getIntent();
        r2 = r2.getAction();
        r3 = -1;
        r4 = r2.hashCode();
        r5 = -2035256254; // 0xffffffff86b07442 float:-6.637467E-35 double:NaN;
        r6 = 1;
        if (r4 == r5) goto L_0x005b;
    L_0x004b:
        r0 = 317500393; // 0x12ecabe9 float:1.4936073E-27 double:1.568660367E-315;
        if (r4 == r0) goto L_0x0051;
    L_0x0050:
        goto L_0x0064;
    L_0x0051:
        r0 = "android.net.wifi.action.REQUEST_DISABLE";
        r0 = r2.equals(r0);
        if (r0 == 0) goto L_0x0064;
    L_0x0059:
        r0 = r6;
        goto L_0x0065;
    L_0x005b:
        r4 = "android.net.wifi.action.REQUEST_ENABLE";
        r4 = r2.equals(r4);
        if (r4 == 0) goto L_0x0064;
    L_0x0063:
        goto L_0x0065;
    L_0x0064:
        r0 = r3;
    L_0x0065:
        switch(r0) {
            case 0: goto L_0x0070;
            case 1: goto L_0x006c;
            default: goto L_0x0068;
        };
    L_0x0068:
        r7.finish();
        goto L_0x0073;
    L_0x006c:
        r0 = 3;
        r7.mState = r0;
        goto L_0x0073;
    L_0x0070:
        r7.mState = r6;
    L_0x0073:
        return;
    L_0x0074:
        r0 = move-exception;
        r2 = "RequestToggleWiFiActivity";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Couldn't find app with package name ";
        r3.append(r4);
        r3.append(r1);
        r3 = r3.toString();
        android.util.Log.e(r2, r3);
        r7.finish();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.wifi.RequestToggleWiFiActivity.onCreate(android.os.Bundle):void");
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                finish();
                return;
            case -1:
                int i = this.mState;
                if (i == 1) {
                    Log.d(LOG_TAG, "onClick: setWifiEnabled : 1");
                    this.mWiFiManager.setWifiEnabled(true);
                    this.mState = 2;
                    scheduleToggleTimeout();
                    updateUi();
                    return;
                } else if (i == 3) {
                    Log.d(LOG_TAG, "onClick: setWifiEnabled : 3");
                    this.mWiFiManager.setWifiEnabled(false);
                    this.mState = 4;
                    scheduleToggleTimeout();
                    updateUi();
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        this.mReceiver.register();
        int wifiState = this.mWiFiManager.getWifiState();
        switch (this.mState) {
            case 1:
                switch (wifiState) {
                    case 2:
                        this.mState = 2;
                        scheduleToggleTimeout();
                        break;
                    case 3:
                        setResult(-1);
                        finish();
                        return;
                }
                break;
            case 2:
                switch (wifiState) {
                    case 0:
                    case 1:
                        this.mState = 1;
                        break;
                    case 2:
                        scheduleToggleTimeout();
                        break;
                    case 3:
                        setResult(-1);
                        finish();
                        return;
                }
                break;
            case 3:
                switch (wifiState) {
                    case 1:
                        setResult(-1);
                        finish();
                        return;
                    case 2:
                        this.mState = 4;
                        scheduleToggleTimeout();
                        break;
                }
                break;
            case 4:
                switch (wifiState) {
                    case 0:
                        scheduleToggleTimeout();
                        break;
                    case 1:
                        setResult(-1);
                        finish();
                        return;
                    case 2:
                    case 3:
                        this.mState = 3;
                        break;
                }
                break;
        }
        updateUi();
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        this.mReceiver.unregister();
        unscheduleToggleTimeout();
        super.onStop();
    }

    private void updateUi() {
        if (this.mLastUpdateState != this.mState) {
            this.mLastUpdateState = this.mState;
            switch (this.mState) {
                case 1:
                    this.mAlertParams.mPositiveButtonText = getString(R.string.allow);
                    this.mAlertParams.mPositiveButtonListener = this;
                    this.mAlertParams.mNegativeButtonText = getString(R.string.deny);
                    this.mAlertParams.mNegativeButtonListener = this;
                    this.mAlertParams.mMessage = getString(R.string.wifi_ask_enable, new Object[]{this.mAppLabel});
                    break;
                case 2:
                    this.mAlert.setButton(-1, null, null, null);
                    this.mAlert.setButton(-2, null, null, null);
                    this.mAlertParams.mPositiveButtonText = null;
                    this.mAlertParams.mPositiveButtonListener = null;
                    this.mAlertParams.mNegativeButtonText = null;
                    this.mAlertParams.mNegativeButtonListener = null;
                    this.mAlertParams.mMessage = getString(R.string.wifi_starting);
                    break;
                case 3:
                    this.mAlertParams.mPositiveButtonText = getString(R.string.allow);
                    this.mAlertParams.mPositiveButtonListener = this;
                    this.mAlertParams.mNegativeButtonText = getString(R.string.deny);
                    this.mAlertParams.mNegativeButtonListener = this;
                    this.mAlertParams.mMessage = getString(R.string.wifi_ask_disable, new Object[]{this.mAppLabel});
                    break;
                case 4:
                    this.mAlert.setButton(-1, null, null, null);
                    this.mAlert.setButton(-2, null, null, null);
                    this.mAlertParams.mPositiveButtonText = null;
                    this.mAlertParams.mPositiveButtonListener = null;
                    this.mAlertParams.mNegativeButtonText = null;
                    this.mAlertParams.mNegativeButtonListener = null;
                    this.mAlertParams.mMessage = getString(R.string.wifi_stopping);
                    break;
            }
            setupAlert();
        }
    }

    public void dismiss() {
    }

    private void scheduleToggleTimeout() {
        getWindow().getDecorView().postDelayed(this.mTimeoutCommand, TOGGLE_TIMEOUT_MILLIS);
    }

    private void unscheduleToggleTimeout() {
        getWindow().getDecorView().removeCallbacks(this.mTimeoutCommand);
    }
}
