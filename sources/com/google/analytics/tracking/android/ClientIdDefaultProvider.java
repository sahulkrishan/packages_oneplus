package com.google.analytics.tracking.android;

import android.content.Context;
import com.google.android.gms.common.util.VisibleForTesting;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

class ClientIdDefaultProvider implements DefaultProvider {
    private static ClientIdDefaultProvider sInstance;
    private static final Object sInstanceLock = new Object();
    private String mClientId;
    private boolean mClientIdLoaded = false;
    private final Object mClientIdLock = new Object();
    private final Context mContext;

    public static void initializeProvider(Context c) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new ClientIdDefaultProvider(c);
            }
        }
    }

    @VisibleForTesting
    static void dropInstance() {
        synchronized (sInstanceLock) {
            sInstance = null;
        }
    }

    public static ClientIdDefaultProvider getProvider() {
        ClientIdDefaultProvider clientIdDefaultProvider;
        synchronized (sInstanceLock) {
            clientIdDefaultProvider = sInstance;
        }
        return clientIdDefaultProvider;
    }

    protected ClientIdDefaultProvider(Context c) {
        this.mContext = c;
        asyncInitializeClientId();
    }

    public boolean providesField(String field) {
        return Fields.CLIENT_ID.equals(field);
    }

    public String getValue(String field) {
        if (Fields.CLIENT_ID.equals(field)) {
            return blockingGetClientId();
        }
        return null;
    }

    private String blockingGetClientId() {
        if (!this.mClientIdLoaded) {
            synchronized (this.mClientIdLock) {
                if (!this.mClientIdLoaded) {
                    Log.v("Waiting for clientId to load");
                    do {
                        try {
                            this.mClientIdLock.wait();
                        } catch (InterruptedException e) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Exception while waiting for clientId: ");
                            stringBuilder.append(e);
                            Log.e(stringBuilder.toString());
                        }
                    } while (!this.mClientIdLoaded);
                }
            }
        }
        Log.v("Loaded clientId");
        return this.mClientId;
    }

    private boolean storeClientId(String clientId) {
        try {
            Log.v("Storing clientId.");
            FileOutputStream fos = this.mContext.openFileOutput("gaClientId", 0);
            fos.write(clientId.getBytes());
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e("Error creating clientId file.");
            return false;
        } catch (IOException e2) {
            Log.e("Error writing to clientId file.");
            return false;
        }
    }

    /* Access modifiers changed, original: protected */
    public String generateClientId() {
        String result = UUID.randomUUID().toString().toLowerCase();
        if (storeClientId(result)) {
            return result;
        }
        return "0";
    }

    private void asyncInitializeClientId() {
        new Thread("client_id_fetcher") {
            public void run() {
                synchronized (ClientIdDefaultProvider.this.mClientIdLock) {
                    ClientIdDefaultProvider.this.mClientId = ClientIdDefaultProvider.this.initializeClientId();
                    ClientIdDefaultProvider.this.mClientIdLoaded = true;
                    ClientIdDefaultProvider.this.mClientIdLock.notifyAll();
                }
            }
        }.start();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String initializeClientId() {
        String rslt = null;
        try {
            FileInputStream input = this.mContext.openFileInput("gaClientId");
            byte[] bytes = new byte[128];
            int readLen = input.read(bytes, 0, 128);
            if (input.available() > 0) {
                Log.e("clientId file seems corrupted, deleting it.");
                input.close();
                this.mContext.deleteFile("gaClientId");
            } else if (readLen <= 0) {
                Log.e("clientId file seems empty, deleting it.");
                input.close();
                this.mContext.deleteFile("gaClientId");
            } else {
                rslt = new String(bytes, 0, readLen);
                input.close();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Log.e("Error reading clientId file, deleting it.");
            this.mContext.deleteFile("gaClientId");
        }
        if (rslt == null) {
            return generateClientId();
        }
        return rslt;
    }
}
