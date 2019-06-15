package com.oneplus.settings.highpowerapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HighPowerAppModel implements OnClickListener {
    private static final String TAG = "HighPowerAppModel";
    private static final boolean USE_FAKE_DATA = false;
    private IMyActivityManager mActivityManager;
    private List<HighPowerApp> mAppList = new ArrayList();
    private SoftReference<Context> mContext;
    private List<PackageInfo> mData = new ArrayList();
    private List<IHighPowerAppObserver> mDataObserverList = new LinkedList();
    private Object mDataObserverListLock = new Byte[0];
    private Map<String, PackageInfo> mInstalledPackages = new ConcurrentHashMap();
    private PackageManager mPackageManager;
    private ExecutorService mThreadPool = Executors.newCachedThreadPool();

    public HighPowerAppModel(Context ctx, IHighPowerAppObserver observer) {
        this.mContext = new SoftReference(ctx);
        this.mActivityManager = MyActivityManager.get(ctx);
        registerObserver(observer);
        initialize();
    }

    public void initialize() {
        this.mThreadPool.execute(new Runnable() {
            public void run() {
                HighPowerAppModel.this.process();
            }
        });
    }

    public void update() {
        this.mThreadPool.execute(new Runnable() {
            public void run() {
                HighPowerAppModel.this.process();
            }
        });
    }

    private void process() {
        List<HighPowerApp> list = this.mActivityManager.getBgPowerHungryList();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HighPowerAppModel getBgPowerHungryList: ");
        stringBuilder.append(list);
        Log.d(str, stringBuilder.toString());
        for (HighPowerApp app : list) {
            if (this.mInstalledPackages.containsKey(app.pkgName)) {
                app.uid = ((PackageInfo) this.mInstalledPackages.get(app.pkgName)).applicationInfo.uid;
            } else {
                if (this.mPackageManager == null) {
                    this.mPackageManager = getContext().getPackageManager();
                }
                try {
                    PackageInfo info = this.mPackageManager.getPackageInfo(app.pkgName, 0);
                    this.mInstalledPackages.put(info.packageName, info);
                    app.uid = info.applicationInfo.uid;
                } catch (Exception e) {
                }
            }
        }
        Collections.sort(list, new Comparator<HighPowerApp>() {
            public int compare(HighPowerApp o1, HighPowerApp o2) {
                return o1.pkgName.hashCode() - o2.pkgName.hashCode();
            }
        });
        this.mAppList = list;
        notifyDataChanged();
    }

    public void stopApp(String pkg) {
        for (HighPowerApp app : this.mAppList) {
            if (app.pkgName.equals(pkg)) {
                this.mActivityManager.stopBgPowerHungryApp(pkg, app.powerLevel);
                return;
            }
        }
    }

    public List<HighPowerApp> getDataList() {
        return this.mAppList;
    }

    private List<HighPowerApp> makeFakeList() {
        List<HighPowerApp> list = new ArrayList();
        for (int i = 0; i < this.mData.size(); i++) {
            PackageInfo info = (PackageInfo) this.mData.get(i);
            String pkg = info.packageName;
            int uid = info.applicationInfo.uid;
            boolean locked = false;
            boolean stoped = false;
            int level = 0;
            switch (i % 3) {
                case 0:
                    stoped = true;
                    level = 1;
                    break;
                case 1:
                    locked = true;
                    level = 0;
                    break;
                case 2:
                    level = -1;
                    break;
            }
            boolean stoped2 = stoped;
            int level2 = level;
            list.add(new HighPowerApp(pkg, uid, level2, locked, stoped2, System.currentTimeMillis()));
        }
        return list;
    }

    public void onClick(View v) {
        Switch s = (Switch) v;
        this.mActivityManager.setBgMonitorMode(s.isChecked());
        if (s.isChecked()) {
            update();
        }
    }

    public boolean getBgMonitorMode() {
        return this.mActivityManager.getBgMonitorMode();
    }

    public void updateInstalledPackages() {
        this.mPackageManager = getContext().getPackageManager();
        this.mInstalledPackages.clear();
        try {
            List<PackageInfo> packages = this.mPackageManager.getInstalledPackages(2);
            if (!packages.isEmpty()) {
                for (PackageInfo info : packages) {
                    if (!PackageUtils.isSystemApplication(getContext(), info.packageName)) {
                        this.mInstalledPackages.put(info.packageName, info);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    public void registerObserver(IHighPowerAppObserver observer) {
        synchronized (this.mDataObserverListLock) {
            if (!this.mDataObserverList.contains(observer)) {
                this.mDataObserverList.add(observer);
            }
        }
    }

    public void unregisterObserver(IHighPowerAppObserver observer) {
        synchronized (this.mDataObserverListLock) {
            if (this.mDataObserverList.contains(observer)) {
                this.mDataObserverList.remove(observer);
            }
        }
    }

    private void notifyDataChanged() {
        if (this.mDataObserverList != null) {
            synchronized (this.mDataObserverListLock) {
                for (IHighPowerAppObserver observer : this.mDataObserverList) {
                    if (observer != null) {
                        observer.OnDataChanged();
                    }
                }
            }
        }
    }

    public void releaseResource() {
        if (this.mThreadPool != null) {
            this.mThreadPool.shutdown();
            this.mThreadPool = null;
        }
    }

    private Context getContext() {
        return (Context) this.mContext.get();
    }
}
