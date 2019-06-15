package com.oneplus.settings.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPFaceUnlockModeLottieViewCategory extends Preference {
    private static final String ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE = "oneplus_face_unlock_powerkey_recognize_enable";
    private static final int SWIPE_UP_MODE = 0;
    private static final int USE_POWER_BUTTON_MODE = 1;
    private LayoutInflater inflater;
    private ContentResolver mContentResolver;
    private Context mContext;
    private boolean mHasInited = false;
    private int mLayoutResId = R.layout.op_single_lottie_instructions_category;
    private LottieAnimationView mLottieAnim;
    private ViewPager mViewPager;
    private View mViewPagerContainer;

    public OPFaceUnlockModeLottieViewCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPFaceUnlockModeLottieViewCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFaceUnlockModeLottieViewCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mLottieAnim = (LottieAnimationView) view.findViewById(R.id.op_single_lottie_view);
        view.setDividerAllowedBelow(false);
        this.mHasInited = true;
        startAnim();
    }

    public int getUnlockMode() {
        return System.getInt(this.mContext.getContentResolver(), "oneplus_face_unlock_powerkey_recognize_enable", 0);
    }

    private void setAnimationResource() {
        if (this.mHasInited) {
            if (getUnlockMode() == 0) {
                if (OPUtils.isBlackModeOn(this.mContentResolver)) {
                    this.mLottieAnim.setAnimation("op_face_unlock_by_swipe_up_dark.json");
                } else {
                    this.mLottieAnim.setAnimation("op_face_unlock_by_swipe_up_light.json");
                }
            } else if (OPUtils.isBlackModeOn(this.mContentResolver)) {
                this.mLottieAnim.setAnimation("op_face_unlock_by_use_power_key_dark.json");
            } else {
                this.mLottieAnim.setAnimation("op_face_unlock_by_use_power_key_light.json");
            }
        }
    }

    public void startAnim() {
        if (this.mHasInited) {
            setViewType(getUnlockMode());
        }
    }

    public void setViewType(int type) {
        if (this.mHasInited) {
            stopAnim();
            setAnimationResource();
            this.mLottieAnim.playAnimation();
        }
    }

    public void stopAnim() {
        if (this.mHasInited) {
            this.mLottieAnim.cancelAnimation();
        }
    }

    public void releaseAnim() {
        if (this.mHasInited) {
            this.mLottieAnim.cancelAnimation();
            this.mLottieAnim = null;
        }
    }
}
