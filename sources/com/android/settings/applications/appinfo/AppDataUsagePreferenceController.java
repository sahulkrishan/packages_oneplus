package com.android.settings.applications.appinfo;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.datausage.AppDataUsage;
import com.android.settings.datausage.DataUsageList;
import com.android.settings.datausage.DataUsageUtils;
import com.android.settingslib.AppItem;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;

public class AppDataUsagePreferenceController extends AppInfoPreferenceControllerBase implements LoaderCallbacks<ChartData>, LifecycleObserver, OnResume, OnPause {
    private ChartData mChartData;
    private INetworkStatsSession mStatsSession;

    public AppDataUsagePreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        return isBandwidthControlEnabled() ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            try {
                this.mStatsSession = Stub.asInterface(ServiceManager.getService("netstats")).openSession();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void updateState(Preference preference) {
        preference.setSummary(getDataSummary());
    }

    public void onResume() {
        if (this.mStatsSession != null) {
            int uid = this.mParent.getAppEntry().info.uid;
            AppItem app = new AppItem(uid);
            app.addUid(uid);
            LoaderManager loaderManager = this.mParent.getLoaderManager();
            AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
            loaderManager.restartLoader(2, ChartDataLoader.buildArgs(getTemplate(this.mContext), app), this);
        }
    }

    public void onPause() {
        LoaderManager loaderManager = this.mParent.getLoaderManager();
        AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
        loaderManager.destroyLoader(2);
    }

    public Loader<ChartData> onCreateLoader(int id, Bundle args) {
        return new ChartDataLoader(this.mContext, this.mStatsSession, args);
    }

    public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
        this.mChartData = data;
        updateState(this.mPreference);
    }

    public void onLoaderReset(Loader<ChartData> loader) {
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppDataUsage.class;
    }

    private CharSequence getDataSummary() {
        if (this.mChartData == null) {
            return this.mContext.getString(R.string.computing_size);
        }
        if (this.mChartData.detail.getTotalBytes() == 0) {
            return this.mContext.getString(R.string.no_data_usage);
        }
        return this.mContext.getString(R.string.data_summary_format, new Object[]{Formatter.formatFileSize(this.mContext, totalBytes), DateUtils.formatDateTime(this.mContext, this.mChartData.detail.getStart(), 65552)});
    }

    private static NetworkTemplate getTemplate(Context context) {
        if (DataUsageList.hasReadyMobileRadio(context)) {
            return NetworkTemplate.buildTemplateMobileWildcard();
        }
        if (DataUsageUtils.hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        }
        return NetworkTemplate.buildTemplateEthernet();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isBandwidthControlEnabled() {
        return Utils.isBandwidthControlEnabled();
    }
}
