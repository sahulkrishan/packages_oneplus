package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.MapProfile;
import com.android.settingslib.bluetooth.PanProfile;
import com.android.settingslib.bluetooth.PbapServerProfile;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.List;

public class BluetoothDetailsProfilesController extends BluetoothDetailsController implements OnPreferenceClickListener {
    @VisibleForTesting
    static final String HIGH_QUALITY_AUDIO_PREF_TAG = "A2dpProfileHighQualityAudio";
    private static final String KEY_PROFILES_GROUP = "bluetooth_profiles";
    private CachedBluetoothDevice mCachedDevice;
    private LocalBluetoothManager mManager;
    private LocalBluetoothProfileManager mProfileManager = this.mManager.getProfileManager();
    private PreferenceCategory mProfilesContainer;

    public BluetoothDetailsProfilesController(Context context, PreferenceFragment fragment, LocalBluetoothManager manager, CachedBluetoothDevice device, Lifecycle lifecycle) {
        super(context, fragment, device, lifecycle);
        this.mManager = manager;
        this.mCachedDevice = device;
        lifecycle.addObserver(this);
    }

    /* Access modifiers changed, original: protected */
    public void init(PreferenceScreen screen) {
        this.mProfilesContainer = (PreferenceCategory) screen.findPreference(getPreferenceKey());
        refresh();
    }

    private SwitchPreference createProfilePreference(Context context, LocalBluetoothProfile profile) {
        SwitchPreference pref = new SwitchPreference(context);
        pref.setKey(profile.toString());
        pref.setTitle(profile.getNameResource(this.mCachedDevice.getDevice()));
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    private void refreshProfilePreference(SwitchPreference profilePref, LocalBluetoothProfile profile) {
        BluetoothDevice device = this.mCachedDevice.getDevice();
        profilePref.setEnabled(this.mCachedDevice.isBusy() ^ 1);
        if (profile instanceof MapProfile) {
            profilePref.setChecked(this.mCachedDevice.getMessagePermissionChoice() == 1);
        } else if (profile instanceof PbapServerProfile) {
            profilePref.setChecked(this.mCachedDevice.getPhonebookPermissionChoice() == 1);
        } else if (profile instanceof PanProfile) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("device : ");
            stringBuilder.append(device);
            stringBuilder.append(" the device connection status : ");
            stringBuilder.append(profile.getConnectionStatus(device));
            Log.d(str, stringBuilder.toString());
            profilePref.setChecked(profile.getConnectionStatus(device) == 2);
        } else {
            profilePref.setChecked(profile.isPreferred(device));
        }
        if (profile instanceof A2dpProfile) {
            A2dpProfile a2dp = (A2dpProfile) profile;
            SwitchPreference highQualityPref = (SwitchPreference) this.mProfilesContainer.findPreference(HIGH_QUALITY_AUDIO_PREF_TAG);
            if (highQualityPref == null) {
                return;
            }
            if (a2dp.isPreferred(device) && a2dp.supportsHighQualityAudio(device)) {
                highQualityPref.setVisible(true);
                highQualityPref.setTitle((CharSequence) a2dp.getHighQualityAudioOptionLabel(device));
                highQualityPref.setChecked(a2dp.isHighQualityAudioEnabled(device));
                highQualityPref.setEnabled(1 ^ this.mCachedDevice.isBusy());
                return;
            }
            highQualityPref.setVisible(false);
        }
    }

    private void enableProfile(LocalBluetoothProfile profile, BluetoothDevice device, SwitchPreference profilePref) {
        if (profile instanceof PbapServerProfile) {
            this.mCachedDevice.setPhonebookPermissionChoice(1);
            return;
        }
        if (profile instanceof MapProfile) {
            this.mCachedDevice.setMessagePermissionChoice(1);
        }
        profile.setPreferred(device, true);
        this.mCachedDevice.connectProfile(profile);
    }

    private void disableProfile(LocalBluetoothProfile profile, BluetoothDevice device, SwitchPreference profilePref) {
        this.mCachedDevice.disconnect(profile);
        profile.setPreferred(device, false);
        if (profile instanceof MapProfile) {
            this.mCachedDevice.setMessagePermissionChoice(2);
        } else if (profile instanceof PbapServerProfile) {
            this.mCachedDevice.setPhonebookPermissionChoice(2);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        LocalBluetoothProfile profile = this.mProfileManager.getProfileByName(preference.getKey());
        if (profile == null) {
            PbapServerProfile psp = this.mManager.getProfileManager().getPbapProfile();
            if (!TextUtils.equals(preference.getKey(), psp.toString())) {
                return false;
            }
            profile = psp;
        }
        SwitchPreference profilePref = (SwitchPreference) preference;
        BluetoothDevice device = this.mCachedDevice.getDevice();
        String name = BluetoothDetailsProfilesController.class.getName();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("profilePref title = ");
        stringBuilder.append(profilePref.getTitle());
        stringBuilder.append(" device = ");
        stringBuilder.append(device);
        stringBuilder.append(" profilePref.isChecked() = ");
        stringBuilder.append(profilePref.isChecked());
        Log.w(name, stringBuilder.toString());
        if (profilePref.isChecked()) {
            enableProfile(profile, device, profilePref);
        } else {
            disableProfile(profile, device, profilePref);
        }
        refreshProfilePreference(profilePref, profile);
        return true;
    }

    private List<LocalBluetoothProfile> getProfiles() {
        List<LocalBluetoothProfile> result = this.mCachedDevice.getConnectableProfiles();
        if (this.mCachedDevice.getPhonebookPermissionChoice() != 0) {
            result.add(this.mManager.getProfileManager().getPbapProfile());
        }
        MapProfile mapProfile = this.mManager.getProfileManager().getMapProfile();
        if (this.mCachedDevice.getMessagePermissionChoice() != 0) {
            result.add(mapProfile);
        }
        return result;
    }

    private void maybeAddHighQualityAudioPref(LocalBluetoothProfile profile) {
        if (profile instanceof A2dpProfile) {
            BluetoothDevice device = this.mCachedDevice.getDevice();
            A2dpProfile a2dp = (A2dpProfile) profile;
            if (device != null && a2dp.supportsHighQualityAudio(device)) {
                SwitchPreference highQualityAudioPref = new SwitchPreference(this.mProfilesContainer.getContext());
                highQualityAudioPref.setKey(HIGH_QUALITY_AUDIO_PREF_TAG);
                highQualityAudioPref.setVisible(false);
                highQualityAudioPref.setOnPreferenceClickListener(new -$$Lambda$BluetoothDetailsProfilesController$pv2kZi3KDLDrPBqbb1ECR74MeRo(this, a2dp));
                this.mProfilesContainer.addPreference(highQualityAudioPref);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void refresh() {
        SwitchPreference pref;
        for (LocalBluetoothProfile profile : getProfiles()) {
            pref = (SwitchPreference) this.mProfilesContainer.findPreference(profile.toString());
            if (pref == null) {
                pref = createProfilePreference(this.mProfilesContainer.getContext(), profile);
                this.mProfilesContainer.addPreference(pref);
                maybeAddHighQualityAudioPref(profile);
            }
            refreshProfilePreference(pref, profile);
        }
        for (LocalBluetoothProfile profile2 : this.mCachedDevice.getRemovedProfiles()) {
            pref = (SwitchPreference) this.mProfilesContainer.findPreference(profile2.toString());
            if (pref != null) {
                this.mProfilesContainer.removePreference(pref);
            }
        }
    }

    public String getPreferenceKey() {
        return KEY_PROFILES_GROUP;
    }
}
