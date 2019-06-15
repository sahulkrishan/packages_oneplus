package com.android.settings.connecteddevice;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.bluetooth.BluetoothDeviceRenamePreferenceController;
import com.android.settings.bluetooth.BluetoothSwitchPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.widget.FooterPreference;
import java.util.ArrayList;
import java.util.List;

public class BluetoothDashboardFragment extends DashboardFragment {
    public static final String KEY_BLUETOOTH_SCREEN = "bluetooth_switchbar_screen";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.bluetooth_settings_title);
            data.screenTitle = context.getString(R.string.bluetooth_settings_title);
            data.keywords = context.getString(R.string.keywords_bluetooth_settings);
            data.key = BluetoothDashboardFragment.KEY_BLUETOOTH_SCREEN;
            result.add(data);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            BluetoothManager manager = (BluetoothManager) context.getSystemService("bluetooth");
            if (manager != null) {
                int status;
                if (manager.getAdapter() != null) {
                    status = 0;
                } else {
                    status = 2;
                }
                if (status != 0) {
                    keys.add(BluetoothDashboardFragment.KEY_BLUETOOTH_SCREEN);
                }
            }
            return keys;
        }
    };
    private static final String TAG = "BluetoothDashboardFrag";
    private BluetoothSwitchPreferenceController mController;
    private FooterPreference mFooterPreference;
    private SwitchBar mSwitchBar;

    public int getMetricsCategory() {
        return 1390;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_uri_bluetooth_screen;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.bluetooth_screen;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mFooterPreference = this.mFooterPreferenceMixin.createFooterPreference();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((BluetoothDeviceRenamePreferenceController) use(BluetoothDeviceRenamePreferenceController.class)).setFragment(this);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        this.mSwitchBar = activity.getSwitchBar();
        this.mController = new BluetoothSwitchPreferenceController(activity, new SwitchBarController(this.mSwitchBar), this.mFooterPreference);
        Lifecycle lifecycle = getLifecycle();
        if (lifecycle != null) {
            lifecycle.addObserver(this.mController);
        }
    }
}
