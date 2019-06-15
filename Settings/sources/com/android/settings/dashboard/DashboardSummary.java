package com.android.settings.dashboard;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionManager;
import com.android.settings.dashboard.conditional.ConditionManager.ConditionListener;
import com.android.settings.dashboard.conditional.FocusRecyclerView;
import com.android.settings.dashboard.conditional.FocusRecyclerView.FocusListener;
import com.android.settings.dashboard.suggestions.SuggestionFeatureProvider;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.ActionBarShadowController;
import com.android.settingslib.drawer.CategoryKey;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.suggestions.SuggestionControllerMixin;
import com.android.settingslib.suggestions.SuggestionControllerMixin.SuggestionControllerHost;
import com.android.settingslib.utils.ThreadUtils;
import com.oneplus.setting.lib.SettingsDrawerActivity;
import com.oneplus.setting.lib.SettingsDrawerActivity.CategoryListener;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class DashboardSummary extends InstrumentedFragment implements CategoryListener, ConditionListener, FocusListener, SuggestionControllerHost {
    public static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;
    public static final String HAS_NEW_VERSION_TO_UPDATE = "has_new_version_to_update";
    private static final int MAX_WAIT_MILLIS = 3000;
    private static final String STATE_CATEGORIES_CHANGE_CALLED = "categories_change_called";
    private static final String STATE_SCROLL_POSITION = "scroll_position";
    private static final String TAG = "DashboardSummary";
    private final Uri ALL_DOWNLOAD_FILES_URI = Uri.parse("content://com.oneplus.ap.upgrader.provider/all_download_files");
    private DashboardAdapter mAdapter;
    private ConditionManager mConditionManager;
    private FocusRecyclerView mDashboard;
    private DashboardFeatureProvider mDashboardFeatureProvider;
    private final Handler mHandler = new Handler();
    @VisibleForTesting
    boolean mIsOnCategoriesChangedCalled;
    private LinearLayoutManager mLayoutManager;
    private boolean mOnConditionsChangedCalled;
    private ContentResolver mResolver;
    private DashboardCategory mStagingCategory;
    private List<Suggestion> mStagingSuggestions;
    private SuggestionControllerMixin mSuggestionControllerMixin;
    private SummaryLoader mSummaryLoader;
    private SystemUpdateObserver mSystemUpdateObserver;

    private class SystemUpdateObserver extends ContentObserver {
        private final Uri SYSTEM_UPDATE_URI = System.getUriFor("has_new_version_to_update");
        private final Uri ZEN_MODE = Global.getUriFor("zen_mode");

        public SystemUpdateObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (!selfChange) {
                if (this.SYSTEM_UPDATE_URI.equals(uri) || DashboardSummary.this.ALL_DOWNLOAD_FILES_URI.equals(uri)) {
                    DashboardSummary.this.rebuildUI();
                }
            }
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void startObserving() {
            ContentResolver cr = DashboardSummary.this.getContext().getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(this.SYSTEM_UPDATE_URI, false, this, -1);
            if (OPUtils.isAppPakExist(DashboardSummary.this.getActivity(), "com.oneplus.appupgrader")) {
                cr.registerContentObserver(DashboardSummary.this.ALL_DOWNLOAD_FILES_URI, false, this, -1);
            }
        }

        public void stopObserving() {
            DashboardSummary.this.getContext().getContentResolver().unregisterContentObserver(this);
        }
    }

    public int getMetricsCategory() {
        return 35;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "Creating SuggestionControllerMixin");
        SuggestionFeatureProvider suggestionFeatureProvider = FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context);
        if (suggestionFeatureProvider.isSuggestionEnabled(context)) {
            this.mSuggestionControllerMixin = new SuggestionControllerMixin(context, this, getLifecycle(), suggestionFeatureProvider.getSuggestionServiceComponent());
        }
    }

    public LoaderManager getLoaderManager() {
        if (isAdded()) {
            return super.getLoaderManager();
        }
        return null;
    }

    public void onCreate(Bundle savedInstanceState) {
        long startTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting DashboardSummary");
        Activity activity = getActivity();
        this.mDashboardFeatureProvider = FeatureFactory.getFactory(activity).getDashboardFeatureProvider(activity);
        this.mSummaryLoader = new SummaryLoader(activity, CategoryKey.CATEGORY_HOMEPAGE);
        this.mConditionManager = ConditionManager.get(activity, false);
        getLifecycle().addObserver(this.mConditionManager);
        if (savedInstanceState != null) {
            this.mIsOnCategoriesChangedCalled = savedInstanceState.getBoolean(STATE_CATEGORIES_CHANGE_CALLED);
        }
        if (getContext() != null) {
            this.mResolver = getContext().getContentResolver();
            this.mSystemUpdateObserver = new SystemUpdateObserver(new Handler());
            this.mSystemUpdateObserver.startObserving();
        }
    }

    public void onDestroy() {
        this.mSummaryLoader.release();
        if (this.mSystemUpdateObserver != null) {
            this.mSystemUpdateObserver.stopObserving();
            this.mSystemUpdateObserver = null;
        }
        super.onDestroy();
    }

    public void onResume() {
        long startTime = System.currentTimeMillis();
        super.onResume();
        ((SettingsDrawerActivity) getActivity()).addCategoryListener(this);
        this.mSummaryLoader.setListening(true);
        int metricsCategory = getMetricsCategory();
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                this.mMetricsFeatureProvider.visible(getContext(), metricsCategory, c.getMetricsConstant());
            }
        }
    }

    public void onPause() {
        super.onPause();
        ((SettingsDrawerActivity) getActivity()).remCategoryListener(this);
        this.mSummaryLoader.setListening(false);
        for (Condition c : this.mConditionManager.getConditions()) {
            if (c.shouldShow()) {
                this.mMetricsFeatureProvider.hidden(getContext(), c.getMetricsConstant());
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        long startTime = System.currentTimeMillis();
        if (hasWindowFocus) {
            Log.d(TAG, "Listening for condition changes");
            this.mConditionManager.addListener(this);
            Log.d(TAG, "conditions refreshed");
            this.mConditionManager.refreshAll();
            return;
        }
        Log.d(TAG, "Stopped listening for condition changes");
        this.mConditionManager.remListener(this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mLayoutManager != null) {
            outState.putBoolean(STATE_CATEGORIES_CHANGE_CALLED, this.mIsOnCategoriesChangedCalled);
            outState.putInt(STATE_SCROLL_POSITION, this.mLayoutManager.findFirstVisibleItemPosition());
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        long startTime = System.currentTimeMillis();
        View root = inflater.inflate(R.layout.dashboard, container, false);
        this.mDashboard = (FocusRecyclerView) root.findViewById(R.id.dashboard_container);
        this.mLayoutManager = new LinearLayoutManager(getContext());
        this.mLayoutManager.setOrientation(1);
        if (bundle != null) {
            this.mLayoutManager.scrollToPosition(bundle.getInt(STATE_SCROLL_POSITION));
        }
        this.mDashboard.setLayoutManager(this.mLayoutManager);
        this.mDashboard.setHasFixedSize(true);
        this.mDashboard.setListener(this);
        this.mDashboard.setItemAnimator(new DashboardItemAnimator());
        this.mAdapter = new DashboardAdapter(getContext(), bundle, this.mConditionManager.getConditions(), this.mSuggestionControllerMixin, getLifecycle());
        this.mDashboard.setAdapter(this.mAdapter);
        this.mSummaryLoader.setSummaryConsumer(this.mAdapter);
        ActionBarShadowController.attachToRecyclerView(getActivity().findViewById(R.id.search_bar_container), getLifecycle(), this.mDashboard);
        rebuildUI();
        return root;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void rebuildUI() {
        Log.d(TAG, "rebuildUI");
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$DashboardSummary$8s9N5t2Nn47oUx2XbtJ_BLsLzIY(this));
    }

    public static /* synthetic */ void lambda$rebuildUI$0(DashboardSummary dashboardSummary) {
        Log.d(TAG, "postOnBackgroundThread updateCategory start");
        dashboardSummary.updateCategory();
        Log.d(TAG, "postOnBackgroundThread updateCategory end");
    }

    public void onCategoriesChanged() {
        if (this.mIsOnCategoriesChangedCalled) {
            rebuildUI();
        }
        this.mIsOnCategoriesChangedCalled = true;
    }

    public void onConditionsChanged() {
        Log.d(TAG, "onConditionsChanged");
        boolean z = true;
        if (this.mOnConditionsChangedCalled) {
            if (this.mLayoutManager.findFirstCompletelyVisibleItemPosition() > 1) {
                z = false;
            }
            boolean scrollToTop = z;
            this.mAdapter.setConditions(this.mConditionManager.getConditions());
            if (scrollToTop) {
                this.mDashboard.scrollToPosition(0);
                return;
            }
            return;
        }
        this.mOnConditionsChangedCalled = true;
    }

    public void onSuggestionReady(List<Suggestion> suggestions) {
        this.mStagingSuggestions = suggestions;
        this.mAdapter.setSuggestions(suggestions);
        if (this.mStagingCategory != null) {
            Log.d(TAG, "Category has loaded, setting category from suggestionReady");
            this.mHandler.removeCallbacksAndMessages(null);
            this.mAdapter.setCategory(this.mStagingCategory);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @WorkerThread
    public void updateCategory() {
        Log.d(TAG, "update category");
        DashboardCategory category = this.mDashboardFeatureProvider.getTilesForCategory(CategoryKey.CATEGORY_HOMEPAGE);
        this.mSummaryLoader.updateSummaryToCache(category);
        this.mStagingCategory = category;
        if (this.mAdapter == null) {
            Log.e(TAG, "update category, adapter is null!");
            return;
        }
        boolean systemHasUpdate = false;
        boolean z = true;
        if (getContext() != null) {
            systemHasUpdate = System.getInt(getContext().getContentResolver(), "has_new_version_to_update", 0) == 1;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Has updates? ");
        if (!systemHasUpdate && null == null) {
            z = false;
        }
        stringBuilder.append(z);
        Log.d(str, stringBuilder.toString());
        if (systemHasUpdate || null != null) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DashboardSummary$IEZgZ97m6Eczh4OO9ztmxtZNqM8(this));
        } else {
            ThreadUtils.postOnMainThread(new -$$Lambda$DashboardSummary$S4ZnJYAtpWnSKH5Ya-6PeP-43T4(this));
        }
        if (this.mSuggestionControllerMixin == null) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DashboardSummary$kCUZowpTTsEozF-ygTzgGisYUiM(this));
            return;
        }
        if (this.mSuggestionControllerMixin.isSuggestionLoaded()) {
            Log.d(TAG, "Suggestion has loaded, setting suggestion/category");
            ThreadUtils.postOnMainThread(new -$$Lambda$DashboardSummary$_vJSQ4JiQV1-72BBEMy_dNfqxE4(this));
        } else {
            Log.d(TAG, "Suggestion NOT loaded, delaying setCategory by 3000ms");
            this.mHandler.postDelayed(new -$$Lambda$DashboardSummary$Ok9Mu3hGa9SiRJnEAHBsEN1wR_0(this), 3000);
        }
    }

    public static /* synthetic */ void lambda$updateCategory$4(DashboardSummary dashboardSummary) {
        if (dashboardSummary.mStagingSuggestions != null) {
            dashboardSummary.mAdapter.setSuggestions(dashboardSummary.mStagingSuggestions);
        }
        dashboardSummary.mAdapter.setCategory(dashboardSummary.mStagingCategory);
    }

    /* JADX WARNING: Missing block: B:6:0x0022, code skipped:
            if (r1 != null) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:7:0x0024, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:12:0x002e, code skipped:
            if (r1 == null) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:13:0x0031, code skipped:
            if (r0 <= 0) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:19:?, code skipped:
            return false;
     */
    /* JADX WARNING: Missing block: B:20:?, code skipped:
            return true;
     */
    public boolean hasNewAppVersion() {
        /*
        r8 = this;
        r0 = 0;
        r1 = 0;
        r5 = "canInstall =?";
        r2 = "1";
        r6 = new java.lang.String[]{r2};	 Catch:{ Exception -> 0x002a }
        r2 = r8.getContext();	 Catch:{ Exception -> 0x002a }
        r2 = r2.getContentResolver();	 Catch:{ Exception -> 0x002a }
        r3 = r8.ALL_DOWNLOAD_FILES_URI;	 Catch:{ Exception -> 0x002a }
        r4 = 0;
        r7 = 0;
        r2 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x002a }
        r1 = r2;
        if (r1 == 0) goto L_0x0022;
    L_0x001d:
        r2 = r1.getCount();	 Catch:{ Exception -> 0x002a }
        r0 = r2;
    L_0x0022:
        if (r1 == 0) goto L_0x0031;
    L_0x0024:
        r1.close();
        goto L_0x0031;
    L_0x0028:
        r2 = move-exception;
        goto L_0x0037;
    L_0x002a:
        r2 = move-exception;
        r2.printStackTrace();	 Catch:{ all -> 0x0028 }
        if (r1 == 0) goto L_0x0031;
    L_0x0030:
        goto L_0x0024;
    L_0x0031:
        if (r0 <= 0) goto L_0x0035;
    L_0x0033:
        r2 = 1;
        goto L_0x0036;
    L_0x0035:
        r2 = 0;
    L_0x0036:
        return r2;
    L_0x0037:
        if (r1 == 0) goto L_0x003c;
    L_0x0039:
        r1.close();
    L_0x003c:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.dashboard.DashboardSummary.hasNewAppVersion():boolean");
    }
}
