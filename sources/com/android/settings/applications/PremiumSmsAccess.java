package com.android.settings.applications;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.AppStateSmsPremBridge.SmsState;
import com.android.settings.notification.EmptyTextSettings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.android.settingslib.widget.FooterPreference;
import java.util.ArrayList;

public class PremiumSmsAccess extends EmptyTextSettings implements Callback, Callbacks, OnPreferenceChangeListener {
    private ApplicationsState mApplicationsState;
    private Session mSession;
    private AppStateSmsPremBridge mSmsBackend;

    private class PremiumSmsPreference extends DropDownPreference {
        private final AppEntry mAppEntry;

        public PremiumSmsPreference(AppEntry appEntry, Context context) {
            super(context);
            this.mAppEntry = appEntry;
            this.mAppEntry.ensureLabel(context);
            setTitle((CharSequence) this.mAppEntry.label);
            if (this.mAppEntry.icon != null) {
                setIcon(this.mAppEntry.icon);
            }
            setEntries((int) R.array.security_settings_premium_sms_values);
            setEntryValues(new CharSequence[]{String.valueOf(1), String.valueOf(2), String.valueOf(3)});
            setValue(String.valueOf(getCurrentValue()));
            setSummary("%s");
        }

        private int getCurrentValue() {
            if (this.mAppEntry.extraInfo instanceof SmsState) {
                return ((SmsState) this.mAppEntry.extraInfo).smsState;
            }
            return 0;
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            if (getIcon() == null) {
                holder.itemView.post(new Runnable() {
                    public void run() {
                        PremiumSmsAccess.this.mApplicationsState.ensureIcon(PremiumSmsPreference.this.mAppEntry);
                        PremiumSmsPreference.this.setIcon(PremiumSmsPreference.this.mAppEntry.icon);
                    }
                });
            }
            super.onBindViewHolder(holder);
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mApplicationsState = ApplicationsState.getInstance((Application) getContext().getApplicationContext());
        this.mSession = this.mApplicationsState.newSession(this, getLifecycle());
        this.mSmsBackend = new AppStateSmsPremBridge(getContext(), this.mApplicationsState, this);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLoading(true, false);
    }

    public void onResume() {
        super.onResume();
        this.mSmsBackend.resume();
    }

    public void onPause() {
        this.mSmsBackend.pause();
        super.onPause();
    }

    public void onDestroy() {
        this.mSmsBackend.release();
        super.onDestroy();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.premium_sms_settings;
    }

    public int getMetricsCategory() {
        return 388;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        PremiumSmsPreference pref = (PremiumSmsPreference) preference;
        int smsState = Integer.parseInt((String) newValue);
        logSpecialPermissionChange(smsState, pref.mAppEntry.info.packageName);
        this.mSmsBackend.setSmsState(pref.mAppEntry.info.packageName, smsState);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(int smsState, String packageName) {
        int category = 0;
        switch (smsState) {
            case 1:
                category = 778;
                break;
            case 2:
                category = 779;
                break;
            case 3:
                category = 780;
                break;
        }
        if (category != 0) {
            FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), category, packageName, new Pair[0]);
        }
    }

    private void updatePrefs(ArrayList<AppEntry> apps) {
        if (apps != null) {
            setEmptyText(R.string.premium_sms_none);
            int i = 0;
            setLoading(false, true);
            PreferenceScreen screen = getPreferenceScreen();
            screen.removeAll();
            screen.setOrderingAsAdded(true);
            while (i < apps.size()) {
                PremiumSmsPreference smsPreference = new PremiumSmsPreference((AppEntry) apps.get(i), getPrefContext());
                smsPreference.setOnPreferenceChangeListener(this);
                screen.addPreference(smsPreference);
                i++;
            }
            if (apps.size() != 0) {
                FooterPreference footer = new FooterPreference(getPrefContext());
                footer.setTitle((int) R.string.premium_sms_warning);
                screen.addPreference(footer);
            }
        }
    }

    private void update() {
        updatePrefs(this.mSession.rebuild(AppStateSmsPremBridge.FILTER_APP_PREMIUM_SMS, ApplicationsState.ALPHA_COMPARATOR));
    }

    public void onExtraInfoUpdated() {
        update();
    }

    public void onRebuildComplete(ArrayList<AppEntry> apps) {
        updatePrefs(apps);
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
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
    }
}
