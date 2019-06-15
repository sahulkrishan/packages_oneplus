package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothAudioBitsPerSamplePreferenceController extends AbstractBluetoothA2dpPreferenceController {
    private static final String BLUETOOTH_SELECT_A2DP_BITS_PER_SAMPLE_KEY = "bluetooth_select_a2dp_bits_per_sample";
    private static final int DEFAULT_INDEX = 0;

    public BluetoothAudioBitsPerSamplePreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context, lifecycle, store);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_A2DP_BITS_PER_SAMPLE_KEY;
    }

    /* Access modifiers changed, original: protected */
    public String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_bits_per_sample_values);
    }

    /* Access modifiers changed, original: protected */
    public String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_bits_per_sample_summaries);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIndex() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void writeConfigurationValues(Object newValue) {
        int bitsPerSampleValue = 0;
        switch (this.mPreference.findIndexOfValue(newValue.toString())) {
            case 1:
                bitsPerSampleValue = 1;
                break;
            case 2:
                bitsPerSampleValue = 2;
                break;
            case 3:
                bitsPerSampleValue = 4;
                break;
        }
        this.mBluetoothA2dpConfigStore.setBitsPerSample(bitsPerSampleValue);
    }

    /* Access modifiers changed, original: protected */
    public int getCurrentA2dpSettingIndex(BluetoothCodecConfig config) {
        int bitsPerSample = config.getBitsPerSample();
        if (bitsPerSample == 4) {
            return 3;
        }
        switch (bitsPerSample) {
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }
}
