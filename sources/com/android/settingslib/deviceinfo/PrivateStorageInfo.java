package com.android.settingslib.deviceinfo;

import android.app.AppGlobals;
import android.app.usage.StorageStatsManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import java.io.IOException;

public class PrivateStorageInfo {
    private static final String TAG = "PrivateStorageInfo";
    public final long freeBytes;
    public final long totalBytes;

    public PrivateStorageInfo(long freeBytes, long totalBytes) {
        this.freeBytes = freeBytes;
        this.totalBytes = totalBytes;
    }

    public static PrivateStorageInfo getPrivateStorageInfo(StorageVolumeProvider sm) {
        StorageStatsManager stats = (StorageStatsManager) AppGlobals.getInitialApplication().getSystemService(StorageStatsManager.class);
        long privateFreeBytes = 0;
        long privateTotalBytes = 0;
        for (VolumeInfo info : sm.getVolumes()) {
            if (info.getType() == 1 && info.isMountedReadable()) {
                try {
                    privateTotalBytes += sm.getTotalBytes(stats, info);
                    privateFreeBytes += sm.getFreeBytes(stats, info);
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
        return new PrivateStorageInfo(privateFreeBytes, privateTotalBytes);
    }

    public static long getTotalSize(VolumeInfo info, long totalInternalStorage) {
        try {
            return ((StorageStatsManager) AppGlobals.getInitialApplication().getSystemService(StorageStatsManager.class)).getTotalBytes(info.getFsUuid());
        } catch (IOException e) {
            Log.w(TAG, e);
            return 0;
        }
    }
}
