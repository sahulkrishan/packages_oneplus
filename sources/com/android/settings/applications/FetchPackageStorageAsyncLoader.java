package com.android.settings.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.applications.StorageStatsSource.AppStorageStats;
import com.android.settingslib.utils.AsyncLoader;
import java.io.IOException;

public class FetchPackageStorageAsyncLoader extends AsyncLoader<AppStorageStats> {
    private static final String TAG = "FetchPackageStorage";
    private final ApplicationInfo mInfo;
    private final StorageStatsSource mSource;
    private final UserHandle mUser;

    public FetchPackageStorageAsyncLoader(Context context, StorageStatsSource source, ApplicationInfo info, UserHandle user) {
        super(context);
        this.mSource = (StorageStatsSource) Preconditions.checkNotNull(source);
        this.mInfo = info;
        this.mUser = user;
    }

    public AppStorageStats loadInBackground() {
        try {
            return this.mSource.getStatsForPackage(this.mInfo.volumeUuid, this.mInfo.packageName, this.mUser);
        } catch (NameNotFoundException | IOException e) {
            Log.w(TAG, "Package may have been removed during query, failing gracefully", e);
            return null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(AppStorageStats result) {
    }
}
