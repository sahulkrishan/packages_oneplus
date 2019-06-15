package com.android.settings.users;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.RestrictedLockUtils;

public class UserDetailsSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final int DIALOG_CONFIRM_ENABLE_CALLING = 2;
    private static final int DIALOG_CONFIRM_ENABLE_CALLING_AND_SMS = 3;
    private static final int DIALOG_CONFIRM_REMOVE = 1;
    static final String EXTRA_USER_GUEST = "guest_user";
    static final String EXTRA_USER_ID = "user_id";
    private static final String KEY_ENABLE_TELEPHONY = "enable_calling";
    private static final String KEY_REMOVE_USER = "remove_user";
    private static final String TAG = UserDetailsSettings.class.getSimpleName();
    private Bundle mDefaultGuestRestrictions;
    private boolean mGuestUser;
    private SwitchPreference mPhonePref;
    private Preference mRemoveUserPref;
    private UserInfo mUserInfo;
    private UserManager mUserManager;

    public int getMetricsCategory() {
        return 98;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.mUserManager = (UserManager) context.getSystemService("user");
        addPreferencesFromResource(R.xml.user_details_settings);
        this.mPhonePref = (SwitchPreference) findPreference(KEY_ENABLE_TELEPHONY);
        this.mRemoveUserPref = findPreference(KEY_REMOVE_USER);
        this.mGuestUser = getArguments().getBoolean(EXTRA_USER_GUEST, false);
        if (this.mGuestUser) {
            removePreference(KEY_REMOVE_USER);
            this.mPhonePref.setTitle((int) R.string.user_enable_calling);
            this.mDefaultGuestRestrictions = this.mUserManager.getDefaultGuestRestrictions();
            this.mPhonePref.setChecked(this.mDefaultGuestRestrictions.getBoolean("no_outgoing_calls") ^ 1);
        } else {
            int userId = getArguments().getInt("user_id", -1);
            if (userId != -1) {
                this.mUserInfo = this.mUserManager.getUserInfo(userId);
                this.mPhonePref.setChecked(this.mUserManager.hasUserRestriction("no_outgoing_calls", new UserHandle(userId)) ^ 1);
                this.mRemoveUserPref.setOnPreferenceClickListener(this);
            } else {
                throw new RuntimeException("Arguments to this fragment must contain the user id");
            }
        }
        if (RestrictedLockUtils.hasBaseUserRestriction(context, "no_remove_user", UserHandle.myUserId())) {
            removePreference(KEY_REMOVE_USER);
        }
        this.mPhonePref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mRemoveUserPref) {
            return false;
        }
        if (this.mUserManager.isAdminUser()) {
            showDialog(1);
            return true;
        }
        throw new RuntimeException("Only admins can remove a user");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Boolean.TRUE.equals(newValue)) {
            int i;
            if (this.mGuestUser) {
                i = 2;
            } else {
                i = 3;
            }
            showDialog(i);
            return false;
        }
        enableCallsAndSms(false);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void enableCallsAndSms(boolean enabled) {
        this.mPhonePref.setChecked(enabled);
        if (this.mGuestUser) {
            this.mDefaultGuestRestrictions.putBoolean("no_outgoing_calls", enabled ^ 1);
            this.mDefaultGuestRestrictions.putBoolean("no_sms", true);
            this.mUserManager.setDefaultGuestRestrictions(this.mDefaultGuestRestrictions);
            for (UserInfo user : this.mUserManager.getUsers(true)) {
                if (user.isGuest()) {
                    UserHandle userHandle = UserHandle.of(user.id);
                    for (String key : this.mDefaultGuestRestrictions.keySet()) {
                        this.mUserManager.setUserRestriction(key, this.mDefaultGuestRestrictions.getBoolean(key), userHandle);
                    }
                }
            }
            return;
        }
        UserHandle userHandle2 = UserHandle.of(this.mUserInfo.id);
        this.mUserManager.setUserRestriction("no_outgoing_calls", enabled ^ 1, userHandle2);
        this.mUserManager.setUserRestriction("no_sms", enabled ^ 1, userHandle2);
    }

    public Dialog onCreateDialog(int dialogId) {
        if (getActivity() == null) {
            return null;
        }
        switch (dialogId) {
            case 1:
                return UserDialogs.createRemoveDialog(getActivity(), this.mUserInfo.id, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.removeUser();
                    }
                });
            case 2:
                return UserDialogs.createEnablePhoneCallsDialog(getActivity(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.enableCallsAndSms(true);
                    }
                });
            case 3:
                return UserDialogs.createEnablePhoneCallsAndSmsDialog(getActivity(), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserDetailsSettings.this.enableCallsAndSms(true);
                    }
                });
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unsupported dialogId ");
                stringBuilder.append(dialogId);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case 1:
                return 591;
            case 2:
                return 592;
            case 3:
                return 593;
            default:
                return 0;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removeUser() {
        this.mUserManager.removeUser(this.mUserInfo.id);
        finishFragment();
    }
}
