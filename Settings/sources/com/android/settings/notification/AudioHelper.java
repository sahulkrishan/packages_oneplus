package com.android.settings.notification;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.Utils;

public class AudioHelper {
    private AudioManager mAudioManager = ((AudioManager) this.mContext.getSystemService("audio"));
    private Context mContext;

    public AudioHelper(Context context) {
        this.mContext = context;
    }

    public boolean isSingleVolume() {
        return AudioSystem.isSingleVolume(this.mContext);
    }

    public int getManagedProfileId(UserManager um) {
        return Utils.getManagedProfileId(um, UserHandle.myUserId());
    }

    public boolean isUserUnlocked(UserManager um, int userId) {
        return um.isUserUnlocked(userId);
    }

    public Context createPackageContextAsUser(int profileId) {
        return Utils.createPackageContextAsUser(this.mContext, profileId);
    }

    public int getRingerModeInternal() {
        return this.mAudioManager.getRingerModeInternal();
    }

    public int getLastAudibleStreamVolume(int stream) {
        return this.mAudioManager.getLastAudibleStreamVolume(stream);
    }

    public int getStreamVolume(int stream) {
        return this.mAudioManager.getStreamVolume(stream);
    }

    public boolean setStreamVolume(int stream, int volume) {
        this.mAudioManager.setStreamVolume(stream, volume, 0);
        return true;
    }

    public int getMaxVolume(int stream) {
        return this.mAudioManager.getStreamMaxVolume(stream);
    }
}
