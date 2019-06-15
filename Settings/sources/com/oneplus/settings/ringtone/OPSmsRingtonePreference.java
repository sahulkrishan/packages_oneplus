package com.oneplus.settings.ringtone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings.System;
import android.util.AttributeSet;
import com.android.settings.RingtonePreference;

public class OPSmsRingtonePreference extends RingtonePreference {
    public OPSmsRingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        if (ringtonePickerIntent != null) {
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
            ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, 8);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveRingtone(Uri ringtoneUri) {
        System.putString(getContext().getContentResolver(), "mms_notification", ringtoneUri != null ? ringtoneUri.toString() : null);
    }

    /* Access modifiers changed, original: protected */
    public Uri onRestoreRingtone() {
        return getDefaultSmsNotificationRingtone(getContext());
    }

    private Uri getDefaultSmsNotificationRingtone(Context context) {
        String uriString = System.getString(context.getContentResolver(), "mms_notification");
        return uriString != null ? Uri.parse(uriString) : null;
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        Intent ringtonePickerIntent = new Intent(getContext(), OPRingtonePickerActivity.class);
        ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, onRestoreRingtone());
        ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", getShowSilent());
        ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TYPE, 8);
        ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_TITLE, getTitle());
        getContext().startActivity(ringtonePickerIntent);
    }
}
