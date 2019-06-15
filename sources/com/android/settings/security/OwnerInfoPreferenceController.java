package com.android.settings.security;

import android.app.Fragment;
import android.content.Context;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.users.OwnerInfoSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class OwnerInfoPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume {
    private static final String KEY_OWNER_INFO = "owner_info_settings";
    private static final int MY_USER_ID = UserHandle.myUserId();
    private final LockPatternUtils mLockPatternUtils;
    private RestrictedPreference mOwnerInfoPref;
    private final Fragment mParent;

    public interface OwnerInfoCallback {
        void onOwnerInfoUpdated();
    }

    public OwnerInfoPreferenceController(Context context, Fragment parent, Lifecycle lifecycle) {
        super(context);
        this.mParent = parent;
        this.mLockPatternUtils = new LockPatternUtils(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mOwnerInfoPref = (RestrictedPreference) screen.findPreference(KEY_OWNER_INFO);
    }

    public void onResume() {
        updateEnableState();
        updateSummary();
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_OWNER_INFO;
    }

    public void updateEnableState() {
        if (this.mOwnerInfoPref != null) {
            if (isDeviceOwnerInfoEnabled()) {
                this.mOwnerInfoPref.setDisabledByAdmin(getDeviceOwner());
            } else {
                this.mOwnerInfoPref.setDisabledByAdmin(null);
                this.mOwnerInfoPref.setEnabled(this.mLockPatternUtils.isLockScreenDisabled(MY_USER_ID) ^ 1);
                if (this.mOwnerInfoPref.isEnabled()) {
                    this.mOwnerInfoPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            OwnerInfoSettings.show(OwnerInfoPreferenceController.this.mParent);
                            return true;
                        }
                    });
                }
            }
        }
    }

    public void updateSummary() {
        if (this.mOwnerInfoPref == null) {
            return;
        }
        if (isDeviceOwnerInfoEnabled()) {
            this.mOwnerInfoPref.setSummary((CharSequence) getDeviceOwnerInfo());
            return;
        }
        CharSequence ownerInfo;
        RestrictedPreference restrictedPreference = this.mOwnerInfoPref;
        if (isOwnerInfoEnabled()) {
            ownerInfo = getOwnerInfo();
        } else {
            ownerInfo = this.mContext.getString(R.string.owner_info_settings_summary);
        }
        restrictedPreference.setSummary(ownerInfo);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isDeviceOwnerInfoEnabled() {
        return this.mLockPatternUtils.isDeviceOwnerInfoEnabled();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getDeviceOwnerInfo() {
        return this.mLockPatternUtils.getDeviceOwnerInfo();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isOwnerInfoEnabled() {
        return this.mLockPatternUtils.isOwnerInfoEnabled(MY_USER_ID);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getOwnerInfo() {
        return this.mLockPatternUtils.getOwnerInfo(MY_USER_ID);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public EnforcedAdmin getDeviceOwner() {
        return RestrictedLockUtils.getDeviceOwner(this.mContext);
    }
}
