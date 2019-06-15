package com.android.settings.development;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class KeepActivitiesPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String IMMEDIATELY_DESTROY_ACTIVITIES_KEY = "immediately_destroy_activities";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    private IActivityManager mActivityManager;

    public KeepActivitiesPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return IMMEDIATELY_DESTROY_ACTIVITIES_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mActivityManager = getActivityManager();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeImmediatelyDestroyActivitiesOptions(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), "always_finish_activities", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeImmediatelyDestroyActivitiesOptions(false);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    private void writeImmediatelyDestroyActivitiesOptions(boolean isEnabled) {
        try {
            this.mActivityManager.setAlwaysFinish(isEnabled);
        } catch (RemoteException e) {
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public IActivityManager getActivityManager() {
        return ActivityManager.getService();
    }
}
