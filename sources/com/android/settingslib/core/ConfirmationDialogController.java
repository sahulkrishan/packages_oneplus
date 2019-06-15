package com.android.settingslib.core;

import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;

public interface ConfirmationDialogController {
    void dismissConfirmationDialog();

    String getPreferenceKey();

    boolean isConfirmationDialogShowing();

    void showConfirmationDialog(@Nullable Preference preference);
}
