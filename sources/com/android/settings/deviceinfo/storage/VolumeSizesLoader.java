package com.android.settings.deviceinfo.storage;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.storage.VolumeInfo;
import android.support.annotation.VisibleForTesting;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageVolumeProvider;
import com.android.settingslib.utils.AsyncLoader;
import java.io.IOException;

public class VolumeSizesLoader extends AsyncLoader<PrivateStorageInfo> {
    private StorageStatsManager mStats;
    private VolumeInfo mVolume;
    private StorageVolumeProvider mVolumeProvider;

    public VolumeSizesLoader(Context context, StorageVolumeProvider volumeProvider, StorageStatsManager stats, VolumeInfo volume) {
        super(context);
        this.mVolumeProvider = volumeProvider;
        this.mStats = stats;
        this.mVolume = volume;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(PrivateStorageInfo result) {
    }

    public PrivateStorageInfo loadInBackground() {
        try {
            return getVolumeSize(this.mVolumeProvider, this.mStats, this.mVolume);
        } catch (IOException e) {
            return null;
        }
    }

    @VisibleForTesting
    static PrivateStorageInfo getVolumeSize(StorageVolumeProvider storageVolumeProvider, StorageStatsManager stats, VolumeInfo info) throws IOException {
        return new PrivateStorageInfo(storageVolumeProvider.getFreeBytes(stats, info), storageVolumeProvider.getTotalBytes(stats, info));
    }
}
