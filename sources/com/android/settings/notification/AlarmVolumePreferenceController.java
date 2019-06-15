package com.android.settings.notification;

import android.content.Context;
import android.text.TextUtils;
import com.android.settings.R;

public class AlarmVolumePreferenceController extends VolumeSeekBarPreferenceController {
    private static final String KEY_ALARM_VOLUME = "alarm_volume";

    public AlarmVolumePreferenceController(Context context) {
        super(context, KEY_ALARM_VOLUME);
    }

    public int getAvailabilityStatus() {
        return (!this.mContext.getResources().getBoolean(R.bool.config_show_alarm_volume) || this.mHelper.isSingleVolume()) ? 2 : 0;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_ALARM_VOLUME);
    }

    public String getPreferenceKey() {
        return KEY_ALARM_VOLUME;
    }

    public int getAudioStream() {
        return 4;
    }

    public int getMuteIcon() {
        return 17302265;
    }
}
