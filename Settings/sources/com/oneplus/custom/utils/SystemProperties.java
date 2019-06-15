package com.oneplus.custom.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class SystemProperties {
    private static Method sSystemPropertiesGetMethod = null;

    SystemProperties() {
    }

    public static String get(String name) {
        if (sSystemPropertiesGetMethod == null) {
            try {
                Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
                if (systemPropertiesClass != null) {
                    sSystemPropertiesGetMethod = systemPropertiesClass.getMethod("get", new Class[]{String.class});
                }
            } catch (ClassNotFoundException | NoSuchMethodException e) {
            }
        }
        if (sSystemPropertiesGetMethod != null) {
            try {
                return (String) sSystemPropertiesGetMethod.invoke(null, new Object[]{name});
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
            }
        }
        return null;
    }
}
