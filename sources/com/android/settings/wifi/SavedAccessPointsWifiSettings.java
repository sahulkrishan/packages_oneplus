package com.android.settings.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.icu.text.Collator;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPointPreference.UserBadgeCache;
import com.android.settingslib.wifi.WifiSavedConfigUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SavedAccessPointsWifiSettings extends SettingsPreferenceFragment implements Indexable, WifiDialogListener {
    @VisibleForTesting
    static final int MSG_UPDATE_PREFERENCES = 1;
    private static final Comparator<AccessPoint> SAVED_NETWORK_COMPARATOR = new Comparator<AccessPoint>() {
        final Collator mCollator = Collator.getInstance();

        public int compare(AccessPoint ap1, AccessPoint ap2) {
            return this.mCollator.compare(nullToEmpty(ap1.getConfigName()), nullToEmpty(ap2.getConfigName()));
        }

        private String nullToEmpty(String string) {
            return string == null ? "" : string;
        }
    };
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
    private static final String TAG = "SavedAccessPoints";
    private Bundle mAccessPointSavedState;
    private Preference mAddNetworkPreference;
    private WifiDialog mDialog;
    private AccessPoint mDlgAccessPoint;
    @VisibleForTesting
    final ActionListener mForgetListener = new ActionListener() {
        public void onSuccess() {
            SavedAccessPointsWifiSettings.this.postUpdatePreference();
        }

        public void onFailure(int reason) {
            SavedAccessPointsWifiSettings.this.postUpdatePreference();
        }
    };
    @VisibleForTesting
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                SavedAccessPointsWifiSettings.this.initPreferences();
            }
        }
    };
    private final ActionListener mSaveListener = new ActionListener() {
        public void onSuccess() {
            SavedAccessPointsWifiSettings.this.postUpdatePreference();
        }

        public void onFailure(int reason) {
            Activity activity = SavedAccessPointsWifiSettings.this.getActivity();
            if (activity != null) {
                Toast.makeText(activity, R.string.wifi_failed_save_message, 0).show();
            }
        }
    };
    private AccessPoint mSelectedAccessPoint;
    private UserBadgeCache mUserBadgeCache;
    private WifiManager mWifiManager;

    public int getMetricsCategory() {
        return 106;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_display_saved_access_points);
        this.mUserBadgeCache = new UserBadgeCache(getPackageManager());
    }

    public void onResume() {
        super.onResume();
        initPreferences();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
            this.mAccessPointSavedState = savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
        }
    }

    private void initPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Context context = getPrefContext();
        List<AccessPoint> accessPoints = WifiSavedConfigUtils.getAllConfigs(context, this.mWifiManager);
        Collections.sort(accessPoints, SAVED_NETWORK_COMPARATOR);
        cacheRemoveAllPrefs(preferenceScreen);
        int accessPointsSize = accessPoints.size();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= accessPointsSize) {
                break;
            }
            String key;
            AccessPoint ap = (AccessPoint) accessPoints.get(i2);
            if (ap.isPasspointConfig()) {
                StringBuilder builder = new StringBuilder();
                builder.append(ap.getPasspointFqdn());
                if (TextUtils.isEmpty(ap.getConfigName())) {
                    builder.append(',');
                    builder.append(ap.getConfigName());
                }
                key = builder.toString();
            } else {
                key = ap.getKey();
            }
            String key2 = key;
            LongPressAccessPointPreference preference = (LongPressAccessPointPreference) getCachedPreference(key2);
            if (preference == null) {
                preference = new LongPressAccessPointPreference(ap, context, this.mUserBadgeCache, true, this);
                preference.setKey(key2);
                preference.setIcon(null);
                preferenceScreen.addPreference(preference);
            }
            preference.setOrder(i2);
            i = i2 + 1;
        }
        removeCachedPrefs(preferenceScreen);
        if (this.mAddNetworkPreference == null) {
            this.mAddNetworkPreference = new Preference(getPrefContext());
            this.mAddNetworkPreference.setIcon((int) R.drawable.ic_menu_add_inset);
            this.mAddNetworkPreference.setTitle((int) R.string.wifi_add_network);
        }
        this.mAddNetworkPreference.setOrder(accessPointsSize);
        preferenceScreen.addPreference(this.mAddNetworkPreference);
        if (getPreferenceScreen().getPreferenceCount() < 1) {
            Log.w(TAG, "Saved networks activity loaded, but there are no saved networks!");
        }
    }

    private void postUpdatePreference() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void showWifiDialog(LongPressAccessPointPreference accessPoint) {
        if (this.mDialog != null) {
            removeDialog(1);
            this.mDialog = null;
        }
        if (accessPoint != null) {
            this.mDlgAccessPoint = accessPoint.getAccessPoint();
        } else {
            this.mDlgAccessPoint = null;
            this.mAccessPointSavedState = null;
        }
        showDialog(1);
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId != 1) {
            return super.onCreateDialog(dialogId);
        }
        if (this.mDlgAccessPoint == null && this.mAccessPointSavedState == null) {
            this.mDialog = WifiDialog.createFullscreen(getActivity(), this, null, 1);
        } else {
            if (this.mDlgAccessPoint == null) {
                this.mDlgAccessPoint = new AccessPoint(getActivity(), this.mAccessPointSavedState);
                this.mAccessPointSavedState = null;
            }
            this.mDialog = WifiDialog.createModal(getActivity(), this, this.mDlgAccessPoint, 0);
        }
        this.mSelectedAccessPoint = this.mDlgAccessPoint;
        return this.mDialog;
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId != 1) {
            return 0;
        }
        return 602;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mDialog != null && this.mDialog.isShowing() && this.mDlgAccessPoint != null) {
            this.mAccessPointSavedState = new Bundle();
            this.mDlgAccessPoint.saveWifiState(this.mAccessPointSavedState);
            outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, this.mAccessPointSavedState);
        }
    }

    public void onForget(WifiDialog dialog) {
        if (this.mSelectedAccessPoint != null) {
            if (this.mSelectedAccessPoint.isPasspointConfig()) {
                try {
                    this.mWifiManager.removePasspointConfiguration(this.mSelectedAccessPoint.getPasspointFqdn());
                } catch (RuntimeException e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Failed to remove Passpoint configuration for ");
                    stringBuilder.append(this.mSelectedAccessPoint.getConfigName());
                    Log.e(str, stringBuilder.toString());
                }
                postUpdatePreference();
            } else {
                this.mWifiManager.forget(this.mSelectedAccessPoint.getConfig().networkId, this.mForgetListener);
            }
            this.mSelectedAccessPoint = null;
        }
    }

    public void onSubmit(WifiDialog dialog) {
        this.mWifiManager.save(dialog.getController().getConfig(), this.mSaveListener);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof LongPressAccessPointPreference) {
            showWifiDialog((LongPressAccessPointPreference) preference);
            return true;
        } else if (preference != this.mAddNetworkPreference) {
            return super.onPreferenceTreeClick(preference);
        } else {
            showWifiDialog(null);
            return true;
        }
    }
}
