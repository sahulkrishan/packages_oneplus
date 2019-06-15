package com.android.settings.print;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.print.PrintManager.PrintJobStateChangeListener;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PrintJobSettingsFragment extends SettingsPreferenceFragment {
    private static final String EXTRA_PRINT_JOB_ID = "EXTRA_PRINT_JOB_ID";
    private static final String LOG_TAG = PrintJobSettingsFragment.class.getSimpleName();
    private static final int MENU_ITEM_ID_CANCEL = 1;
    private static final int MENU_ITEM_ID_RESTART = 2;
    private static final String PRINT_JOB_MESSAGE_PREFERENCE = "print_job_message_preference";
    private static final String PRINT_JOB_PREFERENCE = "print_job_preference";
    private Preference mMessagePreference;
    private PrintJobId mPrintJobId;
    private Preference mPrintJobPreference;
    private final PrintJobStateChangeListener mPrintJobStateChangeListener = new PrintJobStateChangeListener() {
        public void onPrintJobStateChanged(PrintJobId printJobId) {
            PrintJobSettingsFragment.this.updateUi();
        }
    };
    private PrintManager mPrintManager;

    public int getMetricsCategory() {
        return 78;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        addPreferencesFromResource(R.xml.print_job_settings);
        this.mPrintJobPreference = findPreference(PRINT_JOB_PREFERENCE);
        this.mMessagePreference = findPreference(PRINT_JOB_MESSAGE_PREFERENCE);
        this.mPrintManager = ((PrintManager) getActivity().getSystemService("print")).getGlobalPrintManagerForUser(getActivity().getUserId());
        getActivity().getActionBar().setTitle(R.string.print_print_job);
        processArguments();
        setHasOptionsMenu(true);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setEnabled(false);
    }

    public void onStart() {
        super.onStart();
        this.mPrintManager.addPrintJobStateChangeListener(this.mPrintJobStateChangeListener);
        updateUi();
    }

    public void onStop() {
        super.onStop();
        this.mPrintManager.removePrintJobStateChangeListener(this.mPrintJobStateChangeListener);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        PrintJob printJob = getPrintJob();
        if (printJob != null) {
            if (!printJob.getInfo().isCancelling()) {
                menu.add(0, 1, 0, getString(R.string.print_cancel)).setShowAsAction(1);
            }
            if (printJob.isFailed()) {
                menu.add(0, 2, 0, getString(R.string.print_restart)).setShowAsAction(1);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        PrintJob printJob = getPrintJob();
        if (printJob != null) {
            switch (item.getItemId()) {
                case 1:
                    printJob.cancel();
                    finish();
                    return true;
                case 2:
                    printJob.restart();
                    finish();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void processArguments() {
        String printJobId = getArguments().getString(EXTRA_PRINT_JOB_ID);
        if (printJobId == null) {
            printJobId = getIntent().getStringExtra(EXTRA_PRINT_JOB_ID);
            if (printJobId == null) {
                Log.w(LOG_TAG, "EXTRA_PRINT_JOB_ID not set");
                finish();
                return;
            }
        }
        this.mPrintJobId = PrintJobId.unflattenFromString(printJobId);
    }

    private PrintJob getPrintJob() {
        return this.mPrintManager.getPrintJob(this.mPrintJobId);
    }

    private void updateUi() {
        PrintJob printJob = getPrintJob();
        if (printJob == null) {
            finish();
        } else if (printJob.isCancelled() || printJob.isCompleted()) {
            finish();
        } else {
            Drawable icon;
            PrintJobInfo info = printJob.getInfo();
            int state = info.getState();
            if (state != 6) {
                switch (state) {
                    case 1:
                        this.mPrintJobPreference.setTitle(getString(R.string.print_configuring_state_title_template, new Object[]{info.getLabel()}));
                        break;
                    case 2:
                    case 3:
                        if (!printJob.getInfo().isCancelling()) {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_printing_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        }
                        this.mPrintJobPreference.setTitle(getString(R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
                        break;
                    case 4:
                        if (!printJob.getInfo().isCancelling()) {
                            this.mPrintJobPreference.setTitle(getString(R.string.print_blocked_state_title_template, new Object[]{info.getLabel()}));
                            break;
                        }
                        this.mPrintJobPreference.setTitle(getString(R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
                        break;
                }
            }
            this.mPrintJobPreference.setTitle(getString(R.string.print_failed_state_title_template, new Object[]{info.getLabel()}));
            this.mPrintJobPreference.setSummary(getString(R.string.print_job_summary, new Object[]{info.getPrinterName(), DateUtils.formatSameDayTime(info.getCreationTime(), info.getCreationTime(), 3, 3)}));
            TypedArray a = getActivity().obtainStyledAttributes(new int[]{16843817});
            int tintColor = a.getColor(0, 0);
            a.recycle();
            int state2 = info.getState();
            if (state2 != 6) {
                switch (state2) {
                    case 2:
                    case 3:
                        icon = getActivity().getDrawable(17302725);
                        icon.setTint(tintColor);
                        this.mPrintJobPreference.setIcon(icon);
                        break;
                    case 4:
                        break;
                }
            }
            icon = getActivity().getDrawable(17302726);
            icon.setTint(tintColor);
            this.mPrintJobPreference.setIcon(icon);
            CharSequence status = info.getStatus(getPackageManager());
            if (TextUtils.isEmpty(status)) {
                getPreferenceScreen().removePreference(this.mMessagePreference);
            } else {
                if (getPreferenceScreen().findPreference(PRINT_JOB_MESSAGE_PREFERENCE) == null) {
                    getPreferenceScreen().addPreference(this.mMessagePreference);
                }
                this.mMessagePreference.setSummary(status);
            }
            getActivity().invalidateOptionsMenu();
        }
    }
}
