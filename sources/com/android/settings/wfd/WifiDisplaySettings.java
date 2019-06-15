package com.android.settings.wfd;

import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.media.MediaRouter;
import android.media.MediaRouter.Callback;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Slog;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.MediaRouteDialogPresenter;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.List;

public final class WifiDisplaySettings extends SettingsPreferenceFragment implements Indexable {
    private static final int CHANGE_ALL = -1;
    private static final int CHANGE_ROUTES = 2;
    private static final int CHANGE_SETTINGS = 1;
    private static final int CHANGE_WIFI_DISPLAY_STATUS = 4;
    private static final boolean DEBUG = false;
    private static final int MENU_ID_ENABLE_WIFI_DISPLAY = 1;
    private static final int ORDER_AVAILABLE = 3;
    private static final int ORDER_CERTIFICATION = 1;
    private static final int ORDER_CONNECTED = 2;
    private static final int ORDER_UNAVAILABLE = 4;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.wifi_display_settings;
            result.add(sir);
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60.INSTANCE;
    private static final String TAG = "WifiDisplaySettings";
    private boolean mAutoGO;
    private PreferenceGroup mCertCategory;
    private DisplayManager mDisplayManager;
    private TextView mEmptyView;
    private final Handler mHandler = new Handler();
    private boolean mListen;
    private int mListenChannel;
    private int mOperatingChannel;
    private int mPendingChanges;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED")) {
                WifiDisplaySettings.this.scheduleUpdate(4);
            }
        }
    };
    private MediaRouter mRouter;
    private final Callback mRouterCallback = new SimpleCallback() {
        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            WifiDisplaySettings.this.scheduleUpdate(2);
        }
    };
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            WifiDisplaySettings.this.scheduleUpdate(1);
        }
    };
    private boolean mStarted;
    private final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            int changes = WifiDisplaySettings.this.mPendingChanges;
            WifiDisplaySettings.this.mPendingChanges = 0;
            WifiDisplaySettings.this.update(changes);
        }
    };
    private boolean mWifiDisplayCertificationOn;
    private boolean mWifiDisplayOnSetting;
    private WifiDisplayStatus mWifiDisplayStatus;
    private Channel mWifiP2pChannel;
    private WifiP2pManager mWifiP2pManager;
    private int mWpsConfig = 4;

    private class RoutePreference extends Preference implements OnPreferenceClickListener {
        private final RouteInfo mRoute;

        public RoutePreference(Context context, RouteInfo route) {
            super(context);
            this.mRoute = route;
            setTitle(route.getName());
            setSummary(route.getDescription());
            setEnabled(route.isEnabled());
            if (route.isSelected()) {
                setOrder(2);
                if (route.isConnecting()) {
                    setSummary((int) R.string.wifi_display_status_connecting);
                } else {
                    setSummary((int) R.string.wifi_display_status_connected);
                }
            } else if (isEnabled()) {
                setOrder(3);
            } else {
                setOrder(4);
                if (route.getStatusCode() == 5) {
                    setSummary((int) R.string.wifi_display_status_in_use);
                } else {
                    setSummary((int) R.string.wifi_display_status_not_available);
                }
            }
            setOnPreferenceClickListener(this);
        }

        public boolean onPreferenceClick(Preference preference) {
            WifiDisplaySettings.this.toggleRoute(this.mRoute);
            return true;
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final MediaRouter mRouter;
        private final Callback mRouterCallback = new SimpleCallback() {
            public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
                SummaryProvider.this.updateSummary();
            }

            public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
                SummaryProvider.this.updateSummary();
            }

            public void onRouteAdded(MediaRouter router, RouteInfo info) {
                SummaryProvider.this.updateSummary();
            }

            public void onRouteRemoved(MediaRouter router, RouteInfo info) {
                SummaryProvider.this.updateSummary();
            }

            public void onRouteChanged(MediaRouter router, RouteInfo info) {
                SummaryProvider.this.updateSummary();
            }
        };
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mRouter = (MediaRouter) context.getSystemService("media_router");
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mRouter.addCallback(4, this.mRouterCallback);
                updateSummary();
                return;
            }
            this.mRouter.removeCallback(this.mRouterCallback);
        }

        private void updateSummary() {
            String summary = this.mContext.getString(R.string.disconnected);
            int routeCount = this.mRouter.getRouteCount();
            for (int i = 0; i < routeCount; i++) {
                RouteInfo route = this.mRouter.getRouteAt(i);
                if (route.matchesTypes(4) && route.isSelected() && !route.isConnecting()) {
                    summary = this.mContext.getString(R.string.wifi_display_status_connected);
                    break;
                }
            }
            this.mSummaryLoader.setSummary(this, summary);
        }
    }

    private class UnpairedWifiDisplayPreference extends Preference implements OnPreferenceClickListener {
        private final WifiDisplay mDisplay;

        public UnpairedWifiDisplayPreference(Context context, WifiDisplay display) {
            super(context);
            this.mDisplay = display;
            setTitle((CharSequence) display.getFriendlyDisplayName());
            setSummary(17041187);
            setEnabled(display.canConnect());
            if (isEnabled()) {
                setOrder(3);
            } else {
                setOrder(4);
                setSummary((int) R.string.wifi_display_status_in_use);
            }
            setOnPreferenceClickListener(this);
        }

        public boolean onPreferenceClick(Preference preference) {
            WifiDisplaySettings.this.pairWifiDisplay(this.mDisplay);
            return true;
        }
    }

    private class WifiDisplayRoutePreference extends RoutePreference implements OnClickListener {
        private final WifiDisplay mDisplay;

        public WifiDisplayRoutePreference(Context context, RouteInfo route, WifiDisplay display) {
            super(context, route);
            this.mDisplay = display;
            setWidgetLayoutResource(R.layout.wifi_display_preference);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            ImageView deviceDetails = (ImageView) view.findViewById(R.id.deviceDetails);
            if (deviceDetails != null) {
                deviceDetails.setOnClickListener(this);
                if (!isEnabled()) {
                    TypedValue value = new TypedValue();
                    getContext().getTheme().resolveAttribute(16842803, value, true);
                    deviceDetails.setImageAlpha((int) (value.getFloat() * 255.0f));
                    deviceDetails.setEnabled(true);
                }
            }
        }

        public void onClick(View v) {
            WifiDisplaySettings.this.showWifiDisplayOptionsDialog(this.mDisplay);
        }
    }

    public int getMetricsCategory() {
        return 102;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.mRouter = (MediaRouter) context.getSystemService("media_router");
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        this.mWifiP2pChannel = this.mWifiP2pManager.initialize(context, Looper.getMainLooper(), null);
        addPreferencesFromResource(R.xml.wifi_display_settings);
        setHasOptionsMenu(true);
    }

    public int getHelpResource() {
        return R.string.help_url_remote_display;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mEmptyView = (TextView) getView().findViewById(16908292);
        this.mEmptyView.setText(R.string.wifi_display_no_devices_found);
        setEmptyView(this.mEmptyView);
    }

    public void onStart() {
        super.onStart();
        this.mStarted = true;
        Context context = getActivity();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
        context.registerReceiver(this.mReceiver, filter);
        getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_on"), false, this.mSettingsObserver);
        getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_certification_on"), false, this.mSettingsObserver);
        getContentResolver().registerContentObserver(Global.getUriFor("wifi_display_wps_config"), false, this.mSettingsObserver);
        this.mRouter.addCallback(4, this.mRouterCallback, 1);
        update(-1);
    }

    public void onStop() {
        super.onStop();
        this.mStarted = false;
        getActivity().unregisterReceiver(this.mReceiver);
        getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        this.mRouter.removeCallback(this.mRouterCallback);
        unscheduleUpdate();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!(this.mWifiDisplayStatus == null || this.mWifiDisplayStatus.getFeatureState() == 0)) {
            MenuItem item = menu.add(0, 1, 0, R.string.wifi_display_enable_menu_item);
            item.setCheckable(true);
            item.setChecked(this.mWifiDisplayOnSetting);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return super.onOptionsItemSelected(item);
        }
        this.mWifiDisplayOnSetting = item.isChecked() ^ 1;
        item.setChecked(this.mWifiDisplayOnSetting);
        Global.putInt(getContentResolver(), "wifi_display_on", this.mWifiDisplayOnSetting);
        return true;
    }

    public static boolean isAvailable(Context context) {
        return (context.getSystemService("display") == null || !context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct") || context.getSystemService("wifip2p") == null) ? false : true;
    }

    private void scheduleUpdate(int changes) {
        if (this.mStarted) {
            if (this.mPendingChanges == 0) {
                this.mHandler.post(this.mUpdateRunnable);
            }
            this.mPendingChanges |= changes;
        }
    }

    private void unscheduleUpdate() {
        if (this.mPendingChanges != 0) {
            this.mPendingChanges = 0;
            this.mHandler.removeCallbacks(this.mUpdateRunnable);
        }
    }

    private void update(int changes) {
        int i;
        boolean invalidateOptions = false;
        int i2 = 0;
        if ((changes & 1) != 0) {
            boolean z = true;
            this.mWifiDisplayOnSetting = Global.getInt(getContentResolver(), "wifi_display_on", 0) != 0;
            if (Global.getInt(getContentResolver(), "wifi_display_certification_on", 0) == 0) {
                z = false;
            }
            this.mWifiDisplayCertificationOn = z;
            this.mWpsConfig = Global.getInt(getContentResolver(), "wifi_display_wps_config", 4);
            invalidateOptions = true;
        }
        if ((changes & 4) != 0) {
            this.mWifiDisplayStatus = this.mDisplayManager.getWifiDisplayStatus();
            invalidateOptions = true;
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        int routeCount = this.mRouter.getRouteCount();
        for (i = 0; i < routeCount; i++) {
            RouteInfo route = this.mRouter.getRouteAt(i);
            if (route.matchesTypes(4)) {
                preferenceScreen.addPreference(createRoutePreference(route));
            }
        }
        if (this.mWifiDisplayStatus != null && this.mWifiDisplayStatus.getFeatureState() == 3) {
            WifiDisplay[] displays = this.mWifiDisplayStatus.getDisplays();
            i = displays.length;
            while (i2 < i) {
                WifiDisplay display = displays[i2];
                if (!(display.isRemembered() || !display.isAvailable() || display.equals(this.mWifiDisplayStatus.getActiveDisplay()))) {
                    preferenceScreen.addPreference(new UnpairedWifiDisplayPreference(getPrefContext(), display));
                }
                i2++;
            }
            if (this.mWifiDisplayCertificationOn) {
                buildCertificationMenu(preferenceScreen);
            }
        }
        if (invalidateOptions) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private RoutePreference createRoutePreference(RouteInfo route) {
        WifiDisplay display = findWifiDisplay(route.getDeviceAddress());
        if (display != null) {
            return new WifiDisplayRoutePreference(getPrefContext(), route, display);
        }
        return new RoutePreference(getPrefContext(), route);
    }

    private WifiDisplay findWifiDisplay(String deviceAddress) {
        if (!(this.mWifiDisplayStatus == null || deviceAddress == null)) {
            for (WifiDisplay display : this.mWifiDisplayStatus.getDisplays()) {
                if (display.getDeviceAddress().equals(deviceAddress)) {
                    return display;
                }
            }
        }
        return null;
    }

    private void buildCertificationMenu(PreferenceScreen preferenceScreen) {
        if (this.mCertCategory == null) {
            this.mCertCategory = new PreferenceCategory(getPrefContext());
            this.mCertCategory.setTitle((int) R.string.wifi_display_certification_heading);
            this.mCertCategory.setOrder(1);
        } else {
            this.mCertCategory.removeAll();
        }
        preferenceScreen.addPreference(this.mCertCategory);
        if (!this.mWifiDisplayStatus.getSessionInfo().getGroupId().isEmpty()) {
            Preference p = new Preference(getPrefContext());
            p.setTitle((int) R.string.wifi_display_session_info);
            p.setSummary(this.mWifiDisplayStatus.getSessionInfo().toString());
            this.mCertCategory.addPreference(p);
            if (this.mWifiDisplayStatus.getSessionInfo().getSessionId() != 0) {
                this.mCertCategory.addPreference(new Preference(getPrefContext()) {
                    public void onBindViewHolder(PreferenceViewHolder view) {
                        super.onBindViewHolder(view);
                        Button b = (Button) view.findViewById(R.id.left_button);
                        b.setText(R.string.wifi_display_pause);
                        b.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                WifiDisplaySettings.this.mDisplayManager.pauseWifiDisplay();
                            }
                        });
                        b = (Button) view.findViewById(R.id.right_button);
                        b.setText(R.string.wifi_display_resume);
                        b.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                WifiDisplaySettings.this.mDisplayManager.resumeWifiDisplay();
                            }
                        });
                    }
                });
                this.mCertCategory.setLayoutResource(R.layout.two_buttons_panel);
            }
        }
        SwitchPreference pref = new SwitchPreference(getPrefContext()) {
            /* Access modifiers changed, original: protected */
            public void onClick() {
                WifiDisplaySettings.this.mListen = WifiDisplaySettings.this.mListen ^ 1;
                WifiDisplaySettings.this.setListenMode(WifiDisplaySettings.this.mListen);
                setChecked(WifiDisplaySettings.this.mListen);
            }
        };
        pref.setTitle((int) R.string.wifi_display_listen_mode);
        pref.setChecked(this.mListen);
        this.mCertCategory.addPreference(pref);
        pref = new SwitchPreference(getPrefContext()) {
            /* Access modifiers changed, original: protected */
            public void onClick() {
                WifiDisplaySettings.this.mAutoGO = WifiDisplaySettings.this.mAutoGO ^ 1;
                if (WifiDisplaySettings.this.mAutoGO) {
                    WifiDisplaySettings.this.startAutoGO();
                } else {
                    WifiDisplaySettings.this.stopAutoGO();
                }
                setChecked(WifiDisplaySettings.this.mAutoGO);
            }
        };
        pref.setTitle((int) R.string.wifi_display_autonomous_go);
        pref.setChecked(this.mAutoGO);
        this.mCertCategory.addPreference(pref);
        ListPreference lp = new ListPreference(getPrefContext());
        lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                int wpsConfig = Integer.parseInt((String) value);
                if (wpsConfig != WifiDisplaySettings.this.mWpsConfig) {
                    WifiDisplaySettings.this.mWpsConfig = wpsConfig;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    Global.putInt(WifiDisplaySettings.this.getActivity().getContentResolver(), "wifi_display_wps_config", WifiDisplaySettings.this.mWpsConfig);
                }
                return true;
            }
        });
        this.mWpsConfig = Global.getInt(getActivity().getContentResolver(), "wifi_display_wps_config", 4);
        CharSequence[] wpsEntries = new String[]{"Default", "PBC", "KEYPAD", "DISPLAY"};
        CharSequence[] wpsValues = new String[]{"4", "0", "2", "1"};
        lp.setKey("wps");
        lp.setTitle((int) R.string.wifi_display_wps_config);
        lp.setEntries(wpsEntries);
        lp.setEntryValues(wpsValues);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append(this.mWpsConfig);
        lp.setValue(stringBuilder.toString());
        lp.setSummary("%1$s");
        this.mCertCategory.addPreference(lp);
        lp = new ListPreference(getPrefContext());
        lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                int channel = Integer.parseInt((String) value);
                if (channel != WifiDisplaySettings.this.mListenChannel) {
                    WifiDisplaySettings.this.mListenChannel = channel;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    WifiDisplaySettings.this.setWifiP2pChannels(WifiDisplaySettings.this.mListenChannel, WifiDisplaySettings.this.mOperatingChannel);
                }
                return true;
            }
        });
        CharSequence[] lcEntries = new String[]{"Auto", "1", "6", "11"};
        CharSequence[] lcValues = new String[]{"0", "1", "6", "11"};
        lp.setKey("listening_channel");
        lp.setTitle((int) R.string.wifi_display_listen_channel);
        lp.setEntries(lcEntries);
        lp.setEntryValues(lcValues);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("");
        stringBuilder2.append(this.mListenChannel);
        lp.setValue(stringBuilder2.toString());
        lp.setSummary("%1$s");
        this.mCertCategory.addPreference(lp);
        lp = new ListPreference(getPrefContext());
        lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                int channel = Integer.parseInt((String) value);
                if (channel != WifiDisplaySettings.this.mOperatingChannel) {
                    WifiDisplaySettings.this.mOperatingChannel = channel;
                    WifiDisplaySettings.this.getActivity().invalidateOptionsMenu();
                    WifiDisplaySettings.this.setWifiP2pChannels(WifiDisplaySettings.this.mListenChannel, WifiDisplaySettings.this.mOperatingChannel);
                }
                return true;
            }
        });
        CharSequence[] ocEntries = new String[]{"Auto", "1", "6", "11", "36"};
        CharSequence[] ocValues = new String[]{"0", "1", "6", "11", "36"};
        lp.setKey("operating_channel");
        lp.setTitle((int) R.string.wifi_display_operating_channel);
        lp.setEntries(ocEntries);
        lp.setEntryValues(ocValues);
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("");
        stringBuilder3.append(this.mOperatingChannel);
        lp.setValue(stringBuilder3.toString());
        lp.setSummary("%1$s");
        this.mCertCategory.addPreference(lp);
    }

    private void startAutoGO() {
        this.mWifiP2pManager.createGroup(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                String str = WifiDisplaySettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to start AutoGO with reason ");
                stringBuilder.append(reason);
                stringBuilder.append(".");
                Slog.e(str, stringBuilder.toString());
            }
        });
    }

    private void stopAutoGO() {
        this.mWifiP2pManager.removeGroup(this.mWifiP2pChannel, new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                String str = WifiDisplaySettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to stop AutoGO with reason ");
                stringBuilder.append(reason);
                stringBuilder.append(".");
                Slog.e(str, stringBuilder.toString());
            }
        });
    }

    private void setListenMode(final boolean enable) {
        this.mWifiP2pManager.listen(this.mWifiP2pChannel, enable, new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                String str = WifiDisplaySettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to ");
                stringBuilder.append(enable ? "entered" : "exited");
                stringBuilder.append(" listen mode with reason ");
                stringBuilder.append(reason);
                stringBuilder.append(".");
                Slog.e(str, stringBuilder.toString());
            }
        });
    }

    private void setWifiP2pChannels(int lc, int oc) {
        this.mWifiP2pManager.setWifiP2pChannels(this.mWifiP2pChannel, lc, oc, new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                String str = WifiDisplaySettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to set wifi p2p channels with reason ");
                stringBuilder.append(reason);
                stringBuilder.append(".");
                Slog.e(str, stringBuilder.toString());
            }
        });
    }

    private void toggleRoute(RouteInfo route) {
        if (route.isSelected()) {
            MediaRouteDialogPresenter.showDialogFragment(getActivity(), 4, null);
        } else {
            route.select();
        }
    }

    private void pairWifiDisplay(WifiDisplay display) {
        if (display.canConnect()) {
            this.mDisplayManager.connectWifiDisplay(display.getDeviceAddress());
        }
    }

    private void showWifiDisplayOptionsDialog(final WifiDisplay display) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.wifi_display_options, null);
        final EditText nameEditText = (EditText) view.findViewById(R.id.name);
        nameEditText.setText(display.getFriendlyDisplayName());
        DialogInterface.OnClickListener done = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString().trim();
                if (name.isEmpty() || name.equals(display.getDeviceName())) {
                    name = null;
                }
                WifiDisplaySettings.this.mDisplayManager.renameWifiDisplay(display.getDeviceAddress(), name);
            }
        };
        new Builder(getActivity()).setCancelable(true).setTitle(R.string.wifi_display_options_title).setView(view).setPositiveButton(R.string.wifi_display_options_done, done).setNegativeButton(R.string.wifi_display_options_forget, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiDisplaySettings.this.mDisplayManager.forgetWifiDisplay(display.getDeviceAddress());
            }
        }).create().show();
    }
}
