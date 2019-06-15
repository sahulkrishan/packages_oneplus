package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class BluetoothPermissionActivity extends AlertActivity implements OnClickListener, OnPreferenceChangeListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "BluetoothPermissionActivity";
    private BluetoothDevice mDevice;
    private Button mOkButton;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL") && intent.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2) == BluetoothPermissionActivity.this.mRequestType) {
                if (BluetoothPermissionActivity.this.mDevice.equals((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"))) {
                    BluetoothPermissionActivity.this.dismissDialog();
                }
            }
        }
    };
    private boolean mReceiverRegistered = false;
    private int mRequestType = 0;
    private String mReturnClass = null;
    private String mReturnPackage = null;
    private View mView;
    private TextView messageView;

    private void dismissDialog() {
        dismiss();
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if (i.getAction().equals("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST")) {
            this.mDevice = (BluetoothDevice) i.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            this.mReturnPackage = i.getStringExtra("android.bluetooth.device.extra.PACKAGE_NAME");
            this.mReturnClass = i.getStringExtra("android.bluetooth.device.extra.CLASS_NAME");
            this.mRequestType = i.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("onCreate() Request type: ");
            stringBuilder.append(this.mRequestType);
            Log.i(str, stringBuilder.toString());
            if (this.mRequestType == 1) {
                showDialog(getString(R.string.bluetooth_connection_permission_request), this.mRequestType);
            } else if (this.mRequestType == 2) {
                showDialog(getString(R.string.bluetooth_phonebook_request), this.mRequestType);
            } else if (this.mRequestType == 3) {
                showDialog(getString(R.string.bluetooth_map_request), this.mRequestType);
            } else if (this.mRequestType == 4) {
                showDialog(getString(R.string.bluetooth_sap_request), this.mRequestType);
            } else {
                str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Error: bad request type: ");
                stringBuilder2.append(this.mRequestType);
                Log.e(str, stringBuilder2.toString());
                finish();
                return;
            }
            registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL"));
            this.mReceiverRegistered = true;
            return;
        }
        Log.e(TAG, "Error: this activity may be started only with intent ACTION_CONNECTION_ACCESS_REQUEST");
        finish();
    }

    private void showDialog(String title, int requestType) {
        AlertParams p = this.mAlertParams;
        p.mTitle = title;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("showDialog() Request type: ");
        stringBuilder.append(this.mRequestType);
        stringBuilder.append(" this: ");
        stringBuilder.append(this);
        Log.i(str, stringBuilder.toString());
        switch (requestType) {
            case 1:
                p.mView = createConnectionDialogView();
                break;
            case 2:
                p.mView = createPhonebookDialogView();
                break;
            case 3:
                p.mView = createMapDialogView();
                break;
            case 4:
                p.mView = createSapDialogView();
                break;
        }
        p.mPositiveButtonText = getString(R.string.yes);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.no);
        p.mNegativeButtonListener = this;
        this.mOkButton = this.mAlert.getButton(-1);
        setupAlert();
    }

    public void onBackPressed() {
        Log.i(TAG, "Back button pressed! ignoring");
    }

    private View createConnectionDialogView() {
        String mRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_connection_dialog_text, new Object[]{mRemoteName}));
        return this.mView;
    }

    private View createPhonebookDialogView() {
        String mRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_pb_acceptance_dialog_text, new Object[]{mRemoteName, mRemoteName}));
        return this.mView;
    }

    private View createMapDialogView() {
        String mRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_map_acceptance_dialog_text, new Object[]{mRemoteName, mRemoteName}));
        return this.mView;
    }

    private View createSapDialogView() {
        String mRemoteName = Utils.createRemoteName(this, this.mDevice);
        this.mView = getLayoutInflater().inflate(R.layout.bluetooth_access, null);
        this.messageView = (TextView) this.mView.findViewById(R.id.message);
        this.messageView.setText(getString(R.string.bluetooth_sap_acceptance_dialog_text, new Object[]{mRemoteName, mRemoteName}));
        return this.mView;
    }

    private void onPositive() {
        Log.d(TAG, "onPositive");
        sendReplyIntentToReceiver(true, true);
        finish();
    }

    private void onNegative() {
        Log.d(TAG, "onNegative");
        boolean always = true;
        if (this.mRequestType == 3) {
            LocalBluetoothManager bluetoothManager = Utils.getLocalBtManager(this);
            CachedBluetoothDeviceManager cachedDeviceManager = bluetoothManager.getCachedDeviceManager();
            CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(this.mDevice);
            if (cachedDevice == null) {
                cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(), bluetoothManager.getProfileManager(), this.mDevice);
            }
            always = cachedDevice.checkAndIncreaseMessageRejectionCount();
        }
        sendReplyIntentToReceiver(false, always);
    }

    private void sendReplyIntentToReceiver(boolean allowed, boolean always) {
        int i;
        Intent intent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        if (!(this.mReturnPackage == null || this.mReturnClass == null)) {
            intent.setClassName(this.mReturnPackage, this.mReturnClass);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sendReplyIntentToReceiver() Request type: ");
        stringBuilder.append(this.mRequestType);
        stringBuilder.append(" mReturnPackage");
        stringBuilder.append(this.mReturnPackage);
        stringBuilder.append(" mReturnClass");
        stringBuilder.append(this.mReturnClass);
        Log.i(str, stringBuilder.toString());
        str = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
        if (allowed) {
            i = 1;
        } else {
            i = 2;
        }
        intent.putExtra(str, i);
        intent.putExtra("android.bluetooth.device.extra.ALWAYS_ALLOWED", always);
        intent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
        intent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
        sendBroadcast(intent, "android.permission.BLUETOOTH_ADMIN");
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                onNegative();
                return;
            case -1:
                onPositive();
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mReceiverRegistered) {
            unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
