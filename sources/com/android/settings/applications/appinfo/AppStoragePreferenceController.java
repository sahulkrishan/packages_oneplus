package com.android.settings.applications.appinfo;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStorageSettings;
import com.android.settings.applications.FetchPackageStorageAsyncLoader;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.applications.StorageStatsSource.AppStorageStats;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AppStoragePreferenceController extends AppInfoPreferenceControllerBase implements LoaderCallbacks<AppStorageStats>, LifecycleObserver, OnResume, OnPause {
    private AppStorageStats mLastResult;

    public AppStoragePreferenceController(Context context, String key) {
        super(context, key);
    }

    public void updateState(Preference preference) {
        AppEntry appEntry = this.mParent.getAppEntry();
        if (appEntry != null && appEntry.info != null) {
            preference.setSummary(getStorageSummary(this.mLastResult, (appEntry.info.flags & 262144) != 0));
        }
    }

    public void onResume() {
        LoaderManager loaderManager = this.mParent.getLoaderManager();
        AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
        loaderManager.restartLoader(3, Bundle.EMPTY, this);
    }

    public void onPause() {
        LoaderManager loaderManager = this.mParent.getLoaderManager();
        AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
        loaderManager.destroyLoader(3);
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppStorageSettings.class;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getStorageSummary(AppStorageStats stats, boolean isExternal) {
        if (stats == null) {
            return this.mContext.getText(R.string.computing_size);
        }
        int i;
        CharSequence storageType = this.mContext;
        if (isExternal) {
            i = R.string.storage_type_external;
        } else {
            i = R.string.storage_type_internal;
        }
        storageType = storageType.getString(i);
        return this.mContext.getString(R.string.storage_summary_format, new Object[]{Formatter.formatFileSize(this.mContext, stats.getTotalBytes()), storageType.toString().toLowerCase()});
    }

    public Loader<AppStorageStats> onCreateLoader(int id, Bundle args) {
        return new FetchPackageStorageAsyncLoader(this.mContext, new StorageStatsSource(this.mContext), this.mParent.getAppEntry().info, UserHandle.of(UserHandle.myUserId()));
    }

    public void onLoadFinished(Loader<AppStorageStats> loader, AppStorageStats result) {
        this.mLastResult = result;
        updateState(this.mPreference);
    }

    public void onLoaderReset(Loader<AppStorageStats> loader) {
    }
}
