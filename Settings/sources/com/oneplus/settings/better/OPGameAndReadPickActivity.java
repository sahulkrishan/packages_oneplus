package com.oneplus.settings.better;

import android.app.ActionBar;
import android.app.AppOpsManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPGameAndReadPickActivity extends BaseActivity implements OnItemClickListener {
    private List<OPAppModel> mAppList = new ArrayList();
    private ListView mAppListView;
    private AppOpsManager mAppOpsManager;
    private int mAppType;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPGameAndReadPickActivity.this.mOPGameAndReadPickAdapter != null && OPGameAndReadPickActivity.this.mOPApplicationLoader != null) {
                OPGameAndReadPickActivity.this.mAppList.clear();
                OPGameAndReadPickActivity.this.mAppList.addAll(OPGameAndReadPickActivity.this.mOPApplicationLoader.getAppListByType(msg.what));
                OPGameAndReadPickActivity.this.mOPGameAndReadPickAdapter.setData(OPGameAndReadPickActivity.this.mAppList);
                OPGameAndReadPickActivity.this.mOPGameAndReadPickAdapter.setAppType(OPGameAndReadPickActivity.this.mAppType);
                View emptyView = OPGameAndReadPickActivity.this.findViewById(R.id.op_empty_list_tips_view);
                if (OPGameAndReadPickActivity.this.mAppList.isEmpty()) {
                    emptyView.setVisibility(0);
                    OPGameAndReadPickActivity.this.mAppListView.setEmptyView(emptyView);
                }
            }
        }
    };
    private View mLoadingContainer;
    private OPApplicationLoader mOPApplicationLoader;
    private OPGameAndReadPickAdapter mOPGameAndReadPickAdapter;
    private PackageManager mPackageManager;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_game_read_app_list_activity);
        this.mAppType = getIntent().getIntExtra(OPConstants.OP_LOAD_APP_TYEP, 0);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        if (this.mAppType == 68) {
            actionBar.setTitle(getString(R.string.oneplus_game_mode_app_list));
        } else if (this.mAppType == 67) {
            actionBar.setTitle(getString(R.string.oneplus_read_mode_app_list));
        } else if (this.mAppType == 63) {
            actionBar.setTitle(getString(R.string.oneplus_app_locker_add_apps));
        }
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this, this.mAppOpsManager, this.mPackageManager);
        this.mOPApplicationLoader.setAppType(this.mAppType);
        initView();
    }

    private void initView() {
        this.mAppListView = (ListView) findViewById(R.id.op_app_list);
        OPUtils.setListDivider(SettingsBaseApplication.mApplication, this.mAppListView, R.drawable.op_list_divider_margin_start_4, R.drawable.op_list_divider_margin_end_4, R.dimen.oneplus_contorl_divider_height_standard);
        this.mOPGameAndReadPickAdapter = new OPGameAndReadPickAdapter(this, this.mAppList);
        this.mAppListView.setAdapter(this.mOPGameAndReadPickAdapter);
        this.mAppListView.setOnItemClickListener(this);
        this.mLoadingContainer = findViewById(R.id.loading_container);
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        this.mOPApplicationLoader.loadSelectedGameOrReadAppMap(this.mAppType);
        this.mOPApplicationLoader.initData(2, this.mHandler);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        int i;
        OPAppModel model = (OPAppModel) this.mAppListView.getItemAtPosition(position);
        boolean isSelected = this.mOPGameAndReadPickAdapter.getSelected(position) ^ true;
        this.mOPGameAndReadPickAdapter.setSelected(position, isSelected);
        AppOpsManager appOpsManager = this.mAppOpsManager;
        int i2 = this.mAppType;
        int uid = model.getUid();
        String pkgName = model.getPkgName();
        if (isSelected) {
            i = 0;
        } else {
            i = 1;
        }
        appOpsManager.setMode(i2, uid, pkgName, i);
        StringBuilder gameModeAppList = new StringBuilder(OPUtils.getGameModeAppListString(this));
        String gameModePkg = OPUtils.getGameModeAppString(model);
        if (OPUtils.isInRemovedGameAppListString(this, model)) {
            model.setEditMode(true);
        }
        if (model.isEditMode()) {
            if (isSelected) {
                int index = gameModeAppList.indexOf(gameModePkg);
                if (index != -1) {
                    gameModeAppList.delete(index, gameModePkg.length() + index);
                }
            } else {
                gameModeAppList.append(gameModePkg);
            }
            OPUtils.saveGameModeRemovedAppLisStrings(this, gameModeAppList.toString());
        }
    }
}
