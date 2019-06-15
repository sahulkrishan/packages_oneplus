package com.android.settings.notification;

import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.oneplus.settings.utils.OPConstants;

public class SoundPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, OnActivityResultListener {
    protected static final int CODE = 200;
    private static final String KEY_SOUND = "ringtone";
    private final SettingsPreferenceFragment mFragment;
    private final ImportanceListener mListener;
    private NotificationSoundPreference mPreference;

    public SoundPreferenceController(Context context, SettingsPreferenceFragment hostFragment, ImportanceListener importanceListener, NotificationBackend backend) {
        super(context, backend);
        this.mFragment = hostFragment;
        this.mListener = importanceListener;
    }

    public String getPreferenceKey() {
        return KEY_SOUND;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable() || this.mChannel == null) {
            return false;
        }
        if (this.mAppRow.pkg != null && this.mAppRow.pkg.equals(OPConstants.PACKAGENAME_MMS) && this.mChannel.getId().equals("default")) {
            return false;
        }
        if (checkCanBeVisible(3) && !isDefaultChannel()) {
            z = true;
        }
        return z;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (NotificationSoundPreference) screen.findPreference(getPreferenceKey());
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null && this.mChannel != null) {
            NotificationSoundPreference pref = (NotificationSoundPreference) preference;
            boolean z = this.mAdmin == null && isChannelConfigurable();
            pref.setEnabled(z);
            pref.setRingtone(this.mChannel.getSound());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            this.mChannel.setSound((Uri) newValue, this.mChannel.getAudioAttributes());
            saveChannel();
        }
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_SOUND.equals(preference.getKey()) || this.mFragment == null) {
            return false;
        }
        NotificationSoundPreference pref = (NotificationSoundPreference) preference;
        pref.onPrepareRingtonePickerIntent(pref.getIntent());
        this.mFragment.startActivityForResult(preference.getIntent(), 200);
        return true;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (200 != requestCode) {
            return false;
        }
        if (this.mPreference != null) {
            this.mPreference.onActivityResult(requestCode, resultCode, data);
        }
        this.mListener.onImportanceChanged();
        return true;
    }

    protected static boolean hasValidSound(NotificationChannel channel) {
        return (channel == null || channel.getSound() == null || Uri.EMPTY.equals(channel.getSound())) ? false : true;
    }
}
