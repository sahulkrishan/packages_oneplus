package com.oneplus.settings.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.RestrictedPreference;
import com.oneplus.settings.utils.OPUtils;

public class OPTextViewButtonPreference extends RestrictedPreference {
    private boolean mButtonEnable;
    private String mButtonString;
    private boolean mButtonVisible;
    private Context mContext;
    private Drawable mIcon;
    private ImageView mLeftIcon;
    private OnClickListener mOnClickListener;
    private TextView mRightButton;
    private ColorStateList mTextButtonColor;
    private TextView mTextTitle;
    private String mTextTitleString;
    private int resid = R.layout.op_textview_button_prefrence;

    public OPTextViewButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    public OPTextViewButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPTextViewButtonPreference(Context context) {
        super(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.resid);
        this.mTextTitleString = "";
        this.mButtonString = "";
        this.mIcon = null;
        this.mButtonEnable = false;
        this.mButtonVisible = true;
        this.mTextButtonColor = OPUtils.creatOneplusPrimaryColorStateList(this.mContext);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mLeftIcon = (ImageView) view.findViewById(R.id.left_ico);
        this.mRightButton = (TextView) view.findViewById(R.id.right_button);
        this.mRightButton.setVisibility(this.mButtonVisible ? 0 : 8);
        this.mRightButton.setTextColor(this.mTextButtonColor);
        this.mRightButton.setOnClickListener(this.mOnClickListener);
        this.mRightButton.setEnabled(this.mButtonEnable);
        this.mRightButton.setText(this.mButtonString);
        this.mTextTitle = (TextView) view.findViewById(R.id.lefttitle);
        this.mTextTitle.setText(this.mTextTitleString);
        if (this.mIcon != null) {
            this.mLeftIcon.setImageDrawable(this.mIcon);
        }
    }

    public void setOnButtonClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public TextView getLeftButton() {
        return this.mRightButton;
    }

    public void setButtonEnable(boolean enable) {
        this.mButtonEnable = enable;
        notifyChanged();
    }

    public void setButtonVisible(boolean visible) {
        this.mButtonVisible = visible;
        notifyChanged();
    }

    public void setLeftTextTitle(String title) {
        this.mTextTitleString = title;
        notifyChanged();
    }

    public String getLeftTextTitle() {
        return this.mTextTitleString;
    }

    public void setButtonString(String buttonString) {
        this.mButtonString = buttonString;
        notifyChanged();
    }

    public String getButtonString() {
        return this.mButtonString;
    }

    public void setIcon(Drawable drawable) {
        this.mIcon = drawable;
        notifyChanged();
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setTitle(CharSequence title) {
        setLeftTextTitle(title.toString());
    }

    public CharSequence getTitle() {
        return this.mTextTitleString;
    }
}
