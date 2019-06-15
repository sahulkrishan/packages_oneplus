package com.android.settings.enterprise;

import android.content.Context;
import java.util.Date;

public class SecurityLogsPreferenceController extends AdminActionPreferenceControllerBase {
    private static final String KEY_SECURITY_LOGS = "security_logs";

    public SecurityLogsPreferenceController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public Date getAdminActionTimestamp() {
        return this.mFeatureProvider.getLastSecurityLogRetrievalTime();
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.isSecurityLoggingEnabled() || this.mFeatureProvider.getLastSecurityLogRetrievalTime() != null;
    }

    public String getPreferenceKey() {
        return KEY_SECURITY_LOGS;
    }
}
