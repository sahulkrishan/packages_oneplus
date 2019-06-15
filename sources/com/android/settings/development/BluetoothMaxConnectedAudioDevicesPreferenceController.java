package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothMaxConnectedAudioDevicesPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String MAX_CONNECTED_AUDIO_DEVICES_PREFERENCE_KEY = "bluetooth_max_connected_audio_devices";
    @VisibleForTesting
    static final String MAX_CONNECTED_AUDIO_DEVICES_PROPERTY = "persist.bluetooth.maxconnectedaudiodevices";
    private final int mDefaultMaxConnectedAudioDevices = this.mContext.getResources().getInteger(17694740);

    public BluetoothMaxConnectedAudioDevicesPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return MAX_CONNECTED_AUDIO_DEVICES_PREFERENCE_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ListPreference listPreference = this.mPreference;
        CharSequence[] entries = listPreference.getEntries();
        entries[0] = String.format(entries[0].toString(), new Object[]{Integer.valueOf(this.mDefaultMaxConnectedAudioDevices)});
        listPreference.setEntries(entries);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String newValueString = newValue.toString();
        if (((ListPreference) preference).findIndexOfValue(newValueString) <= 0) {
            newValueString = "";
        }
        SystemProperties.set(MAX_CONNECTED_AUDIO_DEVICES_PROPERTY, newValueString);
        updateState(preference);
        return true;
    }

    public void updateState(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        CharSequence[] entries = listPreference.getEntries();
        CharSequence[] entriesvalue = listPreference.getEntryValues();
        CharSequence[] supportentries = new CharSequence[(this.mDefaultMaxConnectedAudioDevices + 1)];
        CharSequence[] supportentriesvalue = new CharSequence[(this.mDefaultMaxConnectedAudioDevices + 1)];
        for (int i = 0; i <= this.mDefaultMaxConnectedAudioDevices; i++) {
            supportentries[i] = entries[i];
            supportentriesvalue[i] = entriesvalue[i];
        }
        listPreference.setEntries(supportentries);
        listPreference.setEntryValues(supportentriesvalue);
        String currentValue = SystemProperties.get(MAX_CONNECTED_AUDIO_DEVICES_PROPERTY);
        int index = 0;
        if (!currentValue.isEmpty()) {
            index = listPreference.findIndexOfValue(currentValue);
            if (index < 0) {
                SystemProperties.set(MAX_CONNECTED_AUDIO_DEVICES_PROPERTY, "");
                index = 0;
            }
        }
        listPreference.setValueIndex(index);
        listPreference.setSummary(entries[index]);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        super.onDeveloperOptionsSwitchEnabled();
        updateState(this.mPreference);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(MAX_CONNECTED_AUDIO_DEVICES_PROPERTY, "");
        updateState(this.mPreference);
    }
}
