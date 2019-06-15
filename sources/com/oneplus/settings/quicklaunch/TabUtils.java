package com.oneplus.settings.quicklaunch;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.oneplus.lib.widget.OPTabLayout;
import com.oneplus.lib.widget.OPTabLayout.OnTabSelectedListener;
import com.oneplus.lib.widget.OPTabLayout.Tab;
import java.lang.ref.WeakReference;

public class TabUtils {

    public interface OnSetupTabListener {
        void onSetupTab(int i, Tab tab);
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private int mPendingSelection = -1;
        private int mScrollState;
        private final WeakReference<OPTabLayout> mTabLayoutRef;

        public TabLayoutOnPageChangeListener(OPTabLayout tabLayout) {
            this.mTabLayoutRef = new WeakReference(tabLayout);
        }

        public void onPageScrollStateChanged(int state) {
            this.mScrollState = state;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            OPTabLayout tabLayout = (OPTabLayout) this.mTabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.setScrollPosition(position, positionOffset, true);
                if (this.mScrollState == 0 && this.mPendingSelection != -1) {
                    tabLayout.getTabAt(this.mPendingSelection).select();
                    this.mPendingSelection = -1;
                }
            }
        }

        public void onPageSelected(int position) {
            this.mPendingSelection = position;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.mViewPager = viewPager;
        }

        public void onTabSelected(Tab tab) {
            this.mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(Tab tab) {
        }

        public void onTabReselected(Tab tab) {
        }
    }

    public static void setupWithViewPager(OPTabLayout tabLayout, ViewPager viewPager, OnSetupTabListener listener) {
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter != null) {
            setTabsFromPagerAdapter(tabLayout, adapter, listener);
            viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager));
            if (adapter.getCount() > 0) {
                int curItem = viewPager.getCurrentItem();
                if (tabLayout.getSelectedTabPosition() != curItem) {
                    tabLayout.getTabAt(curItem).select();
                    return;
                }
                return;
            }
            return;
        }
        throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
    }

    public static void setTabsFromPagerAdapter(OPTabLayout tabLayout, PagerAdapter adapter, OnSetupTabListener listener) {
        tabLayout.removeAllTabs();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            Tab tab = tabLayout.newTab();
            listener.onSetupTab(i, tab);
            tabLayout.addTab(tab);
        }
    }
}
