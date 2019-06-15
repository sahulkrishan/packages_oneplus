package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.UiThread;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class UsbDetailsController extends AbstractPreferenceController implements PreferenceControllerMixin {
    public static final boolean DATAROLE_OPEN = false;
    protected final Context mContext;
    protected final UsbDetailsFragment mFragment;
    @VisibleForTesting
    Handler mHandler;
    protected final UsbBackend mUsbBackend;

    @UiThread
    public abstract void refresh(boolean z, long j, int i, int i2);

    public UsbDetailsController(Context context, UsbDetailsFragment fragment, UsbBackend backend) {
        super(context);
        this.mContext = context;
        this.mFragment = fragment;
        this.mUsbBackend = backend;
        this.mHandler = new Handler(context.getMainLooper());
    }

    public boolean isAvailable() {
        return true;
    }
}
