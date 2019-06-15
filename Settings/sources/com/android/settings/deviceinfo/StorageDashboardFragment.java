package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;
import android.view.View;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.deviceinfo.storage.AutomaticStorageManagementSwitchPreferenceController;
import com.android.settings.deviceinfo.storage.CachedStorageValuesHelper;
import com.android.settings.deviceinfo.storage.SecondaryUserController;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.AppsStorageResult;
import com.android.settings.deviceinfo.storage.StorageAsyncLoader.ResultHandler;
import com.android.settings.deviceinfo.storage.StorageItemPreferenceController;
import com.android.settings.deviceinfo.storage.StorageSummaryDonutPreferenceController;
import com.android.settings.deviceinfo.storage.UserIconLoader;
import com.android.settings.deviceinfo.storage.VolumeSizesLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageDashboardFragment extends DashboardFragment implements LoaderCallbacks<SparseArray<AppsStorageResult>> {
    private static final int ICON_JOB_ID = 1;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.storage_dashboard_fragment;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            StorageManager sm = (StorageManager) context.getSystemService(StorageManager.class);
            UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
            List<AbstractPreferenceController> controllers = new ArrayList();
            controllers.add(new StorageSummaryDonutPreferenceController(context));
            controllers.add(new StorageItemPreferenceController(context, null, null, new StorageManagerVolumeProvider(sm)));
            controllers.addAll(SecondaryUserController.getSecondaryUserControllers(context, userManager));
            return controllers;
        }
    };
    private static final int STORAGE_JOB_ID = 0;
    private static final String TAG = "StorageDashboardFrag";
    private static final int VOLUME_SIZE_JOB_ID = 2;
    private SparseArray<AppsStorageResult> mAppsResult;
    private CachedStorageValuesHelper mCachedStorageValuesHelper;
    private PrivateVolumeOptionMenuController mOptionMenuController;
    private StorageItemPreferenceController mPreferenceController;
    private List<AbstractPreferenceController> mSecondaryUsers;
    private PrivateStorageInfo mStorageInfo;
    private StorageSummaryDonutPreferenceController mSummaryController;
    private VolumeInfo mVolume;

    public final class IconLoaderCallbacks implements LoaderCallbacks<SparseArray<Drawable>> {
        public Loader<SparseArray<Drawable>> onCreateLoader(int id, Bundle args) {
            return new UserIconLoader(StorageDashboardFragment.this.getContext(), new -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$yGwysNy4Bq4_2nwwvU2QePhZgvU(this));
        }

        public void onLoadFinished(Loader<SparseArray<Drawable>> loader, SparseArray<Drawable> data) {
            StorageDashboardFragment.this.mSecondaryUsers.stream().filter(-$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$7UIHa462aQ5cO1d2zsPI99b5Y1Y.INSTANCE).forEach(new -$$Lambda$StorageDashboardFragment$IconLoaderCallbacks$Jn0eBlqBHbuO-2COJ4jEmaXSJJc(data));
        }

        public void onLoaderReset(Loader<SparseArray<Drawable>> loader) {
        }
    }

    public final class VolumeSizeCallbacks implements LoaderCallbacks<PrivateStorageInfo> {
        public Loader<PrivateStorageInfo> onCreateLoader(int id, Bundle args) {
            Context context = StorageDashboardFragment.this.getContext();
            return new VolumeSizesLoader(context, new StorageManagerVolumeProvider((StorageManager) context.getSystemService(StorageManager.class)), (StorageStatsManager) context.getSystemService(StorageStatsManager.class), StorageDashboardFragment.this.mVolume);
        }

        public void onLoaderReset(Loader<PrivateStorageInfo> loader) {
        }

        public void onLoadFinished(Loader<PrivateStorageInfo> loader, PrivateStorageInfo privateStorageInfo) {
            if (privateStorageInfo == null) {
                StorageDashboardFragment.this.getActivity().finish();
                return;
            }
            StorageDashboardFragment.this.mStorageInfo = privateStorageInfo;
            StorageDashboardFragment.this.maybeCacheFreshValues();
            StorageDashboardFragment.this.onReceivedSizes();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Activity activity = getActivity();
        this.mVolume = Utils.maybeInitializeVolume((StorageManager) activity.getSystemService(StorageManager.class), getArguments());
        if (this.mVolume == null) {
            activity.finish();
        } else {
            initializeOptionsMenu(activity);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initializeOptionsMenu(Activity activity) {
        this.mOptionMenuController = new PrivateVolumeOptionMenuController(activity, this.mVolume, new PackageManagerWrapper(activity.getPackageManager()));
        getLifecycle().addObserver(this.mOptionMenuController);
        setHasOptionsMenu(true);
        activity.invalidateOptionsMenu();
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        initializeCacheProvider();
        maybeSetLoading(isQuotaSupported());
        Activity activity = getActivity();
        EntityHeaderController.newInstance(activity, this, null).setRecyclerView(getListView(), getLifecycle()).styleActionBar(activity);
    }

    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, Bundle.EMPTY, this);
        getLoaderManager().restartLoader(2, Bundle.EMPTY, new VolumeSizeCallbacks());
        getLoaderManager().initLoader(1, Bundle.EMPTY, new IconLoaderCallbacks());
    }

    public int getHelpResource() {
        return R.string.help_url_storage_dashboard;
    }

    private void onReceivedSizes() {
        if (this.mStorageInfo != null) {
            long privateUsedBytes = this.mStorageInfo.totalBytes - this.mStorageInfo.freeBytes;
            this.mSummaryController.updateBytes(privateUsedBytes, this.mStorageInfo.totalBytes);
            this.mPreferenceController.setVolume(this.mVolume);
            this.mPreferenceController.setUsedSize(privateUsedBytes);
            this.mPreferenceController.setTotalSize(this.mStorageInfo.totalBytes);
            int size = this.mSecondaryUsers.size();
            for (int i = 0; i < size; i++) {
                AbstractPreferenceController controller = (AbstractPreferenceController) this.mSecondaryUsers.get(i);
                if (controller instanceof SecondaryUserController) {
                    ((SecondaryUserController) controller).setTotalSize(this.mStorageInfo.totalBytes);
                }
            }
        }
        if (this.mAppsResult != null) {
            this.mPreferenceController.onLoadFinished(this.mAppsResult, UserHandle.myUserId());
            updateSecondaryUserControllers(this.mSecondaryUsers, this.mAppsResult);
            if (getView().findViewById(R.id.loading_container).getVisibility() == 0) {
                setLoading(false, true);
            }
        }
    }

    public int getMetricsCategory() {
        return 745;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.storage_dashboard_fragment;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mSummaryController = new StorageSummaryDonutPreferenceController(context);
        controllers.add(this.mSummaryController);
        this.mPreferenceController = new StorageItemPreferenceController(context, this, this.mVolume, new StorageManagerVolumeProvider((StorageManager) context.getSystemService(StorageManager.class)));
        controllers.add(this.mPreferenceController);
        this.mSecondaryUsers = SecondaryUserController.getSecondaryUserControllers(context, (UserManager) context.getSystemService(UserManager.class));
        controllers.addAll(this.mSecondaryUsers);
        AutomaticStorageManagementSwitchPreferenceController asmController = new AutomaticStorageManagementSwitchPreferenceController(context, this.mMetricsFeatureProvider, getFragmentManager());
        getLifecycle().addObserver(asmController);
        controllers.add(asmController);
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public void setVolume(VolumeInfo info) {
        this.mVolume = info;
    }

    private void updateSecondaryUserControllers(List<AbstractPreferenceController> controllers, SparseArray<AppsStorageResult> stats) {
        int size = controllers.size();
        for (int i = 0; i < size; i++) {
            AbstractPreferenceController controller = (AbstractPreferenceController) controllers.get(i);
            if (controller instanceof ResultHandler) {
                ((ResultHandler) controller).handleResult(stats);
            }
        }
    }

    public Loader<SparseArray<AppsStorageResult>> onCreateLoader(int id, Bundle args) {
        Context context = getContext();
        return new StorageAsyncLoader(context, (UserManager) context.getSystemService(UserManager.class), this.mVolume.fsUuid, new StorageStatsSource(context), new PackageManagerWrapper(context.getPackageManager()));
    }

    public void onLoadFinished(Loader<SparseArray<AppsStorageResult>> loader, SparseArray<AppsStorageResult> data) {
        this.mAppsResult = data;
        maybeCacheFreshValues();
        onReceivedSizes();
    }

    public void onLoaderReset(Loader<SparseArray<AppsStorageResult>> loader) {
    }

    @VisibleForTesting
    public void setCachedStorageValuesHelper(CachedStorageValuesHelper helper) {
        this.mCachedStorageValuesHelper = helper;
    }

    @VisibleForTesting
    public PrivateStorageInfo getPrivateStorageInfo() {
        return this.mStorageInfo;
    }

    @VisibleForTesting
    public void setPrivateStorageInfo(PrivateStorageInfo info) {
        this.mStorageInfo = info;
    }

    @VisibleForTesting
    public SparseArray<AppsStorageResult> getAppsStorageResult() {
        return this.mAppsResult;
    }

    @VisibleForTesting
    public void setAppsStorageResult(SparseArray<AppsStorageResult> info) {
        this.mAppsResult = info;
    }

    @VisibleForTesting
    public void initializeCachedValues() {
        PrivateStorageInfo info = this.mCachedStorageValuesHelper.getCachedPrivateStorageInfo();
        SparseArray<AppsStorageResult> loaderResult = this.mCachedStorageValuesHelper.getCachedAppsStorageResult();
        if (info != null && loaderResult != null) {
            this.mStorageInfo = info;
            this.mAppsResult = loaderResult;
        }
    }

    @VisibleForTesting
    public void maybeSetLoading(boolean isQuotaSupported) {
        if ((isQuotaSupported && (this.mStorageInfo == null || this.mAppsResult == null)) || (!isQuotaSupported && this.mStorageInfo == null)) {
            setLoading(true, false);
        }
    }

    private void initializeCacheProvider() {
        this.mCachedStorageValuesHelper = new CachedStorageValuesHelper(getContext(), UserHandle.myUserId());
        initializeCachedValues();
        onReceivedSizes();
    }

    private void maybeCacheFreshValues() {
        if (this.mStorageInfo != null && this.mAppsResult != null) {
            this.mCachedStorageValuesHelper.cacheResult(this.mStorageInfo, (AppsStorageResult) this.mAppsResult.get(UserHandle.myUserId()));
        }
    }

    private boolean isQuotaSupported() {
        return ((StorageStatsManager) getActivity().getSystemService(StorageStatsManager.class)).isQuotaSupported(this.mVolume.fsUuid);
    }
}
