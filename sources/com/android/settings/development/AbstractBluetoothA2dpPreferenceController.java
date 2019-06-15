package com.android.settings.development;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothCodecConfig;
import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public abstract class AbstractBluetoothA2dpPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin, BluetoothServiceConnectionListener, LifecycleObserver, OnDestroy {
    @VisibleForTesting
    static final int STREAMING_LABEL_ID = 2131886914;
    protected BluetoothA2dp mBluetoothA2dp;
    protected final BluetoothA2dpConfigStore mBluetoothA2dpConfigStore;
    private final String[] mListSummaries = getListSummaries();
    private final String[] mListValues = getListValues();
    protected ListPreference mPreference;

    public abstract int getCurrentA2dpSettingIndex(BluetoothCodecConfig bluetoothCodecConfig);

    public abstract int getDefaultIndex();

    public abstract String[] getListSummaries();

    public abstract String[] getListValues();

    public abstract void writeConfigurationValues(Object obj);

    public AbstractBluetoothA2dpPreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context);
        this.mBluetoothA2dpConfigStore = store;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ListPreference) screen.findPreference(getPreferenceKey());
        this.mPreference.setValue(this.mListValues[getDefaultIndex()]);
        this.mPreference.setSummary(this.mListSummaries[getDefaultIndex()]);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mBluetoothA2dp == null) {
            return false;
        }
        writeConfigurationValues(newValue);
        BluetoothCodecConfig codecConfig = this.mBluetoothA2dpConfigStore.createCodecConfig();
        synchronized (this.mBluetoothA2dpConfigStore) {
            if (this.mBluetoothA2dp != null) {
                setCodecConfigPreference(null, codecConfig);
            }
        }
        int index = this.mPreference.findIndexOfValue(newValue.toString());
        if (index == getDefaultIndex()) {
            this.mPreference.setSummary(this.mListSummaries[index]);
        } else {
            this.mPreference.setSummary(this.mContext.getResources().getString(R.string.bluetooth_select_a2dp_codec_streaming_label, new Object[]{this.mListSummaries[index]}));
        }
        return true;
    }

    public void updateState(Preference preference) {
        if (getCodecConfig(null) != null && this.mPreference != null) {
            BluetoothCodecConfig codecConfig;
            synchronized (this.mBluetoothA2dpConfigStore) {
                codecConfig = getCodecConfig(null);
            }
            int index = getCurrentA2dpSettingIndex(codecConfig);
            this.mPreference.setValue(this.mListValues[index]);
            if (index == getDefaultIndex()) {
                this.mPreference.setSummary(this.mListSummaries[index]);
            } else {
                this.mPreference.setSummary(this.mContext.getResources().getString(R.string.bluetooth_select_a2dp_codec_streaming_label, new Object[]{this.mListSummaries[index]}));
            }
            writeConfigurationValues(this.mListValues[index]);
        }
    }

    public void onBluetoothServiceConnected(BluetoothA2dp bluetoothA2dp) {
        this.mBluetoothA2dp = bluetoothA2dp;
        updateState(this.mPreference);
    }

    public void onBluetoothCodecUpdated() {
    }

    public void onBluetoothServiceDisconnected() {
        this.mBluetoothA2dp = null;
    }

    public void onDestroy() {
        this.mBluetoothA2dp = null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setCodecConfigPreference(BluetoothDevice device, BluetoothCodecConfig config) {
        if (config.getCodecName().equals("AAC")) {
            SystemProperties.set("persist.vendor.bt.a2dp.aac_whitelist", Boolean.toString(false));
        } else {
            SystemProperties.set("persist.vendor.bt.a2dp.aac_whitelist", Boolean.toString(true));
        }
        this.mBluetoothA2dp.setCodecConfigPreference(device, config);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public BluetoothCodecConfig getCodecConfig(BluetoothDevice device) {
        if (this.mBluetoothA2dp != null) {
            BluetoothCodecStatus codecStatus = this.mBluetoothA2dp.getCodecStatus(device);
            if (codecStatus != null) {
                return codecStatus.getCodecConfig();
            }
        }
        return null;
    }
}
