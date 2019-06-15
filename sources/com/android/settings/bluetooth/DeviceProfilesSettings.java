package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.DunServerProfile;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.bluetooth.MapProfile;
import com.android.settingslib.bluetooth.PanProfile;
import com.android.settingslib.bluetooth.PbapServerProfile;

public final class DeviceProfilesSettings extends InstrumentedDialogFragment implements Callback, OnClickListener, View.OnClickListener {
    public static final String ARG_DEVICE_ADDRESS = "device_address";
    @VisibleForTesting
    static final String HIGH_QUALITY_AUDIO_PREF_TAG = "A2dpProfileHighQualityAudio";
    private static final String KEY_PBAP_SERVER = "PBAP Server";
    private static final String KEY_PROFILE_CONTAINER = "profile_container";
    private static final String KEY_UNPAIR = "unpair";
    private static final String TAG = "DeviceProfilesSettings";
    private CachedBluetoothDevice mCachedDevice;
    private AlertDialog mDisconnectDialog;
    private LocalBluetoothManager mManager;
    private ViewGroup mProfileContainer;
    private boolean mProfileGroupIsRemoved;
    private TextView mProfileLabel;
    private LocalBluetoothProfileManager mProfileManager;
    private View mRootView;

    public int getMetricsCategory() {
        return 539;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mManager = Utils.getLocalBtManager(getActivity());
        CachedBluetoothDeviceManager deviceManager = this.mManager.getCachedDeviceManager();
        BluetoothDevice remoteDevice = this.mManager.getBluetoothAdapter().getRemoteDevice(getArguments().getString("device_address"));
        this.mCachedDevice = deviceManager.findDevice(remoteDevice);
        if (this.mCachedDevice == null) {
            this.mCachedDevice = deviceManager.addDevice(this.mManager.getBluetoothAdapter(), this.mManager.getProfileManager(), remoteDevice);
        }
        this.mProfileManager = this.mManager.getProfileManager();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mRootView = LayoutInflater.from(getContext()).inflate(R.layout.device_profiles_settings, null);
        this.mProfileContainer = (ViewGroup) this.mRootView.findViewById(R.id.profiles_section);
        this.mProfileLabel = (TextView) this.mRootView.findViewById(R.id.profiles_label);
        ((EditText) this.mRootView.findViewById(R.id.name)).setText(this.mCachedDevice.getName(), BufferType.EDITABLE);
        return new Builder(getContext()).setView(this.mRootView).setNeutralButton(R.string.forget, this).setPositiveButton(R.string.okay, this).setTitle(R.string.bluetooth_preference_paired_devices).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -3) {
            this.mCachedDevice.unpair();
        } else if (which == -1) {
            this.mCachedDevice.setName(((EditText) this.mRootView.findViewById(R.id.name)).getText().toString());
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDisconnectDialog != null) {
            this.mDisconnectDialog.dismiss();
            this.mDisconnectDialog = null;
        }
        if (this.mCachedDevice != null) {
            this.mCachedDevice.unregisterCallback(this);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onResume() {
        super.onResume();
        this.mManager.setForegroundActivity(getActivity());
        if (this.mCachedDevice != null) {
            this.mCachedDevice.registerCallback(this);
            if (this.mCachedDevice.getBondState() == 10) {
                dismiss();
            } else {
                addPreferencesForProfiles();
                refresh();
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mCachedDevice != null) {
            this.mCachedDevice.unregisterCallback(this);
        }
        this.mManager.setForegroundActivity(null);
    }

    private void addPreferencesForProfiles() {
        this.mProfileContainer.removeAllViews();
        for (LocalBluetoothProfile profile : this.mCachedDevice.getConnectableProfiles()) {
            CheckBox pref = createProfilePreference(profile);
            if (!((profile instanceof PbapServerProfile) || (profile instanceof MapProfile))) {
                this.mProfileContainer.addView(pref);
            }
            if (profile instanceof A2dpProfile) {
                BluetoothDevice device = this.mCachedDevice.getDevice();
                A2dpProfile a2dpProfile = (A2dpProfile) profile;
                if (a2dpProfile.supportsHighQualityAudio(device)) {
                    CheckBox highQualityPref = new CheckBox(getActivity());
                    highQualityPref.setTag(HIGH_QUALITY_AUDIO_PREF_TAG);
                    highQualityPref.setOnClickListener(new -$$Lambda$DeviceProfilesSettings$qBNrFA8-Smm3qyHXDkezO-CS7tQ(a2dpProfile, device, highQualityPref));
                    highQualityPref.setVisibility(8);
                    this.mProfileContainer.addView(highQualityPref);
                }
                refreshProfilePreference(pref, profile);
            }
        }
        int pbapPermission = this.mCachedDevice.getPhonebookPermissionChoice();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("addPreferencesForProfiles: pbapPermission = ");
        stringBuilder.append(pbapPermission);
        Log.d(str, stringBuilder.toString());
        if (pbapPermission != 0) {
            this.mProfileContainer.addView(createProfilePreference(this.mManager.getProfileManager().getPbapProfile()));
        }
        MapProfile mapProfile = this.mManager.getProfileManager().getMapProfile();
        int mapPermission = this.mCachedDevice.getMessagePermissionChoice();
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("addPreferencesForProfiles: mapPermission = ");
        stringBuilder2.append(mapPermission);
        Log.d(str2, stringBuilder2.toString());
        if (mapPermission != 0) {
            this.mProfileContainer.addView(createProfilePreference(mapProfile));
        }
        showOrHideProfileGroup();
    }

    private void showOrHideProfileGroup() {
        int numProfiles = this.mProfileContainer.getChildCount();
        if (!this.mProfileGroupIsRemoved && numProfiles == 0) {
            this.mProfileContainer.setVisibility(8);
            this.mProfileLabel.setVisibility(8);
            this.mProfileGroupIsRemoved = true;
        } else if (this.mProfileGroupIsRemoved && numProfiles != 0) {
            this.mProfileContainer.setVisibility(0);
            this.mProfileLabel.setVisibility(0);
            this.mProfileGroupIsRemoved = false;
        }
    }

    private CheckBox createProfilePreference(LocalBluetoothProfile profile) {
        CheckBox pref = new CheckBox(getActivity());
        pref.setTag(profile.toString());
        pref.setText(profile.getNameResource(this.mCachedDevice.getDevice()));
        pref.setOnClickListener(this);
        refreshProfilePreference(pref, profile);
        return pref;
    }

    public void onClick(View v) {
        if (v instanceof CheckBox) {
            LocalBluetoothProfile prof = getProfileOf(v);
            if (prof != null) {
                onProfileClicked(prof, (CheckBox) v);
            } else {
                Log.e(TAG, "Error: Can't get the profile for the preference");
            }
        }
    }

    private void onProfileClicked(LocalBluetoothProfile profile, CheckBox profilePref) {
        BluetoothDevice device = this.mCachedDevice.getDevice();
        if (profilePref.isChecked()) {
            if (profile instanceof MapProfile) {
                this.mCachedDevice.setMessagePermissionChoice(1);
            }
            if (profile instanceof PbapServerProfile) {
                this.mCachedDevice.setPhonebookPermissionChoice(1);
                refreshProfilePreference(profilePref, profile);
                return;
            }
            if (!profile.isPreferred(device)) {
                profile.setPreferred(device, true);
                this.mCachedDevice.connectProfile(profile);
            } else if (profile instanceof PanProfile) {
                this.mCachedDevice.connectProfile(profile);
            } else {
                profile.setPreferred(device, false);
            }
            refreshProfilePreference(profilePref, profile);
        } else {
            profilePref.setChecked(true);
            askDisconnect(this.mManager.getForegroundActivity(), profile);
        }
    }

    private void askDisconnect(Context context, final LocalBluetoothProfile profile) {
        final CachedBluetoothDevice device = this.mCachedDevice;
        String name = device.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }
        String profileName = context.getString(profile.getNameResource(device.getDevice()));
        String title = context.getString(R.string.bluetooth_disable_profile_title);
        String message = context.getString(R.string.bluetooth_disable_profile_message, new Object[]{profileName, name});
        this.mDisconnectDialog = Utils.showDisconnectDialog(context, this.mDisconnectDialog, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    device.disconnect(profile);
                    profile.setPreferred(device.getDevice(), false);
                    if (profile instanceof MapProfile) {
                        device.setMessagePermissionChoice(2);
                    }
                    if (profile instanceof PbapServerProfile) {
                        device.setPhonebookPermissionChoice(2);
                    }
                }
                DeviceProfilesSettings.this.refreshProfilePreference(DeviceProfilesSettings.this.findProfile(profile.toString()), profile);
            }
        }, title, Html.fromHtml(message));
    }

    public void onDeviceAttributesChanged() {
        refresh();
    }

    private void refresh() {
        EditText deviceNameField = (EditText) this.mRootView.findViewById(R.id.name);
        if (deviceNameField != null) {
            deviceNameField.setText(this.mCachedDevice.getName());
            Utils.setEditTextCursorPosition(deviceNameField);
        }
        refreshProfiles();
    }

    private void refreshProfiles() {
        CheckBox profilePref;
        for (LocalBluetoothProfile profile : this.mCachedDevice.getConnectableProfiles()) {
            profilePref = findProfile(profile.toString());
            if (profilePref == null) {
                this.mProfileContainer.addView(createProfilePreference(profile));
            } else {
                refreshProfilePreference(profilePref, profile);
            }
        }
        for (LocalBluetoothProfile profile2 : this.mCachedDevice.getRemovedProfiles()) {
            profilePref = findProfile(profile2.toString());
            if (profilePref != null) {
                int pbapPermission;
                String str;
                StringBuilder stringBuilder;
                if (profile2 instanceof PbapServerProfile) {
                    pbapPermission = this.mCachedDevice.getPhonebookPermissionChoice();
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("refreshProfiles: pbapPermission = ");
                    stringBuilder.append(pbapPermission);
                    Log.d(str, stringBuilder.toString());
                    if (pbapPermission != 0) {
                    }
                }
                if (profile2 instanceof MapProfile) {
                    pbapPermission = this.mCachedDevice.getMessagePermissionChoice();
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("refreshProfiles: mapPermission = ");
                    stringBuilder.append(pbapPermission);
                    Log.d(str, stringBuilder.toString());
                    if (pbapPermission != 0) {
                    }
                }
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Removing ");
                stringBuilder2.append(profile2.toString());
                stringBuilder2.append(" from profile list");
                Log.d(str2, stringBuilder2.toString());
                this.mProfileContainer.removeView(profilePref);
            }
        }
        showOrHideProfileGroup();
    }

    private CheckBox findProfile(String profile) {
        return (CheckBox) this.mProfileContainer.findViewWithTag(profile);
    }

    private void refreshProfilePreference(CheckBox profilePref, LocalBluetoothProfile profile) {
        BluetoothDevice device = this.mCachedDevice.getDevice();
        profilePref.setEnabled(this.mCachedDevice.isBusy() ^ 1);
        if (profile instanceof MapProfile) {
            profilePref.setChecked(this.mCachedDevice.getMessagePermissionChoice() == 1);
        } else if (profile instanceof PbapServerProfile) {
            profilePref.setChecked(this.mCachedDevice.getPhonebookPermissionChoice() == 1);
        } else if (profile instanceof PanProfile) {
            profilePref.setChecked(profile.getConnectionStatus(device) == 2);
        } else if (profile instanceof DunServerProfile) {
            profilePref.setChecked(profile.getConnectionStatus(device) == 2);
        } else {
            profilePref.setChecked(profile.isPreferred(device));
        }
        if (profile instanceof A2dpProfile) {
            A2dpProfile a2dpProfile = (A2dpProfile) profile;
            View v = this.mProfileContainer.findViewWithTag(HIGH_QUALITY_AUDIO_PREF_TAG);
            if (v instanceof CheckBox) {
                CheckBox highQualityPref = (CheckBox) v;
                highQualityPref.setText(a2dpProfile.getHighQualityAudioOptionLabel(device));
                highQualityPref.setChecked(a2dpProfile.isHighQualityAudioEnabled(device));
                if (a2dpProfile.isPreferred(device)) {
                    v.setVisibility(0);
                    v.setEnabled(1 ^ this.mCachedDevice.isBusy());
                    return;
                }
                v.setVisibility(8);
            }
        }
    }

    private LocalBluetoothProfile getProfileOf(View v) {
        if (!(v instanceof CheckBox)) {
            return null;
        }
        String key = (String) v.getTag();
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            return this.mProfileManager.getProfileByName(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
