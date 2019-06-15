package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerUsageAdvanced extends PowerUsageBase {
    private static final String KEY_APP_LIST = "app_list";
    private static final String KEY_BATTERY_GRAPH = "battery_graph";
    private static final String KEY_SHOW_ALL_APPS = "show_all_apps";
    @VisibleForTesting
    static final int MENU_TOGGLE_APPS = 2;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.power_usage_advanced;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            List<AbstractPreferenceController> controllers = new ArrayList();
            controllers.add(new BatteryAppListPreferenceController(context, PowerUsageAdvanced.KEY_APP_LIST, null, null, null));
            return controllers;
        }
    };
    private static final String TAG = "AdvancedBatteryUsage";
    private BatteryAppListPreferenceController mBatteryAppListPreferenceController;
    private BatteryUtils mBatteryUtils;
    @VisibleForTesting
    BatteryHistoryPreference mHistPref;
    private PowerUsageFeatureProvider mPowerUsageFeatureProvider;
    @VisibleForTesting
    boolean mShowAllApps = false;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        this.mHistPref = (BatteryHistoryPreference) findPreference(KEY_BATTERY_GRAPH);
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        updateHistPrefSummary(context);
        restoreSavedInstance(icicle);
    }

    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            BatteryEntry.clearUidCache();
        }
    }

    public int getMetricsCategory() {
        return 51;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.power_usage_advanced;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 2, 0, this.mShowAllApps ? R.string.hide_extra_apps : R.string.show_all_apps);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 2) {
            return super.onOptionsItemSelected(item);
        }
        this.mShowAllApps ^= 1;
        item.setTitle(this.mShowAllApps ? R.string.hide_extra_apps : R.string.show_all_apps);
        this.mMetricsFeatureProvider.action(getContext(), 852, this.mShowAllApps);
        restartBatteryStatsLoader(0);
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void restoreSavedInstance(Bundle savedInstance) {
        if (savedInstance != null) {
            this.mShowAllApps = savedInstance.getBoolean(KEY_SHOW_ALL_APPS, false);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SHOW_ALL_APPS, this.mShowAllApps);
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mBatteryAppListPreferenceController = new BatteryAppListPreferenceController(context, KEY_APP_LIST, getLifecycle(), (SettingsActivity) getActivity(), this);
        controllers.add(this.mBatteryAppListPreferenceController);
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public void refreshUi(int refreshType) {
        Context context = getContext();
        if (context != null) {
            updatePreference(this.mHistPref);
            updateHistPrefSummary(context);
            this.mBatteryAppListPreferenceController.refreshAppListGroup(this.mStatsHelper, this.mShowAllApps);
        }
    }

    private void updateHistPrefSummary(Context context) {
        boolean plugged = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("plugged", -1) != 0;
        if (!this.mPowerUsageFeatureProvider.isEnhancedBatteryPredictionEnabled(context) || plugged) {
            this.mHistPref.hideBottomSummary();
        } else {
            this.mHistPref.setBottomSummary(this.mPowerUsageFeatureProvider.getAdvancedUsageScreenInfoString());
        }
    }
}
