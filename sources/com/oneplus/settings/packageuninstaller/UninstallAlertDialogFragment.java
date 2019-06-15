package com.oneplus.settings.packageuninstaller;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settings.R;
import com.oneplus.settings.packageuninstaller.UninstallerActivity.DialogInfo;

public class UninstallAlertDialogFragment extends DialogFragment implements OnClickListener {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        PackageManager pm = getActivity().getPackageManager();
        DialogInfo dialogInfo = ((UninstallerActivity) getActivity()).getDialogInfo();
        CharSequence appLabel = dialogInfo.appInfo.loadSafeLabel(pm);
        Builder dialogBuilder = new Builder(getActivity());
        StringBuilder messageBuilder = new StringBuilder();
        if (dialogInfo.activityInfo != null) {
            if (!dialogInfo.activityInfo.loadSafeLabel(pm).equals(appLabel)) {
                messageBuilder.append(getString(R.string.uninstall_activity_text, new Object[]{dialogInfo.activityInfo.loadSafeLabel(pm)}));
                messageBuilder.append(" ");
                messageBuilder.append(appLabel);
                messageBuilder.append(".\n\n");
            }
        }
        boolean isUpdate = (dialogInfo.appInfo.flags & 128) != 0;
        UserManager userManager = UserManager.get(getActivity());
        if (isUpdate) {
            if (isSingleUser(userManager)) {
                messageBuilder.append(getString(R.string.uninstall_update_text));
            } else {
                messageBuilder.append(getString(R.string.uninstall_update_text_multiuser));
            }
        } else if (dialogInfo.allUsers && !isSingleUser(userManager)) {
            messageBuilder.append(getString(R.string.oneplus_uninstatll_multi_app_msg));
        } else if (dialogInfo.user.equals(Process.myUserHandle())) {
            messageBuilder.append(getString(R.string.oneplus_uninstatll_multi_main_app_msg));
        } else {
            UserInfo userInfo = userManager.getUserInfo(dialogInfo.user.getIdentifier());
            UserHandle userHandle = dialogInfo.user;
            if (UserHandle.getUserId(dialogInfo.appInfo.uid) == 999) {
                messageBuilder.append(getString(R.string.oneplus_uninstatll_multi_app_msg, new Object[]{appLabel}));
                appLabel = getString(R.string.oneplus_uninstatll_multi_app_title, new Object[]{appLabel});
            } else {
                messageBuilder.append(getString(R.string.uninstall_application_text_user, new Object[]{userInfo.name}));
            }
        }
        dialogBuilder.setTitle(appLabel);
        dialogBuilder.setPositiveButton(17039370, this);
        dialogBuilder.setNegativeButton(17039360, this);
        dialogBuilder.setMessage(messageBuilder.toString());
        return dialogBuilder.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            ((UninstallerActivity) getActivity()).startUninstallProgress();
        } else {
            ((UninstallerActivity) getActivity()).dispatchAborted();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isAdded()) {
            getActivity().finish();
        }
    }

    private boolean isSingleUser(UserManager userManager) {
        int userCount = userManager.getUserCount();
        if (userCount == 1) {
            return true;
        }
        if (UserManager.isSplitSystemUser() && userCount == 2) {
            return true;
        }
        return false;
    }
}
