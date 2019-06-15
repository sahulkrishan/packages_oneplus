package com.android.settings.datausage;

import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.datausage.CellDataPreference.DataStateListener;
import com.android.settings.datausage.CycleAdapter.CycleItem;
import com.android.settings.datausage.CycleAdapter.SpinnerInterface;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.AppItem;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;
import com.android.settingslib.net.SummaryForAllUidLoader;
import com.android.settingslib.net.UidDetail;
import com.android.settingslib.net.UidDetailProvider;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DataUsageList extends DataUsageBase {
    public static final String EXTRA_NETWORK_TEMPLATE = "network_template";
    public static final String EXTRA_SUB_ID = "sub_id";
    private static final String KEY_APPS_GROUP = "apps_group";
    private static final String KEY_CHART_DATA = "chart_data";
    private static final String KEY_USAGE_AMOUNT = "usage_amount";
    private static final int LOADER_CHART_DATA = 2;
    private static final int LOADER_SUMMARY = 3;
    private static final boolean LOGD = false;
    private static final String TAG = "DataUsage";
    private PreferenceGroup mApps;
    private ChartDataUsagePreference mChart;
    private ChartData mChartData;
    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(DataUsageList.this.getActivity(), DataUsageList.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            DataUsageList.this.mLoadingViewController.showContent(false);
            DataUsageList.this.mChartData = data;
            DataUsageList.this.mChart.setNetworkStats(DataUsageList.this.mChartData.network);
            DataUsageList.this.updatePolicy();
        }

        public void onLoaderReset(Loader<ChartData> loader) {
            DataUsageList.this.mChartData = null;
            DataUsageList.this.mChart.setNetworkStats(null);
        }
    };
    private CycleAdapter mCycleAdapter;
    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            long[] section = OPDataUsageUtils.getDataUsageSectionTimeMillByAccountDay(DataUsageList.this.getPrefContext(), DataUsageList.this.mSubId);
            CycleItem cycle = new CycleItem(DataUsageList.this.getPrefContext(), section[0], section[1]);
            DataUsageList.this.mChart.setVisibleRange(cycle.start, cycle.end);
            DataUsageList.this.updateDetailData();
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private Spinner mCycleSpinner;
    private final DataStateListener mDataStateListener = new DataStateListener() {
        public void onChange(boolean selfChange) {
            DataUsageList.this.updatePolicy();
        }
    };
    private View mHeader;
    private LoadingViewController mLoadingViewController;
    private INetworkStatsSession mStatsSession;
    @VisibleForTesting
    int mSubId = -1;
    private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(DataUsageList.this.getActivity(), DataUsageList.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
            DataUsageList.this.bindStats(data, DataUsageList.this.services.mPolicyManager.getUidsWithPolicy(1));
            updateEmptyVisible();
        }

        public void onLoaderReset(Loader<NetworkStats> loader) {
            DataUsageList.this.bindStats(null, new int[0]);
            updateEmptyVisible();
        }

        private void updateEmptyVisible() {
            Object obj = null;
            Object obj2 = DataUsageList.this.mApps.getPreferenceCount() != 0 ? 1 : null;
            if (DataUsageList.this.getPreferenceScreen().getPreferenceCount() != 0) {
                obj = 1;
            }
            if (obj2 == obj) {
                return;
            }
            if (DataUsageList.this.mApps.getPreferenceCount() != 0) {
                DataUsageList.this.getPreferenceScreen().addPreference(DataUsageList.this.mUsageAmount);
                DataUsageList.this.getPreferenceScreen().addPreference(DataUsageList.this.mApps);
                return;
            }
            DataUsageList.this.getPreferenceScreen().removeAll();
        }
    };
    @VisibleForTesting
    NetworkTemplate mTemplate;
    private UidDetailProvider mUidDetailProvider;
    private Preference mUsageAmount;
    private TextView tv_filter_datetime;

    public int getMetricsCategory() {
        return 341;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        if (!isBandwidthControlEnabled()) {
            Log.w(TAG, "No bandwidth control; leaving");
            getActivity().finish();
        }
        try {
            this.mStatsSession = this.services.mStatsService.openSession();
            this.mUidDetailProvider = new UidDetailProvider(context);
            addPreferencesFromResource(R.xml.data_usage_list);
            this.mUsageAmount = findPreference(KEY_USAGE_AMOUNT);
            this.mChart = (ChartDataUsagePreference) findPreference(KEY_CHART_DATA);
            this.mApps = (PreferenceGroup) findPreference(KEY_APPS_GROUP);
            processArgument();
            this.mChart.setShowWifi(this.mTemplate.isMatchRuleMobile() ^ 1);
            this.mChart.setSubId(this.mSubId);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        this.mHeader = setPinnedHeaderView((int) R.layout.apps_filter_spinner);
        this.mHeader.findViewById(R.id.filter_settings).setOnClickListener(new -$$Lambda$DataUsageList$YolaBauY8HvHsYGl5vfnCCKHiAQ(this));
        this.mCycleSpinner = (Spinner) this.mHeader.findViewById(R.id.filter_spinner);
        this.mCycleSpinner.setVisibility(8);
        this.tv_filter_datetime = (TextView) this.mHeader.findViewById(R.id.tv_filter_datetime);
        long[] section = OPDataUsageUtils.getDataUsageSectionTimeMillByAccountDay(getPrefContext(), this.mSubId);
        this.mChart.setVisibleRange(section[0], section[1]);
        this.tv_filter_datetime.setText(Utils.formatDateRange(getPrefContext(), section[0], section[1]));
        ((ImageView) this.mHeader.findViewById(R.id.filter_settings)).setVisibility(8);
        this.mCycleAdapter = new CycleAdapter(this.mCycleSpinner.getContext(), new SpinnerInterface() {
            public void setAdapter(CycleAdapter cycleAdapter) {
                DataUsageList.this.mCycleSpinner.setAdapter(cycleAdapter);
            }

            public void setOnItemSelectedListener(OnItemSelectedListener listener) {
                DataUsageList.this.mCycleSpinner.setOnItemSelectedListener(listener);
            }

            public Object getSelectedItem() {
                return DataUsageList.this.mCycleSpinner.getSelectedItem();
            }

            public void setSelection(int position) {
                DataUsageList.this.mCycleSpinner.setSelection(position);
            }
        }, this.mCycleListener, true);
        this.mLoadingViewController = new LoadingViewController(getView().findViewById(R.id.loading_container), getListView());
        this.mLoadingViewController.showLoadingViewDelayed();
    }

    public static /* synthetic */ void lambda$onViewCreated$0(DataUsageList dataUsageList, View btn) {
        Bundle args = new Bundle();
        args.putParcelable("network_template", dataUsageList.mTemplate);
        new SubSettingLauncher(dataUsageList.getContext()).setDestination(BillingCycleSettings.class.getName()).setTitle((int) R.string.billing_cycle).setSourceMetricsCategory(dataUsageList.getMetricsCategory()).setArguments(args).launch();
    }

    public void onResume() {
        super.onResume();
        this.mDataStateListener.setListener(true, this.mSubId, getContext());
        updateBody();
        new AsyncTask<Void, Void, Void>() {
            /* Access modifiers changed, original: protected|varargs */
            public Void doInBackground(Void... params) {
                try {
                    Thread.sleep(2000);
                    DataUsageList.this.services.mStatsService.forceUpdate();
                } catch (RemoteException | InterruptedException e) {
                }
                return null;
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(Void result) {
                if (DataUsageList.this.isAdded()) {
                    DataUsageList.this.updateBody();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void onPause() {
        super.onPause();
        this.mDataStateListener.setListener(false, this.mSubId, getContext());
    }

    public void onDestroy() {
        this.mUidDetailProvider.clearCache();
        this.mUidDetailProvider = null;
        TrafficStats.closeQuietly(this.mStatsSession);
        super.onDestroy();
    }

    /* Access modifiers changed, original: 0000 */
    public void processArgument() {
        Intent intent;
        Bundle args = getArguments();
        if (args != null) {
            this.mSubId = args.getInt("sub_id", -1);
            this.mTemplate = (NetworkTemplate) args.getParcelable("network_template");
        }
        if (this.mTemplate == null && this.mSubId == -1) {
            intent = getIntent();
            this.mSubId = intent.getIntExtra("sub_id", -1);
            this.mTemplate = (NetworkTemplate) intent.getParcelableExtra("network_template");
        }
        if (this.mTemplate == null && this.mSubId == -1) {
            intent = getIntent();
            this.mSubId = intent.getIntExtra("android.provider.extra.SUB_ID", -1);
            this.mTemplate = (NetworkTemplate) intent.getParcelableExtra("network_template");
        }
    }

    private void updateBody() {
        if (isAdded()) {
            Context context = getActivity();
            getLoaderManager().restartLoader(2, ChartDataLoader.buildArgs(this.mTemplate, null), this.mChartDataCallbacks);
            getActivity().invalidateOptionsMenu();
            int seriesColor = context.getColor(R.color.sim_noitification);
            if (this.mSubId != -1) {
                SubscriptionInfo sir = this.services.mSubscriptionManager.getActiveSubscriptionInfo(this.mSubId);
                if (sir != null) {
                    seriesColor = sir.getIconTint();
                }
            }
            this.mChart.setColors(seriesColor, Color.argb(127, Color.red(seriesColor), Color.green(seriesColor), Color.blue(seriesColor)));
        }
    }

    private void updatePolicy() {
        NetworkPolicy policy = this.services.mPolicyEditor.getPolicy(this.mTemplate);
        View configureButton = this.mHeader.findViewById(R.id.filter_settings);
        if (isNetworkPolicyModifiable(policy, this.mSubId) && isMobileDataAvailable(this.mSubId)) {
            this.mChart.setNetworkPolicy(policy);
        } else {
            this.mChart.setNetworkPolicy(null);
        }
        if (this.mCycleAdapter.updateCycleList(policy, this.mChartData)) {
            updateDetailData();
        }
    }

    private void updateDetailData() {
        long start = this.mChart.getInspectStart();
        long end = this.mChart.getInspectEnd();
        long now = System.currentTimeMillis();
        Context context = getActivity();
        Entry entry = null;
        if (this.mChartData != null) {
            entry = this.mChartData.network.getValues(start, end, now, null);
        }
        Entry entry2 = entry;
        getLoaderManager().restartLoader(3, SummaryForAllUidLoader.buildArgs(this.mTemplate, start, end), this.mSummaryCallbacks);
        String totalPhrase = OPUtils.formatFileSize(context, entry2 != null ? entry2.rxBytes + entry2.txBytes : 0);
        this.mUsageAmount.setTitle(getString(R.string.data_used_template, new Object[]{totalPhrase}));
    }

    public void bindStats(NetworkStats stats, int[] restrictedUids) {
        int uid;
        int currentUserId;
        int i;
        PackageManager pm;
        int userId;
        NetworkStats networkStats = stats;
        int[] iArr = restrictedUids;
        ArrayList<AppItem> items = new ArrayList();
        int currentUserId2 = ActivityManager.getCurrentUser();
        UserManager userManager = UserManager.get(getContext());
        List<UserHandle> profiles = userManager.getUserProfiles();
        SparseArray<AppItem> knownItems = new SparseArray();
        PackageManager pm2 = getContext().getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm2.getApplicationInfo(OPConstants.PACKAGENAME_DIALER, 1);
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("get dialer getApplicationInfo failed ");
            stringBuilder.append(e);
            Log.d(str, stringBuilder.toString());
        }
        int i2 = 0;
        int size = networkStats != null ? stats.size() : 0;
        long largest = 0;
        NetworkStats.Entry entry = null;
        int i3 = 0;
        while (true) {
            int i4 = i3;
            if (i4 >= size) {
                break;
            }
            int i5;
            NetworkStats.Entry entry2;
            int i6;
            int size2;
            NetworkStats.Entry entry3;
            NetworkStats.Entry entry4 = networkStats.getValues(i4, entry);
            uid = entry4.uid;
            int userId2 = UserHandle.getUserId(uid);
            try {
                ApplicationInfo systemInfo = pm2.getApplicationInfo(pm2.getPackagesForUid(uid)[i2], i2);
                if (userId2 != 999 || systemInfo == null) {
                    i5 = 1;
                } else {
                    i5 = 1;
                    if ((systemInfo.flags & 1) > 0) {
                        entry2 = entry4;
                        i6 = i4;
                        size2 = size;
                        currentUserId = currentUserId2;
                        currentUserId2 = 1;
                        i = 0;
                        pm = pm2;
                        i3 = i6 + 1;
                        pm2 = pm;
                        entry = entry2;
                        i2 = i;
                        size = size2;
                        currentUserId2 = currentUserId;
                        networkStats = stats;
                    }
                }
                entry3 = entry4;
            } catch (Exception e2) {
                i5 = 1;
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                entry3 = entry4;
                stringBuilder2.append("get dialer getApplicationInfo failed ");
                stringBuilder2.append(e2);
                Log.d(str2, stringBuilder2.toString());
            }
            int uid2;
            if (UserHandle.isApp(uid)) {
                int uid3;
                if (profiles.contains(new UserHandle(userId2))) {
                    if (userId2 != currentUserId2) {
                        uid3 = uid;
                        entry2 = entry3;
                        i6 = i4;
                        size2 = size;
                        currentUserId = currentUserId2;
                        currentUserId2 = i5;
                        i = 0;
                        pm = pm2;
                        largest = accumulate(UidDetailProvider.buildKeyForUser(userId2), knownItems, entry2, 0, items, largest);
                    } else {
                        uid3 = uid;
                        i6 = i4;
                        size2 = size;
                        currentUserId = currentUserId2;
                        currentUserId2 = i5;
                        entry2 = entry3;
                        i = 0;
                        pm = pm2;
                    }
                    i3 = uid3;
                    i4 = 2;
                } else {
                    userId = userId2;
                    uid3 = uid;
                    i6 = i4;
                    size2 = size;
                    currentUserId = currentUserId2;
                    currentUserId2 = i5;
                    entry2 = entry3;
                    i = 0;
                    pm = pm2;
                    if (userManager.getUserInfo(userId) == null) {
                        userId2 = -4;
                        i2 = 2;
                    } else {
                        userId2 = UidDetailProvider.buildKeyForUser(userId);
                        i2 = 0;
                    }
                    i4 = i2;
                    i3 = userId2;
                }
                uid2 = uid3;
            } else {
                i6 = i4;
                size2 = size;
                currentUserId = currentUserId2;
                currentUserId2 = i5;
                entry2 = entry3;
                i = 0;
                pm = pm2;
                uid2 = uid;
                if (uid2 == -4 || uid2 == -5) {
                    i3 = uid2;
                    userId2 = 2;
                } else {
                    i3 = 1000;
                    userId2 = 2;
                }
                i4 = userId2;
            }
            largest = accumulate(i3, knownItems, entry2, i4, items, largest);
            i3 = i6 + 1;
            pm2 = pm;
            entry = entry2;
            i2 = i;
            size = size2;
            currentUserId2 = currentUserId;
            networkStats = stats;
        }
        pm = pm2;
        i = i2;
        currentUserId = currentUserId2;
        i3 = iArr.length;
        for (userId = i; userId < i3; userId++) {
            uid = iArr[userId];
            if (profiles.contains(new UserHandle(UserHandle.getUserId(uid)))) {
                AppItem item = (AppItem) knownItems.get(uid);
                if (item == null) {
                    item = new AppItem(uid);
                    item.total = -1;
                    items.add(item);
                    knownItems.put(item.key, item);
                }
                item.restricted = true;
            }
        }
        Collections.sort(items);
        this.mApps.removeAll();
        UserInfo multiAppUserInfo = OPUtils.getCorpUserInfo(getContext());
        uid = i;
        while (uid < items.size()) {
            AppDataUsagePreference preference = new AppDataUsagePreference(getContext(), (AppItem) items.get(uid), largest != 0 ? (int) ((((AppItem) items.get(uid)).total * 100) / largest) : i, this.mUidDetailProvider);
            if (!String.valueOf(((AppItem) items.get(uid)).key).equals(preference.getTitle())) {
                preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        AppItem item = ((AppDataUsagePreference) preference).getItem();
                        if (item.key == 1000 || item.key == -4) {
                            AppDataUsage.OSUidDetail = new UidDetail();
                            AppDataUsage.OSUidDetail.icon = preference.getIcon();
                            AppDataUsage.OSUidDetail.label = preference.getTitle();
                        }
                        DataUsageList.this.startAppDataUsage(item);
                        return true;
                    }
                });
                if (multiAppUserInfo != null) {
                    if (multiAppUserInfo.id == 999 && ((AppItem) items.get(uid)).key < 0) {
                    }
                }
                this.mApps.addPreference(preference);
            }
            uid++;
        }
    }

    private void startAppDataUsage(AppItem item) {
        Bundle args = new Bundle();
        args.putParcelable(AppDataUsage.ARG_APP_ITEM, item);
        args.putParcelable("network_template", this.mTemplate);
        args.putInt(AppDataUsage.ARG_SUBID, this.mSubId);
        args.putInt("uid", item.key);
        new SubSettingLauncher(getContext()).setDestination(AppDataUsage.class.getName()).setTitle((int) R.string.app_data_usage).setArguments(args).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    private static long accumulate(int collapseKey, SparseArray<AppItem> knownItems, NetworkStats.Entry entry, int itemCategory, ArrayList<AppItem> items, long largest) {
        int uid = entry.uid;
        AppItem item = (AppItem) knownItems.get(collapseKey);
        if (item == null) {
            item = new AppItem(collapseKey);
            item.category = itemCategory;
            items.add(item);
            knownItems.put(item.key, item);
        }
        item.addUid(uid);
        item.total += entry.rxBytes + entry.txBytes;
        return Math.max(largest, item.total);
    }

    public static boolean hasReadyMobileRadio(Context context) {
        ConnectivityManager conn = ConnectivityManager.from(context);
        TelephonyManager tele = TelephonyManager.from(context);
        List<SubscriptionInfo> subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        boolean retVal = false;
        if (subInfoList == null) {
            return false;
        }
        boolean isReady = true;
        Iterator it = subInfoList.iterator();
        while (true) {
            int i = 1;
            if (!it.hasNext()) {
                break;
            }
            if (tele.getSimState(((SubscriptionInfo) it.next()).getSimSlotIndex()) != 5) {
                i = 0;
            }
            isReady &= i;
        }
        if (conn.isNetworkSupported(0) && isReady) {
            retVal = true;
        }
        return retVal;
    }

    public static boolean hasReadyMobileRadio(Context context, int subId) {
        ConnectivityManager conn = ConnectivityManager.from(context);
        boolean isReady = TelephonyManager.from(context).getSimState(SubscriptionManager.getSlotIndex(subId)) == 5;
        if (conn.isNetworkSupported(0) && isReady) {
            return true;
        }
        return false;
    }
}
