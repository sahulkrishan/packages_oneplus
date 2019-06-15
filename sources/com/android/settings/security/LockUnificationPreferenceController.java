package com.android.settings.security;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.AbstractPreferenceController;

public class LockUnificationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_UNIFICATION = "unification";
    private static final int MY_USER_ID = UserHandle.myUserId();
    private String mCurrentDevicePassword;
    private String mCurrentProfilePassword;
    private final SecuritySettings mHost;
    private final LockPatternUtils mLockPatternUtils;
    private final int mProfileChallengeUserId = Utils.getManagedProfileId(this.mUm, MY_USER_ID);
    private final UserManager mUm;
    private RestrictedSwitchPreference mUnifyProfile;

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mUnifyProfile = (RestrictedSwitchPreference) screen.findPreference(KEY_UNIFICATION);
    }

    public LockUnificationPreferenceController(Context context, SecuritySettings host) {
        super(context);
        this.mHost = host;
        this.mUm = (UserManager) context.getSystemService("user");
        this.mLockPatternUtils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
    }

    public boolean isAvailable() {
        return this.mProfileChallengeUserId != -10000 && this.mLockPatternUtils.isSeparateProfileChallengeAllowed(this.mProfileChallengeUserId);
    }

    public String getPreferenceKey() {
        return KEY_UNIFICATION;
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean z = false;
        if (Utils.startQuietModeDialogIfNecessary(this.mContext, this.mUm, this.mProfileChallengeUserId)) {
            return false;
        }
        if (((Boolean) value).booleanValue()) {
            if (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId) >= 65536 && this.mLockPatternUtils.isSeparateProfileChallengeAllowedToUnify(this.mProfileChallengeUserId)) {
                z = true;
            }
            UnificationConfirmationDialog.newInstance(z).show(this.mHost);
        } else {
            if (!new ChooseLockSettingsHelper(this.mHost.getActivity(), this.mHost).launchConfirmationActivity(130, this.mContext.getString(R.string.unlock_set_unlock_launch_picker_title), true, MY_USER_ID)) {
                ununifyLocks();
            }
        }
        return true;
    }

    public void updateState(Preference preference) {
        if (this.mUnifyProfile != null) {
            boolean separate = this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId);
            this.mUnifyProfile.setChecked(separate ^ 1);
            if (separate) {
                this.mUnifyProfile.setDisabledByAdmin(RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_unified_password", this.mProfileChallengeUserId));
            }
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 130 && resultCode == -1) {
            ununifyLocks();
            return true;
        } else if (requestCode == 128 && resultCode == -1) {
            this.mCurrentDevicePassword = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            launchConfirmProfileLockForUnification();
            return true;
        } else if (requestCode != 129 || resultCode != -1) {
            return false;
        } else {
            this.mCurrentProfilePassword = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            unifyLocks();
            return true;
        }
    }

    private void ununifyLocks() {
        Bundle extras = new Bundle();
        extras.putInt("android.intent.extra.USER_ID", this.mProfileChallengeUserId);
        new SubSettingLauncher(this.mContext).setDestination(ChooseLockGenericFragment.class.getName()).setTitle((int) R.string.lock_settings_picker_title_profile).setSourceMetricsCategory(this.mHost.getMetricsCategory()).setArguments(extras).launch();
    }

    /* Access modifiers changed, original: 0000 */
    public void launchConfirmDeviceLockForUnification() {
        if (!new ChooseLockSettingsHelper(this.mHost.getActivity(), this.mHost).launchConfirmationActivity(128, this.mContext.getString(R.string.unlock_set_unlock_launch_picker_title), true, MY_USER_ID)) {
            launchConfirmProfileLockForUnification();
        }
    }

    private void launchConfirmProfileLockForUnification() {
        if (!new ChooseLockSettingsHelper(this.mHost.getActivity(), this.mHost).launchConfirmationActivity(129, this.mContext.getString(R.string.unlock_set_unlock_launch_picker_title_profile), true, this.mProfileChallengeUserId)) {
            unifyLocks();
        }
    }

    private void unifyLocks() {
        int profileQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId);
        if (profileQuality == 65536) {
            this.mLockPatternUtils.saveLockPattern(LockPatternUtils.stringToPattern(this.mCurrentProfilePassword), this.mCurrentDevicePassword, MY_USER_ID);
        } else {
            this.mLockPatternUtils.saveLockPassword(this.mCurrentProfilePassword, this.mCurrentDevicePassword, profileQuality, MY_USER_ID);
        }
        this.mLockPatternUtils.setSeparateProfileChallengeEnabled(this.mProfileChallengeUserId, false, this.mCurrentProfilePassword);
        this.mLockPatternUtils.setVisiblePatternEnabled(this.mLockPatternUtils.isVisiblePatternEnabled(this.mProfileChallengeUserId), MY_USER_ID);
        this.mCurrentDevicePassword = null;
        this.mCurrentProfilePassword = null;
    }

    /* Access modifiers changed, original: 0000 */
    public void unifyUncompliantLocks() {
        this.mLockPatternUtils.setSeparateProfileChallengeEnabled(this.mProfileChallengeUserId, false, this.mCurrentProfilePassword);
        new SubSettingLauncher(this.mContext).setDestination(ChooseLockGenericFragment.class.getName()).setTitle((int) R.string.oneplus_choose_screen_lock_method).setSourceMetricsCategory(this.mHost.getMetricsCategory()).launch();
    }
}
