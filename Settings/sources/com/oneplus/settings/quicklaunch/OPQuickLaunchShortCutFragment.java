package com.oneplus.settings.quicklaunch;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPQuickLaunchShortCutFragment extends Fragment implements OnItemClickListener {
    public static final String[] sPayWaysKey = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_key);
    public static final int[] sPayWaysValue = SettingsBaseApplication.mApplication.getResources().getIntArray(R.array.oneplus_quickpay_ways_value);
    private List<OPAppModel> mAppList = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private List<OPAppModel> mDefaultpayAppList = new ArrayList();
    private View mEmptyView;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPQuickLaunchShortCutFragment.this.mOPShortcutListAdapter != null && OPQuickLaunchShortCutFragment.this.mOPApplicationLoader != null) {
                OPQuickLaunchShortCutFragment.this.mAppList.clear();
                OPQuickLaunchShortCutFragment.this.mAppList.addAll(OPQuickLaunchShortCutFragment.this.mDefaultpayAppList);
                OPQuickLaunchShortCutFragment.this.mAppList.addAll(OPQuickLaunchShortCutFragment.this.mOPApplicationLoader.getAppListByType(msg.what));
                OPQuickLaunchShortCutFragment.this.mOPShortcutListAdapter.setData(OPQuickLaunchShortCutFragment.this.mAppList);
                OPQuickLaunchShortCutFragment.this.mOPShortcutListAdapter.setAppType(5);
                if (OPQuickLaunchShortCutFragment.this.mAppList.isEmpty()) {
                    OPQuickLaunchShortCutFragment.this.mListView.setVisibility(0);
                    OPQuickLaunchShortCutFragment.this.mListView.setEmptyView(OPQuickLaunchShortCutFragment.this.mEmptyView);
                }
            }
        }
    };
    private ListView mListView;
    private View mLoadingContainer;
    private OPApplicationLoader mOPApplicationLoader;
    private OPShortcutListAdapter mOPShortcutListAdapter;
    private PackageManager mPackageManager;
    private String[] mPayWaysName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_quickpay_ways_name);
    private Map<Integer, OPAppModel> mSelectedApp = new HashMap();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (this.mContext != null) {
            this.mPackageManager = this.mContext.getPackageManager();
            this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
            this.mOPApplicationLoader = new OPApplicationLoader(this.mContext, this.mAppOpsManager, this.mPackageManager);
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.op_app_list_activity, null);
        initViews(parent);
        createDefaultAppList();
        return parent;
    }

    private void initViews(View view) {
        this.mListView = (ListView) view.findViewById(R.id.op_app_list);
        this.mOPShortcutListAdapter = new OPShortcutListAdapter(this.mContext, this.mAppList);
        this.mListView.setAdapter(this.mOPShortcutListAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mLoadingContainer = view.findViewById(R.id.loading_container);
        this.mEmptyView = view.findViewById(R.id.op_empty_list_tips_view);
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        this.mOPApplicationLoader.setNeedLoadWorkProfileApps(false);
        this.mOPApplicationLoader.initData(5, this.mHandler);
    }

    private List<OPAppModel> createDefaultAppList() {
        OPAppModel oPAppModel;
        OPAppModel oPAppModel2;
        this.mDefaultpayAppList.clear();
        if (OPUtils.isAppExist(this.mContext, OPConstants.PACKAGE_WECHAT)) {
            oPAppModel = new OPAppModel(OPConstants.PACKAGE_WECHAT, this.mPayWaysName[0], String.valueOf(0), 0, false);
            oPAppModel.setType(2);
            oPAppModel.setAppIcon(OPUtils.getQuickPayIconByType(this.mContext, 0));
            oPAppModel.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel));
            oPAppModel2 = new OPAppModel(OPConstants.PACKAGE_WECHAT, this.mPayWaysName[1], String.valueOf(1), 0, false);
            oPAppModel2.setType(2);
            oPAppModel2.setAppIcon(OPUtils.getQuickPayIconByType(this.mContext, 1));
            oPAppModel2.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel2));
            this.mDefaultpayAppList.add(oPAppModel);
            this.mDefaultpayAppList.add(oPAppModel2);
        }
        if (OPUtils.isAppExist(this.mContext, OPConstants.PACKAGE_ALIPAY)) {
            oPAppModel = new OPAppModel(OPConstants.PACKAGE_ALIPAY, this.mPayWaysName[2], String.valueOf(2), 0, false);
            oPAppModel.setType(2);
            oPAppModel.setAppIcon(OPUtils.getQuickPayIconByType(this.mContext, 2));
            oPAppModel.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel));
            oPAppModel2 = new OPAppModel(OPConstants.PACKAGE_ALIPAY, this.mPayWaysName[3], String.valueOf(3), 0, false);
            oPAppModel2.setType(2);
            oPAppModel2.setAppIcon(OPUtils.getQuickPayIconByType(this.mContext, 3));
            oPAppModel2.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel2));
            this.mDefaultpayAppList.add(oPAppModel);
            this.mDefaultpayAppList.add(oPAppModel2);
        }
        if (OPUtils.isAppExist(this.mContext, OPConstants.PACKAGE_PAYTM)) {
            oPAppModel = new OPAppModel(OPConstants.PACKAGE_PAYTM, this.mPayWaysName[4], String.valueOf(4), 0, false);
            oPAppModel.setType(2);
            oPAppModel.setAppIcon(OPUtils.getAppIcon(this.mContext, OPConstants.PACKAGE_PAYTM));
            oPAppModel.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel));
            this.mDefaultpayAppList.add(oPAppModel);
        }
        return this.mDefaultpayAppList;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean isSelected = this.mOPShortcutListAdapter.getSelected(position) ^ 1;
        if (!isSelected || OPUtils.getQuickLaunchShortcutsAccount(this.mContext) < 6) {
            this.mOPShortcutListAdapter.setSelected(position, isSelected);
            OPAppModel model = (OPAppModel) this.mListView.getItemAtPosition(position);
            StringBuilder quickLauncherhortcut = new StringBuilder(OPUtils.getAllQuickLaunchStrings(this.mContext));
            String quickShortcut = OPUtils.getQuickLaunchShortcutsString(model);
            if (OPUtils.isQuickPayModel(model)) {
                quickShortcut = OPUtils.getQuickPayAppString(model);
            }
            if (isSelected) {
                quickLauncherhortcut.append(quickShortcut);
            } else {
                int index = quickLauncherhortcut.indexOf(quickShortcut);
                quickLauncherhortcut.delete(index, quickShortcut.length() + index);
            }
            OPUtils.saveQuickLaunchStrings(this.mContext, quickLauncherhortcut.toString());
            return;
        }
        Toast.makeText(this.mContext, this.mContext.getString(R.string.oneplus_max_shortcuts_tips), 0).show();
    }
}
