package com.android.settings.wifi.tether;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnStartTetheringCallback;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class WifiTetherSwitchBarController implements OnSwitchChangeListener, LifecycleObserver, OnStart, OnStop, Listener {
    private static final int ICCID_INDEX = 1;
    private static final int PILOT_INDEX = 4;
    private static final Uri SOFTSIM_URL = Uri.parse("content://com.redteamobile.provider");
    private static final String TAG = "WifiTetherSwitchBarController";
    private static final IntentFilter WIFI_INTENT_FILTER = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    @VisibleForTesting
    final DataSaverBackend mDataSaverBackend;
    private Handler mHandler = new Handler();
    @VisibleForTesting
    final OnStartTetheringCallback mOnStartTetheringCallback = new OnStartTetheringCallback() {
        public void onTetheringFailed() {
            super.onTetheringFailed();
            WifiTetherSwitchBarController.this.mSwitchBar.setChecked(false);
            WifiTetherSwitchBarController.this.updateWifiSwitch();
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                WifiTetherSwitchBarController.this.handleWifiApStateChanged(intent.getIntExtra("wifi_state", 14));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                WifiTetherSwitchBarController.this.updateWifiSwitch();
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                WifiTetherSwitchBarController.this.mSoftSimPilotModeEnabled = WifiTetherSwitchBarController.this.isPilotModeEnabled(context);
                WifiTetherSwitchBarController.this.updateWifiSwitch();
            }
        }
    };
    private boolean mSoftSimPilotModeEnabled = false;
    private final SwitchWidgetController mSwitchBar;
    private final WifiManager mWifiManager;

    static {
        WIFI_INTENT_FILTER.addAction("android.intent.action.AIRPLANE_MODE");
        WIFI_INTENT_FILTER.addAction("android.intent.action.SIM_STATE_CHANGED");
    }

    WifiTetherSwitchBarController(Context context, SwitchWidgetController switchBar) {
        boolean z = false;
        this.mContext = context;
        this.mSwitchBar = switchBar;
        this.mDataSaverBackend = new DataSaverBackend(context);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        SwitchWidgetController switchWidgetController = this.mSwitchBar;
        if (this.mWifiManager.getWifiApState() == 13) {
            z = true;
        }
        switchWidgetController.setChecked(z);
        this.mSwitchBar.setListener(this);
        this.mSoftSimPilotModeEnabled = isPilotModeEnabled(context);
        updateWifiSwitch();
    }

    public void onStart() {
        this.mDataSaverBackend.addListener(this);
        this.mSwitchBar.startListening();
        this.mContext.registerReceiver(this.mReceiver, WIFI_INTENT_FILTER);
    }

    public void onStop() {
        this.mDataSaverBackend.remListener(this);
        this.mSwitchBar.stopListening();
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public boolean onSwitchToggled(boolean isChecked) {
        if (!isChecked) {
            stopTether();
        } else if (!this.mWifiManager.isWifiApEnabled()) {
            startTether();
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void stopTether() {
        this.mSwitchBar.setEnabled(false);
        this.mConnectivityManager.stopTethering(0);
    }

    /* Access modifiers changed, original: 0000 */
    public void startTether() {
        Log.d(TAG, "startTether");
        this.mSwitchBar.setEnabled(false);
        this.mConnectivityManager.startTethering(0, true, this.mOnStartTetheringCallback, new Handler(Looper.getMainLooper()));
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case 10:
                if (this.mSwitchBar.isChecked()) {
                    this.mSwitchBar.setChecked(false);
                }
                this.mSwitchBar.setEnabled(false);
                return;
            case 11:
                this.mSwitchBar.setChecked(false);
                updateWifiSwitch();
                return;
            case 12:
                this.mSwitchBar.setEnabled(false);
                return;
            case 13:
                if (!this.mSwitchBar.isChecked()) {
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            WifiTetherSwitchBarController.this.mSwitchBar.setChecked(true);
                        }
                    }, 300);
                }
                updateWifiSwitch();
                return;
            default:
                this.mSwitchBar.setChecked(false);
                updateWifiSwitch();
                return;
        }
    }

    private void updateWifiSwitch() {
        if ((Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) || this.mSoftSimPilotModeEnabled) {
            this.mSwitchBar.setEnabled(false);
        } else {
            this.mSwitchBar.setEnabled(1 ^ this.mDataSaverBackend.isDataSaverEnabled());
        }
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        updateWifiSwitch();
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }

    private boolean getPilotModeFromSim(Context context, int slot) {
        boolean isPilotMode = false;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("slot=\"");
            stringBuilder.append(slot);
            stringBuilder.append("\"");
            Cursor cursor = context.getContentResolver().query(SOFTSIM_URL, new String[]{"slot", "iccid", "permit_package", "forbid_package", "pilot"}, new StringBuilder(stringBuilder.toString()).toString(), null, "slot");
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String iccId = cursor.getString(1);
                    isPilotMode = Boolean.parseBoolean(cursor.getString(4));
                    if (iccId == null || !isPilotMode) {
                        cursor.moveToNext();
                    } else {
                        cursor.close();
                        return true;
                    }
                }
                cursor.close();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "getPilotModeFromSim SQLiteException ", e);
        }
        return false;
    }

    private boolean isPilotModeEnabled(Context context) {
        int count = TelephonyManager.getDefault().getSimCount();
        for (int slotId = 0; slotId < count; slotId++) {
            if (getPilotModeFromSim(context, slotId)) {
                Log.i(TAG, "Soft sim is in pilot mode");
                return true;
            }
        }
        Log.i(TAG, "No SIM is in pilot mode");
        return false;
    }
}
