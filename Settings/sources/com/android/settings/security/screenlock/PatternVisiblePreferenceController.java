package com.android.settings.security.screenlock;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class PatternVisiblePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String PREF_KEY = "visiblepattern";
    private final LockPatternUtils mLockPatternUtils;
    private final int mUserId;

    public PatternVisiblePreferenceController(Context context, int userId, LockPatternUtils lockPatternUtils) {
        super(context);
        this.mUserId = userId;
        this.mLockPatternUtils = lockPatternUtils;
    }

    public boolean isAvailable() {
        return isPatternLock();
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(this.mLockPatternUtils.isVisiblePatternEnabled(this.mUserId));
    }

    private boolean isPatternLock() {
        return this.mLockPatternUtils.isSecure(this.mUserId) && this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId) == 65536;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mLockPatternUtils.setVisiblePatternEnabled(((Boolean) newValue).booleanValue(), this.mUserId);
        return true;
    }
}
