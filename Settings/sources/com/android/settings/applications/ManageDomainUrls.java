package com.android.settings.applications;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.ArraySet;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;

public class ManageDomainUrls extends SettingsPreferenceFragment implements Callbacks, OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final int INSTALLED_APP_DETAILS = 1;
    private ApplicationsState mApplicationsState;
    private PreferenceGroup mDomainAppList;
    private Preference mInstantAppAccountPreference;
    private Session mSession;
    private SwitchPreference mWebAction;

    @VisibleForTesting
    static class DomainAppPreference extends AppPreference {
        private final ApplicationsState mApplicationsState;
        private final AppEntry mEntry;
        private final PackageManager mPm;

        public DomainAppPreference(Context context, ApplicationsState applicationsState, AppEntry entry) {
            super(context);
            this.mApplicationsState = applicationsState;
            this.mPm = context.getPackageManager();
            this.mEntry = entry;
            this.mEntry.ensureLabel(getContext());
            setState();
            if (this.mEntry.icon != null) {
                setIcon(this.mEntry.icon);
            }
        }

        private void setState() {
            setTitle((CharSequence) this.mEntry.label);
            setSummary(getDomainsSummary(this.mEntry.info.packageName));
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (this.mEntry.icon == null) {
                holder.itemView.post(new Runnable() {
                    public void run() {
                        DomainAppPreference.this.mApplicationsState.ensureIcon(DomainAppPreference.this.mEntry);
                        DomainAppPreference.this.setIcon(DomainAppPreference.this.mEntry.icon);
                    }
                });
            }
            super.onBindViewHolder(holder);
        }

        private CharSequence getDomainsSummary(String packageName) {
            if (this.mPm.getIntentVerificationStatusAsUser(packageName, UserHandle.myUserId()) == 3) {
                return getContext().getString(R.string.domain_urls_summary_none);
            }
            ArraySet<String> result = Utils.getHandledDomains(this.mPm, packageName);
            if (result.size() == 0) {
                return getContext().getString(R.string.domain_urls_summary_none);
            }
            if (result.size() == 1) {
                return getContext().getString(R.string.domain_urls_summary_one, new Object[]{result.valueAt(0)});
            }
            return getContext().getString(R.string.domain_urls_summary_some, new Object[]{result.valueAt(0)});
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setAnimationAllowed(true);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        setHasOptionsMenu(true);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.manage_domain_url_settings;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
    }

    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        if (getContext() != null) {
            boolean z = true;
            if (Global.getInt(getContext().getContentResolver(), "enable_ephemeral_feature", 1) == 0) {
                this.mDomainAppList = getPreferenceScreen();
            } else {
                PreferenceGroup preferenceScreen = getPreferenceScreen();
                if (preferenceScreen.getPreferenceCount() == 0) {
                    PreferenceCategory webActionCategory = new PreferenceCategory(getPrefContext());
                    webActionCategory.setTitle((int) R.string.web_action_section_title);
                    preferenceScreen.addPreference(webActionCategory);
                    this.mWebAction = new SwitchPreference(getPrefContext());
                    this.mWebAction.setTitle((int) R.string.web_action_enable_title);
                    this.mWebAction.setSummary((int) R.string.web_action_enable_summary);
                    SwitchPreference switchPreference = this.mWebAction;
                    if (Secure.getInt(getContentResolver(), "instant_apps_enabled", 1) == 0) {
                        z = false;
                    }
                    switchPreference.setChecked(z);
                    this.mWebAction.setOnPreferenceChangeListener(this);
                    webActionCategory.addPreference(this.mWebAction);
                    ComponentName instantAppSettingsComponent = getActivity().getPackageManager().getInstantAppResolverSettingsComponent();
                    Intent instantAppSettingsIntent = null;
                    if (instantAppSettingsComponent != null) {
                        instantAppSettingsIntent = new Intent().setComponent(instantAppSettingsComponent);
                    }
                    if (instantAppSettingsIntent != null) {
                        Intent launchIntent = instantAppSettingsIntent;
                        this.mInstantAppAccountPreference = new Preference(getPrefContext());
                        this.mInstantAppAccountPreference.setTitle((int) R.string.instant_apps_settings);
                        this.mInstantAppAccountPreference.setOnPreferenceClickListener(new -$$Lambda$ManageDomainUrls$agHbI5vf9m7UaPnJCYH2ithkZhk(this, launchIntent));
                        webActionCategory.addPreference(this.mInstantAppAccountPreference);
                    }
                    if (!(OPUtils.isAppExist(getActivity(), OPConstants.PACKAGENAME_GMS) && OPUtils.isApplicationEnabled(getActivity(), OPConstants.PACKAGENAME_GMS))) {
                        webActionCategory.setVisible(false);
                    }
                    this.mDomainAppList = new PreferenceCategory(getPrefContext());
                    this.mDomainAppList.setTitle((int) R.string.domain_url_section_title);
                    preferenceScreen.addPreference(this.mDomainAppList);
                }
            }
            rebuildAppList(this.mDomainAppList, apps);
        }
    }

    public static /* synthetic */ boolean lambda$onRebuildComplete$0(ManageDomainUrls manageDomainUrls, Intent launchIntent, Preference pref) {
        try {
            manageDomainUrls.startActivity(launchIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mWebAction) {
            return false;
        }
        Secure.putInt(getContentResolver(), "instant_apps_enabled", ((Boolean) newValue).booleanValue());
        return true;
    }

    private void rebuild() {
        ArrayList<AppEntry> apps = this.mSession.rebuild(ApplicationsState.FILTER_WITH_DOMAIN_URLS, ApplicationsState.ALPHA_COMPARATOR);
        if (apps != null) {
            onRebuildComplete(apps);
        }
    }

    private void rebuildAppList(PreferenceGroup group, ArrayList<AppEntry> apps) {
        cacheRemoveAllPrefs(group);
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry entry = (AppEntry) apps.get(i);
            String key = new StringBuilder();
            key.append(entry.info.packageName);
            key.append("|");
            key.append(entry.info.uid);
            key = key.toString();
            DomainAppPreference preference = (DomainAppPreference) getCachedPreference(key);
            if (preference == null) {
                preference = new DomainAppPreference(getPrefContext(), this.mApplicationsState, entry);
                preference.setKey(key);
                preference.setOnPreferenceClickListener(this);
                group.addPreference(preference);
            } else {
                preference.reuse();
            }
            preference.setOrder(i);
        }
        removeCachedPrefs(group);
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
        rebuild();
    }

    public int getMetricsCategory() {
        return 143;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getClass() != DomainAppPreference.class) {
            return false;
        }
        AppEntry entry = ((DomainAppPreference) preference).mEntry;
        AppInfoBase.startAppInfoFragment(AppLaunchSettings.class, R.string.auto_launch_label, entry.info.packageName, entry.info.uid, this, 1, getMetricsCategory());
        return true;
    }
}
