package com.android.settings.development;

import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class LocalBackupPasswordPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private static final String LOCAL_BACKUP_PASSWORD = "local_backup_password";
    private final IBackupManager mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
    private final UserManager mUserManager;

    public LocalBackupPasswordPreferenceController(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public String getPreferenceKey() {
        return LOCAL_BACKUP_PASSWORD;
    }

    public void updateState(Preference preference) {
        updatePasswordSummary(preference);
    }

    private void updatePasswordSummary(Preference preference) {
        boolean z = isAdminUser() && this.mBackupManager != null;
        preference.setEnabled(z);
        if (this.mBackupManager != null) {
            try {
                if (this.mBackupManager.hasBackupPassword()) {
                    preference.setSummary((int) R.string.local_backup_password_summary_change);
                } else {
                    preference.setSummary((int) R.string.local_backup_password_summary_none);
                }
            } catch (RemoteException e) {
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isAdminUser() {
        return this.mUserManager.isAdminUser();
    }
}
