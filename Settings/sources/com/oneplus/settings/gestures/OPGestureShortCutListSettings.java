package com.oneplus.settings.gestures;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPGestureShortCutListSettings extends BaseActivity implements OnItemClickListener {
    public static final String TAG = "OPGestureShortCutListSettings";
    private OPGestureAppModel mAPPOPGestureAppModel;
    private Drawable mAppDrawable;
    private ApplicationInfo mApplicationInfo;
    private List<OPGestureAppModel> mGestureAppList = new ArrayList();
    private String mGestureKey;
    private String mGesturePackage;
    private ListView mGestureShortcutListView;
    private String mGestureSummary;
    private int mGestureUid;
    private OPGestureShortcutsAdapter mOPGestureShortcutsAdapter;
    private PackageManager mPackageManager;
    private List<ShortcutInfo> mShortcutInfo;
    private String mTitle;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_gesture_app_list_activity);
        Intent intent = getIntent();
        this.mGestureKey = intent.getStringExtra(OPConstants.OP_GESTURE_KEY);
        this.mGesturePackage = intent.getStringExtra(OPConstants.OP_GESTURE_PACKAGE);
        this.mGestureUid = intent.getIntExtra(OPConstants.OP_GESTURE_PACKAGE_UID, -1);
        this.mTitle = intent.getStringExtra(OPConstants.OP_GESTURE_PACKAGE_APP);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(this.mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.mPackageManager = getPackageManager();
        try {
            this.mApplicationInfo = this.mPackageManager.getApplicationInfo(this.mGesturePackage, 0);
            this.mAppDrawable = this.mApplicationInfo.loadIcon(this.mPackageManager);
        } catch (NameNotFoundException e) {
        }
        initView();
    }

    private void initView() {
        this.mGestureShortcutListView = (ListView) findViewById(R.id.op_gesture_app_list);
        OPUtils.setListDivider(SettingsBaseApplication.mApplication, this.mGestureShortcutListView, R.drawable.op_list_divider_margin_start_4, R.drawable.op_list_divider_margin_end_4, R.dimen.oneplus_contorl_divider_height_standard);
        this.mGestureShortcutListView.setOnItemClickListener(this);
    }

    private void initData() {
        getSystemService("launcherapps");
        this.mShortcutInfo = OPGestureUtils.loadShortCuts(this, this.mGesturePackage);
        if (this.mShortcutInfo != null) {
            this.mGestureAppList.clear();
            OPGestureAppModel appModel = new OPGestureAppModel(this.mGesturePackage, this.mTitle, "", 0);
            appModel.setAppIcon(this.mAppDrawable);
            this.mGestureAppList.add(appModel);
            int size = this.mShortcutInfo.size();
            for (int i = 0; i < size; i++) {
                ShortcutInfo s = (ShortcutInfo) this.mShortcutInfo.get(i);
                CharSequence label = s.getLongLabel();
                if (TextUtils.isEmpty(label)) {
                    label = s.getShortLabel();
                }
                if (TextUtils.isEmpty(label)) {
                    label = s.getId();
                }
                OPGestureAppModel model = new OPGestureAppModel(s.getPackage(), label.toString(), s.getId(), 0);
                try {
                    model.setAppIcon(createPackageContext(this.mGesturePackage, 0).getResources().getDrawable(s.getIconResourceId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mGestureAppList.add(model);
            }
        }
    }

    private void refreshList() {
        initData();
        if (!this.mGesturePackage.equals(OPGestureUtils.getGesturePackageName(this, this.mGestureKey))) {
            ContentResolver contentResolver = getContentResolver();
            String str = this.mGestureKey;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(OPConstants.OPEN_APP);
            stringBuilder.append(this.mGesturePackage);
            System.putString(contentResolver, str, stringBuilder.toString());
        }
        this.mGestureSummary = OPGestureUtils.getShortCutsNameByID(this, this.mGesturePackage, OPGestureUtils.getShortCutIdByGestureKey(this, this.mGestureKey));
        this.mOPGestureShortcutsAdapter = new OPGestureShortcutsAdapter(this, this.mGestureAppList, TextUtils.isEmpty(this.mGestureSummary) ? this.mTitle : this.mGestureSummary);
        this.mGestureShortcutListView.setAdapter(this.mOPGestureShortcutsAdapter);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        refreshList();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OPGestureAppModel model = (OPGestureAppModel) this.mGestureShortcutListView.getItemAtPosition(position);
        if (position != 0) {
            openShortCuts(model);
        } else {
            openApps(model);
        }
        setResult(-1);
        finish();
    }

    private void openApps(OPGestureAppModel model) {
        ContentResolver contentResolver = getContentResolver();
        String str = this.mGestureKey;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_APP);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(this.mGestureUid);
        System.putString(contentResolver, str, stringBuilder.toString());
    }

    private void openShortCuts(OPGestureAppModel model) {
        ContentResolver contentResolver = getContentResolver();
        String str = this.mGestureKey;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_SHORTCUT);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(model.getShortCutId());
        stringBuilder.append(";");
        stringBuilder.append(this.mGestureUid);
        System.putString(contentResolver, str, stringBuilder.toString());
    }
}
