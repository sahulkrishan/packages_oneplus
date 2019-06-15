package com.oneplus.settings.quicklaunch;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.quickpay.QuickPayLottieAnimPreference.OnPreferenceViewClickListener;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPQuickLaunchListSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener, OnPreferenceViewClickListener {
    private static final String CFGKEY_REMOVE_DIALOG = "showingAppRemoveDialog";
    private static final String CFGKEY_REMOVE_MODE = "appRemoveMode";
    private static final int MENU_ID_REMOVE = 2;
    private OPAppDragAndDropAdapter mAdapter;
    private Button mAddView;
    private List<OPAppModel> mAppList = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPQuickLaunchListSettings.this.mAdapter != null && OPQuickLaunchListSettings.this.mOPApplicationLoader != null) {
                OPQuickLaunchListSettings.this.mAppList.clear();
                OPQuickLaunchListSettings.this.mAppList.addAll(OPQuickLaunchListSettings.this.mOPApplicationLoader.getAppListByType(msg.what));
                OPQuickLaunchListSettings.this.mAdapter.setAppList(OPQuickLaunchListSettings.this.mAppList);
                OPAppLinearLayoutManager llm = new OPAppLinearLayoutManager(OPQuickLaunchListSettings.this.mContext, OPQuickLaunchListSettings.this.mAdapter);
                llm.setAutoMeasureEnabled(true);
                OPQuickLaunchListSettings.this.mListView.setLayoutManager(llm);
                OPQuickLaunchListSettings.this.mListView.setHasFixedSize(true);
                OPQuickLaunchListSettings.this.mAdapter.setRecyclerView(OPQuickLaunchListSettings.this.mListView);
                OPQuickLaunchListSettings.this.mListView.setAdapter(OPQuickLaunchListSettings.this.mAdapter);
            }
        }
    };
    private boolean mIsUiRestricted;
    private String mLastListSettings;
    private RecyclerView mListView;
    private Menu mMenu;
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;
    private boolean mRemoveMode;
    private boolean mShowingRemoveDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.oneplus_shortcuts_settings);
        }
        this.mAdapter = new OPAppDragAndDropAdapter(this.mContext, this.mAppList);
        if (this.mContext != null) {
            this.mPackageManager = this.mContext.getPackageManager();
            this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        }
        this.mLastListSettings = OPUtils.getAllQuickLaunchStrings(this.mContext);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onResume() {
        super.onResume();
        initData();
    }

    public void onPause() {
        super.onPause();
        if (!TextUtils.equals(this.mLastListSettings, OPUtils.getAllQuickLaunchStrings(this.mContext))) {
            OPUtils.sendAppTrackerForQuickLaunch();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstState) {
        View result = super.onCreateView(inflater, container, savedInstState);
        configureDragAndDrop(inflater.inflate(R.layout.op_drag_list, (ViewGroup) result));
        return result;
    }

    private void initData() {
        this.mAppList.clear();
        this.mAppList = OPUtils.parseAllQuickLaunchStrings(this.mContext);
        if (this.mAppList.size() > 0) {
            System.putInt(getContentResolver(), OPConstants.HAVE_EDIT_QUICK_LAUNCH_LIST, 1);
        }
        this.mAdapter.setAppList(this.mAppList);
        OPAppLinearLayoutManager llm = new OPAppLinearLayoutManager(this.mContext, this.mAdapter);
        llm.setAutoMeasureEnabled(true);
        this.mListView.setLayoutManager(llm);
        this.mListView.setHasFixedSize(true);
        this.mAdapter.setRecyclerView(this.mListView);
        this.mListView.setAdapter(this.mAdapter);
    }

    private void updateAddViewStatus() {
        if (this.mAppList == null || this.mAppList.size() < 6) {
            this.mAddView.setText(R.string.oneplus_shortcuts_add);
            this.mAddView.setEnabled(true);
            return;
        }
        this.mAddView.setText(R.string.oneplus_max_shortcuts_tips);
        this.mAddView.setEnabled(false);
    }

    private void configureDragAndDrop(View view) {
        this.mListView = (RecyclerView) view.findViewById(R.id.dragList);
        this.mAddView = (Button) view.findViewById(R.id.add_more);
        this.mAddView.setText(getActivity().getString(R.string.oneplus_shortcuts_add));
        this.mAddView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OPQuickLaunchListSettings.this.startActivityForResult(new Intent("com.oneplus.action.QUICKPAY_LAUNCH_CATEGORY_SETTINGS"), 0);
            }
        });
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        return false;
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public void onPreferenceViewClick(View view) {
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            this.mRemoveMode = savedInstanceState.getBoolean(CFGKEY_REMOVE_MODE, false);
            this.mShowingRemoveDialog = savedInstanceState.getBoolean(CFGKEY_REMOVE_DIALOG, false);
        }
        setRemoveMode(this.mRemoveMode);
        this.mAdapter.restoreState(savedInstanceState);
        boolean z = this.mShowingRemoveDialog;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CFGKEY_REMOVE_MODE, this.mRemoveMode);
        outState.putBoolean(CFGKEY_REMOVE_DIALOG, this.mShowingRemoveDialog);
        this.mAdapter.saveState(outState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 2) {
            if (this.mRemoveMode) {
                this.mRemoveMode = false;
                this.mAdapter.removeChecked();
                setRemoveMode(false);
            } else {
                setRemoveMode(true);
            }
            return true;
        } else if (itemId != 16908332 || !this.mRemoveMode) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            setRemoveMode(false);
            return true;
        }
    }

    private void setRemoveMode(boolean mRemoveMode) {
        this.mRemoveMode = mRemoveMode;
        this.mAdapter.setRemoveMode(mRemoveMode);
        this.mAddView.setVisibility(mRemoveMode ? 4 : 0);
        updateVisibilityOfRemoveMenu();
    }

    private void updateVisibilityOfRemoveMenu() {
        if (this.mMenu != null) {
            int i = 2;
            MenuItem menuItemRemove = this.mMenu.findItem(2);
            if (menuItemRemove != null) {
                if (!this.mRemoveMode) {
                    i = 0;
                }
                menuItemRemove.setShowAsAction(i);
            }
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
