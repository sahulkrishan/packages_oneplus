package com.oneplus.settings.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

public class OPViewPagerGuideCategory extends Preference {
    private static final int BACK_ANIM_INDEX = 3;
    private static final int HOME_ANIM_INDEX = 1;
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    private static final int LAND_ANIM_INDEX = 4;
    public static final int LAND_TYPE = 2;
    private static final int PREVIOUS_APP_ANIM_INDEX = 0;
    private static final int RECENT_ANIM_INDEX = 2;
    private static final int TYPE_ALWAYS_SHOW_NAVIGATION_BAR = 1;
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;
    private static final int TYPE_HIDE_NAVIGATION_BAR = 2;
    public static final int VERTICAL_TYPE = 1;
    private LayoutInflater inflater;
    private String[] mAnimationDarkId;
    private String[] mAnimationWhiteId;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrIndex = 0;
    private Drawable mCurrentDrawable;
    private int mCurrentType = 1;
    private int[] mDescriptionId;
    private View mDotContainer;
    private ArrayList<View> mGuideViews = new ArrayList();
    private boolean mHasInited = false;
    private int mLayoutItemID = R.layout.op_viewpager_guide_item_vertical_layout;
    private int mLayoutResId = R.layout.op_viewpager_guide_category;
    private Drawable mNormalDrawable;
    private ImageView mPage0;
    private ImageView mPage1;
    private int[] mTitleId;
    private ViewPager mViewPager;
    private View mViewPagerContainer;

    public class MyOnPageChangeListener implements OnPageChangeListener {
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:
                    OPViewPagerGuideCategory.this.mPage0.setImageDrawable(OPViewPagerGuideCategory.this.mCurrentDrawable);
                    OPViewPagerGuideCategory.this.mPage1.setImageDrawable(OPViewPagerGuideCategory.this.mNormalDrawable);
                    break;
                case 1:
                    OPViewPagerGuideCategory.this.mPage1.setImageDrawable(OPViewPagerGuideCategory.this.mCurrentDrawable);
                    OPViewPagerGuideCategory.this.mPage0.setImageDrawable(OPViewPagerGuideCategory.this.mNormalDrawable);
                    break;
            }
            OPViewPagerGuideCategory.this.playCurrentPageAnim(arg0);
            OPViewPagerGuideCategory.this.mCurrIndex = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public OPViewPagerGuideCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPViewPagerGuideCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPViewPagerGuideCategory(Context context, AttributeSet attrs, int defStyle) {
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

    public void setType(int type) {
        switch (type) {
            case 1:
                this.mLayoutItemID = R.layout.op_viewpager_guide_item_vertical_layout;
                return;
            case 2:
                this.mLayoutItemID = R.layout.op_viewpager_guide_item_landscape_layout;
                return;
            default:
                return;
        }
    }

    public void setAnimationWhiteResources(String[] animationId) {
        this.mAnimationWhiteId = animationId;
    }

    public void setAnimationDarkResources(String[] animationId) {
        this.mAnimationDarkId = animationId;
    }

    public void setTitleResources(int[] titleId) {
        this.mTitleId = titleId;
    }

    public void setDescriptionIdResources(int[] descriptionId) {
        this.mDescriptionId = descriptionId;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mDotContainer = view.findViewById(R.id.op_viewpager_guide_dot_container);
        this.mPage0 = (ImageView) view.findViewById(R.id.page0);
        this.mPage1 = (ImageView) view.findViewById(R.id.page1);
        this.mPage0.setImageDrawable(this.mCurrentDrawable);
        this.mPage1.setImageDrawable(this.mNormalDrawable);
        this.mViewPagerContainer = view.findViewById(R.id.op_viewpager_guide_container);
        this.mViewPager = (ViewPager) view.findViewById(R.id.op_viewpager_guide_viewpager);
        this.mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        initViewPage();
        view.setDividerAllowedBelow(false);
    }

    public void showDotView(boolean show) {
        if (this.mDotContainer != null) {
            this.mDotContainer.setVisibility(show ? 0 : 8);
        }
    }

    public void initViewPage() {
        LayoutInflater mLi = LayoutInflater.from(this.mContext);
        this.mGuideViews.clear();
        for (int i = 0; i < this.mTitleId.length; i++) {
            View itemView = mLi.inflate(this.mLayoutItemID, null);
            ((TextView) itemView.findViewById(R.id.guide_title)).setText(this.mTitleId[i]);
            ((TextView) itemView.findViewById(R.id.guide_summary)).setText(this.mDescriptionId[i]);
            LottieAnimationView mAnimView = (LottieAnimationView) itemView.findViewById(R.id.guide_anim);
            if (OPUtils.isBlackModeOn(this.mContentResolver)) {
                mAnimView.setAnimation(this.mAnimationDarkId[i]);
            } else {
                mAnimView.setAnimation(this.mAnimationWhiteId[i]);
            }
            this.mGuideViews.add(itemView);
        }
        if (this.mTitleId.length == 1) {
            showDotView(false);
        }
        this.mViewPager.setAdapter(new PagerAdapter() {
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            public int getCount() {
                return OPViewPagerGuideCategory.this.mGuideViews.size();
            }

            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView((View) OPViewPagerGuideCategory.this.mGuideViews.get(position));
            }

            public Object instantiateItem(View container, int position) {
                ((ViewPager) container).addView((View) OPViewPagerGuideCategory.this.mGuideViews.get(position));
                return OPViewPagerGuideCategory.this.mGuideViews.get(position);
            }
        });
        this.mViewPager.setCurrentItem(this.mCurrIndex);
        this.mHasInited = true;
        startAnim();
    }

    public void startAnim() {
        if (this.mHasInited) {
            playCurrentPageAnim(this.mCurrIndex);
        }
    }

    public void playCurrentPageAnim(int index) {
        for (int i = 0; i < this.mGuideViews.size(); i++) {
            LottieAnimationView animView = (LottieAnimationView) ((View) this.mGuideViews.get(i)).findViewById(R.id.guide_anim);
            if (index == i) {
                animView.playAnimation();
            } else {
                animView.cancelAnimation();
            }
        }
    }

    public void stopAnim() {
        if (this.mHasInited) {
            playCurrentPageAnim(-1);
        }
    }

    public void releaseAnim() {
        stopAnim();
        if (this.mGuideViews != null) {
            this.mGuideViews.clear();
        }
    }
}
