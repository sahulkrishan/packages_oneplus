package com.google.analytics.tracking.android;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import com.google.analytics.tracking.android.GAUsage.Field;
import com.google.android.gms.common.util.VisibleForTesting;

public class GAServiceManager extends ServiceManager {
    private static final int MSG_KEY = 1;
    private static final Object MSG_OBJECT = new Object();
    private static GAServiceManager instance;
    private boolean connected = true;
    private Context ctx;
    private int dispatchPeriodInSeconds = 1800;
    private Handler handler;
    private boolean listenForNetwork = true;
    private AnalyticsStoreStateListener listener = new AnalyticsStoreStateListener() {
        public void reportStoreIsEmpty(boolean isEmpty) {
            GAServiceManager.this.updatePowerSaveMode(isEmpty, GAServiceManager.this.connected);
        }
    };
    private GANetworkReceiver networkReceiver;
    private boolean pendingDispatch = true;
    private boolean pendingForceLocalDispatch;
    private String pendingHostOverride;
    private AnalyticsStore store;
    private boolean storeIsEmpty = false;
    private volatile AnalyticsThread thread;

    public static GAServiceManager getInstance() {
        if (instance == null) {
            instance = new GAServiceManager();
        }
        return instance;
    }

    private GAServiceManager() {
    }

    @VisibleForTesting
    static void clearInstance() {
        instance = null;
    }

    @VisibleForTesting
    GAServiceManager(Context ctx, AnalyticsThread thread, AnalyticsStore store, boolean listenForNetwork) {
        this.store = store;
        this.thread = thread;
        this.listenForNetwork = listenForNetwork;
        initialize(ctx, thread);
    }

    private void initializeNetworkReceiver() {
        this.networkReceiver = new GANetworkReceiver(this);
        this.networkReceiver.register(this.ctx);
    }

    private void initializeHandler() {
        this.handler = new Handler(this.ctx.getMainLooper(), new Callback() {
            public boolean handleMessage(Message msg) {
                if (1 == msg.what && GAServiceManager.MSG_OBJECT.equals(msg.obj)) {
                    GAUsage.getInstance().setDisableUsage(true);
                    GAServiceManager.this.dispatchLocalHits();
                    GAUsage.getInstance().setDisableUsage(false);
                    if (GAServiceManager.this.dispatchPeriodInSeconds > 0 && !GAServiceManager.this.storeIsEmpty) {
                        GAServiceManager.this.handler.sendMessageDelayed(GAServiceManager.this.handler.obtainMessage(1, GAServiceManager.MSG_OBJECT), (long) (GAServiceManager.this.dispatchPeriodInSeconds * 1000));
                    }
                }
                return true;
            }
        });
        if (this.dispatchPeriodInSeconds > 0) {
            this.handler.sendMessageDelayed(this.handler.obtainMessage(1, MSG_OBJECT), (long) (this.dispatchPeriodInSeconds * 1000));
        }
    }

