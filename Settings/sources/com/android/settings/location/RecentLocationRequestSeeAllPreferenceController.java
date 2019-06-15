package com.android.settings.location;

import android.content.Context;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.location.RecentLocationApps;
import com.android.settingslib.location.RecentLocationApps.Request;
import com.oneplus.settings.ui.OPButtonPreference;
import java.util.List;
import java.util.UUID;

public class RecentLocationRequestSeeAllPreferenceController extends LocationBasePreferenceController {
    private static final String KEY_ALL_RECENT_LOCATION_REQUESTS = "all_recent_location_requests";
    private final RecentLocationRequestSeeAllFragment mFragment;
    private RecentLocationApps mRecentLocationApps;

    public RecentLocationRequestSeeAllPreferenceController(Context context, Lifecycle lifecycle, RecentLocationRequestSeeAllFragment fragment) {
        this(context, lifecycle, fragment, new RecentLocationApps(context));
    }

    @VisibleForTesting
    RecentLocationRequestSeeAllPreferenceController(Context context, Lifecycle lifecycle, RecentLocationRequestSeeAllFragment fragment, RecentLocationApps recentLocationApps) {
        super(context, lifecycle);
        this.mFragment = fragment;
        this.mRecentLocationApps = recentLocationApps;
    }

    public String getPreferenceKey() {
        return KEY_ALL_RECENT_LOCATION_REQUESTS;
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
        this.mCategoryRecentLocationRequests.setEnabled(this.mLocationEnabler.isEnabled(mode));
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mCategoryRecentLocationRequests = (PreferenceCategory) screen.findPreference(KEY_ALL_RECENT_LOCATION_REQUESTS);
    }

    public void updateState(Preference preference) {
        this.mCategoryRecentLocationRequests.removeAll();
        List<Request> requests = this.mRecentLocationApps.getAppListSorted();
        List<String> currentUsingGpsPkgs = getCurrentUsingGpsListForUid();
        for (Request request : requests) {
            this.mCategoryRecentLocationRequests.addPreference(createAppPreference(preference.getContext(), request, currentUsingGpsPkgs));
        }
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
                        RecentLocationRequestSeeAllPreferenceController.this.forceStopPackage(appEntry, pref);
                        pref.setButtonEnable(false);
                    }
                });
            }
        }
        return pref;
    }
}
