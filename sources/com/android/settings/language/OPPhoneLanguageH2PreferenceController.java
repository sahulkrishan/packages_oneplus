package com.android.settings.language;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.localepicker.OPLocalePicker;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class OPPhoneLanguageH2PreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_PHONE_LANGUAGE = "phone_language_h2os";

    public OPPhoneLanguageH2PreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        boolean config_show_phone_language = this.mContext.getResources().getBoolean(R.bool.config_show_phone_language);
        long length = (long) this.mContext.getAssets().getLocales().length;
        boolean isO2 = OPUtils.isO2();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("config_show_phone_language:");
        stringBuilder.append(config_show_phone_language);
        stringBuilder.append(", length:");
        stringBuilder.append(length);
        stringBuilder.append(", isO2:");
        stringBuilder.append(isO2);
        Log.d("OPPhoneLanguageH2PreferenceController", stringBuilder.toString());
        return config_show_phone_language && length > 1 && !isO2;
    }

    public void updateState(Preference preference) {
        if (preference != null) {
            preference.setSummary(FeatureFactory.getFactory(this.mContext).getLocaleFeatureProvider().getLocaleNames());
        }
    }

    public void updateNonIndexableKeys(List<String> keys) {
        keys.add(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_PHONE_LANGUAGE;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_PHONE_LANGUAGE.equals(preference.getKey())) {
            return false;
        }
        new SubSettingLauncher(this.mContext).setDestination(OPLocalePicker.class.getName()).setSourceMetricsCategory(750).setTitle((int) R.string.pref_title_lang_selection).launch();
        return true;
    }
}
