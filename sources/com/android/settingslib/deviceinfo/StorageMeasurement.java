package com.android.settingslib.deviceinfo;

import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseLongArray;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class StorageMeasurement {
    private static final String TAG = "StorageMeasurement";
    private final Context mContext;
    private WeakReference<MeasurementReceiver> mReceiver;
    private final VolumeInfo mSharedVolume;
    private final StorageStatsManager mStats = ((StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class));
    private final UserManager mUser = ((UserManager) this.mContext.getSystemService(UserManager.class));
    private final VolumeInfo mVolume;

    private class MeasureTask extends AsyncTask<Void, Void, MeasurementDetails> {
        private MeasureTask() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public MeasurementDetails doInBackground(Void... params) {
            return StorageMeasurement.this.measureExactStorage();
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(MeasurementDetails result) {
            MeasurementReceiver receiver = StorageMeasurement.this.mReceiver != null ? (MeasurementReceiver) StorageMeasurement.this.mReceiver.get() : null;
            if (receiver != null) {
                receiver.onDetailsChanged(result);
            }
        }
    }

    public static class MeasurementDetails {
        public SparseLongArray appsSize = new SparseLongArray();
        public long availSize;
        public long cacheSize;
        public SparseArray<HashMap<String, Long>> mediaSize = new SparseArray();
        public SparseLongArray miscSize = new SparseLongArray();
        public long totalSize;
        public SparseLongArray usersSize = new SparseLongArray();

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MeasurementDetails: [totalSize: ");
            stringBuilder.append(this.totalSize);
            stringBuilder.append(" availSize: ");
            stringBuilder.append(this.availSize);
            stringBuilder.append(" cacheSize: ");
            stringBuilder.append(this.cacheSize);
            stringBuilder.append(" mediaSize: ");
            stringBuilder.append(this.mediaSize);
            stringBuilder.append(" miscSize: ");
            stringBuilder.append(this.miscSize);
            stringBuilder.append("usersSize: ");
            stringBuilder.append(this.usersSize);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }

    public interface MeasurementReceiver {
        void onDetailsChanged(MeasurementDetails measurementDetails);
    }

    public StorageMeasurement(Context context, VolumeInfo volume, VolumeInfo sharedVolume) {
        this.mContext = context.getApplicationContext();
        this.mVolume = volume;
        this.mSharedVolume = sharedVolume;
    }

    public void setReceiver(MeasurementReceiver receiver) {
        if (this.mReceiver == null || this.mReceiver.get() == null) {
            this.mReceiver = new WeakReference(receiver);
        }
    }

    public void forceMeasure() {
        measure();
    }

    public void measure() {
        new MeasureTask().execute(new Void[0]);
    }

    public void onDestroy() {
        this.mReceiver = null;
    }

    private MeasurementDetails measureExactStorage() {
        List<UserInfo> users = this.mUser.getUsers();
        long start = SystemClock.elapsedRealtime();
        MeasurementDetails details = new MeasurementDetails();
        if (this.mVolume == null) {
            return details;
        }
        if (this.mVolume.getType() == 0) {
            details.totalSize = this.mVolume.getPath().getTotalSpace();
            details.availSize = this.mVolume.getPath().getUsableSpace();
            return details;
        }
        try {
            details.totalSize = this.mStats.getTotalBytes(this.mVolume.fsUuid);
            details.availSize = this.mStats.getFreeBytes(this.mVolume.fsUuid);
            long finishTotal = SystemClock.elapsedRealtime();
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Measured total storage in ");
            stringBuilder.append(finishTotal - start);
            stringBuilder.append("ms");
            Log.d(str, stringBuilder.toString());
            if (this.mSharedVolume != null && this.mSharedVolume.isMountedReadable()) {
                for (UserInfo user : users) {
                    HashMap<String, Long> mediaMap = new HashMap();
                    details.mediaSize.put(user.id, mediaMap);
                    try {
                        ExternalStorageStats stats = this.mStats.queryExternalStatsForUser(this.mSharedVolume.fsUuid, UserHandle.of(user.id));
                        addValue(details.usersSize, user.id, stats.getTotalBytes());
                        mediaMap.put(Environment.DIRECTORY_MUSIC, Long.valueOf(stats.getAudioBytes()));
                        mediaMap.put(Environment.DIRECTORY_MOVIES, Long.valueOf(stats.getVideoBytes()));
                        mediaMap.put(Environment.DIRECTORY_PICTURES, Long.valueOf(stats.getImageBytes()));
                        addValue(details.miscSize, user.id, ((stats.getTotalBytes() - stats.getAudioBytes()) - stats.getVideoBytes()) - stats.getImageBytes());
                    } catch (IOException e) {
                        Log.w(TAG, e);
                    }
                }
            }
            long finishShared = SystemClock.elapsedRealtime();
            str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Measured shared storage in ");
            stringBuilder2.append(finishShared - finishTotal);
            stringBuilder2.append("ms");
            Log.d(str, stringBuilder2.toString());
            if (this.mVolume.getType() == 1 && this.mVolume.isMountedReadable()) {
                for (UserInfo user2 : users) {
                    try {
                        StorageStats stats2 = this.mStats.queryStatsForUser(this.mVolume.fsUuid, UserHandle.of(user2.id));
                        if (user2.id == UserHandle.myUserId()) {
                            addValue(details.usersSize, user2.id, stats2.getCodeBytes());
                        }
                        addValue(details.usersSize, user2.id, stats2.getDataBytes());
                        addValue(details.appsSize, user2.id, stats2.getCodeBytes() + stats2.getDataBytes());
                        details.cacheSize += stats2.getCacheBytes();
                    } catch (IOException e2) {
                        Log.w(TAG, e2);
                    }
                }
            }
            long finishPrivate = SystemClock.elapsedRealtime();
            str = TAG;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Measured private storage in ");
            stringBuilder3.append(finishPrivate - finishShared);
            stringBuilder3.append("ms");
            Log.d(str, stringBuilder3.toString());
            return details;
        } catch (IOException e22) {
            Log.w(TAG, e22);
            return details;
        }
    }

    private static void addValue(SparseLongArray array, int key, long value) {
        array.put(key, array.get(key) + value);
    }
}
