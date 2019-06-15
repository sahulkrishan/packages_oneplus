package com.android.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.preference.AndroidResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settings.widget.DotsPageIndicator;
import com.android.settings.widget.LabeledSeekBar;

public abstract class PreviewSeekBarPreferenceFragment extends SettingsPreferenceFragment {
    protected int mActivityLayoutResId;
    protected int mCurrentIndex;
    protected String[] mEntries;
    protected int mInitialIndex;
    private TextView mLabel;
    private View mLarger;
    private DotsPageIndicator mPageIndicator;
    private OnPageChangeListener mPageIndicatorPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.setPagerIndicatorContentDescription(position);
        }
    };
    private OnPageChangeListener mPreviewPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.mPreviewPager.sendAccessibilityEvent(16384);
        }
    };
    private ViewPager mPreviewPager;
    private PreviewPagerAdapter mPreviewPagerAdapter;
    protected int[] mPreviewSampleResIds;
    private LabeledSeekBar mSeekBar;
    private View mSmaller;

    private class onPreviewSeekBarChangeListener implements OnSeekBarChangeListener {
        private boolean mSeekByTouch;

        private onPreviewSeekBarChangeListener() {
        }

        /* synthetic */ onPreviewSeekBarChangeListener(PreviewSeekBarPreferenceFragment x0, AnonymousClass1 x1) {
            this();
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PreviewSeekBarPreferenceFragment.this.setPreviewLayer(progress, false);
            if (!this.mSeekByTouch) {
                PreviewSeekBarPreferenceFragment.this.commit();
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            this.mSeekByTouch = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.isAnimating()) {
                PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.setAnimationEndAction(new Runnable() {
                    public void run() {
                        PreviewSeekBarPreferenceFragment.this.commit();
                    }
                });
            } else {
                PreviewSeekBarPreferenceFragment.this.commit();
            }
            this.mSeekByTouch = false;
        }
    }

    public abstract void commit();

    public abstract Configuration createConfig(Configuration configuration, int i);

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup listContainer = (ViewGroup) root.findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER);
        listContainer.removeAllViews();
        View content = inflater.inflate(this.mActivityLayoutResId, listContainer, false);
        listContainer.addView(content);
        this.mLabel = (TextView) content.findViewById(R.id.current_label);
        int max = Math.max(1, this.mEntries.length - 1);
        this.mSeekBar = (LabeledSeekBar) content.findViewById(R.id.seek_bar);
        this.mSeekBar.setLabels(this.mEntries);
        this.mSeekBar.setMax(max);
        this.mSmaller = content.findViewById(R.id.smaller);
        this.mSmaller.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int progress = PreviewSeekBarPreferenceFragment.this.mSeekBar.getProgress();
                if (progress > 0) {
                    PreviewSeekBarPreferenceFragment.this.mSeekBar.setProgress(progress - 1, true);
                }
            }
        });
        this.mLarger = content.findViewById(R.id.larger);
        this.mLarger.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int progress = PreviewSeekBarPreferenceFragment.this.mSeekBar.getProgress();
                if (progress < PreviewSeekBarPreferenceFragment.this.mSeekBar.getMax()) {
                    PreviewSeekBarPreferenceFragment.this.mSeekBar.setProgress(progress + 1, true);
                }
            }
        });
        if (this.mEntries.length == 1) {
            this.mSeekBar.setEnabled(false);
        }
        Context context = getContext();
        Configuration origConfig = context.getResources().getConfiguration();
        boolean isLayoutRtl = origConfig.getLayoutDirection() == 1;
        Configuration[] configurations = new Configuration[this.mEntries.length];
        for (int i = 0; i < this.mEntries.length; i++) {
            configurations[i] = createConfig(origConfig, i);
        }
        this.mPreviewPager = (ViewPager) content.findViewById(R.id.preview_pager);
        this.mPreviewPagerAdapter = new PreviewPagerAdapter(context, isLayoutRtl, this.mPreviewSampleResIds, configurations);
        this.mPreviewPager.setAdapter(this.mPreviewPagerAdapter);
        this.mPreviewPager.setCurrentItem(isLayoutRtl ? this.mPreviewSampleResIds.length - 1 : 0);
        this.mPreviewPager.addOnPageChangeListener(this.mPreviewPageChangeListener);
        this.mPageIndicator = (DotsPageIndicator) content.findViewById(R.id.page_indicator);
        if (this.mPreviewSampleResIds.length > 1) {
            this.mPageIndicator.setViewPager(this.mPreviewPager);
            this.mPageIndicator.setVisibility(0);
            this.mPageIndicator.setOnPageChangeListener(this.mPageIndicatorPageChangeListener);
        } else {
            this.mPageIndicator.setVisibility(8);
        }
        setPreviewLayer(this.mInitialIndex, false);
        return root;
    }

    public void onStart() {
        super.onStart();
        this.mSeekBar.setProgress(this.mCurrentIndex);
        this.mSeekBar.setOnSeekBarChangeListener(new onPreviewSeekBarChangeListener(this, null));
    }

    public void onStop() {
        super.onStop();
        this.mSeekBar.setOnSeekBarChangeListener(null);
    }

    private void setPreviewLayer(int index, boolean animate) {
        this.mLabel.setText(this.mEntries[index]);
        boolean z = false;
        this.mSmaller.setEnabled(index > 0);
        View view = this.mLarger;
        if (index < this.mEntries.length - 1) {
            z = true;
        }
        view.setEnabled(z);
        setPagerIndicatorContentDescription(this.mPreviewPager.getCurrentItem());
        this.mPreviewPagerAdapter.setPreviewLayer(index, this.mCurrentIndex, this.mPreviewPager.getCurrentItem(), animate);
        this.mCurrentIndex = index;
    }

    private void setPagerIndicatorContentDescription(int position) {
        this.mPageIndicator.setContentDescription(getPrefContext().getString(R.string.preview_page_indicator_content_description, new Object[]{Integer.valueOf(position + 1), Integer.valueOf(this.mPreviewSampleResIds.length)}));
    }
}
