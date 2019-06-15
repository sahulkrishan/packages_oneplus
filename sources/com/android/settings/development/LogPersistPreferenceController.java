package com.android.settings.development;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.development.AbstractLogpersistPreferenceController;

public class LogPersistPreferenceController extends AbstractLogpersistPreferenceController implements PreferenceControllerMixin {
    private final DevelopmentSettingsDashboardFragment mFragment;

    public LogPersistPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mFragment = fragment;
    }

    public void showConfirmationDialog(@Nullable Preference preference) {
        DisableLogPersistWarningDialog.show(this.mFragment);
    }

    public void dismissConfirmationDialog() {
    }

    public boolean isConfirmationDialogShowing() {
        return false;
    }

    public void updateState(Preference preference) {
        updateLogpersistValues();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeLogpersistOption(null, true);
    }

    public void onDisableLogPersistDialogConfirmed() {
        setLogpersistOff(true);
        updateLogpersistValues();
    }

    public void onDisableLogPersistDialogRejected() {
        updateLogpersistValues();
    }
}
