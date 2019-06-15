package com.android.settings.print;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintJob;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.print.PrintManager.PrintJobStateChangeListener;
import android.print.PrintServicesLoader;
import android.printservice.PrintServiceInfo;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.utils.ProfileSettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class PrintSettingsFragment extends ProfileSettingsPreferenceFragment implements Indexable, OnClickListener {
    static final String EXTRA_CHECKED = "EXTRA_CHECKED";
    static final String EXTRA_PRINT_JOB_ID = "EXTRA_PRINT_JOB_ID";
    private static final String EXTRA_PRINT_SERVICE_COMPONENT_NAME = "EXTRA_PRINT_SERVICE_COMPONENT_NAME";
    static final String EXTRA_SERVICE_COMPONENT_NAME = "EXTRA_SERVICE_COMPONENT_NAME";
    static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final int LOADER_ID_PRINT_JOBS_LOADER = 1;
    private static final int LOADER_ID_PRINT_SERVICES = 2;
    private static final int ORDER_LAST = 2147483646;
    private static final String PRINT_JOBS_CATEGORY = "print_jobs_category";
    private static final String PRINT_SERVICES_CATEGORY = "print_services_category";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = R.xml.print_settings;
            indexables.add(indexable);
            return indexables;
        }
    };
    public static final String TAG = "PrintSettingsFragment";
    private PreferenceCategory mActivePrintJobsCategory;
    private Button mAddNewServiceButton;
    private PrintJobsController mPrintJobsController;
    private PreferenceCategory mPrintServicesCategory;
    private PrintServicesController mPrintServicesController;

    private final class PrintJobsController implements LoaderCallbacks<List<PrintJobInfo>> {
        private PrintJobsController() {
        }

        /* synthetic */ PrintJobsController(PrintSettingsFragment x0, AnonymousClass1 x1) {
            this();
        }

        public Loader<List<PrintJobInfo>> onCreateLoader(int id, Bundle args) {
            if (id == 1) {
                return new PrintJobsLoader(PrintSettingsFragment.this.getContext());
            }
            return null;
        }

        public void onLoadFinished(Loader<List<PrintJobInfo>> loader, List<PrintJobInfo> printJobs) {
            if (printJobs == null || printJobs.isEmpty()) {
                PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
            } else {
                if (PrintSettingsFragment.this.getPreferenceScreen().findPreference(PrintSettingsFragment.PRINT_JOBS_CATEGORY) == null) {
                    PrintSettingsFragment.this.getPreferenceScreen().addPreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
                }
                PrintSettingsFragment.this.mActivePrintJobsCategory.removeAll();
                Context context = PrintSettingsFragment.this.getPrefContext();
                if (context == null) {
                    Log.w(PrintSettingsFragment.TAG, "No preference context, skip adding print jobs");
                    return;
                }
                for (PrintJobInfo printJob : printJobs) {
                    Drawable icon;
                    Preference preference = new Preference(context);
                    preference.setPersistent(false);
                    preference.setFragment(PrintJobSettingsFragment.class.getName());
                    preference.setKey(printJob.getId().flattenToString());
                    int state = printJob.getState();
                    if (state != 6) {
                        switch (state) {
                            case 2:
                            case 3:
                                if (!printJob.isCancelling()) {
                                    preference.setTitle(PrintSettingsFragment.this.getString(R.string.print_printing_state_title_template, new Object[]{printJob.getLabel()}));
                                    break;
                                }
                                preference.setTitle(PrintSettingsFragment.this.getString(R.string.print_cancelling_state_title_template, new Object[]{printJob.getLabel()}));
                                break;
                            case 4:
                                if (!printJob.isCancelling()) {
                                    preference.setTitle(PrintSettingsFragment.this.getString(R.string.print_blocked_state_title_template, new Object[]{printJob.getLabel()}));
                                    break;
                                }
                                preference.setTitle(PrintSettingsFragment.this.getString(R.string.print_cancelling_state_title_template, new Object[]{printJob.getLabel()}));
                                break;
                        }
                    }
                    preference.setTitle(PrintSettingsFragment.this.getString(R.string.print_failed_state_title_template, new Object[]{printJob.getLabel()}));
                    preference.setSummary(PrintSettingsFragment.this.getString(R.string.print_job_summary, new Object[]{printJob.getPrinterName(), DateUtils.formatSameDayTime(printJob.getCreationTime(), printJob.getCreationTime(), 3, 3)}));
                    TypedArray a = PrintSettingsFragment.this.getActivity().obtainStyledAttributes(new int[]{16843817});
                    int tintColor = a.getColor(0, 0);
                    a.recycle();
                    int state2 = printJob.getState();
                    if (state2 != 6) {
                        switch (state2) {
                            case 2:
                            case 3:
                                icon = PrintSettingsFragment.this.getActivity().getDrawable(17302725);
                                icon.setTint(tintColor);
                                preference.setIcon(icon);
                                continue;
                            case 4:
                                break;
                            default:
                                break;
                        }
                    }
                    icon = PrintSettingsFragment.this.getActivity().getDrawable(17302726);
                    icon.setTint(tintColor);
                    preference.setIcon(icon);
                    preference.getExtras().putString(PrintSettingsFragment.EXTRA_PRINT_JOB_ID, printJob.getId().flattenToString());
                    PrintSettingsFragment.this.mActivePrintJobsCategory.addPreference(preference);
                }
            }
        }

        public void onLoaderReset(Loader<List<PrintJobInfo>> loader) {
            PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mActivePrintJobsCategory);
        }
    }

    private static final class PrintJobsLoader extends AsyncTaskLoader<List<PrintJobInfo>> {
        private static final boolean DEBUG = false;
        private static final String LOG_TAG = "PrintJobsLoader";
        private PrintJobStateChangeListener mPrintJobStateChangeListener;
        private List<PrintJobInfo> mPrintJobs = new ArrayList();
        private final PrintManager mPrintManager;

        public PrintJobsLoader(Context context) {
            super(context);
            this.mPrintManager = ((PrintManager) context.getSystemService("print")).getGlobalPrintManagerForUser(context.getUserId());
        }

        public void deliverResult(List<PrintJobInfo> printJobs) {
            if (isStarted()) {
                super.deliverResult(printJobs);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStartLoading() {
            if (!this.mPrintJobs.isEmpty()) {
                deliverResult(new ArrayList(this.mPrintJobs));
            }
            if (this.mPrintJobStateChangeListener == null) {
                this.mPrintJobStateChangeListener = new PrintJobStateChangeListener() {
                    public void onPrintJobStateChanged(PrintJobId printJobId) {
                        PrintJobsLoader.this.onForceLoad();
                    }
                };
                this.mPrintManager.addPrintJobStateChangeListener(this.mPrintJobStateChangeListener);
            }
            if (this.mPrintJobs.isEmpty()) {
                onForceLoad();
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStopLoading() {
            onCancelLoad();
        }

        /* Access modifiers changed, original: protected */
        public void onReset() {
            onStopLoading();
            this.mPrintJobs.clear();
            if (this.mPrintJobStateChangeListener != null) {
                this.mPrintManager.removePrintJobStateChangeListener(this.mPrintJobStateChangeListener);
                this.mPrintJobStateChangeListener = null;
            }
        }

        public List<PrintJobInfo> loadInBackground() {
            List<PrintJobInfo> printJobInfos = null;
            List<PrintJob> printJobs = this.mPrintManager.getPrintJobs();
            int printJobCount = printJobs.size();
            for (int i = 0; i < printJobCount; i++) {
                PrintJobInfo printJob = ((PrintJob) printJobs.get(i)).getInfo();
                if (PrintSettingPreferenceController.shouldShowToUser(printJob)) {
                    if (printJobInfos == null) {
                        printJobInfos = new ArrayList();
                    }
                    printJobInfos.add(printJob);
                }
            }
            return printJobInfos;
        }
    }

    private final class PrintServicesController implements LoaderCallbacks<List<PrintServiceInfo>> {
        private PrintServicesController() {
        }

        /* synthetic */ PrintServicesController(PrintSettingsFragment x0, AnonymousClass1 x1) {
            this();
        }

        public Loader<List<PrintServiceInfo>> onCreateLoader(int id, Bundle args) {
            PrintManager printManager = (PrintManager) PrintSettingsFragment.this.getContext().getSystemService("print");
            if (printManager != null) {
                return new PrintServicesLoader(printManager, PrintSettingsFragment.this.getContext(), 3);
            }
            return null;
        }

        public void onLoadFinished(Loader<List<PrintServiceInfo>> loader, List<PrintServiceInfo> services) {
            if (services.isEmpty()) {
                PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mPrintServicesCategory);
                return;
            }
            if (PrintSettingsFragment.this.getPreferenceScreen().findPreference(PrintSettingsFragment.PRINT_SERVICES_CATEGORY) == null) {
                PrintSettingsFragment.this.getPreferenceScreen().addPreference(PrintSettingsFragment.this.mPrintServicesCategory);
            }
            PrintSettingsFragment.this.mPrintServicesCategory.removeAll();
            PackageManager pm = PrintSettingsFragment.this.getActivity().getPackageManager();
            Context context = PrintSettingsFragment.this.getPrefContext();
            if (context == null) {
                Log.w(PrintSettingsFragment.TAG, "No preference context, skip adding print services");
                return;
            }
            for (PrintServiceInfo service : services) {
                Preference preference = new Preference(context);
                CharSequence title = service.getResolveInfo().loadLabel(pm).toString();
                preference.setTitle(title);
                ComponentName componentName = service.getComponentName();
                preference.setKey(componentName.flattenToString());
                preference.setFragment(PrintServiceSettingsFragment.class.getName());
                preference.setPersistent(false);
                if (service.isEnabled()) {
                    preference.setSummary(PrintSettingsFragment.this.getString(R.string.print_feature_state_on));
                } else {
                    preference.setSummary(PrintSettingsFragment.this.getString(R.string.print_feature_state_off));
                }
                if (service.getResolveInfo().loadIcon(pm) != null) {
                    preference.setIcon((int) R.drawable.ic_settings_print);
                }
                Bundle extras = preference.getExtras();
                extras.putBoolean(PrintSettingsFragment.EXTRA_CHECKED, service.isEnabled());
                extras.putString(PrintSettingsFragment.EXTRA_TITLE, title);
                extras.putString(PrintSettingsFragment.EXTRA_SERVICE_COMPONENT_NAME, componentName.flattenToString());
                PrintSettingsFragment.this.mPrintServicesCategory.addPreference(preference);
            }
            Preference addNewServicePreference = PrintSettingsFragment.this.newAddServicePreferenceOrNull();
            if (addNewServicePreference != null) {
                PrintSettingsFragment.this.mPrintServicesCategory.addPreference(addNewServicePreference);
            }
        }

        public void onLoaderReset(Loader<List<PrintServiceInfo>> loader) {
            PrintSettingsFragment.this.getPreferenceScreen().removePreference(PrintSettingsFragment.this.mPrintServicesCategory);
        }
    }

    public int getMetricsCategory() {
        return 80;
    }

    public int getHelpResource() {
        return R.string.help_uri_printing;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        addPreferencesFromResource(R.xml.print_settings);
        this.mActivePrintJobsCategory = (PreferenceCategory) findPreference(PRINT_JOBS_CATEGORY);
        this.mPrintServicesCategory = (PreferenceCategory) findPreference(PRINT_SERVICES_CATEGORY);
        getPreferenceScreen().removePreference(this.mActivePrintJobsCategory);
        this.mPrintJobsController = new PrintJobsController(this, null);
        getLoaderManager().initLoader(1, null, this.mPrintJobsController);
        this.mPrintServicesController = new PrintServicesController(this, null);
        getLoaderManager().initLoader(2, null, this.mPrintServicesController);
        return root;
    }

    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        startSubSettingsIfNeeded();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_print_state, contentRoot, false);
        ((TextView) emptyView.findViewById(R.id.message)).setText(R.string.print_no_services_installed);
        if (createAddNewServiceIntentOrNull() != null) {
            this.mAddNewServiceButton = (Button) emptyView.findViewById(R.id.add_new_service);
            this.mAddNewServiceButton.setOnClickListener(this);
            this.mAddNewServiceButton.setVisibility(0);
        }
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    /* Access modifiers changed, original: protected */
    public String getIntentActionString() {
        return "android.settings.ACTION_PRINT_SETTINGS";
    }

    private Preference newAddServicePreferenceOrNull() {
        Intent addNewServiceIntent = createAddNewServiceIntentOrNull();
        if (addNewServiceIntent == null) {
            return null;
        }
        Preference preference = new Preference(getPrefContext());
        preference.setTitle((int) R.string.print_menu_item_add_service);
        preference.setIcon((int) R.drawable.ic_menu_add);
        preference.setOrder(ORDER_LAST);
        preference.setIntent(addNewServiceIntent);
        preference.setPersistent(false);
        return preference;
    }

    private Intent createAddNewServiceIntentOrNull() {
        String searchUri = Secure.getString(getContentResolver(), "print_service_search_uri");
        if (TextUtils.isEmpty(searchUri)) {
            return null;
        }
        return new Intent("android.intent.action.VIEW", Uri.parse(searchUri));
    }

    private void startSubSettingsIfNeeded() {
        if (getArguments() != null) {
            String componentName = getArguments().getString(EXTRA_PRINT_SERVICE_COMPONENT_NAME);
            if (componentName != null) {
                getArguments().remove(EXTRA_PRINT_SERVICE_COMPONENT_NAME);
                Preference prereference = findPreference(componentName);
                if (prereference != null) {
                    prereference.performClick();
                }
            }
        }
    }

    public void onClick(View v) {
        if (this.mAddNewServiceButton == v) {
            Intent addNewServiceIntent = createAddNewServiceIntentOrNull();
            if (addNewServiceIntent != null) {
                try {
                    startActivity(addNewServiceIntent);
                } catch (ActivityNotFoundException e) {
                    Log.w(TAG, "Unable to start activity", e);
                }
            }
        }
    }
}
