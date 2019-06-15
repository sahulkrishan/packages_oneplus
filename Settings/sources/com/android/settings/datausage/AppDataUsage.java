package com.android.settings.datausage;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.AppItem;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.net.ChartData;
import com.android.settingslib.net.ChartDataLoader;
import com.android.settingslib.net.UidDetail;
import com.android.settingslib.net.UidDetailProvider;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import com.oneplus.settings.ui.OPProgressDialog;
import com.oneplus.settings.ui.OPProgressDialog.OnTimeOutListener;
import com.oneplus.settings.utils.OPFirewallRule;
import com.oneplus.settings.utils.OPFirewallUtils;
import com.oneplus.settings.utils.OPUtils;
import java.util.Iterator;
import java.util.List;

public class AppDataUsage extends DataUsageBase implements OnPreferenceChangeListener, Listener {
    public static final String ARG_APP_ITEM = "app_item";
    public static final String ARG_NETWORK_TEMPLATE = "network_template";
    public static final String ARG_SUBID = "arg_subid";
    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_APP_SETTINGS = "app_settings";
    private static final String KEY_BACKGROUND_USAGE = "background_usage";
    private static final String KEY_CYCLE = "cycle";
    private static final String KEY_DISABLE_MOBILE = "disabled_mobile";
    private static final String KEY_DISABLE_WIFI = "disabled_wifi";
    private static final String KEY_FOREGROUND_USAGE = "foreground_usage";
    private static final String KEY_PF_CYCLE = "pf_cycle";
    private static final String KEY_RESTRICT_BACKGROUND = "restrict_background";
    private static final String KEY_TOTAL_USAGE = "total_usage";
    private static final String KEY_UNRESTRICTED_DATA = "unrestricted_data_saver";
    private static final int LOADER_APP_PREF = 3;
    private static final int LOADER_CHART_DATA = 2;
    public static UidDetail OSUidDetail = null;
    public static int SYSTEM_UID = 1000;
    private static final String TAG = "AppDataUsage";
    private AppItem mAppItem;
    private PreferenceCategory mAppList;
    private final LoaderCallbacks<ArraySet<Preference>> mAppPrefCallbacks = new LoaderCallbacks<ArraySet<Preference>>() {
        public Loader<ArraySet<Preference>> onCreateLoader(int i, Bundle bundle) {
            return new AppPrefLoader(AppDataUsage.this.getPrefContext(), AppDataUsage.this.mPackages, AppDataUsage.this.getPackageManager());
        }

        public void onLoadFinished(Loader<ArraySet<Preference>> loader, ArraySet<Preference> preferences) {
            if (preferences != null && AppDataUsage.this.mAppList != null) {
                Iterator it = preferences.iterator();
                while (it.hasNext()) {
                    AppDataUsage.this.mAppList.addPreference((Preference) it.next());
                }
            }
        }

        public void onLoaderReset(Loader<ArraySet<Preference>> loader) {
        }
    };
    private Preference mAppSettings;
    private Intent mAppSettingsIntent;
    private Preference mBackgroundUsage;
    private ChartData mChartData;
    private final LoaderCallbacks<ChartData> mChartDataCallbacks = new LoaderCallbacks<ChartData>() {
        public Loader<ChartData> onCreateLoader(int id, Bundle args) {
            return new ChartDataLoader(AppDataUsage.this.getActivity(), AppDataUsage.this.mStatsSession, args);
        }

        public void onLoadFinished(Loader<ChartData> loader, ChartData data) {
            AppDataUsage.this.mChartData = data;
            AppDataUsage.this.mCycleAdapter.updateCycleList(AppDataUsage.this.mPolicy, AppDataUsage.this.mChartData);
            AppDataUsage.this.bindData();
        }

        public void onLoaderReset(Loader<ChartData> loader) {
        }
    };
    private SpinnerPreference mCycle;
    private CycleAdapter mCycleAdapter;
    private OnItemSelectedListener mCycleListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            AppDataUsage.this.mCycle.getSelectedItem();
            AppDataUsage.this.bindData();
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
            AppDataUsage.this.bindData();
        }
    };
    private Preference mCycleText;
    private DataSaverBackend mDataSaverBackend;
    private SwitchPreference mDisabledData;
    private SwitchPreference mDisabledWifi;
    private long mEnd;
    private Preference mForegroundUsage;
    private Drawable mIcon;
    private CharSequence mLabel;
    private PackageManagerWrapper mPackageManagerWrapper;
    private String mPackageName;
    private final ArraySet<String> mPackages = new ArraySet();
    private NetworkPolicy mPolicy;
    private RestrictedSwitchPreference mRestrictBackground;
    private long mStart;
    private INetworkStatsSession mStatsSession;
    private int mSubId = 0;
    private NetworkTemplate mTemplate;
    private Preference mTotalUsage;
    private RestrictedSwitchPreference mUnrestrictedData;

    class UpdateRuleTask extends AsyncTask<Void, Integer, Integer> {
        public static final int TYPE_MOBILE = 0;
        public static final int TYPE_WLAN = 1;
        private Context mContext;
        OPProgressDialog progressDialog;
        private boolean state;
        private int type;
        private int uid;

        public UpdateRuleTask(Context context, int uid, boolean disable, int type) {
            this.mContext = context;
            this.uid = uid;
            this.state = disable;
            this.type = type;
        }

        /* Access modifiers changed, original: protected */
        public void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new OPProgressDialog(this.mContext);
            this.progressDialog.setMessage(this.mContext.getString(R.string.settings_safetylegal_activity_loading));
            this.progressDialog.setTimeOut(5000, new OnTimeOutListener() {
                public void onTimeOut(OPProgressDialog dialog) {
                    Log.d("UpdateRuleTask", "UpdateRuleTask onTimeOut");
                }
            });
            this.progressDialog.showDelay(1000);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Integer doInBackground(Void... params) {
            List<ApplicationInfo> apps = OPDataUsageUtils.getApplicationInfoByUid(this.mContext, this.uid);
            for (ApplicationInfo appInfo : apps) {
                if (appInfo != null) {
                    if (this.type == 0) {
                        OPFirewallUtils.addOrUpdateRole(AppDataUsage.this.getContext(), new OPFirewallRule(appInfo.packageName, null, Integer.valueOf(this.state)));
                    } else {
                        OPFirewallUtils.addOrUpdateRole(AppDataUsage.this.getContext(), new OPFirewallRule(appInfo.packageName, Integer.valueOf(this.state), null));
                    }
                }
            }
            return Integer.valueOf(apps.size());
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (this.type == 0) {
                AppDataUsage.this.mDisabledData.setChecked(this.state);
            } else {
                AppDataUsage.this.mDisabledWifi.setChecked(this.state);
            }
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPackageManagerWrapper = new PackageManagerWrapper(getPackageManager());
        Bundle args = getArguments();
        try {
            NetworkTemplate networkTemplate;
            this.mStatsSession = this.services.mStatsService.openSession();
            this.mAppItem = args != null ? (AppItem) args.getParcelable(ARG_APP_ITEM) : null;
            if (args != null) {
                networkTemplate = (NetworkTemplate) args.getParcelable("network_template");
            } else {
                networkTemplate = null;
            }
            this.mTemplate = networkTemplate;
            if (this.mTemplate == null) {
                Context context = getContext();
                this.mTemplate = DataUsageUtils.getDefaultTemplate(context, DataUsageUtils.getDefaultSubscriptionId(context));
            }
            if (this.mAppItem == null) {
                int uid;
                if (args != null) {
                    uid = args.getInt("uid", -1);
                } else {
                    uid = getActivity().getIntent().getIntExtra("uid", -1);
                }
                if (uid == -1) {
                    getActivity().finish();
                } else {
                    addUid(uid);
                    this.mAppItem = new AppItem(uid);
                    this.mAppItem.addUid(uid);
                }
            } else {
                for (int i = 0; i < this.mAppItem.uids.size(); i++) {
                    addUid(this.mAppItem.uids.keyAt(i));
                }
            }
            addPreferencesFromResource(R.xml.app_data_usage);
            this.mTotalUsage = findPreference(KEY_TOTAL_USAGE);
            this.mForegroundUsage = findPreference(KEY_FOREGROUND_USAGE);
            this.mBackgroundUsage = findPreference(KEY_BACKGROUND_USAGE);
            this.mSubId = args != null ? args.getInt(ARG_SUBID) : 0;
            long[] section = OPDataUsageUtils.getDataUsageSectionTimeMillByAccountDay(getPrefContext(), this.mSubId);
            this.mStart = section[0];
            this.mEnd = section[1];
            CharSequence sectionTextString = Utils.formatDateRange(getPrefContext(), this.mStart, this.mEnd);
            this.mCycleText = findPreference(KEY_PF_CYCLE);
            this.mCycleText.setTitle(sectionTextString);
            this.mDisabledData = (SwitchPreference) findPreference(KEY_DISABLE_MOBILE);
            this.mDisabledWifi = (SwitchPreference) findPreference(KEY_DISABLE_WIFI);
            int userId = UserHandle.getUserId(this.mAppItem.key);
            if (OPUtils.isGuestMode() || !UserHandle.isApp(this.mAppItem.key) || userId == 999) {
                this.mDisabledData.setVisible(false);
                this.mDisabledWifi.setVisible(false);
            } else {
                this.mDisabledData.setOnPreferenceChangeListener(this);
                this.mDisabledWifi.setOnPreferenceChangeListener(this);
                updateFireWallState();
            }
            this.mCycle = (SpinnerPreference) findPreference(KEY_CYCLE);
            this.mCycle.setVisible(false);
            this.mCycleAdapter = new CycleAdapter(getContext(), this.mCycle, this.mCycleListener, false);
            if (this.mAppItem.key > 0) {
                if (this.mPackages.size() != 0) {
                    try {
                        ApplicationInfo info = this.mPackageManagerWrapper.getApplicationInfoAsUser((String) this.mPackages.valueAt(0), 0, UserHandle.getUserId(this.mAppItem.key));
                        this.mIcon = IconDrawableFactory.newInstance(getActivity()).getBadgedIcon(info);
                        this.mLabel = info.loadLabel(this.mPackageManagerWrapper.getPackageManager());
                        this.mPackageName = info.packageName;
                    } catch (NameNotFoundException e) {
                    }
                }
                if (UserHandle.isApp(this.mAppItem.key)) {
                    this.mRestrictBackground = (RestrictedSwitchPreference) findPreference("restrict_background");
                    this.mRestrictBackground.setOnPreferenceChangeListener(this);
                    this.mUnrestrictedData = (RestrictedSwitchPreference) findPreference(KEY_UNRESTRICTED_DATA);
                    this.mUnrestrictedData.setOnPreferenceChangeListener(this);
                } else {
                    removePreference(KEY_UNRESTRICTED_DATA);
                    removePreference("restrict_background");
                }
                this.mDataSaverBackend = new DataSaverBackend(getContext());
                this.mAppSettings = findPreference(KEY_APP_SETTINGS);
                this.mAppSettingsIntent = new Intent("android.intent.action.MANAGE_NETWORK_USAGE");
                this.mAppSettingsIntent.addCategory("android.intent.category.DEFAULT");
                PackageManager pm = getPackageManager();
                boolean matchFound = false;
                Iterator it = this.mPackages.iterator();
                while (it.hasNext()) {
                    this.mAppSettingsIntent.setPackage((String) it.next());
                    if (pm.resolveActivity(this.mAppSettingsIntent, 0) != null) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    removePreference(KEY_APP_SETTINGS);
                    this.mAppSettings = null;
                }
                if (this.mPackages.size() > 1) {
                    this.mAppList = (PreferenceCategory) findPreference(KEY_APP_LIST);
                    getLoaderManager().initLoader(3, Bundle.EMPTY, this.mAppPrefCallbacks);
                } else {
                    removePreference(KEY_APP_LIST);
                }
            } else {
                Context context2 = getActivity();
                UidDetail uidDetail = new UidDetailProvider(context2).getUidDetail(this.mAppItem.key, true);
                this.mIcon = uidDetail.icon;
                this.mLabel = uidDetail.label;
                if (TextUtils.isEmpty(this.mLabel)) {
                    UserManager um = UserManager.get(getActivity());
                    UserInfo info2 = um.getUserInfo(userId);
                    PackageManager pm2 = getPackageManager();
                    if (info2 != null) {
                        this.mIcon = com.android.settingslib.Utils.getUserIcon(getActivity(), um, info2);
                        this.mLabel = com.android.settingslib.Utils.getUserLabel(getActivity(), info2);
                    }
                }
                this.mPackageName = context2.getPackageName();
                removePreference(KEY_UNRESTRICTED_DATA);
                removePreference(KEY_APP_SETTINGS);
                removePreference("restrict_background");
                removePreference(KEY_APP_LIST);
            }
            if (this.mAppItem.key == SYSTEM_UID && OSUidDetail != null) {
                this.mIcon = OSUidDetail.icon;
                this.mLabel = OSUidDetail.label;
            }
        } catch (RemoteException e2) {
            throw new RuntimeException(e2);
        }
    }

    public void onDestroy() {
        TrafficStats.closeQuietly(this.mStatsSession);
        if (OSUidDetail != null) {
            OSUidDetail.icon.setCallback(null);
            OSUidDetail = null;
        }
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        if (this.mDataSaverBackend != null) {
            this.mDataSaverBackend.addListener(this);
        }
        this.mPolicy = this.services.mPolicyEditor.getPolicy(this.mTemplate);
        getLoaderManager().restartLoader(2, ChartDataLoader.buildArgs(this.mTemplate, this.mAppItem), this.mChartDataCallbacks);
        updatePrefs();
    }

    public void onPause() {
        super.onPause();
        if (this.mDataSaverBackend != null) {
            this.mDataSaverBackend.remListener(this);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mRestrictBackground) {
            this.mDataSaverBackend.setIsBlacklisted(this.mAppItem.key, this.mPackageName, ((Boolean) newValue).booleanValue() ^ 1);
            updatePrefs();
            return true;
        } else if (preference == this.mUnrestrictedData) {
            this.mDataSaverBackend.setIsWhitelisted(this.mAppItem.key, this.mPackageName, ((Boolean) newValue).booleanValue());
            return true;
        } else {
            if (preference == this.mDisabledWifi) {
                new UpdateRuleTask(getPrefContext(), this.mAppItem.key, ((Boolean) newValue).booleanValue(), 1).execute(new Void[0]);
            } else if (preference == this.mDisabledData) {
                new UpdateRuleTask(getPrefContext(), this.mAppItem.key, ((Boolean) newValue).booleanValue(), 0).execute(new Void[0]);
            }
            return false;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference != this.mAppSettings) {
            return super.onPreferenceTreeClick(preference);
        }
        try {
            getActivity().startActivityAsUser(this.mAppSettingsIntent, new UserHandle(UserHandle.getUserId(this.mAppItem.key)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updatePrefs() {
        updatePrefs(getAppRestrictBackground(), getUnrestrictData());
    }

    private void updatePrefs(boolean restrictBackground, boolean unrestrictData) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfMeteredDataRestricted(getContext(), this.mPackageName, UserHandle.getUserId(this.mAppItem.key));
        if (this.mRestrictBackground != null) {
            this.mRestrictBackground.setChecked(restrictBackground ^ 1);
            this.mRestrictBackground.setDisabledByAdmin(admin);
        }
        if (this.mUnrestrictedData == null) {
            return;
        }
        if (restrictBackground) {
            this.mUnrestrictedData.setVisible(false);
            return;
        }
        this.mUnrestrictedData.setVisible(true);
        this.mUnrestrictedData.setChecked(unrestrictData);
        this.mUnrestrictedData.setDisabledByAdmin(admin);
    }

    private void addUid(int uid) {
        String[] packages = getPackageManager().getPackagesForUid(uid);
        if (packages != null) {
            for (Object add : packages) {
                this.mPackages.add(add);
            }
        }
    }

    private void bindData() {
        long foregroundBytes;
        long backgroundBytes;
        if (this.mChartData == null || this.mStart == 0) {
            foregroundBytes = 0;
            backgroundBytes = 0;
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            Entry entry = this.mChartData.detailDefault.getValues(this.mStart, this.mEnd, currentTimeMillis, null);
            backgroundBytes = entry.rxBytes + entry.txBytes;
            entry = this.mChartData.detailForeground.getValues(this.mStart, this.mEnd, currentTimeMillis, entry);
            foregroundBytes = entry.rxBytes + entry.txBytes;
        }
        long foregroundBytes2 = foregroundBytes;
        long totalBytes = backgroundBytes + foregroundBytes2;
        Context context = getContext();
        this.mTotalUsage.setSummary(OPUtils.formatFileSize(context, totalBytes));
        this.mForegroundUsage.setSummary(OPUtils.formatFileSize(context, foregroundBytes2));
        this.mBackgroundUsage.setSummary(OPUtils.formatFileSize(context, backgroundBytes));
    }

    private boolean getAppRestrictBackground() {
        return (this.services.mPolicyManager.getUidPolicy(this.mAppItem.key) & 1) != 0;
    }

    private boolean getUnrestrictData() {
        if (this.mDataSaverBackend != null) {
            return this.mDataSaverBackend.isWhitelisted(this.mAppItem.key);
        }
        return false;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String pkg = this.mPackages.size() != 0 ? (String) this.mPackages.valueAt(0) : null;
        int uid = 0;
        if (pkg != null) {
            try {
                uid = this.mPackageManagerWrapper.getPackageUidAsUser(pkg, UserHandle.getUserId(this.mAppItem.key));
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping UID because cannot find package ");
                stringBuilder.append(pkg);
                Log.w(str, stringBuilder.toString());
            }
        }
        boolean showInfoButton = this.mAppItem.key > 0;
        Activity activity = getActivity();
        getPreferenceScreen().addPreference(EntityHeaderController.newInstance(activity, this, null).setRecyclerView(getListView(), getLifecycle()).setUid(uid).setHasAppInfoLink(showInfoButton).setButtonActions(0, 0).setIcon(this.mIcon).setLabel(this.mLabel).setPackageName(pkg).done(activity, getPrefContext()));
    }

    public int getMetricsCategory() {
        return 343;
    }

    public void onDataSaverChanged(boolean isDataSaving) {
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
        if (this.mAppItem.uids.get(uid, false)) {
            updatePrefs(getAppRestrictBackground(), isWhitelisted);
        }
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
        if (this.mAppItem.uids.get(uid, false)) {
            updatePrefs(isBlacklisted, getUnrestrictData());
        }
    }

    private void updateFireWallState() {
        List<ApplicationInfo> apps = OPDataUsageUtils.getApplicationInfoByUid(getPrefContext(), this.mAppItem.key);
        if (apps != null && !apps.isEmpty()) {
            boolean z = false;
            ApplicationInfo appInfo = (ApplicationInfo) apps.get(0);
            if (appInfo != null) {
                OPFirewallRule role = OPFirewallUtils.selectFirewallRuleByPkg(getContext(), appInfo.packageName);
                if (role == null || role.getMobile() == null) {
                    this.mDisabledData.setChecked(false);
                } else {
                    this.mDisabledData.setChecked(role.getMobile().intValue() != 0);
                }
                if (role == null || role.getWlan() == null) {
                    this.mDisabledWifi.setChecked(false);
                    return;
                }
                SwitchPreference switchPreference = this.mDisabledWifi;
                if (role.getWlan().intValue() != 0) {
                    z = true;
                }
                switchPreference.setChecked(z);
            }
        }
    }
}
