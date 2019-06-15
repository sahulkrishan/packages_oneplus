package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;
import android.text.Editable;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.settings.R;
import com.android.settings.bluetooth.BluetoothPairingDialogFragment.BluetoothPairingDialogListener;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import java.util.Locale;

public class BluetoothPairingController implements OnCheckedChangeListener, BluetoothPairingDialogListener {
    private static final int BLUETOOTH_PASSKEY_MAX_LENGTH = 6;
    private static final int BLUETOOTH_PIN_MAX_LENGTH = 16;
    public static final int CONFIRMATION_DIALOG = 1;
    public static final int DISPLAY_PASSKEY_DIALOG = 2;
    public static final int INVALID_DIALOG_TYPE = -1;
    private static final String TAG = "BTPairingController";
    public static final int USER_ENTRY_DIALOG = 0;
    private LocalBluetoothManager mBluetoothManager;
    @VisibleForTesting
    BluetoothDevice mDevice;
    private String mDeviceName;
    private int mPasskey;
    private String mPasskeyFormatted;
    private boolean mPbapAllowed;
    private LocalBluetoothProfile mPbapClientProfile;
    @VisibleForTesting
    int mType;
    private String mUserInput;

    public BluetoothPairingController(Intent intent, Context context) {
        this.mBluetoothManager = Utils.getLocalBtManager(context);
        this.mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        String message = "";
        if (this.mBluetoothManager == null) {
            throw new IllegalStateException("Could not obtain LocalBluetoothManager");
        } else if (this.mDevice != null) {
            this.mType = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
            this.mPasskey = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE);
            this.mDeviceName = this.mBluetoothManager.getCachedDeviceManager().getName(this.mDevice);
            this.mPbapClientProfile = this.mBluetoothManager.getProfileManager().getPbapClientProfile();
            this.mPasskeyFormatted = formatKey(this.mPasskey);
        } else {
            throw new IllegalStateException("Could not find BluetoothDevice");
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            this.mPbapAllowed = true;
        } else {
            this.mPbapAllowed = false;
        }
    }

    public void onDialogPositiveClick(BluetoothPairingDialogFragment dialog) {
        if (this.mPbapAllowed) {
            this.mDevice.setPhonebookAccessPermission(1);
        } else {
            this.mDevice.setPhonebookAccessPermission(2);
        }
        if (getDialogType() == 0) {
            onPair(this.mUserInput);
        } else {
            onPair(null);
        }
    }

    public void onDialogNegativeClick(BluetoothPairingDialogFragment dialog) {
        this.mDevice.setPhonebookAccessPermission(2);
        onCancel();
    }

    public int getDialogType() {
        switch (this.mType) {
            case 0:
            case 1:
            case 7:
                return 0;
            case 2:
            case 3:
            case 6:
                return 1;
            case 4:
            case 5:
                return 2;
            default:
                return -1;
        }
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public boolean isProfileReady() {
        return this.mPbapClientProfile != null && this.mPbapClientProfile.isProfileReady();
    }

    public boolean getContactSharingState() {
        switch (this.mDevice.getPhonebookAccessPermission()) {
            case 1:
                return true;
            case 2:
                return false;
            default:
                return this.mDevice.getBluetoothClass() != null && this.mDevice.getBluetoothClass().getDeviceClass() == 1032;
        }
    }

    public void setContactSharingState() {
        if (this.mDevice.getPhonebookAccessPermission() != 1 && this.mDevice.getPhonebookAccessPermission() != 2) {
            if (this.mDevice.getBluetoothClass().getDeviceClass() == 1032) {
                onCheckedChanged(null, true);
            } else {
                onCheckedChanged(null, false);
            }
        }
    }

    public boolean isPasskeyValid(Editable s) {
        boolean requires16Digits = this.mType == 7;
        if ((s.length() < 16 || !requires16Digits) && (s.length() <= 0 || requires16Digits)) {
            return false;
        }
        return true;
    }

    public int getDeviceVariantMessageId() {
        int i = this.mType;
        if (i != 7) {
            switch (i) {
                case 0:
                    break;
                case 1:
                    return R.string.bluetooth_enter_passkey_other_device;
                default:
                    return -1;
            }
        }
        return R.string.bluetooth_enter_pin_other_device;
    }

    public int getDeviceVariantMessageHintId() {
        int i = this.mType;
        if (i == 7) {
            return R.string.bluetooth_pin_values_hint_16_digits;
        }
        switch (i) {
            case 0:
            case 1:
                return R.string.bluetooth_pin_values_hint;
            default:
                return -1;
        }
    }

    public int getDeviceMaxPasskeyLength() {
        int i = this.mType;
        if (i != 7) {
            switch (i) {
                case 0:
                    break;
                case 1:
                    return 6;
                default:
                    return 0;
            }
        }
        return 16;
    }

    public boolean pairingCodeIsAlphanumeric() {
        if (this.mType != 1) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void notifyDialogDisplayed() {
        if (this.mType == 4) {
            this.mDevice.setPairingConfirmation(true);
        } else if (this.mType == 5) {
            this.mDevice.setPin(BluetoothDevice.convertPinToBytes(this.mPasskeyFormatted));
        }
    }

    public boolean isDisplayPairingKeyVariant() {
        switch (this.mType) {
            case 4:
            case 5:
            case 6:
                return true;
            default:
                return false;
        }
    }

    public boolean hasPairingContent() {
        int i = this.mType;
        if (i != 2) {
            switch (i) {
                case 4:
                case 5:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public String getPairingContent() {
        if (hasPairingContent()) {
            return this.mPasskeyFormatted;
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void updateUserInput(String input) {
        this.mUserInput = input;
    }

    private String formatKey(int passkey) {
        int i = this.mType;
        if (i != 2) {
            switch (i) {
                case 4:
                    break;
                case 5:
                    return String.format("%04d", new Object[]{Integer.valueOf(passkey)});
                default:
                    return null;
            }
        }
        return String.format(Locale.US, "%06d", new Object[]{Integer.valueOf(passkey)});
    }

    private void onPair(String passkey) {
        Log.d(TAG, "Pairing dialog accepted");
        switch (this.mType) {
            case 0:
            case 7:
                byte[] pinBytes = BluetoothDevice.convertPinToBytes(passkey);
                if (pinBytes != null) {
                    this.mDevice.setPin(pinBytes);
                    break;
                }
                return;
            case 1:
                this.mDevice.setPasskey(Integer.parseInt(passkey));
                break;
            case 2:
            case 3:
                this.mDevice.setPairingConfirmation(true);
                break;
            case 4:
            case 5:
                break;
            case 6:
                this.mDevice.setRemoteOutOfBandData();
                break;
            default:
                Log.e(TAG, "Incorrect pairing type received");
                break;
        }
    }

    public void onCancel() {
        Log.d(TAG, "Pairing dialog canceled");
        this.mDevice.cancelPairingUserInput();
    }

    public boolean deviceEquals(BluetoothDevice device) {
        return this.mDevice == device;
    }
}
