package com.android.settings.deviceinfo;

import android.content.Intent;
import android.content.pm.IPackageMoveObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IVoldTaskListener.Stub;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StorageWizardFormatProgress extends StorageWizardBase {
    private static final String PROP_DEBUG_STORAGE_SLOW = "sys.debug.storage_slow";
    private boolean mFormatPrivate;
    private PartitionTask mTask;

    public static class PartitionTask extends AsyncTask<Void, Integer, Exception> {
        public StorageWizardFormatProgress mActivity;
        private volatile long mPrivateBench;
        private volatile int mProgress = 20;

        /* Access modifiers changed, original: protected|varargs */
        public Exception doInBackground(Void... params) {
            StorageWizardFormatProgress activity = this.mActivity;
            StorageManager storage = this.mActivity.mStorage;
            try {
                if (activity.mFormatPrivate) {
                    storage.partitionPrivate(activity.mDisk.getId());
                    publishProgress(new Integer[]{Integer.valueOf(40)});
                    VolumeInfo privateVol = activity.findFirstVolume(1, 25);
                    final CompletableFuture<PersistableBundle> result = new CompletableFuture();
                    storage.benchmark(privateVol.getId(), new Stub() {
                        public void onStatus(int status, PersistableBundle extras) {
                            PartitionTask.this.publishProgress(new Integer[]{Integer.valueOf(40 + ((status * 40) / 100))});
                        }

                        public void onFinished(int status, PersistableBundle extras) {
                            result.complete(extras);
                        }
                    });
                    this.mPrivateBench = ((PersistableBundle) result.get(60, TimeUnit.SECONDS)).getLong("run", Long.MAX_VALUE);
                    if (activity.mDisk.isDefaultPrimary() && Objects.equals(storage.getPrimaryStorageUuid(), "primary_physical")) {
                        Log.d("StorageSettings", "Just formatted primary physical; silently moving storage to new emulated volume");
                        storage.setPrimaryStorageUuid(privateVol.getFsUuid(), new SilentObserver());
                    }
                } else {
                    storage.partitionPublic(activity.mDisk.getId());
                }
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        /* Access modifiers changed, original: protected|varargs */
        public void onProgressUpdate(Integer... progress) {
            this.mProgress = progress[0].intValue();
            this.mActivity.setCurrentProgress(this.mProgress);
        }

        public void setActivity(StorageWizardFormatProgress activity) {
            this.mActivity = activity;
            this.mActivity.setCurrentProgress(this.mProgress);
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Exception e) {
            StorageWizardFormatProgress activity = this.mActivity;
            if (!activity.isDestroyed()) {
                if (e != null) {
                    Log.e("StorageSettings", "Failed to partition", e);
                    Toast.makeText(activity, e.getMessage(), 1).show();
                    activity.finishAffinity();
                    return;
                }
                if (activity.mFormatPrivate) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("New volume took ");
                    stringBuilder.append(this.mPrivateBench);
                    stringBuilder.append("ms to run benchmark");
                    Log.d("StorageSettings", stringBuilder.toString());
                    if (this.mPrivateBench > 2000 || SystemProperties.getBoolean(StorageWizardFormatProgress.PROP_DEBUG_STORAGE_SLOW, false)) {
                        this.mActivity.onFormatFinishedSlow();
                    } else {
                        this.mActivity.onFormatFinished();
                    }
                } else {
                    this.mActivity.onFormatFinished();
                }
            }
        }
    }

    private static class SilentObserver extends IPackageMoveObserver.Stub {
        private SilentObserver() {
        }

        public void onCreated(int moveId, Bundle extras) {
        }

        public void onStatusChanged(int moveId, int status, long estMillis) {
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mDisk == null) {
            finish();
            return;
        }
        setContentView(R.layout.storage_wizard_progress);
        setKeepScreenOn(true);
        this.mFormatPrivate = getIntent().getBooleanExtra("format_private", false);
        setHeaderText(R.string.storage_wizard_format_progress_title, getDiskShortDescription());
        setBodyText(R.string.storage_wizard_format_progress_body, getDiskDescription());
        this.mTask = (PartitionTask) getLastNonConfigurationInstance();
        if (this.mTask == null) {
            this.mTask = new PartitionTask();
            this.mTask.setActivity(this);
            this.mTask.execute(new Void[0]);
        } else {
            this.mTask.setActivity(this);
        }
    }

    public Object onRetainNonConfigurationInstance() {
        return this.mTask;
    }

    public void onFormatFinished() {
        Intent intent = new Intent(this, StorageWizardFormatSlow.class);
        intent.putExtra("format_slow", false);
        startActivity(intent);
        finishAffinity();
    }

    public void onFormatFinishedSlow() {
        Intent intent = new Intent(this, StorageWizardFormatSlow.class);
        intent.putExtra("format_slow", true);
        startActivity(intent);
        finishAffinity();
    }
}
