package com.android.settingslib.deviceinfo;

import android.app.usage.StorageStatsManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import java.io.IOException;
import java.util.List;

public class StorageManagerVolumeProvider implements StorageVolumeProvider {
    private StorageManager mStorageManager;

    public StorageManagerVolumeProvider(StorageManager sm) {
        this.mStorageManager = sm;
    }

    public long getPrimaryStorageSize() {
        return this.mStorageManager.getPrimaryStorageSize();
    }

    public List<VolumeInfo> getVolumes() {
        return this.mStorageManager.getVolumes();
    }

    public VolumeInfo findEmulatedForPrivate(VolumeInfo privateVolume) {
        return this.mStorageManager.findEmulatedForPrivate(privateVolume);
    }

    public long getTotalBytes(StorageStatsManager stats, VolumeInfo volume) throws IOException {
        return stats.getTotalBytes(volume.getFsUuid());
    }

    public long getFreeBytes(StorageStatsManager stats, VolumeInfo volume) throws IOException {
        return stats.getFreeBytes(volume.getFsUuid());
    }
}
