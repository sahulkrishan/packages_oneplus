package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.oneplus.settings.utils.OPUtils;

public class WifiDialog extends AlertDialog implements WifiConfigUiBase, OnClickListener {
    private static final int BUTTON_FORGET = -3;
    private static final int BUTTON_SUBMIT = -1;
    private static Context mContext;
    private final AccessPoint mAccessPoint;
    private WifiConfigController mController;
    private boolean mHideSubmitButton;
    private final WifiDialogListener mListener;
    private final int mMode;
    private View mView;

    public interface WifiDialogListener {
        void onForget(WifiDialog wifiDialog);

        void onSubmit(WifiDialog wifiDialog);
    }

    public static WifiDialog createFullscreen(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode) {
        mContext = context;
        return new WifiDialog(context, listener, accessPoint, mode, R.style.f955Theme.Settings.NoActionBar, false);
    }

    public static WifiDialog createModal(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode) {
        mContext = context;
        return new WifiDialog(context, listener, accessPoint, mode, R.style.WiFiDialog, mode == 0);
    }

    WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint, int mode, int style, boolean hideSubmitButton) {
        super(context, style);
        mContext = context;
        this.mMode = mode;
        this.mListener = listener;
        this.mAccessPoint = accessPoint;
        this.mHideSubmitButton = hideSubmitButton;
    }

    public WifiConfigController getController() {
        return this.mController;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setView(this.mView);
        setInverseBackgroundForced(true);
        this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mMode);
        super.onCreate(savedInstanceState);
        OPUtils.setLightNavigationBar(getWindow(), OPUtils.getThemeMode(mContext.getContentResolver()));
        if (this.mHideSubmitButton) {
            this.mController.hideSubmitButton();
        } else {
            this.mController.enableSubmitIfAppropriate();
        }
        if (this.mAccessPoint == null) {
            this.mController.hideForgetButton();
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mController.updatePassword();
    }

    public void dispatchSubmit() {
        if (this.mListener != null) {
            this.mListener.onSubmit(this);
        }
        dismiss();
    }

    public void onClick(DialogInterface dialogInterface, int id) {
        if (this.mListener != null) {
            if (id != -3) {
                if (id == -1) {
                    this.mListener.onSubmit(this);
                }
            } else if (WifiUtils.isNetworkLockedDown(getContext(), this.mAccessPoint.getConfig())) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtils.getDeviceOwner(getContext()));
            } else {
                this.mListener.onForget(this);
            }
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public Button getSubmitButton() {
        return getButton(-1);
    }

    public Button getForgetButton() {
        return getButton(-3);
    }

    public Button getCancelButton() {
        return getButton(-2);
    }

    public void setSubmitButton(CharSequence text) {
        setButton(-1, text, this);
    }

    public void setForgetButton(CharSequence text) {
        setButton(-3, text, this);
    }

    public void setCancelButton(CharSequence text) {
        setButton(-2, text, this);
    }
}
