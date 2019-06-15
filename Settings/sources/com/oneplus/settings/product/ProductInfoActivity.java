package com.oneplus.settings.product;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class ProductInfoActivity extends Activity {
    private static int count;
    private TextView mCountTextView;
    private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
        public void onPageSelected(int position) {
            ProductInfoActivity.this.updatePagerViews(position);
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageScrollStateChanged(int state) {
        }
    };
    private NovicePagerAdapter mPagerAdapter = null;
    private ViewPager mViewPager = null;
    private List<View> mViews = null;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_europe_and_america)))) {
            getWindow().setFlags(1024, 1024);
            setRequestedOrientation(0);
        }
        try {
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        setContentView(R.layout.op_product_layout);
        this.mViews = new ArrayList();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_china_and_india)) || Build.MODEL.equalsIgnoreCase(getString(R.string.oneplus_model_for_europe_and_america))) {
            for (int i = 0; i < 11; i++) {
                View customView = inflater.inflate(R.layout.op_product_img_item, null);
                ImageView iv = (ImageView) customView.findViewById(R.id.image);
                Resources resources = getResources();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("product_info_6x_");
                stringBuilder.append(autoGenericCode(i + 1, 2));
                iv.setImageResource(resources.getIdentifier(stringBuilder.toString(), "drawable", getPackageName()));
                this.mViews.add(customView);
            }
        } else {
            boolean is16859 = OPUtils.isSurportProductInfo16859(getApplicationContext());
            for (int i2 = 0; i2 < 18; i2++) {
                View customView2 = inflater.inflate(R.layout.op_product_img_item, null);
                ImageView iv2 = (ImageView) customView2.findViewById(R.id.image);
                Resources resources2 = getResources();
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("product_info_");
                stringBuilder2.append(autoGenericCode(i2 + 1, 2));
                iv2.setImageResource(resources2.getIdentifier(stringBuilder2.toString(), "drawable", getPackageName()));
                this.mViews.add(customView2);
            }
        }
        this.mViewPager = (ViewPager) findViewById(R.id.main_pager);
        this.mCountTextView = (TextView) findViewById(R.id.textcount);
        this.mCountTextView.setVisibility(4);
        this.mPagerAdapter = new NovicePagerAdapter(this.mViews);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setCurrentItem(0);
        this.mViewPager.setOnPageChangeListener(this.mPageChangeListener);
        count = this.mPagerAdapter.getCount();
        TextView textView = this.mCountTextView;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("1/");
        stringBuilder3.append(count);
        textView.setText(stringBuilder3.toString());
    }

    public static String autoGenericCode(int code, int num) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%0");
        stringBuilder.append(num);
        stringBuilder.append("d");
        return String.format(stringBuilder.toString(), new Object[]{Integer.valueOf(code)});
    }

    private void updatePagerViews(int position) {
        TextView textView = this.mCountTextView;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(position + 1);
        stringBuilder.append("/");
        stringBuilder.append(count);
        textView.setText(stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
    }

    /* Access modifiers changed, original: protected */
    public void onResume() {
        super.onResume();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* Access modifiers changed, original: protected */
    public void onPause() {
        super.onPause();
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
    }
}
