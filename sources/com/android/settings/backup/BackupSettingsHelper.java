package com.android.settings.backup;

import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Settings.PrivacySettingsActivity;
import java.net.URISyntaxException;

public class BackupSettingsHelper {
    private static final String TAG = "BackupSettingsHelper";
    private IBackupManager mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
    private Context mContext;

    public BackupSettingsHelper(Context context) {
        this.mContext = context;
    }

    public Intent getIntentForBackupSettings() {
        if (isIntentProvidedByTransport()) {
            return getIntentForBackupSettingsFromTransport();
        }
        Log.e(TAG, "Backup transport has not provided an intent or the component for the intent is not found!");
        return getIntentForDefaultBackupSettings();
    }

    public String getLabelForBackupSettings() {
        String label = getLabelFromBackupTransport();
        if (label == null || label.isEmpty()) {
            return this.mContext.getString(R.string.privacy_settings_title);
        }
        return label;
    }

    public String getSummaryForBackupSettings() {
        String summary = getSummaryFromBackupTransport();
        if (summary == null) {
            return this.mContext.getString(R.string.backup_configure_account_default_summary);
        }
        return summary;
    }

    public boolean isBackupProvidedByManufacturer() {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Checking if intent provided by manufacturer");
        }
        String intentString = this.mContext.getResources().getString(R.string.config_backup_settings_intent);
        return (intentString == null || intentString.isEmpty()) ? false : true;
    }

    public String getLabelProvidedByManufacturer() {
        return this.mContext.getResources().getString(R.string.config_backup_settings_label);
    }

    public Intent getIntentProvidedByManufacturer() {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "Getting a backup settings intent provided by manufacturer");
        }
        String intentString = this.mContext.getResources().getString(R.string.config_backup_settings_intent);
        if (!(intentString == null || intentString.isEmpty())) {
            try {
                return Intent.parseUri(intentString, 0);
            } catch (URISyntaxException e) {
                Log.e(TAG, "Invalid intent provided by the manufacturer.", e);
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Intent getIntentForBackupSettingsFromTransport() {
        Intent intent = getIntentFromBackupTransport();
        if (intent != null) {
            intent.putExtra("backup_services_available", isBackupServiceActive());
        }
        return intent;
    }

    private Intent getIntentForDefaultBackupSettings() {
        return new Intent(this.mContext, PrivacySettingsActivity.class);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isIntentProvidedByTransport() {
        Intent intent = getIntentFromBackupTransport();
        return (intent == null || intent.resolveActivity(this.mContext.getPackageManager()) == null) ? false : true;
    }

    private Intent getIntentFromBackupTransport() {
        try {
            Intent intent = this.mBackupManager.getDataManagementIntent(this.mBackupManager.getCurrentTransport());
            if (Log.isLoggable(TAG, 3)) {
                if (intent != null) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Parsed intent from backup transport: ");
                    stringBuilder.append(intent.toString());
                    Log.d(str, stringBuilder.toString());
                } else {
                    Log.d(TAG, "Received a null intent from backup transport");
                }
            }
            return intent;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting data management intent", e);
            return null;
        }
    }

    private boolean isBackupServiceActive() {
        try {
            return this.mBackupManager.isBackupServiceActive(UserHandle.myUserId());
        } catch (Exception e) {
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getLabelFromBackupTransport() {
        try {
            String label = this.mBackupManager.getDataManagementLabel(this.mBackupManager.getCurrentTransport());
            if (Log.isLoggable(TAG, 3)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Received the backup settings label from backup transport: ");
                stringBuilder.append(label);
                Log.d(str, stringBuilder.toString());
            }
            return label;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting data management label", e);
            return null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getSummaryFromBackupTransport() {
        try {
            String summary = this.mBackupManager.getDestinationString(this.mBackupManager.getCurrentTransport());
            if (Log.isLoggable(TAG, 3)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Received the backup settings summary from backup transport: ");
                stringBuilder.append(summary);
                Log.d(str, stringBuilder.toString());
            }
            return summary;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting data management summary", e);
            return null;
        }
    }
}
