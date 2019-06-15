package com.android.settingslib.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkKey;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.NetworkScoreManager;
import android.net.ScoredNetwork;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.WifiNetworkScoreCache.CacheListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.utils.ThreadUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiTracker implements LifecycleObserver, OnStart, OnStop, OnDestroy {
    private static final long DEFAULT_MAX_CACHED_SCORE_AGE_MILLIS = 1200000;
    private static final long MAX_SCAN_RESULT_AGE_MILLIS = 25000;
    private static final String TAG = "WifiTracker";
    private static final int WIFI_RESCAN_INTERVAL_MS = 10000;
    public static boolean sVerboseLogging;
    private final AtomicBoolean mConnected;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final IntentFilter mFilter;
    @GuardedBy("mLock")
    private final List<AccessPoint> mInternalAccessPoints;
    private WifiInfo mLastInfo;
    private NetworkInfo mLastNetworkInfo;
    private final WifiListenerExecutor mListener;
    private final Object mLock;
    private long mMaxSpeedLabelScoreCacheAge;
    private WifiTrackerNetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    private final NetworkScoreManager mNetworkScoreManager;
    private boolean mNetworkScoringUiEnabled;
    @VisibleForTesting
    final BroadcastReceiver mReceiver;
    private boolean mRegistered;
    @GuardedBy("mLock")
    private final Set<NetworkKey> mRequestedScores;
    private final HashMap<String, ScanResult> mScanResultCache;
    @VisibleForTesting
    Scanner mScanner;
    private WifiNetworkScoreCache mScoreCache;
    private boolean mStaleScanResults;
    private final WifiManager mWifiManager;
    @VisibleForTesting
    Handler mWorkHandler;
    private HandlerThread mWorkThread;

    private static class Multimap<K, V> {
        private final HashMap<K, List<V>> store;

        private Multimap() {
            this.store = new HashMap();
        }

        /* synthetic */ Multimap(AnonymousClass1 x0) {
            this();
        }

        /* Access modifiers changed, original: 0000 */
        public List<V> getAll(K key) {
            List<V> values = (List) this.store.get(key);
            return values != null ? values : Collections.emptyList();
        }

        /* Access modifiers changed, original: 0000 */
        public void put(K key, V val) {
            List<V> curVals = (List) this.store.get(key);
            if (curVals == null) {
                curVals = new ArrayList(3);
                this.store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    @VisibleForTesting
    class Scanner extends Handler {
        static final int MSG_SCAN = 0;
        private int mRetry = 0;

        Scanner() {
        }

        /* Access modifiers changed, original: 0000 */
        public void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void pause() {
            this.mRetry = 0;
            removeMessages(0);
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public boolean isScanning() {
            return hasMessages(0);
        }

        public void handleMessage(Message message) {
            if (message.what == 0) {
                if (WifiTracker.this.mWifiManager.startScan()) {
                    this.mRetry = 0;
                } else {
                    int i = this.mRetry + 1;
                    this.mRetry = i;
                    if (i >= 3) {
                        this.mRetry = 0;
                        if (WifiTracker.this.mContext != null) {
                            Toast.makeText(WifiTracker.this.mContext, R.string.wifi_fail_to_scan, 1).show();
                        }
                        return;
                    }
                }
                sendEmptyMessageDelayed(0, 10000);
            }
        }
    }

    public interface WifiListener {
        void onAccessPointsChanged();

        void onConnectedChanged();

        void onWifiStateChanged(int i);
    }

    private final class WifiTrackerNetworkCallback extends NetworkCallback {
        private WifiTrackerNetworkCallback() {
        }

        /* synthetic */ WifiTrackerNetworkCallback(WifiTracker x0, AnonymousClass1 x1) {
            this();
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
            if (network.equals(WifiTracker.this.mWifiManager.getCurrentNetwork())) {
                WifiTracker.this.updateNetworkInfo(null);
            }
        }
    }

    @VisibleForTesting
    class WifiListenerExecutor implements WifiListener {
        private final WifiListener mDelegatee;

        public WifiListenerExecutor(WifiListener listener) {
            this.mDelegatee = listener;
        }

        public void onWifiStateChanged(int state) {
            runAndLog(new -$$Lambda$WifiTracker$WifiListenerExecutor$PZBvWEzpVHhaI95PbZNbzEgAH1I(this, state), String.format("Invoking onWifiStateChanged callback with state %d", new Object[]{Integer.valueOf(state)}));
        }

        public void onConnectedChanged() {
            WifiListener wifiListener = this.mDelegatee;
            Objects.requireNonNull(wifiListener);
            runAndLog(new -$$Lambda$6PbPNXCvqbAnKbPWPJrs-dDWQEQ(wifiListener), "Invoking onConnectedChanged callback");
        }

        public void onAccessPointsChanged() {
            WifiListener wifiListener = this.mDelegatee;
            Objects.requireNonNull(wifiListener);
            runAndLog(new -$$Lambda$evcvquoPxZkPmBIit31UXvhXEJk(wifiListener), "Invoking onAccessPointsChanged callback");
        }

        private void runAndLog(Runnable r, String verboseLog) {
            ThreadUtils.postOnMainThread(new -$$Lambda$WifiTracker$WifiListenerExecutor$BMWc3s6WnR_Ijg_9a3gQADAjI3Y(this, verboseLog, r));
        }

        public static /* synthetic */ void lambda$runAndLog$1(WifiListenerExecutor wifiListenerExecutor, String verboseLog, Runnable r) {
            if (WifiTracker.this.mRegistered) {
                if (WifiTracker.isVerboseLoggingEnabled()) {
                    Log.i(WifiTracker.TAG, verboseLog);
                }
                r.run();
            }
        }
    }

    private static final boolean DBG() {
        return Log.isLoggable(TAG, 3);
    }

    private static boolean isVerboseLoggingEnabled() {
        return sVerboseLogging || Log.isLoggable(TAG, 2);
    }

    private static IntentFilter newIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        filter.addAction("android.net.wifi.NETWORK_IDS_CHANGED");
        filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        filter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        filter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.RSSI_CHANGED");
        filter.addAction("android.intent.action.AUTH_PASSWORD_WRONG");
        return filter;
    }

    @Deprecated
    public WifiTracker(Context context, WifiListener wifiListener, boolean includeSaved, boolean includeScans) {
        this(context, wifiListener, (WifiManager) context.getSystemService(WifiManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), (NetworkScoreManager) context.getSystemService(NetworkScoreManager.class), newIntentFilter());
    }

    public WifiTracker(Context context, WifiListener wifiListener, @NonNull Lifecycle lifecycle, boolean includeSaved, boolean includeScans) {
        this(context, wifiListener, (WifiManager) context.getSystemService(WifiManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), (NetworkScoreManager) context.getSystemService(NetworkScoreManager.class), newIntentFilter());
        lifecycle.addObserver(this);
    }

    @VisibleForTesting
    WifiTracker(Context context, WifiListener wifiListener, WifiManager wifiManager, ConnectivityManager connectivityManager, NetworkScoreManager networkScoreManager, IntentFilter filter) {
        this.mConnected = new AtomicBoolean(false);
        this.mLock = new Object();
        this.mInternalAccessPoints = new ArrayList();
        this.mRequestedScores = new ArraySet();
        this.mStaleScanResults = true;
        this.mScanResultCache = new HashMap();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                WifiTracker.sVerboseLogging = WifiTracker.this.mWifiManager.getVerboseLoggingLevel() > 0;
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    WifiTracker.this.updateWifiState(intent.getIntExtra("wifi_state", 4));
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                    WifiTracker.this.mStaleScanResults = false;
                    WifiTracker.this.fetchScansAndConfigsAndUpdateAccessPoints();
                } else if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action) || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(action)) {
                    WifiTracker.this.fetchScansAndConfigsAndUpdateAccessPoints();
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    WifiTracker.this.updateNetworkInfo((NetworkInfo) intent.getParcelableExtra("networkInfo"));
                    WifiTracker.this.fetchScansAndConfigsAndUpdateAccessPoints();
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    WifiTracker.this.updateNetworkInfo(WifiTracker.this.mConnectivityManager.getNetworkInfo(WifiTracker.this.mWifiManager.getCurrentNetwork()));
                } else if ("android.intent.action.AUTH_PASSWORD_WRONG".equals(action)) {
                    Toast.makeText(context, R.string.wifi_auth_password_wrong, 0).show();
                }
            }
        };
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mListener = new WifiListenerExecutor(wifiListener);
        this.mConnectivityManager = connectivityManager;
        this.mFilter = filter;
        this.mNetworkRequest = new Builder().clearCapabilities().addCapability(15).addTransportType(1).build();
        this.mNetworkScoreManager = networkScoreManager;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("WifiTracker{");
        stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
        stringBuilder.append("}");
        HandlerThread workThread = new HandlerThread(stringBuilder.toString(), 10);
        workThread.start();
        setWorkThread(workThread);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setWorkThread(HandlerThread workThread) {
        this.mWorkThread = workThread;
        this.mWorkHandler = new Handler(workThread.getLooper());
        this.mScoreCache = new WifiNetworkScoreCache(this.mContext, new CacheListener(this.mWorkHandler) {
            public void networkCacheUpdated(List<ScoredNetwork> networks) {
                if (WifiTracker.this.mRegistered) {
                    if (Log.isLoggable(WifiTracker.TAG, 2)) {
                        String str = WifiTracker.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Score cache was updated with networks: ");
                        stringBuilder.append(networks);
                        Log.v(str, stringBuilder.toString());
                    }
                    WifiTracker.this.updateNetworkScores();
                }
            }
        });
    }

    public void onDestroy() {
        this.mWorkThread.quit();
    }

    private void pauseScanning() {
        if (this.mScanner != null) {
            this.mScanner.pause();
            this.mScanner = null;
        }
        this.mStaleScanResults = true;
    }

    public void resumeScanning() {
        if (this.mScanner == null) {
            this.mScanner = new Scanner();
        }
        if (this.mWifiManager.isWifiEnabled()) {
            this.mScanner.resume();
        }
    }

    public void onStart() {
        forceUpdate();
        registerScoreCache();
        boolean z = false;
        if (Global.getInt(this.mContext.getContentResolver(), "network_scoring_ui_enabled", 0) == 1) {
            z = true;
        }
        this.mNetworkScoringUiEnabled = z;
        this.mMaxSpeedLabelScoreCacheAge = Global.getLong(this.mContext.getContentResolver(), "speed_label_cache_eviction_age_millis", DEFAULT_MAX_CACHED_SCORE_AGE_MILLIS);
        resumeScanning();
        if (!this.mRegistered) {
            this.mContext.registerReceiver(this.mReceiver, this.mFilter, null, this.mWorkHandler);
            this.mNetworkCallback = new WifiTrackerNetworkCallback(this, null);
            this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback, this.mWorkHandler);
            this.mRegistered = true;
        }
    }

    private void forceUpdate() {
        this.mLastInfo = this.mWifiManager.getConnectionInfo();
        try {
            this.mLastNetworkInfo = this.mConnectivityManager.getNetworkInfo(this.mWifiManager.getCurrentNetwork());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        fetchScansAndConfigsAndUpdateAccessPoints();
    }

    private void registerScoreCache() {
        this.mNetworkScoreManager.registerNetworkScoreCache(1, this.mScoreCache, 2);
    }

    private void requestScoresForNetworkKeys(Collection<NetworkKey> keys) {
        if (!keys.isEmpty()) {
            if (DBG()) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Requesting scores for Network Keys: ");
                stringBuilder.append(keys);
                Log.d(str, stringBuilder.toString());
            }
            this.mNetworkScoreManager.requestScores((NetworkKey[]) keys.toArray(new NetworkKey[keys.size()]));
            synchronized (this.mLock) {
                this.mRequestedScores.addAll(keys);
            }
        }
    }

    public void onStop() {
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
            this.mRegistered = false;
        }
        unregisterScoreCache();
        pauseScanning();
        this.mWorkHandler.removeCallbacksAndMessages(null);
    }

    private void unregisterScoreCache() {
        this.mNetworkScoreManager.unregisterNetworkScoreCache(1, this.mScoreCache);
        synchronized (this.mLock) {
            this.mRequestedScores.clear();
        }
    }

    public List<AccessPoint> getAccessPoints() {
        ArrayList arrayList;
        synchronized (this.mLock) {
            arrayList = new ArrayList(this.mInternalAccessPoints);
        }
        return arrayList;
    }

    public WifiManager getManager() {
        return this.mWifiManager;
    }

    public boolean isWifiEnabled() {
        return this.mWifiManager.isWifiEnabled();
    }

    public int getNumSavedNetworks() {
        return WifiSavedConfigUtils.getAllConfigs(this.mContext, this.mWifiManager).size();
    }

    public boolean isConnected() {
        return this.mConnected.get();
    }

    public void dump(PrintWriter pw) {
        pw.println("  - wifi tracker ------");
        for (AccessPoint accessPoint : getAccessPoints()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("  ");
            stringBuilder.append(accessPoint);
            pw.println(stringBuilder.toString());
        }
    }

    private ArrayMap<String, List<ScanResult>> updateScanResultCache(List<ScanResult> newResults) {
        for (ScanResult newResult : newResults) {
            if (newResult.SSID != null) {
                if (!newResult.SSID.isEmpty()) {
                    this.mScanResultCache.put(newResult.BSSID, newResult);
                }
            }
        }
        if (!this.mStaleScanResults) {
            evictOldScans();
        }
        ArrayMap<String, List<ScanResult>> scanResultsByApKey = new ArrayMap();
        for (ScanResult result : this.mScanResultCache.values()) {
            if (!(result.SSID == null || result.SSID.length() == 0)) {
                if (!result.capabilities.contains("[IBSS]")) {
                    List<ScanResult> resultList;
                    String apKey = AccessPoint.getKey(result);
                    if (scanResultsByApKey.containsKey(apKey)) {
                        resultList = (List) scanResultsByApKey.get(apKey);
                    } else {
                        resultList = new ArrayList();
                        scanResultsByApKey.put(apKey, resultList);
                    }
                    resultList.add(result);
                }
            }
        }
        return scanResultsByApKey;
    }

    private void evictOldScans() {
        long nowMs = SystemClock.elapsedRealtime();
        Iterator<ScanResult> iter = this.mScanResultCache.values().iterator();
        while (iter.hasNext()) {
            if (nowMs - (((ScanResult) iter.next()).timestamp / 1000) > MAX_SCAN_RESULT_AGE_MILLIS) {
                iter.remove();
            }
        }
    }

    private WifiConfiguration getWifiConfigurationForNetworkId(int networkId, List<WifiConfiguration> configs) {
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (this.mLastInfo != null && networkId == config.networkId && (!config.selfAdded || config.numAssociation != 0)) {
                    return config;
                }
            }
        }
        return null;
    }

    private void fetchScansAndConfigsAndUpdateAccessPoints() {
        List<ScanResult> newScanResults = this.mWifiManager.getScanResults();
        if (isVerboseLoggingEnabled()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Fetched scan results: ");
            stringBuilder.append(newScanResults);
            Log.i(str, stringBuilder.toString());
        }
        updateAccessPoints(newScanResults, this.mWifiManager.getConfiguredNetworks());
    }

    private void updateAccessPoints(List<ScanResult> newScanResults, List<WifiConfiguration> configs) {
        List<WifiConfiguration> list = configs;
        Multimap<String, AccessPoint> apMap = new Multimap();
        ArrayMap<String, List<ScanResult>> scanResultsByApKey = updateScanResultCache(newScanResults);
        WifiConfiguration connectionConfig = null;
        if (this.mLastInfo != null) {
            connectionConfig = getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId(), list);
        }
        WifiConfiguration connectionConfig2 = connectionConfig;
        synchronized (this.mLock) {
            Iterator it;
            List<AccessPoint> cachedAccessPoints = new ArrayList(this.mInternalAccessPoints);
            for (AccessPoint accessPoint : cachedAccessPoints) {
                accessPoint.clearConfig();
            }
            ArrayList<AccessPoint> accessPoints = new ArrayList();
            ArrayList scoresToRequest = new ArrayList();
            if (list != null) {
                it = configs.iterator();
                while (it.hasNext()) {
                    WifiConfiguration config = (WifiConfiguration) it.next();
                    if (!config.selfAdded || config.numAssociation != 0) {
                        Iterator it2;
                        AccessPoint accessPoint2 = getCachedOrCreate(config, (List) cachedAccessPoints);
                        if (!(this.mLastInfo == null || this.mLastNetworkInfo == null)) {
                            accessPoint2.update(connectionConfig2, this.mLastInfo, this.mLastNetworkInfo);
                        }
                        boolean apFound = false;
                        for (Entry<String, List<ScanResult>> entry : scanResultsByApKey.entrySet()) {
                            ScanResult result = (ScanResult) ((List) entry.getValue()).get(0);
                            it2 = it;
                            if (result.SSID.equals(accessPoint2.getSsidStr()) && accessPoint2.getSecurity() == AccessPoint.getSecurity(result)) {
                                apFound = true;
                                break;
                            } else {
                                it = it2;
                                list = configs;
                            }
                        }
                        it2 = it;
                        if (!apFound) {
                            accessPoint2.setUnreachable();
                        }
                        accessPoints.add(accessPoint2);
                        apMap.put(accessPoint2.getSsidStr(), accessPoint2);
                        it = it2;
                        list = configs;
                    }
                }
            }
            for (Entry<String, List<ScanResult>> entry2 : scanResultsByApKey.entrySet()) {
                AccessPoint accessPoint3;
                for (ScanResult result2 : (List) entry2.getValue()) {
                    NetworkKey key = NetworkKey.createFromScanResult(result2);
                    if (!(key == null || this.mRequestedScores.contains(key))) {
                        scoresToRequest.add(key);
                    }
                }
                boolean found = false;
                ScanResult result22 = (ScanResult) ((List) entry2.getValue()).get(0);
                for (AccessPoint accessPoint32 : apMap.getAll(result22.SSID)) {
                    if (accessPoint32.matches(result22)) {
                        accessPoint32.setScanResults((Collection) entry2.getValue());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    accessPoint32 = getCachedOrCreate((List) entry2.getValue(), (List) cachedAccessPoints);
                    if (!(this.mLastInfo == null || this.mLastNetworkInfo == null)) {
                        accessPoint32.update(connectionConfig2, this.mLastInfo, this.mLastNetworkInfo);
                    }
                    if (result22.isPasspointNetwork()) {
                        try {
                            connectionConfig = this.mWifiManager.getMatchingWifiConfig(result22);
                            if (connectionConfig != null) {
                                accessPoint32.update(connectionConfig);
                            }
                        } catch (UnsupportedOperationException e) {
                        }
                    }
                    accessPoints.add(accessPoint32);
                    apMap.put(accessPoint32.getSsidStr(), accessPoint32);
                }
            }
            if (accessPoints.isEmpty() && connectionConfig2 != null) {
                AccessPoint activeAp = new AccessPoint(this.mContext, connectionConfig2);
                activeAp.update(connectionConfig2, this.mLastInfo, this.mLastNetworkInfo);
                accessPoints.add(activeAp);
                scoresToRequest.add(NetworkKey.createFromWifiInfo(this.mLastInfo));
            }
            requestScoresForNetworkKeys(scoresToRequest);
            it = accessPoints.iterator();
            while (it.hasNext()) {
                ((AccessPoint) it.next()).update(this.mScoreCache, this.mNetworkScoringUiEnabled, this.mMaxSpeedLabelScoreCacheAge);
            }
            Collections.sort(accessPoints);
            if (DBG()) {
                Log.d(TAG, "------ Dumping SSIDs that were not seen on this scan ------");
                for (AccessPoint prevAccessPoint : this.mInternalAccessPoints) {
                    if (prevAccessPoint.getSsid() != null) {
                        String prevSsid = prevAccessPoint.getSsidStr();
                        boolean found2 = false;
                        Iterator it3 = accessPoints.iterator();
                        while (it3.hasNext()) {
                            AccessPoint newAccessPoint = (AccessPoint) it3.next();
                            if (newAccessPoint.getSsidStr() != null && newAccessPoint.getSsidStr().equals(prevSsid)) {
                                found2 = true;
                                break;
                            }
                        }
                        if (!found2) {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Did not find ");
                            stringBuilder.append(prevSsid);
                            stringBuilder.append(" in this scan");
                            Log.d(str, stringBuilder.toString());
                        }
                    }
                }
                Log.d(TAG, "---- Done dumping SSIDs that were not seen on this scan ----");
            }
            this.mInternalAccessPoints.clear();
            this.mInternalAccessPoints.addAll(accessPoints);
        }
        conditionallyNotifyListeners();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AccessPoint getCachedOrCreate(List<ScanResult> scanResults, List<AccessPoint> cache) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            if (((AccessPoint) cache.get(i)).getKey().equals(AccessPoint.getKey((ScanResult) scanResults.get(0)))) {
                AccessPoint ret = (AccessPoint) cache.remove(i);
                ret.setScanResults(scanResults);
                return ret;
            }
        }
        return new AccessPoint(this.mContext, (Collection) scanResults);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AccessPoint getCachedOrCreate(WifiConfiguration config, List<AccessPoint> cache) {
        int N = cache.size();
        for (int i = 0; i < N; i++) {
            if (((AccessPoint) cache.get(i)).matches(config)) {
                AccessPoint ret = (AccessPoint) cache.remove(i);
                ret.loadConfig(config);
                return ret;
            }
        }
        return new AccessPoint(this.mContext, config);
    }

    private void updateNetworkInfo(NetworkInfo networkInfo) {
        if (!this.mWifiManager.isWifiEnabled()) {
            clearAccessPointsAndConditionallyUpdate();
        } else if (networkInfo != null) {
            this.mLastNetworkInfo = networkInfo;
            if (DBG()) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("mLastNetworkInfo set: ");
                stringBuilder.append(this.mLastNetworkInfo);
                Log.d(str, stringBuilder.toString());
            }
            if (networkInfo.isConnected() != this.mConnected.getAndSet(networkInfo.isConnected())) {
                this.mListener.onConnectedChanged();
            }
        }
        WifiConfiguration connectionConfig = null;
        this.mLastInfo = this.mWifiManager.getConnectionInfo();
        if (DBG()) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("mLastInfo set as: ");
            stringBuilder2.append(this.mLastInfo);
            Log.d(str2, stringBuilder2.toString());
        }
        if (this.mLastInfo != null) {
            connectionConfig = getWifiConfigurationForNetworkId(this.mLastInfo.getNetworkId(), this.mWifiManager.getConfiguredNetworks());
        }
        boolean updated = false;
        boolean reorder = false;
        synchronized (this.mLock) {
            for (int i = this.mInternalAccessPoints.size() - 1; i >= 0; i--) {
                AccessPoint ap = (AccessPoint) this.mInternalAccessPoints.get(i);
                boolean previouslyConnected = ap.isActive();
                if (ap.update(connectionConfig, this.mLastInfo, this.mLastNetworkInfo)) {
                    updated = true;
                    if (previouslyConnected != ap.isActive()) {
                        reorder = true;
                    }
                }
                if (ap.update(this.mScoreCache, this.mNetworkScoringUiEnabled, this.mMaxSpeedLabelScoreCacheAge)) {
                    reorder = true;
                    updated = true;
                }
            }
            if (reorder) {
                Collections.sort(this.mInternalAccessPoints);
            }
            if (updated) {
                conditionallyNotifyListeners();
            }
        }
    }

    private void clearAccessPointsAndConditionallyUpdate() {
        synchronized (this.mLock) {
            if (!this.mInternalAccessPoints.isEmpty()) {
                this.mInternalAccessPoints.clear();
                conditionallyNotifyListeners();
            }
        }
    }

    private void updateNetworkScores() {
        synchronized (this.mLock) {
            boolean updated = false;
            for (int i = 0; i < this.mInternalAccessPoints.size(); i++) {
                if (((AccessPoint) this.mInternalAccessPoints.get(i)).update(this.mScoreCache, this.mNetworkScoringUiEnabled, this.mMaxSpeedLabelScoreCacheAge)) {
                    updated = true;
                }
            }
            if (updated) {
                Collections.sort(this.mInternalAccessPoints);
                conditionallyNotifyListeners();
            }
        }
    }

    private void updateWifiState(int state) {
        if (state != 3) {
            clearAccessPointsAndConditionallyUpdate();
            this.mLastInfo = null;
            this.mLastNetworkInfo = null;
            if (this.mScanner != null) {
                this.mScanner.pause();
            }
            this.mStaleScanResults = true;
        } else if (this.mScanner != null) {
            this.mScanner.resume();
        }
        this.mListener.onWifiStateChanged(state);
    }

    private void conditionallyNotifyListeners() {
        if (!this.mStaleScanResults) {
            this.mListener.onAccessPointsChanged();
        }
    }
}
