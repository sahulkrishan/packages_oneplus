package com.google.tagmanager;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import com.google.android.gms.common.util.VisibleForTesting;

class ServiceManagerImpl extends ServiceManager {
    private static final int MSG_KEY = 1;
    private static final Object MSG_OBJECT = new Object();
    private static ServiceManagerImpl instance;
    private boolean connected = true;
    private Context ctx;
    private int dispatchPeriodInSeconds = 1800;
    private Handler handler;
    private boolean listenForNetwork = true;
    private HitStoreStateListener listener = new HitStoreStateListener() {
        public void reportStoreIsEmpty(boolean isEmpty) {
            ServiceManagerImpl.this.updatePowerSaveMode(isEmpty, ServiceManagerImpl.this.connected);
        }
    };
    private NetworkReceiver networkReceiver;
    private boolean pendingDispatch = true;
    private boolean readyToDispatch = false;
    private HitStore store;
    private boolean storeIsEmpty = false;
    private volatile HitSendingThread thread;

    public static ServiceManagerImpl getInstance() {
        if (instance == null) {
            instance = new ServiceManagerImpl();
        }
        return instance;
    }

    private ServiceManagerImpl() {
    }

    @VisibleForTesting
    static void clearInstance() {
        instance = null;
    }

    @VisibleForTesting
    ServiceManagerImpl(Context ctx, HitSendingThread thread, HitStore store, boolean listenForNetwork) {
        this.store = store;
        this.thread = thread;
        this.listenForNetwork = listenForNetwork;
        initialize(ctx, thread);
    }

    private void initializeNetworkReceiver() {
        this.networkReceiver = new NetworkReceiver(this);
        this.networkReceiver.register(this.ctx);
    }

    private void initializeHandler() {
        this.handler = new Handler(this.ctx.getMainLooper(), new Callback() {
            public boolean handleMessage(Message msg) {
                if (1 == msg.what && ServiceManagerImpl.MSG_OBJECT.equals(msg.obj)) {
                    ServiceManagerImpl.this.dispatch();
                    if (ServiceManagerImpl.this.dispatchPeriodInSeconds > 0 && !ServiceManagerImpl.this.storeIsEmpty) {
                        ServiceManagerImpl.this.handler.sendMessageDelayed(ServiceManagerImpl.this.handler.obtainMessage(1, ServiceManagerImpl.MSG_OBJECT), (long) (ServiceManagerImpl.this.dispatchPeriodInSeconds * 1000));
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
    /* JADX WARNING: Missing block: B:11:0x0014, code skipped:
            return;
     */
    public synchronized void initialize(android.content.Context r2, com.google.tagmanager.HitSendingThread r3) {
        /*
        r1 = this;
        monitor-enter(r1);
        r0 = r1.ctx;	 Catch:{ all -> 0x0015 }
        if (r0 == 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r1);
        return;
    L_0x0007:
        r0 = r2.getApplicationContext();	 Catch:{ all -> 0x0015 }
        r1.ctx = r0;	 Catch:{ all -> 0x0015 }
        r0 = r1.thread;	 Catch:{ all -> 0x0015 }
        if (r0 != 0) goto L_0x0013;
    L_0x0011:
        r1.thread = r3;	 Catch:{ all -> 0x0015 }
    L_0x0013:
        monitor-exit(r1);
        return;
    L_0x0015:
        r2 = move-exception;
        monitor-exit(r1);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.ServiceManagerImpl.initialize(android.content.Context, com.google.tagmanager.HitSendingThread):void");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public HitStoreStateListener getListener() {
        return this.listener;
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized HitStore getStore() {
        if (this.store == null) {
            if (this.ctx != null) {
                this.store = new PersistentHitStore(this.listener, this.ctx);
            } else {
                throw new IllegalStateException("Cant get a store unless we have a context");
            }
        }
        if (this.handler == null) {
            initializeHandler();
        }
        this.readyToDispatch = true;
        if (this.pendingDispatch) {
            dispatch();
            this.pendingDispatch = false;
        }
        if (this.networkReceiver == null && this.listenForNetwork) {
            initializeNetworkReceiver();
        }
        return this.store;
    }

    public synchronized void dispatch() {
        if (this.readyToDispatch) {
            this.thread.queueToThread(new Runnable() {
                public void run() {
                    ServiceManagerImpl.this.store.dispatch();
                }
            });
            return;
        }
        Log.v("Dispatch call queued. Dispatch will run once initialization is complete.");
        this.pendingDispatch = true;
    }

    /* JADX WARNING: Missing block: B:23:0x003f, code skipped:
            return;
     */
    public synchronized void setDispatchPeriod(int r5) {
        /*
        r4 = this;
        monitor-enter(r4);
        r0 = r4.handler;	 Catch:{ all -> 0x0040 }
        if (r0 != 0) goto L_0x000e;
    L_0x0005:
        r0 = "Dispatch period set with null handler. Dispatch will run once initialization is complete.";
        com.google.tagmanager.Log.v(r0);	 Catch:{ all -> 0x0040 }
        r4.dispatchPeriodInSeconds = r5;	 Catch:{ all -> 0x0040 }
        monitor-exit(r4);
        return;
    L_0x000e:
        r0 = r4.storeIsEmpty;	 Catch:{ all -> 0x0040 }
        r1 = 1;
        if (r0 != 0) goto L_0x0022;
    L_0x0013:
        r0 = r4.connected;	 Catch:{ all -> 0x0040 }
        if (r0 == 0) goto L_0x0022;
    L_0x0017:
        r0 = r4.dispatchPeriodInSeconds;	 Catch:{ all -> 0x0040 }
        if (r0 <= 0) goto L_0x0022;
    L_0x001b:
        r0 = r4.handler;	 Catch:{ all -> 0x0040 }
        r2 = MSG_OBJECT;	 Catch:{ all -> 0x0040 }
        r0.removeMessages(r1, r2);	 Catch:{ all -> 0x0040 }
    L_0x0022:
        r4.dispatchPeriodInSeconds = r5;	 Catch:{ all -> 0x0040 }
        if (r5 <= 0) goto L_0x003e;
    L_0x0026:
        r0 = r4.storeIsEmpty;	 Catch:{ all -> 0x0040 }
        if (r0 != 0) goto L_0x003e;
    L_0x002a:
        r0 = r4.connected;	 Catch:{ all -> 0x0040 }
        if (r0 == 0) goto L_0x003e;
    L_0x002e:
        r0 = r4.handler;	 Catch:{ all -> 0x0040 }
        r2 = r4.handler;	 Catch:{ all -> 0x0040 }
        r3 = MSG_OBJECT;	 Catch:{ all -> 0x0040 }
        r1 = r2.obtainMessage(r1, r3);	 Catch:{ all -> 0x0040 }
        r2 = r5 * 1000;
        r2 = (long) r2;	 Catch:{ all -> 0x0040 }
        r0.sendMessageDelayed(r1, r2);	 Catch:{ all -> 0x0040 }
    L_0x003e:
        monitor-exit(r4);
        return;
    L_0x0040:
        r5 = move-exception;
        monitor-exit(r4);
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.ServiceManagerImpl.setDispatchPeriod(int):void");
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
