package com.android.settings.utils;

import android.content.Context;
import android.view.ContextThemeWrapper;

public class LocalClassLoaderContextThemeWrapper extends ContextThemeWrapper {
    private Class mLocalClass;

    public LocalClassLoaderContextThemeWrapper(Class clazz, Context base, int themeResId) {
        super(base, themeResId);
        this.mLocalClass = clazz;
    }

    public ClassLoader getClassLoader() {
        return this.mLocalClass.getClassLoader();
    }
}
