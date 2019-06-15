package com.android.settings.development;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothCodecStatus;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settings.development.BluetoothA2dpHwOffloadRebootDialog.OnA2dpHwDialogConfirmedListener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.development.AbstractEnableAdbPreferenceController;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settingslib.development.SystemPropPoker;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DevelopmentSettingsDashboardFragment extends RestrictedDashboardFragment implements OnSwitchChangeListener, OemUnlockDialogHost, AdbDialogHost, AdbClearKeysDialogHost, LogPersistDialogHost, OnA2dpHwDialogConfirmedListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(context);
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.development_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DevelopmentSettingsDashboardFragment.buildPreferenceControllers(context, null, null, null, null);
        }
    };
    private static final String TAG = "DevSettingsDashboard";
    private static OemUnlockPreferenceController mOemController;
    private BluetoothA2dp mBluetoothA2dp;
    private final BluetoothA2dpConfigStore mBluetoothA2dpConfigStore = new BluetoothA2dpConfigStore();
    private final BroadcastReceiver mBluetoothA2dpReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = DevelopmentSettingsDashboardFragment.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mBluetoothA2dpReceiver.onReceive intent=");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
            if ("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED".equals(intent.getAction())) {
                BluetoothCodecStatus codecStatus = (BluetoothCodecStatus) intent.getParcelableExtra("android.bluetooth.codec.extra.CODEC_STATUS");
                String str2 = DevelopmentSettingsDashboardFragment.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Received BluetoothCodecStatus=");
                stringBuilder2.append(codecStatus);
                Log.d(str2, stringBuilder2.toString());
                for (AbstractPreferenceController controller : DevelopmentSettingsDashboardFragment.this.mPreferenceControllers) {
                    if (controller instanceof BluetoothServiceConnectionListener) {
                        ((BluetoothServiceConnectionListener) controller).onBluetoothCodecUpdated();
                    }
                }
            }
        }
    };
    private final ServiceListener mBluetoothA2dpServiceListener = new ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            synchronized (DevelopmentSettingsDashboardFragment.this.mBluetoothA2dpConfigStore) {
                DevelopmentSettingsDashboardFragment.this.mBluetoothA2dp = (BluetoothA2dp) proxy;
            }
            for (AbstractPreferenceController controller : DevelopmentSettingsDashboardFragment.this.mPreferenceControllers) {
                if (controller instanceof BluetoothServiceConnectionListener) {
                    ((BluetoothServiceConnectionListener) controller).onBluetoothServiceConnected(DevelopmentSettingsDashboardFragment.this.mBluetoothA2dp);
                }
            }
        }

        public void onServiceDisconnected(int profile) {
            synchronized (DevelopmentSettingsDashboardFragment.this.mBluetoothA2dpConfigStore) {
                DevelopmentSettingsDashboardFragment.this.mLastConnectedBluetoothA2dp = DevelopmentSettingsDashboardFragment.this.mBluetoothA2dp;
                DevelopmentSettingsDashboardFragment.this.mBluetoothA2dp = null;
            }
            for (AbstractPreferenceController controller : DevelopmentSettingsDashboardFragment.this.mPreferenceControllers) {
                if (controller instanceof BluetoothServiceConnectionListener) {
                    ((BluetoothServiceConnectionListener) controller).onBluetoothServiceDisconnected();
                }
            }
        }
    };
    private final BroadcastReceiver mEnableAdbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            for (AbstractPreferenceController controller : DevelopmentSettingsDashboardFragment.this.mPreferenceControllers) {
                if (controller instanceof AdbOnChangeListener) {
                    ((AdbOnChangeListener) controller).onAdbSettingChanged();
                }
            }
        }
    };
    private boolean mIsAvailable = true;
    private BluetoothA2dp mLastConnectedBluetoothA2dp;
    private List<AbstractPreferenceController> mPreferenceControllers = new ArrayList();
    private SwitchBar mSwitchBar;
    private DevelopmentSwitchBarController mSwitchBarController;

    public DevelopmentSettingsDashboardFragment() {
        super("no_debugging_features");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (Utils.isMonkeyRunning()) {
            getActivity().finish();
        }
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        setIfOnlyAvailableForAdmins(true);
        if (isUiRestricted() || !Utils.isDeviceProvisioned(getActivity())) {
            this.mIsAvailable = false;
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R.string.development_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBarController = new DevelopmentSwitchBarController(this, this.mSwitchBar, this.mIsAvailable, getLifecycle());
        this.mSwitchBar.show();
        if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(getContext())) {
            enableDeveloperOptions();
        } else {
            disableDeveloperOptions();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registerReceivers();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            Log.d(TAG, "bluetooth on");
            adapter.closeProfileProxy(2, this.mBluetoothA2dp);
            this.mBluetoothA2dp = null;
            adapter.getProfileProxy(getActivity(), this.mBluetoothA2dpServiceListener, 2);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceivers();
        if (mOemController != null) {
            mOemController.unBindSimlockConnection();
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.closeProfileProxy(2, this.mBluetoothA2dp);
            if (!(this.mLastConnectedBluetoothA2dp == null || this.mLastConnectedBluetoothA2dp == this.mBluetoothA2dp)) {
                adapter.closeProfileProxy(2, this.mLastConnectedBluetoothA2dp);
            }
            this.mLastConnectedBluetoothA2dp = null;
            this.mBluetoothA2dp = null;
        }
    }

    public int getMetricsCategory() {
        return 39;
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (switchView == this.mSwitchBar.getSwitch() && isChecked != DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(getContext())) {
            if (isChecked) {
                EnableDevelopmentSettingWarningDialog.show(this);
            } else {
                disableDeveloperOptions();
            }
        }
    }

    public void onOemUnlockDialogConfirmed() {
        ((OemUnlockPreferenceController) getDevelopmentOptionsController(OemUnlockPreferenceController.class)).onOemUnlockConfirmed();
    }

    public void onOemUnlockDialogDismissed() {
        ((OemUnlockPreferenceController) getDevelopmentOptionsController(OemUnlockPreferenceController.class)).onOemUnlockDismissed();
    }

    public void onEnableAdbDialogConfirmed() {
        ((AdbPreferenceController) getDevelopmentOptionsController(AdbPreferenceController.class)).onAdbDialogConfirmed();
    }

    public void onEnableAdbDialogDismissed() {
        ((AdbPreferenceController) getDevelopmentOptionsController(AdbPreferenceController.class)).onAdbDialogDismissed();
    }

    public void onAdbClearKeysDialogConfirmed() {
        ((ClearAdbKeysPreferenceController) getDevelopmentOptionsController(ClearAdbKeysPreferenceController.class)).onClearAdbKeysConfirmed();
    }

    public void onDisableLogPersistDialogConfirmed() {
        ((LogPersistPreferenceController) getDevelopmentOptionsController(LogPersistPreferenceController.class)).onDisableLogPersistDialogConfirmed();
    }

    public void onDisableLogPersistDialogRejected() {
        ((LogPersistPreferenceController) getDevelopmentOptionsController(LogPersistPreferenceController.class)).onDisableLogPersistDialogRejected();
    }

    public void onA2dpHwDialogConfirmed() {
        ((BluetoothA2dpHwOffloadPreferenceController) getDevelopmentOptionsController(BluetoothA2dpHwOffloadPreferenceController.class)).onA2dpHwDialogConfirmed();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean handledResult = false;
        for (AbstractPreferenceController controller : this.mPreferenceControllers) {
            if (controller instanceof OnActivityResultListener) {
                handledResult |= ((OnActivityResultListener) controller).onActivityResult(requestCode, resultCode, data);
            }
        }
        if (!handledResult) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return Utils.isMonkeyRunning() ? R.xml.placeholder_prefs : R.xml.development_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        if (Utils.isMonkeyRunning()) {
            this.mPreferenceControllers = new ArrayList();
            return null;
        }
        this.mPreferenceControllers = buildPreferenceControllers(context, getActivity(), getLifecycle(), this, new BluetoothA2dpConfigStore());
        return this.mPreferenceControllers;
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mEnableAdbReceiver, new IntentFilter(AbstractEnableAdbPreferenceController.ACTION_ENABLE_ADB_STATE_CHANGED));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.a2dp.profile.action.CODEC_CONFIG_CHANGED");
        getActivity().registerReceiver(this.mBluetoothA2dpReceiver, filter);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mEnableAdbReceiver);
        getActivity().unregisterReceiver(this.mBluetoothA2dpReceiver);
    }

    private void enableDeveloperOptions() {
        if (!Utils.isMonkeyRunning()) {
            DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(getContext(), true);
            for (AbstractPreferenceController controller : this.mPreferenceControllers) {
                if (controller instanceof DeveloperOptionsPreferenceController) {
                    ((DeveloperOptionsPreferenceController) controller).onDeveloperOptionsEnabled();
                }
            }
        }
    }

    private void disableDeveloperOptions() {
        if (!Utils.isMonkeyRunning()) {
            DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(getContext(), false);
            SystemPropPoker poker = SystemPropPoker.getInstance();
            poker.blockPokes();
            for (AbstractPreferenceController controller : this.mPreferenceControllers) {
                if (controller instanceof DeveloperOptionsPreferenceController) {
                    ((DeveloperOptionsPreferenceController) controller).onDeveloperOptionsDisabled();
                }
            }
            poker.unblockPokes();
            poker.poke();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onEnableDevelopmentOptionsConfirmed() {
        enableDeveloperOptions();
    }

    /* Access modifiers changed, original: 0000 */
    public void onEnableDevelopmentOptionsRejected() {
        this.mSwitchBar.setChecked(false);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Activity activity, Lifecycle lifecycle, DevelopmentSettingsDashboardFragment fragment, BluetoothA2dpConfigStore bluetoothA2dpConfigStore) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new MemoryUsagePreferenceController(context));
        controllers.add(new BugReportPreferenceController(context));
        controllers.add(new LocalBackupPasswordPreferenceController(context));
        controllers.add(new OPGetLogsPreferenceController(context));
        controllers.add(new OPAdvancedRebootPreferenceController(context, lifecycle));
        controllers.add(new OPWifiVerboseMultiBroadcastPreferenceController(context));
        if (OPUtils.isNeedTcpTimestampsControl()) {
            controllers.add(new OPTcpTimestampsPreferenceController(context));
        }
        controllers.add(new NfcNonStdPreferenceController(context));
        controllers.add(new StayAwakePreferenceController(context, lifecycle));
        controllers.add(new HdcpCheckingPreferenceController(context));
        controllers.add(new DarkUIPreferenceController(context));
        controllers.add(new BluetoothSnoopLogPreferenceController(context));
        mOemController = new OemUnlockPreferenceController(context, activity, fragment);
        controllers.add(mOemController);
        controllers.add(new FileEncryptionPreferenceController(context));
        controllers.add(new WebViewAppPreferenceController(context));
        controllers.add(new CoolColorTemperaturePreferenceController(context));
        controllers.add(new DisableAutomaticUpdatesPreferenceController(context));
        controllers.add(new AdbPreferenceController(context, fragment));
        controllers.add(new ClearAdbKeysPreferenceController(context, fragment));
        controllers.add(new LocalTerminalPreferenceController(context));
        controllers.add(new BugReportInPowerPreferenceController(context));
        controllers.add(new MockLocationAppPreferenceController(context, fragment));
        controllers.add(new DebugViewAttributesPreferenceController(context));
        controllers.add(new SelectDebugAppPreferenceController(context, fragment));
        controllers.add(new WaitForDebuggerPreferenceController(context));
        controllers.add(new EnableGpuDebugLayersPreferenceController(context));
        controllers.add(new VerifyAppsOverUsbPreferenceController(context));
        controllers.add(new LogdSizePreferenceController(context));
        controllers.add(new LogPersistPreferenceController(context, fragment, lifecycle));
        controllers.add(new CameraLaserSensorPreferenceController(context));
        controllers.add(new WifiDisplayCertificationPreferenceController(context));
        controllers.add(new WifiCoverageExtendPreferenceController(context));
        controllers.add(new WifiVerboseLoggingPreferenceController(context));
        controllers.add(new WifiConnectedMacRandomizationPreferenceController(context));
        controllers.add(new MobileDataAlwaysOnPreferenceController(context));
        controllers.add(new TetheringHardwareAccelPreferenceController(context));
        controllers.add(new BluetoothDeviceNoNamePreferenceController(context));
        controllers.add(new BluetoothAbsoluteVolumePreferenceController(context));
        controllers.add(new BluetoothAvrcpVersionPreferenceController(context));
        controllers.add(new BluetoothA2dpHwOffloadPreferenceController(context, fragment));
        controllers.add(new BluetoothAudioCodecPreferenceController(context, lifecycle, bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioSampleRatePreferenceController(context, lifecycle, bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioBitsPerSamplePreferenceController(context, lifecycle, bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioChannelModePreferenceController(context, lifecycle, bluetoothA2dpConfigStore));
        controllers.add(new BluetoothAudioQualityPreferenceController(context, lifecycle, bluetoothA2dpConfigStore));
        controllers.add(new BluetoothMaxConnectedAudioDevicesPreferenceController(context));
        controllers.add(new ShowTapsPreferenceController(context));
        controllers.add(new PointerLocationPreferenceController(context));
        controllers.add(new ShowSurfaceUpdatesPreferenceController(context));
        controllers.add(new ShowLayoutBoundsPreferenceController(context));
        controllers.add(new RtlLayoutPreferenceController(context));
        controllers.add(new WindowAnimationScalePreferenceController(context));
        controllers.add(new EmulateDisplayCutoutPreferenceController(context));
        controllers.add(new TransitionAnimationScalePreferenceController(context));
        controllers.add(new AnimatorDurationScalePreferenceController(context));
        controllers.add(new SecondaryDisplayPreferenceController(context));
        controllers.add(new ForceGpuRenderingPreferenceController(context));
        controllers.add(new GpuViewUpdatesPreferenceController(context));
        controllers.add(new HardwareLayersUpdatesPreferenceController(context));
        controllers.add(new DebugGpuOverdrawPreferenceController(context));
        controllers.add(new DebugNonRectClipOperationsPreferenceController(context));
        controllers.add(new ForceMSAAPreferenceController(context));
        controllers.add(new HardwareOverlaysPreferenceController(context));
        controllers.add(new SimulateColorSpacePreferenceController(context));
        controllers.add(new UsbAudioRoutingPreferenceController(context));
        controllers.add(new StrictModePreferenceController(context));
        controllers.add(new ProfileGpuRenderingPreferenceController(context));
        controllers.add(new KeepActivitiesPreferenceController(context));
        controllers.add(new BackgroundProcessLimitPreferenceController(context));
        controllers.add(new ShowFirstCrashDialogPreferenceController(context));
        controllers.add(new AppsNotRespondingPreferenceController(context));
        controllers.add(new NotificationChannelWarningsPreferenceController(context));
        controllers.add(new AllowAppsOnExternalPreferenceController(context));
        controllers.add(new ResizableActivityPreferenceController(context));
        controllers.add(new FreeformWindowsPreferenceController(context));
        controllers.add(new ShortcutManagerThrottlingPreferenceController(context));
        controllers.add(new EnableGnssRawMeasFullTrackingPreferenceController(context));
        controllers.add(new DefaultLaunchPreferenceController(context, "running_apps"));
        controllers.add(new DefaultLaunchPreferenceController(context, "demo_mode"));
        controllers.add(new DefaultLaunchPreferenceController(context, "quick_settings_tiles"));
        controllers.add(new DefaultLaunchPreferenceController(context, "feature_flags_dashboard"));
        controllers.add(new DefaultLaunchPreferenceController(context, "default_usb_configuration"));
        controllers.add(new DefaultLaunchPreferenceController(context, "density"));
        controllers.add(new DefaultLaunchPreferenceController(context, "background_check"));
        controllers.add(new DefaultLaunchPreferenceController(context, "inactive_apps"));
        return controllers;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public <T extends AbstractPreferenceController> T getDevelopmentOptionsController(Class<T> clazz) {
        return use(clazz);
    }
}
