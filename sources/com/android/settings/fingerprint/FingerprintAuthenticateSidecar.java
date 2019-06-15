package com.android.settings.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.CancellationSignal;
import com.android.settings.core.InstrumentedFragment;

public class FingerprintAuthenticateSidecar extends InstrumentedFragment {
    private static final String TAG = "FingerprintAuthenticateSidecar";
    private AuthenticationCallback mAuthenticationCallback = new AuthenticationCallback() {
        public void onAuthenticationSucceeded(AuthenticationResult result) {
            FingerprintAuthenticateSidecar.this.mCancellationSignal = null;
            if (FingerprintAuthenticateSidecar.this.mListener != null) {
                FingerprintAuthenticateSidecar.this.mListener.onAuthenticationSucceeded(result);
                return;
            }
            FingerprintAuthenticateSidecar.this.mAuthenticationResult = result;
            FingerprintAuthenticateSidecar.this.mAuthenticationError = null;
        }

        public void onAuthenticationFailed() {
            if (FingerprintAuthenticateSidecar.this.mListener != null) {
                FingerprintAuthenticateSidecar.this.mListener.onAuthenticationFailed();
            }
        }

        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            FingerprintAuthenticateSidecar.this.mCancellationSignal = null;
            if (FingerprintAuthenticateSidecar.this.mListener != null) {
                FingerprintAuthenticateSidecar.this.mListener.onAuthenticationError(errMsgId, errString);
                return;
            }
            FingerprintAuthenticateSidecar.this.mAuthenticationError = new AuthenticationError(errMsgId, errString);
            FingerprintAuthenticateSidecar.this.mAuthenticationResult = null;
        }

        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            if (FingerprintAuthenticateSidecar.this.mListener != null) {
                FingerprintAuthenticateSidecar.this.mListener.onAuthenticationHelp(helpMsgId, helpString);
            }
        }
    };
    private AuthenticationError mAuthenticationError;
    private AuthenticationResult mAuthenticationResult;
    private CancellationSignal mCancellationSignal;
    private FingerprintManager mFingerprintManager;
    private Listener mListener;

    private class AuthenticationError {
        int error;
        CharSequence errorString;

        public AuthenticationError(int errMsgId, CharSequence errString) {
            this.error = errMsgId;
            this.errorString = errString;
        }
    }

    public interface Listener {
        void onAuthenticationError(int i, CharSequence charSequence);

        void onAuthenticationFailed();

        void onAuthenticationHelp(int i, CharSequence charSequence);

        void onAuthenticationSucceeded(AuthenticationResult authenticationResult);
    }

    public int getMetricsCategory() {
        return 1221;
    }

    public void setFingerprintManager(FingerprintManager fingerprintManager) {
        this.mFingerprintManager = fingerprintManager;
    }

    public void startAuthentication(int userId) {
        this.mCancellationSignal = new CancellationSignal();
        this.mFingerprintManager.authenticate(null, this.mCancellationSignal, 0, this.mAuthenticationCallback, null, userId);
    }

    public void stopAuthentication() {
        if (!(this.mCancellationSignal == null || this.mCancellationSignal.isCanceled())) {
            this.mCancellationSignal.cancel();
        }
        this.mCancellationSignal = null;
    }

    public void setListener(Listener listener) {
        if (this.mListener == null && listener != null) {
            if (this.mAuthenticationResult != null) {
                listener.onAuthenticationSucceeded(this.mAuthenticationResult);
                this.mAuthenticationResult = null;
            }
            if (!(this.mAuthenticationError == null || this.mAuthenticationError.error == 5)) {
                listener.onAuthenticationError(this.mAuthenticationError.error, this.mAuthenticationError.errorString);
                this.mAuthenticationError = null;
            }
        }
        this.mListener = listener;
    }
}
