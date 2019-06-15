package com.android.settings.applications.manageapplications;

import android.app.Activity;
import android.app.usage.IUsageStatsManager;
import android.app.usage.IUsageStatsManager.Stub;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.android.settings.R;
import com.android.settings.Settings.BgOptimizeAppListActivity;
import com.android.settings.Settings.BgOptimizeSwitchActivity;
import com.android.settings.Settings.ChangeWifiStateActivity;
import com.android.settings.Settings.DirectoryAccessSettingsActivity;
import com.android.settings.Settings.DisplaySizeAdaptionAppListActivity;
import com.android.settings.Settings.GamesStorageActivity;
import com.android.settings.Settings.HighPowerApplicationsActivity;
import com.android.settings.Settings.ManageExternalSourcesActivity;
import com.android.settings.Settings.MoviesStorageActivity;
import com.android.settings.Settings.NotificationAppListActivity;
import com.android.settings.Settings.OverlaySettingsActivity;
import com.android.settings.Settings.PhotosStorageActivity;
import com.android.settings.Settings.StorageUseActivity;
import com.android.settings.Settings.UsageAccessSettingsActivity;
import com.android.settings.Settings.WriteSettingsActivity;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.AppStateDirectoryAccessBridge;
import com.android.settings.applications.AppStateInstallAppsBridge;
import com.android.settings.applications.AppStateNotificationBridge;
import com.android.settings.applications.AppStateNotificationBridge.NotificationsSentState;
import com.android.settings.applications.AppStateOverlayBridge;
import com.android.settings.applications.AppStatePowerBridge;
import com.android.settings.applications.AppStateUsageBridge;
import com.android.settings.applications.AppStateUsageBridge.UsageState;
import com.android.settings.applications.AppStateWriteSettingsBridge;
import com.android.settings.applications.AppStorageSettings;
import com.android.settings.applications.DefaultAppSettings;
import com.android.settings.applications.DirectoryAccessDetails;
import com.android.settings.applications.InstalledAppCounter;
import com.android.settings.applications.UsageAccessDetails;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;
import com.android.settings.applications.appinfo.DrawOverlayDetails;
import com.android.settings.applications.appinfo.ExternalSourcesDetails;
import com.android.settings.applications.appinfo.WriteSettingsDetails;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.fuelgauge.HighPowerDetail;
import com.android.settings.notification.AppNotificationSettings;
import com.android.settings.notification.ConfigureNotificationSettings;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.widget.LoadingViewController;
import com.android.settings.wifi.AppStateChangeWifiStateBridge;
import com.android.settings.wifi.ChangeWifiStateDetails;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.android.settingslib.applications.ApplicationsState.VolumeFilter;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import com.android.settingslib.utils.ThreadUtils;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import com.oneplus.settings.backgroundoptimize.AppBgOptimizeBridge;
import com.oneplus.settings.backgroundoptimize.BgOptimizeDetail;
import com.oneplus.settings.displaysizeadaption.DisplaySizeAdaptionBridge;
import com.oneplus.settings.displaysizeadaption.DisplaySizeAdaptionDetail;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class ManageApplications extends InstrumentedFragment implements OnClickListener, OnItemSelectedListener {
    private static final int ADVANCED_SETTINGS = 2;
    public static final String APP_CHG = "chg";
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    public static final String EXTRA_CLASSNAME = "classname";
    private static final String EXTRA_HAS_BRIDGE = "hasBridge";
    private static final String EXTRA_HAS_ENTRIES = "hasEntries";
    private static final String EXTRA_SHOW_SYSTEM = "showSystem";
    private static final String EXTRA_SORT_ORDER = "sortOrder";
    public static final String EXTRA_STORAGE_TYPE = "storageType";
    public static final String EXTRA_VOLUME_NAME = "volumeName";
    public static final String EXTRA_VOLUME_UUID = "volumeUuid";
    public static final String EXTRA_WORK_ID = "workId";
    public static final String EXTRA_WORK_ONLY = "workProfileOnly";
    private static final int INSTALLED_APP_DETAILS = 1;
    public static final Set<Integer> LIST_TYPES_WITH_INSTANT = new ArraySet(Arrays.asList(new Integer[]{Integer.valueOf(0), Integer.valueOf(3)}));
    public static final int LIST_TYPE_BACKGROUND_OPTIMIZE = 15;
    public static final int LIST_TYPE_DIRECTORY_ACCESS = 12;
    public static final int LIST_TYPE_DISPLAY_SIZE_ADAPION = 14;
    public static final int LIST_TYPE_GAMES = 9;
    public static final int LIST_TYPE_HIGH_POWER = 5;
    public static final int LIST_TYPE_MAIN = 0;
    public static final int LIST_TYPE_MANAGE_SOURCES = 8;
    public static final int LIST_TYPE_MOVIES = 10;
    public static final int LIST_TYPE_NOTIFICATION = 1;
    public static final int LIST_TYPE_OVERLAY = 6;
    public static final int LIST_TYPE_PHOTOGRAPHY = 11;
    public static final int LIST_TYPE_STORAGE = 3;
    public static final int LIST_TYPE_USAGE_ACCESS = 4;
    public static final int LIST_TYPE_WIFI_ACCESS = 13;
    public static final int LIST_TYPE_WRITE_SETTINGS = 7;
    private static final int NO_USER_SPECIFIED = -1;
    public static final int SIZE_EXTERNAL = 2;
    public static final int SIZE_INTERNAL = 1;
    public static final int SIZE_TOTAL = 0;
    public static final int STORAGE_TYPE_DEFAULT = 0;
    public static final int STORAGE_TYPE_LEGACY = 2;
    public static final int STORAGE_TYPE_MUSIC = 1;
    public static final int STORAGE_TYPE_PHOTOS_VIDEOS = 3;
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };
    static final String TAG = "ManageApplications";
    private ApplicationsAdapter mApplications;
    private ApplicationsState mApplicationsState;
    private String mCurrentPkgName;
    private int mCurrentUid;
    private View mEmptyView;
    private AppFilterItem mFilter;
    private FilterSpinnerAdapter mFilterAdapter;
    private Spinner mFilterSpinner;
    CharSequence mInvalidSizeStr;
    private boolean mIsWorkOnly;
    private View mListContainer;
    public int mListType;
    private View mLoadingContainer;
    private NotificationBackend mNotificationBackend;
    private Menu mOptionsMenu;
    private RecyclerView mRecyclerView;
    private ResetAppsHelper mResetAppsHelper;
    private View mRootView;
    private boolean mShowSystem;
    @VisibleForTesting
    int mSortOrder = R.id.sort_order_alpha;
    private View mSpinnerHeader;
    private int mStorageType;
    private IUsageStatsManager mUsageStatsManager;
    private UserManager mUserManager;
    private String mVolumeUuid;
    private int mWorkUserId;

    static class FilterSpinnerAdapter extends ArrayAdapter<CharSequence> {
        private final Context mContext;
        private final ArrayList<AppFilterItem> mFilterOptions = new ArrayList();
        private final ManageApplications mManageApplications;

        public FilterSpinnerAdapter(ManageApplications manageApplications) {
            super(manageApplications.getContext(), R.layout.filter_spinner_item);
            this.mContext = manageApplications.getContext();
            this.mManageApplications = manageApplications;
            setDropDownViewResource(17367049);
        }

        public AppFilterItem getFilter(int position) {
            return (AppFilterItem) this.mFilterOptions.get(position);
        }

        public void setFilterEnabled(@FilterType int filter, boolean enabled) {
            if (enabled) {
                enableFilter(filter);
            } else {
                disableFilter(filter);
            }
        }

        public void enableFilter(@FilterType int filterType) {
            AppFilterItem filter = AppFilterRegistry.getInstance().get(filterType);
            if (!this.mFilterOptions.contains(filter)) {
                String str;
                StringBuilder stringBuilder;
                int i;
                if (ManageApplications.DEBUG) {
                    str = ManageApplications.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Enabling filter ");
                    stringBuilder.append(filter);
                    Log.d(str, stringBuilder.toString());
                }
                this.mFilterOptions.add(filter);
                Collections.sort(this.mFilterOptions);
                View access$400 = this.mManageApplications.mSpinnerHeader;
                if (this.mFilterOptions.size() > 1) {
                    i = 0;
                } else {
                    i = 8;
                }
                access$400.setVisibility(i);
                notifyDataSetChanged();
                if (this.mFilterOptions.size() == 1) {
                    if (ManageApplications.DEBUG) {
                        str = ManageApplications.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Auto selecting filter ");
                        stringBuilder.append(filter);
                        Log.d(str, stringBuilder.toString());
                    }
                    this.mManageApplications.mFilterSpinner.setSelection(0);
                    this.mManageApplications.onItemSelected(null, null, 0, 0);
                }
            }
        }

        public void disableFilter(@FilterType int filterType) {
            AppFilterItem filter = AppFilterRegistry.getInstance().get(filterType);
            if (this.mFilterOptions.remove(filter)) {
                String str;
                StringBuilder stringBuilder;
                int i;
                if (ManageApplications.DEBUG) {
                    str = ManageApplications.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Disabling filter ");
                    stringBuilder.append(filter);
                    Log.d(str, stringBuilder.toString());
                }
                Collections.sort(this.mFilterOptions);
                View access$400 = this.mManageApplications.mSpinnerHeader;
                if (this.mFilterOptions.size() > 1) {
                    i = 0;
                } else {
                    i = 8;
                }
                access$400.setVisibility(i);
                notifyDataSetChanged();
                if (this.mManageApplications.mFilter == filter && this.mFilterOptions.size() > 0) {
                    if (ManageApplications.DEBUG) {
                        str = ManageApplications.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Auto selecting filter ");
                        stringBuilder.append(this.mFilterOptions.get(0));
                        Log.d(str, stringBuilder.toString());
                    }
                    this.mManageApplications.mFilterSpinner.setSelection(0);
                    this.mManageApplications.onItemSelected(null, null, 0, 0);
                }
            }
        }

        public int getCount() {
            return this.mFilterOptions.size();
        }

        public CharSequence getItem(int position) {
            return this.mContext.getText(((AppFilterItem) this.mFilterOptions.get(position)).getTitle());
        }
    }

    static class ApplicationsAdapter extends Adapter<ApplicationViewHolder> implements Callbacks, Callback {
        private static final String STATE_LAST_SCROLL_INDEX = "state_last_scroll_index";
        private static final int VIEW_TYPE_APP = 0;
        private static final int VIEW_TYPE_EXTRA_VIEW = 1;
        private AppFilterItem mAppFilter;
        private AppFilter mCompositeFilter;
        private final Context mContext;
        private ArrayList<AppEntry> mEntries;
        private final AppStateBaseBridge mExtraInfoBridge;
        private FileViewHolderController mExtraViewController;
        private boolean mHasReceivedBridgeCallback;
        private boolean mHasReceivedLoadEntries;
        private int mLastIndex = -1;
        private int mLastSortMode = -1;
        private final LoadingViewController mLoadingViewController;
        private final ManageApplications mManageApplications;
        @VisibleForTesting
        OnScrollListener mOnScrollListener;
        private RecyclerView mRecyclerView;
        private boolean mResumed;
        private final Session mSession;
        private final ApplicationsState mState;
        private int mWhichSize = 0;

        public static class OnScrollListener extends android.support.v7.widget.RecyclerView.OnScrollListener {
            private ApplicationsAdapter mAdapter;
            private boolean mDelayNotifyDataChange;
            private int mScrollState = 0;

            public OnScrollListener(ApplicationsAdapter adapter) {
                this.mAdapter = adapter;
            }

            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                this.mScrollState = newState;
                if (this.mScrollState == 0 && this.mDelayNotifyDataChange) {
                    this.mDelayNotifyDataChange = false;
                    this.mAdapter.notifyDataSetChanged();
                }
            }

            public void postNotifyItemChange(int index) {
                if (this.mScrollState == 0) {
                    this.mAdapter.notifyItemChanged(index);
                } else {
                    this.mDelayNotifyDataChange = true;
                }
            }
        }

        public ApplicationsAdapter(ApplicationsState state, ManageApplications manageApplications, AppFilterItem appFilter, Bundle savedInstanceState) {
            setHasStableIds(true);
            this.mState = state;
            this.mSession = state.newSession(this);
            this.mManageApplications = manageApplications;
            this.mLoadingViewController = new LoadingViewController(this.mManageApplications.mLoadingContainer, this.mManageApplications.mListContainer);
            this.mContext = manageApplications.getActivity();
            this.mAppFilter = appFilter;
            if (this.mManageApplications.mListType == 1) {
                this.mExtraInfoBridge = new AppStateNotificationBridge(this.mContext, this.mState, this, manageApplications.mUsageStatsManager, manageApplications.mUserManager, manageApplications.mNotificationBackend);
            } else if (this.mManageApplications.mListType == 4) {
                this.mExtraInfoBridge = new AppStateUsageBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 5) {
                this.mExtraInfoBridge = new AppStatePowerBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 6) {
                this.mExtraInfoBridge = new AppStateOverlayBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 7) {
                this.mExtraInfoBridge = new AppStateWriteSettingsBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 8) {
                this.mExtraInfoBridge = new AppStateInstallAppsBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 12) {
                this.mExtraInfoBridge = new AppStateDirectoryAccessBridge(this.mState, this);
            } else if (this.mManageApplications.mListType == 13) {
                this.mExtraInfoBridge = new AppStateChangeWifiStateBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 14) {
                this.mExtraInfoBridge = new DisplaySizeAdaptionBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 15) {
                this.mExtraInfoBridge = new AppBgOptimizeBridge(this.mContext, this.mState, this);
            } else {
                this.mExtraInfoBridge = null;
            }
            if (savedInstanceState != null) {
                this.mLastIndex = savedInstanceState.getInt(STATE_LAST_SCROLL_INDEX);
            }
        }

        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            this.mRecyclerView = recyclerView;
            this.mOnScrollListener = new OnScrollListener(this);
            this.mRecyclerView.addOnScrollListener(this.mOnScrollListener);
        }

        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            this.mRecyclerView.removeOnScrollListener(this.mOnScrollListener);
            this.mOnScrollListener = null;
            this.mRecyclerView = null;
        }

        public void setCompositeFilter(AppFilter compositeFilter) {
            this.mCompositeFilter = compositeFilter;
            rebuild();
        }

        public void setFilter(AppFilterItem appFilter) {
            this.mAppFilter = appFilter;
            if (7 == appFilter.getFilterType()) {
                rebuild(R.id.sort_order_frequent_notification);
            } else if (6 == appFilter.getFilterType()) {
                rebuild(R.id.sort_order_recent_notification);
            } else {
                rebuild();
            }
        }

        public void setExtraViewController(FileViewHolderController extraViewController) {
            this.mExtraViewController = extraViewController;
            ThreadUtils.postOnBackgroundThread(new -$$Lambda$ManageApplications$ApplicationsAdapter$qMEtWjKuRu1RgrWKYhF-ScJDD7E(this));
        }

        public static /* synthetic */ void lambda$setExtraViewController$1(ApplicationsAdapter applicationsAdapter) {
            Log.d(ManageApplications.TAG, "postOnBackgroundThread setExtraViewController start");
            applicationsAdapter.mExtraViewController.queryStats();
            ThreadUtils.postOnMainThread(new -$$Lambda$ManageApplications$ApplicationsAdapter$zUDf4sT2ElTE4vuQaXRj16znehk(applicationsAdapter));
            Log.d(ManageApplications.TAG, "postOnBackgroundThread setExtraViewController end");
        }

        public void resume(int sort) {
            if (ManageApplications.DEBUG) {
                String str = ManageApplications.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Resume!  mResumed=");
                stringBuilder.append(this.mResumed);
                Log.i(str, stringBuilder.toString());
            }
            if (this.mResumed) {
                rebuild(sort);
                return;
            }
            this.mResumed = true;
            this.mSession.onResume();
            this.mLastSortMode = sort;
            if (this.mExtraInfoBridge != null) {
                this.mExtraInfoBridge.resume();
            }
            rebuild();
        }

        public void pause() {
            if (this.mResumed) {
                this.mResumed = false;
                this.mSession.onPause();
                if (this.mExtraInfoBridge != null) {
                    this.mExtraInfoBridge.pause();
                }
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(STATE_LAST_SCROLL_INDEX, ((LinearLayoutManager) this.mManageApplications.mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        }

        public void release() {
            this.mSession.onDestroy();
            if (this.mExtraInfoBridge != null) {
                this.mExtraInfoBridge.release();
            }
        }

        public void rebuild(int sort) {
            if (sort != this.mLastSortMode) {
                this.mManageApplications.mSortOrder = sort;
                this.mLastSortMode = sort;
                rebuild();
            }
        }

        public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (this.mManageApplications.mListType == 1) {
                view = ApplicationViewHolder.newView(parent, true);
            } else {
                view = ApplicationViewHolder.newView(parent, false);
            }
            return new ApplicationViewHolder(view, shouldUseStableItemHeight(this.mManageApplications.mListType));
        }

        public int getItemViewType(int position) {
            boolean isLastItem = getItemCount() - 1 == position;
            if (hasExtraView() && isLastItem) {
                return 1;
            }
            return 0;
        }

        public void rebuild() {
            if (this.mHasReceivedLoadEntries && (this.mExtraInfoBridge == null || this.mHasReceivedBridgeCallback)) {
                Comparator<AppEntry> comparatorObj;
                if (Environment.isExternalStorageEmulated()) {
                    this.mWhichSize = 0;
                } else {
                    this.mWhichSize = 1;
                }
                AppFilter filterObj = this.mAppFilter.getFilter();
                if (this.mCompositeFilter != null) {
                    filterObj = new CompoundFilter(filterObj, this.mCompositeFilter);
                }
                if (!this.mManageApplications.mShowSystem) {
                    if (ManageApplications.LIST_TYPES_WITH_INSTANT.contains(Integer.valueOf(this.mManageApplications.mListType))) {
                        filterObj = new CompoundFilter(filterObj, ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER_AND_INSTANT);
                    } else {
                        filterObj = new CompoundFilter(filterObj, ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER);
                    }
                }
                switch (this.mLastSortMode) {
                    case R.id.sort_order_frequent_notification /*2131363083*/:
                        comparatorObj = AppStateNotificationBridge.FREQUENCY_NOTIFICATION_COMPARATOR;
                        break;
                    case R.id.sort_order_recent_notification /*2131363084*/:
                        comparatorObj = AppStateNotificationBridge.RECENT_NOTIFICATION_COMPARATOR;
                        break;
                    case R.id.sort_order_size /*2131363085*/:
                        switch (this.mWhichSize) {
                            case 1:
                                comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
                                break;
                            case 2:
                                comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
                                break;
                            default:
                                comparatorObj = ApplicationsState.SIZE_COMPARATOR;
                                break;
                        }
                    default:
                        comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
                        break;
                }
                AppFilter finalFilterObj = new CompoundFilter(filterObj, ApplicationsState.FILTER_NOT_HIDE);
                filterObj = finalFilterObj;
                ThreadUtils.postOnBackgroundThread(new -$$Lambda$ManageApplications$ApplicationsAdapter$z53WdtAYQ69qQ4PsDaqCwHe1hfA(this, finalFilterObj, comparatorObj));
            }
        }

        public static /* synthetic */ void lambda$rebuild$3(ApplicationsAdapter applicationsAdapter, AppFilter finalFilterObj, Comparator comparatorObj) {
            Log.d(ManageApplications.TAG, "postOnBackgroundThread rebuild start");
            ArrayList<AppEntry> entries = applicationsAdapter.mSession.rebuild(finalFilterObj, comparatorObj, false);
            if (entries != null) {
                ThreadUtils.postOnMainThread(new -$$Lambda$ManageApplications$ApplicationsAdapter$u5dJjouyXSv-EkAyCVJkIO0SEV0(applicationsAdapter, entries));
            }
            Log.d(ManageApplications.TAG, "postOnBackgroundThread rebuild end");
        }

        @VisibleForTesting
        static boolean shouldUseStableItemHeight(int listType) {
            if (listType != 1) {
                return true;
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:10:0x0019, code skipped:
            return false;
     */
        private static boolean packageNameEquals(android.content.pm.PackageItemInfo r2, android.content.pm.PackageItemInfo r3) {
            /*
            r0 = 0;
            if (r2 == 0) goto L_0x0019;
        L_0x0003:
            if (r3 != 0) goto L_0x0006;
        L_0x0005:
            goto L_0x0019;
        L_0x0006:
            r1 = r2.packageName;
            if (r1 == 0) goto L_0x0018;
        L_0x000a:
            r1 = r3.packageName;
            if (r1 != 0) goto L_0x000f;
        L_0x000e:
            goto L_0x0018;
        L_0x000f:
            r0 = r2.packageName;
            r1 = r3.packageName;
            r0 = r0.equals(r1);
            return r0;
        L_0x0018:
            return r0;
        L_0x0019:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.manageapplications.ManageApplications$ApplicationsAdapter.packageNameEquals(android.content.pm.PackageItemInfo, android.content.pm.PackageItemInfo):boolean");
        }

        private ArrayList<AppEntry> removeDuplicateIgnoringUser(ArrayList<AppEntry> entries) {
            int size = entries.size();
            ArrayList<AppEntry> returnEntries = new ArrayList(size);
            PackageItemInfo lastInfo = null;
            for (int i = 0; i < size; i++) {
                AppEntry appEntry = (AppEntry) entries.get(i);
                PackageItemInfo info = appEntry.info;
                if (!packageNameEquals(lastInfo, appEntry.info)) {
                    returnEntries.add(appEntry);
                }
                lastInfo = info;
            }
            returnEntries.trimToSize();
            return returnEntries;
        }

        public void onRebuildComplete(ArrayList<AppEntry> entries) {
            int filterType = this.mAppFilter.getFilterType();
            if (filterType == 0 || filterType == 1) {
                entries = removeDuplicateIgnoringUser(entries);
            }
            this.mEntries = entries;
            notifyDataSetChanged();
            boolean z = false;
            if (getItemCount() == 0) {
                this.mManageApplications.mRecyclerView.setVisibility(8);
                this.mManageApplications.mEmptyView.setVisibility(0);
            } else {
                this.mManageApplications.mEmptyView.setVisibility(8);
                this.mManageApplications.mRecyclerView.setVisibility(0);
            }
            if (this.mLastIndex != -1 && getItemCount() > this.mLastIndex) {
                this.mManageApplications.mRecyclerView.getLayoutManager().scrollToPosition(this.mLastIndex);
                this.mLastIndex = -1;
            }
            String str = ManageApplications.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onRebuildComplete mSession.getAllApps().size() = ");
            stringBuilder.append(this.mSession.getAllApps().size());
            stringBuilder.append("mManageApplications.mListContainer is VISIBLE = ");
            if (this.mManageApplications.mListContainer.getVisibility() != 0) {
                z = true;
            }
            stringBuilder.append(z);
            Log.d(str, stringBuilder.toString());
            if (!(this.mSession.getAllApps().size() == 0 || this.mManageApplications.mListContainer.getVisibility() == 0)) {
                this.mLoadingViewController.showContent(true);
            }
            if (this.mManageApplications.mListType != 4) {
                this.mManageApplications.setHasDisabled(this.mState.haveDisabledApps());
                this.mManageApplications.setHasInstant(this.mState.haveInstantApps());
            }
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public void updateLoading() {
            String str = ManageApplications.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateLoading mHasReceivedLoadEntries = ");
            stringBuilder.append(this.mHasReceivedLoadEntries);
            stringBuilder.append(" mSession.getAllApps().size() = ");
            stringBuilder.append(this.mSession.getAllApps().size());
            Log.d(str, stringBuilder.toString());
            boolean appLoaded = this.mHasReceivedLoadEntries && this.mSession.getAllApps().size() != 0;
            if (appLoaded) {
                this.mLoadingViewController.showContent(false);
            } else {
                this.mLoadingViewController.showLoadingViewDelayed();
            }
        }

        public void onExtraInfoUpdated() {
            this.mHasReceivedBridgeCallback = true;
            rebuild();
        }

        public void onRunningStateChanged(boolean running) {
            this.mManageApplications.getActivity().setProgressBarIndeterminateVisibility(running);
        }

        public void onPackageListChanged() {
            rebuild();
        }

        public void onPackageIconChanged() {
        }

        public void onLoadEntriesCompleted() {
            this.mHasReceivedLoadEntries = true;
            rebuild();
        }

        public void onPackageSizeChanged(String packageName) {
            if (this.mEntries != null) {
                int size = this.mEntries.size();
                for (int i = 0; i < size; i++) {
                    ApplicationInfo info = ((AppEntry) this.mEntries.get(i)).info;
                    if (info != null || TextUtils.equals(packageName, info.packageName)) {
                        if (TextUtils.equals(this.mManageApplications.mCurrentPkgName, info.packageName)) {
                            rebuild();
                            return;
                        }
                        this.mOnScrollListener.postNotifyItemChange(i);
                    }
                }
            }
        }

        public void onLauncherInfoChanged() {
            if (!this.mManageApplications.mShowSystem) {
                rebuild();
            }
        }

        public void onAllSizesComputed() {
            if (this.mLastSortMode == R.id.sort_order_size) {
                rebuild();
            }
        }

        public void onExtraViewCompleted() {
            if (hasExtraView()) {
                notifyItemChanged(getItemCount() - 1);
            }
        }

        public int getItemCount() {
            if (this.mEntries == null) {
                return 0;
            }
            return this.mEntries.size() + hasExtraView();
        }

        public int getApplicationCount() {
            return this.mEntries != null ? this.mEntries.size() : 0;
        }

        public AppEntry getAppEntry(int position) {
            return (AppEntry) this.mEntries.get(position);
        }

        public long getItemId(int position) {
            if (position == this.mEntries.size()) {
                return -1;
            }
            return ((AppEntry) this.mEntries.get(position)).id;
        }

        public boolean isEnabled(int position) {
            if (getItemViewType(position) == 1 || this.mManageApplications.mListType != 5) {
                return true;
            }
            return 1 ^ PowerWhitelistBackend.getInstance(this.mContext).isSysWhitelisted(((AppEntry) this.mEntries.get(position)).info.packageName);
        }

        public void onBindViewHolder(ApplicationViewHolder holder, int position) {
            if (this.mEntries == null || this.mExtraViewController == null || position != this.mEntries.size()) {
                AppEntry entry = (AppEntry) this.mEntries.get(position);
                synchronized (entry) {
                    holder.setTitle(entry.label);
                    this.mState.ensureIcon(entry);
                    holder.setIcon(entry.icon);
                    updateSummary(holder, entry);
                    updateSwitch(holder, entry);
                    holder.updateDisableView(entry.info);
                }
                holder.setEnabled(isEnabled(position));
            } else {
                this.mExtraViewController.setupView(holder);
            }
            holder.itemView.setOnClickListener(this.mManageApplications);
        }

        private void updateSummary(ApplicationViewHolder holder, AppEntry entry) {
            int i = this.mManageApplications.mListType;
            boolean z = true;
            if (i != 1) {
                switch (i) {
                    case 4:
                        if (entry.extraInfo != null) {
                            try {
                                if (new UsageState((PermissionState) entry.extraInfo).isPermissible()) {
                                    i = R.string.app_permission_summary_allowed;
                                } else {
                                    i = R.string.app_permission_summary_not_allowed;
                                }
                                holder.setSummary(i);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                        holder.setSummary(null);
                        return;
                    case 5:
                        holder.setSummary(HighPowerDetail.getSummary(this.mContext, entry));
                        return;
                    case 6:
                        holder.setSummary(DrawOverlayDetails.getSummary(this.mContext, entry));
                        return;
                    case 7:
                        holder.setSummary(WriteSettingsDetails.getSummary(this.mContext, entry));
                        return;
                    case 8:
                        holder.setSummary(ExternalSourcesDetails.getPreferenceSummary(this.mContext, entry));
                        return;
                    default:
                        switch (i) {
                            case 12:
                                holder.setSummary(null);
                                return;
                            case 13:
                                holder.setSummary(ChangeWifiStateDetails.getSummary(this.mContext, entry));
                                return;
                            case 14:
                                holder.setSummary(DisplaySizeAdaptionDetail.getSummary(this.mContext, entry));
                                return;
                            case 15:
                                holder.setSummary(BgOptimizeDetail.getSummary(this.mContext, entry));
                                return;
                            default:
                                holder.updateSizeText(entry, this.mManageApplications.mInvalidSizeStr, this.mWhichSize);
                                return;
                        }
                }
            } else if (entry.extraInfo != null) {
                Context context = this.mContext;
                NotificationsSentState notificationsSentState = (NotificationsSentState) entry.extraInfo;
                if (this.mLastSortMode != R.id.sort_order_recent_notification) {
                    z = false;
                }
                holder.setSummary(AppStateNotificationBridge.getSummary(context, notificationsSentState, z));
            } else {
                holder.setSummary(null);
            }
        }

        private void updateSwitch(ApplicationViewHolder holder, AppEntry entry) {
            boolean z = true;
            if (this.mManageApplications.mListType == 1) {
                if (OPConstants.PACKAGENAME_DESKCOLCK.equals(entry.info.packageName) || OPConstants.PACKAGENAME_INCALLUI.equals(entry.info.packageName) || "com.google.android.calendar".equals(entry.info.packageName) || "com.oneplus.calendar".equals(entry.info.packageName) || OPConstants.PACKAGENAME_DIALER.equals(entry.info.packageName)) {
                    holder.updateSwitch(((AppStateNotificationBridge) this.mExtraInfoBridge).getSwitchOnClickListener(entry), false, AppStateNotificationBridge.checkSwitch(entry));
                } else {
                    holder.updateSwitch(((AppStateNotificationBridge) this.mExtraInfoBridge).getSwitchOnClickListener(entry), AppStateNotificationBridge.enableSwitch(entry), AppStateNotificationBridge.checkSwitch(entry));
                }
                if (entry.extraInfo != null) {
                    Context context = this.mContext;
                    NotificationsSentState notificationsSentState = (NotificationsSentState) entry.extraInfo;
                    if (this.mLastSortMode != R.id.sort_order_recent_notification) {
                        z = false;
                    }
                    holder.setSummary(AppStateNotificationBridge.getSummary(context, notificationsSentState, z));
                    return;
                }
                holder.setSummary(null);
            }
        }

        private boolean hasExtraView() {
            return this.mExtraViewController != null && this.mExtraViewController.shouldShow();
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                new InstalledAppCounter(this.mContext, -1, new PackageManagerWrapper(this.mContext.getPackageManager())) {
                    /* Access modifiers changed, original: protected */
                    public void onCountComplete(int num) {
                        SummaryProvider.this.mLoader.setSummary(SummaryProvider.this, SummaryProvider.this.mContext.getString(R.string.apps_summary, new Object[]{Integer.valueOf(num)}));
                    }
                }.execute(new Void[0]);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Activity activity = getActivity();
        this.mApplicationsState = ApplicationsState.getInstance(activity.getApplication());
        Intent intent = activity.getIntent();
        Bundle args = getArguments();
        int screenTitle = intent.getIntExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, R.string.application_info_label);
        String className = args != null ? args.getString(EXTRA_CLASSNAME) : null;
        if (className == null) {
            className = intent.getComponent().getClassName();
        }
        int i = -1;
        boolean z = false;
        if (className.equals(StorageUseActivity.class.getName())) {
            if (args == null || !args.containsKey(EXTRA_VOLUME_UUID)) {
                this.mListType = 0;
            } else {
                this.mVolumeUuid = args.getString(EXTRA_VOLUME_UUID);
                this.mStorageType = args.getInt(EXTRA_STORAGE_TYPE, 0);
                this.mListType = 3;
            }
            this.mSortOrder = R.id.sort_order_size;
        } else if (className.equals(UsageAccessSettingsActivity.class.getName())) {
            this.mListType = 4;
            screenTitle = R.string.usage_access;
        } else if (className.equals(HighPowerApplicationsActivity.class.getName())) {
            this.mListType = 5;
            this.mShowSystem = true;
            screenTitle = R.string.high_power_apps;
        } else if (className.equals(OverlaySettingsActivity.class.getName())) {
            this.mListType = 6;
            screenTitle = R.string.system_alert_window_settings;
        } else if (className.equals(WriteSettingsActivity.class.getName())) {
            this.mListType = 7;
            screenTitle = R.string.write_settings;
        } else if (className.equals(ManageExternalSourcesActivity.class.getName())) {
            this.mListType = 8;
            screenTitle = R.string.install_other_apps;
        } else if (className.equals(GamesStorageActivity.class.getName())) {
            this.mListType = 9;
            this.mSortOrder = R.id.sort_order_size;
        } else if (className.equals(MoviesStorageActivity.class.getName())) {
            this.mListType = 10;
            this.mSortOrder = R.id.sort_order_size;
        } else if (className.equals(PhotosStorageActivity.class.getName())) {
            this.mListType = 11;
            this.mSortOrder = R.id.sort_order_size;
            this.mStorageType = args.getInt(EXTRA_STORAGE_TYPE, 0);
        } else if (className.equals(DirectoryAccessSettingsActivity.class.getName())) {
            this.mListType = 12;
            screenTitle = R.string.directory_access;
        } else if (className.equals(ChangeWifiStateActivity.class.getName())) {
            this.mListType = 13;
            screenTitle = R.string.change_wifi_state_title;
        } else if (className.equals(NotificationAppListActivity.class.getName())) {
            this.mListType = 1;
            this.mUsageStatsManager = Stub.asInterface(ServiceManager.getService("usagestats"));
            this.mUserManager = UserManager.get(getContext());
            this.mNotificationBackend = new NotificationBackend();
            this.mSortOrder = R.id.sort_order_recent_notification;
            screenTitle = R.string.app_notifications_title;
        } else if (className.equals(DisplaySizeAdaptionAppListActivity.class.getName())) {
            this.mListType = 14;
            screenTitle = R.string.oneplus_app_display_fullscreen_title;
        } else if (className.equals(BgOptimizeAppListActivity.class.getName())) {
            this.mListType = 15;
            screenTitle = R.string.high_power_apps;
        } else {
            if (screenTitle == -1) {
                screenTitle = R.string.application_info_label;
            }
            this.mListType = 0;
        }
        AppFilterRegistry appFilterRegistry = AppFilterRegistry.getInstance();
        this.mFilter = appFilterRegistry.get(appFilterRegistry.getDefaultFilterType(this.mListType));
        if (args != null) {
            z = args.getBoolean(EXTRA_WORK_ONLY);
        }
        this.mIsWorkOnly = z;
        if (args != null) {
            i = args.getInt(EXTRA_WORK_ID);
        }
        this.mWorkUserId = i;
        if (savedInstanceState != null) {
            this.mSortOrder = savedInstanceState.getInt(EXTRA_SORT_ORDER, this.mSortOrder);
            this.mShowSystem = savedInstanceState.getBoolean(EXTRA_SHOW_SYSTEM, this.mShowSystem);
        }
        this.mInvalidSizeStr = activity.getText(R.string.invalid_size_value);
        this.mResetAppsHelper = new ResetAppsHelper(activity);
        if (screenTitle > 0) {
            activity.setTitle(screenTitle);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.manage_applications_apps, null);
        this.mLoadingContainer = this.mRootView.findViewById(R.id.loading_container);
        this.mListContainer = this.mRootView.findViewById(R.id.list_container);
        if (this.mListContainer != null) {
            this.mEmptyView = this.mListContainer.findViewById(16908292);
            this.mApplications = new ApplicationsAdapter(this.mApplicationsState, this, this.mFilter, savedInstanceState);
            if (savedInstanceState != null) {
                this.mApplications.mHasReceivedLoadEntries = savedInstanceState.getBoolean(EXTRA_HAS_ENTRIES, false);
                this.mApplications.mHasReceivedBridgeCallback = savedInstanceState.getBoolean(EXTRA_HAS_BRIDGE, false);
            }
            int userId = this.mIsWorkOnly ? this.mWorkUserId : UserHandle.getUserId(this.mCurrentUid);
            Context context;
            if (this.mStorageType == 1) {
                context = getContext();
                this.mApplications.setExtraViewController(new MusicViewHolderController(context, new StorageStatsSource(context), this.mVolumeUuid, UserHandle.of(userId)));
            } else if (this.mStorageType == 3) {
                context = getContext();
                this.mApplications.setExtraViewController(new PhotosViewHolderController(context, new StorageStatsSource(context), this.mVolumeUuid, UserHandle.of(userId)));
            }
            this.mRecyclerView = (RecyclerView) this.mListContainer.findViewById(R.id.apps_list);
            this.mRecyclerView.setItemAnimator(null);
            this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
            this.mRecyclerView.setAdapter(this.mApplications);
        }
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) this.mRootView.getLayoutParams()).removeBorders = true;
        }
        createHeader();
        this.mResetAppsHelper.onRestoreInstanceState(savedInstanceState);
        return this.mRootView;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void createHeader() {
        FrameLayout pinnedHeader = (FrameLayout) this.mRootView.findViewById(R.id.pinned_header);
        this.mSpinnerHeader = getActivity().getLayoutInflater().inflate(R.layout.apps_filter_spinner, pinnedHeader, false);
        this.mFilterSpinner = (Spinner) this.mSpinnerHeader.findViewById(R.id.filter_spinner);
        this.mFilterAdapter = new FilterSpinnerAdapter(this);
        this.mFilterSpinner.setAdapter(this.mFilterAdapter);
        this.mFilterSpinner.setOnItemSelectedListener(this);
        pinnedHeader.addView(this.mSpinnerHeader, 0);
        AppFilterRegistry appFilterRegistry = AppFilterRegistry.getInstance();
        this.mFilterAdapter.enableFilter(appFilterRegistry.getDefaultFilterType(this.mListType));
        if (this.mListType == 0) {
            boolean showWorkApps = false;
            if (OPUtils.hasMultiAppProfiles(UserManager.get(getActivity()))) {
                if (UserManager.get(getActivity()).getUserProfiles().size() > 2) {
                    showWorkApps = true;
                }
            } else if (UserManager.get(getActivity()).getUserProfiles().size() > 1) {
                showWorkApps = true;
            }
            if (showWorkApps) {
                this.mFilterAdapter.enableFilter(8);
                this.mFilterAdapter.enableFilter(9);
            }
        }
        if (this.mListType == 1) {
            this.mFilterAdapter.enableFilter(6);
            this.mFilterAdapter.enableFilter(7);
            this.mFilterAdapter.disableFilter(2);
        }
        if (this.mListType == 5) {
            this.mFilterAdapter.enableFilter(1);
        }
        if (this.mListType == 14) {
            this.mFilterAdapter.disableFilter(2);
            this.mFilterAdapter.enableFilter(16);
            this.mFilterAdapter.enableFilter(17);
            this.mFilterAdapter.enableFilter(18);
        }
        if (this.mListType == 15) {
            this.mFilterAdapter.disableFilter(2);
            this.mFilterAdapter.enableFilter(19);
            this.mFilterAdapter.enableFilter(20);
        }
        AppFilter compositeFilter = getCompositeFilter(this.mListType, this.mStorageType, this.mVolumeUuid);
        if (this.mIsWorkOnly) {
            compositeFilter = new CompoundFilter(compositeFilter, appFilterRegistry.get(9).getFilter());
        }
        if (compositeFilter != null) {
            this.mApplications.setCompositeFilter(compositeFilter);
        }
    }

    @VisibleForTesting
    static AppFilter getCompositeFilter(int listType, int storageType, String volumeUuid) {
        AppFilter filter = new VolumeFilter(volumeUuid);
        if (listType == 3) {
            if (storageType == 1) {
                filter = new CompoundFilter(ApplicationsState.FILTER_AUDIO, filter);
            } else if (storageType == 0) {
                filter = new CompoundFilter(ApplicationsState.FILTER_OTHER_APPS, filter);
            }
            return filter;
        } else if (listType == 9) {
            return new CompoundFilter(ApplicationsState.FILTER_GAMES, filter);
        } else {
            if (listType == 10) {
                return new CompoundFilter(ApplicationsState.FILTER_MOVIES, filter);
            }
            if (listType == 11) {
                return new CompoundFilter(ApplicationsState.FILTER_PHOTOS, filter);
            }
            return null;
        }
    }

    public int getMetricsCategory() {
        switch (this.mListType) {
            case 0:
                return 65;
            case 1:
                return Const.CODE_C1_CW5;
            case 3:
                if (this.mStorageType == 1) {
                    return 839;
                }
                return 182;
            case 4:
                return 95;
            case 5:
                return 184;
            case 6:
                return 221;
            case 7:
                return 221;
            case 8:
                return 808;
            case 9:
                return 838;
            case 10:
                return 935;
            case 11:
                return 1092;
            case 12:
                return 1283;
            case 13:
                return 338;
            case 14:
                return 65;
            case 15:
                return 65;
            default:
                return 0;
        }
    }

    public void onStart() {
        super.onStart();
        updateView();
        if (this.mApplications != null) {
            this.mApplications.resume(this.mSortOrder);
            this.mApplications.updateLoading();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mResetAppsHelper.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SORT_ORDER, this.mSortOrder);
        outState.putBoolean(EXTRA_SHOW_SYSTEM, this.mShowSystem);
        outState.putBoolean(EXTRA_HAS_ENTRIES, this.mApplications.mHasReceivedLoadEntries);
        outState.putBoolean(EXTRA_HAS_BRIDGE, this.mApplications.mHasReceivedBridgeCallback);
        if (this.mApplications != null) {
            this.mApplications.onSaveInstanceState(outState);
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mApplications != null) {
            this.mApplications.pause();
        }
        this.mResetAppsHelper.stop();
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mApplications != null) {
            this.mApplications.release();
        }
        this.mRootView = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && this.mCurrentPkgName != null) {
            if (this.mListType == 1) {
                this.mApplications.mExtraInfoBridge.forceUpdate(this.mCurrentPkgName, this.mCurrentUid);
            } else if (this.mListType == 5 || this.mListType == 6 || this.mListType == 7 || this.mListType == 14 || this.mListType == 15) {
                this.mApplications.mExtraInfoBridge.forceUpdate(this.mCurrentPkgName, this.mCurrentUid);
            } else {
                this.mApplicationsState.requestSize(this.mCurrentPkgName, UserHandle.getUserId(this.mCurrentUid));
            }
        }
    }

    private void startApplicationDetailsActivity() {
        int i = this.mListType;
        if (i != 1) {
            switch (i) {
                case 3:
                    startAppInfoFragment(AppStorageSettings.class, R.string.storage_settings);
                    return;
                case 4:
                    startAppInfoFragment(UsageAccessDetails.class, R.string.usage_access);
                    return;
                case 5:
                    HighPowerDetail.show(this, this.mCurrentUid, this.mCurrentPkgName, 1);
                    return;
                case 6:
                    startAppInfoFragment(DrawOverlayDetails.class, R.string.overlay_settings);
                    return;
                case 7:
                    startAppInfoFragment(WriteSettingsDetails.class, R.string.write_system_settings);
                    return;
                case 8:
                    startAppInfoFragment(ExternalSourcesDetails.class, R.string.install_other_apps);
                    return;
                case 9:
                    startAppInfoFragment(AppStorageSettings.class, R.string.game_storage_settings);
                    return;
                case 10:
                    startAppInfoFragment(AppStorageSettings.class, R.string.storage_movies_tv);
                    return;
                case 11:
                    startAppInfoFragment(AppStorageSettings.class, R.string.storage_photos_videos);
                    return;
                case 12:
                    startAppInfoFragment(DirectoryAccessDetails.class, R.string.directory_access);
                    return;
                case 13:
                    startAppInfoFragment(ChangeWifiStateDetails.class, R.string.change_wifi_state_title);
                    return;
                case 14:
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        DisplaySizeAdaptionDetail.show(this, this.mCurrentPkgName, 1);
                        return;
                    }
                    return;
                case 15:
                    Activity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        BgOptimizeDetail.show(this, this.mCurrentPkgName, 1);
                        return;
                    }
                    return;
                default:
                    startAppInfoFragment(AppInfoDashboardFragment.class, R.string.application_info_label);
                    return;
            }
        }
        startAppInfoFragment(AppNotificationSettings.class, R.string.notifications_title);
    }

    private void startAppInfoFragment(Class<?> fragment, int titleRes) {
        AppInfoBase.startAppInfoFragment(fragment, titleRes, this.mCurrentPkgName, this.mCurrentUid, this, 1, getMetricsCategory());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Activity activity = getActivity();
        if (activity != null && this.mListType != 14) {
            HelpUtils.prepareHelpMenuItem(activity, menu, getHelpResource(), getClass().getName());
            this.mOptionsMenu = menu;
            inflater.inflate(R.menu.manage_apps, menu);
            updateOptionsMenu();
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    public void onDestroyOptionsMenu() {
        this.mOptionsMenu = null;
    }

    /* Access modifiers changed, original: 0000 */
    public int getHelpResource() {
        if (this.mListType == 0) {
            return R.string.help_uri_apps;
        }
        if (this.mListType == 4) {
            return R.string.help_url_usage_access;
        }
        return R.string.help_uri_notifications;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:34:0x00b5, code skipped:
            if (android.util.OpFeatures.isSupport(new int[]{85}) == false) goto L_0x00b9;
     */
    public void updateOptionsMenu() {
        /*
        r8 = this;
        r0 = r8.mOptionsMenu;
        if (r0 != 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r0 = r8.mOptionsMenu;
        r1 = 2131361870; // 0x7f0a004e float:1.8343505E38 double:1.053032679E-314;
        r0 = r0.findItem(r1);
        r1 = 0;
        r0.setVisible(r1);
        r0 = r8.mOptionsMenu;
        r2 = 2131363082; // 0x7f0a050a float:1.8345963E38 double:1.0530332776E-314;
        r0 = r0.findItem(r2);
        r3 = r8.mListType;
        r4 = 3;
        r5 = 1;
        if (r3 != r4) goto L_0x0027;
    L_0x0021:
        r3 = r8.mSortOrder;
        if (r3 == r2) goto L_0x0027;
    L_0x0025:
        r2 = r5;
        goto L_0x0028;
    L_0x0027:
        r2 = r1;
    L_0x0028:
        r0.setVisible(r2);
        r0 = r8.mOptionsMenu;
        r2 = 2131363085; // 0x7f0a050d float:1.8345969E38 double:1.053033279E-314;
        r0 = r0.findItem(r2);
        r3 = r8.mListType;
        if (r3 != r4) goto L_0x003e;
    L_0x0038:
        r3 = r8.mSortOrder;
        if (r3 == r2) goto L_0x003e;
    L_0x003c:
        r2 = r5;
        goto L_0x003f;
    L_0x003e:
        r2 = r1;
    L_0x003f:
        r0.setVisible(r2);
        r0 = r8.mOptionsMenu;
        r2 = 2131363060; // 0x7f0a04f4 float:1.8345918E38 double:1.053033267E-314;
        r0 = r0.findItem(r2);
        r3 = r8.mShowSystem;
        r4 = 5;
        if (r3 != 0) goto L_0x0056;
    L_0x0050:
        r3 = r8.mListType;
        if (r3 == r4) goto L_0x0056;
    L_0x0054:
        r3 = r5;
        goto L_0x0057;
    L_0x0056:
        r3 = r1;
    L_0x0057:
        r0.setVisible(r3);
        r0 = r8.mOptionsMenu;
        r3 = 2131362387; // 0x7f0a0253 float:1.8344553E38 double:1.0530329343E-314;
        r0 = r0.findItem(r3);
        r3 = r8.mShowSystem;
        if (r3 == 0) goto L_0x006d;
    L_0x0067:
        r3 = r8.mListType;
        if (r3 == r4) goto L_0x006d;
    L_0x006b:
        r3 = r5;
        goto L_0x006e;
    L_0x006d:
        r3 = r1;
    L_0x006e:
        r0.setVisible(r3);
        r0 = r8.mOptionsMenu;
        r3 = 2131362942; // 0x7f0a047e float:1.8345679E38 double:1.0530332085E-314;
        r0 = r0.findItem(r3);
        r4 = r8.mListType;
        if (r4 != 0) goto L_0x0080;
    L_0x007e:
        r4 = r5;
        goto L_0x0081;
    L_0x0080:
        r4 = r1;
    L_0x0081:
        r0.setVisible(r4);
        r0 = r8.mOptionsMenu;
        r4 = 2131363084; // 0x7f0a050c float:1.8345967E38 double:1.0530332786E-314;
        r0 = r0.findItem(r4);
        r0.setVisible(r1);
        r0 = r8.mOptionsMenu;
        r4 = 2131363083; // 0x7f0a050b float:1.8345965E38 double:1.053033278E-314;
        r0 = r0.findItem(r4);
        r0.setVisible(r1);
        r0 = r8.mOptionsMenu;
        r4 = 2131361948; // 0x7f0a009c float:1.8343663E38 double:1.0530327174E-314;
        r0 = r0.findItem(r4);
        r4 = r8.mListType;
        r6 = 15;
        if (r4 != r6) goto L_0x00b8;
    L_0x00ab:
        r4 = new int[r5];
        r7 = 85;
        r4[r1] = r7;
        r4 = android.util.OpFeatures.isSupport(r4);
        if (r4 != 0) goto L_0x00b8;
    L_0x00b7:
        goto L_0x00b9;
    L_0x00b8:
        r5 = r1;
    L_0x00b9:
        r0.setVisible(r5);
        r0 = r8.mListType;
        if (r0 != r6) goto L_0x00d2;
    L_0x00c0:
        r0 = r8.mOptionsMenu;
        r0 = r0.findItem(r2);
        r0.setVisible(r1);
        r0 = r8.mOptionsMenu;
        r0 = r0.findItem(r3);
        r0.setVisible(r1);
    L_0x00d2:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.manageapplications.ManageApplications.updateOptionsMenu():void");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (item.getItemId()) {
            case R.id.advanced /*2131361870*/:
                if (this.mListType == 1) {
                    new SubSettingLauncher(getContext()).setDestination(ConfigureNotificationSettings.class.getName()).setTitle((int) R.string.configure_notification_settings).setSourceMetricsCategory(getMetricsCategory()).setResultListener(this, 2).launch();
                } else {
                    new SubSettingLauncher(getContext()).setDestination(DefaultAppSettings.class.getName()).setTitle((int) R.string.configure_apps).setSourceMetricsCategory(getMetricsCategory()).setResultListener(this, 2).launch();
                }
                return true;
            case R.id.bg_optimize_preferences /*2131361948*/:
                Intent intent = null;
                try {
                    intent = new Intent("com.android.settings.action.BACKGROUND_OPTIMIZE_SWITCH");
                    intent.putExtra(EXTRA_CLASSNAME, BgOptimizeSwitchActivity.class.getName());
                    getActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("No activity found for ");
                    stringBuilder.append(intent);
                    Log.d(str, stringBuilder.toString());
                }
                return true;
            case R.id.hide_system /*2131362387*/:
            case R.id.show_system /*2131363060*/:
                this.mShowSystem ^= 1;
                this.mApplications.rebuild();
                break;
            case R.id.reset_app_preferences /*2131362942*/:
                this.mResetAppsHelper.buildResetDialog();
                return true;
            case R.id.sort_order_alpha /*2131363082*/:
            case R.id.sort_order_size /*2131363085*/:
                if (this.mApplications != null) {
                    this.mApplications.rebuild(menuId);
                    break;
                }
                break;
            default:
                return false;
        }
        updateOptionsMenu();
        return true;
    }

    public void onClick(View view) {
        if (this.mApplications != null) {
            int position = this.mRecyclerView.getChildAdapterPosition(view);
            if (position == -1) {
                Log.w(TAG, "Cannot find position for child, skipping onClick handling");
                return;
            }
            if (this.mApplications.getApplicationCount() > position) {
                AppEntry entry = this.mApplications.getAppEntry(position);
                this.mCurrentPkgName = entry.info.packageName;
                this.mCurrentUid = entry.info.uid;
                startApplicationDetailsActivity();
            } else {
                this.mApplications.mExtraViewController.onClick(this);
            }
        }
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mFilter = this.mFilterAdapter.getFilter(position);
        this.mApplications.setFilter(this.mFilter);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Selecting filter ");
            stringBuilder.append(this.mFilter);
            Log.d(str, stringBuilder.toString());
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void updateView() {
        updateOptionsMenu();
        Activity host = getActivity();
        if (host != null) {
            host.invalidateOptionsMenu();
        }
    }

    public void setHasDisabled(boolean hasDisabledApps) {
        if (this.mListType == 0) {
            this.mFilterAdapter.setFilterEnabled(3, hasDisabledApps);
            this.mFilterAdapter.setFilterEnabled(5, hasDisabledApps);
        }
    }

    public void setHasInstant(boolean haveInstantApps) {
        if (LIST_TYPES_WITH_INSTANT.contains(Integer.valueOf(this.mListType))) {
            this.mFilterAdapter.setFilterEnabled(4, haveInstantApps);
        }
    }
}
