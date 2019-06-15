package com.android.settings;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.settings.OPRebootWipeUserdata;
import com.oneplus.settings.utils.OPUtils;

public class MasterClearConfirm extends InstrumentedFragment {
    private View mContentView;
    private boolean mEraseEsims;
    private boolean mEraseSdCard;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Utils.isMonkeyRunning()) {
                final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager) MasterClearConfirm.this.getActivity().getSystemService("persistent_data_block");
                OemLockManager oemLockManager = (OemLockManager) MasterClearConfirm.this.getActivity().getSystemService("oem_lock");
                if (pdbManager == null || oemLockManager.isOemUnlockAllowed() || !Utils.isDeviceProvisioned(MasterClearConfirm.this.getActivity())) {
                    MasterClearConfirm.this.doMasterClear();
                } else {
                    new AsyncTask<Void, Void, Void>() {
                        int mOldOrientation;
                        ProgressDialog mProgressDialog;

                        /* Access modifiers changed, original: protected|varargs */
                        public Void doInBackground(Void... params) {
                            pdbManager.wipe();
                            return null;
                        }

                        /* Access modifiers changed, original: protected */
                        public void onPostExecute(Void aVoid) {
                            this.mProgressDialog.hide();
                            if (MasterClearConfirm.this.getActivity() != null) {
                                MasterClearConfirm.this.getActivity().setRequestedOrientation(this.mOldOrientation);
                                MasterClearConfirm.this.doMasterClear();
                            }
                        }

                        /* Access modifiers changed, original: protected */
                        public void onPreExecute() {
                            this.mProgressDialog = AnonymousClass1.this.getProgressDialog();
                            this.mProgressDialog.show();
                            this.mOldOrientation = MasterClearConfirm.this.getActivity().getRequestedOrientation();
                            MasterClearConfirm.this.getActivity().setRequestedOrientation(14);
                        }
                    }.execute(new Void[0]);
                }
            }
        }

        private ProgressDialog getProgressDialog() {
            ProgressDialog progressDialog = new ProgressDialog(MasterClearConfirm.this.getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(MasterClearConfirm.this.getActivity().getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(MasterClearConfirm.this.getActivity().getString(R.string.master_clear_progress_text));
            return progressDialog;
        }
    };
    private String mPowerOnPsw;

    private void doMasterClear() {
        try {
            if (this.mEraseSdCard) {
                if (OPUtils.isSurportNoNeedPowerOnPassword(getActivity())) {
                    OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "MasterClearConfirm", "--wipe_data", this.mPowerOnPsw);
                } else if (checkIfNeedPasswordToPowerOn()) {
                    OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "MasterClearConfirm", "--wipe_data", this.mPowerOnPsw);
                } else {
                    OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "MasterClearConfirm", "--wipe_data", "");
                }
            } else if (OPUtils.isSurportNoNeedPowerOnPassword(getActivity())) {
                OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "OPMasterClearConfirm", "--delete_data", this.mPowerOnPsw);
            } else if (checkIfNeedPasswordToPowerOn()) {
                OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "OPMasterClearConfirm", "--delete_data", this.mPowerOnPsw);
            } else {
                OPRebootWipeUserdata.rebootWipeUserData(getActivity(), false, "OPMasterClearConfirm", "--delete_data", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MasterClearConfim", "bootCommand Reboot failed (no permissions?)");
        }
    }

    public boolean checkIfNeedPasswordToPowerOn() {
        return Global.getInt(getActivity().getContentResolver(), "require_password_to_decrypt", 0) == 1;
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(R.id.execute_master_clear).setOnClickListener(this.mFinalClickListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_factory_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_factory_reset", UserHandle.myUserId())) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        }
        if (admin != null) {
            new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_factory_reset", admin).setOnDismissListener(new -$$Lambda$MasterClearConfirm$weRgiuD2TQnm7jx9NX_-qHWwsHU(this)).show();
            return new View(getActivity());
        }
        this.mContentView = inflater.inflate(R.layout.master_clear_confirm, null);
        establishFinalConfirmationState();
        setAccessibilityTitle();
        return this.mContentView;
    }

    private void setAccessibilityTitle() {
        CharSequence currentTitle = getActivity().getTitle();
        TextView confirmationMessage = (TextView) this.mContentView.findViewById(R.id.master_clear_confirm);
        if (confirmationMessage != null) {
            String accessibleText = new StringBuilder(currentTitle);
            accessibleText.append(",");
            accessibleText.append(confirmationMessage.getText());
            getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, accessibleText.toString()));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        boolean z = false;
        boolean z2 = args != null && args.getBoolean("erase_sd");
        this.mEraseSdCard = z2;
        if (args != null && args.getBoolean("erase_esim")) {
            z = true;
        }
        this.mEraseEsims = z;
        if (args != null) {
            this.mPowerOnPsw = args.getString("power_on_psw");
        }
    }

    public int getMetricsCategory() {
        return 67;
    }
}
