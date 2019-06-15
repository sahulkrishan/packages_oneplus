package com.android.settings.enterprise;

import android.content.Context;
import java.util.Date;

public class BugReportsPreferenceController extends AdminActionPreferenceControllerBase {
    private static final String KEY_BUG_REPORTS = "bug_reports";

    public BugReportsPreferenceController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public Date getAdminActionTimestamp() {
        return this.mFeatureProvider.getLastBugReportRequestTime();
    }

    public String getPreferenceKey() {
        return KEY_BUG_REPORTS;
    }
}
