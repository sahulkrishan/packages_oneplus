package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.oneplus.settings.ringtone.OPRingtoneManager;
import com.oneplus.settings.ringtone.OPRingtonePickerActivity;

public class NotificationSoundPreference extends RingtonePreference {
    private Uri mRingtone;

    public NotificationSoundPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIntent(new Intent(context, OPRingtonePickerActivity.class));
    }

    /* Access modifiers changed, original: protected */
    public Uri onRestoreRingtone() {
        return this.mRingtone;
    }

    public void setRingtone(Uri ringtone) {
        this.mRingtone = ringtone;
        setSummary((CharSequence) " ");
        updateRingtoneName(this.mRingtone);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = (Uri) data.getParcelableExtra(OPRingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            setRingtone(uri);
            callChangeListener(uri);
        }
        return true;
    }

    private void updateRingtoneName(final Uri uri) {
        new AsyncTask<Object, Void, CharSequence>() {
            /* Access modifiers changed, original: protected|varargs */
            public CharSequence doInBackground(Object... params) {
                if (uri == null) {
                    return NotificationSoundPreference.this.getContext().getString(17040838);
                }
                if (RingtoneManager.isDefault(uri)) {
                    return NotificationSoundPreference.this.getContext().getString(R.string.notification_sound_default);
                }
                if ("android.resource".equals(uri.getScheme())) {
                    return NotificationSoundPreference.this.getContext().getString(R.string.notification_unknown_sound_title);
                }
                return Ringtone.getTitle(NotificationSoundPreference.this.getContext(), uri, false, true);
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(CharSequence name) {
                NotificationSoundPreference.this.setSummary(name);
            }
        }.execute(new Object[0]);
    }
}
