package com.android.settings.applications;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settings.Utils;
import java.util.List;

public class AppLaunchSettings extends AppInfoWithHeader implements OnClickListener, OnPreferenceChangeListener {
    private static final String KEY_APP_LINK_STATE = "app_link_state";
    private static final String KEY_CLEAR_DEFAULTS = "app_launch_clear_defaults";
    private static final String KEY_SUPPORTED_DOMAIN_URLS = "app_launch_supported_domain_urls";
    private static final String TAG = "AppLaunchSettings";
    private static final Intent sBrowserIntent = new Intent().setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").setData(Uri.parse("http:"));
    private AppDomainsPreference mAppDomainUrls;
    private DropDownPreference mAppLinkState;
    private ClearDefaultsPreference mClearDefaultsPreference;
    private boolean mHasDomainUrls;
    private boolean mIsBrowser;
    private PackageManager mPm;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.installed_app_launch_settings);
        this.mAppDomainUrls = (AppDomainsPreference) findPreference(KEY_SUPPORTED_DOMAIN_URLS);
        this.mClearDefaultsPreference = (ClearDefaultsPreference) findPreference(KEY_CLEAR_DEFAULTS);
        this.mAppLinkState = (DropDownPreference) findPreference(KEY_APP_LINK_STATE);
        this.mPm = getActivity().getPackageManager();
        this.mIsBrowser = isBrowserApp(this.mPackageName);
        this.mHasDomainUrls = (this.mAppEntry.info.privateFlags & 16) != 0;
        if (!this.mIsBrowser) {
            CharSequence[] entries = getEntries(this.mPackageName, this.mPm.getIntentFilterVerifications(this.mPackageName), this.mPm.getAllIntentFilters(this.mPackageName));
            this.mAppDomainUrls.setTitles(entries);
            this.mAppDomainUrls.setValues(new int[entries.length]);
        }
        buildStateDropDown();
    }

    private boolean isBrowserApp(String packageName) {
        sBrowserIntent.setPackage(packageName);
        List<ResolveInfo> list = this.mPm.queryIntentActivitiesAsUser(sBrowserIntent, 131072, UserHandle.myUserId());
        int count = list.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo info = (ResolveInfo) list.get(i);
            if (info.activityInfo != null && info.handleAllWebDataURI) {
                return true;
            }
        }
        return false;
    }

    private void buildStateDropDown() {
        if (this.mIsBrowser) {
            this.mAppLinkState.setShouldDisableView(true);
            this.mAppLinkState.setEnabled(false);
            this.mAppDomainUrls.setShouldDisableView(true);
            this.mAppDomainUrls.setEnabled(false);
            return;
        }
        this.mAppLinkState.setEntries(new CharSequence[]{getString(R.string.app_link_open_always), getString(R.string.app_link_open_ask), getString(R.string.app_link_open_never)});
        DropDownPreference dropDownPreference = this.mAppLinkState;
        r4 = new CharSequence[3];
        int i = 4;
        r4[1] = Integer.toString(4);
        r4[2] = Integer.toString(3);
        dropDownPreference.setEntryValues(r4);
        this.mAppLinkState.setEnabled(this.mHasDomainUrls);
        if (this.mHasDomainUrls) {
            int state = this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, UserHandle.myUserId());
            DropDownPreference dropDownPreference2 = this.mAppLinkState;
            if (state != 0) {
                i = state;
            }
            dropDownPreference2.setValue(Integer.toString(i));
            this.mAppLinkState.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return AppLaunchSettings.this.updateAppLinkState(Integer.parseInt((String) newValue));
                }
            });
        }
    }

    private boolean updateAppLinkState(int newState) {
        boolean z = false;
        if (this.mIsBrowser) {
            return false;
        }
        int userId = UserHandle.myUserId();
        if (this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, userId) == newState) {
            return false;
        }
        boolean success = this.mPm.updateIntentVerificationStatusAsUser(this.mPackageName, newState, userId);
        if (success) {
            if (newState == this.mPm.getIntentVerificationStatusAsUser(this.mPackageName, userId)) {
                z = true;
            }
            success = z;
        } else {
            Log.e(TAG, "Couldn't update intent verification status!");
        }
        return success;
    }

    private CharSequence[] getEntries(String packageName, List<IntentFilterVerificationInfo> list, List<IntentFilter> list2) {
        ArraySet<String> result = Utils.getHandledDomains(this.mPm, packageName);
        return (CharSequence[]) result.toArray(new CharSequence[result.size()]);
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        this.mClearDefaultsPreference.setPackageName(this.mPackageName);
        this.mClearDefaultsPreference.setAppEntry(this.mAppEntry);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public void onClick(View v) {
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    public int getMetricsCategory() {
        return 17;
    }
}
