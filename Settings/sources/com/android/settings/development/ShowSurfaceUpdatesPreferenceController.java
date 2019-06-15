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

public class ShowSurfaceUpdatesPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final int SETTING_VALUE_OFF = 0;
    private static final int SETTING_VALUE_ON = 1;
    private static final String SHOW_SCREEN_UPDATES_KEY = "show_screen_updates";
    private static final String SURFACE_COMPOSER_INTERFACE_KEY = "android.ui.ISurfaceComposer";
    @VisibleForTesting
    static final int SURFACE_FLINGER_READ_CODE = 1010;
    @VisibleForTesting
    static final String SURFACE_FLINGER_SERVICE_KEY = "SurfaceFlinger";
    private static final int SURFACE_FLINGER_WRITE_SURFACE_UPDATES_CODE = 1002;
    private final IBinder mSurfaceFlinger = ServiceManager.getService(SURFACE_FLINGER_SERVICE_KEY);

    public ShowSurfaceUpdatesPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHOW_SCREEN_UPDATES_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeShowUpdatesSetting(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        updateShowUpdatesSetting();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SwitchPreference preference = this.mPreference;
        if (preference.isChecked()) {
            writeShowUpdatesSetting(false);
            preference.setChecked(false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateShowUpdatesSetting() {
        try {
            if (this.mSurfaceFlinger != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE_KEY);
                boolean z = false;
                this.mSurfaceFlinger.transact(1010, data, reply, 0);
                int showCpu = reply.readInt();
                int enableGL = reply.readInt();
                SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
                if (reply.readInt() != 0) {
                    z = true;
                }
                switchPreference.setChecked(z);
                reply.recycle();
                data.recycle();
            }
        } catch (RemoteException e) {
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void writeShowUpdatesSetting(boolean isEnabled) {
        try {
            if (this.mSurfaceFlinger != null) {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE_KEY);
                data.writeInt(isEnabled);
                this.mSurfaceFlinger.transact(1002, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException e) {
        }
        updateShowUpdatesSetting();
    }
}
