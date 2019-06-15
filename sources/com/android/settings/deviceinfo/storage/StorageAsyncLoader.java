package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.applications.StorageStatsSource.AppStorageStats;
import com.android.settingslib.applications.StorageStatsSource.ExternalStorageStats;
import com.android.settingslib.utils.AsyncLoader;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StorageAsyncLoader extends AsyncLoader<SparseArray<AppsStorageResult>> {
    private static final String TAG = "StorageAsyncLoader";
    private PackageManagerWrapper mPackageManager;
    private ArraySet<String> mSeenPackages;
    private StorageStatsSource mStatsManager;
    private UserManager mUserManager;
    private String mUuid;

    public static class AppsStorageResult {
        public long cacheSize;
        public ExternalStorageStats externalStats;
        public long gamesSize;
        public long musicAppsSize;
        public long otherAppsSize;
        public long photosAppsSize;
        public long videoAppsSize;
    }

    public interface ResultHandler {
        void handleResult(SparseArray<AppsStorageResult> sparseArray);
    }

    public StorageAsyncLoader(Context context, UserManager userManager, String uuid, StorageStatsSource source, PackageManagerWrapper pm) {
        super(context);
        this.mUserManager = userManager;
        this.mUuid = uuid;
        this.mStatsManager = source;
        this.mPackageManager = pm;
    }

    public SparseArray<AppsStorageResult> loadInBackground() {
        return loadApps();
    }

    private SparseArray<AppsStorageResult> loadApps() {
        this.mSeenPackages = new ArraySet();
        SparseArray<AppsStorageResult> result = new SparseArray();
        List<UserInfo> infos = this.mUserManager.getUsers();
        Collections.sort(infos, new Comparator<UserInfo>() {
            public int compare(UserInfo userInfo, UserInfo otherUser) {
                return Integer.compare(userInfo.id, otherUser.id);
            }
        });
        int userCount = infos.size();
        for (int i = 0; i < userCount; i++) {
            UserInfo info = (UserInfo) infos.get(i);
            result.put(info.id, getStorageResultForUser(info.id));
        }
        return result;
    }

    private AppsStorageResult getStorageResultForUser(int userId) {
        UserHandle myUser;
        Log.d(TAG, "Loading apps");
        List<ApplicationInfo> applicationInfos = this.mPackageManager.getInstalledApplicationsAsUser(null, userId);
        AppsStorageResult result = new AppsStorageResult();
        UserHandle myUser2 = UserHandle.of(userId);
        int size = applicationInfos.size();
        int i = 0;
        while (i < size) {
            List<ApplicationInfo> applicationInfos2;
            int size2;
            ApplicationInfo app = (ApplicationInfo) applicationInfos.get(i);
            try {
                AppStorageStats stats = this.mStatsManager.getStatsForPackage(this.mUuid, app.packageName, myUser2);
                long dataSize = stats.getDataBytes();
                long cacheQuota = this.mStatsManager.getCacheQuotaBytes(this.mUuid, app.uid);
                long cacheBytes = stats.getCacheBytes();
                long blamedSize = dataSize;
                if (cacheQuota < cacheBytes) {
                    blamedSize = (blamedSize - cacheBytes) + cacheQuota;
                }
                applicationInfos2 = applicationInfos;
                myUser = myUser2;
                if (!this.mSeenPackages.contains(app.packageName)) {
                    blamedSize += stats.getCodeBytes();
                    this.mSeenPackages.add(app.packageName);
                }
                switch (app.category) {
                    case 0:
                        size2 = size;
                        result.gamesSize += blamedSize;
                        break;
                    case 1:
                        size2 = size;
                        result.musicAppsSize += blamedSize;
                        break;
                    case 2:
                        size2 = size;
                        result.videoAppsSize += blamedSize;
                        break;
                    case 3:
                        size2 = size;
                        result.photosAppsSize += blamedSize;
                        break;
                    default:
                        size2 = size;
                        if ((app.flags & 33554432) == 0) {
                            result.otherAppsSize += blamedSize;
                            break;
                        }
                        result.gamesSize += blamedSize;
                        break;
                }
            } catch (NameNotFoundException | IOException e) {
                applicationInfos2 = applicationInfos;
                myUser = myUser2;
                size2 = size;
                Log.w(TAG, "App unexpectedly not found", e);
            }
            i++;
            applicationInfos = applicationInfos2;
            myUser2 = myUser;
            size = size2;
        }
        myUser = myUser2;
        Log.d(TAG, "Loading external stats");
        try {
            result.externalStats = this.mStatsManager.getExternalStorageStats(this.mUuid, UserHandle.of(userId));
        } catch (IOException e2) {
            Log.w(TAG, e2);
        }
        Log.d(TAG, "Obtaining result completed");
        return result;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(SparseArray<AppsStorageResult> sparseArray) {
    }
}
