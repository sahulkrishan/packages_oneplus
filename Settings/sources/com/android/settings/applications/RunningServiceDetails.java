package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog.Builder;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.RunningProcessesView.ActiveItem;
import com.android.settings.applications.RunningProcessesView.ViewHolder;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.datetime.ZoneGetter;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;
import java.util.Collections;

public class RunningServiceDetails extends InstrumentedFragment implements OnRefreshUiListener {
    static final int DIALOG_CONFIRM_STOP = 1;
    static final String KEY_BACKGROUND = "background";
    static final String KEY_PROCESS = "process";
    static final String KEY_UID = "uid";
    static final String KEY_USER_ID = "user_id";
    static final String TAG = "RunningServicesDetails";
    final ArrayList<ActiveDetail> mActiveDetails = new ArrayList();
    ViewGroup mAllDetails;
    ActivityManager mAm;
    StringBuilder mBuilder = new StringBuilder(128);
    boolean mHaveData;
    LayoutInflater mInflater;
    MergedItem mMergedItem;
    int mNumProcesses;
    int mNumServices;
    String mProcessName;
    TextView mProcessesHeader;
    View mRootView;
    TextView mServicesHeader;
    boolean mShowBackground;
    ViewGroup mSnippet;
    ActiveItem mSnippetActiveItem;
    ViewHolder mSnippetViewHolder;
    RunningState mState;
    int mUid;
    int mUserId;

    class ActiveDetail implements OnClickListener {
        ActiveItem mActiveItem;
        ComponentName mInstaller;
        PendingIntent mManageIntent;
        Button mReportButton;
        View mRootView;
        ServiceItem mServiceItem;
        Button mStopButton;
        ViewHolder mViewHolder;

        ActiveDetail() {
        }

        /* Access modifiers changed, original: 0000 */
        public void stopActiveService(boolean confirmed) {
            ServiceItem si = this.mServiceItem;
            if (confirmed || (si.mServiceInfo.applicationInfo.flags & 1) == 0) {
                RunningServiceDetails.this.getActivity().stopService(new Intent().setComponent(si.mRunningService.service));
                if (RunningServiceDetails.this.mMergedItem == null) {
                    RunningServiceDetails.this.mState.updateNow();
                    RunningServiceDetails.this.finish();
                } else if (RunningServiceDetails.this.mShowBackground || RunningServiceDetails.this.mMergedItem.mServices.size() > 1) {
                    RunningServiceDetails.this.mState.updateNow();
                } else {
                    RunningServiceDetails.this.mState.updateNow();
                    RunningServiceDetails.this.finish();
                }
                return;
            }
            RunningServiceDetails.this.showConfirmStopDialog(si.mRunningService.service);
        }

