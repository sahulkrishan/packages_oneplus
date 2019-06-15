package com.google.tagmanager;

import android.content.Context;
import android.net.Uri;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.tagmanager.Container.Callback;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagManager {
    private static TagManager sInstance;
    private final ContainerProvider mContainerProvider;
    private final ConcurrentMap<String, Container> mContainers;
    private final Context mContext;
    private volatile String mCtfeServerAddr;
    private final DataLayer mDataLayer;
    private volatile RefreshMode mRefreshMode;

    static class ContainerOpenException extends RuntimeException {
        private final String mContainerId;

        private ContainerOpenException(String containerId) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Container already open: ");
            stringBuilder.append(containerId);
            super(stringBuilder.toString());
            this.mContainerId = containerId;
        }

        public String getContainerId() {
            return this.mContainerId;
        }
    }

    @VisibleForTesting
    interface ContainerProvider {
        Container newContainer(Context context, String str, TagManager tagManager);
    }

    public enum RefreshMode {
        STANDARD,
        DEFAULT_CONTAINER
    }

    @Deprecated
    public interface Logger extends Logger {
    }

    @VisibleForTesting
    TagManager(Context context, ContainerProvider containerProvider, DataLayer dataLayer) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.mContainerProvider = containerProvider;
            this.mRefreshMode = RefreshMode.STANDARD;
            this.mContainers = new ConcurrentHashMap();
            this.mDataLayer = dataLayer;
            this.mDataLayer.registerListener(new Listener() {
                public void changed(Map<Object, Object> update) {
                    Object eventValue = update.get("event");
                    if (eventValue != null) {
                        TagManager.this.refreshTagsInAllContainers(eventValue.toString());
                    }
                }
            });
            this.mDataLayer.registerListener(new AdwordsClickReferrerListener(this.mContext));
            return;
        }
        throw new NullPointerException("context cannot be null");
    }

    public static TagManager getInstance(Context context) {
        TagManager tagManager;
        synchronized (TagManager.class) {
            if (sInstance == null) {
                if (context != null) {
                    sInstance = new TagManager(context, new ContainerProvider() {
                        public Container newContainer(Context context, String containerId, TagManager tagManager) {
                            return new Container(context, containerId, tagManager);
                        }
                    }, new DataLayer(new DataLayerPersistentStoreImpl(context)));
                } else {
                    Log.e("TagManager.getInstance requires non-null context.");
                    throw new NullPointerException();
                }
            }
            tagManager = sInstance;
        }
        return tagManager;
    }

    @VisibleForTesting
    static void clearInstance() {
        synchronized (TagManager.class) {
            sInstance = null;
        }
    }

    public DataLayer getDataLayer() {
        return this.mDataLayer;
    }

    public Container openContainer(String containerId, Callback callback) {
        Container container = this.mContainerProvider.newContainer(this.mContext, containerId, this);
        if (this.mContainers.putIfAbsent(containerId, container) == null) {
            if (this.mCtfeServerAddr != null) {
                container.setCtfeServerAddress(this.mCtfeServerAddr);
            }
            container.load(callback);
            return container;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Container id:");
        stringBuilder.append(containerId);
        stringBuilder.append(" has already been opened.");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setLogger(Logger logger) {
        Log.setLogger(logger);
    }

    public Logger getLogger() {
        return Log.getLogger();
    }

    public void setRefreshMode(RefreshMode mode) {
        this.mRefreshMode = mode;
    }

    public RefreshMode getRefreshMode() {
        return this.mRefreshMode;
    }

    public Container getContainer(String containerId) {
        return (Container) this.mContainers.get(containerId);
    }

    /* Access modifiers changed, original: declared_synchronized */
    public synchronized boolean setPreviewData(Uri data) {
        PreviewManager previewManager = PreviewManager.getInstance();
        if (!previewManager.setPreviewData(data)) {
            return false;
        }
        String previewContainerId = previewManager.getContainerId();
        switch (previewManager.getPreviewMode()) {
            case NONE:
                Container exitPreviewContainer = (Container) this.mContainers.get(previewContainerId);
                if (exitPreviewContainer != null) {
                    exitPreviewContainer.setCtfeUrlPathAndQuery(null);
                    exitPreviewContainer.refresh();
                    break;
                }
                break;
            case CONTAINER:
            case CONTAINER_DEBUG:
                for (Entry<String, Container> entry : this.mContainers.entrySet()) {
                    Container container = (Container) entry.getValue();
                    if (((String) entry.getKey()).equals(previewContainerId)) {
                        container.setCtfeUrlPathAndQuery(previewManager.getCTFEUrlPath());
                        container.refresh();
                    } else if (container.getCtfeUrlPathAndQuery() != null) {
                        container.setCtfeUrlPathAndQuery(null);
                        container.refresh();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setCtfeServerAddress(String addr) {
        this.mCtfeServerAddr = addr;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean removeContainer(String containerId) {
        return this.mContainers.remove(containerId) != null;
    }

    private void refreshTagsInAllContainers(String eventName) {
        for (Container container : this.mContainers.values()) {
            container.evaluateTags(eventName);
        }
    }
}
