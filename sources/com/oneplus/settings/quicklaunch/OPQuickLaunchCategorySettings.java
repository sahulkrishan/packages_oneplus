package com.oneplus.settings.quicklaunch;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;
import android.widget.Toolbar;
import com.android.settings.R;
import com.oneplus.lib.widget.OPTabLayout;
import com.oneplus.lib.widget.OPTabLayout.Tab;
import com.oneplus.settings.quicklaunch.TabUtils.OnSetupTabListener;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPQuickLaunchCategorySettings extends FragmentActivity implements OnPageChangeListener {
    public static final int APPLICATION_FRAGMENT_INDEX = 1;
    private static final String APPLICATION_FRAGMENT_TAG = "browse_fragment";
    public static final int SHORTCUT_FRAGMENT_INDEX = 0;
    private static final String SHORTCUT_FRAGMENT_TAG = "directory_fragment";
    private OPQuickLaunchAppFragment mApplicationFragment;
    private int mCurrentIndex = 0;
    private final List<String> mFragmentTitles = new ArrayList();
    private final List<Fragment> mFragments = new ArrayList();
    private OPPagerAdapter mPagerAdapter;
    private OPQuickLaunchShortCutFragment mShortcutFragment;
    private OPTabLayout mTabLayout;
    private ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OPUtils.isWhiteModeOn(getContentResolver())) {
            getWindow().getDecorView().setSystemUiVisibility(8192);
        }
        setContentView(R.layout.op_quick_launcher_category_settings);
        setActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        this.mViewPager = (ViewPager) findViewById(R.id.op_quick_launch_category_viewpager);
        this.mTabLayout = (OPTabLayout) findViewById(R.id.tabs);
        initFragments();
        initViewPager();
        initTabLayout();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void initViewPager() {
        this.mPagerAdapter = new OPPagerAdapter(getSupportFragmentManager());
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mPagerAdapter.updateData(this.mFragments);
        this.mViewPager.addOnPageChangeListener(this);
    }

    private void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        this.mFragmentTitles.clear();
        this.mFragments.clear();
        this.mShortcutFragment = (OPQuickLaunchShortCutFragment) fragmentManager.findFragmentByTag(SHORTCUT_FRAGMENT_TAG);
        if (this.mShortcutFragment == null) {
            this.mShortcutFragment = new OPQuickLaunchShortCutFragment();
            transaction.add(R.id.op_quick_launch_category_viewpager, this.mShortcutFragment, SHORTCUT_FRAGMENT_TAG);
        }
        this.mFragments.add(this.mShortcutFragment);
        this.mFragmentTitles.add(getString(R.string.oneplus_shortcuts_title));
        transaction.hide(this.mShortcutFragment);
        this.mApplicationFragment = (OPQuickLaunchAppFragment) fragmentManager.findFragmentByTag(APPLICATION_FRAGMENT_TAG);
        if (this.mApplicationFragment == null) {
            this.mApplicationFragment = new OPQuickLaunchAppFragment();
            transaction.add(R.id.op_quick_launch_category_viewpager, this.mApplicationFragment, APPLICATION_FRAGMENT_TAG);
        }
        this.mFragments.add(this.mApplicationFragment);
        this.mFragmentTitles.add(getString(R.string.oneplus_apps_title));
        transaction.hide(this.mApplicationFragment);
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    private void initTabLayout() {
        TabUtils.setupWithViewPager(this.mTabLayout, this.mViewPager, new OnSetupTabListener() {
            public void onSetupTab(int position, Tab tab) {
                tab.setText((CharSequence) OPQuickLaunchCategorySettings.this.mFragmentTitles.get(position));
            }
        });
        this.mViewPager.setCurrentItem(this.mCurrentIndex);
    }

    public void onPageSelected(int position) {
        this.mTabLayout.getTabAt(position).select();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageScrollStateChanged(int state) {
    }
}
