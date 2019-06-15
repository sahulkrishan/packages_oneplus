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

public class OPFingerPrintRecognitionContinueWaveView extends FrameLayout {
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
    private SvgView mSvgView_11_01;
    private SvgView mSvgView_11_05;
    private SvgView mSvgView_12_04;
    private SvgView mSvgView_13_03;
    private SvgView mSvgView_13_04;
    private SvgView mSvgView_14_02;
    private SvgView mSvgView_14_03;
    private SvgView mSvgView_15_02;
    private SvgView mSvgView_15_07;
    private SvgView mSvgView_16_03;
    private SvgView mSvgView_16_06;
    private SvgView mSvgView_17_05;
    private SvgView mSvgView_17_06;
    private SvgView mSvgView_18_05;
    private SvgView mSvgView_18_06;
    private SvgView mSvgView_19_05;
    private SvgView mSvgView_19_07;
    private SvgView mSvgView_20_07;

    public OPFingerPrintRecognitionContinueWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public OPFingerPrintRecognitionContinueWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPFingerPrintRecognitionContinueWaveView(Context context) {
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
            drawable = this.mContext.getResources().getDrawable(R.drawable.opfinger_anim_color_fod_bg_2);
        } else {
            drawable = this.mContext.getResources().getDrawable(R.drawable.opfinger_anim_color_bg_2);
        }
        if (OPUtils.isSupportCustomFingerprint()) {
            drawable.setTint(Color.parseColor(colorString));
        }
        FrameLayout frameLayout = this.mFingerPrintView;
    }

    public void initSvgView(Context context, FrameLayout container) {
        this.mSvgView_11_01 = getSvgView(R.raw.opfinger_fod_anim_11_01, container);
        this.mSvgView_11_05 = getSvgView(R.raw.opfinger_fod_anim_11_05, container);
        this.mSvgView_12_04 = getSvgView(R.raw.opfinger_fod_anim_12_04, container);
        this.mSvgView_13_03 = getSvgView(R.raw.opfinger_fod_anim_13_03, container);
        this.mSvgView_13_04 = getSvgView(R.raw.opfinger_fod_anim_13_04, container);
        this.mSvgView_14_02 = getSvgView(R.raw.opfinger_fod_anim_14_02, container);
        this.mSvgView_14_03 = getSvgView(R.raw.opfinger_fod_anim_14_03, container);
        this.mSvgView_15_02 = getSvgView(R.raw.opfinger_fod_anim_15_02, container);
        this.mSvgView_15_07 = getSvgView(R.raw.opfinger_fod_anim_15_07, container);
        this.mSvgView_16_03 = getSvgView(R.raw.opfinger_fod_anim_16_03, container);
        this.mSvgView_16_06 = getSvgView(R.raw.opfinger_fod_anim_16_06, container);
        this.mSvgView_17_05 = getSvgView(R.raw.opfinger_fod_anim_17_05, container);
        this.mSvgView_17_06 = getSvgView(R.raw.opfinger_fod_anim_17_06, container);
        this.mSvgView_18_05 = getSvgView(R.raw.opfinger_fod_anim_18_05, container);
        this.mSvgView_18_06 = getSvgView(R.raw.opfinger_fod_anim_18_06, container);
        this.mSvgView_19_05 = getSvgView(R.raw.opfinger_fod_anim_19_05, container);
        this.mSvgView_19_07 = getSvgView(R.raw.opfinger_fod_anim_19_07, container);
        this.mSvgView_20_07 = getSvgView(R.raw.opfinger_fod_anim_20_07, container);
        addView(this.mSvgView_11_01);
        addView(this.mSvgView_11_05);
        addView(this.mSvgView_12_04);
        addView(this.mSvgView_13_03);
        addView(this.mSvgView_13_04);
        addView(this.mSvgView_14_02);
        addView(this.mSvgView_14_03);
        addView(this.mSvgView_15_02);
        addView(this.mSvgView_15_07);
        addView(this.mSvgView_16_03);
        addView(this.mSvgView_16_06);
        addView(this.mSvgView_17_05);
        addView(this.mSvgView_17_06);
        addView(this.mSvgView_18_05);
        addView(this.mSvgView_18_06);
        addView(this.mSvgView_19_05);
        addView(this.mSvgView_19_07);
        addView(this.mSvgView_20_07);
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
        this.mSvgView_11_01.startAnimation(animSet);
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
        this.mSvgView_14_02.startAnimation(animSet);
        this.mSvgView_15_02.startAnimation(animSet);
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
        this.mSvgView_14_03.startAnimation(animSet);
        this.mSvgView_16_03.startAnimation(animSet);
        this.mSvgView_13_03.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(96);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(446);
        animSet.addAnimation(scalToNormalAnim);
        this.mSvgView_12_04.startAnimation(animSet);
        this.mSvgView_13_04.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(128);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(414);
        animSet.addAnimation(scalToNormalAnim);
        this.mSvgView_11_05.startAnimation(animSet);
        this.mSvgView_17_05.startAnimation(animSet);
        this.mSvgView_18_05.startAnimation(animSet);
        this.mSvgView_19_05.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(160);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(382);
        animSet.addAnimation(scalToNormalAnim);
        this.mSvgView_16_06.startAnimation(animSet);
        this.mSvgView_17_06.startAnimation(animSet);
        this.mSvgView_18_06.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setStartOffset(192);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scaleAnimation2.setDuration(150);
        scaleAnimation2.setStartOffset(350);
        animSet.addAnimation(scaleAnimation2);
        this.mSvgView_15_07.startAnimation(animSet);
        this.mSvgView_19_07.startAnimation(animSet);
        this.mSvgView_20_07.startAnimation(animSet);
    }

    private SvgView getSvgView(int resId, FrameLayout container) {
        SvgView svgView = (SvgView) this.mLayoutInflater.inflate(R.layout.op_finger_input_item_svg, container, false);
        svgView.setSvgResource(resId);
        return svgView;
    }

    public void revealWithoutAnimation() {
        this.mSvgView_11_01.revealWithoutAnimation();
        this.mSvgView_11_05.revealWithoutAnimation();
        this.mSvgView_12_04.revealWithoutAnimation();
        this.mSvgView_13_03.revealWithoutAnimation();
        this.mSvgView_13_04.revealWithoutAnimation();
        this.mSvgView_14_02.revealWithoutAnimation();
        this.mSvgView_14_03.revealWithoutAnimation();
        this.mSvgView_15_02.revealWithoutAnimation();
        this.mSvgView_15_07.revealWithoutAnimation();
        this.mSvgView_16_03.revealWithoutAnimation();
        this.mSvgView_16_06.revealWithoutAnimation();
        this.mSvgView_17_05.revealWithoutAnimation();
        this.mSvgView_17_06.revealWithoutAnimation();
        this.mSvgView_18_05.revealWithoutAnimation();
        this.mSvgView_18_06.revealWithoutAnimation();
        this.mSvgView_19_05.revealWithoutAnimation();
        this.mSvgView_19_07.revealWithoutAnimation();
        this.mSvgView_20_07.revealWithoutAnimation();
    }

    public void resetWithoutAnimation() {
        this.mSvgView_11_01.resetWithoutAnimation();
        this.mSvgView_11_05.resetWithoutAnimation();
        this.mSvgView_12_04.resetWithoutAnimation();
        this.mSvgView_13_03.resetWithoutAnimation();
        this.mSvgView_13_04.resetWithoutAnimation();
        this.mSvgView_14_02.resetWithoutAnimation();
        this.mSvgView_14_03.resetWithoutAnimation();
        this.mSvgView_15_02.resetWithoutAnimation();
        this.mSvgView_15_07.resetWithoutAnimation();
        this.mSvgView_16_03.resetWithoutAnimation();
        this.mSvgView_16_06.resetWithoutAnimation();
        this.mSvgView_17_05.resetWithoutAnimation();
        this.mSvgView_17_06.resetWithoutAnimation();
        this.mSvgView_18_05.resetWithoutAnimation();
        this.mSvgView_18_06.resetWithoutAnimation();
        this.mSvgView_19_05.resetWithoutAnimation();
        this.mSvgView_19_07.resetWithoutAnimation();
        this.mSvgView_20_07.resetWithoutAnimation();
    }

    public void resetWithAnimation() {
        this.mSvgView_11_01.resetWithAnimation();
        this.mSvgView_11_05.resetWithAnimation();
        this.mSvgView_12_04.resetWithAnimation();
        this.mSvgView_13_03.resetWithAnimation();
        this.mSvgView_13_04.resetWithAnimation();
        this.mSvgView_14_02.resetWithAnimation();
        this.mSvgView_14_03.resetWithAnimation();
        this.mSvgView_15_02.resetWithAnimation();
        this.mSvgView_15_07.resetWithAnimation();
        this.mSvgView_16_03.resetWithAnimation();
        this.mSvgView_16_06.resetWithAnimation();
        this.mSvgView_17_05.resetWithAnimation();
        this.mSvgView_17_06.resetWithAnimation();
        this.mSvgView_18_05.resetWithAnimation();
        this.mSvgView_18_06.resetWithAnimation();
        this.mSvgView_19_05.resetWithAnimation();
        this.mSvgView_19_07.resetWithAnimation();
        this.mSvgView_20_07.resetWithAnimation();
    }

    public void doRecognitionByCount(int count, int percent, boolean success) {
        if (OPUtils.getFingerprintScaleAnimStep(this.mContext) == 8) {
            count += 2;
        }
        switch (count) {
            case 11:
                this.mSvgView_11_01.reveal(success);
                this.mSvgView_11_05.reveal(success);
                return;
            case 12:
                this.mSvgView_12_04.reveal(success);
                return;
            case 13:
                this.mSvgView_13_03.reveal(success);
                this.mSvgView_13_04.reveal(success);
                return;
            case 14:
                this.mSvgView_14_02.reveal(success);
                this.mSvgView_14_03.reveal(success);
                return;
            case 15:
                this.mSvgView_15_02.reveal(success);
                this.mSvgView_15_07.reveal(success);
                return;
            case 16:
                this.mSvgView_16_03.reveal(success);
                this.mSvgView_16_06.reveal(success);
                return;
            case 17:
                this.mSvgView_17_05.reveal(success);
                this.mSvgView_17_06.reveal(success);
                return;
            case 18:
                this.mSvgView_18_05.reveal(success);
                this.mSvgView_18_06.reveal(success);
                return;
            case 19:
                this.mSvgView_19_05.reveal(success);
                this.mSvgView_19_07.reveal(success);
                return;
            case 20:
                this.mSvgView_20_07.reveal(success);
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
