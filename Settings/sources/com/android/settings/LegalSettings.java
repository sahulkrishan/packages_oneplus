package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.BoostFramework;
import com.android.settings.display.TimeoutPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.lib.util.AnimatorUtils;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegalSettings extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_FROM_SETTINGS = "key_from_settings";
    private static final String KEY_LEGAL_NOTICES = "op_legal_notices";
    private static final int KEY_LEGAL_NOTICES_TYPE = 1;
    private static final String KEY_LICENSE = "license";
    private static final String KEY_LOCATION_INFORMATION = "op_location_information";
    private static final int KEY_LOCATION_INFORMATION_TYPE = 9;
    private static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    private static final String KEY_PERMISSION_AGREEMENT = "op_permission_agreement";
    private static final int KEY_PERMISSION_AGREEMENT_TYPE = 4;
    private static final String KEY_PRIVACE_POLICY = "op_privacy_policy";
    private static final int KEY_PRIVACE_POLICY_TYPE = 3;
    private static final String KEY_TERMS = "terms";
    private static final String KEY_USER_AGREEMENT = "op_user_agreements";
    private static final int KEY_USER_AGREEMENT_TYPE = 2;
    private static final String KEY_WALLPAPER_ATTRIBUTIONS = "wallpaper_attributions";
    private static final String KEY_WEBVIEW_LICENSE = "webview_license";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    private static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.about_legal;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            keys.add(LegalSettings.KEY_TERMS);
            if (!checkIntentAction(context, "android.settings.LICENSE")) {
                keys.add(LegalSettings.KEY_LICENSE);
            }
            if (!checkIntentAction(context, "android.settings.COPYRIGHT")) {
                keys.add(LegalSettings.KEY_COPYRIGHT);
            }
            if (!checkIntentAction(context, "android.settings.WEBVIEW_LICENSE")) {
                keys.add(LegalSettings.KEY_WEBVIEW_LICENSE);
            }
            if (OPUtils.isO2()) {
                keys.add(LegalSettings.KEY_PERMISSION_AGREEMENT);
            }
            return keys;
        }

        private boolean checkIntentAction(Context context, String action) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(new Intent(action), 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                if ((((ResolveInfo) list.get(i)).activityInfo.applicationInfo.flags & 1) != 0) {
                    return true;
                }
            }
            return false;
        }
    };
    private int[] aBoostParamVal = new int[]{1082130432, 1400, 1082130688, 1400};
    private BoostFramework mBoostFrameworkPer;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.about_legal);
        Activity act = getActivity();
        PreferenceGroup parentPreference = getPreferenceScreen();
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS, 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE, 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT, 1);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_WEBVIEW_LICENSE, 1);
        if (OPUtils.isO2()) {
            parentPreference.removePreference(findPreference(KEY_PERMISSION_AGREEMENT));
        }
        if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5000")) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_2017_values);
            parentPreference.removePreference(findPreference("icon_attributions"));
        } else if (Build.MODEL.equalsIgnoreCase("ONEPLUS A5010")) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_a5010_values);
            parentPreference.removePreference(findPreference("icon_attributions"));
        } else if (Build.MODEL.equalsIgnoreCase(ONEPLUS_A6000) || Build.MODEL.equalsIgnoreCase(ONEPLUS_A6003)) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_17819_values);
            parentPreference.removePreference(findPreference("icon_attributions"));
        } else if (Build.MODEL.equalsIgnoreCase(act.getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equalsIgnoreCase(act.getString(R.string.oneplus_model_for_europe_and_america))) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_17819_values);
            parentPreference.removePreference(findPreference("icon_attributions"));
        }
        if (CUSTOM_TYPE.SW.equals(OpCustomizeSettings.getCustomType())) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_starwar_values);
        } else if (CUSTOM_TYPE.AVG.equals(OpCustomizeSettings.getCustomType())) {
            findPreference(KEY_WALLPAPER_ATTRIBUTIONS).setSummary((int) R.string.oneplus_wallpaper_attributions_avg_values);
        }
        parentPreference.removePreference(findPreference(KEY_LEGAL_NOTICES));
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        try {
            if (preference.getKey().equals(KEY_LEGAL_NOTICES)) {
                startLegalActivity(1);
                return true;
            } else if (preference.getKey().equals(KEY_USER_AGREEMENT)) {
                startLegalActivity(2);
                return true;
            } else if (preference.getKey().equals(KEY_PRIVACE_POLICY)) {
                startLegalActivity(3);
                return true;
            } else {
                if (preference.getKey().equals(KEY_PERMISSION_AGREEMENT)) {
                    startLegalActivity(4);
                } else if (preference.getKey().equals(KEY_LOCATION_INFORMATION)) {
                    startLegalActivity(9);
                }
                if (KEY_TERMS.equals(preference.getKey())) {
                    if (this.mBoostFrameworkPer == null) {
                        this.mBoostFrameworkPer = new BoostFramework();
                    }
                    this.mBoostFrameworkPer.perfLockAcquire(TimeoutPreferenceController.FALLBACK_SCREEN_TIMEOUT_VALUE, this.aBoostParamVal);
                }
                return super.onPreferenceTreeClick(preference);
            }
        } catch (ActivityNotFoundException e) {
        }
    }

    private void startLegalActivity(int type) {
        Intent intent = new Intent(OPLEGAL_NOTICES_ACTION);
        intent.putExtra(KEY_NOTICES_TYPE, type);
        intent.putExtra(KEY_FROM_SETTINGS, true);
        startActivity(intent);
    }

    public int getMetricsCategory() {
        return AnimatorUtils.time_part5;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void checkWallpaperAttributionAvailability(Context context) {
        if (!context.getResources().getBoolean(R.bool.config_show_wallpaper_attribution)) {
            removePreference(KEY_WALLPAPER_ATTRIBUTIONS);
        }
    }
}
