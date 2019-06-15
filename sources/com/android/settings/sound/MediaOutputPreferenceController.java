package com.android.settings.sound;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.HearingAidProfile;

public class MediaOutputPreferenceController extends AudioSwitchPreferenceController {
    public MediaOutputPreferenceController(Context context, String key) {
        super(context, key);
    }

    public void updateState(Preference preference) {
        if (preference != null) {
            if (isStreamFromOutputDevice(3, 32768)) {
                this.mPreference.setVisible(false);
                preference.setSummary(this.mContext.getText(R.string.media_output_summary_unavailable));
            } else if (Utils.isAudioModeOngoingCall(this.mContext)) {
                this.mPreference.setVisible(false);
                preference.setSummary(this.mContext.getText(R.string.media_out_summary_ongoing_call_state));
            } else {
                this.mConnectedDevices.clear();
                if (this.mAudioManager.getMode() == 0) {
                    this.mConnectedDevices.addAll(getConnectedA2dpDevices());
                    this.mConnectedDevices.addAll(getConnectedHearingAidDevices());
                }
                int numDevices = this.mConnectedDevices.size();
                CharSequence[] defaultMediaOutput;
                if (numDevices == 0) {
                    this.mPreference.setVisible(false);
                    defaultMediaOutput = new CharSequence[]{this.mContext.getText(R.string.media_output_default_summary)};
                    this.mSelectedIndex = getDefaultDeviceIndex();
                    preference.setSummary(summary);
                    setPreference(defaultMediaOutput, defaultMediaOutput, preference);
                    return;
                }
                this.mPreference.setVisible(true);
                defaultMediaOutput = new CharSequence[(numDevices + 1)];
                CharSequence[] mediaValues = new CharSequence[(numDevices + 1)];
                setupPreferenceEntries(defaultMediaOutput, mediaValues, findActiveDevice(3));
                if (isStreamFromOutputDevice(3, 67108864)) {
                    this.mSelectedIndex = getDefaultDeviceIndex();
                }
                setPreference(defaultMediaOutput, mediaValues, preference);
            }
        }
    }

    public void setActiveBluetoothDevice(BluetoothDevice device) {
        if (this.mAudioManager.getMode() == 0) {
            HearingAidProfile hapProfile = this.mProfileManager.getHearingAidProfile();
            A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
            if (hapProfile == null || a2dpProfile == null || device != null) {
                if (!(hapProfile == null || hapProfile.getHiSyncId(device) == 0)) {
                    hapProfile.setActiveDevice(device);
                }
                if (a2dpProfile != null) {
                    a2dpProfile.setActiveDevice(device);
                }
                return;
            }
            hapProfile.setActiveDevice(null);
            a2dpProfile.setActiveDevice(null);
        }
    }
}
