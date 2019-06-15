package com.oneplus.settings.product;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.lib.util.ReflectUtil;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class OPPreInstalledAppList extends SettingsPreferenceFragment {
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    public static String[] sOneplusH2PreIinstalledAppsCompany;
    public static String[] sOneplusH2PreIinstalledAppsFunction;
    public static String[] sOneplusH2PreIinstalledAppsName;
    private final String ONEPLUS_PRE_INSTALL_APP_CATEGORY = "oneplus_pre_install_app_category";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (OPUtils.isGuaProject()) {
            sOneplusH2PreIinstalledAppsName = getContext().getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name_gua);
            sOneplusH2PreIinstalledAppsCompany = getContext().getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company_gua);
            sOneplusH2PreIinstalledAppsFunction = getContext().getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function_gua);
        } else if (Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_model_for_europe_and_america))) {
            sOneplusH2PreIinstalledAppsName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name_fat);
            sOneplusH2PreIinstalledAppsCompany = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company_fat);
            sOneplusH2PreIinstalledAppsFunction = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function_fat);
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000)) {
            sOneplusH2PreIinstalledAppsName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name_new);
            sOneplusH2PreIinstalledAppsCompany = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company_new);
            sOneplusH2PreIinstalledAppsFunction = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function_new);
        } else if (ReflectUtil.isFeatureSupported(OPConstants.FEATURE_QUICKPAY_ANIM_FOR_ENCHILADA)) {
            sOneplusH2PreIinstalledAppsName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name_new);
            sOneplusH2PreIinstalledAppsCompany = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company_new);
            sOneplusH2PreIinstalledAppsFunction = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function_new);
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000") || Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            sOneplusH2PreIinstalledAppsName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name_5_5T);
            sOneplusH2PreIinstalledAppsCompany = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company_5_5T);
            sOneplusH2PreIinstalledAppsFunction = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function_5_5T);
        } else {
            sOneplusH2PreIinstalledAppsName = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_name);
            sOneplusH2PreIinstalledAppsCompany = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_company);
            sOneplusH2PreIinstalledAppsFunction = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_h2_pre_installed_app_function);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.op_pre_install_app_list);
        PreferenceCategory screenCategory = (PreferenceCategory) getPreferenceScreen().findPreference("oneplus_pre_install_app_category");
        for (int i = 0; i < sOneplusH2PreIinstalledAppsName.length; i++) {
            Preference preference = new Preference(getContext());
            preference.setLayoutResource(R.layout.op_preference_material);
            preference.setIconSpaceReserved(true);
            preference.setTitle(sOneplusH2PreIinstalledAppsName[i]);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(sOneplusH2PreIinstalledAppsFunction[i]);
            stringBuilder.append(" / ");
            stringBuilder.append(sOneplusH2PreIinstalledAppsCompany[i]);
            preference.setSummary(stringBuilder.toString());
            screenCategory.addPreference(preference);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
