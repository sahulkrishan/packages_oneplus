package com.oneplus.settings.quicklaunch;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class OPPagerAdapter extends PagerAdapter {
    private Fragment mCurrentItem;
    private final FragmentManager mFragmentManager;
    private List<Fragment> mFragments = new ArrayList();
    private FragmentTransaction mTransaction = null;

    public OPPagerAdapter(FragmentManager fm) {
        this.mFragmentManager = fm;
    }

    public void updateData(List<Fragment> fragments) {
        if (fragments != null) {
            this.mFragments = fragments;
        } else {
            this.mFragments.clear();
        }
        notifyDataSetChanged();
    }

    private Fragment getFragment(int position) {
        return (Fragment) this.mFragments.get(position);
    }

    public int getCount() {
        return this.mFragments.size();
    }

    public int getItemPosition(Object object) {
        for (int i = 0; i < this.mFragments.size(); i++) {
            if (this.mFragments.get(i) == object) {
                return i;
            }
        }
        return -2;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        if (this.mTransaction == null) {
            this.mTransaction = this.mFragmentManager.beginTransaction();
        }
        Fragment f = getFragment(position);
        this.mTransaction.show(f);
        f.setUserVisibleHint(f == this.mCurrentItem);
        return f;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        if (this.mTransaction == null) {
            this.mTransaction = this.mFragmentManager.beginTransaction();
        }
        this.mTransaction.hide((Fragment) object);
    }

    public void finishUpdate(ViewGroup container) {
        if (this.mTransaction != null) {
            this.mTransaction.commitAllowingStateLoss();
            this.mTransaction = null;
            this.mFragmentManager.executePendingTransactions();
        }
    }

    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (this.mFragments.get(position) != this.mCurrentItem) {
            this.mCurrentItem = (Fragment) this.mFragments.get(position);
        }
    }
}
