package com.android.settings.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.widget.FooterPreference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocationFooterPreferenceController extends LocationBasePreferenceController implements LifecycleObserver, OnPause {
    private static final Intent INJECT_INTENT = new Intent("com.android.settings.location.DISPLAYED_FOOTER");
    private static final String KEY_LOCATION_FOOTER = "location_footer";
    private static final String TAG = "LocationFooter";
    private final Context mContext;
    private Collection<ComponentName> mFooterInjectors = new ArrayList();
    private final PackageManager mPackageManager = this.mContext.getPackageManager();

    private static class FooterData {
        final ApplicationInfo applicationInfo;
        final ComponentName componentName;
        final int footerStringRes;

        FooterData(int footerRes, ApplicationInfo appInfo, ComponentName componentName) {
            this.footerStringRes = footerRes;
            this.applicationInfo = appInfo;
            this.componentName = componentName;
        }
    }

    public LocationFooterPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mContext = context;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public String getPreferenceKey() {
        return KEY_LOCATION_FOOTER;
    }

    public void updateState(Preference preference) {
        PreferenceCategory category = (PreferenceCategory) preference;
        category.removeAll();
        this.mFooterInjectors.clear();
        for (FooterData data : getFooterData()) {
            FooterPreference footerPreference = new FooterPreference(preference.getContext());
            try {
                footerPreference.setTitle((CharSequence) this.mPackageManager.getResourcesForApplication(data.applicationInfo).getString(data.footerStringRes));
                category.addPreference(footerPreference);
                sendBroadcastFooterDisplayed(data.componentName);
                this.mFooterInjectors.add(data.componentName);
            } catch (NameNotFoundException e) {
                if (Log.isLoggable(TAG, 5)) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Resources not found for application ");
                    stringBuilder.append(data.applicationInfo.packageName);
                    Log.w(str, stringBuilder.toString());
                }
            }
        }
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
    }

    public boolean isAvailable() {
        return getFooterData().isEmpty() ^ 1;
    }

    public void onPause() {
        for (ComponentName componentName : this.mFooterInjectors) {
            Intent intent = new Intent("com.android.settings.location.REMOVED_FOOTER");
            intent.setComponent(componentName);
            this.mContext.sendBroadcast(intent);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void sendBroadcastFooterDisplayed(ComponentName componentName) {
        Intent intent = new Intent("com.android.settings.location.DISPLAYED_FOOTER");
        intent.setComponent(componentName);
        this.mContext.sendBroadcast(intent);
    }

    private Collection<FooterData> getFooterData() {
        List<ResolveInfo> resolveInfos = this.mPackageManager.queryBroadcastReceivers(INJECT_INTENT, 128);
        if (resolveInfos == null) {
            if (Log.isLoggable(TAG, 6)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to resolve intent ");
                stringBuilder.append(INJECT_INTENT);
                Log.e(str, stringBuilder.toString());
                return Collections.emptyList();
            }
        } else if (Log.isLoggable(TAG, 3)) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Found broadcast receivers: ");
            stringBuilder2.append(resolveInfos);
            Log.d(str2, stringBuilder2.toString());
        }
        Collection<FooterData> footerDataList = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            ApplicationInfo appInfo = activityInfo.applicationInfo;
            String str3;
            StringBuilder stringBuilder3;
            if ((appInfo.flags & 1) == 0 && Log.isLoggable(TAG, 5)) {
                str3 = TAG;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Ignoring attempt to inject footer from app not in system image: ");
                stringBuilder3.append(resolveInfo);
                Log.w(str3, stringBuilder3.toString());
            } else if (activityInfo.metaData == null && Log.isLoggable(TAG, 3)) {
                str3 = TAG;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("No METADATA in broadcast receiver ");
                stringBuilder3.append(activityInfo.name);
                Log.d(str3, stringBuilder3.toString());
            } else {
                int footerTextRes = activityInfo.metaData.getInt("com.android.settings.location.FOOTER_STRING");
                if (footerTextRes != 0) {
                    footerDataList.add(new FooterData(footerTextRes, appInfo, new ComponentName(activityInfo.packageName, activityInfo.name)));
                } else if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "No mapping of integer exists for com.android.settings.location.FOOTER_STRING");
                }
            }
        }
        return footerDataList;
    }
}
