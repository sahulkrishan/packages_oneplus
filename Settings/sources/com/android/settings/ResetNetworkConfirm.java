package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.provider.Telephony.Sms;
import android.support.annotation.VisibleForTesting;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.ims.ImsManager;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.network.ApnSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class ResetNetworkConfirm extends InstrumentedFragment {
    private static final boolean DBG = true;
    private static final int RESET_COMPLETED = 1;
    private static final int START_RESET = 0;
    private static final String TAG = "ResetNetworkConfirm";
    private View mContentView;
    @VisibleForTesting
    boolean mEraseEsim;
    @VisibleForTesting
    EraseEsimAsyncTask mEraseEsimTask;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Utils.isMonkeyRunning()) {
                ResetNetworkConfirm.this.mHandler.sendEmptyMessage(0);
                new Thread(new Runnable() {
                    public void run() {
                        Context context = ResetNetworkConfirm.this.getActivity();
                        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                        if (connectivityManager != null) {
                            connectivityManager.factoryReset();
                        }
                        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
                        if (wifiManager != null) {
                            wifiManager.factoryReset();
                        }
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                        NetworkPolicyManager policyManager = (NetworkPolicyManager) context.getSystemService("netpolicy");
                        if (policyManager != null) {
                            policyManager.factoryReset(telephonyManager.getSubscriberId(ResetNetworkConfirm.this.mSubId));
                        }
                        ImsManager.getInstance(context, SubscriptionManager.getPhoneId(ResetNetworkConfirm.this.mSubId)).factoryReset();
                        if (telephonyManager != null) {
                            telephonyManager.factoryReset(ResetNetworkConfirm.this.mSubId);
                        }
                        ResetNetworkConfirm.this.restoreDefaultApn(context);
                        ResetNetworkConfirm.this.cleanUpSmsRawTable(context);
                        BluetoothManager btManager = (BluetoothManager) context.getSystemService("bluetooth");
                        if (btManager != null) {
                            BluetoothAdapter btAdapter = btManager.getAdapter();
                            if (btAdapter != null) {
                                btAdapter.factoryReset();
                                LocalBluetoothManager mLocalBtManager = LocalBluetoothManager.getInstance(context, null);
                                if (mLocalBtManager != null) {
                                    mLocalBtManager.getCachedDeviceManager().clearAllDevices();
                                }
                                ResetNetworkConfirm.this.mHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                }).start();
            }
        }
    };
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (ResetNetworkConfirm.this.mProgressDialog != null && ResetNetworkConfirm.this.isActivityValide() && ResetNetworkConfirm.this.getActivity().hasWindowFocus() && !ResetNetworkConfirm.this.mProgressDialog.isShowing()) {
                        ResetNetworkConfirm.this.mProgressDialog.show();
                        return;
                    }
                    return;
                case 1:
                    if (ResetNetworkConfirm.this.mProgressDialog != null && ResetNetworkConfirm.this.isActivityValide()) {
                        ResetNetworkConfirm.this.mProgressDialog.dismiss();
                    }
                    Activity activity = ResetNetworkConfirm.this.getActivity();
                    if (activity != null && !activity.isDestroyed()) {
                        Toast.makeText(activity, R.string.reset_network_complete_toast, 0).show();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private ProgressDialog mProgressDialog;
    private int mSubId = -1;

    private static class EraseEsimAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;
        private final String mPackageName;

        EraseEsimAsyncTask(Context context, String packageName) {
            this.mContext = context;
            this.mPackageName = packageName;
        }

        /* Access modifiers changed, original: protected|varargs */
        public Boolean doInBackground(Void... params) {
            return Boolean.valueOf(RecoverySystem.wipeEuiccData(this.mContext, this.mPackageName));
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Boolean succeeded) {
            if (succeeded.booleanValue()) {
                Toast.makeText(this.mContext, R.string.reset_network_complete_toast, 0).show();
            } else {
                new Builder(this.mContext).setTitle(R.string.reset_esim_error_title).setMessage(R.string.reset_esim_error_msg).setPositiveButton(17039370, null).show();
            }
        }
    }

    private boolean isActivityValide() {
        Activity activity = getActivity();
        return (activity == null || activity.isDestroyed()) ? false : true;
    }

    private void cleanUpSmsRawTable(Context context) {
        context.getContentResolver().delete(Uri.withAppendedPath(Sms.CONTENT_URI, "raw/permanentDelete"), null, null);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void esimFactoryReset(Context context, String packageName) {
        if (this.mEraseEsim) {
            this.mEraseEsimTask = new EraseEsimAsyncTask(context, packageName);
            this.mEraseEsimTask.execute(new Void[0]);
            return;
        }
        Toast.makeText(context, R.string.reset_network_complete_toast, 0).show();
    }

    private void restoreDefaultApn(Context context) {
        Uri uri = Uri.parse(ApnSettings.RESTORE_CARRIERS_URI);
        if (SubscriptionManager.isUsableSubIdValue(this.mSubId)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("subId/");
            stringBuilder.append(String.valueOf(this.mSubId));
            uri = Uri.withAppendedPath(uri, stringBuilder.toString());
        }
        context.getContentResolver().delete(uri, null, null);
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_reset_network).setOnClickListener(this.mFinalClickListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return inflater.inflate(R.layout.network_reset_disallowed_screen, null);
        }
        if (admin != null) {
            new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_network_reset", admin).setOnDismissListener(new -$$Lambda$ResetNetworkConfirm$YTG2-gTxf5vyFkKGLAaR8nzFOxo(this)).show();
            return new View(getContext());
        }
        this.mProgressDialog = new ProgressDialog(getActivity());
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setMessage(getResources().getString(R.string.oneplus_reset_network));
        this.mContentView = inflater.inflate(R.layout.reset_network_confirm, null);
        establishFinalConfirmationState();
        return this.mContentView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            this.mSubId = args.getInt("subscription", -1);
            this.mEraseEsim = args.getBoolean("erase_esim");
        }
    }

    public void onDestroy() {
        if (this.mEraseEsimTask != null) {
            this.mEraseEsimTask.cancel(true);
            this.mEraseEsimTask = null;
        }
        super.onDestroy();
    }

    public int getMetricsCategory() {
        return 84;
    }
}
