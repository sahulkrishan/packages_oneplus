package com.oneplus.settings.defaultapp.controller;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.applications.defaultapps.DefaultAppPreferenceController;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.defaultapp.DefaultAppActivityInfo;
import com.oneplus.settings.defaultapp.DefaultAppLogic;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultBasePreferenceController extends DefaultAppPreferenceController {
    private static final String TAG = DefaultBasePreferenceController.class.getSimpleName();
    protected List<DefaultAppActivityInfo> mAppInfoList = this.mLogic.getAppInfoList(this.mType);
    protected List<String> mAppNameInfoList = this.mLogic.getAppPackageNameList(this.mType, this.mAppInfoList);
    protected DefaultAppLogic mLogic = DefaultAppLogic.getInstance(SettingsBaseApplication.mApplication);
    protected PackageManagerWrapper mPm;
    protected String mType = getType();

    public abstract String getType();

    public DefaultBasePreferenceController(Context context) {
        super(context);
        this.mPm = new PackageManagerWrapper(context.getPackageManager());
    }

    public boolean isAvailable() {
        List<DefaultAppInfo> candidates = getCandidates();
        return (candidates == null || candidates.isEmpty()) ? false : true;
    }

    public String getPreferenceKey() {
        return this.mType;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        CharSequence defaultAppLabel = getDefaultAppLabel();
        if (!TextUtils.isEmpty(defaultAppLabel)) {
            preference.setSummary(defaultAppLabel);
        }
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        try {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mPackageManager.getPackageManager().getApplicationInfo(this.mLogic.getPmDefaultAppPackageName(this.mType), 0));
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public CharSequence getDefaultAppLabel() {
        CharSequence defaultAppLabel = null;
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo defaultApp = getDefaultAppInfo();
        if (defaultApp != null) {
            defaultAppLabel = defaultApp.loadLabel();
        }
        if (TextUtils.isEmpty(defaultAppLabel)) {
            return getOnlyAppLabel();
        }
        return defaultAppLabel;
    }

    public Drawable getDefaultAppIcon() {
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo defaultApp = getDefaultAppInfo();
        if (defaultApp != null) {
            return defaultApp.loadIcon();
        }
        return getOnlyAppIcon();
    }

    private List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        Context context = this.mContext;
        for (String applicationInfoAsUser : (String[]) this.mAppNameInfoList.toArray(new String[this.mAppNameInfoList.size()])) {
            try {
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mPm.getApplicationInfoAsUser(applicationInfoAsUser, 0, this.mUserId)));
            } catch (NameNotFoundException e) {
            }
        }
        return candidates;
    }

    private CharSequence getOnlyAppLabel() {
        try {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mPackageManager.getPackageManager().getApplicationInfo(this.mLogic.getDefaultAppPackageName(this.mType), 0)).loadLabel();
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getOnlyAppLabel error . e:");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
            return null;
        }
    }

    private Drawable getOnlyAppIcon() {
        try {
            return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mPackageManager.getPackageManager().getApplicationInfo(this.mLogic.getDefaultAppPackageName(this.mType), 0)).loadIcon();
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getOnlyAppIcon error . e:");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
            return null;
        }
    }

    public boolean isDefault(String pkg, int userId) {
        String defaultPackage = this.mLogic.getPmDefaultAppPackageName(this.mType);
        if (defaultPackage != null) {
            return defaultPackage.equals(pkg);
        }
        String defaultAppPackageName = this.mLogic.getDefaultAppPackageName(this.mType);
        if (defaultAppPackageName != null) {
            return defaultAppPackageName.equals(pkg);
        }
        return false;
    }
}
