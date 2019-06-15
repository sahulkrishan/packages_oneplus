package android.support.v7.preference;

import android.annotation.SuppressLint;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class OPUtils {
    @SuppressLint({"PrivateApi"})
    public static boolean isFeatureSupported(String featureName) {
        boolean isSupport = false;
        try {
            Class<?> cla = Class.forName("android.util.OpFeatures");
            Method method = cla.getDeclaredMethod("isSupport", new Class[]{int[].class});
            Field field = cla.getDeclaredField(featureName);
            method.setAccessible(true);
            field.setAccessible(true);
            Object[] objArr = new Object[1];
            objArr[0] = new int[]{field.getInt(null)};
            return ((Boolean) method.invoke(null, objArr)).booleanValue();
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(featureName);
            stringBuilder.append(" is not supported");
            Log.i("isFeatureSupported", stringBuilder.toString());
            return isSupport;
        }
    }

    public static boolean isSupportXVibrate() {
        return isFeatureSupported("OP_FEATURE_X_LINEAR_VIBRATION_MOTOR");
    }
}
