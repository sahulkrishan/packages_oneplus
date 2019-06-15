package com.android.settings.wifi;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;

public final class WifiNoInternetDialog extends AlertActivity implements OnClickListener {
    private static final String TAG = "WifiNoInternetDialog";
    private String mAction;
    private CheckBox mAlwaysAllow;
    private ConnectivityManager mCM;
    private Network mNetwork;
    private NetworkCallback mNetworkCallback;
    private String mNetworkName;

    private boolean isKnownAction(Intent intent) {
        return intent.getAction().equals("android.net.conn.PROMPT_UNVALIDATED") || intent.getAction().equals("android.net.conn.PROMPT_LOST_VALIDATION");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String str;
        StringBuilder stringBuilder;
        if (intent != null && isKnownAction(intent) && "netId".equals(intent.getScheme())) {
            this.mAction = intent.getAction();
            try {
                this.mNetwork = new Network(Integer.parseInt(intent.getData().getSchemeSpecificPart()));
            } catch (NullPointerException | NumberFormatException e) {
                this.mNetwork = null;
            }
            if (this.mNetwork == null) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Can't determine network from '");
                stringBuilder.append(intent.getData());
                stringBuilder.append("' , exiting");
                Log.e(str, stringBuilder.toString());
                finish();
                return;
            }
            NetworkRequest request = new Builder().clearCapabilities().build();
            this.mNetworkCallback = new NetworkCallback() {
                public void onLost(Network network) {
                    if (WifiNoInternetDialog.this.mNetwork.equals(network)) {
                        String str = WifiNoInternetDialog.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Network ");
                        stringBuilder.append(WifiNoInternetDialog.this.mNetwork);
                        stringBuilder.append(" disconnected");
                        Log.d(str, stringBuilder.toString());
                        WifiNoInternetDialog.this.finish();
                    }
                }

                public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
                    if (WifiNoInternetDialog.this.mNetwork.equals(network) && nc.hasCapability(16)) {
                        String str = WifiNoInternetDialog.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Network ");
                        stringBuilder.append(WifiNoInternetDialog.this.mNetwork);
                        stringBuilder.append(" validated");
                        Log.d(str, stringBuilder.toString());
                        WifiNoInternetDialog.this.finish();
                    }
                }
            };
            this.mCM = (ConnectivityManager) getSystemService("connectivity");
            this.mCM.registerNetworkCallback(request, this.mNetworkCallback);
            NetworkInfo ni = this.mCM.getNetworkInfo(this.mNetwork);
            NetworkCapabilities nc = this.mCM.getNetworkCapabilities(this.mNetwork);
            if (ni == null || !ni.isConnectedOrConnecting() || nc == null) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Network ");
                stringBuilder2.append(this.mNetwork);
                stringBuilder2.append(" is not connected: ");
                stringBuilder2.append(ni);
                Log.d(str2, stringBuilder2.toString());
                finish();
                return;
            }
            this.mNetworkName = nc.getSSID();
            if (this.mNetworkName != null) {
                this.mNetworkName = WifiInfo.removeDoubleQuotes(this.mNetworkName);
            }
            createDialog();
            return;
        }
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected intent ");
        stringBuilder.append(intent);
        stringBuilder.append(", exiting");
        Log.e(str, stringBuilder.toString());
        finish();
    }

    private void createDialog() {
        this.mAlert.setIcon(R.drawable.ic_settings_wireless);
        AlertParams ap = this.mAlertParams;
        if ("android.net.conn.PROMPT_UNVALIDATED".equals(this.mAction)) {
            ap.mTitle = this.mNetworkName;
            ap.mMessage = getString(R.string.no_internet_access_text);
            ap.mPositiveButtonText = getString(R.string.yes);
            ap.mNegativeButtonText = getString(R.string.no);
        } else {
            ap.mTitle = getString(R.string.lost_internet_access_title);
            ap.mMessage = getString(R.string.lost_internet_access_text);
            ap.mPositiveButtonText = getString(R.string.lost_internet_access_switch);
            ap.mNegativeButtonText = getString(R.string.lost_internet_access_cancel);
        }
        ap.mPositiveButtonListener = this;
        ap.mNegativeButtonListener = this;
        View checkbox = LayoutInflater.from(ap.mContext).inflate(17367090, null);
        ap.mView = checkbox;
        this.mAlwaysAllow = (CheckBox) checkbox.findViewById(16908711);
        if ("android.net.conn.PROMPT_UNVALIDATED".equals(this.mAction)) {
            this.mAlwaysAllow.setText(getString(R.string.no_internet_access_remember));
        } else {
            this.mAlwaysAllow.setText(getString(R.string.lost_internet_access_persist));
        }
        setupAlert();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        if (this.mNetworkCallback != null) {
            this.mCM.unregisterNetworkCallback(this.mNetworkCallback);
            this.mNetworkCallback = null;
        }
        super.onDestroy();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -2 || which == -1) {
            String what;
            String action;
            boolean always = this.mAlwaysAllow.isChecked();
            boolean z = false;
            boolean accept;
            if ("android.net.conn.PROMPT_UNVALIDATED".equals(this.mAction)) {
                what = "NO_INTERNET";
                if (which == -1) {
                    z = true;
                }
                accept = z;
                action = accept ? "Connect" : "Ignore";
                this.mCM.setAcceptUnvalidated(this.mNetwork, accept, always);
            } else {
                what = "LOST_INTERNET";
                if (which == -1) {
                    z = true;
                }
                accept = z;
                action = accept ? "Switch away" : "Get stuck";
                if (always) {
                    Global.putString(this.mAlertParams.mContext.getContentResolver(), "network_avoid_bad_wifi", accept ? "1" : "0");
                } else if (accept) {
                    this.mCM.setAvoidUnvalidated(this.mNetwork);
                }
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(what);
            stringBuilder.append(": ");
            stringBuilder.append(action);
            stringBuilder.append(" network=");
            stringBuilder.append(this.mNetwork);
            stringBuilder.append(always ? " and remember" : "");
            Log.d(str, stringBuilder.toString());
        }
    }
}
