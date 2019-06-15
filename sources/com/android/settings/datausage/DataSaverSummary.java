package com.android.settings.datausage;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.datausage.AppStateDataUsageBridge.DataUsageState;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public class DataSaverSummary extends SettingsPreferenceFragment implements OnSwitchChangeListener, Listener, Callback, Callbacks {
    private static final String KEY_UNRESTRICTED_ACCESS = "unrestricted_access";
    private static final String PROPERTY_DATA_SAVER_STATE = "persist.radio.data_saver.state";
    static final String TAG = "DataSaverSummary";
    private ApplicationsState mApplicationsState;
    private DataSaverBackend mDataSaverBackend;
    private AppStateDataUsageBridge mDataUsageBridge;
    private Session mSession;
    private SwitchBar mSwitchBar;
    private boolean mSwitching;
    private Preference mUnrestrictedAccess;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.data_saver);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle(17039765);
        this.mUnrestrictedAccess = findPreference(KEY_UNRESTRICTED_ACCESS);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBar.setSwitchBarText(R.string.data_saver_switch_title, R.string.data_saver_switch_title);
        this.mSwitchBar.show();
        this.mSwitchBar.addOnSwitchChangeListener(this);
    }

    public void onResume() {
        super.onResume();
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataUsageBridge = new AppStateDataUsageBridge(this.mApplicationsState, this, this.mDataSaverBackend);
        this.mSession = this.mApplicationsState.newSession(this);
        this.mDataSaverBackend.refreshWhitelist();
        this.mDataSaverBackend.refreshBlacklist();
        this.mDataSaverBackend.addListener(this);
        this.mDataUsageBridge.resume();
    }

    public void onPause() {
        super.onPause();
        this.mDataSaverBackend.remListener(this);
        this.mDataUsageBridge.pause();
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        synchronized (this) {
            if (this.mSwitching) {
                return;
            }
            this.mSwitching = true;
            this.mDataSaverBackend.setDataSaverEnabled(isChecked);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onSwitchChanged = ");
            stringBuilder.append(isChecked);
            Log.d(str, stringBuilder.toString());
            TelephonyManager.setTelephonyProperty(PROPERTY_DATA_SAVER_STATE, isChecked ? "1" : "0");
        }
    }

    public int getMetricsCategory() {
        return 348;
    }

    public int getHelpResource() {
        return R.string.help_url_data_saver;
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        synchronized (this) {
            this.mSwitchBar.setChecked(isDataSaving);
            this.mSwitching = false;
        }
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }

    public void onExtraInfoUpdated() {
        if (isAdded()) {
            ArrayList<AppEntry> allApps = this.mSession.getAllApps();
            int N = allApps.size();
            int count = 0;
            for (int i = 0; i < N; i++) {
                AppEntry entry = (AppEntry) allApps.get(i);
                if (ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER.filterApp(entry) && entry.extraInfo != null && ((DataUsageState) entry.extraInfo).isDataSaverWhitelisted) {
                    count++;
                }
            }
            this.mUnrestrictedAccess.setSummary(getResources().getQuantityString(R.plurals.data_saver_unrestricted_summary, count, new Object[]{Integer.valueOf(count)}));
        }
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
        this.mSwitchBar.postDelayed(new Runnable() {
            public void run() {
                Log.d(DataSaverSummary.TAG, "onLoadEntriesCompleted............");
                DataSaverSummary.this.mDataUsageBridge.resume();
            }
        }, 300);
    }
}
