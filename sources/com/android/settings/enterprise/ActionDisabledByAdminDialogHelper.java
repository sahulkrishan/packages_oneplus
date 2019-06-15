package com.android.settings.enterprise;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.util.IconDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.DeviceAdminAdd;
import com.android.settings.R;
import com.android.settings.Settings.DeviceAdminSettingsActivity;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.Objects;

public class ActionDisabledByAdminDialogHelper {
    private static final String TAG = ActionDisabledByAdminDialogHelper.class.getName();
    private Activity mActivity;
    private ViewGroup mDialogView;
    private EnforcedAdmin mEnforcedAdmin;
    private String mRestriction = null;

    public ActionDisabledByAdminDialogHelper(Activity activity) {
        this.mActivity = activity;
    }

    public Builder prepareDialogBuilder(String restriction, EnforcedAdmin enforcedAdmin) {
        this.mEnforcedAdmin = enforcedAdmin;
        this.mRestriction = restriction;
        Builder builder = new Builder(this.mActivity);
        this.mDialogView = (ViewGroup) LayoutInflater.from(builder.getContext()).inflate(R.layout.admin_support_details_dialog, null);
        initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId, this.mRestriction);
        return builder.setPositiveButton(R.string.okay, null).setNeutralButton(R.string.learn_more, new -$$Lambda$ActionDisabledByAdminDialogHelper$1vfAOqcacTgM-c2XJLB5Z1-4lQ4(this)).setView(this.mDialogView);
    }

    public static /* synthetic */ void lambda$prepareDialogBuilder$0(ActionDisabledByAdminDialogHelper actionDisabledByAdminDialogHelper, DialogInterface dialog, int which) {
        actionDisabledByAdminDialogHelper.showAdminPolicies(actionDisabledByAdminDialogHelper.mEnforcedAdmin, actionDisabledByAdminDialogHelper.mActivity);
        actionDisabledByAdminDialogHelper.mActivity.finish();
    }

    public void updateDialog(String restriction, EnforcedAdmin admin) {
        if (!this.mEnforcedAdmin.equals(admin) || !Objects.equals(this.mRestriction, restriction)) {
            this.mEnforcedAdmin = admin;
            this.mRestriction = restriction;
            initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId, this.mRestriction);
        }
    }

    private void initializeDialogViews(View root, ComponentName admin, int userId, String restriction) {
        if (admin != null) {
            if (RestrictedLockUtils.isAdminInCurrentUserOrProfile(this.mActivity, admin) && RestrictedLockUtils.isCurrentUserOrProfile(this.mActivity, userId)) {
                ((ImageView) root.findViewById(R.id.admin_support_icon)).setImageDrawable(Utils.getBadgedIcon(IconDrawableFactory.newInstance(this.mActivity), this.mActivity.getPackageManager(), admin.getPackageName(), userId));
            } else {
                admin = null;
            }
            setAdminSupportTitle(root, restriction);
            setAdminSupportDetails(this.mActivity, root, new EnforcedAdmin(admin, userId));
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setAdminSupportTitle(View root, String restriction) {
        TextView titleView = (TextView) root.findViewById(R.id.admin_support_dialog_title);
        if (titleView != null) {
            if (restriction == null) {
                titleView.setText(R.string.disabled_by_policy_title);
                return;
            }
            Object obj = -1;
            switch (restriction.hashCode()) {
                case -1040305701:
                    if (restriction.equals("no_sms")) {
                        obj = 2;
                        break;
                    }
                    break;
                case -932215031:
                    if (restriction.equals("policy_disable_camera")) {
                        obj = 3;
                        break;
                    }
                    break;
                case 620339799:
                    if (restriction.equals("policy_disable_screen_capture")) {
                        obj = 4;
                        break;
                    }
                    break;
                case 1416425725:
                    if (restriction.equals("policy_suspend_packages")) {
                        obj = 6;
                        break;
                    }
                    break;
                case 1950494080:
                    if (restriction.equals("no_outgoing_calls")) {
                        obj = 1;
                        break;
                    }
                    break;
                case 2052329662:
                    if (restriction.equals("policy_mandatory_backups")) {
                        obj = 5;
                        break;
                    }
                    break;
                case 2135693260:
                    if (restriction.equals("no_adjust_volume")) {
                        obj = null;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    titleView.setText(R.string.disabled_by_policy_title_adjust_volume);
                    break;
                case 1:
                    titleView.setText(R.string.disabled_by_policy_title_outgoing_calls);
                    break;
                case 2:
                    titleView.setText(R.string.disabled_by_policy_title_sms);
                    break;
                case 3:
                    titleView.setText(R.string.disabled_by_policy_title_camera);
                    break;
                case 4:
                    titleView.setText(R.string.disabled_by_policy_title_screen_capture);
                    break;
                case 5:
                    titleView.setText(R.string.disabled_by_policy_title_turn_off_backups);
                    break;
                case 6:
                    titleView.setText(R.string.disabled_by_policy_title_suspend_packages);
                    break;
                default:
                    titleView.setText(R.string.disabled_by_policy_title);
                    break;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setAdminSupportDetails(Activity activity, View root, EnforcedAdmin enforcedAdmin) {
        if (enforcedAdmin != null && enforcedAdmin.component != null) {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService("device_policy");
            if (RestrictedLockUtils.isAdminInCurrentUserOrProfile(activity, enforcedAdmin.component) && RestrictedLockUtils.isCurrentUserOrProfile(activity, enforcedAdmin.userId)) {
                if (enforcedAdmin.userId == -10000) {
                    enforcedAdmin.userId = UserHandle.myUserId();
                }
                CharSequence supportMessage = null;
                if (UserHandle.isSameApp(Process.myUid(), 1000)) {
                    supportMessage = dpm.getShortSupportMessageForUser(enforcedAdmin.component, enforcedAdmin.userId);
                }
                if (supportMessage != null) {
                    ((TextView) root.findViewById(R.id.admin_support_msg)).setText(supportMessage);
                }
            } else {
                enforcedAdmin.component = null;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void showAdminPolicies(EnforcedAdmin enforcedAdmin, Activity activity) {
        Intent intent = new Intent();
        if (enforcedAdmin.component != null) {
            intent.setClass(activity, DeviceAdminAdd.class);
            intent.putExtra("android.app.extra.DEVICE_ADMIN", enforcedAdmin.component);
            intent.putExtra(DeviceAdminAdd.EXTRA_CALLED_FROM_SUPPORT_DIALOG, true);
            activity.startActivityAsUser(intent, new UserHandle(enforcedAdmin.userId));
            return;
        }
        intent.setClass(activity, DeviceAdminSettingsActivity.class);
        intent.addFlags(268435456);
        activity.startActivity(intent);
    }
}
