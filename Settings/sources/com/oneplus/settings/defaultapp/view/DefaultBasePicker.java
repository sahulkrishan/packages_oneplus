package com.oneplus.settings.defaultapp.view;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.defaultapp.DefaultAppActivityInfo;
import com.oneplus.settings.defaultapp.DefaultAppLogic;
import com.oneplus.settings.defaultapp.DefaultAppUtils;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DefaultBasePicker extends DefaultAppPickerFragment {
    protected List<DefaultAppActivityInfo> mAppInfoList = this.mLogic.getAppInfoList(this.mType);
    protected List<String> mAppNameInfoList = this.mLogic.getAppPackageNameList(this.mType, this.mAppInfoList);
    protected DefaultAppLogic mLogic = DefaultAppLogic.getInstance(SettingsBaseApplication.mApplication);
    protected String mType = getType();

    public abstract String getType();

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        switch (DefaultAppUtils.getKeyTypeInt(this.mType)) {
            case 0:
                return R.xml.op_default_camera_settings;
            case 1:
                return R.xml.op_default_gallery_settings;
            case 2:
                return R.xml.op_default_music_settings;
            case 3:
                return R.xml.op_default_mail_settings;
            default:
                return 0;
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return this.mLogic.getPmDefaultAppPackageName(this.mType);
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String packageName) {
        String defaultAppPackageName = this.mLogic.getDefaultAppPackageName(this.mType);
        String pmDefaultAppPkg = this.mLogic.getPmDefaultAppPackageName(this.mType);
        if (TextUtils.isEmpty(packageName) || (Objects.equals(packageName, defaultAppPackageName) && Objects.equals(pmDefaultAppPkg, defaultAppPackageName))) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("persistString packageName:");
        stringBuilder.append(packageName);
        stringBuilder.append(", local defaultAppPackageName:");
        stringBuilder.append(defaultAppPackageName);
        stringBuilder.append(",pmDefaultAppPkg:");
        stringBuilder.append(pmDefaultAppPkg);
        Log.d("BaseDefaultPreference", stringBuilder.toString());
        this.mLogic.setDefaultAppPosition(this.mType, this.mAppInfoList, this.mAppNameInfoList, packageName);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        Context context = getContext();
        for (String applicationInfoAsUser : (String[]) this.mAppNameInfoList.toArray(new String[this.mAppNameInfoList.size()])) {
            try {
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mPm.getApplicationInfoAsUser(applicationInfoAsUser, 0, this.mUserId)));
            } catch (NameNotFoundException e) {
            }
        }
        return candidates;
    }
}
