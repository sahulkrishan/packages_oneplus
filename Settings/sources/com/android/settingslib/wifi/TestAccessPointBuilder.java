package com.android.settingslib.wifi;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Keep;
import java.util.ArrayList;

@Keep
public class TestAccessPointBuilder {
    private static final int MAX_RSSI = -55;
    private static final int MIN_RSSI = -100;
    private String mBssid = null;
    private String mCarrierName = null;
    Context mContext;
    private String mFqdn = null;
    private boolean mIsCarrierAp = false;
    private int mNetworkId = -1;
    private NetworkInfo mNetworkInfo = null;
    private String mProviderFriendlyName = null;
    private int mRssi = AccessPoint.UNREACHABLE_RSSI;
    private ArrayList<ScanResult> mScanResults;
    private ArrayList<TimestampedScoredNetwork> mScoredNetworkCache;
    private int mSecurity = 0;
    private int mSpeed = 0;
    private WifiConfiguration mWifiConfig;
    private WifiInfo mWifiInfo;
    private String ssid = "TestSsid";

    @Keep
    public TestAccessPointBuilder(Context context) {
        this.mContext = context;
    }

    @Keep
    public AccessPoint build() {
        Bundle bundle = new Bundle();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.networkId = this.mNetworkId;
        wifiConfig.BSSID = this.mBssid;
        bundle.putString("key_ssid", this.ssid);
        bundle.putParcelable("key_config", wifiConfig);
        bundle.putParcelable("key_networkinfo", this.mNetworkInfo);
        bundle.putParcelable("key_wifiinfo", this.mWifiInfo);
        if (this.mFqdn != null) {
            bundle.putString("key_fqdn", this.mFqdn);
        }
        if (this.mProviderFriendlyName != null) {
            bundle.putString("key_provider_friendly_name", this.mProviderFriendlyName);
        }
        if (this.mScanResults != null) {
            bundle.putParcelableArray("key_scanresults", (Parcelable[]) this.mScanResults.toArray(new Parcelable[this.mScanResults.size()]));
        }
        if (this.mScoredNetworkCache != null) {
            bundle.putParcelableArrayList("key_scorednetworkcache", this.mScoredNetworkCache);
        }
        bundle.putInt("key_security", this.mSecurity);
        bundle.putInt("key_speed", this.mSpeed);
        bundle.putBoolean("key_is_carrier_ap", this.mIsCarrierAp);
        if (this.mCarrierName != null) {
            bundle.putString("key_carrier_name", this.mCarrierName);
        }
        AccessPoint ap = new AccessPoint(this.mContext, bundle);
        ap.setRssi(this.mRssi);
        return ap;
    }

    @Keep
    public TestAccessPointBuilder setActive(boolean active) {
        if (active) {
            this.mNetworkInfo = new NetworkInfo(8, 8, "TestNetwork", "TestNetwork");
        } else {
            this.mNetworkInfo = null;
        }
        return this;
    }

    @Keep
    public TestAccessPointBuilder setLevel(int level) {
        if (level == 0) {
            this.mRssi = MIN_RSSI;
        } else if (level >= 5) {
            this.mRssi = MAX_RSSI;
        } else {
            this.mRssi = (int) (((((float) level) * 45.0f) / 4.0f) - 0.044921875f);
        }
        return this;
    }

    @Keep
    public TestAccessPointBuilder setNetworkInfo(NetworkInfo info) {
        this.mNetworkInfo = info;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setRssi(int rssi) {
        this.mRssi = rssi;
        return this;
    }

    public TestAccessPointBuilder setSpeed(int speed) {
        this.mSpeed = speed;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setReachable(boolean reachable) {
        if (!reachable) {
            this.mRssi = AccessPoint.UNREACHABLE_RSSI;
        } else if (this.mRssi == AccessPoint.UNREACHABLE_RSSI) {
            this.mRssi = MIN_RSSI;
        }
        return this;
    }

    @Keep
    public TestAccessPointBuilder setSaved(boolean saved) {
        if (saved) {
            this.mNetworkId = 1;
        } else {
            this.mNetworkId = -1;
        }
        return this;
    }

    @Keep
    public TestAccessPointBuilder setSecurity(int security) {
        this.mSecurity = security;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setSsid(String newSsid) {
        this.ssid = newSsid;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setFqdn(String fqdn) {
        this.mFqdn = fqdn;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setProviderFriendlyName(String friendlyName) {
        this.mProviderFriendlyName = friendlyName;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setWifiInfo(WifiInfo info) {
        this.mWifiInfo = info;
        return this;
    }

    @Keep
    public TestAccessPointBuilder setNetworkId(int networkId) {
        this.mNetworkId = networkId;
        return this;
    }

    public TestAccessPointBuilder setBssid(String bssid) {
        this.mBssid = bssid;
        return this;
    }

    public TestAccessPointBuilder setScanResults(ArrayList<ScanResult> scanResults) {
        this.mScanResults = scanResults;
        return this;
    }

    public TestAccessPointBuilder setIsCarrierAp(boolean isCarrierAp) {
        this.mIsCarrierAp = isCarrierAp;
        return this;
    }

    public TestAccessPointBuilder setCarrierName(String carrierName) {
        this.mCarrierName = carrierName;
        return this;
    }

    public TestAccessPointBuilder setScoredNetworkCache(ArrayList<TimestampedScoredNetwork> scoredNetworkCache) {
        this.mScoredNetworkCache = scoredNetworkCache;
        return this;
    }
}
