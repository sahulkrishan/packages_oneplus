package com.android.settings.security.screenlock;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.TimeoutListPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.trustagent.TrustAgentManager;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.AbstractPreferenceController;

public class LockAfterTimeoutPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_LOCK_AFTER_TIMEOUT = "lock_after_timeout";
    private final DevicePolicyManager mDPM;
    private final LockPatternUtils mLockPatternUtils;
    private final TrustAgentManager mTrustAgentManager;
    private final int mUserId;

    public LockAfterTimeoutPreferenceController(Context context, int userId, LockPatternUtils lockPatternUtils) {
        super(context);
        this.mUserId = userId;
        this.mLockPatternUtils = lockPatternUtils;
        this.mDPM = (DevicePolicyManager) context.getSystemService("device_policy");
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

    public String getPreferenceKey() {
        return KEY_LOCK_AFTER_TIMEOUT;
    }

    public void updateState(Preference preference) {
        setupLockAfterPreference((TimeoutListPreference) preference);
        updateLockAfterPreferenceSummary((TimeoutListPreference) preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            Secure.putInt(this.mContext.getContentResolver(), "lock_screen_lock_after_timeout", Integer.parseInt((String) newValue));
            updateState(preference);
        } catch (NumberFormatException e) {
            Log.e(PreferenceControllerMixin.TAG, "could not persist lockAfter timeout setting", e);
        }
        return true;
    }

    private void setupLockAfterPreference(TimeoutListPreference preference) {
        preference.setValue(String.valueOf(Secure.getLong(this.mContext.getContentResolver(), "lock_screen_lock_after_timeout", 5000)));
        if (this.mDPM != null) {
            preference.removeUnusableTimeouts(Math.max(0, this.mDPM.getMaximumTimeToLock(null, UserHandle.myUserId()) - ((long) Math.max(0, System.getInt(this.mContext.getContentResolver(), "screen_off_timeout", 0)))), RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(this.mContext));
        }
    }

    private void updateLockAfterPreferenceSummary(TimeoutListPreference preference) {
        CharSequence summary;
        if (preference.isDisabledByAdmin()) {
            summary = this.mContext.getText(R.string.disabled_by_policy_title);
        } else {
            long currentTimeout = Secure.getLong(this.mContext.getContentResolver(), "lock_screen_lock_after_timeout", 5000);
            CharSequence[] entries = preference.getEntries();
            CharSequence[] values = preference.getEntryValues();
            int best = 0;
            for (int i = 0; i < values.length; i++) {
                if (currentTimeout >= Long.valueOf(values[i].toString()).longValue()) {
                    best = i;
                }
            }
            if (TextUtils.isEmpty(this.mTrustAgentManager.getActiveTrustAgentLabel(this.mContext, this.mLockPatternUtils))) {
                summary = this.mContext.getString(R.string.lock_after_timeout_summary, new Object[]{entries[best]});
            } else {
                CharSequence summary2;
                if (Long.valueOf(values[best].toString()).longValue() == 0) {
                    summary2 = this.mContext.getString(R.string.lock_immediately_summary_with_exception, new Object[]{trustAgentLabel});
                } else {
                    summary2 = this.mContext.getString(R.string.lock_after_timeout_summary_with_exception, new Object[]{entries[best], trustAgentLabel});
                }
                summary = summary2;
            }
        }
        preference.setSummary(summary);
    }
}
