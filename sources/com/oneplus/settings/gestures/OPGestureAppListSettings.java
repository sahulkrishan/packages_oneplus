package com.oneplus.settings.gestures;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPConstants;
import java.util.ArrayList;
import java.util.List;

public class OPGestureAppListSettings extends BaseActivity implements OnItemClickListener {
    public static final int DEFAULT_GESTURE_COUNT = 6;
    public static final int SHORTCUT_REQUESET_CODE = 1;
    private static final int TIME_DELAY = 100;
    private List<OPAppModel> mDefaultGestureAppList = new ArrayList();
    private List<OPAppModel> mGestureAppList = new ArrayList();
    private ListView mGestureAppListView;
    private String mGestureKey;
    private String mGesturePackageName;
    private String mGestureSummary;
    private String mGestureTitle;
    private int mGestureUid;
    private int mGestureValueIndex;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && OPGestureAppListSettings.this.mOPGestureAppAdapter != null && OPGestureAppListSettings.this.mOPApplicationLoader != null) {
                OPGestureAppListSettings.this.mGestureAppList.clear();
                OPGestureAppListSettings.this.mGestureAppList.addAll(OPGestureAppListSettings.this.mDefaultGestureAppList);
                OPGestureAppListSettings.this.mGestureAppList.addAll(OPGestureAppListSettings.this.mOPApplicationLoader.getAllAppList());
                OPGestureAppListSettings.this.mOPGestureAppAdapter.setData(OPGestureAppListSettings.this.mGestureAppList);
                OPGestureAppListSettings.this.mGestureAppListView.setSelection(OPGestureAppListSettings.this.getSelectionPosition());
            }
        }
    };
    private View mLoadingContainer;
    private OPApplicationLoader mOPApplicationLoader;
    private OPGestureAppAdapter mOPGestureAppAdapter;
    private PackageManager mPackageManager;

    private int getSelectionPosition() {
        this.mGestureSummary = OPGestureUtils.getGestureSummarybyGestureKey(this, this.mGestureKey);
        this.mGesturePackageName = OPGestureUtils.getGesturePackageName(this, this.mGestureKey);
        for (int i = 0; i < this.mGestureAppList.size(); i++) {
            if (i < 6) {
                if (this.mGestureSummary.equals(((OPAppModel) this.mGestureAppList.get(i)).getLabel())) {
                    return i;
                }
            } else if (this.mGesturePackageName.equals(((OPAppModel) this.mGestureAppList.get(i)).getPkgName())) {
                return i;
            }
        }
        return 0;
    }

    private List<OPAppModel> createDefaultAppList() {
        this.mDefaultGestureAppList.clear();
        OPAppModel oPAppModel = new OPAppModel("", getString(R.string.oneplus_draw_gesture_start_none), "", 0, false);
        oPAppModel = new OPAppModel("", getString(R.string.oneplus_gestures_open_camera), "", 0, false);
        OPAppModel openFrontCamera = new OPAppModel("", getString(R.string.oneplus_gestures_open_front_camera), "", 0, false);
        OPAppModel takeVideo = new OPAppModel("", getString(R.string.oneplus_gestures_take_video), "", 0, false);
        OPAppModel openFlashlight = new OPAppModel("", getString(R.string.oneplus_gestures_open_flashlight, new Object[]{Boolean.valueOf(false)}), "", 0, false);
        OPAppModel openShelf = new OPAppModel("", getString(R.string.hardware_keys_action_shelf), "", 0, false);
        this.mDefaultGestureAppList.add(oPAppModel);
        this.mDefaultGestureAppList.add(oPAppModel);
        this.mDefaultGestureAppList.add(openFrontCamera);
        this.mDefaultGestureAppList.add(takeVideo);
        this.mDefaultGestureAppList.add(openFlashlight);
        this.mDefaultGestureAppList.add(openShelf);
        return this.mDefaultGestureAppList;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_gesture_app_list_activity);
        Intent intent = getIntent();
        this.mGestureKey = intent.getStringExtra(OPConstants.OP_GESTURE_KEY);
        this.mGestureTitle = intent.getStringExtra(OPConstants.OP_GESTURE_TITLE);
        this.mGestureValueIndex = OPGestureUtils.getIndexByGestureValueKey(this.mGestureKey);
        this.mGestureSummary = OPGestureUtils.getGestureSummarybyGestureKey(this, this.mGestureKey);
        String uid = OPGestureUtils.getGesturePacakgeUid(this, this.mGestureKey);
        this.mGestureUid = TextUtils.isEmpty(uid) ? -1 : Integer.valueOf(uid).intValue();
        this.mGesturePackageName = OPGestureUtils.getGesturePackageName(this, this.mGestureKey);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(this.mGestureTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        initView();
    }

    private void initView() {
        this.mGestureAppListView = (ListView) findViewById(R.id.op_gesture_app_list);
        this.mGestureAppListView.setOnItemClickListener(this);
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this, this.mPackageManager);
        this.mLoadingContainer = findViewById(R.id.loading_container);
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        this.mOPApplicationLoader.setNeedLoadWorkProfileApps(false);
        createDefaultAppList();
        this.mOPGestureAppAdapter = new OPGestureAppAdapter(this, this.mPackageManager, this.mGestureSummary);
        this.mGestureAppListView.setAdapter(this.mOPGestureAppAdapter);
        initData();
    }

    private void initData() {
        this.mOPApplicationLoader.initData(0, this.mHandler);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OPGestureUtils.set1(this, this.mGestureValueIndex);
        boolean hasShortCut = false;
        switch (position) {
            case 0:
                doNothing();
                break;
            case 1:
                openBackCamera();
                break;
            case 2:
                openFrontCamera();
                break;
            case 3:
                openTakeVideo();
                break;
            case 4:
                openFlashLight();
                break;
            case 5:
                openShelf();
                break;
            default:
                OPAppModel model = (OPAppModel) this.mGestureAppListView.getItemAtPosition(position);
                if (!OPGestureUtils.hasShortCuts(this, model.getPkgName())) {
                    openApps(model);
                    break;
                }
                gotoShortCutsPickPage(model);
                hasShortCut = true;
                break;
        }
        refreshList();
        if (!hasShortCut) {
            finish();
        }
    }

    private void refreshList() {
        this.mGestureSummary = OPGestureUtils.getGestureSummarybyGestureKey(this, this.mGestureKey);
        this.mGesturePackageName = OPGestureUtils.getGesturePackageName(this, this.mGestureKey);
        String shortCutId = OPGestureUtils.getShortCutIdByGestureKey(this, this.mGestureKey);
        boolean z = OPGestureUtils.hasShortCutsGesture(this, this.mGestureKey) && OPGestureUtils.hasShortCutsId(this, this.mGesturePackageName, shortCutId);
        this.mOPGestureAppAdapter.setSelectedItem(this.mGestureSummary, this.mGesturePackageName, this.mGestureUid, z, OPGestureUtils.getShortCutsNameByID(this, this.mGesturePackageName, shortCutId));
    }

    private void doNothing() {
        OPGestureUtils.set0(this, this.mGestureValueIndex);
        System.putString(getContentResolver(), this.mGestureKey, "");
    }

    private void openBackCamera() {
        System.putString(getContentResolver(), this.mGestureKey, OPConstants.OPEN_BACK_CAMERA);
    }

    private void openFrontCamera() {
        System.putString(getContentResolver(), this.mGestureKey, OPConstants.OPEN_FRONT_CAMERA);
    }

    private void openTakeVideo() {
        System.putString(getContentResolver(), this.mGestureKey, OPConstants.OPEN_TAKE_VIDEO);
    }

    private void openFlashLight() {
        System.putString(getContentResolver(), this.mGestureKey, OPConstants.OPEN_FLASH_LIGHT);
    }

    private void openShelf() {
        System.putString(getContentResolver(), this.mGestureKey, OPConstants.OPEN_SHELF);
    }

    private void openApps(OPAppModel model) {
        ContentResolver contentResolver = getContentResolver();
        String str = this.mGestureKey;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_APP);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(model.getUid());
        System.putString(contentResolver, str, stringBuilder.toString());
    }

    private void gotoShortCutsPickPage(OPAppModel model) {
        Intent intent = new Intent(OPConstants.ONEPLUS_GESTURE_SHORTCUT_LIST_ACTION);
        intent.putExtra(OPConstants.OP_GESTURE_KEY, this.mGestureKey);
        intent.putExtra(OPConstants.OP_GESTURE_PACKAGE, model.getPkgName());
        intent.putExtra(OPConstants.OP_GESTURE_PACKAGE_UID, model.getUid());
        intent.putExtra(OPConstants.OP_GESTURE_PACKAGE_APP, model.getLabel());
        startActivityForResult(intent, 1);
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

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1) {
            finish();
        }
    }
}
