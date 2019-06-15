package com.android.settings.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.wifi.AccessPoint;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class WifiStatusTest extends Activity {
    private static final String TAG = "WifiStatusTest";
    private TextView mBSSID;
    private TextView mHiddenSSID;
    private TextView mHttpClientTest;
    private String mHttpClientTestResult;
    private TextView mIPAddr;
    private TextView mLinkSpeed;
    private TextView mMACAddr;
    private TextView mNetworkId;
    private TextView mNetworkState;
    OnClickListener mPingButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            WifiStatusTest.this.updatePingState();
        }
    };
    private TextView mPingHostname;
    private String mPingHostnameResult;
    private TextView mRSSI;
    private TextView mSSID;
    private TextView mScanList;
    private TextView mSupplicantState;
    private WifiManager mWifiManager;
    private TextView mWifiState;
    private IntentFilter mWifiStateFilter;
    private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                WifiStatusTest.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                WifiStatusTest.this.handleNetworkStateChanged((NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                WifiStatusTest.this.handleScanResultsAvailable();
            } else if (!intent.getAction().equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
                if (intent.getAction().equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    WifiStatusTest.this.handleSupplicantStateChanged((SupplicantState) intent.getParcelableExtra("newState"), intent.hasExtra("supplicantError"), intent.getIntExtra("supplicantError", 0));
                } else if (intent.getAction().equals("android.net.wifi.RSSI_CHANGED")) {
                    WifiStatusTest.this.handleSignalChanged(intent.getIntExtra("newRssi", 0));
                } else if (!intent.getAction().equals("android.net.wifi.NETWORK_IDS_CHANGED")) {
                    Log.e(WifiStatusTest.TAG, "Received an unknown Wifi Intent");
                }
            }
        }
    };
    private Button pingTestButton;
    private Button updateButton;
    OnClickListener updateButtonHandler = new OnClickListener() {
        public void onClick(View v) {
            WifiInfo wifiInfo = WifiStatusTest.this.mWifiManager.getConnectionInfo();
            WifiStatusTest.this.setWifiStateText(WifiStatusTest.this.mWifiManager.getWifiState());
            WifiStatusTest.this.mBSSID.setText(wifiInfo.getBSSID());
            WifiStatusTest.this.mHiddenSSID.setText(String.valueOf(wifiInfo.getHiddenSSID()));
            int ipAddr = wifiInfo.getIpAddress();
            StringBuffer ipBuf = new StringBuffer();
            ipBuf.append(ipAddr & 255);
            ipBuf.append('.');
            int i = ipAddr >>> 8;
            ipAddr = i;
            ipBuf.append(i & 255);
            ipBuf.append('.');
            i = ipAddr >>> 8;
            ipAddr = i;
            ipBuf.append(i & 255);
            ipBuf.append('.');
            int i2 = ipAddr >>> 8;
            ipAddr = i2;
            ipBuf.append(i2 & 255);
            WifiStatusTest.this.mIPAddr.setText(ipBuf);
            TextView access$1100 = WifiStatusTest.this.mLinkSpeed;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.valueOf(wifiInfo.getLinkSpeed()));
            stringBuilder.append(" Mbps");
            access$1100.setText(stringBuilder.toString());
            WifiStatusTest.this.mMACAddr.setText(wifiInfo.getMacAddress());
            WifiStatusTest.this.mNetworkId.setText(String.valueOf(wifiInfo.getNetworkId()));
            WifiStatusTest.this.mRSSI.setText(String.valueOf(wifiInfo.getRssi()));
            WifiStatusTest.this.mSSID.setText(wifiInfo.getSSID());
            WifiStatusTest.this.setSupplicantStateText(wifiInfo.getSupplicantState());
        }
    };

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mWifiStateFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mWifiStateFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mWifiStateFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mWifiStateFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mWifiStateFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mWifiStateFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
        setContentView(R.layout.wifi_status_test);
        this.updateButton = (Button) findViewById(R.id.update);
        this.updateButton.setOnClickListener(this.updateButtonHandler);
        this.mWifiState = (TextView) findViewById(R.id.wifi_state);
        this.mNetworkState = (TextView) findViewById(R.id.network_state);
        this.mSupplicantState = (TextView) findViewById(R.id.supplicant_state);
        this.mRSSI = (TextView) findViewById(R.id.rssi);
        this.mBSSID = (TextView) findViewById(R.id.bssid);
        this.mSSID = (TextView) findViewById(R.id.ssid);
        this.mHiddenSSID = (TextView) findViewById(R.id.hidden_ssid);
        this.mIPAddr = (TextView) findViewById(R.id.ipaddr);
        this.mMACAddr = (TextView) findViewById(R.id.macaddr);
        this.mNetworkId = (TextView) findViewById(R.id.networkid);
        this.mLinkSpeed = (TextView) findViewById(R.id.link_speed);
        this.mScanList = (TextView) findViewById(R.id.scan_list);
        this.mPingHostname = (TextView) findViewById(R.id.pingHostname);
        this.mHttpClientTest = (TextView) findViewById(R.id.httpClientTest);
        this.pingTestButton = (Button) findViewById(R.id.ping_test);
        this.pingTestButton.setOnClickListener(this.mPingButtonHandler);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mWifiStateReceiver);
    }

    private void setSupplicantStateText(SupplicantState supplicantState) {
        if (SupplicantState.FOUR_WAY_HANDSHAKE.equals(supplicantState)) {
            this.mSupplicantState.setText("FOUR WAY HANDSHAKE");
        } else if (SupplicantState.ASSOCIATED.equals(supplicantState)) {
            this.mSupplicantState.setText("ASSOCIATED");
        } else if (SupplicantState.ASSOCIATING.equals(supplicantState)) {
            this.mSupplicantState.setText("ASSOCIATING");
        } else if (SupplicantState.COMPLETED.equals(supplicantState)) {
            this.mSupplicantState.setText("COMPLETED");
        } else if (SupplicantState.DISCONNECTED.equals(supplicantState)) {
            this.mSupplicantState.setText("DISCONNECTED");
        } else if (SupplicantState.DORMANT.equals(supplicantState)) {
            this.mSupplicantState.setText("DORMANT");
        } else if (SupplicantState.GROUP_HANDSHAKE.equals(supplicantState)) {
            this.mSupplicantState.setText("GROUP HANDSHAKE");
        } else if (SupplicantState.INACTIVE.equals(supplicantState)) {
            this.mSupplicantState.setText("INACTIVE");
        } else if (SupplicantState.INVALID.equals(supplicantState)) {
            this.mSupplicantState.setText("INVALID");
        } else if (SupplicantState.SCANNING.equals(supplicantState)) {
            this.mSupplicantState.setText("SCANNING");
        } else if (SupplicantState.UNINITIALIZED.equals(supplicantState)) {
            this.mSupplicantState.setText("UNINITIALIZED");
        } else {
            this.mSupplicantState.setText("BAD");
            Log.e(TAG, "supplicant state is bad");
        }
    }

    private void setWifiStateText(int wifiState) {
        String wifiStateString;
        switch (wifiState) {
            case 0:
                wifiStateString = getString(R.string.wifi_state_disabling);
                break;
            case 1:
                wifiStateString = getString(R.string.wifi_state_disabled);
                break;
            case 2:
                wifiStateString = getString(R.string.wifi_state_enabling);
                break;
            case 3:
                wifiStateString = getString(R.string.wifi_state_enabled);
                break;
            case 4:
                wifiStateString = getString(R.string.wifi_state_unknown);
                break;
            default:
                wifiStateString = "BAD";
                Log.e(TAG, "wifi state is bad");
                break;
        }
        this.mWifiState.setText(wifiStateString);
    }

    private void handleSignalChanged(int rssi) {
        this.mRSSI.setText(String.valueOf(rssi));
    }

    private void handleWifiStateChanged(int wifiState) {
        setWifiStateText(wifiState);
    }

    private void handleScanResultsAvailable() {
        List<ScanResult> list = this.mWifiManager.getScanResults();
        StringBuffer scanList = new StringBuffer();
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                ScanResult scanResult = (ScanResult) list.get(i);
                if (!(scanResult == null || TextUtils.isEmpty(scanResult.SSID))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(scanResult.SSID);
                    stringBuilder.append(" ");
                    scanList.append(stringBuilder.toString());
                }
            }
        }
        this.mScanList.setText(scanList);
    }

    private void handleSupplicantStateChanged(SupplicantState state, boolean hasError, int error) {
        if (hasError) {
            this.mSupplicantState.setText("ERROR AUTHENTICATING");
        } else {
            setSupplicantStateText(state);
        }
    }

    private void handleNetworkStateChanged(NetworkInfo networkInfo) {
        if (this.mWifiManager.isWifiEnabled()) {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            this.mNetworkState.setText(AccessPoint.getSummary(this, info.getSSID(), networkInfo.getDetailedState(), info.getNetworkId() == -1, null));
        }
    }

    private final void updatePingState() {
        final Handler handler = new Handler();
        this.mPingHostnameResult = getResources().getString(R.string.radioInfo_unknown);
        this.mHttpClientTestResult = getResources().getString(R.string.radioInfo_unknown);
        this.mPingHostname.setText(this.mPingHostnameResult);
        this.mHttpClientTest.setText(this.mHttpClientTestResult);
        final Runnable updatePingResults = new Runnable() {
            public void run() {
                WifiStatusTest.this.mPingHostname.setText(WifiStatusTest.this.mPingHostnameResult);
                WifiStatusTest.this.mHttpClientTest.setText(WifiStatusTest.this.mHttpClientTestResult);
            }
        };
        new Thread() {
            public void run() {
                WifiStatusTest.this.pingHostname();
                handler.post(updatePingResults);
            }
        }.start();
        new Thread() {
            public void run() {
                WifiStatusTest.this.httpClientTest();
                handler.post(updatePingResults);
            }
        }.start();
    }

    private final void pingHostname() {
        try {
            if (Runtime.getRuntime().exec("ping -c 1 -w 100 www.google.com").waitFor() == 0) {
                this.mPingHostnameResult = "Pass";
            } else {
                this.mPingHostnameResult = "Fail: Host unreachable";
            }
        } catch (UnknownHostException e) {
            this.mPingHostnameResult = "Fail: Unknown Host";
        } catch (IOException e2) {
            this.mPingHostnameResult = "Fail: IOException";
        } catch (InterruptedException e3) {
            this.mPingHostnameResult = "Fail: InterruptedException";
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    private void httpClientTest() {
        /*
        r4 = this;
        r0 = 0;
        r1 = new java.net.URL;	 Catch:{ IOException -> 0x003b }
        r2 = "https://www.google.com";
        r1.<init>(r2);	 Catch:{ IOException -> 0x003b }
        r2 = r1.openConnection();	 Catch:{ IOException -> 0x003b }
        r2 = (java.net.HttpURLConnection) r2;	 Catch:{ IOException -> 0x003b }
        r0 = r2;
        r2 = r0.getResponseCode();	 Catch:{ IOException -> 0x003b }
        r3 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        if (r2 != r3) goto L_0x001c;
    L_0x0017:
        r2 = "Pass";
        r4.mHttpClientTestResult = r2;	 Catch:{ IOException -> 0x003b }
        goto L_0x0033;
    L_0x001c:
        r2 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x003b }
        r2.<init>();	 Catch:{ IOException -> 0x003b }
        r3 = "Fail: Code: ";
        r2.append(r3);	 Catch:{ IOException -> 0x003b }
        r3 = r0.getResponseMessage();	 Catch:{ IOException -> 0x003b }
        r2.append(r3);	 Catch:{ IOException -> 0x003b }
        r2 = r2.toString();	 Catch:{ IOException -> 0x003b }
        r4.mHttpClientTestResult = r2;	 Catch:{ IOException -> 0x003b }
    L_0x0033:
        if (r0 == 0) goto L_0x0043;
    L_0x0035:
        r0.disconnect();
        goto L_0x0043;
    L_0x0039:
        r1 = move-exception;
        goto L_0x0044;
    L_0x003b:
        r1 = move-exception;
        r2 = "Fail: IOException";
        r4.mHttpClientTestResult = r2;	 Catch:{ all -> 0x0039 }
        if (r0 == 0) goto L_0x0043;
    L_0x0042:
        goto L_0x0035;
    L_0x0043:
        return;
    L_0x0044:
        if (r0 == 0) goto L_0x0049;
    L_0x0046:
        r0.disconnect();
    L_0x0049:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.wifi.WifiStatusTest.httpClientTest():void");
    }
}
