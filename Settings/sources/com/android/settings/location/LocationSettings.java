package com.android.settings.location;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class LocationSettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.location_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return LocationSettings.buildPreferenceControllers(context, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            context.getSystemService("user");
            if (Utils.getManagedProfile(UserManager.get(context)) == null) {
                result.add(LocationForWorkPreferenceController.KEY_MANAGED_PROFILE_SWITCH);
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "LocationSettings";
    private LocationSwitchBarController mSwitchBarController;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, LocationPreferenceController.getLocationSummary(this.mContext));
            }
        }
    }

    public static class AgpsPreferenceController extends BasePreferenceController {
        private static final String ASSISTED_GPS_SUPL_HOST = "assisted_gps_supl_host";
        private static final String ASSISTED_GPS_SUPL_PORT = "assisted_gps_supl_port";
        private static final String KEY_ASSISTED_GPS = "assisted_gps";
        private static final String PROPERTIES_FILE = "/etc/gps.conf";
        private CheckBoxPreference mAgpsPreference;

        public AgpsPreferenceController(Context context) {
            super(context, KEY_ASSISTED_GPS);
        }

        public String getPreferenceKey() {
            return KEY_ASSISTED_GPS;
        }

        public int getAvailabilityStatus() {
            if (this.mContext.getResources().getBoolean(R.bool.config_agps_enabled)) {
                return 0;
            }
            return 2;
        }

        public void displayPreference(PreferenceScreen screen) {
            super.displayPreference(screen);
            this.mAgpsPreference = (CheckBoxPreference) screen.findPreference(KEY_ASSISTED_GPS);
        }

        public void updateState(Preference preference) {
            if (this.mAgpsPreference != null) {
                CheckBoxPreference checkBoxPreference = this.mAgpsPreference;
                boolean z = true;
                if (Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 0) != 1) {
                    z = false;
                }
                checkBoxPreference.setChecked(z);
            }
        }

        public boolean handlePreferenceTreeClick(Preference preference) {
            if (!KEY_ASSISTED_GPS.equals(preference.getKey())) {
                return false;
            }
            ContentResolver cr = this.mContext.getContentResolver();
            boolean switchState = this.mAgpsPreference.isChecked();
            if (switchState && (Global.getString(cr, ASSISTED_GPS_SUPL_HOST) == null || Global.getString(cr, ASSISTED_GPS_SUPL_PORT) == null)) {
                FileInputStream stream = null;
                try {
                    Properties properties = new Properties();
                    stream = new FileInputStream(new File(PROPERTIES_FILE));
                    properties.load(stream);
                    Global.putString(cr, ASSISTED_GPS_SUPL_HOST, properties.getProperty("SUPL_HOST", null));
                    Global.putString(cr, ASSISTED_GPS_SUPL_PORT, properties.getProperty("SUPL_PORT", null));
                    try {
                        stream.close();
                    } catch (Exception e) {
                    }
                } catch (IOException e2) {
                    String str = LocationSettings.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Could not open GPS configuration file /etc/gps.conf, e=");
                    stringBuilder.append(e2);
                    Log.e(str, stringBuilder.toString());
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Throwable th) {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Exception e3) {
                        }
                    }
                }
            }
            Global.putInt(cr, "assisted_gps_enabled", switchState);
            return true;
        }
    }

    public int getMetricsCategory() {
        return 63;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        SwitchBar switchBar = activity.getSwitchBar();
        switchBar.setSwitchBarText(R.string.location_settings_master_switch_title, R.string.location_settings_master_switch_title);
        this.mSwitchBarController = new LocationSwitchBarController(activity, switchBar, getLifecycle());
        switchBar.show();
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.location_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, this, getLifecycle());
    }

    static void addPreferencesSorted(List<Preference> prefs, PreferenceGroup container) {
        Collections.sort(prefs, new Comparator<Preference>() {
            public int compare(Preference lhs, Preference rhs) {
                return lhs.getTitle().toString().compareTo(rhs.getTitle().toString());
            }
        });
        for (Preference entry : prefs) {
            container.addPreference(entry);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_location_access;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, LocationSettings fragment, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new AppLocationPermissionPreferenceController(context));
        controllers.add(new LocationForWorkPreferenceController(context, lifecycle));
        controllers.add(new RecentLocationRequestPreferenceController(context, fragment, lifecycle));
        controllers.add(new LocationScanningPreferenceController(context));
        controllers.add(new LocationServicePreferenceController(context, fragment, lifecycle));
        controllers.add(new LocationFooterPreferenceController(context, lifecycle));
        controllers.add(new AgpsPreferenceController(context));
        return controllers;
    }
}
