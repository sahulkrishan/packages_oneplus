package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.util.SparseIntArray;
import com.android.internal.os.BatterySipper;

public interface PowerUsageFeatureProvider {
    Intent getAdditionalBatteryInfoIntent();

    String getAdvancedUsageScreenInfoString();

    boolean getEarlyWarningSignal(Context context, String str);

    Estimate getEnhancedBatteryPrediction(Context context);

    SparseIntArray getEnhancedBatteryPredictionCurve(Context context, long j);

    String getEnhancedEstimateDebugString(String str);

    String getOldEstimateDebugString(String str);

    boolean isAdditionalBatteryInfoEnabled();

    boolean isAdvancedUiEnabled();

    boolean isEnhancedBatteryPredictionEnabled(Context context);

    boolean isEstimateDebugEnabled();

    boolean isLocationSettingEnabled(String[] strArr);

    boolean isPowerAccountingToggleEnabled();

    boolean isSmartBatterySupported();

    boolean isTypeService(BatterySipper batterySipper);

    boolean isTypeSystem(BatterySipper batterySipper);
}
