package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.preference.Preference;
import com.android.settings.Utils;
import com.android.settings.applications.AppDomainsPreference;
import com.android.settingslib.applications.AppUtils;
import java.util.Set;

public class InstantAppDomainsPreferenceController extends AppInfoPreferenceControllerBase {
    private PackageManager mPackageManager = this.mContext.getPackageManager();

    public InstantAppDomainsPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        int i = 0;
        int isAvailabity = 0;
        try {
            if (!AppUtils.isInstant(this.mParent.getPackageInfo().applicationInfo)) {
                i = 3;
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return isAvailabity;
        }
    }

    public void updateState(Preference preference) {
        AppDomainsPreference instantAppDomainsPreference = (AppDomainsPreference) preference;
        Set<String> handledDomainSet = Utils.getHandledDomains(this.mPackageManager, this.mParent.getPackageInfo().packageName);
        String[] handledDomains = (String[]) handledDomainSet.toArray(new String[handledDomainSet.size()]);
        instantAppDomainsPreference.setTitles(handledDomains);
        instantAppDomainsPreference.setValues(new int[handledDomains.length]);
    }
}
