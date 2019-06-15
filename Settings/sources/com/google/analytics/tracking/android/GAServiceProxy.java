package com.google.analytics.tracking.android;

import android.content.Context;
import android.content.Intent;
import com.google.analytics.tracking.android.AnalyticsGmsCoreClient.OnConnectedListener;
import com.google.analytics.tracking.android.AnalyticsGmsCoreClient.OnConnectionFailedListener;
import com.google.android.gms.analytics.internal.Command;
import com.google.android.gms.common.util.VisibleForTesting;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

class GAServiceProxy implements ServiceProxy, OnConnectedListener, OnConnectionFailedListener {
    private static final long FAILED_CONNECT_WAIT_TIME = 3000;
    private static final int MAX_TRIES = 2;
    private static final long RECONNECT_WAIT_TIME = 5000;
    private static final long SERVICE_CONNECTION_TIMEOUT = 300000;
    private volatile AnalyticsClient client;
    private Clock clock;
    private volatile int connectTries;
    private final Context ctx;
    private volatile Timer disconnectCheckTimer;
    private volatile Timer failedConnectTimer;
    private boolean forceLocalDispatch;
    private final GoogleAnalytics gaInstance;
    private long idleTimeout;
    private volatile long lastRequestTime;
    private boolean pendingClearHits;
    private boolean pendingDispatch;
    private boolean pendingServiceDisconnect;
    private final Queue<HitParams> queue;
    private volatile Timer reConnectTimer;
    private volatile ConnectState state;
    private AnalyticsStore store;
    private AnalyticsStore testStore;
    private final AnalyticsThread thread;

    private enum ConnectState {
        CONNECTING,
        CONNECTED_SERVICE,
        CONNECTED_LOCAL,
        BLOCKED,
        PENDING_CONNECTION,
        PENDING_DISCONNECT,
        DISCONNECTED
    }

    private class DisconnectCheckTask extends TimerTask {
        private DisconnectCheckTask() {
        }

        /* synthetic */ DisconnectCheckTask(GAServiceProxy x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            if (GAServiceProxy.this.state == ConnectState.CONNECTED_SERVICE && GAServiceProxy.this.queue.isEmpty() && GAServiceProxy.this.lastRequestTime + GAServiceProxy.this.idleTimeout < GAServiceProxy.this.clock.currentTimeMillis()) {
                Log.v("Disconnecting due to inactivity");
                GAServiceProxy.this.disconnectFromService();
                return;
            }
            GAServiceProxy.this.disconnectCheckTimer.schedule(new DisconnectCheckTask(), GAServiceProxy.this.idleTimeout);
        }
    }

    private class FailedConnectTask extends TimerTask {
        private FailedConnectTask() {
        }

        /* synthetic */ FailedConnectTask(GAServiceProxy x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            if (GAServiceProxy.this.state == ConnectState.CONNECTING) {
                GAServiceProxy.this.useStore();
            }
        }
    }

    private static class HitParams {
        private final List<Command> commands;
        private final long hitTimeInMilliseconds;
        private final String path;
        private final Map<String, String> wireFormatParams;

        public HitParams(Map<String, String> wireFormatParams, long hitTimeInMilliseconds, String path, List<Command> commands) {
            this.wireFormatParams = wireFormatParams;
            this.hitTimeInMilliseconds = hitTimeInMilliseconds;
            this.path = path;
            this.commands = commands;
        }

        public Map<String, String> getWireFormatParams() {
            return this.wireFormatParams;
        }

        public long getHitTimeInMilliseconds() {
            return this.hitTimeInMilliseconds;
        }

        public String getPath() {
            return this.path;
        }

        public List<Command> getCommands() {
            return this.commands;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PATH: ");
            sb.append(this.path);
            if (this.wireFormatParams != null) {
                sb.append("  PARAMS: ");
                for (Entry<String, String> entry : this.wireFormatParams.entrySet()) {
                    sb.append((String) entry.getKey());
                    sb.append("=");
                    sb.append((String) entry.getValue());
                    sb.append(",  ");
                }
            }
            return sb.toString();
        }
    }

