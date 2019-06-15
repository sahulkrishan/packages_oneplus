package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.ArrayMap;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.settings.CancellablePreference;
import com.android.settings.CancellablePreference.OnCancelListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SummaryPreference;
import com.android.settings.Utils;
import com.android.settings.applications.ProcStatsEntry.Service;
import com.android.settings.widget.EntityHeaderController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ProcessStatsDetail extends SettingsPreferenceFragment {
    public static final String EXTRA_MAX_MEMORY_USAGE = "max_memory_usage";
    public static final String EXTRA_PACKAGE_ENTRY = "package_entry";
    public static final String EXTRA_TOTAL_SCALE = "total_scale";
    public static final String EXTRA_TOTAL_TIME = "total_time";
    public static final String EXTRA_WEIGHT_TO_RAM = "weight_to_ram";
    private static final String KEY_DETAILS_HEADER = "status_header";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_MAX_USAGE = "max_usage";
    private static final String KEY_PROCS = "processes";
    public static final int MENU_FORCE_STOP = 1;
    private static final String TAG = "ProcessStatsDetail";
    static final Comparator<ProcStatsEntry> sEntryCompare = new Comparator<ProcStatsEntry>() {
        public int compare(ProcStatsEntry lhs, ProcStatsEntry rhs) {
            if (lhs.mRunWeight < rhs.mRunWeight) {
                return 1;
            }
            if (lhs.mRunWeight > rhs.mRunWeight) {
                return -1;
            }
            return 0;
        }
    };
    static final Comparator<Service> sServiceCompare = new Comparator<Service>() {
        public int compare(Service lhs, Service rhs) {
            if (lhs.mDuration < rhs.mDuration) {
                return 1;
            }
            if (lhs.mDuration > rhs.mDuration) {
                return -1;
            }
            return 0;
        }
    };
    static final Comparator<PkgService> sServicePkgCompare = new Comparator<PkgService>() {
        public int compare(PkgService lhs, PkgService rhs) {
            if (lhs.mDuration < rhs.mDuration) {
                return 1;
            }
            if (lhs.mDuration > rhs.mDuration) {
                return -1;
            }
            return 0;
        }
    };
    private ProcStatsPackageEntry mApp;
    private DevicePolicyManager mDpm;
    private MenuItem mForceStop;
    private double mMaxMemoryUsage;
    private long mOnePercentTime;
    private PackageManager mPm;
    private PreferenceCategory mProcGroup;
    private final ArrayMap<ComponentName, CancellablePreference> mServiceMap = new ArrayMap();
    private double mTotalScale;
    private long mTotalTime;
    private double mWeightToRam;

    static class PkgService {
        long mDuration;
        final ArrayList<Service> mServices = new ArrayList();

        PkgService() {
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getActivity().getPackageManager();
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        Bundle args = getArguments();
        this.mApp = (ProcStatsPackageEntry) args.getParcelable(EXTRA_PACKAGE_ENTRY);
        this.mApp.retrieveUiData(getActivity(), this.mPm);
        this.mWeightToRam = args.getDouble(EXTRA_WEIGHT_TO_RAM);
        this.mTotalTime = args.getLong(EXTRA_TOTAL_TIME);
        this.mMaxMemoryUsage = args.getDouble(EXTRA_MAX_MEMORY_USAGE);
        this.mTotalScale = args.getDouble(EXTRA_TOTAL_SCALE);
        this.mOnePercentTime = this.mTotalTime / 100;
        this.mServiceMap.clear();
        createDetails();
        setHasOptionsMenu(true);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mApp.mUiTargetApp == null) {
            finish();
            return;
        }
        Drawable badgedIcon;
        int i;
        Activity activity = getActivity();
        EntityHeaderController recyclerView = EntityHeaderController.newInstance(activity, this, null).setRecyclerView(getListView(), getLifecycle());
        if (this.mApp.mUiTargetApp != null) {
            badgedIcon = IconDrawableFactory.newInstance(activity).getBadgedIcon(this.mApp.mUiTargetApp);
        } else {
            badgedIcon = new ColorDrawable(0);
        }
        recyclerView = recyclerView.setIcon(badgedIcon).setLabel(this.mApp.mUiLabel).setPackageName(this.mApp.mPackage);
        if (this.mApp.mUiTargetApp != null) {
            i = this.mApp.mUiTargetApp.uid;
        } else {
            i = -10000;
        }
        getPreferenceScreen().addPreference(recyclerView.setUid(i).setHasAppInfoLink(true).setButtonActions(0, 0).done(activity, getPrefContext()));
    }

    public int getMetricsCategory() {
        return 21;
    }

    public void onResume() {
        super.onResume();
        checkForceStop();
        updateRunningServices();
    }

    private void updateRunningServices() {
        List<RunningServiceInfo> runningServices = ((ActivityManager) getActivity().getSystemService("activity")).getRunningServices(Integer.MAX_VALUE);
        int N = this.mServiceMap.size();
        int i = 0;
        for (int i2 = 0; i2 < N; i2++) {
            ((CancellablePreference) this.mServiceMap.valueAt(i2)).setCancellable(false);
        }
        N = runningServices.size();
        while (i < N) {
            RunningServiceInfo runningService = (RunningServiceInfo) runningServices.get(i);
            if ((runningService.started || runningService.clientLabel != 0) && (runningService.flags & 8) == 0) {
                final ComponentName service = runningService.service;
                CancellablePreference pref = (CancellablePreference) this.mServiceMap.get(service);
                if (pref != null) {
                    pref.setOnCancelListener(new OnCancelListener() {
                        public void onCancel(CancellablePreference preference) {
                            ProcessStatsDetail.this.stopService(service.getPackageName(), service.getClassName());
                        }
                    });
                    pref.setCancellable(true);
                }
            }
            i++;
        }
    }

    private void createDetails() {
        addPreferencesFromResource(R.xml.app_memory_settings);
        this.mProcGroup = (PreferenceCategory) findPreference(KEY_PROCS);
        fillProcessesSection();
        SummaryPreference summaryPreference = (SummaryPreference) findPreference(KEY_DETAILS_HEADER);
        double avgRam = ((this.mApp.mRunWeight > this.mApp.mBgWeight ? 1 : (this.mApp.mRunWeight == this.mApp.mBgWeight ? 0 : -1)) > 0 ? this.mApp.mRunWeight : this.mApp.mBgWeight) * this.mWeightToRam;
        float avgRatio = (float) (avgRam / this.mMaxMemoryUsage);
        float remainingRatio = 1.0f - avgRatio;
        Context context = getActivity();
        summaryPreference.setRatios(avgRatio, 0.0f, remainingRatio);
        BytesResult usedResult = Formatter.formatBytes(context.getResources(), (long) avgRam, 1);
        summaryPreference.setAmount(usedResult.value);
        summaryPreference.setUnits(usedResult.units);
        findPreference(KEY_FREQUENCY).setSummary(ProcStatsPackageEntry.getFrequency(((float) Math.max(this.mApp.mRunDuration, this.mApp.mBgDuration)) / ((float) this.mTotalTime), getActivity()));
        findPreference(KEY_MAX_USAGE).setSummary(Formatter.formatShortFileSize(getContext(), (long) ((((double) Math.max(this.mApp.mMaxBgMem, this.mApp.mMaxRunMem)) * this.mTotalScale) * 1024.0d)));
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mForceStop = menu.add(0, 1, 0, R.string.force_stop);
        checkForceStop();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return false;
        }
        killProcesses();
        return true;
    }

    private void fillProcessesSection() {
        int ie;
        ProcStatsEntry entry;
        this.mProcGroup.removeAll();
        ArrayList<ProcStatsEntry> entries = new ArrayList();
        for (ie = 0; ie < this.mApp.mEntries.size(); ie++) {
            entry = (ProcStatsEntry) this.mApp.mEntries.get(ie);
            if (entry.mPackage.equals(Utils.OS_PKG)) {
                entry.mLabel = entry.mName;
            } else {
                entry.mLabel = getProcessName(this.mApp.mUiLabel, entry);
            }
            entries.add(entry);
        }
        Collections.sort(entries, sEntryCompare);
        for (ie = 0; ie < entries.size(); ie++) {
            entry = (ProcStatsEntry) entries.get(ie);
            Preference processPref = new Preference(getPrefContext());
            processPref.setTitle(entry.mLabel);
            processPref.setSelectable(false);
            long duration = Math.max(entry.mRunDuration, entry.mBgDuration);
            String memoryString = Formatter.formatShortFileSize(getActivity(), Math.max((long) (entry.mRunWeight * this.mWeightToRam), (long) (entry.mBgWeight * this.mWeightToRam)));
            CharSequence frequency = ProcStatsPackageEntry.getFrequency(((float) duration) / ((float) this.mTotalTime), getActivity());
            processPref.setSummary(getString(R.string.memory_use_running_format, new Object[]{memoryString, frequency}));
            this.mProcGroup.addPreference(processPref);
        }
        if (this.mProcGroup.getPreferenceCount() < 2) {
            getPreferenceScreen().removePreference(this.mProcGroup);
        }
    }

    private static String capitalize(String processName) {
        char c = processName.charAt(0);
        if (!Character.isLowerCase(c)) {
            return processName;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Character.toUpperCase(c));
        stringBuilder.append(processName.substring(1));
        return stringBuilder.toString();
    }

    private static String getProcessName(String appLabel, ProcStatsEntry entry) {
        String processName = entry.mName;
        if (processName.contains(":")) {
            return capitalize(processName.substring(processName.lastIndexOf(58) + 1));
        }
        if (!processName.startsWith(entry.mPackage)) {
            return processName;
        }
        if (processName.length() == entry.mPackage.length()) {
            return appLabel;
        }
        int start = entry.mPackage.length();
        if (processName.charAt(start) == '.') {
            start++;
        }
        return capitalize(processName.substring(start));
    }

    private void fillServicesSection(ProcStatsEntry entry, PreferenceCategory processPref) {
        int ip;
        HashMap<String, PkgService> pkgServices = new HashMap();
        ArrayList<PkgService> pkgList = new ArrayList();
        for (ip = 0; ip < entry.mServices.size(); ip++) {
            String pkg = (String) entry.mServices.keyAt(ip);
            PkgService psvc = null;
            ArrayList<Service> services = (ArrayList) entry.mServices.valueAt(ip);
            for (int is = services.size() - 1; is >= 0; is--) {
                Service pent = (Service) services.get(is);
                if (pent.mDuration >= this.mOnePercentTime) {
                    if (psvc == null) {
                        psvc = (PkgService) pkgServices.get(pkg);
                        if (psvc == null) {
                            psvc = new PkgService();
                            pkgServices.put(pkg, psvc);
                            pkgList.add(psvc);
                        }
                    }
                    psvc.mServices.add(pent);
                    psvc.mDuration += pent.mDuration;
                }
            }
        }
        Collections.sort(pkgList, sServicePkgCompare);
        for (ip = 0; ip < pkgList.size(); ip++) {
            ArrayList<Service> services2 = ((PkgService) pkgList.get(ip)).mServices;
            Collections.sort(services2, sServiceCompare);
            for (int is2 = 0; is2 < services2.size(); is2++) {
                Service service = (Service) services2.get(is2);
                CharSequence label = getLabel(service);
                CancellablePreference servicePref = new CancellablePreference(getPrefContext());
                servicePref.setSelectable(false);
                servicePref.setTitle(label);
                servicePref.setSummary(ProcStatsPackageEntry.getFrequency(((float) service.mDuration) / ((float) this.mTotalTime), getActivity()));
                processPref.addPreference(servicePref);
                this.mServiceMap.put(new ComponentName(service.mPackage, service.mName), servicePref);
            }
        }
    }

    private CharSequence getLabel(Service service) {
        try {
            ServiceInfo serviceInfo = getPackageManager().getServiceInfo(new ComponentName(service.mPackage, service.mName), 0);
            if (serviceInfo.labelRes != 0) {
                return serviceInfo.loadLabel(getPackageManager());
            }
        } catch (NameNotFoundException e) {
        }
        String label = service.mName;
        int tail = label.lastIndexOf(46);
        if (tail >= 0 && tail < label.length() - 1) {
            label = label.substring(tail + 1);
        }
        return label;
    }

    private void stopService(String pkg, String name) {
        try {
            if ((getActivity().getPackageManager().getApplicationInfo(pkg, 0).flags & 1) != 0) {
                showStopServiceDialog(pkg, name);
            } else {
                doStopService(pkg, name);
            }
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Can't find app ");
            stringBuilder.append(pkg);
            Log.e(str, stringBuilder.toString(), e);
        }
    }

    private void showStopServiceDialog(final String pkg, final String name) {
        new Builder(getActivity()).setTitle(R.string.runningservicedetails_stop_dlg_title).setMessage(R.string.runningservicedetails_stop_dlg_text).setPositiveButton(R.string.dlg_ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ProcessStatsDetail.this.doStopService(pkg, name);
            }
        }).setNegativeButton(R.string.dlg_cancel, null).show();
    }

    private void doStopService(String pkg, String name) {
        getActivity().stopService(new Intent().setClassName(pkg, name));
        updateRunningServices();
    }

    private void killProcesses() {
        ActivityManager am = (ActivityManager) getActivity().getSystemService("activity");
        for (int i = 0; i < this.mApp.mEntries.size(); i++) {
            ProcStatsEntry ent = (ProcStatsEntry) this.mApp.mEntries.get(i);
            for (int j = 0; j < ent.mPackages.size(); j++) {
                am.forceStopPackage((String) ent.mPackages.get(j));
            }
        }
    }

    private void checkForceStop() {
        if (this.mForceStop != null) {
            if (((ProcStatsEntry) this.mApp.mEntries.get(0)).mUid < MediaPlayerGlue.FAST_FORWARD_REWIND_STEP) {
                this.mForceStop.setVisible(false);
                return;
            }
            boolean isStarted = false;
            int i = 0;
            while (i < this.mApp.mEntries.size()) {
                ProcStatsEntry ent = (ProcStatsEntry) this.mApp.mEntries.get(i);
                boolean isStarted2 = isStarted;
                for (isStarted = false; isStarted < ent.mPackages.size(); isStarted++) {
                    String pkg = (String) ent.mPackages.get(isStarted);
                    if (this.mDpm.packageHasActiveAdmins(pkg)) {
                        this.mForceStop.setEnabled(false);
                        return;
                    }
                    try {
                        if ((this.mPm.getApplicationInfo(pkg, 0).flags & 2097152) == 0) {
                            isStarted2 = true;
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
                i++;
                isStarted = isStarted2;
            }
            if (isStarted) {
                this.mForceStop.setVisible(true);
            }
        }
    }
}
