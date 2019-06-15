package com.oneplus.settings.opfinger;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.settings.R;

public class OPFingerPrintFodBgAnimView extends FrameLayout {
    public static final int ANIM_DELAY_TIME = 32;
    public static final int PATH_1 = 0;
    public static final int PATH_2 = 1;
    public static final int PATH_3 = 2;
    public static final int PATH_4 = 3;
    public static final int PATH_5 = 4;
    public static final int PATH_6 = 5;
    public static final int PATH_7 = 6;
    private Context mContext;
    private FrameLayout mFodBgView;
    private LayoutInflater mLayoutInflater;
    private ImageView mPath_1;
    private ImageView mPath_10;
    private ImageView mPath_11;
    private ImageView mPath_2;
    private ImageView mPath_3;
    private ImageView mPath_4;
    private ImageView mPath_5;
    private ImageView mPath_6;
    private ImageView mPath_7;
    private ImageView mPath_8;
    private ImageView mPath_9;

    public OPFingerPrintFodBgAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public OPFingerPrintFodBgAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPFingerPrintFodBgAnimView(Context context) {
        super(context);
        initView(context);
    }

    public void initView(Context context) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mFodBgView = (FrameLayout) this.mLayoutInflater.inflate(R.layout.op_finger_enroll_fod_bg_anim_view, this);
        this.mPath_1 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_01);
        this.mPath_2 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_02);
        this.mPath_3 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_03);
        this.mPath_4 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_04);
        this.mPath_5 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_05);
        this.mPath_6 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_06);
        this.mPath_7 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_07);
        this.mPath_8 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_08);
        this.mPath_9 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_09);
        this.mPath_10 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_10);
        this.mPath_11 = (ImageView) this.mFodBgView.findViewById(R.id.opfinger_fod_anim_bg_11);
        initBgAnimView(context, this.mFodBgView);
    }

    public void initBgAnimView(Context context, FrameLayout container) {
        setCenterVisible(true);
    }

    public static String autoGenericCode(int code, int num) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%0");
        stringBuilder.append(num);
        stringBuilder.append("d");
        return String.format(stringBuilder.toString(), new Object[]{Integer.valueOf(code)});
    }

    public void setCenterVisible(boolean visible) {
        int i = 0;
        this.mPath_1.setVisibility(visible ? 0 : 8);
        this.mPath_2.setVisibility(visible ? 0 : 8);
        this.mPath_3.setVisibility(visible ? 0 : 8);
        ImageView imageView = this.mPath_4;
        if (!visible) {
            i = 8;
        }
        imageView.setVisibility(i);
        this.mPath_5.setVisibility(8);
        this.mPath_6.setVisibility(8);
        this.mPath_7.setVisibility(8);
        this.mPath_8.setVisibility(8);
        this.mPath_9.setVisibility(8);
        this.mPath_10.setVisibility(8);
        this.mPath_11.setVisibility(8);
    }

    public void setEdgeVisible(boolean visible) {
        int i = 8;
        this.mPath_1.setVisibility(8);
        this.mPath_2.setVisibility(8);
        this.mPath_3.setVisibility(8);
        this.mPath_4.setVisibility(8);
        this.mPath_5.setVisibility(visible ? 0 : 8);
        this.mPath_6.setVisibility(visible ? 0 : 8);
        this.mPath_7.setVisibility(visible ? 0 : 8);
        this.mPath_8.setVisibility(visible ? 0 : 8);
        this.mPath_9.setVisibility(visible ? 0 : 8);
        this.mPath_10.setVisibility(visible ? 0 : 8);
        ImageView imageView = this.mPath_11;
        if (visible) {
            i = 0;
        }
        imageView.setVisibility(i);
    }

    public void hide(boolean visible) {
        if (this.mFodBgView != null) {
            this.mFodBgView.setVisibility(8);
        }
    }

    public void startTouchDownAnim() {
        AnimationSet animSet = new AnimationSet(true);
        ScaleAnimation scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(0);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scaleAnimation.setFillAfter(false);
        scaleAnimation.setDuration(150);
        scaleAnimation.setStartOffset(542);
        animSet.addAnimation(scaleAnimation);
        this.mPath_1.startAnimation(animSet);
        this.mPath_5.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(32);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setFillAfter(false);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(510);
        animSet.addAnimation(scalToNormalAnim);
        this.mPath_2.startAnimation(animSet);
        this.mPath_6.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(64);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setFillAfter(false);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(478);
        animSet.addAnimation(scalToNormalAnim);
        this.mPath_3.startAnimation(animSet);
        this.mPath_7.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(96);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setFillAfter(false);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(446);
        animSet.addAnimation(scalToNormalAnim);
        this.mPath_4.startAnimation(animSet);
        this.mPath_8.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(128);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setFillAfter(false);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(414);
        animSet.addAnimation(scalToNormalAnim);
        this.mPath_9.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(160);
        animSet.addAnimation(scalSmallAnim);
        scalToNormalAnim = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scalToNormalAnim.setFillAfter(false);
        scalToNormalAnim.setDuration(150);
        scalToNormalAnim.setStartOffset(382);
        animSet.addAnimation(scalToNormalAnim);
        this.mPath_10.startAnimation(animSet);
        animSet = new AnimationSet(true);
        scalSmallAnim = new ScaleAnimation(1.0f, 0.94f, 1.0f, 0.94f, 2, 0.5f, 2, 0.5f);
        scalSmallAnim.setFillAfter(true);
        scalSmallAnim.setDuration(300);
        scalSmallAnim.setStartOffset(192);
        animSet.addAnimation(scalSmallAnim);
        ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.0f, 1.0638298f, 1.0f, 1.0638298f, 2, 0.5f, 2, 0.5f);
        scaleAnimation2.setFillAfter(false);
        scaleAnimation2.setDuration(150);
        scaleAnimation2.setStartOffset(350);
        animSet.addAnimation(scaleAnimation2);
        this.mPath_11.startAnimation(animSet);
    }
}