    private class ReconnectTask extends TimerTask {
        private ReconnectTask() {
        }

        /* synthetic */ ReconnectTask(GAServiceProxy x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            GAServiceProxy.this.connectToService();
        }
    }

    @VisibleForTesting
    GAServiceProxy(Context ctx, AnalyticsThread thread, AnalyticsStore store, GoogleAnalytics gaInstance) {
        this.queue = new ConcurrentLinkedQueue();
        this.idleTimeout = SERVICE_CONNECTION_TIMEOUT;
        this.testStore = store;
        this.ctx = ctx;
        this.thread = thread;
        this.gaInstance = gaInstance;
        this.clock = new Clock() {
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        };
        this.connectTries = 0;
        this.state = ConnectState.DISCONNECTED;
    }

    GAServiceProxy(Context ctx, AnalyticsThread thread) {
        this(ctx, thread, null, GoogleAnalytics.getInstance(ctx));
    }

    /* Access modifiers changed, original: 0000 */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void putHit(Map<String, String> wireFormatParams, long hitTimeInMilliseconds, String path, List<Command> commands) {
        Log.v("putHit called");
        this.queue.add(new HitParams(wireFormatParams, hitTimeInMilliseconds, path, commands));
        sendQueue();
    }

    public void dispatch() {
        switch (this.state) {
            case CONNECTED_LOCAL:
                dispatchToStore();
                return;
            case CONNECTED_SERVICE:
                return;
            default:
                this.pendingDispatch = true;
                return;
        }
    }

