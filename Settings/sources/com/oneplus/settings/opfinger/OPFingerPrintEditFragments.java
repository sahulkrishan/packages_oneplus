package com.oneplus.settings.opfinger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.lib.widget.OPEditText;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPProgressDialog;
import com.oneplus.settings.utils.OPUtils;

public class OPFingerPrintEditFragments extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private static final int DELETED_MESSAGE = 2;
    private static final int DELETED_MESSAGE_DELAYED = 100;
    private static final int DELETED_MESSAGE_FAILED = 3;
    private static final int DELETING_MESSAGE = 1;
    public static final String FINGERPRINT_PARCELABLE = "fingerprint_parcelable";
    private static final String KEY_OPFINGER_DELETE = "opfingerprint_delete";
    private static final String KEY_OPFINGER_EDIT = "key_opfinger_edit";
    private static final String KEY_OPFINGER_RENAME = "opfingerprint_rename";
    private static final int MSG_REFRESH_FINGERPRINT_TEMPLATES = 5;
    private static final int SHOW_DELETE_DIALOG_MESSAGE = 7;
    private static final int SHOW_RENAME_DIALOG_MESSAGE = 4;
    private static final int SHOW_WARNING_DIALOG = 6;
    private static final String TAG = "OPFingerPrintEditFragments";
    private boolean isDeleteDialogShow;
    private boolean isRenameDialogShow;
    private boolean isWarnDialogShow;
    private AlertDialog mDeleteDialog;
    private Preference mDeltePreference;
    private Fingerprint mFingerprint;
    private FingerprintManager mFingerprintManager;
    private CharSequence mFingerprintName;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Activity activity = OPFingerPrintEditFragments.this.getActivity();
                    if (activity != null && !activity.isDestroyed()) {
                        OPFingerPrintEditFragments.this.mProgressDialog.setMessage(OPFingerPrintEditFragments.this.getResources().getString(R.string.oneplus_deleteing_fingerprint_list));
                        OPFingerPrintEditFragments.this.mProgressDialog.show();
                        break;
                    }
                    return;
                case 3:
                    OPFingerPrintEditFragments.this.mProgressDialog.dismiss();
                    Toast.makeText(SettingsBaseApplication.mApplication, R.string.oneplus_deleted_fingerprint_list_failed, 0).show();
                    break;
                case 4:
                    OPFingerPrintEditFragments.this.showRenameDialog();
                    break;
                case 5:
                    Toast.makeText(SettingsBaseApplication.mApplication, R.string.oneplus_deleted_fingerprint_list, 0).show();
                    if (OPFingerPrintEditFragments.this.mProgressDialog != null) {
                        OPFingerPrintEditFragments.this.mProgressDialog.dismiss();
                    }
                    OPFingerPrintEditFragments.this.finish();
                    break;
                case 6:
                    OPFingerPrintEditFragments.this.showWarnigDialog((Fingerprint) msg.obj);
                    break;
                case 7:
                    OPFingerPrintEditFragments.this.showDeleteDialog((Fingerprint) msg.obj);
                    break;
            }
        }
    };
    private OPFingerPrintEditCategory mOPFingerPrintEditViewCategory;
    private OPProgressDialog mProgressDialog;
    private RemovalCallback mRemoveCallback = new RemovalCallback() {
        public void onRemovalSucceeded(Fingerprint fingerprint, int remaining) {
            OPFingerPrintEditFragments.this.mHandler.obtainMessage(5, fingerprint.getFingerId(), 0).sendToTarget();
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
            Activity activity = OPFingerPrintEditFragments.this.getActivity();
            if (activity != null) {
                Toast.makeText(activity, errString, 0);
            }
        }
    };
    private AlertDialog mRenameDialog;
    private Preference mRenamePreference;
    private int mUserId;
    private AlertDialog mWarnDialog;
    private String renameData;
    private OPEditText renameEdit;

    public class RenameDialog extends DialogFragment {
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View renameView = LayoutInflater.from(getActivity()).inflate(R.layout.op_fingerprint_rename_dialog, null);
            final OPEditText renameEdit = (OPEditText) renameView.findViewById(R.id.opfinger_rename_ed);
            renameEdit.setHint(OPFingerPrintEditFragments.this.mFingerprintName);
            renameEdit.requestFocus();
            renameEdit.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s == " ") {
                        renameEdit.setText("");
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            });
            return new Builder(getActivity()).setTitle(R.string.user_rename).setView(renameView).setCancelable(true).setPositiveButton(R.string.okay, null).setNegativeButton(R.string.alert_dialog_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onCreate(Bundle icicle) {
        if (icicle != null) {
            this.isRenameDialogShow = icicle.getBoolean("renamedialog");
            this.isDeleteDialogShow = icicle.getBoolean("deletedialog");
            this.isWarnDialogShow = icicle.getBoolean("warndialog");
            this.renameData = icicle.getString("renamedata");
        }
        super.onCreate(icicle);
        this.mUserId = getActivity().getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        this.mFingerprint = (Fingerprint) getArguments().getParcelable(FINGERPRINT_PARCELABLE);
        if (icicle != null) {
            this.mFingerprintName = icicle.getCharSequence("fingerprint_name");
        } else if (this.mFingerprint != null) {
            this.mFingerprintName = this.mFingerprint.getName();
        }
        this.mProgressDialog = new OPProgressDialog(getActivity());
        this.mFingerprintManager = (FingerprintManager) getActivity().getSystemService("fingerprint");
        addPreferencesFromResource(R.xml.op_fingerprint_edit);
        initViews();
        if (this.isRenameDialogShow) {
            showRenameDialog();
        } else if (this.isWarnDialogShow) {
            showWarnigDialog(this.mFingerprint);
        } else if (this.isDeleteDialogShow) {
            showDeleteDialog(this.mFingerprint);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRenameDialog != null) {
            outState.putBoolean("renamedialog", this.mRenameDialog.isShowing());
            outState.putString("renamedata", this.renameEdit.getText().toString());
        }
        if (this.mWarnDialog != null) {
            outState.putBoolean("warndialog", this.mWarnDialog.isShowing());
        }
        if (this.mDeleteDialog != null) {
            outState.putBoolean("deletedialog", this.mDeleteDialog.isShowing());
        }
        outState.putCharSequence("fingerprint_name", this.mFingerprintName);
    }

    private void initViews() {
        this.mOPFingerPrintEditViewCategory = (OPFingerPrintEditCategory) findPreference(KEY_OPFINGER_EDIT);
        this.mRenamePreference = findPreference(KEY_OPFINGER_RENAME);
        this.mDeltePreference = findPreference(KEY_OPFINGER_DELETE);
        this.mOPFingerPrintEditViewCategory.setFingerprintName(this.mFingerprintName);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    private void renameFingerPrint(int fingerId, String newName) {
        this.mFingerprintManager.rename(fingerId, this.mUserId, newName);
    }

    private void deleteFingerPrint(Fingerprint fingerPrint) {
        this.mFingerprintManager.remove(fingerPrint, this.mUserId, this.mRemoveCallback);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (KEY_OPFINGER_RENAME.equals(key)) {
            this.mHandler.sendEmptyMessage(4);
        } else if (KEY_OPFINGER_DELETE.equals(key)) {
            new Thread(new Runnable() {
                public void run() {
                    if (OPFingerPrintEditFragments.this.mFingerprintManager != null) {
                        Message msg = OPFingerPrintEditFragments.this.mHandler.obtainMessage();
                        if (OPFingerPrintEditFragments.this.mFingerprintManager.getEnrolledFingerprints().size() == 1) {
                            msg.what = 6;
                        } else {
                            msg.what = 7;
                        }
                        msg.obj = OPFingerPrintEditFragments.this.mFingerprint;
                        OPFingerPrintEditFragments.this.mHandler.sendMessage(msg);
                        return;
                    }
                    OPFingerPrintEditFragments.this.mHandler.sendEmptyMessage(3);
                }
            }).start();
        }
        return true;
    }

    public void showWarnigDialog(final Fingerprint fingerprint) {
        this.mWarnDialog = new Builder(getActivity()).setTitle(R.string.fingerprint_last_delete_title).setMessage(R.string.fingerprint_last_delete_message).setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OPFingerPrintEditFragments.this.mHandler.sendEmptyMessage(1);
                OPFingerPrintEditFragments.this.deleteFingerPrint(fingerprint);
                OPFingerPrintEditFragments.this.mHandler.sendEmptyMessageDelayed(2, 100);
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        this.mWarnDialog.show();
    }

    public void showDeleteDialog(final Fingerprint fingerprint) {
        this.mDeleteDialog = new Builder(getActivity()).setTitle(R.string.security_settings_fingerprint_enroll_dialog_delete).setMessage(R.string.oneplus_fingerprint_delete_confirm_message).setPositiveButton(R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OPFingerPrintEditFragments.this.mHandler.sendEmptyMessage(1);
                OPFingerPrintEditFragments.this.deleteFingerPrint(fingerprint);
                OPFingerPrintEditFragments.this.mHandler.sendEmptyMessageDelayed(2, 100);
            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        this.mDeleteDialog.show();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }

    public void showRenameDialog() {
        View renameView = LayoutInflater.from(getActivity()).inflate(R.layout.op_fingerprint_rename_dialog, null);
        this.renameEdit = (OPEditText) renameView.findViewById(R.id.opfinger_rename_ed);
        this.renameEdit.setHint(this.mFingerprintName);
        this.renameEdit.requestFocus();
        this.renameEdit.setText(this.renameData);
        this.renameEdit.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mRenameDialog = new Builder(getActivity()).setTitle(R.string.user_rename).setView(renameView).setCancelable(true).setPositiveButton(R.string.okay, null).setNegativeButton(R.string.alert_dialog_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        this.mRenameDialog.show();
        this.mRenameDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String renameText = OPFingerPrintEditFragments.this.renameEdit.getText().toString().trim();
                if ("".equals(renameText)) {
                    OPFingerPrintEditFragments.this.mFingerprintName;
                    Toast.makeText(OPFingerPrintEditFragments.this.getActivity(), R.string.oneplus_opfinger_input_only_space, 0).show();
                    return;
                }
                OPFingerPrintEditFragments.this.mFingerprintName = renameText;
                OPFingerPrintEditFragments.this.renameFingerPrint(OPFingerPrintEditFragments.this.mFingerprint.getFingerId(), renameText);
                OPFingerPrintEditFragments.this.mFingerprintName = renameText;
                OPFingerPrintEditFragments.this.mOPFingerPrintEditViewCategory.setFingerprintName(renameText);
                OPFingerPrintEditFragments.this.mRenameDialog.dismiss();
            }
        });
    }
}
