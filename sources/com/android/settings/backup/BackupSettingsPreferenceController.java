package com.android.settings.backup;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class BackupSettingsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String BACKUP_SETTINGS = "backup_settings";
    private static final String MANUFACTURER_SETTINGS = "manufacturer_backup";
    private Intent mBackupSettingsIntent;
    private String mBackupSettingsSummary;
    private String mBackupSettingsTitle;
    private Intent mManufacturerIntent;
    private String mManufacturerLabel;

    public BackupSettingsPreferenceController(Context context) {
        super(context);
        BackupSettingsHelper settingsHelper = new BackupSettingsHelper(context);
        this.mBackupSettingsIntent = settingsHelper.getIntentForBackupSettings();
        this.mBackupSettingsTitle = settingsHelper.getLabelForBackupSettings();
        this.mBackupSettingsSummary = settingsHelper.getSummaryForBackupSettings();
        this.mManufacturerIntent = settingsHelper.getIntentProvidedByManufacturer();
        this.mManufacturerLabel = settingsHelper.getLabelProvidedByManufacturer();
    }

    public void displayPreference(PreferenceScreen screen) {
        Preference backupSettings = screen.findPreference(BACKUP_SETTINGS);
        Preference manufacturerSettings = screen.findPreference(MANUFACTURER_SETTINGS);
        backupSettings.setIntent(this.mBackupSettingsIntent);
        backupSettings.setTitle(this.mBackupSettingsTitle);
        backupSettings.setSummary(this.mBackupSettingsSummary);
        manufacturerSettings.setIntent(this.mManufacturerIntent);
        manufacturerSettings.setTitle(this.mManufacturerLabel);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }
}
