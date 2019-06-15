package com.oneplus.settings.opfinger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPFingerPrintRecognitionView extends FrameLayout {
    public static final int ANIM_DELAY_TIME = 32;
    public static final int PATH_1 = 0;
    public static final int PATH_2 = 1;
    public static final int PATH_3 = 2;
    public static final int PATH_4 = 3;
    public static final int PATH_5 = 4;
    public static final int PATH_6 = 5;
    public static final int PATH_7 = 6;
    private Context mContext;
    private FrameLayout mFingerPrintView;
    private LayoutInflater mLayoutInflater;
    private SvgView mSvgView_01;
    private SvgView mSvgView_02;
    private SvgView mSvgView_03;
    private SvgView mSvgView_04;
    private SvgView mSvgView_05;
    private SvgView mSvgView_06;
    private SvgView mSvgView_07;
    private SvgView mSvgView_08;
    private SvgView mSvgView_08_for_fod;
    private SvgView mSvgView_09;
    private SvgView mSvgView_10;

    public OPFingerPrintRecognitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public OPFingerPrintRecognitionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPFingerPrintRecognitionView(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mFingerPrintView = (FrameLayout) this.mLayoutInflater.inflate(R.layout.op_finger_input_anim_layout, this);
        setEnrollAnimBgColor("#414141");
        initSvgView(context, this.mFingerPrintView);
    }

    public void setEnrollAnimBgColor(String colorString) {
        Drawable drawable;
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            drawable = this.mContext.getResources().getDrawable(R.drawable.opfinger_anim_color_fod_bg);
        } else {
            drawable = this.mContext.getResources().getDrawable(R.drawable.opfinger_anim_color_bg);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            drawable.setTint(Color.parseColor(colorString));
        }
        FrameLayout frameLayout = this.mFingerPrintView;
    }

    public void initSvgView(Context context, FrameLayout container) {
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_01 = getSvgView(R.raw.opfinger_fod_anim_01, container);
            this.mSvgView_02 = getSvgView(R.raw.opfinger_fod_anim_02, container);
            this.mSvgView_03 = getSvgView(R.raw.opfinger_fod_anim_03, container);
            this.mSvgView_04 = getSvgView(R.raw.opfinger_fod_anim_04, container);
            this.mSvgView_05 = getSvgView(R.raw.opfinger_fod_anim_05, container);
            this.mSvgView_06 = getSvgView(R.raw.opfinger_fod_anim_06, container);
            this.mSvgView_07 = getSvgView(R.raw.opfinger_fod_anim_07, container);
            this.mSvgView_08 = getSvgView(R.raw.opfinger_fod_anim_08_03, container);
            this.mSvgView_08_for_fod = getSvgView(R.raw.opfinger_fod_anim_08_04, container);
            this.mSvgView_09 = getSvgView(R.raw.opfinger_fod_anim_09, container);
            this.mSvgView_10 = getSvgView(R.raw.opfinger_fod_anim_10, container);
        } else if (!OPUtils.isSurportBackFingerprint(context) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_01 = getSvgView(R.raw.opfinger_anim_01, container);
            this.mSvgView_02 = getSvgView(R.raw.opfinger_anim_02, container);
            this.mSvgView_03 = getSvgView(R.raw.opfinger_anim_03, container);
            this.mSvgView_04 = getSvgView(R.raw.opfinger_anim_04, container);
            this.mSvgView_05 = getSvgView(R.raw.opfinger_anim_05, container);
            this.mSvgView_06 = getSvgView(R.raw.opfinger_anim_06, container);
            this.mSvgView_07 = getSvgView(R.raw.opfinger_anim_07, container);
            this.mSvgView_08 = getSvgView(R.raw.opfinger_anim_08, container);
            this.mSvgView_09 = getSvgView(R.raw.opfinger_anim_09, container);
            this.mSvgView_10 = getSvgView(R.raw.opfinger_anim_10, container);
        } else if (OPUtils.isFingerprintNeedEnrollTime16(context)) {
            this.mSvgView_01 = getSvgView(R.raw.opfinger_anim_16_01, container);
            this.mSvgView_02 = getSvgView(R.raw.opfinger_anim_16_02, container);
            this.mSvgView_03 = getSvgView(R.raw.opfinger_anim_16_03, container);
            this.mSvgView_04 = getSvgView(R.raw.opfinger_anim_16_04, container);
            this.mSvgView_05 = getSvgView(R.raw.opfinger_anim_16_05, container);
            this.mSvgView_06 = getSvgView(R.raw.opfinger_anim_16_06, container);
            this.mSvgView_07 = getSvgView(R.raw.opfinger_anim_16_07, container);
            this.mSvgView_08 = getSvgView(R.raw.opfinger_anim_16_08, container);
        } else {
            this.mSvgView_01 = getSvgView(R.raw.opfinger_anim_17801_01, container);
            this.mSvgView_02 = getSvgView(R.raw.opfinger_anim_17801_02, container);
            this.mSvgView_03 = getSvgView(R.raw.opfinger_anim_17801_03, container);
            this.mSvgView_04 = getSvgView(R.raw.opfinger_anim_17801_04, container);
            this.mSvgView_05 = getSvgView(R.raw.opfinger_anim_17801_05, container);
            this.mSvgView_06 = getSvgView(R.raw.opfinger_anim_17801_06, container);
            this.mSvgView_07 = getSvgView(R.raw.opfinger_anim_17801_07, container);
            this.mSvgView_08 = getSvgView(R.raw.opfinger_anim_17801_08, container);
        }
        addView(this.mSvgView_01);
        addView(this.mSvgView_02);
        addView(this.mSvgView_03);
        addView(this.mSvgView_04);
        addView(this.mSvgView_05);
        addView(this.mSvgView_06);
        addView(this.mSvgView_07);
        addView(this.mSvgView_08);
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_08_for_fod);
            addView(this.mSvgView_09);
            addView(this.mSvgView_10);
        } else if (!OPUtils.isSurportBackFingerprint(context) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_09);
            addView(this.mSvgView_10);
        }
        resetWithoutAnimation();
    }

    public void startTouchDownAnim() {
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(0);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scaleAnimation.setDuration(150);
        scaleAnimation.setStartOffset(542);
        animSet.addAnimation(scaleAnimation);
        this.mSvgView_01.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(32);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(510);
        animSet.addAnimation(scalToNormalAnim);
        this.mSvgView_02.startAnimation(animSet);
        this.mSvgView_03.startAnimation(animSet);
        this.mSvgView_04.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(64);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(478);
        animSet.addAnimation(scalToNormalAnim);
        this.mSvgView_05.startAnimation(animSet);
        this.mSvgView_06.startAnimation(animSet);
        this.mSvgView_07.startAnimation(animSet);
        this.mSvgView_08.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(96);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scaleAnimation2.setDuration(150);
        scaleAnimation2.setStartOffset(446);
        animSet.addAnimation(scaleAnimation2);
        this.mSvgView_08_for_fod.startAnimation(animSet);
        this.mSvgView_09.startAnimation(animSet);
        this.mSvgView_10.startAnimation(animSet);
    }

    private SvgView getSvgView(int resId, FrameLayout container) {
        SvgView svgView = (SvgView) this.mLayoutInflater.inflate(R.layout.op_finger_input_item_svg, container, false);
        svgView.setSvgResource(resId);
        return svgView;
    }

    public void resetWithoutAnimation() {
        this.mSvgView_01.resetWithoutAnimation();
        this.mSvgView_02.resetWithoutAnimation();
        this.mSvgView_03.resetWithoutAnimation();
        this.mSvgView_04.resetWithoutAnimation();
        this.mSvgView_05.resetWithoutAnimation();
        this.mSvgView_06.resetWithoutAnimation();
        this.mSvgView_07.resetWithoutAnimation();
        this.mSvgView_08.resetWithoutAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_08_for_fod.resetWithoutAnimation();
            this.mSvgView_09.resetWithoutAnimation();
            this.mSvgView_10.resetWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_09.resetWithoutAnimation();
            this.mSvgView_10.resetWithoutAnimation();
        }
    }

    public void resetWithAnimation() {
        this.mSvgView_01.resetWithAnimation();
        this.mSvgView_02.resetWithAnimation();
        this.mSvgView_03.resetWithAnimation();
        this.mSvgView_04.resetWithAnimation();
        this.mSvgView_05.resetWithAnimation();
        this.mSvgView_06.resetWithAnimation();
        this.mSvgView_07.resetWithAnimation();
        this.mSvgView_08.resetWithAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_08_for_fod.resetWithoutAnimation();
            this.mSvgView_09.resetWithoutAnimation();
            this.mSvgView_10.resetWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_09.resetWithAnimation();
            this.mSvgView_10.resetWithAnimation();
        }
    }

    public void revealWithoutAnimation() {
        this.mSvgView_01.revealWithoutAnimation();
        this.mSvgView_02.revealWithoutAnimation();
        this.mSvgView_03.revealWithoutAnimation();
        this.mSvgView_04.revealWithoutAnimation();
        this.mSvgView_05.revealWithoutAnimation();
        this.mSvgView_06.revealWithoutAnimation();
        this.mSvgView_07.revealWithoutAnimation();
        this.mSvgView_08.revealWithoutAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_08_for_fod.resetWithoutAnimation();
            this.mSvgView_09.resetWithoutAnimation();
            this.mSvgView_10.resetWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_09.revealWithoutAnimation();
            this.mSvgView_10.revealWithoutAnimation();
        }
    }

    public void doRecognition(int percent, boolean success) {
        if (percent >= 16 && percent < 17) {
            this.mSvgView_01.reveal(success);
        } else if (percent >= 17 && percent < 21) {
            this.mSvgView_02.reveal(success);
        } else if (percent >= 21 && percent < 28) {
            this.mSvgView_03.reveal(success);
        } else if (percent >= 29 && percent < 37) {
            this.mSvgView_04.reveal(success);
        } else if (percent >= 37 && percent < 40) {
            this.mSvgView_05.reveal(success);
        } else if (percent >= 40 && percent < 46) {
            this.mSvgView_06.reveal(success);
        } else if (percent >= 46 && percent < 50) {
            this.mSvgView_07.reveal(success);
        } else if (percent >= 50 && percent < 55) {
            this.mSvgView_08.reveal(success);
        } else if (percent >= 55 && percent < 60) {
            this.mSvgView_09.reveal(success);
        } else if (percent >= 60 && percent < 65) {
            this.mSvgView_10.reveal(success);
        }
    }

    public void doRecognitionByCount(int count, int percent, boolean success) {
        switch (count) {
            case 1:
                this.mSvgView_01.reveal(success);
                return;
            case 2:
                this.mSvgView_02.reveal(success);
                return;
            case 3:
                this.mSvgView_03.reveal(success);
                return;
            case 4:
                this.mSvgView_04.reveal(success);
                return;
            case 5:
                this.mSvgView_05.reveal(success);
                return;
            case 6:
                this.mSvgView_06.reveal(success);
                return;
            case 7:
                this.mSvgView_07.reveal(success);
                return;
            case 8:
                this.mSvgView_08.reveal(success);
                if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext) && this.mSvgView_08_for_fod != null) {
                    this.mSvgView_08_for_fod.reveal(success);
                    return;
                }
                return;
            case 9:
                if (this.mSvgView_09 != null) {
                    this.mSvgView_09.reveal(success);
                    return;
                }
                return;
            case 10:
                if (this.mSvgView_09 != null) {
                    this.mSvgView_10.reveal(success);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setBackGround(int resId) {
        if (this.mFingerPrintView != null) {
            this.mFingerPrintView.setBackgroundResource(resId);
        }
    }
}
