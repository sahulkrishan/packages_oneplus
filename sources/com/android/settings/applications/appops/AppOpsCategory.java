package com.android.settings.applications.appops;

import android.app.AppOpsManager.OpEntry;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.applications.appops.AppOpsState.AppOpEntry;
import com.android.settings.applications.appops.AppOpsState.OpsTemplate;
import java.util.List;

public class AppOpsCategory extends ListFragment implements LoaderCallbacks<List<AppOpEntry>> {
    AppListAdapter mAdapter;
    AppOpsState mState;

    public static class AppListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        List<AppOpEntry> mList;
        private final Resources mResources;
        private final AppOpsState mState;

        public AppListAdapter(Context context, AppOpsState state) {
            this.mResources = context.getResources();
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mState = state;
        }

        public void setData(List<AppOpEntry> data) {
            this.mList = data;
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.mList != null ? this.mList.size() : 0;
        }

        public AppOpEntry getItem(int position) {
            return (AppOpEntry) this.mList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            boolean z = false;
            if (convertView == null) {
                view = this.mInflater.inflate(R.layout.app_ops_item, parent, false);
            } else {
                view = convertView;
            }
            AppOpEntry item = getItem(position);
            ((ImageView) view.findViewById(R.id.app_icon)).setImageDrawable(item.getAppEntry().getIcon());
            ((TextView) view.findViewById(R.id.app_name)).setText(item.getAppEntry().getLabel());
            ((TextView) view.findViewById(R.id.op_name)).setText(item.getTimeText(this.mResources, false));
            view.findViewById(R.id.op_time).setVisibility(8);
            Switch switchR = (Switch) view.findViewById(R.id.op_switch);
            if (item.getPrimaryOpMode() == 0) {
                z = true;
            }
            switchR.setChecked(z);
            return view;
        }
    }

    public static class AppListLoader extends AsyncTaskLoader<List<AppOpEntry>> {
        List<AppOpEntry> mApps;
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        PackageIntentReceiver mPackageObserver;
        final AppOpsState mState;
        final OpsTemplate mTemplate;

        public AppListLoader(Context context, AppOpsState state, OpsTemplate template) {
            super(context);
            this.mState = state;
            this.mTemplate = template;
        }

        public List<AppOpEntry> loadInBackground() {
            return this.mState.buildState(this.mTemplate, 0, null, AppOpsState.LABEL_COMPARATOR);
        }

        public void deliverResult(List<AppOpEntry> apps) {
            if (isReset() && apps != null) {
                onReleaseResources(apps);
            }
            List<AppOpEntry> oldApps = apps;
            this.mApps = apps;
            if (isStarted()) {
                super.deliverResult(apps);
            }
            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStartLoading() {
            onContentChanged();
            if (this.mApps != null) {
                deliverResult(this.mApps);
            }
            if (this.mPackageObserver == null) {
                this.mPackageObserver = new PackageIntentReceiver(this);
            }
            boolean configChange = this.mLastConfig.applyNewConfig(getContext().getResources());
            if (takeContentChanged() || this.mApps == null || configChange) {
                forceLoad();
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStopLoading() {
            cancelLoad();
        }

        public void onCanceled(List<AppOpEntry> apps) {
            super.onCanceled(apps);
            onReleaseResources(apps);
        }

        /* Access modifiers changed, original: protected */
        public void onReset() {
            super.onReset();
            onStopLoading();
            if (this.mApps != null) {
                onReleaseResources(this.mApps);
                this.mApps = null;
            }
            if (this.mPackageObserver != null) {
                getContext().unregisterReceiver(this.mPackageObserver);
                this.mPackageObserver = null;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onReleaseResources(List<AppOpEntry> list) {
        }
    }

    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        /* Access modifiers changed, original: 0000 */
        public boolean applyNewConfig(Resources res) {
            int configChanges = this.mLastConfiguration.updateFrom(res.getConfiguration());
            if (!(this.mLastDensity != res.getDisplayMetrics().densityDpi) && (configChanges & 772) == 0) {
                return false;
            }
            this.mLastDensity = res.getDisplayMetrics().densityDpi;
            return true;
        }
    }

    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            this.mLoader = loader;
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            this.mLoader.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            this.mLoader.getContext().registerReceiver(this, sdFilter);
        }

        public void onReceive(Context context, Intent intent) {
            this.mLoader.onContentChanged();
        }
    }

    public AppOpsCategory(OpsTemplate template) {
        Bundle args = new Bundle();
        args.putParcelable("template", template);
        setArguments(args);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mState = new AppOpsState(getActivity());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No applications");
        setHasOptionsMenu(true);
        this.mAdapter = new AppListAdapter(getActivity(), this.mState);
        setListAdapter(this.mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        AppOpEntry entry = this.mAdapter.getItem(position);
        if (entry != null) {
            Switch sw = (Switch) v.findViewById(R.id.op_switch);
            int mode = 1;
            boolean checked = sw.isChecked() ^ true;
            sw.setChecked(checked);
            OpEntry op = entry.getOpEntry(0);
            if (checked) {
                mode = 0;
            }
            this.mState.getAppOpsManager().setMode(op.getOp(), entry.getAppEntry().getApplicationInfo().uid, entry.getAppEntry().getApplicationInfo().packageName, mode);
            entry.overridePrimaryOpMode(mode);
        }
    }

    public Loader<List<AppOpEntry>> onCreateLoader(int id, Bundle args) {
        Bundle fargs = getArguments();
        OpsTemplate template = null;
        if (fargs != null) {
            template = (OpsTemplate) fargs.getParcelable("template");
        }
        return new AppListLoader(getActivity(), this.mState, template);
    }

    public void onLoadFinished(Loader<List<AppOpEntry>> loader, List<AppOpEntry> data) {
        this.mAdapter.setData(data);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<List<AppOpEntry>> loader) {
        this.mAdapter.setData(null);
    }
}
