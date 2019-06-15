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
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OPQuickLaunchAppFragment extends Fragment implements OnItemClickListener {
    private List<OPAppModel> mAppList = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private View mEmptyView;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPQuickLaunchAppFragment.this.mOPApplicationListAdapter != null && OPQuickLaunchAppFragment.this.mOPApplicationLoader != null) {
                OPQuickLaunchAppFragment.this.mAppList.clear();
                OPQuickLaunchAppFragment.this.mAppList.addAll(OPQuickLaunchAppFragment.this.mOPApplicationLoader.getAppListByType(msg.what));
                OPQuickLaunchAppFragment.this.mOPApplicationListAdapter.setData(OPQuickLaunchAppFragment.this.mAppList);
                OPQuickLaunchAppFragment.this.mOPApplicationListAdapter.setAppType(4);
                if (OPQuickLaunchAppFragment.this.mAppList.isEmpty()) {
                    OPQuickLaunchAppFragment.this.mListView.setVisibility(0);
                    OPQuickLaunchAppFragment.this.mListView.setEmptyView(OPQuickLaunchAppFragment.this.mEmptyView);
                }
            }
        }
    };
    private ListView mListView;
    private View mLoadingContainer;
    private OPApplicationListAdapter mOPApplicationListAdapter;
    private OPApplicationLoader mOPApplicationLoader;
    private PackageManager mPackageManager;
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
        return parent;
    }

    private void initViews(View view) {
        this.mListView = (ListView) view.findViewById(R.id.op_app_list);
        this.mOPApplicationListAdapter = new OPApplicationListAdapter(this.mContext, this.mAppList);
        this.mListView.setAdapter(this.mOPApplicationListAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mLoadingContainer = view.findViewById(R.id.loading_container);
        this.mEmptyView = view.findViewById(R.id.op_empty_list_tips_view);
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        this.mOPApplicationLoader.setNeedLoadWorkProfileApps(false);
        this.mOPApplicationLoader.initData(4, this.mHandler);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean isSelected = this.mOPApplicationListAdapter.getSelected(position) ^ 1;
        if (!isSelected || OPUtils.getQuickLaunchShortcutsAccount(this.mContext) < 6) {
            this.mOPApplicationListAdapter.setSelected(position, isSelected);
            OPAppModel model = (OPAppModel) this.mListView.getItemAtPosition(position);
            StringBuilder quickLauncherApp = new StringBuilder(OPUtils.getAllQuickLaunchStrings(this.mContext));
            String quickApp = OPUtils.getQuickLaunchAppString(model);
            if (isSelected) {
                quickLauncherApp.append(quickApp);
            } else {
                int index = quickLauncherApp.indexOf(quickApp);
                quickLauncherApp.delete(index, quickApp.length() + index);
            }
            OPUtils.saveQuickLaunchStrings(this.mContext, quickLauncherApp.toString());
            return;
        }
        Toast.makeText(this.mContext, this.mContext.getString(R.string.oneplus_max_shortcuts_tips), 0).show();
    }
}
