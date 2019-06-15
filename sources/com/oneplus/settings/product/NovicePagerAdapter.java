package com.oneplus.settings.product;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class NovicePagerAdapter extends PagerAdapter {
    public List<View> mListViews = null;

    public NovicePagerAdapter(List<View> mListViews) {
        this.mListViews = mListViews;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            ((ViewPager) container).removeView((View) this.mListViews.get(position));
        } catch (Exception e) {
        }
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView((View) this.mListViews.get(position), 0);
        return this.mListViews.get(position);
    }

    public int getCount() {
        if (this.mListViews != null) {
            return this.mListViews.size();
        }
        return 0;
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
