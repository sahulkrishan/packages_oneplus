package com.google.tagmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.analytics.containertag.proto.Serving.SupplementedResource;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.tagmanager.LoadCallback.Failure;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class ResourceLoader implements Runnable {
    private static final String CTFE_URL_PREFIX = "/r?id=";
    private static final String CTFE_URL_SUFFIX = "&v=a62676326";
    private static final String PREVIOUS_CONTAINER_VERSION_QUERY_NAME = "pv";
    @VisibleForTesting
    static final String SDK_VERSION = "a62676326";
    private LoadCallback<SupplementedResource> mCallback;
    private final NetworkClientFactory mClientFactory;
    private final String mContainerId;
    private final Context mContext;
    private volatile CtfeHost mCtfeHost;
    private volatile String mCtfeUrlPathAndQuery;
    private final String mDefaultCtfeUrlPathAndQuery;
    private volatile String mPreviousVersion;

    public ResourceLoader(Context context, String containerId, CtfeHost ctfeHost) {
        this(context, containerId, new NetworkClientFactory(), ctfeHost);
    }

    @VisibleForTesting
    ResourceLoader(Context context, String containerId, NetworkClientFactory factory, CtfeHost ctfeHost) {
        this.mContext = context;
        this.mClientFactory = factory;
        this.mContainerId = containerId;
        this.mCtfeHost = ctfeHost;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CTFE_URL_PREFIX);
        stringBuilder.append(containerId);
        this.mDefaultCtfeUrlPathAndQuery = stringBuilder.toString();
        this.mCtfeUrlPathAndQuery = this.mDefaultCtfeUrlPathAndQuery;
        this.mPreviousVersion = null;
    }

    public void run() {
        if (this.mCallback != null) {
            this.mCallback.startLoad();
            loadResource();
            return;
        }
        throw new IllegalStateException("callback must be set before execute");
    }

    private boolean okToLoad() {
        NetworkInfo network = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (network != null && network.isConnected()) {
            return true;
        }
        Log.v("...no network connectivity");
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void setLoadCallback(LoadCallback<SupplementedResource> callback) {
        this.mCallback = callback;
    }

    private void loadResource() {
        StringBuilder stringBuilder;
        if (okToLoad()) {
            Log.v("Start loading resource from network ...");
            String url = getCtfeUrl();
            NetworkClient networkClient = this.mClientFactory.createNetworkClient();
            try {
                InputStream inputStream = networkClient.getInputStream(url);
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ResourceUtil.copyStream(inputStream, outputStream);
                    SupplementedResource resource = SupplementedResource.parseFrom(outputStream.toByteArray());
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Successfully loaded supplemented resource: ");
                    stringBuilder2.append(resource);
                    Log.v(stringBuilder2.toString());
                    if (resource.resource == null) {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("No change for container: ");
                        stringBuilder2.append(this.mContainerId);
                        Log.v(stringBuilder2.toString());
                    }
                    this.mCallback.onSuccess(resource);
                    networkClient.close();
                    Log.v("Load resource from network finished.");
                    return;
                } catch (IOException e) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Error when parsing downloaded resources from url: ");
                    stringBuilder.append(url);
                    stringBuilder.append(" ");
                    stringBuilder.append(e.getMessage());
                    Log.w(stringBuilder.toString(), e);
                    this.mCallback.onFailure(Failure.SERVER_ERROR);
                } catch (Throwable th) {
                    networkClient.close();
                }
            } catch (FileNotFoundException e2) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("No data is retrieved from the given url: ");
                stringBuilder.append(url);
                stringBuilder.append(". Make sure container_id: ");
                stringBuilder.append(this.mContainerId);
                stringBuilder.append(" is correct.");
                Log.w(stringBuilder.toString());
                this.mCallback.onFailure(Failure.SERVER_ERROR);
                networkClient.close();
                return;
            } catch (IOException e3) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Error when loading resources from url: ");
                stringBuilder.append(url);
                stringBuilder.append(" ");
                stringBuilder.append(e3.getMessage());
                Log.w(stringBuilder.toString(), e3);
                this.mCallback.onFailure(Failure.IO_ERROR);
                networkClient.close();
                return;
            }
        }
        this.mCallback.onFailure(Failure.NOT_AVAILABLE);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getCtfeUrl() {
        StringBuilder stringBuilder;
        String url = new StringBuilder();
        url.append(this.mCtfeHost.getCtfeServerAddress());
        url.append(this.mCtfeUrlPathAndQuery);
        url.append(CTFE_URL_SUFFIX);
        url = url.toString();
        if (!(this.mPreviousVersion == null || this.mPreviousVersion.trim().equals(""))) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(url);
            stringBuilder.append("&pv=");
            stringBuilder.append(this.mPreviousVersion);
            url = stringBuilder.toString();
        }
        if (!PreviewManager.getInstance().getPreviewMode().equals(PreviewMode.CONTAINER_DEBUG)) {
            return url;
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(url);
        stringBuilder.append("&gtm_debug=x");
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setCtfeURLPathAndQuery(String urlPathAndQuery) {
        if (urlPathAndQuery == null) {
            this.mCtfeUrlPathAndQuery = this.mDefaultCtfeUrlPathAndQuery;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Setting CTFE URL path: ");
        stringBuilder.append(urlPathAndQuery);
        Log.d(stringBuilder.toString());
        this.mCtfeUrlPathAndQuery = urlPathAndQuery;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPreviousVersion(String version) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Setting previous container version: ");
        stringBuilder.append(version);
        Log.d(stringBuilder.toString());
        this.mPreviousVersion = version;
    }
}
