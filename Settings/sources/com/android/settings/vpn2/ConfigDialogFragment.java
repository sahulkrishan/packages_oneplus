package com.android.settings.vpn2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.Credentials;
import android.security.KeyStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.vpn2.ConfirmLockdownFragment.ConfirmLockdownListener;

public class ConfigDialogFragment extends InstrumentedDialogFragment implements OnClickListener, OnShowListener, View.OnClickListener, ConfirmLockdownListener {
    private static final String ARG_EDITING = "editing";
    private static final String ARG_EXISTS = "exists";
    private static final String ARG_PROFILE = "profile";
    private static final String TAG = "ConfigDialogFragment";
    private static final String TAG_CONFIG_DIALOG = "vpnconfigdialog";
    private Context mContext;
    private final IConnectivityManager mService = Stub.asInterface(ServiceManager.getService("connectivity"));
    private boolean mUnlocking = false;

    public int getMetricsCategory() {
        return 545;
    }

    public static void show(VpnSettings parent, VpnProfile profile, boolean edit, boolean exists) {
        if (parent.isAdded()) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_PROFILE, profile);
            args.putBoolean(ARG_EDITING, edit);
            args.putBoolean(ARG_EXISTS, exists);
            ConfigDialogFragment frag = new ConfigDialogFragment();
            frag.setArguments(args);
            frag.setTargetFragment(parent, 0);
            frag.show(parent.getFragmentManager(), TAG_CONFIG_DIALOG);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void onResume() {
        super.onResume();
        if (KeyStore.getInstance().isUnlocked()) {
            this.mUnlocking = false;
            return;
        }
        if (this.mUnlocking) {
            dismiss();
        } else {
            Credentials.getInstance().unlock(this.mContext);
        }
        this.mUnlocking ^= 1;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        Dialog dialog = new ConfigDialog(getActivity(), this, (VpnProfile) args.getParcelable(ARG_PROFILE), args.getBoolean(ARG_EDITING), args.getBoolean(ARG_EXISTS));
        dialog.setOnShowListener(this);
        return dialog;
    }

    public void onShow(DialogInterface dialogInterface) {
        ((AlertDialog) getDialog()).getButton(-1).setOnClickListener(this);
    }

    public void onClick(View positiveButton) {
        onClick(getDialog(), -1);
    }

    public void onConfirmLockdown(Bundle options, boolean isAlwaysOn, boolean isLockdown) {
        connect((VpnProfile) options.getParcelable(ARG_PROFILE), isAlwaysOn);
        dismiss();
    }

    public void onClick(DialogInterface dialogInterface, int button) {
        ConfigDialog dialog = (ConfigDialog) getDialog();
        VpnProfile profile = dialog.getProfile();
        if (button == -1) {
            boolean shouldLockdown = dialog.isVpnAlwaysOn();
            boolean z = shouldLockdown || !dialog.isEditing();
            boolean shouldConnect = z;
            boolean wasLockdown = VpnUtils.isAnyLockdownActive(this.mContext);
            try {
                boolean replace = VpnUtils.isVpnActive(this.mContext);
                if (shouldConnect && !isConnected(profile) && ConfirmLockdownFragment.shouldShow(replace, wasLockdown, shouldLockdown)) {
                    Bundle opts = new Bundle();
                    opts.putParcelable(ARG_PROFILE, profile);
                    ConfirmLockdownFragment.show(this, replace, shouldLockdown, wasLockdown, shouldLockdown, opts);
                } else if (shouldConnect) {
                    connect(profile, shouldLockdown);
                } else {
                    save(profile, false);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to check active VPN state. Skipping.", e);
            }
        } else if (button == -3) {
            if (disconnect(profile)) {
                KeyStore keyStore = KeyStore.getInstance();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("VPN_");
                stringBuilder.append(profile.key);
                keyStore.delete(stringBuilder.toString(), -1);
                updateLockdownVpn(false, profile);
            } else {
                Log.e(TAG, "Failed to disconnect VPN. Leaving profile in keystore.");
                return;
            }
        }
        dismiss();
    }

    public void onCancel(DialogInterface dialog) {
        dismiss();
        super.onCancel(dialog);
    }

    private void updateLockdownVpn(boolean isVpnAlwaysOn, VpnProfile profile) {
        if (isVpnAlwaysOn) {
            if (profile.isValidLockdownProfile()) {
                ConnectivityManager.from(this.mContext).setAlwaysOnVpnPackageForUser(UserHandle.myUserId(), null, false);
                VpnUtils.setLockdownVpn(this.mContext, profile.key);
            } else {
                Toast.makeText(this.mContext, R.string.vpn_lockdown_config_error, 1).show();
            }
        } else if (VpnUtils.isVpnLockdown(profile.key)) {
            VpnUtils.clearLockdownVpn(this.mContext);
        }
    }

    private void save(VpnProfile profile, boolean lockdown) {
        KeyStore instance = KeyStore.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("VPN_");
        stringBuilder.append(profile.key);
        instance.put(stringBuilder.toString(), profile.encode(), -1, 0);
        disconnect(profile);
        updateLockdownVpn(lockdown, profile);
    }

    private void connect(VpnProfile profile, boolean lockdown) {
        save(profile, lockdown);
        if (!VpnUtils.isVpnLockdown(profile.key)) {
            VpnUtils.clearLockdownVpn(this.mContext);
            try {
                this.mService.startLegacyVpn(profile);
            } catch (IllegalStateException e) {
                Toast.makeText(this.mContext, R.string.vpn_no_network, 1).show();
            } catch (RemoteException e2) {
                Log.e(TAG, "Failed to connect", e2);
            }
        }
    }

    private boolean disconnect(VpnProfile profile) {
        try {
            if (isConnected(profile)) {
                return VpnUtils.disconnectLegacyVpn(getContext());
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disconnect", e);
            return false;
        }
    }

    private boolean isConnected(VpnProfile profile) throws RemoteException {
        LegacyVpnInfo connected = this.mService.getLegacyVpnInfo(UserHandle.myUserId());
        return connected != null && profile.key.equals(connected.key);
    }
}
