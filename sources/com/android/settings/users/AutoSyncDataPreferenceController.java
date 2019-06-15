package com.android.settings.users;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.core.AbstractPreferenceController;

public class AutoSyncDataPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_AUTO_SYNC_ACCOUNT = "auto_sync_account_data";
    private static final String TAG = "AutoSyncDataController";
    private static final String TAG_CONFIRM_AUTO_SYNC_CHANGE = "confirmAutoSyncChange";
    private final Fragment mParentFragment;
    protected UserHandle mUserHandle = Process.myUserHandle();
    protected final UserManager mUserManager;

    public static class ConfirmAutoSyncChangeFragment extends InstrumentedDialogFragment implements OnClickListener {
        private static final String SAVE_ENABLING = "enabling";
        private static final String SAVE_USER_HANDLE = "userHandle";
        boolean mEnabling;
        SwitchPreference mPreference;
        UserHandle mUserHandle;

        public static void show(Fragment parent, boolean enabling, UserHandle userHandle, SwitchPreference preference) {
            if (parent.isAdded()) {
                ConfirmAutoSyncChangeFragment dialog = new ConfirmAutoSyncChangeFragment();
                dialog.mEnabling = enabling;
                dialog.mUserHandle = userHandle;
                dialog.setTargetFragment(parent, 0);
                dialog.mPreference = preference;
                dialog.show(parent.getFragmentManager(), AutoSyncDataPreferenceController.TAG_CONFIRM_AUTO_SYNC_CHANGE);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            if (savedInstanceState != null) {
                this.mEnabling = savedInstanceState.getBoolean(SAVE_ENABLING);
                this.mUserHandle = (UserHandle) savedInstanceState.getParcelable(SAVE_USER_HANDLE);
            }
            Builder builder = new Builder(context);
            if (this.mEnabling) {
                builder.setTitle(R.string.data_usage_auto_sync_on_dialog_title);
                builder.setMessage(R.string.data_usage_auto_sync_on_dialog);
            } else {
                builder.setTitle(R.string.data_usage_auto_sync_off_dialog_title);
                builder.setMessage(R.string.data_usage_auto_sync_off_dialog);
            }
            builder.setPositiveButton(17039370, this);
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(SAVE_ENABLING, this.mEnabling);
            outState.putParcelable(SAVE_USER_HANDLE, this.mUserHandle);
        }

        public int getMetricsCategory() {
            return 535;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                ContentResolver.setMasterSyncAutomaticallyAsUser(this.mEnabling, Process.myUserHandle().getIdentifier());
                if (this.mPreference != null) {
                    this.mPreference.setChecked(this.mEnabling);
                }
            }
        }
    }

    public AutoSyncDataPreferenceController(Context context, Fragment parent) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mParentFragment = parent;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) preference).setChecked(ContentResolver.getMasterSyncAutomaticallyAsUser(Process.myUserHandle().getIdentifier()));
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return false;
        }
        SwitchPreference switchPreference = (SwitchPreference) preference;
        boolean checked = switchPreference.isChecked();
        switchPreference.setChecked(checked ^ 1);
        if (ActivityManager.isUserAMonkey()) {
            Log.d(TAG, "ignoring monkey's attempt to flip sync state");
        } else {
            ConfirmAutoSyncChangeFragment.show(this.mParentFragment, checked, this.mUserHandle, switchPreference);
        }
        return true;
    }

    public boolean isAvailable() {
        if (this.mUserManager.isManagedProfile() || (!this.mUserManager.isRestrictedProfile() && this.mUserManager.getProfiles(UserHandle.myUserId()).size() != 1)) {
            return false;
        }
        return true;
    }

    public String getPreferenceKey() {
        return KEY_AUTO_SYNC_ACCOUNT;
    }
}
