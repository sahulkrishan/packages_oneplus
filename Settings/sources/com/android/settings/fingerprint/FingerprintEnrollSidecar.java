package com.android.settings.fingerprint;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.EnrollmentCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.password.ChooseLockSettingsHelper;
import java.util.ArrayList;

public class FingerprintEnrollSidecar extends InstrumentedFragment {
    private static final String TAG = "Settings_Sidecar";
    private boolean mDone;
    private boolean mEnrolling;
    private EnrollmentCallback mEnrollmentCallback = new EnrollmentCallback() {
        public void onEnrollmentProgress(int remaining) {
            if (FingerprintEnrollSidecar.this.mEnrollmentSteps == -1) {
                FingerprintEnrollSidecar.this.mEnrollmentSteps = remaining;
            }
            FingerprintEnrollSidecar.this.mEnrollmentRemaining = remaining;
            FingerprintEnrollSidecar.this.mDone = remaining == 0;
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentProgressChange(FingerprintEnrollSidecar.this.mEnrollmentSteps, remaining);
            } else {
                FingerprintEnrollSidecar.this.mQueuedEvents.add(new QueuedEnrollmentProgress(FingerprintEnrollSidecar.this.mEnrollmentSteps, remaining));
            }
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentHelp(helpMsgId, helpString);
            } else {
                FingerprintEnrollSidecar.this.mQueuedEvents.add(new QueuedEnrollmentHelp(helpMsgId, helpString));
            }
        }

        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentError(errMsgId, errString);
            } else {
                FingerprintEnrollSidecar.this.mQueuedEvents.add(new QueuedEnrollmentError(errMsgId, errString));
            }
            FingerprintEnrollSidecar.this.mEnrolling = false;
        }
    };
    private CancellationSignal mEnrollmentCancel;
    private int mEnrollmentRemaining = 0;
    private int mEnrollmentSteps = -1;
    private FingerprintManager mFingerprintManager;
    private Handler mHandler = new Handler();
    private Listener mListener;
    private ArrayList<QueuedEvent> mQueuedEvents = new ArrayList();
    private final Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollSidecar.this.cancelEnrollment();
        }
    };
    private byte[] mToken;
    private int mUserId;

    public interface Listener {
        void onEnrollmentError(int i, CharSequence charSequence);

        void onEnrollmentHelp(int i, CharSequence charSequence);

        void onEnrollmentProgressChange(int i, int i2);
    }

    private abstract class QueuedEvent {
        public abstract void send(Listener listener);

        private QueuedEvent() {
        }

        /* synthetic */ QueuedEvent(FingerprintEnrollSidecar x0, AnonymousClass1 x1) {
            this();
        }
    }

    private class QueuedEnrollmentError extends QueuedEvent {
        int errMsgId;
        CharSequence errString;

        public QueuedEnrollmentError(int errMsgId, CharSequence errString) {
            super(FingerprintEnrollSidecar.this, null);
            this.errMsgId = errMsgId;
            this.errString = errString;
        }

        public void send(Listener listener) {
            listener.onEnrollmentError(this.errMsgId, this.errString);
        }
    }

    private class QueuedEnrollmentHelp extends QueuedEvent {
        int helpMsgId;
        CharSequence helpString;

        public QueuedEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            super(FingerprintEnrollSidecar.this, null);
            this.helpMsgId = helpMsgId;
            this.helpString = helpString;
        }

        public void send(Listener listener) {
            listener.onEnrollmentHelp(this.helpMsgId, this.helpString);
        }
    }

    private class QueuedEnrollmentProgress extends QueuedEvent {
        int enrollmentSteps;
        int remaining;

        public QueuedEnrollmentProgress(int enrollmentSteps, int remaining) {
            super(FingerprintEnrollSidecar.this, null);
            this.enrollmentSteps = enrollmentSteps;
            this.remaining = remaining;
        }

        public void send(Listener listener) {
            listener.onEnrollmentProgressChange(this.enrollmentSteps, this.remaining);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mFingerprintManager = Utils.getFingerprintManagerOrNull(activity);
        this.mToken = activity.getIntent().getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        this.mUserId = activity.getIntent().getIntExtra("android.intent.extra.USER_ID", -10000);
    }

    public void onStart() {
        super.onStart();
        if (!this.mEnrolling) {
            startEnrollment();
        }
    }

    public void onStop() {
        super.onStop();
        if (!getActivity().isChangingConfigurations()) {
            Log.d(TAG, "onStop: cancelEnrollment");
            cancelEnrollment();
        }
    }

    private void startEnrollment() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mEnrollmentSteps = -1;
        this.mEnrollmentCancel = new CancellationSignal();
        if (this.mUserId != -10000) {
            this.mFingerprintManager.setActiveUser(this.mUserId);
        }
        Log.d(TAG, "startEnrollment: ");
        this.mFingerprintManager.enroll(this.mToken, this.mEnrollmentCancel, 0, this.mUserId, this.mEnrollmentCallback);
        this.mEnrolling = true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean cancelEnrollment() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        if (!this.mEnrolling) {
            return false;
        }
        Log.d(TAG, "cancelEnrollment: ");
        this.mEnrollmentCancel.cancel();
        this.mEnrolling = false;
        this.mEnrollmentSteps = -1;
        return true;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
        if (this.mListener != null) {
            for (int i = 0; i < this.mQueuedEvents.size(); i++) {
                ((QueuedEvent) this.mQueuedEvents.get(i)).send(this.mListener);
            }
            this.mQueuedEvents.clear();
        }
    }

    public int getEnrollmentSteps() {
        return this.mEnrollmentSteps;
    }

    public int getEnrollmentRemaining() {
        return this.mEnrollmentRemaining;
    }

    public boolean isDone() {
        return this.mDone;
    }

    public int getMetricsCategory() {
        return 245;
    }

    public boolean isEnrolling() {
        return this.mEnrolling;
    }
}
