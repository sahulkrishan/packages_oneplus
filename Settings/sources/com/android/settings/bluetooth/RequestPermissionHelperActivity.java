package com.android.settings.bluetooth;

import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class RequestPermissionHelperActivity extends AlertActivity implements OnClickListener {
    public static final String ACTION_INTERNAL_REQUEST_BT_OFF = "com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_OFF";
    public static final String ACTION_INTERNAL_REQUEST_BT_ON = "com.android.settings.bluetooth.ACTION_INTERNAL_REQUEST_BT_ON";
    public static final String EXTRA_APP_LABEL = "com.android.settings.bluetooth.extra.APP_LABEL";
    private static final String TAG = "RequestPermissionHelperActivity";
    private CharSequence mAppLabel;
    private LocalBluetoothAdapter mLocalAdapter;
    private int mRequest;
    private int mTimeout = -1;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        if (parseIntent()) {
            if (getResources().getBoolean(R.bool.auto_confirm_bluetooth_activation_dialog)) {
                onClick(null, -1);
                dismiss();
            }
            createDialog();
            return;
        }
        finish();
    }

    /* Access modifiers changed, original: 0000 */
    public void createDialog() {
        AlertParams p = this.mAlertParams;
        int i = this.mRequest;
        CharSequence string;
        if (i != 1) {
            if (i == 3) {
                if (this.mAppLabel != null) {
                    string = getString(R.string.bluetooth_ask_disablement, new Object[]{this.mAppLabel});
                } else {
                    string = getString(R.string.bluetooth_ask_disablement_no_name);
                }
                p.mMessage = string;
            }
        } else if (this.mTimeout < 0) {
            if (this.mAppLabel != null) {
                string = getString(R.string.bluetooth_ask_enablement, new Object[]{this.mAppLabel});
            } else {
                string = getString(R.string.bluetooth_ask_enablement_no_name);
            }
            p.mMessage = string;
        } else if (this.mTimeout == 0) {
            if (this.mAppLabel != null) {
                string = getString(R.string.bluetooth_ask_enablement_and_lasting_discovery, new Object[]{this.mAppLabel});
            } else {
                string = getString(R.string.bluetooth_ask_enablement_and_lasting_discovery_no_name);
            }
            p.mMessage = string;
        } else {
            try {
                if (this.mAppLabel != null) {
                    string = getString(R.string.bluetooth_ask_enablement_and_discovery, new Object[]{this.mAppLabel, Integer.valueOf(this.mTimeout)});
                } else {
                    string = getString(R.string.bluetooth_ask_enablement_and_discovery_no_name, new Object[]{Integer.valueOf(this.mTimeout)});
                }
                p.mMessage = string;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        p.mPositiveButtonText = getString(R.string.allow);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.deny);
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (this.mRequest) {
            case 1:
            case 2:
                if (((UserManager) getSystemService(UserManager.class)).hasUserRestriction("no_bluetooth")) {
                    Intent intent = ((DevicePolicyManager) getSystemService(DevicePolicyManager.class)).createAdminSupportIntent("no_bluetooth");
                    if (intent != null) {
                        startActivity(intent);
                        return;
                    }
                    return;
                }
                this.mLocalAdapter.enable();
                setResult(-1);
                return;
            case 3:
                this.mLocalAdapter.disable();
                setResult(-1);
                return;
            default:
                return;
        }
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        if (ACTION_INTERNAL_REQUEST_BT_ON.equals(action)) {
            this.mRequest = 1;
            if (intent.hasExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION")) {
                this.mTimeout = intent.getIntExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 120);
            }
        } else if (!ACTION_INTERNAL_REQUEST_BT_OFF.equals(action)) {
            return false;
        } else {
            this.mRequest = 3;
        }
        LocalBluetoothManager manager = Utils.getLocalBtManager(this);
        if (manager == null) {
            Log.e(TAG, "Error: there's a problem starting Bluetooth");
            return false;
        }
        this.mAppLabel = getIntent().getCharSequenceExtra(EXTRA_APP_LABEL);
        this.mLocalAdapter = manager.getBluetoothAdapter();
        return true;
    }
}
