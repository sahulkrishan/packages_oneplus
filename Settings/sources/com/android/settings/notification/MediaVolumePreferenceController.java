package com.android.settings.notification;

import android.content.Context;
import android.text.TextUtils;
import com.android.settings.R;

public class MediaVolumePreferenceController extends VolumeSeekBarPreferenceController {
    private static final String KEY_MEDIA_VOLUME = "media_volume";

    public MediaVolumePreferenceController(Context context) {
        super(context, KEY_MEDIA_VOLUME);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_media_volume)) {
            return 0;
        }
        return 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_MEDIA_VOLUME);
    }

    public String getPreferenceKey() {
        return KEY_MEDIA_VOLUME;
    }

    public int getAudioStream() {
        return 3;
    }

    public int getMuteIcon() {
        return R.drawable.ic_media_stream_off;
    }
}
