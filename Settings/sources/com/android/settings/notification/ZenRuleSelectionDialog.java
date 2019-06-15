package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.EventInfo;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.utils.ZenServiceListing;
import com.android.settings.utils.ZenServiceListing.Callback;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class ZenRuleSelectionDialog extends InstrumentedDialogFragment {
    private static final boolean DEBUG = ZenModeSettings.DEBUG;
    private static final Comparator<ZenRuleInfo> RULE_TYPE_COMPARATOR = new Comparator<ZenRuleInfo>() {
        private final Collator mCollator = Collator.getInstance();

        public int compare(ZenRuleInfo lhs, ZenRuleInfo rhs) {
            int byAppName = this.mCollator.compare(lhs.packageLabel, rhs.packageLabel);
            if (byAppName != 0) {
                return byAppName;
            }
            return this.mCollator.compare(lhs.title, rhs.title);
        }
    };
    private static final String TAG = "ZenRuleSelectionDialog";
    private static Context mContext;
    private static NotificationManager mNm;
    private static PackageManager mPm;
    protected static PositiveClickListener mPositiveClickListener;
    private static ZenServiceListing mServiceListing;
    private LinearLayout mRuleContainer;
    private final Callback mServiceListingCallback = new Callback() {
        public void onServicesReloaded(Set<ServiceInfo> services) {
            if (ZenRuleSelectionDialog.DEBUG) {
                String str = ZenRuleSelectionDialog.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Services reloaded: count=");
                stringBuilder.append(services.size());
                Log.d(str, stringBuilder.toString());
            }
            Set<ZenRuleInfo> externalRuleTypes = new TreeSet(ZenRuleSelectionDialog.RULE_TYPE_COMPARATOR);
            for (ServiceInfo serviceInfo : services) {
                ZenRuleInfo ri = AbstractZenModeAutomaticRulePreferenceController.getRuleInfo(ZenRuleSelectionDialog.mPm, serviceInfo);
                if (ri != null && ri.configurationActivity != null && ZenRuleSelectionDialog.mNm.isNotificationPolicyAccessGrantedForPackage(ri.packageName) && (ri.ruleInstanceLimit <= 0 || ri.ruleInstanceLimit >= ZenRuleSelectionDialog.mNm.getRuleInstanceCount(serviceInfo.getComponentName()) + 1)) {
                    externalRuleTypes.add(ri);
                }
            }
            ZenRuleSelectionDialog.this.bindExternalRules(externalRuleTypes);
        }
    };

    private class LoadIconTask extends AsyncTask<ApplicationInfo, Void, Drawable> {
        private final WeakReference<ImageView> viewReference;

        public LoadIconTask(ImageView view) {
            this.viewReference = new WeakReference(view);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Drawable doInBackground(ApplicationInfo... params) {
            return params[0].loadIcon(ZenRuleSelectionDialog.mPm);
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Drawable icon) {
            if (icon != null) {
                ImageView view = (ImageView) this.viewReference.get();
                if (view != null) {
                    view.setImageDrawable(icon);
                }
            }
        }
    }

    public interface PositiveClickListener {
        void onExternalRuleSelected(ZenRuleInfo zenRuleInfo, Fragment fragment);

        void onSystemRuleSelected(ZenRuleInfo zenRuleInfo, Fragment fragment);
    }

    public int getMetricsCategory() {
        return 1270;
    }

    public static void show(Context context, Fragment parent, PositiveClickListener listener, ZenServiceListing serviceListing) {
        mPositiveClickListener = listener;
        mContext = context;
        mPm = mContext.getPackageManager();
        mNm = (NotificationManager) mContext.getSystemService("notification");
        mServiceListing = serviceListing;
        ZenRuleSelectionDialog dialog = new ZenRuleSelectionDialog();
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.zen_rule_type_selection, null, false);
        this.mRuleContainer = (LinearLayout) v.findViewById(R.id.rule_container);
        if (mServiceListing != null) {
            bindType(defaultNewEvent());
            bindType(defaultNewSchedule());
            mServiceListing.addZenCallback(this.mServiceListingCallback);
            mServiceListing.reloadApprovedServices();
        }
        return new Builder(getContext()).setTitle(R.string.zen_mode_choose_rule_type).setView(v).setNegativeButton(R.string.cancel, null).create();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mServiceListing != null) {
            mServiceListing.removeZenCallback(this.mServiceListingCallback);
        }
    }

    private void bindType(final ZenRuleInfo ri) {
        try {
            ApplicationInfo info = mPm.getApplicationInfo(ri.packageName, 0);
            LinearLayout v = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.zen_rule_type, null, false);
            ImageView iconView = (ImageView) v.findViewById(R.id.icon);
            ((TextView) v.findViewById(R.id.title)).setText(ri.title);
            if (!ri.isSystem) {
                new LoadIconTask(iconView).execute(new ApplicationInfo[]{info});
                TextView subtitle = (TextView) v.findViewById(R.id.subtitle);
                subtitle.setText(info.loadLabel(mPm));
                subtitle.setVisibility(0);
            } else if (ZenModeConfig.isValidScheduleConditionId(ri.defaultConditionId)) {
                iconView.setImageDrawable(mContext.getDrawable(R.drawable.ic_timelapse));
            } else if (ZenModeConfig.isValidEventConditionId(ri.defaultConditionId)) {
                iconView.setImageDrawable(mContext.getDrawable(R.drawable.ic_event));
            }
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ZenRuleSelectionDialog.this.dismiss();
                    if (ri.isSystem) {
                        ZenRuleSelectionDialog.mPositiveClickListener.onSystemRuleSelected(ri, ZenRuleSelectionDialog.this.getTargetFragment());
                    } else {
                        ZenRuleSelectionDialog.mPositiveClickListener.onExternalRuleSelected(ri, ZenRuleSelectionDialog.this.getTargetFragment());
                    }
                }
            });
            this.mRuleContainer.addView(v);
        } catch (NameNotFoundException e) {
        }
    }

    private ZenRuleInfo defaultNewSchedule() {
        ScheduleInfo schedule = new ScheduleInfo();
        schedule.days = ZenModeConfig.ALL_DAYS;
        schedule.startHour = 22;
        schedule.endHour = 7;
        ZenRuleInfo rt = new ZenRuleInfo();
        rt.settingsAction = ZenModeScheduleRuleSettings.ACTION;
        rt.title = mContext.getString(R.string.zen_schedule_rule_type_name);
        rt.packageName = ZenModeConfig.getEventConditionProvider().getPackageName();
        rt.defaultConditionId = ZenModeConfig.toScheduleConditionId(schedule);
        rt.serviceComponent = ZenModeConfig.getScheduleConditionProvider();
        rt.isSystem = true;
        return rt;
    }

    private ZenRuleInfo defaultNewEvent() {
        EventInfo event = new EventInfo();
        event.calendar = null;
        event.reply = 0;
        ZenRuleInfo rt = new ZenRuleInfo();
        rt.settingsAction = ZenModeEventRuleSettings.ACTION;
        rt.title = mContext.getString(R.string.zen_event_rule_type_name);
        rt.packageName = ZenModeConfig.getScheduleConditionProvider().getPackageName();
        rt.defaultConditionId = ZenModeConfig.toEventConditionId(event);
        rt.serviceComponent = ZenModeConfig.getEventConditionProvider();
        rt.isSystem = true;
        return rt;
    }

    private void bindExternalRules(Set<ZenRuleInfo> externalRuleTypes) {
        for (ZenRuleInfo ri : externalRuleTypes) {
            bindType(ri);
        }
    }
}
