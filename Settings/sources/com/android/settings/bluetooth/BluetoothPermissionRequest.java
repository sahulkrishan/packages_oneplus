package com.android.settings.bluetooth;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public final class BluetoothPermissionRequest extends BroadcastReceiver {
    private static final String BLUETOOTH_NOTIFICATION_CHANNEL = "bluetooth_notification_channel";
    private static final boolean DEBUG = false;
    private static final int NOTIFICATION_ID = 17301632;
    private static final String NOTIFICATION_TAG_MAP = "Message Access";
    private static final String NOTIFICATION_TAG_PBAP = "Phonebook Access";
    private static final String NOTIFICATION_TAG_SAP = "SIM Access";
    private static final String TAG = "BluetoothPermissionRequest";
    Context mContext;
    BluetoothDevice mDevice;
    private NotificationChannel mNotificationChannel = null;
    int mRequestType;
    String mReturnClass = null;
    String mReturnPackage = null;

    public void onReceive(Context context, Intent intent) {
        Context context2 = context;
        Intent intent2 = intent;
        this.mContext = context2;
        String action = intent.getAction();
        if (action.equals("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST")) {
            UserManager um = (UserManager) context2.getSystemService("user");
            if (!um.isManagedProfile()) {
                this.mDevice = (BluetoothDevice) intent2.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                this.mRequestType = intent2.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 1);
                this.mReturnPackage = intent2.getStringExtra("android.bluetooth.device.extra.PACKAGE_NAME");
                this.mReturnClass = intent2.getStringExtra("android.bluetooth.device.extra.CLASS_NAME");
                if (!checkUserChoice()) {
                    Intent connectionAccessIntent = new Intent(action);
                    connectionAccessIntent.setClass(context2, BluetoothPermissionActivity.class);
                    connectionAccessIntent.setFlags(402653184);
                    connectionAccessIntent.setType(Integer.toString(this.mRequestType));
                    connectionAccessIntent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
                    connectionAccessIntent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
                    connectionAccessIntent.putExtra("android.bluetooth.device.extra.PACKAGE_NAME", this.mReturnPackage);
                    connectionAccessIntent.putExtra("android.bluetooth.device.extra.CLASS_NAME", this.mReturnClass);
                    String deviceName = null;
                    String deviceAddress = this.mDevice != null ? this.mDevice.getAddress() : null;
                    if (this.mDevice != null) {
                        deviceName = this.mDevice.getName();
                    }
                    if (((PowerManager) context2.getSystemService("power")).isScreenOn() && LocalBluetoothPreferences.shouldShowDialogInForeground(context2, deviceAddress, deviceName)) {
                        context2.startActivity(connectionAccessIntent);
                    } else {
                        String title;
                        String message;
                        Intent deleteIntent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
                        deleteIntent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
                        deleteIntent.putExtra("android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT", 2);
                        deleteIntent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
                        String deviceAlias = Utils.createRemoteName(context2, this.mDevice);
                        int i;
                        switch (this.mRequestType) {
                            case 2:
                                i = 0;
                                title = context2.getString(R.string.bluetooth_phonebook_request);
                                message = context2.getString(R.string.bluetooth_pb_acceptance_dialog_text, new Object[]{deviceAlias, deviceAlias});
                                break;
                            case 3:
                                title = context2.getString(R.string.bluetooth_map_request);
                                message = context2.getString(R.string.bluetooth_map_acceptance_dialog_text, new Object[]{deviceAlias, deviceAlias});
                                break;
                            case 4:
                                title = context2.getString(R.string.bluetooth_sap_request);
                                message = context2.getString(R.string.bluetooth_sap_acceptance_dialog_text, new Object[]{deviceAlias, deviceAlias});
                                break;
                            default:
                                i = 0;
                                title = context2.getString(R.string.bluetooth_connection_permission_request);
                                message = context2.getString(R.string.bluetooth_connection_dialog_text, new Object[]{deviceAlias, deviceAlias});
                                break;
                        }
                        NotificationManager notificationManager = (NotificationManager) context2.getSystemService("notification");
                        if (this.mNotificationChannel == null) {
                            this.mNotificationChannel = new NotificationChannel(BLUETOOTH_NOTIFICATION_CHANNEL, context2.getString(R.string.bluetooth), 4);
                            notificationManager.createNotificationChannel(this.mNotificationChannel);
                        } else {
                            String str = deviceAlias;
                        }
                        Notification notification = new Builder(context2, BLUETOOTH_NOTIFICATION_CHANNEL).setContentTitle(title).setTicker(message).setContentText(message).setSmallIcon(NOTIFICATION_ID).setAutoCancel(true).setPriority(2).setOnlyAlertOnce(false).setDefaults(-1).setContentIntent(PendingIntent.getActivity(context2, 0, connectionAccessIntent, 0)).setDeleteIntent(PendingIntent.getBroadcast(context2, 0, deleteIntent, 0)).setColor(context2.getColor(17170775)).setLocalOnly(true).build();
                        notification.flags |= 32;
                        notificationManager.notify(getNotificationTag(this.mRequestType), NOTIFICATION_ID, notification);
                    }
                }
            }
        } else if (action.equals("android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL")) {
            NotificationManager manager = (NotificationManager) context2.getSystemService("notification");
            this.mRequestType = intent2.getIntExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", 2);
            manager.cancel(getNotificationTag(this.mRequestType), NOTIFICATION_ID);
        }
    }

    private String getNotificationTag(int requestType) {
        if (requestType == 2) {
            return NOTIFICATION_TAG_PBAP;
        }
        if (this.mRequestType == 3) {
            return NOTIFICATION_TAG_MAP;
        }
        if (this.mRequestType == 4) {
            return NOTIFICATION_TAG_SAP;
        }
        return null;
    }

    private boolean checkUserChoice() {
        boolean processed = false;
        if (this.mRequestType != 2 && this.mRequestType != 3 && this.mRequestType != 4) {
            return false;
        }
        LocalBluetoothManager bluetoothManager = Utils.getLocalBtManager(this.mContext);
        CachedBluetoothDeviceManager cachedDeviceManager = bluetoothManager.getCachedDeviceManager();
        CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(this.mDevice);
        if (cachedDevice == null) {
            cachedDevice = cachedDeviceManager.addDevice(bluetoothManager.getBluetoothAdapter(), bluetoothManager.getProfileManager(), this.mDevice);
        }
        String intentName = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
        int phonebookPermission;
        String str;
        StringBuilder stringBuilder;
        if (this.mRequestType == 2) {
            phonebookPermission = cachedDevice.getPhonebookPermissionChoice();
            if (phonebookPermission != 0) {
                if (phonebookPermission == 1) {
                    sendReplyIntentToReceiver(true);
                    processed = true;
                } else if (phonebookPermission == 2) {
                    sendReplyIntentToReceiver(false);
                    processed = true;
                } else {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Bad phonebookPermission: ");
                    stringBuilder.append(phonebookPermission);
                    Log.e(str, stringBuilder.toString());
                }
            }
        } else if (this.mRequestType == 3) {
            phonebookPermission = cachedDevice.getMessagePermissionChoice();
            if (phonebookPermission != 0) {
                if (phonebookPermission == 1) {
                    sendReplyIntentToReceiver(true);
                    processed = true;
                } else if (phonebookPermission == 2) {
                    sendReplyIntentToReceiver(false);
                    processed = true;
                } else {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Bad messagePermission: ");
                    stringBuilder.append(phonebookPermission);
                    Log.e(str, stringBuilder.toString());
                }
            }
        } else if (this.mRequestType == 4) {
            phonebookPermission = cachedDevice.getSimPermissionChoice();
            if (phonebookPermission != 0) {
                if (phonebookPermission == 1) {
                    sendReplyIntentToReceiver(true);
                    processed = true;
                } else if (phonebookPermission == 2) {
                    sendReplyIntentToReceiver(false);
                    processed = true;
                } else {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Bad simPermission: ");
                    stringBuilder.append(phonebookPermission);
                    Log.e(str, stringBuilder.toString());
                }
            }
        }
        return processed;
    }

    private void sendReplyIntentToReceiver(boolean allowed) {
        int i;
        Intent intent = new Intent("android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        if (!(this.mReturnPackage == null || this.mReturnClass == null)) {
            intent.setClassName(this.mReturnPackage, this.mReturnClass);
        }
        String str = "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT";
        if (allowed) {
            i = 1;
        } else {
            i = 2;
        }
        intent.putExtra(str, i);
        intent.putExtra("android.bluetooth.device.extra.DEVICE", this.mDevice);
        intent.putExtra("android.bluetooth.device.extra.ACCESS_REQUEST_TYPE", this.mRequestType);
        this.mContext.sendBroadcast(intent, "android.permission.BLUETOOTH_ADMIN");
    }
}
