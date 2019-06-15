package com.android.settings.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class RequestPermissionActivity extends Activity implements OnClickListener {
    private static final int MAX_DISCOVERABLE_TIMEOUT = 3600;
    static final int REQUEST_DISABLE = 3;
    static final int REQUEST_ENABLE = 1;
    static final int REQUEST_ENABLE_DISCOVERABLE = 2;
    private static final String TAG = "RequestPermissionActivity";
    private CharSequence mAppLabel;
    private AlertDialog mDialog;
    private LocalBluetoothAdapter mLocalAdapter;
    private BroadcastReceiver mReceiver;
    private int mRequest;
    private int mTimeout = 120;

    private final class StateChangeReceiver extends BroadcastReceiver {
        private static final long TOGGLE_TIMEOUT_MILLIS = 10000;

        public StateChangeReceiver() {
            RequestPermissionActivity.this.getWindow().getDecorView().postDelayed(new -$$Lambda$RequestPermissionActivity$StateChangeReceiver$q4ZilZjRzY7SLoogXiJIIa__yMA(this), TOGGLE_TIMEOUT_MILLIS);
        }

        public static /* synthetic */ void lambda$new$0(StateChangeReceiver stateChangeReceiver) {
            if (!RequestPermissionActivity.this.isFinishing() && !RequestPermissionActivity.this.isDestroyed()) {
                RequestPermissionActivity.this.cancelAndFinish();
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int currentState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
                switch (RequestPermissionActivity.this.mRequest) {
                    case 1:
                    case 2:
                        if (currentState == 12) {
                            RequestPermissionActivity.this.proceedAndFinish();
                            break;
                        }
                        break;
                    case 3:
                        if (currentState == 10) {
                            RequestPermissionActivity.this.proceedAndFinish();
                            break;
                        }
                        break;
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        if (parseIntent()) {
            finish();
            return;
        }
        int btState = this.mLocalAdapter.getState();
        Intent intent;
        String str;
        StringBuilder stringBuilder;
        if (this.mRequest != 3) {
            switch (btState) {
                case 10:
                case 11:
                case 13:
                    intent = new Intent(this, RequestPermissionHelperActivity.class);
                    intent.setAction(RequestPermissionHelperActivity.ACTION_INTERNAL_REQUEST_BT_ON);
                    intent.putExtra(RequestPermissionHelperActivity.EXTRA_APP_LABEL, this.mAppLabel);
                    if (this.mRequest == 2) {
                        intent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", this.mTimeout);
                    }
                    startActivityForResult(intent, 0);
                    break;
                case 12:
                    if (this.mRequest != 1) {
                        createDialog();
                        break;
                    } else {
                        proceedAndFinish();
                        break;
                    }
                default:
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown adapter state: ");
                    stringBuilder.append(btState);
                    Log.e(str, stringBuilder.toString());
                    cancelAndFinish();
                    break;
            }
        }
        switch (btState) {
            case 10:
            case 13:
                proceedAndFinish();
                break;
            case 11:
            case 12:
                intent = new Intent(this, RequestPermissionHelperActivity.class);
                intent.putExtra(RequestPermissionHelperActivity.EXTRA_APP_LABEL, this.mAppLabel);
                intent.setAction(RequestPermissionHelperActivity.ACTION_INTERNAL_REQUEST_BT_OFF);
                startActivityForResult(intent, 0);
                break;
            default:
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown adapter state: ");
                stringBuilder.append(btState);
                Log.e(str, stringBuilder.toString());
                cancelAndFinish();
                break;
        }
    }

    private void createDialog() {
        if (getResources().getBoolean(R.bool.auto_confirm_bluetooth_activation_dialog)) {
            onClick(null, -1);
            return;
        }
        Builder builder = new Builder(this);
        if (this.mReceiver != null) {
            switch (this.mRequest) {
                case 1:
                case 2:
                    builder.setMessage(getString(R.string.bluetooth_turning_on));
                    break;
                default:
                    builder.setMessage(getString(R.string.bluetooth_turning_off));
                    break;
            }
            builder.setCancelable(false);
        } else {
            CharSequence message;
            if (this.mTimeout == 0) {
                if (this.mAppLabel != null) {
                    message = getString(R.string.bluetooth_ask_lasting_discovery, new Object[]{this.mAppLabel});
                } else {
                    message = getString(R.string.bluetooth_ask_lasting_discovery_no_name);
                }
                builder.setMessage(message);
            } else {
                try {
                    if (this.mAppLabel != null) {
                        message = getString(R.string.bluetooth_ask_discovery, new Object[]{this.mAppLabel, Integer.valueOf(this.mTimeout)});
                    } else {
                        message = getString(R.string.bluetooth_ask_discovery_no_name, new Object[]{Integer.valueOf(this.mTimeout)});
                    }
                    builder.setMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            builder.setPositiveButton(getString(R.string.allow), this);
            builder.setNegativeButton(getString(R.string.deny), this);
        }
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    /* Access modifiers changed, original: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            cancelAndFinish();
            return;
        }
        switch (this.mRequest) {
            case 1:
            case 2:
                if (this.mLocalAdapter.getBluetoothState() != 12) {
                    this.mReceiver = new StateChangeReceiver();
                    registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                    createDialog();
                    break;
                }
                proceedAndFinish();
                break;
            case 3:
                if (this.mLocalAdapter.getBluetoothState() != 10) {
                    this.mReceiver = new StateChangeReceiver();
                    registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                    createDialog();
                    break;
                }
                proceedAndFinish();
                break;
            default:
                cancelAndFinish();
                break;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                setResult(0);
                finish();
                return;
            case -1:
                proceedAndFinish();
                return;
            default:
                return;
        }
    }

    private void proceedAndFinish() {
        int returnCode;
        if (this.mRequest == 1 || this.mRequest == 3) {
            returnCode = -1;
        } else if (this.mLocalAdapter.setScanMode(23, this.mTimeout)) {
            long endTime = System.currentTimeMillis() + (((long) this.mTimeout) * 1000);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this, endTime);
            if (this.mTimeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this, endTime);
            }
            returnCode = this.mTimeout;
            if (returnCode < 1) {
                returnCode = 1;
            }
        } else {
            returnCode = 0;
        }
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        setResult(returnCode);
        finish();
    }

    private void cancelAndFinish() {
        setResult(0);
        finish();
    }

    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return true;
        }
        if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_ENABLE")) {
            this.mRequest = 1;
        } else if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_DISABLE")) {
            this.mRequest = 3;
        } else if (intent.getAction().equals("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE")) {
            this.mRequest = 2;
            this.mTimeout = intent.getIntExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 120);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Setting Bluetooth Discoverable Timeout = ");
            stringBuilder.append(this.mTimeout);
            Log.d(str, stringBuilder.toString());
            if (this.mTimeout < 1 || this.mTimeout > 3600) {
                this.mTimeout = 120;
            }
        } else {
            Log.e(TAG, "Error: this activity may be started only with intent android.bluetooth.adapter.action.REQUEST_ENABLE or android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            setResult(0);
            return true;
        }
        LocalBluetoothManager manager = Utils.getLocalBtManager(this);
        if (manager == null) {
            Log.e(TAG, "Error: there's a problem starting Bluetooth");
            setResult(0);
            return true;
        }
        String packageName = getCallingPackage();
        if (TextUtils.isEmpty(packageName)) {
            packageName = getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
        }
        if (!TextUtils.isEmpty(packageName)) {
            try {
                this.mAppLabel = getPackageManager().getApplicationInfo(packageName, 0).loadSafeLabel(getPackageManager());
            } catch (NameNotFoundException e) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Couldn't find app with package name ");
                stringBuilder2.append(packageName);
                Log.e(str2, stringBuilder2.toString());
                setResult(0);
                return true;
            }
        }
        this.mLocalAdapter = manager.getBluetoothAdapter();
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }
}
