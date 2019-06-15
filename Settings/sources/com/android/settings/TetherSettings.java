package com.android.settings;

import android.app.Activity;
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
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.wifi.tether.WifiTetherPreferenceController;
import com.android.settings.wifi.tether.WifiTetherSettings;
import com.android.settingslib.TetherUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TetherSettings extends RestrictedSettingsFragment implements Listener {
    public static final String BLUETOOTH_MANAGER_SERVICE = "bluetooth_manager";
    private static final String DATA_SAVER_FOOTER = "disabled_on_data_saver";
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    private static final int MSG_UPDATE_STATE = 1;
    private static final String TAG = "TetheringSettings";
    private static final int UPDATE_STATE_DELAY = 10;
    private static final String USB_TETHER_SETTINGS = "usb_tether_settings";
    private AtomicReference<IBluetoothStateChangeCallback> mBluetoohListener = new AtomicReference();
    private boolean mBluetoothEnableForTether;
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference();
    private String[] mBluetoothRegexs;
    private IBluetoothManager mBluetoothService;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new Stub() {
        public void onBluetoothStateChange(boolean up) {
            String str = TetherSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("state:");
            stringBuilder.append(up);
            Log.d(str, stringBuilder.toString());
            if (up) {
                TetherSettings.this.openProfileProxy();
                TetherSettings.this.registerBluetoothStateListener(false);
            }
        }
    };
    private SwitchPreference mBluetoothTether;
    private ConnectivityManager mCm;
    private DataSaverBackend mDataSaverBackend;
    private boolean mDataSaverEnabled;
    private Preference mDataSaverFooter;
    private Handler mHandler = new Handler();
    private BluetoothPan mLastBluetoothPan;
    private boolean mMassStorageActive;
    private ServiceListener mProfileServiceListener = new ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            TetherSettings.this.mBluetoothPan.set((BluetoothPan) proxy);
        }

        public void onServiceDisconnected(int profile) {
            TetherSettings.this.mLastBluetoothPan = (BluetoothPan) TetherSettings.this.mBluetoothPan.get();
            TetherSettings.this.mBluetoothPan.set(null);
        }
    };
    private OnStartTetheringCallback mStartTetheringCallback;
    private BroadcastReceiver mTetherChangeReceiver;
    private final Handler mTimeoutHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Log.i(TetherSettings.TAG, "Timeout to ensure the right tethering state");
                TetherSettings.this.updateState();
            }
        }
    };
    private boolean mUnavailable;
    private boolean mUsbConnected;
    private String[] mUsbRegexs;
    private SwitchPreference mUsbTether;
    private WifiTetherPreferenceController mWifiTetherPreferenceController;

    private static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<TetherSettings> mTetherSettings;

        OnStartTetheringCallback(TetherSettings settings) {
            this.mTetherSettings = new WeakReference(settings);
        }

        public void onTetheringStarted() {
            update();
        }

        public void onTetheringFailed() {
            update();
        }

        private void update() {
            TetherSettings settings = (TetherSettings) this.mTetherSettings.get();
            if (settings != null) {
                settings.updateState();
            }
        }
    }

    private class TetherChangeReceiver extends BroadcastReceiver {
        private TetherChangeReceiver() {
        }

        /* synthetic */ TetherChangeReceiver(TetherSettings x0, AnonymousClass1 x1) {
            this();
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                ArrayList<String> available = intent.getStringArrayListExtra("availableArray");
                ArrayList<String> active = intent.getStringArrayListExtra("tetherArray");
                ArrayList<String> errored = intent.getStringArrayListExtra("erroredArray");
                TetherSettings.this.updateState((String[]) available.toArray(new String[available.size()]), (String[]) active.toArray(new String[active.size()]), (String[]) errored.toArray(new String[errored.size()]));
            } else if (action.equals("android.intent.action.MEDIA_SHARED")) {
                TetherSettings.this.mMassStorageActive = true;
                TetherSettings.this.updateState();
            } else if (action.equals("android.intent.action.MEDIA_UNSHARED")) {
                TetherSettings.this.mMassStorageActive = false;
                TetherSettings.this.updateState();
            } else if (action.equals("android.hardware.usb.action.USB_STATE")) {
                TetherSettings.this.mUsbConnected = intent.getBooleanExtra("connected", false);
                TetherSettings.this.updateState();
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                if (TetherSettings.this.mBluetoothEnableForTether) {
                    int intExtra = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
                    if (intExtra == Integer.MIN_VALUE || intExtra == 10) {
                        TetherSettings.this.mBluetoothEnableForTether = false;
                    } else if (intExtra == 12) {
                        TetherSettings.this.startTethering(2);
                        TetherSettings.this.mBluetoothEnableForTether = false;
                    }
                }
                TetherSettings.this.updateState();
            }
        }
    }

    public int getMetricsCategory() {
        return 90;
    }

    public TetherSettings() {
        super("no_config_tethering");
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mWifiTetherPreferenceController = new WifiTetherPreferenceController(context, getLifecycle());
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent i = getActivity().getIntent();
        if (i != null) {
            String fromQuickSetting = i.getStringExtra("from_quick_setting");
            if (!TextUtils.isEmpty(fromQuickSetting) && "1".equals(fromQuickSetting)) {
                Log.i(TAG, "Redirect...");
                ((SettingsActivity) getActivity()).startPreferencePanel(WifiTetherSettings.class.getName(), null, R.string.master_clear_confirm_title, null, null, 0);
                finish();
            }
        }
        addPreferencesFromResource(R.xml.tether_prefs);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.tethering_footer_info);
        this.mDataSaverBackend = new DataSaverBackend(getContext());
        this.mDataSaverEnabled = this.mDataSaverBackend.isDataSaverEnabled();
        this.mDataSaverFooter = findPreference(DATA_SAVER_FOOTER);
        setIfOnlyAvailableForAdmins(true);
        if (isUiRestricted()) {
            this.mUnavailable = true;
            getPreferenceScreen().removeAll();
            return;
        }
        Activity activity = getActivity();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (adapter.isEnabled()) {
                adapter.getProfileProxy(activity.getApplicationContext(), this.mProfileServiceListener, 5);
            } else {
                registerBluetoothStateListener(true);
            }
        }
        this.mUsbTether = (SwitchPreference) findPreference(USB_TETHER_SETTINGS);
        this.mBluetoothTether = (SwitchPreference) findPreference(ENABLE_BLUETOOTH_TETHERING);
        this.mDataSaverBackend.addListener(this);
        this.mCm = (ConnectivityManager) getSystemService("connectivity");
        this.mUsbRegexs = this.mCm.getTetherableUsbRegexs();
        this.mBluetoothRegexs = this.mCm.getTetherableBluetoothRegexs();
        boolean usbAvailable = this.mUsbRegexs.length != 0;
        boolean bluetoothAvailable = this.mBluetoothRegexs.length != 0;
        if (!usbAvailable || Utils.isMonkeyRunning()) {
            getPreferenceScreen().removePreference(this.mUsbTether);
        }
        this.mWifiTetherPreferenceController.displayPreference(getPreferenceScreen());
        if (bluetoothAvailable) {
            BluetoothPan pan = (BluetoothPan) this.mBluetoothPan.get();
            if (pan == null || !pan.isTetheringOn()) {
                this.mBluetoothTether.setChecked(false);
            } else {
                this.mBluetoothTether.setChecked(true);
            }
        } else {
            getPreferenceScreen().removePreference(this.mBluetoothTether);
        }
        onDataSaverChanged(this.mDataSaverBackend.isDataSaverEnabled());
    }

    public void onDestroy() {
        this.mDataSaverBackend.remListener(this);
        closeProfileProxy();
        registerBluetoothStateListener(false);
        super.onDestroy();
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        this.mDataSaverEnabled = isDataSaving;
        this.mUsbTether.setEnabled(this.mDataSaverEnabled ^ 1);
        this.mBluetoothTether.setEnabled(this.mDataSaverEnabled ^ 1);
        this.mDataSaverFooter.setVisible(this.mDataSaverEnabled);
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }

    public void onStart() {
        super.onStart();
        if (this.mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R.string.tethering_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        Activity activity = getActivity();
        this.mStartTetheringCallback = new OnStartTetheringCallback(this);
        this.mMassStorageActive = "shared".equals(Environment.getExternalStorageState());
        this.mTetherChangeReceiver = new TetherChangeReceiver(this, null);
        Intent intent = activity.registerReceiver(this.mTetherChangeReceiver, new IntentFilter("android.net.conn.TETHER_STATE_CHANGED"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_UNSHARED");
        filter.addDataScheme("file");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        activity.registerReceiver(this.mTetherChangeReceiver, filter);
        if (intent != null) {
            this.mTetherChangeReceiver.onReceive(activity, intent);
        }
        updateState();
    }

    public void onStop() {
        super.onStop();
        if (!this.mUnavailable) {
            getActivity().unregisterReceiver(this.mTetherChangeReceiver);
            this.mTetherChangeReceiver = null;
            this.mStartTetheringCallback = null;
        }
    }

    private void updateState() {
        updateState(this.mCm.getTetherableIfaces(), this.mCm.getTetheredIfaces(), this.mCm.getTetheringErroredIfaces());
    }

    private void updateState(String[] available, String[] tethered, String[] errored) {
        updateUsbState(available, tethered, errored);
        updateBluetoothState();
    }

    private void updateUsbState(String[] available, String[] tethered, String[] errored) {
        int usbError;
        String[] strArr = available;
        String[] strArr2 = tethered;
        String[] strArr3 = errored;
        boolean usbAvailable = this.mUsbConnected && !this.mMassStorageActive;
        int length = strArr.length;
        int usbError2 = 0;
        int usbError3 = 0;
        while (usbError3 < length) {
            String s = strArr[usbError3];
            usbError = usbError2;
            for (String regex : this.mUsbRegexs) {
                if (s.matches(regex) && usbError == 0) {
                    usbError = this.mCm.getLastTetherError(s);
                }
            }
            usbError3++;
            usbError2 = usbError;
        }
        length = strArr2.length;
        boolean usbTethered = false;
        usbError3 = 0;
        while (usbError3 < length) {
            String s2 = strArr2[usbError3];
            boolean usbTethered2 = usbTethered;
            for (String regex2 : this.mUsbRegexs) {
                if (s2.matches(regex2)) {
                    usbTethered2 = true;
                }
            }
            usbError3++;
            usbTethered = usbTethered2;
        }
        length = strArr3.length;
        boolean usbErrored = false;
        usbError3 = 0;
        while (usbError3 < length) {
            String s3 = strArr3[usbError3];
            boolean usbErrored2 = usbErrored;
            for (String regex3 : this.mUsbRegexs) {
                if (s3.matches(regex3)) {
                    usbErrored2 = true;
                }
            }
            usbError3++;
            usbErrored = usbErrored2;
        }
        if (usbTethered) {
            this.mUsbTether.setEnabled(this.mDataSaverEnabled ^ 1);
            this.mUsbTether.setChecked(true);
        } else if (usbAvailable) {
            this.mUsbTether.setEnabled(1 ^ this.mDataSaverEnabled);
            this.mUsbTether.setChecked(false);
        } else {
            this.mUsbTether.setEnabled(false);
            this.mUsbTether.setChecked(false);
        }
    }

    private void updateBluetoothState() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            int btState = adapter.getState();
            if (btState == 13) {
                this.mBluetoothTether.setEnabled(false);
            } else if (btState == 11) {
                this.mBluetoothTether.setEnabled(false);
            } else {
                BluetoothPan bluetoothPan = (BluetoothPan) this.mBluetoothPan.get();
                if (btState == 12 && bluetoothPan != null && bluetoothPan.isTetheringOn()) {
                    this.mBluetoothTether.setChecked(true);
                    this.mBluetoothTether.setEnabled(this.mDataSaverEnabled ^ 1);
                } else {
                    this.mBluetoothTether.setEnabled(1 ^ this.mDataSaverEnabled);
                    this.mBluetoothTether.setChecked(false);
                }
            }
        }
    }

    public static boolean isProvisioningNeededButUnavailable(Context context) {
        return TetherUtil.isProvisioningNeeded(context) && !isIntentAvailable(context);
    }

    private static boolean isIntentAvailable(Context context) {
        String[] provisionApp = context.getResources().getStringArray(17236019);
        boolean z = false;
        if (provisionApp.length < 2) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(provisionApp[0], provisionApp[1]);
        if (packageManager.queryIntentActivities(intent, 65536).size() > 0) {
            z = true;
        }
        return z;
    }

    private void startTethering(int choice) {
        if (choice == 2) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.getState() == 10) {
                this.mBluetoothEnableForTether = true;
                adapter.enable();
                this.mBluetoothTether.setEnabled(false);
                return;
            }
        }
        this.mCm.startTethering(choice, true, this.mStartTetheringCallback, this.mHandler);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mUsbTether) {
            if (this.mUsbTether.isChecked()) {
                startTethering(1);
            } else {
                this.mCm.stopTethering(1);
            }
        } else if (preference == this.mBluetoothTether) {
            if (this.mBluetoothTether.isChecked()) {
                startTethering(2);
            } else {
                this.mCm.stopTethering(2);
                this.mTimeoutHandler.sendMessageDelayed(this.mTimeoutHandler.obtainMessage(1, this), 10);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    public int getHelpResource() {
        return R.string.help_url_tether;
    }

    private void registerBluetoothStateListener(boolean register) {
        IBluetoothManager mgr = getBluetoothManager();
        if (mgr != null) {
            if (register) {
                try {
                    if (this.mBluetoohListener.get() == null) {
                        Log.d(TAG, "register listener");
                        mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
                        this.mBluetoohListener.set(this.mBluetoothStateChangeCallback);
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    return;
                }
            }
            if (!register && this.mBluetoohListener.get() != null) {
                Log.d(TAG, "unregister listener");
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
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && this.mBluetoothPan.get() == null) {
            Log.d(TAG, "openProfileProxy");
            adapter.getProfileProxy(getActivity().getApplicationContext(), this.mProfileServiceListener, 5);
        }
    }

    private void closeProfileProxy() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && this.mBluetoothPan.get() != null) {
            Log.d(TAG, "closeProfileProxy");
            adapter.closeProfileProxy(5, (BluetoothProfile) this.mBluetoothPan.get());
            this.mBluetoothPan.set(null);
        } else if (adapter != null && this.mLastBluetoothPan != null) {
            Log.d(TAG, "close last ProfileProxy");
            adapter.closeProfileProxy(5, this.mLastBluetoothPan);
            this.mLastBluetoothPan = null;
        }
    }
}
