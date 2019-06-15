package com.android.settings.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.TetherSettings;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.concurrent.atomic.AtomicReference;

public class TetherPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnResume, OnPause, OnDestroy {
    public static final String BLUETOOTH_MANAGER_SERVICE = "bluetooth_manager";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    private final boolean mAdminDisallowedTetherConfig;
    private SettingObserver mAirplaneModeObserver;
    private AtomicReference<IBluetoothStateChangeCallback> mBluetoohListener;
    private final BluetoothAdapter mBluetoothAdapter;
    private final AtomicReference<BluetoothPan> mBluetoothPan;
    private IBluetoothManager mBluetoothService;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback;
    @VisibleForTesting
    final ServiceListener mBtProfileServiceListener;
    private final ConnectivityManager mConnectivityManager;
    private BluetoothPan mLastBluetoothPan;
    private Preference mPreference;
    private TetherBroadcastReceiver mTetherReceiver;

    class SettingObserver extends ContentObserver {
        public final Uri uri = Global.getUriFor("airplane_mode_on");

        public SettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.uri.equals(uri)) {
                boolean z = false;
                if (Global.getInt(TetherPreferenceController.this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
                    z = true;
                }
                if (z) {
                    TetherPreferenceController.this.updateSummaryToOff();
                }
            }
        }
    }

    @VisibleForTesting
    class TetherBroadcastReceiver extends BroadcastReceiver {
        TetherBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            TetherPreferenceController.this.updateSummary();
        }
    }

    @VisibleForTesting(otherwise = 5)
    TetherPreferenceController() {
        super(null);
        this.mBluetoohListener = new AtomicReference();
        this.mBtProfileServiceListener = new ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                TetherPreferenceController.this.mBluetoothPan.set((BluetoothPan) proxy);
                TetherPreferenceController.this.updateSummary();
            }

            public void onServiceDisconnected(int profile) {
                TetherPreferenceController.this.mLastBluetoothPan = (BluetoothPan) TetherPreferenceController.this.mBluetoothPan.get();
                TetherPreferenceController.this.mBluetoothPan.set(null);
            }
        };
        this.mBluetoothStateChangeCallback = new Stub() {
            public void onBluetoothStateChange(boolean up) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("state:");
                stringBuilder.append(up);
                Log.d("TetherPreferenceController", stringBuilder.toString());
                if (up) {
                    TetherPreferenceController.this.openProfileProxy();
                    TetherPreferenceController.this.registerBluetoothStateListener(false);
                }
            }
        };
        this.mAdminDisallowedTetherConfig = false;
        this.mBluetoothPan = new AtomicReference();
        this.mConnectivityManager = null;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public TetherPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mBluetoohListener = new AtomicReference();
        this.mBtProfileServiceListener = /* anonymous class already generated */;
        this.mBluetoothStateChangeCallback = /* anonymous class already generated */;
        this.mBluetoothPan = new AtomicReference();
        this.mAdminDisallowedTetherConfig = isTetherConfigDisallowed(context);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY_TETHER_SETTINGS);
        if (this.mPreference != null && !this.mAdminDisallowedTetherConfig) {
            this.mPreference.setTitle(Utils.getTetheringLabel(this.mConnectivityManager));
            this.mPreference.setEnabled(TetherSettings.isProvisioningNeededButUnavailable(this.mContext) ^ 1);
        }
    }

    public boolean isAvailable() {
        boolean isBlocked = !(this.mConnectivityManager.isTetheringSupported() || this.mAdminDisallowedTetherConfig) || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_tethering", UserHandle.myUserId());
        if (isBlocked) {
            return false;
        }
        return true;
    }

    public void updateState(Preference preference) {
        updateSummary();
    }

    public String getPreferenceKey() {
        return KEY_TETHER_SETTINGS;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (this.mBluetoothAdapter == null) {
            registerBluetoothStateListener(true);
        } else if (this.mBluetoothAdapter.isEnabled() && this.mBluetoothAdapter.getState() == 12) {
            this.mBluetoothAdapter.getProfileProxy(this.mContext, this.mBtProfileServiceListener, 5);
        }
    }

    public void onResume() {
        if (this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled() && this.mBluetoothAdapter.getState() == 12 && ((BluetoothProfile) this.mBluetoothPan.get()) == null) {
            this.mBluetoothAdapter.getProfileProxy(this.mContext, this.mBtProfileServiceListener, 5);
        }
        if (this.mAirplaneModeObserver == null) {
            this.mAirplaneModeObserver = new SettingObserver();
        }
        if (this.mTetherReceiver == null) {
            this.mTetherReceiver = new TetherBroadcastReceiver();
        }
        this.mContext.registerReceiver(this.mTetherReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"));
        this.mContext.getContentResolver().registerContentObserver(this.mAirplaneModeObserver.uri, false, this.mAirplaneModeObserver);
    }

    public void onPause() {
        if (this.mAirplaneModeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        }
        if (this.mTetherReceiver != null) {
            this.mContext.unregisterReceiver(this.mTetherReceiver);
        }
    }

    public void onDestroy() {
        closeProfileProxy();
        registerBluetoothStateListener(false);
    }

    public static boolean isTetherConfigDisallowed(Context context) {
        return RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_config_tethering", UserHandle.myUserId()) != null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateSummary() {
        if (this.mPreference != null) {
            String[] allTethered = this.mConnectivityManager.getTetheredIfaces();
            String[] wifiTetherRegex = this.mConnectivityManager.getTetherableWifiRegexs();
            String[] bluetoothRegex = this.mConnectivityManager.getTetherableBluetoothRegexs();
            boolean hotSpotOn = false;
            boolean tetherOn = false;
            boolean z = false;
            if (allTethered != null) {
                if (wifiTetherRegex != null) {
                    boolean hotSpotOn2 = false;
                    for (String tethered : allTethered) {
                        for (String regex : wifiTetherRegex) {
                            if (tethered.matches(regex)) {
                                hotSpotOn2 = true;
                                break;
                            }
                        }
                    }
                    hotSpotOn = hotSpotOn2;
                }
                if (allTethered.length > 1) {
                    tetherOn = true;
                } else if (allTethered.length == 1) {
                    tetherOn = !hotSpotOn;
                } else {
                    tetherOn = false;
                }
            }
            if (!(tetherOn || bluetoothRegex == null || bluetoothRegex.length <= 0 || this.mBluetoothAdapter == null || this.mBluetoothAdapter.getState() != 12)) {
                BluetoothPan pan = (BluetoothPan) this.mBluetoothPan.get();
                if (pan != null && pan.isTetheringOn()) {
                    z = true;
                }
                tetherOn = z;
            }
            if (!hotSpotOn && !tetherOn) {
                this.mPreference.setSummary((int) R.string.switch_off_text);
            } else if (hotSpotOn && tetherOn) {
                this.mPreference.setSummary((int) R.string.tether_settings_summary_hotspot_on_tether_on);
            } else if (hotSpotOn) {
                this.mPreference.setSummary((int) R.string.tether_settings_summary_hotspot_on_tether_off);
            } else {
                this.mPreference.setSummary((int) R.string.tether_settings_summary_hotspot_off_tether_on);
            }
        }
    }

    private void updateSummaryToOff() {
        if (this.mPreference != null) {
            this.mPreference.setSummary((int) R.string.switch_off_text);
        }
    }

    private void registerBluetoothStateListener(boolean register) {
        IBluetoothManager mgr = getBluetoothManager();
        if (mgr != null) {
            if (register) {
                try {
                    if (this.mBluetoohListener.get() == null) {
                        Log.d("TetherPreferenceController", "register listener");
                        mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
                        this.mBluetoohListener.set(this.mBluetoothStateChangeCallback);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(PreferenceControllerMixin.TAG, "", e);
                    return;
                }
            }
            if (!register && this.mBluetoohListener.get() != null) {
                Log.d("TetherPreferenceController", "unregister listener");
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
                this.mBluetoohListener.set(null);
            }
        }
    }

    private IBluetoothManager getBluetoothManager() {
        if (this.mBluetoothService != null) {
            return this.mBluetoothService;
        }
        this.mBluetoothService = IBluetoothManager.Stub.asInterface(ServiceManager.getService("bluetooth_manager"));
        return this.mBluetoothService;
    }

    private void openProfileProxy() {
        if (this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled() && this.mBluetoothPan.get() == null) {
            Log.d("TetherPreferenceController", "openProfileProxy");
            this.mBluetoothAdapter.getProfileProxy(this.mContext, this.mBtProfileServiceListener, 5);
        }
    }

    private void closeProfileProxy() {
        if (this.mBluetoothAdapter != null && this.mBluetoothPan.getAndSet(null) != null) {
            Log.d("TetherPreferenceController", "closeProfileProxy");
            this.mBluetoothAdapter.closeProfileProxy(5, (BluetoothProfile) this.mBluetoothPan.getAndSet(null));
            this.mBluetoothPan.set(null);
        } else if (this.mBluetoothAdapter != null && this.mLastBluetoothPan != null) {
            Log.d("TetherPreferenceController", "close last ProfileProxy");
            this.mBluetoothAdapter.closeProfileProxy(5, this.mLastBluetoothPan);
            this.mLastBluetoothPan = null;
        }
    }
}
