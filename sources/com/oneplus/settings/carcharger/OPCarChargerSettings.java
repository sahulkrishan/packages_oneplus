package com.oneplus.settings.carcharger;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.widget.FooterPreference;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPCarChargerSettings extends DashboardFragment {
    private static final String KEY_CAR_CHARGER_AUTO_TURN_ON = "car_charger_auto_turn_on";
    private static final String KEY_CAR_CHARGER_AUTO_TURN_ON_DND = "car_charger_auto_turn_on_dnd";
    @VisibleForTesting
    static final String KEY_FOOTER_PREF = "footer_preference";
    private static final String TAG = "OPCarChargerSettings";
    @VisibleForTesting
    FooterPreference mFooterPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPreferencesFromPreferenceScreen();
    }

    /* Access modifiers changed, original: 0000 */
    public void initPreferencesFromPreferenceScreen() {
        this.mFooterPreference = (FooterPreference) findPreference("footer_preference");
        updateFooterPreference(this.mFooterPreference);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateFooterPreference(Preference mPreference) {
        if (OPUtils.isO2()) {
            mPreference.setTitle(getString(R.string.oneplus_auto_turn_on_car_charger_info_o2));
        } else {
            mPreference.setTitle(getString(R.string.oneplus_auto_turn_on_car_charger_info_h2));
        }
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new OPCarChargerPreferenceController(context, KEY_CAR_CHARGER_AUTO_TURN_ON));
        controllers.add(new OPCarChargerPreferenceController(context, KEY_CAR_CHARGER_AUTO_TURN_ON_DND));
        controllers.add(new OPCarChargerAutoOpenSpecifiedAppPreferenceController(context));
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_car_charger_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
