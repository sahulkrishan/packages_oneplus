package com.android.settings.bluetooth;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public abstract class BluetoothDetailsController extends AbstractPreferenceController implements PreferenceControllerMixin, Callback, LifecycleObserver, OnPause, OnResume {
    protected final CachedBluetoothDevice mCachedDevice;
    protected final Context mContext;
    protected final PreferenceFragment mFragment;

    public abstract void init(PreferenceScreen preferenceScreen);

    public abstract void refresh();

    public BluetoothDetailsController(Context context, PreferenceFragment fragment, CachedBluetoothDevice device, Lifecycle lifecycle) {
        super(context);
        this.mContext = context;
        this.mFragment = fragment;
        this.mCachedDevice = device;
        lifecycle.addObserver(this);
    }

    public void onPause() {
        this.mCachedDevice.unregisterCallback(this);
    }

    public void onResume() {
        this.mCachedDevice.registerCallback(this);
        refresh();
    }

    public boolean isAvailable() {
        return true;
    }

    public void onDeviceAttributesChanged() {
        refresh();
    }

    public final void displayPreference(PreferenceScreen screen) {
        init(screen);
        super.displayPreference(screen);
    }
}
