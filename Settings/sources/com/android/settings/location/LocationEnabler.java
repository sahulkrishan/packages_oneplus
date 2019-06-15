package com.android.settings.location;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class LocationEnabler implements LifecycleObserver, OnResume, OnPause {
    @VisibleForTesting
    static final IntentFilter INTENT_FILTER_LOCATION_MODE_CHANGED = new IntentFilter("android.location.MODE_CHANGED");
    private static final String TAG = "LocationEnabler";
    private final Context mContext;
    private final LocationModeChangeListener mListener;
    @VisibleForTesting
    BroadcastReceiver mReceiver;
    private final UserManager mUserManager;

    public interface LocationModeChangeListener {
        void onLocationModeChanged(int i, boolean z);
    }

    public LocationEnabler(Context context, LocationModeChangeListener listener, Lifecycle lifecycle) {
        this.mContext = context;
        this.mListener = listener;
        this.mUserManager = (UserManager) context.getSystemService("user");
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void onResume() {
        if (this.mReceiver == null) {
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (Log.isLoggable(LocationEnabler.TAG, 3)) {
                        String str = LocationEnabler.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Received location mode change intent: ");
                        stringBuilder.append(intent);
                        Log.d(str, stringBuilder.toString());
                    }
                    LocationEnabler.this.refreshLocationMode();
                }
            };
        }
        this.mContext.registerReceiver(this.mReceiver, INTENT_FILTER_LOCATION_MODE_CHANGED);
        refreshLocationMode();
    }

    public void onPause() {
        try {
            this.mContext.unregisterReceiver(this.mReceiver);
        } catch (RuntimeException e) {
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshLocationMode() {
        int mode = Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
        if (Log.isLoggable(TAG, 4)) {
            Log.i(TAG, "Location mode has been changed");
        }
        if (this.mListener != null) {
            this.mListener.onLocationModeChanged(mode, isRestricted());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setLocationEnabled(boolean enabled) {
        int currentMode = Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
        if (isRestricted()) {
            if (Log.isLoggable(TAG, 4)) {
                Log.i(TAG, "Restricted user, not setting location mode");
            }
            if (this.mListener != null) {
                this.mListener.onLocationModeChanged(currentMode, true);
            }
            return;
        }
        Utils.updateLocationEnabled(this.mContext, enabled, UserHandle.myUserId(), 1);
        refreshLocationMode();
    }

    /* Access modifiers changed, original: 0000 */
    public void setLocationMode(int mode) {
        int currentMode = Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
        if (isRestricted()) {
            if (Log.isLoggable(TAG, 4)) {
                Log.i(TAG, "Restricted user, not setting location mode");
            }
            if (this.mListener != null) {
                this.mListener.onLocationModeChanged(currentMode, true);
            }
            return;
        }
        Utils.updateLocationMode(this.mContext, currentMode, mode, ActivityManager.getCurrentUser(), 1);
        refreshLocationMode();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isEnabled(int mode) {
        return (mode == 0 || isRestricted()) ? false : true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isManagedProfileRestrictedByBase() {
        UserHandle managedProfile = com.android.settings.Utils.getManagedProfile(this.mUserManager);
        return managedProfile != null && hasShareLocationRestriction(managedProfile.getIdentifier());
    }

    /* Access modifiers changed, original: 0000 */
    public EnforcedAdmin getShareLocationEnforcedAdmin(int userId) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_share_location", userId);
        if (admin == null) {
            return RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_config_location", userId);
        }
        return admin;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasShareLocationRestriction(int userId) {
        return RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_share_location", userId);
    }

    private boolean isRestricted() {
        return this.mUserManager.hasUserRestriction("no_share_location");
    }
}