    /* Access modifiers changed, original: declared_synchronized */
    /* JADX WARNING: Missing block: B:16:0x0027, code skipped:
            return;
     */
    public synchronized void initialize(android.content.Context r3, com.google.analytics.tracking.android.AnalyticsThread r4) {
        /*
        r2 = this;
        monitor-enter(r2);
        r0 = r2.ctx;	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r2);
        return;
    L_0x0007:
        r0 = r3.getApplicationContext();	 Catch:{ all -> 0x0028 }
        r2.ctx = r0;	 Catch:{ all -> 0x0028 }
        r0 = r2.thread;	 Catch:{ all -> 0x0028 }
        if (r0 != 0) goto L_0x0026;
    L_0x0011:
        r2.thread = r4;	 Catch:{ all -> 0x0028 }
        r0 = r2.pendingDispatch;	 Catch:{ all -> 0x0028 }
        r1 = 0;
        if (r0 == 0) goto L_0x001d;
    L_0x0018:
        r2.dispatchLocalHits();	 Catch:{ all -> 0x0028 }
        r2.pendingDispatch = r1;	 Catch:{ all -> 0x0028 }
    L_0x001d:
        r0 = r2.pendingForceLocalDispatch;	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x0026;
    L_0x0021:
        r2.setForceLocalDispatch();	 Catch:{ all -> 0x0028 }
        r2.pendingForceLocalDispatch = r1;	 Catch:{ all -> 0x0028 }
    L_0x0026:
        monitor-exit(r2);
        return;
    L_0x0028:
        r3 = move-exception;
        monitor-exit(r2);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.GAServiceManager.initialize(android.content.Context, com.google.analytics.tracking.android.AnalyticsThread):void");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public AnalyticsStoreStateListener getListener() {
        return this.listener;
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized AnalyticsStore getStore() {
        if (this.store == null) {
            if (this.ctx != null) {
                this.store = new PersistentAnalyticsStore(this.listener, this.ctx);
                if (this.pendingHostOverride != null) {
                    this.store.getDispatcher().overrideHostUrl(this.pendingHostOverride);
                    this.pendingHostOverride = null;
                }
            } else {
                throw new IllegalStateException("Cant get a store unless we have a context");
            }
        }
        if (this.handler == null) {
            initializeHandler();
        }
        if (this.networkReceiver == null && this.listenForNetwork) {
            initializeNetworkReceiver();
        }
        return this.store;
    }

    /* Access modifiers changed, original: declared_synchronized */
    @VisibleForTesting
    public synchronized void overrideHostUrl(String hostOverride) {
        if (this.store == null) {
            this.pendingHostOverride = hostOverride;
        } else {
            this.store.getDispatcher().overrideHostUrl(hostOverride);
        }
    }

    @Deprecated
    public synchronized void dispatchLocalHits() {
        if (this.thread == null) {
            Log.v("Dispatch call queued. Dispatch will run once initialization is complete.");
            this.pendingDispatch = true;
            return;
        }
        GAUsage.getInstance().setUsage(Field.DISPATCH);
        this.thread.dispatch();
    }

    /* JADX WARNING: Missing block: B:23:0x0048, code skipped:
            return;
     */
    @java.lang.Deprecated
    public synchronized void setLocalDispatchPeriod(int r5) {
        /*
        r4 = this;
        monitor-enter(r4);
        r0 = r4.handler;	 Catch:{ all -> 0x0049 }
        if (r0 != 0) goto L_0x000e;
    L_0x0005:
        r0 = "Dispatch period set with null handler. Dispatch will run once initialization is complete.";
        com.google.analytics.tracking.android.Log.v(r0);	 Catch:{ all -> 0x0049 }
        r4.dispatchPeriodInSeconds = r5;	 Catch:{ all -> 0x0049 }
        monitor-exit(r4);
        return;
    L_0x000e:
        r0 = com.google.analytics.tracking.android.GAUsage.getInstance();	 Catch:{ all -> 0x0049 }
        r1 = com.google.analytics.tracking.android.GAUsage.Field.SET_DISPATCH_PERIOD;	 Catch:{ all -> 0x0049 }
        r0.setUsage(r1);	 Catch:{ all -> 0x0049 }
        r0 = r4.storeIsEmpty;	 Catch:{ all -> 0x0049 }
        r1 = 1;
        if (r0 != 0) goto L_0x002b;
    L_0x001c:
        r0 = r4.connected;	 Catch:{ all -> 0x0049 }
        if (r0 == 0) goto L_0x002b;
    L_0x0020:
        r0 = r4.dispatchPeriodInSeconds;	 Catch:{ all -> 0x0049 }
        if (r0 <= 0) goto L_0x002b;
    L_0x0024:
        r0 = r4.handler;	 Catch:{ all -> 0x0049 }
        r2 = MSG_OBJECT;	 Catch:{ all -> 0x0049 }
        r0.removeMessages(r1, r2);	 Catch:{ all -> 0x0049 }
    L_0x002b:
        r4.dispatchPeriodInSeconds = r5;	 Catch:{ all -> 0x0049 }
        if (r5 <= 0) goto L_0x0047;
    L_0x002f:
        r0 = r4.storeIsEmpty;	 Catch:{ all -> 0x0049 }
        if (r0 != 0) goto L_0x0047;
    L_0x0033:
        r0 = r4.connected;	 Catch:{ all -> 0x0049 }
        if (r0 == 0) goto L_0x0047;
    L_0x0037:
        r0 = r4.handler;	 Catch:{ all -> 0x0049 }
        r2 = r4.handler;	 Catch:{ all -> 0x0049 }
        r3 = MSG_OBJECT;	 Catch:{ all -> 0x0049 }
        r1 = r2.obtainMessage(r1, r3);	 Catch:{ all -> 0x0049 }
        r2 = r5 * 1000;
        r2 = (long) r2;	 Catch:{ all -> 0x0049 }
        r0.sendMessageDelayed(r1, r2);	 Catch:{ all -> 0x0049 }
    L_0x0047:
        monitor-exit(r4);
        return;
    L_0x0049:
        r5 = move-exception;
        monitor-exit(r4);
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.GAServiceManager.setLocalDispatchPeriod(int):void");
    }

    @Deprecated
    public void setForceLocalDispatch() {
        if (this.thread == null) {
            Log.v("setForceLocalDispatch() queued. It will be called once initialization is complete.");
            this.pendingForceLocalDispatch = true;
            return;
        }
        GAUsage.getInstance().setUsage(Field.SET_FORCE_LOCAL_DISPATCH);
        this.thread.setForceLocalDispatch();
    }

    /* Access modifiers changed, original: declared_synchronized */
    @VisibleForTesting
    public synchronized void updatePowerSaveMode(boolean storeIsEmpty, boolean connected) {
        if (this.storeIsEmpty != storeIsEmpty || this.connected != connected) {
            String str;
            if (storeIsEmpty || !connected) {
                if (this.dispatchPeriodInSeconds > 0) {
                    this.handler.removeMessages(1, MSG_OBJECT);
                }
            }
            if (!storeIsEmpty && connected && this.dispatchPeriodInSeconds > 0) {
                this.handler.sendMessageDelayed(this.handler.obtainMessage(1, MSG_OBJECT), (long) (this.dispatchPeriodInSeconds * 1000));
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PowerSaveMode ");
            if (!storeIsEmpty) {
                if (connected) {
                    str = "terminated.";
                    stringBuilder.append(str);
                    Log.v(stringBuilder.toString());
                    this.storeIsEmpty = storeIsEmpty;
                    this.connected = connected;
                }
            }
            str = "initiated.";
            stringBuilder.append(str);
            Log.v(stringBuilder.toString());
            this.storeIsEmpty = storeIsEmpty;
            this.connected = connected;
        }
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized void updateConnectivityStatus(boolean connected) {
        updatePowerSaveMode(this.storeIsEmpty, connected);
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized void onRadioPowered() {
        if (!this.storeIsEmpty && this.connected && this.dispatchPeriodInSeconds > 0) {
            this.handler.removeMessages(1, MSG_OBJECT);
            this.handler.sendMessage(this.handler.obtainMessage(1, MSG_OBJECT));
        }
    }
}
