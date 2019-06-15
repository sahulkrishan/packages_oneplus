package com.android.settings.connecteddevice;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.widget.FooterPreference;
import com.oneplus.settings.utils.OPUtils;

public class OPBluetoothCarKitDashboardFragment extends DashboardFragment {
    @VisibleForTesting
    static final String KEY_FOOTER_PREF = "footer_preference";
    private static final String TAG = "OPBluetoothCarKitDashboardFragment";
    @VisibleForTesting
    FooterPreference mFooterPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPreferencesFromPreferenceScreen();
        Bundle args = new Bundle();
        args.putBoolean(SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
        SearchMenuController.init((ObservablePreferenceFragment) this);
    }

    /* Access modifiers changed, original: 0000 */
    public void initPreferencesFromPreferenceScreen() {
        this.mFooterPreference = (FooterPreference) findPreference("footer_preference");
        updateFooterPreference(this.mFooterPreference);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateFooterPreference(Preference mPreference) {
        mPreference.setTitle((int) R.string.oneplus_add_bluetooth_car_kit_summary);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_bluetooth_car_kit;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((OPRecognizedBluetoothCarKitsDeviceGroupController) use(OPRecognizedBluetoothCarKitsDeviceGroupController.class)).init(this);
        ((OPOtherPairedBluetoothDevicesGroupController) use(OPOtherPairedBluetoothDevicesGroupController.class)).init(this);
        ((OPRecognizedBluetoothCarKitNoDevicesPreferenceController) use(OPRecognizedBluetoothCarKitNoDevicesPreferenceController.class)).init(this);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
