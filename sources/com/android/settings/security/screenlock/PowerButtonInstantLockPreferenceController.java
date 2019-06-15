package com.android.settings.security.screenlock;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.trustagent.TrustAgentManager;
import com.android.settingslib.core.AbstractPreferenceController;

public class PowerButtonInstantLockPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_POWER_INSTANTLY_LOCKS = "power_button_instantly_locks";
    private final LockPatternUtils mLockPatternUtils;
    private final TrustAgentManager mTrustAgentManager;
    private final int mUserId;

    public PowerButtonInstantLockPreferenceController(Context context, int userId, LockPatternUtils lockPatternUtils) {
        super(context);
        this.mUserId = userId;
        this.mLockPatternUtils = lockPatternUtils;
        this.mTrustAgentManager = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getTrustAgentManager();
    }

    public boolean isAvailable() {
        if (!this.mLockPatternUtils.isSecure(this.mUserId)) {
            return false;
        }
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId);
        if (keyguardStoredPasswordQuality == 65536 || keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608 || keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
            return true;
        }
        return false;
    }

    public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(this.mLockPatternUtils.getPowerButtonInstantlyLocks(this.mUserId));
        if (TextUtils.isEmpty(this.mTrustAgentManager.getActiveTrustAgentLabel(this.mContext, this.mLockPatternUtils))) {
            preference.setSummary((int) R.string.summary_placeholder);
            return;
        }
        preference.setSummary(this.mContext.getString(R.string.lockpattern_settings_power_button_instantly_locks_summary, new Object[]{trustAgentLabel}));
    }

    public String getPreferenceKey() {
        return KEY_POWER_INSTANTLY_LOCKS;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mLockPatternUtils.setPowerButtonInstantlyLocks(((Boolean) newValue).booleanValue(), this.mUserId);
        return true;
    }
}
