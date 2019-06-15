package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothAudioCodecPreferenceController extends AbstractBluetoothA2dpPreferenceController {
    private static final String BLUETOOTH_SELECT_A2DP_CODEC_KEY = "bluetooth_select_a2dp_codec";
    private static final int DEFAULT_INDEX = 0;

    public BluetoothAudioCodecPreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context, lifecycle, store);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_A2DP_CODEC_KEY;
    }

    /* Access modifiers changed, original: protected */
    public String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_values);
    }

    /* Access modifiers changed, original: protected */
    public String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_summaries);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIndex() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void writeConfigurationValues(Object newValue) {
        int codecTypeValue = 0;
        int codecPriorityValue = 0;
        switch (this.mPreference.findIndexOfValue(newValue.toString())) {
            case 0:
                switch (this.mPreference.findIndexOfValue(this.mPreference.getValue())) {
                    case 1:
                        codecTypeValue = 0;
                        break;
                    case 2:
                        codecTypeValue = 1;
                        break;
                    case 3:
                        codecTypeValue = 2;
                        break;
                    case 4:
                        codecTypeValue = 3;
                        break;
                    case 5:
                        codecTypeValue = 5;
                        break;
                }
                break;
            case 1:
                codecTypeValue = 0;
                codecPriorityValue = 1000000;
                break;
            case 2:
                codecTypeValue = 1;
                codecPriorityValue = 1000000;
                break;
            case 3:
                codecTypeValue = 2;
                codecPriorityValue = 1000000;
                break;
            case 4:
                codecTypeValue = 3;
                codecPriorityValue = 1000000;
                break;
            case 5:
                codecTypeValue = 5;
                codecPriorityValue = 1000000;
                break;
            case 6:
                synchronized (this.mBluetoothA2dpConfigStore) {
                    if (this.mBluetoothA2dp != null) {
                        this.mBluetoothA2dp.enableOptionalCodecs(null);
                    }
                }
                return;
            case 7:
                synchronized (this.mBluetoothA2dpConfigStore) {
                    if (this.mBluetoothA2dp != null) {
                        this.mBluetoothA2dp.disableOptionalCodecs(null);
                    }
                }
                return;
        }
        this.mBluetoothA2dpConfigStore.setCodecType(codecTypeValue);
        this.mBluetoothA2dpConfigStore.setCodecPriority(codecPriorityValue);
    }

    /* Access modifiers changed, original: protected */
    public int getCurrentA2dpSettingIndex(BluetoothCodecConfig config) {
        int codecType = config.getCodecType();
        if (codecType == 5) {
            return 5;
        }
        switch (codecType) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return 0;
        }
    }
}
