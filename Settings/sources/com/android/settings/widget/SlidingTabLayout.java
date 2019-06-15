package com.android.settings.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public final class SlidingTabLayout extends FrameLayout implements OnClickListener {
    private final View mIndicatorView = this.mLayoutInflater.inflate(R.layout.sliding_tab_indicator_view, this, false);
    private final LayoutInflater mLayoutInflater;
    private int mSelectedPosition;
    private float mSelectionOffset;
    private final LinearLayout mTitleView;
    private RtlCompatibleViewPager mViewPager;

    private final class InternalViewPagerListener implements OnPageChangeListener {
        private int mScrollState;

        private InternalViewPagerListener() {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int titleCount = SlidingTabLayout.this.mTitleView.getChildCount();
            if (titleCount != 0 && position >= 0 && position < titleCount) {
                SlidingTabLayout.this.onViewPagerPageChanged(position, positionOffset);
            }
        }

        public void onPageScrollStateChanged(int state) {
            this.mScrollState = state;
        }

        public void onPageSelected(int position) {
            position = SlidingTabLayout.this.mViewPager.getRtlAwareIndex(position);
            if (this.mScrollState == 0) {
                SlidingTabLayout.this.onViewPagerPageChanged(position, 0.0f);
            }
            int titleCount = SlidingTabLayout.this.mTitleView.getChildCount();
            int i = 0;
            while (i < titleCount) {
                SlidingTabLayout.this.mTitleView.getChildAt(i).setSelected(position == i);
                i++;
            }
        }
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTitleView = new LinearLayout(context);
        this.mTitleView.setGravity(1);
        addView(this.mTitleView, -1, -2);
        addView(this.mIndicatorView, this.mIndicatorView.getLayoutParams());
    }

    public void setViewPager(RtlCompatibleViewPager viewPager) {
        this.mTitleView.removeAllViews();
        this.mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int titleCount = this.mTitleView.getChildCount();
        if (titleCount > 0) {
            this.mIndicatorView.measure(MeasureSpec.makeMeasureSpec(this.mTitleView.getMeasuredWidth() / titleCount, Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec(this.mIndicatorView.getMeasuredHeight(), Ints.MAX_POWER_OF_TWO));
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mTitleView.getChildCount() > 0) {
            int indicatorBottom = getMeasuredHeight();
            int indicatorHeight = this.mIndicatorView.getMeasuredHeight();
            int indicatorWidth = this.mIndicatorView.getMeasuredWidth();
            int totalWidth = getMeasuredWidth();
            this.mTitleView.layout(getPaddingLeft(), 0, this.mTitleView.getMeasuredWidth() + getPaddingRight(), this.mTitleView.getMeasuredHeight());
            if (isRtlMode()) {
                this.mIndicatorView.layout(totalWidth - indicatorWidth, indicatorBottom - indicatorHeight, totalWidth, indicatorBottom);
            } else {
                this.mIndicatorView.layout(0, indicatorBottom - indicatorHeight, indicatorWidth, indicatorBottom);
            }
        }
    }

    public void onClick(View v) {
        int titleCount = this.mTitleView.getChildCount();
        for (int i = 0; i < titleCount; i++) {
            if (v == this.mTitleView.getChildAt(i)) {
                this.mViewPager.setCurrentItem(i);
                return;
            }
        }
    }

    private void onViewPagerPageChanged(int position, float positionOffset) {
        this.mSelectedPosition = position;
        this.mSelectionOffset = positionOffset;
        this.mIndicatorView.setTranslationX((float) (isRtlMode() ? -getIndicatorLeft() : getIndicatorLeft()));
    }

    private void populateTabStrip() {
        PagerAdapter adapter = this.mViewPager.getAdapter();
        int i = 0;
        while (i < adapter.getCount()) {
            TextView tabTitleView = (TextView) this.mLayoutInflater.inflate(R.layout.sliding_tab_title_view, this.mTitleView, false);
            tabTitleView.setText(adapter.getPageTitle(i));
            tabTitleView.setOnClickListener(this);
            this.mTitleView.addView(tabTitleView);
            tabTitleView.setSelected(i == this.mViewPager.getCurrentItem());
            i++;
        }
    }

    private int getIndicatorLeft() {
        int left = this.mTitleView.getChildAt(this.mSelectedPosition).getLeft();
        if (this.mSelectionOffset <= 0.0f || this.mSelectedPosition >= getChildCount() - 1) {
            return left;
        }
        return (int) ((this.mSelectionOffset * ((float) this.mTitleView.getChildAt(this.mSelectedPosition + 1).getLeft())) + ((1.0f - this.mSelectionOffset) * ((float) left)));
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == 1;
    }
}
