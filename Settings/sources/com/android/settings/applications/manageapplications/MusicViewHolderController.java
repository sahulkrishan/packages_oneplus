package com.android.settings.applications.manageapplications;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.support.annotation.WorkerThread;
import android.text.format.Formatter;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.applications.StorageStatsSource;
import java.io.IOException;

public class MusicViewHolderController implements FileViewHolderController {
    private static final String AUTHORITY_MEDIA = "com.android.providers.media.documents";
    private static final String TAG = "MusicViewHolderCtrl";
    private Context mContext;
    private long mMusicSize;
    private StorageStatsSource mSource;
    private UserHandle mUser;
    private String mVolumeUuid;

    public MusicViewHolderController(Context context, StorageStatsSource source, String volumeUuid, UserHandle user) {
        this.mContext = context;
        this.mSource = source;
        this.mVolumeUuid = volumeUuid;
        this.mUser = user;
    }

    @WorkerThread
    public void queryStats() {
        try {
            this.mMusicSize = this.mSource.getExternalStorageStats(this.mVolumeUuid, this.mUser).audioBytes;
        } catch (IOException e) {
            this.mMusicSize = 0;
            Log.w(TAG, e);
        }
    }

    public boolean shouldShow() {
        return true;
    }

    public void setupView(ApplicationViewHolder holder) {
        holder.setIcon((int) R.drawable.ic_headset_24dp);
        holder.setTitle(this.mContext.getText(R.string.audio_files_title));
        holder.setSummary(Formatter.formatFileSize(this.mContext, this.mMusicSize));
    }

    public void onClick(Fragment fragment) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(DocumentsContract.buildRootUri(AUTHORITY_MEDIA, "audio_root"), "vnd.android.document/root");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra("android.intent.extra.USER_ID", this.mUser);
        Utils.launchIntent(fragment, intent);
    }
}
