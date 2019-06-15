package com.android.settings.security;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;

public class ChangeProfileScreenLockPreferenceController extends ChangeScreenLockPreferenceController {
    private static final String KEY_UNLOCK_SET_OR_CHANGE_PROFILE = "unlock_set_or_change_profile";

    public ChangeProfileScreenLockPreferenceController(Context context, SecuritySettings host) {
        super(context, host);
    }

    public boolean isAvailable() {
        if (this.mProfileChallengeUserId == -10000 || !this.mLockPatternUtils.isSeparateProfileChallengeAllowed(this.mProfileChallengeUserId)) {
            return false;
        }
        if (!this.mLockPatternUtils.isSecure(this.mProfileChallengeUserId)) {
            return true;
        }
        int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId);
        if (keyguardStoredPasswordQuality == 65536 || keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608 || keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
            return true;
        }
        return false;
    }

    public String getPreferenceKey() {
        return KEY_UNLOCK_SET_OR_CHANGE_PROFILE;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey()) || Utils.startQuietModeDialogIfNecessary(this.mContext, this.mUm, this.mProfileChallengeUserId)) {
            return false;
        }
        Bundle extras = new Bundle();
        extras.putInt("android.intent.extra.USER_ID", this.mProfileChallengeUserId);
        new SubSettingLauncher(this.mContext).setDestination(ChooseLockGenericFragment.class.getName()).setTitle((int) R.string.lock_settings_picker_title_profile).setSourceMetricsCategory(this.mHost.getMetricsCategory()).setArguments(extras).launch();
        return true;
    }

    public void updateState(Preference preference) {
        updateSummary(preference, this.mProfileChallengeUserId);
        if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId)) {
            disableIfPasswordQualityManaged(this.mProfileChallengeUserId);
            return;
        }
        this.mPreference.setSummary((CharSequence) this.mContext.getString(R.string.lock_settings_profile_unified_summary));
        this.mPreference.setEnabled(false);
    }
}
