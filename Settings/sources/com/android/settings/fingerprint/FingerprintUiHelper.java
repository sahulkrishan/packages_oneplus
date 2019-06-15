package com.android.settings.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.oneplus.settings.utils.OPUtils;

public class FingerprintUiHelper extends AuthenticationCallback {
    private static final long ERROR_TIMEOUT = 1300;
    private Callback mCallback;
    private CancellationSignal mCancellationSignal;
    private TextView mErrorTextView;
    private FingerprintManager mFingerprintManager;
    private ImageView mIcon;
    private Runnable mResetErrorTextRunnable = new Runnable() {
        public void run() {
            FingerprintUiHelper.this.mErrorTextView.setText("");
            FingerprintUiHelper.this.mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };
    private int mUserId;

    public interface Callback {
        void onAuthenticated();

        void onFingerprintIconVisibilityChanged(boolean z);
    }

    public FingerprintUiHelper(ImageView icon, TextView errorTextView, Callback callback, int userId) {
        this.mFingerprintManager = Utils.getFingerprintManagerOrNull(icon.getContext());
        this.mIcon = icon;
        this.mErrorTextView = errorTextView;
        this.mCallback = callback;
        this.mUserId = userId;
    }

    public void startListening() {
        if (this.mFingerprintManager != null && this.mFingerprintManager.isHardwareDetected() && this.mFingerprintManager.getEnrolledFingerprints(this.mUserId).size() > 0) {
            this.mCancellationSignal = new CancellationSignal();
            this.mFingerprintManager.setActiveUser(this.mUserId);
            this.mFingerprintManager.authenticate(null, this.mCancellationSignal, 0, this, null, this.mUserId);
            setFingerprintIconVisibility(true);
            this.mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    }

    public void stopListening() {
        if (this.mCancellationSignal != null) {
            this.mCancellationSignal.cancel();
            this.mCancellationSignal = null;
        }
    }

    public boolean isListening() {
        return (this.mCancellationSignal == null || this.mCancellationSignal.isCanceled()) ? false : true;
    }

    private void setFingerprintIconVisibility(boolean visible) {
        int i = 8;
        if (OPUtils.isSupportCustomFingerprint()) {
            this.mIcon.setVisibility(8);
        } else {
            ImageView imageView = this.mIcon;
            if (visible) {
                i = 0;
            }
            imageView.setVisibility(i);
        }
        this.mCallback.onFingerprintIconVisibilityChanged(visible);
    }

    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (errMsgId != 5) {
            showError(errString);
            setFingerprintIconVisibility(false);
        }
    }

    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    public void onAuthenticationFailed() {
        showError(this.mIcon.getResources().getString(R.string.fingerprint_not_recognized));
    }

    public void onAuthenticationSucceeded(AuthenticationResult result) {
        this.mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        this.mCallback.onAuthenticated();
    }

    private void showError(CharSequence error) {
        if (isListening()) {
            this.mIcon.setImageResource(R.drawable.ic_fingerprint_error);
            this.mErrorTextView.setText(error);
            this.mErrorTextView.removeCallbacks(this.mResetErrorTextRunnable);
            this.mErrorTextView.postDelayed(this.mResetErrorTextRunnable, ERROR_TIMEOUT);
        }
    }
}
