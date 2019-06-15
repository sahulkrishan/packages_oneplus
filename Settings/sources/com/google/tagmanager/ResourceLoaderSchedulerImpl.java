package com.google.tagmanager;

import android.content.Context;
import com.google.analytics.containertag.proto.Serving.SupplementedResource;
import com.google.android.gms.common.util.VisibleForTesting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class ResourceLoaderSchedulerImpl implements ResourceLoaderScheduler {
    private static final boolean MAY_INTERRUPT_IF_RUNNING = true;
    private LoadCallback<SupplementedResource> mCallback;
    private boolean mClosed;
    private final String mContainerId;
    private final Context mContext;
    private CtfeHost mCtfeHost;
    private String mCtfeUrlPathAndQuery;
    private final ScheduledExecutorService mExecutor;
    private ScheduledFuture<?> mFuture;
    private final ResourceLoaderFactory mResourceLoaderFactory;

    interface ResourceLoaderFactory {
        ResourceLoader createResourceLoader(CtfeHost ctfeHost);
    }

    interface ScheduledExecutorServiceFactory {
        ScheduledExecutorService createExecutorService();
    }

    public ResourceLoaderSchedulerImpl(Context context, String containerId, CtfeHost ctfeHost) {
        this(context, containerId, ctfeHost, null, null);
    }

    @VisibleForTesting
    ResourceLoaderSchedulerImpl(Context context, String containerId, CtfeHost ctfeHost, ScheduledExecutorServiceFactory executorServiceFactory, ResourceLoaderFactory resourceLoaderFactory) {
        this.mCtfeHost = ctfeHost;
        this.mContext = context;
        this.mContainerId = containerId;
        if (executorServiceFactory == null) {
            executorServiceFactory = new ScheduledExecutorServiceFactory() {
                public ScheduledExecutorService createExecutorService() {
                    return Executors.newSingleThreadScheduledExecutor();
                }
            };
        }
        this.mExecutor = executorServiceFactory.createExecutorService();
        if (resourceLoaderFactory == null) {
            this.mResourceLoaderFactory = new ResourceLoaderFactory() {
                public ResourceLoader createResourceLoader(CtfeHost ctfeHost) {
                    return new ResourceLoader(ResourceLoaderSchedulerImpl.this.mContext, ResourceLoaderSchedulerImpl.this.mContainerId, ctfeHost);
                }
            };
        } else {
            this.mResourceLoaderFactory = resourceLoaderFactory;
        }
    }

    public synchronized void close() {
        ensureNotClosed();
        if (this.mFuture != null) {
            this.mFuture.cancel(false);
        }
        this.mExecutor.shutdown();
        this.mClosed = true;
    }

    public synchronized void setLoadCallback(LoadCallback<SupplementedResource> callback) {
        ensureNotClosed();
        this.mCallback = callback;
    }

    public synchronized void loadAfterDelay(long delayInMillis, String previousVersion) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("loadAfterDelay: containerId=");
        stringBuilder.append(this.mContainerId);
        stringBuilder.append(" delay=");
        stringBuilder.append(delayInMillis);
        Log.v(stringBuilder.toString());
        ensureNotClosed();
        if (this.mCallback != null) {
            if (this.mFuture != null) {
                this.mFuture.cancel(false);
            }
            this.mFuture = this.mExecutor.schedule(createResourceLoader(previousVersion), delayInMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("callback must be set before loadAfterDelay() is called.");
        }
    }

    public synchronized void setCtfeURLPathAndQuery(String urlPathAndQuery) {
        ensureNotClosed();
        this.mCtfeUrlPathAndQuery = urlPathAndQuery;
    }

    private synchronized void ensureNotClosed() {
        if (this.mClosed) {
            throw new IllegalStateException("called method after closed");
        }
    }

    private ResourceLoader createResourceLoader(String previousVersion) {
        ResourceLoader resourceLoader = this.mResourceLoaderFactory.createResourceLoader(this.mCtfeHost);
        resourceLoader.setLoadCallback(this.mCallback);
        resourceLoader.setCtfeURLPathAndQuery(this.mCtfeUrlPathAndQuery);
        resourceLoader.setPreviousVersion(previousVersion);
        return resourceLoader;
    }
}
