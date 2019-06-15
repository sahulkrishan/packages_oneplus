package com.android.settings.datausage;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settings.datausage.AppStateDataUsageBridge.DataUsageState;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.AppSwitchPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreferenceHelper;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public class UnrestrictedDataAccess extends SettingsPreferenceFragment implements Callbacks, Callback, OnPreferenceChangeListener {
    private static final String EXTRA_SHOW_SYSTEM = "show_system";
    private static final int MENU_SHOW_SYSTEM = 43;
    private ApplicationsState mApplicationsState;
    private DataSaverBackend mDataSaverBackend;
    private AppStateDataUsageBridge mDataUsageBridge;
    private boolean mExtraLoaded;
    private AppFilter mFilter;
    private Session mSession;
    private boolean mShowSystem;

    @VisibleForTesting
    class AccessPreference extends AppSwitchPreference implements Listener {
        private final AppEntry mEntry;
        private final RestrictedPreferenceHelper mHelper;
        private final DataUsageState mState = ((DataUsageState) this.mEntry.extraInfo);

        public AccessPreference(Context context, AppEntry entry) {
            super(context);
            setWidgetLayoutResource(R.layout.op_restricted_switch_widget);
            this.mHelper = new RestrictedPreferenceHelper(context, this, null);
            this.mEntry = entry;
            this.mEntry.ensureLabel(getContext());
            setDisabledByAdmin(RestrictedLockUtils.checkIfMeteredDataRestricted(context, entry.info.packageName, UserHandle.getUserId(entry.info.uid)));
            setState();
            if (this.mEntry.icon != null) {
                setIcon(this.mEntry.icon);
            }
        }

        public void onAttached() {
            super.onAttached();
            UnrestrictedDataAccess.this.mDataSaverBackend.addListener(this);
        }

        public void onDetached() {
            UnrestrictedDataAccess.this.mDataSaverBackend.remListener(this);
            super.onDetached();
        }

        /* Access modifiers changed, original: protected */
        public void onClick() {
            if (this.mState == null || !this.mState.isDataSaverBlacklisted) {
                super.onClick();
            } else {
                AppInfoDashboardFragment.startAppInfoFragment(AppDataUsage.class, R.string.app_data_usage, null, UnrestrictedDataAccess.this, this.mEntry);
            }
        }

        public void performClick() {
            if (!this.mHelper.performClick()) {
                super.performClick();
            }
        }

        private void setState() {
            setTitle((CharSequence) this.mEntry.label);
            if (this.mState != null) {
                setChecked(this.mState.isDataSaverWhitelisted);
                if (isDisabledByAdmin()) {
                    setSummary((int) R.string.disabled_by_admin);
                } else if (this.mState.isDataSaverBlacklisted) {
                    setSummary((int) R.string.restrict_background_blacklisted);
                } else {
                    setSummary((CharSequence) "");
                }
            }
        }

        public void reuse() {
            setState();
            notifyChanged();
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (this.mEntry.icon == null || getIcon() == null) {
                holder.itemView.post(new Runnable() {
                    public void run() {
                        UnrestrictedDataAccess.this.mApplicationsState.ensureIcon(AccessPreference.this.mEntry);
                        AccessPreference.this.setIcon(AccessPreference.this.mEntry.icon);
                    }
                });
            }
            boolean disabledByAdmin = isDisabledByAdmin();
            View widgetFrame = holder.findViewById(16908312);
            int i = 0;
            if (disabledByAdmin) {
                widgetFrame.setVisibility(0);
            } else {
                int i2;
                if (this.mState == null || !this.mState.isDataSaverBlacklisted) {
                    i2 = 0;
                } else {
                    i2 = 4;
                }
                widgetFrame.setVisibility(i2);
            }
            super.onBindViewHolder(holder);
            this.mHelper.onBindViewHolder(holder);
            holder.findViewById(R.id.restricted_icon).setVisibility(disabledByAdmin ? 0 : 8);
            View findViewById = holder.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
            if (disabledByAdmin) {
                i = 8;
            }
            findViewById.setVisibility(i);
        }

        public void onDataSaverChanged(boolean isDataSaving) {
        }

        public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
            if (this.mState != null && this.mEntry.info.uid == uid) {
                this.mState.isDataSaverWhitelisted = isWhitelisted;
                reuse();
            }
        }

        public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
            if (this.mState != null && this.mEntry.info.uid == uid) {
                this.mState.isDataSaverBlacklisted = isBlacklisted;
                reuse();
            }
        }

        public void setDisabledByAdmin(EnforcedAdmin admin) {
            this.mHelper.setDisabledByAdmin(admin);
        }

        public boolean isDisabledByAdmin() {
            return this.mHelper.isDisabledByAdmin();
        }

        @VisibleForTesting
        public AppEntry getEntryForTest() {
            return this.mEntry;
        }
    }

    public void onCreate(Bundle icicle) {
        AppFilter appFilter;
        super.onCreate(icicle);
        setAnimationAllowed(true);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        boolean z = icicle != null && icicle.getBoolean(EXTRA_SHOW_SYSTEM);
        this.mShowSystem = z;
        if (this.mShowSystem) {
            appFilter = ApplicationsState.FILTER_ALL_ENABLED;
        } else {
            appFilter = ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
        }
        this.mFilter = appFilter;
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 43, 0, this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 43) {
            AppFilter appFilter;
            this.mShowSystem ^= 1;
            item.setTitle(this.mShowSystem ? R.string.menu_hide_system : R.string.menu_show_system);
            if (this.mShowSystem) {
                appFilter = ApplicationsState.FILTER_ALL_ENABLED;
            } else {
                appFilter = ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER;
            }
            this.mFilter = appFilter;
            if (this.mExtraLoaded) {
                rebuild();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SHOW_SYSTEM, this.mShowSystem);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLoading(true, false);
    }

    public void onResume() {
        super.onResume();
        this.mDataUsageBridge.resume();
    }

    public void onPause() {
        super.onPause();
        this.mDataUsageBridge.pause();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mDataUsageBridge.release();
    }

    public void onExtraInfoUpdated() {
        this.mExtraLoaded = true;
        rebuild();
    }

    public int getHelpResource() {
        return R.string.help_url_unrestricted_data_access;
    }

    private void rebuild() {
        ArrayList<AppEntry> apps = this.mSession.rebuild(this.mFilter, ApplicationsState.ALPHA_COMPARATOR);
        if (apps != null) {
            onRebuildComplete(apps);
        }
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
    }

    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        if (getContext() != null) {
            cacheRemoveAllPrefs(getPreferenceScreen());
            int N = apps.size();
            for (int i = 0; i < N; i++) {
                AppEntry entry = (AppEntry) apps.get(i);
                if (shouldAddPreference(entry)) {
                    String key = new StringBuilder();
                    key.append(entry.info.packageName);
                    key.append("|");
                    key.append(entry.info.uid);
                    key = key.toString();
                    AccessPreference preference = (AccessPreference) getCachedPreference(key);
                    if (preference == null) {
                        preference = new AccessPreference(getPrefContext(), entry);
                        preference.setKey(key);
                        preference.setOnPreferenceChangeListener(this);
                        getPreferenceScreen().addPreference(preference);
                    } else {
                        preference.setDisabledByAdmin(RestrictedLockUtils.checkIfMeteredDataRestricted(getContext(), entry.info.packageName, UserHandle.getUserId(entry.info.uid)));
                        preference.reuse();
                    }
                    preference.setOrder(i);
                }
            }
            setLoading(false, true);
            removeCachedPrefs(getPreferenceScreen());
        }
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
    }

    public int getMetricsCategory() {
        return 349;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.unrestricted_data_access_settings;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean whitelisted = false;
        if (preference instanceof AccessPreference) {
            AccessPreference accessPreference = (AccessPreference) preference;
            if (accessPreference.mState != null) {
                if (newValue == Boolean.TRUE) {
                    whitelisted = true;
                }
                logSpecialPermissionChange(whitelisted, accessPreference.mEntry.info.packageName);
                this.mDataSaverBackend.setIsWhitelisted(accessPreference.mEntry.info.uid, accessPreference.mEntry.info.packageName, whitelisted);
                accessPreference.mState.isDataSaverWhitelisted = whitelisted;
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean whitelisted, String packageName) {
        int logCategory;
        if (whitelisted) {
            logCategory = 781;
        } else {
            logCategory = 782;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean shouldAddPreference(AppEntry app) {
        return (app == null || !UserHandle.isApp(app.info.uid) || app.extraInfo == null) ? false : true;
    }
}
