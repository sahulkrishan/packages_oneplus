package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminInfo.PolicyInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources.NotFoundException;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.EventLog;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AppSecurityPermissions;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.users.UserDialogs;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.settings.utils.OPUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.xmlpull.v1.XmlPullParserException;

public class DeviceAdminAdd extends Activity {
    static final int DIALOG_WARNING = 1;
    public static final String EXTRA_CALLED_FROM_SUPPORT_DIALOG = "android.app.extra.CALLED_FROM_SUPPORT_DIALOG";
    public static final String EXTRA_DEVICE_ADMIN_PACKAGE_NAME = "android.app.extra.DEVICE_ADMIN_PACKAGE_NAME";
    private static final int MAX_ADD_MSG_LINES = 15;
    private static final int MAX_ADD_MSG_LINES_LANDSCAPE = 2;
    private static final int MAX_ADD_MSG_LINES_PORTRAIT = 5;
    static final String TAG = "DeviceAdminAdd";
    Button mActionButton;
    TextView mAddMsg;
    boolean mAddMsgEllipsized = true;
    ImageView mAddMsgExpander;
    CharSequence mAddMsgText;
    boolean mAdding;
    boolean mAddingProfileOwner;
    TextView mAdminDescription;
    ImageView mAdminIcon;
    TextView mAdminName;
    ViewGroup mAdminPolicies;
    boolean mAdminPoliciesInitialized;
    TextView mAdminWarning;
    AppOpsManager mAppOps;
    Button mCancelButton;
    DevicePolicyManager mDPM;
    DeviceAdminInfo mDeviceAdmin;
    Handler mHandler;
    boolean mIsCalledFromSupportDialog = false;
    String mProfileOwnerName;
    TextView mProfileOwnerWarning;
    boolean mRefreshing;
    TextView mSupportMessage;
    private final IBinder mToken = new Binder();
    Button mUninstallButton;
    boolean mUninstalling = false;
    boolean mWaitingForRemoveMsg;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle icicle) {
        String str;
        StringBuilder stringBuilder;
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null && VERSION.SDK_INT >= 11) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        super.onCreate(icicle);
        this.mHandler = new Handler(getMainLooper());
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mAppOps = (AppOpsManager) getSystemService("appops");
        PackageManager packageManager = getPackageManager();
        if ((getIntent().getFlags() & 268435456) != 0) {
            Log.w(TAG, "Cannot start ADD_DEVICE_ADMIN as a new task");
            finish();
            return;
        }
        String str2;
        StringBuilder stringBuilder2;
        int i = 0;
        this.mIsCalledFromSupportDialog = getIntent().getBooleanExtra(EXTRA_CALLED_FROM_SUPPORT_DIALOG, false);
        String action = getIntent().getAction();
        ComponentName who = (ComponentName) getIntent().getParcelableExtra("android.app.extra.DEVICE_ADMIN");
        if (who == null) {
            Optional<ComponentName> installedAdmin = findAdminWithPackageName(getIntent().getStringExtra(EXTRA_DEVICE_ADMIN_PACKAGE_NAME));
            if (installedAdmin.isPresent()) {
                who = (ComponentName) installedAdmin.get();
                this.mUninstalling = true;
            } else {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("No component specified in ");
                stringBuilder2.append(action);
                Log.w(str2, stringBuilder2.toString());
                finish();
                return;
            }
        }
        ComponentName who2 = who;
        if (action != null && action.equals("android.app.action.SET_PROFILE_OWNER")) {
            setResult(0);
            setFinishOnTouchOutside(true);
            this.mAddingProfileOwner = true;
            this.mProfileOwnerName = getIntent().getStringExtra("android.app.extra.PROFILE_OWNER_NAME");
            String callingPackage = getCallingPackage();
            if (callingPackage == null || !callingPackage.equals(who2.getPackageName())) {
                Log.e(TAG, "Unknown or incorrect caller");
                finish();
                return;
            }
            try {
                if ((packageManager.getPackageInfo(callingPackage, 0).applicationInfo.flags & 1) == 0) {
                    Log.e(TAG, "Cannot set a non-system app as a profile owner");
                    finish();
                    return;
                }
            } catch (NameNotFoundException e) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Cannot find the package ");
                stringBuilder2.append(callingPackage);
                Log.e(str2, stringBuilder2.toString());
                finish();
                return;
            }
        }
        try {
            String str3;
            ActivityInfo ai = packageManager.getReceiverInfo(who2, 128);
            if (!this.mDPM.isAdminActive(who2)) {
                List<ResolveInfo> avail = packageManager.queryBroadcastReceivers(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 32768);
                int count = avail == null ? 0 : avail.size();
                boolean found = false;
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= count) {
                        break;
                    }
                    ResolveInfo ri = (ResolveInfo) avail.get(i3);
                    if (ai.packageName.equals(ri.activityInfo.packageName) && ai.name.equals(ri.activityInfo.name)) {
                        try {
                            ri.activityInfo = ai;
                            DeviceAdminInfo dpi = new DeviceAdminInfo(this, ri);
                            found = true;
                            break;
                        } catch (XmlPullParserException e2) {
                            str2 = TAG;
                            StringBuilder stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("Bad ");
                            stringBuilder3.append(ri.activityInfo);
                            Log.w(str2, stringBuilder3.toString(), e2);
                        } catch (IOException e3) {
                            String str4 = TAG;
                            StringBuilder stringBuilder4 = new StringBuilder();
                            stringBuilder4.append("Bad ");
                            stringBuilder4.append(ri.activityInfo);
                            Log.w(str4, stringBuilder4.toString(), e3);
                        }
                    } else {
                        i2 = i3 + 1;
                    }
                }
                if (!found) {
                    str3 = TAG;
                    StringBuilder stringBuilder5 = new StringBuilder();
                    stringBuilder5.append("Request to add invalid device admin: ");
                    stringBuilder5.append(who2);
                    Log.w(str3, stringBuilder5.toString());
                    finish();
                    return;
                }
            }
            ResolveInfo ri2 = new ResolveInfo();
            ri2.activityInfo = ai;
            try {
                this.mDeviceAdmin = new DeviceAdminInfo(this, ri2);
                if ("android.app.action.ADD_DEVICE_ADMIN".equals(getIntent().getAction())) {
                    this.mRefreshing = false;
                    if (this.mDPM.isAdminActive(who2)) {
                        if (this.mDPM.isRemovingAdmin(who2, Process.myUserHandle().getIdentifier())) {
                            str3 = TAG;
                            i = new StringBuilder();
                            i.append("Requested admin is already being removed: ");
                            i.append(who2);
                            Log.w(str3, i.toString());
                            finish();
                            return;
                        }
                        ArrayList<PolicyInfo> newPolicies = this.mDeviceAdmin.getUsedPolicies();
                        while (i < newPolicies.size()) {
                            if (!this.mDPM.hasGrantedPolicy(who2, ((PolicyInfo) newPolicies.get(i)).ident)) {
                                this.mRefreshing = true;
                                break;
                            }
                            i++;
                        }
                        if (!this.mRefreshing) {
                            setResult(-1);
                            finish();
                            return;
                        }
                    }
                }
                if (!this.mAddingProfileOwner || this.mDPM.hasUserSetupCompleted()) {
                    this.mAddMsgText = getIntent().getCharSequenceExtra("android.app.extra.ADD_EXPLANATION");
                    setContentView(R.layout.device_admin_add);
                    this.mAdminIcon = (ImageView) findViewById(R.id.admin_icon);
                    this.mAdminName = (TextView) findViewById(R.id.admin_name);
                    this.mAdminDescription = (TextView) findViewById(R.id.admin_description);
                    this.mProfileOwnerWarning = (TextView) findViewById(R.id.profile_owner_warning);
                    this.mAddMsg = (TextView) findViewById(R.id.add_msg);
                    this.mAddMsgExpander = (ImageView) findViewById(R.id.add_msg_expander);
                    OnClickListener onClickListener = new OnClickListener() {
                        public void onClick(View v) {
                            DeviceAdminAdd.this.toggleMessageEllipsis(DeviceAdminAdd.this.mAddMsg);
                        }
                    };
                    this.mAddMsgExpander.setOnClickListener(onClickListener);
                    this.mAddMsg.setOnClickListener(onClickListener);
                    this.mAddMsg.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            int i = 0;
                            boolean hideMsgExpander = DeviceAdminAdd.this.mAddMsg.getLineCount() <= DeviceAdminAdd.this.getEllipsizedLines();
                            ImageView imageView = DeviceAdminAdd.this.mAddMsgExpander;
                            if (hideMsgExpander) {
                                i = 8;
                            }
                            imageView.setVisibility(i);
                            if (hideMsgExpander) {
                                DeviceAdminAdd.this.mAddMsg.setOnClickListener(null);
                                ((View) DeviceAdminAdd.this.mAddMsgExpander.getParent()).invalidate();
                            }
                            DeviceAdminAdd.this.mAddMsg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                    toggleMessageEllipsis(this.mAddMsg);
                    this.mAdminWarning = (TextView) findViewById(R.id.admin_warning);
                    this.mAdminPolicies = (ViewGroup) findViewById(R.id.admin_policies);
                    this.mSupportMessage = (TextView) findViewById(R.id.admin_support_message);
                    this.mCancelButton = (Button) findViewById(R.id.cancel_button);
                    this.mCancelButton.setFilterTouchesWhenObscured(true);
                    this.mCancelButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            EventLog.writeEvent(EventLogTags.EXP_DET_DEVICE_ADMIN_DECLINED_BY_USER, DeviceAdminAdd.this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
                            DeviceAdminAdd.this.finish();
                        }
                    });
                    this.mUninstallButton = (Button) findViewById(R.id.uninstall_button);
                    this.mUninstallButton.setFilterTouchesWhenObscured(true);
                    this.mUninstallButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            EventLog.writeEvent(EventLogTags.EXP_DET_DEVICE_ADMIN_UNINSTALLED_BY_USER, DeviceAdminAdd.this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
                            DeviceAdminAdd.this.mDPM.uninstallPackageWithActiveAdmins(DeviceAdminAdd.this.mDeviceAdmin.getPackageName());
                            DeviceAdminAdd.this.finish();
                        }
                    });
                    this.mActionButton = (Button) findViewById(R.id.action_button);
                    View restrictedAction = findViewById(R.id.restricted_action);
                    restrictedAction.setFilterTouchesWhenObscured(true);
                    restrictedAction.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (DeviceAdminAdd.this.mActionButton.isEnabled()) {
                                if (DeviceAdminAdd.this.mAdding) {
                                    DeviceAdminAdd.this.addAndFinish();
                                } else if (DeviceAdminAdd.this.isManagedProfile(DeviceAdminAdd.this.mDeviceAdmin) && DeviceAdminAdd.this.mDeviceAdmin.getComponent().equals(DeviceAdminAdd.this.mDPM.getProfileOwner())) {
                                    final int userId = UserHandle.myUserId();
                                    UserDialogs.createRemoveDialog(DeviceAdminAdd.this, userId, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            UserManager.get(DeviceAdminAdd.this).removeUser(userId);
                                            DeviceAdminAdd.this.finish();
                                        }
                                    }).show();
                                } else if (DeviceAdminAdd.this.mUninstalling) {
                                    DeviceAdminAdd.this.mDPM.uninstallPackageWithActiveAdmins(DeviceAdminAdd.this.mDeviceAdmin.getPackageName());
                                    DeviceAdminAdd.this.finish();
                                } else if (!DeviceAdminAdd.this.mWaitingForRemoveMsg) {
                                    try {
                                        ActivityManager.getService().stopAppSwitches();
                                    } catch (RemoteException e) {
                                    }
                                    DeviceAdminAdd.this.mWaitingForRemoveMsg = true;
                                    DeviceAdminAdd.this.mDPM.getRemoveWarning(DeviceAdminAdd.this.mDeviceAdmin.getComponent(), new RemoteCallback(new OnResultListener() {
                                        public void onResult(Bundle result) {
                                            CharSequence msg;
                                            if (result != null) {
                                                msg = result.getCharSequence("android.app.extra.DISABLE_WARNING");
                                            } else {
                                                msg = null;
                                            }
                                            DeviceAdminAdd.this.continueRemoveAction(msg);
                                        }
                                    }, DeviceAdminAdd.this.mHandler));
                                    DeviceAdminAdd.this.getWindow().getDecorView().getHandler().postDelayed(new Runnable() {
                                        public void run() {
                                            DeviceAdminAdd.this.continueRemoveAction(null);
                                        }
                                    }, 2000);
                                }
                                return;
                            }
                            DeviceAdminAdd.this.showPolicyTransparencyDialogIfRequired();
                        }
                    });
                    return;
                }
                addAndFinish();
            } catch (XmlPullParserException e22) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to retrieve device policy ");
                stringBuilder.append(who2);
                Log.w(str, stringBuilder.toString(), e22);
                finish();
            } catch (IOException e32) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to retrieve device policy ");
                stringBuilder.append(who2);
                Log.w(str, stringBuilder.toString(), e32);
                finish();
            }
        } catch (NameNotFoundException e4) {
            NameNotFoundException nameNotFoundException = e4;
            str2 = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Unable to retrieve device policy ");
            stringBuilder2.append(who2);
            Log.w(str2, stringBuilder2.toString(), e4);
            finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void showPolicyTransparencyDialogIfRequired() {
        if (isManagedProfile(this.mDeviceAdmin) && this.mDeviceAdmin.getComponent().equals(this.mDPM.getProfileOwner()) && !hasBaseCantRemoveProfileRestriction()) {
            EnforcedAdmin enforcedAdmin = getAdminEnforcingCantRemoveProfile();
            if (enforcedAdmin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this, enforcedAdmin);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void addAndFinish() {
        try {
            logSpecialPermissionChange(true, this.mDeviceAdmin.getComponent().getPackageName());
            this.mDPM.setActiveAdmin(this.mDeviceAdmin.getComponent(), this.mRefreshing);
            EventLog.writeEvent(EventLogTags.EXP_DET_DEVICE_ADMIN_ACTIVATED_BY_USER, this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
            setResult(-1);
        } catch (RuntimeException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception trying to activate admin ");
            stringBuilder.append(this.mDeviceAdmin.getComponent());
            Log.w(str, stringBuilder.toString(), e);
            if (this.mDPM.isAdminActive(this.mDeviceAdmin.getComponent())) {
                setResult(-1);
            }
        }
        if (this.mAddingProfileOwner) {
            try {
                this.mDPM.setProfileOwner(this.mDeviceAdmin.getComponent(), this.mProfileOwnerName, UserHandle.myUserId());
            } catch (RuntimeException e2) {
                setResult(0);
            }
        }
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    public void continueRemoveAction(CharSequence msg) {
        if (this.mWaitingForRemoveMsg) {
            this.mWaitingForRemoveMsg = false;
            if (msg == null) {
                try {
                    ActivityManager.getService().resumeAppSwitches();
                } catch (RemoteException e) {
                }
                logSpecialPermissionChange(false, this.mDeviceAdmin.getComponent().getPackageName());
                this.mDPM.removeActiveAdmin(this.mDeviceAdmin.getComponent());
                finish();
            } else {
                try {
                    ActivityManager.getService().stopAppSwitches();
                } catch (RemoteException e2) {
                }
                Bundle args = new Bundle();
                args.putCharSequence("android.app.extra.DISABLE_WARNING", msg);
                showDialog(1, args);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void logSpecialPermissionChange(boolean allow, String packageName) {
        int logCategory;
        if (allow) {
            logCategory = 766;
        } else {
            logCategory = 767;
        }
        FeatureFactory.getFactory(this).getMetricsFeatureProvider().action(this, logCategory, packageName, new Pair[0]);
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        this.mActionButton.setEnabled(true);
        updateInterface();
        this.mAppOps.setUserRestriction(24, true, this.mToken);
        this.mAppOps.setUserRestriction(45, true, this.mToken);
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
        this.mActionButton.setEnabled(false);
        this.mAppOps.setUserRestriction(24, false, this.mToken);
        this.mAppOps.setUserRestriction(45, false, this.mToken);
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException e) {
        }
    }

    /* Access modifiers changed, original: protected */
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mIsCalledFromSupportDialog) {
            finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public Dialog onCreateDialog(int id, Bundle args) {
        if (id != 1) {
            return super.onCreateDialog(id, args);
        }
        CharSequence msg = args.getCharSequence("android.app.extra.DISABLE_WARNING");
        Builder builder = new Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    ActivityManager.getService().resumeAppSwitches();
                } catch (RemoteException e) {
                }
                DeviceAdminAdd.this.mDPM.removeActiveAdmin(DeviceAdminAdd.this.mDeviceAdmin.getComponent());
                DeviceAdminAdd.this.finish();
            }
        });
        builder.setNegativeButton(R.string.dlg_cancel, null);
        return builder.create();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateInterface() {
        findViewById(R.id.restricted_icon).setVisibility(8);
        this.mAdminIcon.setImageDrawable(this.mDeviceAdmin.loadIcon(getPackageManager()));
        this.mAdminName.setText(this.mDeviceAdmin.loadLabel(getPackageManager()));
        try {
            this.mAdminDescription.setText(this.mDeviceAdmin.loadDescription(getPackageManager()));
            this.mAdminDescription.setVisibility(0);
        } catch (NotFoundException e) {
            this.mAdminDescription.setVisibility(8);
        }
        if (this.mAddingProfileOwner) {
            this.mProfileOwnerWarning.setVisibility(0);
        }
        if (this.mAddMsgText != null) {
            this.mAddMsg.setText(this.mAddMsgText);
            this.mAddMsg.setVisibility(0);
        } else {
            this.mAddMsg.setVisibility(8);
            this.mAddMsgExpander.setVisibility(8);
        }
        boolean z = true;
        if (this.mRefreshing || this.mAddingProfileOwner || !this.mDPM.isAdminActive(this.mDeviceAdmin.getComponent())) {
            addDeviceAdminPolicies(true);
            this.mAdminWarning.setText(getString(R.string.device_admin_warning, new Object[]{this.mDeviceAdmin.getActivityInfo().applicationInfo.loadLabel(getPackageManager())}));
            if (this.mAddingProfileOwner) {
                setTitle(getText(R.string.profile_owner_add_title));
            } else {
                setTitle(getText(R.string.add_device_admin_msg));
            }
            this.mActionButton.setText(getText(R.string.add_device_admin));
            if (isAdminUninstallable()) {
                this.mUninstallButton.setVisibility(0);
            }
            this.mSupportMessage.setVisibility(8);
            this.mAdding = true;
            return;
        }
        this.mAdding = false;
        boolean isProfileOwner = this.mDeviceAdmin.getComponent().equals(this.mDPM.getProfileOwner());
        boolean isManagedProfile = isManagedProfile(this.mDeviceAdmin);
        if (isProfileOwner && isManagedProfile) {
            this.mAdminWarning.setText(R.string.admin_profile_owner_message);
            this.mActionButton.setText(R.string.remove_managed_profile_label);
            EnforcedAdmin admin = getAdminEnforcingCantRemoveProfile();
            boolean hasBaseRestriction = hasBaseCantRemoveProfileRestriction();
            if (!(admin == null || hasBaseRestriction)) {
                findViewById(R.id.restricted_icon).setVisibility(0);
            }
            Button button = this.mActionButton;
            if (admin != null || hasBaseRestriction) {
                z = false;
            }
            button.setEnabled(z);
        } else if (isProfileOwner || this.mDeviceAdmin.getComponent().equals(this.mDPM.getDeviceOwnerComponentOnCallingUser())) {
            if (isProfileOwner) {
                this.mAdminWarning.setText(R.string.admin_profile_owner_user_message);
            } else {
                this.mAdminWarning.setText(R.string.admin_device_owner_message);
            }
            this.mActionButton.setText(R.string.remove_device_admin);
            this.mActionButton.setEnabled(false);
        } else {
            addDeviceAdminPolicies(false);
            this.mAdminWarning.setText(getString(R.string.device_admin_status, new Object[]{this.mDeviceAdmin.getActivityInfo().applicationInfo.loadLabel(getPackageManager())}));
            setTitle(R.string.active_device_admin_msg);
            if (this.mUninstalling) {
                this.mActionButton.setText(R.string.remove_and_uninstall_device_admin);
            } else {
                this.mActionButton.setText(R.string.remove_device_admin);
            }
        }
        CharSequence supportMessage = this.mDPM.getLongSupportMessageForUser(this.mDeviceAdmin.getComponent(), UserHandle.myUserId());
        if (TextUtils.isEmpty(supportMessage)) {
            this.mSupportMessage.setVisibility(8);
            return;
        }
        this.mSupportMessage.setText(supportMessage);
        this.mSupportMessage.setVisibility(0);
    }

    private EnforcedAdmin getAdminEnforcingCantRemoveProfile() {
        return RestrictedLockUtils.checkIfRestrictionEnforced(this, "no_remove_managed_profile", getParentUserId());
    }

    private boolean hasBaseCantRemoveProfileRestriction() {
        return RestrictedLockUtils.hasBaseUserRestriction(this, "no_remove_managed_profile", getParentUserId());
    }

    private int getParentUserId() {
        return UserManager.get(this).getProfileParent(UserHandle.myUserId()).id;
    }

    private void addDeviceAdminPolicies(boolean showDescription) {
        if (!this.mAdminPoliciesInitialized) {
            boolean isAdminUser = UserManager.get(this).isAdminUser();
            Iterator it = this.mDeviceAdmin.getUsedPolicies().iterator();
            while (it.hasNext()) {
                PolicyInfo pi = (PolicyInfo) it.next();
                View view = AppSecurityPermissions.getPermissionItemView(this, getText(isAdminUser ? pi.label : pi.labelForSecondaryUsers), showDescription ? getText(isAdminUser ? pi.description : pi.descriptionForSecondaryUsers) : "", true);
                ((ImageView) view.findViewById(16909166)).getDrawable().setTint(getResources().getColor(R.color.oneplus_contorl_icon_color_active_default));
                ((TextView) view.findViewById(16909170)).setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_secondary));
                ((TextView) view.findViewById(16909172)).setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_secondary));
                this.mAdminPolicies.addView(view);
            }
            this.mAdminPoliciesInitialized = true;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void toggleMessageEllipsis(View v) {
        int i;
        TextView tv = (TextView) v;
        this.mAddMsgEllipsized ^= 1;
        tv.setEllipsize(this.mAddMsgEllipsized ? TruncateAt.END : null);
        tv.setMaxLines(this.mAddMsgEllipsized ? getEllipsizedLines() : 15);
        ImageView imageView = this.mAddMsgExpander;
        if (this.mAddMsgEllipsized) {
            i = 17302205;
        } else {
            i = 17302204;
        }
        imageView.setImageResource(i);
    }

    /* Access modifiers changed, original: 0000 */
    public int getEllipsizedLines() {
        Display d = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        return d.getHeight() > d.getWidth() ? 5 : 2;
    }

    private boolean isManagedProfile(DeviceAdminInfo adminInfo) {
        UserInfo info = UserManager.get(this).getUserInfo(UserHandle.getUserId(adminInfo.getActivityInfo().applicationInfo.uid));
        return info != null ? info.isManagedProfile() : false;
    }

    private Optional<ComponentName> findAdminWithPackageName(String packageName) {
        List<ComponentName> admins = this.mDPM.getActiveAdmins();
        if (admins == null) {
            return Optional.empty();
        }
        return admins.stream().filter(new -$$Lambda$DeviceAdminAdd$3kbf0VppdPbIFmWVVpDZ5dj27E4(packageName)).findAny();
    }

    private boolean isAdminUninstallable() {
        return this.mDeviceAdmin.getActivityInfo().applicationInfo.isSystemApp() ^ 1;
    }
}
