package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settingslib.applications.StorageStatsSource.ExternalStorageStats;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import java.util.concurrent.TimeUnit;

public class CachedStorageValuesHelper {
    public static final String CACHE_APPS_SIZE_KEY = "cache_apps_size";
    public static final String EXTERNAL_APP_BYTES = "external_apps_bytes";
    public static final String EXTERNAL_AUDIO_BYTES = "external_audio_bytes";
    public static final String EXTERNAL_IMAGE_BYTES = "external_image_bytes";
    public static final String EXTERNAL_TOTAL_BYTES = "external_total_bytes";
    public static final String EXTERNAL_VIDEO_BYTES = "external_video_bytes";
    public static final String FREE_BYTES_KEY = "free_bytes";
    public static final String GAME_APPS_SIZE_KEY = "game_apps_size";
    public static final String MUSIC_APPS_SIZE_KEY = "music_apps_size";
    public static final String OTHER_APPS_SIZE_KEY = "other_apps_size";
    public static final String PHOTO_APPS_SIZE_KEY = "photo_apps_size";
    @VisibleForTesting
    public static final String SHARED_PREFERENCES_NAME = "CachedStorageValues";
    public static final String TIMESTAMP_KEY = "last_query_timestamp";
    public static final String TOTAL_BYTES_KEY = "total_bytes";
    public static final String USER_ID_KEY = "user_id";
    public static final String VIDEO_APPS_SIZE_KEY = "video_apps_size";
    private final Long mClobberThreshold;
    protected Clock mClock = new Clock();
    private final SharedPreferences mSharedPreferences;
    private final int mUserId;

    static class Clock {
        Clock() {
        }

        public long getCurrentTime() {
            return System.currentTimeMillis();
        }
    }

    public CachedStorageValuesHelper(Context context, int userId) {
        this.mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        this.mUserId = userId;
        this.mClobberThreshold = Long.valueOf(Global.getLong(context.getContentResolver(), "storage_settings_clobber_threshold", TimeUnit.MINUTES.toMillis(5)));
    }

    public PrivateStorageInfo getCachedPrivateStorageInfo() {
        if (!isDataValid()) {
            return null;
        }
        long freeBytes = this.mSharedPreferences.getLong(FREE_BYTES_KEY, -1);
        long totalBytes = this.mSharedPreferences.getLong(TOTAL_BYTES_KEY, -1);
        if (freeBytes < 0 || totalBytes < 0) {
            return null;
        }
        return new PrivateStorageInfo(freeBytes, totalBytes);
    }

