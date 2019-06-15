package com.android.settings.applications;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public abstract class AppStateBaseBridge implements Callbacks {
    protected final Session mAppSession;
    protected final ApplicationsState mAppState;
    protected final Callback mCallback;
    protected final BackgroundHandler mHandler;
    protected final MainHandler mMainHandler;

    private class BackgroundHandler extends Handler {
        private static final int MSG_FORCE_LOAD_PKG = 2;
        private static final int MSG_LOAD_ALL = 1;

        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AppStateBaseBridge.this.loadAllExtraInfo();
                    AppStateBaseBridge.this.mMainHandler.sendEmptyMessage(1);
                    return;
                case 2:
                    ArrayList<AppEntry> apps = AppStateBaseBridge.this.mAppSession.getAllApps();
                    int N = apps.size();
                    String pkg = msg.obj;
                    int uid = msg.arg1;
                    for (int i = 0; i < N; i++) {
                        AppEntry app = (AppEntry) apps.get(i);
                        if (app.info.uid == uid && pkg.equals(app.info.packageName)) {
                            AppStateBaseBridge.this.updateExtraInfo(app, pkg, uid);
                        }
                    }
                    AppStateBaseBridge.this.mMainHandler.sendEmptyMessage(1);
                    return;
                default:
                    return;
            }
        }
    }

    public interface Callback {
        void onExtraInfoUpdated();
    }

    private class MainHandler extends Handler {
        private static final int MSG_INFO_UPDATED = 1;

        private MainHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                AppStateBaseBridge.this.mCallback.onExtraInfoUpdated();
            }
        }
    }

    public abstract void loadAllExtraInfo();

    public abstract void updateExtraInfo(AppEntry appEntry, String str, int i);

    public AppStateBaseBridge(ApplicationsState appState, Callback callback) {
        Looper backgroundLooper;
        this.mAppState = appState;
        this.mAppSession = this.mAppState != null ? this.mAppState.newSession(this) : null;
        this.mCallback = callback;
        if (this.mAppState != null) {
            backgroundLooper = this.mAppState.getBackgroundLooper();
        } else {
            backgroundLooper = Looper.getMainLooper();
        }
        this.mHandler = new BackgroundHandler(backgroundLooper);
        this.mMainHandler = new MainHandler();
    }

    public void resume() {
        this.mHandler.sendEmptyMessage(1);
        this.mAppSession.onResume();
    }

    public void pause() {
        this.mAppSession.onPause();
    }

    public void release() {
        this.mAppSession.onDestroy();
    }

    public void forceUpdate(String pkg, int uid) {
        this.mHandler.obtainMessage(2, uid, 0, pkg).sendToTarget();
    }

    public void onPackageListChanged() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onLoadEntriesCompleted() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }
}
