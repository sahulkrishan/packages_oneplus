package com.android.settings.deviceinfo;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settings.deviceinfo.storage.StorageItemPreferenceController;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public class StorageProfileFragment extends DashboardFragment implements LoaderCallbacks<SparseArray<AppsStorageResult>> {
    private static final int APPS_JOB_ID = 0;
    private static final String TAG = "StorageProfileFragment";
    public static final String USER_ID_EXTRA = "userId";
    private StorageItemPreferenceController mPreferenceController;
    private int mUserId;
    private VolumeInfo mVolume;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        this.mVolume = Utils.maybeInitializeVolume((StorageManager) getActivity().getSystemService(StorageManager.class), args);
        if (this.mVolume == null) {
            getActivity().finish();
            return;
        }
        this.mPreferenceController.setVolume(this.mVolume);
        this.mUserId = args.getInt(USER_ID_EXTRA, UserHandle.myUserId());
        this.mPreferenceController.setUserId(UserHandle.of(this.mUserId));
    }

    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, Bundle.EMPTY, this);
    }

    public int getMetricsCategory() {
        return 845;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.storage_profile_fragment;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        Context context2 = context;
        this.mPreferenceController = new StorageItemPreferenceController(context2, this, this.mVolume, new StorageManagerVolumeProvider((StorageManager) context.getSystemService(StorageManager.class)), true);
        controllers.add(this.mPreferenceController);
        return controllers;
    }

    public Loader<SparseArray<AppsStorageResult>> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        return new StorageAsyncLoader(context, (UserManager) context.getSystemService(UserManager.class), this.mVolume.fsUuid, new StorageStatsSource(context), new PackageManagerWrapper(context.getPackageManager()));
    }

    public void onLoadFinished(Loader<SparseArray<AppsStorageResult>> loader, SparseArray<AppsStorageResult> result) {
        this.mPreferenceController.onLoadFinished(result, this.mUserId);
    }

    public void onLoaderReset(Loader<SparseArray<AppsStorageResult>> loader) {
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreferenceController(StorageItemPreferenceController controller) {
        this.mPreferenceController = controller;
    }
}
