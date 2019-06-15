package com.android.settings.wifi.calling;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.ims.ImsManager;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settings.widget.RtlCompatibleViewPager;
import com.android.settings.widget.SlidingTabLayout;
import com.android.settingslib.core.lifecycle.ObservableFragment;
import java.util.List;

public class WifiCallingSettings extends InstrumentedFragment implements HelpResourceProvider {
    private static final String TAG = "WifiCallingSettings";
    private WifiCallingViewPagerAdapter mPagerAdapter;
    private List<SubscriptionInfo> mSil;
    private SlidingTabLayout mTabLayout;
    private RtlCompatibleViewPager mViewPager;

    private final class WifiCallingViewPagerAdapter extends FragmentPagerAdapter {
        private final RtlCompatibleViewPager mViewPager;

        public WifiCallingViewPagerAdapter(FragmentManager fragmentManager, RtlCompatibleViewPager viewPager) {
            super(fragmentManager);
            this.mViewPager = viewPager;
        }

        public CharSequence getPageTitle(int position) {
            return String.valueOf(((SubscriptionInfo) WifiCallingSettings.this.mSil.get(position)).getDisplayName());
        }

        public Fragment getItem(int position) {
            String str = WifiCallingSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Adapter getItem ");
            stringBuilder.append(position);
            Log.d(str, stringBuilder.toString());
            Bundle args = new Bundle();
            args.putBoolean(SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR, false);
            args.putInt("subId", ((SubscriptionInfo) WifiCallingSettings.this.mSil.get(position)).getSubscriptionId());
            WifiCallingSettingsForSub fragment = new WifiCallingSettingsForSub();
            fragment.setArguments(args);
            return fragment;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            String str = WifiCallingSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Adapter instantiateItem ");
            stringBuilder.append(position);
            Log.d(str, stringBuilder.toString());
            return super.instantiateItem(container, this.mViewPager.getRtlAwareIndex(position));
        }

        public int getCount() {
            if (WifiCallingSettings.this.mSil == null) {
                Log.d(WifiCallingSettings.TAG, "Adapter getCount null mSil ");
                return 0;
            }
            String str = WifiCallingSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Adapter getCount ");
            stringBuilder.append(WifiCallingSettings.this.mSil.size());
            Log.d(str, stringBuilder.toString());
            return WifiCallingSettings.this.mSil.size();
        }
    }

    public int getMetricsCategory() {
        return 105;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wifi_calling_settings_tabs, container, false);
        this.mTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        this.mViewPager = (RtlCompatibleViewPager) view.findViewById(R.id.view_pager);
        this.mPagerAdapter = new WifiCallingViewPagerAdapter(getChildFragmentManager(), this.mViewPager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        return view;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        SearchMenuController.init((ObservableFragment) this);
        HelpMenuController.init((ObservableFragment) this);
        updateSubList();
    }

    public void onStart() {
        super.onStart();
        if (this.mSil == null || this.mSil.size() <= 1) {
            this.mTabLayout.setVisibility(8);
        } else {
            this.mTabLayout.setViewPager(this.mViewPager);
        }
    }

    public int getHelpResource() {
        return R.string.help_uri_wifi_calling;
    }

    private void updateSubList() {
        this.mSil = SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoList();
        if (this.mSil != null) {
            int i = 0;
            while (i < this.mSil.size()) {
                if (ImsManager.getInstance(getActivity(), ((SubscriptionInfo) this.mSil.get(i)).getSimSlotIndex()).isWfcEnabledByPlatform()) {
                    i++;
                } else {
                    this.mSil.remove(i);
                }
            }
        }
    }
}
