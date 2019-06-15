package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import com.oneplus.settings.ringtone.OPRingtonePickerActivity;

public class DefaultRingtonePreference extends RingtonePreference {
    private static final String TAG = "DefaultRingtonePreference";

    public DefaultRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        if (ringtonePickerIntent != null) {
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveRingtone(Uri ringtoneUri) {
        RingtoneManager.setActualDefaultRingtoneUri(this.mUserContext, getRingtoneType(), ringtoneUri);
    }

    /* Access modifiers changed, original: protected */
    public Uri onRestoreRingtone() {
        return OPRingtoneManager.getActualDefaultRingtoneUri(this.mUserContext, getRingtoneType());
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        Log.d("volume", "ringtone click");
        Intent intent = new Intent(getContext(), OPRingtonePickerActivity.class);
        onPrepareRingtonePickerIntent(intent);
        intent.putExtra("CURRENT_USER_ID", getUserId());
        getContext().startActivity(intent);
    }
}
