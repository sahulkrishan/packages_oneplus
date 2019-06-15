package com.android.settings.applications.manageapplications;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.support.annotation.WorkerThread;
import android.text.format.Formatter;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.applications.StorageStatsSource.ExternalStorageStats;
import java.io.IOException;

public class PhotosViewHolderController implements FileViewHolderController {
    private static final String IMAGE_MIME_TYPE = "image/*";
    private static final String TAG = "PhotosViewHolderCtrl";
    private Context mContext;
    private long mFilesSize;
    private StorageStatsSource mSource;
    private UserHandle mUser;
    private String mVolumeUuid;

    public PhotosViewHolderController(Context context, StorageStatsSource source, String volumeUuid, UserHandle user) {
        this.mContext = context;
        this.mSource = source;
        this.mVolumeUuid = volumeUuid;
        this.mUser = user;
    }

    @WorkerThread
    public void queryStats() {
        try {
            ExternalStorageStats stats = this.mSource.getExternalStorageStats(this.mVolumeUuid, this.mUser);
            this.mFilesSize = stats.imageBytes + stats.videoBytes;
        } catch (IOException e) {
            this.mFilesSize = 0;
            Log.w(TAG, e);
        }
    }

    public boolean shouldShow() {
        return true;
    }

    public void setupView(ApplicationViewHolder holder) {
        holder.setIcon((int) R.drawable.ic_photo_library);
        holder.setTitle(this.mContext.getText(R.string.storage_detail_images));
        holder.setSummary(Formatter.formatFileSize(this.mContext, this.mFilesSize));
    }

    public void onClick(Fragment fragment) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setFlags(524288);
        intent.setType(IMAGE_MIME_TYPE);
        intent.putExtra("android.intent.extra.FROM_STORAGE", true);
        Utils.launchIntent(fragment, intent);
    }
}
