package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class CachedBluetoothDeviceManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "CachedBluetoothDeviceManager";
    private final LocalBluetoothManager mBtManager;
    @VisibleForTesting
    final List<CachedBluetoothDevice> mCachedDevices = new ArrayList();
    @VisibleForTesting
    final Map<Long, CachedBluetoothDevice> mCachedDevicesMapForHearingAids = new HashMap();
    private Context mContext;
    @VisibleForTesting
    final List<CachedBluetoothDevice> mHearingAidDevicesNotAddedInCache = new ArrayList();

    CachedBluetoothDeviceManager(Context context, LocalBluetoothManager localBtManager) {
        this.mContext = context;
        this.mBtManager = localBtManager;
    }

    public synchronized Collection<CachedBluetoothDevice> getCachedDevicesCopy() {
        return new ArrayList(this.mCachedDevices);
    }

    public static boolean onDeviceDisappeared(CachedBluetoothDevice cachedDevice) {
        cachedDevice.setJustDiscovered(false);
        if (cachedDevice.getBondState() == 10) {
            return true;
        }
        return false;
    }

    public void onDeviceNameUpdated(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.refreshName();
        }
    }

    public synchronized CachedBluetoothDevice findDevice(BluetoothDevice device) {
        for (CachedBluetoothDevice cachedDevice : this.mCachedDevices) {
            if (cachedDevice != null && device != null && cachedDevice.getAddress().equals(device.getAddress())) {
                return cachedDevice;
            }
        }
        for (CachedBluetoothDevice cachedDevice2 : this.mHearingAidDevicesNotAddedInCache) {
            if (cachedDevice2 != null && device != null && cachedDevice2.getAddress().equals(device.getAddress())) {
                return cachedDevice2;
            }
        }
        return null;
    }

    public CachedBluetoothDevice addDevice(LocalBluetoothAdapter adapter, LocalBluetoothProfileManager profileManager, BluetoothDevice device) {
        CachedBluetoothDevice newDevice = new CachedBluetoothDevice(this.mContext, adapter, profileManager, device);
        if (!(profileManager.getHearingAidProfile() == null || profileManager.getHearingAidProfile().getHiSyncId(newDevice.getDevice()) == 0)) {
            newDevice.setHiSyncId(profileManager.getHearingAidProfile().getHiSyncId(newDevice.getDevice()));
        }
        if (isPairAddedInCache(newDevice.getHiSyncId())) {
            synchronized (this) {
                this.mHearingAidDevicesNotAddedInCache.add(newDevice);
            }
        } else {
            synchronized (this) {
                this.mCachedDevices.add(newDevice);
                if (!(newDevice.getHiSyncId() == 0 || this.mCachedDevicesMapForHearingAids.containsKey(Long.valueOf(newDevice.getHiSyncId())))) {
                    this.mCachedDevicesMapForHearingAids.put(Long.valueOf(newDevice.getHiSyncId()), newDevice);
                }
                this.mBtManager.getEventManager().dispatchDeviceAdded(newDevice);
            }
        }
        return newDevice;
    }

    private synchronized boolean isPairAddedInCache(long hiSyncId) {
        if (hiSyncId == 0) {
            return false;
        }
        if (this.mCachedDevicesMapForHearingAids.containsKey(Long.valueOf(hiSyncId))) {
            return true;
        }
        return false;
    }

    public synchronized String getHearingAidPairDeviceSummary(CachedBluetoothDevice device) {
        String pairDeviceSummary;
        pairDeviceSummary = null;
        if (device.getHiSyncId() != 0) {
            for (CachedBluetoothDevice hearingAidDevice : this.mHearingAidDevicesNotAddedInCache) {
                if (hearingAidDevice.getHiSyncId() != 0 && hearingAidDevice.getHiSyncId() == device.getHiSyncId()) {
                    pairDeviceSummary = hearingAidDevice.getConnectionSummary();
                }
            }
        }
        return pairDeviceSummary;
    }

    public synchronized void addDeviceNotaddedInMap(CachedBluetoothDevice device) {
        this.mHearingAidDevicesNotAddedInCache.add(device);
    }

    public synchronized void updateHearingAidsDevices(LocalBluetoothProfileManager profileManager) {
        HearingAidProfile profileProxy = profileManager.getHearingAidProfile();
        if (profileProxy == null) {
            log("updateHearingAidsDevices: getHearingAidProfile() is null");
            return;
        }
        Set<Long> syncIdChangedSet = new HashSet();
        for (CachedBluetoothDevice cachedDevice : this.mCachedDevices) {
            if (cachedDevice.getHiSyncId() == 0) {
                long newHiSyncId = profileProxy.getHiSyncId(cachedDevice.getDevice());
                if (newHiSyncId != 0) {
                    cachedDevice.setHiSyncId(newHiSyncId);
                    syncIdChangedSet.add(Long.valueOf(newHiSyncId));
                }
            }
        }
        for (Long syncId : syncIdChangedSet) {
            onHiSyncIdChanged(syncId.longValue());
        }
    }

    public String getName(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null && cachedDevice.getName() != null) {
            return cachedDevice.getName();
        }
        String name = device.getAliasName();
        if (name != null) {
            return name;
        }
        return device.getAddress();
    }

    public synchronized void clearNonBondedDevices() {
        this.mCachedDevicesMapForHearingAids.entrySet().removeIf(-$$Lambda$CachedBluetoothDeviceManager$e-LCWrNLhjsTC176L1agksvai7c.INSTANCE);
        this.mCachedDevices.removeIf(-$$Lambda$CachedBluetoothDeviceManager$kt27ylP2LAAkpFyw4Jk0DP1n8j4.INSTANCE);
        this.mHearingAidDevicesNotAddedInCache.removeIf(-$$Lambda$CachedBluetoothDeviceManager$RiRiNHhShon-xX_yLpIJ83GKKko.INSTANCE);
    }

    static /* synthetic */ boolean lambda$clearNonBondedDevices$0(Entry entries) {
        return ((CachedBluetoothDevice) entries.getValue()).getBondState() == 10;
    }

    static /* synthetic */ boolean lambda$clearNonBondedDevices$1(CachedBluetoothDevice cachedDevice) {
        return cachedDevice.getBondState() == 10;
    }

    static /* synthetic */ boolean lambda$clearNonBondedDevices$2(CachedBluetoothDevice hearingAidDevice) {
        return hearingAidDevice.getBondState() == 10;
    }

    public synchronized void clearAllDevices() {
        for (int i = this.mCachedDevices.size() - 1; i >= 0; i--) {
            CachedBluetoothDevice cachedDevice = (CachedBluetoothDevice) this.mCachedDevices.get(i);
            this.mCachedDevices.remove(i);
        }
    }

    public synchronized void onScanningStateChanged(boolean started) {
        if (started) {
            int i;
            for (i = this.mCachedDevices.size() - 1; i >= 0; i--) {
                ((CachedBluetoothDevice) this.mCachedDevices.get(i)).setJustDiscovered(false);
            }
            for (i = this.mHearingAidDevicesNotAddedInCache.size() - 1; i >= 0; i--) {
                ((CachedBluetoothDevice) this.mHearingAidDevicesNotAddedInCache.get(i)).setJustDiscovered(false);
            }
        }
    }

    public synchronized void onBtClassChanged(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.refreshBtClass();
        }
    }

    public synchronized void onUuidChanged(BluetoothDevice device) {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null) {
            cachedDevice.onUuidChanged();
        }
    }

    public synchronized void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == 13) {
            int i;
            CachedBluetoothDevice cachedDevice;
            for (i = this.mCachedDevices.size() - 1; i >= 0; i--) {
                cachedDevice = (CachedBluetoothDevice) this.mCachedDevices.get(i);
                if (cachedDevice.getBondState() != 12) {
                    cachedDevice.setJustDiscovered(false);
                    this.mCachedDevices.remove(i);
                    if (cachedDevice.getHiSyncId() != 0 && this.mCachedDevicesMapForHearingAids.containsKey(Long.valueOf(cachedDevice.getHiSyncId()))) {
                        this.mCachedDevicesMapForHearingAids.remove(Long.valueOf(cachedDevice.getHiSyncId()));
                    }
                } else {
                    cachedDevice.clearProfileConnectionState();
                }
            }
            for (i = this.mHearingAidDevicesNotAddedInCache.size() - 1; i >= 0; i--) {
                cachedDevice = (CachedBluetoothDevice) this.mHearingAidDevicesNotAddedInCache.get(i);
                if (cachedDevice.getBondState() != 12) {
                    cachedDevice.setJustDiscovered(false);
                    this.mHearingAidDevicesNotAddedInCache.remove(i);
                } else {
                    cachedDevice.clearProfileConnectionState();
                }
            }
        }
    }

    public synchronized void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
        for (CachedBluetoothDevice cachedDevice : this.mCachedDevices) {
            cachedDevice.onActiveDeviceChanged(Objects.equals(cachedDevice, activeDevice), bluetoothProfile);
        }
    }

    public synchronized void onHiSyncIdChanged(long hiSyncId) {
        int firstMatchedIndex = -1;
        for (int i = this.mCachedDevices.size() - 1; i >= 0; i--) {
            CachedBluetoothDevice cachedDevice = (CachedBluetoothDevice) this.mCachedDevices.get(i);
            if (cachedDevice.getHiSyncId() == hiSyncId) {
                if (firstMatchedIndex != -1) {
                    int indexToRemoveFromUi;
                    CachedBluetoothDevice deviceToRemoveFromUi;
                    if (cachedDevice.isConnected()) {
                        indexToRemoveFromUi = firstMatchedIndex;
                        deviceToRemoveFromUi = (CachedBluetoothDevice) this.mCachedDevices.get(firstMatchedIndex);
                        this.mCachedDevicesMapForHearingAids.put(Long.valueOf(hiSyncId), cachedDevice);
                    } else {
                        indexToRemoveFromUi = i;
                        deviceToRemoveFromUi = cachedDevice;
                        this.mCachedDevicesMapForHearingAids.put(Long.valueOf(hiSyncId), (CachedBluetoothDevice) this.mCachedDevices.get(firstMatchedIndex));
                    }
                    this.mCachedDevices.remove(indexToRemoveFromUi);
                    this.mHearingAidDevicesNotAddedInCache.add(deviceToRemoveFromUi);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onHiSyncIdChanged: removed from UI device=");
                    stringBuilder.append(deviceToRemoveFromUi);
                    stringBuilder.append(", with hiSyncId=");
                    stringBuilder.append(hiSyncId);
                    log(stringBuilder.toString());
                    this.mBtManager.getEventManager().dispatchDeviceRemoved(deviceToRemoveFromUi);
                } else {
                    this.mCachedDevicesMapForHearingAids.put(Long.valueOf(hiSyncId), cachedDevice);
                    firstMatchedIndex = i;
                }
            }
        }
    }

    private CachedBluetoothDevice getHearingAidOtherDevice(CachedBluetoothDevice thisDevice, long hiSyncId) {
        if (hiSyncId == 0) {
            return null;
        }
        for (CachedBluetoothDevice notCachedDevice : this.mHearingAidDevicesNotAddedInCache) {
            if (hiSyncId == notCachedDevice.getHiSyncId() && !Objects.equals(notCachedDevice, thisDevice)) {
                return notCachedDevice;
            }
        }
        CachedBluetoothDevice cachedDevice = (CachedBluetoothDevice) this.mCachedDevicesMapForHearingAids.get(Long.valueOf(hiSyncId));
        if (Objects.equals(cachedDevice, thisDevice)) {
            return null;
        }
        return cachedDevice;
    }

    private void hearingAidSwitchDisplayDevice(CachedBluetoothDevice toDisplayDevice, CachedBluetoothDevice toHideDevice, long hiSyncId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("hearingAidSwitchDisplayDevice: toDisplayDevice=");
        stringBuilder.append(toDisplayDevice);
        stringBuilder.append(", toHideDevice=");
        stringBuilder.append(toHideDevice);
        log(stringBuilder.toString());
        this.mHearingAidDevicesNotAddedInCache.add(toHideDevice);
        this.mCachedDevices.remove(toHideDevice);
        this.mBtManager.getEventManager().dispatchDeviceRemoved(toHideDevice);
        this.mHearingAidDevicesNotAddedInCache.remove(toDisplayDevice);
        this.mCachedDevices.add(toDisplayDevice);
        this.mCachedDevicesMapForHearingAids.put(Long.valueOf(hiSyncId), toDisplayDevice);
        this.mBtManager.getEventManager().dispatchDeviceAdded(toDisplayDevice);
    }

    /* JADX WARNING: Missing block: B:30:0x0056, code skipped:
            return;
     */
    public synchronized void onProfileConnectionStateChanged(com.android.settingslib.bluetooth.CachedBluetoothDevice r6, int r7, int r8) {
        /*
        r5 = this;
        monitor-enter(r5);
        r0 = 21;
        if (r8 != r0) goto L_0x0055;
    L_0x0005:
        r0 = r6.getHiSyncId();	 Catch:{ all -> 0x0052 }
        r2 = 0;
        r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r0 == 0) goto L_0x0055;
    L_0x000f:
        r0 = r6.getBondState();	 Catch:{ all -> 0x0052 }
        r1 = 12;
        if (r0 != r1) goto L_0x0055;
    L_0x0017:
        r0 = r6.getHiSyncId();	 Catch:{ all -> 0x0052 }
        r2 = r5.getHearingAidOtherDevice(r6, r0);	 Catch:{ all -> 0x0052 }
        if (r2 != 0) goto L_0x0023;
    L_0x0021:
        monitor-exit(r5);
        return;
    L_0x0023:
        r3 = 2;
        if (r7 != r3) goto L_0x0032;
    L_0x0026:
        r3 = r5.mHearingAidDevicesNotAddedInCache;	 Catch:{ all -> 0x0052 }
        r3 = r3.contains(r6);	 Catch:{ all -> 0x0052 }
        if (r3 == 0) goto L_0x0032;
    L_0x002e:
        r5.hearingAidSwitchDisplayDevice(r6, r2, r0);	 Catch:{ all -> 0x0052 }
        goto L_0x0055;
    L_0x0032:
        if (r7 != 0) goto L_0x0055;
    L_0x0034:
        r3 = r2.isConnected();	 Catch:{ all -> 0x0052 }
        if (r3 == 0) goto L_0x0055;
    L_0x003a:
        r3 = r5.mCachedDevicesMapForHearingAids;	 Catch:{ all -> 0x0052 }
        r4 = java.lang.Long.valueOf(r0);	 Catch:{ all -> 0x0052 }
        r3 = r3.get(r4);	 Catch:{ all -> 0x0052 }
        r3 = (com.android.settingslib.bluetooth.CachedBluetoothDevice) r3;	 Catch:{ all -> 0x0052 }
        if (r3 == 0) goto L_0x0055;
    L_0x0048:
        r4 = java.util.Objects.equals(r6, r3);	 Catch:{ all -> 0x0052 }
        if (r4 == 0) goto L_0x0055;
    L_0x004e:
        r5.hearingAidSwitchDisplayDevice(r2, r6, r0);	 Catch:{ all -> 0x0052 }
        goto L_0x0055;
    L_0x0052:
        r6 = move-exception;
        monitor-exit(r5);
        throw r6;
    L_0x0055:
        monitor-exit(r5);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.bluetooth.CachedBluetoothDeviceManager.onProfileConnectionStateChanged(com.android.settingslib.bluetooth.CachedBluetoothDevice, int, int):void");
    }

    /* JADX WARNING: Missing block: B:23:0x0075, code skipped:
            return;
     */
    public synchronized void onDeviceUnpaired(com.android.settingslib.bluetooth.CachedBluetoothDevice r7) {
        /*
        r6 = this;
        monitor-enter(r6);
        r0 = r7.getHiSyncId();	 Catch:{ all -> 0x0076 }
        r2 = 0;
        r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
        if (r2 != 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r6);
        return;
    L_0x000d:
        r2 = r6.mHearingAidDevicesNotAddedInCache;	 Catch:{ all -> 0x0076 }
        r2 = r2.size();	 Catch:{ all -> 0x0076 }
        r2 = r2 + -1;
    L_0x0015:
        if (r2 < 0) goto L_0x0049;
    L_0x0017:
        r3 = r6.mHearingAidDevicesNotAddedInCache;	 Catch:{ all -> 0x0076 }
        r3 = r3.get(r2);	 Catch:{ all -> 0x0076 }
        r3 = (com.android.settingslib.bluetooth.CachedBluetoothDevice) r3;	 Catch:{ all -> 0x0076 }
        r4 = r3.getHiSyncId();	 Catch:{ all -> 0x0076 }
        r4 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1));
        if (r4 != 0) goto L_0x0046;
    L_0x0027:
        r4 = r6.mHearingAidDevicesNotAddedInCache;	 Catch:{ all -> 0x0076 }
        r4.remove(r2);	 Catch:{ all -> 0x0076 }
        if (r7 != r3) goto L_0x002f;
    L_0x002e:
        goto L_0x0046;
    L_0x002f:
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0076 }
        r4.<init>();	 Catch:{ all -> 0x0076 }
        r5 = "onDeviceUnpaired: Unpair device=";
        r4.append(r5);	 Catch:{ all -> 0x0076 }
        r4.append(r3);	 Catch:{ all -> 0x0076 }
        r4 = r4.toString();	 Catch:{ all -> 0x0076 }
        r6.log(r4);	 Catch:{ all -> 0x0076 }
        r3.unpair();	 Catch:{ all -> 0x0076 }
    L_0x0046:
        r2 = r2 + -1;
        goto L_0x0015;
    L_0x0049:
        r2 = r6.mCachedDevicesMapForHearingAids;	 Catch:{ all -> 0x0076 }
        r3 = java.lang.Long.valueOf(r0);	 Catch:{ all -> 0x0076 }
        r2 = r2.get(r3);	 Catch:{ all -> 0x0076 }
        r2 = (com.android.settingslib.bluetooth.CachedBluetoothDevice) r2;	 Catch:{ all -> 0x0076 }
        if (r2 == 0) goto L_0x0074;
    L_0x0057:
        r3 = java.util.Objects.equals(r7, r2);	 Catch:{ all -> 0x0076 }
        if (r3 != 0) goto L_0x0074;
    L_0x005d:
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0076 }
        r3.<init>();	 Catch:{ all -> 0x0076 }
        r4 = "onDeviceUnpaired: Unpair mapped device=";
        r3.append(r4);	 Catch:{ all -> 0x0076 }
        r3.append(r2);	 Catch:{ all -> 0x0076 }
        r3 = r3.toString();	 Catch:{ all -> 0x0076 }
        r6.log(r3);	 Catch:{ all -> 0x0076 }
        r2.unpair();	 Catch:{ all -> 0x0076 }
    L_0x0074:
        monitor-exit(r6);
        return;
    L_0x0076:
        r7 = move-exception;
        monitor-exit(r6);
        throw r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.bluetooth.CachedBluetoothDeviceManager.onDeviceUnpaired(com.android.settingslib.bluetooth.CachedBluetoothDevice):void");
    }

    public synchronized void dispatchAudioModeChanged() {
        for (CachedBluetoothDevice cachedDevice : this.mCachedDevices) {
            cachedDevice.onAudioModeChanged();
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
