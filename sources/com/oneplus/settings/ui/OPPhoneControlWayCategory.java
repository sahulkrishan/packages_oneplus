package com.oneplus.settings.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;

public class OPPhoneControlWayCategory extends Preference {
    private static final int BACK_ANIM_INDEX = 2;
    private static final int HOME_ANIM_INDEX = 0;
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    private static final int LAND_ANIM_INDEX = 4;
    private static final int PREVIOUS_APP_ANIM_INDEX = 3;
    private static final int RECENT_ANIM_INDEX = 1;
    private static final int TYPE_ALWAYS_SHOW_NAVIGATION_BAR = 1;
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;
    private static final int TYPE_HIDE_NAVIGATION_BAR = 2;
    private LayoutInflater inflater;
    private LottieAnimationView mAlwaysShowNavigationBarAnim;
    private LottieAnimationView mBackAnim;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrIndex = 0;
    private Drawable mCurrentDrawable;
    private boolean mHasInited = false;
    private LottieAnimationView mHideNavigationBarAnim;
    private LottieAnimationView mHomeAnim;
    private LottieAnimationView mLandAnim;
    private int mLayoutResId = R.layout.op_phone_control_way_instructions_category;
    private Drawable mNormalDrawable;
    private ImageView mPage0;
    private ImageView mPage1;
    private ImageView mPage2;
    private ImageView mPage3;
    private ImageView mPage4;
    private LottieAnimationView mPreviousAppAnim;
    private LottieAnimationView mRecentAnim;
    private ViewPager mViewPager;
    private View mViewPagerContainer;

