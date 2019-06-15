package com.oneplus.settings.opfinger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;

public class OPFingerPrintInputViewCategory extends PreferenceCategory {
    private LayoutInflater inflater;
    private Context mContext;
    private Handler mHandler = new Handler();
    private ImageView mImage;
    private int mLayoutResId = R.layout.op_fingerprint_input_category;
    private Button mOPFingerInputCompletedComfirmBtn;
    private TextView mOPFingerInputTipsSubTitle;
    private TextView mOPFingerInputTipsTitle;
    private OPFingerPrintRecognitionContinueView mOPFingerPrintRecognitionContinueView;
    private OPFingerPrintRecognitionView mOPFingerPrintRecognitionView;
    public OnOPFingerComfirmListener mOnOPFingerComfirmListener;
    private int mPercent = 0;

    public interface OnOPFingerComfirmListener {
        void onOPFingerComfirmClick();
    }

    public OPFingerPrintInputViewCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPFingerPrintInputViewCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFingerPrintInputViewCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mOPFingerPrintRecognitionView = (OPFingerPrintRecognitionView) view.findViewById(R.id.op_finger_recognition_view);
        this.mOPFingerPrintRecognitionContinueView = (OPFingerPrintRecognitionContinueView) view.findViewById(R.id.op_finger_recognition_continue_view);
        this.mOPFingerInputTipsTitle = (TextView) view.findViewById(R.id.opfinger_input_tips_title_tv);
        this.mOPFingerInputTipsSubTitle = (TextView) view.findViewById(R.id.opfinger_input_tips_subtitle_tv);
        this.mOPFingerInputCompletedComfirmBtn = (Button) view.findViewById(R.id.opfinger_input_completed_comfirm_btn);
        this.mOPFingerInputCompletedComfirmBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (OPFingerPrintInputViewCategory.this.mOnOPFingerComfirmListener != null) {
                    OPFingerPrintInputViewCategory.this.mOnOPFingerComfirmListener.onOPFingerComfirmClick();
                }
            }
        });
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
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(0);
    }

    public void setTipsContinueContent() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_recognize_continue_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
    }

    public void showContinueView() {
        this.mOPFingerPrintRecognitionContinueView.setVisibility(0);
        AnimationSet animSet = new AnimationSet(true);
        AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        alphaAnim.setDuration(500);
        animSet.addAnimation(alphaAnim);
        ScaleAnimation scaleAnim = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnim.setDuration(500);
        animSet.addAnimation(scaleAnim);
        this.mOPFingerPrintRecognitionContinueView.setAnimation(animSet);
        animSet.start();
    }

    public void doRecognition(int count, int percent, boolean success) {
        if (count <= 13) {
            if (this.mOPFingerPrintRecognitionView != null) {
                this.mOPFingerPrintRecognitionView.doRecognitionByCount(count, percent, success);
            }
        } else if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.doRecognitionByCount(count, percent, success);
        }
    }

    public void resetTextAndBtn() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_setting_tips_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_setting_tips_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
    }

    public void enrollFailed() {
        this.mOPFingerInputTipsTitle.setText(R.string.oneplus_opfinger_input_failed_title);
        this.mOPFingerInputTipsSubTitle.setText(R.string.oneplus_opfinger_input_failed_sub);
        this.mOPFingerInputCompletedComfirmBtn.setVisibility(8);
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }

    public void revealWithoutAnimation() {
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.revealWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.revealWithoutAnimation();
        }
    }

    public void resetWithoutAnimation() {
        resetTextAndBtn();
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithoutAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }

    public void resetWithAnimation() {
        resetTextAndBtn();
        if (this.mOPFingerPrintRecognitionView != null) {
            this.mOPFingerPrintRecognitionView.resetWithAnimation();
        }
        if (this.mOPFingerPrintRecognitionContinueView != null) {
            this.mOPFingerPrintRecognitionContinueView.resetWithoutAnimation();
            this.mOPFingerPrintRecognitionContinueView.setVisibility(8);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(this.mLayoutResId, container, false);
    }
}
