package com.android.settingslib.fuelgauge;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.VisibleForTesting;
import android.telecom.DefaultDialerManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.util.ArrayUtils;

public class PowerWhitelistBackend {
    private static final String DEVICE_IDLE_SERVICE = "deviceidle";
    private static final String TAG = "PowerWhitelistBackend";
    private static PowerWhitelistBackend sInstance;
    private final Context mAppContext;
    private final IDeviceIdleController mDeviceIdleService;
    private final ArraySet<String> mSysWhitelistedApps;
    private final ArraySet<String> mSysWhitelistedAppsExceptIdle;
    private final ArraySet<String> mWhitelistedApps;

    public PowerWhitelistBackend(Context context) {
        this(context, Stub.asInterface(ServiceManager.getService(DEVICE_IDLE_SERVICE)));
    }

    @VisibleForTesting
    PowerWhitelistBackend(Context context, IDeviceIdleController deviceIdleService) {
        this.mWhitelistedApps = new ArraySet();
        this.mSysWhitelistedApps = new ArraySet();
        this.mSysWhitelistedAppsExceptIdle = new ArraySet();
        this.mAppContext = context.getApplicationContext();
        this.mDeviceIdleService = deviceIdleService;
        refreshList();
    }

    public int getWhitelistSize() {
        return this.mWhitelistedApps.size();
    }

    public boolean isSysWhitelisted(String pkg) {
        return this.mSysWhitelistedApps.contains(pkg);
    }

    public boolean isWhitelisted(String pkg) {
        if (this.mWhitelistedApps.contains(pkg)) {
            return true;
        }
        if (!this.mAppContext.getPackageManager().hasSystemFeature("android.hardware.telephony")) {
            return false;
        }
        ComponentName defaultSms = SmsApplication.getDefaultSmsApplication(this.mAppContext, true);
        if ((defaultSms != null && TextUtils.equals(pkg, defaultSms.getPackageName())) || TextUtils.equals(pkg, DefaultDialerManager.getDefaultDialerApplication(this.mAppContext)) || ((DevicePolicyManager) this.mAppContext.getSystemService(DevicePolicyManager.class)).packageHasActiveAdmins(pkg)) {
            return true;
        }
        return false;
    }

    public boolean isWhitelisted(String[] pkgs) {
        if (ArrayUtils.isEmpty(pkgs)) {
            return false;
        }
        for (String pkg : pkgs) {
            if (isWhitelisted(pkg)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSysWhitelistedExceptIdle(String pkg) {
        return this.mSysWhitelistedAppsExceptIdle.contains(pkg);
    }

    public boolean isSysWhitelistedExceptIdle(String[] pkgs) {
        if (ArrayUtils.isEmpty(pkgs)) {
            return false;
        }
        for (String pkg : pkgs) {
            if (isSysWhitelistedExceptIdle(pkg)) {
                return true;
            }
        }
        return false;
    }

    public void addApp(String pkg) {
        try {
            this.mDeviceIdleService.addPowerSaveWhitelistApp(pkg);
            this.mWhitelistedApps.add(pkg);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    public void removeApp(String pkg) {
        try {
            this.mDeviceIdleService.removePowerSaveWhitelistApp(pkg);
            this.mWhitelistedApps.remove(pkg);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to reach IDeviceIdleController", e);
        }
    }

    @VisibleForTesting
    public void refreshList() {
        this.mSysWhitelistedApps.clear();
        this.mSysWhitelistedAppsExceptIdle.clear();
        this.mWhitelistedApps.clear();
        if (this.mDeviceIdleService != null) {
            try {
                int i = 0;
                for (String app : this.mDeviceIdleService.getFullPowerWhitelist()) {
                    this.mWhitelistedApps.add(app);
                }
                for (String app2 : this.mDeviceIdleService.getSystemPowerWhitelist()) {
                    this.mSysWhitelistedApps.add(app2);
                }
                String[] sysWhitelistedAppsExceptIdle = this.mDeviceIdleService.getSystemPowerWhitelistExceptIdle();
                int length = sysWhitelistedAppsExceptIdle.length;
                while (i < length) {
                    this.mSysWhitelistedAppsExceptIdle.add(sysWhitelistedAppsExceptIdle[i]);
                    i++;
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to reach IDeviceIdleController", e);
            }
        }
    }

    public static PowerWhitelistBackend getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PowerWhitelistBackend(context);
        }
        return sInstance;
    }
}