    public class MyOnPageChangeListener implements OnPageChangeListener {
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:
                    OPPhoneControlWayCategory.this.mPage0.setImageDrawable(OPPhoneControlWayCategory.this.mCurrentDrawable);
                    OPPhoneControlWayCategory.this.mPage1.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage2.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage3.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage4.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    break;
                case 1:
                    OPPhoneControlWayCategory.this.mPage1.setImageDrawable(OPPhoneControlWayCategory.this.mCurrentDrawable);
                    OPPhoneControlWayCategory.this.mPage0.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage2.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage3.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage4.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    break;
                case 2:
                    OPPhoneControlWayCategory.this.mPage2.setImageDrawable(OPPhoneControlWayCategory.this.mCurrentDrawable);
                    OPPhoneControlWayCategory.this.mPage0.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage1.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage3.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage4.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    break;
                case 3:
                    OPPhoneControlWayCategory.this.mPage3.setImageDrawable(OPPhoneControlWayCategory.this.mCurrentDrawable);
                    OPPhoneControlWayCategory.this.mPage0.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage1.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage2.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage4.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    break;
                case 4:
                    OPPhoneControlWayCategory.this.mPage4.setImageDrawable(OPPhoneControlWayCategory.this.mCurrentDrawable);
                    OPPhoneControlWayCategory.this.mPage0.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage1.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage2.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    OPPhoneControlWayCategory.this.mPage3.setImageDrawable(OPPhoneControlWayCategory.this.mNormalDrawable);
                    break;
            }
            OPPhoneControlWayCategory.this.playCurrentPageAnim(arg0);
            OPPhoneControlWayCategory.this.mCurrIndex = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public OPPhoneControlWayCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPPhoneControlWayCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPPhoneControlWayCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mCurrentDrawable = this.mContext.getResources().getDrawable(R.drawable.op_page_now);
        this.mNormalDrawable = this.mContext.getResources().getDrawable(R.drawable.op_page);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mCurrentDrawable.setTint(Color.parseColor("#FFFFFF"));
            this.mNormalDrawable.setTint(Color.parseColor("#4CFFFFFF"));
        } else {
            this.mCurrentDrawable.setTint(Color.parseColor("#969696"));
            this.mNormalDrawable.setTint(Color.parseColor("#E6E6E6"));
        }
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mAlwaysShowNavigationBarAnim = (LottieAnimationView) view.findViewById(R.id.always_show_nb_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mAlwaysShowNavigationBarAnim.setAnimation("op_fix_navigation_bar_dark_anim.json");
        } else {
            this.mAlwaysShowNavigationBarAnim.setAnimation("op_fix_navigation_bar_light_anim.json");
        }
        this.mHideNavigationBarAnim = (LottieAnimationView) view.findViewById(R.id.hide_nb_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mHideNavigationBarAnim.setAnimation("op_back_home_black.json");
        } else {
            this.mHideNavigationBarAnim.setAnimation("op_back_home_light.json");
        }
        this.mHideNavigationBarAnim.loop(true);
        this.mHideNavigationBarAnim.playAnimation();
        this.mPage0 = (ImageView) view.findViewById(R.id.page0);
        this.mPage1 = (ImageView) view.findViewById(R.id.page1);
        this.mPage2 = (ImageView) view.findViewById(R.id.page2);
        this.mPage3 = (ImageView) view.findViewById(R.id.page3);
        this.mPage4 = (ImageView) view.findViewById(R.id.page4);
        this.mPage0.setImageDrawable(this.mCurrentDrawable);
        this.mPage1.setImageDrawable(this.mNormalDrawable);
        this.mPage2.setImageDrawable(this.mNormalDrawable);
        this.mPage3.setImageDrawable(this.mNormalDrawable);
        this.mPage4.setImageDrawable(this.mNormalDrawable);
        if (!OPUtils.isSupportNewGesture()) {
            this.mPage4.setVisibility(8);
        }
        this.mViewPagerContainer = view.findViewById(R.id.gesture_anim_viewpager_container);
        this.mViewPager = (ViewPager) view.findViewById(R.id.gesture_anim_viewpager);
        this.mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        initViewPage();
        view.setDividerAllowedBelow(false);
    }

    private void initViewPage() {
        LayoutInflater mLi = LayoutInflater.from(this.mContext);
        View home = mLi.inflate(R.layout.op_fullscreen_gesture_guide_layout, null);
        ((TextView) home.findViewById(R.id.fullscreen_guide_title)).setText(R.string.oneplus_fullscreen_home_guide_title);
        ((TextView) home.findViewById(R.id.fullscreen_guide_summary)).setText(R.string.oneplus_fullscreen_home_guide_summary);
        this.mHomeAnim = (LottieAnimationView) home.findViewById(R.id.fullscreen_guide_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mHomeAnim.setAnimation("op_home_gesture_dark_anim.json");
        } else {
            this.mHomeAnim.setAnimation("op_home_gesture_light_anim.json");
        }
        this.mHomeAnim.loop(true);
        this.mHomeAnim.playAnimation();
        View recent = mLi.inflate(R.layout.op_fullscreen_gesture_guide_layout, null);
        ((TextView) recent.findViewById(R.id.fullscreen_guide_title)).setText(R.string.oneplus_fullscreen_recent_guide_title);
        ((TextView) recent.findViewById(R.id.fullscreen_guide_summary)).setText(R.string.oneplus_fullscreen_recent_guide_summary);
        this.mRecentAnim = (LottieAnimationView) recent.findViewById(R.id.fullscreen_guide_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mRecentAnim.setAnimation("op_recent_gesture_dark_anim.json");
        } else {
            this.mRecentAnim.setAnimation("op_recent_gesture_light_anim.json");
        }
        this.mRecentAnim.loop(true);
        this.mRecentAnim.playAnimation();
        View back = mLi.inflate(R.layout.op_fullscreen_gesture_guide_layout, null);
        ((TextView) back.findViewById(R.id.fullscreen_guide_title)).setText(R.string.oneplus_fullscreen_back_guide_title);
        TextView backSummary = (TextView) back.findViewById(R.id.fullscreen_guide_summary);
        backSummary.setText(R.string.oneplus_fullscreen_back_guide_summary);
        this.mBackAnim = (LottieAnimationView) back.findViewById(R.id.fullscreen_guide_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mBackAnim.setAnimation("op_back_gesture_dark_anim.json");
        } else {
            this.mBackAnim.setAnimation("op_back_gesture_light_anim.json");
        }
        this.mBackAnim.loop(true);
        this.mBackAnim.playAnimation();
        View previousApp = mLi.inflate(R.layout.op_fullscreen_gesture_guide_layout, null);
        ((TextView) previousApp.findViewById(R.id.fullscreen_guide_title)).setText(R.string.oneplus_fullscreen_previous_app_guide_title);
        ((TextView) previousApp.findViewById(R.id.fullscreen_guide_summary)).setText(R.string.oneplus_fullscreen_previous_app_guide_summary);
        this.mPreviousAppAnim = (LottieAnimationView) previousApp.findViewById(R.id.fullscreen_guide_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mPreviousAppAnim.setAnimation("op_previous_app_gesture_dark_anim.json");
        } else {
            this.mPreviousAppAnim.setAnimation("op_previous_app_gesture_light_anim.json");
        }
        this.mPreviousAppAnim.loop(true);
        this.mPreviousAppAnim.playAnimation();
        View land = mLi.inflate(R.layout.op_fullscreen_gesture_guide_layout_land, null);
        ((TextView) land.findViewById(R.id.fullscreen_guide_title)).setText(R.string.oneplus_fullscreen_landscape_guide_title);
        TextView landSummary = (TextView) land.findViewById(R.id.fullscreen_guide_summary);
        landSummary.setText(R.string.oneplus_fullscreen_landscape_guide_summary);
        this.mLandAnim = (LottieAnimationView) land.findViewById(R.id.fullscreen_guide_anim);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mLandAnim.setAnimation("op_landscape_dark_anim.json");
        } else {
            this.mLandAnim.setAnimation("op_landscape_light_anim.json");
        }
        this.mLandAnim.loop(true);
        this.mLandAnim.playAnimation();
        final ArrayList<View> views = new ArrayList();
        views.add(home);
        views.add(recent);
        views.add(back);
        if (OPUtils.isSupportNewGesture()) {
            views.add(previousApp);
        }
        views.add(land);
        this.mViewPager.setAdapter(new PagerAdapter() {
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            public int getCount() {
                return views.size();
            }

            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView((View) views.get(position));
            }

            public Object instantiateItem(View container, int position) {
                ((ViewPager) container).addView((View) views.get(position));
                return views.get(position);
            }
        });
        this.mViewPager.setCurrentItem(this.mCurrIndex);
        this.mHasInited = true;
        startAnim();
    }

    public void startAnim() {
        if (this.mHasInited) {
            setViewType(System.getInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1));
        }
    }

    private boolean getBackHomeEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "swipe_up_to_switch_apps_enabled", this.mContext.getResources().getBoolean(17957058)) != 0;
    }

    public void setViewType(int type) {
        if (this.mHasInited) {
            if (getBackHomeEnabled()) {
                type = 2;
            }
            stopAnim();
            switch (type) {
                case 1:
                    this.mAlwaysShowNavigationBarAnim.setVisibility(0);
                    this.mAlwaysShowNavigationBarAnim.playAnimation();
                    this.mHideNavigationBarAnim.setVisibility(8);
                    this.mViewPagerContainer.setVisibility(8);
                    break;
                case 2:
                    this.mAlwaysShowNavigationBarAnim.setVisibility(8);
                    this.mHideNavigationBarAnim.setVisibility(0);
                    this.mHideNavigationBarAnim.playAnimation();
                    this.mViewPagerContainer.setVisibility(8);
                    break;
                case 3:
                    this.mAlwaysShowNavigationBarAnim.setVisibility(8);
                    this.mHideNavigationBarAnim.setVisibility(8);
                    this.mViewPagerContainer.setVisibility(0);
                    playCurrentPageAnim(this.mCurrIndex);
                    break;
            }
        }
    }

    public void stopAnim() {
        if (this.mHasInited) {
            this.mAlwaysShowNavigationBarAnim.cancelAnimation();
            this.mHideNavigationBarAnim.cancelAnimation();
            this.mPreviousAppAnim.cancelAnimation();
            this.mHomeAnim.cancelAnimation();
            this.mRecentAnim.cancelAnimation();
            this.mBackAnim.cancelAnimation();
            this.mLandAnim.cancelAnimation();
        }
    }

    public void releaseAnim() {
        if (this.mHasInited) {
            this.mAlwaysShowNavigationBarAnim.cancelAnimation();
            this.mHideNavigationBarAnim.cancelAnimation();
            this.mPreviousAppAnim.cancelAnimation();
            this.mHomeAnim.cancelAnimation();
            this.mRecentAnim.cancelAnimation();
            this.mBackAnim.cancelAnimation();
            this.mLandAnim.cancelAnimation();
            this.mAlwaysShowNavigationBarAnim = null;
            this.mHideNavigationBarAnim = null;
            this.mPreviousAppAnim = null;
            this.mHomeAnim = null;
            this.mRecentAnim = null;
            this.mBackAnim = null;
            this.mLandAnim = null;
        }
    }

    public void playCurrentPageAnim(int index) {
        switch (index) {
            case 0:
                this.mPreviousAppAnim.cancelAnimation();
                this.mHomeAnim.playAnimation();
                this.mRecentAnim.cancelAnimation();
                this.mBackAnim.cancelAnimation();
                this.mLandAnim.cancelAnimation();
                return;
            case 1:
                this.mPreviousAppAnim.cancelAnimation();
                this.mHomeAnim.cancelAnimation();
                this.mRecentAnim.playAnimation();
                this.mBackAnim.cancelAnimation();
                this.mLandAnim.cancelAnimation();
                return;
            case 2:
                this.mPreviousAppAnim.cancelAnimation();
                this.mHomeAnim.cancelAnimation();
                this.mRecentAnim.cancelAnimation();
                this.mBackAnim.playAnimation();
                this.mLandAnim.cancelAnimation();
                return;
            case 3:
                if (OPUtils.isSupportNewGesture()) {
                    this.mPreviousAppAnim.playAnimation();
                    this.mHomeAnim.cancelAnimation();
                    this.mRecentAnim.cancelAnimation();
                    this.mBackAnim.cancelAnimation();
                    this.mLandAnim.cancelAnimation();
                    return;
                }
                break;
            case 4:
                break;
            default:
                return;
        }
        this.mPreviousAppAnim.cancelAnimation();
        this.mHomeAnim.cancelAnimation();
        this.mRecentAnim.cancelAnimation();
        this.mBackAnim.cancelAnimation();
        this.mLandAnim.playAnimation();
    }
}
