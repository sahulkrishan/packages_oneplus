package com.oneplus.settings.carcharger;

import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPCarChargerOpenApp extends BaseActivity implements OnItemClickListener {
    private int hasRecommendedCount;
    private List<OPAppModel> mCarChargerAppsList = new ArrayList();
    private OPCarChargerOpenAppAdapter mCarChargerOpenAppAdapter;
    private ListView mCarChargerOpenAppListView;
    private List<OPAppModel> mCarChargerRecommendedAppsList = new ArrayList();
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && OPCarChargerOpenApp.this.mCarChargerOpenAppAdapter != null && OPCarChargerOpenApp.this.mOPApplicationLoader != null) {
                OPCarChargerOpenApp.this.mCarChargerAppsList.clear();
                OPCarChargerOpenApp.this.mCarChargerAppsList.addAll(OPCarChargerOpenApp.this.mCarChargerRecommendedAppsList);
                OPCarChargerOpenApp.this.mCarChargerAppsList.addAll(OPCarChargerOpenApp.this.mOPApplicationLoader.getAllAppList());
                OPCarChargerOpenApp.this.mCarChargerOpenAppAdapter.setData(OPCarChargerOpenApp.this.mCarChargerAppsList);
                OPCarChargerOpenApp.this.mCarChargerOpenAppAdapter.setHasRecommendedCount(OPCarChargerOpenApp.this.hasRecommendedCount);
                OPCarChargerOpenApp.this.mCarChargerOpenAppListView.setSelection(OPCarChargerOpenApp.this.getSelectionPosition());
            }
        }
    };
    private View mLoadingContainer;
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;

    private int getSelectionPosition() {
        String selectPackageName = System.getString(getApplicationContext().getContentResolver(), "op_charger_mode_auto_open_app");
        int i = 0;
        while (i < this.mCarChargerAppsList.size()) {
            if (selectPackageName != null && selectPackageName.equals(((OPAppModel) this.mCarChargerAppsList.get(i)).getPkgName())) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_car_charger_open_app_list);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.oneplus_auto_open_specified_app);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        initView();
    }

    private void initView() {
        this.mCarChargerOpenAppListView = (ListView) findViewById(R.id.op_car_charger_open_app_list);
        this.mCarChargerOpenAppListView.setOnItemClickListener(this);
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this, this.mPackageManager);
        this.mOPApplicationLoader.setAppType(80);
        this.mLoadingContainer = findViewById(R.id.loading_container);
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        this.mOPApplicationLoader.setNeedLoadWorkProfileApps(false);
        createCarModeRecommendedAppsList();
        this.mCarChargerOpenAppAdapter = new OPCarChargerOpenAppAdapter(this, this.mPackageManager);
        this.mCarChargerOpenAppListView.setAdapter(this.mCarChargerOpenAppAdapter);
        initData();
    }

    private List<OPAppModel> createCarModeRecommendedAppsList() {
        this.mCarChargerRecommendedAppsList.clear();
        this.mCarChargerRecommendedAppsList.add(new OPAppModel("", getString(R.string.oneplus_auto_open_app_none), "", 0, false));
        String[] recommendedapps = getResources().getStringArray(R.array.op_car_mode_recommended_apps);
        for (int i = 0; i < recommendedapps.length; i++) {
            if (OPUtils.isAppExist(getApplicationContext(), recommendedapps[i])) {
                this.hasRecommendedCount++;
                OPAppModel oPAppModel = new OPAppModel(recommendedapps[i], OPUtils.getAppLabel(getApplicationContext(), recommendedapps[i]), "", 0, false);
                oPAppModel.setAppIcon(OPUtils.getAppIcon(getApplicationContext(), recommendedapps[i]));
                this.mCarChargerRecommendedAppsList.add(oPAppModel);
            }
        }
        return this.mCarChargerRecommendedAppsList;
    }

    private void initData() {
        this.mOPApplicationLoader.initData(0, this.mHandler);
    }

    private void refreshList() {
        this.mCarChargerOpenAppAdapter.setSelectedItem(System.getString(getApplicationContext().getContentResolver(), "op_care_charger_auto_open_app"));
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        refreshList();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OPAppModel model = (OPAppModel) this.mCarChargerOpenAppListView.getItemAtPosition(position);
        System.putString(getApplicationContext().getContentResolver(), "op_care_charger_auto_open_app", model.getPkgName());
        OPUtils.sendAppTracker("charge_app", model.getPkgName());
        refreshList();
    }
}
