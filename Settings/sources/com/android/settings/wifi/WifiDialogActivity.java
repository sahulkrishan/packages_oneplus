package com.android.settings.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.SetupWizardUtils;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settingslib.wifi.AccessPoint;
import com.android.setupwizardlib.util.WizardManagerHelper;

public class WifiDialogActivity extends Activity implements WifiDialogListener, OnDismissListener {
    private static final String KEY_ACCESS_POINT_STATE = "access_point_state";
    @VisibleForTesting
    static final String KEY_CONNECT_FOR_CALLER = "connect_for_caller";
    private static final String KEY_WIFI_CONFIGURATION = "wifi_configuration";
    private static final int RESULT_CONNECTED = 1;
    private static final int RESULT_FORGET = 2;
    private static final String TAG = "WifiDialogActivity";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (WizardManagerHelper.isSetupWizardIntent(intent)) {
            setTheme(SetupWizardUtils.getTransparentTheme(intent));
        }
        super.onCreate(savedInstanceState);
        Bundle accessPointState = intent.getBundleExtra(KEY_ACCESS_POINT_STATE);
        AccessPoint accessPoint = null;
        if (accessPointState != null) {
            accessPoint = new AccessPoint((Context) this, accessPointState);
        }
        WifiDialog dialog = WifiDialog.createModal(this, this, accessPoint, 1);
        dialog.show();
        dialog.setOnDismissListener(this);
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void onForget(WifiDialog dialog) {
        WifiManager wifiManager = (WifiManager) getSystemService(WifiManager.class);
        AccessPoint accessPoint = dialog.getController().getAccessPoint();
        if (accessPoint != null) {
            if (accessPoint.isSaved()) {
                wifiManager.forget(accessPoint.getConfig().networkId, null);
            } else if (accessPoint.getNetworkInfo() == null || accessPoint.getNetworkInfo().getState() == State.DISCONNECTED) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to forget invalid network ");
                stringBuilder.append(accessPoint.getConfig());
                Log.e(str, stringBuilder.toString());
            } else {
                wifiManager.disableEphemeralNetwork(AccessPoint.convertToQuotedString(accessPoint.getSsidStr()));
            }
        }
        Intent resultData = new Intent();
        if (accessPoint != null) {
            Bundle accessPointState = new Bundle();
            accessPoint.saveWifiState(accessPointState);
            resultData.putExtra(KEY_ACCESS_POINT_STATE, accessPointState);
        }
        setResult(2);
        finish();
    }

    public void onSubmit(WifiDialog dialog) {
        WifiConfiguration config = dialog.getController().getConfig();
        AccessPoint accessPoint = dialog.getController().getAccessPoint();
        WifiManager wifiManager = (WifiManager) getSystemService(WifiManager.class);
        if (getIntent().getBooleanExtra(KEY_CONNECT_FOR_CALLER, true)) {
            if (config != null) {
                wifiManager.save(config, null);
                if (accessPoint != null) {
                    NetworkInfo networkInfo = accessPoint.getNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected()) {
                        wifiManager.connect(config, null);
                    }
                }
            } else if (accessPoint != null && accessPoint.isSaved()) {
                wifiManager.connect(accessPoint.getConfig(), null);
            }
        }
        Intent resultData = new Intent();
        if (accessPoint != null) {
            Bundle accessPointState = new Bundle();
            accessPoint.saveWifiState(accessPointState);
            resultData.putExtra(KEY_ACCESS_POINT_STATE, accessPointState);
        }
        if (config != null) {
            resultData.putExtra(KEY_WIFI_CONFIGURATION, config);
        }
        setResult(1, resultData);
        finish();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }
}
