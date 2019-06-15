package com.android.settings.location;

import android.content.Context;
import android.os.UserHandle;
import android.widget.Switch;
import com.android.settings.location.LocationEnabler.LocationModeChangeListener;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class LocationSwitchBarController implements OnSwitchChangeListener, LocationModeChangeListener, LifecycleObserver, OnStart, OnStop {
    private final LocationEnabler mLocationEnabler;
    private final Switch mSwitch = this.mSwitchBar.getSwitch();
    private final SwitchBar mSwitchBar;
    private boolean mValidListener;

    public LocationSwitchBarController(Context context, SwitchBar switchBar, Lifecycle lifecycle) {
        this.mSwitchBar = switchBar;
        this.mLocationEnabler = new LocationEnabler(context, this, lifecycle);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void onStart() {
        if (!this.mValidListener) {
            this.mSwitchBar.addOnSwitchChangeListener(this);
            this.mValidListener = true;
        }
    }

    public void onStop() {
        if (this.mValidListener) {
            this.mSwitchBar.removeOnSwitchChangeListener(this);
            this.mValidListener = false;
        }
    }

    public void onLocationModeChanged(int mode, boolean restricted) {
        boolean enabled = this.mLocationEnabler.isEnabled(mode);
        int userId = UserHandle.myUserId();
        EnforcedAdmin admin = this.mLocationEnabler.getShareLocationEnforcedAdmin(userId);
        if (this.mLocationEnabler.hasShareLocationRestriction(userId) || admin == null) {
            this.mSwitchBar.setEnabled(restricted ^ 1);
        } else {
            this.mSwitchBar.setDisabledByAdmin(admin);
        }
        if (enabled != this.mSwitch.isChecked()) {
            if (this.mValidListener) {
                this.mSwitchBar.removeOnSwitchChangeListener(this);
            }
            this.mSwitch.setChecked(enabled);
            if (this.mValidListener) {
                this.mSwitchBar.addOnSwitchChangeListener(this);
            }
        }
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        this.mLocationEnabler.setLocationEnabled(isChecked);
    }
}
