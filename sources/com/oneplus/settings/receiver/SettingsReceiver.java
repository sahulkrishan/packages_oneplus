package com.oneplus.settings.receiver;

import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.bluetooth.Utils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.google.common.primitives.Ints;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPPrefUtil;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.utils.OPZenModeUtils;

public class SettingsReceiver extends BroadcastReceiver {
    private static final String ACTION_OTG_AUTO_SHUTDOWN = "oneplus.intent.action.otg_auto_shutdown";
    private static final String ACTION_THREE_KEY = "com.oem.intent.action.THREE_KEY_MODE";
    private static final String BOOT_BROADCAST = "android.intent.action.BOOT_COMPLETED";
    private static final int NO_OEM_FONT_DIALOG = 0;
    private static final int SYSTEM_DEFALUT_FONT = 1;
    private AppOpsManager mAppOpsManager;
    private PackageManager mPackageManager;
    private UserManager mUm;
    private int mZenMode = 0;

    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        this.mZenMode = NotificationManager.from(context).getZenMode();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("action = ");
        stringBuilder.append(action);
        Log.d("SettingsReceiver", stringBuilder.toString());
        if ("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) {
            OPUtils.setAppUpdated(true);
        }
        if (OPConstants.ONEPLUS_ACTION_PACKAGE_REMOVED.equals(action)) {
            String pkgName = intent.getStringExtra("package_name");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("ACTION_PACKAGE_REMOVED pkgName= ");
            stringBuilder2.append(pkgName);
            Log.d("SettingsReceiver", stringBuilder2.toString());
            if (this.mAppOpsManager == null) {
                this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
            }
            if (this.mPackageManager == null) {
                this.mPackageManager = context.getPackageManager();
            }
            try {
                if (!OPUtils.hasMultiApp(context, pkgName)) {
                    this.mAppOpsManager.setMode(69, this.mPackageManager.getApplicationInfoAsUser(pkgName, 1, 0).uid, pkgName, 1);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (OPUtils.ONEPLUS_CLOUD_PACKAGE.equals(pkgName)) {
                OPUtils.isExist_Cloud_Package = null;
            }
        }
        if ("android.intent.action.PACKAGE_ADDED".equals(action) && intent.getData() != null && OPUtils.ONEPLUS_CLOUD_PACKAGE.equals(intent.getData().getSchemeSpecificPart())) {
            OPUtils.isExist_Cloud_Package = null;
        }
        if (action.equals("codeaurora.net.conn.TETHER_AUTO_SHUT_DOWN_SOFTAP")) {
            Log.d("SettingsReceiver", "Auto shutdown wifi ap if no device connected in 5 mins ");
            OPUtils.stopTethering(context);
        }
        if (action.equals(ACTION_THREE_KEY)) {
            OPZenModeUtils.getInstance(context).sendAppTrackerDelay();
        }
        if (action.equals(BOOT_BROADCAST) && context.getSharedPreferences("App_Tracker", 0).getInt("zen_mode", 0) != this.mZenMode) {
            OPZenModeUtils.getInstance(context).sendAppTrackerDelay();
        }
        if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
            if (OPUtils.isSupportFontStyleSetting()) {
                Log.i("SettingsReceiver", " isSupportFontStyleSetting Language change");
                setFontMode(System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "oem_font_mode", 1, 0));
            } else {
                Log.i("SettingsReceiver", "! isSupportFontStyleSetting Language change");
                setFontMode(1);
            }
        }
        if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
            int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            Log.d("SettingsReceiver", "android.bluetooth.adapter.action.STATE_CHANGED");
            int scanmodestatus = OPPrefUtil.getInt("oneplus_bluetooth_scan_mode_flag", 0);
            if (state == 12 && scanmodestatus == 0) {
                setBluetoothScanMode();
            }
        }
        if (action.equals("com.oem.intent.action.BOOT_COMPLETED")) {
            OPUtils.restoreBackupEntranceInLauncher(context);
            OPUtils.disableCardPackageEntranceInLauncher(context);
            OPUtils.enableAppBgService(context);
            OPUtils.enablePackageInstaller(context);
            OPUtils.disableWirelessAdbDebuging();
            OPUtils.sendAppTrackerForAllSettings();
            this.mUm = (UserManager) context.getSystemService("user");
            if (this.mUm != null && this.mUm.isUserRunning(999)) {
                Log.d("SettingsReceiver", "Handle Parallel App Requirement");
                try {
                    System.putIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "oem_acc_sensor_three_finger", System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "oem_acc_sensor_three_finger", 0), 999);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                new Thread(new Runnable() {
                    public void run() {
                        if (OPUtils.isO2()) {
                            OPUtils.installMultiApp(context, OPConstants.PACKAGENAME_GMS, 999);
                        }
                        OPUtils.installMultiApp(context, "com.oneplus.ifaaservice", 999);
                    }
                }).start();
            }
        }
        if (ACTION_OTG_AUTO_SHUTDOWN.equals(intent.getAction())) {
            SystemProperties.set("persist.sys.oem.otg_support", "false");
            Global.putInt(context.getContentResolver(), "oneplus_otg_auto_disable", 0);
            if (System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "oneplus_otg_auto_disable_is_first", 0, 0) == 0) {
                NotificationChannel notificationChannel = new NotificationChannel("OTG_INTENT_NOTIFICATION_CHANNEL", context.getResources().getString(R.string.oneplus_otg_title), 3);
                Builder builder = new Builder(context, "OTG_INTENT_NOTIFICATION_CHANNEL").setSmallIcon(R.drawable.op_ic_otg).setAutoCancel(true).setContentTitle(context.getResources().getString(R.string.oneplus_otg_title));
                Intent resultIntent = new Intent("oneplus.intent.action.OTG_SETTINGS");
                resultIntent.addFlags(268435456);
                builder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, Ints.MAX_POWER_OF_TWO));
                NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                notificationManager.createNotificationChannel(notificationChannel);
                notificationManager.notify(R.string.oneplus_otg_title, builder.build());
                System.putInt(context.getContentResolver(), "oneplus_otg_auto_disable_is_first", 1);
            }
        }
    }

    private void setFontMode(int value) {
        Intent intent = new Intent("android.settings.OEM_FONT_MODE");
        intent.putExtra("oem_font_mode", value);
        intent.putExtra("oem_font_dialog", 0);
        intent.addFlags(268435456);
        SettingsBaseApplication.mApplication.sendBroadcast(intent);
    }

    private void setBluetoothScanMode() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String mDeviceName = System.getString(SettingsBaseApplication.mApplication.getContentResolver(), "oem_oneplus_devicename");
                    LocalBluetoothAdapter mLocalAdapter = Utils.getLocalBtManager(SettingsBaseApplication.mApplication).getBluetoothAdapter();
                    int scanmode = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "bluetooth_default_scan_mode", 21);
                    if (mDeviceName != null && mLocalAdapter != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("bluetooth scan mode = ");
                        stringBuilder.append(scanmode);
                        Log.d("SettingsReceiver", stringBuilder.toString());
                        mLocalAdapter.setName(mDeviceName);
                        mLocalAdapter.setScanMode(scanmode);
                        OPPrefUtil.putInt("oneplus_bluetooth_scan_mode_flag", 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
