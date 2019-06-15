package com.android.settings.fingerprint;

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.core.InstrumentedFragment;
import java.util.LinkedList;
import java.util.Queue;

public class FingerprintRemoveSidecar extends InstrumentedFragment {
    private static final String TAG = "FingerprintRemoveSidecar";
    FingerprintManager mFingerprintManager;
    private Fingerprint mFingerprintRemoving;
    private Queue<Object> mFingerprintsRemoved = new LinkedList();
    private Listener mListener;
    private RemovalCallback mRemoveCallback = new RemovalCallback() {
        public void onRemovalSucceeded(Fingerprint fingerprint, int remaining) {
            if (FingerprintRemoveSidecar.this.mListener != null) {
                FingerprintRemoveSidecar.this.mListener.onRemovalSucceeded(fingerprint);
            } else {
                FingerprintRemoveSidecar.this.mFingerprintsRemoved.add(fingerprint);
            }
            FingerprintRemoveSidecar.this.mFingerprintRemoving = null;
        }

        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
            if (FingerprintRemoveSidecar.this.mListener != null) {
                FingerprintRemoveSidecar.this.mListener.onRemovalError(fp, errMsgId, errString);
            } else {
                FingerprintRemoveSidecar.this.mFingerprintsRemoved.add(new RemovalError(fp, errMsgId, errString));
            }
            FingerprintRemoveSidecar.this.mFingerprintRemoving = null;
        }
    };

    public interface Listener {
        void onRemovalError(Fingerprint fingerprint, int i, CharSequence charSequence);

        void onRemovalSucceeded(Fingerprint fingerprint);
    }

    private class RemovalError {
        int errMsgId;
        CharSequence errString;
        Fingerprint fingerprint;

        public RemovalError(Fingerprint fingerprint, int errMsgId, CharSequence errString) {
            this.fingerprint = fingerprint;
            this.errMsgId = errMsgId;
            this.errString = errString;
        }
    }

    public void startRemove(Fingerprint fingerprint, int userId) {
        if (this.mFingerprintRemoving != null) {
            Log.e(TAG, "Remove already in progress");
            return;
        }
        if (userId != -10000) {
            this.mFingerprintManager.setActiveUser(userId);
        }
        this.mFingerprintRemoving = fingerprint;
        this.mFingerprintManager.remove(fingerprint, userId, this.mRemoveCallback);
    }

    public void setFingerprintManager(FingerprintManager fingerprintManager) {
        this.mFingerprintManager = fingerprintManager;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setListener(Listener listener) {
        if (this.mListener == null && listener != null) {
            while (!this.mFingerprintsRemoved.isEmpty()) {
                RemovalError o = this.mFingerprintsRemoved.poll();
                if (o instanceof Fingerprint) {
                    listener.onRemovalSucceeded((Fingerprint) o);
                } else if (o instanceof RemovalError) {
                    RemovalError e = o;
                    listener.onRemovalError(e.fingerprint, e.errMsgId, e.errString);
                }
            }
        }
        this.mListener = listener;
    }

    /* Access modifiers changed, original: final */
    public final boolean isRemovingFingerprint(int fid) {
        return inProgress() && this.mFingerprintRemoving.getFingerId() == fid;
    }

    /* Access modifiers changed, original: final */
    public final boolean inProgress() {
        return this.mFingerprintRemoving != null;
    }

    public int getMetricsCategory() {
        return 934;
    }
}
