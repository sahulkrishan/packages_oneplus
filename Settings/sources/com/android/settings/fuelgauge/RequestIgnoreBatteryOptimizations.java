package com.android.settings.fuelgauge;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;

public class RequestIgnoreBatteryOptimizations extends AlertActivity implements OnClickListener {
    private static final String DEVICE_IDLE_SERVICE = "deviceidle";
    static final String TAG = "RequestIgnoreBatteryOptimizations";
    IDeviceIdleController mDeviceIdleService;
    String mPackageName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDeviceIdleService = Stub.asInterface(ServiceManager.getService(DEVICE_IDLE_SERVICE));
        Uri data = getIntent().getData();
        String str;
        StringBuilder stringBuilder;
        if (data == null) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No data supplied for IGNORE_BATTERY_OPTIMIZATION_SETTINGS in: ");
            stringBuilder.append(getIntent());
            Log.w(str, stringBuilder.toString());
            finish();
            return;
        }
        this.mPackageName = data.getSchemeSpecificPart();
        if (this.mPackageName == null) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No data supplied for IGNORE_BATTERY_OPTIMIZATION_SETTINGS in: ");
            stringBuilder.append(getIntent());
            Log.w(str, stringBuilder.toString());
            finish();
        } else if (((PowerManager) getSystemService(PowerManager.class)).isIgnoringBatteryOptimizations(this.mPackageName)) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Not should prompt, already ignoring optimizations: ");
            stringBuilder2.append(this.mPackageName);
            Log.i(str2, stringBuilder2.toString());
            finish();
        } else {
            String str3;
            StringBuilder stringBuilder3;
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(this.mPackageName, 0);
                if (getPackageManager().checkPermission("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", this.mPackageName) != 0) {
                    str3 = TAG;
                    stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Requested package ");
                    stringBuilder3.append(this.mPackageName);
                    stringBuilder3.append(" does not hold permission ");
                    stringBuilder3.append("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                    Log.w(str3, stringBuilder3.toString());
                    finish();
                    return;
                }
                AlertParams p = this.mAlertParams;
                p.mTitle = getText(R.string.high_power_prompt_title);
                p.mMessage = getString(R.string.high_power_prompt_body, new Object[]{ai.loadLabel(getPackageManager())});
                p.mPositiveButtonText = getText(R.string.allow);
                p.mNegativeButtonText = getText(R.string.deny);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonListener = this;
                setupAlert();
            } catch (NameNotFoundException e) {
                str3 = TAG;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Requested package doesn't exist: ");
                stringBuilder3.append(this.mPackageName);
                Log.w(str3, stringBuilder3.toString());
                finish();
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            try {
                this.mDeviceIdleService.addPowerSaveWhitelistApp(this.mPackageName);
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to reach IDeviceIdleController", e);
            }
            setResult(-1);
        }
    }
}
