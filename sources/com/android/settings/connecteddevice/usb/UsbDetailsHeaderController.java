package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.EntityHeaderController;

public class UsbDetailsHeaderController extends UsbDetailsController {
    private static final String KEY_DEVICE_HEADER = "usb_device_header";
    private EntityHeaderController mHeaderController;

    public UsbDetailsHeaderController(Context context, UsbDetailsFragment fragment, UsbBackend backend) {
        super(context, fragment, backend);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mHeaderController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, ((LayoutPreference) screen.findPreference(KEY_DEVICE_HEADER)).findViewById(R.id.entity_header));
    }

    /* Access modifiers changed, original: protected */
    public void refresh(boolean connected, long functions, int powerRole, int dataRole) {
        this.mHeaderController.setLabel(this.mContext.getString(R.string.usb_pref));
        this.mHeaderController.setIcon(this.mContext.getDrawable(R.drawable.ic_usb));
        this.mHeaderController.done(this.mFragment.getActivity(), true);
    }

    public String getPreferenceKey() {
        return KEY_DEVICE_HEADER;
    }
}
