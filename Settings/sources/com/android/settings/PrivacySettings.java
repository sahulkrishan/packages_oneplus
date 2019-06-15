package com.android.settings;

import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrivacySettings extends SettingsPreferenceFragment {
    @VisibleForTesting
    static final String AUTO_RESTORE = "auto_restore";
    @VisibleForTesting
    static final String BACKUP_DATA = "backup_data";
    private static final String BACKUP_INACTIVE = "backup_inactive";
    @VisibleForTesting
    static final String CONFIGURE_ACCOUNT = "configure_account";
    @VisibleForTesting
    static final String DATA_MANAGEMENT = "data_management";
    private static final String GSETTINGS_PROVIDER = "com.google.settings";
    private static final String TAG = "PrivacySettings";
    private SwitchPreference mAutoRestore;
    private Preference mBackup;
    private IBackupManager mBackupManager;
    private Preference mConfigure;
    private boolean mEnabled;
    private Preference mManageData;
    private OnPreferenceChangeListener preferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (!(preference instanceof SwitchPreference)) {
                return true;
            }
            boolean nextValue = ((Boolean) newValue).booleanValue();
            boolean result = false;
            if (preference == PrivacySettings.this.mAutoRestore) {
                try {
                    PrivacySettings.this.mBackupManager.setAutoRestore(nextValue);
                    result = true;
                } catch (RemoteException e) {
                    PrivacySettings.this.mAutoRestore.setChecked(nextValue ^ 1);
                }
            }
            return result;
        }
    };

    public int getMetricsCategory() {
        return 81;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mEnabled = UserManager.get(getActivity()).isAdminUser();
        if (this.mEnabled) {
            addPreferencesFromResource(R.xml.privacy_settings);
            PreferenceScreen screen = getPreferenceScreen();
            this.mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
            setPreferenceReferences(screen);
            Set<String> keysToRemove = new HashSet();
            getNonVisibleKeys(getActivity(), keysToRemove);
            for (int i = screen.getPreferenceCount() - 1; i >= 0; i--) {
                Preference preference = screen.getPreference(i);
                if (keysToRemove.contains(preference.getKey())) {
                    screen.removePreference(preference);
                }
            }
            updateToggles();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mEnabled) {
            updateToggles();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreferenceReferences(PreferenceScreen screen) {
        this.mBackup = screen.findPreference(BACKUP_DATA);
        this.mAutoRestore = (SwitchPreference) screen.findPreference(AUTO_RESTORE);
        this.mAutoRestore.setOnPreferenceChangeListener(this.preferenceChangeListener);
        this.mConfigure = screen.findPreference(CONFIGURE_ACCOUNT);
        this.mManageData = screen.findPreference(DATA_MANAGEMENT);
    }

    private void updateToggles() {
        ContentResolver res = getContentResolver();
        boolean backupEnabled = false;
        Intent configIntent = null;
        String configSummary = null;
        Intent manageIntent = null;
        CharSequence manageLabel = null;
        boolean manageEnabled = false;
        try {
            int i;
            backupEnabled = this.mBackupManager.isBackupEnabled();
            String transport = this.mBackupManager.getCurrentTransport();
            configIntent = validatedActivityIntent(this.mBackupManager.getConfigurationIntent(transport), "config");
            configSummary = this.mBackupManager.getDestinationString(transport);
            manageIntent = validatedActivityIntent(this.mBackupManager.getDataManagementIntent(transport), "management");
            manageLabel = this.mBackupManager.getDataManagementLabel(transport);
            Preference preference = this.mBackup;
            if (backupEnabled) {
                i = R.string.accessibility_feature_state_on;
            } else {
                i = R.string.accessibility_feature_state_off;
            }
            preference.setSummary(i);
        } catch (RemoteException e) {
            this.mBackup.setEnabled(false);
        }
        this.mAutoRestore.setChecked(Secure.getInt(res, "backup_auto_restore", 1) == 1);
        this.mAutoRestore.setEnabled(backupEnabled);
        boolean configureEnabled = configIntent != null && backupEnabled;
        this.mConfigure.setEnabled(configureEnabled);
        this.mConfigure.setIntent(configIntent);
        setConfigureSummary(configSummary);
        if (manageIntent != null && backupEnabled) {
            manageEnabled = true;
        }
        if (manageEnabled) {
            this.mManageData.setIntent(manageIntent);
            if (manageLabel != null) {
                this.mManageData.setTitle(manageLabel);
                return;
            }
            return;
        }
        getPreferenceScreen().removePreference(this.mManageData);
    }

    private Intent validatedActivityIntent(Intent intent, String logLabel) {
        if (intent == null) {
            return intent;
        }
        List<ResolveInfo> resolved = getPackageManager().queryIntentActivities(intent, null);
        if (resolved != null && !resolved.isEmpty()) {
            return intent;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Backup ");
        stringBuilder.append(logLabel);
        stringBuilder.append(" intent ");
        stringBuilder.append(null);
        stringBuilder.append(" fails to resolve; ignoring");
        Log.e(str, stringBuilder.toString());
        return null;
    }

    private void setConfigureSummary(String summary) {
        if (summary != null) {
            this.mConfigure.setSummary((CharSequence) summary);
        } else {
            this.mConfigure.setSummary((int) R.string.backup_configure_account_default_summary);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_backup_reset;
    }

    private static void getNonVisibleKeys(Context context, Collection<String> nonVisibleKeys) {
        boolean vendorSpecific = false;
        boolean isServiceActive = false;
        try {
            isServiceActive = Stub.asInterface(ServiceManager.getService("backup")).isBackupServiceActive(UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed querying backup manager service activity status. Assuming it is inactive.");
        }
        if (context.getPackageManager().resolveContentProvider(GSETTINGS_PROVIDER, 0) == null) {
            vendorSpecific = true;
        }
        if (vendorSpecific || isServiceActive) {
            nonVisibleKeys.add(BACKUP_INACTIVE);
        }
        if (vendorSpecific || !isServiceActive) {
            nonVisibleKeys.add(BACKUP_DATA);
            nonVisibleKeys.add(AUTO_RESTORE);
            nonVisibleKeys.add(CONFIGURE_ACCOUNT);
        }
    }
}
