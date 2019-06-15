package com.android.settings.notification;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import com.android.settings.R;

public class CallVolumePreferenceController extends VolumeSeekBarPreferenceController {
    private AudioManager mAudioManager;

    public CallVolumePreferenceController(Context context, String key) {
        super(context, key);
        this.mAudioManager = (AudioManager) context.getSystemService(AudioManager.class);
    }

    public int getAvailabilityStatus() {
        return (!this.mContext.getResources().getBoolean(R.bool.config_show_call_volume) || this.mHelper.isSingleVolume()) ? 2 : 0;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "call_volume");
    }

    public int getAudioStream() {
        if (this.mAudioManager.isBluetoothScoOn()) {
            return 6;
        }
        return 0;
    }

    public int getMuteIcon() {
        return R.drawable.ic_local_phone_24_lib;
    }
}
