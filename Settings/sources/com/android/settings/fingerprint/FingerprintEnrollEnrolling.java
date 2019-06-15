package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Animatable2.AnimationCallback;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.opfinger.OPFingerPrintEnrollView;
import com.oneplus.settings.opfinger.OPFingerPrintEnrollView.OnOPFingerComfirmListener;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.io.PrintStream;

public class FingerprintEnrollEnrolling extends FingerprintEnrollBase implements Listener, OnOPFingerComfirmListener {
    private static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;
    private static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;
    private static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;
    private static final int FINGERPRINT_ACQUIRED_TOO_SIMILAR = 1002;
    private static final AudioAttributes FINGERPRINT_ENROLLING_SONFICATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static final int FINISH_DELAY = 250;
    private static final int HINT_TIMEOUT_DURATION = 2500;
    private static final int ICON_TOUCH_COUNT_SHOW_UNTIL_DIALOG_SHOWN = 3;
    private static final long ICON_TOUCH_DURATION_UNTIL_DIALOG_SHOWN = 500;
    private static final int PAUSE_ENROLL_STATE = 9;
    private static final int PROGRESS_BAR_MAX = 10000;
    private static final int RESUME_ENROLL_STATE = 8;
    private static final int RESUME_ENROLL_STATE_FOR_FINISH = 10;
    private static final String TAG = "FingerprintEnrollEnrolling";
    static final String TAG_SIDECAR = "sidecar";
    private static final VibrationEffect VIBRATE_EFFECT_ERROR = VibrationEffect.createWaveform(new long[]{0, 5, 55, 60}, -1);
    Runnable callFingerprintServiceRunnable = new Runnable() {
        public void run() {
            if (FingerprintEnrollEnrolling.this.mSidecar == null) {
                FingerprintEnrollEnrolling.this.mSidecar = new FingerprintEnrollSidecar();
                FingerprintEnrollEnrolling.this.getFragmentManager().beginTransaction().add(FingerprintEnrollEnrolling.this.mSidecar, FingerprintEnrollEnrolling.TAG_SIDECAR).commit();
            }
            FingerprintEnrollEnrolling.this.mSidecar.setListener(FingerprintEnrollEnrolling.this);
        }
    };
    private boolean mAnimationCancelled;
    private boolean mConfirmCompleted = false;
    private int mCurrentProgress = 0;
    private final Runnable mDelayedFinishRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.launchFinish(FingerprintEnrollEnrolling.this.mToken);
        }
    };
    private LottieAnimationView mEdgeEnrollAnimView;
    private int mEnrollState = -1;
    private int mEnrollSuccessCount = 0;
    private View mEnrollingAnim;
    private TextView mErrorText;
    private Interpolator mFastOutLinearInInterpolator;
    private Interpolator mFastOutSlowInInterpolator;
    private FingerprintManager mFingerprintManager;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private boolean mHasInputCompleted = false;
    private final AnimationCallback mIconAnimationCallback = new AnimationCallback() {
        public void onAnimationEnd(Drawable d) {
            if (!FingerprintEnrollEnrolling.this.mAnimationCancelled) {
                FingerprintEnrollEnrolling.this.mProgressBar.post(new Runnable() {
                    public void run() {
                        FingerprintEnrollEnrolling.this.startIconAnimation();
                    }
                });
            }
        }
    };
    private AnimatedVectorDrawable mIconAnimationDrawable;
    private Drawable mIconBackgroundDrawable;
    private int mIconTouchCount;
    private int mIndicatorBackgroundActivatedColor;
    private int mIndicatorBackgroundRestingColor;
    private boolean mIsEnrollPaused = false;
    private boolean mLaunchingFinish;
    private Interpolator mLinearOutSlowInInterpolator;
    protected boolean mNeedHideNavBar = true;
    private boolean mNeedJumpToFingerprintSettings = false;
    private Button mNextButton;
    protected OPFingerPrintEnrollView mOPFingerPrintEnrollView;
    private boolean mOnBackPress = false;
    private ObjectAnimator mProgressAnim;
    private final AnimatorListener mProgressAnimationListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (FingerprintEnrollEnrolling.this.mProgressBar.getProgress() >= 10000) {
                FingerprintEnrollEnrolling.this.mProgressBar.postDelayed(FingerprintEnrollEnrolling.this.mDelayedFinishRunnable, 250);
            }
        }

        public void onAnimationCancel(Animator animation) {
        }
    };
    private ProgressBar mProgressBar;
    protected TextView mRepeatMessage;
    private boolean mRestoring;
    private boolean mScreenNavBarEnabled = false;
    private final Runnable mShowDialogRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.showIconTouchDialog();
        }
    };
    private FingerprintEnrollSidecar mSidecar;
    protected TextView mStartMessage;
    StatusBarManager mStatusBarManager;
    private final Runnable mTouchAgainRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollEnrolling.this.clearError();
            FingerprintEnrollEnrolling.this.showError(FingerprintEnrollEnrolling.this.getString(R.string.security_settings_fingerprint_enroll_lift_touch_again));
        }
    };
    private boolean mValidEnroll = true;
    private Vibrator mVibrator;
    private WakeLock mWakeLock;
    private Runnable mWakeLockUseRunnable = new Runnable() {
        public void run() {
            if (FingerprintEnrollEnrolling.this.mWakeLock != null) {
                FingerprintEnrollEnrolling.this.releaseWakeLock();
            }
        }
    };
    int overlayLayoutId = -1;

    public enum KeyLockMode {
        NORMAL,
        POWER,
        POWER_HOME,
        HOME,
        FOOT,
        BACK_SWITCH,
        BASE
    }

    public static class ErrorDialog extends InstrumentedDialogFragment {
        static ErrorDialog newInstance(CharSequence msg, int msgId) {
            ErrorDialog dlg = new ErrorDialog();
            Bundle args = new Bundle();
            args.putCharSequence("error_msg", msg);
            args.putInt("error_id", msgId);
            dlg.setArguments(args);
            return dlg;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            CharSequence errorString = getArguments().getCharSequence("error_msg");
            final int errMsgId = getArguments().getInt("error_id");
            builder.setTitle(R.string.oneplus_security_settings_fingerprint_enroll_error_dialog_title).setMessage(errorString).setCancelable(false).setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (errMsgId == 3) {
                    }
                    ErrorDialog.this.getActivity().finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        public int getMetricsCategory() {
            return 569;
        }
    }

    public static class IconTouchDialog extends InstrumentedDialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.security_settings_fingerprint_enroll_touch_dialog_title).setMessage(R.string.security_settings_fingerprint_enroll_touch_dialog_message).setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }

        public int getMetricsCategory() {
            return 568;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        int layoutId;
        if (OPUtils.isSupportCustomFingerprint()) {
            setTheme(R.style.OnePlusFingerprintEnrolling);
        }
        super.onCreate(savedInstanceState);
        boolean z = false;
        if (isInMultiWindowMode()) {
            Toast.makeText(this, R.string.oneplus_cannot_enroll_fingerprint_in_splitting_screen, 0).show();
            finish();
        }
        this.mNeedJumpToFingerprintSettings = getIntent().getBooleanExtra("needJumpToFingerprintSettings", false);
        this.mFingerprintManager = Utils.getFingerprintManagerOrNull(this);
        if (OPUtils.isSupportCustomFingerprint()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.oneplus_add_fingerprint_list);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            if (this.overlayLayoutId != -1) {
                layoutId = this.overlayLayoutId;
            } else {
                layoutId = R.layout.op_custom_fingerprint_enroll_enrolling_base;
            }
        } else if (!OPUtils.isSurportBackFingerprint(this)) {
            layoutId = R.layout.fingerprint_enroll_enrolling_base;
        } else if (this.overlayLayoutId != -1) {
            layoutId = this.overlayLayoutId;
        } else {
            layoutId = R.layout.op_back_fingerprint_enroll_enrolling_base;
        }
        setContentView(layoutId);
        enrollAnimMatchDifferentDpi();
        setHeaderText(R.string.security_settings_fingerprint_enroll_repeat_title);
        this.mStartMessage = (TextView) findViewById(R.id.start_message);
        this.mRepeatMessage = (TextView) findViewById(R.id.repeat_message);
        this.mErrorText = (TextView) findViewById(R.id.error_text);
        this.mVibrator = (Vibrator) getSystemService(Vibrator.class);
        if (OPUtils.isSupportCustomFingerprint()) {
            this.mNextButton = (Button) findViewById(R.id.next_button);
            this.mNextButton.setTextColor(-1);
            this.mNextButton.setOnClickListener(this);
            this.mNextButton.setVisibility(8);
            this.mEdgeEnrollAnimView = (LottieAnimationView) findViewById(R.id.op_finger_edge_enroll_view);
            this.mEdgeEnrollAnimView.loop(true);
        }
        ((Button) findViewById(R.id.skip_button)).setOnClickListener(this);
        this.mRestoring = savedInstanceState != null;
        initFingerPrintEnrollView();
        this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
        if (System.getInt(getContentResolver(), OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, 0) == 1) {
            z = true;
        }
        this.mScreenNavBarEnabled = z;
    }

    private void initFingerPrintEnrollView() {
        this.mOPFingerPrintEnrollView = (OPFingerPrintEnrollView) findViewById(R.id.op_finger_enroll_view);
        this.mOPFingerPrintEnrollView.setTitleView(getHeadView());
        this.mOPFingerPrintEnrollView.setSubTitleView(this.mStartMessage);
        this.mOPFingerPrintEnrollView.hideHeaderView();
        this.mOPFingerPrintEnrollView.setOnOPFingerComfirmListener(this);
        if (OPUtils.isSupportCustomFingerprint()) {
            this.mOPFingerPrintEnrollView.setEnrollAnimVisibility(false);
        }
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this, AndroidResources.FAST_OUT_LINEAR_IN);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this, AndroidResources.LINEAR_OUT_SLOW_IN);
    }

    private void enrollAnimMatchDifferentDpi() {
        if (OPUtils.isSupportCustomFingerprint()) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("zhuyang--fingerprint:");
            stringBuilder.append(displayMetrics.densityDpi);
            stringBuilder.append("");
            printStream.println(stringBuilder.toString());
            this.mEnrollingAnim = findViewById(R.id.fingerprint_enroll_enrolling_anim);
            if (this.mEnrollingAnim == null) {
                return;
            }
            if ((displayMetrics.densityDpi > 420 && System.getInt(getContentResolver(), "op_navigation_bar_type", 1) == 3) || (displayMetrics.densityDpi > 480 && System.getInt(getContentResolver(), "op_navigation_bar_type", 1) != 3)) {
                LayoutParams enrollingAnimLayout = new LayoutParams(-2, -2);
                enrollingAnimLayout.setMargins(0, 0, 0, 0);
                this.mEnrollingAnim.setLayoutParams(enrollingAnimLayout);
            }
        }
    }

    private TextView getHeadView() {
        TextView layoutTitle = getLayout().getHeaderTextView();
        if (OPUtils.isSupportCustomFingerprint()) {
            layoutTitle.setTextColor(getResources().getColor(R.color.oneplus_contorl_text_color_primary_dark));
        }
        return layoutTitle;
    }

    public void onResume() {
        super.onResume();
        if (OPUtils.isSupportCustomFingerprint()) {
            disableRecentAndHomeKey();
        } else if (OPUtils.isSurportBackFingerprint(this)) {
            disableRecentKey();
        } else {
            disableAllKey();
        }
        if (!(this.mConfirmCompleted || (this.mOnBackPress && this.mHasInputCompleted))) {
            this.mOPFingerPrintEnrollView.resetWithoutAnimation();
        }
        this.mHasInputCompleted = false;
        this.mConfirmCompleted = false;
        this.mOnBackPress = false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!this.mIsEnrollPaused && !isFinishing()) {
            changeEnrollStateByFocusChanged(hasFocus);
        }
    }

    private void changeEnrollStateByFocusChanged(boolean hasFocus) {
        IFingerprintService ifp = Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (ifp != null) {
            int state;
            if (hasFocus) {
                state = 8;
            } else {
                state = 9;
            }
            try {
                ifp.updateStatus(state);
                Log.w(TAG, "changeEnrollStateByFocusChanged ");
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("updateStatus , ");
                stringBuilder.append(e);
                Log.w(str, stringBuilder.toString());
            }
        }
    }

    private void showScreenNavBar(boolean hide) {
        System.putInt(getContentResolver(), OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, hide ^ 1);
    }

    public void enableAllKey() {
        if (this.mLaunchingFinish) {
            setFingerprintEnrolling(true);
        } else {
            setFingerprintEnrolling(false);
        }
        if (this.mScreenNavBarEnabled) {
            showScreenNavBar(false);
        }
    }

    public void disableAllKey() {
        this.mLaunchingFinish = false;
        setFingerprintEnrolling(true);
        if (this.mScreenNavBarEnabled) {
            showScreenNavBar(true);
        }
    }

    private void disableRecentAndHomeKey() {
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(18874368);
        }
    }

    private void enableRecentAndHomeKey() {
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(0);
        }
    }

    private void disableRecentKey() {
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(16777216);
        }
    }

    private void enableRecentKey() {
        if (this.mStatusBarManager != null) {
            this.mStatusBarManager.disable(0);
        }
    }

    private void setFingerprintEnrolling(boolean enrolling) {
        if (!OPUtils.isSurportBackFingerprint(this)) {
            boolean z = false;
            if (System.getInt(getApplicationContext().getContentResolver(), OPConstants.OEM_ACC_FINGERPRINT_ENROLLING, 0) != 0) {
                z = true;
            }
            if (enrolling != z) {
                System.putInt(getApplicationContext().getContentResolver(), OPConstants.OEM_ACC_FINGERPRINT_ENROLLING, enrolling);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        this.mOnBackPress = true;
        if (this.mSidecar != null) {
            this.mSidecar.setListener(null);
            this.mSidecar.cancelEnrollment();
            getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
            this.mSidecar = null;
        }
        finish();
        return true;
    }

    public void onOPFingerComfirmClick() {
        this.mConfirmCompleted = true;
        setResult(1);
        finish();
    }

    private void acquireWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(26, TAG);
            this.mWakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            this.mCurrentProgress = 0;
            this.mEnrollSuccessCount = 0;
            if (OPUtils.isSupportCustomFingerprint()) {
                enableRecentAndHomeKey();
            } else if (OPUtils.isSurportBackFingerprint(this)) {
                enableRecentKey();
            } else {
                enableAllKey();
            }
            releaseWakeLock();
            if (!this.mConfirmCompleted && this.mOnBackPress) {
                boolean z = this.mHasInputCompleted;
            }
            this.mOPFingerPrintEnrollView.hideWarningTips();
            this.mHandler.removeCallbacks(this.mTouchAgainRunnable);
            this.mConfirmCompleted = false;
            this.mOnBackPress = false;
            this.mHasInputCompleted = false;
            resumeEnroll(false, 10);
            setResult(1);
            finish();
        }
    }

    public void onDestroy() {
        if (OPUtils.isSupportCustomFingerprint()) {
            if (this.mNeedHideNavBar) {
                enableAllKey();
            } else {
                enableRecentAndHomeKey();
            }
        } else if (OPUtils.isSurportBackFingerprint(this)) {
            enableRecentKey();
        } else {
            enableAllKey();
        }
        if (this.mEdgeEnrollAnimView != null) {
            this.mEdgeEnrollAnimView.cancelAnimation();
            this.mEdgeEnrollAnimView = null;
        }
        super.onDestroy();
    }

    private void fadeIn(final View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", new float[]{0.0f, 1.0f});
        animator.setDuration(300);
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView != null && FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView.equals(view)) {
                    FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView.setVisibility(0);
                }
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        animator.start();
    }

    private void fadeOut(final View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", new float[]{1.0f, 0.0f});
        animator.setDuration(300);
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView != null && FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView.equals(view)) {
                    FingerprintEnrollEnrolling.this.mEdgeEnrollAnimView.setVisibility(8);
                }
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        animator.start();
    }

    private void pauseEnroll() {
        if (OPUtils.isSupportCustomFingerprint()) {
            this.mNextButton.setVisibility(0);
            fadeOut(this.mOPFingerPrintEnrollView);
            fadeIn(this.mEdgeEnrollAnimView);
            this.mEdgeEnrollAnimView.playAnimation();
        }
        this.mIsEnrollPaused = true;
        IFingerprintService ifp = Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (ifp != null) {
            try {
                ifp.updateStatus(9);
                this.mEnrollState = 9;
                Log.w(TAG, "pauseEnroll ");
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("updateStatus , ");
                stringBuilder.append(e);
                Log.w(str, stringBuilder.toString());
            }
        }
    }

    private void showContinueView() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                FingerprintEnrollEnrolling.this.mOPFingerPrintEnrollView.showContinueView();
            }
        }, 300);
    }

    private void resumeEnroll(boolean needRefreshUI, int state) {
        if (OPUtils.isSupportCustomFingerprint() && needRefreshUI) {
            this.mNextButton.setVisibility(8);
            fadeIn(this.mOPFingerPrintEnrollView);
            fadeOut(this.mEdgeEnrollAnimView);
            showContinueView();
            this.mEdgeEnrollAnimView.pauseAnimation();
        }
        this.mIsEnrollPaused = false;
        IFingerprintService ifp = Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (ifp != null && this.mEnrollState == 9) {
            try {
                ifp.updateStatus(state);
                this.mEnrollState = state;
                Log.w(TAG, "resumeEnroll ");
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("updateStatus , ");
                stringBuilder.append(e);
                Log.w(str, stringBuilder.toString());
            }
        }
    }

    private void updateProgress(boolean animate, int enrollSteps, int enrollStepsRemaining) {
        if (enrollSteps != -1) {
            this.mOPFingerPrintEnrollView.setEnrollAnimVisibility(true);
            int progress = (((enrollSteps + 1) - enrollStepsRemaining) * 100) / (enrollSteps + 1);
            if (progress <= this.mCurrentProgress || enrollStepsRemaining > enrollSteps) {
                this.mCurrentProgress = progress;
                this.mOPFingerPrintEnrollView.doRecognition(this.mEnrollSuccessCount + 1, progress, false);
            } else {
                clearError();
                this.mCurrentProgress = progress;
                this.mEnrollSuccessCount++;
                this.mOPFingerPrintEnrollView.doRecognition(this.mEnrollSuccessCount, progress, true);
            }
            if (this.mEnrollSuccessCount == OPUtils.getFingerprintScaleAnimStep(this)) {
                pauseEnroll();
                this.mOPFingerPrintEnrollView.setTipsContinueContent();
                if (!OPUtils.isSupportCustomFingerprint()) {
                    showContinueView();
                }
            } else if (enrollSteps != -1 && enrollStepsRemaining == 0) {
                System.out.println("oneplus--setTipsCompletedContent");
            }
            this.mHandler.removeCallbacks(this.mTouchAgainRunnable);
            if (progress >= 100) {
                this.mHasInputCompleted = true;
                this.mHandler.removeCallbacks(this.mTouchAgainRunnable);
                this.mHandler.postDelayed(this.mDelayedFinishRunnable, 250);
            }
            if (this.mWakeLock == null || !(this.mWakeLock == null || this.mWakeLock.isHeld())) {
                acquireWakeLock();
            }
            this.mHandler.removeCallbacks(this.mWakeLockUseRunnable);
            this.mHandler.postDelayed(this.mWakeLockUseRunnable, (long) System.getInt(getContentResolver(), "screen_off_timeout", 0));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag(TAG_SIDECAR);
        delayCallFingerprintService();
    }

    private void delayCallFingerprintService() {
        this.mHandler.removeCallbacks(this.callFingerprintServiceRunnable);
        this.mHandler.postDelayed(this.callFingerprintServiceRunnable, 150);
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        this.mAnimationCancelled = false;
    }

    private void startIconAnimation() {
        this.mIconAnimationDrawable.start();
    }

    private void stopIconAnimation() {
        this.mAnimationCancelled = true;
        this.mIconAnimationDrawable.stop();
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        this.mHandler.removeCallbacks(this.callFingerprintServiceRunnable);
        if (this.mSidecar != null) {
            this.mSidecar.setListener(null);
        }
        if (!isChangingConfigurations() && this.mSidecar != null) {
            this.mSidecar.cancelEnrollment();
            getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
        }
    }

    public void onBackPressed() {
        if (this.mSidecar != null) {
            this.mSidecar.setListener(null);
            this.mSidecar.cancelEnrollment();
            getFragmentManager().beginTransaction().remove(this.mSidecar).commitAllowingStateLoss();
            this.mSidecar = null;
        }
        super.onBackPressed();
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.next_button) {
            resumeEnroll(true, 8);
        } else if (id != R.id.skip_button) {
            super.onClick(v);
        } else {
            setResult(2);
            finish();
        }
    }

    private void animateProgress(int progress) {
        if (this.mProgressAnim != null) {
            this.mProgressAnim.cancel();
        }
        ObjectAnimator anim = ObjectAnimator.ofInt(this.mProgressBar, NotificationCompat.CATEGORY_PROGRESS, new int[]{this.mProgressBar.getProgress(), progress});
        anim.addListener(this.mProgressAnimationListener);
        anim.setInterpolator(this.mFastOutSlowInInterpolator);
        anim.setDuration(250);
        anim.start();
        this.mProgressAnim = anim;
    }

    private void animateFlash() {
        ValueAnimator anim = ValueAnimator.ofArgb(new int[]{this.mIndicatorBackgroundRestingColor, this.mIndicatorBackgroundActivatedColor});
        final AnimatorUpdateListener listener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintEnrollEnrolling.this.mIconBackgroundDrawable.setTint(((Integer) animation.getAnimatedValue()).intValue());
            }
        };
        anim.addUpdateListener(listener);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ValueAnimator anim = ValueAnimator.ofArgb(new int[]{FingerprintEnrollEnrolling.this.mIndicatorBackgroundActivatedColor, FingerprintEnrollEnrolling.this.mIndicatorBackgroundRestingColor});
                anim.addUpdateListener(listener);
                anim.setDuration(300);
                anim.setInterpolator(FingerprintEnrollEnrolling.this.mLinearOutSlowInInterpolator);
                anim.start();
            }
        });
        anim.setInterpolator(this.mFastOutSlowInInterpolator);
        anim.setDuration(300);
        anim.start();
    }

    private void launchFinish(byte[] token) {
        this.mLaunchingFinish = true;
        if (OPUtils.isSupportCustomFingerprint() && this.mFingerprintManager.getEnrolledFingerprints().size() == 1) {
            OPGestureUtils.set1(this, 15);
            int currentUser = ActivityManager.getCurrentUser();
            System.putIntForUser(getContentResolver(), "prox_wake_enabled", 1, currentUser);
            OPUtils.sendAppTracker(OPConstants.PICK_UP_PHONE_SHOW, 1);
            Secure.putIntForUser(getContentResolver(), "doze_enabled", 1, currentUser);
            if (OPGestureUtils.get(System.getInt(getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0), 7) == 0) {
                OPGestureUtils.set1(this, 11);
            }
        }
        Intent intent = getFinishIntent();
        intent.addFlags(637534208);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        intent.putExtra("needJumpToFingerprintSettings", this.mNeedJumpToFingerprintSettings);
        startActivity(intent);
        overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
        finish();
    }

    /* Access modifiers changed, original: protected */
    public Intent getFinishIntent() {
        return new Intent(this, FingerprintEnrollFinish.class);
    }

    private void updateDescription() {
        if (this.mSidecar.getEnrollmentSteps() == -1) {
            setHeaderText(R.string.security_settings_fingerprint_enroll_start_title);
            this.mStartMessage.setVisibility(0);
            this.mRepeatMessage.setVisibility(4);
            return;
        }
        setHeaderText(R.string.security_settings_fingerprint_enroll_repeat_title, true);
        this.mStartMessage.setVisibility(4);
        this.mRepeatMessage.setVisibility(0);
    }

    public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onEnrollmentHelp:");
        stringBuilder.append(helpString);
        stringBuilder.append(" helpMsgId:");
        stringBuilder.append(helpMsgId);
        Log.d(str, stringBuilder.toString());
        this.mValidEnroll = false;
        this.mOPFingerPrintEnrollView.setEnrollAnimVisibility(true);
        if (OPUtils.isSupportCustomFingerprint()) {
            if (helpMsgId == 1) {
                showError(getText(R.string.oneplus_fingerprint_acquired_partial));
            } else if (helpMsgId == 3) {
                showError(getText(R.string.oneplus_fingerprint_acquired_imager_dirty));
            } else if (helpMsgId == 5) {
                showError(getText(R.string.oneplus_fingerprint_acquired_too_fast));
            } else if (helpMsgId == 1002) {
                showError(getText(R.string.oneplus_fingerprint_acquired_too_similar));
            } else if (helpMsgId != 1100) {
                showError(helpString);
            } else {
                showError(getText(R.string.oneplus_security_settings_fingerprint_exists_dialog_message));
            }
        } else if (helpMsgId == 1002) {
            helpString = getText(R.string.oneplus_fingerprint_acquired_too_similar);
            showError(helpString);
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("FINGERPRINT_ACQUIRED_TOO_SIMILAR:");
            stringBuilder.append(helpString);
            Log.d(str, stringBuilder.toString());
        } else if (helpMsgId != 1100) {
            showError(helpString);
        } else {
            showError(getText(R.string.oneplus_security_settings_fingerprint_exists_dialog_message));
        }
    }

    public void onEnrollmentError(int errMsgId, CharSequence errString) {
        int msgId;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onEnrollmentError:");
        stringBuilder.append(errString);
        stringBuilder.append(" errMsgId:");
        stringBuilder.append(errMsgId);
        Log.d(str, stringBuilder.toString());
        this.mValidEnroll = false;
        this.mOPFingerPrintEnrollView.setEnrollAnimVisibility(true);
        if (errMsgId != 3) {
            msgId = R.string.oneplus_finger_input_error_tips;
        } else {
            msgId = R.string.security_settings_fingerprint_enroll_error_timeout_dialog_message;
        }
        showErrorDialog(getText(msgId), errMsgId);
        this.mErrorText.removeCallbacks(this.mTouchAgainRunnable);
    }

    public void onEnrollmentProgressChange(int steps, int remaining) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onEnrollmentProgressChange--mValidEnroll:");
        stringBuilder.append(this.mValidEnroll);
        stringBuilder.append(" steps:");
        stringBuilder.append(steps);
        stringBuilder.append(" remaining:");
        stringBuilder.append(remaining);
        Log.d(str, stringBuilder.toString());
        updateProgress(true, steps, remaining);
    }

    private int getProgress(int steps, int remaining) {
        if (steps == -1) {
            return 0;
        }
        return (10000 * Math.max(0, (steps + 1) - remaining)) / (steps + 1);
    }

    private void showErrorDialog(CharSequence msg, int msgId) {
        ErrorDialog.newInstance(msg, msgId).show(getFragmentManager(), ErrorDialog.class.getName());
    }

    private void showIconTouchDialog() {
        this.mIconTouchCount = 0;
        new IconTouchDialog().show(getFragmentManager(), null);
    }

    private void showError(CharSequence error) {
        this.mErrorText.setText(error);
        if (this.mErrorText.getVisibility() == 4) {
            this.mErrorText.setVisibility(0);
            this.mErrorText.setTranslationY((float) getResources().getDimensionPixelSize(R.dimen.fingerprint_error_text_appear_distance));
            this.mErrorText.setAlpha(0.0f);
            this.mErrorText.animate().alpha(1.0f).translationY(0.0f).setDuration(200).setInterpolator(this.mLinearOutSlowInInterpolator).start();
            return;
        }
        this.mErrorText.animate().cancel();
        this.mErrorText.setAlpha(1.0f);
        this.mErrorText.setTranslationY(0.0f);
    }

    private void clearError() {
        if (this.mErrorText.getVisibility() == 0) {
            this.mErrorText.animate().alpha(0.0f).translationY((float) getResources().getDimensionPixelSize(R.dimen.fingerprint_error_text_disappear_distance)).setDuration(100).setInterpolator(this.mFastOutLinearInInterpolator).withEndAction(new -$$Lambda$FingerprintEnrollEnrolling$aLk12WuaBTV2piitP3fdnB0w-eM(this)).start();
        }
    }

    public int getMetricsCategory() {
        return 240;
    }
}
