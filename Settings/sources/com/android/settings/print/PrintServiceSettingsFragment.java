package com.android.settings.print;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.Loader;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.print.PrintManager;
import android.print.PrintServicesLoader;
import android.print.PrinterDiscoverySession;
import android.print.PrinterDiscoverySession.OnPrintersChangeListener;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrintServiceSettingsFragment extends SettingsPreferenceFragment implements OnSwitchChangeListener, LoaderCallbacks<List<PrintServiceInfo>> {
    private static final int LOADER_ID_PRINTERS_LOADER = 1;
    private static final int LOADER_ID_PRINT_SERVICE_LOADER = 2;
    private static final String LOG_TAG = "PrintServiceSettingsFragment";
    private Intent mAddPrintersIntent;
    private ComponentName mComponentName;
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        public void onChanged() {
            invalidateOptionsMenuIfNeeded();
            PrintServiceSettingsFragment.this.updateEmptyView();
        }

        public void onInvalidated() {
            invalidateOptionsMenuIfNeeded();
        }

        private void invalidateOptionsMenuIfNeeded() {
            int unfilteredItemCount = PrintServiceSettingsFragment.this.mPrintersAdapter.getUnfilteredCount();
            if ((PrintServiceSettingsFragment.this.mLastUnfilteredItemCount <= 0 && unfilteredItemCount > 0) || (PrintServiceSettingsFragment.this.mLastUnfilteredItemCount > 0 && unfilteredItemCount <= 0)) {
                PrintServiceSettingsFragment.this.getActivity().invalidateOptionsMenu();
            }
            PrintServiceSettingsFragment.this.mLastUnfilteredItemCount = unfilteredItemCount;
        }
    };
    private int mLastUnfilteredItemCount;
    private String mPreferenceKey;
    private PrintersAdapter mPrintersAdapter;
    private SearchView mSearchView;
    private boolean mServiceEnabled;
    private Intent mSettingsIntent;
    private SwitchBar mSwitchBar;
    private ToggleSwitch mToggleSwitch;

    private final class PrintersAdapter extends BaseAdapter implements LoaderCallbacks<List<PrinterInfo>>, Filterable {
        private final List<PrinterInfo> mFilteredPrinters;
        private CharSequence mLastSearchString;
        private final Object mLock;
        private final List<PrinterInfo> mPrinters;

        private PrintersAdapter() {
            this.mLock = new Object();
            this.mPrinters = new ArrayList();
            this.mFilteredPrinters = new ArrayList();
        }

        /* synthetic */ PrintersAdapter(PrintServiceSettingsFragment x0, AnonymousClass1 x1) {
            this();
        }

        public void enable() {
            PrintServiceSettingsFragment.this.getLoaderManager().initLoader(1, null, this);
        }

        public void disable() {
            PrintServiceSettingsFragment.this.getLoaderManager().destroyLoader(1);
            this.mPrinters.clear();
        }

        public int getUnfilteredCount() {
            return this.mPrinters.size();
        }

        public Filter getFilter() {
            return new Filter() {
                /* Access modifiers changed, original: protected */
                public FilterResults performFiltering(CharSequence constraint) {
                    synchronized (PrintersAdapter.this.mLock) {
                        if (TextUtils.isEmpty(constraint)) {
                            return null;
                        }
                        FilterResults results = new FilterResults();
                        List<PrinterInfo> filteredPrinters = new ArrayList();
                        String constraintLowerCase = constraint.toString().toLowerCase();
                        int printerCount = PrintersAdapter.this.mPrinters.size();
                        for (int i = 0; i < printerCount; i++) {
                            PrinterInfo printer = (PrinterInfo) PrintersAdapter.this.mPrinters.get(i);
                            String name = printer.getName();
                            if (name != null && name.toLowerCase().contains(constraintLowerCase)) {
                                filteredPrinters.add(printer);
                            }
                        }
                        results.values = filteredPrinters;
                        results.count = filteredPrinters.size();
                        return results;
                    }
                }

                /* Access modifiers changed, original: protected */
                public void publishResults(CharSequence constraint, FilterResults results) {
                    synchronized (PrintersAdapter.this.mLock) {
                        PrintersAdapter.this.mLastSearchString = constraint;
                        PrintersAdapter.this.mFilteredPrinters.clear();
                        if (results == null) {
                            PrintersAdapter.this.mFilteredPrinters.addAll(PrintersAdapter.this.mPrinters);
                        } else {
                            PrintersAdapter.this.mFilteredPrinters.addAll(results.values);
                        }
                    }
                    PrintersAdapter.this.notifyDataSetChanged();
                }
            };
        }

        public int getCount() {
            int size;
            synchronized (this.mLock) {
                size = this.mFilteredPrinters.size();
            }
            return size;
        }

        public Object getItem(int position) {
            Object obj;
            synchronized (this.mLock) {
                obj = this.mFilteredPrinters.get(position);
            }
            return obj;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean isActionable(int position) {
            return ((PrinterInfo) getItem(position)).getStatus() != 3;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = PrintServiceSettingsFragment.this.getActivity().getLayoutInflater().inflate(R.layout.printer_dropdown_item, parent, false);
            }
            convertView.setEnabled(isActionable(position));
            final PrinterInfo printer = (PrinterInfo) getItem(position);
            CharSequence title = printer.getName();
            CharSequence subtitle = printer.getDescription();
            Drawable icon = printer.loadIcon(PrintServiceSettingsFragment.this.getActivity());
            ((TextView) convertView.findViewById(R.id.title)).setText(title);
            TextView subtitleView = (TextView) convertView.findViewById(R.id.subtitle);
            if (TextUtils.isEmpty(subtitle)) {
                subtitleView.setText(null);
                subtitleView.setVisibility(8);
            } else {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(0);
            }
            LinearLayout moreInfoView = (LinearLayout) convertView.findViewById(R.id.more_info);
            if (printer.getInfoIntent() != null) {
                moreInfoView.setVisibility(0);
                moreInfoView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            PrintServiceSettingsFragment.this.getActivity().startIntentSender(printer.getInfoIntent().getIntentSender(), null, 0, 0, 0);
                        } catch (SendIntentException e) {
                            Log.e(PrintServiceSettingsFragment.LOG_TAG, "Could not execute pending info intent: %s", e);
                        }
                    }
                });
            } else {
                moreInfoView.setVisibility(8);
            }
            ImageView iconView = (ImageView) convertView.findViewById(R.id.icon);
            if (icon != null) {
                iconView.setVisibility(0);
                if (!isActionable(position)) {
                    icon.mutate();
                    TypedValue value = new TypedValue();
                    PrintServiceSettingsFragment.this.getActivity().getTheme().resolveAttribute(16842803, value, true);
                    icon.setAlpha((int) (value.getFloat() * 255.0f));
                }
                iconView.setImageDrawable(icon);
            } else {
                iconView.setVisibility(8);
            }
            return convertView;
        }

        public Loader<List<PrinterInfo>> onCreateLoader(int id, Bundle args) {
            if (id == 1) {
                return new PrintersLoader(PrintServiceSettingsFragment.this.getContext());
            }
            return null;
        }

        public void onLoadFinished(Loader<List<PrinterInfo>> loader, List<PrinterInfo> printers) {
            synchronized (this.mLock) {
                this.mPrinters.clear();
                int printerCount = printers.size();
                for (int i = 0; i < printerCount; i++) {
                    PrinterInfo printer = (PrinterInfo) printers.get(i);
                    if (printer.getId().getServiceName().equals(PrintServiceSettingsFragment.this.mComponentName)) {
                        this.mPrinters.add(printer);
                    }
                }
                this.mFilteredPrinters.clear();
                this.mFilteredPrinters.addAll(this.mPrinters);
                if (!TextUtils.isEmpty(this.mLastSearchString)) {
                    getFilter().filter(this.mLastSearchString);
                }
            }
            notifyDataSetChanged();
        }

        public void onLoaderReset(Loader<List<PrinterInfo>> loader) {
            synchronized (this.mLock) {
                this.mPrinters.clear();
                this.mFilteredPrinters.clear();
                this.mLastSearchString = null;
            }
            notifyDataSetInvalidated();
        }
    }

    private static class PrintersLoader extends Loader<List<PrinterInfo>> {
        private static final boolean DEBUG = false;
        private static final String LOG_TAG = "PrintersLoader";
        private PrinterDiscoverySession mDiscoverySession;
        private final Map<PrinterId, PrinterInfo> mPrinters = new LinkedHashMap();

        public PrintersLoader(Context context) {
            super(context);
        }

        public void deliverResult(List<PrinterInfo> printers) {
            if (isStarted()) {
                super.deliverResult(printers);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStartLoading() {
            if (!this.mPrinters.isEmpty()) {
                deliverResult(new ArrayList(this.mPrinters.values()));
            }
            onForceLoad();
        }

        /* Access modifiers changed, original: protected */
        public void onStopLoading() {
            onCancelLoad();
        }

        /* Access modifiers changed, original: protected */
        public void onForceLoad() {
            loadInternal();
        }

        /* Access modifiers changed, original: protected */
        public boolean onCancelLoad() {
            return cancelInternal();
        }

        /* Access modifiers changed, original: protected */
        public void onReset() {
            onStopLoading();
            this.mPrinters.clear();
            if (this.mDiscoverySession != null) {
                this.mDiscoverySession.destroy();
                this.mDiscoverySession = null;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onAbandon() {
            onStopLoading();
        }

        private boolean cancelInternal() {
            if (this.mDiscoverySession == null || !this.mDiscoverySession.isPrinterDiscoveryStarted()) {
                return false;
            }
            this.mDiscoverySession.stopPrinterDiscovery();
            return true;
        }

        private void loadInternal() {
            if (this.mDiscoverySession == null) {
                this.mDiscoverySession = ((PrintManager) getContext().getSystemService("print")).createPrinterDiscoverySession();
                this.mDiscoverySession.setOnPrintersChangeListener(new OnPrintersChangeListener() {
                    public void onPrintersChanged() {
                        PrintersLoader.this.deliverResult(new ArrayList(PrintersLoader.this.mDiscoverySession.getPrinters()));
                    }
                });
            }
            this.mDiscoverySession.startPrinterDiscovery(null);
        }
    }

    public int getMetricsCategory() {
        return 79;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String title = getArguments().getString("EXTRA_TITLE");
        if (!TextUtils.isEmpty(title)) {
            getActivity().setTitle(title);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        this.mServiceEnabled = getArguments().getBoolean("EXTRA_CHECKED");
        return root;
    }

    public void onStart() {
        super.onStart();
        updateEmptyView();
        updateUiForServiceState();
    }

    public void onPause() {
        if (this.mSearchView != null) {
            this.mSearchView.setOnQueryTextListener(null);
        }
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents();
        updateUiForArguments();
        getListView().setVisibility(8);
        getBackupListView().setVisibility(0);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
        this.mSwitchBar.hide();
    }

    private void onPreferenceToggled(String preferenceKey, boolean enabled) {
        ((PrintManager) getContext().getSystemService("print")).setPrintServiceEnabled(this.mComponentName, enabled);
    }

    private ListView getBackupListView() {
        return (ListView) getView().findViewById(R.id.backup_list);
    }

    private void updateEmptyView() {
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getBackupListView().getEmptyView();
        if (!this.mToggleSwitch.isChecked()) {
            if (!(emptyView == null || emptyView.getId() == R.id.empty_print_state)) {
                contentRoot.removeView(emptyView);
                emptyView = null;
            }
            if (emptyView == null) {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_print_state, contentRoot, false);
                ((ImageView) emptyView.findViewById(R.id.icon)).setContentDescription(getString(R.string.print_service_disabled));
                ((TextView) emptyView.findViewById(R.id.message)).setText(R.string.print_service_disabled);
                contentRoot.addView(emptyView);
                getBackupListView().setEmptyView(emptyView);
            }
        } else if (this.mPrintersAdapter.getUnfilteredCount() <= 0) {
            if (!(emptyView == null || emptyView.getId() == R.id.empty_printers_list_service_enabled)) {
                contentRoot.removeView(emptyView);
                emptyView = null;
            }
            if (emptyView == null) {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_printers_list_service_enabled, contentRoot, false);
                contentRoot.addView(emptyView);
                getBackupListView().setEmptyView(emptyView);
            }
        } else if (this.mPrintersAdapter.getCount() <= 0) {
            if (!(emptyView == null || emptyView.getId() == R.id.empty_print_state)) {
                contentRoot.removeView(emptyView);
                emptyView = null;
            }
            if (emptyView == null) {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_print_state, contentRoot, false);
                ((ImageView) emptyView.findViewById(R.id.icon)).setContentDescription(getString(R.string.print_no_printers_found));
                ((TextView) emptyView.findViewById(R.id.message)).setText(R.string.print_no_printers_found);
                contentRoot.addView(emptyView);
                getBackupListView().setEmptyView(emptyView);
            }
        }
    }

    private void updateUiForServiceState() {
        if (this.mServiceEnabled) {
            this.mSwitchBar.setCheckedInternal(true);
            this.mPrintersAdapter.enable();
        } else {
            this.mSwitchBar.setCheckedInternal(false);
            this.mPrintersAdapter.disable();
        }
        getActivity().invalidateOptionsMenu();
    }

    private void initComponents() {
        this.mPrintersAdapter = new PrintersAdapter(this, null);
        this.mPrintersAdapter.registerDataSetObserver(this.mDataObserver);
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBar.addOnSwitchChangeListener(this);
        this.mSwitchBar.show();
        this.mToggleSwitch = this.mSwitchBar.getSwitch();
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
            public boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean checked) {
                PrintServiceSettingsFragment.this.onPreferenceToggled(PrintServiceSettingsFragment.this.mPreferenceKey, checked);
                return false;
            }
        });
        getBackupListView().setSelector(new ColorDrawable(0));
        getBackupListView().setAdapter(this.mPrintersAdapter);
        getBackupListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PrinterInfo printer = (PrinterInfo) PrintServiceSettingsFragment.this.mPrintersAdapter.getItem(position);
                if (printer.getInfoIntent() != null) {
                    try {
                        PrintServiceSettingsFragment.this.getActivity().startIntentSender(printer.getInfoIntent().getIntentSender(), null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        Log.e(PrintServiceSettingsFragment.LOG_TAG, "Could not execute info intent: %s", e);
                    }
                }
            }
        });
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        updateEmptyView();
    }

    private void updateUiForArguments() {
        Bundle arguments = getArguments();
        this.mComponentName = ComponentName.unflattenFromString(arguments.getString("EXTRA_SERVICE_COMPONENT_NAME"));
        this.mPreferenceKey = this.mComponentName.flattenToString();
        this.mSwitchBar.setCheckedInternal(arguments.getBoolean("EXTRA_CHECKED"));
        getLoaderManager().initLoader(2, null, this);
        setHasOptionsMenu(true);
    }

    public Loader<List<PrintServiceInfo>> onCreateLoader(int id, Bundle args) {
        return new PrintServicesLoader((PrintManager) getContext().getSystemService("print"), getContext(), 3);
    }

    public void onLoadFinished(Loader<List<PrintServiceInfo>> loader, List<PrintServiceInfo> services) {
        Intent settingsIntent;
        PrintServiceInfo service = null;
        if (services != null) {
            int numServices = services.size();
            for (int i = 0; i < numServices; i++) {
                if (((PrintServiceInfo) services.get(i)).getComponentName().equals(this.mComponentName)) {
                    service = (PrintServiceInfo) services.get(i);
                    break;
                }
            }
        }
        if (service == null) {
            finishFragment();
        }
        this.mServiceEnabled = service.isEnabled();
        if (service.getSettingsActivityName() != null) {
            settingsIntent = new Intent("android.intent.action.MAIN");
            settingsIntent.setComponent(new ComponentName(service.getComponentName().getPackageName(), service.getSettingsActivityName()));
            List<ResolveInfo> resolvedActivities = getPackageManager().queryIntentActivities(settingsIntent, 0);
            if (!resolvedActivities.isEmpty() && ((ResolveInfo) resolvedActivities.get(0)).activityInfo.exported) {
                this.mSettingsIntent = settingsIntent;
            }
        } else {
            this.mSettingsIntent = null;
        }
        if (service.getAddPrintersActivityName() != null) {
            settingsIntent = new Intent("android.intent.action.MAIN");
            settingsIntent.setComponent(new ComponentName(service.getComponentName().getPackageName(), service.getAddPrintersActivityName()));
            List<ResolveInfo> resolvedActivities2 = getPackageManager().queryIntentActivities(settingsIntent, 0);
            if (!resolvedActivities2.isEmpty() && ((ResolveInfo) resolvedActivities2.get(0)).activityInfo.exported) {
                this.mAddPrintersIntent = settingsIntent;
            }
        } else {
            this.mAddPrintersIntent = null;
        }
        updateUiForServiceState();
    }

    public void onLoaderReset(Loader<List<PrintServiceInfo>> loader) {
        updateUiForServiceState();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.print_service_settings, menu);
        MenuItem addPrinters = menu.findItem(R.id.print_menu_item_add_printer);
        if (!this.mServiceEnabled || this.mAddPrintersIntent == null) {
            menu.removeItem(R.id.print_menu_item_add_printer);
        } else {
            addPrinters.setIntent(this.mAddPrintersIntent);
        }
        MenuItem settings = menu.findItem(R.id.print_menu_item_settings);
        if (!this.mServiceEnabled || this.mSettingsIntent == null) {
            menu.removeItem(R.id.print_menu_item_settings);
        } else {
            settings.setIntent(this.mSettingsIntent);
        }
        MenuItem searchItem = menu.findItem(R.id.print_menu_item_search);
        if (!this.mServiceEnabled || this.mPrintersAdapter.getUnfilteredCount() <= 0) {
            menu.removeItem(R.id.print_menu_item_search);
            return;
        }
        this.mSearchView = (SearchView) searchItem.getActionView();
        this.mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            public boolean onQueryTextChange(String searchString) {
                PrintServiceSettingsFragment.this.mPrintersAdapter.getFilter().filter(searchString);
                return true;
            }
        });
        this.mSearchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View view) {
                if (AccessibilityManager.getInstance(PrintServiceSettingsFragment.this.getActivity()).isEnabled()) {
                    view.announceForAccessibility(PrintServiceSettingsFragment.this.getString(R.string.print_search_box_shown_utterance));
                }
            }

            public void onViewDetachedFromWindow(View view) {
                Activity activity = PrintServiceSettingsFragment.this.getActivity();
                if (activity != null && !activity.isFinishing() && AccessibilityManager.getInstance(activity).isEnabled()) {
                    view.announceForAccessibility(PrintServiceSettingsFragment.this.getString(R.string.print_search_box_hidden_utterance));
                }
            }
        });
    }
}
