package com.oneplus.settings.laboratory;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.settings.R;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OPLabFeatureActivity extends BaseActivity implements OnItemClickListener {
    public static final int DATA_LOAD_COMPLETED = 0;
    private static final String ONEPLUS_LAB_FEATURE_ICON_ID = "oneplus_lab_feature_icon_id";
    private static final String ONEPLUS_LAB_FEATURE_KEY = "oneplus_lab_feature_key";
    private static final String ONEPLUS_LAB_FEATURE_SUMMARY = "oneplus_lab_feature_Summary";
    private static final String ONEPLUS_LAB_FEATURE_TITLE = "oneplus_lab_feature_title";
    private static final String ONEPLUS_LAB_FEATURE_TOGGLE_COUNT = "oneplus_lab_feature_toggle_count";
    private static final String ONEPLUS_LAB_FEATURE_TOGGLE_NAMES = "oneplus_lab_feature_toggle_names";
    private static final String ONEPLUS_NFC_SECURITY_MODULE_KEY = "oneplus_nfc_security_module_key";
    private static final String PLUGIN_ACTION = "com.android.ONEPLUS_LAB_PLUGIN";
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && OPLabFeatureActivity.this.mPluginListAdapter != null) {
                List<OPLabPluginModel> pluginData = new ArrayList();
                pluginData.addAll(OPLabFeatureActivity.this.mPluginData);
                OPLabFeatureActivity.this.mPluginListAdapter.setData(pluginData);
            }
        }
    };
    private List<OPLabPluginModel> mPluginData = new ArrayList();
    private ImageView mPluginHeadImageView;
    private ListView mPluginList;
    private OPLabPluginListAdapter mPluginListAdapter;
    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_lab_feature_list_activity);
        this.mPluginHeadImageView = (ImageView) findViewById(R.id.op_lab_feature_plugin_head);
        this.mPluginList = (ListView) findViewById(R.id.op_lab_feature_plugin_list);
        if (OPUtils.isBlackModeOn(getContentResolver())) {
            this.mPluginHeadImageView.setImageResource(R.drawable.oneplus_lab_head_bg_dark);
        } else {
            this.mPluginHeadImageView.setImageResource(R.drawable.oneplus_lab_head_bg_light);
        }
        this.mPluginListAdapter = new OPLabPluginListAdapter(this, this.mPluginData);
        this.mPluginList.setAdapter(this.mPluginListAdapter);
        this.mPluginList.setOnItemClickListener(this);
        initData(this.mHandler);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void initData(final Handler handler) {
        this.mThreadPool.execute(new Runnable() {
            public void run() {
                OPLabFeatureActivity.this.fetchLockedAppListByPackageInfo();
                handler.sendEmptyMessage(0);
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        gotoDetailPage(this.mPluginListAdapter.getItem(position));
    }

    private void gotoDetailPage(OPLabPluginModel labPluginModel) {
        Intent intent = new Intent("oneplus.intent.action.ONEPLUS_LAB_FEATURE_DETAILS");
        intent.putExtra(ONEPLUS_LAB_FEATURE_TOGGLE_COUNT, labPluginModel.getToggleCount());
        intent.putExtra(ONEPLUS_LAB_FEATURE_TOGGLE_NAMES, labPluginModel.getMultiToggleName());
        intent.putExtra(ONEPLUS_LAB_FEATURE_TITLE, labPluginModel.getFeatureTitle());
        intent.putExtra(ONEPLUS_LAB_FEATURE_SUMMARY, labPluginModel.getFeatureSummary());
        intent.putExtra(ONEPLUS_LAB_FEATURE_KEY, labPluginModel.getFeatureKey());
        intent.putExtra(ONEPLUS_LAB_FEATURE_ICON_ID, labPluginModel.geFeatureIconId());
        startActivity(intent);
    }

    public void fetchLockedAppListByPackageInfo() {
        Exception e;
        OPLabFeatureActivity oPLabFeatureActivity = this;
        try {
            long curtime = System.currentTimeMillis();
            List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(128);
            if (!packageInfos.isEmpty()) {
                Iterator it = packageInfos.iterator();
                while (it.hasNext()) {
                    PackageInfo packageInfo = (PackageInfo) it.next();
                    Bundle metaData = packageInfo.applicationInfo.metaData;
                    if (metaData != null && metaData.containsKey("oneplus_lab_feature")) {
                        Context context = oPLabFeatureActivity.createPackageContext(packageInfo.packageName, 0);
                        String oneplus_lab_feature = metaData.getString("oneplus_lab_feature");
                        String[] featureColume = oneplus_lab_feature.split(";");
                        int i = 0;
                        while (i < featureColume.length) {
                            List<PackageInfo> packageInfos2;
                            long curtime2;
                            Iterator it2;
                            Bundle metaData2;
                            String oneplus_lab_feature2;
                            String[] featureColume2;
                            int i2;
                            int i3;
                            int i4;
                            OPLabPluginModel pluginModel = new OPLabPluginModel();
                            String[] columeKey = featureColume[i].split(",");
                            int columrKeyLength = columeKey.length;
                            int featureToggleCount = 2;
                            int featureKeyId;
                            String featureKey;
                            if (columrKeyLength > 4) {
                                String featureTitle = columeKey[0];
                                packageInfos2 = packageInfos;
                                curtime2 = curtime;
                                packageInfos = context.getResources().getIdentifier(featureTitle, "string", packageInfo.packageName);
                                String featureSummary = columeKey[1];
                                it2 = it;
                                metaData2 = metaData;
                                int featureSummaryId = context.getResources().getIdentifier(featureSummary, "string", packageInfo.packageName);
                                String featureSummary2 = featureSummary;
                                String featureTitle2 = featureTitle;
                                curtime = context.getResources().getIdentifier(columeKey[2], "drawable", packageInfo.packageName);
                                oneplus_lab_feature2 = oneplus_lab_feature;
                                featureKeyId = context.getResources().getIdentifier(columeKey[3], "string", packageInfo.packageName);
                                featureKey = featureKeyId != 0 ? context.getResources().getString(featureKeyId) : columeKey[3];
                                if (TextUtils.isEmpty(featureKey)) {
                                    featureColume2 = featureColume;
                                    i2 = i;
                                    i3 = 0;
                                    i = i2 + 1;
                                    i4 = i3;
                                    packageInfos = packageInfos2;
                                    curtime = curtime2;
                                    it = it2;
                                    metaData = metaData2;
                                    oneplus_lab_feature = oneplus_lab_feature2;
                                    featureColume = featureColume2;
                                } else {
                                    int featureToggleCount2;
                                    featureToggleCount = Integer.parseInt(columeKey[4]);
                                    String[] featureToggleNameIds = (String[]) Arrays.copyOfRange(columeKey, 5, columrKeyLength);
                                    oneplus_lab_feature = new String[featureToggleNameIds.length];
                                    int j = 0;
                                    while (true) {
                                        int featureKeyId2 = featureKeyId;
                                        featureColume2 = featureColume;
                                        int j2 = j;
                                        if (j2 >= featureToggleNameIds.length) {
                                            break;
                                        }
                                        featureToggleCount2 = featureToggleCount;
                                        i2 = i;
                                        try {
                                            int featureToggleNameId = context.getResources().getIdentifier(featureToggleNameIds[j2], "string", packageInfo.packageName);
                                            oneplus_lab_feature[j2] = featureToggleNameId != 0 ? context.getResources().getString(featureToggleNameId) : featureToggleNameIds[j2];
                                            j = j2 + 1;
                                            featureKeyId = featureKeyId2;
                                            featureColume = featureColume2;
                                            featureToggleCount = featureToggleCount2;
                                            i = i2;
                                        } catch (Exception e2) {
                                            e = e2;
                                            Log.e("PluginDemo", "some unknown error happened.");
                                            e.printStackTrace();
                                        }
                                    }
                                    i2 = i;
                                    featureToggleCount2 = featureToggleCount;
                                    pluginModel.setFeatureIconId(curtime);
                                    pluginModel.setFeatureTitle(packageInfos != null ? context.getResources().getString(packageInfos) : featureTitle2);
                                    pluginModel.setFeatureSummary(featureSummaryId != 0 ? context.getResources().getString(featureSummaryId) : featureSummary2);
                                    pluginModel.setMultiToggleName(oneplus_lab_feature);
                                    pluginModel.setFeatureKey(featureKey);
                                    featureToggleCount = featureToggleCount2;
                                    i3 = 0;
                                    if (!OPUtils.isSurportSimNfc(context) || !ONEPLUS_NFC_SECURITY_MODULE_KEY.equals(featureKey)) {
                                        pluginModel.setToggleCount(featureToggleCount);
                                        oPLabFeatureActivity = this;
                                        oPLabFeatureActivity.mPluginData.add(pluginModel);
                                        i = i2 + 1;
                                        i4 = i3;
                                        packageInfos = packageInfos2;
                                        curtime = curtime2;
                                        it = it2;
                                        metaData = metaData2;
                                        oneplus_lab_feature = oneplus_lab_feature2;
                                        featureColume = featureColume2;
                                    }
                                }
                            } else {
                                String featureTitle3;
                                packageInfos2 = packageInfos;
                                curtime2 = curtime;
                                it2 = it;
                                metaData2 = metaData;
                                oneplus_lab_feature2 = oneplus_lab_feature;
                                featureColume2 = featureColume;
                                i2 = i;
                                if (columrKeyLength > 1) {
                                    i3 = 0;
                                    featureTitle3 = columeKey[0];
                                } else {
                                    i3 = 0;
                                    featureTitle3 = "";
                                }
                                int featureTitleId = context.getResources().getIdentifier(featureTitle3, "string", packageInfo.packageName);
                                String featureSummary3 = columrKeyLength > 2 ? columeKey[1] : "";
                                featureKeyId = context.getResources().getIdentifier(featureSummary3, "string", packageInfo.packageName);
                                int featureIcon = context.getResources().getIdentifier(columeKey[2], "drawable", packageInfo.packageName);
                                i4 = context.getResources().getIdentifier(columeKey[3], "string", packageInfo.packageName);
                                oneplus_lab_feature = i4 != 0 ? context.getResources().getString(i4) : columeKey[3];
                                if (!TextUtils.isEmpty(oneplus_lab_feature)) {
                                    pluginModel.setFeatureIconId(featureIcon);
                                    pluginModel.setFeatureTitle(featureTitleId != 0 ? context.getResources().getString(featureTitleId) : featureTitle3);
                                    pluginModel.setFeatureSummary(featureKeyId != 0 ? context.getResources().getString(featureKeyId) : featureSummary3);
                                    pluginModel.setFeatureKey(oneplus_lab_feature);
                                    featureKey = oneplus_lab_feature;
                                    if (!OPUtils.isSurportSimNfc(context)) {
                                    }
                                    pluginModel.setToggleCount(featureToggleCount);
                                    oPLabFeatureActivity = this;
                                    oPLabFeatureActivity.mPluginData.add(pluginModel);
                                    i = i2 + 1;
                                    i4 = i3;
                                    packageInfos = packageInfos2;
                                    curtime = curtime2;
                                    it = it2;
                                    metaData = metaData2;
                                    oneplus_lab_feature = oneplus_lab_feature2;
                                    featureColume = featureColume2;
                                }
                            }
                            oPLabFeatureActivity = this;
                            i = i2 + 1;
                            i4 = i3;
                            packageInfos = packageInfos2;
                            curtime = curtime2;
                            it = it2;
                            metaData = metaData2;
                            oneplus_lab_feature = oneplus_lab_feature2;
                            featureColume = featureColume2;
                        }
                    }
                    packageInfos = packageInfos;
                    curtime = curtime;
                    it = it;
                }
            }
        } catch (Exception e3) {
            e = e3;
        }
    }

    public void fetchLockedAppListByActivityInfo() {
        try {
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(new Intent(PLUGIN_ACTION), 128);
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String oneplus_lab_package_name = reInfo.activityInfo.metaData.getString("oneplus_lab_package_name");
                    String oneplus_lab_feature_title = reInfo.activityInfo.metaData.getString(ONEPLUS_LAB_FEATURE_TITLE);
                    String oneplus_lab_feature_summary = reInfo.activityInfo.metaData.getString("oneplus_lab_feature_summary");
                    String oneplus_lab_feature_toggle_key = reInfo.activityInfo.metaData.getString("oneplus_lab_feature_toggle_key");
                    int oneplus_lab_feature_iconid = reInfo.activityInfo.metaData.getInt(ONEPLUS_LAB_FEATURE_ICON_ID);
                    int uid = reInfo.activityInfo.applicationInfo.uid;
                    OPLabPluginModel pluginModel = new OPLabPluginModel();
                    pluginModel.setPackageName(oneplus_lab_package_name);
                    pluginModel.setFeatureTitle(oneplus_lab_feature_title);
                    pluginModel.setFeatureSummary(oneplus_lab_feature_summary);
                    pluginModel.setFeatureKey(oneplus_lab_feature_toggle_key);
                    pluginModel.setFeatureIconId(oneplus_lab_feature_iconid);
                    this.mPluginData.add(pluginModel);
                }
            }
        } catch (Exception e) {
            Log.e("PluginDemo", "some unknown error happened.");
            e.printStackTrace();
        }
    }
}
