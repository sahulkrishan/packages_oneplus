package com.android.settings.applications.appinfo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment.Callback;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;

public class AppHeaderViewPreferenceController extends BasePreferenceController implements Callback, LifecycleObserver, OnStart {
    private static final String KEY_HEADER = "header_view";
    private EntityHeaderController mEntityHeaderController;
    private LayoutPreference mHeader;
    private final Lifecycle mLifecycle;
    private final String mPackageName;
    private final AppInfoDashboardFragment mParent;

    public AppHeaderViewPreferenceController(Context context, AppInfoDashboardFragment parent, String packageName, Lifecycle lifecycle) {
        super(context, KEY_HEADER);
        this.mParent = parent;
        this.mPackageName = packageName;
        this.mLifecycle = lifecycle;
        if (this.mLifecycle != null) {
            this.mLifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mHeader = (LayoutPreference) screen.findPreference(KEY_HEADER);
        this.mEntityHeaderController = EntityHeaderController.newInstance(this.mParent.getActivity(), this.mParent, this.mHeader.findViewById(R.id.entity_header)).setPackageName(this.mPackageName).setButtonActions(0, 0).bindHeaderButtons();
    }

    public void onStart() {
        this.mEntityHeaderController.setRecyclerView(this.mParent.getListView(), this.mLifecycle).styleActionBar(this.mParent.getActivity());
    }

    public void refreshUi() {
        setAppLabelAndIcon(this.mParent.getPackageInfo(), this.mParent.getAppEntry());
    }

    private void setAppLabelAndIcon(PackageInfo pkgInfo, AppEntry appEntry) {
        Activity activity = this.mParent.getActivity();
        boolean isInstantApp = AppUtils.isInstant(pkgInfo.applicationInfo);
        this.mEntityHeaderController.setLabel(appEntry).setIcon(appEntry).setSummary(isInstantApp ? null : this.mContext.getString(Utils.getInstallationStatus(appEntry.info))).setIsInstantApp(isInstantApp).done(activity, false);
    }
}
