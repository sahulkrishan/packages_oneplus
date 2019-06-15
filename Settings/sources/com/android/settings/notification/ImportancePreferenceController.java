package com.android.settings.notification;

import android.app.NotificationChannel;
import android.content.Context;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.core.PreferenceControllerMixin;

public class ImportancePreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_IMPORTANCE = "importance";
    private ImportanceListener mImportanceListener;

    public ImportancePreferenceController(Context context, ImportanceListener importanceListener, NotificationBackend backend) {
        super(context, backend);
        this.mImportanceListener = importanceListener;
    }

    public String getPreferenceKey() {
        return KEY_IMPORTANCE;
    }

    public boolean isAvailable() {
        if (super.isAvailable() && this.mChannel != null) {
            return isDefaultChannel() ^ 1;
        }
        return false;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null && this.mChannel != null) {
            boolean z = this.mAdmin == null && isChannelConfigurable();
            preference.setEnabled(z);
            preference.setSummary(getImportanceSummary(this.mChannel));
            CharSequence[] entries = new CharSequence[4];
            CharSequence[] values = new CharSequence[4];
            int index = 0;
            for (int i = 4; i >= 1; i--) {
                entries[index] = getImportanceSummary(new NotificationChannel("", "", i));
                values[index] = String.valueOf(i);
                index++;
            }
            RestrictedListPreference pref = (RestrictedListPreference) preference;
            pref.setEntries(entries);
            pref.setEntryValues(values);
            pref.setValue(String.valueOf(this.mChannel.getImportance()));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            int importance = Integer.parseInt((String) newValue);
            if (this.mChannel.getImportance() < 3 && !SoundPreferenceController.hasValidSound(this.mChannel) && importance >= 3) {
                this.mChannel.setSound(RingtoneManager.getDefaultUri(2), this.mChannel.getAudioAttributes());
                this.mChannel.lockFields(32);
            }
            this.mChannel.setImportance(importance);
            this.mChannel.lockFields(4);
            saveChannel();
            this.mImportanceListener.onImportanceChanged();
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getImportanceSummary(NotificationChannel channel) {
        String summary = "";
        int importance = channel.getImportance();
        if (importance != NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) {
            switch (importance) {
                case 1:
                    summary = this.mContext.getString(R.string.notification_importance_min);
                    break;
                case 2:
                    summary = this.mContext.getString(R.string.notification_importance_low);
                    break;
                case 3:
                    if (!SoundPreferenceController.hasValidSound(channel)) {
                        summary = this.mContext.getString(R.string.notification_importance_low);
                        break;
                    }
                    summary = this.mContext.getString(R.string.notification_importance_default);
                    break;
                case 4:
                case 5:
                    if (!SoundPreferenceController.hasValidSound(channel)) {
                        summary = this.mContext.getString(R.string.notification_importance_high_silent);
                        break;
                    }
                    summary = this.mContext.getString(R.string.notification_importance_high);
                    break;
                default:
                    return "";
            }
        }
        summary = this.mContext.getString(R.string.notification_importance_unspecified);
        return summary;
    }
}
