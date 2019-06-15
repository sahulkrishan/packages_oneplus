package com.android.settings.bluetooth;

import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public final class BluetoothPairingService extends Service {
    private static final String ACTION_DISMISS_PAIRING = "com.android.settings.bluetooth.ACTION_DISMISS_PAIRING";
    private static final String BLUETOOTH_NOTIFICATION_CHANNEL = "bluetooth_notification_channel";
    private static final int NOTIFICATION_ID = 17301632;
    private static final String TAG = "BluetoothPairingService";
    private final BroadcastReceiver mCancelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int bondState;
            if (action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
                if (!(bondState == 10 || bondState == 12)) {
                    return;
                }
            } else if (action.equals(BluetoothPairingService.ACTION_DISMISS_PAIRING)) {
                String str = BluetoothPairingService.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Notification cancel ");
                stringBuilder.append(BluetoothPairingService.this.mDevice.getAddress());
                stringBuilder.append(" (");
                stringBuilder.append(BluetoothPairingService.this.mDevice.getName());
                stringBuilder.append(")");
                Log.d(str, stringBuilder.toString());
                BluetoothPairingService.this.mDevice.cancelPairingUserInput();
            } else {
                bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
                String str2 = BluetoothPairingService.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Dismiss pairing for ");
                stringBuilder2.append(BluetoothPairingService.this.mDevice.getAddress());
                stringBuilder2.append(" (");
                stringBuilder2.append(BluetoothPairingService.this.mDevice.getName());
                stringBuilder2.append("), BondState: ");
                stringBuilder2.append(bondState);
                Log.d(str2, stringBuilder2.toString());
            }
            BluetoothPairingService.this.stopForeground(true);
            BluetoothPairingService.this.stopSelf();
        }
    };
    private BluetoothDevice mDevice;
    private boolean mRegistered = false;

    public static Intent getPairingDialogIntent(Context context, Intent intent) {
        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        int type = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
        Intent pairingIntent = new Intent();
        pairingIntent.setClass(context, BluetoothPairingDialog.class);
        pairingIntent.putExtra("android.bluetooth.device.extra.DEVICE", device);
        pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_VARIANT", type);
        if (type == 2 || type == 4 || type == 5) {
            pairingIntent.putExtra("android.bluetooth.device.extra.PAIRING_KEY", intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE));
        }
        pairingIntent.setAction("android.bluetooth.device.action.PAIRING_REQUEST");
        pairingIntent.setFlags(268435456);
        return pairingIntent;
    }

    public void onCreate() {
        ((NotificationManager) getSystemService("notification")).createNotificationChannel(new NotificationChannel(BLUETOOTH_NOTIFICATION_CHANNEL, getString(R.string.bluetooth), 4));
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent intent2 = intent;
        if (intent2 == null) {
            Log.e(TAG, "Can't start: null intent!");
            stopSelf();
            return 2;
        }
        Resources res = getResources();
        Builder builder = new Builder(this, BLUETOOTH_NOTIFICATION_CHANNEL).setSmallIcon(NOTIFICATION_ID).setTicker(res.getString(R.string.bluetooth_notif_ticker)).setLocalOnly(true);
        PendingIntent pairIntent = PendingIntent.getActivity(this, 0, getPairingDialogIntent(this, intent), Ints.MAX_POWER_OF_TWO);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_DISMISS_PAIRING), Ints.MAX_POWER_OF_TWO);
        this.mDevice = (BluetoothDevice) intent2.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
        if (this.mDevice == null || this.mDevice.getBondState() == 11) {
            String name = intent2.getStringExtra("android.bluetooth.device.extra.NAME");
            if (TextUtils.isEmpty(name)) {
                BluetoothDevice device = (BluetoothDevice) intent2.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                name = device != null ? device.getAliasName() : res.getString(17039374);
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Show pairing notification for ");
            stringBuilder.append(this.mDevice.getAddress());
            stringBuilder.append(" (");
            stringBuilder.append(name);
            stringBuilder.append(")");
            Log.d(str, stringBuilder.toString());
            builder.setContentTitle(res.getString(R.string.bluetooth_notif_title)).setContentText(res.getString(R.string.bluetooth_notif_message, new Object[]{name})).setContentIntent(pairIntent).setDefaults(1).setColor(getColor(17170775)).addAction(new Action.Builder(0, res.getString(R.string.bluetooth_device_context_pair_connect), pairIntent).build()).addAction(new Action.Builder(0, res.getString(17039360), dismissIntent).build());
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
            filter.addAction("android.bluetooth.device.action.PAIRING_CANCEL");
            filter.addAction(ACTION_DISMISS_PAIRING);
            registerReceiver(this.mCancelReceiver, filter);
            this.mRegistered = true;
            startForeground(NOTIFICATION_ID, builder.getNotification());
            return 3;
        }
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Device ");
        stringBuilder2.append(this.mDevice);
        stringBuilder2.append(" not bonding: ");
        stringBuilder2.append(this.mDevice.getBondState());
        Log.w(str2, stringBuilder2.toString());
        stopSelf();
        return 2;
    }

    public void onDestroy() {
        if (this.mRegistered) {
            unregisterReceiver(this.mCancelReceiver);
            this.mRegistered = false;
        }
        stopForeground(true);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
