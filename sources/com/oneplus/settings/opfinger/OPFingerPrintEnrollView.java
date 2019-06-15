package com.oneplus.settings.opfinger;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.oneplus.lib.widget.button.OPButton;
import com.oneplus.settings.utils.OPUtils;

public class OPFingerPrintEnrollView extends RelativeLayout {
    private LayoutInflater inflater;
    private Context mContext;
    private LottieAnimationView mEnrollCompletedAnim;
    private Handler mHandler = new Handler();
    private ImageView mImage;
    private int mLayoutResId = R.layout.op_fingerprint_input_category;
    private OPButton mOPFingerInputCompletedComfirmBtn;
    private TextView mOPFingerInputTipsSubTitle;
    private TextView mOPFingerInputTipsTitle;
    private TextView mOPFingerInputTipsWarning;
    private OPFingerPrintFodBgAnimView mOPFingerPrintFodBgAnimView;
    private OPFingerPrintRecognitionContinueView mOPFingerPrintRecognitionContinueView;
    private OPFingerPrintRecognitionContinueWaveView mOPFingerPrintRecognitionContinueWaveView;
    private OPFingerPrintRecognitionView mOPFingerPrintRecognitionView;
    public OnOPFingerComfirmListener mOnOPFingerComfirmListener;
    private int mPercent = 0;
    private View mView;

    public interface OnOPFingerComfirmListener {
        void onOPFingerComfirmClick();
    }

    public OPFingerPrintEnrollView(Context context) {
        super(context);
        initViews(context);
    }

    public OPFingerPrintEnrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFingerPrintEnrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public void setTitleView(TextView titleView) {
        this.mOPFingerInputTipsTitle = titleView;
    }

    public void setSubTitleView(TextView subTitleView) {
        this.mOPFingerInputTipsSubTitle = subTitleView;
    }

