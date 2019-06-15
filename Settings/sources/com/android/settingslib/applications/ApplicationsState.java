package com.android.settingslib.applications;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Application;
import android.app.usage.StorageStatsManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageStatsObserver.Stub;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.Utils;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.Collator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class ApplicationsState {
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppEntry object1, AppEntry object2) {
            int compareResult = this.sCollator.compare(object1.label, object2.label);
            if (compareResult != 0) {
                return compareResult;
            }
            if (!(object1.info == null || object2.info == null)) {
                compareResult = this.sCollator.compare(object1.info.packageName, object2.info.packageName);
                if (compareResult != 0) {
                    return compareResult;
                }
            }
            return object1.info.uid - object2.info.uid;
        }
    };
    static final boolean DEBUG = false;
    static final boolean DEBUG_LOCKING = false;
    public static final int DEFAULT_SESSION_FLAGS = 15;
    public static final Comparator<AppEntry> EXTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.externalSize < object2.externalSize) {
                return 1;
            }
            if (object1.externalSize > object2.externalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    public static final AppFilter FILTER_ALL_ENABLED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return entry.info.enabled && !AppUtils.isInstant(entry.info);
        }
    };
    public static final AppFilter FILTER_AUDIO = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            boolean isMusicApp;
            synchronized (entry) {
                boolean z = true;
                if (entry.info.category != 1) {
                    z = false;
                }
                isMusicApp = z;
            }
            return isMusicApp;
        }
    };
    public static final AppFilter FILTER_DISABLED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return (entry.info.enabled || AppUtils.isInstant(entry.info)) ? false : true;
        }
    };
    public static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            if (AppUtils.isInstant(entry.info)) {
                return false;
            }
            if (ApplicationsState.hasFlag(entry.info.flags, 128) || !ApplicationsState.hasFlag(entry.info.flags, 1) || entry.hasLauncherEntry) {
                return true;
            }
            if (ApplicationsState.hasFlag(entry.info.flags, 1) && entry.isHomeApp) {
                return true;
            }
            return false;
        }
    };
    public static final AppFilter FILTER_DOWNLOADED_AND_LAUNCHER_AND_INSTANT = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return AppUtils.isInstant(entry.info) || ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER.filterApp(entry);
        }
    };
    public static final AppFilter FILTER_EVERYTHING = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return true;
        }
    };
    public static final AppFilter FILTER_GAMES = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            boolean isGame;
            synchronized (info.info) {
                if (!ApplicationsState.hasFlag(info.info.flags, 33554432)) {
                    if (info.info.category != 0) {
                        isGame = false;
                    }
                }
                isGame = true;
            }
            return isGame;
        }
    };
    public static final AppFilter FILTER_INSTANT = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return AppUtils.isInstant(entry.info);
        }
    };
    public static final AppFilter FILTER_MOVIES = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            boolean isMovieApp;
            synchronized (entry) {
                isMovieApp = entry.info.category == 2;
            }
            return isMovieApp;
        }
    };
    public static final AppFilter FILTER_NOT_HIDE = new AppFilter() {
        private String[] mHidePackageNames;

        public void init(Context context) {
            this.mHidePackageNames = context.getResources().getStringArray(17236012);
        }

        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            if (!ArrayUtils.contains(this.mHidePackageNames, entry.info.packageName) || (entry.info.enabled && entry.info.enabledSetting != 4)) {
                return true;
            }
            return false;
        }
    };
    public static final AppFilter FILTER_OTHER_APPS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            boolean isCategorized;
            synchronized (entry) {
                if (!(ApplicationsState.FILTER_AUDIO.filterApp(entry) || ApplicationsState.FILTER_GAMES.filterApp(entry) || ApplicationsState.FILTER_MOVIES.filterApp(entry))) {
                    if (!ApplicationsState.FILTER_PHOTOS.filterApp(entry)) {
                        isCategorized = false;
                    }
                }
                isCategorized = true;
            }
            if (isCategorized) {
                return false;
            }
            return true;
        }
    };
    public static final AppFilter FILTER_PERSONAL = new AppFilter() {
        private int mCurrentUser;

        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        public boolean filterApp(AppEntry entry) {
            return UserHandle.getUserId(entry.info.uid) == this.mCurrentUser;
        }
    };
    public static final AppFilter FILTER_PHOTOS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            boolean isPhotosApp;
            synchronized (entry) {
                isPhotosApp = entry.info.category == 3;
            }
            return isPhotosApp;
        }
    };
    public static final AppFilter FILTER_THIRD_PARTY = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            if (!ApplicationsState.hasFlag(entry.info.flags, 128) && ApplicationsState.hasFlag(entry.info.flags, 1)) {
                return false;
            }
            return true;
        }
    };
    public static final AppFilter FILTER_WITHOUT_DISABLED_UNTIL_USED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return entry.info.enabledSetting != 4;
        }
    };
    public static final AppFilter FILTER_WITH_DOMAIN_URLS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry entry) {
            return !AppUtils.isInstant(entry.info) && ApplicationsState.hasFlag(entry.info.privateFlags, 16);
        }
    };
    public static final AppFilter FILTER_WORK = new AppFilter() {
        private int mCurrentUser;

        public void init() {
            this.mCurrentUser = ActivityManager.getCurrentUser();
        }

        public boolean filterApp(AppEntry entry) {
            return UserHandle.getUserId(entry.info.uid) != this.mCurrentUser;
        }
    };
    public static final int FLAG_SESSION_REQUEST_HOME_APP = 1;
    public static final int FLAG_SESSION_REQUEST_ICONS = 2;
    public static final int FLAG_SESSION_REQUEST_LAUNCHER = 8;
    public static final int FLAG_SESSION_REQUEST_LEANBACK_LAUNCHER = 16;
    public static final int FLAG_SESSION_REQUEST_SIZES = 4;
    public static final Comparator<AppEntry> INTERNAL_SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.internalSize < object2.internalSize) {
                return 1;
            }
            if (object1.internalSize > object2.internalSize) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    public static final int MULTI_APP_USER_ID = 999;
    static final Pattern REMOVE_DIACRITICALS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    public static final Comparator<AppEntry> SIZE_COMPARATOR = new Comparator<AppEntry>() {
        public int compare(AppEntry object1, AppEntry object2) {
            if (object1.size < object2.size) {
                return 1;
            }
            if (object1.size > object2.size) {
                return -1;
            }
            return ApplicationsState.ALPHA_COMPARATOR.compare(object1, object2);
        }
    };
    public static final int SIZE_INVALID = -2;
    public static final int SIZE_UNKNOWN = -1;
    static final String TAG = "ApplicationsState";
    static ApplicationsState sInstance;
    static final Object sLock = new Object();
    final ArrayList<Session> mActiveSessions = new ArrayList();
    final int mAdminRetrieveFlags;
    final ArrayList<AppEntry> mAppEntries = new ArrayList();
    List<ApplicationInfo> mApplications = new ArrayList();
    final BackgroundHandler mBackgroundHandler;
    final Context mContext;
    String mCurComputingSizePkg;
    int mCurComputingSizeUserId;
    UUID mCurComputingSizeUuid;
    long mCurId = 1;
    final IconDrawableFactory mDrawableFactory;
    final SparseArray<HashMap<String, AppEntry>> mEntriesMap = new SparseArray();
    boolean mHaveDisabledApps;
    boolean mHaveInstantApps;
    final InterestingConfigChanges mInterestingConfigChanges = new InterestingConfigChanges();
    final IPackageManager mIpm;
    final MainHandler mMainHandler = new MainHandler(Looper.getMainLooper());
    PackageIntentReceiver mPackageIntentReceiver;
    final PackageManager mPm;
    final ArrayList<Session> mRebuildingSessions = new ArrayList();
    boolean mResumed;
    final int mRetrieveFlags;
    final ArrayList<Session> mSessions = new ArrayList();
    boolean mSessionsChanged;
    final StorageStatsManager mStats;
    final HandlerThread mThread;
    final UserManager mUm;

    public interface AppFilter {
        boolean filterApp(AppEntry appEntry);

        void init();

        void init(Context context) {
            init();
        }
    }

    private class BackgroundHandler extends Handler {
        static final int MSG_LOAD_ENTRIES = 2;
        static final int MSG_LOAD_HOME_APP = 3;
        static final int MSG_LOAD_ICONS = 6;
        static final int MSG_LOAD_LAUNCHER = 4;
        static final int MSG_LOAD_LEANBACK_LAUNCHER = 5;
        static final int MSG_LOAD_SIZES = 7;
        static final int MSG_REBUILD_LIST = 1;
        boolean mRunning;
        final Stub mStatsObserver = new Stub() {
            /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x0020, B:40:0x00d4] */
            /* JADX WARNING: Missing block: B:42:0x00d5, code skipped:
            if (r4 == false) goto L_0x00fb;
     */
            /* JADX WARNING: Missing block: B:44:?, code skipped:
            r1.this$1.this$0.mMainHandler.sendMessage(r1.this$1.this$0.mMainHandler.obtainMessage(4, r2.packageName));
     */
            /* JADX WARNING: Missing block: B:53:0x00f7, code skipped:
            r0 = th;
     */
            /* JADX WARNING: Missing block: B:64:0x0129, code skipped:
            return;
     */
            /* JADX WARNING: Missing block: B:69:0x012f, code skipped:
            r0 = th;
     */
            public void onGetStatsCompleted(android.content.pm.PackageStats r18, boolean r19) {
                /*
                r17 = this;
                r1 = r17;
                r2 = r18;
                if (r19 != 0) goto L_0x0007;
            L_0x0006:
                return;
            L_0x0007:
                r4 = 0;
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;
                r0 = com.android.settingslib.applications.ApplicationsState.this;
                r5 = r0.mEntriesMap;
                monitor-enter(r5);
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012a }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012a }
                r0 = r0.mEntriesMap;	 Catch:{ all -> 0x012a }
                r6 = r2.userHandle;	 Catch:{ all -> 0x012a }
                r0 = r0.get(r6);	 Catch:{ all -> 0x012a }
                r0 = (java.util.HashMap) r0;	 Catch:{ all -> 0x012a }
                r6 = r0;
                if (r6 != 0) goto L_0x0022;
            L_0x0020:
                monitor-exit(r5);	 Catch:{ all -> 0x012f }
                return;
            L_0x0022:
                r0 = r2.packageName;	 Catch:{ all -> 0x012a }
                r0 = r6.get(r0);	 Catch:{ all -> 0x012a }
                r0 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r0;	 Catch:{ all -> 0x012a }
                r7 = r0;
                if (r7 == 0) goto L_0x00f9;
            L_0x002d:
                monitor-enter(r7);	 Catch:{ all -> 0x012a }
                r0 = 0;
                r7.sizeStale = r0;	 Catch:{ all -> 0x00f2 }
                r8 = 0;
                r7.sizeLoadStart = r8;	 Catch:{ all -> 0x00f2 }
                r8 = r2.externalCodeSize;	 Catch:{ all -> 0x00f2 }
                r10 = r2.externalObbSize;	 Catch:{ all -> 0x00f2 }
                r8 = r8 + r10;
                r10 = r2.externalDataSize;	 Catch:{ all -> 0x00f2 }
                r12 = r2.externalMediaSize;	 Catch:{ all -> 0x00f2 }
                r10 = r10 + r12;
                r12 = r8 + r10;
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00f2 }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00f2 }
                r14 = r0.getTotalInternalSize(r2);	 Catch:{ all -> 0x00f2 }
                r12 = r12 + r14;
                r14 = r7.size;	 Catch:{ all -> 0x00f2 }
                r0 = (r14 > r12 ? 1 : (r14 == r12 ? 0 : -1));
                if (r0 != 0) goto L_0x0082;
            L_0x0050:
                r14 = r7.cacheSize;	 Catch:{ all -> 0x00f2 }
                r16 = r4;
                r3 = r2.cacheSize;	 Catch:{ all -> 0x00ee }
                r0 = (r14 > r3 ? 1 : (r14 == r3 ? 0 : -1));
                if (r0 != 0) goto L_0x0084;
            L_0x005a:
                r3 = r7.codeSize;	 Catch:{ all -> 0x00ee }
                r14 = r2.codeSize;	 Catch:{ all -> 0x00ee }
                r0 = (r3 > r14 ? 1 : (r3 == r14 ? 0 : -1));
                if (r0 != 0) goto L_0x0084;
            L_0x0062:
                r3 = r7.dataSize;	 Catch:{ all -> 0x00ee }
                r14 = r2.dataSize;	 Catch:{ all -> 0x00ee }
                r0 = (r3 > r14 ? 1 : (r3 == r14 ? 0 : -1));
                if (r0 != 0) goto L_0x0084;
            L_0x006a:
                r3 = r7.externalCodeSize;	 Catch:{ all -> 0x00ee }
                r0 = (r3 > r8 ? 1 : (r3 == r8 ? 0 : -1));
                if (r0 != 0) goto L_0x0084;
            L_0x0070:
                r3 = r7.externalDataSize;	 Catch:{ all -> 0x00ee }
                r0 = (r3 > r10 ? 1 : (r3 == r10 ? 0 : -1));
                if (r0 != 0) goto L_0x0084;
            L_0x0076:
                r3 = r7.externalCacheSize;	 Catch:{ all -> 0x00ee }
                r14 = r2.externalCacheSize;	 Catch:{ all -> 0x00ee }
                r0 = (r3 > r14 ? 1 : (r3 == r14 ? 0 : -1));
                if (r0 == 0) goto L_0x007f;
            L_0x007e:
                goto L_0x0084;
            L_0x007f:
                r4 = r16;
                goto L_0x00d4;
            L_0x0082:
                r16 = r4;
            L_0x0084:
                r7.size = r12;	 Catch:{ all -> 0x00ee }
                r3 = r2.cacheSize;	 Catch:{ all -> 0x00ee }
                r7.cacheSize = r3;	 Catch:{ all -> 0x00ee }
                r3 = r2.codeSize;	 Catch:{ all -> 0x00ee }
                r7.codeSize = r3;	 Catch:{ all -> 0x00ee }
                r3 = r2.dataSize;	 Catch:{ all -> 0x00ee }
                r7.dataSize = r3;	 Catch:{ all -> 0x00ee }
                r7.externalCodeSize = r8;	 Catch:{ all -> 0x00ee }
                r7.externalDataSize = r10;	 Catch:{ all -> 0x00ee }
                r3 = r2.externalCacheSize;	 Catch:{ all -> 0x00ee }
                r7.externalCacheSize = r3;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00ee }
                r3 = r7.size;	 Catch:{ all -> 0x00ee }
                r0 = r0.getSizeStr(r3);	 Catch:{ all -> 0x00ee }
                r7.sizeStr = r0;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00ee }
                r3 = r0.getTotalInternalSize(r2);	 Catch:{ all -> 0x00ee }
                r7.internalSize = r3;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00ee }
                r3 = r7.internalSize;	 Catch:{ all -> 0x00ee }
                r0 = r0.getSizeStr(r3);	 Catch:{ all -> 0x00ee }
                r7.internalSizeStr = r0;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00ee }
                r3 = r0.getTotalExternalSize(r2);	 Catch:{ all -> 0x00ee }
                r7.externalSize = r3;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x00ee }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00ee }
                r3 = r7.externalSize;	 Catch:{ all -> 0x00ee }
                r0 = r0.getSizeStr(r3);	 Catch:{ all -> 0x00ee }
                r7.externalSizeStr = r0;	 Catch:{ all -> 0x00ee }
                r0 = 1;
                r4 = r0;
            L_0x00d4:
                monitor-exit(r7);	 Catch:{ all -> 0x00f7 }
                if (r4 == 0) goto L_0x00fb;
            L_0x00d7:
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r0 = r0.mMainHandler;	 Catch:{ all -> 0x012f }
                r3 = 4;
                r8 = r2.packageName;	 Catch:{ all -> 0x012f }
                r0 = r0.obtainMessage(r3, r8);	 Catch:{ all -> 0x012f }
                r3 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r3 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r3 = r3.mMainHandler;	 Catch:{ all -> 0x012f }
                r3.sendMessage(r0);	 Catch:{ all -> 0x012f }
                goto L_0x00fb;
            L_0x00ee:
                r0 = move-exception;
                r4 = r16;
                goto L_0x00f5;
            L_0x00f2:
                r0 = move-exception;
                r16 = r4;
            L_0x00f5:
                monitor-exit(r7);	 Catch:{ all -> 0x00f7 }
                throw r0;	 Catch:{ all -> 0x012f }
            L_0x00f7:
                r0 = move-exception;
                goto L_0x00f5;
            L_0x00f9:
                r16 = r4;
            L_0x00fb:
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r0 = r0.mCurComputingSizePkg;	 Catch:{ all -> 0x012f }
                if (r0 == 0) goto L_0x0128;
            L_0x0103:
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r0 = r0.mCurComputingSizePkg;	 Catch:{ all -> 0x012f }
                r3 = r2.packageName;	 Catch:{ all -> 0x012f }
                r0 = r0.equals(r3);	 Catch:{ all -> 0x012f }
                if (r0 == 0) goto L_0x0128;
            L_0x0111:
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r0 = r0.mCurComputingSizeUserId;	 Catch:{ all -> 0x012f }
                r3 = r2.userHandle;	 Catch:{ all -> 0x012f }
                if (r0 != r3) goto L_0x0128;
            L_0x011b:
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x012f }
                r3 = 0;
                r0.mCurComputingSizePkg = r3;	 Catch:{ all -> 0x012f }
                r0 = com.android.settingslib.applications.ApplicationsState.BackgroundHandler.this;	 Catch:{ all -> 0x012f }
                r3 = 7;
                r0.sendEmptyMessage(r3);	 Catch:{ all -> 0x012f }
            L_0x0128:
                monitor-exit(r5);	 Catch:{ all -> 0x012f }
                return;
            L_0x012a:
                r0 = move-exception;
                r16 = r4;
            L_0x012d:
                monitor-exit(r5);	 Catch:{ all -> 0x012f }
                throw r0;
            L_0x012f:
                r0 = move-exception;
                goto L_0x012d;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$BackgroundHandler$AnonymousClass1.onGetStatsCompleted(android.content.pm.PackageStats, boolean):void");
            }
        };

        BackgroundHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:8:0x0026, code skipped:
            r0 = false;
     */
        /* JADX WARNING: Missing block: B:9:0x0027, code skipped:
            if (r3 == null) goto L_0x003c;
     */
        /* JADX WARNING: Missing block: B:10:0x0029, code skipped:
            r4 = 0;
     */
        /* JADX WARNING: Missing block: B:12:0x002e, code skipped:
            if (r4 >= r3.size()) goto L_0x003c;
     */
        /* JADX WARNING: Missing block: B:13:0x0030, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r3.get(r4)).handleRebuildList();
            r4 = r4 + 1;
     */
        /* JADX WARNING: Missing block: B:14:0x003c, code skipped:
            r4 = getCombinedSessionFlags(r1.this$0.mSessions);
            r6 = 8388608;
            r14 = true;
     */
        /* JADX WARNING: Missing block: B:15:0x0050, code skipped:
            switch(r2.what) {
                case 1: goto L_0x03e3;
                case 2: goto L_0x031a;
                case 3: goto L_0x02b9;
                case 4: goto L_0x01a4;
                case 5: goto L_0x01a4;
                case 6: goto L_0x0124;
                case 7: goto L_0x0057;
                default: goto L_0x0053;
            };
     */
        /* JADX WARNING: Missing block: B:16:0x0053, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:18:0x005b, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r4, 4) == false) goto L_0x0120;
     */
        /* JADX WARNING: Missing block: B:19:0x005d, code skipped:
            r5 = r1.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:20:0x0061, code skipped:
            monitor-enter(r5);
     */
        /* JADX WARNING: Missing block: B:23:0x0066, code skipped:
            if (r1.this$0.mCurComputingSizePkg == null) goto L_0x006a;
     */
        /* JADX WARNING: Missing block: B:24:0x0068, code skipped:
            monitor-exit(r5);
     */
        /* JADX WARNING: Missing block: B:25:0x0069, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:26:0x006a, code skipped:
            r7 = android.os.SystemClock.uptimeMillis();
            r9 = 0;
     */
        /* JADX WARNING: Missing block: B:28:0x0077, code skipped:
            if (r9 >= r1.this$0.mAppEntries.size()) goto L_0x00f4;
     */
        /* JADX WARNING: Missing block: B:29:0x0079, code skipped:
            r10 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r1.this$0.mAppEntries.get(r9);
     */
        /* JADX WARNING: Missing block: B:30:0x008b, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r10.info.flags, 8388608) == false) goto L_0x00f0;
     */
        /* JADX WARNING: Missing block: B:32:0x0093, code skipped:
            if (r10.size == -1) goto L_0x0099;
     */
        /* JADX WARNING: Missing block: B:34:0x0097, code skipped:
            if (r10.sizeStale == false) goto L_0x00f0;
     */
        /* JADX WARNING: Missing block: B:36:0x009f, code skipped:
            if (r10.sizeLoadStart == 0) goto L_0x00ab;
     */
        /* JADX WARNING: Missing block: B:38:0x00a9, code skipped:
            if (r10.sizeLoadStart >= (r7 - 20000)) goto L_0x00ee;
     */
        /* JADX WARNING: Missing block: B:40:0x00ad, code skipped:
            if (r1.mRunning != false) goto L_0x00c4;
     */
        /* JADX WARNING: Missing block: B:41:0x00af, code skipped:
            r1.mRunning = true;
            r1.this$0.mMainHandler.sendMessage(r1.this$0.mMainHandler.obtainMessage(6, java.lang.Integer.valueOf(1)));
     */
        /* JADX WARNING: Missing block: B:42:0x00c4, code skipped:
            r10.sizeLoadStart = r7;
            r1.this$0.mCurComputingSizeUuid = r10.info.storageUuid;
            r1.this$0.mCurComputingSizePkg = r10.info.packageName;
            r1.this$0.mCurComputingSizeUserId = android.os.UserHandle.getUserId(r10.info.uid);
            r1.this$0.mBackgroundHandler.post(new com.android.settingslib.applications.-$$Lambda$ApplicationsState$BackgroundHandler$7jhXQzAcRoT6ACDzmPBTQMi7Ldc(r1));
     */
        /* JADX WARNING: Missing block: B:43:0x00ee, code skipped:
            monitor-exit(r5);
     */
        /* JADX WARNING: Missing block: B:44:0x00ef, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:45:0x00f0, code skipped:
            r9 = r9 + 1;
     */
        /* JADX WARNING: Missing block: B:47:0x00fd, code skipped:
            if (r1.this$0.mMainHandler.hasMessages(5) != false) goto L_0x011b;
     */
        /* JADX WARNING: Missing block: B:48:0x00ff, code skipped:
            r1.this$0.mMainHandler.sendEmptyMessage(5);
            r1.mRunning = false;
            r1.this$0.mMainHandler.sendMessage(r1.this$0.mMainHandler.obtainMessage(6, java.lang.Integer.valueOf(0)));
     */
        /* JADX WARNING: Missing block: B:49:0x011b, code skipped:
            monitor-exit(r5);
     */
        /* JADX WARNING: Missing block: B:54:0x0120, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:56:0x0128, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r4, 2) == false) goto L_0x019f;
     */
        /* JADX WARNING: Missing block: B:57:0x012a, code skipped:
            r5 = 0;
            r6 = r1.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:58:0x012f, code skipped:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:59:0x0131, code skipped:
            r7 = r0;
     */
        /* JADX WARNING: Missing block: B:62:0x013a, code skipped:
            if (r7 >= r1.this$0.mAppEntries.size()) goto L_0x0182;
     */
        /* JADX WARNING: Missing block: B:63:0x013c, code skipped:
            if (r5 >= 2) goto L_0x0182;
     */
        /* JADX WARNING: Missing block: B:64:0x013e, code skipped:
            r11 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r1.this$0.mAppEntries.get(r7);
     */
        /* JADX WARNING: Missing block: B:65:0x014b, code skipped:
            if (r11.icon == null) goto L_0x0151;
     */
        /* JADX WARNING: Missing block: B:67:0x014f, code skipped:
            if (r11.mounted != false) goto L_0x017c;
     */
        /* JADX WARNING: Missing block: B:68:0x0151, code skipped:
            monitor-enter(r11);
     */
        /* JADX WARNING: Missing block: B:71:0x015e, code skipped:
            if (r11.ensureIconLocked(r1.this$0.mContext, r1.this$0.mDrawableFactory) == false) goto L_0x017b;
     */
        /* JADX WARNING: Missing block: B:73:0x0162, code skipped:
            if (r1.mRunning != false) goto L_0x0179;
     */
        /* JADX WARNING: Missing block: B:74:0x0164, code skipped:
            r1.mRunning = true;
            r1.this$0.mMainHandler.sendMessage(r1.this$0.mMainHandler.obtainMessage(6, java.lang.Integer.valueOf(1)));
     */
        /* JADX WARNING: Missing block: B:75:0x0179, code skipped:
            r5 = r5 + 1;
     */
        /* JADX WARNING: Missing block: B:76:0x017b, code skipped:
            monitor-exit(r11);
     */
        /* JADX WARNING: Missing block: B:77:0x017c, code skipped:
            r0 = r7 + 1;
     */
        /* JADX WARNING: Missing block: B:82:0x0182, code skipped:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:83:0x0183, code skipped:
            if (r5 <= 0) goto L_0x0196;
     */
        /* JADX WARNING: Missing block: B:85:0x018d, code skipped:
            if (r1.this$0.mMainHandler.hasMessages(3) != false) goto L_0x0196;
     */
        /* JADX WARNING: Missing block: B:86:0x018f, code skipped:
            r1.this$0.mMainHandler.sendEmptyMessage(3);
     */
        /* JADX WARNING: Missing block: B:87:0x0196, code skipped:
            if (r5 < 2) goto L_0x019f;
     */
        /* JADX WARNING: Missing block: B:88:0x0198, code skipped:
            sendEmptyMessage(6);
     */
        /* JADX WARNING: Missing block: B:93:0x019f, code skipped:
            sendEmptyMessage(7);
     */
        /* JADX WARNING: Missing block: B:95:0x01a6, code skipped:
            if (r2.what != 4) goto L_0x01ae;
     */
        /* JADX WARNING: Missing block: B:97:0x01ac, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r4, 8) != false) goto L_0x01bb;
     */
        /* JADX WARNING: Missing block: B:99:0x01b1, code skipped:
            if (r2.what != 5) goto L_0x02a6;
     */
        /* JADX WARNING: Missing block: B:101:0x01b9, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r4, 16) == false) goto L_0x02a6;
     */
        /* JADX WARNING: Missing block: B:102:0x01bb, code skipped:
            r5 = new android.content.Intent("android.intent.action.MAIN", null);
     */
        /* JADX WARNING: Missing block: B:103:0x01c5, code skipped:
            if (r2.what != 4) goto L_0x01ca;
     */
        /* JADX WARNING: Missing block: B:104:0x01c7, code skipped:
            r6 = "android.intent.category.LAUNCHER";
     */
        /* JADX WARNING: Missing block: B:105:0x01ca, code skipped:
            r6 = android.support.v4.content.IntentCompat.CATEGORY_LEANBACK_LAUNCHER;
     */
        /* JADX WARNING: Missing block: B:106:0x01cc, code skipped:
            r5.addCategory(r6);
            r6 = 0;
     */
        /* JADX WARNING: Missing block: B:108:0x01d8, code skipped:
            if (r6 >= r1.this$0.mEntriesMap.size()) goto L_0x028f;
     */
        /* JADX WARNING: Missing block: B:109:0x01da, code skipped:
            r7 = r1.this$0.mEntriesMap.keyAt(r6);
            r9 = r1.this$0.mPm.queryIntentActivitiesAsUser(r5, 786944, r7);
            r10 = r1.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:110:0x01f1, code skipped:
            monitor-enter(r10);
     */
        /* JADX WARNING: Missing block: B:112:?, code skipped:
            r11 = (java.util.HashMap) r1.this$0.mEntriesMap.valueAt(r6);
            r18 = r9.size();
            r16 = r0;
     */
        /* JADX WARNING: Missing block: B:113:0x0204, code skipped:
            r15 = r18;
            r0 = r16;
     */
        /* JADX WARNING: Missing block: B:114:0x020a, code skipped:
            if (r0 >= r15) goto L_0x0274;
     */
        /* JADX WARNING: Missing block: B:115:0x020c, code skipped:
            r13 = (android.content.pm.ResolveInfo) r9.get(r0);
            r12 = r13.activityInfo.packageName;
     */
        /* JADX WARNING: Missing block: B:116:0x0220, code skipped:
            r8 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r11.get(r12);
     */
        /* JADX WARNING: Missing block: B:117:0x0224, code skipped:
            if (r8 == null) goto L_0x0240;
     */
        /* JADX WARNING: Missing block: B:119:?, code skipped:
            r8.hasLauncherEntry = r14;
     */
        /* JADX WARNING: Missing block: B:120:0x022a, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:122:?, code skipped:
            r8.launcherEntryEnabled = r13.activityInfo.enabled | r8.launcherEntryEnabled;
     */
        /* JADX WARNING: Missing block: B:123:0x0233, code skipped:
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:124:0x0236, code skipped:
            r0 = th;
     */
        /* JADX WARNING: Missing block: B:125:0x0237, code skipped:
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:126:0x023a, code skipped:
            r0 = th;
     */
        /* JADX WARNING: Missing block: B:127:0x023b, code skipped:
            r23 = r3;
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:128:0x0240, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:130:?, code skipped:
            r3 = com.android.settingslib.applications.ApplicationsState.TAG;
            r14 = new java.lang.StringBuilder();
     */
        /* JADX WARNING: Missing block: B:131:0x0249, code skipped:
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:133:?, code skipped:
            r14.append("Cannot find pkg: ");
            r14.append(r12);
            r14.append(" on user ");
            r14.append(r7);
            android.util.Log.w(r3, r14.toString());
     */
        /* JADX WARNING: Missing block: B:134:0x0262, code skipped:
            r16 = r0 + 1;
            r18 = r15;
            r3 = r23;
            r5 = r24;
            r14 = true;
     */
        /* JADX WARNING: Missing block: B:135:0x0270, code skipped:
            r0 = th;
     */
        /* JADX WARNING: Missing block: B:136:0x0271, code skipped:
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:137:0x0274, code skipped:
            r23 = r3;
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:138:0x0278, code skipped:
            monitor-exit(r10);
     */
        /* JADX WARNING: Missing block: B:139:0x0279, code skipped:
            r6 = r6 + 1;
            r3 = r23;
            r5 = r24;
            r0 = false;
            r14 = true;
     */
        /* JADX WARNING: Missing block: B:140:0x0286, code skipped:
            r0 = th;
     */
        /* JADX WARNING: Missing block: B:141:0x0287, code skipped:
            r23 = r3;
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:142:0x028b, code skipped:
            monitor-exit(r10);
     */
        /* JADX WARNING: Missing block: B:143:0x028c, code skipped:
            throw r0;
     */
        /* JADX WARNING: Missing block: B:144:0x028d, code skipped:
            r0 = th;
     */
        /* JADX WARNING: Missing block: B:145:0x028f, code skipped:
            r23 = r3;
            r24 = r5;
     */
        /* JADX WARNING: Missing block: B:146:0x029c, code skipped:
            if (r1.this$0.mMainHandler.hasMessages(7) != false) goto L_0x02a8;
     */
        /* JADX WARNING: Missing block: B:147:0x029e, code skipped:
            r1.this$0.mMainHandler.sendEmptyMessage(7);
     */
        /* JADX WARNING: Missing block: B:148:0x02a6, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:150:0x02ab, code skipped:
            if (r2.what != 4) goto L_0x02b3;
     */
        /* JADX WARNING: Missing block: B:151:0x02ad, code skipped:
            sendEmptyMessage(5);
     */
        /* JADX WARNING: Missing block: B:152:0x02b3, code skipped:
            sendEmptyMessage(6);
     */
        /* JADX WARNING: Missing block: B:153:0x02b9, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:154:0x02c0, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r4, 1) == null) goto L_0x0314;
     */
        /* JADX WARNING: Missing block: B:155:0x02c2, code skipped:
            r3 = new java.util.ArrayList();
            r1.this$0.mPm.getHomeActivities(r3);
            r5 = r1.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:156:0x02d3, code skipped:
            monitor-enter(r5);
     */
        /* JADX WARNING: Missing block: B:158:?, code skipped:
            r0 = r1.this$0.mEntriesMap.size();
            r20 = 0;
     */
        /* JADX WARNING: Missing block: B:159:0x02de, code skipped:
            r6 = r20;
     */
        /* JADX WARNING: Missing block: B:160:0x02e0, code skipped:
            if (r6 >= r0) goto L_0x030f;
     */
        /* JADX WARNING: Missing block: B:161:0x02e2, code skipped:
            r7 = (java.util.HashMap) r1.this$0.mEntriesMap.valueAt(r6);
            r8 = r3.iterator();
     */
        /* JADX WARNING: Missing block: B:163:0x02f4, code skipped:
            if (r8.hasNext() == false) goto L_0x030c;
     */
        /* JADX WARNING: Missing block: B:164:0x02f6, code skipped:
            r11 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r7.get(((android.content.pm.ResolveInfo) r8.next()).activityInfo.packageName);
     */
        /* JADX WARNING: Missing block: B:165:0x0306, code skipped:
            if (r11 == null) goto L_0x030b;
     */
        /* JADX WARNING: Missing block: B:166:0x0308, code skipped:
            r11.isHomeApp = true;
     */
        /* JADX WARNING: Missing block: B:168:0x030c, code skipped:
            r20 = r6 + 1;
     */
        /* JADX WARNING: Missing block: B:169:0x030f, code skipped:
            monitor-exit(r5);
     */
        /* JADX WARNING: Missing block: B:174:0x0314, code skipped:
            sendEmptyMessage(4);
     */
        /* JADX WARNING: Missing block: B:175:0x031a, code skipped:
            r23 = r3;
            r3 = r1.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:176:0x0321, code skipped:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:177:0x0322, code skipped:
            r5 = 0;
            r0 = 0;
     */
        /* JADX WARNING: Missing block: B:180:0x032c, code skipped:
            if (r0 >= r1.this$0.mApplications.size()) goto L_0x03c3;
     */
        /* JADX WARNING: Missing block: B:182:0x032f, code skipped:
            if (r5 >= 6) goto L_0x03c3;
     */
        /* JADX WARNING: Missing block: B:184:0x0333, code skipped:
            if (r1.mRunning != false) goto L_0x034d;
     */
        /* JADX WARNING: Missing block: B:185:0x0335, code skipped:
            r1.mRunning = true;
            r1.this$0.mMainHandler.sendMessage(r1.this$0.mMainHandler.obtainMessage(6, java.lang.Integer.valueOf(1)));
     */
        /* JADX WARNING: Missing block: B:187:0x034e, code skipped:
            r11 = (android.content.pm.ApplicationInfo) r1.this$0.mApplications.get(r0);
            r12 = android.os.UserHandle.getUserId(r11.uid);
     */
        /* JADX WARNING: Missing block: B:188:0x036e, code skipped:
            if (((java.util.HashMap) r1.this$0.mEntriesMap.get(r12)).get(r11.packageName) != null) goto L_0x0377;
     */
        /* JADX WARNING: Missing block: B:189:0x0370, code skipped:
            r5 = r5 + 1;
            com.android.settingslib.applications.ApplicationsState.access$100(r1.this$0, r11);
     */
        /* JADX WARNING: Missing block: B:190:0x0377, code skipped:
            if (r12 == 0) goto L_0x03bc;
     */
        /* JADX WARNING: Missing block: B:192:0x0382, code skipped:
            if (r1.this$0.mEntriesMap.indexOfKey(0) < 0) goto L_0x03ba;
     */
        /* JADX WARNING: Missing block: B:193:0x0384, code skipped:
            r13 = (com.android.settingslib.applications.ApplicationsState.AppEntry) ((java.util.HashMap) r1.this$0.mEntriesMap.get(0)).get(r11.packageName);
     */
        /* JADX WARNING: Missing block: B:194:0x0396, code skipped:
            if (r13 == null) goto L_0x03bc;
     */
        /* JADX WARNING: Missing block: B:196:0x03a0, code skipped:
            if (com.android.settingslib.applications.ApplicationsState.access$200(r13.info.flags, r6) != false) goto L_0x03bc;
     */
        /* JADX WARNING: Missing block: B:197:0x03a2, code skipped:
            ((java.util.HashMap) r1.this$0.mEntriesMap.get(0)).remove(r11.packageName);
            r1.this$0.mAppEntries.remove(r13);
     */
        /* JADX WARNING: Missing block: B:198:0x03ba, code skipped:
            r15 = 0;
     */
        /* JADX WARNING: Missing block: B:200:0x03bd, code skipped:
            r0 = r0 + 1;
            r6 = 8388608;
     */
        /* JADX WARNING: Missing block: B:201:0x03c3, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:203:0x03c5, code skipped:
            if (r5 < 6) goto L_0x03cb;
     */
        /* JADX WARNING: Missing block: B:204:0x03c7, code skipped:
            sendEmptyMessage(2);
     */
        /* JADX WARNING: Missing block: B:206:0x03d3, code skipped:
            if (r1.this$0.mMainHandler.hasMessages(8) != false) goto L_0x03dc;
     */
        /* JADX WARNING: Missing block: B:207:0x03d5, code skipped:
            r1.this$0.mMainHandler.sendEmptyMessage(8);
     */
        /* JADX WARNING: Missing block: B:208:0x03dc, code skipped:
            sendEmptyMessage(3);
     */
        /* JADX WARNING: Missing block: B:213:0x03e3, code skipped:
            r23 = r3;
     */
        /* JADX WARNING: Missing block: B:214:0x03e5, code skipped:
            return;
     */
        public void handleMessage(android.os.Message r27) {
            /*
            r26 = this;
            r1 = r26;
            r2 = r27;
            r3 = 0;
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r0.mRebuildingSessions;
            monitor-enter(r4);
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03ea }
            r0 = r0.mRebuildingSessions;	 Catch:{ all -> 0x03ea }
            r0 = r0.size();	 Catch:{ all -> 0x03ea }
            if (r0 <= 0) goto L_0x0025;
        L_0x0014:
            r0 = new java.util.ArrayList;	 Catch:{ all -> 0x03ea }
            r5 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03ea }
            r5 = r5.mRebuildingSessions;	 Catch:{ all -> 0x03ea }
            r0.<init>(r5);	 Catch:{ all -> 0x03ea }
            r3 = r0;
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03ea }
            r0 = r0.mRebuildingSessions;	 Catch:{ all -> 0x03ea }
            r0.clear();	 Catch:{ all -> 0x03ea }
        L_0x0025:
            monitor-exit(r4);	 Catch:{ all -> 0x03e6 }
            r0 = 0;
            if (r3 == 0) goto L_0x003c;
        L_0x0029:
            r4 = r0;
        L_0x002a:
            r5 = r3.size();
            if (r4 >= r5) goto L_0x003c;
        L_0x0030:
            r5 = r3.get(r4);
            r5 = (com.android.settingslib.applications.ApplicationsState.Session) r5;
            r5.handleRebuildList();
            r4 = r4 + 1;
            goto L_0x002a;
        L_0x003c:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r4.mSessions;
            r4 = r1.getCombinedSessionFlags(r4);
            r5 = r2.what;
            r6 = 8388608; // 0x800000 float:1.17549435E-38 double:4.144523E-317;
            r7 = 8;
            r8 = 7;
            r9 = 3;
            r10 = 2;
            r12 = 4;
            r13 = 6;
            r14 = 1;
            switch(r5) {
                case 1: goto L_0x03e3;
                case 2: goto L_0x031a;
                case 3: goto L_0x02b9;
                case 4: goto L_0x01a4;
                case 5: goto L_0x01a4;
                case 6: goto L_0x0124;
                case 7: goto L_0x0057;
                default: goto L_0x0053;
            };
        L_0x0053:
            r23 = r3;
            goto L_0x03e5;
        L_0x0057:
            r5 = com.android.settingslib.applications.ApplicationsState.hasFlag(r4, r12);
            if (r5 == 0) goto L_0x0120;
        L_0x005d:
            r5 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = r5.mEntriesMap;
            monitor-enter(r5);
            r7 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r7 = r7.mCurComputingSizePkg;	 Catch:{ all -> 0x011d }
            if (r7 == 0) goto L_0x006a;
        L_0x0068:
            monitor-exit(r5);	 Catch:{ all -> 0x011d }
            return;
        L_0x006a:
            r7 = android.os.SystemClock.uptimeMillis();	 Catch:{ all -> 0x011d }
            r9 = r0;
        L_0x006f:
            r10 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r10 = r10.mAppEntries;	 Catch:{ all -> 0x011d }
            r10 = r10.size();	 Catch:{ all -> 0x011d }
            if (r9 >= r10) goto L_0x00f4;
        L_0x0079:
            r10 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r10 = r10.mAppEntries;	 Catch:{ all -> 0x011d }
            r10 = r10.get(r9);	 Catch:{ all -> 0x011d }
            r10 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r10;	 Catch:{ all -> 0x011d }
            r12 = r10.info;	 Catch:{ all -> 0x011d }
            r12 = r12.flags;	 Catch:{ all -> 0x011d }
            r12 = com.android.settingslib.applications.ApplicationsState.hasFlag(r12, r6);	 Catch:{ all -> 0x011d }
            if (r12 == 0) goto L_0x00f0;
        L_0x008d:
            r11 = r10.size;	 Catch:{ all -> 0x011d }
            r16 = -1;
            r11 = (r11 > r16 ? 1 : (r11 == r16 ? 0 : -1));
            if (r11 == 0) goto L_0x0099;
        L_0x0095:
            r11 = r10.sizeStale;	 Catch:{ all -> 0x011d }
            if (r11 == 0) goto L_0x00f0;
        L_0x0099:
            r11 = r10.sizeLoadStart;	 Catch:{ all -> 0x011d }
            r15 = 0;
            r0 = (r11 > r15 ? 1 : (r11 == r15 ? 0 : -1));
            if (r0 == 0) goto L_0x00ab;
        L_0x00a1:
            r11 = r10.sizeLoadStart;	 Catch:{ all -> 0x011d }
            r15 = 20000; // 0x4e20 float:2.8026E-41 double:9.8813E-320;
            r15 = r7 - r15;
            r0 = (r11 > r15 ? 1 : (r11 == r15 ? 0 : -1));
            if (r0 >= 0) goto L_0x00ee;
        L_0x00ab:
            r0 = r1.mRunning;	 Catch:{ all -> 0x011d }
            if (r0 != 0) goto L_0x00c4;
        L_0x00af:
            r1.mRunning = r14;	 Catch:{ all -> 0x011d }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r0 = r0.mMainHandler;	 Catch:{ all -> 0x011d }
            r6 = java.lang.Integer.valueOf(r14);	 Catch:{ all -> 0x011d }
            r0 = r0.obtainMessage(r13, r6);	 Catch:{ all -> 0x011d }
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x011d }
            r6.sendMessage(r0);	 Catch:{ all -> 0x011d }
        L_0x00c4:
            r10.sizeLoadStart = r7;	 Catch:{ all -> 0x011d }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r10.info;	 Catch:{ all -> 0x011d }
            r6 = r6.storageUuid;	 Catch:{ all -> 0x011d }
            r0.mCurComputingSizeUuid = r6;	 Catch:{ all -> 0x011d }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r10.info;	 Catch:{ all -> 0x011d }
            r6 = r6.packageName;	 Catch:{ all -> 0x011d }
            r0.mCurComputingSizePkg = r6;	 Catch:{ all -> 0x011d }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r10.info;	 Catch:{ all -> 0x011d }
            r6 = r6.uid;	 Catch:{ all -> 0x011d }
            r6 = android.os.UserHandle.getUserId(r6);	 Catch:{ all -> 0x011d }
            r0.mCurComputingSizeUserId = r6;	 Catch:{ all -> 0x011d }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r0 = r0.mBackgroundHandler;	 Catch:{ all -> 0x011d }
            r6 = new com.android.settingslib.applications.-$$Lambda$ApplicationsState$BackgroundHandler$7jhXQzAcRoT6ACDzmPBTQMi7Ldc;	 Catch:{ all -> 0x011d }
            r6.<init>(r1);	 Catch:{ all -> 0x011d }
            r0.post(r6);	 Catch:{ all -> 0x011d }
        L_0x00ee:
            monitor-exit(r5);	 Catch:{ all -> 0x011d }
            return;
        L_0x00f0:
            r9 = r9 + 1;
            goto L_0x006f;
        L_0x00f4:
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x011d }
            r9 = 5;
            r6 = r6.hasMessages(r9);	 Catch:{ all -> 0x011d }
            if (r6 != 0) goto L_0x011b;
        L_0x00ff:
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x011d }
            r6.sendEmptyMessage(r9);	 Catch:{ all -> 0x011d }
            r1.mRunning = r0;	 Catch:{ all -> 0x011d }
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x011d }
            r0 = java.lang.Integer.valueOf(r0);	 Catch:{ all -> 0x011d }
            r0 = r6.obtainMessage(r13, r0);	 Catch:{ all -> 0x011d }
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x011d }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x011d }
            r6.sendMessage(r0);	 Catch:{ all -> 0x011d }
        L_0x011b:
            monitor-exit(r5);	 Catch:{ all -> 0x011d }
            goto L_0x0120;
        L_0x011d:
            r0 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x011d }
            throw r0;
        L_0x0120:
            r23 = r3;
            goto L_0x03e5;
        L_0x0124:
            r5 = com.android.settingslib.applications.ApplicationsState.hasFlag(r4, r10);
            if (r5 == 0) goto L_0x019f;
        L_0x012a:
            r5 = 0;
            r6 = com.android.settingslib.applications.ApplicationsState.this;
            r6 = r6.mEntriesMap;
            monitor-enter(r6);
        L_0x0131:
            r7 = r0;
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x019c }
            r0 = r0.mAppEntries;	 Catch:{ all -> 0x019c }
            r0 = r0.size();	 Catch:{ all -> 0x019c }
            if (r7 >= r0) goto L_0x0182;
        L_0x013c:
            if (r5 >= r10) goto L_0x0182;
        L_0x013e:
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x019c }
            r0 = r0.mAppEntries;	 Catch:{ all -> 0x019c }
            r0 = r0.get(r7);	 Catch:{ all -> 0x019c }
            r0 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r0;	 Catch:{ all -> 0x019c }
            r11 = r0;
            r0 = r11.icon;	 Catch:{ all -> 0x019c }
            if (r0 == 0) goto L_0x0151;
        L_0x014d:
            r0 = r11.mounted;	 Catch:{ all -> 0x019c }
            if (r0 != 0) goto L_0x017c;
        L_0x0151:
            monitor-enter(r11);	 Catch:{ all -> 0x019c }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x017f }
            r0 = r0.mContext;	 Catch:{ all -> 0x017f }
            r12 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x017f }
            r12 = r12.mDrawableFactory;	 Catch:{ all -> 0x017f }
            r0 = r11.ensureIconLocked(r0, r12);	 Catch:{ all -> 0x017f }
            if (r0 == 0) goto L_0x017b;
        L_0x0160:
            r0 = r1.mRunning;	 Catch:{ all -> 0x017f }
            if (r0 != 0) goto L_0x0179;
        L_0x0164:
            r1.mRunning = r14;	 Catch:{ all -> 0x017f }
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x017f }
            r0 = r0.mMainHandler;	 Catch:{ all -> 0x017f }
            r12 = java.lang.Integer.valueOf(r14);	 Catch:{ all -> 0x017f }
            r0 = r0.obtainMessage(r13, r12);	 Catch:{ all -> 0x017f }
            r12 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x017f }
            r12 = r12.mMainHandler;	 Catch:{ all -> 0x017f }
            r12.sendMessage(r0);	 Catch:{ all -> 0x017f }
        L_0x0179:
            r5 = r5 + 1;
        L_0x017b:
            monitor-exit(r11);	 Catch:{ all -> 0x017f }
        L_0x017c:
            r0 = r7 + 1;
            goto L_0x0131;
        L_0x017f:
            r0 = move-exception;
            monitor-exit(r11);	 Catch:{ all -> 0x017f }
            throw r0;	 Catch:{ all -> 0x019c }
        L_0x0182:
            monitor-exit(r6);	 Catch:{ all -> 0x019c }
            if (r5 <= 0) goto L_0x0196;
        L_0x0185:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r0 = r0.hasMessages(r9);
            if (r0 != 0) goto L_0x0196;
        L_0x018f:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r0.sendEmptyMessage(r9);
        L_0x0196:
            if (r5 < r10) goto L_0x019f;
        L_0x0198:
            r1.sendEmptyMessage(r13);
            goto L_0x0120;
        L_0x019c:
            r0 = move-exception;
            monitor-exit(r6);	 Catch:{ all -> 0x019c }
            throw r0;
        L_0x019f:
            r1.sendEmptyMessage(r8);
            goto L_0x0120;
        L_0x01a4:
            r5 = r2.what;
            if (r5 != r12) goto L_0x01ae;
        L_0x01a8:
            r5 = com.android.settingslib.applications.ApplicationsState.hasFlag(r4, r7);
            if (r5 != 0) goto L_0x01bb;
        L_0x01ae:
            r5 = r2.what;
            r6 = 5;
            if (r5 != r6) goto L_0x02a6;
        L_0x01b3:
            r5 = 16;
            r5 = com.android.settingslib.applications.ApplicationsState.hasFlag(r4, r5);
            if (r5 == 0) goto L_0x02a6;
        L_0x01bb:
            r5 = new android.content.Intent;
            r6 = "android.intent.action.MAIN";
            r7 = 0;
            r5.<init>(r6, r7);
            r6 = r2.what;
            if (r6 != r12) goto L_0x01ca;
        L_0x01c7:
            r6 = "android.intent.category.LAUNCHER";
            goto L_0x01cc;
        L_0x01ca:
            r6 = "android.intent.category.LEANBACK_LAUNCHER";
        L_0x01cc:
            r5.addCategory(r6);
            r6 = r0;
        L_0x01d0:
            r7 = com.android.settingslib.applications.ApplicationsState.this;
            r7 = r7.mEntriesMap;
            r7 = r7.size();
            if (r6 >= r7) goto L_0x028f;
        L_0x01da:
            r7 = com.android.settingslib.applications.ApplicationsState.this;
            r7 = r7.mEntriesMap;
            r7 = r7.keyAt(r6);
            r9 = com.android.settingslib.applications.ApplicationsState.this;
            r9 = r9.mPm;
            r10 = 786944; // 0xc0200 float:1.102743E-39 double:3.88802E-318;
            r9 = r9.queryIntentActivitiesAsUser(r5, r10, r7);
            r10 = com.android.settingslib.applications.ApplicationsState.this;
            r10 = r10.mEntriesMap;
            monitor-enter(r10);
            r11 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x0286 }
            r11 = r11.mEntriesMap;	 Catch:{ all -> 0x0286 }
            r11 = r11.valueAt(r6);	 Catch:{ all -> 0x0286 }
            r11 = (java.util.HashMap) r11;	 Catch:{ all -> 0x0286 }
            r16 = r9.size();	 Catch:{ all -> 0x0286 }
            r18 = r16;
            r16 = r0;
        L_0x0204:
            r19 = r16;
            r15 = r18;
            r0 = r19;
            if (r0 >= r15) goto L_0x0274;
        L_0x020c:
            r16 = r9.get(r0);	 Catch:{ all -> 0x0286 }
            r16 = (android.content.pm.ResolveInfo) r16;	 Catch:{ all -> 0x0286 }
            r21 = r16;
            r13 = r21;
            r12 = r13.activityInfo;	 Catch:{ all -> 0x0286 }
            r12 = r12.packageName;	 Catch:{ all -> 0x0286 }
            r16 = r11.get(r12);	 Catch:{ all -> 0x0286 }
            r16 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r16;	 Catch:{ all -> 0x0286 }
            r22 = r16;
            r8 = r22;
            if (r8 == 0) goto L_0x0240;
        L_0x0226:
            r8.hasLauncherEntry = r14;	 Catch:{ all -> 0x023a }
            r14 = r8.launcherEntryEnabled;	 Catch:{ all -> 0x023a }
            r23 = r3;
            r3 = r13.activityInfo;	 Catch:{ all -> 0x0236 }
            r3 = r3.enabled;	 Catch:{ all -> 0x0236 }
            r3 = r3 | r14;
            r8.launcherEntryEnabled = r3;	 Catch:{ all -> 0x0236 }
            r24 = r5;
            goto L_0x0262;
        L_0x0236:
            r0 = move-exception;
            r24 = r5;
            goto L_0x028b;
        L_0x023a:
            r0 = move-exception;
            r23 = r3;
            r24 = r5;
            goto L_0x028b;
        L_0x0240:
            r23 = r3;
            r3 = "ApplicationsState";
            r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0270 }
            r14.<init>();	 Catch:{ all -> 0x0270 }
            r24 = r5;
            r5 = "Cannot find pkg: ";
            r14.append(r5);	 Catch:{ all -> 0x028d }
            r14.append(r12);	 Catch:{ all -> 0x028d }
            r5 = " on user ";
            r14.append(r5);	 Catch:{ all -> 0x028d }
            r14.append(r7);	 Catch:{ all -> 0x028d }
            r5 = r14.toString();	 Catch:{ all -> 0x028d }
            android.util.Log.w(r3, r5);	 Catch:{ all -> 0x028d }
        L_0x0262:
            r16 = r0 + 1;
            r18 = r15;
            r3 = r23;
            r5 = r24;
            r0 = 0;
            r8 = 7;
            r12 = 4;
            r13 = 6;
            r14 = 1;
            goto L_0x0204;
        L_0x0270:
            r0 = move-exception;
            r24 = r5;
            goto L_0x028b;
        L_0x0274:
            r23 = r3;
            r24 = r5;
            monitor-exit(r10);	 Catch:{ all -> 0x028d }
            r6 = r6 + 1;
            r3 = r23;
            r5 = r24;
            r0 = 0;
            r8 = 7;
            r12 = 4;
            r13 = 6;
            r14 = 1;
            goto L_0x01d0;
        L_0x0286:
            r0 = move-exception;
            r23 = r3;
            r24 = r5;
        L_0x028b:
            monitor-exit(r10);	 Catch:{ all -> 0x028d }
            throw r0;
        L_0x028d:
            r0 = move-exception;
            goto L_0x028b;
        L_0x028f:
            r23 = r3;
            r24 = r5;
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r3 = 7;
            r0 = r0.hasMessages(r3);
            if (r0 != 0) goto L_0x02a8;
        L_0x029e:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r0.sendEmptyMessage(r3);
            goto L_0x02a8;
        L_0x02a6:
            r23 = r3;
        L_0x02a8:
            r0 = r2.what;
            r3 = 4;
            if (r0 != r3) goto L_0x02b3;
        L_0x02ad:
            r0 = 5;
            r1.sendEmptyMessage(r0);
            goto L_0x03e5;
        L_0x02b3:
            r0 = 6;
            r1.sendEmptyMessage(r0);
            goto L_0x03e5;
        L_0x02b9:
            r23 = r3;
            r0 = 1;
            r3 = com.android.settingslib.applications.ApplicationsState.hasFlag(r4, r0);
            if (r3 == 0) goto L_0x0314;
        L_0x02c2:
            r0 = new java.util.ArrayList;
            r0.<init>();
            r3 = r0;
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mPm;
            r0.getHomeActivities(r3);
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = r0.mEntriesMap;
            monitor-enter(r5);
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x0311 }
            r0 = r0.mEntriesMap;	 Catch:{ all -> 0x0311 }
            r0 = r0.size();	 Catch:{ all -> 0x0311 }
            r20 = 0;
        L_0x02de:
            r6 = r20;
            if (r6 >= r0) goto L_0x030f;
        L_0x02e2:
            r7 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x0311 }
            r7 = r7.mEntriesMap;	 Catch:{ all -> 0x0311 }
            r7 = r7.valueAt(r6);	 Catch:{ all -> 0x0311 }
            r7 = (java.util.HashMap) r7;	 Catch:{ all -> 0x0311 }
            r8 = r3.iterator();	 Catch:{ all -> 0x0311 }
        L_0x02f0:
            r9 = r8.hasNext();	 Catch:{ all -> 0x0311 }
            if (r9 == 0) goto L_0x030c;
        L_0x02f6:
            r9 = r8.next();	 Catch:{ all -> 0x0311 }
            r9 = (android.content.pm.ResolveInfo) r9;	 Catch:{ all -> 0x0311 }
            r10 = r9.activityInfo;	 Catch:{ all -> 0x0311 }
            r10 = r10.packageName;	 Catch:{ all -> 0x0311 }
            r11 = r7.get(r10);	 Catch:{ all -> 0x0311 }
            r11 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r11;	 Catch:{ all -> 0x0311 }
            if (r11 == 0) goto L_0x030b;
        L_0x0308:
            r12 = 1;
            r11.isHomeApp = r12;	 Catch:{ all -> 0x0311 }
        L_0x030b:
            goto L_0x02f0;
        L_0x030c:
            r20 = r6 + 1;
            goto L_0x02de;
        L_0x030f:
            monitor-exit(r5);	 Catch:{ all -> 0x0311 }
            goto L_0x0314;
        L_0x0311:
            r0 = move-exception;
            monitor-exit(r5);	 Catch:{ all -> 0x0311 }
            throw r0;
        L_0x0314:
            r0 = 4;
            r1.sendEmptyMessage(r0);
            goto L_0x03e5;
        L_0x031a:
            r23 = r3;
            r0 = 0;
            r3 = com.android.settingslib.applications.ApplicationsState.this;
            r3 = r3.mEntriesMap;
            monitor-enter(r3);
            r5 = r0;
            r0 = 0;
        L_0x0324:
            r8 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r8 = r8.mApplications;	 Catch:{ all -> 0x03e0 }
            r8 = r8.size();	 Catch:{ all -> 0x03e0 }
            if (r0 >= r8) goto L_0x03c3;
        L_0x032e:
            r8 = 6;
            if (r5 >= r8) goto L_0x03c3;
        L_0x0331:
            r8 = r1.mRunning;	 Catch:{ all -> 0x03e0 }
            if (r8 != 0) goto L_0x034d;
        L_0x0335:
            r8 = 1;
            r1.mRunning = r8;	 Catch:{ all -> 0x03e0 }
            r11 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r11 = r11.mMainHandler;	 Catch:{ all -> 0x03e0 }
            r12 = java.lang.Integer.valueOf(r8);	 Catch:{ all -> 0x03e0 }
            r13 = 6;
            r11 = r11.obtainMessage(r13, r12);	 Catch:{ all -> 0x03e0 }
            r12 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r12 = r12.mMainHandler;	 Catch:{ all -> 0x03e0 }
            r12.sendMessage(r11);	 Catch:{ all -> 0x03e0 }
            goto L_0x034e;
        L_0x034d:
            r8 = 1;
        L_0x034e:
            r11 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r11 = r11.mApplications;	 Catch:{ all -> 0x03e0 }
            r11 = r11.get(r0);	 Catch:{ all -> 0x03e0 }
            r11 = (android.content.pm.ApplicationInfo) r11;	 Catch:{ all -> 0x03e0 }
            r12 = r11.uid;	 Catch:{ all -> 0x03e0 }
            r12 = android.os.UserHandle.getUserId(r12);	 Catch:{ all -> 0x03e0 }
            r13 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r13 = r13.mEntriesMap;	 Catch:{ all -> 0x03e0 }
            r13 = r13.get(r12);	 Catch:{ all -> 0x03e0 }
            r13 = (java.util.HashMap) r13;	 Catch:{ all -> 0x03e0 }
            r14 = r11.packageName;	 Catch:{ all -> 0x03e0 }
            r13 = r13.get(r14);	 Catch:{ all -> 0x03e0 }
            if (r13 != 0) goto L_0x0377;
        L_0x0370:
            r5 = r5 + 1;
            r13 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r13.getEntryLocked(r11);	 Catch:{ all -> 0x03e0 }
        L_0x0377:
            if (r12 == 0) goto L_0x03bc;
        L_0x0379:
            r13 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r13 = r13.mEntriesMap;	 Catch:{ all -> 0x03e0 }
            r14 = 0;
            r13 = r13.indexOfKey(r14);	 Catch:{ all -> 0x03e0 }
            if (r13 < 0) goto L_0x03ba;
        L_0x0384:
            r13 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r13 = r13.mEntriesMap;	 Catch:{ all -> 0x03e0 }
            r13 = r13.get(r14);	 Catch:{ all -> 0x03e0 }
            r13 = (java.util.HashMap) r13;	 Catch:{ all -> 0x03e0 }
            r14 = r11.packageName;	 Catch:{ all -> 0x03e0 }
            r13 = r13.get(r14);	 Catch:{ all -> 0x03e0 }
            r13 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r13;	 Catch:{ all -> 0x03e0 }
            if (r13 == 0) goto L_0x03bc;
        L_0x0398:
            r14 = r13.info;	 Catch:{ all -> 0x03e0 }
            r14 = r14.flags;	 Catch:{ all -> 0x03e0 }
            r14 = com.android.settingslib.applications.ApplicationsState.hasFlag(r14, r6);	 Catch:{ all -> 0x03e0 }
            if (r14 != 0) goto L_0x03bc;
        L_0x03a2:
            r14 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r14 = r14.mEntriesMap;	 Catch:{ all -> 0x03e0 }
            r15 = 0;
            r14 = r14.get(r15);	 Catch:{ all -> 0x03e0 }
            r14 = (java.util.HashMap) r14;	 Catch:{ all -> 0x03e0 }
            r6 = r11.packageName;	 Catch:{ all -> 0x03e0 }
            r14.remove(r6);	 Catch:{ all -> 0x03e0 }
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x03e0 }
            r6 = r6.mAppEntries;	 Catch:{ all -> 0x03e0 }
            r6.remove(r13);	 Catch:{ all -> 0x03e0 }
            goto L_0x03bd;
        L_0x03ba:
            r15 = r14;
            goto L_0x03bd;
        L_0x03bc:
            r15 = 0;
        L_0x03bd:
            r0 = r0 + 1;
            r6 = 8388608; // 0x800000 float:1.17549435E-38 double:4.144523E-317;
            goto L_0x0324;
        L_0x03c3:
            monitor-exit(r3);	 Catch:{ all -> 0x03e0 }
            r0 = 6;
            if (r5 < r0) goto L_0x03cb;
        L_0x03c7:
            r1.sendEmptyMessage(r10);
            goto L_0x03df;
        L_0x03cb:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r0 = r0.hasMessages(r7);
            if (r0 != 0) goto L_0x03dc;
        L_0x03d5:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mMainHandler;
            r0.sendEmptyMessage(r7);
        L_0x03dc:
            r1.sendEmptyMessage(r9);
        L_0x03df:
            goto L_0x03e5;
        L_0x03e0:
            r0 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x03e0 }
            throw r0;
        L_0x03e3:
            r23 = r3;
        L_0x03e5:
            return;
        L_0x03e6:
            r0 = move-exception;
            r23 = r3;
            goto L_0x03eb;
        L_0x03ea:
            r0 = move-exception;
        L_0x03eb:
            monitor-exit(r4);	 Catch:{ all -> 0x03ea }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$BackgroundHandler.handleMessage(android.os.Message):void");
        }

        /* JADX WARNING: Removed duplicated region for block: B:5:0x0040 A:{ExcHandler: NameNotFoundException | IOException (r0_3 'e' java.lang.Exception), Splitter:B:0:0x0000} */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing block: B:5:0x0040, code skipped:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:6:0x0041, code skipped:
            r1 = com.android.settingslib.applications.ApplicationsState.TAG;
            r2 = new java.lang.StringBuilder();
            r2.append("Failed to query stats: ");
            r2.append(r0);
            android.util.Log.w(r1, r2.toString());
     */
        /* JADX WARNING: Missing block: B:8:?, code skipped:
            r4.mStatsObserver.onGetStatsCompleted(null, false);
     */
        /* JADX WARNING: Missing block: B:10:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:11:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:13:?, code skipped:
            return;
     */
        public static /* synthetic */ void lambda$handleMessage$0(com.android.settingslib.applications.ApplicationsState.BackgroundHandler r4) {
            /*
            r0 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r0 = r0.mStats;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1 = r1.mCurComputingSizeUuid;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r2.mCurComputingSizePkg;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r3 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r3 = r3.mCurComputingSizeUserId;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r3 = android.os.UserHandle.of(r3);	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r0 = r0.queryStatsForPackage(r1, r2, r3);	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1 = new android.content.pm.PackageStats;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r2.mCurComputingSizePkg;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r3 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r3 = r3.mCurComputingSizeUserId;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1.<init>(r2, r3);	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r0.getCodeBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1.codeSize = r2;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r0.getDataBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1.dataSize = r2;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r0.getCacheBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r1.cacheSize = r2;	 Catch:{ NameNotFoundException | IOException -> 0x0040, NameNotFoundException | IOException -> 0x0040 }
            r2 = r4.mStatsObserver;	 Catch:{ RemoteException -> 0x003e }
            r3 = 1;
            r2.onGetStatsCompleted(r1, r3);	 Catch:{ RemoteException -> 0x003e }
            goto L_0x003f;
        L_0x003e:
            r2 = move-exception;
        L_0x003f:
            goto L_0x0060;
        L_0x0040:
            r0 = move-exception;
            r1 = "ApplicationsState";
            r2 = new java.lang.StringBuilder;
            r2.<init>();
            r3 = "Failed to query stats: ";
            r2.append(r3);
            r2.append(r0);
            r2 = r2.toString();
            android.util.Log.w(r1, r2);
            r1 = r4.mStatsObserver;	 Catch:{ RemoteException -> 0x005f }
            r2 = 0;
            r3 = 0;
            r1.onGetStatsCompleted(r2, r3);	 Catch:{ RemoteException -> 0x005f }
            goto L_0x0060;
        L_0x005f:
            r1 = move-exception;
        L_0x0060:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$BackgroundHandler.lambda$handleMessage$0(com.android.settingslib.applications.ApplicationsState$BackgroundHandler):void");
        }

        private int getCombinedSessionFlags(List<Session> sessions) {
            int flags;
            synchronized (ApplicationsState.this.mEntriesMap) {
                flags = 0;
                for (Session session : sessions) {
                    flags |= session.mFlags;
                }
            }
            return flags;
        }
    }

    public interface Callbacks {
        void onAllSizesComputed();

        void onLauncherInfoChanged();

        void onLoadEntriesCompleted();

        void onPackageIconChanged();

        void onPackageListChanged();

        void onPackageSizeChanged(String str);

        void onRebuildComplete(ArrayList<AppEntry> arrayList);

        void onRunningStateChanged(boolean z);
    }

    class MainHandler extends Handler {
        static final int MSG_ALL_SIZES_COMPUTED = 5;
        static final int MSG_LAUNCHER_INFO_CHANGED = 7;
        static final int MSG_LOAD_ENTRIES_COMPLETE = 8;
        static final int MSG_PACKAGE_ICON_CHANGED = 3;
        static final int MSG_PACKAGE_LIST_CHANGED = 2;
        static final int MSG_PACKAGE_SIZE_CHANGED = 4;
        static final int MSG_REBUILD_COMPLETE = 1;
        static final int MSG_RUNNING_STATE_CHANGED = 6;

        public MainHandler(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Missing block: B:2:0x000e, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:3:0x0017, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:4:0x0019, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onLoadEntriesCompleted();
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:5:0x002c, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:6:0x0035, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:7:0x0037, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onLauncherInfoChanged();
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:16:0x0072, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:17:0x007b, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:18:0x007d, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onAllSizesComputed();
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:19:0x0092, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:20:0x009b, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:21:0x009d, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onPackageSizeChanged((java.lang.String) r5.obj);
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:22:0x00b5, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:23:0x00be, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:24:0x00c0, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onPackageIconChanged();
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:25:0x00d4, code skipped:
            r0 = r1;
     */
        /* JADX WARNING: Missing block: B:26:0x00dd, code skipped:
            if (r0 >= r4.this$0.mActiveSessions.size()) goto L_0x0108;
     */
        /* JADX WARNING: Missing block: B:27:0x00df, code skipped:
            ((com.android.settingslib.applications.ApplicationsState.Session) r4.this$0.mActiveSessions.get(r0)).mCallbacks.onPackageListChanged();
            r1 = r0 + 1;
     */
        /* JADX WARNING: Missing block: B:43:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:44:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:46:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:47:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:48:?, code skipped:
            return;
     */
        /* JADX WARNING: Missing block: B:49:?, code skipped:
            return;
     */
        public void handleMessage(android.os.Message r5) {
            /*
            r4 = this;
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0.rebuildActiveSessions();
            r0 = r5.what;
            r1 = 0;
            switch(r0) {
                case 1: goto L_0x00f2;
                case 2: goto L_0x00d3;
                case 3: goto L_0x00b4;
                case 4: goto L_0x0091;
                case 5: goto L_0x0071;
                case 6: goto L_0x004b;
                case 7: goto L_0x002b;
                case 8: goto L_0x000d;
                default: goto L_0x000b;
            };
        L_0x000b:
            goto L_0x0108;
        L_0x000e:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x0108;
        L_0x0019:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r1.onLoadEntriesCompleted();
            r1 = r0 + 1;
            goto L_0x000e;
        L_0x002c:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x0049;
        L_0x0037:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r1.onLauncherInfoChanged();
            r1 = r0 + 1;
            goto L_0x002c;
        L_0x0049:
            goto L_0x0108;
        L_0x004b:
            r0 = r1;
        L_0x004c:
            r2 = com.android.settingslib.applications.ApplicationsState.this;
            r2 = r2.mActiveSessions;
            r2 = r2.size();
            if (r0 >= r2) goto L_0x006f;
        L_0x0056:
            r2 = com.android.settingslib.applications.ApplicationsState.this;
            r2 = r2.mActiveSessions;
            r2 = r2.get(r0);
            r2 = (com.android.settingslib.applications.ApplicationsState.Session) r2;
            r2 = r2.mCallbacks;
            r3 = r5.arg1;
            if (r3 == 0) goto L_0x0068;
        L_0x0066:
            r3 = 1;
            goto L_0x0069;
        L_0x0068:
            r3 = r1;
        L_0x0069:
            r2.onRunningStateChanged(r3);
            r0 = r0 + 1;
            goto L_0x004c;
        L_0x006f:
            goto L_0x0108;
        L_0x0072:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x008f;
        L_0x007d:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r1.onAllSizesComputed();
            r1 = r0 + 1;
            goto L_0x0072;
        L_0x008f:
            goto L_0x0108;
        L_0x0092:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x00b3;
        L_0x009d:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r2 = r5.obj;
            r2 = (java.lang.String) r2;
            r1.onPackageSizeChanged(r2);
            r1 = r0 + 1;
            goto L_0x0092;
        L_0x00b3:
            goto L_0x0108;
        L_0x00b5:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x00d2;
        L_0x00c0:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r1.onPackageIconChanged();
            r1 = r0 + 1;
            goto L_0x00b5;
        L_0x00d2:
            goto L_0x0108;
        L_0x00d4:
            r0 = r1;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.size();
            if (r0 >= r1) goto L_0x00f1;
        L_0x00df:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.get(r0);
            r1 = (com.android.settingslib.applications.ApplicationsState.Session) r1;
            r1 = r1.mCallbacks;
            r1.onPackageListChanged();
            r1 = r0 + 1;
            goto L_0x00d4;
        L_0x00f1:
            goto L_0x0108;
        L_0x00f2:
            r0 = r5.obj;
            r0 = (com.android.settingslib.applications.ApplicationsState.Session) r0;
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r1 = r1.mActiveSessions;
            r1 = r1.contains(r0);
            if (r1 == 0) goto L_0x0107;
        L_0x0100:
            r1 = r0.mCallbacks;
            r2 = r0.mLastAppList;
            r1.onRebuildComplete(r2);
        L_0x0108:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$MainHandler.handleMessage(android.os.Message):void");
        }
    }

    private class PackageIntentReceiver extends BroadcastReceiver {
        private PackageIntentReceiver() {
        }

        /* synthetic */ PackageIntentReceiver(ApplicationsState x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: 0000 */
        public void registerReceiver() {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addDataScheme("package");
            ApplicationsState.this.mContext.registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            ApplicationsState.this.mContext.registerReceiver(this, sdFilter);
            IntentFilter userFilter = new IntentFilter();
            userFilter.addAction("android.intent.action.USER_ADDED");
            userFilter.addAction("android.intent.action.USER_REMOVED");
            ApplicationsState.this.mContext.registerReceiver(this, userFilter);
        }

        /* Access modifiers changed, original: 0000 */
        public void unregisterReceiver() {
            ApplicationsState.this.mContext.unregisterReceiver(this);
        }

        /* JADX WARNING: Missing block: B:42:0x0101, code skipped:
            return;
     */
        public void onReceive(android.content.Context r11, android.content.Intent r12) {
            /*
            r10 = this;
            r0 = r12.getAction();
            r1 = "android.intent.action.PACKAGE_ADDED";
            r1 = r1.equals(r0);
            r2 = 0;
            if (r1 == 0) goto L_0x0032;
        L_0x000d:
            r1 = r12.getData();
            r3 = r1.getEncodedSchemeSpecificPart();
        L_0x0016:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r4.mEntriesMap;
            r4 = r4.size();
            if (r2 >= r4) goto L_0x0030;
        L_0x0020:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = r5.mEntriesMap;
            r5 = r5.keyAt(r2);
            r4.addPackage(r3, r5);
            r2 = r2 + 1;
            goto L_0x0016;
        L_0x0030:
            goto L_0x0100;
        L_0x0032:
            r1 = "android.intent.action.PACKAGE_REMOVED";
            r1 = r1.equals(r0);
            if (r1 == 0) goto L_0x005f;
        L_0x003a:
            r1 = r12.getData();
            r3 = r1.getEncodedSchemeSpecificPart();
        L_0x0043:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r4.mEntriesMap;
            r4 = r4.size();
            if (r2 >= r4) goto L_0x005d;
        L_0x004d:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = r5.mEntriesMap;
            r5 = r5.keyAt(r2);
            r4.removePackage(r3, r5);
            r2 = r2 + 1;
            goto L_0x0043;
        L_0x005d:
            goto L_0x0100;
        L_0x005f:
            r1 = "android.intent.action.PACKAGE_CHANGED";
            r1 = r1.equals(r0);
            if (r1 == 0) goto L_0x008c;
        L_0x0067:
            r1 = r12.getData();
            r3 = r1.getEncodedSchemeSpecificPart();
        L_0x0070:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r4.mEntriesMap;
            r4 = r4.size();
            if (r2 >= r4) goto L_0x008a;
        L_0x007a:
            r4 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = com.android.settingslib.applications.ApplicationsState.this;
            r5 = r5.mEntriesMap;
            r5 = r5.keyAt(r2);
            r4.invalidatePackage(r3, r5);
            r2 = r2 + 1;
            goto L_0x0070;
        L_0x008a:
            goto L_0x0100;
        L_0x008c:
            r1 = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
            r1 = r1.equals(r0);
            if (r1 != 0) goto L_0x00c7;
        L_0x0094:
            r1 = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
            r1 = r1.equals(r0);
            if (r1 == 0) goto L_0x009d;
        L_0x009c:
            goto L_0x00c7;
        L_0x009d:
            r1 = "android.intent.action.USER_ADDED";
            r1 = r1.equals(r0);
            r2 = -10000; // 0xffffffffffffd8f0 float:NaN double:NaN;
            if (r1 == 0) goto L_0x00b3;
        L_0x00a7:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r3 = "android.intent.extra.user_handle";
            r2 = r12.getIntExtra(r3, r2);
            r1.addUser(r2);
            goto L_0x0100;
        L_0x00b3:
            r1 = "android.intent.action.USER_REMOVED";
            r1 = r1.equals(r0);
            if (r1 == 0) goto L_0x0100;
        L_0x00bb:
            r1 = com.android.settingslib.applications.ApplicationsState.this;
            r3 = "android.intent.extra.user_handle";
            r2 = r12.getIntExtra(r3, r2);
            r1.removeUser(r2);
            goto L_0x0100;
        L_0x00c7:
            r1 = "android.intent.extra.changed_package_list";
            r1 = r12.getStringArrayExtra(r1);
            if (r1 == 0) goto L_0x0101;
        L_0x00cf:
            r3 = r1.length;
            if (r3 != 0) goto L_0x00d3;
        L_0x00d2:
            goto L_0x0101;
        L_0x00d3:
            r3 = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
            r3 = r3.equals(r0);
            if (r3 == 0) goto L_0x00ff;
        L_0x00db:
            r4 = r1.length;
            r5 = r2;
        L_0x00dd:
            if (r5 >= r4) goto L_0x00ff;
        L_0x00df:
            r6 = r1[r5];
            r7 = r2;
        L_0x00e2:
            r8 = com.android.settingslib.applications.ApplicationsState.this;
            r8 = r8.mEntriesMap;
            r8 = r8.size();
            if (r7 >= r8) goto L_0x00fc;
        L_0x00ec:
            r8 = com.android.settingslib.applications.ApplicationsState.this;
            r9 = com.android.settingslib.applications.ApplicationsState.this;
            r9 = r9.mEntriesMap;
            r9 = r9.keyAt(r7);
            r8.invalidatePackage(r6, r9);
            r7 = r7 + 1;
            goto L_0x00e2;
        L_0x00fc:
            r5 = r5 + 1;
            goto L_0x00dd;
        L_0x0100:
            return;
        L_0x0101:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$PackageIntentReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SessionFlags {
    }

    public static class SizeInfo {
        public long cacheSize;
        public long codeSize;
        public long dataSize;
        public long externalCacheSize;
        public long externalCodeSize;
        public long externalDataSize;
    }

    public static class AppEntry extends SizeInfo {
        public final File apkFile;
        public long externalSize;
        public String externalSizeStr;
        public Object extraInfo;
        public boolean hasLauncherEntry;
        public Drawable icon;
        public final long id;
        public ApplicationInfo info;
        public long internalSize;
        public String internalSizeStr;
        public boolean isHomeApp;
        public String label;
        public boolean launcherEntryEnabled;
        public boolean mounted;
        public String normalizedLabel;
        public long size = -1;
        public long sizeLoadStart;
        public boolean sizeStale = true;
        public String sizeStr;

        public String getNormalizedLabel() {
            if (this.normalizedLabel != null) {
                return this.normalizedLabel;
            }
            this.normalizedLabel = ApplicationsState.normalize(this.label);
            return this.normalizedLabel;
        }

        @VisibleForTesting(otherwise = 2)
        public AppEntry(Context context, ApplicationInfo info, long id) {
            this.apkFile = new File(info.sourceDir);
            this.id = id;
            this.info = info;
            ensureLabel(context);
        }

        public void ensureLabel(Context context) {
            if (this.label != null && this.mounted) {
                return;
            }
            if (this.apkFile.exists()) {
                this.mounted = true;
                CharSequence label = this.info.loadLabel(context.getPackageManager());
                this.label = label != null ? label.toString() : this.info.packageName;
                return;
            }
            this.mounted = false;
            this.label = this.info.packageName;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean ensureIconLocked(Context context, IconDrawableFactory drawableFactory) {
            if (this.icon == null) {
                if (this.apkFile.exists()) {
                    this.icon = drawableFactory.getBadgedIcon(this.info);
                    return true;
                }
                this.mounted = false;
                this.icon = context.getDrawable(17303559);
            } else if (!this.mounted && this.apkFile.exists()) {
                this.mounted = true;
                this.icon = drawableFactory.getBadgedIcon(this.info);
                return true;
            }
            return false;
        }

        public String getVersion(Context context) {
            try {
                return context.getPackageManager().getPackageInfo(this.info.packageName, 0).versionName;
            } catch (NameNotFoundException e) {
                return "";
            }
        }
    }

    public static class CompoundFilter implements AppFilter {
        private final AppFilter mFirstFilter;
        private final AppFilter mSecondFilter;

        public CompoundFilter(AppFilter first, AppFilter second) {
            this.mFirstFilter = first;
            this.mSecondFilter = second;
        }

        public void init(Context context) {
            this.mFirstFilter.init(context);
            this.mSecondFilter.init(context);
        }

        public void init() {
            this.mFirstFilter.init();
            this.mSecondFilter.init();
        }

        public boolean filterApp(AppEntry info) {
            return this.mFirstFilter.filterApp(info) && this.mSecondFilter.filterApp(info);
        }
    }

    public class Session implements LifecycleObserver {
        final Callbacks mCallbacks;
        private int mFlags = 15;
        private final boolean mHasLifecycle;
        ArrayList<AppEntry> mLastAppList;
        boolean mRebuildAsync;
        Comparator<AppEntry> mRebuildComparator;
        AppFilter mRebuildFilter;
        boolean mRebuildForeground;
        boolean mRebuildRequested;
        ArrayList<AppEntry> mRebuildResult;
        final Object mRebuildSync = new Object();
        boolean mResumed;

        Session(Callbacks callbacks, Lifecycle lifecycle) {
            this.mCallbacks = callbacks;
            if (lifecycle != null) {
                lifecycle.addObserver(this);
                this.mHasLifecycle = true;
                return;
            }
            this.mHasLifecycle = false;
        }

        public int getSessionFlags() {
            return this.mFlags;
        }

        public void setSessionFlags(int flags) {
            this.mFlags = flags;
        }

        @OnLifecycleEvent(Event.ON_RESUME)
        public void onResume() {
            synchronized (ApplicationsState.this.mEntriesMap) {
                if (!this.mResumed) {
                    this.mResumed = true;
                    ApplicationsState.this.mSessionsChanged = true;
                    ApplicationsState.this.doResumeIfNeededLocked();
                }
            }
        }

        @OnLifecycleEvent(Event.ON_PAUSE)
        public void onPause() {
            synchronized (ApplicationsState.this.mEntriesMap) {
                if (this.mResumed) {
                    this.mResumed = false;
                    ApplicationsState.this.mSessionsChanged = true;
                    ApplicationsState.this.mBackgroundHandler.removeMessages(1, this);
                    ApplicationsState.this.doPauseIfNeededLocked();
                }
            }
        }

        public ArrayList<AppEntry> getAllApps() {
            ArrayList arrayList;
            synchronized (ApplicationsState.this.mEntriesMap) {
                arrayList = new ArrayList(ApplicationsState.this.mAppEntries);
            }
            return arrayList;
        }

        public ArrayList<AppEntry> rebuild(AppFilter filter, Comparator<AppEntry> comparator) {
            return rebuild(filter, comparator, true);
        }

        public ArrayList<AppEntry> rebuild(AppFilter filter, Comparator<AppEntry> comparator, boolean foreground) {
            synchronized (this.mRebuildSync) {
                synchronized (ApplicationsState.this.mRebuildingSessions) {
                    ApplicationsState.this.mRebuildingSessions.add(this);
                    this.mRebuildRequested = true;
                    this.mRebuildAsync = true;
                    this.mRebuildFilter = filter;
                    this.mRebuildComparator = comparator;
                    this.mRebuildForeground = foreground;
                    this.mRebuildResult = null;
                    if (!ApplicationsState.this.mBackgroundHandler.hasMessages(1)) {
                        ApplicationsState.this.mBackgroundHandler.sendMessage(ApplicationsState.this.mBackgroundHandler.obtainMessage(1));
                    }
                }
            }
            return null;
        }

        /* Access modifiers changed, original: 0000 */
        /* JADX WARNING: Missing block: B:11:0x0020, code skipped:
            if (r1 == null) goto L_0x0029;
     */
        /* JADX WARNING: Missing block: B:12:0x0022, code skipped:
            r1.init(r8.this$0.mContext);
     */
        /* JADX WARNING: Missing block: B:13:0x0029, code skipped:
            r4 = r8.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:14:0x002d, code skipped:
            monitor-enter(r4);
     */
        /* JADX WARNING: Missing block: B:16:?, code skipped:
            r0 = new java.util.ArrayList(r8.this$0.mAppEntries);
     */
        /* JADX WARNING: Missing block: B:17:0x0037, code skipped:
            monitor-exit(r4);
     */
        /* JADX WARNING: Missing block: B:18:0x0038, code skipped:
            r5 = new java.util.ArrayList();
     */
        /* JADX WARNING: Missing block: B:20:0x0043, code skipped:
            if (r3 >= r0.size()) goto L_0x0070;
     */
        /* JADX WARNING: Missing block: B:21:0x0045, code skipped:
            r4 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r0.get(r3);
     */
        /* JADX WARNING: Missing block: B:22:0x004b, code skipped:
            if (r4 == null) goto L_0x006d;
     */
        /* JADX WARNING: Missing block: B:23:0x004d, code skipped:
            if (r1 == null) goto L_0x0055;
     */
        /* JADX WARNING: Missing block: B:25:0x0053, code skipped:
            if (r1.filterApp(r4) == false) goto L_0x006d;
     */
        /* JADX WARNING: Missing block: B:26:0x0055, code skipped:
            r6 = r8.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:27:0x0059, code skipped:
            monitor-enter(r6);
     */
        /* JADX WARNING: Missing block: B:28:0x005a, code skipped:
            if (r2 == null) goto L_0x0066;
     */
        /* JADX WARNING: Missing block: B:30:?, code skipped:
            r4.ensureLabel(r8.this$0.mContext);
     */
        /* JADX WARNING: Missing block: B:33:0x0066, code skipped:
            r5.add(r4);
     */
        /* JADX WARNING: Missing block: B:34:0x0069, code skipped:
            monitor-exit(r6);
     */
        /* JADX WARNING: Missing block: B:38:0x006d, code skipped:
            r3 = r3 + 1;
     */
        /* JADX WARNING: Missing block: B:39:0x0070, code skipped:
            if (r2 == null) goto L_0x007f;
     */
        /* JADX WARNING: Missing block: B:40:0x0072, code skipped:
            r3 = r8.this$0.mEntriesMap;
     */
        /* JADX WARNING: Missing block: B:41:0x0076, code skipped:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:43:?, code skipped:
            java.util.Collections.sort(r5, r2);
     */
        /* JADX WARNING: Missing block: B:44:0x007a, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:49:0x007f, code skipped:
            r3 = r8.mRebuildSync;
     */
        /* JADX WARNING: Missing block: B:50:0x0081, code skipped:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:53:0x0084, code skipped:
            if (r8.mRebuildRequested != false) goto L_0x00ae;
     */
        /* JADX WARNING: Missing block: B:54:0x0086, code skipped:
            r8.mLastAppList = r5;
     */
        /* JADX WARNING: Missing block: B:55:0x008a, code skipped:
            if (r8.mRebuildAsync != false) goto L_0x0094;
     */
        /* JADX WARNING: Missing block: B:56:0x008c, code skipped:
            r8.mRebuildResult = r5;
            r8.mRebuildSync.notifyAll();
     */
        /* JADX WARNING: Missing block: B:58:0x009d, code skipped:
            if (r8.this$0.mMainHandler.hasMessages(1, r8) != false) goto L_0x00ae;
     */
        /* JADX WARNING: Missing block: B:59:0x009f, code skipped:
            r8.this$0.mMainHandler.sendMessage(r8.this$0.mMainHandler.obtainMessage(1, r8));
     */
        /* JADX WARNING: Missing block: B:60:0x00ae, code skipped:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:61:0x00af, code skipped:
            android.os.Process.setThreadPriority(10);
     */
        /* JADX WARNING: Missing block: B:62:0x00b4, code skipped:
            return;
     */
        public void handleRebuildList() {
            /*
            r8 = this;
            r0 = r8.mRebuildSync;
            monitor-enter(r0);
            r1 = r8.mRebuildRequested;	 Catch:{ all -> 0x00bb }
            if (r1 != 0) goto L_0x0009;
        L_0x0007:
            monitor-exit(r0);	 Catch:{ all -> 0x00bb }
            return;
        L_0x0009:
            r1 = r8.mRebuildFilter;	 Catch:{ all -> 0x00bb }
            r2 = r8.mRebuildComparator;	 Catch:{ all -> 0x00bb }
            r3 = 0;
            r8.mRebuildRequested = r3;	 Catch:{ all -> 0x00bb }
            r4 = 0;
            r8.mRebuildFilter = r4;	 Catch:{ all -> 0x00bb }
            r8.mRebuildComparator = r4;	 Catch:{ all -> 0x00bb }
            r4 = r8.mRebuildForeground;	 Catch:{ all -> 0x00bb }
            if (r4 == 0) goto L_0x001f;
        L_0x0019:
            r4 = -2;
            android.os.Process.setThreadPriority(r4);	 Catch:{ all -> 0x00bb }
            r8.mRebuildForeground = r3;	 Catch:{ all -> 0x00bb }
        L_0x001f:
            monitor-exit(r0);	 Catch:{ all -> 0x00bb }
            if (r1 == 0) goto L_0x0029;
        L_0x0022:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r0 = r0.mContext;
            r1.init(r0);
        L_0x0029:
            r0 = com.android.settingslib.applications.ApplicationsState.this;
            r4 = r0.mEntriesMap;
            monitor-enter(r4);
            r0 = new java.util.ArrayList;	 Catch:{ all -> 0x00b8 }
            r5 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00b8 }
            r5 = r5.mAppEntries;	 Catch:{ all -> 0x00b8 }
            r0.<init>(r5);	 Catch:{ all -> 0x00b8 }
            monitor-exit(r4);	 Catch:{ all -> 0x00b8 }
            r4 = new java.util.ArrayList;
            r4.<init>();
            r5 = r4;
        L_0x003f:
            r4 = r0.size();
            if (r3 >= r4) goto L_0x0070;
        L_0x0045:
            r4 = r0.get(r3);
            r4 = (com.android.settingslib.applications.ApplicationsState.AppEntry) r4;
            if (r4 == 0) goto L_0x006d;
        L_0x004d:
            if (r1 == 0) goto L_0x0055;
        L_0x004f:
            r6 = r1.filterApp(r4);
            if (r6 == 0) goto L_0x006d;
        L_0x0055:
            r6 = com.android.settingslib.applications.ApplicationsState.this;
            r6 = r6.mEntriesMap;
            monitor-enter(r6);
            if (r2 == 0) goto L_0x0066;
        L_0x005c:
            r7 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x0064 }
            r7 = r7.mContext;	 Catch:{ all -> 0x0064 }
            r4.ensureLabel(r7);	 Catch:{ all -> 0x0064 }
            goto L_0x0066;
        L_0x0064:
            r7 = move-exception;
            goto L_0x006b;
        L_0x0066:
            r5.add(r4);	 Catch:{ all -> 0x0064 }
            monitor-exit(r6);	 Catch:{ all -> 0x0064 }
            goto L_0x006d;
        L_0x006b:
            monitor-exit(r6);	 Catch:{ all -> 0x0064 }
            throw r7;
        L_0x006d:
            r3 = r3 + 1;
            goto L_0x003f;
        L_0x0070:
            if (r2 == 0) goto L_0x007f;
        L_0x0072:
            r3 = com.android.settingslib.applications.ApplicationsState.this;
            r3 = r3.mEntriesMap;
            monitor-enter(r3);
            java.util.Collections.sort(r5, r2);	 Catch:{ all -> 0x007c }
            monitor-exit(r3);	 Catch:{ all -> 0x007c }
            goto L_0x007f;
        L_0x007c:
            r4 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x007c }
            throw r4;
        L_0x007f:
            r3 = r8.mRebuildSync;
            monitor-enter(r3);
            r4 = r8.mRebuildRequested;	 Catch:{ all -> 0x00b5 }
            if (r4 != 0) goto L_0x00ae;
        L_0x0086:
            r8.mLastAppList = r5;	 Catch:{ all -> 0x00b5 }
            r4 = r8.mRebuildAsync;	 Catch:{ all -> 0x00b5 }
            if (r4 != 0) goto L_0x0094;
        L_0x008c:
            r8.mRebuildResult = r5;	 Catch:{ all -> 0x00b5 }
            r4 = r8.mRebuildSync;	 Catch:{ all -> 0x00b5 }
            r4.notifyAll();	 Catch:{ all -> 0x00b5 }
            goto L_0x00ae;
        L_0x0094:
            r4 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00b5 }
            r4 = r4.mMainHandler;	 Catch:{ all -> 0x00b5 }
            r6 = 1;
            r4 = r4.hasMessages(r6, r8);	 Catch:{ all -> 0x00b5 }
            if (r4 != 0) goto L_0x00ae;
        L_0x009f:
            r4 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00b5 }
            r4 = r4.mMainHandler;	 Catch:{ all -> 0x00b5 }
            r4 = r4.obtainMessage(r6, r8);	 Catch:{ all -> 0x00b5 }
            r6 = com.android.settingslib.applications.ApplicationsState.this;	 Catch:{ all -> 0x00b5 }
            r6 = r6.mMainHandler;	 Catch:{ all -> 0x00b5 }
            r6.sendMessage(r4);	 Catch:{ all -> 0x00b5 }
        L_0x00ae:
            monitor-exit(r3);	 Catch:{ all -> 0x00b5 }
            r3 = 10;
            android.os.Process.setThreadPriority(r3);
            return;
        L_0x00b5:
            r4 = move-exception;
            monitor-exit(r3);	 Catch:{ all -> 0x00b5 }
            throw r4;
        L_0x00b8:
            r0 = move-exception;
            monitor-exit(r4);	 Catch:{ all -> 0x00b8 }
            throw r0;
        L_0x00bb:
            r1 = move-exception;
            monitor-exit(r0);	 Catch:{ all -> 0x00bb }
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState$Session.handleRebuildList():void");
        }

        @OnLifecycleEvent(Event.ON_DESTROY)
        public void onDestroy() {
            if (!this.mHasLifecycle) {
                onPause();
            }
            synchronized (ApplicationsState.this.mEntriesMap) {
                ApplicationsState.this.mSessions.remove(this);
            }
        }
    }

    public static class VolumeFilter implements AppFilter {
        private final String mVolumeUuid;

        public VolumeFilter(String volumeUuid) {
            this.mVolumeUuid = volumeUuid;
        }

        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return Objects.equals(info.info.volumeUuid, this.mVolumeUuid);
        }
    }

    public static ApplicationsState getInstance(Application app) {
        ApplicationsState applicationsState;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ApplicationsState(app);
            }
            applicationsState = sInstance;
        }
        return applicationsState;
    }

    private ApplicationsState(Application app) {
        this.mContext = app;
        this.mPm = this.mContext.getPackageManager();
        this.mDrawableFactory = IconDrawableFactory.newInstance(this.mContext);
        this.mIpm = AppGlobals.getPackageManager();
        this.mUm = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mStats = (StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class);
        for (int userId : this.mUm.getProfileIdsWithDisabled(UserHandle.myUserId())) {
            this.mEntriesMap.put(userId, new HashMap());
        }
        this.mThread = new HandlerThread("ApplicationsState.Loader", 10);
        this.mThread.start();
        this.mBackgroundHandler = new BackgroundHandler(this.mThread.getLooper());
        this.mAdminRetrieveFlags = 4227584;
        this.mRetrieveFlags = 33280;
        synchronized (this.mEntriesMap) {
            try {
                this.mEntriesMap.wait(1);
            } catch (InterruptedException e) {
            }
        }
    }

    public Looper getBackgroundLooper() {
        return this.mThread.getLooper();
    }

    public Session newSession(Callbacks callbacks) {
        return newSession(callbacks, null);
    }

    public Session newSession(Callbacks callbacks, Lifecycle lifecycle) {
        Session s = new Session(callbacks, lifecycle);
        synchronized (this.mEntriesMap) {
            this.mSessions.add(s);
        }
        return s;
    }

    /* Access modifiers changed, original: 0000 */
    public void doResumeIfNeededLocked() {
        if (!this.mResumed) {
            int i;
            this.mResumed = true;
            if (this.mPackageIntentReceiver == null) {
                this.mPackageIntentReceiver = new PackageIntentReceiver(this, null);
                this.mPackageIntentReceiver.registerReceiver();
            }
            this.mApplications = new ArrayList();
            for (UserInfo user : this.mUm.getProfiles(UserHandle.myUserId())) {
                try {
                    if (this.mEntriesMap.indexOfKey(user.id) < 0) {
                        this.mEntriesMap.put(user.id, new HashMap());
                    }
                    this.mApplications.addAll(this.mIpm.getInstalledApplications(user.isAdmin() ? this.mAdminRetrieveFlags : this.mRetrieveFlags, user.id).getList());
                } catch (RemoteException e) {
                }
            }
            int i2 = 0;
            if (this.mInterestingConfigChanges.applyNewConfig(this.mContext.getResources())) {
                clearEntries();
            } else {
                for (i = 0; i < this.mAppEntries.size(); i++) {
                    ((AppEntry) this.mAppEntries.get(i)).sizeStale = true;
                }
            }
            this.mHaveDisabledApps = false;
            this.mHaveInstantApps = false;
            while (true) {
                i = i2;
                if (i >= this.mApplications.size()) {
                    break;
                }
                ApplicationInfo info = (ApplicationInfo) this.mApplications.get(i);
                if (!info.enabled) {
                    if (info.enabledSetting != 3) {
                        this.mApplications.remove(i);
                        i--;
                        i2 = i + 1;
                    } else {
                        this.mHaveDisabledApps = true;
                    }
                }
                if (!this.mHaveInstantApps && AppUtils.isInstant(info)) {
                    this.mHaveInstantApps = true;
                }
                int userId = UserHandle.getUserId(info.uid);
                if (userId != 999 || (info.flags & 1) <= 0) {
                    AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(info.packageName);
                    if (entry != null) {
                        entry.info = info;
                    }
                    i2 = i + 1;
                } else {
                    this.mApplications.remove(i);
                    i--;
                    i2 = i + 1;
                }
            }
            if (this.mAppEntries.size() > this.mApplications.size()) {
                clearEntries();
            }
            this.mCurComputingSizePkg = null;
            if (!this.mBackgroundHandler.hasMessages(2)) {
                this.mBackgroundHandler.sendEmptyMessage(2);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void clearEntries() {
        for (int i = 0; i < this.mEntriesMap.size(); i++) {
            ((HashMap) this.mEntriesMap.valueAt(i)).clear();
        }
        this.mAppEntries.clear();
    }

    public boolean haveDisabledApps() {
        return this.mHaveDisabledApps;
    }

    public boolean haveInstantApps() {
        return this.mHaveInstantApps;
    }

    /* Access modifiers changed, original: 0000 */
    public void doPauseIfNeededLocked() {
        if (this.mResumed) {
            int i = 0;
            while (i < this.mSessions.size()) {
                if (!((Session) this.mSessions.get(i)).mResumed) {
                    i++;
                } else {
                    return;
                }
            }
            doPauseLocked();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void doPauseLocked() {
        this.mResumed = false;
        if (this.mPackageIntentReceiver != null) {
            this.mPackageIntentReceiver.unregisterReceiver();
            this.mPackageIntentReceiver = null;
        }
    }

    public AppEntry getEntry(String packageName, int userId) {
        AppEntry entry;
        synchronized (this.mEntriesMap) {
            entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(packageName);
            if (entry == null) {
                ApplicationInfo info = getAppInfoLocked(packageName, userId);
                if (info == null) {
                    try {
                        info = this.mIpm.getApplicationInfo(packageName, 0, userId);
                    } catch (RemoteException e) {
                        Log.w(TAG, "getEntry couldn't reach PackageManager", e);
                        return null;
                    }
                }
                if (info != null) {
                    entry = getEntryLocked(info);
                }
            }
        }
        return entry;
    }

    private ApplicationInfo getAppInfoLocked(String pkg, int userId) {
        for (int i = 0; i < this.mApplications.size(); i++) {
            ApplicationInfo info = (ApplicationInfo) this.mApplications.get(i);
            if (pkg.equals(info.packageName) && userId == UserHandle.getUserId(info.uid)) {
                return info;
            }
        }
        return null;
    }

    public void ensureIcon(AppEntry entry) {
        if (entry.icon == null) {
            synchronized (entry) {
                entry.ensureIconLocked(this.mContext, this.mDrawableFactory);
            }
        }
    }

    public void requestSize(String packageName, int userId) {
        synchronized (this.mEntriesMap) {
            AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(packageName);
            if (entry != null && hasFlag(entry.info.flags, 8388608)) {
                this.mBackgroundHandler.post(new -$$Lambda$ApplicationsState$LuXUFbWTiS5lu-nO9WUp0g2nHmU(this, entry, packageName, userId));
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0046 A:{ExcHandler: NameNotFoundException | IOException (r0_2 'e' java.lang.Exception), Splitter:B:0:0x0000} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:5:0x0046, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0047, code skipped:
            r1 = TAG;
            r2 = new java.lang.StringBuilder();
            r2.append("Failed to query stats: ");
            r2.append(r0);
            android.util.Log.w(r1, r2.toString());
     */
    /* JADX WARNING: Missing block: B:8:?, code skipped:
            r6.mBackgroundHandler.mStatsObserver.onGetStatsCompleted(null, false);
     */
    /* JADX WARNING: Missing block: B:10:?, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:13:?, code skipped:
            return;
     */
    public static /* synthetic */ void lambda$requestSize$0(com.android.settingslib.applications.ApplicationsState r6, com.android.settingslib.applications.ApplicationsState.AppEntry r7, java.lang.String r8, int r9) {
        /*
        r0 = r6.mStats;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r1 = r7.info;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r1 = r1.storageUuid;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r2 = android.os.UserHandle.of(r9);	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r0 = r0.queryStatsForPackage(r1, r8, r2);	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r1 = r6.mStats;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r2 = r7.info;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r2 = r2.storageUuid;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r2 = r2.toString();	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3 = r7.info;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3 = r3.uid;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r1 = r1.getCacheQuotaBytes(r2, r3);	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3 = new android.content.pm.PackageStats;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3.<init>(r8, r9);	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r4 = r0.getCodeBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3.codeSize = r4;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r4 = r0.getDataBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3.dataSize = r4;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r4 = r0.getCacheBytes();	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r4 = java.lang.Math.min(r4, r1);	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r3.cacheSize = r4;	 Catch:{ NameNotFoundException | IOException -> 0x0046, NameNotFoundException | IOException -> 0x0046 }
        r4 = r6.mBackgroundHandler;	 Catch:{ RemoteException -> 0x0044 }
        r4 = r4.mStatsObserver;	 Catch:{ RemoteException -> 0x0044 }
        r5 = 1;
        r4.onGetStatsCompleted(r3, r5);	 Catch:{ RemoteException -> 0x0044 }
        goto L_0x0045;
    L_0x0044:
        r4 = move-exception;
    L_0x0045:
        goto L_0x0068;
    L_0x0046:
        r0 = move-exception;
        r1 = "ApplicationsState";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Failed to query stats: ";
        r2.append(r3);
        r2.append(r0);
        r2 = r2.toString();
        android.util.Log.w(r1, r2);
        r1 = r6.mBackgroundHandler;	 Catch:{ RemoteException -> 0x0067 }
        r1 = r1.mStatsObserver;	 Catch:{ RemoteException -> 0x0067 }
        r2 = 0;
        r3 = 0;
        r1.onGetStatsCompleted(r2, r3);	 Catch:{ RemoteException -> 0x0067 }
        goto L_0x0068;
    L_0x0067:
        r1 = move-exception;
    L_0x0068:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.applications.ApplicationsState.lambda$requestSize$0(com.android.settingslib.applications.ApplicationsState, com.android.settingslib.applications.ApplicationsState$AppEntry, java.lang.String, int):void");
    }

    /* Access modifiers changed, original: 0000 */
    public long sumCacheSizes() {
        long sum = 0;
        synchronized (this.mEntriesMap) {
            for (int i = this.mAppEntries.size() - 1; i >= 0; i--) {
                sum += ((AppEntry) this.mAppEntries.get(i)).cacheSize;
            }
        }
        return sum;
    }

    /* Access modifiers changed, original: 0000 */
    public int indexOfApplicationInfoLocked(String pkgName, int userId) {
        for (int i = this.mApplications.size() - 1; i >= 0; i--) {
            ApplicationInfo appInfo = (ApplicationInfo) this.mApplications.get(i);
            if (appInfo.packageName.equals(pkgName) && UserHandle.getUserId(appInfo.uid) == userId) {
                return i;
            }
        }
        return -1;
    }

    /* Access modifiers changed, original: 0000 */
    public void addPackage(String pkgName, int userId) {
        try {
            synchronized (this.mEntriesMap) {
                if (!this.mResumed) {
                } else if (indexOfApplicationInfoLocked(pkgName, userId) >= 0) {
                } else {
                    ApplicationInfo info = this.mIpm.getApplicationInfo(pkgName, this.mUm.isUserAdmin(userId) ? this.mAdminRetrieveFlags : this.mRetrieveFlags, userId);
                    if (info == null) {
                        return;
                    }
                    if (!info.enabled) {
                        if (info.enabledSetting != 3) {
                            return;
                        }
                        this.mHaveDisabledApps = true;
                    }
                    if (AppUtils.isInstant(info)) {
                        this.mHaveInstantApps = true;
                    }
                    this.mApplications.add(info);
                    if (!this.mBackgroundHandler.hasMessages(2)) {
                        this.mBackgroundHandler.sendEmptyMessage(2);
                    }
                    if (!this.mMainHandler.hasMessages(2)) {
                        this.mMainHandler.sendEmptyMessage(2);
                    }
                }
            }
        } catch (RemoteException e) {
        }
    }

    public void removePackage(String pkgName, int userId) {
        synchronized (this.mEntriesMap) {
            int idx = indexOfApplicationInfoLocked(pkgName, userId);
            if (idx >= 0) {
                AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(pkgName);
                if (entry != null) {
                    ((HashMap) this.mEntriesMap.get(userId)).remove(pkgName);
                    this.mAppEntries.remove(entry);
                }
                ApplicationInfo info = (ApplicationInfo) this.mApplications.get(idx);
                this.mApplications.remove(idx);
                if (!info.enabled) {
                    this.mHaveDisabledApps = false;
                    for (ApplicationInfo otherInfo : this.mApplications) {
                        if (!otherInfo.enabled) {
                            this.mHaveDisabledApps = true;
                            break;
                        }
                    }
                }
                if (AppUtils.isInstant(info)) {
                    this.mHaveInstantApps = false;
                    for (ApplicationInfo otherInfo2 : this.mApplications) {
                        if (AppUtils.isInstant(otherInfo2)) {
                            this.mHaveInstantApps = true;
                            break;
                        }
                    }
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    public void invalidatePackage(String pkgName, int userId) {
        removePackage(pkgName, userId);
        addPackage(pkgName, userId);
    }

    private void addUser(int userId) {
        if (ArrayUtils.contains(this.mUm.getProfileIdsWithDisabled(UserHandle.myUserId()), userId)) {
            synchronized (this.mEntriesMap) {
                this.mEntriesMap.put(userId, new HashMap());
                if (this.mResumed) {
                    doPauseLocked();
                    doResumeIfNeededLocked();
                }
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    private void removeUser(int userId) {
        synchronized (this.mEntriesMap) {
            HashMap<String, AppEntry> userMap = (HashMap) this.mEntriesMap.get(userId);
            if (userMap != null) {
                for (AppEntry appEntry : userMap.values()) {
                    this.mAppEntries.remove(appEntry);
                    this.mApplications.remove(appEntry.info);
                }
                this.mEntriesMap.remove(userId);
                if (!this.mMainHandler.hasMessages(2)) {
                    this.mMainHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    private AppEntry getEntryLocked(ApplicationInfo info) {
        int userId = UserHandle.getUserId(info.uid);
        AppEntry entry = (AppEntry) ((HashMap) this.mEntriesMap.get(userId)).get(info.packageName);
        if (entry == null) {
            Context context = this.mContext;
            long j = this.mCurId;
            this.mCurId = 1 + j;
            entry = new AppEntry(context, info, j);
            ((HashMap) this.mEntriesMap.get(userId)).put(info.packageName, entry);
            this.mAppEntries.add(entry);
            return entry;
        } else if (entry.info == info) {
            return entry;
        } else {
            entry.info = info;
            return entry;
        }
    }

    private long getTotalInternalSize(PackageStats ps) {
        if (ps != null) {
            return ps.codeSize + ps.dataSize;
        }
        return -2;
    }

    private long getTotalExternalSize(PackageStats ps) {
        if (ps != null) {
            return (((ps.externalCodeSize + ps.externalDataSize) + ps.externalCacheSize) + ps.externalMediaSize) + ps.externalObbSize;
        }
        return -2;
    }

    private String getSizeStr(long size) {
        if (size >= 0) {
            return Utils.formatFileSize(this.mContext, size);
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public void rebuildActiveSessions() {
        synchronized (this.mEntriesMap) {
            if (this.mSessionsChanged) {
                this.mActiveSessions.clear();
                for (int i = 0; i < this.mSessions.size(); i++) {
                    Session s = (Session) this.mSessions.get(i);
                    if (s.mResumed) {
                        this.mActiveSessions.add(s);
                    }
                }
                return;
            }
        }
    }

    public static String normalize(String str) {
        return REMOVE_DIACRITICALS_PATTERN.matcher(Normalizer.normalize(str, Form.NFD)).replaceAll("").toLowerCase();
    }

    private static boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }
}
