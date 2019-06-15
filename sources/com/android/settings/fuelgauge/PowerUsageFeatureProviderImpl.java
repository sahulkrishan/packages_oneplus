package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.util.SparseIntArray;
import com.android.internal.os.BatterySipper;
import com.android.internal.util.ArrayUtils;

public class PowerUsageFeatureProviderImpl implements PowerUsageFeatureProvider {
    private static final String[] PACKAGES_SYSTEM = new String[]{PACKAGE_MEDIA_PROVIDER, PACKAGE_CALENDAR_PROVIDER, "com.android.systemui"};
    private static final String PACKAGE_CALENDAR_PROVIDER = "com.android.providers.calendar";
    private static final String PACKAGE_MEDIA_PROVIDER = "com.android.providers.media";
    private static final String PACKAGE_SYSTEMUI = "com.android.systemui";
    protected Context mContext;
    protected PackageManager mPackageManager;

    public PowerUsageFeatureProviderImpl(Context context) {
        this.mPackageManager = context.getPackageManager();
        this.mContext = context.getApplicationContext();
    }

    public boolean isTypeService(BatterySipper sipper) {
        return false;
    }

    public boolean isTypeSystem(BatterySipper sipper) {
        int uid = sipper.uidObj == null ? -1 : sipper.getUid();
        sipper.mPackages = this.mPackageManager.getPackagesForUid(uid);
        if (uid >= 0 && uid < MediaPlayerGlue.FAST_FORWARD_REWIND_STEP) {
            return true;
        }
        if (sipper.mPackages != null) {
            for (String packageName : sipper.mPackages) {
                if (ArrayUtils.contains(PACKAGES_SYSTEM, packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLocationSettingEnabled(String[] packages) {
        return false;
    }

    public boolean isAdditionalBatteryInfoEnabled() {
        return false;
    }

    public Intent getAdditionalBatteryInfoIntent() {
        return null;
    }

    public boolean isAdvancedUiEnabled() {
        return true;
    }

    public boolean isPowerAccountingToggleEnabled() {
        return true;
    }

    public Estimate getEnhancedBatteryPrediction(Context context) {
        return null;
    }

    public SparseIntArray getEnhancedBatteryPredictionCurve(Context context, long zeroTime) {
        return null;
    }

    public boolean isEnhancedBatteryPredictionEnabled(Context context) {
        return false;
    }

    public String getEnhancedEstimateDebugString(String timeRemaining) {
        return null;
    }

    public boolean isEstimateDebugEnabled() {
        return false;
    }

    public String getOldEstimateDebugString(String timeRemaining) {
        return null;
    }

    public String getAdvancedUsageScreenInfoString() {
        return null;
    }

    public boolean getEarlyWarningSignal(Context context, String id) {
        return false;
    }

    public boolean isSmartBatterySupported() {
        return true;
    }
}
