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

public class OPFingerPrintRecognitionContinueView extends FrameLayout {
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
    private SvgView mSvgView_11;
    private SvgView mSvgView_12;
    private SvgView mSvgView_13;
    private SvgView mSvgView_14;
    private SvgView mSvgView_15;
    private SvgView mSvgView_16;
    private SvgView mSvgView_17;
    private SvgView mSvgView_18;
    private SvgView mSvgView_19;
    private SvgView mSvgView_20;

    public OPFingerPrintRecognitionContinueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public OPFingerPrintRecognitionContinueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPFingerPrintRecognitionContinueView(Context context) {
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
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_11 = getSvgView(R.raw.opfinger_fod_anim_11, container);
            this.mSvgView_12 = getSvgView(R.raw.opfinger_fod_anim_12, container);
            this.mSvgView_13 = getSvgView(R.raw.opfinger_fod_anim_13, container);
            this.mSvgView_14 = getSvgView(R.raw.opfinger_fod_anim_14, container);
            this.mSvgView_15 = getSvgView(R.raw.opfinger_fod_anim_15, container);
            this.mSvgView_16 = getSvgView(R.raw.opfinger_fod_anim_16, container);
            this.mSvgView_17 = getSvgView(R.raw.opfinger_fod_anim_17, container);
            this.mSvgView_18 = getSvgView(R.raw.opfinger_fod_anim_18, container);
            this.mSvgView_19 = getSvgView(R.raw.opfinger_fod_anim_19, container);
            this.mSvgView_20 = getSvgView(R.raw.opfinger_fod_anim_20, container);
        } else if (OPUtils.isSupportCustomFingerprint() && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_11 = getSvgView(R.raw.opfinger_anim_16_09, container);
            this.mSvgView_12 = getSvgView(R.raw.opfinger_anim_16_10, container);
            this.mSvgView_13 = getSvgView(R.raw.opfinger_anim_16_11, container);
            this.mSvgView_14 = getSvgView(R.raw.opfinger_anim_16_12, container);
            this.mSvgView_15 = getSvgView(R.raw.opfinger_anim_17_13, container);
            this.mSvgView_16 = getSvgView(R.raw.opfinger_anim_17_14, container);
            this.mSvgView_17 = getSvgView(R.raw.opfinger_anim_17_15, container);
            this.mSvgView_18 = getSvgView(R.raw.opfinger_anim_17_16, container);
            this.mSvgView_19 = getSvgView(R.raw.opfinger_anim_17_17, container);
        } else if (!OPUtils.isSurportBackFingerprint(context) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_11 = getSvgView(R.raw.opfinger_anim_11, container);
            this.mSvgView_12 = getSvgView(R.raw.opfinger_anim_12, container);
            this.mSvgView_13 = getSvgView(R.raw.opfinger_anim_13, container);
            this.mSvgView_14 = getSvgView(R.raw.opfinger_anim_14, container);
            this.mSvgView_15 = getSvgView(R.raw.opfinger_anim_15, container);
            this.mSvgView_16 = getSvgView(R.raw.opfinger_anim_16, container);
            this.mSvgView_17 = getSvgView(R.raw.opfinger_anim_17, container);
            this.mSvgView_18 = getSvgView(R.raw.opfinger_anim_18, container);
            this.mSvgView_19 = getSvgView(R.raw.opfinger_anim_19, container);
            this.mSvgView_20 = getSvgView(R.raw.opfinger_anim_20, container);
        } else if (OPUtils.isFingerprintNeedEnrollTime16(context)) {
            this.mSvgView_11 = getSvgView(R.raw.opfinger_anim_16_09, container);
            this.mSvgView_12 = getSvgView(R.raw.opfinger_anim_16_10, container);
            this.mSvgView_13 = getSvgView(R.raw.opfinger_anim_16_11, container);
            this.mSvgView_14 = getSvgView(R.raw.opfinger_anim_16_12, container);
            this.mSvgView_15 = getSvgView(R.raw.opfinger_anim_16_13, container);
            this.mSvgView_16 = getSvgView(R.raw.opfinger_anim_16_14, container);
            this.mSvgView_17 = getSvgView(R.raw.opfinger_anim_16_15, container);
            this.mSvgView_18 = getSvgView(R.raw.opfinger_anim_16_16, container);
        } else {
            this.mSvgView_11 = getSvgView(R.raw.opfinger_anim_17801_09, container);
            this.mSvgView_12 = getSvgView(R.raw.opfinger_anim_17801_10, container);
            this.mSvgView_13 = getSvgView(R.raw.opfinger_anim_17801_11, container);
            this.mSvgView_14 = getSvgView(R.raw.opfinger_anim_17801_12, container);
        }
        addView(this.mSvgView_11);
        addView(this.mSvgView_12);
        addView(this.mSvgView_13);
        addView(this.mSvgView_14);
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_15);
            addView(this.mSvgView_16);
            addView(this.mSvgView_17);
            addView(this.mSvgView_18);
            addView(this.mSvgView_19);
            addView(this.mSvgView_20);
        } else if (OPUtils.isSupportCustomFingerprint() && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_15);
            addView(this.mSvgView_16);
            addView(this.mSvgView_17);
            addView(this.mSvgView_18);
            addView(this.mSvgView_19);
        } else if (OPUtils.isFingerprintNeedEnrollTime16(context) && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_15);
            addView(this.mSvgView_16);
            addView(this.mSvgView_17);
            addView(this.mSvgView_18);
        } else if (!OPUtils.isSurportBackFingerprint(context) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            addView(this.mSvgView_15);
            addView(this.mSvgView_16);
            addView(this.mSvgView_17);
            addView(this.mSvgView_18);
            addView(this.mSvgView_19);
            addView(this.mSvgView_20);
        }
    }

    public void startTouchDownAnim() {
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        ScaleAnimation scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setDuration(300);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(300);
        animSet.addAnimation(scalSmallAnim);
        animSet.addAnimation(scalToNormalAnim);
        animSet.setStartOffset(0);
        this.mSvgView_11.startAnimation(animSet);
        this.mSvgView_12.startAnimation(animSet);
        animSet.setStartOffset(32);
        this.mSvgView_14.startAnimation(animSet);
        this.mSvgView_15.startAnimation(animSet);
        animSet.setStartOffset(64);
        this.mSvgView_13.startAnimation(animSet);
        this.mSvgView_16.startAnimation(animSet);
        animSet.setStartOffset(96);
        animSet.setStartOffset(128);
        this.mSvgView_17.startAnimation(animSet);
        this.mSvgView_18.startAnimation(animSet);
        animSet.setStartOffset(160);
        animSet.setStartOffset(192);
        this.mSvgView_19.startAnimation(animSet);
        this.mSvgView_20.startAnimation(animSet);
    }

    private SvgView getSvgView(int resId, FrameLayout container) {
        SvgView svgView = (SvgView) this.mLayoutInflater.inflate(R.layout.op_finger_input_item_svg, container, false);
        svgView.setSvgResource(resId);
        return svgView;
    }

    public void revealWithoutAnimation() {
        this.mSvgView_11.revealWithoutAnimation();
        this.mSvgView_12.revealWithoutAnimation();
        this.mSvgView_13.revealWithoutAnimation();
        this.mSvgView_14.revealWithoutAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
            this.mSvgView_19.revealWithoutAnimation();
            this.mSvgView_20.revealWithoutAnimation();
        } else if (OPUtils.isSupportCustomFingerprint() && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
            this.mSvgView_19.revealWithoutAnimation();
        } else if (OPUtils.isFingerprintNeedEnrollTime16(this.mContext) && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
            this.mSvgView_19.revealWithoutAnimation();
            this.mSvgView_20.revealWithoutAnimation();
        }
    }

    public void resetWithoutAnimation() {
        this.mSvgView_11.resetWithoutAnimation();
        this.mSvgView_12.resetWithoutAnimation();
        this.mSvgView_13.resetWithoutAnimation();
        this.mSvgView_14.resetWithoutAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.resetWithoutAnimation();
            this.mSvgView_16.resetWithoutAnimation();
            this.mSvgView_17.resetWithoutAnimation();
            this.mSvgView_18.resetWithoutAnimation();
            this.mSvgView_19.resetWithoutAnimation();
            this.mSvgView_20.resetWithoutAnimation();
        } else if (OPUtils.isSupportCustomFingerprint() && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
            this.mSvgView_19.resetWithoutAnimation();
        } else if (OPUtils.isFingerprintNeedEnrollTime16(this.mContext) && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.resetWithoutAnimation();
            this.mSvgView_16.resetWithoutAnimation();
            this.mSvgView_17.resetWithoutAnimation();
            this.mSvgView_18.resetWithoutAnimation();
            this.mSvgView_19.resetWithoutAnimation();
            this.mSvgView_20.resetWithoutAnimation();
        }
    }

    public void resetWithAnimation() {
        this.mSvgView_11.resetWithAnimation();
        this.mSvgView_12.resetWithAnimation();
        this.mSvgView_13.resetWithAnimation();
        this.mSvgView_14.resetWithAnimation();
        if (OPUtils.isSupportCustomFingerprint() && OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.resetWithAnimation();
            this.mSvgView_16.resetWithAnimation();
            this.mSvgView_17.resetWithAnimation();
            this.mSvgView_18.resetWithAnimation();
            this.mSvgView_19.resetWithAnimation();
            this.mSvgView_20.resetWithAnimation();
        } else if (OPUtils.isSupportCustomFingerprint() && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
            this.mSvgView_19.revealWithoutAnimation();
        } else if (OPUtils.isFingerprintNeedEnrollTime16(this.mContext) && !OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.revealWithoutAnimation();
            this.mSvgView_16.revealWithoutAnimation();
            this.mSvgView_17.revealWithoutAnimation();
            this.mSvgView_18.revealWithoutAnimation();
        } else if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isFingerprintNeedEnrollTime20(this.mContext)) {
            this.mSvgView_15.resetWithAnimation();
            this.mSvgView_16.resetWithAnimation();
            this.mSvgView_17.resetWithAnimation();
            this.mSvgView_18.resetWithAnimation();
            this.mSvgView_19.resetWithAnimation();
            this.mSvgView_20.resetWithAnimation();
        }
    }

    public void doRecognition(int percent, boolean success) {
        if (percent >= 65 && percent < 70) {
            this.mSvgView_11.reveal(success);
        } else if (percent >= 70 && percent < 75) {
            this.mSvgView_12.reveal(success);
        } else if (percent >= 75 && percent < 80) {
            this.mSvgView_13.reveal(success);
        } else if (percent >= 80 && percent < 85) {
            this.mSvgView_14.reveal(success);
        } else if (percent >= 85 && percent < 90) {
            this.mSvgView_15.reveal(success);
        } else if (percent >= 90 && percent < 95) {
            this.mSvgView_16.reveal(success);
        } else if (percent >= 95 && percent < 100) {
            this.mSvgView_17.reveal(success);
        } else if (percent == 100) {
            this.mSvgView_18.reveal(success);
        }
    }

    public void doRecognitionByCount(int count, int percent, boolean success) {
        if (OPUtils.getFingerprintScaleAnimStep(this.mContext) == 8) {
            count += 2;
        }
        switch (count) {
            case 11:
                this.mSvgView_11.reveal(success);
                return;
            case 12:
                this.mSvgView_12.reveal(success);
                return;
            case 13:
                this.mSvgView_13.reveal(success);
                return;
            case 14:
                this.mSvgView_14.reveal(success);
                return;
            case 15:
                if (this.mSvgView_15 != null) {
                    this.mSvgView_15.reveal(success);
                    return;
                }
                return;
            case 16:
                if (this.mSvgView_16 != null) {
                    this.mSvgView_16.reveal(success);
                    return;
                }
                return;
            case 17:
                if (this.mSvgView_17 != null) {
                    this.mSvgView_17.reveal(success);
                    return;
                }
                return;
            case 18:
                if (percent >= 100) {
                    if (this.mSvgView_18 != null) {
                        this.mSvgView_18.reveal(success);
                    }
                    if (this.mSvgView_19 != null) {
                        this.mSvgView_19.reveal(success);
                    }
                    if (this.mSvgView_20 != null) {
                        this.mSvgView_20.reveal(success);
                        return;
                    }
                    return;
                } else if (this.mSvgView_18 != null) {
                    this.mSvgView_18.reveal(success);
                    return;
                } else {
                    return;
                }
            case 19:
                if (percent >= 100) {
                    if (this.mSvgView_19 != null) {
                        this.mSvgView_19.reveal(success);
                    }
                    if (this.mSvgView_20 != null) {
                        this.mSvgView_20.reveal(success);
                        return;
                    }
                    return;
                } else if (this.mSvgView_19 != null) {
                    this.mSvgView_19.reveal(success);
                    return;
                } else {
                    return;
                }
            case 20:
                if (this.mSvgView_20 != null) {
                    this.mSvgView_20.reveal(success);
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
