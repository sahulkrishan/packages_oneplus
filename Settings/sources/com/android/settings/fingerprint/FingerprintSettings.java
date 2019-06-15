package com.android.settings.fingerprint;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SubSettings;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.fingerprint.FingerprintAuthenticateSidecar.Listener;
import com.android.settings.password.ChooseLockGeneric;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.utils.AnnotationSpan;
import com.android.settings.utils.AnnotationSpan.LinkInfo;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.TwoTargetPreference;
import com.android.settingslib.widget.FooterPreference;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.HashMap;
import java.util.List;

public class FingerprintSettings extends SubSettings {
    public static final String ANNOTATION_ADMIN_DETAILS = "admin_details";
    public static final String ANNOTATION_URL = "url";
    private static final long LOCKOUT_DURATION = 30000;
    protected static final int RESULT_FINISHED = 1;
    protected static final int RESULT_SKIP = 2;
    protected static final int RESULT_TIMEOUT = 3;
    private static final String TAG = "FingerprintSettings";

    public static class FingerprintPreference extends TwoTargetPreference {
        private View mDeleteView;
        private Fingerprint mFingerprint;
        private final OnDeleteClickListener mOnDeleteClickListener;
        private View mView;

        public interface OnDeleteClickListener {
            void onDeleteClick(FingerprintPreference fingerprintPreference);
        }

        public FingerprintPreference(Context context, OnDeleteClickListener onDeleteClickListener) {
            super(context);
            this.mOnDeleteClickListener = onDeleteClickListener;
        }

        public View getView() {
            return this.mView;
        }

        public void setFingerprint(Fingerprint item) {
            this.mFingerprint = item;
        }

        public Fingerprint getFingerprint() {
            return this.mFingerprint;
        }

