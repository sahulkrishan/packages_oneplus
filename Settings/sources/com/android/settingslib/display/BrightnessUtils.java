package com.android.settingslib.display;

import android.support.v4.app.FrameMetricsAggregator;
import android.util.MathUtils;

public class BrightnessUtils {
    private static final float A = 0.17883277f;
    private static final float B = 0.28466892f;
    private static final float C = 0.5599107f;
    public static final int GAMMA_SPACE_MAX = 1023;
    private static final float R = 0.5f;

    public static final int convertGammaToLinear(int val, int min, int max) {
        float normalizedVal;
        float ret;
        if (val <= 512) {
            normalizedVal = MathUtils.norm(0.0f, 590.0f, (float) val);
        } else {
            normalizedVal = MathUtils.norm(0.0f, 1023.0f, (float) (890 + (((val - 512) * Const.CODE_C1_CW5) / FrameMetricsAggregator.EVERY_DURATION)));
        }
        if (normalizedVal <= 0.5f) {
            ret = MathUtils.sq(normalizedVal / 0.5f);
        } else {
            ret = MathUtils.exp((normalizedVal - C) / A) + B;
        }
        return Math.round(MathUtils.lerp((float) min, (float) max, ret / 12.0f));
    }

    public static final int convertLinearToGamma(int val, int min, int max) {
        float ret;
        float normalizedVal = MathUtils.norm((float) min, (float) max, (float) val) * 12.0f;
        if (normalizedVal <= 1.0f) {
            ret = MathUtils.sqrt(normalizedVal) * 0.5f;
        } else {
            ret = (A * MathUtils.log(normalizedVal - B)) + C;
        }
        int seekbarValue = Math.round(MathUtils.lerp(0.0f, 1023.0f, ret));
        if (ret <= 0.8699902f) {
            return Math.round((float) ((seekbarValue * 512) / 890));
        }
        return Math.round((float) (512 + (((seekbarValue - 890) * FrameMetricsAggregator.EVERY_DURATION) / Const.CODE_C1_CW5)));
    }
}
