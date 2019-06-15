package com.google.tagmanager;

import android.os.Build.VERSION;
import com.google.android.gms.common.util.VisibleForTesting;

class NetworkClientFactory {
    NetworkClientFactory() {
    }

    public NetworkClient createNetworkClient() {
        if (getSdkVersion() < 8) {
            return new HttpNetworkClient();
        }
        return new HttpUrlConnectionNetworkClient();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getSdkVersion() {
        return VERSION.SDK_INT;
    }
}
