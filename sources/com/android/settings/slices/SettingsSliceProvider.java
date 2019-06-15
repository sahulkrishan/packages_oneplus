package com.android.settings.slices;

import android.app.slice.SliceManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import com.android.settings.bluetooth.BluetoothSliceBuilder;
import com.android.settings.location.LocationSliceBuilder;
import com.android.settings.notification.ZenModeSliceBuilder;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wifi.WifiSliceBuilder;
import com.android.settings.wifi.calling.WifiCallingSliceHelper;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.utils.ThreadUtils;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsSliceProvider extends SliceProvider {
    public static final String ACTION_SLIDER_CHANGED = "com.android.settings.slice.action.SLIDER_CHANGED";
    public static final String ACTION_TOGGLE_CHANGED = "com.android.settings.slice.action.TOGGLE_CHANGED";
    public static final String EXTRA_SLICE_KEY = "com.android.settings.slice.extra.key";
    public static final String EXTRA_SLICE_PLATFORM_DEFINED = "com.android.settings.slice.extra.platform";
    public static final String SLICE_AUTHORITY = "com.android.settings.slices";
    private static final String TAG = "SettingsSliceProvider";
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    final Set<Uri> mRegisteredUris = new ArraySet();
    @VisibleForTesting
    Map<Uri, SliceData> mSliceDataCache;
    @VisibleForTesting
    Map<Uri, SliceData> mSliceWeakDataCache;
    @VisibleForTesting
    SlicesDatabaseAccessor mSlicesDatabaseAccessor;

    public SettingsSliceProvider() {
        super("android.permission.READ_SEARCH_INDEXABLES");
    }

    public boolean onCreateSliceProvider() {
        this.mSlicesDatabaseAccessor = new SlicesDatabaseAccessor(getContext());
        this.mSliceDataCache = new ConcurrentHashMap();
        this.mSliceWeakDataCache = new WeakHashMap();
        return true;
    }

    public Uri onMapIntentToUri(Intent intent) {
        try {
            return ((SliceManager) getContext().getSystemService(SliceManager.class)).mapIntentToUri(SliceDeepLinkSpringBoard.parse(intent.getData(), getContext().getPackageName()));
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public void onSlicePinned(Uri sliceUri) {
        if (WifiSliceBuilder.WIFI_URI.equals(sliceUri)) {
            registerIntentToUri(WifiSliceBuilder.INTENT_FILTER, sliceUri);
        } else if (ZenModeSliceBuilder.ZEN_MODE_URI.equals(sliceUri)) {
            registerIntentToUri(ZenModeSliceBuilder.INTENT_FILTER, sliceUri);
        } else if (BluetoothSliceBuilder.BLUETOOTH_URI.equals(sliceUri)) {
            registerIntentToUri(BluetoothSliceBuilder.INTENT_FILTER, sliceUri);
        } else {
            loadSliceInBackground(sliceUri);
        }
    }

    public void onSliceUnpinned(Uri sliceUri) {
        if (this.mRegisteredUris.contains(sliceUri)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unregistering uri broadcast relay: ");
            stringBuilder.append(sliceUri);
            Log.d(str, stringBuilder.toString());
            SliceBroadcastRelay.unregisterReceivers(getContext(), sliceUri);
            this.mRegisteredUris.remove(sliceUri);
        }
        this.mSliceDataCache.remove(sliceUri);
    }

    public Slice onBindSlice(Uri sliceUri) {
        ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        try {
            if (!ThreadUtils.isMainThread()) {
                ThreadPolicy build = new Builder().permitAll().build();
            }
            Slice createWifiCallingSlice;
            if (getBlockedKeys().contains(sliceUri.getLastPathSegment())) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Requested blocked slice with Uri: ");
                stringBuilder.append(sliceUri);
                Log.e(str, stringBuilder.toString());
                StrictMode.setThreadPolicy(oldPolicy);
                return null;
            } else if (WifiCallingSliceHelper.WIFI_CALLING_URI.equals(sliceUri)) {
                createWifiCallingSlice = FeatureFactory.getFactory(getContext()).getSlicesFeatureProvider().getNewWifiCallingSliceHelper(getContext()).createWifiCallingSlice(sliceUri);
                StrictMode.setThreadPolicy(oldPolicy);
                return createWifiCallingSlice;
            } else if (WifiSliceBuilder.WIFI_URI.equals(sliceUri)) {
                createWifiCallingSlice = WifiSliceBuilder.getSlice(getContext());
                StrictMode.setThreadPolicy(oldPolicy);
                return createWifiCallingSlice;
            } else if (ZenModeSliceBuilder.ZEN_MODE_URI.equals(sliceUri)) {
                createWifiCallingSlice = ZenModeSliceBuilder.getSlice(getContext());
                StrictMode.setThreadPolicy(oldPolicy);
                return createWifiCallingSlice;
            } else if (BluetoothSliceBuilder.BLUETOOTH_URI.equals(sliceUri)) {
                createWifiCallingSlice = BluetoothSliceBuilder.getSlice(getContext());
                StrictMode.setThreadPolicy(oldPolicy);
                return createWifiCallingSlice;
            } else if (LocationSliceBuilder.LOCATION_URI.equals(sliceUri)) {
                createWifiCallingSlice = LocationSliceBuilder.getSlice(getContext());
                StrictMode.setThreadPolicy(oldPolicy);
                return createWifiCallingSlice;
            } else {
                SliceData cachedSliceData = (SliceData) this.mSliceWeakDataCache.get(sliceUri);
                Slice sliceStub;
                if (cachedSliceData == null) {
                    loadSliceInBackground(sliceUri);
                    sliceStub = getSliceStub(sliceUri);
                    StrictMode.setThreadPolicy(oldPolicy);
                    return sliceStub;
                }
                if (!this.mSliceDataCache.containsKey(sliceUri)) {
                    this.mSliceWeakDataCache.remove(sliceUri);
                }
                sliceStub = SliceBuilderUtils.buildSlice(getContext(), cachedSliceData);
                StrictMode.setThreadPolicy(oldPolicy);
                return sliceStub;
            }
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public Collection<Uri> onGetSliceDescendants(Uri uri) {
        List<Uri> descendants = new ArrayList();
        if (SliceBuilderUtils.getPathData(uri) != null) {
            descendants.add(uri);
            return descendants;
        }
        String authority = uri.getAuthority();
        String pathPrefix = uri.getPath();
        boolean isPathEmpty = pathPrefix.isEmpty();
        if (isPathEmpty && TextUtils.isEmpty(authority)) {
            List<String> platformKeys = this.mSlicesDatabaseAccessor.getSliceKeys(true);
            List<String> oemKeys = this.mSlicesDatabaseAccessor.getSliceKeys(false);
            descendants.addAll(buildUrisFromKeys(platformKeys, "android.settings.slices"));
            descendants.addAll(buildUrisFromKeys(oemKeys, SLICE_AUTHORITY));
            descendants.addAll(getSpecialCaseUris(true));
            descendants.addAll(getSpecialCaseUris(false));
            return descendants;
        } else if (!isPathEmpty && !TextUtils.equals(pathPrefix, "/action") && !TextUtils.equals(pathPrefix, "/intent")) {
            return descendants;
        } else {
            boolean isPlatformUri = TextUtils.equals(authority, "android.settings.slices");
            descendants.addAll(buildUrisFromKeys(this.mSlicesDatabaseAccessor.getSliceKeys(isPlatformUri), authority));
            descendants.addAll(getSpecialCaseUris(isPlatformUri));
            return descendants;
        }
    }

    private List<Uri> buildUrisFromKeys(List<String> keys, String authority) {
        List<Uri> descendants = new ArrayList();
        Uri.Builder builder = new Uri.Builder().scheme("content").authority(authority).appendPath("action");
        String newUriPathPrefix = "action/";
        for (String key : keys) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("action/");
            stringBuilder.append(key);
            builder.path(stringBuilder.toString());
            descendants.add(builder.build());
        }
        return descendants;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void loadSlice(Uri uri) {
        long startBuildTime = System.currentTimeMillis();
        try {
            SliceData sliceData = this.mSlicesDatabaseAccessor.getSliceDataFromUri(uri);
            IntentFilter filter = SliceBuilderUtils.getPreferenceController(getContext(), sliceData).getIntentFilter();
            if (filter != null) {
                registerIntentToUri(filter, uri);
            }
            if (((SliceManager) getContext().getSystemService(SliceManager.class)).getPinnedSlices().contains(uri)) {
                this.mSliceDataCache.put(uri, sliceData);
            }
            this.mSliceWeakDataCache.put(uri, sliceData);
            getContext().getContentResolver().notifyChange(uri, null);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Built slice (");
            stringBuilder.append(uri);
            stringBuilder.append(") in: ");
            stringBuilder.append(System.currentTimeMillis() - startBuildTime);
            Log.d(str, stringBuilder.toString());
        } catch (IllegalStateException e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Could not get slice data for uri: ");
            stringBuilder2.append(uri);
            Log.e(str2, stringBuilder2.toString(), e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void loadSliceInBackground(Uri uri) {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$SettingsSliceProvider$3mq4GNawZ0Wc-zLrSLnj1f92or0(this, uri));
    }

    public static /* synthetic */ void lambda$loadSliceInBackground$0(SettingsSliceProvider settingsSliceProvider, Uri uri) {
        Log.d(TAG, "postOnBackgroundThread load Slice start");
        settingsSliceProvider.loadSlice(uri);
        Log.d(TAG, "postOnBackgroundThread load Slice end");
    }

    private Slice getSliceStub(Uri uri) {
        return new Slice.Builder(uri).build();
    }

    private List<Uri> getSpecialCaseUris(boolean isPlatformUri) {
        if (isPlatformUri) {
            return getSpecialCasePlatformUris();
        }
        return getSpecialCaseOemUris();
    }

    private List<Uri> getSpecialCasePlatformUris() {
        return Arrays.asList(new Uri[]{WifiSliceBuilder.WIFI_URI, BluetoothSliceBuilder.BLUETOOTH_URI, LocationSliceBuilder.LOCATION_URI});
    }

    private List<Uri> getSpecialCaseOemUris() {
        return Arrays.asList(new Uri[]{ZenModeSliceBuilder.ZEN_MODE_URI});
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void registerIntentToUri(IntentFilter intentFilter, Uri sliceUri) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Registering Uri for broadcast relay: ");
        stringBuilder.append(sliceUri);
        Log.d(str, stringBuilder.toString());
        this.mRegisteredUris.add(sliceUri);
        SliceBroadcastRelay.registerReceiver(getContext(), sliceUri, SliceRelayReceiver.class, intentFilter);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Set<String> getBlockedKeys() {
        String value = Global.getString(getContext().getContentResolver(), "blocked_slices");
        Set<String> set = new ArraySet();
        try {
            this.mParser.setString(value);
            Collections.addAll(set, parseStringArray(value));
            return set;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Bad Settings Slices Whitelist flags", e);
            return set;
        }
    }

    private String[] parseStringArray(String value) {
        if (value != null) {
            String[] parts = value.split(":");
            if (parts.length > 0) {
                return parts;
            }
        }
        return new String[0];
    }
}
