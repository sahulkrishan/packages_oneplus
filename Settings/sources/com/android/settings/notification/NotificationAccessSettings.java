package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ManagedServiceSettings.Config;
import com.android.settings.utils.ManagedServiceSettings.Config.Builder;
import com.android.settings.utils.ManagedServiceSettings.ScaryWarningDialogFragment;

public class NotificationAccessSettings extends ManagedServiceSettings {
    private static final Config CONFIG = new Builder().setTag(TAG).setSetting("enabled_notification_listeners").setIntentAction("android.service.notification.NotificationListenerService").setPermission("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE").setNoun("notification listener").setWarningDialogTitle(R.string.notification_listener_security_warning_title).setWarningDialogSummary(R.string.notification_listener_security_warning_summary).setEmptyText(R.string.no_notification_listeners).build();
    private static final String TAG = NotificationAccessSettings.class.getSimpleName();
    private NotificationManager mNm;

    public static class FriendlyWarningDialogFragment extends InstrumentedDialogFragment {
        static final String KEY_COMPONENT = "c";
        static final String KEY_LABEL = "l";

        public FriendlyWarningDialogFragment setServiceInfo(ComponentName cn, String label, Fragment target) {
            Bundle args = new Bundle();
            args.putString(KEY_COMPONENT, cn.flattenToString());
            args.putString(KEY_LABEL, label);
            setArguments(args);
            setTargetFragment(target, 0);
            return this;
        }

        public int getMetricsCategory() {
            return 552;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String label = args.getString(KEY_LABEL);
            ComponentName cn = ComponentName.unflattenFromString(args.getString(KEY_COMPONENT));
            NotificationAccessSettings parent = (NotificationAccessSettings) getTargetFragment();
            return new AlertDialog.Builder(getContext()).setMessage(getResources().getString(R.string.notification_listener_disable_warning_summary, new Object[]{label})).setCancelable(true).setPositiveButton(R.string.notification_listener_disable_warning_confirm, new -$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$ND5PkKgvmxdEIdAr9gHIhLyAwTU(parent, cn)).setNegativeButton(R.string.notification_listener_disable_warning_cancel, -$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$dxECkfkY-zLrkSsUm1OLKJMeIiE.INSTANCE).create();
        }

        static /* synthetic */ void lambda$onCreateDialog$1(DialogInterface dialog, int id) {
        }
    }

    public int getMetricsCategory() {
        return 179;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mNm = (NotificationManager) context.getSystemService(NotificationManager.class);
    }

    /* Access modifiers changed, original: protected */
    public Config getConfig() {
        return CONFIG;
    }

    /* Access modifiers changed, original: protected */
    public boolean setEnabled(ComponentName service, String title, boolean enable) {
        logSpecialPermissionChange(enable, service.getPackageName());
        if (enable) {
            if (isServiceEnabled(service)) {
                return true;
            }
            new ScaryWarningDialogFragment().setServiceInfo(service, title, this).show(getFragmentManager(), "dialog");
            return false;
        } else if (!isServiceEnabled(service)) {
            return true;
        } else {
            new FriendlyWarningDialogFragment().setServiceInfo(service, title, this).show(getFragmentManager(), "friendlydialog");
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean isServiceEnabled(ComponentName cn) {
        return this.mNm.isNotificationListenerAccessGranted(cn);
    }

    /* Access modifiers changed, original: protected */
    public void enable(ComponentName service) {
        this.mNm.setNotificationListenerAccessGranted(service, true);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.notification_access_settings;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean enable, String packageName) {
        int logCategory;
        if (enable) {
            logCategory = 776;
        } else {
            logCategory = 777;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    private static void disable(NotificationAccessSettings parent, ComponentName cn) {
        parent.mNm.setNotificationListenerAccessGranted(cn, false);
        AsyncTask.execute(new -$$Lambda$NotificationAccessSettings$5Getr2Y6VpjSaSB3qVPpmCZNr9A(parent, cn));
    }

    static /* synthetic */ void lambda$disable$0(NotificationAccessSettings parent, ComponentName cn) {
        if (!parent.mNm.isNotificationPolicyAccessGrantedForPackage(cn.getPackageName())) {
            parent.mNm.removeAutomaticZenRules(cn.getPackageName());
        }
    }
}
