package com.android.settings.sound;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.media.MediaRouter.Callback;
import android.media.MediaRouter.RouteGroup;
import android.media.MediaRouter.RouteInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.FeatureFlagUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.FeatureFlags;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.HearingAidProfile;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class AudioSwitchPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener, BluetoothCallback, LifecycleObserver, OnStart, OnStop {
    private static final int INVALID_INDEX = -1;
    private static final String TAG = "AudioSwitchPreferenceController";
    protected final AudioManager mAudioManager;
    private final AudioManagerAudioDeviceCallback mAudioManagerAudioDeviceCallback = new AudioManagerAudioDeviceCallback();
    protected AudioSwitchCallback mAudioSwitchPreferenceCallback;
    protected final List<BluetoothDevice> mConnectedDevices = new ArrayList();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private LocalBluetoothManager mLocalBluetoothManager;
    protected final MediaRouter mMediaRouter;
    private final MediaRouterCallback mMediaRouterCallback = new MediaRouterCallback();
    protected Preference mPreference;
    protected LocalBluetoothProfileManager mProfileManager;
    private final WiredHeadsetBroadcastReceiver mReceiver = new WiredHeadsetBroadcastReceiver();
    protected int mSelectedIndex;

    private class AudioManagerAudioDeviceCallback extends AudioDeviceCallback {
        private AudioManagerAudioDeviceCallback() {
        }

        public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            AudioSwitchPreferenceController.this.updateState(AudioSwitchPreferenceController.this.mPreference);
        }

        public void onAudioDevicesRemoved(AudioDeviceInfo[] devices) {
            AudioSwitchPreferenceController.this.updateState(AudioSwitchPreferenceController.this.mPreference);
        }
    }

    public interface AudioSwitchCallback {
        void onPreferenceDataChanged(ListPreference listPreference);
    }

    private class MediaRouterCallback extends Callback {
        private MediaRouterCallback() {
        }

        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
        }

        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            if (info != null && !info.isDefault()) {
                AudioSwitchPreferenceController.this.updateState(AudioSwitchPreferenceController.this.mPreference);
            }
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            if (info != null && !info.isDefault()) {
                AudioSwitchPreferenceController.this.updateState(AudioSwitchPreferenceController.this.mPreference);
            }
        }

        public void onRouteGrouped(MediaRouter router, RouteInfo info, RouteGroup group, int index) {
        }

        public void onRouteUngrouped(MediaRouter router, RouteInfo info, RouteGroup group) {
        }

        public void onRouteVolumeChanged(MediaRouter router, RouteInfo info) {
        }
    }

    private class WiredHeadsetBroadcastReceiver extends BroadcastReceiver {
        private WiredHeadsetBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.HEADSET_PLUG".equals(action) || "android.media.STREAM_DEVICES_CHANGED_ACTION".equals(action)) {
                AudioSwitchPreferenceController.this.updateState(AudioSwitchPreferenceController.this.mPreference);
            }
        }
    }

    public abstract void setActiveBluetoothDevice(BluetoothDevice bluetoothDevice);

    public AudioSwitchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        FutureTask<LocalBluetoothManager> localBtManagerFutureTask = new FutureTask(new -$$Lambda$AudioSwitchPreferenceController$GC_sYSWqqCmy3hCGLKM8AEFN_-Y(this));
        try {
            localBtManagerFutureTask.run();
            this.mLocalBluetoothManager = (LocalBluetoothManager) localBtManagerFutureTask.get();
            if (this.mLocalBluetoothManager == null) {
                Log.e(TAG, "Bluetooth is not supported on this device");
            } else {
                this.mProfileManager = this.mLocalBluetoothManager.getProfileManager();
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.w(TAG, "Error getting LocalBluetoothManager.", e);
        }
    }

    public final int getAvailabilityStatus() {
        return (FeatureFlagUtils.isEnabled(this.mContext, FeatureFlags.AUDIO_SWITCHER_SETTINGS) && this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) ? 0 : 1;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String address = (String) newValue;
        if (!(preference instanceof ListPreference)) {
            return false;
        }
        ListPreference listPreference = (ListPreference) preference;
        if (TextUtils.equals(address, this.mContext.getText(R.string.media_output_default_summary))) {
            this.mSelectedIndex = getDefaultDeviceIndex();
            setActiveBluetoothDevice(null);
            listPreference.setSummary(this.mContext.getText(R.string.media_output_default_summary));
        } else {
            int connectedDeviceIndex = getConnectedDeviceIndex(address);
            if (connectedDeviceIndex == -1) {
                return false;
            }
            BluetoothDevice btDevice = (BluetoothDevice) this.mConnectedDevices.get(connectedDeviceIndex);
            this.mSelectedIndex = connectedDeviceIndex;
            setActiveBluetoothDevice(btDevice);
            listPreference.setSummary(btDevice.getAliasName());
        }
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(this.mPreferenceKey);
        this.mPreference.setVisible(false);
    }

    public void onStart() {
        if (this.mLocalBluetoothManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        this.mLocalBluetoothManager.setForegroundActivity(this.mContext);
        register();
    }

    public void onStop() {
        if (this.mLocalBluetoothManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        this.mLocalBluetoothManager.setForegroundActivity(null);
        unregister();
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
        updateState(this.mPreference);
    }

    public void onAudioModeChanged() {
        updateState(this.mPreference);
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
        updateState(this.mPreference);
    }

    public void onBluetoothStateChanged(int bluetoothState) {
    }

    public void onScanningStateChanged(boolean started) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        updateState(this.mPreference);
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
    }

    public void setCallback(AudioSwitchCallback callback) {
        this.mAudioSwitchPreferenceCallback = callback;
    }

    /* Access modifiers changed, original: protected */
    public boolean isStreamFromOutputDevice(int streamType, int device) {
        return (this.mAudioManager.getDevicesForStream(streamType) & device) != 0;
    }

    /* Access modifiers changed, original: protected */
    public List<BluetoothDevice> getConnectedHfpDevices() {
        List<BluetoothDevice> connectedDevices = new ArrayList();
        HeadsetProfile hfpProfile = this.mProfileManager.getHeadsetProfile();
        if (hfpProfile == null) {
            return connectedDevices;
        }
        for (BluetoothDevice device : hfpProfile.getConnectedDevices()) {
            if (device.isConnected()) {
                connectedDevices.add(device);
            }
        }
        return connectedDevices;
    }

    /* Access modifiers changed, original: protected */
    public List<BluetoothDevice> getConnectedA2dpDevices() {
        List<BluetoothDevice> connectedDevices = new ArrayList();
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        if (a2dpProfile == null) {
            return connectedDevices;
        }
        for (BluetoothDevice device : a2dpProfile.getConnectedDevices()) {
            if (device.isConnected()) {
                connectedDevices.add(device);
            }
        }
        return connectedDevices;
    }

    /* Access modifiers changed, original: protected */
    public List<BluetoothDevice> getConnectedHearingAidDevices() {
        List<BluetoothDevice> connectedDevices = new ArrayList();
        HearingAidProfile hapProfile = this.mProfileManager.getHearingAidProfile();
        if (hapProfile == null) {
            return connectedDevices;
        }
        List<Long> devicesHiSyncIds = new ArrayList();
        for (BluetoothDevice device : hapProfile.getConnectedDevices()) {
            long hiSyncId = hapProfile.getHiSyncId(device);
            if (!devicesHiSyncIds.contains(Long.valueOf(hiSyncId)) && device.isConnected()) {
                devicesHiSyncIds.add(Long.valueOf(hiSyncId));
                connectedDevices.add(device);
            }
        }
        return connectedDevices;
    }

    /* Access modifiers changed, original: protected */
    public BluetoothDevice findActiveDevice(int streamType) {
        if (streamType != 3 && streamType != 0) {
            return null;
        }
        if (isStreamFromOutputDevice(3, 896)) {
            return this.mProfileManager.getA2dpProfile().getActiveDevice();
        }
        if (isStreamFromOutputDevice(0, 112)) {
            return this.mProfileManager.getHeadsetProfile().getActiveDevice();
        }
        if (isStreamFromOutputDevice(streamType, 134217728)) {
            for (BluetoothDevice btDevice : this.mProfileManager.getHearingAidProfile().getActiveDevices()) {
                if (btDevice != null && this.mConnectedDevices.contains(btDevice)) {
                    return btDevice;
                }
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDefaultDeviceIndex() {
        return this.mConnectedDevices.size();
    }

    /* Access modifiers changed, original: 0000 */
    public void setupPreferenceEntries(CharSequence[] mediaOutputs, CharSequence[] mediaValues, BluetoothDevice activeDevice) {
        this.mSelectedIndex = getDefaultDeviceIndex();
        mediaOutputs[this.mSelectedIndex] = this.mContext.getText(R.string.media_output_default_summary);
        mediaValues[this.mSelectedIndex] = this.mContext.getText(R.string.media_output_default_summary);
        int size = this.mConnectedDevices.size();
        for (int i = 0; i < size; i++) {
            BluetoothDevice btDevice = (BluetoothDevice) this.mConnectedDevices.get(i);
            mediaOutputs[i] = btDevice.getAliasName();
            mediaValues[i] = btDevice.getAddress();
            if (btDevice.equals(activeDevice)) {
                this.mSelectedIndex = i;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setPreference(CharSequence[] mediaOutputs, CharSequence[] mediaValues, Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setEntries(mediaOutputs);
        listPreference.setEntryValues(mediaValues);
        listPreference.setValueIndex(this.mSelectedIndex);
        listPreference.setSummary(mediaOutputs[this.mSelectedIndex]);
        this.mAudioSwitchPreferenceCallback.onPreferenceDataChanged(listPreference);
    }

    private int getConnectedDeviceIndex(String hardwareAddress) {
        if (this.mConnectedDevices != null) {
            int size = this.mConnectedDevices.size();
            for (int i = 0; i < size; i++) {
                if (TextUtils.equals(((BluetoothDevice) this.mConnectedDevices.get(i)).getAddress(), hardwareAddress)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void register() {
        this.mLocalBluetoothManager.getEventManager().registerCallback(this);
        this.mAudioManager.registerAudioDeviceCallback(this.mAudioManagerAudioDeviceCallback, this.mHandler);
        this.mMediaRouter.addCallback(4, this.mMediaRouterCallback);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void unregister() {
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this);
        this.mAudioManager.unregisterAudioDeviceCallback(this.mAudioManagerAudioDeviceCallback);
        this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
        this.mContext.unregisterReceiver(this.mReceiver);
    }
}
