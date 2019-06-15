package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.Pair;
import com.android.settingslib.R;
import com.android.settingslib.graph.BluetoothDeviceLayerDrawable;

public class Utils {
    public static final boolean D = true;
    public static final boolean V = false;
    private static ErrorListener sErrorListener;

    public interface ErrorListener {
        void onShowError(Context context, String str, int i);
    }

    public static int getConnectionStateSummary(int connectionState) {
        switch (connectionState) {
            case 0:
                return R.string.bluetooth_disconnected;
            case 1:
                return R.string.bluetooth_connecting;
            case 2:
                return R.string.bluetooth_connected;
            case 3:
                return R.string.bluetooth_disconnecting;
            default:
                return 0;
        }
    }

    static void showError(Context context, String name, int messageResId) {
        if (sErrorListener != null) {
            sErrorListener.onShowError(context, name, messageResId);
        }
    }

    public static void setErrorListener(ErrorListener listener) {
        sErrorListener = listener;
    }

    public static Pair<Drawable, String> getBtClassDrawableWithDescription(Context context, CachedBluetoothDevice cachedDevice) {
        return getBtClassDrawableWithDescription(context, cachedDevice, 1.0f);
    }

    public static Pair<Drawable, String> getBtClassDrawableWithDescription(Context context, CachedBluetoothDevice cachedDevice, float iconScale) {
        BluetoothClass btClass = cachedDevice.getBtClass();
        int level = cachedDevice.getBatteryLevel();
        if (btClass != null) {
            int majorDeviceClass = btClass.getMajorDeviceClass();
            if (majorDeviceClass == 256) {
                return new Pair(getBluetoothDrawable(context, R.drawable.ic_bt_laptop, level, iconScale), context.getString(R.string.bluetooth_talkback_computer));
            }
            if (majorDeviceClass == 512) {
                return new Pair(getBluetoothDrawable(context, R.drawable.ic_bt_cellphone, level, iconScale), context.getString(R.string.bluetooth_talkback_phone));
            }
            if (majorDeviceClass == 1280) {
                return new Pair(getBluetoothDrawable(context, HidProfile.getHidClassDrawable(btClass), level, iconScale), context.getString(R.string.bluetooth_talkback_input_peripheral));
            }
            if (majorDeviceClass == 1536) {
                return new Pair(getBluetoothDrawable(context, R.drawable.ic_settings_print, level, iconScale), context.getString(R.string.bluetooth_talkback_imaging));
            }
        }
        for (LocalBluetoothProfile profile : cachedDevice.getProfiles()) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return new Pair(getBluetoothDrawable(context, resId, level, iconScale), null);
            }
        }
        if (btClass != null) {
            if (btClass.doesClassMatch(0)) {
                return new Pair(getBluetoothDrawable(context, R.drawable.ic_bt_headset_hfp, level, iconScale), context.getString(R.string.bluetooth_talkback_headset));
            }
            if (btClass.doesClassMatch(1)) {
                return new Pair(getBluetoothDrawable(context, R.drawable.ic_bt_headphones_a2dp, level, iconScale), context.getString(R.string.bluetooth_talkback_headphone));
            }
        }
        return new Pair(getBluetoothDrawable(context, R.drawable.ic_settings_bluetooth, level, iconScale), context.getString(R.string.bluetooth_talkback_bluetooth));
    }

    public static Drawable getBluetoothDrawable(Context context, @DrawableRes int resId, int batteryLevel, float iconScale) {
        if (batteryLevel != -1) {
            return BluetoothDeviceLayerDrawable.createLayerDrawable(context, resId, batteryLevel, iconScale);
        }
        return context.getDrawable(resId);
    }
}
