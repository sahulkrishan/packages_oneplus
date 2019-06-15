package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiEnabler implements OnSwitchChangeListener {
    private static final String EVENT_DATA_IS_WIFI_ON = "is_wifi_on";
    private static final int EVENT_UPDATE_INDEX = 0;
    private AtomicBoolean mConnected;
    private final ConnectivityManager mConnectivityManager;
    private Context mContext;
    private final IntentFilter mIntentFilter;
    private boolean mListeningToOnSwitchChange;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final BroadcastReceiver mReceiver;
    private boolean mStateMachineEvent;
    private final SwitchWidgetController mSwitchWidget;
    private final WifiManager mWifiManager;

    public WifiEnabler(Context context, SwitchWidgetController switchWidget, MetricsFeatureProvider metricsFeatureProvider) {
        this(context, switchWidget, metricsFeatureProvider, (ConnectivityManager) context.getSystemService("connectivity"));
    }

    @VisibleForTesting
    WifiEnabler(Context context, SwitchWidgetController switchWidget, MetricsFeatureProvider metricsFeatureProvider, ConnectivityManager connectivityManager) {
        this.mListeningToOnSwitchChange = false;
        this.mConnected = new AtomicBoolean(false);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    WifiEnabler.this.handleWifiStateChanged(WifiEnabler.this.mWifiManager.getWifiState());
                } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                    if (!WifiEnabler.this.mConnected.get()) {
                        WifiEnabler.this.handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra("newState")));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    WifiEnabler.this.mConnected.set(info.isConnected());
                    WifiEnabler.this.handleStateChanged(info.getDetailedState());
                }
            }
        };
        this.mContext = context;
        this.mSwitchWidget = switchWidget;
        this.mSwitchWidget.setListener(this);
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mConnectivityManager = connectivityManager;
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        setupSwitchController();
    }

    public void setupSwitchController() {
        handleWifiStateChanged(this.mWifiManager.getWifiState());
        if (!this.mListeningToOnSwitchChange) {
            this.mSwitchWidget.startListening();
            this.mListeningToOnSwitchChange = true;
        }
        this.mSwitchWidget.setupView();
    }

    public void teardownSwitchController() {
        if (this.mListeningToOnSwitchChange) {
            this.mSwitchWidget.stopListening();
            this.mListeningToOnSwitchChange = false;
        }
        this.mSwitchWidget.teardownView();
    }

    public void resume(Context context) {
        this.mContext = context;
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        if (!this.mListeningToOnSwitchChange) {
            this.mSwitchWidget.startListening();
            this.mListeningToOnSwitchChange = true;
        }
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mReceiver);
        if (this.mListeningToOnSwitchChange) {
            this.mSwitchWidget.stopListening();
            this.mListeningToOnSwitchChange = false;
        }
    }

    private void handleWifiStateChanged(int state) {
        this.mSwitchWidget.setDisabledByAdmin(null);
        switch (state) {
            case 0:
                this.mSwitchWidget.setEnabled(false);
                break;
            case 1:
                setSwitchBarChecked(false);
                this.mSwitchWidget.setEnabled(true);
                break;
            case 2:
                this.mSwitchWidget.setEnabled(false);
                break;
            case 3:
                setSwitchBarChecked(true);
                this.mSwitchWidget.setEnabled(true);
                break;
            default:
                setSwitchBarChecked(false);
                this.mSwitchWidget.setEnabled(true);
                break;
        }
        if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_tethering", UserHandle.myUserId())) {
            this.mSwitchWidget.setEnabled(false);
            return;
        }
        this.mSwitchWidget.setDisabledByAdmin(RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_config_tethering", UserHandle.myUserId()));
    }

    private void setSwitchBarChecked(boolean checked) {
        this.mStateMachineEvent = true;
        this.mSwitchWidget.setChecked(checked);
        this.mStateMachineEvent = false;
    }

    private void handleStateChanged(DetailedState state) {
    }

    public boolean onSwitchToggled(boolean isChecked) {
        if (this.mStateMachineEvent) {
            return true;
        }
        if (!isChecked || WirelessUtils.isRadioAllowed(this.mContext, "wifi")) {
            this.mSwitchWidget.setEnabled(false);
            if (isChecked) {
                this.mMetricsFeatureProvider.action(this.mContext, (int) Const.CODE_C1_TGW, new Pair[0]);
            } else {
                this.mMetricsFeatureProvider.action(this.mContext, (int) Const.CODE_C1_HDW, this.mConnected.get());
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onSwitchToggled: setWifiEnabled: ");
            stringBuilder.append(isChecked);
            Log.d("WifiEnabler", stringBuilder.toString());
            if (!this.mWifiManager.setWifiEnabled(isChecked)) {
                this.mSwitchWidget.setEnabled(true);
                Toast.makeText(this.mContext, R.string.wifi_error, 0).show();
            }
            return true;
        }
        Toast.makeText(this.mContext, R.string.wifi_in_airplane_mode, 0).show();
        this.mSwitchWidget.setChecked(false);
        return false;
    }
}
