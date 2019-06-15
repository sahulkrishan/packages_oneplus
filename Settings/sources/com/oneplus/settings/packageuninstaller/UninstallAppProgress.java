package com.oneplus.settings.packageuninstaller;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import com.android.settings.R;
import com.oneplus.settings.utils.OPConstants;
import java.lang.ref.WeakReference;

public class UninstallAppProgress extends Activity {
    private static final String FRAGMENT_TAG = "progress_fragment";
    private static final int QUICK_INSTALL_DELAY_MILLIS = 500;
    private static final String TAG = "UninstallAppProgress";
    private static final int UNINSTALL_COMPLETE = 1;
    private static final int UNINSTALL_IS_SLOW = 2;
    private boolean mAllUsers;
    private ApplicationInfo mAppInfo;
    private IBinder mCallback;
    private Handler mHandler = new MessageHandler(this);
    private boolean mIsViewInitialized;
    private volatile int mResultCode = -1;

    private static class MessageHandler extends Handler {
        private final WeakReference<UninstallAppProgress> mActivity;

        public MessageHandler(UninstallAppProgress activity) {
            this.mActivity = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            UninstallAppProgress activity = (UninstallAppProgress) this.mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private class PackageDeleteObserver extends Stub {
        private PackageDeleteObserver() {
        }

        public void packageDeleted(String packageName, int returnCode) {
            Message msg = UninstallAppProgress.this.mHandler.obtainMessage(1);
            msg.arg1 = returnCode;
            msg.obj = packageName;
            UninstallAppProgress.this.mHandler.sendMessage(msg);
        }
    }

    public interface ProgressFragment {
        void setDeviceManagerButtonVisible(boolean z);

        void setUsersButtonVisible(boolean z);

        void showCompletion(CharSequence charSequence);
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x0130  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0105  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x01b7  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01af  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01d0  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01c7  */
    private void handleMessage(android.os.Message r14) {
        /*
        r13 = this;
        r0 = r13.isFinishing();
        if (r0 != 0) goto L_0x021d;
    L_0x0006:
        r0 = r13.isDestroyed();
        if (r0 == 0) goto L_0x000e;
    L_0x000c:
        goto L_0x021d;
    L_0x000e:
        r0 = r14.what;
        switch(r0) {
            case 1: goto L_0x001a;
            case 2: goto L_0x0015;
            default: goto L_0x0013;
        };
    L_0x0013:
        goto L_0x021c;
    L_0x0015:
        r13.initView();
        goto L_0x021c;
    L_0x001a:
        r0 = r13.mHandler;
        r1 = 2;
        r0.removeMessages(r1);
        r0 = r14.arg1;
        r1 = 1;
        if (r0 == r1) goto L_0x0028;
    L_0x0025:
        r13.initView();
    L_0x0028:
        r0 = r14.arg1;
        r13.mResultCode = r0;
        r0 = r14.obj;
        r0 = (java.lang.String) r0;
        r2 = r13.mCallback;
        if (r2 == 0) goto L_0x0049;
    L_0x0034:
        r1 = r13.mCallback;
        r1 = android.content.pm.IPackageDeleteObserver2.Stub.asInterface(r1);
        r2 = r13.mAppInfo;	 Catch:{ RemoteException -> 0x0044 }
        r2 = r2.packageName;	 Catch:{ RemoteException -> 0x0044 }
        r3 = r13.mResultCode;	 Catch:{ RemoteException -> 0x0044 }
        r1.onPackageDeleted(r2, r3, r0);	 Catch:{ RemoteException -> 0x0044 }
        goto L_0x0045;
    L_0x0044:
        r2 = move-exception;
    L_0x0045:
        r13.finish();
        return;
    L_0x0049:
        r2 = r13.getIntent();
        r3 = "android.intent.extra.RETURN_RESULT";
        r4 = 0;
        r2 = r2.getBooleanExtra(r3, r4);
        if (r2 == 0) goto L_0x006f;
    L_0x0056:
        r2 = new android.content.Intent;
        r2.<init>();
        r3 = "android.intent.extra.INSTALL_RESULT";
        r4 = r13.mResultCode;
        r2.putExtra(r3, r4);
        r3 = r13.mResultCode;
        if (r3 != r1) goto L_0x0068;
    L_0x0066:
        r1 = -1;
    L_0x0068:
        r13.setResult(r1, r2);
        r13.finish();
        return;
    L_0x006f:
        r2 = r14.arg1;
        r3 = -4;
        r5 = 2131890820; // 0x7f121284 float:1.9416343E38 double:1.053294015E-314;
        if (r2 == r3) goto L_0x0168;
    L_0x0077:
        r3 = -2;
        if (r2 == r3) goto L_0x00bb;
    L_0x007a:
        if (r2 == r1) goto L_0x00a2;
    L_0x007c:
        r1 = "UninstallAppProgress";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Uninstall failed for ";
        r2.append(r3);
        r2.append(r0);
        r3 = " with code ";
        r2.append(r3);
        r3 = r14.arg1;
        r2.append(r3);
        r2 = r2.toString();
        android.util.Log.d(r1, r2);
        r1 = r13.getString(r5);
        goto L_0x0213;
    L_0x00a2:
        r2 = 2131890818; // 0x7f121282 float:1.9416339E38 double:1.053294014E-314;
        r2 = r13.getString(r2);
        r13.notifyPackageRemoved();
        r3 = r13.getBaseContext();
        r1 = android.widget.Toast.makeText(r3, r2, r1);
        r1.show();
        r13.setResultAndFinish();
        return;
    L_0x00bb:
        r2 = "user";
        r2 = r13.getSystemService(r2);
        r2 = (android.os.UserManager) r2;
        r3 = "device_policy";
        r3 = android.os.ServiceManager.getService(r3);
        r3 = android.app.admin.IDevicePolicyManager.Stub.asInterface(r3);
        r5 = android.os.UserHandle.myUserId();
        r6 = 0;
        r7 = r2.getUsers();
        r7 = r7.iterator();
    L_0x00da:
        r8 = r7.hasNext();
        if (r8 == 0) goto L_0x0103;
    L_0x00e0:
        r8 = r7.next();
        r8 = (android.content.pm.UserInfo) r8;
        r9 = r8.id;
        r9 = r13.isProfileOfOrSame(r2, r5, r9);
        if (r9 == 0) goto L_0x00ef;
    L_0x00ee:
        goto L_0x00da;
    L_0x00ef:
        r9 = r8.id;	 Catch:{ RemoteException -> 0x00fa }
        r9 = r3.packageHasActiveAdmins(r0, r9);	 Catch:{ RemoteException -> 0x00fa }
        if (r9 == 0) goto L_0x00f9;
    L_0x00f7:
        r6 = r8;
        goto L_0x0103;
    L_0x00f9:
        goto L_0x0102;
    L_0x00fa:
        r9 = move-exception;
        r10 = "UninstallAppProgress";
        r11 = "Failed to talk to package manager";
        android.util.Log.e(r10, r11, r9);
    L_0x0102:
        goto L_0x00da;
    L_0x0103:
        if (r6 != 0) goto L_0x0130;
    L_0x0105:
        r4 = "UninstallAppProgress";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "Uninstall failed because ";
        r7.append(r8);
        r7.append(r0);
        r8 = " is a device admin";
        r7.append(r8);
        r7 = r7.toString();
        android.util.Log.d(r4, r7);
        r4 = r13.getProgressFragment();
        r4.setDeviceManagerButtonVisible(r1);
        r1 = 2131890822; // 0x7f121286 float:1.9416347E38 double:1.053294016E-314;
        r1 = r13.getString(r1);
    L_0x012e:
        goto L_0x0213;
    L_0x0130:
        r7 = "UninstallAppProgress";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "Uninstall failed because ";
        r8.append(r9);
        r8.append(r0);
        r9 = " is a device admin of user ";
        r8.append(r9);
        r8.append(r6);
        r8 = r8.toString();
        android.util.Log.d(r7, r8);
        r7 = r13.getProgressFragment();
        r7.setDeviceManagerButtonVisible(r4);
        r7 = 2131890823; // 0x7f121287 float:1.9416349E38 double:1.0532940163E-314;
        r7 = r13.getString(r7);
        r1 = new java.lang.Object[r1];
        r8 = r6.name;
        r1[r4] = r8;
        r1 = java.lang.String.format(r7, r1);
        goto L_0x0213;
    L_0x0168:
        r2 = "user";
        r2 = r13.getSystemService(r2);
        r2 = (android.os.UserManager) r2;
        r3 = "package";
        r3 = android.os.ServiceManager.getService(r3);
        r3 = android.content.pm.IPackageManager.Stub.asInterface(r3);
        r6 = r2.getUsers();
        r7 = -10000; // 0xffffffffffffd8f0 float:NaN double:NaN;
        r8 = r4;
    L_0x0181:
        r9 = r6.size();
        if (r8 >= r9) goto L_0x01a5;
    L_0x0187:
        r9 = r6.get(r8);
        r9 = (android.content.pm.UserInfo) r9;
        r10 = r9.id;	 Catch:{ RemoteException -> 0x019a }
        r10 = r3.getBlockUninstallForUser(r0, r10);	 Catch:{ RemoteException -> 0x019a }
        if (r10 == 0) goto L_0x0199;
    L_0x0195:
        r10 = r9.id;	 Catch:{ RemoteException -> 0x019a }
        r7 = r10;
        goto L_0x01a5;
    L_0x0199:
        goto L_0x01a2;
    L_0x019a:
        r10 = move-exception;
        r11 = "UninstallAppProgress";
        r12 = "Failed to talk to package manager";
        android.util.Log.e(r11, r12, r10);
    L_0x01a2:
        r8 = r8 + 1;
        goto L_0x0181;
    L_0x01a5:
        r8 = android.os.UserHandle.myUserId();
        r9 = r13.isProfileOfOrSame(r2, r8, r7);
        if (r9 == 0) goto L_0x01b7;
    L_0x01af:
        r4 = r13.getProgressFragment();
        r4.setDeviceManagerButtonVisible(r1);
        goto L_0x01c5;
    L_0x01b7:
        r9 = r13.getProgressFragment();
        r9.setDeviceManagerButtonVisible(r4);
        r4 = r13.getProgressFragment();
        r4.setUsersButtonVisible(r1);
    L_0x01c5:
        if (r7 != 0) goto L_0x01d0;
    L_0x01c7:
        r1 = 2131890815; // 0x7f12127f float:1.9416332E38 double:1.0532940124E-314;
        r1 = r13.getString(r1);
        goto L_0x012e;
    L_0x01d0:
        r1 = -10000; // 0xffffffffffffd8f0 float:NaN double:NaN;
        if (r7 != r1) goto L_0x01ff;
    L_0x01d4:
        r1 = "UninstallAppProgress";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r9 = "Uninstall failed for ";
        r4.append(r9);
        r4.append(r0);
        r9 = " with code ";
        r4.append(r9);
        r9 = r14.arg1;
        r4.append(r9);
        r9 = " no blocking user";
        r4.append(r9);
        r4 = r4.toString();
        android.util.Log.d(r1, r4);
        r1 = r13.getString(r5);
        goto L_0x012e;
    L_0x01ff:
        r1 = r13.mAllUsers;
        if (r1 == 0) goto L_0x020b;
    L_0x0203:
        r1 = 2131890809; // 0x7f121279 float:1.941632E38 double:1.0532940094E-314;
        r1 = r13.getString(r1);
        goto L_0x0212;
    L_0x020b:
        r1 = 2131890816; // 0x7f121280 float:1.9416334E38 double:1.053294013E-314;
        r1 = r13.getString(r1);
        r2 = r13.getProgressFragment();
        r2.showCompletion(r1);
    L_0x021c:
        return;
    L_0x021d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.packageuninstaller.UninstallAppProgress.handleMessage(android.os.Message):void");
    }

    private boolean isProfileOfOrSame(UserManager userManager, int userId, int profileId) {
        boolean z = true;
        if (userId == profileId) {
            return true;
        }
        UserInfo parentUser = userManager.getProfileParent(profileId);
        if (parentUser == null || parentUser.id != userId) {
            z = false;
        }
        return z;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        this.mAppInfo = (ApplicationInfo) intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        this.mCallback = intent.getIBinderExtra("android.content.pm.extra.CALLBACK");
        if (icicle != null) {
            this.mResultCode = -1;
            if (this.mCallback != null) {
                try {
                    IPackageDeleteObserver2.Stub.asInterface(this.mCallback).onPackageDeleted(this.mAppInfo.packageName, this.mResultCode, null);
                } catch (RemoteException e) {
                }
                finish();
            } else {
                setResultAndFinish();
            }
            return;
        }
        int i = 0;
        this.mAllUsers = intent.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
        UserHandle user = (UserHandle) intent.getParcelableExtra("android.intent.extra.USER");
        if (user == null) {
            user = Process.myUserHandle();
        }
        PackageDeleteObserver observer = new PackageDeleteObserver();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getWindow().setStatusBarColor(0);
        getWindow().setNavigationBarColor(0);
        try {
            PackageManager packageManager = getPackageManager();
            String str = this.mAppInfo.packageName;
            if (this.mAllUsers) {
                i = 2;
            }
            packageManager.deletePackageAsUser(str, observer, i, user.getIdentifier());
        } catch (IllegalArgumentException e2) {
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not find package, not deleting ");
            stringBuilder.append(this.mAppInfo.packageName);
            Log.w(str2, stringBuilder.toString(), e2);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 500);
    }

    public ApplicationInfo getAppInfo() {
        return this.mAppInfo;
    }

    public void setResultAndFinish() {
        setResult(this.mResultCode);
        finish();
    }

    private void initView() {
        if (!this.mIsViewInitialized) {
            boolean isUpdate = true;
            this.mIsViewInitialized = true;
            TypedValue attribute = new TypedValue();
            getTheme().resolveAttribute(16842836, attribute, true);
            if (attribute.type < 28 || attribute.type > 31) {
                getWindow().setBackgroundDrawable(getResources().getDrawable(attribute.resourceId, getTheme()));
            } else {
                getWindow().setBackgroundDrawable(new ColorDrawable(attribute.data));
            }
            getTheme().resolveAttribute(16843858, attribute, true);
            getWindow().setNavigationBarColor(attribute.data);
            getTheme().resolveAttribute(16843857, attribute, true);
            getWindow().setStatusBarColor(attribute.data);
            if ((this.mAppInfo.flags & 128) == 0) {
                isUpdate = false;
            }
            setTitle(isUpdate ? R.string.uninstall_update_title : R.string.uninstall_application_title);
            getFragmentManager().beginTransaction().add(16908290, new UninstallAppProgressFragment(), FRAGMENT_TAG).commitNowAllowingStateLoss();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == 4) {
            if (this.mResultCode == -1) {
                return true;
            }
            setResult(this.mResultCode);
        }
        return super.dispatchKeyEvent(ev);
    }

    private void notifyPackageRemoved() {
        Intent intent = new Intent(OPConstants.ONEPLUS_ACTION_PACKAGE_REMOVED);
        intent.setFlags(285212672);
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }

    private ProgressFragment getProgressFragment() {
        return (ProgressFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
}
