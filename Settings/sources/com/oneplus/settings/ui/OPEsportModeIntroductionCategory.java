package com.oneplus.settings.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class OPEsportModeIntroductionCategory extends Preference {
    private ContentResolver mContentResolver;
    private Context mContext;
    private ImageView mEsportmodeIntroductionImageView;
    private TextView mEsportmodeIntroductionSummary;
    private int mLayoutResId = R.layout.op_esport_mode_instruction_category;

    public OPEsportModeIntroductionCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPEsportModeIntroductionCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPEsportModeIntroductionCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setClickable(false);
        this.mEsportmodeIntroductionImageView = (ImageView) view.findViewById(R.id.esportmode_introduction_imageview);
        this.mEsportmodeIntroductionSummary = (TextView) view.findViewById(R.id.esportmode_introduction_summary);
        if (OPUtils.isBlackModeOn(this.mContentResolver)) {
            this.mEsportmodeIntroductionImageView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.op_esport_mode_introduction_dark));
        } else {
            this.mEsportmodeIntroductionImageView.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.op_esport_mode_introduction_light));
        }
        view.setDividerAllowedBelow(false);
    }
}
