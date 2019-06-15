package com.android.settings.development;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class HardwareOverlaysPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String DISABLE_OVERLAYS_KEY = "disable_overlays";
    private static final int SETTING_VALUE_OFF = 0;
    private static final int SETTING_VALUE_ON = 1;
    private static final String SURFACE_COMPOSER_INTERFACE_KEY = "android.ui.ISurfaceComposer";
    private static final int SURFACE_FLINGER_DISABLE_OVERLAYS_CODE = 1008;
    @VisibleForTesting
    static final int SURFACE_FLINGER_READ_CODE = 1010;
    private static final String SURFACE_FLINGER_SERVICE_KEY = "SurfaceFlinger";
    private final IBinder mSurfaceFlinger = ServiceManager.getService(SURFACE_FLINGER_SERVICE_KEY);

    public HardwareOverlaysPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return DISABLE_OVERLAYS_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeHardwareOverlaysSetting(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        updateHardwareOverlaysSetting();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SwitchPreference switchPreference = this.mPreference;
        if (switchPreference.isChecked()) {
            writeHardwareOverlaysSetting(false);
            switchPreference.setChecked(false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateHardwareOverlaysSetting() {
        if (this.mSurfaceFlinger != null) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE_KEY);
                boolean z = false;
                this.mSurfaceFlinger.transact(1010, data, reply, 0);
                int showCpu = reply.readInt();
                int enableGL = reply.readInt();
                int showUpdates = reply.readInt();
                int showBackground = reply.readInt();
                SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
                if (reply.readInt() != 0) {
                    z = true;
                }
                switchPreference.setChecked(z);
                reply.recycle();
                data.recycle();
            } catch (RemoteException e) {
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void writeHardwareOverlaysSetting(boolean isEnabled) {
        if (this.mSurfaceFlinger != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE_KEY);
                data.writeInt(isEnabled);
                this.mSurfaceFlinger.transact(1008, data, null, 0);
                data.recycle();
            } catch (RemoteException e) {
            }
            updateHardwareOverlaysSetting();
        }
    }
}
