package com.android.settings.development;

import android.bluetooth.BluetoothCodecConfig;
import android.content.Context;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothAudioQualityPreferenceController extends AbstractBluetoothA2dpPreferenceController {
    private static final String BLUETOOTH_SELECT_A2DP_LDAC_PLAYBACK_QUALITY_KEY = "bluetooth_select_a2dp_ldac_playback_quality";
    private static final int DEFAULT_INDEX = 3;

    public BluetoothAudioQualityPreferenceController(Context context, Lifecycle lifecycle, BluetoothA2dpConfigStore store) {
        super(context, lifecycle, store);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SELECT_A2DP_LDAC_PLAYBACK_QUALITY_KEY;
    }

    /* Access modifiers changed, original: protected */
    public String[] getListValues() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_ldac_playback_quality_values);
    }

    /* Access modifiers changed, original: protected */
    public String[] getListSummaries() {
        return this.mContext.getResources().getStringArray(R.array.bluetooth_a2dp_codec_ldac_playback_quality_summaries);
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIndex() {
        return 3;
    }

    /* Access modifiers changed, original: protected */
    public void writeConfigurationValues(Object newValue) {
        int index = this.mPreference.findIndexOfValue(newValue.toString());
        int codecSpecific1Value = 0;
        switch (index) {
            case 0:
            case 1:
            case 2:
            case 3:
                codecSpecific1Value = 1000 + index;
                break;
        }
        this.mBluetoothA2dpConfigStore.setCodecSpecific1Value(codecSpecific1Value);
    }

    /* Access modifiers changed, original: protected */
    public int getCurrentA2dpSettingIndex(BluetoothCodecConfig config) {
        int index = (int) config.getCodecSpecific1();
        if (index > 0) {
            index %= 10;
        } else {
            index = 3;
        }
        switch (index) {
            case 0:
            case 1:
            case 2:
            case 3:
                return index;
            default:
                return 3;
        }
    }
}
