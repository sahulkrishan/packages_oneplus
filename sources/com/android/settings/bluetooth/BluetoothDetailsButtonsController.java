package com.android.settings.bluetooth;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class BluetoothDetailsButtonsController extends BluetoothDetailsController {
    private static final String KEY_ACTION_BUTTONS = "action_buttons";
    private ActionButtonPreference mActionButtons;
    private boolean mConnectButtonInitialized;
    private boolean mIsConnected;

    public BluetoothDetailsButtonsController(Context context, PreferenceFragment fragment, CachedBluetoothDevice device, Lifecycle lifecycle) {
        super(context, fragment, device, lifecycle);
        this.mIsConnected = device.isConnected();
    }

    private void onForgetButtonPressed() {
        ForgetDeviceDialogFragment.newInstance(this.mCachedDevice.getAddress()).show(this.mFragment.getFragmentManager(), ForgetDeviceDialogFragment.TAG);
    }

    /* Access modifiers changed, original: protected */
    public void init(PreferenceScreen screen) {
        this.mActionButtons = ((ActionButtonPreference) screen.findPreference(getPreferenceKey())).setButton1Text(R.string.forget).setButton1OnClickListener(new -$$Lambda$BluetoothDetailsButtonsController$10mSfoM1rAEvasn6gc-o1iWQgIA(this)).setButton1Positive(false).setButton1Enabled(true);
    }

    /* Access modifiers changed, original: protected */
    public void refresh() {
        this.mActionButtons.setButton2Enabled(this.mCachedDevice.isBusy() ^ 1);
        boolean previouslyConnected = this.mIsConnected;
        this.mIsConnected = this.mCachedDevice.isConnected();
        if (this.mIsConnected) {
            if (!this.mConnectButtonInitialized || !previouslyConnected) {
                this.mActionButtons.setButton2Text(R.string.bluetooth_device_context_disconnect).setButton2OnClickListener(new -$$Lambda$BluetoothDetailsButtonsController$AbsgPn9bfqFfvfi3BgeGPbSW3X0(this)).setButton2Positive(false);
                this.mConnectButtonInitialized = true;
            }
        } else if (!this.mConnectButtonInitialized || previouslyConnected) {
            this.mActionButtons.setButton2Text(R.string.bluetooth_device_context_connect).setButton2OnClickListener(new -$$Lambda$BluetoothDetailsButtonsController$eZ36ezumIpXzpP7dOOnqn-gI5Uk(this)).setButton2Positive(true);
            this.mConnectButtonInitialized = true;
        }
    }

    public String getPreferenceKey() {
        return KEY_ACTION_BUTTONS;
    }
}
