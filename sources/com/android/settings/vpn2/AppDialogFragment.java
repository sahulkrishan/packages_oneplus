package com.android.settings.vpn2;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class AppDialogFragment extends InstrumentedDialogFragment implements com.android.settings.vpn2.AppDialog.Listener {
    private static final String ARG_CONNECTED = "connected";
    private static final String ARG_LABEL = "label";
    private static final String ARG_MANAGING = "managing";
    private static final String ARG_PACKAGE = "package";
    private static final String TAG = "AppDialogFragment";
    private static final String TAG_APP_DIALOG = "vpnappdialog";
    private Listener mListener;
    private PackageInfo mPackageInfo;
    private final IConnectivityManager mService = Stub.asInterface(ServiceManager.getService("connectivity"));
    private UserManager mUserManager;

    public interface Listener {
        void onCancel();

        void onForget();
    }

    public int getMetricsCategory() {
        return 546;
    }

    public static void show(Fragment parent, PackageInfo packageInfo, String label, boolean managing, boolean connected) {
        if (managing || connected) {
            show(parent, null, packageInfo, label, managing, connected);
        }
    }

    public static void show(Fragment parent, Listener listener, PackageInfo packageInfo, String label, boolean managing, boolean connected) {
        if (parent.isAdded()) {
            Bundle args = new Bundle();
            args.putParcelable("package", packageInfo);
            args.putString(ARG_LABEL, label);
            args.putBoolean(ARG_MANAGING, managing);
            args.putBoolean(ARG_CONNECTED, connected);
            AppDialogFragment frag = new AppDialogFragment();
            frag.mListener = listener;
            frag.setArguments(args);
            frag.setTargetFragment(parent, 0);
            frag.show(parent.getFragmentManager(), TAG_APP_DIALOG);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserManager = UserManager.get(getContext());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String label = args.getString(ARG_LABEL);
        boolean managing = args.getBoolean(ARG_MANAGING);
        boolean connected = args.getBoolean(ARG_CONNECTED);
        this.mPackageInfo = (PackageInfo) args.getParcelable("package");
        if (managing) {
            return new AppDialog(getActivity(), this, this.mPackageInfo, label);
        }
        Builder dlog = new Builder(getActivity()).setTitle(label).setMessage(getActivity().getString(R.string.vpn_disconnect_confirm)).setNegativeButton(getActivity().getString(R.string.vpn_cancel), null);
        if (connected && !isUiRestricted()) {
            dlog.setPositiveButton(getActivity().getString(R.string.vpn_disconnect), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AppDialogFragment.this.onDisconnect(dialog);
                }
            });
        }
        return dlog.create();
    }

    public void onCancel(DialogInterface dialog) {
        dismiss();
        if (this.mListener != null) {
            this.mListener.onCancel();
        }
        super.onCancel(dialog);
    }

    public void onForget(DialogInterface dialog) {
        if (!isUiRestricted()) {
            int userId = getUserId();
            try {
                this.mService.setVpnPackageAuthorization(this.mPackageInfo.packageName, userId, false);
                onDisconnect(dialog);
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to forget authorization of ");
                stringBuilder.append(this.mPackageInfo.packageName);
                stringBuilder.append(" for user ");
                stringBuilder.append(userId);
                Log.e(str, stringBuilder.toString(), e);
            }
            if (this.mListener != null) {
                this.mListener.onForget();
            }
        }
    }

    private void onDisconnect(DialogInterface dialog) {
        if (!isUiRestricted()) {
            int userId = getUserId();
            try {
                if (this.mPackageInfo.packageName.equals(VpnUtils.getConnectedPackage(this.mService, userId))) {
                    this.mService.setAlwaysOnVpnPackage(userId, null, false);
                    this.mService.prepareVpn(this.mPackageInfo.packageName, "[Legacy VPN]", userId);
                }
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to disconnect package ");
                stringBuilder.append(this.mPackageInfo.packageName);
                stringBuilder.append(" for user ");
                stringBuilder.append(userId);
                Log.e(str, stringBuilder.toString(), e);
            }
        }
    }

    private boolean isUiRestricted() {
        return this.mUserManager.hasUserRestriction("no_config_vpn", UserHandle.of(getUserId()));
    }

    private int getUserId() {
        return UserHandle.getUserId(this.mPackageInfo.applicationInfo.uid);
    }
}
