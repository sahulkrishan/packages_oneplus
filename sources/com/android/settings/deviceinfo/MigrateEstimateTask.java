package com.android.settings.deviceinfo;

import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.telecom.Log;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import java.io.IOException;
import java.util.UUID;

public abstract class MigrateEstimateTask extends AsyncTask<Void, Void, Long> {
    private static final String EXTRA_SIZE_BYTES = "size_bytes";
    private static final long SPEED_ESTIMATE_BPS = 10485760;
    private final Context mContext;
    private long mSizeBytes = -1;

    public abstract void onPostExecute(String str, String str2);

    public MigrateEstimateTask(Context context) {
        this.mContext = context;
    }

    public void copyFrom(Intent intent) {
        this.mSizeBytes = intent.getLongExtra(EXTRA_SIZE_BYTES, -1);
    }

    public void copyTo(Intent intent) {
        intent.putExtra(EXTRA_SIZE_BYTES, this.mSizeBytes);
    }

    /* Access modifiers changed, original: protected|varargs */
    public Long doInBackground(Void... params) {
        if (this.mSizeBytes != -1) {
            return Long.valueOf(this.mSizeBytes);
        }
        UserManager user = (UserManager) this.mContext.getSystemService(UserManager.class);
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        StorageStatsManager stats = (StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class);
        VolumeInfo emulatedVol = storage.findEmulatedForPrivate(this.mContext.getPackageManager().getPrimaryStorageCurrentVolume());
        if (emulatedVol == null) {
            Log.w("StorageSettings", "Failed to find current primary storage", new Object[0]);
            return Long.valueOf(-1);
        }
        try {
            UUID emulatedUuid = storage.getUuidForPath(emulatedVol.getPath());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Measuring size of ");
            stringBuilder.append(emulatedUuid);
            Log.d("StorageSettings", stringBuilder.toString(), new Object[0]);
            long size = 0;
            for (UserInfo u : user.getUsers()) {
                ExternalStorageStats s = stats.queryExternalStatsForUser(emulatedUuid, UserHandle.of(u.id));
                size += s.getTotalBytes();
                if (u.id == 0) {
                    size += s.getObbBytes();
                }
            }
            return Long.valueOf(size);
        } catch (IOException e) {
            Log.w("StorageSettings", "Failed to measure", new Object[]{e});
            return Long.valueOf(-1);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(Long result) {
        this.mSizeBytes = result.longValue();
        onPostExecute(Formatter.formatFileSize(this.mContext, this.mSizeBytes), DateUtils.formatDuration(Math.max((this.mSizeBytes * 1000) / SPEED_ESTIMATE_BPS, 1000)).toString());
    }
}
