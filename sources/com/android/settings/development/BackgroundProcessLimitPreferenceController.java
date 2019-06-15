package com.android.settings.development;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BackgroundProcessLimitPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String APP_PROCESS_LIMIT_KEY = "app_process_limit";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public BackgroundProcessLimitPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(R.array.app_process_limit_values);
        this.mListSummaries = context.getResources().getStringArray(R.array.app_process_limit_entries);
    }

    public String getPreferenceKey() {
        return APP_PROCESS_LIMIT_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeAppProcessLimitOptions(newValue);
        updateAppProcessLimitOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateAppProcessLimitOptions();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeAppProcessLimitOptions(null);
    }

    private void updateAppProcessLimitOptions() {
        try {
            int limit = getActivityManagerService().getProcessLimit();
            int index = 0;
            for (int i = 0; i < this.mListValues.length; i++) {
                if (Integer.parseInt(this.mListValues[i]) >= limit) {
                    index = i;
                    break;
                }
            }
            ListPreference listPreference = this.mPreference;
            listPreference.setValue(this.mListValues[index]);
            listPreference.setSummary(this.mListSummaries[index]);
        } catch (RemoteException e) {
        }
    }

    private void writeAppProcessLimitOptions(Object newValue) {
        int limit;
        if (newValue != null) {
            try {
                limit = Integer.parseInt(newValue.toString());
            } catch (RemoteException e) {
                return;
            }
        }
        limit = -1;
        getActivityManagerService().setProcessLimit(limit);
        updateAppProcessLimitOptions();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public IActivityManager getActivityManagerService() {
        return ActivityManager.getService();
    }
}
