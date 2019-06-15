package com.android.settings.development;

import android.support.annotation.NonNull;
import com.android.settings.Utils;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.development.DevelopmentSettingsEnabler;

public class DevelopmentSwitchBarController implements LifecycleObserver, OnStart, OnStop {
    private final boolean mIsAvailable;
    private final DevelopmentSettingsDashboardFragment mSettings;
    private final SwitchBar mSwitchBar;

    public DevelopmentSwitchBarController(@NonNull DevelopmentSettingsDashboardFragment settings, SwitchBar switchBar, boolean isAvailable, Lifecycle lifecycle) {
        this.mSwitchBar = switchBar;
        boolean z = isAvailable && !Utils.isMonkeyRunning();
        this.mIsAvailable = z;
        this.mSettings = settings;
        if (this.mIsAvailable) {
            lifecycle.addObserver(this);
        } else {
            this.mSwitchBar.setEnabled(false);
        }
    }

    public void onStart() {
        this.mSwitchBar.setChecked(DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mSettings.getContext()));
        this.mSwitchBar.addOnSwitchChangeListener(this.mSettings);
    }

    public void onStop() {
        this.mSwitchBar.removeOnSwitchChangeListener(this.mSettings);
    }
}
