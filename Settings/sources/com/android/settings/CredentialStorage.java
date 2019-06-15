package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.support.v4.view.PointerIconCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.security.ConfigureKeyGuardDialog;
import com.android.settings.vpn2.VpnUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public final class CredentialStorage extends Activity {
    public static final String ACTION_INSTALL = "com.android.credentials.INSTALL";
    public static final String ACTION_RESET = "com.android.credentials.RESET";
    public static final String ACTION_UNLOCK = "com.android.credentials.UNLOCK";
    private static final int CONFIRM_CLEAR_SYSTEM_CREDENTIAL_REQUEST = 2;
    private static final int CONFIRM_KEY_GUARD_REQUEST = 1;
    public static final int MIN_PASSWORD_QUALITY = 65536;
    private static final String TAG = "CredentialStorage";
    private Bundle mInstallBundle;
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private int mRetriesRemaining = -1;

    /* renamed from: com.android.settings.CredentialStorage$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$security$KeyStore$State = new int[State.values().length];

        static {
            try {
                $SwitchMap$android$security$KeyStore$State[State.UNINITIALIZED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$security$KeyStore$State[State.LOCKED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$security$KeyStore$State[State.UNLOCKED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private class MarkKeyAsUserSelectable extends AsyncTask<Void, Void, Boolean> {
        final String mAlias;

        public MarkKeyAsUserSelectable(String alias) {
            this.mAlias = alias;
        }

        /* Access modifiers changed, original: protected|varargs */
        public Boolean doInBackground(Void... unused) {
            String str;
            StringBuilder stringBuilder;
            KeyChainConnection keyChainConnection;
            try {
                keyChainConnection = KeyChain.bind(CredentialStorage.this);
                keyChainConnection.getService().setUserSelectable(this.mAlias, true);
                Boolean valueOf = Boolean.valueOf(true);
                if (keyChainConnection != null) {
                    keyChainConnection.close();
                }
                return valueOf;
            } catch (RemoteException e) {
                str = CredentialStorage.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to mark key ");
                stringBuilder.append(this.mAlias);
                stringBuilder.append(" as user-selectable.");
                Log.w(str, stringBuilder.toString());
                return Boolean.valueOf(false);
            } catch (InterruptedException e2) {
                str = CredentialStorage.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to mark key ");
                stringBuilder.append(this.mAlias);
                stringBuilder.append(" as user-selectable.");
                Log.w(str, stringBuilder.toString());
                Thread.currentThread().interrupt();
                return Boolean.valueOf(false);
            } catch (Throwable th) {
                r2.addSuppressed(th);
            }
        }
    }

    private class ResetDialog implements OnClickListener, OnDismissListener {
        private boolean mResetConfirmed;

        /* synthetic */ ResetDialog(CredentialStorage x0, AnonymousClass1 x1) {
            this();
        }

        private ResetDialog() {
            AlertDialog dialog = new Builder(CredentialStorage.this).setTitle(17039380).setMessage(R.string.credentials_reset_hint).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            dialog.setOnDismissListener(this);
            dialog.show();
        }

        public void onClick(DialogInterface dialog, int button) {
            this.mResetConfirmed = button == -1;
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mResetConfirmed) {
                this.mResetConfirmed = false;
                if (CredentialStorage.this.confirmKeyGuard(2)) {
                    return;
                }
            }
            CredentialStorage.this.finish();
        }
    }

    private class ResetKeyStoreAndKeyChain extends AsyncTask<Void, Void, Boolean> {
        private ResetKeyStoreAndKeyChain() {
        }

        /* synthetic */ ResetKeyStoreAndKeyChain(CredentialStorage x0, AnonymousClass1 x1) {
            this();
        }

        /* Access modifiers changed, original: protected|varargs */
        public Boolean doInBackground(Void... unused) {
            new LockPatternUtils(CredentialStorage.this).resetKeyStore(UserHandle.myUserId());
            KeyChainConnection keyChainConnection;
            try {
                keyChainConnection = KeyChain.bind(CredentialStorage.this);
                try {
                    Boolean valueOf = Boolean.valueOf(keyChainConnection.getService().reset());
                    keyChainConnection.close();
                    return valueOf;
                } catch (RemoteException e) {
                    Boolean valueOf2 = Boolean.valueOf(false);
                    keyChainConnection.close();
                    return valueOf2;
                }
            } catch (InterruptedException e2) {
                Thread.currentThread().interrupt();
                return Boolean.valueOf(false);
            } catch (Throwable th) {
                keyChainConnection.close();
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Boolean success) {
            if (success.booleanValue()) {
                Toast.makeText(CredentialStorage.this, R.string.credentials_erased, 0).show();
                CredentialStorage.this.clearLegacyVpnIfEstablished();
            } else {
                Toast.makeText(CredentialStorage.this, R.string.credentials_not_erased, 0).show();
            }
            CredentialStorage.this.finish();
        }
    }

    private class UnlockDialog implements TextWatcher, OnClickListener, OnDismissListener {
        private final Button mButton;
        private final TextView mError;
        private final TextView mOldPassword;
        private boolean mUnlockConfirmed;

        /* synthetic */ UnlockDialog(CredentialStorage x0, AnonymousClass1 x1) {
            this();
        }

        private UnlockDialog() {
            CharSequence text;
            View view = View.inflate(CredentialStorage.this, R.layout.credentials_dialog, null);
            if (CredentialStorage.this.mRetriesRemaining == -1) {
                text = CredentialStorage.this.getResources().getText(R.string.credentials_unlock_hint);
            } else if (CredentialStorage.this.mRetriesRemaining > 3) {
                text = CredentialStorage.this.getResources().getText(R.string.credentials_wrong_password);
            } else if (CredentialStorage.this.mRetriesRemaining == 1) {
                text = CredentialStorage.this.getResources().getText(R.string.credentials_reset_warning);
            } else {
                text = CredentialStorage.this.getString(R.string.credentials_reset_warning_plural, new Object[]{Integer.valueOf(r7.mRetriesRemaining)});
            }
            ((TextView) view.findViewById(R.id.hint)).setText(text);
            this.mOldPassword = (TextView) view.findViewById(R.id.old_password);
            this.mOldPassword.setVisibility(0);
            this.mOldPassword.addTextChangedListener(this);
            this.mError = (TextView) view.findViewById(R.id.error);
            AlertDialog dialog = new Builder(CredentialStorage.this).setView(view).setTitle(R.string.credentials_unlock).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            dialog.setOnDismissListener(this);
            dialog.show();
            this.mButton = dialog.getButton(-1);
            this.mButton.setEnabled(false);
        }

        public void afterTextChanged(Editable editable) {
            Button button = this.mButton;
            boolean z = this.mOldPassword == null || this.mOldPassword.getText().length() > 0;
            button.setEnabled(z);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void onClick(DialogInterface dialog, int button) {
            this.mUnlockConfirmed = button == -1;
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mUnlockConfirmed) {
                this.mUnlockConfirmed = false;
                this.mError.setVisibility(0);
                CredentialStorage.this.mKeyStore.unlock(this.mOldPassword.getText().toString());
                int error = CredentialStorage.this.mKeyStore.getLastError();
                if (error == 1) {
                    CredentialStorage.this.mRetriesRemaining = -1;
                    Toast.makeText(CredentialStorage.this, R.string.credentials_enabled, 0).show();
                    CredentialStorage.this.ensureKeyGuard();
                } else if (error == 3) {
                    CredentialStorage.this.mRetriesRemaining = -1;
                    Toast.makeText(CredentialStorage.this, R.string.credentials_erased, 0).show();
                    CredentialStorage.this.handleUnlockOrInstall();
                } else if (error >= 10) {
                    CredentialStorage.this.mRetriesRemaining = (error - 10) + 1;
                    CredentialStorage.this.handleUnlockOrInstall();
                }
                return;
            }
            CredentialStorage.this.finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (((UserManager) getSystemService("user")).hasUserRestriction("no_config_credentials")) {
            if (ACTION_UNLOCK.equals(action) && this.mKeyStore.state() == State.UNINITIALIZED) {
                ensureKeyGuard();
            } else {
                finish();
            }
        } else if (ACTION_RESET.equals(action)) {
            ResetDialog resetDialog = new ResetDialog(this, null);
        } else {
            if (ACTION_INSTALL.equals(action) && checkCallerIsCertInstallerOrSelfInProfile()) {
                this.mInstallBundle = intent.getExtras();
            }
            handleUnlockOrInstall();
        }
    }

    private void handleUnlockOrInstall() {
        if (!isFinishing()) {
            switch (AnonymousClass1.$SwitchMap$android$security$KeyStore$State[this.mKeyStore.state().ordinal()]) {
                case 1:
                    ensureKeyGuard();
                    return;
                case 2:
                    UnlockDialog unlockDialog = new UnlockDialog(this, null);
                    return;
                case 3:
                    if (checkKeyGuardQuality()) {
                        installIfAvailable();
                        finish();
                        return;
                    }
                    new ConfigureKeyGuardDialog().show(getFragmentManager(), ConfigureKeyGuardDialog.TAG);
                    return;
                default:
                    return;
            }
        }
    }

    private void ensureKeyGuard() {
        if (!checkKeyGuardQuality()) {
            new ConfigureKeyGuardDialog().show(getFragmentManager(), ConfigureKeyGuardDialog.TAG);
        } else if (!confirmKeyGuard(1)) {
            finish();
        }
    }

    private boolean checkKeyGuardQuality() {
        return new LockPatternUtils(this).getActivePasswordQuality(UserManager.get(this).getCredentialOwnerProfile(UserHandle.myUserId())) >= 65536;
    }

    private boolean isHardwareBackedKey(byte[] keyData) {
        try {
            return KeyChain.isBoundKeyAlgorithm(new AlgorithmId(new ObjectIdentifier(PrivateKeyInfo.getInstance(new ASN1InputStream(new ByteArrayInputStream(keyData)).readObject()).getAlgorithmId().getAlgorithm().getId())).getName());
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse key data");
            return false;
        }
    }

    private void installIfAvailable() {
        if (this.mInstallBundle != null && !this.mInstallBundle.isEmpty()) {
            Bundle bundle = this.mInstallBundle;
            this.mInstallBundle = null;
            int uid = bundle.getInt("install_as_uid", -1);
            if (uid == -1 || UserHandle.isSameUser(uid, Process.myUid())) {
                String key;
                String str;
                StringBuilder stringBuilder;
                if (bundle.containsKey("user_private_key_name")) {
                    key = bundle.getString("user_private_key_name");
                    byte[] value = bundle.getByteArray("user_private_key_data");
                    int flags = 1;
                    if (uid == PointerIconCompat.TYPE_ALIAS && isHardwareBackedKey(value)) {
                        Log.d(TAG, "Saving private key with FLAG_NONE for WIFI_UID");
                        flags = 0;
                    }
                    if (!this.mKeyStore.importKey(key, value, uid, flags)) {
                        str = TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Failed to install ");
                        stringBuilder2.append(key);
                        stringBuilder2.append(" as uid ");
                        stringBuilder2.append(uid);
                        Log.e(str, stringBuilder2.toString());
                        return;
                    } else if (uid == 1000 || uid == -1) {
                        new MarkKeyAsUserSelectable(key.replaceFirst("^USRPKEY_", "")).execute(new Void[0]);
                    }
                }
                if (bundle.containsKey("user_certificate_name")) {
                    key = bundle.getString("user_certificate_name");
                    if (!this.mKeyStore.put(key, bundle.getByteArray("user_certificate_data"), uid, 0)) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to install ");
                        stringBuilder.append(key);
                        stringBuilder.append(" as uid ");
                        stringBuilder.append(uid);
                        Log.e(str, stringBuilder.toString());
                        return;
                    }
                }
                if (bundle.containsKey("ca_certificates_name")) {
                    key = bundle.getString("ca_certificates_name");
                    if (!this.mKeyStore.put(key, bundle.getByteArray("ca_certificates_data"), uid, 0)) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to install ");
                        stringBuilder.append(key);
                        stringBuilder.append(" as uid ");
                        stringBuilder.append(uid);
                        Log.e(str, stringBuilder.toString());
                        return;
                    }
                }
                sendBroadcast(new Intent("android.security.action.KEYCHAIN_CHANGED"));
                setResult(-1);
                return;
            }
            int dstUserId = UserHandle.getUserId(uid);
            int myUserId = UserHandle.myUserId();
            if (uid != PointerIconCompat.TYPE_ALIAS) {
                String str2 = TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Failed to install credentials as uid ");
                stringBuilder3.append(uid);
                stringBuilder3.append(": cross-user installs may only target wifi uids");
                Log.e(str2, stringBuilder3.toString());
                return;
            }
            startActivityAsUser(new Intent(ACTION_INSTALL).setFlags(33554432).putExtras(bundle), new UserHandle(dstUserId));
        }
    }

    private void clearLegacyVpnIfEstablished() {
        if (VpnUtils.disconnectLegacyVpn(getApplicationContext())) {
            Toast.makeText(this, R.string.vpn_disconnected, 0).show();
        }
    }

    private boolean checkCallerIsCertInstallerOrSelfInProfile() {
        boolean z = true;
        if (TextUtils.equals("com.android.certinstaller", getCallingPackage())) {
            if (getPackageManager().checkSignatures(getCallingPackage(), getPackageName()) != 0) {
                z = false;
            }
            return z;
        }
        try {
            int launchedFromUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
            if (launchedFromUid == -1) {
                Log.e(TAG, "com.android.credentials.INSTALL must be started with startActivityForResult");
                return false;
            } else if (!UserHandle.isSameApp(launchedFromUid, Process.myUid())) {
                return false;
            } else {
                UserInfo parentInfo = ((UserManager) getSystemService("user")).getProfileParent(UserHandle.getUserId(launchedFromUid));
                return parentInfo != null && parentInfo.id == UserHandle.myUserId();
            }
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean confirmKeyGuard(int requestCode) {
        return new ChooseLockSettingsHelper(this).launchConfirmationActivity(requestCode, getResources().getText(R.string.credentials_title), true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1) {
                String password = data.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                if (!TextUtils.isEmpty(password)) {
                    this.mKeyStore.unlock(password);
                    return;
                }
            }
            finish();
        } else if (requestCode == 2) {
            if (resultCode == -1) {
                new ResetKeyStoreAndKeyChain(this, null).execute(new Void[0]);
                return;
            }
            finish();
        }
    }
}