    public void hideHeaderView() {
        ImageView titleBgShadowView = (ImageView) findViewById(R.id.setup_title_view_bg_shadow);
        TextView fingerInputTipsTitle = (TextView) this.mView.findViewById(R.id.opfinger_input_tips_title_tv);
        TextView fingerInputTipsSubTitle = (TextView) this.mView.findViewById(R.id.opfinger_input_tips_subtitle_tv);
        ((ImageView) findViewById(R.id.setup_title_view_bg)).setVisibility(8);
        titleBgShadowView.setVisibility(8);
        fingerInputTipsTitle.setVisibility(8);
        fingerInputTipsSubTitle.setVisibility(8);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mView = LayoutInflater.from(context).inflate(R.layout.op_fingerprint_input_category, this);
        this.mOPFingerPrintFodBgAnimView = (OPFingerPrintFodBgAnimView) this.mView.findViewById(R.id.op_finger_fod_bg_view);
        this.mOPFingerPrintRecognitionView = (OPFingerPrintRecognitionView) this.mView.findViewById(R.id.op_finger_recognition_view);
        this.mOPFingerPrintRecognitionContinueView = (OPFingerPrintRecognitionContinueView) this.mView.findViewById(R.id.op_finger_recognition_continue_view);
        this.mOPFingerPrintRecognitionContinueWaveView = (OPFingerPrintRecognitionContinueWaveView) this.mView.findViewById(R.id.op_finger_recognition_continue_wave_view);
        this.mOPFingerInputTipsTitle = (TextView) this.mView.findViewById(R.id.opfinger_input_tips_title_tv);
        this.mOPFingerInputTipsSubTitle = (TextView) this.mView.findViewById(R.id.opfinger_input_tips_subtitle_tv);
        this.mOPFingerInputTipsWarning = (TextView) this.mView.findViewById(R.id.opfinger_input_tips_warning);
        this.mOPFingerInputCompletedComfirmBtn = (OPButton) this.mView.findViewById(R.id.opfinger_input_completed_comfirm_btn);
        this.mOPFingerInputCompletedComfirmBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (OPFingerPrintEnrollView.this.mOnOPFingerComfirmListener != null) {
                    OPFingerPrintEnrollView.this.mOnOPFingerComfirmListener.onOPFingerComfirmClick();
                }
            }
        });
        this.mEnrollCompletedAnim = (LottieAnimationView) findViewById(R.id.opfinger_fod_enroll_completed_anim);
    }

    public void playEnrollCompletedAnim() {
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.setVisibility(8);
        }
        if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
        if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
            this.mOPFingerPrintRecognitionContinueWaveView.setVisibility(8);
        }
        if (this.mOPFingerPrintFodBgAnimView != null) {
            this.mOPFingerPrintFodBgAnimView.setVisibility(8);
        }
        if (this.mEnrollCompletedAnim != null) {
            this.mEnrollCompletedAnim.setVisibility(0);
            this.mEnrollCompletedAnim.playAnimation();
        }
    }

    public void releaseEnrollCompletedAnim() {
        if (this.mEnrollCompletedAnim != null) {
            this.mEnrollCompletedAnim.cancelAnimation();
            this.mEnrollCompletedAnim = null;
        }
    }

    public void startTouchDownAnim() {
        if (isNeedWaveEffect()) {
            if (this.mOPFingerPrintFodBgAnimView != null) {
                this.mOPFingerPrintFodBgAnimView.startTouchDownAnim();
            }
            if (this.mOPFingerPrintRecognitionView != null) {
                this.mOPFingerPrintRecognitionView.startTouchDownAnim();
            }
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.startTouchDownAnim();
            }
        }
    }

    public void setCenterVisible(boolean visible) {
        if (this.mOPFingerPrintFodBgAnimView != null) {
            this.mOPFingerPrintFodBgAnimView.setCenterVisible(visible);
        }
    }

    public void setEdgeVisible(boolean visible) {
        if (this.mOPFingerPrintFodBgAnimView != null) {
            this.mOPFingerPrintFodBgAnimView.setEdgeVisible(visible);
        }
    }

    public boolean isNeedWaveEffect() {
        return OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext);
    }

    public void setEnrollAnimVisibility(boolean visibility) {
        float f = 0.0f;
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.setAlpha(visibility ? 1.0f : 0.0f);
        }
        if (isNeedWaveEffect()) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                OPFingerPrintRecognitionContinueWaveView oPFingerPrintRecognitionContinueWaveView = this.mOPFingerPrintRecognitionContinueWaveView;
                if (visibility) {
                    f = 1.0f;
                }
                oPFingerPrintRecognitionContinueWaveView.setAlpha(f);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            OPFingerPrintRecognitionContinueView oPFingerPrintRecognitionContinueView = this.mOPFingerPrintRecognitionContinueView;
            if (visibility) {
                f = 1.0f;
            }
            oPFingerPrintRecognitionContinueView.setAlpha(f);
        }
    }

    public void setEnrollAnimBgColor(String colorString) {
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.setEnrollAnimBgColor(colorString);
        }
        if (isNeedWaveEffect()) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.setEnrollAnimBgColor(colorString);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.setEnrollAnimBgColor(colorString);
        }
    }

    public void setOnOPFingerComfirmListener(OnOPFingerComfirmListener listener) {
        this.mOnOPFingerComfirmListener = listener;
    }

    public void setTipsProgressContent(int count, int percent) {
        if (count == 13) {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_title);
            this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_sub);
            this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        } else if (percent >= 100) {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_saving_title);
            this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_saving_sub);
            this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        } else {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_recognize_title);
            this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_recognize_sub);
            this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        }
    }

    public void showWarningTips(CharSequence warningMsg) {
        if (this.mOPFingerInputTipsWarning != null) {
            this.mOPFingerInputTipsWarning.setText(warningMsg);
            this.mOPFingerInputTipsWarning.setAlpha(1.0f);
            this.mOPFingerInputTipsWarning.setVisibility(0);
        }
    }

    public void hideWarningTips() {
        if (this.mOPFingerInputTipsWarning != null) {
            this.mOPFingerInputTipsWarning.setText("");
            this.mOPFingerInputTipsWarning.setVisibility(4);
        }
    }

    public TextView getWarningTipsView() {
        return this.mOPFingerInputTipsWarning;
    }

    public void setTipsStatusContent(int status) {
        if (status == 1) {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_setting_tips_title);
            this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_setting_tips_sub);
        } else if (status == 3) {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_up_title);
            this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_up_sub);
        }
    }

    public void setTipsCompletedContent() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_completed_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_completed_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
    }

    public void setTipsContinueContent() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
    }

    public void showContinueView() {
        if (isNeedWaveEffect()) {
            this.mOPFingerPrintRecognitionContinueWaveView.setVisibility(0);
        } else {
            this.mOPFingerPrintRecognitionContinueView.setVisibility(0);
        }
        AnimationSet animSet = new AnimationSet(true);
        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(500);
        animSet.addAnimation(alphaAnim);
        ScaleAnimation scaleAnim = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnim.setDuration(500);
        animSet.addAnimation(scaleAnim);
        if (isNeedWaveEffect()) {
            this.mOPFingerPrintRecognitionContinueWaveView.setAnimation(animSet);
        } else {
            this.mOPFingerPrintRecognitionContinueView.setAnimation(animSet);
        }
        animSet.start();
    }

    public void doEnroll(int totalStep, int remainingStep, boolean success) {
        if (remainingStep >= 7) {
            if (this.mOPFingerPrintRecognitionView != null) {
                this.mOPFingerPrintRecognitionView.doRecognitionByCount(remainingStep, 0, success);
            }
        } else if (isNeedWaveEffect()) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.doRecognitionByCount(remainingStep, 0, success);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.doRecognitionByCount(remainingStep, 0, success);
        }
    }

    public void doRecognition(int count, int percent, boolean success) {
        int scaleAnimationStep = OPUtils.getFingerprintScaleAnimStep(this.mContext);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_fingerprint_enroll_summary);
        if (count <= scaleAnimationStep) {
            if (this.mOPFingerPrintRecognitionView != null) {
                this.mOPFingerPrintRecognitionView.doRecognitionByCount(count, percent, success);
            }
        } else if (isNeedWaveEffect()) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.doRecognitionByCount(count, percent, success);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.doRecognitionByCount(count, percent, success);
        }
    }

    public void resetTextAndBtn() {
        if (this.mOPFingerInputTipsTitle != null) {
            this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_setting_tips_title);
        }
        if (this.mOPFingerInputTipsSubTitle != null) {
            int stringId;
            if (OPUtils.isSupportCustomFingerprint()) {
                stringId = R.string.oneplus_fingerprint_enroll_summary;
            } else if (OPUtils.isSurportBackFingerprint(this.mContext)) {
                stringId = R.string.oneplus_opfinger_input_setting_back_tips_sub;
            } else {
                stringId = R.string.oneplus_opfinger_input_setting_tips_sub;
            }
            this.mOPFingerInputTipsSubTitle.setText(stringId);
        }
        if (this.mOPFingerInputCompletedComfirmBtn != null) {
            this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        }
    }

    public void enrollFailed() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_failed_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_failed_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.resetWithoutAnimation();
                this.mOPFingerPrintRecognitionContinueWaveView.setVisibility(8);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }

    public void revealWithoutAnimation() {
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.revealWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.revealWithoutAnimation();
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.revealWithoutAnimation();
        }
    }

    public void resetWithoutAnimation() {
        resetTextAndBtn();
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.resetWithoutAnimation();
                this.mOPFingerPrintRecognitionContinueWaveView.setVisibility(8);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }

    public void resetWithAnimation() {
        resetTextAndBtn();
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
            if (this.mOPFingerPrintRecognitionContinueWaveView != null) {
                this.mOPFingerPrintRecognitionContinueWaveView.resetWithoutAnimation();
                this.mOPFingerPrintRecognitionContinueWaveView.setVisibility(8);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }
}
