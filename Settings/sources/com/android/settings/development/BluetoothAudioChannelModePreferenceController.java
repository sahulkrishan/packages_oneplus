package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothAudioChannelModePreferenceController extends AbstractBluetoothA2dpPreferenceController {
    private static final String BLUETOOTH_SELECT_A2DP_CHANNEL_MODE_KEY = "bluetooth_select_a2dp_channel_mode";
    private static final int DEFAULT_INDEX = 0;

    public BluetoothAudioChannelModePreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context, lifecycle, store);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_A2DP_CHANNEL_MODE_KEY;
    }

    /* Access modifiers changed, original: protected */
    public String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_channel_mode_values);
    }

    /* Access modifiers changed, original: protected */
    public String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_channel_mode_summaries);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIndex() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void writeConfigurationValues(Object newValue) {
        int channelModeValue = 0;
        switch (this.mPreference.findIndexOfValue(newValue.toString())) {
            case 1:
                channelModeValue = 1;
                break;
            case 2:
                channelModeValue = 2;
                break;
        }
        this.mBluetoothA2dpConfigStore.setChannelMode(channelModeValue);
    }

    /* Access modifiers changed, original: protected */
    public int getCurrentA2dpSettingIndex(BluetoothCodecConfig config) {
        switch (config.getChannelMode()) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }
}
