package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothAudioSampleRatePreferenceController extends AbstractBluetoothA2dpPreferenceController {
    private static final String BLUETOOTH_SELECT_A2DP_SAMPLE_RATE_KEY = "bluetooth_select_a2dp_sample_rate";
    private static final int DEFAULT_INDEX = 0;

    public BluetoothAudioSampleRatePreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context, lifecycle, store);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_A2DP_SAMPLE_RATE_KEY;
    }

    /* Access modifiers changed, original: protected */
    public String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_sample_rate_values);
    }

    /* Access modifiers changed, original: protected */
    public String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_sample_rate_summaries);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIndex() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void writeConfigurationValues(Object newValue) {
        int sampleRateValue = 0;
        switch (this.mPreference.findIndexOfValue(newValue.toString())) {
            case 0:
                sampleRateValue = 0;
                break;
            case 1:
                sampleRateValue = 1;
                break;
            case 2:
                sampleRateValue = 2;
                break;
            case 3:
                sampleRateValue = 4;
                break;
            case 4:
                sampleRateValue = 8;
                break;
        }
        this.mBluetoothA2dpConfigStore.setSampleRate(sampleRateValue);
    }

    /* Access modifiers changed, original: protected */
    public int getCurrentA2dpSettingIndex(BluetoothCodecConfig config) {
        int sampleRate = config.getSampleRate();
        if (sampleRate == 4) {
            return 3;
        }
        if (sampleRate == 8) {
            return 4;
        }
        switch (sampleRate) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }
}
