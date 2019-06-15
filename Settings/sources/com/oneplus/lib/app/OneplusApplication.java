package com.oneplus.lib.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;
import com.oneplus.lib.util.AppUtils;

@SuppressLint({"Registered"})
public class OneplusApplication extends Application {
    private static final String TAG = OneplusApplication.class.getSimpleName();
    private static OneplusApplication instance;

    public static Context getContext() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        if (AppUtils.versionCodeChanged(this)) {
            onVersionChanged(AppUtils.getPrevVersion(this), AppUtils.getCurrentVersion(this));
            AppUtils.setCurrentVersion(this);
        }
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onVersionChanged(int previousCode, int currentCode) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPackageName());
        stringBuilder.append(" previousCode is: ");
        stringBuilder.append(previousCode);
        Log.i(str, stringBuilder.toString());
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append(getPackageName());
        stringBuilder.append(" currentCode is: ");
        stringBuilder.append(currentCode);
        Log.i(str, stringBuilder.toString());
    }
}
