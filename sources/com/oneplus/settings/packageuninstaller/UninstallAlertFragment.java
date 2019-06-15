package com.oneplus.settings.packageuninstaller;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedAction.Builder;
import com.android.settings.R;
import com.oneplus.settings.packageuninstaller.UninstallerActivity.DialogInfo;
import java.util.List;

public class UninstallAlertFragment extends GuidedStepFragment {
    public int onProvideTheme() {
        return R.style.f932Theme.Leanback.GuidedStep;
    }

    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        PackageManager pm = getActivity().getPackageManager();
        DialogInfo dialogInfo = ((UninstallerActivity) getActivity()).getDialogInfo();
        CharSequence appLabel = dialogInfo.appInfo.loadSafeLabel(pm);
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
            messageBuilder.append(getString(R.string.uninstall_application_text_all_users));
        } else if (dialogInfo.user.equals(Process.myUserHandle())) {
            messageBuilder.append(getString(R.string.uninstall_application_text));
        } else {
            messageBuilder.append(getString(R.string.uninstall_application_text_user, new Object[]{userManager.getUserInfo(dialogInfo.user.getIdentifier()).name}));
        }
        return new Guidance(appLabel.toString(), messageBuilder.toString(), null, dialogInfo.appInfo.loadIcon(pm));
    }

    public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(((Builder) new Builder(getContext()).clickAction(-4)).build());
        actions.add(((Builder) new Builder(getContext()).clickAction(-5)).build());
    }

    public void onGuidedActionClicked(GuidedAction action) {
        if (!isAdded()) {
            return;
        }
        if (action.getId() == -4) {
            ((UninstallerActivity) getActivity()).startUninstallProgress();
            getActivity().finish();
            return;
        }
        ((UninstallerActivity) getActivity()).dispatchAborted();
        getActivity().setResult(1);
        getActivity().finish();
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
