package com.android.settings.applications.defaultapps;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultEmergencyPicker extends DefaultAppPickerFragment {
    public int getMetricsCategory() {
        return 786;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_emergency_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        List<ResolveInfo> infos = this.mPm.getPackageManager().queryIntentActivities(DefaultEmergencyPreferenceController.QUERY_INTENT, 0);
        PackageInfo bestMatch = null;
        Context context = getContext();
        for (ResolveInfo info : infos) {
            try {
                PackageInfo packageInfo = this.mPm.getPackageManager().getPackageInfo(info.activityInfo.packageName, 0);
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                candidates.add(new DefaultAppInfo(context, this.mPm, appInfo));
                if (isSystemApp(appInfo) && (bestMatch == null || bestMatch.firstInstallTime > packageInfo.firstInstallTime)) {
                    bestMatch = packageInfo;
                }
            } catch (NameNotFoundException e) {
            }
            if (bestMatch != null && TextUtils.isEmpty(getDefaultKey())) {
                setDefaultKey(bestMatch.packageName);
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getConfirmationMessage(CandidateInfo info) {
        if (Utils.isPackageDirectBootAware(getContext(), info.getKey())) {
            return null;
        }
        return getContext().getString(R.string.direct_boot_unaware_dialog_message);
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return Secure.getString(getContext().getContentResolver(), "emergency_assistance_application");
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        ContentResolver contentResolver = getContext().getContentResolver();
        String previousValue = Secure.getString(contentResolver, "emergency_assistance_application");
        if (TextUtils.isEmpty(key) || TextUtils.equals(key, previousValue)) {
            return false;
        }
        Secure.putString(contentResolver, "emergency_assistance_application", key);
        return true;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info == null || (info.flags & 1) == 0) ? false : true;
    }
}