        /* Access modifiers changed, original: protected */
        public int getSecondTargetResId() {
            return R.layout.preference_widget_delete;
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            this.mView = view.itemView;
            this.mDeleteView = view.itemView.findViewById(R.id.delete_button);
            this.mDeleteView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (FingerprintPreference.this.mOnDeleteClickListener != null) {
                        FingerprintPreference.this.mOnDeleteClickListener.onDeleteClick(FingerprintPreference.this);
                    }
                }
            });
        }
    }

    public static class FingerprintSettingsFragment extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnDeleteClickListener {
        private static final int ADD_FINGERPRINT_REQUEST = 10;
        private static final int CHOOSE_FINGERPRINT_ANIMATION = 11;
        private static final int CHOOSE_LOCK_GENERIC_REQUEST = 102;
        private static final int CONFIRM_REQUEST = 101;
        protected static final boolean DEBUG = true;
        private static final String KEY_CUSTOM_ANIMATION = "key_custom_animation";
        private static final String KEY_CUSTOM_TOGGLE_ONE = "key_custom_toggle_one";
        private static final String KEY_CUSTOM_TOGGLE_TWO = "key_custom_toggle_two";
        private static final String KEY_FINGERPRINT_ADD = "key_fingerprint_add";
        private static final String KEY_FINGERPRINT_ENABLE_KEYGUARD_TOGGLE = "fingerprint_enable_keyguard_toggle";
        private static final String KEY_FINGERPRINT_ITEM_PREFIX = "key_fingerprint_item";
        private static final String KEY_LAUNCHED_CONFIRM = "launched_confirm";
        private static final int MSG_FINGER_AUTH_ERROR = 1003;
        private static final int MSG_FINGER_AUTH_FAIL = 1002;
        private static final int MSG_FINGER_AUTH_HELP = 1004;
        private static final int MSG_FINGER_AUTH_SUCCESS = 1001;
        private static final int MSG_REFRESH_FINGERPRINT_TEMPLATES = 1000;
        private static final int RESET_HIGHLIGHT_DELAY_MS = 500;
        private static final String TAG = "FingerprintSettings";
        private static final String TAG_AUTHENTICATE_SIDECAR = "authenticate_sidecar";
        private static final String TAG_REMOVAL_SIDECAR = "removal_sidecar";
        private final String KEY_AOD_ENABLED = "prox_wake_enabled";
        private final String KEY_NOTIFICATION_WAKE_ENABLED = "notification_wake_enabled";
        Listener mAuthenticateListener = new Listener() {
            public void onAuthenticationSucceeded(AuthenticationResult result) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1001, result.getFingerprint().getFingerId(), 0).sendToTarget();
            }

            public void onAuthenticationFailed() {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1002).sendToTarget();
            }

            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1003, errMsgId, 0, errString).sendToTarget();
            }

            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1004, helpMsgId, 0, helpString).sendToTarget();
            }
        };
        private FingerprintAuthenticateSidecar mAuthenticateSidecar;
        private int mCurrentUser;
        private final Runnable mFingerprintLockoutReset = new Runnable() {
            public void run() {
                FingerprintSettingsFragment.this.mInFingerprintLockout = false;
                FingerprintSettingsFragment.this.retryFingerprint();
            }
        };
        private FingerprintManager mFingerprintManager;
        private HashMap<Integer, String> mFingerprintsRenaming;
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        if (FingerprintSettingsFragment.this.mFingerprintManager.getEnrolledFingerprints(FingerprintSettingsFragment.this.mUserId).size() == 0) {
                            OPGestureUtils.set0(SettingsBaseApplication.mApplication, 15);
                        }
                        FingerprintSettingsFragment.this.removeFingerprintPreference(msg.arg1);
                        FingerprintSettingsFragment.this.updateAddPreference();
                        FingerprintSettingsFragment.this.retryFingerprint();
                        return;
                    case 1001:
                        FingerprintSettingsFragment.this.highlightFingerprintItem(msg.arg1);
                        FingerprintSettingsFragment.this.retryFingerprint();
                        return;
                    case 1003:
                        FingerprintSettingsFragment.this.handleError(msg.arg1, (CharSequence) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
        private Drawable mHighlightDrawable;
        private boolean mInFingerprintLockout;
        private boolean mLaunchedConfirm;
        FingerprintRemoveSidecar.Listener mRemovalListener = new FingerprintRemoveSidecar.Listener() {
            public void onRemovalSucceeded(Fingerprint fingerprint) {
                FingerprintSettingsFragment.this.mHandler.obtainMessage(1000, fingerprint.getFingerId(), 0).sendToTarget();
                updateDialog();
            }

            public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                Activity activity = FingerprintSettingsFragment.this.getActivity();
                if (activity != null) {
                    Toast.makeText(activity, errString, 0);
                }
                updateDialog();
            }

            private void updateDialog() {
                RenameDialog renameDialog = (RenameDialog) FingerprintSettingsFragment.this.getFragmentManager().findFragmentByTag(RenameDialog.class.getName());
                if (renameDialog != null) {
                    renameDialog.enableDelete();
                }
            }
        };
        private FingerprintRemoveSidecar mRemovalSidecar;
        private byte[] mToken;
        private int mUserId;

        public static class ConfirmLastDeleteDialog extends InstrumentedDialogFragment {
            private Fingerprint mFp;

            public int getMetricsCategory() {
                return 571;
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int i;
                this.mFp = (Fingerprint) getArguments().getParcelable("fingerprint");
                boolean isProfileChallengeUser = getArguments().getBoolean("isProfileChallengeUser");
                Builder title = new Builder(getActivity()).setTitle(R.string.fingerprint_last_delete_title);
                if (isProfileChallengeUser) {
                    i = R.string.fingerprint_last_delete_message_profile_challenge;
                } else {
                    i = R.string.fingerprint_last_delete_message;
                }
                return title.setMessage(i).setPositiveButton(R.string.fingerprint_last_delete_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((FingerprintSettingsFragment) ConfirmLastDeleteDialog.this.getTargetFragment()).deleteFingerPrint(ConfirmLastDeleteDialog.this.mFp);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
            }
        }

        public static class DeleteFingerprintDialog extends InstrumentedDialogFragment implements DialogInterface.OnClickListener {
            private static final String KEY_FINGERPRINT = "fingerprint";
            private AlertDialog mAlertDialog;
            private Fingerprint mFp;

            public static DeleteFingerprintDialog newInstance(Fingerprint fp, FingerprintSettingsFragment target) {
                DeleteFingerprintDialog dialog = new DeleteFingerprintDialog();
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_FINGERPRINT, fp);
                dialog.setArguments(bundle);
                dialog.setTargetFragment(target, 0);
                return dialog;
            }

            public int getMetricsCategory() {
                return 570;
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                this.mFp = (Fingerprint) getArguments().getParcelable(KEY_FINGERPRINT);
                this.mAlertDialog = new Builder(getActivity()).setTitle(getString(R.string.fingerprint_delete_title, new Object[]{this.mFp.getName()})).setMessage(R.string.fingerprint_delete_message).setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_delete, this).setNegativeButton(R.string.cancel, null).create();
                return this.mAlertDialog;
            }

            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    int fingerprintId = this.mFp.getFingerId();
                    String str = FingerprintSettingsFragment.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Removing fpId=");
                    stringBuilder.append(fingerprintId);
                    Log.v(str, stringBuilder.toString());
                    this.mMetricsFeatureProvider.action(getContext(), 253, fingerprintId);
                    ((FingerprintSettingsFragment) getTargetFragment()).deleteFingerPrint(this.mFp);
                }
            }
        }

        public static class RenameDialog extends InstrumentedDialogFragment {
            private AlertDialog mAlertDialog;
            private boolean mDeleteInProgress;
            private EditText mDialogTextField;
            private String mFingerName;
            private Fingerprint mFp;
            private Boolean mTextHadFocus;
            private int mTextSelectionEnd;
            private int mTextSelectionStart;

            public void setDeleteInProgress(boolean deleteInProgress) {
                this.mDeleteInProgress = deleteInProgress;
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                this.mFp = (Fingerprint) getArguments().getParcelable("fingerprint");
                if (savedInstanceState != null) {
                    this.mFingerName = savedInstanceState.getString("fingerName");
                    this.mTextHadFocus = Boolean.valueOf(savedInstanceState.getBoolean("textHadFocus"));
                    this.mTextSelectionStart = savedInstanceState.getInt("startSelection");
                    this.mTextSelectionEnd = savedInstanceState.getInt("endSelection");
                }
                this.mAlertDialog = new Builder(getActivity()).setView(R.layout.fingerprint_rename_dialog).setTitle(R.string.security_settings_fingerprint_enroll_dialog_name_label).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok, null).create();
                this.mAlertDialog.setOnShowListener(new OnShowListener() {
                    public void onShow(DialogInterface dialog) {
                        RenameDialog.this.mDialogTextField = (EditText) RenameDialog.this.mAlertDialog.findViewById(R.id.fingerprint_rename_field);
                        RenameDialog.this.mDialogTextField.setText(RenameDialog.this.mFingerName == null ? RenameDialog.this.mFp.getName() : RenameDialog.this.mFingerName);
                        if (RenameDialog.this.mTextHadFocus == null) {
                            RenameDialog.this.mDialogTextField.selectAll();
                        } else {
                            RenameDialog.this.mDialogTextField.setSelection(RenameDialog.this.mTextSelectionStart, RenameDialog.this.mTextSelectionEnd);
                        }
                        if (RenameDialog.this.mDeleteInProgress) {
                            RenameDialog.this.mAlertDialog.getButton(-2).setEnabled(false);
                        }
                        RenameDialog.this.mDialogTextField.requestFocus();
                        RenameDialog.this.mAlertDialog.getButton(-1).setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                String newName = RenameDialog.this.mDialogTextField.getText().toString();
                                if (TextUtils.isEmpty(newName)) {
                                    Toast.makeText(RenameDialog.this.getActivity(), R.string.oneplus_opfinger_input_only_space, 0).show();
                                    return;
                                }
                                CharSequence name = RenameDialog.this.mFp.getName();
                                if (!TextUtils.equals(newName, name)) {
                                    String str = FingerprintSettingsFragment.TAG;
                                    StringBuilder stringBuilder = new StringBuilder();
                                    stringBuilder.append("rename ");
                                    stringBuilder.append(name);
                                    stringBuilder.append(" to ");
                                    stringBuilder.append(newName);
                                    Log.d(str, stringBuilder.toString());
                                    RenameDialog.this.mMetricsFeatureProvider.action(RenameDialog.this.getContext(), 254, RenameDialog.this.mFp.getFingerId());
                                    ((FingerprintSettingsFragment) RenameDialog.this.getTargetFragment()).renameFingerPrint(RenameDialog.this.mFp.getFingerId(), newName);
                                }
                                RenameDialog.this.mAlertDialog.dismiss();
                            }
                        });
                    }
                });
                if (this.mTextHadFocus == null || this.mTextHadFocus.booleanValue()) {
                    this.mAlertDialog.getWindow().setSoftInputMode(5);
                }
                return this.mAlertDialog;
            }

            public void enableDelete() {
                this.mDeleteInProgress = false;
                if (this.mAlertDialog != null) {
                    this.mAlertDialog.getButton(-2).setEnabled(true);
                }
            }

            public void onSaveInstanceState(Bundle outState) {
                super.onSaveInstanceState(outState);
                if (this.mDialogTextField != null) {
                    outState.putString("fingerName", this.mDialogTextField.getText().toString());
                    outState.putBoolean("textHadFocus", this.mDialogTextField.hasFocus());
                    outState.putInt("startSelection", this.mDialogTextField.getSelectionStart());
                    outState.putInt("endSelection", this.mDialogTextField.getSelectionEnd());
                }
            }

            public int getMetricsCategory() {
                return 570;
            }
        }

        /* Access modifiers changed, original: protected */
        public void handleError(int errMsgId, CharSequence msg) {
            if (errMsgId != 5) {
                if (errMsgId == 7) {
                    this.mInFingerprintLockout = true;
                    if (!this.mHandler.hasCallbacks(this.mFingerprintLockoutReset)) {
                        this.mHandler.postDelayed(this.mFingerprintLockoutReset, FingerprintSettings.LOCKOUT_DURATION);
                    }
                } else if (errMsgId == 9) {
                    this.mInFingerprintLockout = true;
                }
                if (this.mInFingerprintLockout) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, msg, 0).show();
                    }
                }
                retryFingerprint();
            }
        }

        /* JADX WARNING: Missing block: B:9:0x001f, code skipped:
            return;
     */
        private void retryFingerprint() {
            /*
            r2 = this;
            r0 = r2.mRemovalSidecar;
            r0 = r0.inProgress();
            if (r0 != 0) goto L_0x001f;
        L_0x0008:
            r0 = r2.mFingerprintManager;
            r1 = r2.mUserId;
            r0 = r0.getEnrolledFingerprints(r1);
            r0 = r0.size();
            if (r0 != 0) goto L_0x0017;
        L_0x0016:
            goto L_0x001f;
        L_0x0017:
            r0 = r2.mLaunchedConfirm;
            if (r0 == 0) goto L_0x001c;
        L_0x001b:
            return;
        L_0x001c:
            r0 = r2.mInFingerprintLockout;
            return;
        L_0x001f:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fingerprint.FingerprintSettings$FingerprintSettingsFragment.retryFingerprint():void");
        }

        public int getMetricsCategory() {
            return 49;
        }

        public void onCreate(Bundle savedInstanceState) {
            int i;
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            this.mCurrentUser = ActivityManager.getCurrentUser();
            this.mFingerprintManager = Utils.getFingerprintManagerOrNull(activity);
            this.mAuthenticateSidecar = (FingerprintAuthenticateSidecar) getFragmentManager().findFragmentByTag(TAG_AUTHENTICATE_SIDECAR);
            if (this.mAuthenticateSidecar == null) {
                this.mAuthenticateSidecar = new FingerprintAuthenticateSidecar();
                getFragmentManager().beginTransaction().add(this.mAuthenticateSidecar, TAG_AUTHENTICATE_SIDECAR).commit();
            }
            this.mAuthenticateSidecar.setFingerprintManager(this.mFingerprintManager);
            this.mRemovalSidecar = (FingerprintRemoveSidecar) getFragmentManager().findFragmentByTag(TAG_REMOVAL_SIDECAR);
            if (this.mRemovalSidecar == null) {
                this.mRemovalSidecar = new FingerprintRemoveSidecar();
                getFragmentManager().beginTransaction().add(this.mRemovalSidecar, TAG_REMOVAL_SIDECAR).commit();
            }
            this.mRemovalSidecar.setFingerprintManager(this.mFingerprintManager);
            this.mRemovalSidecar.setListener(this.mRemovalListener);
            RenameDialog renameDialog = (RenameDialog) getFragmentManager().findFragmentByTag(RenameDialog.class.getName());
            if (renameDialog != null) {
                renameDialog.setDeleteInProgress(this.mRemovalSidecar.inProgress());
            }
            this.mFingerprintsRenaming = new HashMap();
            this.mToken = getActivity().getIntent().getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
            if (savedInstanceState != null) {
                this.mFingerprintsRenaming = (HashMap) savedInstanceState.getSerializable("mFingerprintsRenaming");
                this.mToken = savedInstanceState.getByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                this.mLaunchedConfirm = savedInstanceState.getBoolean(KEY_LAUNCHED_CONFIRM, false);
            }
            this.mUserId = getActivity().getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
            if (this.mToken == null && !this.mLaunchedConfirm) {
                this.mLaunchedConfirm = true;
                launchChooseOrConfirmLock();
            }
            FooterPreference pref = this.mFooterPreferenceMixin.createFooterPreference();
            EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(activity, 32, this.mUserId);
            LinkInfo adminLinkInfo = new LinkInfo(FingerprintSettings.ANNOTATION_ADMIN_DETAILS, new -$$Lambda$FingerprintSettings$FingerprintSettingsFragment$yE_lJ-MtxexMYsEgD8_Zrh5Z2iY(activity, admin));
            LinkInfo linkInfo = new LinkInfo(activity, "url", HelpUtils.getHelpIntent(activity, getString(getHelpResource()), activity.getClass().getName()));
            if (admin != null) {
                i = R.string.security_settings_fingerprint_enroll_disclaimer_lockscreen_disabled;
            } else {
                i = R.string.security_settings_fingerprint_enroll_disclaimer;
            }
            pref.setTitle(AnnotationSpan.linkifyRemoveFingerprintUrl(getText(i), linkInfo, adminLinkInfo));
        }

        /* Access modifiers changed, original: protected */
        public void removeFingerprintPreference(int fingerprintId) {
            String name = genKey(fingerprintId);
            Preference prefToRemove = findPreference(name);
            String str;
            StringBuilder stringBuilder;
            if (prefToRemove == null) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Can't find preference to remove: ");
                stringBuilder.append(name);
                Log.w(str, stringBuilder.toString());
            } else if (!getPreferenceScreen().removePreference(prefToRemove)) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to remove preference with key ");
                stringBuilder.append(name);
                Log.w(str, stringBuilder.toString());
            }
        }

        private PreferenceScreen createPreferenceHierarchy() {
            PreferenceScreen root = getPreferenceScreen();
            if (root != null) {
                root.removeAll();
            }
            addPreferencesFromResource(R.xml.security_settings_fingerprint);
            root = getPreferenceScreen();
            addFingerprintItemPreferences(root);
            setPreferenceScreen(root);
            return root;
        }

        private void addFingerprintItemPreferences(PreferenceGroup root) {
            root.removeAll();
            List<Fingerprint> items = this.mFingerprintManager.getEnrolledFingerprints(this.mUserId);
            int fingerprintCount = items.size();
            for (int i = 0; i < fingerprintCount; i++) {
                Fingerprint item = (Fingerprint) items.get(i);
                FingerprintPreference pref = new FingerprintPreference(root.getContext(), this);
                pref.setKey(genKey(item.getFingerId()));
                pref.setTitle(item.getName());
                pref.setFingerprint(item);
                pref.setPersistent(false);
                pref.setIcon((int) R.drawable.ic_fingerprint_24dp);
                if (this.mRemovalSidecar.isRemovingFingerprint(item.getFingerId())) {
                    pref.setEnabled(false);
                }
                if (this.mFingerprintsRenaming.containsKey(Integer.valueOf(item.getFingerId()))) {
                    pref.setTitle((CharSequence) this.mFingerprintsRenaming.get(Integer.valueOf(item.getFingerId())));
                }
                root.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
            }
            Preference addPreference = new Preference(root.getContext());
            addPreference.setKey(KEY_FINGERPRINT_ADD);
            addPreference.setTitle((int) R.string.fingerprint_add_title);
            addPreference.setIcon((int) R.drawable.ic_menu_add);
            root.addPreference(addPreference);
            addPreference.setOnPreferenceChangeListener(this);
            updateAddPreference();
            if (OPUtils.isSupportCustomFingerprint()) {
                addCustomAnimationPickPage(root);
                addCustomToggleCategory(root);
            }
        }

        private void addCustomAnimationPickPage(PreferenceGroup root) {
            Preference animPreference = new Preference(root.getContext());
            animPreference.setKey(KEY_CUSTOM_ANIMATION);
            animPreference.setTitle((int) R.string.oneplus_fingerprint_animation_effect_title);
            animPreference.setSummary(getCustomAnimationName());
            animPreference.setOnPreferenceChangeListener(this);
            root.addPreference(animPreference);
        }

        private int getCustomAnimationName() {
            switch (System.getIntForUser(getContext().getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2)) {
                case 0:
                    return R.string.oneplus_select_fingerprint_animation_effect_1;
                case 1:
                    return R.string.oneplus_select_fingerprint_animation_effect_2;
                case 2:
                    return R.string.oneplus_select_fingerprint_animation_effect_3;
                case 3:
                    return R.string.op_theme_3_title;
                default:
                    return R.string.oneplus_select_fingerprint_animation_effect_1;
            }
        }

        private void addCustomToggleCategory(PreferenceGroup root) {
            PreferenceCategory customToggleCategory = new PreferenceCategory(root.getContext());
            customToggleCategory.setTitle((int) R.string.oneplus_security_settings_fingerprint_toggle_category_title);
            SwitchPreference customToggleOne = new SwitchPreference(root.getContext());
            customToggleOne.setKey(KEY_CUSTOM_TOGGLE_ONE);
            customToggleOne.setTitle((int) R.string.oneplus_hand_up_to_show);
            boolean z = true;
            customToggleOne.setChecked(1 == System.getIntForUser(getContext().getContentResolver(), "prox_wake_enabled", 0, this.mCurrentUser));
            customToggleOne.setOnPreferenceChangeListener(this);
            SwitchPreference customToggleTwo = new SwitchPreference(root.getContext());
            customToggleTwo.setKey(KEY_CUSTOM_TOGGLE_TWO);
            customToggleTwo.setTitle((int) R.string.oneplus_security_settings_fingerprint_toggle_two_title);
            if (OPGestureUtils.get(System.getInt(getContext().getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0), 11) != 1) {
                z = false;
            }
            customToggleTwo.setChecked(z);
            customToggleTwo.setOnPreferenceChangeListener(this);
            root.addPreference(customToggleCategory);
            root.addPreference(customToggleOne);
            root.addPreference(customToggleTwo);
        }

        private boolean needDisableDoze() {
            boolean singleTap = OPGestureUtils.get(System.getInt(getContext().getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0), 11) == 1;
            boolean aodEnabled = 1 == System.getIntForUser(getContext().getContentResolver(), "prox_wake_enabled", 0, this.mCurrentUser);
            boolean notificationWakeEnabled = 1 == Secure.getIntForUser(getContext().getContentResolver(), "notification_wake_enabled", 0, this.mCurrentUser);
            if (singleTap || aodEnabled || notificationWakeEnabled) {
                return false;
            }
            return true;
        }

        private void updateAddPreference() {
            if (getActivity() != null) {
                boolean z = false;
                boolean tooMany = this.mFingerprintManager.getEnrolledFingerprints(this.mUserId).size() >= getContext().getResources().getInteger(17694790);
                boolean removalInProgress = this.mRemovalSidecar.inProgress();
                CharSequence maxSummary = tooMany ? getContext().getString(R.string.fingerprint_add_max, new Object[]{Integer.valueOf(max)}) : "";
                Preference addPreference = findPreference(KEY_FINGERPRINT_ADD);
                addPreference.setSummary(maxSummary);
                if (!(tooMany || removalInProgress)) {
                    z = true;
                }
                addPreference.setEnabled(z);
            }
        }

        private static String genKey(int id) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("key_fingerprint_item_");
            stringBuilder.append(id);
            return stringBuilder.toString();
        }

        public void onResume() {
            super.onResume();
            this.mInFingerprintLockout = false;
            updatePreferences();
            if (this.mRemovalSidecar != null) {
                this.mRemovalSidecar.setListener(this.mRemovalListener);
            }
        }

        private void updatePreferences() {
            createPreferenceHierarchy();
            retryFingerprint();
        }

        public void onPause() {
            super.onPause();
            if (this.mRemovalSidecar != null) {
                this.mRemovalSidecar.setListener(null);
            }
            FingerprintAuthenticateSidecar fingerprintAuthenticateSidecar = this.mAuthenticateSidecar;
        }

        public void onSaveInstanceState(Bundle outState) {
            outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
            outState.putBoolean(KEY_LAUNCHED_CONFIRM, this.mLaunchedConfirm);
            outState.putSerializable("mFingerprintsRenaming", this.mFingerprintsRenaming);
        }

        public boolean onPreferenceTreeClick(Preference pref) {
            String key = pref.getKey();
            Intent intent;
            if (KEY_FINGERPRINT_ADD.equals(key)) {
                if (OPUtils.isSupportCustomFingerprint()) {
                    launchFindSensor(this.mToken);
                } else {
                    intent = new Intent();
                    intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
                    intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                    intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
                    startActivityForResult(intent, 10);
                    getActivity().overridePendingTransition(R.anim.op_activity_fingeprint_open_enter, R.anim.op_activity_fingeprint_close_exit);
                }
            } else if (KEY_CUSTOM_ANIMATION.equals(key)) {
                intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$OPCustomFingerprintAnimSettingsActivity");
                startActivityForResult(intent, 11);
            } else if (pref instanceof FingerprintPreference) {
                showRenameDialog(((FingerprintPreference) pref).getFingerprint());
            }
            return super.onPreferenceTreeClick(pref);
        }

        private void launchFindSensor(byte[] token) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", FingerprintEnrollFindSensor.class.getName());
            if (token != null) {
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
            }
            if (this.mUserId != -10000) {
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
            }
            intent.putExtra("needJumpToFingerprintSettings", false);
            getActivity().startActivityForResult(intent, 111);
        }

        public void onDeleteClick(FingerprintPreference p) {
            boolean z = true;
            if (this.mFingerprintManager.getEnrolledFingerprints(this.mUserId).size() <= 1) {
                z = false;
            }
            boolean hasMultipleFingerprint = z;
            Fingerprint fp = p.getFingerprint();
            if (!hasMultipleFingerprint) {
                ConfirmLastDeleteDialog lastDeleteDialog = new ConfirmLastDeleteDialog();
                boolean isProfileChallengeUser = UserManager.get(getContext()).isManagedProfile(this.mUserId);
                Bundle args = new Bundle();
                args.putParcelable("fingerprint", fp);
                args.putBoolean("isProfileChallengeUser", isProfileChallengeUser);
                lastDeleteDialog.setArguments(args);
                lastDeleteDialog.setTargetFragment(this, 0);
                lastDeleteDialog.show(getFragmentManager(), ConfirmLastDeleteDialog.class.getName());
            } else if (this.mRemovalSidecar.inProgress()) {
                Log.d(TAG, "Fingerprint delete in progress, skipping");
            } else {
                DeleteFingerprintDialog.newInstance(fp, this).show(getFragmentManager(), DeleteFingerprintDialog.class.getName());
            }
        }

        private void showRenameDialog(Fingerprint fp) {
            RenameDialog renameDialog = new RenameDialog();
            Bundle args = new Bundle();
            if (this.mFingerprintsRenaming.containsKey(Integer.valueOf(fp.getFingerId()))) {
                args.putParcelable("fingerprint", new Fingerprint((CharSequence) this.mFingerprintsRenaming.get(Integer.valueOf(fp.getFingerId())), fp.getGroupId(), fp.getFingerId(), fp.getDeviceId()));
            } else {
                args.putParcelable("fingerprint", fp);
            }
            renameDialog.setDeleteInProgress(this.mRemovalSidecar.inProgress());
            renameDialog.setArguments(args);
            renameDialog.setTargetFragment(this, 0);
            renameDialog.show(getFragmentManager(), RenameDialog.class.getName());
        }

        public boolean onPreferenceChange(Preference preference, Object objValue) {
            String key = preference.getKey();
            if (!KEY_FINGERPRINT_ENABLE_KEYGUARD_TOGGLE.equals(key)) {
                int value;
                String str;
                StringBuilder stringBuilder;
                if (KEY_CUSTOM_TOGGLE_ONE.equals(key)) {
                    value = ((Boolean) objValue).booleanValue();
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("prox_wake_enabled:");
                    stringBuilder.append(value);
                    Log.d(str, stringBuilder.toString());
                    OPUtils.sendAppTracker(OPConstants.PICK_UP_PHONE_SHOW, value);
                    if (value != 0) {
                        System.putIntForUser(getContext().getContentResolver(), "prox_wake_enabled", 1, this.mCurrentUser);
                        Secure.putIntForUser(getContext().getContentResolver(), "doze_enabled", 1, this.mCurrentUser);
                        if (OPUtils.isSupportCustomFingerprint()) {
                            OPGestureUtils.set1(getContext(), 15);
                        }
                    } else {
                        System.putIntForUser(getContext().getContentResolver(), "prox_wake_enabled", 0, this.mCurrentUser);
                        if (needDisableDoze()) {
                            Secure.putIntForUser(getContext().getContentResolver(), "doze_enabled", 0, this.mCurrentUser);
                            if (OPUtils.isSupportCustomFingerprint()) {
                                OPGestureUtils.set0(getContext(), 15);
                            }
                        }
                    }
                } else if (KEY_CUSTOM_TOGGLE_TWO.equals(key)) {
                    value = ((Boolean) objValue).booleanValue();
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("single_tab_enabled:");
                    stringBuilder.append(value);
                    Log.d(str, stringBuilder.toString());
                    OPUtils.sendAppTracker(OPConstants.TAP_SCREEN_SHOW, value);
                    if (value != 0) {
                        if (OPGestureUtils.get(System.getInt(getActivity().getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0), 7) == 1) {
                            Toast.makeText(getContext(), R.string.oneplus_security_settings_fingerprint_toggle_two_toast_2, 0).show();
                            OPGestureUtils.set0(getContext(), 7);
                        }
                        OPGestureUtils.set1(getContext(), 11);
                        Secure.putIntForUser(getContext().getContentResolver(), "doze_enabled", 1, this.mCurrentUser);
                    } else {
                        OPGestureUtils.set0(getContext(), 11);
                        if (needDisableDoze()) {
                            Secure.putIntForUser(getContext().getContentResolver(), "doze_enabled", 0, this.mCurrentUser);
                            if (OPUtils.isSupportCustomFingerprint()) {
                                OPGestureUtils.set0(getContext(), 15);
                            }
                        }
                    }
                } else {
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Unknown key:");
                    stringBuilder2.append(key);
                    Log.v(str2, stringBuilder2.toString());
                }
            }
            return true;
        }

        public int getHelpResource() {
            return R.string.help_url_fingerprint;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 102 || requestCode == 101) {
                this.mLaunchedConfirm = false;
                if ((resultCode == 1 || resultCode == -1) && data != null) {
                    this.mToken = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                }
            } else if (requestCode == 10 && resultCode == 3) {
                Activity activity = getActivity();
                activity.setResult(3);
                activity.finish();
            }
            if (this.mToken == null) {
                getActivity().finish();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            if (getActivity().isFinishing()) {
                int result = this.mFingerprintManager.postEnroll();
                if (result < 0) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("postEnroll failed: result = ");
                    stringBuilder.append(result);
                    Log.w(str, stringBuilder.toString());
                }
            }
        }

        private Drawable getHighlightDrawable() {
            if (this.mHighlightDrawable == null) {
                Activity activity = getActivity();
                if (activity != null) {
                    this.mHighlightDrawable = activity.getDrawable(R.drawable.preference_highlight);
                }
            }
            return this.mHighlightDrawable;
        }

        private void highlightFingerprintItem(int fpId) {
            FingerprintPreference fpref = (FingerprintPreference) findPreference(genKey(fpId));
            Drawable highlight = getHighlightDrawable();
            if (highlight != null && fpref != null) {
                final View view = fpref.getView();
                highlight.setHotspot((float) (view.getWidth() / 2), (float) (view.getHeight() / 2));
                view.setBackground(highlight);
                view.setPressed(true);
                view.setPressed(false);
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        view.setBackground(null);
                    }
                }, 500);
            }
        }

        private void launchChooseOrConfirmLock() {
            Intent intent = new Intent();
            long challenge = this.mFingerprintManager.preEnroll();
            if (!new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(101, getString(R.string.security_settings_fingerprint_preference_title), null, null, challenge, this.mUserId)) {
                intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
                intent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
                intent.putExtra(ChooseLockGenericFragment.HIDE_DISABLED_PREFS, true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
                intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
                startActivityForResult(intent, 102);
            }
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public void deleteFingerPrint(Fingerprint fingerPrint) {
            this.mRemovalSidecar.startRemove(fingerPrint, this.mUserId);
            findPreference(genKey(fingerPrint.getFingerId())).setEnabled(false);
            updateAddPreference();
        }

        private void renameFingerPrint(int fingerId, String newName) {
            this.mFingerprintManager.rename(fingerId, this.mUserId, newName);
            if (!TextUtils.isEmpty(newName)) {
                this.mFingerprintsRenaming.put(Integer.valueOf(fingerId), newName);
            }
            updatePreferences();
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, FingerprintSettingsFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (FingerprintSettingsFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.security_settings_fingerprint_preference_title));
    }
}
