package com.android.settings.deletionhelper;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class AutomaticStorageManagerSettings extends DashboardFragment implements OnPreferenceChangeListener {
    private static final String KEY_DAYS = "days";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return false;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return AutomaticStorageManagerSettings.buildPreferenceControllers(context);
        }
    };
    private DropDownPreference mDaysToRetain;
    private SwitchBar mSwitchBar;
    private AutomaticStorageManagerSwitchBarController mSwitchController;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initializeDaysToRetainPreference();
        initializeSwitchBar();
        return view;
    }

    private void initializeDaysToRetainPreference() {
        this.mDaysToRetain = (DropDownPreference) findPreference(KEY_DAYS);
        this.mDaysToRetain.setOnPreferenceChangeListener(this);
        int photosDaysToRetain = Secure.getInt(getContentResolver(), "automatic_storage_manager_days_to_retain", Utils.getDefaultStorageManagerDaysToRetain(getResources()));
        String[] stringValues = getResources().getStringArray(R.array.automatic_storage_management_days_values);
        this.mDaysToRetain.setValue(stringValues[daysValueToIndex(photosDaysToRetain, stringValues)]);
    }

    private void initializeSwitchBar() {
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBar.setSwitchBarText(R.string.automatic_storage_manager_master_switch_title, R.string.automatic_storage_manager_master_switch_title);
        this.mSwitchBar.show();
        this.mSwitchController = new AutomaticStorageManagerSwitchBarController(getContext(), this.mSwitchBar, this.mMetricsFeatureProvider, this.mDaysToRetain, getFragmentManager());
    }

    public void onResume() {
        super.onResume();
        this.mDaysToRetain.setEnabled(Utils.isStorageManagerEnabled(getContext()));
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.automatic_storage_management_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mSwitchBar.hide();
        this.mSwitchController.tearDown();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_DAYS.equals(preference.getKey())) {
            Secure.putInt(getContentResolver(), "automatic_storage_manager_days_to_retain", Integer.parseInt((String) newValue));
        }
        return true;
    }

    public int getMetricsCategory() {
        return 458;
    }

    public int getHelpResource() {
        return R.string.help_uri_storage;
    }

    private static int daysValueToIndex(int value, String[] indices) {
        for (int i = 0; i < indices.length; i++) {
            if (value == Integer.parseInt(indices[i])) {
                return i;
            }
        }
        return indices.length - 1;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new AutomaticStorageManagerDescriptionPreferenceController(context));
        return controllers;
    }
}
