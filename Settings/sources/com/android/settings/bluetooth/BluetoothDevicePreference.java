package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.widget.GearPreference;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.Utils;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public final class BluetoothDevicePreference extends GearPreference implements Callback {
    private static final String TAG = "BluetoothDevicePref";
    private static int sDimAlpha = Integer.MIN_VALUE;
    private String contentDescription = null;
    private final CachedBluetoothDevice mCachedDevice;
    private AlertDialog mDisconnectDialog;
    private boolean mHideSecondTarget = false;
    Resources mResources = getContext().getResources();
    private final boolean mShowDevicesWithoutNames;
    private final UserManager mUserManager;

    public BluetoothDevicePreference(Context context, CachedBluetoothDevice cachedDevice, boolean showDeviceWithoutNames) {
        super(context, null);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mShowDevicesWithoutNames = showDeviceWithoutNames;
        if (sDimAlpha == Integer.MIN_VALUE) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16842803, outValue, true);
            sDimAlpha = (int) (outValue.getFloat() * 255.0f);
        }
        this.mCachedDevice = cachedDevice;
        this.mCachedDevice.registerCallback(this);
        onDeviceAttributesChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void rebind() {
        notifyChanged();
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldHideSecondTarget() {
        return this.mCachedDevice == null || this.mCachedDevice.getBondState() != 12 || this.mUserManager.hasUserRestriction("no_config_bluetooth") || this.mHideSecondTarget;
    }

    /* Access modifiers changed, original: protected */
    public int getSecondTargetResId() {
        return R.layout.preference_widget_gear;
    }

    /* Access modifiers changed, original: 0000 */
    public CachedBluetoothDevice getCachedDevice() {
        return this.mCachedDevice;
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        this.mCachedDevice.unregisterCallback(this);
        if (this.mDisconnectDialog != null) {
            this.mDisconnectDialog.dismiss();
            this.mDisconnectDialog = null;
        }
    }

    public CachedBluetoothDevice getBluetoothDevice() {
        return this.mCachedDevice;
    }

    public void hideSecondTarget(boolean hideSecondTarget) {
        this.mHideSecondTarget = hideSecondTarget;
    }

    public void onDeviceAttributesChanged() {
        setTitle((CharSequence) this.mCachedDevice.getName());
        setSummary((CharSequence) this.mCachedDevice.getConnectionSummary());
        Pair<Drawable, String> pair = Utils.getBtClassDrawableWithDescription(getContext(), this.mCachedDevice);
        if (pair.first != null) {
            setIcon((Drawable) pair.first);
            this.contentDescription = (String) pair.second;
        }
        boolean z = true;
        setEnabled(this.mCachedDevice.isBusy() ^ 1);
        if (!(this.mShowDevicesWithoutNames || this.mCachedDevice.hasHumanReadableName())) {
            z = false;
        }
        setVisible(z);
        notifyHierarchyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        ImageView deviceDetails;
        if (findPreferenceInHierarchy("bt_checkbox") != null) {
            setDependency("bt_checkbox");
        }
        if (this.mCachedDevice.getBondState() == 12) {
            deviceDetails = (ImageView) view.findViewById(R.id.settings_button);
            if (deviceDetails != null) {
                deviceDetails.setOnClickListener(this);
            }
        }
        deviceDetails = (ImageView) view.findViewById(16908294);
        if (deviceDetails != null) {
            deviceDetails.setContentDescription(this.contentDescription);
        }
        super.onBindViewHolder(view);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof BluetoothDevicePreference)) {
            return false;
        }
        return this.mCachedDevice.equals(((BluetoothDevicePreference) o).mCachedDevice);
    }

    public int hashCode() {
        return this.mCachedDevice.hashCode();
    }

    public int compareTo(Preference another) {
        if (another instanceof BluetoothDevicePreference) {
            return this.mCachedDevice.compareTo(((BluetoothDevicePreference) another).mCachedDevice);
        }
        return super.compareTo(another);
    }

    /* Access modifiers changed, original: 0000 */
    public void onClicked() {
        Context context = getContext();
        int bondState = this.mCachedDevice.getBondState();
        MetricsFeatureProvider metricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        if (this.mCachedDevice.isConnected()) {
            metricsFeatureProvider.action(context, 868, new Pair[0]);
            askDisconnect();
        } else if (bondState == 12) {
            metricsFeatureProvider.action(context, 867, new Pair[0]);
            this.mCachedDevice.connect(true);
        } else if (bondState == 10) {
            metricsFeatureProvider.action(context, 866, new Pair[0]);
            if (!this.mCachedDevice.hasHumanReadableName()) {
                metricsFeatureProvider.action(context, 1096, new Pair[0]);
            }
            pair();
        }
    }

    private void askDisconnect() {
        Context context = getContext();
        String name = this.mCachedDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.bluetooth_device);
        }
        String message = context.getString(R.string.bluetooth_disconnect_all_profiles, new Object[]{name});
        String title = context.getString(R.string.bluetooth_disconnect_title);
        this.mDisconnectDialog = Utils.showDisconnectDialog(context, this.mDisconnectDialog, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevicePreference.this.mCachedDevice.disconnect();
            }
        }, title, Html.fromHtml(message));
    }

    private void pair() {
        if (!this.mCachedDevice.startPairing()) {
            Utils.showError(getContext(), this.mCachedDevice.getName(), R.string.bluetooth_pairing_error_message);
        }
    }
}
