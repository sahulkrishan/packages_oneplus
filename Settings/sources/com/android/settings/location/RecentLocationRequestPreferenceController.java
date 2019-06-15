package com.android.settings.location;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.location.RecentLocationApps;
import com.android.settingslib.location.RecentLocationApps.Request;
import com.oneplus.settings.ui.OPButtonPreference;
import java.util.List;
import java.util.UUID;

public class RecentLocationRequestPreferenceController extends LocationBasePreferenceController {
    private static final String KEY_RECENT_LOCATION_REQUESTS = "recent_location_requests";
    @VisibleForTesting
    static final String KEY_SEE_ALL_BUTTON = "recent_location_requests_see_all_button";
    private final LocationSettings mFragment;
    private final RecentLocationApps mRecentLocationApps;
    private Preference mSeeAllButton;

    static class PackageEntryClickedListener implements OnPreferenceClickListener {
        private final DashboardFragment mFragment;
        private final String mPackage;
        private final UserHandle mUserHandle;

        public PackageEntryClickedListener(DashboardFragment fragment, String packageName, UserHandle userHandle) {
            this.mFragment = fragment;
            this.mPackage = packageName;
            this.mUserHandle = userHandle;
        }

        public boolean onPreferenceClick(Preference preference) {
            Bundle args = new Bundle();
            args.putString("package", this.mPackage);
            new SubSettingLauncher(this.mFragment.getContext()).setDestination(AppInfoDashboardFragment.class.getName()).setArguments(args).setTitle((int) R.string.application_info_label).setUserHandle(this.mUserHandle).setSourceMetricsCategory(this.mFragment.getMetricsCategory()).launch();
            return true;
        }
    }

    public RecentLocationRequestPreferenceController(Context context, LocationSettings fragment, Lifecycle lifecycle) {
        this(context, fragment, lifecycle, new RecentLocationApps(context));
    }

    @VisibleForTesting
    RecentLocationRequestPreferenceController(Context context, LocationSettings fragment, Lifecycle lifecycle, RecentLocationApps recentApps) {
        super(context, lifecycle);
        this.mFragment = fragment;
        this.mRecentLocationApps = recentApps;
    }

    public String getPreferenceKey() {
        return KEY_RECENT_LOCATION_REQUESTS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mCategoryRecentLocationRequests = (PreferenceCategory) screen.findPreference(KEY_RECENT_LOCATION_REQUESTS);
        this.mSeeAllButton = screen.findPreference(KEY_SEE_ALL_BUTTON);
    }

    public void updateState(Preference preference) {
        this.mCategoryRecentLocationRequests.removeAll();
        int i = 0;
        this.mSeeAllButton.setVisible(false);
        Context prefContext = preference == null ? this.mContext : preference.getContext();
        List<Request> recentLocationRequests = this.mRecentLocationApps.getAppListSorted();
        List<String> currentUsingGpsPkgs = getCurrentUsingGpsListForUid();
        if (recentLocationRequests.size() > 3) {
            while (i < 3) {
                this.mCategoryRecentLocationRequests.addPreference(createAppPreference(prefContext, (Request) recentLocationRequests.get(i), currentUsingGpsPkgs));
                i++;
            }
            this.mSeeAllButton.setVisible(true);
        } else if (recentLocationRequests.size() > 0) {
            for (Request request : recentLocationRequests) {
                this.mCategoryRecentLocationRequests.addPreference(createAppPreference(prefContext, request, currentUsingGpsPkgs));
            }
        } else {
            Preference banner = createAppPreference(prefContext);
            banner.setTitle((int) R.string.location_no_recent_apps);
            banner.setSelectable(false);
            this.mCategoryRecentLocationRequests.addPreference(banner);
        }
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
        this.mCategoryRecentLocationRequests.setEnabled(this.mLocationEnabler.isEnabled(mode));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AppPreference createAppPreference(Context prefContext) {
        return new AppPreference(prefContext);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Preference createAppPreference(Context prefContext, Request request, List<String> currentUsingGpsPkgs) {
        final OPButtonPreference pref = new OPButtonPreference(prefContext);
        pref.setKey(UUID.randomUUID().toString().replace("-", ""));
        pref.setIcon(request.icon);
        pref.setTitle(request.label);
        pref.setOnPreferenceClickListener(new PackageEntryClickedListener(this.mFragment, request.packageName, request.userHandle));
        if (currentUsingGpsPkgs == null || !currentUsingGpsPkgs.contains(String.valueOf(request.uid))) {
            pref.setButtonVisible(false);
        } else {
            final AppEntry appEntry = this.mState.getEntry(request.packageName, request.userHandle.getIdentifier());
            boolean mAppsControlDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(prefContext, "no_control_apps", UserHandle.myUserId());
            boolean packageHasActiveAdmins = this.mDpm.packageHasActiveAdmins(request.packageName);
            if (packageHasActiveAdmins || mAppsControlDisallowedBySystem) {
                String str = PreferenceControllerMixin.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("packageHasActiveAdmins:");
                stringBuilder.append(packageHasActiveAdmins);
                stringBuilder.append(", mAppsControlDisallowedBySystem:");
                stringBuilder.append(mAppsControlDisallowedBySystem);
                Log.d(str, stringBuilder.toString());
                pref.setButtonEnable(false);
                pref.setButtonVisible(false);
                pref.setSummary(request.contentDescription);
            } else {
                pref.setSummary((int) R.string.oneplus_gps_using);
                pref.setButtonVisible(true);
                pref.setButtonEnable(true);
                pref.setButtonString(prefContext.getString(R.string.oneplus_stop_run));
                pref.setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        RecentLocationRequestPreferenceController.this.forceStopPackage(appEntry, pref);
                        pref.setButtonEnable(false);
                    }
                });
            }
        }
        return pref;
    }
}
