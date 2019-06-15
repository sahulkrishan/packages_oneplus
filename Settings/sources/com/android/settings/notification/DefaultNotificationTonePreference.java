package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.settings.DefaultRingtonePreference;
import com.oneplus.settings.ringtone.OPRingtoneManager;

public class DefaultNotificationTonePreference extends DefaultRingtonePreference {
    private Uri mRingtone;

    public DefaultNotificationTonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public Uri onRestoreRingtone() {
        return this.mRingtone;
    }

    public void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        super.onPrepareRingtonePickerIntent(ringtonePickerIntent);
        ringtonePickerIntent.putExtra(OPRingtoneManager.EXTRA_RINGTONE_EXISTING_URI, this.mRingtone);
    }

    public void setRingtone(Uri ringtone) {
        this.mRingtone = ringtone;
        updateRingtoneName(this.mRingtone);
    }

    private void updateRingtoneName(final Uri uri) {
        new AsyncTask<Object, Void, CharSequence>() {
            /* Access modifiers changed, original: protected|varargs */
            public CharSequence doInBackground(Object... params) {
                return Ringtone.getTitle(DefaultNotificationTonePreference.this.mUserContext, uri, false, true);
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(CharSequence name) {
                DefaultNotificationTonePreference.this.setSummary(name);
            }
        }.execute(new Object[0]);
    }
}
