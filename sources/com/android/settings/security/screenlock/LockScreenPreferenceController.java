package com.android.settings.security.screenlock;

import android.content.Context;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.notification.LockScreenNotificationPreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class LockScreenPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume {
    static final String KEY_LOCKSCREEN_PREFERENCES = "lockscreen_preferences";
    private static final int MY_USER_ID = UserHandle.myUserId();
    private final LockPatternUtils mLockPatternUtils;
    private Preference mPreference;

    public LockScreenPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_LOCKSCREEN_PREFERENCES);
        this.mLockPatternUtils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public int getAvailabilityStatus() {
        int i = 0;
        if (this.mLockPatternUtils.isSecure(MY_USER_ID)) {
            if (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(MY_USER_ID) == 0) {
                i = 3;
            }
            return i;
        }
        if (this.mLockPatternUtils.isLockScreenDisabled(MY_USER_ID)) {
            i = 3;
        }
        return i;
    }

    public void updateState(Preference preference) {
        preference.setSummary(LockScreenNotificationPreferenceController.getSummaryResource(this.mContext));
    }

    public void onResume() {
        this.mPreference.setVisible(isAvailable());
    }
}
