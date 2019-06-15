package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo.State;
import android.net.NetworkRequest.Builder;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.SummaryUpdater.OnSummaryChangeListener;
import com.android.settings.widget.SwitchBarController;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settings.wifi.details.WifiNetworkDetailsFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPoint.AccessPointListener;
import com.android.settingslib.wifi.AccessPointPreference;
import com.android.settingslib.wifi.AccessPointPreference.UserBadgeCache;
import com.android.settingslib.wifi.WifiTracker;
import com.android.settingslib.wifi.WifiTracker.WifiListener;
import com.android.settingslib.wifi.WifiTrackerFactory;
import java.util.ArrayList;
import java.util.List;

public class WifiSettings extends RestrictedSettingsFragment implements Indexable, WifiListener, AccessPointListener, WifiDialogListener {
    public static final String DATA_KEY_REFERENCE = "main_toggle_wifi";
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";
    public static final String EXTRA_START_CONNECT_SSID = "wifi_start_connect_ssid";
    private static final int MENU_ID_CONNECT = 7;
    private static final int MENU_ID_FORGET = 8;
    private static final int MENU_ID_MODIFY = 9;
    private static final int MENU_ID_WRITE_NFC = 10;
    private static final String PREF_KEY_ACCESS_POINTS = "access_points";
    private static final String PREF_KEY_ADDITIONAL_SETTINGS = "additional_settings";
    private static final String PREF_KEY_CONFIGURE_WIFI_SETTINGS = "configure_settings";
    private static final String PREF_KEY_CONNECTED_ACCESS_POINTS = "connected_access_point";
    private static final String PREF_KEY_EMPTY_WIFI_LIST = "wifi_empty_list";
    private static final String PREF_KEY_SAVED_NETWORKS = "saved_networks";
    private static final String SAVED_WIFI_NFC_DIALOG_STATE = "wifi_nfc_dlg_state";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
    private static final String SAVE_DIALOG_MODE = "dialog_mode";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            if (res.getBoolean(R.bool.config_show_wifi_settings)) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.wifi_settings);
                data.screenTitle = res.getString(R.string.wifi_settings);
                data.keywords = res.getString(R.string.keywords_wifi);
                data.key = WifiSettings.DATA_KEY_REFERENCE;
                result.add(data);
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "WifiSettings";
    public static final String WAPI_CERT_MANAGE_ACTION = "android.Wapi.CertManage";
    public static final int WIFI_DIALOG_ID = 1;
    private static final int WRITE_NFC_DIALOG_ID = 6;
    private Bundle mAccessPointSavedState;
    private PreferenceCategory mAccessPointsPreferenceCategory;
    private Preference mAddPreference;
    private PreferenceCategory mAdditionalSettingsPreferenceCategory;
    private CaptivePortalNetworkCallback mCaptivePortalNetworkCallback;
    private boolean mClickedConnect;
    private Preference mConfigureWifiSettingsPreference;
    private ActionListener mConnectListener;
    private PreferenceCategory mConnectedAccessPointPreferenceCategory;
    private ConnectivityManager mConnectivityManager;
    private WifiDialog mDialog;
    private int mDialogMode;
    private AccessPoint mDlgAccessPoint;
    private boolean mEnableNextOnConnection;
    private ActionListener mForgetListener;
    private final Runnable mHideProgressBarRunnable = new -$$Lambda$WifiSettings$ojra5gZ2Zt1OL2cVDalsbhFOQY0(this);
    private boolean mIsRestricted;
    private String mOpenSsid;
    private View mProgressHeader;
    private ActionListener mSaveListener;
    private Preference mSavedNetworksPreference;
    private AccessPoint mSelectedAccessPoint;
    private LinkablePreference mStatusMessagePreference;
    private final Runnable mUpdateAccessPointsRunnable = new -$$Lambda$WifiSettings$Dc8tARLt9797q5fiCWMG56ysJZ4(this);
    private UserBadgeCache mUserBadgeCache;
    private WifiEnabler mWifiEnabler;
    protected WifiManager mWifiManager;
    private Bundle mWifiNfcDialogSavedState;
    private WriteWifiConfigToNfcDialog mWifiToNfcDialog;
    private WifiTracker mWifiTracker;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider, OnSummaryChangeListener {
        private final Context mContext;
        @VisibleForTesting
        WifiSummaryUpdater mSummaryHelper = new WifiSummaryUpdater(this.mContext, this);
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            this.mSummaryHelper.register(listening);
        }

        public void onSummaryChanged(String summary) {
            this.mSummaryLoader.setSummary(this, summary);
        }
    }

    private static boolean isVerboseLoggingEnabled() {
        return WifiTracker.sVerboseLogging || Log.isLoggable(TAG, 2);
    }

    public WifiSettings() {
        super("no_config_wifi");
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            this.mProgressHeader = setPinnedHeaderView((int) R.layout.wifi_progress_header).findViewById(R.id.progress_bar_animation);
            setProgressBarVisible(false);
        }
        ((SettingsActivity) activity).getSwitchBar().setSwitchBarText(R.string.wifi_settings_master_switch_title, R.string.wifi_settings_master_switch_title);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setOrientationToPortrait();
        setAnimationAllowed(false);
        addPreferences();
        this.mIsRestricted = isUiRestricted();
    }

    private void setOrientationToPortrait() {
        if (WifiSettingsActivity.class.getSimpleName().equals(getActivity().getClass().getSimpleName())) {
            getActivity().setRequestedOrientation(1);
        }
    }

    private void addPreferences() {
        addPreferencesFromResource(R.xml.wifi_settings);
        this.mConnectedAccessPointPreferenceCategory = (PreferenceCategory) findPreference(PREF_KEY_CONNECTED_ACCESS_POINTS);
        this.mAccessPointsPreferenceCategory = (PreferenceCategory) findPreference(PREF_KEY_ACCESS_POINTS);
        this.mAdditionalSettingsPreferenceCategory = (PreferenceCategory) findPreference(PREF_KEY_ADDITIONAL_SETTINGS);
        this.mConfigureWifiSettingsPreference = findPreference(PREF_KEY_CONFIGURE_WIFI_SETTINGS);
        this.mSavedNetworksPreference = findPreference(PREF_KEY_SAVED_NETWORKS);
        Context prefContext = getPrefContext();
        this.mAddPreference = new Preference(prefContext);
        this.mAddPreference.setIcon((int) R.drawable.ic_menu_add_inset);
        this.mAddPreference.setTitle((int) R.string.wifi_add_network);
        this.mStatusMessagePreference = new LinkablePreference(prefContext);
        this.mUserBadgeCache = new UserBadgeCache(getPackageManager());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiTracker = WifiTrackerFactory.create(getActivity(), this, getLifecycle(), true, true);
        this.mWifiManager = this.mWifiTracker.getManager();
        if (getActivity() != null) {
            this.mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(ConnectivityManager.class);
        }
        this.mConnectListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                Activity activity = WifiSettings.this.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_failed_connect_message, 0).show();
                }
            }
        };
        this.mSaveListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                Activity activity = WifiSettings.this.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_failed_save_message, 0).show();
                }
            }
        };
        this.mForgetListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
                Activity activity = WifiSettings.this.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, R.string.wifi_failed_forget_message, 0).show();
                }
            }
        };
        if (savedInstanceState != null) {
            this.mDialogMode = savedInstanceState.getInt(SAVE_DIALOG_MODE);
            if (savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
                this.mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
            }
            if (savedInstanceState.containsKey(SAVED_WIFI_NFC_DIALOG_STATE)) {
                this.mWifiNfcDialogSavedState = savedInstanceState.getBundle(SAVED_WIFI_NFC_DIALOG_STATE);
            }
        }
        Intent intent = getActivity().getIntent();
        this.mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);
        if (this.mEnableNextOnConnection && hasNextButton()) {
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService("connectivity");
            if (connectivity != null) {
                changeNextButtonState(connectivity.getNetworkInfo(1).isConnected());
            }
        }
        registerForContextMenu(getListView());
        setHasOptionsMenu(true);
        if (intent.hasExtra(EXTRA_START_CONNECT_SSID)) {
            this.mOpenSsid = intent.getStringExtra(EXTRA_START_CONNECT_SSID);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.teardownSwitchController();
        }
    }

    public void onStart() {
        super.onStart();
        this.mWifiEnabler = createWifiEnabler();
        if (this.mIsRestricted) {
            restrictUi();
        } else {
            onWifiStateChanged(this.mWifiManager.getWifiState());
        }
    }

    private void restrictUi() {
        if (!isUiRestrictedByOnlyAdmin()) {
            getEmptyTextView().setText(R.string.wifi_empty_list_user_restricted);
        }
        getPreferenceScreen().removeAll();
    }

    private WifiEnabler createWifiEnabler() {
        SettingsActivity activity = (SettingsActivity) getActivity();
        return new WifiEnabler(activity, new SwitchBarController(activity.getSwitchBar()), this.mMetricsFeatureProvider);
    }

    public void onResume() {
        Activity activity = getActivity();
        super.onResume();
        boolean alreadyImmutablyRestricted = this.mIsRestricted;
        this.mIsRestricted = isUiRestricted();
        if (!alreadyImmutablyRestricted && this.mIsRestricted) {
            restrictUi();
        }
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.resume(activity);
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.pause();
        }
    }

    public void onStop() {
        getView().removeCallbacks(this.mUpdateAccessPointsRunnable);
        getView().removeCallbacks(this.mHideProgressBarRunnable);
        unregisterCaptivePortalNetworkCallback();
        super.onStop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean formerlyRestricted = this.mIsRestricted;
        this.mIsRestricted = isUiRestricted();
        if (formerlyRestricted && !this.mIsRestricted && getPreferenceScreen().getPreferenceCount() == 0) {
            addPreferences();
        }
    }

    public int getMetricsCategory() {
        return 103;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mDialog != null && this.mDialog.isShowing()) {
            outState.putInt(SAVE_DIALOG_MODE, this.mDialogMode);
            if (this.mDlgAccessPoint != null) {
                this.mAccessPointSavedState = new Bundle();
                this.mDlgAccessPoint.saveWifiState(this.mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, this.mAccessPointSavedState);
            }
        }
        if (this.mWifiToNfcDialog != null && this.mWifiToNfcDialog.isShowing()) {
            Bundle savedState = new Bundle();
            this.mWifiToNfcDialog.saveState(savedState);
            outState.putBundle(SAVED_WIFI_NFC_DIALOG_STATE, savedState);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        Preference preference = (Preference) view.getTag();
        if (preference instanceof LongPressAccessPointPreference) {
            this.mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            menu.setHeaderTitle(this.mSelectedAccessPoint.getSsid());
            if (this.mSelectedAccessPoint.isConnectable()) {
                menu.add(0, 7, 0, R.string.wifi_menu_connect);
            }
            if (!WifiUtils.isNetworkLockedDown(getActivity(), this.mSelectedAccessPoint.getConfig())) {
                if (this.mSelectedAccessPoint.isSaved() || this.mSelectedAccessPoint.isEphemeral()) {
                    menu.add(0, 8, 0, R.string.wifi_menu_forget);
                }
                if (this.mSelectedAccessPoint.isSaved()) {
                    menu.add(0, 9, 0, R.string.wifi_menu_modify);
                }
            }
        } else if (preference instanceof ConnectedAccessPointPreference) {
            this.mSelectedAccessPoint = ((ConnectedAccessPointPreference) preference).getAccessPoint();
            menu.setHeaderTitle(this.mSelectedAccessPoint.getSsid());
            if (this.mSelectedAccessPoint.isConnectable()) {
                menu.add(0, 7, 0, R.string.wifi_menu_connect);
            }
            if (!WifiUtils.isNetworkLockedDown(getActivity(), this.mSelectedAccessPoint.getConfig())) {
                if (this.mSelectedAccessPoint.isSaved() || this.mSelectedAccessPoint.isEphemeral()) {
                    menu.add(0, 8, 0, R.string.wifi_menu_forget);
                }
                if (this.mSelectedAccessPoint.isSaved()) {
                    menu.add(0, 9, 0, R.string.wifi_menu_modify);
                }
            }
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (this.mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case 7:
                boolean isSavedNetwork = this.mSelectedAccessPoint.isSaved();
                if (isSavedNetwork) {
                    connect(this.mSelectedAccessPoint.getConfig(), isSavedNetwork);
                } else if (this.mSelectedAccessPoint.getSecurity() == 0) {
                    this.mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(this.mSelectedAccessPoint.getConfig(), isSavedNetwork);
                } else {
                    showDialog(this.mSelectedAccessPoint, 1);
                }
                return true;
            case 8:
                forget();
                return true;
            case 9:
                showDialog(this.mSelectedAccessPoint, 2);
                return true;
            case 10:
                showDialog(6);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getFragment() != null) {
            preference.setOnPreferenceClickListener(null);
            return super.onPreferenceTreeClick(preference);
        }
        if (preference instanceof LongPressAccessPointPreference) {
            this.mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            if (this.mSelectedAccessPoint == null) {
                return false;
            }
            if (this.mSelectedAccessPoint.isActive()) {
                return super.onPreferenceTreeClick(preference);
            }
            WifiConfiguration config = this.mSelectedAccessPoint.getConfig();
            if (this.mSelectedAccessPoint.getSecurity() == 0) {
                this.mSelectedAccessPoint.generateOpenNetworkConfig();
                connect(this.mSelectedAccessPoint.getConfig(), this.mSelectedAccessPoint.isSaved());
            } else if (this.mSelectedAccessPoint.isSaved() && config != null && config.getNetworkSelectionStatus() != null && config.getNetworkSelectionStatus().getHasEverConnected()) {
                connect(config, true);
            } else if (this.mSelectedAccessPoint.isPasspoint()) {
                connect(config, true);
            } else {
                showDialog(this.mSelectedAccessPoint, 1);
            }
        } else if (preference != this.mAddPreference) {
            return super.onPreferenceTreeClick(preference);
        } else {
            onAddNetworkPressed();
        }
        return true;
    }

    private void showDialog(AccessPoint accessPoint, int dialogMode) {
        if (accessPoint != null) {
            if (WifiUtils.isNetworkLockedDown(getActivity(), accessPoint.getConfig()) && accessPoint.isActive()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), RestrictedLockUtils.getDeviceOwner(getActivity()));
                return;
            }
        }
        if (this.mDialog != null) {
            removeDialog(1);
            this.mDialog = null;
        }
        this.mDlgAccessPoint = accessPoint;
        this.mDialogMode = dialogMode;
        showDialog(1);
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == 1) {
            if (this.mDlgAccessPoint == null && this.mAccessPointSavedState == null) {
                this.mDialog = WifiDialog.createFullscreen(getActivity(), this, this.mDlgAccessPoint, this.mDialogMode);
            } else {
                if (this.mDlgAccessPoint == null) {
                    this.mDlgAccessPoint = new AccessPoint(getActivity(), this.mAccessPointSavedState);
                    this.mAccessPointSavedState = null;
                }
                this.mDialog = WifiDialog.createModal(getActivity(), this, this.mDlgAccessPoint, this.mDialogMode);
            }
            this.mSelectedAccessPoint = this.mDlgAccessPoint;
            return this.mDialog;
        } else if (dialogId != 6) {
            return super.onCreateDialog(dialogId);
        } else {
            if (this.mSelectedAccessPoint != null) {
                this.mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(getActivity(), this.mSelectedAccessPoint.getSecurity());
            } else if (this.mWifiNfcDialogSavedState != null) {
                this.mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(getActivity(), this.mWifiNfcDialogSavedState);
            }
            return this.mWifiToNfcDialog;
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == 1) {
            return 603;
        }
        if (dialogId != 6) {
            return 0;
        }
        return 606;
    }

    public void onAccessPointsChanged() {
        Log.d(TAG, "onAccessPointsChanged (WifiTracker) callback initiated");
        updateAccessPointsDelayed();
    }

    private void updateAccessPointsDelayed() {
        if (!(getActivity() == null || this.mIsRestricted || !this.mWifiManager.isWifiEnabled())) {
            View view = getView();
            Handler handler = view.getHandler();
            if (handler == null || !handler.hasCallbacks(this.mUpdateAccessPointsRunnable)) {
                setProgressBarVisible(true);
                view.postDelayed(this.mUpdateAccessPointsRunnable, 300);
            }
        }
    }

    public void onWifiStateChanged(int state) {
        if (!this.mIsRestricted) {
            switch (this.mWifiManager.getWifiState()) {
                case 0:
                    removeConnectedAccessPointPreference();
                    this.mAccessPointsPreferenceCategory.removeAll();
                    addMessagePreference(R.string.wifi_stopping);
                    break;
                case 1:
                    setOffMessage();
                    setAdditionalSettingsSummaries();
                    setProgressBarVisible(false);
                    break;
                case 2:
                    removeConnectedAccessPointPreference();
                    this.mAccessPointsPreferenceCategory.removeAll();
                    addMessagePreference(R.string.wifi_starting);
                    setProgressBarVisible(true);
                    break;
                case 3:
                    updateAccessPointPreferences();
                    break;
            }
        }
    }

    public void onConnectedChanged() {
        changeNextButtonState(this.mWifiTracker.isConnected());
    }

    private static boolean isDisabledByWrongPassword(AccessPoint accessPoint) {
        WifiConfiguration config = accessPoint.getConfig();
        boolean z = false;
        if (config == null) {
            return false;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus == null || networkStatus.isNetworkEnabled()) {
            return false;
        }
        if (13 == networkStatus.getNetworkSelectionDisableReason()) {
            z = true;
        }
        return z;
    }

    private void updateAccessPointPreferences() {
        if (this.mWifiManager.isWifiEnabled()) {
            List<AccessPoint> accessPoints = this.mWifiTracker.getAccessPoints();
            if (isVerboseLoggingEnabled()) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("updateAccessPoints called for: ");
                stringBuilder.append(accessPoints);
                Log.i(str, stringBuilder.toString());
            }
            boolean hasAvailableAccessPoints = false;
            this.mAccessPointsPreferenceCategory.removePreference(this.mStatusMessagePreference);
            cacheRemoveAllPrefs(this.mAccessPointsPreferenceCategory);
            int index = configureConnectedAccessPointPreferenceCategory(accessPoints);
            int numAccessPoints = accessPoints.size();
            while (index < numAccessPoints) {
                AccessPoint accessPoint = (AccessPoint) accessPoints.get(index);
                if (accessPoint.isReachable()) {
                    String key = accessPoint.getKey();
                    hasAvailableAccessPoints = true;
                    LongPressAccessPointPreference pref = (LongPressAccessPointPreference) getCachedPreference(key);
                    if (pref != null) {
                        pref.setOrder(index);
                    } else {
                        LongPressAccessPointPreference preference = createLongPressAccessPointPreference(accessPoint);
                        preference.setKey(key);
                        preference.setOrder(index);
                        if (this.mOpenSsid != null && this.mOpenSsid.equals(accessPoint.getSsidStr()) && accessPoint.getSecurity() != 0 && (!accessPoint.isSaved() || isDisabledByWrongPassword(accessPoint))) {
                            onPreferenceTreeClick(preference);
                            this.mOpenSsid = null;
                        }
                        this.mAccessPointsPreferenceCategory.addPreference(preference);
                        accessPoint.setListener(this);
                        preference.refresh();
                    }
                }
                index++;
            }
            removeCachedPrefs(this.mAccessPointsPreferenceCategory);
            this.mAddPreference.setOrder(index);
            this.mAccessPointsPreferenceCategory.addPreference(this.mAddPreference);
            setAdditionalSettingsSummaries();
            if (hasAvailableAccessPoints) {
                getView().postDelayed(this.mHideProgressBarRunnable, 1700);
            } else {
                setProgressBarVisible(true);
                Preference pref2 = new Preference(getPrefContext());
                pref2.setSelectable(false);
                pref2.setSummary((int) R.string.wifi_empty_list_wifi_on);
                int index2 = index + 1;
                pref2.setOrder(index);
                pref2.setKey(PREF_KEY_EMPTY_WIFI_LIST);
                this.mAccessPointsPreferenceCategory.addPreference(pref2);
                index = index2;
            }
        }
    }

    private LongPressAccessPointPreference createLongPressAccessPointPreference(AccessPoint accessPoint) {
        return new LongPressAccessPointPreference(accessPoint, getPrefContext(), this.mUserBadgeCache, false, R.drawable.ic_wifi_signal_0, this);
    }

    private ConnectedAccessPointPreference createConnectedAccessPointPreference(AccessPoint accessPoint) {
        return new ConnectedAccessPointPreference(accessPoint, getPrefContext(), this.mUserBadgeCache, R.drawable.ic_wifi_signal_0, false, this);
    }

    private boolean configureConnectedAccessPointPreferenceCategory(List<AccessPoint> accessPoints) {
        if (accessPoints.size() == 0) {
            removeConnectedAccessPointPreference();
            return false;
        }
        AccessPoint connectedAp = (AccessPoint) accessPoints.get(0);
        if (connectedAp == null) {
            return false;
        }
        if (!connectedAp.isActive()) {
            removeConnectedAccessPointPreference();
            return false;
        } else if (this.mConnectedAccessPointPreferenceCategory.getPreferenceCount() == 0) {
            addConnectedAccessPointPreference(connectedAp);
            return true;
        } else {
            ConnectedAccessPointPreference preference = (ConnectedAccessPointPreference) this.mConnectedAccessPointPreferenceCategory.getPreference(0);
            if (preference.getAccessPoint() != connectedAp) {
                removeConnectedAccessPointPreference();
                addConnectedAccessPointPreference(connectedAp);
                return true;
            }
            preference.refresh();
            registerCaptivePortalNetworkCallback(getCurrentWifiNetwork(), preference);
            return true;
        }
    }

    private void addConnectedAccessPointPreference(AccessPoint connectedAp) {
        ConnectedAccessPointPreference pref = createConnectedAccessPointPreference(connectedAp);
        registerCaptivePortalNetworkCallback(getCurrentWifiNetwork(), pref);
        pref.setOnPreferenceClickListener(new -$$Lambda$WifiSettings$xBxQqI4PVRSANGJ1NAFPK4yzyyw(this, pref));
        pref.setOnGearClickListener(new -$$Lambda$WifiSettings$gxNoP_iqTz6xulv3o7cQv7agDKI(this, pref));
        pref.refresh();
        this.mConnectedAccessPointPreferenceCategory.addPreference(pref);
        this.mConnectedAccessPointPreferenceCategory.setVisible(true);
        if (this.mClickedConnect) {
            this.mClickedConnect = false;
            scrollToPreference((Preference) this.mConnectedAccessPointPreferenceCategory);
        }
    }

    public static /* synthetic */ boolean lambda$addConnectedAccessPointPreference$2(WifiSettings wifiSettings, ConnectedAccessPointPreference pref, Preference preference) {
        pref.getAccessPoint().saveWifiState(pref.getExtras());
        if (wifiSettings.mCaptivePortalNetworkCallback == null || !wifiSettings.mCaptivePortalNetworkCallback.isCaptivePortal()) {
            wifiSettings.launchNetworkDetailsFragment(pref);
        } else {
            wifiSettings.mConnectivityManager.startCaptivePortalApp(wifiSettings.mCaptivePortalNetworkCallback.getNetwork());
        }
        return true;
    }

    public static /* synthetic */ void lambda$addConnectedAccessPointPreference$3(WifiSettings wifiSettings, ConnectedAccessPointPreference pref, ConnectedAccessPointPreference preference) {
        pref.getAccessPoint().saveWifiState(pref.getExtras());
        wifiSettings.launchNetworkDetailsFragment(pref);
    }

    private void registerCaptivePortalNetworkCallback(Network wifiNetwork, ConnectedAccessPointPreference pref) {
        if (wifiNetwork == null || pref == null) {
            Log.w(TAG, "Network or Preference were null when registering callback.");
        } else if (this.mCaptivePortalNetworkCallback == null || !this.mCaptivePortalNetworkCallback.isSameNetworkAndPreference(wifiNetwork, pref)) {
            unregisterCaptivePortalNetworkCallback();
            this.mCaptivePortalNetworkCallback = new CaptivePortalNetworkCallback(wifiNetwork, pref);
            this.mConnectivityManager.registerNetworkCallback(new Builder().clearCapabilities().addTransportType(1).build(), this.mCaptivePortalNetworkCallback, new Handler(Looper.getMainLooper()));
        }
    }

    private void unregisterCaptivePortalNetworkCallback() {
        if (this.mCaptivePortalNetworkCallback != null) {
            try {
                this.mConnectivityManager.unregisterNetworkCallback(this.mCaptivePortalNetworkCallback);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unregistering CaptivePortalNetworkCallback failed.", e);
            }
            this.mCaptivePortalNetworkCallback = null;
        }
    }

    private void launchNetworkDetailsFragment(ConnectedAccessPointPreference pref) {
        new SubSettingLauncher(getContext()).setTitle((int) R.string.pref_title_network_details).setDestination(WifiNetworkDetailsFragment.class.getName()).setArguments(pref.getExtras()).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    private Network getCurrentWifiNetwork() {
        return this.mWifiManager != null ? this.mWifiManager.getCurrentNetwork() : null;
    }

    private void removeConnectedAccessPointPreference() {
        this.mConnectedAccessPointPreferenceCategory.removeAll();
        this.mConnectedAccessPointPreferenceCategory.setVisible(false);
        unregisterCaptivePortalNetworkCallback();
    }

    private void setAdditionalSettingsSummaries() {
        int i;
        this.mAdditionalSettingsPreferenceCategory.addPreference(this.mConfigureWifiSettingsPreference);
        Preference preference = this.mConfigureWifiSettingsPreference;
        if (isWifiWakeupEnabled()) {
            i = R.string.wifi_configure_settings_preference_summary_wakeup_on;
        } else {
            i = R.string.wifi_configure_settings_preference_summary_wakeup_off;
        }
        preference.setSummary(getString(i));
        int numSavedNetworks = this.mWifiTracker.getNumSavedNetworks();
        if (numSavedNetworks > 0) {
            this.mAdditionalSettingsPreferenceCategory.addPreference(this.mSavedNetworksPreference);
            this.mSavedNetworksPreference.setSummary(getResources().getQuantityString(R.plurals.wifi_saved_access_points_summary, numSavedNetworks, new Object[]{Integer.valueOf(numSavedNetworks)}));
            return;
        }
        this.mAdditionalSettingsPreferenceCategory.removePreference(this.mSavedNetworksPreference);
    }

    private boolean isWifiWakeupEnabled() {
        PowerManager powerManager = (PowerManager) getSystemService("power");
        ContentResolver contentResolver = getContentResolver();
        return Global.getInt(contentResolver, "wifi_wakeup_enabled", 0) == 1 && Global.getInt(contentResolver, "wifi_scan_always_enabled", 0) == 1 && Global.getInt(contentResolver, "airplane_mode_on", 0) == 0 && !powerManager.isPowerSaveMode();
    }

    private void setOffMessage() {
        CharSequence description;
        CharSequence title = getText(R.string.wifi_empty_list_wifi_off);
        boolean z = true;
        if (Global.getInt(getActivity().getContentResolver(), "wifi_scan_always_enabled", 0) != 1) {
            z = false;
        }
        if (z) {
            description = getText(R.string.wifi_scan_notify_text);
        } else {
            description = getText(R.string.wifi_scan_notify_text_scanning_off);
        }
        this.mStatusMessagePreference.setText(title, description, new -$$Lambda$WifiSettings$G0-vWzmi3g45SjhkhuPVMzYpO5w(this));
        removeConnectedAccessPointPreference();
        this.mAccessPointsPreferenceCategory.removeAll();
        this.mAccessPointsPreferenceCategory.addPreference(this.mStatusMessagePreference);
    }

    private void addMessagePreference(int messageId) {
        this.mStatusMessagePreference.setTitle(messageId);
        removeConnectedAccessPointPreference();
        this.mAccessPointsPreferenceCategory.removeAll();
        this.mAccessPointsPreferenceCategory.addPreference(this.mStatusMessagePreference);
    }

    /* Access modifiers changed, original: protected */
    public void setProgressBarVisible(boolean visible) {
        if (this.mProgressHeader != null) {
            this.mProgressHeader.setVisibility(visible ? 0 : 8);
        }
    }

    private void changeNextButtonState(boolean enabled) {
        if (this.mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(enabled);
        }
    }

    public void onForget(WifiDialog dialog) {
        forget();
    }

    public void onSubmit(WifiDialog dialog) {
        if (this.mDialog != null) {
            submit(this.mDialog.getController());
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void submit(WifiConfigController configController) {
        WifiConfiguration config = configController.getConfig();
        if (config == null) {
            if (this.mSelectedAccessPoint != null && this.mSelectedAccessPoint.isSaved()) {
                connect(this.mSelectedAccessPoint.getConfig(), true);
            }
        } else if (configController.getMode() == 2) {
            if (configController.checkWapiParam()) {
                this.mWifiManager.save(config, this.mSaveListener);
            } else {
                if (configController.getCurSecurity() == 5) {
                    startWapiCertManage();
                }
                return;
            }
        } else if (configController.checkWapiParam()) {
            this.mWifiManager.save(config, this.mSaveListener);
            if (this.mSelectedAccessPoint != null) {
                connect(config, false);
            }
        } else {
            if (configController.getCurSecurity() == 5) {
                startWapiCertManage();
            }
            return;
        }
        this.mWifiTracker.resumeScanning();
    }

    /* Access modifiers changed, original: 0000 */
    public void forget() {
        this.mMetricsFeatureProvider.action(getActivity(), (int) Const.CODE_C1_DSW, new Pair[0]);
        if (this.mSelectedAccessPoint.isSaved()) {
            if (this.mSelectedAccessPoint.getConfig().isPasspoint()) {
                this.mWifiManager.removePasspointConfiguration(this.mSelectedAccessPoint.getConfig().FQDN);
            } else {
                this.mWifiManager.forget(this.mSelectedAccessPoint.getConfig().networkId, this.mForgetListener);
            }
        } else if (this.mSelectedAccessPoint.getNetworkInfo() == null || this.mSelectedAccessPoint.getNetworkInfo().getState() == State.DISCONNECTED) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to forget invalid network ");
            stringBuilder.append(this.mSelectedAccessPoint.getConfig());
            Log.e(str, stringBuilder.toString());
            return;
        } else {
            this.mWifiManager.disableEphemeralNetwork(AccessPoint.convertToQuotedString(this.mSelectedAccessPoint.getSsidStr()));
        }
        this.mWifiTracker.resumeScanning();
        changeNextButtonState(false);
    }

    /* Access modifiers changed, original: protected */
    public void connect(WifiConfiguration config, boolean isSavedNetwork) {
        this.mMetricsFeatureProvider.action(getVisibilityLogger(), 135, isSavedNetwork);
        this.mWifiManager.connect(config, this.mConnectListener);
        this.mClickedConnect = true;
    }

    /* Access modifiers changed, original: protected */
    public void connect(int networkId, boolean isSavedNetwork) {
        this.mMetricsFeatureProvider.action(getActivity(), 135, isSavedNetwork);
        this.mWifiManager.connect(networkId, this.mConnectListener);
    }

    /* Access modifiers changed, original: 0000 */
    public void onAddNetworkPressed() {
        this.mMetricsFeatureProvider.action(getActivity(), (int) Const.CODE_C1_CW6, new Pair[0]);
        this.mSelectedAccessPoint = null;
        showDialog(null, 1);
    }

    public int getHelpResource() {
        return R.string.help_url_wifi;
    }

    public void onAccessPointChanged(final AccessPoint accessPoint) {
        Log.d(TAG, "onAccessPointChanged (singular) callback initiated");
        View view = getView();
        if (view != null) {
            view.post(new Runnable() {
                public void run() {
                    Object tag = accessPoint.getTag();
                    if (tag != null) {
                        ((AccessPointPreference) tag).refresh();
                    }
                }
            });
        }
    }

    private void startWapiCertManage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.wapi_no_vaild_cert));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.wapi_yes, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(WifiSettings.TAG, "startWapiCertManage: yes");
                WifiSettings.this.startActivity(new Intent(WifiSettings.WAPI_CERT_MANAGE_ACTION));
            }
        });
        builder.setNegativeButton(R.string.wapi_no, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(WifiSettings.TAG, "startWapiCertManage: no");
            }
        });
        builder.create().show();
    }

    public void onLevelChanged(AccessPoint accessPoint) {
        ((AccessPointPreference) accessPoint.getTag()).onLevelChanged();
    }
}
