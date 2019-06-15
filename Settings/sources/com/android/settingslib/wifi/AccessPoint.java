package com.android.settingslib.wifi;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.ScoredNetwork;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TtsSpan.TelephoneBuilder;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.R;
import com.android.settingslib.utils.ThreadUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AccessPoint implements Comparable<AccessPoint> {
    public static final int HIGHER_FREQ_24GHZ = 2500;
    public static final int HIGHER_FREQ_5GHZ = 5900;
    static final String KEY_CARRIER_AP_EAP_TYPE = "key_carrier_ap_eap_type";
    static final String KEY_CARRIER_NAME = "key_carrier_name";
    static final String KEY_CONFIG = "key_config";
    static final String KEY_FQDN = "key_fqdn";
    static final String KEY_IS_CARRIER_AP = "key_is_carrier_ap";
    static final String KEY_NETWORKINFO = "key_networkinfo";
    static final String KEY_PROVIDER_FRIENDLY_NAME = "key_provider_friendly_name";
    static final String KEY_PSKTYPE = "key_psktype";
    static final String KEY_SCANRESULTS = "key_scanresults";
    static final String KEY_SCOREDNETWORKCACHE = "key_scorednetworkcache";
    static final String KEY_SECURITY = "key_security";
    static final String KEY_SPEED = "key_speed";
    static final String KEY_SSID = "key_ssid";
    static final String KEY_WIFIINFO = "key_wifiinfo";
    public static final int LOWER_FREQ_24GHZ = 2400;
    public static final int LOWER_FREQ_5GHZ = 4900;
    private static final int PSK_UNKNOWN = 0;
    private static final int PSK_WPA = 1;
    private static final int PSK_WPA2 = 2;
    private static final int PSK_WPA_WPA2 = 3;
    public static final int SECURITY_DPP = 6;
    public static final int SECURITY_EAP = 3;
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_OWE = 8;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_SAE = 7;
    public static final int SECURITY_WAPI_CERT = 5;
    public static final int SECURITY_WAPI_PSK = 4;
    public static final int SECURITY_WEP = 1;
    public static final int SIGNAL_LEVELS = 5;
    static final String TAG = "SettingsLib.AccessPoint";
    public static final int UNREACHABLE_RSSI = WifiConfiguration.INVALID_RSSI;
    static final AtomicInteger sLastId = new AtomicInteger(0);
    private String bssid;
    AccessPointListener mAccessPointListener;
    private int mCarrierApEapType;
    private String mCarrierName;
    private WifiConfiguration mConfig;
    private final Context mContext;
    private String mFqdn;
    int mId;
    private WifiInfo mInfo;
    private boolean mIsCarrierAp;
    private boolean mIsScoredNetworkMetered;
    private String mKey;
    private NetworkInfo mNetworkInfo;
    private String mProviderFriendlyName;
    private int mRssi;
    private final ArraySet<ScanResult> mScanResults;
    private final Map<String, TimestampedScoredNetwork> mScoredNetworkCache;
    private int mSpeed;
    private Object mTag;
    private int networkId;
    private int pskType;
    private int security;
    private String ssid;
    private int wapiPskType;

    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Speed {
        public static final int FAST = 20;
        public static final int MODERATE = 10;
        public static final int NONE = 0;
        public static final int SLOW = 5;
        public static final int VERY_FAST = 30;
    }

    public AccessPoint(Context context, Bundle savedState) {
        this.mScanResults = new ArraySet();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        int i = 0;
        this.pskType = 0;
        this.mRssi = UNREACHABLE_RSSI;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        if (savedState.containsKey(KEY_CONFIG)) {
            this.mConfig = (WifiConfiguration) savedState.getParcelable(KEY_CONFIG);
        }
        if (this.mConfig != null) {
            loadConfig(this.mConfig);
        }
        if (savedState.containsKey(KEY_SSID)) {
            this.ssid = savedState.getString(KEY_SSID);
        }
        if (savedState.containsKey(KEY_SECURITY)) {
            this.security = savedState.getInt(KEY_SECURITY);
        }
        if (savedState.containsKey(KEY_SPEED)) {
            this.mSpeed = savedState.getInt(KEY_SPEED);
        }
        if (savedState.containsKey(KEY_PSKTYPE)) {
            this.pskType = savedState.getInt(KEY_PSKTYPE);
        }
        this.mInfo = (WifiInfo) savedState.getParcelable(KEY_WIFIINFO);
        if (savedState.containsKey(KEY_NETWORKINFO)) {
            this.mNetworkInfo = (NetworkInfo) savedState.getParcelable(KEY_NETWORKINFO);
        }
        if (savedState.containsKey(KEY_SCANRESULTS)) {
            Parcelable[] scanResults = savedState.getParcelableArray(KEY_SCANRESULTS);
            this.mScanResults.clear();
            int length = scanResults.length;
            while (i < length) {
                this.mScanResults.add((ScanResult) scanResults[i]);
                i++;
            }
        }
        if (savedState.containsKey(KEY_SCOREDNETWORKCACHE)) {
            Iterator it = savedState.getParcelableArrayList(KEY_SCOREDNETWORKCACHE).iterator();
            while (it.hasNext()) {
                TimestampedScoredNetwork timedScore = (TimestampedScoredNetwork) it.next();
                this.mScoredNetworkCache.put(timedScore.getScore().networkKey.wifiKey.bssid, timedScore);
            }
        }
        if (savedState.containsKey(KEY_FQDN)) {
            this.mFqdn = savedState.getString(KEY_FQDN);
        }
        if (savedState.containsKey(KEY_PROVIDER_FRIENDLY_NAME)) {
            this.mProviderFriendlyName = savedState.getString(KEY_PROVIDER_FRIENDLY_NAME);
        }
        if (savedState.containsKey(KEY_IS_CARRIER_AP)) {
            this.mIsCarrierAp = savedState.getBoolean(KEY_IS_CARRIER_AP);
        }
        if (savedState.containsKey(KEY_CARRIER_AP_EAP_TYPE)) {
            this.mCarrierApEapType = savedState.getInt(KEY_CARRIER_AP_EAP_TYPE);
        }
        if (savedState.containsKey(KEY_CARRIER_NAME)) {
            this.mCarrierName = savedState.getString(KEY_CARRIER_NAME);
        }
        update(this.mConfig, this.mInfo, this.mNetworkInfo);
        updateKey();
        updateRssi();
        this.mId = sLastId.incrementAndGet();
    }

    public AccessPoint(Context context, WifiConfiguration config) {
        this.mScanResults = new ArraySet();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = UNREACHABLE_RSSI;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        loadConfig(config);
        this.mId = sLastId.incrementAndGet();
    }

    public AccessPoint(Context context, PasspointConfiguration config) {
        this.mScanResults = new ArraySet();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = UNREACHABLE_RSSI;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.mFqdn = config.getHomeSp().getFqdn();
        this.mProviderFriendlyName = config.getHomeSp().getFriendlyName();
        this.mId = sLastId.incrementAndGet();
    }

    AccessPoint(Context context, Collection<ScanResult> results) {
        this.mScanResults = new ArraySet();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mRssi = UNREACHABLE_RSSI;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsCarrierAp = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Cannot construct with an empty ScanResult list");
        }
        this.mScanResults.addAll(results);
        ScanResult firstResult = (ScanResult) results.iterator().next();
        this.ssid = firstResult.SSID;
        this.bssid = firstResult.BSSID;
        this.security = getSecurity(firstResult);
        if (this.security == 2) {
            this.pskType = getPskType(firstResult);
        }
        updateKey();
        updateRssi();
        this.mIsCarrierAp = firstResult.isCarrierAp;
        this.mCarrierApEapType = firstResult.carrierApEapType;
        this.mCarrierName = firstResult.carrierName;
        this.mId = sLastId.incrementAndGet();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void loadConfig(WifiConfiguration config) {
        this.ssid = config.SSID == null ? "" : removeDoubleQuotes(config.SSID);
        this.bssid = config.BSSID;
        this.security = getSecurity(config);
        updateKey();
        this.networkId = config.networkId;
        this.mConfig = config;
        this.wapiPskType = config.wapiPskType;
    }

    private void updateKey() {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(getSsidStr())) {
            builder.append(getBssid());
        } else {
            builder.append(getSsidStr());
        }
        builder.append(',');
        builder.append(getSecurity());
        this.mKey = builder.toString();
    }

    public int compareTo(@NonNull AccessPoint other) {
        if (isActive() && !other.isActive()) {
            return -1;
        }
        if (!isActive() && other.isActive()) {
            return 1;
        }
        if (isReachable() && !other.isReachable()) {
            return -1;
        }
        if (!isReachable() && other.isReachable()) {
            return 1;
        }
        if (isSaved() && !other.isSaved()) {
            return -1;
        }
        if (!isSaved() && other.isSaved()) {
            return 1;
        }
        if (getSpeed() != other.getSpeed()) {
            return other.getSpeed() - getSpeed();
        }
        int difference = WifiManager.calculateSignalLevel(other.mRssi, 5) - WifiManager.calculateSignalLevel(this.mRssi, 5);
        if (difference != 0) {
            return difference;
        }
        difference = getSsidStr().compareToIgnoreCase(other.getSsidStr());
        if (difference != 0) {
            return difference;
        }
        return getSsidStr().compareTo(other.getSsidStr());
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof AccessPoint)) {
            return false;
        }
        if (compareTo((AccessPoint) other) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int result = 0;
        if (this.mInfo != null) {
            result = 0 + (13 * this.mInfo.hashCode());
        }
        return ((result + (19 * this.mRssi)) + (23 * this.networkId)) + (29 * this.ssid.hashCode());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AccessPoint(");
        builder = builder.append(this.ssid);
        if (this.bssid != null) {
            builder.append(":");
            builder.append(this.bssid);
        }
        if (isSaved()) {
            builder.append(',');
            builder.append("saved");
        }
        if (isActive()) {
            builder.append(',');
            builder.append("active");
        }
        if (isEphemeral()) {
            builder.append(',');
            builder.append("ephemeral");
        }
        if (isConnectable()) {
            builder.append(',');
            builder.append("connectable");
        }
        if (this.security != 0) {
            builder.append(',');
            builder.append(securityToString(this.security, this.pskType));
        }
        builder.append(",level=");
        builder.append(getLevel());
        if (this.mSpeed != 0) {
            builder.append(",speed=");
            builder.append(this.mSpeed);
        }
        builder.append(",metered=");
        builder.append(isMetered());
        if (isVerboseLoggingEnabled()) {
            builder.append(",rssi=");
            builder.append(this.mRssi);
            builder.append(",scan cache size=");
            builder.append(this.mScanResults.size());
        }
        builder.append(')');
        return builder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean update(WifiNetworkScoreCache scoreCache, boolean scoringUiEnabled, long maxScoreCacheAgeMillis) {
        boolean scoreChanged = false;
        if (scoringUiEnabled) {
            scoreChanged = updateScores(scoreCache, maxScoreCacheAgeMillis);
        }
        return updateMetered(scoreCache) || scoreChanged;
    }

    private boolean updateScores(WifiNetworkScoreCache scoreCache, long maxScoreCacheAgeMillis) {
        long nowMillis = SystemClock.elapsedRealtime();
        Iterator it = this.mScanResults.iterator();
        while (it.hasNext()) {
            ScanResult result = (ScanResult) it.next();
            ScoredNetwork score = scoreCache.getScoredNetwork(result);
            if (score != null) {
                TimestampedScoredNetwork timedScore = (TimestampedScoredNetwork) this.mScoredNetworkCache.get(result.BSSID);
                if (timedScore == null) {
                    this.mScoredNetworkCache.put(result.BSSID, new TimestampedScoredNetwork(score, nowMillis));
                } else {
                    timedScore.update(score, nowMillis);
                }
            }
        }
        long evictionCutoff = nowMillis - maxScoreCacheAgeMillis;
        Iterator<TimestampedScoredNetwork> iterator = this.mScoredNetworkCache.values().iterator();
        iterator.forEachRemaining(new -$$Lambda$AccessPoint$OIXfUc7y1PqI_zmQ3STe_086YzY(evictionCutoff, iterator));
        return updateSpeed();
    }

    static /* synthetic */ void lambda$updateScores$0(long evictionCutoff, Iterator iterator, TimestampedScoredNetwork timestampedScoredNetwork) {
        if (timestampedScoredNetwork.getUpdatedTimestampMillis() < evictionCutoff) {
            iterator.remove();
        }
    }

    private boolean updateSpeed() {
        int oldSpeed = this.mSpeed;
        this.mSpeed = generateAverageSpeedForSsid();
        boolean changed = oldSpeed != this.mSpeed;
        if (isVerboseLoggingEnabled() && changed) {
            Log.i(TAG, String.format("%s: Set speed to %d", new Object[]{this.ssid, Integer.valueOf(this.mSpeed)}));
        }
        return changed;
    }

    private int generateAverageSpeedForSsid() {
        if (this.mScoredNetworkCache.isEmpty()) {
            return 0;
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, String.format("Generating fallbackspeed for %s using cache: %s", new Object[]{getSsidStr(), this.mScoredNetworkCache}));
        }
        int count = 0;
        int totalSpeed = 0;
        for (TimestampedScoredNetwork timedScore : this.mScoredNetworkCache.values()) {
            int speed = timedScore.getScore().calculateBadge(this.mRssi);
            if (speed != 0) {
                count++;
                totalSpeed += speed;
            }
        }
        int speed2 = count == 0 ? 0 : totalSpeed / count;
        if (isVerboseLoggingEnabled()) {
            Log.i(TAG, String.format("%s generated fallback speed is: %d", new Object[]{getSsidStr(), Integer.valueOf(speed2)}));
        }
        return roundToClosestSpeedEnum(speed2);
    }

    private boolean updateMetered(WifiNetworkScoreCache scoreCache) {
        boolean oldMetering = this.mIsScoredNetworkMetered;
        this.mIsScoredNetworkMetered = false;
        if (!isActive() || this.mInfo == null) {
            Iterator it = this.mScanResults.iterator();
            while (it.hasNext()) {
                ScoredNetwork score = scoreCache.getScoredNetwork((ScanResult) it.next());
                if (score != null) {
                    this.mIsScoredNetworkMetered |= score.meteredHint;
                }
            }
        } else {
            ScoredNetwork score2 = scoreCache.getScoredNetwork(NetworkKey.createFromWifiInfo(this.mInfo));
            if (score2 != null) {
                this.mIsScoredNetworkMetered |= score2.meteredHint;
            }
        }
        if (oldMetering == this.mIsScoredNetworkMetered) {
            return true;
        }
        return false;
    }

    public static String getKey(ScanResult result) {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(result.SSID)) {
            builder.append(result.BSSID);
        } else {
            builder.append(result.SSID);
        }
        builder.append(',');
        builder.append(getSecurity(result));
        return builder.toString();
    }

    public static String getKey(WifiConfiguration config) {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(config.SSID)) {
            builder.append(config.BSSID);
        } else {
            builder.append(removeDoubleQuotes(config.SSID));
        }
        builder.append(',');
        builder.append(getSecurity(config));
        return builder.toString();
    }

    public String getKey() {
        return this.mKey;
    }

    public boolean matches(ScanResult result) {
        return this.ssid.equals(result.SSID) && this.security == getSecurity(result);
    }

    public boolean matches(WifiConfiguration config) {
        boolean z = false;
        if (config.isPasspoint() && this.mConfig != null && this.mConfig.isPasspoint()) {
            if (this.ssid.equals(removeDoubleQuotes(config.SSID)) && config.FQDN.equals(this.mConfig.FQDN)) {
                z = true;
            }
            return z;
        }
        if (this.ssid.equals(removeDoubleQuotes(config.SSID)) && this.security == getSecurity(config) && (this.mConfig == null || this.mConfig.shared == config.shared)) {
            z = true;
        }
        return z;
    }

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public String getPasspointFqdn() {
        return this.mFqdn;
    }

    public void clearConfig() {
        this.mConfig = null;
        this.networkId = -1;
    }

    public boolean isFils256Supported() {
        String capability = "";
        try {
            capability = Stub.asInterface(ServiceManager.getService("wifi")).getCapabilities("key_mgmt");
        } catch (RemoteException e) {
            Log.w(TAG, "Remote Exception", e);
        }
        if (!capability.contains("FILS-SHA256")) {
            return false;
        }
        Iterator it = this.mScanResults.iterator();
        while (it.hasNext()) {
            if (((ScanResult) it.next()).capabilities.contains("FILS-SHA256")) {
                return true;
            }
        }
        return false;
    }

    public boolean isSuiteBSupported() {
        String capability = "";
        try {
            capability = Stub.asInterface(ServiceManager.getService("wifi")).getCapabilities("key_mgmt");
        } catch (RemoteException e) {
            Log.w(TAG, "Remote Exception", e);
        }
        if (!capability.contains("WPA-EAP-SUITE-B-192")) {
            return false;
        }
        Iterator it = this.mScanResults.iterator();
        while (it.hasNext()) {
            if (((ScanResult) it.next()).capabilities.contains("EAP_SUITE_B_192")) {
                return true;
            }
        }
        return false;
    }

    public boolean isFils384Supported() {
        String capability = "";
        try {
            capability = Stub.asInterface(ServiceManager.getService("wifi")).getCapabilities("key_mgmt");
        } catch (RemoteException e) {
            Log.w(TAG, "Remote Exception", e);
        }
        if (!capability.contains("FILS-SHA384")) {
            return false;
        }
        Iterator it = this.mScanResults.iterator();
        while (it.hasNext()) {
            if (((ScanResult) it.next()).capabilities.contains("FILS-SHA384")) {
                return true;
            }
        }
        return false;
    }

    public WifiInfo getInfo() {
        return this.mInfo;
    }

    public int getLevel() {
        return WifiManager.calculateSignalLevel(this.mRssi, 5);
    }

    public int getRssi() {
        return this.mRssi;
    }

    public Set<ScanResult> getScanResults() {
        return this.mScanResults;
    }

    public Map<String, TimestampedScoredNetwork> getScoredNetworkCache() {
        return this.mScoredNetworkCache;
    }

    private void updateRssi() {
        if (!isActive()) {
            int rssi = UNREACHABLE_RSSI;
            Iterator it = this.mScanResults.iterator();
            while (it.hasNext()) {
                ScanResult result = (ScanResult) it.next();
                if (result.level > rssi) {
                    rssi = result.level;
                }
            }
            if (rssi == UNREACHABLE_RSSI || this.mRssi == UNREACHABLE_RSSI) {
                this.mRssi = rssi;
            } else {
                this.mRssi = (this.mRssi + rssi) / 2;
            }
        }
    }

    public boolean isMetered() {
        return this.mIsScoredNetworkMetered || WifiConfiguration.isMetered(this.mConfig, this.mInfo);
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getSecurity() {
        return this.security;
    }

    public String getSecurityString(boolean concise) {
        Context context = this.mContext;
        String string;
        if (isPasspoint() || isPasspointConfig()) {
            if (concise) {
                string = context.getString(R.string.wifi_security_short_eap);
            } else {
                string = context.getString(R.string.wifi_security_eap);
            }
            return string;
        }
        switch (this.security) {
            case 1:
                if (concise) {
                    string = context.getString(R.string.wifi_security_short_wep);
                } else {
                    string = context.getString(R.string.wifi_security_wep);
                }
                return string;
            case 2:
                switch (this.pskType) {
                    case 1:
                        if (concise) {
                            string = context.getString(R.string.wifi_security_short_wpa);
                        } else {
                            string = context.getString(R.string.wifi_security_wpa);
                        }
                        return string;
                    case 2:
                        if (concise) {
                            string = context.getString(R.string.wifi_security_short_wpa2);
                        } else {
                            string = context.getString(R.string.wifi_security_wpa2);
                        }
                        return string;
                    case 3:
                        if (concise) {
                            string = context.getString(R.string.wifi_security_short_wpa_wpa2);
                        } else {
                            string = context.getString(R.string.wifi_security_wpa_wpa2);
                        }
                        return string;
                    default:
                        if (concise) {
                            string = context.getString(R.string.wifi_security_short_psk_generic);
                        } else {
                            string = context.getString(R.string.wifi_security_psk_generic);
                        }
                        return string;
                }
            case 3:
                if (concise) {
                    string = context.getString(R.string.wifi_security_short_eap);
                } else {
                    string = context.getString(R.string.wifi_security_eap);
                }
                return string;
            case 4:
                return context.getString(R.string.wifi_security_wapi_psk);
            case 5:
                return context.getString(R.string.wifi_security_wapi_cert);
            case 6:
                if (concise) {
                    string = context.getString(R.string.wifi_security_short_dpp);
                } else {
                    string = context.getString(R.string.wifi_security_dpp);
                }
                return string;
            case 7:
                if (concise) {
                    string = context.getString(R.string.wifi_security_short_sae);
                } else {
                    string = context.getString(R.string.wifi_security_sae);
                }
                return string;
            case 8:
                if (concise) {
                    string = context.getString(R.string.wifi_security_short_owe);
                } else {
                    string = context.getString(R.string.wifi_security_owe);
                }
                return string;
            default:
                if (concise) {
                    string = "";
                } else {
                    string = context.getString(R.string.wifi_security_none);
                }
                return string;
        }
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public CharSequence getSsid() {
        SpannableString str = new SpannableString(this.ssid);
        str.setSpan(new TelephoneBuilder(this.ssid).build(), 0, this.ssid.length(), 18);
        return str;
    }

    public String getConfigName() {
        if (this.mConfig != null && this.mConfig.isPasspoint()) {
            return this.mConfig.providerFriendlyName;
        }
        if (this.mFqdn != null) {
            return this.mProviderFriendlyName;
        }
        return this.ssid;
    }

    public DetailedState getDetailedState() {
        if (this.mNetworkInfo != null) {
            return this.mNetworkInfo.getDetailedState();
        }
        Log.w(TAG, "NetworkInfo is null, cannot return detailed state");
        return null;
    }

    public boolean isCarrierAp() {
        return this.mIsCarrierAp;
    }

    public int getCarrierApEapType() {
        return this.mCarrierApEapType;
    }

    public String getCarrierName() {
        return this.mCarrierName;
    }

    public String getSavedNetworkSummary() {
        WifiConfiguration config = this.mConfig;
        if (config != null) {
            String systemName = this.mContext.getPackageManager().getNameForUid(1000);
            int userId = UserHandle.getUserId(config.creatorUid);
            ApplicationInfo appInfo = null;
            if (config.creatorName == null || !config.creatorName.equals(systemName)) {
                try {
                    appInfo = AppGlobals.getPackageManager().getApplicationInfo(config.creatorName, 0, userId);
                } catch (RemoteException e) {
                }
            } else {
                appInfo = this.mContext.getApplicationInfo();
            }
            if (!(appInfo == null || appInfo.packageName.equals(this.mContext.getString(R.string.settings_package)) || appInfo.packageName.equals(this.mContext.getString(R.string.certinstaller_package)))) {
                return this.mContext.getString(R.string.saved_network, new Object[]{appInfo.loadLabel(pm)});
            }
        }
        return "";
    }

    public String getSummary() {
        return getSettingsSummary(this.mConfig);
    }

    public String getSettingsSummary() {
        return getSettingsSummary(this.mConfig);
    }

    private String getSettingsSummary(WifiConfiguration config) {
        StringBuilder summary = new StringBuilder();
        if (isActive() && config != null && config.isPasspoint()) {
            summary.append(getSummary(this.mContext, getDetailedState(), false, config.providerFriendlyName));
        } else if (isActive() && config != null && getDetailedState() == DetailedState.CONNECTED && this.mIsCarrierAp) {
            summary.append(String.format(this.mContext.getString(R.string.connected_via_carrier), new Object[]{this.mCarrierName}));
        } else if (isActive()) {
            Context context = this.mContext;
            DetailedState detailedState = getDetailedState();
            boolean z = this.mInfo != null && this.mInfo.isEphemeral();
            summary.append(getSummary(context, detailedState, z));
        } else if (config != null && config.isPasspoint() && config.getNetworkSelectionStatus().isNetworkEnabled()) {
            summary.append(String.format(this.mContext.getString(R.string.available_via_passpoint), new Object[]{config.providerFriendlyName}));
        } else if (config != null && config.hasNoInternetAccess()) {
            int messageID;
            if (config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled()) {
                messageID = R.string.wifi_no_internet_no_reconnect;
            } else {
                messageID = R.string.wifi_no_internet;
            }
            summary.append(this.mContext.getString(messageID));
        } else if (config != null && !config.getNetworkSelectionStatus().isNetworkEnabled()) {
            int networkSelectionDisableReason = config.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
            if (networkSelectionDisableReason != 13) {
                switch (networkSelectionDisableReason) {
                    case 2:
                        summary.append(this.mContext.getString(R.string.wifi_disabled_generic));
                        break;
                    case 3:
                        summary.append(this.mContext.getString(R.string.wifi_disabled_password_failure));
                        break;
                    case 4:
                    case 5:
                        summary.append(this.mContext.getString(R.string.wifi_disabled_network_failure));
                        break;
                }
            }
            summary.append(this.mContext.getString(R.string.wifi_check_password_try_again));
        } else if (config != null && config.getNetworkSelectionStatus().isNotRecommended()) {
            summary.append(this.mContext.getString(R.string.wifi_disabled_by_recommendation_provider));
        } else if (this.mIsCarrierAp) {
            summary.append(String.format(this.mContext.getString(R.string.available_via_carrier), new Object[]{this.mCarrierName}));
        } else if (!isReachable()) {
            summary.append(this.mContext.getString(R.string.wifi_not_in_range));
        } else if (config != null) {
            if (config.recentFailure.getAssociationStatus() != 17) {
                summary.append(this.mContext.getString(R.string.wifi_remembered));
            } else {
                summary.append(this.mContext.getString(R.string.wifi_ap_unable_to_handle_new_sta));
            }
        }
        if (isVerboseLoggingEnabled()) {
            summary.append(WifiUtils.buildLoggingSummary(this, config));
        }
        if (config != null && (WifiUtils.isMeteredOverridden(config) || config.meteredHint)) {
            return this.mContext.getResources().getString(R.string.preference_summary_default_combination, new Object[]{WifiUtils.getMeteredLabel(this.mContext, config), summary.toString()});
        } else if (getSpeedLabel() != null && summary.length() != 0) {
            return this.mContext.getResources().getString(R.string.preference_summary_default_combination, new Object[]{getSpeedLabel(), summary.toString()});
        } else if (getSpeedLabel() != null) {
            return getSpeedLabel();
        } else {
            return summary.toString();
        }
    }

    public boolean isActive() {
        return (this.mNetworkInfo == null || (this.networkId == -1 && this.mNetworkInfo.getState() == State.DISCONNECTED)) ? false : true;
    }

    public boolean isConnectable() {
        return getLevel() != -1 && getDetailedState() == null;
    }

    public boolean isEphemeral() {
        return (this.mInfo == null || !this.mInfo.isEphemeral() || this.mNetworkInfo == null || this.mNetworkInfo.getState() == State.DISCONNECTED) ? false : true;
    }

    public boolean isPasspoint() {
        return this.mConfig != null && this.mConfig.isPasspoint();
    }

    public boolean isPasspointConfig() {
        return this.mFqdn != null;
    }

    public boolean isInfoForThisAccessPoint(WifiConfiguration config, WifiInfo info) {
        boolean z = false;
        if (!isPasspoint() && this.networkId != -1) {
            if (this.networkId == info.getNetworkId()) {
                z = true;
            }
            return z;
        } else if (config != null) {
            return matches(config);
        } else {
            if (info.isEphemeral() || config != null) {
                return this.ssid.equals(removeDoubleQuotes(info.getSSID()));
            }
            return false;
        }
    }

    public boolean isSaved() {
        return this.networkId != -1;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public void generateOpenNetworkConfig() {
        if (this.security != 0) {
            throw new IllegalStateException();
        } else if (this.mConfig == null) {
            this.mConfig = new WifiConfiguration();
            this.mConfig.SSID = convertToQuotedString(this.ssid);
            this.mConfig.allowedKeyManagement.set(0);
        }
    }

    public void saveWifiState(Bundle savedState) {
        if (this.ssid != null) {
            savedState.putString(KEY_SSID, getSsidStr());
        }
        savedState.putInt(KEY_SECURITY, this.security);
        savedState.putInt(KEY_SPEED, this.mSpeed);
        savedState.putInt(KEY_PSKTYPE, this.pskType);
        if (this.mConfig != null) {
            savedState.putParcelable(KEY_CONFIG, this.mConfig);
        }
        savedState.putParcelable(KEY_WIFIINFO, this.mInfo);
        savedState.putParcelableArray(KEY_SCANRESULTS, (Parcelable[]) this.mScanResults.toArray(new Parcelable[this.mScanResults.size()]));
        savedState.putParcelableArrayList(KEY_SCOREDNETWORKCACHE, new ArrayList(this.mScoredNetworkCache.values()));
        if (this.mNetworkInfo != null) {
            savedState.putParcelable(KEY_NETWORKINFO, this.mNetworkInfo);
        }
        if (this.mFqdn != null) {
            savedState.putString(KEY_FQDN, this.mFqdn);
        }
        if (this.mProviderFriendlyName != null) {
            savedState.putString(KEY_PROVIDER_FRIENDLY_NAME, this.mProviderFriendlyName);
        }
        savedState.putBoolean(KEY_IS_CARRIER_AP, this.mIsCarrierAp);
        savedState.putInt(KEY_CARRIER_AP_EAP_TYPE, this.mCarrierApEapType);
        savedState.putString(KEY_CARRIER_NAME, this.mCarrierName);
    }

    public void setListener(AccessPointListener listener) {
        this.mAccessPointListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void setScanResults(Collection<ScanResult> scanResults) {
        String key = getKey();
        for (ScanResult result : scanResults) {
            if (!this.mKey.equals(getKey(result))) {
                throw new IllegalArgumentException(String.format("ScanResult %s\nkey of %s did not match current AP key %s", new Object[]{(ScanResult) r1.next(), getKey(result), key}));
            }
        }
        int oldLevel = getLevel();
        this.mScanResults.clear();
        this.mScanResults.addAll(scanResults);
        updateRssi();
        int newLevel = getLevel();
        if (newLevel > 0 && newLevel != oldLevel) {
            updateSpeed();
            ThreadUtils.postOnMainThread(new -$$Lambda$AccessPoint$MkkIS1nUbezHicDMmYnviyiBJyo(this));
        }
        ThreadUtils.postOnMainThread(new -$$Lambda$AccessPoint$0Yq14aFJZLjPMzFGAvglLaxsblI(this));
        if (!scanResults.isEmpty()) {
            ScanResult result2 = (ScanResult) scanResults.iterator().next();
            if (this.security == 2) {
                this.pskType = getPskType(result2);
            }
            this.mIsCarrierAp = result2.isCarrierAp;
            this.mCarrierApEapType = result2.carrierApEapType;
            this.mCarrierName = result2.carrierName;
        }
    }

    public static /* synthetic */ void lambda$setScanResults$1(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onLevelChanged(accessPoint);
        }
    }

    public static /* synthetic */ void lambda$setScanResults$2(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    public boolean update(WifiConfiguration config, WifiInfo info, NetworkInfo networkInfo) {
        boolean updated = false;
        int oldLevel = getLevel();
        if (info != null && isInfoForThisAccessPoint(config, info)) {
            updated = this.mInfo == null;
            if (this.mConfig != config) {
                update(config);
            }
            if (this.mRssi != info.getRssi() && info.getRssi() != -127) {
                this.mRssi = info.getRssi();
                updated = true;
            } else if (!(this.mNetworkInfo == null || networkInfo == null || this.mNetworkInfo.getDetailedState() == networkInfo.getDetailedState())) {
                updated = true;
            }
            this.mInfo = info;
            this.mNetworkInfo = networkInfo;
        } else if (this.mInfo != null) {
            updated = true;
            this.mInfo = null;
            this.mNetworkInfo = null;
        }
        if (updated && this.mAccessPointListener != null) {
            ThreadUtils.postOnMainThread(new -$$Lambda$AccessPoint$S7H59e_8IxpVPy0V68Oc2-zX-rg(this));
            if (oldLevel != getLevel()) {
                ThreadUtils.postOnMainThread(new -$$Lambda$AccessPoint$QW-1Uw0oxoaKqUtEtPO0oPvH5ng(this));
            }
        }
        return updated;
    }

    public static /* synthetic */ void lambda$update$3(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    public static /* synthetic */ void lambda$update$4(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onLevelChanged(accessPoint);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void update(WifiConfiguration config) {
        this.mConfig = config;
        this.networkId = config != null ? config.networkId : -1;
        ThreadUtils.postOnMainThread(new -$$Lambda$AccessPoint$QyP0aXhFuWtm7lmBu1IY3qbfmBA(this));
    }

    public static /* synthetic */ void lambda$update$5(AccessPoint accessPoint) {
        if (accessPoint.mAccessPointListener != null) {
            accessPoint.mAccessPointListener.onAccessPointChanged(accessPoint);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    /* Access modifiers changed, original: 0000 */
    public void setUnreachable() {
        setRssi(UNREACHABLE_RSSI);
    }

    /* Access modifiers changed, original: 0000 */
    public int getSpeed() {
        return this.mSpeed;
    }

    /* Access modifiers changed, original: 0000 */
    public String getSpeedLabel() {
        return getSpeedLabel(this.mSpeed);
    }

    private static int roundToClosestSpeedEnum(int speed) {
        if (speed < 5) {
            return 0;
        }
        if (speed < 7) {
            return 5;
        }
        if (speed < 15) {
            return 10;
        }
        if (speed < 25) {
            return 20;
        }
        return 30;
    }

    /* Access modifiers changed, original: 0000 */
    public String getSpeedLabel(int speed) {
        return getSpeedLabel(this.mContext, speed);
    }

    private static String getSpeedLabel(Context context, int speed) {
        if (speed == 5) {
            return context.getString(R.string.speed_label_slow);
        }
        if (speed == 10) {
            return context.getString(R.string.speed_label_okay);
        }
        if (speed == 20) {
            return context.getString(R.string.speed_label_fast);
        }
        if (speed != 30) {
            return null;
        }
        return context.getString(R.string.speed_label_very_fast);
    }

    public static String getSpeedLabel(Context context, ScoredNetwork scoredNetwork, int rssi) {
        return getSpeedLabel(context, roundToClosestSpeedEnum(scoredNetwork.calculateBadge(rssi)));
    }

    public boolean isReachable() {
        return this.mRssi != UNREACHABLE_RSSI;
    }

    public static String getSummary(Context context, String ssid, DetailedState state, boolean isEphemeral, String passpointProvider) {
        if (state == DetailedState.CONNECTED && ssid == null) {
            if (!TextUtils.isEmpty(passpointProvider)) {
                return String.format(context.getString(R.string.connected_via_passpoint), new Object[]{passpointProvider});
            } else if (isEphemeral) {
                NetworkScorerAppData scorer = ((NetworkScoreManager) context.getSystemService(NetworkScoreManager.class)).getActiveScorer();
                if (scorer == null || scorer.getRecommendationServiceLabel() == null) {
                    return context.getString(R.string.connected_via_network_scorer_default);
                }
                return String.format(context.getString(R.string.connected_via_network_scorer), new Object[]{scorer.getRecommendationServiceLabel()});
            }
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (state == DetailedState.CONNECTED) {
            NetworkCapabilities nc = null;
            try {
                nc = cm.getNetworkCapabilities(Stub.asInterface(ServiceManager.getService("wifi")).getCurrentNetwork());
            } catch (RemoteException e) {
            }
            if (nc != null) {
                if (nc.hasCapability(17)) {
                    return context.getString(context.getResources().getIdentifier("network_available_sign_in", "string", "android"));
                }
                if (!nc.hasCapability(16)) {
                    return context.getString(R.string.wifi_connected_no_internet);
                }
            }
        }
        if (state == null) {
            Log.w(TAG, "state is null, returning empty summary");
            return "";
        }
        String[] formats = context.getResources().getStringArray(ssid == null ? R.array.wifi_status : R.array.wifi_status_with_ssid);
        int index = state.ordinal();
        if (index >= formats.length || formats[index].length() == 0) {
            return "";
        }
        return String.format(formats[index], new Object[]{ssid});
    }

    public static String getSummary(Context context, DetailedState state, boolean isEphemeral) {
        return getSummary(context, null, state, isEphemeral, null);
    }

    public static String getSummary(Context context, DetailedState state, boolean isEphemeral, String passpointProvider) {
        return getSummary(context, null, state, isEphemeral, passpointProvider);
    }

    public static String convertToQuotedString(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"");
        stringBuilder.append(string);
        stringBuilder.append("\"");
        return stringBuilder.toString();
    }

    private static int getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return 3;
        }
        if (wpa2) {
            return 2;
        }
        if (wpa) {
            return 1;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Received abnormal flag string: ");
        stringBuilder.append(result.capabilities);
        Log.w(str, stringBuilder.toString());
        return 0;
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("DPP")) {
            return 6;
        }
        if (result.capabilities.contains("SAE")) {
            return 7;
        }
        if (result.capabilities.contains("OWE")) {
            return 8;
        }
        if (result.capabilities.contains("WEP")) {
            return 1;
        }
        if (result.capabilities.contains("PSK")) {
            return 2;
        }
        if (result.capabilities.contains("EAP")) {
            return 3;
        }
        if (result.capabilities.contains("WAPI-KEY")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT")) {
            return 5;
        }
        return 0;
    }

    static int getSecurity(WifiConfiguration config) {
        int i = 1;
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (config.allowedKeyManagement.get(190)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(191)) {
            return 5;
        }
        if (config.allowedKeyManagement.get(10)) {
            return 6;
        }
        if (config.allowedKeyManagement.get(11)) {
            return 7;
        }
        if (config.allowedKeyManagement.get(12)) {
            return 8;
        }
        if (config.wepKeys[0] == null) {
            i = 0;
        }
        return i;
    }

    public static String securityToString(int security, int pskType) {
        if (security == 1) {
            return "WEP";
        }
        if (security == 2) {
            if (pskType == 1) {
                return "WPA";
            }
            if (pskType == 2) {
                return "WPA2";
            }
            if (pskType == 3) {
                return "WPA_WPA2";
            }
            return "PSK";
        } else if (security == 3) {
            return "EAP";
        } else {
            if (security == 4) {
                return "WAPI-KEY";
            }
            if (security == 5) {
                return "WAPI-CERT";
            }
            if (security == 6) {
                return "DPP";
            }
            if (security == 7) {
                return "SAE";
            }
            if (security == 8) {
                return "OWE";
            }
            return "NONE";
        }
    }

    static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private static boolean isVerboseLoggingEnabled() {
        return WifiTracker.sVerboseLogging || Log.isLoggable(TAG, 2);
    }
}
