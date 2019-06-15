package com.android.settings.development;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.AbstractEnableAdbPreferenceController;

public class AdbPreferenceController extends AbstractEnableAdbPreferenceController implements PreferenceControllerMixin {
    private final DevelopmentSettingsDashboardFragment mFragment;

    public AdbPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public void onAdbDialogConfirmed() {
        writeAdbSetting(true);
    }

    public void onAdbDialogDismissed() {
        updateState(this.mPreference);
    }

    public void showConfirmationDialog(@Nullable Preference preference) {
        EnableAdbWarningDialog.show(this.mFragment);
    }

    public void dismissConfirmationDialog() {
    }

    public boolean isConfirmationDialogShowing() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeAdbSetting(false);
        this.mPreference.setChecked(false);
    }
}
