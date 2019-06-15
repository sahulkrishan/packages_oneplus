package com.google.tagmanager;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.analytics.containertag.proto.Serving.Resource;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.tagmanager.LoadCallback.Failure;
import com.google.tagmanager.ResourceUtil.ExpandedResource;
import com.google.tagmanager.proto.Resource.ResourceWithMetadata;
import com.google.tagmanager.protobuf.nano.MessageNano;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;

class ResourceStorageImpl implements ResourceStorage {
    private static final String SAVED_RESOURCE_FILENAME_PREFIX = "resource_";
    private static final String SAVED_RESOURCE_SUB_DIR = "google_tagmanager";
    private LoadCallback<ResourceWithMetadata> mCallback;
    private final String mContainerId;
    private final Context mContext;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    ResourceStorageImpl(Context context, String containerId) {
        this.mContext = context;
        this.mContainerId = containerId;
    }

    public void setLoadCallback(LoadCallback<ResourceWithMetadata> callback) {
        this.mCallback = callback;
    }

    public void loadResourceFromDiskInBackground() {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                ResourceStorageImpl.this.loadResourceFromDisk();
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void loadResourceFromDisk() {
        if (this.mCallback != null) {
            this.mCallback.startLoad();
            Log.v("Start loading resource from disk ...");
            if ((PreviewManager.getInstance().getPreviewMode() == PreviewMode.CONTAINER || PreviewManager.getInstance().getPreviewMode() == PreviewMode.CONTAINER_DEBUG) && this.mContainerId.equals(PreviewManager.getInstance().getContainerId())) {
                this.mCallback.onFailure(Failure.NOT_AVAILABLE);
                return;
            }
            try {
                FileInputStream stream = new FileInputStream(getResourceFile());
                try {
                    ByteArrayOutputStream input = new ByteArrayOutputStream();
                    ResourceUtil.copyStream(stream, input);
                    this.mCallback.onSuccess(ResourceWithMetadata.parseFrom(input.toByteArray()));
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.w("error closing stream for reading resource from disk");
                    }
                } catch (IOException e2) {
                    Log.w("error reading resource from disk");
                    this.mCallback.onFailure(Failure.IO_ERROR);
                    stream.close();
                } catch (Throwable th) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        Log.w("error closing stream for reading resource from disk");
                    }
                    throw th;
                }
                Log.v("Load resource from disk finished.");
                return;
            } catch (FileNotFoundException e4) {
                Log.d("resource not on disk");
                this.mCallback.onFailure(Failure.NOT_AVAILABLE);
                return;
            }
        }
        throw new IllegalStateException("callback must be set before execute");
    }

    public void saveResourceToDiskInBackground(final ResourceWithMetadata resource) {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                ResourceStorageImpl.this.saveResourceToDisk(resource);
            }
        });
    }

    public Resource loadResourceFromContainerAsset(String assetFile) {
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Loading default container from ");
        stringBuilder2.append(assetFile);
        Log.v(stringBuilder2.toString());
        AssetManager assets = this.mContext.getAssets();
        if (assets == null) {
            Log.e("No assets found in package");
            return null;
        }
        InputStream is = null;
        try {
            is = assets.open(assetFile);
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ResourceUtil.copyStream(is, output);
                Resource result = Resource.parseFrom(output.toByteArray());
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Parsed default container: ");
                stringBuilder3.append(result);
                Log.v(stringBuilder3.toString());
                try {
                    is.close();
                } catch (IOException e) {
                }
                return result;
            } catch (IOException e2) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Error when parsing: ");
                stringBuilder.append(assetFile);
                Log.w(stringBuilder.toString());
                try {
                    is.close();
                } catch (IOException e3) {
                }
                return null;
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("No asset file: ");
            stringBuilder.append(assetFile);
            stringBuilder.append(" found.");
            Log.w(stringBuilder.toString());
            return null;
        }
    }

    public ExpandedResource loadExpandedResourceFromJsonAsset(String assetFile) {
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("loading default container from ");
        stringBuilder2.append(assetFile);
        Log.v(stringBuilder2.toString());
        AssetManager assets = this.mContext.getAssets();
        if (assets == null) {
            Log.w("Looking for default JSON container in package, but no assets were found.");
            return null;
        }
        InputStream is = null;
        try {
            is = assets.open(assetFile);
            ExpandedResource expandedResourceFromJsonString = JsonUtils.expandedResourceFromJsonString(stringFromInputStream(is));
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            return expandedResourceFromJsonString;
        } catch (IOException e2) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("No asset file: ");
            stringBuilder.append(assetFile);
            stringBuilder.append(" found (or errors reading it).");
            Log.w(stringBuilder.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            return null;
        } catch (JSONException e4) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Error parsing JSON file");
            stringBuilder.append(assetFile);
            stringBuilder.append(" : ");
            stringBuilder.append(e4);
            Log.w(stringBuilder.toString());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e5) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                }
            }
        }
    }

    public synchronized void close() {
        this.mExecutor.shutdown();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean saveResourceToDisk(ResourceWithMetadata resource) {
        File file = getResourceFile();
        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(MessageNano.toByteArray(resource));
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w("error closing stream for writing resource to disk");
                }
                return true;
            } catch (IOException e2) {
                Log.w("Error writing resource to disk. Removing resource from disk.");
                file.delete();
                try {
                    stream.close();
                } catch (IOException e3) {
                    Log.w("error closing stream for writing resource to disk");
                }
                return false;
            } catch (Throwable th) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    Log.w("error closing stream for writing resource to disk");
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            Log.e("Error opening resource file for writing");
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public File getResourceFile() {
        String fileName = new StringBuilder();
        fileName.append(SAVED_RESOURCE_FILENAME_PREFIX);
        fileName.append(this.mContainerId);
        return new File(this.mContext.getDir(SAVED_RESOURCE_SUB_DIR, 0), fileName.toString());
    }

    private String stringFromInputStream(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while (true) {
            int read = reader.read(buffer);
            int n = read;
            if (read == -1) {
                return writer.toString();
            }
            writer.write(buffer, 0, n);
        }
    }
}
