package com.android.settings.fuelgauge.batterytip;

import android.util.KeyValueListParser;
import android.util.Log;

public class AnomalyInfo {
    private static final String KEY_ANOMALY_TYPE = "anomaly_type";
    private static final String KEY_AUTO_RESTRICTION = "auto_restriction";
    private static final String TAG = "AnomalyInfo";
    public final Integer anomalyType;
    public final boolean autoRestriction;

    public AnomalyInfo(String info) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("anomalyInfo: ");
        stringBuilder.append(info);
        Log.i(str, stringBuilder.toString());
        KeyValueListParser parser = new KeyValueListParser(',');
        parser.setString(info);
        this.anomalyType = Integer.valueOf(parser.getInt("anomaly_type", -1));
        this.autoRestriction = parser.getBoolean(KEY_AUTO_RESTRICTION, false);
    }
}
