package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothCodecConfig;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.R;
import com.android.settingslib.wrapper.BluetoothA2dpWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class A2dpProfile implements LocalBluetoothProfile {
    static final String NAME = "A2DP";
    private static final int ORDINAL = 1;
    static final ParcelUuid[] SINK_UUIDS = new ParcelUuid[]{BluetoothUuid.AudioSink, BluetoothUuid.AdvAudioDist};
    private static final String TAG = "A2dpProfile";
    private static boolean V = true;
    private Context mContext;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private boolean mIsProfileReady;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;
    private BluetoothA2dp mService;
    private BluetoothA2dpWrapper mServiceWrapper;

    private final class A2dpServiceListener implements ServiceListener {
        private A2dpServiceListener() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (A2dpProfile.V) {
                Log.d(A2dpProfile.TAG, "Bluetooth service connected");
            }
            A2dpProfile.this.mService = (BluetoothA2dp) proxy;
            A2dpProfile.this.mServiceWrapper = new BluetoothA2dpWrapper(A2dpProfile.this.mService);
            List<BluetoothDevice> deviceList = A2dpProfile.this.mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = (BluetoothDevice) deviceList.remove(0);
                CachedBluetoothDevice device = A2dpProfile.this.mDeviceManager.findDevice(nextDevice);
                if (device == null) {
                    String str = A2dpProfile.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("A2dpProfile found new device: ");
                    stringBuilder.append(nextDevice);
                    Log.w(str, stringBuilder.toString());
                    device = A2dpProfile.this.mDeviceManager.addDevice(A2dpProfile.this.mLocalAdapter, A2dpProfile.this.mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(A2dpProfile.this, 2);
                device.refresh();
            }
            A2dpProfile.this.mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (A2dpProfile.V) {
                Log.d(A2dpProfile.TAG, "Bluetooth service disconnected");
            }
            A2dpProfile.this.mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        return this.mIsProfileReady;
    }

    public int getProfileId() {
        return 2;
    }

    A2dpProfile(Context context, LocalBluetoothAdapter adapter, CachedBluetoothDeviceManager deviceManager, LocalBluetoothProfileManager profileManager) {
        this.mContext = context;
        this.mLocalAdapter = adapter;
        this.mDeviceManager = deviceManager;
        this.mProfileManager = profileManager;
        this.mLocalAdapter.getProfileProxy(context, new A2dpServiceListener(), 2);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setBluetoothA2dpWrapper(BluetoothA2dpWrapper wrapper) {
        this.mServiceWrapper = wrapper;
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null) {
            return new ArrayList(0);
        }
        return this.mService.getDevicesMatchingConnectionStates(new int[]{2, 1, 3});
    }

    public boolean connect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.connect(device);
    }

    public boolean disconnect(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 100) {
            this.mService.setPriority(device, 100);
        }
        return this.mService.disconnect(device);
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getConnectionState(device);
    }

    public boolean setActiveDevice(BluetoothDevice device) {
        if (this.mService == null) {
            return false;
        }
        return this.mService.setActiveDevice(device);
    }

    public BluetoothDevice getActiveDevice() {
        if (this.mService == null) {
            return null;
        }
        return this.mService.getActiveDevice();
    }

    public boolean isPreferred(BluetoothDevice device) {
        boolean z = false;
        if (this.mService == null) {
            return false;
        }
        if (this.mService.getPriority(device) > 0) {
            z = true;
        }
        return z;
    }

    public int getPreferred(BluetoothDevice device) {
        if (this.mService == null) {
            return 0;
        }
        return this.mService.getPriority(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (this.mService != null) {
            if (!preferred) {
                this.mService.setPriority(device, 0);
            } else if (this.mService.getPriority(device) < 100) {
                this.mService.setPriority(device, 100);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isA2dpPlaying() {
        if (this.mService == null) {
            return false;
        }
        for (BluetoothDevice device : this.mService.getConnectedDevices()) {
            if (this.mService.isA2dpPlaying(device)) {
                return true;
            }
        }
        return false;
    }

    public boolean supportsHighQualityAudio(BluetoothDevice device) {
        if (V) {
            Log.d(TAG, " execute supportsHighQualityAudio()");
        }
        boolean z = false;
        if (this.mServiceWrapper == null) {
            if (V) {
                Log.d(TAG, "mServiceWrapper is null.");
            }
            return false;
        }
        if (this.mServiceWrapper.supportsOptionalCodecs(device) == 1) {
            z = true;
        }
        return z;
    }

    public boolean isHighQualityAudioEnabled(BluetoothDevice device) {
        if (V) {
            Log.d(TAG, " execute isHighQualityAudioEnabled()");
        }
        boolean z = false;
        if (this.mServiceWrapper == null) {
            if (V) {
                Log.d(TAG, "mServiceWrapper is null.");
            }
            return false;
        }
        int enabled = this.mServiceWrapper.getOptionalCodecsEnabled(device);
        if (enabled != -1) {
            if (enabled == 1) {
                z = true;
            }
            return z;
        } else if (getConnectionStatus(device) != 2 && supportsHighQualityAudio(device)) {
            return true;
        } else {
            BluetoothCodecConfig codecConfig = null;
            if (this.mServiceWrapper == null) {
                return false;
            }
            if (this.mServiceWrapper.getCodecStatus(device) != null) {
                codecConfig = this.mServiceWrapper.getCodecStatus(device).getCodecConfig();
            }
            if (codecConfig != null) {
                return codecConfig.isMandatoryCodec() ^ 1;
            }
            return false;
        }
    }

    public void setHighQualityAudioEnabled(BluetoothDevice device, boolean enabled) {
        if (V) {
            Log.d(TAG, " execute setHighQualityAudioEnabled()");
        }
        if (this.mServiceWrapper == null) {
            if (V) {
                Log.d(TAG, "mServiceWrapper is null.");
            }
            return;
        }
        int prefValue;
        if (enabled) {
            prefValue = 1;
        } else {
            prefValue = 0;
        }
        if (this.mServiceWrapper != null) {
            this.mServiceWrapper.setOptionalCodecsEnabled(device, prefValue);
        }
        if (getConnectionStatus(device) == 2) {
            if (enabled) {
                this.mService.enableOptionalCodecs(device);
            } else {
                this.mService.disableOptionalCodecs(device);
            }
        }
    }

    public String getHighQualityAudioOptionLabel(BluetoothDevice device) {
        if (V) {
            Log.d(TAG, " execute getHighQualityAudioOptionLabel()");
        }
        int unknownCodecId = R.string.bluetooth_profile_a2dp_high_quality_unknown_codec;
        if (!supportsHighQualityAudio(device) || getConnectionStatus(device) != 2) {
            return this.mContext.getString(unknownCodecId);
        }
        BluetoothCodecConfig[] selectable = null;
        if (!(this.mServiceWrapper == null || this.mServiceWrapper.getCodecStatus(device) == null)) {
            selectable = this.mServiceWrapper.getCodecStatus(device).getCodecsSelectableCapabilities();
            Arrays.sort(selectable, -$$Lambda$A2dpProfile$exPXCssgW4cryyr_RqCY5K-rQFI.INSTANCE);
        }
        BluetoothCodecConfig codecConfig = (selectable == null || selectable.length < 1) ? null : selectable[0];
        int codecType = (codecConfig == null || codecConfig.isMandatoryCodec()) ? 1000000 : codecConfig.getCodecType();
        int index = -1;
        switch (codecType) {
            case 0:
                index = 1;
                break;
            case 1:
                index = 2;
                break;
            case 2:
                index = 3;
                break;
            case 3:
                index = 4;
                break;
            case 4:
                index = 6;
                break;
            case 5:
                index = 5;
                break;
        }
        if (index < 0) {
            return this.mContext.getString(unknownCodecId);
        }
        return this.mContext.getString(R.string.bluetooth_profile_a2dp_high_quality, new Object[]{this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_titles)[index]});
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return 1;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_a2dp;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        if (state == 0) {
            return R.string.bluetooth_a2dp_profile_summary_use_for;
        }
        if (state != 2) {
            return Utils.getConnectionStateSummary(state);
        }
        return R.string.bluetooth_a2dp_profile_summary_connected;
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_headphones_a2dp;
    }

    /* Access modifiers changed, original: protected */
    public void finalize() {
        if (V) {
            Log.d(TAG, "finalize()");
        }
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(2, this.mService);
                this.mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up A2DP proxy", t);
            }
        }
    }
}
