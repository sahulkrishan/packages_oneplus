package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.R;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CachedBluetoothDevice implements Comparable<CachedBluetoothDevice> {
    public static final int ACCESS_ALLOWED = 1;
    public static final int ACCESS_REJECTED = 2;
    public static final int ACCESS_UNKNOWN = 0;
    private static final boolean DEBUG = false;
    private static final long MAX_HOGP_DELAY_FOR_AUTO_CONNECT = 30000;
    private static final long MAX_UUID_DELAY_FOR_AUTO_CONNECT = 5000;
    private static final int MESSAGE_REJECTION_COUNT_LIMIT_TO_PERSIST = 2;
    private static final String MESSAGE_REJECTION_COUNT_PREFS_NAME = "bluetooth_message_reject";
    private static final String TAG = "CachedBluetoothDevice";
    private static final boolean mIsTwsConnectEnabled = false;
    private final AudioManager mAudioManager;
    private BluetoothClass mBtClass;
    private final Collection<Callback> mCallbacks = new ArrayList();
    private long mConnectAttempted;
    private final Context mContext;
    private final BluetoothDevice mDevice;
    private long mHiSyncId;
    private boolean mIsActiveDeviceA2dp = false;
    private boolean mIsActiveDeviceHeadset = false;
    private boolean mIsActiveDeviceHearingAid = false;
    private boolean mIsConnectingErrorPossible;
    private boolean mJustDiscovered;
    private final LocalBluetoothAdapter mLocalAdapter;
    private boolean mLocalNapRoleConnected;
    private int mMessageRejectionCount;
    private String mName;
    private HashMap<LocalBluetoothProfile, Integer> mProfileConnectionState;
    private final LocalBluetoothProfileManager mProfileManager;
    private final List<LocalBluetoothProfile> mProfiles = new ArrayList();
    private final List<LocalBluetoothProfile> mRemovedProfiles = new ArrayList();
    private short mRssi;

    public interface Callback {
        void onDeviceAttributesChanged();
    }

    public long getHiSyncId() {
        return this.mHiSyncId;
    }

    public void setHiSyncId(long id) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setHiSyncId: mDevice ");
        stringBuilder.append(this.mDevice);
        stringBuilder.append(", id ");
        stringBuilder.append(id);
        Log.d(str, stringBuilder.toString());
        this.mHiSyncId = id;
    }

    private BluetoothDevice getTwsPeerDevice() {
        if (this.mDevice.isTwsPlusDevice()) {
            return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.mDevice.getTwsPlusPeerAddress());
        }
        return null;
    }

    private String describe(LocalBluetoothProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:");
        sb.append(this.mDevice);
        if (profile != null) {
            sb.append(" Profile:");
            sb.append(profile);
        }
        return sb.toString();
    }

    /* Access modifiers changed, original: 0000 */
    public void onProfileStateChanged(LocalBluetoothProfile profile, int newProfileState) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onProfileStateChanged: profile ");
        stringBuilder.append(profile);
        stringBuilder.append(" newProfileState ");
        stringBuilder.append(newProfileState);
        Log.d(str, stringBuilder.toString());
        if (this.mLocalAdapter.getBluetoothState() == 13) {
            Log.d(TAG, " BT Turninig Off...Profile conn state change ignored...");
            return;
        }
        this.mProfileConnectionState.put(profile, Integer.valueOf(newProfileState));
        if (newProfileState == 2) {
            if (profile instanceof MapProfile) {
                profile.setPreferred(this.mDevice, true);
            }
            if (!this.mProfiles.contains(profile)) {
                this.mRemovedProfiles.remove(profile);
                synchronized (this.mProfiles) {
                    Log.d(TAG, "Add profile");
                    this.mProfiles.add(profile);
                }
                if ((profile instanceof PanProfile) && ((PanProfile) profile).isLocalRoleNap(this.mDevice)) {
                    this.mLocalNapRoleConnected = true;
                }
            }
        } else if ((profile instanceof MapProfile) && newProfileState == 0) {
            profile.setPreferred(this.mDevice, false);
        } else if (this.mLocalNapRoleConnected && (profile instanceof PanProfile) && ((PanProfile) profile).isLocalRoleNap(this.mDevice) && newProfileState == 0) {
            Log.d(TAG, "Removing PanProfile from device after NAP disconnect");
            synchronized (this.mProfiles) {
                Log.d(TAG, "Remove profile");
                this.mProfiles.remove(profile);
            }
            this.mRemovedProfiles.add(profile);
            this.mLocalNapRoleConnected = false;
        }
        fetchActiveDevices();
    }

    CachedBluetoothDevice(Context context, LocalBluetoothAdapter adapter, LocalBluetoothProfileManager profileManager, BluetoothDevice device) {
        this.mContext = context;
        this.mLocalAdapter = adapter;
        this.mProfileManager = profileManager;
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
        this.mDevice = device;
        this.mProfileConnectionState = new HashMap();
        fillData();
        this.mHiSyncId = 0;
    }

    public void disconnect() {
        for (LocalBluetoothProfile profile : this.mProfiles) {
            disconnect(profile);
        }
        PbapServerProfile PbapProfile = this.mProfileManager.getPbapProfile();
        if (PbapProfile.getConnectionStatus(this.mDevice) == 2) {
            PbapProfile.disconnect(this.mDevice);
        }
    }

    public void disconnect(LocalBluetoothProfile profile) {
        if (profile.disconnect(this.mDevice)) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Command sent successfully:DISCONNECT ");
            stringBuilder.append(describe(profile));
            Log.d(str, stringBuilder.toString());
        }
    }

    public void connect(boolean connectAllProfiles) {
        if (ensurePaired()) {
            this.mConnectAttempted = SystemClock.elapsedRealtime();
            connectWithoutResettingTimer(connectAllProfiles);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onBondingDockConnect() {
        connect(false);
    }

    private void connectWithoutResettingTimer(boolean connectAllProfiles) {
        if (this.mProfiles.isEmpty()) {
            Log.d(TAG, "No profiles. Maybe we will connect later");
            return;
        }
        this.mIsConnectingErrorPossible = true;
        int preferredProfiles = 0;
        for (LocalBluetoothProfile profile : this.mProfiles) {
            if (connectAllProfiles) {
                if (!profile.isConnectable()) {
                }
            } else if (!profile.isAutoConnectable()) {
            }
            if (profile.isPreferred(this.mDevice)) {
                preferredProfiles++;
                connectInt(profile);
            }
        }
        if (preferredProfiles == 0) {
            connectAutoConnectableProfiles();
        }
    }

    private void connectAutoConnectableProfiles() {
        if (ensurePaired()) {
            this.mIsConnectingErrorPossible = true;
            for (LocalBluetoothProfile profile : this.mProfiles) {
                if (profile.isAutoConnectable()) {
                    profile.setPreferred(this.mDevice, true);
                    connectInt(profile);
                }
            }
        }
    }

    public void connectProfile(LocalBluetoothProfile profile) {
        this.mConnectAttempted = SystemClock.elapsedRealtime();
        this.mIsConnectingErrorPossible = true;
        connectInt(profile);
        refresh();
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized void connectInt(LocalBluetoothProfile profile) {
        if (!ensurePaired()) {
            return;
        }
        String str;
        StringBuilder stringBuilder;
        if (profile.connect(this.mDevice)) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Command sent successfully:CONNECT ");
            stringBuilder.append(describe(profile));
            Log.d(str, stringBuilder.toString());
            return;
        }
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Failed to connect ");
        stringBuilder.append(profile.toString());
        stringBuilder.append(" to ");
        stringBuilder.append(this.mName);
        Log.i(str, stringBuilder.toString());
    }

    private boolean ensurePaired() {
        if (getBondState() != 10) {
            return true;
        }
        startPairing();
        return false;
    }

    public boolean startPairing() {
        if (this.mLocalAdapter.isDiscovering()) {
            this.mLocalAdapter.cancelDiscovery();
        }
        if (this.mDevice.createBond()) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isUserInitiatedPairing() {
        return this.mDevice.isBondingInitiatedLocally();
    }

    public void unpair() {
        int state = getBondState();
        if (state == 11) {
            this.mDevice.cancelBondProcess();
        }
        if (state != 10) {
            BluetoothDevice dev = this.mDevice;
            if (this.mDevice.isTwsPlusDevice()) {
                BluetoothDevice peerDevice = getTwsPeerDevice();
                if (peerDevice != null && peerDevice.removeBond()) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Command sent successfully:REMOVE_BOND ");
                    stringBuilder.append(peerDevice.getName());
                    Log.d(str, stringBuilder.toString());
                }
            }
            if (dev != null && dev.removeBond()) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Command sent successfully:REMOVE_BOND ");
                stringBuilder2.append(describe(null));
                Log.d(str2, stringBuilder2.toString());
            }
        }
    }

    public int getProfileConnectionState(LocalBluetoothProfile profile) {
        if (this.mProfileConnectionState.get(profile) == null) {
            this.mProfileConnectionState.put(profile, Integer.valueOf(profile.getConnectionStatus(this.mDevice)));
        }
        return ((Integer) this.mProfileConnectionState.get(profile)).intValue();
    }

    public void clearProfileConnectionState() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Clearing all connection state for dev:");
        stringBuilder.append(this.mDevice.getName());
        Log.d(str, stringBuilder.toString());
        for (LocalBluetoothProfile profile : getProfiles()) {
            this.mProfileConnectionState.put(profile, Integer.valueOf(0));
        }
    }

    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        fetchActiveDevices();
        migratePhonebookPermissionChoice();
        migrateMessagePermissionChoice();
        fetchMessageRejectionCount();
        dispatchAttributesChanged();
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public String getAddress() {
        return this.mDevice.getAddress();
    }

    public String getName() {
        return this.mName;
    }

    /* Access modifiers changed, original: 0000 */
    public void setNewName(String name) {
        if (this.mName == null) {
            this.mName = name;
            if (this.mName == null || TextUtils.isEmpty(this.mName)) {
                this.mName = this.mDevice.getAddress();
            }
            dispatchAttributesChanged();
        }
    }

    public void setName(String name) {
        if (name != null && !TextUtils.equals(name, this.mName)) {
            this.mName = name;
            this.mDevice.setAlias(name);
            dispatchAttributesChanged();
        }
    }

    public boolean setActive() {
        boolean result = false;
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        if (a2dpProfile != null && isConnectedProfile(a2dpProfile) && a2dpProfile.setActiveDevice(getDevice())) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("OnPreferenceClickListener: A2DP active device=");
            stringBuilder.append(this);
            Log.i(str, stringBuilder.toString());
            result = true;
        }
        HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
        if (headsetProfile != null && isConnectedProfile(headsetProfile) && headsetProfile.setActiveDevice(getDevice())) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("OnPreferenceClickListener: Headset active device=");
            stringBuilder2.append(this);
            Log.i(str2, stringBuilder2.toString());
            result = true;
        }
        HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
        if (hearingAidProfile == null || !isConnectedProfile(hearingAidProfile) || !hearingAidProfile.setActiveDevice(getDevice())) {
            return result;
        }
        String str3 = TAG;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("OnPreferenceClickListener: Hearing Aid active device=");
        stringBuilder3.append(this);
        Log.i(str3, stringBuilder3.toString());
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }

    private void fetchName() {
        this.mName = this.mDevice.getAliasName();
        if (TextUtils.isEmpty(this.mName)) {
            this.mName = this.mDevice.getAddress();
        }
    }

    public boolean hasHumanReadableName() {
        return TextUtils.isEmpty(this.mDevice.getAliasName()) ^ 1;
    }

    public int getBatteryLevel() {
        return this.mDevice.getBatteryLevel();
    }

    /* Access modifiers changed, original: 0000 */
    public void refresh() {
        dispatchAttributesChanged();
    }

    public void setJustDiscovered(boolean justDiscovered) {
        if (this.mJustDiscovered != justDiscovered) {
            this.mJustDiscovered = justDiscovered;
            dispatchAttributesChanged();
        }
    }

    public int getBondState() {
        return this.mDevice.getBondState();
    }

    public void onActiveDeviceChanged(boolean isActive, int bluetoothProfile) {
        boolean changed = false;
        boolean z = false;
        if (bluetoothProfile != 21) {
            switch (bluetoothProfile) {
                case 1:
                    if (this.mIsActiveDeviceHeadset != isActive) {
                        z = true;
                    }
                    changed = z;
                    this.mIsActiveDeviceHeadset = isActive;
                    break;
                case 2:
                    if (this.mIsActiveDeviceA2dp != isActive) {
                        z = true;
                    }
                    changed = z;
                    this.mIsActiveDeviceA2dp = isActive;
                    break;
                default:
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onActiveDeviceChanged: unknown profile ");
                    stringBuilder.append(bluetoothProfile);
                    stringBuilder.append(" isActive ");
                    stringBuilder.append(isActive);
                    Log.w(str, stringBuilder.toString());
                    break;
            }
        }
        if (this.mIsActiveDeviceHearingAid != isActive) {
            z = true;
        }
        changed = z;
        this.mIsActiveDeviceHearingAid = isActive;
        if (changed) {
            dispatchAttributesChanged();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onAudioModeChanged() {
        dispatchAttributesChanged();
    }

    @VisibleForTesting(otherwise = 3)
    public boolean isActiveDevice(int bluetoothProfile) {
        if (bluetoothProfile == 21) {
            return this.mIsActiveDeviceHearingAid;
        }
        switch (bluetoothProfile) {
            case 1:
                return this.mIsActiveDeviceHeadset;
            case 2:
                return this.mIsActiveDeviceA2dp;
            default:
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("getActiveDevice: unknown profile ");
                stringBuilder.append(bluetoothProfile);
                Log.w(str, stringBuilder.toString());
                return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setRssi(short rssi) {
        if (this.mRssi != rssi) {
            this.mRssi = rssi;
            dispatchAttributesChanged();
        }
    }

    public boolean isConnected() {
        for (LocalBluetoothProfile profile : this.mProfiles) {
            if (getProfileConnectionState(profile) == 2) {
                return true;
            }
        }
        return false;
    }

    public boolean isConnectedProfile(LocalBluetoothProfile profile) {
        return getProfileConnectionState(profile) == 2;
    }

    public boolean isBusy() {
        Iterator it = this.mProfiles.iterator();
        while (true) {
            boolean z = true;
            if (it.hasNext()) {
                int status = getProfileConnectionState((LocalBluetoothProfile) it.next());
                if (status == 1 || status == 3) {
                    return true;
                }
            } else {
                if (getBondState() != 11) {
                    z = false;
                }
                return z;
            }
        }
        return true;
    }

    private void fetchBtClass() {
        this.mBtClass = this.mDevice.getBluetoothClass();
    }

    private boolean updateProfiles() {
        ParcelUuid[] uuids = this.mDevice.getUuids();
        if (uuids == null) {
            return false;
        }
        ParcelUuid[] localUuids = this.mLocalAdapter.getUuids();
        if (localUuids == null) {
            return false;
        }
        processPhonebookAccess();
        this.mProfileManager.updateProfiles(uuids, localUuids, this.mProfiles, this.mRemovedProfiles, this.mLocalNapRoleConnected, this.mDevice);
        return true;
    }

    private void fetchActiveDevices() {
        if (this.mProfileManager != null) {
            A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
            if (a2dpProfile != null) {
                this.mIsActiveDeviceA2dp = this.mDevice.equals(a2dpProfile.getActiveDevice());
            }
            HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
            if (headsetProfile != null) {
                this.mIsActiveDeviceHeadset = this.mDevice.equals(headsetProfile.getActiveDevice());
            }
            HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
            if (hearingAidProfile != null) {
                this.mIsActiveDeviceHearingAid = hearingAidProfile.getActiveDevices().contains(this.mDevice);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void onUuidChanged() {
        updateProfiles();
        ParcelUuid[] uuids = this.mDevice.getUuids();
        long timeout = MAX_UUID_DELAY_FOR_AUTO_CONNECT;
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hogp)) {
            timeout = MAX_HOGP_DELAY_FOR_AUTO_CONNECT;
        }
        if (!this.mProfiles.isEmpty() && this.mConnectAttempted + timeout > SystemClock.elapsedRealtime()) {
            connectWithoutResettingTimer(false);
        }
        dispatchAttributesChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void onBondingStateChanged(int bondState) {
        if (bondState == 10) {
            this.mProfiles.clear();
            setPhonebookPermissionChoice(0);
            setMessagePermissionChoice(0);
            setSimPermissionChoice(0);
            this.mMessageRejectionCount = 0;
            saveMessageRejectionCount();
        }
        refresh();
        if (bondState != 12) {
            return;
        }
        if (this.mDevice.isBluetoothDock()) {
            onBondingDockConnect();
        } else if (this.mDevice.isBondingInitiatedLocally()) {
            connect(false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setBtClass(BluetoothClass btClass) {
        if (btClass != null && this.mBtClass != btClass) {
            this.mBtClass = btClass;
            dispatchAttributesChanged();
        }
    }

    public BluetoothClass getBtClass() {
        return this.mBtClass;
    }

    public List<LocalBluetoothProfile> getProfiles() {
        return Collections.unmodifiableList(this.mProfiles);
    }

    public List<LocalBluetoothProfile> getConnectableProfiles() {
        List<LocalBluetoothProfile> connectableProfiles = new ArrayList();
        for (LocalBluetoothProfile profile : this.mProfiles) {
            if (profile.isConnectable()) {
                connectableProfiles.add(profile);
            }
        }
        return connectableProfiles;
    }

    public List<LocalBluetoothProfile> getRemovedProfiles() {
        return this.mRemovedProfiles;
    }

    public void registerCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    private void dispatchAttributesChanged() {
        synchronized (this.mCallbacks) {
            for (Callback callback : this.mCallbacks) {
                callback.onDeviceAttributesChanged();
            }
        }
    }

    public String toString() {
        return this.mDevice.toString();
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof CachedBluetoothDevice)) {
            return false;
        }
        return this.mDevice.equals(((CachedBluetoothDevice) o).mDevice);
    }

    public int hashCode() {
        return this.mDevice.getAddress().hashCode();
    }

    public int compareTo(CachedBluetoothDevice another) {
        int comparison = another.isConnected() - isConnected();
        if (comparison != 0) {
            return comparison;
        }
        int i = 0;
        int comparison2 = another.getBondState() == 12 ? 1 : 0;
        if (getBondState() == 12) {
            i = 1;
        }
        comparison2 -= i;
        if (comparison2 != 0) {
            return comparison2;
        }
        comparison = another.mJustDiscovered - this.mJustDiscovered;
        if (comparison != 0) {
            return comparison;
        }
        comparison2 = another.mRssi - this.mRssi;
        if (comparison2 != 0) {
            return comparison2;
        }
        return this.mName.compareTo(another.mName);
    }

    public int getPhonebookPermissionChoice() {
        int permission = this.mDevice.getPhonebookAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    public void setPhonebookPermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setPhonebookAccessPermission(permission);
    }

    private void migratePhonebookPermissionChoice() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("bluetooth_phonebook_permission", 0);
        if (preferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getPhonebookAccessPermission() == 0) {
                int oldPermission = preferences.getInt(this.mDevice.getAddress(), 0);
                if (oldPermission == 1) {
                    this.mDevice.setPhonebookAccessPermission(1);
                } else if (oldPermission == 2) {
                    this.mDevice.setPhonebookAccessPermission(2);
                }
            }
            Editor editor = preferences.edit();
            editor.remove(this.mDevice.getAddress());
            editor.commit();
        }
    }

    public int getMessagePermissionChoice() {
        int permission = this.mDevice.getMessageAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    public void setMessagePermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setMessageAccessPermission(permission);
    }

    public int getSimPermissionChoice() {
        int permission = this.mDevice.getSimAccessPermission();
        if (permission == 1) {
            return 1;
        }
        if (permission == 2) {
            return 2;
        }
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void setSimPermissionChoice(int permissionChoice) {
        int permission = 0;
        if (permissionChoice == 1) {
            permission = 1;
        } else if (permissionChoice == 2) {
            permission = 2;
        }
        this.mDevice.setSimAccessPermission(permission);
    }

    private void migrateMessagePermissionChoice() {
        SharedPreferences preferences = this.mContext.getSharedPreferences("bluetooth_message_permission", 0);
        if (preferences.contains(this.mDevice.getAddress())) {
            if (this.mDevice.getMessageAccessPermission() == 0) {
                int oldPermission = preferences.getInt(this.mDevice.getAddress(), 0);
                if (oldPermission == 1) {
                    this.mDevice.setMessageAccessPermission(1);
                } else if (oldPermission == 2) {
                    this.mDevice.setMessageAccessPermission(2);
                }
            }
            Editor editor = preferences.edit();
            editor.remove(this.mDevice.getAddress());
            editor.commit();
        }
    }

    public boolean checkAndIncreaseMessageRejectionCount() {
        if (this.mMessageRejectionCount < 2) {
            this.mMessageRejectionCount++;
            saveMessageRejectionCount();
        }
        if (this.mMessageRejectionCount >= 2) {
            return true;
        }
        return false;
    }

    private void fetchMessageRejectionCount() {
        this.mMessageRejectionCount = this.mContext.getSharedPreferences(MESSAGE_REJECTION_COUNT_PREFS_NAME, 0).getInt(this.mDevice.getAddress(), 0);
    }

    private void saveMessageRejectionCount() {
        Editor editor = this.mContext.getSharedPreferences(MESSAGE_REJECTION_COUNT_PREFS_NAME, 0).edit();
        if (this.mMessageRejectionCount == 0) {
            editor.remove(this.mDevice.getAddress());
        } else {
            editor.putInt(this.mDevice.getAddress(), this.mMessageRejectionCount);
        }
        editor.commit();
    }

    private void processPhonebookAccess() {
        if (this.mDevice.getBondState() == 12 && BluetoothUuid.containsAnyUuid(this.mDevice.getUuids(), PbapServerProfile.PBAB_CLIENT_UUIDS) && getPhonebookPermissionChoice() == 0) {
            if (this.mDevice.getBluetoothClass() == null || !(this.mDevice.getBluetoothClass().getDeviceClass() == 1032 || this.mDevice.getBluetoothClass().getDeviceClass() == 1028)) {
                setPhonebookPermissionChoice(2);
            } else {
                setPhonebookPermissionChoice(1);
            }
        }
    }

    public int getMaxConnectionState() {
        int maxState = 0;
        synchronized (this.mProfiles) {
            Log.d(TAG, "getMaxConnectionState");
            for (LocalBluetoothProfile profile : getProfiles()) {
                if (profile != null) {
                    int connectionStatus = getProfileConnectionState(profile);
                    if (connectionStatus > maxState) {
                        maxState = connectionStatus;
                    }
                }
            }
        }
        return maxState;
    }

    public String getConnectionSummary() {
        int connectionStatus;
        String string;
        boolean profileConnected = false;
        boolean a2dpConnected = true;
        boolean hfpConnected = true;
        boolean hearingAidConnected = true;
        for (LocalBluetoothProfile profile : getProfiles()) {
            connectionStatus = getProfileConnectionState(profile);
            switch (connectionStatus) {
                case 0:
                    if (profile.isProfileReady()) {
                        if (!(profile instanceof A2dpProfile) && !(profile instanceof A2dpSinkProfile)) {
                            if (!(profile instanceof HeadsetProfile) && !(profile instanceof HfpClientProfile)) {
                                if (!(profile instanceof HearingAidProfile)) {
                                    break;
                                }
                                hearingAidConnected = false;
                                break;
                            }
                            hfpConnected = false;
                            break;
                        }
                        a2dpConnected = false;
                        break;
                    }
                    break;
                case 1:
                case 3:
                    return this.mContext.getString(Utils.getConnectionStateSummary(connectionStatus));
                case 2:
                    profileConnected = true;
                    break;
                default:
                    break;
            }
        }
        String batteryLevelPercentageString = null;
        int batteryLevel = getBatteryLevel();
        if (batteryLevel != -1) {
            batteryLevelPercentageString = Utils.formatPercentage(batteryLevel);
        }
        connectionStatus = R.string.bluetooth_pairing;
        if (profileConnected) {
            if (a2dpConnected || hfpConnected || hearingAidConnected) {
                if (batteryLevelPercentageString != null) {
                    connectionStatus = Utils.isAudioModeOngoingCall(this.mContext) ? this.mIsActiveDeviceHeadset ? R.string.bluetooth_active_battery_level : R.string.bluetooth_battery_level : (this.mIsActiveDeviceHearingAid || this.mIsActiveDeviceA2dp) ? R.string.bluetooth_active_battery_level : R.string.bluetooth_battery_level;
                } else if (Utils.isAudioModeOngoingCall(this.mContext)) {
                    if (this.mIsActiveDeviceHeadset) {
                        connectionStatus = R.string.bluetooth_active_no_battery_level;
                    }
                } else if (this.mIsActiveDeviceHearingAid || this.mIsActiveDeviceA2dp) {
                    connectionStatus = R.string.bluetooth_active_no_battery_level;
                }
            } else if (batteryLevelPercentageString != null) {
                connectionStatus = R.string.bluetooth_battery_level;
            }
        }
        if (connectionStatus != R.string.bluetooth_pairing || getBondState() == 11) {
            string = this.mContext.getString(connectionStatus, new Object[]{batteryLevelPercentageString});
        } else {
            string = null;
        }
        return string;
    }

    public String getCarConnectionSummary() {
        boolean profileConnected = false;
        boolean a2dpNotConnected = false;
        boolean hfpNotConnected = false;
        boolean hearingAidNotConnected = false;
        for (LocalBluetoothProfile profile : getProfiles()) {
            int connectionStatus = getProfileConnectionState(profile);
            switch (connectionStatus) {
                case 0:
                    if (profile.isProfileReady()) {
                        if (!(profile instanceof A2dpProfile) && !(profile instanceof A2dpSinkProfile)) {
                            if (!(profile instanceof HeadsetProfile) && !(profile instanceof HfpClientProfile)) {
                                if (!(profile instanceof HearingAidProfile)) {
                                    break;
                                }
                                hearingAidNotConnected = true;
                                break;
                            }
                            hfpNotConnected = true;
                            break;
                        }
                        a2dpNotConnected = true;
                        break;
                    }
                    break;
                case 1:
                case 3:
                    return this.mContext.getString(Utils.getConnectionStateSummary(connectionStatus));
                case 2:
                    profileConnected = true;
                    break;
                default:
                    break;
            }
        }
        String batteryLevelPercentageString = null;
        int batteryLevel = getBatteryLevel();
        if (batteryLevel != -1) {
            batteryLevelPercentageString = Utils.formatPercentage(batteryLevel);
        }
        String[] activeDeviceStringsArray = this.mContext.getResources().getStringArray(R.array.bluetooth_audio_active_device_summaries);
        String activeDeviceString = activeDeviceStringsArray[0];
        if (this.mIsActiveDeviceA2dp && this.mIsActiveDeviceHeadset) {
            activeDeviceString = activeDeviceStringsArray[1];
        } else {
            if (this.mIsActiveDeviceA2dp) {
                activeDeviceString = activeDeviceStringsArray[2];
            }
            if (this.mIsActiveDeviceHeadset) {
                activeDeviceString = activeDeviceStringsArray[3];
            }
        }
        if (!hearingAidNotConnected && this.mIsActiveDeviceHearingAid) {
            activeDeviceString = activeDeviceStringsArray[1];
            return this.mContext.getString(R.string.bluetooth_connected, new Object[]{activeDeviceString});
        } else if (!profileConnected) {
            return getBondState() == 11 ? this.mContext.getString(R.string.bluetooth_pairing) : null;
        } else if (a2dpNotConnected && hfpNotConnected) {
            if (batteryLevelPercentageString != null) {
                return this.mContext.getString(R.string.bluetooth_connected_no_headset_no_a2dp_battery_level, new Object[]{batteryLevelPercentageString, activeDeviceString});
            }
            return this.mContext.getString(R.string.bluetooth_connected_no_headset_no_a2dp, new Object[]{activeDeviceString});
        } else if (a2dpNotConnected) {
            if (batteryLevelPercentageString != null) {
                return this.mContext.getString(R.string.bluetooth_connected_no_a2dp_battery_level, new Object[]{batteryLevelPercentageString, activeDeviceString});
            }
            return this.mContext.getString(R.string.bluetooth_connected_no_a2dp, new Object[]{activeDeviceString});
        } else if (hfpNotConnected) {
            if (batteryLevelPercentageString != null) {
                return this.mContext.getString(R.string.bluetooth_connected_no_headset_battery_level, new Object[]{batteryLevelPercentageString, activeDeviceString});
            }
            return this.mContext.getString(R.string.bluetooth_connected_no_headset, new Object[]{activeDeviceString});
        } else if (batteryLevelPercentageString != null) {
            return this.mContext.getString(R.string.bluetooth_connected_battery_level, new Object[]{batteryLevelPercentageString, activeDeviceString});
        } else {
            return this.mContext.getString(R.string.bluetooth_connected, new Object[]{activeDeviceString});
        }
    }

    public boolean isA2dpDevice() {
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        A2dpSinkProfile a2dpSinkProfile = this.mProfileManager.getA2dpSinkProfile();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("a2dpProfile :");
        stringBuilder.append(a2dpProfile);
        stringBuilder.append(" a2dpSinkProfile :");
        stringBuilder.append(a2dpSinkProfile);
        Log.i(str, stringBuilder.toString());
        boolean z = true;
        String str2;
        StringBuilder stringBuilder2;
        if (a2dpProfile != null) {
            str2 = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("a2dpProfile :");
            stringBuilder2.append(a2dpProfile);
            stringBuilder2.append("  Device name : ");
            stringBuilder2.append(this.mDevice.getName());
            stringBuilder2.append("  connectionStatus : ");
            stringBuilder2.append(a2dpProfile.getConnectionStatus(this.mDevice));
            Log.i(str2, stringBuilder2.toString());
            if (a2dpProfile.getConnectionStatus(this.mDevice) != 2) {
                z = false;
            }
            return z;
        } else if (a2dpSinkProfile == null) {
            return false;
        } else {
            str2 = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("a2dpSinkProfile :");
            stringBuilder2.append(a2dpSinkProfile);
            stringBuilder2.append("  Device name : ");
            stringBuilder2.append(this.mDevice.getName());
            stringBuilder2.append("  connectionstatus : ");
            stringBuilder2.append(a2dpSinkProfile.getConnectionStatus(this.mDevice));
            Log.i(str2, stringBuilder2.toString());
            if (a2dpSinkProfile.getConnectionStatus(this.mDevice) != 2) {
                z = false;
            }
            return z;
        }
    }

    public boolean isHfpDevice() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HfpDevice Device : ");
        stringBuilder.append(this.mDevice.getName());
        stringBuilder.append("connectionstatus :");
        stringBuilder.append(this.mProfileManager.getHeadsetProfile().getConnectionStatus(this.mDevice));
        Log.i(str, stringBuilder.toString());
        return this.mProfileManager.getHeadsetProfile().getConnectionStatus(this.mDevice) == 2;
    }
}
