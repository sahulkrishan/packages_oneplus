package com.oneplus.settings.multiapp;

import android.accounts.AccountManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.setupwizardlib.util.WizardManagerHelper;
import com.oneplus.lib.util.loading.DialogLoadingHelper;
import com.oneplus.lib.util.loading.LoadingHelper;
import com.oneplus.lib.util.loading.LoadingHelper.FinishShowCallback;
import com.oneplus.settings.BaseActivity;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.apploader.OPApplicationLoader;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.multiapp.OPDeleteNonRequiredAppsTask.Callback;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPMultiAppListSettings extends BaseActivity implements OnItemClickListener {
    private static final int DEFAULT_VALUE = 1;
    public static final int FLAG_FROM = 67108864;
    public static final int INSTALL_MULTI_APP = 88;
    public static final String PROFILE_NAME = "Multi-App";
    public static final String TAG = "OPMultiAppListSettings";
    private static final ComponentName TEST_COMPONENT_NAME = ComponentName.unflattenFromString("com.oneplus.settings.multiapp/com.oneplus.settings.multiapp.OPBasicDeviceAdminReceiver");
    private static final String TEST_PACKAGE_NAME = "com.android.settings";
    private AccountManager mAccountManager;
    private List<OPAppModel> mAppList = new ArrayList();
    private ListView mAppListView;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private AsyncTask<String, Void, Void> mCreateManagedProfileTask;
    private boolean mFirstLoad = true;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (OPMultiAppListSettings.this.mOPMultiAppAdapter != null && OPMultiAppListSettings.this.mOPApplicationLoader != null) {
                OPMultiAppListSettings.this.mAppList.clear();
                OPMultiAppListSettings.this.mAppList.addAll(OPMultiAppListSettings.this.mOPApplicationLoader.getAppListByType(msg.what));
                OPMultiAppListSettings.this.mOPMultiAppAdapter.setData(OPMultiAppListSettings.this.mAppList);
                View emptyView = OPMultiAppListSettings.this.findViewById(R.id.op_empty_list_tips_view);
                if (OPMultiAppListSettings.this.mAppList.isEmpty()) {
                    emptyView.setVisibility(0);
                    OPMultiAppListSettings.this.mAppListView.setEmptyView(emptyView);
                }
            }
        }
    };
    private HandlerThread mHandlerThread;
    private boolean mHasTargetUser = false;
    private int mInitPosition;
    private Handler mInstallMultiApphandler;
    private IPackageManager mIpm;
    private boolean mIsInCreating = false;
    private boolean mIsWarnDialogShowing = false;
    private View mLoadingContainer;
    private LoadingHelper mLoadingHelper;
    private TextView mLoadingMessageView;
    private UserInfo mManagedProfileOrUserInfo;
    private boolean mNeedReloadData = false;
    private OPApplicationLoader mOPApplicationLoader;
    private OPMultiAppAdapter mOPMultiAppAdapter;
    private final BroadcastReceiver mPackageBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (TextUtils.equals(action, "android.intent.action.PACKAGE_REMOVED") || TextUtils.equals(action, "android.intent.action.PACKAGE_ADDED")) {
                    String pkgName = intent.getData().getSchemeSpecificPart();
                    String str = OPMultiAppListSettings.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(pkgName);
                    stringBuilder.append("has changed");
                    Log.d(str, stringBuilder.toString());
                    OPMultiAppListSettings.this.mNeedReloadData = true;
                }
            }
        }
    };
    private PackageManager mPackageManager;
    private ProgressDialog mProgressDialog;
    private Handler mRefreshUIHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 88) {
                int position = msg.arg1;
                OPAppModel model = (OPAppModel) OPMultiAppListSettings.this.mAppListView.getItemAtPosition(position);
                OPMultiAppListSettings.this.mOPMultiAppAdapter.setSelected(position, OPMultiAppListSettings.this.mOPMultiAppAdapter.getSelected(position) ^ 1);
                OPMultiAppListSettings.this.mAppOpsManager.setMode(69, model.getUid(), model.getPkgName(), 0);
                Toast.makeText(OPMultiAppListSettings.this.mContext, OPMultiAppListSettings.this.getEnabledString(model), 0).show();
            }
        }
    };
    private UserManager mUserManager;
    private AlertDialog mWarnDialog;

    private class CreateManagedProfileTask extends AsyncTask<String, Void, Void> {
        private CreateManagedProfileTask() {
        }

        /* synthetic */ CreateManagedProfileTask(OPMultiAppListSettings x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(String... params) {
            StringBuilder stringBuilder;
            try {
                if (OPMultiAppListSettings.this.mUserManager.hasUserRestriction("no_add_user", UserHandle.OWNER)) {
                    OPMultiAppListSettings.this.mUserManager.setUserRestriction("no_add_user", false, UserHandle.OWNER);
                }
                OPMultiAppListSettings.this.mManagedProfileOrUserInfo = OPMultiAppListSettings.this.mUserManager.createProfileForUser(params[0], 67108960, Process.myUserHandle().getIdentifier());
                String str = OPMultiAppListSettings.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Oneplus ManagedProfileOrUserInfo:");
                stringBuilder.append(OPMultiAppListSettings.this.mManagedProfileOrUserInfo);
                Log.d(str, stringBuilder.toString());
                if (OPMultiAppListSettings.this.mManagedProfileOrUserInfo != null) {
                    new OPDeleteNonRequiredAppsTask(OPMultiAppListSettings.this.mContext, "com.android.settings", 1, true, OPMultiAppListSettings.this.mManagedProfileOrUserInfo.id, false, new Callback() {
                        public void onSuccess() {
                            try {
                                OPMultiAppListSettings.this.setUpUserOrProfile();
                                Secure.putIntForUser(OPMultiAppListSettings.this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 1, 999);
                                Secure.putIntForUser(OPMultiAppListSettings.this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, 999);
                                if (OPMultiAppListSettings.this.mLoadingHelper != null) {
                                    OPMultiAppListSettings.this.mLoadingHelper.finishShowProgress(new FinishShowCallback() {
                                        public void finish(boolean shown) {
                                            OPMultiAppListSettings.this.refreshList(OPMultiAppListSettings.this.mInitPosition);
                                            Toast.makeText(OPMultiAppListSettings.this.mContext, OPMultiAppListSettings.this.getEnabledString(OPMultiAppListSettings.this.getModelWithPosition(OPMultiAppListSettings.this.mInitPosition)), 0).show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(OPMultiAppListSettings.TAG, "Provisioning failed", e);
                            }
                            Secure.putIntForUser(OPMultiAppListSettings.this.mContext.getContentResolver(), WizardManagerHelper.SETTINGS_SECURE_USER_SETUP_COMPLETE, 1, OPMultiAppListSettings.this.mManagedProfileOrUserInfo.id);
                        }

                        public void onError() {
                            Log.e(OPMultiAppListSettings.TAG, "Delete non required apps task failed.", new Exception());
                            Log.e(OPMultiAppListSettings.TAG, "onCreate----createProfileForUser--onError");
                            OPMultiAppListSettings.this.initFailed();
                        }
                    }).run();
                    Log.e(OPMultiAppListSettings.TAG, "onCreate----doInBackground-finish");
                    OPMultiAppListSettings.this.mIsInCreating = false;
                } else {
                    OPMultiAppListSettings.this.initFailed();
                }
            } catch (Exception e) {
                String str2 = OPMultiAppListSettings.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Exception");
                stringBuilder.append(e);
                Log.e(str2, stringBuilder.toString());
            }
            return null;
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class PackageDeleteObserver extends Stub {
        private PackageDeleteObserver() {
        }

        /* synthetic */ PackageDeleteObserver(OPMultiAppListSettings x0, AnonymousClass1 x1) {
            this();
        }

        public void packageDeleted(String packageName, int returnCode) {
            String str = OPMultiAppListSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PackageDeleteObserver ,");
            stringBuilder.append(returnCode);
            Log.e(str, stringBuilder.toString());
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_app_list_activity);
        this.mContext = this;
        this.mIpm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        this.mAccountManager = (AccountManager) getSystemService("account");
        this.mUserManager = (UserManager) getSystemService("user");
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mPackageManager = getPackageManager();
        this.mOPApplicationLoader = new OPApplicationLoader(this, this.mAppOpsManager, this.mPackageManager);
        this.mHandlerThread = new HandlerThread("install-multiapp-handler-thread");
        this.mHandlerThread.start();
        this.mInstallMultiApphandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (OPMultiAppListSettings.this.mAppListView != null && OPMultiAppListSettings.this.mOPMultiAppAdapter != null && OPMultiAppListSettings.this.mAppOpsManager != null) {
                    ProgressDialog progressDialog = msg.obj;
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage(OPMultiAppListSettings.this.mContext.getString(R.string.oneplus_multi_app_init));
                    DialogLoadingHelper helper = new DialogLoadingHelper(progressDialog);
                    helper.beginShowProgress();
                    final int position = msg.arg1;
                    OPAppModel model = (OPAppModel) OPMultiAppListSettings.this.mAppListView.getItemAtPosition(position);
                    OPMultiAppListSettings.this.installMultiApp(model.getPkgName(), model);
                    helper.finishShowProgress(new FinishShowCallback() {
                        public void finish(boolean shown) {
                            Message remsg = new Message();
                            remsg.what = 88;
                            remsg.arg1 = position;
                            OPMultiAppListSettings.this.mRefreshUIHandler.sendMessage(remsg);
                            Secure.putIntForUser(OPMultiAppListSettings.this.mContext.getContentResolver(), "notification_badging", Secure.getInt(OPMultiAppListSettings.this.mContext.getContentResolver(), "notification_badging", 1), 999);
                        }
                    });
                }
            }
        };
        initView();
        this.mManagedProfileOrUserInfo = getCorpUserInfo(this.mContext);
        registerPackageReceiver();
    }

    public void onResume() {
        super.onResume();
        if (this.mNeedReloadData && !this.mFirstLoad) {
            startLoadData();
            this.mNeedReloadData = false;
        }
        this.mFirstLoad = false;
    }

    public void onPause() {
        super.onPause();
    }

    private void registerPackageReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        registerReceiver(this.mPackageBroadcastReceiver, packageFilter);
    }

    private void initView() {
        this.mProgressDialog = new ProgressDialog(this);
        this.mAppListView = (ListView) findViewById(R.id.op_app_list);
        OPUtils.setListDivider(SettingsBaseApplication.mApplication, this.mAppListView, R.drawable.op_list_divider_margin_start_4, R.drawable.op_list_divider_margin_end_4, R.dimen.oneplus_contorl_divider_height_standard);
        this.mOPMultiAppAdapter = new OPMultiAppAdapter(this, this.mAppList);
        this.mAppListView.setAdapter(this.mOPMultiAppAdapter);
        this.mAppListView.setOnItemClickListener(this);
        this.mLoadingContainer = findViewById(R.id.loading_container);
        this.mLoadingMessageView = (TextView) this.mLoadingContainer.findViewById(R.id.loading_message);
        this.mLoadingHelper = new LoadingHelper() {
            /* Access modifiers changed, original: protected */
            public Object showProgree() {
                if (OPMultiAppListSettings.this.isFinishing() || OPMultiAppListSettings.this.isDestroyed()) {
                    return OPMultiAppListSettings.this.mProgressDialog;
                }
                if (OPMultiAppListSettings.this.mProgressDialog != null && OPMultiAppListSettings.this.mProgressDialog.isShowing()) {
                    OPMultiAppListSettings.this.mProgressDialog.dismiss();
                }
                OPMultiAppListSettings.this.mProgressDialog.show();
                OPMultiAppListSettings.this.mProgressDialog.setCancelable(false);
                OPMultiAppListSettings.this.mProgressDialog.setCanceledOnTouchOutside(false);
                OPMultiAppListSettings.this.mProgressDialog.setMessage(OPMultiAppListSettings.this.getString(R.string.oneplus_multi_app_init));
                return OPMultiAppListSettings.this.mProgressDialog;
            }

            /* Access modifiers changed, original: protected */
            public void hideProgree(Object progreeView) {
                try {
                    if (!OPMultiAppListSettings.this.isFinishing()) {
                        if (!OPMultiAppListSettings.this.isDestroyed()) {
                            if (OPMultiAppListSettings.this.mProgressDialog != null && OPMultiAppListSettings.this.mProgressDialog.isShowing()) {
                                OPMultiAppListSettings.this.mProgressDialog.dismiss();
                            }
                        }
                    }
                } catch (Throwable th) {
                }
            }
        };
        this.mOPApplicationLoader.setmLoadingContainer(this.mLoadingContainer);
        startLoadData();
    }

    private void startLoadData() {
        this.mOPApplicationLoader.loadSelectedGameOrReadAppMap(69);
        this.mOPApplicationLoader.initData(3, this.mHandler);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Oneplus multi app list item click:");
        stringBuilder.append(this.mIsInCreating);
        Log.d(str, stringBuilder.toString());
        if (!this.mIsInCreating) {
            if (this.mManagedProfileOrUserInfo == null) {
                this.mIsInCreating = true;
                this.mInitPosition = position;
                this.mCreateManagedProfileTask = new CreateManagedProfileTask(this, null);
                this.mLoadingHelper.beginShowProgress();
                this.mCreateManagedProfileTask.execute(new String[]{getString(R.string.oneplus_multi_app)});
            } else {
                refreshList(position, getModelWithPosition(position));
            }
        }
    }

    private OPAppModel getModelWithPosition(int position) {
        return (OPAppModel) this.mAppListView.getItemAtPosition(position);
    }

    private void refreshList(int position) {
        refreshList(position, getModelWithPosition(position));
    }

    private void refreshList(int position, OPAppModel model) {
        if (this.mOPMultiAppAdapter.getSelected(position) ^ 1) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            Message msg = new Message();
            msg.what = 88 + position;
            msg.arg1 = position;
            msg.obj = progressDialog;
            this.mInstallMultiApphandler.sendMessage(msg);
            return;
        }
        showWarnigDialog(position);
    }

    public String getEnabledString(OPAppModel model) {
        return String.format(getString(R.string.oneplus_multi_app_init_succeeded), new Object[]{model.getLabel()});
    }

    private void removeMultiAppByPosition(int position) {
        OPAppModel model = getModelWithPosition(position);
        this.mInstallMultiApphandler.removeMessages(88 + position);
        removeMultiApp(model.getPkgName());
        this.mOPMultiAppAdapter.setSelected(position, false);
        this.mAppOpsManager.setMode(69, model.getUid(), model.getPkgName(), 1);
    }

    private void showWarnigDialog(final int position) {
        if (!isFinishing() && !isDestroyed()) {
            this.mWarnDialog = new Builder(this).setMessage(R.string.oneplus_multi_app_disable_tips).setPositiveButton(R.string.okay, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    OPMultiAppListSettings.this.mIsWarnDialogShowing = false;
                    OPMultiAppListSettings.this.removeMultiAppByPosition(position);
                }
            }).setNegativeButton(R.string.cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    OPMultiAppListSettings.this.mIsWarnDialogShowing = false;
                }
            }).create();
            this.mWarnDialog.setCanceledOnTouchOutside(false);
            this.mWarnDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    OPMultiAppListSettings.this.mIsWarnDialogShowing = false;
                }
            });
            if (!this.mIsWarnDialogShowing) {
                this.mWarnDialog.show();
                this.mIsWarnDialogShowing = true;
            }
        }
    }

    private void installMultiApp(String packageName, OPAppModel model) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("installMultiApp");
        stringBuilder.append(packageName);
        Log.e(str, stringBuilder.toString());
        if (this.mManagedProfileOrUserInfo != null) {
            try {
                String str2;
                StringBuilder stringBuilder2;
                int status = this.mPackageManager.installExistingPackageAsUser(packageName, this.mManagedProfileOrUserInfo.id);
                if (status == -111) {
                    Log.e(TAG, "Could not install mobile device management app on managed profile because the user is restricted");
                } else if (status != -3) {
                    if (status != 1) {
                        str2 = TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Could not install mobile device management app on managed profile. Unknown status: ");
                        stringBuilder2.append(status);
                        Log.e(str2, stringBuilder2.toString());
                    }
                    str2 = TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("installMultiApp");
                    stringBuilder2.append(packageName);
                    stringBuilder2.append("success");
                    Log.e(str2, stringBuilder2.toString());
                    this.mAppOpsManager.setMode(69, model.getUid(), model.getPkgName(), 0);
                    return;
                }
                Log.e(TAG, "Could not install mobile device management app on managed profile because the package could not be found");
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Could not install mobile device management app on managed profile. Unknown status: ");
                stringBuilder2.append(status);
                Log.e(str2, stringBuilder2.toString());
            } catch (NameNotFoundException neverThrown) {
                Log.e(TAG, "This should not happen.", neverThrown);
            }
        }
    }

    private void removeMultiApp(String pkgName) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("removeMultiApp ,");
        stringBuilder.append(pkgName);
        Log.e(str, stringBuilder.toString());
        if (this.mManagedProfileOrUserInfo != null) {
            try {
                IPackageManager.Stub.asInterface(ServiceManager.getService("package")).deletePackageAsUser(pkgName, -1, new PackageDeleteObserver(this, null), this.mManagedProfileOrUserInfo.id, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeProfile() {
        ((DevicePolicyManager) getSystemService("device_policy")).wipeData(0);
    }

    private void initFailed() {
        if (this.mLoadingHelper != null) {
            this.mLoadingHelper.finishShowProgress(new FinishShowCallback() {
                public void finish(boolean shown) {
                    List<UserInfo> users = OPMultiAppListSettings.this.mUserManager.getUsers();
                    if (users == null || users.size() < 4) {
                        Toast.makeText(OPMultiAppListSettings.this.mContext, R.string.oneplus_multi_app_init_failed, 0).show();
                    } else {
                        Toast.makeText(OPMultiAppListSettings.this.mContext, R.string.oneplus_multi_app_init_failed_for_more_users, 0).show();
                    }
                }
            });
        }
    }

    private UserInfo getCorpUserInfo(Context context) {
        int myUser = this.mUserManager.getUserHandle();
        for (UserInfo ui : this.mUserManager.getUsers()) {
            if (ui.id == 999) {
                UserInfo parent = this.mUserManager.getProfileParent(ui.id);
                if (parent != null) {
                    if (parent.id == myUser) {
                        return ui;
                    }
                }
            }
        }
        return null;
    }

    private void setMdmAsManagedProfileOwner() {
        if (!((DevicePolicyManager) getSystemService("device_policy")).setProfileOwner(TEST_COMPONENT_NAME, "com.android.settings", this.mManagedProfileOrUserInfo.id)) {
            Log.e(TAG, "Could not set profile owner.");
        }
    }

    private void setMdmAsActiveAdmin() {
        ((DevicePolicyManager) getSystemService("device_policy")).setActiveAdmin(TEST_COMPONENT_NAME, true, this.mManagedProfileOrUserInfo.id);
    }

    private void enableProfile() {
        int userId = this.mManagedProfileOrUserInfo.id;
        this.mUserManager.setUserName(this.mManagedProfileOrUserInfo.id, getString(R.string.oneplus_multi_app));
        this.mUserManager.setUserEnabled(userId);
        UserInfo parent = this.mUserManager.getProfileParent(userId);
        Intent intent = new Intent("android.intent.action.MANAGED_PROFILE_ADDED");
        intent.putExtra("android.intent.extra.USER", new UserHandle(userId));
        intent.addFlags(1342177280);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(parent.id));
    }

    private void installMdmOnManagedProfile() {
        Log.e(TAG, "Installing mobile device management app on managed profile");
        try {
            String str;
            StringBuilder stringBuilder;
            int status = this.mPackageManager.installExistingPackageAsUser("com.android.settings", this.mManagedProfileOrUserInfo.id);
            if (status == -111) {
                Log.e(TAG, "Could not install mobile device management app on managed profile because the user is restricted");
            } else if (status != -3) {
                if (status != 1) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Could not install mobile device management app on managed profile. Unknown status: ");
                    stringBuilder.append(status);
                    Log.e(str, stringBuilder.toString());
                }
                return;
            }
            Log.e(TAG, "Could not install mobile device management app on managed profile because the package could not be found");
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Could not install mobile device management app on managed profile. Unknown status: ");
            stringBuilder.append(status);
            Log.e(str, stringBuilder.toString());
        } catch (NameNotFoundException e) {
        }
    }

    private void setUserProvisioningState(int state, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService("device_policy");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Setting userProvisioningState for user ");
        stringBuilder.append(userId);
        stringBuilder.append(" to: ");
        stringBuilder.append(state);
        Log.e(str, stringBuilder.toString());
        dpm.setUserProvisioningState(state, userId);
    }

    private void setUpUserOrProfile() {
        enableProfile();
        this.mHasTargetUser = true;
        try {
            ActivityManagerNative.getDefault().startUserInBackground(this.mManagedProfileOrUserInfo.id);
        } catch (RemoteException e) {
        }
    }

    private void refreshListByMovePackage(String pkgName) {
        if (this.mOPMultiAppAdapter != null) {
            List<OPAppModel> appList = new ArrayList();
            appList.addAll(this.mOPApplicationLoader.getAppListByType(69));
            for (OPAppModel model : appList) {
                if (pkgName != null && pkgName.equals(model.getPkgName())) {
                    appList.remove(model);
                }
            }
            this.mOPMultiAppAdapter.setData(appList);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mPackageBroadcastReceiver);
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
        }
    }
}
