package com.android.settings.bluetooth;

import android.content.Context;
import android.provider.Settings.System;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.location.ScanningSettings;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.utils.AnnotationSpan;
import com.android.settings.utils.AnnotationSpan.LinkInfo;
import com.android.settings.widget.SwitchWidgetController;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.widget.FooterPreference;
import com.oneplus.settings.utils.OPUtils;

public class BluetoothSwitchPreferenceController implements LifecycleObserver, OnStart, OnStop, OnSwitchChangeListener, OnClickListener {
    @VisibleForTesting
    LocalBluetoothAdapter mBluetoothAdapter;
    private BluetoothEnabler mBluetoothEnabler;
    private LocalBluetoothManager mBluetoothManager;
    private Context mContext;
    private FooterPreference mFooterPreference;
    private RestrictionUtils mRestrictionUtils;
    private SwitchWidgetController mSwitch;

    public BluetoothSwitchPreferenceController(Context context, SwitchWidgetController switchController, FooterPreference footerPreference) {
        this(context, Utils.getLocalBtManager(context), new RestrictionUtils(), switchController, footerPreference);
    }

    @VisibleForTesting
    public BluetoothSwitchPreferenceController(Context context, LocalBluetoothManager bluetoothManager, RestrictionUtils restrictionUtils, SwitchWidgetController switchController, FooterPreference footerPreference) {
        this.mBluetoothManager = bluetoothManager;
        this.mRestrictionUtils = restrictionUtils;
        this.mSwitch = switchController;
        this.mContext = context;
        this.mFooterPreference = footerPreference;
        this.mSwitch.setupView();
        updateText(this.mSwitch.isChecked());
        if (this.mBluetoothManager != null) {
            this.mBluetoothAdapter = this.mBluetoothManager.getBluetoothAdapter();
        }
        this.mBluetoothEnabler = new BluetoothEnabler(context, switchController, FeatureFactory.getFactory(context).getMetricsFeatureProvider(), this.mBluetoothManager, 870, this.mRestrictionUtils);
        this.mBluetoothEnabler.setToggleCallback(this);
    }

    public void onStart() {
        if (this.mBluetoothAdapter != null) {
            setBluetoothDiscoverableState();
            String mOPDeviceName = System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename");
            this.mBluetoothAdapter.setName(OPUtils.resetDeviceNameIfInvalid(this.mContext));
        }
        this.mBluetoothEnabler.resume(this.mContext);
        if (this.mSwitch != null) {
            updateText(this.mSwitch.isChecked());
        }
    }

    public void onStop() {
        this.mBluetoothEnabler.pause();
    }

    private void setBluetoothDiscoverableState() {
        int mBluetoothScanMode = System.getInt(this.mContext.getContentResolver(), "bluetooth_default_scan_mode", 23);
        if (mBluetoothScanMode == 23) {
            this.mBluetoothAdapter.setScanMode(23);
        } else if (mBluetoothScanMode == 21) {
            this.mBluetoothAdapter.setScanMode(21);
        }
    }

    public boolean onSwitchToggled(boolean isChecked) {
        updateText(isChecked);
        return true;
    }

    public void onClick(View v) {
        new SubSettingLauncher(this.mContext).setDestination(ScanningSettings.class.getName()).setSourceMetricsCategory(1390).launch();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateText(boolean isChecked) {
        if (isChecked || !Utils.isBluetoothScanningEnabled(this.mContext)) {
            this.mFooterPreference.setTitle((int) R.string.bluetooth_empty_list_bluetooth_off);
            return;
        }
        LinkInfo info = new LinkInfo(LinkInfo.DEFAULT_ANNOTATION, this);
        this.mFooterPreference.setTitle(AnnotationSpan.linkify(this.mContext.getText(R.string.bluetooth_scanning_on_info_message), info));
    }
}
