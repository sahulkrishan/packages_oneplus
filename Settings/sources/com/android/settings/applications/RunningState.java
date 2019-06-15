package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RunningState {
    static final long CONTENTS_UPDATE_DELAY = 2000;
    static final boolean DEBUG_COMPARE = false;
    static final int MAX_SERVICES = 100;
    static final int MSG_REFRESH_UI = 3;
    static final int MSG_RESET_CONTENTS = 1;
    static final int MSG_UPDATE_CONTENTS = 2;
    static final int MSG_UPDATE_TIME = 4;
    static final String TAG = "RunningState";
    static final long TIME_UPDATE_DELAY = 1000;
    static Object sGlobalLock = new Object();
    static RunningState sInstance;
    final ArrayList<ProcessItem> mAllProcessItems = new ArrayList();
    final ActivityManager mAm;
    final Context mApplicationContext;
    final Comparator<MergedItem> mBackgroundComparator = new Comparator<MergedItem>() {
        public int compare(MergedItem lhs, MergedItem rhs) {
            int i = -1;
            if (lhs.mUserId != rhs.mUserId) {
                if (lhs.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                if (rhs.mUserId == RunningState.this.mMyUserId) {
                    return 1;
                }
                if (lhs.mUserId >= rhs.mUserId) {
                    i = 1;
                }
                return i;
            } else if (lhs.mProcess == rhs.mProcess) {
                if (lhs.mLabel == rhs.mLabel) {
                    return 0;
                }
                if (lhs.mLabel != null) {
                    i = lhs.mLabel.compareTo(rhs.mLabel);
                }
                return i;
            } else if (lhs.mProcess == null) {
                return -1;
            } else {
                if (rhs.mProcess == null) {
                    return 1;
                }
                RunningAppProcessInfo lhsInfo = lhs.mProcess.mRunningProcessInfo;
                RunningAppProcessInfo rhsInfo = rhs.mProcess.mRunningProcessInfo;
                boolean lhsBg = lhsInfo.importance >= 400;
                if (lhsBg != (rhsInfo.importance >= 400)) {
                    if (lhsBg) {
                        i = 1;
                    }
                    return i;
                }
                boolean lhsA = (lhsInfo.flags & 4) != 0;
                if (lhsA != ((rhsInfo.flags & 4) != 0)) {
                    if (!lhsA) {
                        i = 1;
                    }
                    return i;
                } else if (lhsInfo.lru != rhsInfo.lru) {
                    if (lhsInfo.lru >= rhsInfo.lru) {
                        i = 1;
                    }
                    return i;
                } else if (lhs.mProcess.mLabel == rhs.mProcess.mLabel) {
                    return 0;
                } else {
                    if (lhs.mProcess.mLabel == null) {
                        return 1;
                    }
                    if (rhs.mProcess.mLabel == null) {
                        return -1;
                    }
                    return lhs.mProcess.mLabel.compareTo(rhs.mProcess.mLabel);
                }
            }
        }
    };
    final BackgroundHandler mBackgroundHandler;
    ArrayList<MergedItem> mBackgroundItems = new ArrayList();
    long mBackgroundProcessMemory;
    final HandlerThread mBackgroundThread;
    long mForegroundProcessMemory;
    final Handler mHandler = new Handler() {
        int mNextUpdate = 0;

        /* JADX WARNING: Missing block: B:10:0x0014, code skipped:
            removeMessages(4);
            sendMessageDelayed(obtainMessage(4), 1000);
     */
        /* JADX WARNING: Missing block: B:11:0x0025, code skipped:
            if (r3.this$0.mRefreshUiListener == null) goto L_0x0041;
     */
        /* JADX WARNING: Missing block: B:12:0x0027, code skipped:
            r3.this$0.mRefreshUiListener.onRefreshUi(r3.mNextUpdate);
            r3.mNextUpdate = 0;
     */
        public void handleMessage(android.os.Message r4) {
            /*
            r3 = this;
            r0 = r4.what;
            switch(r0) {
                case 3: goto L_0x0037;
                case 4: goto L_0x0006;
                default: goto L_0x0005;
            };
        L_0x0005:
            goto L_0x0041;
        L_0x0006:
            r0 = com.android.settings.applications.RunningState.this;
            r0 = r0.mLock;
            monitor-enter(r0);
            r1 = com.android.settings.applications.RunningState.this;	 Catch:{ all -> 0x0034 }
            r1 = r1.mResumed;	 Catch:{ all -> 0x0034 }
            if (r1 != 0) goto L_0x0013;
        L_0x0011:
            monitor-exit(r0);	 Catch:{ all -> 0x0034 }
            return;
        L_0x0013:
            monitor-exit(r0);	 Catch:{ all -> 0x0034 }
            r0 = 4;
            r3.removeMessages(r0);
            r0 = r3.obtainMessage(r0);
            r1 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
            r3.sendMessageDelayed(r0, r1);
            r1 = com.android.settings.applications.RunningState.this;
            r1 = r1.mRefreshUiListener;
            if (r1 == 0) goto L_0x0041;
        L_0x0027:
            r1 = com.android.settings.applications.RunningState.this;
            r1 = r1.mRefreshUiListener;
            r2 = r3.mNextUpdate;
            r1.onRefreshUi(r2);
            r1 = 0;
            r3.mNextUpdate = r1;
            goto L_0x0041;
        L_0x0034:
            r1 = move-exception;
            monitor-exit(r0);	 Catch:{ all -> 0x0034 }
            throw r1;
        L_0x0037:
            r0 = r4.arg1;
            if (r0 == 0) goto L_0x003d;
        L_0x003b:
            r0 = 2;
            goto L_0x003e;
        L_0x003d:
            r0 = 1;
        L_0x003e:
            r3.mNextUpdate = r0;
        L_0x0041:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.RunningState$AnonymousClass2.handleMessage(android.os.Message):void");
        }
    };
    boolean mHaveData;
    final boolean mHideManagedProfiles;
    final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();
    final ArrayList<ProcessItem> mInterestingProcesses = new ArrayList();
    ArrayList<BaseItem> mItems = new ArrayList();
    final Object mLock = new Object();
    ArrayList<MergedItem> mMergedItems = new ArrayList();
    final int mMyUserId;
    int mNumBackgroundProcesses;
    int mNumForegroundProcesses;
    int mNumServiceProcesses;
    final SparseArray<MergedItem> mOtherUserBackgroundItems = new SparseArray();
    final SparseArray<MergedItem> mOtherUserMergedItems = new SparseArray();
    final PackageManager mPm;
    final ArrayList<ProcessItem> mProcessItems = new ArrayList();
    OnRefreshUiListener mRefreshUiListener;
    boolean mResumed;
    final SparseArray<ProcessItem> mRunningProcesses = new SparseArray();
    int mSequence = 0;
    final ServiceProcessComparator mServiceProcessComparator = new ServiceProcessComparator();
    long mServiceProcessMemory;
    final SparseArray<HashMap<String, ProcessItem>> mServiceProcessesByName = new SparseArray();
    final SparseArray<ProcessItem> mServiceProcessesByPid = new SparseArray();
    final SparseArray<AppProcessInfo> mTmpAppProcesses = new SparseArray();
    final UserManager mUm;
    private final UserManagerBroadcastReceiver mUmBroadcastReceiver = new UserManagerBroadcastReceiver(this, null);
    ArrayList<MergedItem> mUserBackgroundItems = new ArrayList();
    boolean mWatchingBackgroundItems;

    static class AppProcessInfo {
        boolean hasForegroundServices;
        boolean hasServices;
        final RunningAppProcessInfo info;

        AppProcessInfo(RunningAppProcessInfo _info) {
            this.info = _info;
        }
    }

    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:10:0x0014, code skipped:
            r0 = r4.this$0.mHandler.obtainMessage(3);
            r0.arg1 = com.android.settings.applications.RunningState.access$100(r4.this$0, r4.this$0.mApplicationContext, r4.this$0.mAm);
            r4.this$0.mHandler.sendMessage(r0);
            removeMessages(2);
            sendMessageDelayed(obtainMessage(2), 2000);
     */
        public void handleMessage(android.os.Message r5) {
            /*
            r4 = this;
            r0 = r5.what;
            switch(r0) {
                case 1: goto L_0x0045;
                case 2: goto L_0x0006;
                default: goto L_0x0005;
            };
        L_0x0005:
            goto L_0x004b;
        L_0x0006:
            r0 = com.android.settings.applications.RunningState.this;
            r0 = r0.mLock;
            monitor-enter(r0);
            r1 = com.android.settings.applications.RunningState.this;	 Catch:{ all -> 0x0042 }
            r1 = r1.mResumed;	 Catch:{ all -> 0x0042 }
            if (r1 != 0) goto L_0x0013;
        L_0x0011:
            monitor-exit(r0);	 Catch:{ all -> 0x0042 }
            return;
        L_0x0013:
            monitor-exit(r0);	 Catch:{ all -> 0x0042 }
            r0 = com.android.settings.applications.RunningState.this;
            r0 = r0.mHandler;
            r1 = 3;
            r0 = r0.obtainMessage(r1);
            r1 = com.android.settings.applications.RunningState.this;
            r2 = com.android.settings.applications.RunningState.this;
            r2 = r2.mApplicationContext;
            r3 = com.android.settings.applications.RunningState.this;
            r3 = r3.mAm;
            r1 = r1.update(r2, r3);
            r0.arg1 = r1;
            r1 = com.android.settings.applications.RunningState.this;
            r1 = r1.mHandler;
            r1.sendMessage(r0);
            r1 = 2;
            r4.removeMessages(r1);
            r5 = r4.obtainMessage(r1);
            r1 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
            r4.sendMessageDelayed(r5, r1);
            goto L_0x004b;
        L_0x0042:
            r1 = move-exception;
            monitor-exit(r0);	 Catch:{ all -> 0x0042 }
            throw r1;
        L_0x0045:
            r0 = com.android.settings.applications.RunningState.this;
            r0.reset();
        L_0x004b:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.RunningState$BackgroundHandler.handleMessage(android.os.Message):void");
        }
    }

    static class BaseItem {
        long mActiveSince;
        boolean mBackground;
        int mCurSeq;
        String mCurSizeStr;
        String mDescription;
        CharSequence mDisplayLabel;
        final boolean mIsProcess;
        String mLabel;
        boolean mNeedDivider;
        PackageItemInfo mPackageInfo;
        long mSize;
        String mSizeStr;
        final int mUserId;

        public BaseItem(boolean isProcess, int userId) {
            this.mIsProcess = isProcess;
            this.mUserId = userId;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mPackageInfo == null) {
                return null;
            }
            return state.mPm.getUserBadgedIcon(this.mPackageInfo.loadUnbadgedIcon(state.mPm), new UserHandle(this.mUserId));
        }
    }

    interface OnRefreshUiListener {
        public static final int REFRESH_DATA = 1;
        public static final int REFRESH_STRUCTURE = 2;
        public static final int REFRESH_TIME = 0;

        void onRefreshUi(int i);
    }

    class ServiceProcessComparator implements Comparator<ProcessItem> {
        ServiceProcessComparator() {
        }

        public int compare(ProcessItem object1, ProcessItem object2) {
            int i = 1;
            if (object1.mUserId != object2.mUserId) {
                if (object1.mUserId == RunningState.this.mMyUserId) {
                    return -1;
                }
                if (object2.mUserId == RunningState.this.mMyUserId) {
                    return 1;
                }
                if (object1.mUserId < object2.mUserId) {
                    i = -1;
                }
                return i;
            } else if (object1.mIsStarted != object2.mIsStarted) {
                if (object1.mIsStarted) {
                    i = -1;
                }
                return i;
            } else if (object1.mIsSystem != object2.mIsSystem) {
                if (!object1.mIsSystem) {
                    i = -1;
                }
                return i;
            } else if (object1.mActiveSince == object2.mActiveSince) {
                return 0;
            } else {
                if (object1.mActiveSince > object2.mActiveSince) {
                    i = -1;
                }
                return i;
            }
        }
    }

    private final class UserManagerBroadcastReceiver extends BroadcastReceiver {
        private volatile boolean usersChanged;

        private UserManagerBroadcastReceiver() {
        }

        /* synthetic */ UserManagerBroadcastReceiver(RunningState x0, AnonymousClass1 x1) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (RunningState.this.mLock) {
                if (RunningState.this.mResumed) {
                    RunningState.this.mHaveData = false;
                    RunningState.this.mBackgroundHandler.removeMessages(1);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(1);
                    RunningState.this.mBackgroundHandler.removeMessages(2);
                    RunningState.this.mBackgroundHandler.sendEmptyMessage(2);
                } else {
                    this.usersChanged = true;
                }
            }
        }

        public boolean checkUsersChangedLocked() {
            boolean oldValue = this.usersChanged;
            this.usersChanged = false;
            return oldValue;
        }

        /* Access modifiers changed, original: 0000 */
        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_STOPPED");
            filter.addAction("android.intent.action.USER_STARTED");
            filter.addAction("android.intent.action.USER_INFO_CHANGED");
            context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        }
    }

    static class UserState {
        Drawable mIcon;
        UserInfo mInfo;
        String mLabel;

        UserState() {
        }
    }

    static class MergedItem extends BaseItem {
        final ArrayList<MergedItem> mChildren = new ArrayList();
        private int mLastNumProcesses = -1;
        private int mLastNumServices = -1;
        final ArrayList<ProcessItem> mOtherProcesses = new ArrayList();
        ProcessItem mProcess;
        final ArrayList<ServiceItem> mServices = new ArrayList();
        UserState mUser;

        MergedItem(int userId) {
            super(false, userId);
        }

        private void setDescription(Context context, int numProcesses, int numServices) {
            if (this.mLastNumProcesses != numProcesses || this.mLastNumServices != numServices) {
                this.mLastNumProcesses = numProcesses;
                this.mLastNumServices = numServices;
                int resid = R.string.running_processes_item_description_s_s;
                if (numProcesses != 1) {
                    int i;
                    if (numServices != 1) {
                        i = R.string.running_processes_item_description_p_p;
                    } else {
                        i = R.string.running_processes_item_description_p_s;
                    }
                    resid = i;
                } else if (numServices != 1) {
                    resid = R.string.running_processes_item_description_s_p;
                }
                this.mDescription = context.getResources().getString(resid, new Object[]{Integer.valueOf(numProcesses), Integer.valueOf(numServices)});
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean update(Context context, boolean background) {
            this.mBackground = background;
            if (this.mUser != null) {
                this.mPackageInfo = ((MergedItem) this.mChildren.get(0)).mProcess.mPackageInfo;
                this.mLabel = this.mUser != null ? this.mUser.mLabel : null;
                this.mDisplayLabel = this.mLabel;
                int numProcesses = 0;
                int numServices = 0;
                this.mActiveSince = -1;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = (MergedItem) this.mChildren.get(i);
                    numProcesses += child.mLastNumProcesses;
                    numServices += child.mLastNumServices;
                    if (child.mActiveSince >= 0 && this.mActiveSince < child.mActiveSince) {
                        this.mActiveSince = child.mActiveSince;
                    }
                }
                if (!this.mBackground) {
                    setDescription(context, numProcesses, numServices);
                }
            } else {
                this.mPackageInfo = this.mProcess.mPackageInfo;
                this.mDisplayLabel = this.mProcess.mDisplayLabel;
                this.mLabel = this.mProcess.mLabel;
                if (!this.mBackground) {
                    setDescription(context, (this.mProcess.mPid > 0 ? 1 : 0) + this.mOtherProcesses.size(), this.mServices.size());
                }
                this.mActiveSince = -1;
                for (int i2 = 0; i2 < this.mServices.size(); i2++) {
                    ServiceItem si = (ServiceItem) this.mServices.get(i2);
                    if (si.mActiveSince >= 0 && this.mActiveSince < si.mActiveSince) {
                        this.mActiveSince = si.mActiveSince;
                    }
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean updateSize(Context context) {
            int i;
            if (this.mUser != null) {
                this.mSize = 0;
                for (i = 0; i < this.mChildren.size(); i++) {
                    MergedItem child = (MergedItem) this.mChildren.get(i);
                    child.updateSize(context);
                    this.mSize += child.mSize;
                }
            } else {
                this.mSize = this.mProcess.mSize;
                for (i = 0; i < this.mOtherProcesses.size(); i++) {
                    this.mSize += ((ProcessItem) this.mOtherProcesses.get(i)).mSize;
                }
            }
            String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
            if (sizeStr.equals(this.mSizeStr)) {
                return false;
            }
            this.mSizeStr = sizeStr;
            return false;
        }

        public Drawable loadIcon(Context context, RunningState state) {
            if (this.mUser == null) {
                return super.loadIcon(context, state);
            }
            if (this.mUser.mIcon == null) {
                return context.getDrawable(17302624);
            }
            ConstantState constState = this.mUser.mIcon.getConstantState();
            if (constState == null) {
                return this.mUser.mIcon;
            }
            return constState.newDrawable();
        }
    }

    static class ProcessItem extends BaseItem {
        long mActiveSince;
        ProcessItem mClient;
        final SparseArray<ProcessItem> mDependentProcesses = new SparseArray();
        boolean mInteresting;
        boolean mIsStarted;
        boolean mIsSystem;
        int mLastNumDependentProcesses;
        MergedItem mMergedItem;
        int mPid;
        final String mProcessName;
        RunningAppProcessInfo mRunningProcessInfo;
        int mRunningSeq;
        final HashMap<ComponentName, ServiceItem> mServices = new HashMap();
        final int mUid;

        public ProcessItem(Context context, int uid, String processName) {
            super(true, UserHandle.getUserId(uid));
            this.mDescription = context.getResources().getString(R.string.service_process_name, new Object[]{processName});
            this.mUid = uid;
            this.mProcessName = processName;
        }

        /* Access modifiers changed, original: 0000 */
        public void ensureLabel(PackageManager pm) {
            if (this.mLabel == null) {
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(this.mProcessName, 4194304);
                    if (ai.uid == this.mUid) {
                        this.mDisplayLabel = ai.loadLabel(pm);
                        this.mLabel = this.mDisplayLabel.toString();
                        this.mPackageInfo = ai;
                        return;
                    }
                } catch (NameNotFoundException e) {
                }
                String[] pkgs = pm.getPackagesForUid(this.mUid);
                if (pkgs.length == 1) {
                    try {
                        ApplicationInfo ai2 = pm.getApplicationInfo(pkgs[0], 4194304);
                        this.mDisplayLabel = ai2.loadLabel(pm);
                        this.mLabel = this.mDisplayLabel.toString();
                        this.mPackageInfo = ai2;
                        return;
                    } catch (NameNotFoundException e2) {
                    }
                }
                for (String name : pkgs) {
                    try {
                        PackageInfo pi = pm.getPackageInfo(name, 0);
                        if (pi.sharedUserLabel != 0) {
                            CharSequence nm = pm.getText(name, pi.sharedUserLabel, pi.applicationInfo);
                            if (nm != null) {
                                this.mDisplayLabel = nm;
                                this.mLabel = nm.toString();
                                this.mPackageInfo = pi.applicationInfo;
                                return;
                            }
                        } else {
                            continue;
                        }
                    } catch (NameNotFoundException e3) {
                    }
                }
                if (this.mServices.size() > 0) {
                    this.mPackageInfo = ((ServiceItem) this.mServices.values().iterator().next()).mServiceInfo.applicationInfo;
                    this.mDisplayLabel = this.mPackageInfo.loadLabel(pm);
                    this.mLabel = this.mDisplayLabel.toString();
                    return;
                }
                try {
                    ApplicationInfo ai3 = pm.getApplicationInfo(pkgs[0], 4194304);
                    this.mDisplayLabel = ai3.loadLabel(pm);
                    this.mLabel = this.mDisplayLabel.toString();
                    this.mPackageInfo = ai3;
                } catch (NameNotFoundException e4) {
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean updateService(Context context, RunningServiceInfo service) {
            PackageManager pm = context.getPackageManager();
            boolean changed = false;
            ServiceItem si = (ServiceItem) this.mServices.get(service.service);
            if (si == null) {
                changed = true;
                si = new ServiceItem(this.mUserId);
                si.mRunningService = service;
                try {
                    si.mServiceInfo = ActivityThread.getPackageManager().getServiceInfo(service.service, 4194304, UserHandle.getUserId(service.uid));
                    if (si.mServiceInfo == null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("getServiceInfo returned null for: ");
                        stringBuilder.append(service.service);
                        Log.d("RunningService", stringBuilder.toString());
                        return false;
                    }
                } catch (RemoteException e) {
                }
                si.mDisplayLabel = RunningState.makeLabel(pm, si.mRunningService.service.getClassName(), si.mServiceInfo);
                this.mLabel = this.mDisplayLabel != null ? this.mDisplayLabel.toString() : null;
                si.mPackageInfo = si.mServiceInfo.applicationInfo;
                this.mServices.put(service.service, si);
            }
            si.mCurSeq = this.mCurSeq;
            si.mRunningService = service;
            long activeSince = service.restarting == 0 ? service.activeSince : -1;
            if (si.mActiveSince != activeSince) {
                si.mActiveSince = activeSince;
                changed = true;
            }
            if (service.clientPackage == null || service.clientLabel == 0) {
                if (!si.mShownAsStarted) {
                    si.mShownAsStarted = true;
                    changed = true;
                }
                si.mDescription = context.getResources().getString(R.string.service_started_by_app);
            } else {
                if (si.mShownAsStarted) {
                    si.mShownAsStarted = false;
                    changed = true;
                }
                try {
                    String label = pm.getResourcesForApplication(service.clientPackage).getString(service.clientLabel);
                    si.mDescription = context.getResources().getString(R.string.service_client_name, new Object[]{label});
                } catch (NameNotFoundException e2) {
                    si.mDescription = null;
                }
            }
            return changed;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean updateSize(Context context, long pss, int curSeq) {
            this.mSize = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID * pss;
            if (this.mCurSeq == curSeq) {
                String sizeStr = Formatter.formatShortFileSize(context, this.mSize);
                if (!sizeStr.equals(this.mSizeStr)) {
                    this.mSizeStr = sizeStr;
                    return false;
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean buildDependencyChain(Context context, PackageManager pm, int curSeq) {
            int NP = this.mDependentProcesses.size();
            boolean changed = false;
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = (ProcessItem) this.mDependentProcesses.valueAt(i);
                if (proc.mClient != this) {
                    changed = true;
                    proc.mClient = this;
                }
                proc.mCurSeq = curSeq;
                proc.ensureLabel(pm);
                changed |= proc.buildDependencyChain(context, pm, curSeq);
            }
            if (this.mLastNumDependentProcesses == this.mDependentProcesses.size()) {
                return changed;
            }
            this.mLastNumDependentProcesses = this.mDependentProcesses.size();
            return true;
        }

        /* Access modifiers changed, original: 0000 */
        public void addDependentProcesses(ArrayList<BaseItem> dest, ArrayList<ProcessItem> destProc) {
            int NP = this.mDependentProcesses.size();
            for (int i = 0; i < NP; i++) {
                ProcessItem proc = (ProcessItem) this.mDependentProcesses.valueAt(i);
                proc.addDependentProcesses(dest, destProc);
                dest.add(proc);
                if (proc.mPid > 0) {
                    destProc.add(proc);
                }
            }
        }
    }

    static class ServiceItem extends BaseItem {
        MergedItem mMergedItem;
        RunningServiceInfo mRunningService;
        ServiceInfo mServiceInfo;
        boolean mShownAsStarted;

        public ServiceItem(int userId) {
            super(false, userId);
        }
    }

    static CharSequence makeLabel(PackageManager pm, String className, PackageItemInfo item) {
        if (!(item == null || (item.labelRes == 0 && item.nonLocalizedLabel == null))) {
            CharSequence label = item.loadLabel(pm);
            if (label != null) {
                return label;
            }
        }
        String label2 = className;
        int tail = label2.lastIndexOf(46);
        if (tail >= 0) {
            label2 = label2.substring(tail + 1, label2.length());
        }
        return label2;
    }

    static RunningState getInstance(Context context) {
        RunningState runningState;
        synchronized (sGlobalLock) {
            if (sInstance == null) {
                sInstance = new RunningState(context);
            }
            runningState = sInstance;
        }
        return runningState;
    }

    private RunningState(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        this.mAm = (ActivityManager) this.mApplicationContext.getSystemService("activity");
        this.mPm = this.mApplicationContext.getPackageManager();
        this.mUm = (UserManager) this.mApplicationContext.getSystemService("user");
        this.mMyUserId = UserHandle.myUserId();
        UserInfo userInfo = this.mUm.getUserInfo(this.mMyUserId);
        boolean z = userInfo == null || !userInfo.canHaveProfile();
        this.mHideManagedProfiles = z;
        this.mResumed = false;
        this.mBackgroundThread = new HandlerThread("RunningState:Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new BackgroundHandler(this.mBackgroundThread.getLooper());
        this.mUmBroadcastReceiver.register(this.mApplicationContext);
    }

    /* Access modifiers changed, original: 0000 */
    public void resume(OnRefreshUiListener listener) {
        synchronized (this.mLock) {
            this.mResumed = true;
            this.mRefreshUiListener = listener;
            boolean usersChanged = this.mUmBroadcastReceiver.checkUsersChangedLocked();
            boolean configChanged = this.mInterestingConfigChanges.applyNewConfig(this.mApplicationContext.getResources());
            if (usersChanged || configChanged) {
                this.mHaveData = false;
                this.mBackgroundHandler.removeMessages(1);
                this.mBackgroundHandler.removeMessages(2);
                this.mBackgroundHandler.sendEmptyMessage(1);
            }
            if (!this.mBackgroundHandler.hasMessages(2)) {
                this.mBackgroundHandler.sendEmptyMessage(2);
            }
            this.mHandler.sendEmptyMessage(4);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateNow() {
        synchronized (this.mLock) {
            this.mBackgroundHandler.removeMessages(2);
            this.mBackgroundHandler.sendEmptyMessage(2);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasData() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mHaveData;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    public void waitForData() {
        synchronized (this.mLock) {
            while (!this.mHaveData) {
                try {
                    this.mLock.wait(0);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void pause() {
        synchronized (this.mLock) {
            this.mResumed = false;
            this.mRefreshUiListener = null;
            this.mHandler.removeMessages(4);
        }
    }

    private boolean isInterestingProcess(RunningAppProcessInfo pi) {
        if ((pi.flags & 1) != 0) {
            return true;
        }
        if ((pi.flags & 2) != 0 || pi.importance < 100 || pi.importance >= 350 || pi.importanceReasonCode != 0) {
            return false;
        }
        return true;
    }

    private void reset() {
        this.mServiceProcessesByName.clear();
        this.mServiceProcessesByPid.clear();
        this.mInterestingProcesses.clear();
        this.mRunningProcesses.clear();
        this.mProcessItems.clear();
        this.mAllProcessItems.clear();
    }

    private void addOtherUserItem(Context context, ArrayList<MergedItem> newMergedItems, SparseArray<MergedItem> userItems, MergedItem newItem) {
        MergedItem userItem = (MergedItem) userItems.get(newItem.mUserId);
        boolean first = userItem == null || userItem.mCurSeq != this.mSequence;
        if (first) {
            UserInfo info = this.mUm.getUserInfo(newItem.mUserId);
            if (info != null) {
                if (!this.mHideManagedProfiles || !info.isManagedProfile()) {
                    if (userItem == null) {
                        userItem = new MergedItem(newItem.mUserId);
                        userItems.put(newItem.mUserId, userItem);
                    } else {
                        userItem.mChildren.clear();
                    }
                    userItem.mCurSeq = this.mSequence;
                    userItem.mUser = new UserState();
                    userItem.mUser.mInfo = info;
                    userItem.mUser.mIcon = Utils.getUserIcon(context, this.mUm, info);
                    userItem.mUser.mLabel = Utils.getUserLabel(context, info);
                    newMergedItems.add(userItem);
                } else {
                    return;
                }
            }
            return;
        }
        userItem.mChildren.add(newItem);
    }

    /* JADX WARNING: Missing block: B:392:0x0834, code skipped:
            return r26;
     */
    private boolean update(android.content.Context r45, android.app.ActivityManager r46) {
        /*
        r44 = this;
        r1 = r44;
        r2 = r45;
        r3 = r45.getPackageManager();
        r0 = r1.mSequence;
        r4 = 1;
        r0 = r0 + r4;
        r1.mSequence = r0;
        r0 = 0;
        r5 = 100;
        r6 = r46;
        r5 = r6.getRunningServices(r5);
        if (r5 == 0) goto L_0x001f;
    L_0x001a:
        r8 = r5.size();
        goto L_0x0020;
    L_0x001f:
        r8 = 0;
    L_0x0020:
        r9 = r8;
        r8 = 0;
    L_0x0022:
        if (r8 >= r9) goto L_0x004a;
    L_0x0024:
        r10 = r5.get(r8);
        r10 = (android.app.ActivityManager.RunningServiceInfo) r10;
        r11 = r10.started;
        if (r11 != 0) goto L_0x003a;
    L_0x002e:
        r11 = r10.clientLabel;
        if (r11 != 0) goto L_0x003a;
    L_0x0032:
        r5.remove(r8);
        r8 = r8 + -1;
        r9 = r9 + -1;
        goto L_0x0048;
    L_0x003a:
        r11 = r10.flags;
        r11 = r11 & 8;
        if (r11 == 0) goto L_0x0048;
    L_0x0040:
        r5.remove(r8);
        r8 = r8 + -1;
        r9 = r9 + -1;
    L_0x0048:
        r8 = r8 + r4;
        goto L_0x0022;
        r8 = r46.getRunningAppProcesses();
        if (r8 == 0) goto L_0x0056;
    L_0x0051:
        r10 = r8.size();
        goto L_0x0057;
    L_0x0056:
        r10 = 0;
    L_0x0057:
        r11 = r1.mTmpAppProcesses;
        r11.clear();
        r11 = 0;
    L_0x005d:
        if (r11 >= r10) goto L_0x0074;
    L_0x005f:
        r12 = r8.get(r11);
        r12 = (android.app.ActivityManager.RunningAppProcessInfo) r12;
        r13 = r1.mTmpAppProcesses;
        r14 = r12.pid;
        r15 = new com.android.settings.applications.RunningState$AppProcessInfo;
        r15.<init>(r12);
        r13.put(r14, r15);
        r11 = r11 + 1;
        goto L_0x005d;
    L_0x0074:
        r11 = 0;
    L_0x0075:
        r12 = 0;
        if (r11 >= r9) goto L_0x00a4;
    L_0x0079:
        r14 = r5.get(r11);
        r14 = (android.app.ActivityManager.RunningServiceInfo) r14;
        r17 = r8;
        r7 = r14.restarting;
        r7 = (r7 > r12 ? 1 : (r7 == r12 ? 0 : -1));
        if (r7 != 0) goto L_0x009f;
    L_0x0087:
        r7 = r14.pid;
        if (r7 <= 0) goto L_0x009f;
    L_0x008b:
        r7 = r1.mTmpAppProcesses;
        r8 = r14.pid;
        r7 = r7.get(r8);
        r7 = (com.android.settings.applications.RunningState.AppProcessInfo) r7;
        if (r7 == 0) goto L_0x009f;
    L_0x0097:
        r7.hasServices = r4;
        r8 = r14.foreground;
        if (r8 == 0) goto L_0x009f;
    L_0x009d:
        r7.hasForegroundServices = r4;
    L_0x009f:
        r11 = r11 + 1;
        r8 = r17;
        goto L_0x0075;
    L_0x00a4:
        r17 = r8;
        r7 = r0;
        r0 = 0;
    L_0x00a8:
        if (r0 >= r9) goto L_0x017b;
    L_0x00aa:
        r8 = r5.get(r0);
        r8 = (android.app.ActivityManager.RunningServiceInfo) r8;
        r14 = r8.restarting;
        r11 = (r14 > r12 ? 1 : (r14 == r12 ? 0 : -1));
        if (r11 != 0) goto L_0x0102;
    L_0x00b6:
        r11 = r8.pid;
        if (r11 <= 0) goto L_0x0102;
    L_0x00ba:
        r11 = r1.mTmpAppProcesses;
        r14 = r8.pid;
        r11 = r11.get(r14);
        r11 = (com.android.settings.applications.RunningState.AppProcessInfo) r11;
        if (r11 == 0) goto L_0x0102;
    L_0x00c6:
        r14 = r11.hasForegroundServices;
        if (r14 != 0) goto L_0x0102;
    L_0x00ca:
        r14 = r11.info;
        r14 = r14.importance;
        r15 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        if (r14 >= r15) goto L_0x0102;
    L_0x00d2:
        r14 = 0;
        r15 = r1.mTmpAppProcesses;
        r4 = r11.info;
        r4 = r4.importanceReasonPid;
        r4 = r15.get(r4);
        r4 = (com.android.settings.applications.RunningState.AppProcessInfo) r4;
    L_0x00df:
        if (r4 == 0) goto L_0x00fe;
    L_0x00e1:
        r11 = r4.hasServices;
        if (r11 != 0) goto L_0x00fc;
    L_0x00e5:
        r11 = r4.info;
        r11 = r1.isInterestingProcess(r11);
        if (r11 == 0) goto L_0x00ee;
    L_0x00ed:
        goto L_0x00fc;
    L_0x00ee:
        r11 = r1.mTmpAppProcesses;
        r15 = r4.info;
        r15 = r15.importanceReasonPid;
        r11 = r11.get(r15);
        r4 = r11;
        r4 = (com.android.settings.applications.RunningState.AppProcessInfo) r4;
        goto L_0x00df;
    L_0x00fc:
        r14 = 1;
    L_0x00fe:
        if (r14 == 0) goto L_0x0102;
    L_0x0100:
        goto L_0x0174;
    L_0x0102:
        r4 = r1.mServiceProcessesByName;
        r11 = r8.uid;
        r4 = r4.get(r11);
        r4 = (java.util.HashMap) r4;
        if (r4 != 0) goto L_0x011b;
    L_0x010e:
        r11 = new java.util.HashMap;
        r11.<init>();
        r4 = r11;
        r11 = r1.mServiceProcessesByName;
        r14 = r8.uid;
        r11.put(r14, r4);
    L_0x011b:
        r11 = r8.process;
        r11 = r4.get(r11);
        r11 = (com.android.settings.applications.RunningState.ProcessItem) r11;
        if (r11 != 0) goto L_0x0135;
    L_0x0125:
        r7 = 1;
        r14 = new com.android.settings.applications.RunningState$ProcessItem;
        r15 = r8.uid;
        r12 = r8.process;
        r14.<init>(r2, r15, r12);
        r11 = r14;
        r12 = r8.process;
        r4.put(r12, r11);
    L_0x0135:
        r12 = r11.mCurSeq;
        r13 = r1.mSequence;
        if (r12 == r13) goto L_0x016d;
    L_0x013b:
        r12 = r8.restarting;
        r14 = 0;
        r12 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r12 != 0) goto L_0x0146;
    L_0x0143:
        r12 = r8.pid;
        goto L_0x0147;
    L_0x0146:
        r12 = 0;
    L_0x0147:
        r13 = r11.mPid;
        if (r12 == r13) goto L_0x0164;
    L_0x014b:
        r7 = 1;
        r13 = r11.mPid;
        if (r13 == r12) goto L_0x0164;
    L_0x0150:
        r13 = r11.mPid;
        if (r13 == 0) goto L_0x015b;
    L_0x0154:
        r13 = r1.mServiceProcessesByPid;
        r14 = r11.mPid;
        r13.remove(r14);
    L_0x015b:
        if (r12 == 0) goto L_0x0162;
    L_0x015d:
        r13 = r1.mServiceProcessesByPid;
        r13.put(r12, r11);
    L_0x0162:
        r11.mPid = r12;
    L_0x0164:
        r13 = r11.mDependentProcesses;
        r13.clear();
        r13 = r1.mSequence;
        r11.mCurSeq = r13;
    L_0x016d:
        r12 = r11.updateService(r2, r8);
        r4 = r7 | r12;
        r7 = r4;
    L_0x0174:
        r0 = r0 + 1;
        r4 = 1;
        r12 = 0;
        goto L_0x00a8;
    L_0x017b:
        r0 = 0;
    L_0x017c:
        if (r0 >= r10) goto L_0x01e7;
    L_0x017e:
        r4 = r17;
        r8 = r4.get(r0);
        r8 = (android.app.ActivityManager.RunningAppProcessInfo) r8;
        r11 = r1.mServiceProcessesByPid;
        r12 = r8.pid;
        r11 = r11.get(r12);
        r11 = (com.android.settings.applications.RunningState.ProcessItem) r11;
        if (r11 != 0) goto L_0x01ba;
    L_0x0192:
        r12 = r1.mRunningProcesses;
        r13 = r8.pid;
        r12 = r12.get(r13);
        r11 = r12;
        r11 = (com.android.settings.applications.RunningState.ProcessItem) r11;
        if (r11 != 0) goto L_0x01b5;
    L_0x019f:
        r7 = 1;
        r12 = new com.android.settings.applications.RunningState$ProcessItem;
        r13 = r8.uid;
        r14 = r8.processName;
        r12.<init>(r2, r13, r14);
        r11 = r12;
        r12 = r8.pid;
        r11.mPid = r12;
        r12 = r1.mRunningProcesses;
        r13 = r8.pid;
        r12.put(r13, r11);
    L_0x01b5:
        r12 = r11.mDependentProcesses;
        r12.clear();
    L_0x01ba:
        r12 = r1.isInterestingProcess(r8);
        if (r12 == 0) goto L_0x01d9;
    L_0x01c0:
        r12 = r1.mInterestingProcesses;
        r12 = r12.contains(r11);
        if (r12 != 0) goto L_0x01ce;
    L_0x01c8:
        r7 = 1;
        r12 = r1.mInterestingProcesses;
        r12.add(r11);
    L_0x01ce:
        r12 = r1.mSequence;
        r11.mCurSeq = r12;
        r12 = 1;
        r11.mInteresting = r12;
        r11.ensureLabel(r3);
        goto L_0x01dc;
    L_0x01d9:
        r12 = 0;
        r11.mInteresting = r12;
    L_0x01dc:
        r12 = r1.mSequence;
        r11.mRunningSeq = r12;
        r11.mRunningProcessInfo = r8;
        r0 = r0 + 1;
        r17 = r4;
        goto L_0x017c;
    L_0x01e7:
        r4 = r17;
        r0 = r1.mRunningProcesses;
        r0 = r0.size();
        r8 = r7;
        r7 = r0;
        r0 = 0;
    L_0x01f2:
        if (r0 >= r7) goto L_0x023a;
    L_0x01f4:
        r11 = r1.mRunningProcesses;
        r11 = r11.valueAt(r0);
        r11 = (com.android.settings.applications.RunningState.ProcessItem) r11;
        r12 = r11.mRunningSeq;
        r13 = r1.mSequence;
        if (r12 != r13) goto L_0x022b;
    L_0x0202:
        r12 = r11.mRunningProcessInfo;
        r12 = r12.importanceReasonPid;
        if (r12 == 0) goto L_0x0225;
    L_0x0208:
        r13 = r1.mServiceProcessesByPid;
        r13 = r13.get(r12);
        r13 = (com.android.settings.applications.RunningState.ProcessItem) r13;
        if (r13 != 0) goto L_0x021b;
    L_0x0212:
        r14 = r1.mRunningProcesses;
        r14 = r14.get(r12);
        r13 = r14;
        r13 = (com.android.settings.applications.RunningState.ProcessItem) r13;
    L_0x021b:
        if (r13 == 0) goto L_0x0224;
    L_0x021d:
        r14 = r13.mDependentProcesses;
        r15 = r11.mPid;
        r14.put(r15, r11);
    L_0x0224:
        goto L_0x0228;
    L_0x0225:
        r13 = 0;
        r11.mClient = r13;
    L_0x0228:
        r0 = r0 + 1;
        goto L_0x0239;
    L_0x022b:
        r8 = 1;
        r12 = r1.mRunningProcesses;
        r13 = r1.mRunningProcesses;
        r13 = r13.keyAt(r0);
        r12.remove(r13);
        r7 = r7 + -1;
    L_0x0239:
        goto L_0x01f2;
    L_0x023a:
        r0 = r1.mInterestingProcesses;
        r0 = r0.size();
        r11 = r8;
        r8 = r0;
        r0 = 0;
    L_0x0243:
        if (r0 >= r8) goto L_0x0268;
    L_0x0245:
        r12 = r1.mInterestingProcesses;
        r12 = r12.get(r0);
        r12 = (com.android.settings.applications.RunningState.ProcessItem) r12;
        r13 = r12.mInteresting;
        if (r13 == 0) goto L_0x025b;
    L_0x0251:
        r13 = r1.mRunningProcesses;
        r14 = r12.mPid;
        r13 = r13.get(r14);
        if (r13 != 0) goto L_0x0265;
    L_0x025b:
        r11 = 1;
        r13 = r1.mInterestingProcesses;
        r13.remove(r0);
        r0 = r0 + -1;
        r8 = r8 + -1;
    L_0x0265:
        r12 = 1;
        r0 = r0 + r12;
        goto L_0x0243;
    L_0x0268:
        r0 = r1.mServiceProcessesByPid;
        r12 = r0.size();
        r0 = 0;
    L_0x026f:
        if (r0 >= r12) goto L_0x0289;
    L_0x0271:
        r13 = r1.mServiceProcessesByPid;
        r13 = r13.valueAt(r0);
        r13 = (com.android.settings.applications.RunningState.ProcessItem) r13;
        r14 = r13.mCurSeq;
        r15 = r1.mSequence;
        if (r14 != r15) goto L_0x0286;
    L_0x027f:
        r14 = r1.mSequence;
        r14 = r13.buildDependencyChain(r2, r3, r14);
        r11 = r11 | r14;
    L_0x0286:
        r0 = r0 + 1;
        goto L_0x026f;
    L_0x0289:
        r0 = 0;
        r13 = r11;
        r11 = r0;
        r0 = 0;
    L_0x028d:
        r14 = r1.mServiceProcessesByName;
        r14 = r14.size();
        if (r0 >= r14) goto L_0x033a;
    L_0x0295:
        r14 = r1.mServiceProcessesByName;
        r14 = r14.valueAt(r0);
        r14 = (java.util.HashMap) r14;
        r15 = r14.values();
        r15 = r15.iterator();
    L_0x02a5:
        r17 = r15.hasNext();
        if (r17 == 0) goto L_0x032e;
    L_0x02ab:
        r17 = r15.next();
        r22 = r4;
        r4 = r17;
        r4 = (com.android.settings.applications.RunningState.ProcessItem) r4;
        r23 = r5;
        r5 = r4.mCurSeq;
        r6 = r1.mSequence;
        if (r5 != r6) goto L_0x02f8;
    L_0x02bd:
        r4.ensureLabel(r3);
        r5 = r4.mPid;
        if (r5 != 0) goto L_0x02c9;
    L_0x02c4:
        r5 = r4.mDependentProcesses;
        r5.clear();
    L_0x02c9:
        r5 = r4.mServices;
        r5 = r5.values();
        r5 = r5.iterator();
    L_0x02d3:
        r6 = r5.hasNext();
        if (r6 == 0) goto L_0x02f1;
    L_0x02d9:
        r6 = r5.next();
        r6 = (com.android.settings.applications.RunningState.ServiceItem) r6;
        r24 = r3;
        r3 = r6.mCurSeq;
        r25 = r6;
        r6 = r1.mSequence;
        if (r3 == r6) goto L_0x02ee;
    L_0x02e9:
        r3 = 1;
        r5.remove();
        r13 = r3;
    L_0x02ee:
        r3 = r24;
        goto L_0x02d3;
    L_0x02f1:
        r24 = r3;
        r4 = r22;
        r5 = r23;
        goto L_0x032a;
    L_0x02f8:
        r24 = r3;
        r13 = 1;
        r15.remove();
        r3 = r14.size();
        if (r3 != 0) goto L_0x0319;
    L_0x0304:
        if (r11 != 0) goto L_0x030c;
    L_0x0306:
        r3 = new java.util.ArrayList;
        r3.<init>();
        r11 = r3;
    L_0x030c:
        r3 = r1.mServiceProcessesByName;
        r3 = r3.keyAt(r0);
        r3 = java.lang.Integer.valueOf(r3);
        r11.add(r3);
    L_0x0319:
        r3 = r4.mPid;
        if (r3 == 0) goto L_0x0324;
    L_0x031d:
        r3 = r1.mServiceProcessesByPid;
        r5 = r4.mPid;
        r3.remove(r5);
    L_0x0324:
        r4 = r22;
        r5 = r23;
        r3 = r24;
    L_0x032a:
        r6 = r46;
        goto L_0x02a5;
    L_0x032e:
        r24 = r3;
        r22 = r4;
        r23 = r5;
        r0 = r0 + 1;
        r6 = r46;
        goto L_0x028d;
    L_0x033a:
        r24 = r3;
        r22 = r4;
        r23 = r5;
        if (r11 == 0) goto L_0x035b;
    L_0x0342:
        r0 = 0;
    L_0x0343:
        r3 = r11.size();
        if (r0 >= r3) goto L_0x035b;
    L_0x0349:
        r3 = r11.get(r0);
        r3 = (java.lang.Integer) r3;
        r3 = r3.intValue();
        r4 = r1.mServiceProcessesByName;
        r4.remove(r3);
        r0 = r0 + 1;
        goto L_0x0343;
    L_0x035b:
        if (r13 == 0) goto L_0x0586;
    L_0x035d:
        r0 = new java.util.ArrayList;
        r0.<init>();
        r3 = r0;
        r0 = 0;
    L_0x0364:
        r4 = r1.mServiceProcessesByName;
        r4 = r4.size();
        if (r0 >= r4) goto L_0x0410;
    L_0x036c:
        r4 = r1.mServiceProcessesByName;
        r4 = r4.valueAt(r0);
        r4 = (java.util.HashMap) r4;
        r4 = r4.values();
        r4 = r4.iterator();
    L_0x037c:
        r5 = r4.hasNext();
        if (r5 == 0) goto L_0x0406;
    L_0x0382:
        r5 = r4.next();
        r5 = (com.android.settings.applications.RunningState.ProcessItem) r5;
        r6 = 0;
        r5.mIsSystem = r6;
        r6 = 1;
        r5.mIsStarted = r6;
        r14 = 9223372036854775807; // 0x7fffffffffffffff float:NaN double:NaN;
        r5.mActiveSince = r14;
        r6 = r5.mServices;
        r6 = r6.values();
        r6 = r6.iterator();
    L_0x039f:
        r14 = r6.hasNext();
        if (r14 == 0) goto L_0x03f9;
    L_0x03a5:
        r14 = r6.next();
        r14 = (com.android.settings.applications.RunningState.ServiceItem) r14;
        r15 = r14.mServiceInfo;
        if (r15 == 0) goto L_0x03be;
    L_0x03af:
        r15 = r14.mServiceInfo;
        r15 = r15.applicationInfo;
        r15 = r15.flags;
        r26 = r4;
        r4 = 1;
        r15 = r15 & r4;
        if (r15 == 0) goto L_0x03c0;
    L_0x03bb:
        r5.mIsSystem = r4;
        goto L_0x03c0;
    L_0x03be:
        r26 = r4;
    L_0x03c0:
        r4 = r14.mRunningService;
        if (r4 == 0) goto L_0x03e6;
    L_0x03c4:
        r4 = r14.mRunningService;
        r4 = r4.clientLabel;
        if (r4 == 0) goto L_0x03e6;
    L_0x03ca:
        r4 = 0;
        r5.mIsStarted = r4;
        r28 = r6;
        r27 = r7;
        r6 = r5.mActiveSince;
        r4 = r14.mRunningService;
        r30 = r8;
        r29 = r9;
        r8 = r4.activeSince;
        r4 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r4 <= 0) goto L_0x03ee;
    L_0x03df:
        r4 = r14.mRunningService;
        r6 = r4.activeSince;
        r5.mActiveSince = r6;
        goto L_0x03ee;
    L_0x03e6:
        r28 = r6;
        r27 = r7;
        r30 = r8;
        r29 = r9;
    L_0x03ee:
        r4 = r26;
        r7 = r27;
        r6 = r28;
        r9 = r29;
        r8 = r30;
        goto L_0x039f;
    L_0x03f9:
        r26 = r4;
        r27 = r7;
        r30 = r8;
        r29 = r9;
        r3.add(r5);
        goto L_0x037c;
    L_0x0406:
        r27 = r7;
        r30 = r8;
        r29 = r9;
        r0 = r0 + 1;
        goto L_0x0364;
    L_0x0410:
        r27 = r7;
        r30 = r8;
        r29 = r9;
        r0 = r1.mServiceProcessComparator;
        java.util.Collections.sort(r3, r0);
        r0 = new java.util.ArrayList;
        r0.<init>();
        r4 = r0;
        r0 = new java.util.ArrayList;
        r0.<init>();
        r5 = r0;
        r0 = r1.mProcessItems;
        r0.clear();
        r0 = 0;
    L_0x042d:
        r6 = r3.size();
        if (r0 >= r6) goto L_0x0506;
    L_0x0433:
        r6 = r3.get(r0);
        r6 = (com.android.settings.applications.RunningState.ProcessItem) r6;
        r7 = 0;
        r6.mNeedDivider = r7;
        r7 = r1.mProcessItems;
        r7 = r7.size();
        r8 = r1.mProcessItems;
        r6.addDependentProcesses(r4, r8);
        r4.add(r6);
        r8 = r6.mPid;
        if (r8 <= 0) goto L_0x0453;
    L_0x044e:
        r8 = r1.mProcessItems;
        r8.add(r6);
    L_0x0453:
        r8 = 0;
        r9 = 0;
        r14 = 0;
        r15 = r6.mServices;
        r15 = r15.values();
        r15 = r15.iterator();
    L_0x0460:
        r17 = r15.hasNext();
        if (r17 == 0) goto L_0x048e;
    L_0x0466:
        r17 = r15.next();
        r31 = r3;
        r3 = r17;
        r3 = (com.android.settings.applications.RunningState.ServiceItem) r3;
        r3.mNeedDivider = r14;
        r14 = 1;
        r4.add(r3);
        r32 = r7;
        r7 = r3.mMergedItem;
        if (r7 == 0) goto L_0x0487;
    L_0x047c:
        if (r8 == 0) goto L_0x0483;
    L_0x047e:
        r7 = r3.mMergedItem;
        if (r8 == r7) goto L_0x0483;
    L_0x0482:
        r9 = 0;
    L_0x0483:
        r7 = r3.mMergedItem;
        r8 = r7;
        goto L_0x0489;
    L_0x0487:
        r3 = 0;
        r9 = r3;
    L_0x0489:
        r3 = r31;
        r7 = r32;
        goto L_0x0460;
    L_0x048e:
        r31 = r3;
        r32 = r7;
        if (r9 == 0) goto L_0x04a4;
    L_0x0494:
        if (r8 == 0) goto L_0x04a4;
    L_0x0496:
        r3 = r8.mServices;
        r3 = r3.size();
        r7 = r6.mServices;
        r7 = r7.size();
        if (r3 == r7) goto L_0x04ed;
    L_0x04a4:
        r3 = new com.android.settings.applications.RunningState$MergedItem;
        r7 = r6.mUserId;
        r3.<init>(r7);
        r8 = r3;
        r3 = r6.mServices;
        r3 = r3.values();
        r3 = r3.iterator();
    L_0x04b6:
        r7 = r3.hasNext();
        if (r7 == 0) goto L_0x04ca;
    L_0x04bc:
        r7 = r3.next();
        r7 = (com.android.settings.applications.RunningState.ServiceItem) r7;
        r15 = r8.mServices;
        r15.add(r7);
        r7.mMergedItem = r8;
        goto L_0x04b6;
    L_0x04ca:
        r8.mProcess = r6;
        r3 = r8.mOtherProcesses;
        r3.clear();
        r3 = r32;
    L_0x04d3:
        r7 = r1.mProcessItems;
        r7 = r7.size();
        r15 = 1;
        r7 = r7 - r15;
        if (r3 >= r7) goto L_0x04ed;
    L_0x04dd:
        r7 = r8.mOtherProcesses;
        r15 = r1.mProcessItems;
        r15 = r15.get(r3);
        r15 = (com.android.settings.applications.RunningState.ProcessItem) r15;
        r7.add(r15);
        r3 = r3 + 1;
        goto L_0x04d3;
    L_0x04ed:
        r3 = 0;
        r8.update(r2, r3);
        r3 = r8.mUserId;
        r7 = r1.mMyUserId;
        if (r3 == r7) goto L_0x04fd;
    L_0x04f7:
        r3 = r1.mOtherUserMergedItems;
        r1.addOtherUserItem(r2, r5, r3, r8);
        goto L_0x0500;
    L_0x04fd:
        r5.add(r8);
    L_0x0500:
        r0 = r0 + 1;
        r3 = r31;
        goto L_0x042d;
    L_0x0506:
        r31 = r3;
        r0 = r1.mInterestingProcesses;
        r8 = r0.size();
        r0 = 0;
    L_0x050f:
        if (r0 >= r8) goto L_0x055a;
    L_0x0511:
        r3 = r1.mInterestingProcesses;
        r3 = r3.get(r0);
        r3 = (com.android.settings.applications.RunningState.ProcessItem) r3;
        r6 = r3.mClient;
        if (r6 != 0) goto L_0x0557;
    L_0x051d:
        r6 = r3.mServices;
        r6 = r6.size();
        if (r6 > 0) goto L_0x0557;
    L_0x0525:
        r6 = r3.mMergedItem;
        if (r6 != 0) goto L_0x0536;
    L_0x0529:
        r6 = new com.android.settings.applications.RunningState$MergedItem;
        r7 = r3.mUserId;
        r6.<init>(r7);
        r3.mMergedItem = r6;
        r6 = r3.mMergedItem;
        r6.mProcess = r3;
    L_0x0536:
        r6 = r3.mMergedItem;
        r7 = 0;
        r6.update(r2, r7);
        r6 = r3.mMergedItem;
        r6 = r6.mUserId;
        r7 = r1.mMyUserId;
        if (r6 == r7) goto L_0x054c;
    L_0x0544:
        r6 = r1.mOtherUserMergedItems;
        r7 = r3.mMergedItem;
        r1.addOtherUserItem(r2, r5, r6, r7);
        goto L_0x0552;
    L_0x054c:
        r6 = r3.mMergedItem;
        r7 = 0;
        r5.add(r7, r6);
    L_0x0552:
        r6 = r1.mProcessItems;
        r6.add(r3);
    L_0x0557:
        r0 = r0 + 1;
        goto L_0x050f;
    L_0x055a:
        r0 = r1.mOtherUserMergedItems;
        r3 = r0.size();
        r0 = 0;
    L_0x0561:
        if (r0 >= r3) goto L_0x0578;
    L_0x0563:
        r6 = r1.mOtherUserMergedItems;
        r6 = r6.valueAt(r0);
        r6 = (com.android.settings.applications.RunningState.MergedItem) r6;
        r7 = r6.mCurSeq;
        r9 = r1.mSequence;
        if (r7 != r9) goto L_0x0575;
    L_0x0571:
        r7 = 0;
        r6.update(r2, r7);
    L_0x0575:
        r0 = r0 + 1;
        goto L_0x0561;
    L_0x0578:
        r6 = r1.mLock;
        monitor-enter(r6);
        r1.mItems = r4;	 Catch:{ all -> 0x0583 }
        r1.mMergedItems = r5;	 Catch:{ all -> 0x0583 }
        monitor-exit(r6);	 Catch:{ all -> 0x0583 }
        r30 = r8;
        goto L_0x058c;
    L_0x0583:
        r0 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0583 }
        throw r0;
    L_0x0586:
        r27 = r7;
        r30 = r8;
        r29 = r9;
    L_0x058c:
        r0 = r1.mAllProcessItems;
        r0.clear();
        r0 = r1.mAllProcessItems;
        r3 = r1.mProcessItems;
        r0.addAll(r3);
        r0 = 0;
        r3 = 0;
        r4 = 0;
        r5 = r1.mRunningProcesses;
        r5 = r5.size();
        r6 = r3;
        r3 = r0;
        r0 = 0;
    L_0x05a4:
        r7 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r8 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        if (r0 >= r5) goto L_0x05fc;
    L_0x05aa:
        r9 = r1.mRunningProcesses;
        r9 = r9.valueAt(r0);
        r9 = (com.android.settings.applications.RunningState.ProcessItem) r9;
        r14 = r9.mCurSeq;
        r15 = r1.mSequence;
        if (r14 == r15) goto L_0x05f7;
    L_0x05b8:
        r14 = r9.mRunningProcessInfo;
        r14 = r14.importance;
        if (r14 < r8) goto L_0x05c6;
    L_0x05be:
        r3 = r3 + 1;
        r7 = r1.mAllProcessItems;
        r7.add(r9);
        goto L_0x05f9;
    L_0x05c6:
        r8 = r9.mRunningProcessInfo;
        r8 = r8.importance;
        if (r8 > r7) goto L_0x05d4;
    L_0x05cc:
        r6 = r6 + 1;
        r7 = r1.mAllProcessItems;
        r7.add(r9);
        goto L_0x05f9;
    L_0x05d4:
        r7 = "RunningState";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r14 = "Unknown non-service process: ";
        r8.append(r14);
        r14 = r9.mProcessName;
        r8.append(r14);
        r14 = " #";
        r8.append(r14);
        r14 = r9.mPid;
        r8.append(r14);
        r8 = r8.toString();
        android.util.Log.i(r7, r8);
        goto L_0x05f9;
    L_0x05f7:
        r4 = r4 + 1;
    L_0x05f9:
        r0 = r0 + 1;
        goto L_0x05a4;
    L_0x05fc:
        r14 = 0;
        r18 = 0;
        r20 = 0;
        r9 = 0;
        r17 = 0;
        r16 = 0;
        r25 = r16;
        r0 = r1.mAllProcessItems;	 Catch:{ RemoteException -> 0x0742 }
        r0 = r0.size();	 Catch:{ RemoteException -> 0x0742 }
        r7 = new int[r0];	 Catch:{ RemoteException -> 0x0742 }
        r26 = r16;
    L_0x0613:
        r33 = r26;
        r8 = r33;
        if (r8 >= r0) goto L_0x063a;
    L_0x0619:
        r34 = r0;
        r0 = r1.mAllProcessItems;	 Catch:{ RemoteException -> 0x062e }
        r0 = r0.get(r8);	 Catch:{ RemoteException -> 0x062e }
        r0 = (com.android.settings.applications.RunningState.ProcessItem) r0;	 Catch:{ RemoteException -> 0x062e }
        r0 = r0.mPid;	 Catch:{ RemoteException -> 0x062e }
        r7[r8] = r0;	 Catch:{ RemoteException -> 0x062e }
        r26 = r8 + 1;
        r0 = r34;
        r8 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        goto L_0x0613;
    L_0x062e:
        r0 = move-exception;
        r35 = r5;
        r36 = r10;
        r37 = r11;
        r26 = r13;
        r13 = r9;
        goto L_0x074c;
    L_0x063a:
        r34 = r0;
        r0 = android.app.ActivityManager.getService();	 Catch:{ RemoteException -> 0x0742 }
        r0 = r0.getProcessPss(r7);	 Catch:{ RemoteException -> 0x0742 }
        r8 = 0;
        r26 = r13;
        r13 = r9;
        r9 = r8;
        r8 = r16;
    L_0x064b:
        r35 = r5;
        r5 = r7.length;	 Catch:{ RemoteException -> 0x073c }
        if (r8 >= r5) goto L_0x0737;
    L_0x0650:
        r5 = r1.mAllProcessItems;	 Catch:{ RemoteException -> 0x073c }
        r5 = r5.get(r8);	 Catch:{ RemoteException -> 0x073c }
        r5 = (com.android.settings.applications.RunningState.ProcessItem) r5;	 Catch:{ RemoteException -> 0x073c }
        r36 = r10;
        r37 = r11;
        r10 = r0[r8];	 Catch:{ RemoteException -> 0x0735 }
        r38 = r0;
        r0 = r1.mSequence;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r5.updateSize(r2, r10, r0);	 Catch:{ RemoteException -> 0x0735 }
        r26 = r26 | r0;
        r0 = r5.mCurSeq;	 Catch:{ RemoteException -> 0x0735 }
        r10 = r1.mSequence;	 Catch:{ RemoteException -> 0x0735 }
        if (r0 != r10) goto L_0x0678;
    L_0x066e:
        r10 = r5.mSize;	 Catch:{ RemoteException -> 0x0735 }
        r20 = r20 + r10;
        r39 = r7;
    L_0x0674:
        r7 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        goto L_0x0727;
    L_0x0678:
        r0 = r5.mRunningProcessInfo;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.importance;	 Catch:{ RemoteException -> 0x0735 }
        r10 = 400; // 0x190 float:5.6E-43 double:1.976E-321;
        if (r0 < r10) goto L_0x0719;
    L_0x0680:
        r10 = r5.mSize;	 Catch:{ RemoteException -> 0x0735 }
        r14 = r14 + r10;
        if (r13 == 0) goto L_0x06a5;
    L_0x0685:
        r0 = new com.android.settings.applications.RunningState$MergedItem;	 Catch:{ RemoteException -> 0x0735 }
        r10 = r5.mUserId;	 Catch:{ RemoteException -> 0x0735 }
        r0.<init>(r10);	 Catch:{ RemoteException -> 0x0735 }
        r5.mMergedItem = r0;	 Catch:{ RemoteException -> 0x0735 }
        r10 = r5.mMergedItem;	 Catch:{ RemoteException -> 0x0735 }
        r10.mProcess = r5;	 Catch:{ RemoteException -> 0x0735 }
        r10 = r0.mUserId;	 Catch:{ RemoteException -> 0x0735 }
        r11 = r1.mMyUserId;	 Catch:{ RemoteException -> 0x0735 }
        if (r10 == r11) goto L_0x069a;
    L_0x0698:
        r10 = 1;
        goto L_0x069c;
    L_0x069a:
        r10 = r16;
    L_0x069c:
        r25 = r25 | r10;
        r13.add(r0);	 Catch:{ RemoteException -> 0x0735 }
    L_0x06a1:
        r39 = r7;
        goto L_0x070e;
    L_0x06a5:
        r0 = r1.mBackgroundItems;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.size();	 Catch:{ RemoteException -> 0x0735 }
        if (r9 >= r0) goto L_0x06c3;
    L_0x06ad:
        r0 = r1.mBackgroundItems;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.get(r9);	 Catch:{ RemoteException -> 0x0735 }
        r0 = (com.android.settings.applications.RunningState.MergedItem) r0;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.mProcess;	 Catch:{ RemoteException -> 0x0735 }
        if (r0 == r5) goto L_0x06ba;
    L_0x06b9:
        goto L_0x06c3;
    L_0x06ba:
        r0 = r1.mBackgroundItems;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.get(r9);	 Catch:{ RemoteException -> 0x0735 }
        r0 = (com.android.settings.applications.RunningState.MergedItem) r0;	 Catch:{ RemoteException -> 0x0735 }
        goto L_0x06a1;
    L_0x06c3:
        r0 = new java.util.ArrayList;	 Catch:{ RemoteException -> 0x0735 }
        r0.<init>(r3);	 Catch:{ RemoteException -> 0x0735 }
        r10 = r0;
        r0 = r16;
    L_0x06cb:
        if (r0 >= r9) goto L_0x06ef;
    L_0x06cd:
        r11 = r1.mBackgroundItems;	 Catch:{ RemoteException -> 0x06eb }
        r11 = r11.get(r0);	 Catch:{ RemoteException -> 0x06eb }
        r11 = (com.android.settings.applications.RunningState.MergedItem) r11;	 Catch:{ RemoteException -> 0x06eb }
        r13 = r11.mUserId;	 Catch:{ RemoteException -> 0x06eb }
        r39 = r7;
        r7 = r1.mMyUserId;	 Catch:{ RemoteException -> 0x06eb }
        if (r13 == r7) goto L_0x06df;
    L_0x06dd:
        r7 = 1;
        goto L_0x06e1;
    L_0x06df:
        r7 = r16;
    L_0x06e1:
        r25 = r25 | r7;
        r10.add(r11);	 Catch:{ RemoteException -> 0x06eb }
        r0 = r0 + 1;
        r7 = r39;
        goto L_0x06cb;
    L_0x06eb:
        r0 = move-exception;
        r13 = r10;
        goto L_0x074c;
    L_0x06ef:
        r39 = r7;
        r0 = new com.android.settings.applications.RunningState$MergedItem;	 Catch:{ RemoteException -> 0x06eb }
        r7 = r5.mUserId;	 Catch:{ RemoteException -> 0x06eb }
        r0.<init>(r7);	 Catch:{ RemoteException -> 0x06eb }
        r5.mMergedItem = r0;	 Catch:{ RemoteException -> 0x06eb }
        r7 = r5.mMergedItem;	 Catch:{ RemoteException -> 0x06eb }
        r7.mProcess = r5;	 Catch:{ RemoteException -> 0x06eb }
        r7 = r0.mUserId;	 Catch:{ RemoteException -> 0x06eb }
        r11 = r1.mMyUserId;	 Catch:{ RemoteException -> 0x06eb }
        if (r7 == r11) goto L_0x0706;
    L_0x0704:
        r7 = 1;
        goto L_0x0708;
    L_0x0706:
        r7 = r16;
    L_0x0708:
        r25 = r25 | r7;
        r10.add(r0);	 Catch:{ RemoteException -> 0x06eb }
        r13 = r10;
    L_0x070e:
        r7 = 1;
        r0.update(r2, r7);	 Catch:{ RemoteException -> 0x0735 }
        r0.updateSize(r2);	 Catch:{ RemoteException -> 0x0735 }
        r9 = r9 + 1;
        goto L_0x0674;
    L_0x0719:
        r39 = r7;
        r0 = r5.mRunningProcessInfo;	 Catch:{ RemoteException -> 0x0735 }
        r0 = r0.importance;	 Catch:{ RemoteException -> 0x0735 }
        r7 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        if (r0 > r7) goto L_0x0727;
    L_0x0723:
        r10 = r5.mSize;	 Catch:{ RemoteException -> 0x0735 }
        r18 = r18 + r10;
    L_0x0727:
        r8 = r8 + 1;
        r5 = r35;
        r10 = r36;
        r11 = r37;
        r0 = r38;
        r7 = r39;
        goto L_0x064b;
    L_0x0735:
        r0 = move-exception;
        goto L_0x074c;
    L_0x0737:
        r36 = r10;
        r37 = r11;
        goto L_0x074c;
    L_0x073c:
        r0 = move-exception;
        r36 = r10;
        r37 = r11;
        goto L_0x074c;
    L_0x0742:
        r0 = move-exception;
        r35 = r5;
        r36 = r10;
        r37 = r11;
        r26 = r13;
        r13 = r9;
    L_0x074c:
        r7 = r18;
        r9 = r20;
        if (r13 != 0) goto L_0x0782;
    L_0x0752:
        r0 = r1.mBackgroundItems;
        r0 = r0.size();
        if (r0 <= r3) goto L_0x0782;
    L_0x075a:
        r0 = new java.util.ArrayList;
        r0.<init>(r3);
        r13 = r0;
        r0 = r16;
    L_0x0762:
        if (r0 >= r3) goto L_0x0782;
    L_0x0764:
        r5 = r1.mBackgroundItems;
        r5 = r5.get(r0);
        r5 = (com.android.settings.applications.RunningState.MergedItem) r5;
        r11 = r5.mUserId;
        r40 = r12;
        r12 = r1.mMyUserId;
        if (r11 == r12) goto L_0x0776;
    L_0x0774:
        r11 = 1;
        goto L_0x0778;
    L_0x0776:
        r11 = r16;
    L_0x0778:
        r25 = r25 | r11;
        r13.add(r5);
        r0 = r0 + 1;
        r12 = r40;
        goto L_0x0762;
    L_0x0782:
        r40 = r12;
        if (r13 == 0) goto L_0x07eb;
    L_0x0786:
        if (r25 != 0) goto L_0x078f;
    L_0x0788:
        r17 = r13;
        r42 = r13;
        r5 = r17;
        goto L_0x07ef;
    L_0x078f:
        r0 = new java.util.ArrayList;
        r0.<init>();
        r5 = r13.size();
        r11 = r16;
    L_0x079a:
        if (r11 >= r5) goto L_0x07bc;
    L_0x079c:
        r12 = r13.get(r11);
        r12 = (com.android.settings.applications.RunningState.MergedItem) r12;
        r41 = r5;
        r5 = r12.mUserId;
        r42 = r13;
        r13 = r1.mMyUserId;
        if (r5 == r13) goto L_0x07b2;
    L_0x07ac:
        r5 = r1.mOtherUserBackgroundItems;
        r1.addOtherUserItem(r2, r0, r5, r12);
        goto L_0x07b5;
    L_0x07b2:
        r0.add(r12);
    L_0x07b5:
        r11 = r11 + 1;
        r5 = r41;
        r13 = r42;
        goto L_0x079a;
    L_0x07bc:
        r41 = r5;
        r42 = r13;
        r5 = r1.mOtherUserBackgroundItems;
        r5 = r5.size();
        r11 = r16;
    L_0x07c8:
        if (r11 >= r5) goto L_0x07e6;
    L_0x07ca:
        r12 = r1.mOtherUserBackgroundItems;
        r12 = r12.valueAt(r11);
        r12 = (com.android.settings.applications.RunningState.MergedItem) r12;
        r13 = r12.mCurSeq;
        r43 = r0;
        r0 = r1.mSequence;
        if (r13 != r0) goto L_0x07e1;
    L_0x07da:
        r13 = 1;
        r12.update(r2, r13);
        r12.updateSize(r2);
    L_0x07e1:
        r11 = r11 + 1;
        r0 = r43;
        goto L_0x07c8;
    L_0x07e6:
        r43 = r0;
        r5 = r43;
        goto L_0x07ef;
    L_0x07eb:
        r42 = r13;
        r5 = r17;
    L_0x07ef:
        r0 = r16;
        r11 = r1.mMergedItems;
        r11 = r11.size();
        if (r0 >= r11) goto L_0x0807;
    L_0x07f9:
        r11 = r1.mMergedItems;
        r11 = r11.get(r0);
        r11 = (com.android.settings.applications.RunningState.MergedItem) r11;
        r11.updateSize(r2);
        r16 = r0 + 1;
        goto L_0x07ef;
    L_0x0807:
        r11 = r1.mLock;
        monitor-enter(r11);
        r1.mNumBackgroundProcesses = r3;	 Catch:{ all -> 0x0835 }
        r1.mNumForegroundProcesses = r6;	 Catch:{ all -> 0x0835 }
        r1.mNumServiceProcesses = r4;	 Catch:{ all -> 0x0835 }
        r1.mBackgroundProcessMemory = r14;	 Catch:{ all -> 0x0835 }
        r1.mForegroundProcessMemory = r7;	 Catch:{ all -> 0x0835 }
        r1.mServiceProcessMemory = r9;	 Catch:{ all -> 0x0835 }
        if (r42 == 0) goto L_0x0825;
    L_0x0818:
        r13 = r42;
        r1.mBackgroundItems = r13;	 Catch:{ all -> 0x083a }
        r1.mUserBackgroundItems = r5;	 Catch:{ all -> 0x083a }
        r0 = r1.mWatchingBackgroundItems;	 Catch:{ all -> 0x083a }
        if (r0 == 0) goto L_0x0827;
    L_0x0822:
        r26 = 1;
        goto L_0x0827;
    L_0x0825:
        r13 = r42;
    L_0x0827:
        r0 = r1.mHaveData;	 Catch:{ all -> 0x083a }
        if (r0 != 0) goto L_0x0833;
    L_0x082b:
        r12 = 1;
        r1.mHaveData = r12;	 Catch:{ all -> 0x083a }
        r0 = r1.mLock;	 Catch:{ all -> 0x083a }
        r0.notifyAll();	 Catch:{ all -> 0x083a }
    L_0x0833:
        monitor-exit(r11);	 Catch:{ all -> 0x083a }
        return r26;
    L_0x0835:
        r0 = move-exception;
        r13 = r42;
    L_0x0838:
        monitor-exit(r11);	 Catch:{ all -> 0x083a }
        throw r0;
    L_0x083a:
        r0 = move-exception;
        goto L_0x0838;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.RunningState.update(android.content.Context, android.app.ActivityManager):boolean");
    }

    /* Access modifiers changed, original: 0000 */
    public void setWatchingBackgroundItems(boolean watching) {
        synchronized (this.mLock) {
            this.mWatchingBackgroundItems = watching;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ArrayList<MergedItem> getCurrentMergedItems() {
        ArrayList arrayList;
        synchronized (this.mLock) {
            arrayList = this.mMergedItems;
        }
        return arrayList;
    }

    /* Access modifiers changed, original: 0000 */
    public ArrayList<MergedItem> getCurrentBackgroundItems() {
        ArrayList arrayList;
        synchronized (this.mLock) {
            arrayList = this.mUserBackgroundItems;
        }
        return arrayList;
    }
}
