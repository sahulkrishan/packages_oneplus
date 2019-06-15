package com.android.settings.wifi.tether;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

public class TetherService extends Service {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    @VisibleForTesting
    public static final String EXTRA_RESULT = "EntitlementResult";
    private static final String KEY_TETHERS = "currentTethers";
    private static final int MS_PER_HOUR = 3600000;
    private static final String PREFS = "tetherPrefs";
    private static final int RESULT_DEFAULT = 0;
    private static final int RESULT_OK = -1;
    private static final String TAG = "TetherService";
    private static final String TETHER_CHOICE = "TETHER_TYPE";
    private ArrayList<Integer> mCurrentTethers;
    private int mCurrentTypeIndex;
    private HotspotOffReceiver mHotspotReceiver;
    private boolean mInProvisionCheck;
    private ArrayMap<Integer, List<ResultReceiver>> mPendingCallbacks;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (TetherService.DEBUG) {
                String str = TetherService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Got provision result ");
                stringBuilder.append(intent);
                Log.d(str, stringBuilder.toString());
            }
            if (TetherService.this.getResources().getString(17039727).equals(intent.getAction())) {
                if (TetherService.this.mInProvisionCheck) {
                    int checkType = ((Integer) TetherService.this.mCurrentTethers.get(TetherService.this.mCurrentTypeIndex)).intValue();
                    TetherService.this.mInProvisionCheck = false;
                    int result = intent.getIntExtra(TetherService.EXTRA_RESULT, 0);
                    if (result != -1) {
                        switch (checkType) {
                            case 0:
                                TetherService.this.disableWifiTethering();
                                break;
                            case 1:
                                TetherService.this.disableUsbTethering();
                                break;
                            case 2:
                                TetherService.this.disableBtTethering();
                                break;
                        }
                    }
                    TetherService.this.fireCallbacksForType(checkType, result);
                    if (TetherService.access$204(TetherService.this) >= TetherService.this.mCurrentTethers.size()) {
                        TetherService.this.stopSelf();
                    } else {
                        TetherService.this.startProvisioning(TetherService.this.mCurrentTypeIndex);
                    }
                } else {
                    String str2 = TetherService.TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Unexpected provision response ");
                    stringBuilder2.append(intent);
                    Log.e(str2, stringBuilder2.toString());
                }
            }
        }
    };
    private UsageStatsManagerWrapper mUsageManagerWrapper;

    @VisibleForTesting
    public static class UsageStatsManagerWrapper {
        private final UsageStatsManager mUsageStatsManager;

        UsageStatsManagerWrapper(Context context) {
            this.mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        }

        /* Access modifiers changed, original: 0000 */
        public void setAppInactive(String packageName, boolean isInactive) {
            this.mUsageStatsManager.setAppInactive(packageName, isInactive);
        }
    }

    static /* synthetic */ int access$204(TetherService x0) {
        int i = x0.mCurrentTypeIndex + 1;
        x0.mCurrentTypeIndex = i;
        return i;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (DEBUG) {
            Log.d(TAG, "Creating TetherService");
        }
        registerReceiver(this.mReceiver, new IntentFilter(getResources().getString(17039727)), "android.permission.CONNECTIVITY_INTERNAL", null);
        this.mCurrentTethers = stringToTethers(getSharedPreferences(PREFS, 0).getString(KEY_TETHERS, ""));
        this.mCurrentTypeIndex = 0;
        this.mPendingCallbacks = new ArrayMap(3);
        this.mPendingCallbacks.put(Integer.valueOf(0), new ArrayList());
        this.mPendingCallbacks.put(Integer.valueOf(1), new ArrayList());
        this.mPendingCallbacks.put(Integer.valueOf(2), new ArrayList());
        if (this.mUsageManagerWrapper == null) {
            this.mUsageManagerWrapper = new UsageStatsManagerWrapper(this);
        }
        this.mHotspotReceiver = new HotspotOffReceiver(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int type;
        if (intent.hasExtra("extraAddTetherType")) {
            type = intent.getIntExtra("extraAddTetherType", -1);
            ResultReceiver callback = (ResultReceiver) intent.getParcelableExtra("extraProvisionCallback");
            if (callback != null) {
                List<ResultReceiver> callbacksForType = (List) this.mPendingCallbacks.get(Integer.valueOf(type));
                if (callbacksForType != null) {
                    callbacksForType.add(callback);
                } else {
                    callback.send(1, null);
                    stopSelf();
                    return 2;
                }
            }
            if (!this.mCurrentTethers.contains(Integer.valueOf(type))) {
                if (DEBUG) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Adding tether ");
                    stringBuilder.append(type);
                    Log.d(str, stringBuilder.toString());
                }
                this.mCurrentTethers.add(Integer.valueOf(type));
            }
        }
        if (intent.hasExtra("extraRemTetherType")) {
            if (!this.mInProvisionCheck) {
                type = intent.getIntExtra("extraRemTetherType", -1);
                int index = this.mCurrentTethers.indexOf(Integer.valueOf(type));
                if (DEBUG) {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Removing tether ");
                    stringBuilder2.append(type);
                    stringBuilder2.append(", index ");
                    stringBuilder2.append(index);
                    Log.d(str2, stringBuilder2.toString());
                }
                if (index >= 0) {
                    removeTypeAtIndex(index);
                }
                cancelAlarmIfNecessary();
            } else if (DEBUG) {
                Log.d(TAG, "Don't cancel alarm during provisioning");
            }
        }
        if (intent.getBooleanExtra("extraSetAlarm", false) && this.mCurrentTethers.size() == 1) {
            scheduleAlarm();
        }
        if (intent.getBooleanExtra("extraRunProvision", false)) {
            startProvisioning(this.mCurrentTypeIndex);
        } else if (!this.mInProvisionCheck) {
            if (DEBUG) {
                String str3 = TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Stopping self.  startid: ");
                stringBuilder3.append(startId);
                Log.d(str3, stringBuilder3.toString());
            }
            stopSelf();
            return 2;
        }
        return 3;
    }

    public void onDestroy() {
        if (this.mInProvisionCheck) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("TetherService getting destroyed while mid-provisioning");
            stringBuilder.append(this.mCurrentTethers.get(this.mCurrentTypeIndex));
            Log.e(str, stringBuilder.toString());
        }
        getSharedPreferences(PREFS, 0).edit().putString(KEY_TETHERS, tethersToString(this.mCurrentTethers)).commit();
        unregisterReceivers();
        if (DEBUG) {
            Log.d(TAG, "Destroying TetherService");
        }
        super.onDestroy();
    }

    private void unregisterReceivers() {
        unregisterReceiver(this.mReceiver);
        this.mHotspotReceiver.unregister();
    }

    private void removeTypeAtIndex(int index) {
        this.mCurrentTethers.remove(index);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mCurrentTypeIndex: ");
            stringBuilder.append(this.mCurrentTypeIndex);
            Log.d(str, stringBuilder.toString());
        }
        if (index <= this.mCurrentTypeIndex && this.mCurrentTypeIndex > 0) {
            this.mCurrentTypeIndex--;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setHotspotOffReceiver(HotspotOffReceiver receiver) {
        this.mHotspotReceiver = receiver;
    }

    private ArrayList<Integer> stringToTethers(String tethersStr) {
        ArrayList<Integer> ret = new ArrayList();
        if (TextUtils.isEmpty(tethersStr)) {
            return ret;
        }
        String[] tethersSplit = tethersStr.split(",");
        for (String parseInt : tethersSplit) {
            ret.add(Integer.valueOf(Integer.parseInt(parseInt)));
        }
        return ret;
    }

    private String tethersToString(ArrayList<Integer> tethers) {
        StringBuffer buffer = new StringBuffer();
        int N = tethers.size();
        for (int i = 0; i < N; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(tethers.get(i));
        }
        return buffer.toString();
    }

    private void disableWifiTethering() {
        ((ConnectivityManager) getSystemService("connectivity")).stopTethering(0);
    }

    private void disableUsbTethering() {
        ((ConnectivityManager) getSystemService("connectivity")).setUsbTethering(false);
    }

    private void disableBtTethering() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(this, new ServiceListener() {
                public void onServiceDisconnected(int profile) {
                }

                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    ((BluetoothPan) proxy).setBluetoothTethering(false);
                    adapter.closeProfileProxy(5, proxy);
                }
            }, 5);
        }
    }

    private void startProvisioning(int index) {
        if (index < this.mCurrentTethers.size()) {
            Intent intent = getProvisionBroadcastIntent(index);
            setEntitlementAppActive(index);
            if (DEBUG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Sending provisioning broadcast: ");
                stringBuilder.append(intent.getAction());
                stringBuilder.append(" type: ");
                stringBuilder.append(this.mCurrentTethers.get(index));
                Log.d(str, stringBuilder.toString());
            }
            sendBroadcast(intent);
            this.mInProvisionCheck = true;
        }
    }

    private Intent getProvisionBroadcastIntent(int index) {
        Intent intent = new Intent(getResources().getString(17039726));
        intent.putExtra(TETHER_CHOICE, ((Integer) this.mCurrentTethers.get(index)).intValue());
        intent.setFlags(285212672);
        return intent;
    }

    private void setEntitlementAppActive(int index) {
        List<ResolveInfo> resolvers = getPackageManager().queryBroadcastReceivers(getProvisionBroadcastIntent(index), 131072);
        if (resolvers.isEmpty()) {
            Log.e(TAG, "No found BroadcastReceivers for provision intent.");
            return;
        }
        for (ResolveInfo resolver : resolvers) {
            if (resolver.activityInfo.applicationInfo.isSystemApp()) {
                this.mUsageManagerWrapper.setAppInactive(resolver.activityInfo.packageName, false);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void scheduleAlarm() {
        Intent intent = new Intent(this, TetherService.class);
        intent.putExtra("extraRunProvision", true);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(NotificationCompat.CATEGORY_ALARM);
        long periodMs = (long) (MS_PER_HOUR * getResources().getInteger(17694821));
        long firstTime = SystemClock.elapsedRealtime() + periodMs;
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Scheduling alarm at interval ");
            stringBuilder.append(periodMs);
            Log.d(str, stringBuilder.toString());
        }
        alarmManager.setRepeating(3, firstTime, periodMs, pendingIntent);
        this.mHotspotReceiver.register();
    }

    public static void cancelRecheckAlarmIfNecessary(Context context, int type) {
        Intent intent = new Intent(context, TetherService.class);
        intent.putExtra("extraRemTetherType", type);
        context.startService(intent);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void cancelAlarmIfNecessary() {
        if (this.mCurrentTethers.size() != 0) {
            if (DEBUG) {
                Log.d(TAG, "Tethering still active, not cancelling alarm");
            }
            return;
        }
        ((AlarmManager) getSystemService(NotificationCompat.CATEGORY_ALARM)).cancel(PendingIntent.getService(this, 0, new Intent(this, TetherService.class), 0));
        if (DEBUG) {
            Log.d(TAG, "Tethering no longer active, canceling recheck");
        }
        this.mHotspotReceiver.unregister();
    }

    private void fireCallbacksForType(int type, int result) {
        List<ResultReceiver> callbacksForType = (List) this.mPendingCallbacks.get(Integer.valueOf(type));
        if (callbacksForType != null) {
            int errorCode;
            if (result == -1) {
                errorCode = 0;
            } else {
                errorCode = 11;
            }
            for (ResultReceiver callback : callbacksForType) {
                if (DEBUG) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Firing result: ");
                    stringBuilder.append(errorCode);
                    stringBuilder.append(" to callback");
                    Log.d(str, stringBuilder.toString());
                }
                callback.send(errorCode, null);
            }
            callbacksForType.clear();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setUsageStatsManagerWrapper(UsageStatsManagerWrapper wrapper) {
        this.mUsageManagerWrapper = wrapper;
    }
}
