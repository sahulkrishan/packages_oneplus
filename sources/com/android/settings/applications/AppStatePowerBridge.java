package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import java.util.ArrayList;

public class AppStatePowerBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_POWER_WHITELISTED = new CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED, new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return info.extraInfo == Boolean.TRUE;
        }
    });
    private final PowerWhitelistBackend mBackend;

    public AppStatePowerBridge(Context context, ApplicationsState appState, Callback callback) {
        super(appState, callback);
        this.mBackend = PowerWhitelistBackend.getInstance(context);
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry app = (AppEntry) apps.get(i);
            app.extraInfo = this.mBackend.isWhitelisted(app.info.packageName) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = this.mBackend.isWhitelisted(pkg) ? Boolean.TRUE : Boolean.FALSE;
    }
}
