package com.android.settings.connecteddevice;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.Utils;
import com.oneplus.settings.ui.OPCarKitButtonPreference;
import com.oneplus.settings.utils.OPUtils;

public class OPBluetoothCarKitDevicePreference extends OPCarKitButtonPreference implements Callback {
    private static int sDimAlpha = Integer.MIN_VALUE;
    private String contentDescription = null;
    private final BluetoothAdapter mBluetoothAdapter;
    private final CachedBluetoothDevice mCachedDevice;
    private Context mContext;
    Resources mResources;
    private final boolean mShowDevicesWithoutNames;
    private final UserManager mUserManager;

    public OPBluetoothCarKitDevicePreference(Context context, CachedBluetoothDevice cachedDevice, boolean showDeviceWithoutNames) {
        super(context, null);
        this.mContext = context;
        this.mResources = getContext().getResources();
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
    public CachedBluetoothDevice getCachedDevice() {
        return this.mCachedDevice;
    }

    public CachedBluetoothDevice getBluetoothDevice() {
        return this.mCachedDevice;
    }

    public int hashCode() {
        return this.mCachedDevice.hashCode();
    }

    public void onDeviceAttributesChanged() {
        setLeftTextTitle(this.mCachedDevice.getName());
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

    public void setButtonString(String buttonString) {
        this.mButtonString = buttonString;
    }

    public void setIcon(Drawable drawable) {
        this.mIcon = drawable;
    }

    public void setButtonEnable(boolean enable) {
        this.mButtonEnable = enable;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        if (this.mCachedDevice.getBondState() == 12) {
            if (this.mBluetoothAdapter.isCarkit(this.mCachedDevice.getDevice())) {
                setIcon((int) R.drawable.op_ic_settings_car);
                setButtonString(this.mContext.getString(R.string.oneplus_remove));
                setButtonEnable(true);
                setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        OPBluetoothCarKitDevicePreference.this.mBluetoothAdapter.removeCarkit(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getDevice());
                        OPBluetoothCarKitDevicePreference.this.mContext.sendBroadcast(new Intent("oneplus.action.intent.UpdateBluetoothCarkitDevice"));
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getName());
                        stringBuilder.append("  ");
                        stringBuilder.append(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getAddress());
                        OPUtils.sendAppTracker("blue_car_remove", stringBuilder.toString());
                    }
                });
            } else {
                setButtonString(this.mContext.getString(R.string.oneplus_add));
                setButtonEnable(true);
                setOnButtonClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        OPBluetoothCarKitDevicePreference.this.mBluetoothAdapter.addCarkit(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getDevice());
                        OPBluetoothCarKitDevicePreference.this.mContext.sendBroadcast(new Intent("oneplus.action.intent.UpdateBluetoothCarkitDevice"));
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getName());
                        stringBuilder.append("  ");
                        stringBuilder.append(OPBluetoothCarKitDevicePreference.this.mCachedDevice.getAddress());
                        OPUtils.sendAppTracker("blue_car_add", stringBuilder.toString());
                    }
                });
            }
        }
        super.onBindViewHolder(view);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof OPBluetoothCarKitDevicePreference)) {
            return false;
        }
        return this.mCachedDevice.equals(((OPBluetoothCarKitDevicePreference) o).mCachedDevice);
    }
}
