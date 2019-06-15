package com.google.tagmanager;

import com.google.tagmanager.Container.Callback;
import com.google.tagmanager.Container.RefreshFailure;
import com.google.tagmanager.Container.RefreshType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class ContainerOpener {
    public static final long DEFAULT_TIMEOUT_IN_MILLIS = 2000;
    private static final Map<String, List<Notifier>> mContainerIdNotifiersMap = new HashMap();
    private Clock mClock = new Clock() {
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    };
    private volatile Container mContainer;
    private final String mContainerId;
    private boolean mHaveNotified;
    private Notifier mNotifier;
    private final TagManager mTagManager;
    private final long mTimeoutInMillis;

    public interface ContainerFuture {
        Container get();

        boolean isDone();
    }

    public interface Notifier {
        void containerAvailable(Container container);
    }

    public enum OpenType {
        PREFER_NON_DEFAULT,
        PREFER_FRESH
    }

    private static class ContainerFutureImpl implements ContainerFuture {
        private volatile Container mContainer;
        private Semaphore mContainerIsReady;
        private volatile boolean mHaveGotten;

        private ContainerFutureImpl() {
            this.mContainerIsReady = new Semaphore(0);
        }

        /* synthetic */ ContainerFutureImpl(AnonymousClass1 x0) {
            this();
        }

        public Container get() {
            if (this.mHaveGotten) {
                return this.mContainer;
            }
            try {
                this.mContainerIsReady.acquire();
            } catch (InterruptedException e) {
            }
            this.mHaveGotten = true;
            return this.mContainer;
        }

        public void setContainer(Container container) {
            this.mContainer = container;
            this.mContainerIsReady.release();
        }

        public boolean isDone() {
            return this.mHaveGotten || this.mContainerIsReady.availablePermits() > 0;
        }
    }

    private class WaitForFresh implements Callback {
        private final long mOldestTimeToBeFresh;

        public WaitForFresh(long oldestTimeToBeFresh) {
            this.mOldestTimeToBeFresh = oldestTimeToBeFresh;
        }

        public void containerRefreshBegin(Container container, RefreshType refreshType) {
        }

        public void containerRefreshSuccess(Container container, RefreshType refreshType) {
            if (refreshType == RefreshType.NETWORK || isFresh()) {
                ContainerOpener.this.callNotifiers(container);
            }
        }

        public void containerRefreshFailure(Container container, RefreshType refreshType, RefreshFailure refreshFailure) {
            if (refreshType == RefreshType.NETWORK) {
                ContainerOpener.this.callNotifiers(container);
            }
        }

        private boolean isFresh() {
            return this.mOldestTimeToBeFresh < ContainerOpener.this.mContainer.getLastRefreshTime();
        }
    }

    private class WaitForNonDefaultRefresh implements Callback {
        public void containerRefreshBegin(Container container, RefreshType refreshType) {
        }

        public void containerRefreshSuccess(Container container, RefreshType refreshType) {
            ContainerOpener.this.callNotifiers(container);
        }

        public void containerRefreshFailure(Container container, RefreshType refreshType, RefreshFailure refreshFailure) {
            if (refreshType == RefreshType.NETWORK) {
                ContainerOpener.this.callNotifiers(container);
            }
        }
    }

    private ContainerOpener(TagManager tagManager, String containerId, Long timeoutInMillis, Notifier notifier) {
        this.mTagManager = tagManager;
        this.mContainerId = containerId;
        this.mTimeoutInMillis = timeoutInMillis != null ? Math.max(1, timeoutInMillis.longValue()) : 2000;
        this.mNotifier = notifier;
    }

    public static void openContainer(TagManager tagManager, String containerId, OpenType openType, Long timeoutInMillis, Notifier notifier) {
        if (tagManager == null) {
            throw new NullPointerException("TagManager cannot be null.");
        } else if (containerId == null) {
            throw new NullPointerException("ContainerId cannot be null.");
        } else if (openType == null) {
            throw new NullPointerException("OpenType cannot be null.");
        } else if (notifier != null) {
            new ContainerOpener(tagManager, containerId, timeoutInMillis, notifier).open(openType == OpenType.PREFER_FRESH ? RefreshType.NETWORK : RefreshType.SAVED);
        } else {
            throw new NullPointerException("Notifier cannot be null.");
        }
    }

    public static ContainerFuture openContainer(TagManager tagManager, String containerId, OpenType openType, Long timeoutInMillis) {
        final ContainerFutureImpl future = new ContainerFutureImpl();
        openContainer(tagManager, containerId, openType, timeoutInMillis, new Notifier() {
            public void containerAvailable(Container container) {
                future.setContainer(container);
            }
        });
        return future;
    }

    /* JADX WARNING: Missing block: B:14:0x0059, code skipped:
            if (r2 == false) goto L_0x0065;
     */
    /* JADX WARNING: Missing block: B:15:0x005b, code skipped:
            r11.mNotifier.containerAvailable(r11.mContainer);
            r11.mNotifier = null;
     */
    /* JADX WARNING: Missing block: B:16:0x0064, code skipped:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x0065, code skipped:
            setTimer(java.lang.Math.max(1, r11.mTimeoutInMillis - (r11.mClock.currentTimeMillis() - r0)));
     */
    /* JADX WARNING: Missing block: B:18:0x0078, code skipped:
            return;
     */
    private void open(com.google.tagmanager.Container.RefreshType r12) {
        /*
        r11 = this;
        r0 = r11.mClock;
        r0 = r0.currentTimeMillis();
        r2 = 0;
        r3 = com.google.tagmanager.ContainerOpener.class;
        monitor-enter(r3);
        r4 = r11.mTagManager;	 Catch:{ all -> 0x0082 }
        r5 = r11.mContainerId;	 Catch:{ all -> 0x0082 }
        r4 = r4.getContainer(r5);	 Catch:{ all -> 0x0082 }
        r11.mContainer = r4;	 Catch:{ all -> 0x0082 }
        r4 = r11.mContainer;	 Catch:{ all -> 0x0082 }
        r5 = 0;
        if (r4 != 0) goto L_0x004b;
    L_0x0019:
        r4 = new java.util.ArrayList;	 Catch:{ all -> 0x0082 }
        r4.<init>();	 Catch:{ all -> 0x0082 }
        r6 = r11.mNotifier;	 Catch:{ all -> 0x0082 }
        r4.add(r6);	 Catch:{ all -> 0x0082 }
        r11.mNotifier = r5;	 Catch:{ all -> 0x0082 }
        r6 = mContainerIdNotifiersMap;	 Catch:{ all -> 0x0082 }
        r7 = r11.mContainerId;	 Catch:{ all -> 0x0082 }
        r6.put(r7, r4);	 Catch:{ all -> 0x0082 }
        r6 = r11.mTagManager;	 Catch:{ all -> 0x0082 }
        r7 = r11.mContainerId;	 Catch:{ all -> 0x0082 }
        r8 = com.google.tagmanager.Container.RefreshType.SAVED;	 Catch:{ all -> 0x0082 }
        if (r12 != r8) goto L_0x003a;
    L_0x0034:
        r8 = new com.google.tagmanager.ContainerOpener$WaitForNonDefaultRefresh;	 Catch:{ all -> 0x0082 }
        r8.<init>();	 Catch:{ all -> 0x0082 }
        goto L_0x0044;
    L_0x003a:
        r8 = new com.google.tagmanager.ContainerOpener$WaitForFresh;	 Catch:{ all -> 0x0082 }
        r9 = 43200000; // 0x2932e00 float:2.1626111E-37 double:2.1343636E-316;
        r9 = r0 - r9;
        r8.<init>(r9);	 Catch:{ all -> 0x0082 }
    L_0x0044:
        r6 = r6.openContainer(r7, r8);	 Catch:{ all -> 0x0082 }
        r11.mContainer = r6;	 Catch:{ all -> 0x0082 }
        goto L_0x0058;
    L_0x004b:
        r4 = mContainerIdNotifiersMap;	 Catch:{ all -> 0x0082 }
        r6 = r11.mContainerId;	 Catch:{ all -> 0x0082 }
        r4 = r4.get(r6);	 Catch:{ all -> 0x0082 }
        r4 = (java.util.List) r4;	 Catch:{ all -> 0x0082 }
        if (r4 != 0) goto L_0x0079;
    L_0x0057:
        r2 = 1;
    L_0x0058:
        monitor-exit(r3);	 Catch:{ all -> 0x0082 }
        if (r2 == 0) goto L_0x0065;
    L_0x005b:
        r3 = r11.mNotifier;
        r4 = r11.mContainer;
        r3.containerAvailable(r4);
        r11.mNotifier = r5;
        return;
    L_0x0065:
        r3 = r11.mTimeoutInMillis;
        r5 = r11.mClock;
        r5 = r5.currentTimeMillis();
        r5 = r5 - r0;
        r3 = r3 - r5;
        r5 = 1;
        r5 = java.lang.Math.max(r5, r3);
        r11.setTimer(r5);
        return;
    L_0x0079:
        r6 = r11.mNotifier;	 Catch:{ all -> 0x0082 }
        r4.add(r6);	 Catch:{ all -> 0x0082 }
        r11.mNotifier = r5;	 Catch:{ all -> 0x0082 }
        monitor-exit(r3);	 Catch:{ all -> 0x0082 }
        return;
    L_0x0082:
        r4 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0082 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.tagmanager.ContainerOpener.open(com.google.tagmanager.Container$RefreshType):void");
    }

    private void setTimer(long timeoutInMillis) {
        new Timer("ContainerOpener").schedule(new TimerTask() {
            public void run() {
                Log.i("Timer expired.");
                ContainerOpener.this.callNotifiers(ContainerOpener.this.mContainer);
            }
        }, timeoutInMillis);
    }

    private synchronized void callNotifiers(Container container) {
        if (!this.mHaveNotified) {
            List<Notifier> notifiers;
            synchronized (ContainerOpener.class) {
                notifiers = (List) mContainerIdNotifiersMap.remove(this.mContainerId);
            }
            if (notifiers != null) {
                for (Notifier notifier : notifiers) {
                    notifier.containerAvailable(container);
                }
            }
            this.mHaveNotified = true;
        }
    }
}