        /* JADX WARNING: Missing block: B:20:0x00bb, code skipped:
            if (r7 == null) goto L_0x00be;
     */
        /* JADX WARNING: Missing block: B:31:0x00f5, code skipped:
            if (r1 == null) goto L_0x00f8;
     */
        public void onClick(android.view.View r13) {
            /*
            r12 = this;
            r0 = r12.mReportButton;
            r1 = 0;
            if (r13 != r0) goto L_0x0141;
        L_0x0005:
            r0 = new android.app.ApplicationErrorReport;
            r0.<init>();
            r2 = 5;
            r0.type = r2;
            r2 = r12.mServiceItem;
            r2 = r2.mServiceInfo;
            r2 = r2.packageName;
            r0.packageName = r2;
            r2 = r12.mInstaller;
            r2 = r2.getPackageName();
            r0.installerPackageName = r2;
            r2 = r12.mServiceItem;
            r2 = r2.mRunningService;
            r2 = r2.process;
            r0.processName = r2;
            r2 = java.lang.System.currentTimeMillis();
            r0.time = r2;
            r2 = r12.mServiceItem;
            r2 = r2.mServiceInfo;
            r2 = r2.applicationInfo;
            r2 = r2.flags;
            r3 = 1;
            r2 = r2 & r3;
            if (r2 == 0) goto L_0x0039;
        L_0x0037:
            r2 = r3;
            goto L_0x003a;
        L_0x0039:
            r2 = r1;
        L_0x003a:
            r0.systemApp = r2;
            r2 = new android.app.ApplicationErrorReport$RunningServiceInfo;
            r2.<init>();
            r4 = r12.mActiveItem;
            r4 = r4.mFirstRunTime;
            r6 = 0;
            r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
            if (r4 < 0) goto L_0x0057;
        L_0x004b:
            r4 = android.os.SystemClock.elapsedRealtime();
            r6 = r12.mActiveItem;
            r6 = r6.mFirstRunTime;
            r4 = r4 - r6;
            r2.durationMillis = r4;
            goto L_0x005b;
        L_0x0057:
            r4 = -1;
            r2.durationMillis = r4;
        L_0x005b:
            r4 = new android.content.ComponentName;
            r5 = r12.mServiceItem;
            r5 = r5.mServiceInfo;
            r5 = r5.packageName;
            r6 = r12.mServiceItem;
            r6 = r6.mServiceInfo;
            r6 = r6.name;
            r4.<init>(r5, r6);
            r5 = com.android.settings.applications.RunningServiceDetails.this;
            r5 = r5.getActivity();
            r6 = "service_dump.txt";
            r5 = r5.getFileStreamPath(r6);
            r6 = 0;
            r7 = r6;
            r8 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x00a4 }
            r8.<init>(r5);	 Catch:{ IOException -> 0x00a4 }
            r7 = r8;
            r8 = "activity";
            r9 = r7.getFD();	 Catch:{ IOException -> 0x00a4 }
            r10 = 3;
            r10 = new java.lang.String[r10];	 Catch:{ IOException -> 0x00a4 }
            r11 = "-a";
            r10[r1] = r11;	 Catch:{ IOException -> 0x00a4 }
            r1 = "service";
            r10[r3] = r1;	 Catch:{ IOException -> 0x00a4 }
            r1 = 2;
            r3 = r4.flattenToString();	 Catch:{ IOException -> 0x00a4 }
            r10[r1] = r3;	 Catch:{ IOException -> 0x00a4 }
            android.os.Debug.dumpService(r8, r9, r10);	 Catch:{ IOException -> 0x00a4 }
        L_0x009b:
            r7.close();	 Catch:{ IOException -> 0x009f }
            goto L_0x00be;
        L_0x009f:
            r1 = move-exception;
            goto L_0x00be;
        L_0x00a1:
            r1 = move-exception;
            goto L_0x0139;
        L_0x00a4:
            r1 = move-exception;
            r3 = "RunningServicesDetails";
            r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00a1 }
            r8.<init>();	 Catch:{ all -> 0x00a1 }
            r9 = "Can't dump service: ";
            r8.append(r9);	 Catch:{ all -> 0x00a1 }
            r8.append(r4);	 Catch:{ all -> 0x00a1 }
            r8 = r8.toString();	 Catch:{ all -> 0x00a1 }
            android.util.Log.w(r3, r8, r1);	 Catch:{ all -> 0x00a1 }
            if (r7 == 0) goto L_0x00be;
        L_0x00bd:
            goto L_0x009b;
        L_0x00be:
            r1 = r6;
            r3 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x00de }
            r3.<init>(r5);	 Catch:{ IOException -> 0x00de }
            r1 = r3;
            r8 = r5.length();	 Catch:{ IOException -> 0x00de }
            r3 = (int) r8;	 Catch:{ IOException -> 0x00de }
            r3 = new byte[r3];	 Catch:{ IOException -> 0x00de }
            r1.read(r3);	 Catch:{ IOException -> 0x00de }
            r6 = new java.lang.String;	 Catch:{ IOException -> 0x00de }
            r6.<init>(r3);	 Catch:{ IOException -> 0x00de }
            r2.serviceDetails = r6;	 Catch:{ IOException -> 0x00de }
        L_0x00d6:
            r1.close();	 Catch:{ IOException -> 0x00da }
            goto L_0x00f8;
        L_0x00da:
            r3 = move-exception;
            goto L_0x00f8;
        L_0x00dc:
            r3 = move-exception;
            goto L_0x0131;
        L_0x00de:
            r3 = move-exception;
            r6 = "RunningServicesDetails";
            r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00dc }
            r8.<init>();	 Catch:{ all -> 0x00dc }
            r9 = "Can't read service dump: ";
            r8.append(r9);	 Catch:{ all -> 0x00dc }
            r8.append(r4);	 Catch:{ all -> 0x00dc }
            r8 = r8.toString();	 Catch:{ all -> 0x00dc }
            android.util.Log.w(r6, r8, r3);	 Catch:{ all -> 0x00dc }
            if (r1 == 0) goto L_0x00f8;
        L_0x00f7:
            goto L_0x00d6;
        L_0x00f8:
            r5.delete();
            r3 = "RunningServicesDetails";
            r6 = new java.lang.StringBuilder;
            r6.<init>();
            r8 = "Details: ";
            r6.append(r8);
            r8 = r2.serviceDetails;
            r6.append(r8);
            r6 = r6.toString();
            android.util.Log.i(r3, r6);
            r0.runningServiceInfo = r2;
            r3 = new android.content.Intent;
            r6 = "android.intent.action.APP_ERROR";
            r3.<init>(r6);
            r6 = r12.mInstaller;
            r3.setComponent(r6);
            r6 = "android.intent.extra.BUG_REPORT";
            r3.putExtra(r6, r0);
            r6 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
            r3.addFlags(r6);
            r6 = com.android.settings.applications.RunningServiceDetails.this;
            r6.startActivity(r3);
            return;
        L_0x0131:
            if (r1 == 0) goto L_0x0138;
        L_0x0133:
            r1.close();	 Catch:{ IOException -> 0x0137 }
            goto L_0x0138;
        L_0x0137:
            r6 = move-exception;
        L_0x0138:
            throw r3;
        L_0x0139:
            if (r7 == 0) goto L_0x0140;
        L_0x013b:
            r7.close();	 Catch:{ IOException -> 0x013f }
            goto L_0x0140;
        L_0x013f:
            r3 = move-exception;
        L_0x0140:
            throw r1;
        L_0x0141:
            r0 = r12.mManageIntent;
            if (r0 == 0) goto L_0x0170;
        L_0x0145:
            r0 = com.android.settings.applications.RunningServiceDetails.this;	 Catch:{ SendIntentException -> 0x0169, IllegalArgumentException -> 0x0162, ActivityNotFoundException -> 0x015b }
            r1 = r0.getActivity();	 Catch:{ SendIntentException -> 0x0169, IllegalArgumentException -> 0x0162, ActivityNotFoundException -> 0x015b }
            r0 = r12.mManageIntent;	 Catch:{ SendIntentException -> 0x0169, IllegalArgumentException -> 0x0162, ActivityNotFoundException -> 0x015b }
            r2 = r0.getIntentSender();	 Catch:{ SendIntentException -> 0x0169, IllegalArgumentException -> 0x0162, ActivityNotFoundException -> 0x015b }
            r3 = 0;
            r4 = 268959744; // 0x10080000 float:2.682127E-29 double:1.328837696E-315;
            r5 = 524288; // 0x80000 float:7.34684E-40 double:2.590327E-318;
            r6 = 0;
            r1.startIntentSender(r2, r3, r4, r5, r6);	 Catch:{ SendIntentException -> 0x0169, IllegalArgumentException -> 0x0162, ActivityNotFoundException -> 0x015b }
            goto L_0x016f;
        L_0x015b:
            r0 = move-exception;
            r1 = "RunningServicesDetails";
            android.util.Log.w(r1, r0);
            goto L_0x016f;
        L_0x0162:
            r0 = move-exception;
            r1 = "RunningServicesDetails";
            android.util.Log.w(r1, r0);
            goto L_0x016f;
        L_0x0169:
            r0 = move-exception;
            r1 = "RunningServicesDetails";
            android.util.Log.w(r1, r0);
        L_0x016f:
            goto L_0x01a9;
        L_0x0170:
            r0 = r12.mServiceItem;
            if (r0 == 0) goto L_0x0178;
        L_0x0174:
            r12.stopActiveService(r1);
            goto L_0x01a9;
        L_0x0178:
            r0 = r12.mActiveItem;
            r0 = r0.mItem;
            r0 = r0.mBackground;
            if (r0 == 0) goto L_0x0195;
        L_0x0180:
            r0 = com.android.settings.applications.RunningServiceDetails.this;
            r0 = r0.mAm;
            r1 = r12.mActiveItem;
            r1 = r1.mItem;
            r1 = r1.mPackageInfo;
            r1 = r1.packageName;
            r0.killBackgroundProcesses(r1);
            r0 = com.android.settings.applications.RunningServiceDetails.this;
            r0.finish();
            goto L_0x01a9;
        L_0x0195:
            r0 = com.android.settings.applications.RunningServiceDetails.this;
            r0 = r0.mAm;
            r1 = r12.mActiveItem;
            r1 = r1.mItem;
            r1 = r1.mPackageInfo;
            r1 = r1.packageName;
            r0.forceStopPackage(r1);
            r0 = com.android.settings.applications.RunningServiceDetails.this;
            r0.finish();
        L_0x01a9:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.RunningServiceDetails$ActiveDetail.onClick(android.view.View):void");
        }
    }

    public static class MyAlertDialogFragment extends InstrumentedDialogFragment {
        public static MyAlertDialogFragment newConfirmStop(int id, ComponentName comp) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ZoneGetter.KEY_ID, id);
            args.putParcelable("comp", comp);
            frag.setArguments(args);
            return frag;
        }

        /* Access modifiers changed, original: 0000 */
        public RunningServiceDetails getOwner() {
            return (RunningServiceDetails) getTargetFragment();
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt(ZoneGetter.KEY_ID);
            if (id == 1) {
                final ComponentName comp = (ComponentName) getArguments().getParcelable("comp");
                if (getOwner().activeDetailForService(comp) == null) {
                    return null;
                }
                return new Builder(getActivity()).setTitle(getActivity().getString(R.string.runningservicedetails_stop_dlg_title)).setMessage(getActivity().getString(R.string.runningservicedetails_stop_dlg_text)).setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActiveDetail ad = MyAlertDialogFragment.this.getOwner().activeDetailForService(comp);
                        if (ad != null) {
                            ad.stopActiveService(true);
                        }
                    }
                }).setNegativeButton(R.string.dlg_cancel, null).create();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unknown id ");
            stringBuilder.append(id);
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        public int getMetricsCategory() {
            return 536;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean findMergedItem() {
        MergedItem item = null;
        ArrayList<MergedItem> newItems = this.mShowBackground ? this.mState.getCurrentBackgroundItems() : this.mState.getCurrentMergedItems();
        if (newItems != null) {
            for (int i = 0; i < newItems.size(); i++) {
                MergedItem mi = (MergedItem) newItems.get(i);
                if (mi.mUserId == this.mUserId && ((this.mUid < 0 || mi.mProcess == null || mi.mProcess.mUid == this.mUid) && (this.mProcessName == null || (mi.mProcess != null && this.mProcessName.equals(mi.mProcess.mProcessName))))) {
                    item = mi;
                    break;
                }
            }
        }
        if (this.mMergedItem == item) {
            return false;
        }
        this.mMergedItem = item;
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void addServicesHeader() {
        if (this.mNumServices == 0) {
            this.mServicesHeader = (TextView) this.mInflater.inflate(R.layout.separator_label, this.mAllDetails, false);
            this.mServicesHeader.setText(R.string.runningservicedetails_services_title);
            this.mAllDetails.addView(this.mServicesHeader);
        }
        this.mNumServices++;
    }

    /* Access modifiers changed, original: 0000 */
    public void addProcessesHeader() {
        if (this.mNumProcesses == 0) {
            this.mProcessesHeader = (TextView) this.mInflater.inflate(R.layout.separator_label, this.mAllDetails, false);
            this.mProcessesHeader.setText(R.string.runningservicedetails_processes_title);
            this.mAllDetails.addView(this.mProcessesHeader);
        }
        this.mNumProcesses++;
    }

    /* Access modifiers changed, original: 0000 */
    public void addServiceDetailsView(ServiceItem si, MergedItem mi, boolean isService, boolean inclDetails) {
        if (isService) {
            addServicesHeader();
        } else if (mi.mUserId != UserHandle.myUserId()) {
            addProcessesHeader();
        }
        BaseItem bi = si != null ? si : mi;
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(R.layout.running_service_details_service, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mServiceItem = si;
        detail.mViewHolder = new ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, bi, this.mBuilder);
        if (!inclDetails) {
            root.findViewById(R.id.service).setVisibility(8);
        }
        if (!(si == null || si.mRunningService.clientLabel == 0)) {
            detail.mManageIntent = this.mAm.getRunningServiceControlPanel(si.mRunningService.service);
        }
        TextView description = (TextView) root.findViewById(R.id.comp_description);
        detail.mStopButton = (Button) root.findViewById(R.id.left_button);
        detail.mReportButton = (Button) root.findViewById(R.id.right_button);
        if (!isService || mi.mUserId == UserHandle.myUserId()) {
            boolean z = true;
            if (si != null && si.mServiceInfo.descriptionRes != 0) {
                description.setText(getActivity().getPackageManager().getText(si.mServiceInfo.packageName, si.mServiceInfo.descriptionRes, si.mServiceInfo.applicationInfo));
            } else if (mi.mBackground) {
                description.setText(R.string.background_process_stop_description);
            } else if (detail.mManageIntent != null) {
                try {
                    String label = getActivity().getPackageManager().getResourcesForApplication(si.mRunningService.clientPackage).getString(si.mRunningService.clientLabel);
                    description.setText(getActivity().getString(R.string.service_manage_description, new Object[]{label}));
                } catch (NameNotFoundException e) {
                }
            } else {
                int i;
                Activity activity = getActivity();
                if (si != null) {
                    i = R.string.service_stop_description;
                } else {
                    i = R.string.heavy_weight_stop_description;
                }
                description.setText(activity.getText(i));
            }
            detail.mStopButton.setOnClickListener(detail);
            detail.mStopButton.setText(getActivity().getText(detail.mManageIntent != null ? R.string.service_manage : R.string.service_stop));
            detail.mReportButton.setOnClickListener(detail);
            detail.mReportButton.setText(17040815);
            if (Global.getInt(getActivity().getContentResolver(), "send_action_app_error", 0) == 0 || si == null) {
                detail.mReportButton.setEnabled(false);
            } else {
                detail.mInstaller = ApplicationErrorReport.getErrorReportReceiver(getActivity(), si.mServiceInfo.packageName, si.mServiceInfo.applicationInfo.flags);
                Button button = detail.mReportButton;
                if (detail.mInstaller == null) {
                    z = false;
                }
                button.setEnabled(z);
            }
        } else {
            description.setVisibility(8);
            root.findViewById(R.id.control_buttons_panel).setVisibility(8);
        }
        this.mActiveDetails.add(detail);
    }

    /* Access modifiers changed, original: 0000 */
    public void addProcessDetailsView(ProcessItem pi, boolean isMain) {
        addProcessesHeader();
        ActiveDetail detail = new ActiveDetail();
        View root = this.mInflater.inflate(R.layout.running_service_details_process, this.mAllDetails, false);
        this.mAllDetails.addView(root);
        detail.mRootView = root;
        detail.mViewHolder = new ViewHolder(root);
        detail.mActiveItem = detail.mViewHolder.bind(this.mState, pi, this.mBuilder);
        TextView description = (TextView) root.findViewById(R.id.comp_description);
        if (pi.mUserId != UserHandle.myUserId()) {
            description.setVisibility(8);
        } else if (isMain) {
            description.setText(R.string.main_running_process_description);
        } else {
            int textid = 0;
            CharSequence label = null;
            RunningAppProcessInfo rpi = pi.mRunningProcessInfo;
            ComponentName comp = rpi.importanceReasonComponent;
            switch (rpi.importanceReasonCode) {
                case 1:
                    textid = R.string.process_provider_in_use_description;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ProviderInfo prov = getActivity().getPackageManager().getProviderInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), prov.name, prov);
                            break;
                        } catch (NameNotFoundException e) {
                            break;
                        }
                    }
                    break;
                case 2:
                    textid = R.string.process_service_in_use_description;
                    if (rpi.importanceReasonComponent != null) {
                        try {
                            ServiceInfo serv = getActivity().getPackageManager().getServiceInfo(rpi.importanceReasonComponent, 0);
                            label = RunningState.makeLabel(getActivity().getPackageManager(), serv.name, serv);
                            break;
                        } catch (NameNotFoundException e2) {
                            break;
                        }
                    }
                    break;
            }
            if (!(textid == 0 || label == null)) {
                description.setText(getActivity().getString(textid, new Object[]{label}));
            }
        }
        this.mActiveDetails.add(detail);
    }

    /* Access modifiers changed, original: 0000 */
    public void addDetailsViews(MergedItem item, boolean inclServices, boolean inclProcesses) {
        if (item != null) {
            int i;
            boolean z = true;
            if (inclServices) {
                for (i = 0; i < item.mServices.size(); i++) {
                    addServiceDetailsView((ServiceItem) item.mServices.get(i), item, true, true);
                }
            }
            if (!inclProcesses) {
                return;
            }
            if (item.mServices.size() <= 0) {
                if (item.mUserId == UserHandle.myUserId()) {
                    z = false;
                }
                addServiceDetailsView(null, item, false, z);
                return;
            }
            i = -1;
            while (i < item.mOtherProcesses.size()) {
                ProcessItem pi;
                if (i < 0) {
                    pi = item.mProcess;
                } else {
                    pi = (ProcessItem) item.mOtherProcesses.get(i);
                }
                if (pi == null || pi.mPid > 0) {
                    addProcessDetailsView(pi, i < 0);
                }
                i++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void addDetailViews() {
        for (int i = this.mActiveDetails.size() - 1; i >= 0; i--) {
            this.mAllDetails.removeView(((ActiveDetail) this.mActiveDetails.get(i)).mRootView);
        }
        this.mActiveDetails.clear();
        if (this.mServicesHeader != null) {
            this.mAllDetails.removeView(this.mServicesHeader);
            this.mServicesHeader = null;
        }
        if (this.mProcessesHeader != null) {
            this.mAllDetails.removeView(this.mProcessesHeader);
            this.mProcessesHeader = null;
        }
        this.mNumProcesses = 0;
        this.mNumServices = 0;
        if (this.mMergedItem == null) {
            return;
        }
        if (this.mMergedItem.mUser != null) {
            ArrayList<MergedItem> items;
            int i2;
            if (this.mShowBackground) {
                items = new ArrayList(this.mMergedItem.mChildren);
                Collections.sort(items, this.mState.mBackgroundComparator);
            } else {
                items = this.mMergedItem.mChildren;
            }
            for (i2 = 0; i2 < items.size(); i2++) {
                addDetailsViews((MergedItem) items.get(i2), true, false);
            }
            for (i2 = 0; i2 < items.size(); i2++) {
                addDetailsViews((MergedItem) items.get(i2), false, true);
            }
            return;
        }
        addDetailsViews(this.mMergedItem, true, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshUi(boolean dataChanged) {
        if (findMergedItem()) {
            dataChanged = true;
        }
        if (dataChanged) {
            if (this.mMergedItem != null) {
                this.mSnippetActiveItem = this.mSnippetViewHolder.bind(this.mState, this.mMergedItem, this.mBuilder);
            } else if (this.mSnippetActiveItem != null) {
                this.mSnippetActiveItem.mHolder.size.setText("");
                this.mSnippetActiveItem.mHolder.uptime.setText("");
                this.mSnippetActiveItem.mHolder.description.setText(R.string.no_services);
            } else {
                finish();
                return;
            }
            addDetailViews();
        }
    }

    private void finish() {
        ThreadUtils.postOnMainThread(new -$$Lambda$RunningServiceDetails$YTkFZYBIB00Mbz3Oy26GxrtuRF0(this));
    }

    public static /* synthetic */ void lambda$finish$0(RunningServiceDetails runningServiceDetails) {
        Activity a = runningServiceDetails.getActivity();
        if (a != null) {
            a.onBackPressed();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mUid = getArguments().getInt("uid", -1);
        this.mUserId = getArguments().getInt("user_id", 0);
        this.mProcessName = getArguments().getString(KEY_PROCESS, null);
        this.mShowBackground = getArguments().getBoolean(KEY_BACKGROUND, false);
        this.mAm = (ActivityManager) getActivity().getSystemService("activity");
        this.mInflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
        this.mState = RunningState.getInstance(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.running_service_details, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, false);
        this.mRootView = view;
        this.mAllDetails = (ViewGroup) view.findViewById(R.id.all_details);
        this.mSnippet = (ViewGroup) view.findViewById(R.id.snippet);
        this.mSnippetViewHolder = new ViewHolder(this.mSnippet);
        ensureData();
        return view;
    }

    public void onPause() {
        super.onPause();
        this.mHaveData = false;
        this.mState.pause();
    }

    public int getMetricsCategory() {
        return 85;
    }

    public void onResume() {
        super.onResume();
        ensureData();
    }

    /* Access modifiers changed, original: 0000 */
    public ActiveDetail activeDetailForService(ComponentName comp) {
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            ActiveDetail ad = (ActiveDetail) this.mActiveDetails.get(i);
            if (ad.mServiceItem != null && ad.mServiceItem.mRunningService != null && comp.equals(ad.mServiceItem.mRunningService.service)) {
                return ad;
            }
        }
        return null;
    }

    private void showConfirmStopDialog(ComponentName comp) {
        DialogFragment newFragment = MyAlertDialogFragment.newConfirmStop(1, comp);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "confirmstop");
    }

    /* Access modifiers changed, original: 0000 */
    public void ensureData() {
        if (!this.mHaveData) {
            this.mHaveData = true;
            this.mState.resume(this);
            this.mState.waitForData();
            refreshUi(true);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateTimes() {
        if (this.mSnippetActiveItem != null) {
            this.mSnippetActiveItem.updateTime(getActivity(), this.mBuilder);
        }
        for (int i = 0; i < this.mActiveDetails.size(); i++) {
            ((ActiveDetail) this.mActiveDetails.get(i)).mActiveItem.updateTime(getActivity(), this.mBuilder);
        }
    }

    public void onRefreshUi(int what) {
        if (getActivity() != null) {
            switch (what) {
                case 0:
                    updateTimes();
                    break;
                case 1:
                    refreshUi(false);
                    updateTimes();
                    break;
                case 2:
                    refreshUi(true);
                    updateTimes();
                    break;
            }
        }
    }
}
