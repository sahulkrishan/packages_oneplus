package com.android.settings.enterprise;

import android.content.Context;
import java.util.Date;

public class NetworkLogsPreferenceController extends AdminActionPreferenceControllerBase {
    private static final String KEY_NETWORK_LOGS = "network_logs";

    public NetworkLogsPreferenceController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public Date getAdminActionTimestamp() {
        return this.mFeatureProvider.getLastNetworkLogRetrievalTime();
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.isNetworkLoggingEnabled() || this.mFeatureProvider.getLastNetworkLogRetrievalTime() != null;
    }

    public String getPreferenceKey() {
        return KEY_NETWORK_LOGS;
    }
}
