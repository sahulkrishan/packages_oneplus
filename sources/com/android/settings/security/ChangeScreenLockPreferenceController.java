package com.android.settings.security;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.security.screenlock.ScreenLockSettings;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;

public class ChangeScreenLockPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnGearClickListener {
    private static final String KEY_UNLOCK_SET_OR_CHANGE = "unlock_set_or_change";
    protected final DevicePolicyManager mDPM;
    protected final SecuritySettings mHost;
    protected final LockPatternUtils mLockPatternUtils;
    protected RestrictedPreference mPreference;
    protected final int mProfileChallengeUserId;
    protected final UserManager mUm;
    protected final int mUserId = UserHandle.myUserId();

    public ChangeScreenLockPreferenceController(Context context, SecuritySettings host) {
        super(context);
        this.mUm = (UserManager) context.getSystemService("user");
        this.mDPM = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mLockPatternUtils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
        this.mHost = host;
        this.mProfileChallengeUserId = Utils.getManagedProfileId(this.mUm, this.mUserId);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_unlock_set_or_change);
    }

    public String getPreferenceKey() {
        return KEY_UNLOCK_SET_OR_CHANGE;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (RestrictedPreference) screen.findPreference(getPreferenceKey());
    }

    public void updateState(Preference preference) {
        if (this.mPreference != null && (this.mPreference instanceof GearPreference)) {
            if (this.mLockPatternUtils.isSecure(this.mUserId) || !this.mLockPatternUtils.isLockScreenDisabled(this.mUserId)) {
                ((GearPreference) this.mPreference).setOnGearClickListener(this);
            } else {
                ((GearPreference) this.mPreference).setOnGearClickListener(null);
            }
        }
        updateSummary(preference, this.mUserId);
        disableIfPasswordQualityManaged(this.mUserId);
        if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId)) {
            disableIfPasswordQualityManaged(this.mProfileChallengeUserId);
        }
    }

    public void onGearClick(GearPreference p) {
        if (TextUtils.equals(p.getKey(), getPreferenceKey())) {
            new SubSettingLauncher(this.mContext).setDestination(ScreenLockSettings.class.getName()).setSourceMetricsCategory(this.mHost.getMetricsCategory()).launch();
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        if (this.mProfileChallengeUserId != -10000 && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(this.mProfileChallengeUserId) && StorageManager.isFileEncryptedNativeOnly() && Utils.startQuietModeDialogIfNecessary(this.mContext, this.mUm, this.mProfileChallengeUserId)) {
            return false;
        }
        new SubSettingLauncher(this.mContext).setDestination(ChooseLockGenericFragment.class.getName()).setTitle((int) R.string.oneplus_choose_screen_lock_method).setSourceMetricsCategory(this.mHost.getMetricsCategory()).launch();
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void updateSummary(Preference preference, int userId) {
        if (this.mLockPatternUtils.isSecure(userId)) {
            int keyguardStoredPasswordQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userId);
            if (keyguardStoredPasswordQuality == 65536) {
                preference.setSummary((int) R.string.unlock_set_unlock_mode_pattern);
            } else if (keyguardStoredPasswordQuality == 131072 || keyguardStoredPasswordQuality == 196608) {
                preference.setSummary((int) R.string.unlock_set_unlock_mode_pin);
            } else if (keyguardStoredPasswordQuality == 262144 || keyguardStoredPasswordQuality == 327680 || keyguardStoredPasswordQuality == 393216 || keyguardStoredPasswordQuality == 524288) {
                preference.setSummary((int) R.string.unlock_set_unlock_mode_password);
            }
        } else if (userId == this.mProfileChallengeUserId || this.mLockPatternUtils.isLockScreenDisabled(userId)) {
            preference.setSummary((int) R.string.unlock_set_unlock_mode_off);
        } else {
            preference.setSummary((int) R.string.unlock_set_unlock_mode_none);
        }
        this.mPreference.setEnabled(true);
    }

    /* Access modifiers changed, original: 0000 */
    public void disableIfPasswordQualityManaged(int userId) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfPasswordQualityIsSet(this.mContext, userId);
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (admin != null && dpm.getPasswordQuality(admin.component, userId) == 524288) {
            this.mPreference.setDisabledByAdmin(admin);
        }
    }
}
