package com.oneplus.settings.opfinger;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;

public class OPFingerPrintItemPreference extends Preference {
    private static String BACKGROUND_COLOR = "#239ff1";
    private LayoutInflater inflater;
    private AlphaAnimation mAlphaAnimation;
    private View mBackGroundView;
    private Context mContext;
    private boolean mHighlightBackgroundColor = false;
    private ImageView mIconView;
    private int mLayoutResId = R.layout.op_fingerprint_item_preference;
    private String mOPFingerPrintSummary;
    private String mOPFingerPrintTitle;
    private TextView mSummaryView;
    private TextView mTitleView;

    public OPFingerPrintItemPreference(Context context) {
        super(context);
        initViews(context);
    }

    public OPFingerPrintItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFingerPrintItemPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.mLayoutResId);
    }

    /* Access modifiers changed, original: protected */
    public View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    /* Access modifiers changed, original: protected */
    public void onBindView(View view) {
        super.onBindView(view);
        this.mBackGroundView = view.findViewById(R.id.opfingerprint_item_highlight_view);
        this.mTitleView = (TextView) view.findViewById(R.id.opfingerprint_item_title);
        this.mSummaryView = (TextView) view.findViewById(R.id.opfingerprint_item_summary);
        this.mTitleView.setText(this.mOPFingerPrintTitle);
        this.mSummaryView.setText(this.mOPFingerPrintSummary);
        this.mSummaryView.setVisibility(8);
        this.mAlphaAnimation = new AlphaAnimation(0.0f, 0.4f);
        if (this.mHighlightBackgroundColor) {
            this.mBackGroundView.setBackgroundColor(Color.parseColor(BACKGROUND_COLOR));
            this.mAlphaAnimation.setDuration(500);
            this.mAlphaAnimation.setRepeatCount(1);
            this.mAlphaAnimation.setRepeatMode(2);
            this.mAlphaAnimation.setFillAfter(true);
            this.mAlphaAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    OPFingerPrintItemPreference.this.mHighlightBackgroundColor = false;
                }
            });
            this.mBackGroundView.setAnimation(this.mAlphaAnimation);
            this.mAlphaAnimation.start();
            return;
        }
        this.mBackGroundView.setBackgroundColor(0);
    }

    public void setOPFingerTitle(String title) {
        this.mOPFingerPrintTitle = title;
        notifyChanged();
    }

    public void setOPFingerSummary(String summary) {
        this.mOPFingerPrintSummary = summary;
        notifyChanged();
    }

    public void updateBackgroundColor(boolean highLight) {
        this.mHighlightBackgroundColor = highLight;
        notifyChanged();
    }
}
