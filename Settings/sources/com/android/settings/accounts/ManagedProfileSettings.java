package com.android.settings.accounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.oneplus.settings.ui.OPRestrictedSwitchPreference;

public class ManagedProfileSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String KEY_CONTACT = "contacts_search";
    private static final String KEY_WORK_MODE = "work_mode";
    private static final String TAG = "ManagedProfileSettings";
    private OPRestrictedSwitchPreference mContactPrefrence;
    private Context mContext;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver;
    private UserHandle mManagedUser;
    private UserManager mUserManager;
    private SwitchPreference mWorkModePreference;

    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        private ManagedProfileBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = ManagedProfileSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Received broadcast: ");
            stringBuilder.append(action);
            Log.v(str, stringBuilder.toString());
            if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.getActivity().finish();
                }
            } else if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == ManagedProfileSettings.this.mManagedUser.getIdentifier()) {
                    ManagedProfileSettings.this.updateWorkModePreference();
                }
            } else {
                str = ManagedProfileSettings.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot handle received broadcast: ");
                stringBuilder.append(intent.getAction());
                Log.w(str, stringBuilder.toString());
            }
        }

        public void register(Context context) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
            intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
            context.registerReceiver(this, intentFilter);
        }

        public void unregister(Context context) {
            context.unregisterReceiver(this);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.managed_profile_settings);
        this.mWorkModePreference = (SwitchPreference) findPreference(KEY_WORK_MODE);
        this.mWorkModePreference.setOnPreferenceChangeListener(this);
        this.mContactPrefrence = (OPRestrictedSwitchPreference) findPreference(KEY_CONTACT);
        this.mContactPrefrence.setOnPreferenceChangeListener(this);
        this.mContext = getActivity().getApplicationContext();
        this.mUserManager = (UserManager) getSystemService("user");
        this.mManagedUser = getManagedUserFromArgument();
        if (this.mManagedUser == null) {
            getActivity().finish();
        }
        this.mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver();
        this.mManagedProfileBroadcastReceiver.register(getActivity());
    }

    public void onResume() {
        super.onResume();
        loadDataAndPopulateUi();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mManagedProfileBroadcastReceiver.unregister(getActivity());
    }

    private UserHandle getManagedUserFromArgument() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            UserHandle userHandle = (UserHandle) arguments.getParcelable("android.intent.extra.USER");
            if (userHandle != null && this.mUserManager.isManagedProfile(userHandle.getIdentifier())) {
                return userHandle;
            }
        }
        return Utils.getManagedProfile(this.mUserManager);
    }

    private void loadDataAndPopulateUi() {
        if (this.mWorkModePreference != null) {
            updateWorkModePreference();
        }
        if (this.mContactPrefrence != null) {
            boolean z = false;
            int value = Secure.getIntForUser(getContentResolver(), "managed_profile_contact_remote_search", 0, this.mManagedUser.getIdentifier());
            OPRestrictedSwitchPreference oPRestrictedSwitchPreference = this.mContactPrefrence;
            if (value != 0) {
                z = true;
            }
            oPRestrictedSwitchPreference.setChecked(z);
            this.mContactPrefrence.setDisabledByAdmin(RestrictedLockUtils.checkIfRemoteContactSearchDisallowed(this.mContext, this.mManagedUser.getIdentifier()));
        }
    }

    public int getMetricsCategory() {
        return 401;
    }

    private void updateWorkModePreference() {
        int i;
        boolean isWorkModeOn = this.mUserManager.isQuietModeEnabled(this.mManagedUser) ^ 1;
        this.mWorkModePreference.setChecked(isWorkModeOn);
        SwitchPreference switchPreference = this.mWorkModePreference;
        if (isWorkModeOn) {
            i = R.string.work_mode_on_summary;
        } else {
            i = R.string.work_mode_off_summary;
        }
        switchPreference.setSummary(i);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mWorkModePreference) {
            this.mUserManager.requestQuietModeEnabled(((Boolean) newValue).booleanValue() ^ true, this.mManagedUser);
            return true;
        }
        boolean z = false;
        if (preference != this.mContactPrefrence) {
            return false;
        }
        if (((Boolean) newValue).booleanValue()) {
            z = true;
        }
        Secure.putIntForUser(getContentResolver(), "managed_profile_contact_remote_search", z, this.mManagedUser.getIdentifier());
        return true;
    }
}