    public void clearHits() {
        Log.v("clearHits called");
        this.queue.clear();
        switch (this.state) {
            case CONNECTED_LOCAL:
                this.store.clearHits(0);
                this.pendingClearHits = false;
                return;
            case CONNECTED_SERVICE:
                this.client.clearHits();
                this.pendingClearHits = false;
                return;
            default:
                this.pendingClearHits = true;
                return;
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0029, code skipped:
            return;
     */
    public synchronized void setForceLocalDispatch() {
        /*
        r3 = this;
        monitor-enter(r3);
        r0 = r3.forceLocalDispatch;	 Catch:{ all -> 0x002a }
        if (r0 == 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r3);
        return;
    L_0x0007:
        r0 = "setForceLocalDispatch called.";
        com.google.analytics.tracking.android.Log.v(r0);	 Catch:{ all -> 0x002a }
        r0 = 1;
        r3.forceLocalDispatch = r0;	 Catch:{ all -> 0x002a }
        r1 = com.google.analytics.tracking.android.GAServiceProxy.AnonymousClass3.$SwitchMap$com$google$analytics$tracking$android$GAServiceProxy$ConnectState;	 Catch:{ all -> 0x002a }
        r2 = r3.state;	 Catch:{ all -> 0x002a }
        r2 = r2.ordinal();	 Catch:{ all -> 0x002a }
        r1 = r1[r2];	 Catch:{ all -> 0x002a }
        switch(r1) {
            case 1: goto L_0x0027;
            case 2: goto L_0x0023;
            case 3: goto L_0x0020;
            case 4: goto L_0x001f;
            case 5: goto L_0x001e;
            case 6: goto L_0x001d;
            default: goto L_0x001c;
        };	 Catch:{ all -> 0x002a }
    L_0x001c:
        goto L_0x0028;
    L_0x001d:
        goto L_0x0028;
    L_0x001e:
        goto L_0x0028;
    L_0x001f:
        goto L_0x0028;
    L_0x0020:
        r3.pendingServiceDisconnect = r0;	 Catch:{ all -> 0x002a }
        goto L_0x0028;
    L_0x0023:
        r3.disconnectFromService();	 Catch:{ all -> 0x002a }
        goto L_0x0028;
    L_0x0028:
        monitor-exit(r3);
        return;
    L_0x002a:
        r0 = move-exception;
        monitor-exit(r3);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.GAServiceProxy.setForceLocalDispatch():void");
    }

    private Timer cancelTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
        }
        return null;
    }

    private void clearAllTimers() {
        this.reConnectTimer = cancelTimer(this.reConnectTimer);
        this.failedConnectTimer = cancelTimer(this.failedConnectTimer);
        this.disconnectCheckTimer = cancelTimer(this.disconnectCheckTimer);
    }

    public void createService() {
        if (this.client == null) {
            this.client = new AnalyticsGmsCoreClient(this.ctx, this, this);
            connectToService();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void createService(AnalyticsClient client) {
        if (this.client == null) {
            this.client = client;
            connectToService();
        }
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /* JADX WARNING: Missing block: B:22:0x0087, code skipped:
            r8.lastRequestTime = r8.clock.currentTimeMillis();
     */
    /* JADX WARNING: Missing block: B:27:0x00cc, code skipped:
            if (r8.pendingDispatch == false) goto L_0x00e2;
     */
    /* JADX WARNING: Missing block: B:28:0x00ce, code skipped:
            dispatchToStore();
     */
    /* JADX WARNING: Missing block: B:33:0x00e3, code skipped:
            return;
     */
    private synchronized void sendQueue() {
        /*
        r8 = this;
        monitor-enter(r8);
        r0 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x00e4 }
        r1 = r8.thread;	 Catch:{ all -> 0x00e4 }
        r1 = r1.getThread();	 Catch:{ all -> 0x00e4 }
        r0 = r0.equals(r1);	 Catch:{ all -> 0x00e4 }
        if (r0 != 0) goto L_0x0021;
    L_0x0011:
        r0 = r8.thread;	 Catch:{ all -> 0x00e4 }
        r0 = r0.getQueue();	 Catch:{ all -> 0x00e4 }
        r1 = new com.google.analytics.tracking.android.GAServiceProxy$2;	 Catch:{ all -> 0x00e4 }
        r1.<init>();	 Catch:{ all -> 0x00e4 }
        r0.add(r1);	 Catch:{ all -> 0x00e4 }
        monitor-exit(r8);
        return;
    L_0x0021:
        r0 = r8.pendingClearHits;	 Catch:{ all -> 0x00e4 }
        if (r0 == 0) goto L_0x0028;
    L_0x0025:
        r8.clearHits();	 Catch:{ all -> 0x00e4 }
    L_0x0028:
        r0 = com.google.analytics.tracking.android.GAServiceProxy.AnonymousClass3.$SwitchMap$com$google$analytics$tracking$android$GAServiceProxy$ConnectState;	 Catch:{ all -> 0x00e4 }
        r1 = r8.state;	 Catch:{ all -> 0x00e4 }
        r1 = r1.ordinal();	 Catch:{ all -> 0x00e4 }
        r0 = r0[r1];	 Catch:{ all -> 0x00e4 }
        r1 = 6;
        if (r0 == r1) goto L_0x00d2;
    L_0x0035:
        switch(r0) {
            case 1: goto L_0x0090;
            case 2: goto L_0x003a;
            default: goto L_0x0038;
        };	 Catch:{ all -> 0x00e4 }
    L_0x0038:
        goto L_0x00e2;
    L_0x003a:
        r0 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x00e4 }
        if (r0 != 0) goto L_0x0087;
    L_0x0042:
        r0 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r0 = r0.peek();	 Catch:{ all -> 0x00e4 }
        r0 = (com.google.analytics.tracking.android.GAServiceProxy.HitParams) r0;	 Catch:{ all -> 0x00e4 }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00e4 }
        r1.<init>();	 Catch:{ all -> 0x00e4 }
        r2 = "Sending hit to service   ";
        r1.append(r2);	 Catch:{ all -> 0x00e4 }
        r1.append(r0);	 Catch:{ all -> 0x00e4 }
        r1 = r1.toString();	 Catch:{ all -> 0x00e4 }
        com.google.analytics.tracking.android.Log.v(r1);	 Catch:{ all -> 0x00e4 }
        r1 = r8.gaInstance;	 Catch:{ all -> 0x00e4 }
        r1 = r1.isDryRunEnabled();	 Catch:{ all -> 0x00e4 }
        if (r1 != 0) goto L_0x007c;
    L_0x0066:
        r2 = r8.client;	 Catch:{ all -> 0x00e4 }
        r3 = r0.getWireFormatParams();	 Catch:{ all -> 0x00e4 }
        r4 = r0.getHitTimeInMilliseconds();	 Catch:{ all -> 0x00e4 }
        r6 = r0.getPath();	 Catch:{ all -> 0x00e4 }
        r7 = r0.getCommands();	 Catch:{ all -> 0x00e4 }
        r2.sendHit(r3, r4, r6, r7);	 Catch:{ all -> 0x00e4 }
        goto L_0x0081;
    L_0x007c:
        r1 = "Dry run enabled. Hit not actually sent to service.";
        com.google.analytics.tracking.android.Log.v(r1);	 Catch:{ all -> 0x00e4 }
    L_0x0081:
        r1 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r1.poll();	 Catch:{ all -> 0x00e4 }
        goto L_0x003a;
    L_0x0087:
        r0 = r8.clock;	 Catch:{ all -> 0x00e4 }
        r0 = r0.currentTimeMillis();	 Catch:{ all -> 0x00e4 }
        r8.lastRequestTime = r0;	 Catch:{ all -> 0x00e4 }
        goto L_0x00e2;
    L_0x0090:
        r0 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x00e4 }
        if (r0 != 0) goto L_0x00ca;
    L_0x0098:
        r0 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r0 = r0.poll();	 Catch:{ all -> 0x00e4 }
        r0 = (com.google.analytics.tracking.android.GAServiceProxy.HitParams) r0;	 Catch:{ all -> 0x00e4 }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00e4 }
        r1.<init>();	 Catch:{ all -> 0x00e4 }
        r2 = "Sending hit to store  ";
        r1.append(r2);	 Catch:{ all -> 0x00e4 }
        r1.append(r0);	 Catch:{ all -> 0x00e4 }
        r1 = r1.toString();	 Catch:{ all -> 0x00e4 }
        com.google.analytics.tracking.android.Log.v(r1);	 Catch:{ all -> 0x00e4 }
        r2 = r8.store;	 Catch:{ all -> 0x00e4 }
        r3 = r0.getWireFormatParams();	 Catch:{ all -> 0x00e4 }
        r4 = r0.getHitTimeInMilliseconds();	 Catch:{ all -> 0x00e4 }
        r6 = r0.getPath();	 Catch:{ all -> 0x00e4 }
        r7 = r0.getCommands();	 Catch:{ all -> 0x00e4 }
        r2.putHit(r3, r4, r6, r7);	 Catch:{ all -> 0x00e4 }
        goto L_0x0090;
    L_0x00ca:
        r0 = r8.pendingDispatch;	 Catch:{ all -> 0x00e4 }
        if (r0 == 0) goto L_0x00e2;
    L_0x00ce:
        r8.dispatchToStore();	 Catch:{ all -> 0x00e4 }
        goto L_0x00e2;
    L_0x00d2:
        r0 = "Need to reconnect";
        com.google.analytics.tracking.android.Log.v(r0);	 Catch:{ all -> 0x00e4 }
        r0 = r8.queue;	 Catch:{ all -> 0x00e4 }
        r0 = r0.isEmpty();	 Catch:{ all -> 0x00e4 }
        if (r0 != 0) goto L_0x00e2;
    L_0x00df:
        r8.connectToService();	 Catch:{ all -> 0x00e4 }
    L_0x00e2:
        monitor-exit(r8);
        return;
    L_0x00e4:
        r0 = move-exception;
        monitor-exit(r8);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.analytics.tracking.android.GAServiceProxy.sendQueue():void");
    }

    private void dispatchToStore() {
        this.store.dispatch();
        this.pendingDispatch = false;
    }

    private synchronized void useStore() {
        if (this.state != ConnectState.CONNECTED_LOCAL) {
            clearAllTimers();
            Log.v("falling back to local store");
            if (this.testStore != null) {
                this.store = this.testStore;
            } else {
                GAServiceManager instance = GAServiceManager.getInstance();
                instance.initialize(this.ctx, this.thread);
                this.store = instance.getStore();
            }
            this.state = ConnectState.CONNECTED_LOCAL;
            sendQueue();
        }
    }

    private synchronized void connectToService() {
        if (this.forceLocalDispatch || this.client == null || this.state == ConnectState.CONNECTED_LOCAL) {
            Log.w("client not initialized.");
            useStore();
        } else {
            try {
                this.connectTries++;
                cancelTimer(this.failedConnectTimer);
                this.state = ConnectState.CONNECTING;
                this.failedConnectTimer = new Timer("Failed Connect");
                this.failedConnectTimer.schedule(new FailedConnectTask(this, null), FAILED_CONNECT_WAIT_TIME);
                Log.v("connecting to Analytics service");
                this.client.connect();
            } catch (SecurityException e) {
                Log.w("security exception on connectToService");
                useStore();
            }
        }
    }

    private synchronized void disconnectFromService() {
        if (this.client != null && this.state == ConnectState.CONNECTED_SERVICE) {
            this.state = ConnectState.PENDING_DISCONNECT;
            this.client.disconnect();
        }
    }

    public synchronized void onConnected() {
        this.failedConnectTimer = cancelTimer(this.failedConnectTimer);
        this.connectTries = 0;
        Log.v("Connected to service");
        this.state = ConnectState.CONNECTED_SERVICE;
        if (this.pendingServiceDisconnect) {
            disconnectFromService();
            this.pendingServiceDisconnect = false;
            return;
        }
        sendQueue();
        this.disconnectCheckTimer = cancelTimer(this.disconnectCheckTimer);
        this.disconnectCheckTimer = new Timer("disconnect check");
        this.disconnectCheckTimer.schedule(new DisconnectCheckTask(this, null), this.idleTimeout);
    }

    public synchronized void onDisconnected() {
        if (this.state == ConnectState.PENDING_DISCONNECT) {
            Log.v("Disconnected from service");
            clearAllTimers();
            this.state = ConnectState.DISCONNECTED;
        } else {
            Log.v("Unexpected disconnect.");
            this.state = ConnectState.PENDING_CONNECTION;
            if (this.connectTries < 2) {
                fireReconnectAttempt();
            } else {
                useStore();
            }
        }
    }

    public synchronized void onConnectionFailed(int errorCode, Intent resolution) {
        this.state = ConnectState.PENDING_CONNECTION;
        StringBuilder stringBuilder;
        if (this.connectTries < 2) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Service unavailable (code=");
            stringBuilder.append(errorCode);
            stringBuilder.append("), will retry.");
            Log.w(stringBuilder.toString());
            fireReconnectAttempt();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Service unavailable (code=");
            stringBuilder.append(errorCode);
            stringBuilder.append("), using local store.");
            Log.w(stringBuilder.toString());
            useStore();
        }
    }

    private void fireReconnectAttempt() {
        this.reConnectTimer = cancelTimer(this.reConnectTimer);
        this.reConnectTimer = new Timer("Service Reconnect");
        this.reConnectTimer.schedule(new ReconnectTask(this, null), RECONNECT_WAIT_TIME);
    }
}
