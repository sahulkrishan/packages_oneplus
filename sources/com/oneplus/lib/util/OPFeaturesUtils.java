package com.oneplus.lib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class OPFeaturesUtils {
    private static Method sIsSupport;
    private static Class sOPFeatures;

    static {
        loadFeatures();
    }

    private static void loadFeatures() {
        try {
            sOPFeatures = Class.forName("android.util.OpFeatures");
            sIsSupport = sOPFeatures.getDeclaredMethod("isSupport", new Class[]{int[].class});
        } catch (Exception e) {
        }
    }

    public static boolean isSupportXVibrate() {
        try {
            if (sOPFeatures == null || sIsSupport == null) {
                loadFeatures();
            }
            Field xVibrate = sOPFeatures.getDeclaredField("OP_FEATURE_X_LINEAR_VIBRATION_MOTOR");
            sIsSupport.setAccessible(true);
            xVibrate.setAccessible(true);
            Method method = sIsSupport;
            Object[] objArr = new Object[1];
            objArr[0] = new int[]{xVibrate.getInt(null)};
            return ((Boolean) method.invoke(null, objArr)).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSupportZVibrate() {
        try {
            if (sOPFeatures == null || sIsSupport == null) {
                loadFeatures();
            }
            Field zVibrate = sOPFeatures.getDeclaredField("OP_FEATURE_Z_VIBRATION_MOTOR");
            sIsSupport.setAccessible(true);
            zVibrate.setAccessible(true);
            Method method = sIsSupport;
            Object[] objArr = new Object[1];
            objArr[0] = new int[]{zVibrate.getInt(null)};
            return ((Boolean) method.invoke(null, objArr)).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }
}
