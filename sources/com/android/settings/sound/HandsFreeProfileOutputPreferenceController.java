package com.android.settings.sound;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.HearingAidProfile;

public class HandsFreeProfileOutputPreferenceController extends AudioSwitchPreferenceController {
    public HandsFreeProfileOutputPreferenceController(Context context, String key) {
        super(context, key);
    }

    public void updateState(Preference preference) {
        if (preference != null) {
            if (Utils.isAudioModeOngoingCall(this.mContext)) {
                this.mConnectedDevices.clear();
                this.mConnectedDevices.addAll(getConnectedHfpDevices());
                this.mConnectedDevices.addAll(getConnectedHearingAidDevices());
                int numDevices = this.mConnectedDevices.size();
                if (numDevices == 0) {
                    this.mPreference.setVisible(false);
                    CharSequence[] defaultMediaOutput = new CharSequence[]{this.mContext.getText(R.string.media_output_default_summary)};
                    this.mSelectedIndex = getDefaultDeviceIndex();
                    preference.setSummary(summary);
                    setPreference(defaultMediaOutput, defaultMediaOutput, preference);
                    return;
                }
                this.mPreference.setVisible(true);
                CharSequence[] mediaOutputs = new CharSequence[(numDevices + 1)];
                CharSequence[] mediaValues = new CharSequence[(numDevices + 1)];
                setupPreferenceEntries(mediaOutputs, mediaValues, findActiveDevice(0));
                if (isStreamFromOutputDevice(0, 67108864)) {
                    this.mSelectedIndex = getDefaultDeviceIndex();
                }
                setPreference(mediaOutputs, mediaValues, preference);
                return;
            }
            this.mPreference.setVisible(false);
            preference.setSummary(this.mContext.getText(R.string.media_output_default_summary));
        }
    }

    public void setActiveBluetoothDevice(BluetoothDevice device) {
        if (Utils.isAudioModeOngoingCall(this.mContext)) {
            HearingAidProfile hapProfile = this.mProfileManager.getHearingAidProfile();
            HeadsetProfile hfpProfile = this.mProfileManager.getHeadsetProfile();
            if (hapProfile == null || hfpProfile == null || device != null) {
                if (!(hapProfile == null || hapProfile.getHiSyncId(device) == 0)) {
                    hapProfile.setActiveDevice(device);
                }
                if (hfpProfile != null) {
                    hfpProfile.setActiveDevice(device);
                }
                return;
            }
            hfpProfile.setActiveDevice(null);
            hapProfile.setActiveDevice(null);
        }
    }
}