    public SparseArray<AppsStorageResult> getCachedAppsStorageResult() {
        if (!isDataValid()) {
            return null;
        }
        SparseArray<AppsStorageResult> sparseArray;
        long gamesSize = this.mSharedPreferences.getLong(GAME_APPS_SIZE_KEY, -1);
        long musicAppsSize = this.mSharedPreferences.getLong(MUSIC_APPS_SIZE_KEY, -1);
        long videoAppsSize = this.mSharedPreferences.getLong(VIDEO_APPS_SIZE_KEY, -1);
        long photoAppSize = this.mSharedPreferences.getLong(PHOTO_APPS_SIZE_KEY, -1);
        long otherAppsSize = this.mSharedPreferences.getLong(OTHER_APPS_SIZE_KEY, -1);
        long cacheSize = this.mSharedPreferences.getLong(CACHE_APPS_SIZE_KEY, -1);
        long j;
        long j2;
        long j3;
        long j4;
        if (gamesSize < 0 || musicAppsSize < 0 || videoAppsSize < 0 || photoAppSize < 0 || otherAppsSize < 0) {
            j = cacheSize;
            j2 = videoAppsSize;
            j3 = photoAppSize;
            j4 = otherAppsSize;
            sparseArray = null;
        } else if (cacheSize < 0) {
            j = cacheSize;
            j2 = videoAppsSize;
            j3 = photoAppSize;
            j4 = otherAppsSize;
            sparseArray = null;
        } else {
            long cacheSize2 = cacheSize;
            long externalTotalBytes = this.mSharedPreferences.getLong(EXTERNAL_TOTAL_BYTES, -1);
            long otherAppsSize2 = otherAppsSize;
            otherAppsSize = this.mSharedPreferences.getLong(EXTERNAL_AUDIO_BYTES, -1);
            long photoAppSize2 = photoAppSize;
            photoAppSize = this.mSharedPreferences.getLong(EXTERNAL_VIDEO_BYTES, -1);
            long videoAppsSize2 = videoAppsSize;
            videoAppsSize = this.mSharedPreferences.getLong(EXTERNAL_IMAGE_BYTES, -1);
            long externalAppBytes = this.mSharedPreferences.getLong(EXTERNAL_APP_BYTES, -1);
            if (externalTotalBytes < 0 || otherAppsSize < 0 || photoAppSize < 0 || videoAppsSize < 0) {
                j = cacheSize2;
                j4 = otherAppsSize2;
                j3 = photoAppSize2;
                j2 = videoAppsSize2;
            } else if (externalAppBytes < 0) {
                long j5 = externalTotalBytes;
                j = cacheSize2;
                j4 = otherAppsSize2;
                j3 = photoAppSize2;
                j2 = videoAppsSize2;
            } else {
                ExternalStorageStats externalStorageStats = new ExternalStorageStats(externalTotalBytes, otherAppsSize, photoAppSize, videoAppsSize, externalAppBytes);
                AppsStorageResult result = new AppsStorageResult();
                result.gamesSize = gamesSize;
                result.musicAppsSize = musicAppsSize;
                externalTotalBytes = videoAppsSize2;
                result.videoAppsSize = externalTotalBytes;
                j2 = externalTotalBytes;
                externalTotalBytes = photoAppSize2;
                result.photosAppsSize = externalTotalBytes;
                j3 = externalTotalBytes;
                externalTotalBytes = otherAppsSize2;
                result.otherAppsSize = externalTotalBytes;
                j4 = externalTotalBytes;
                externalTotalBytes = cacheSize2;
                result.cacheSize = externalTotalBytes;
                result.externalStats = externalStorageStats;
                ExternalStorageStats externalStats = externalStorageStats;
                sparseArray = new SparseArray();
                j = externalTotalBytes;
                sparseArray.append(this.mUserId, result);
                return sparseArray;
            }
            return null;
        }
        return sparseArray;
    }

    public void cacheResult(PrivateStorageInfo storageInfo, AppsStorageResult result) {
        this.mSharedPreferences.edit().putLong(FREE_BYTES_KEY, storageInfo.freeBytes).putLong(TOTAL_BYTES_KEY, storageInfo.totalBytes).putLong(GAME_APPS_SIZE_KEY, result.gamesSize).putLong(MUSIC_APPS_SIZE_KEY, result.musicAppsSize).putLong(VIDEO_APPS_SIZE_KEY, result.videoAppsSize).putLong(PHOTO_APPS_SIZE_KEY, result.photosAppsSize).putLong(OTHER_APPS_SIZE_KEY, result.otherAppsSize).putLong(CACHE_APPS_SIZE_KEY, result.cacheSize).putLong(EXTERNAL_TOTAL_BYTES, result.externalStats.totalBytes).putLong(EXTERNAL_AUDIO_BYTES, result.externalStats.audioBytes).putLong(EXTERNAL_VIDEO_BYTES, result.externalStats.videoBytes).putLong(EXTERNAL_IMAGE_BYTES, result.externalStats.imageBytes).putLong(EXTERNAL_APP_BYTES, result.externalStats.appBytes).putInt("user_id", this.mUserId).putLong(TIMESTAMP_KEY, this.mClock.getCurrentTime()).apply();
    }

    private boolean isDataValid() {
        boolean z = false;
        if (this.mSharedPreferences.getInt("user_id", -1) != this.mUserId) {
            return false;
        }
        if (this.mClock.getCurrentTime() - this.mSharedPreferences.getLong(TIMESTAMP_KEY, Long.MAX_VALUE) < this.mClobberThreshold.longValue()) {
            z = true;
        }
        return z;
    }
}
