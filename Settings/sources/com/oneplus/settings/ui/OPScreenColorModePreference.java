package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.android.settings.R;
import java.util.ArrayList;

public class OPScreenColorModePreference extends Preference {
    private static final int layoutResId = 2131558839;
    private int currIndex = 0;
    private Context mContext;
    private ImageView mPage0;
    private ImageView mPage1;
    private ImageView mPage2;
    private ViewPager mViewPager;

    public class MyOnPageChangeListener implements OnPageChangeListener {
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case 0:
                    OPScreenColorModePreference.this.mPage0.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page_now));
                    OPScreenColorModePreference.this.mPage1.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page));
                    break;
                case 1:
                    OPScreenColorModePreference.this.mPage1.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page_now));
                    OPScreenColorModePreference.this.mPage0.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page));
                    OPScreenColorModePreference.this.mPage2.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page));
                    break;
                case 2:
                    OPScreenColorModePreference.this.mPage2.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page_now));
                    OPScreenColorModePreference.this.mPage1.setImageDrawable(OPScreenColorModePreference.this.mContext.getResources().getDrawable(R.drawable.op_page));
                    break;
            }
            OPScreenColorModePreference.this.currIndex = arg0;
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public OPScreenColorModePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public OPScreenColorModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public OPScreenColorModePreference(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        setLayoutResource(R.layout.op_screen_color_mode_preference);
        this.mContext = context;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mViewPager = (ViewPager) view.findViewById(R.id.whatsnew_viewpager);
        this.mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        this.mPage0 = (ImageView) view.findViewById(R.id.page0);
        this.mPage1 = (ImageView) view.findViewById(R.id.page1);
        this.mPage2 = (ImageView) view.findViewById(R.id.page2);
        LayoutInflater mLi = LayoutInflater.from(this.mContext);
        View view1 = mLi.inflate(R.layout.op_screen_image_one, null);
        View view2 = mLi.inflate(R.layout.op_screen_image_two, null);
        View view3 = mLi.inflate(R.layout.op_screen_image_three, null);
        final ArrayList<View> views = new ArrayList();
        views.add(view1);
        views.add(view2);
        views.add(view3);
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
        this.mViewPager.setCurrentItem(this.currIndex);
    }
}
