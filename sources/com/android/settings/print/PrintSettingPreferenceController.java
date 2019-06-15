package com.android.settings.print;

import android.content.Context;
import android.content.pm.PackageManager;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.print.PrintManager.PrintJobStateChangeListener;
import android.printservice.PrintServiceInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.util.List;

public class PrintSettingPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop, PrintJobStateChangeListener {
    private static final String KEY_PRINTING_SETTINGS = "connected_device_printing";
    private final PackageManager mPackageManager;
    private Preference mPreference;
    private final PrintManager mPrintManager;

    public PrintSettingPreferenceController(Context context) {
        super(context, KEY_PRINTING_SETTINGS);
        this.mPackageManager = context.getPackageManager();
        this.mPrintManager = ((PrintManager) context.getSystemService("print")).getGlobalPrintManagerForUser(context.getUserId());
    }

    public int getAvailabilityStatus() {
        return this.mPackageManager.hasSystemFeature("android.software.print") ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public void onStart() {
        this.mPrintManager.addPrintJobStateChangeListener(this);
    }

    public void onStop() {
        this.mPrintManager.removePrintJobStateChangeListener(this);
    }

    public void onPrintJobStateChanged(PrintJobId printJobId) {
        updateState(this.mPreference);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ((RestrictedPreference) preference).checkRestrictionAndSetDisabled("no_printing");
    }

    public CharSequence getSummary() {
        List<PrintJob> printJobs = this.mPrintManager.getPrintJobs();
        int numActivePrintJobs = 0;
        if (printJobs != null) {
            for (PrintJob job : printJobs) {
                if (shouldShowToUser(job.getInfo())) {
                    numActivePrintJobs++;
                }
            }
        }
        if (numActivePrintJobs > 0) {
            return this.mContext.getResources().getQuantityString(R.plurals.print_jobs_summary, numActivePrintJobs, new Object[]{Integer.valueOf(numActivePrintJobs)});
        }
        List<PrintServiceInfo> services = this.mPrintManager.getPrintServices(1);
        if (services == null || services.isEmpty()) {
            return this.mContext.getText(R.string.print_settings_summary_no_service);
        }
        int count = services.size();
        return this.mContext.getResources().getQuantityString(R.plurals.print_settings_summary, count, new Object[]{Integer.valueOf(count)});
    }

    static boolean shouldShowToUser(PrintJobInfo printJob) {
        int state = printJob.getState();
        if (state != 6) {
            switch (state) {
                case 2:
                case 3:
                case 4:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}
