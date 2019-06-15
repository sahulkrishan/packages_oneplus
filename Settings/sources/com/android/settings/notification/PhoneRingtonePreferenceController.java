package com.android.settings.notification;

import android.content.Context;
import com.android.settings.Utils;

public class PhoneRingtonePreferenceController extends RingtonePreferenceControllerBase {
    private static final String KEY_PHONE_RINGTONE = "ringtone";

    public PhoneRingtonePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_PHONE_RINGTONE;
    }

    public boolean isAvailable() {
        return Utils.isVoiceCapable(this.mContext);
    }

    public int getRingtoneType() {
        return 1;
    }
}
