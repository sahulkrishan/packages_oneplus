package com.oneplus.settings.backgroundoptimize;

import android.app.Activity;
import android.content.Context;
import com.android.settings.R;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.notification.NotificationBackend;

public class BgOptimizeApps extends ManageApplications {
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;
        private final NotificationBackend mNotificationBackend;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
            this.mNotificationBackend = new NotificationBackend();
        }

        public void setListening(boolean listening) {
        }

        private void updateSummary(int count) {
            if (count == 0) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.notification_summary_none));
                return;
            }
            this.mLoader.setSummary(this, this.mContext.getResources().getQuantityString(R.plurals.notification_summary, count, new Object[]{Integer.valueOf(count)}));
        }
    }
}
