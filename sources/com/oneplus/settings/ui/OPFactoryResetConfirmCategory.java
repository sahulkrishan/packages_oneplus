package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;

public class OPFactoryResetConfirmCategory extends PreferenceCategory implements OnClickListener {
    private LayoutInflater inflater;
    private Button mConfirmButton;
    private Context mContext;
    private CharSequence mFingerprintName;
    private int mLayoutResId = R.layout.op_master_clear_preference_list_fragment;
    public OnFactoryResetConfirmListener mOnFactoryResetConfirmListener;

    public interface OnFactoryResetConfirmListener {
        void onFactoryResetConfirmClick();
    }

    public OPFactoryResetConfirmCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPFactoryResetConfirmCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFactoryResetConfirmCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mConfirmButton = (Button) view.findViewById(R.id.execute_master_clear);
        this.mConfirmButton.setEnabled(true);
        this.mConfirmButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (this.mOnFactoryResetConfirmListener != null) {
            this.mOnFactoryResetConfirmListener.onFactoryResetConfirmClick();
        }
    }

    public void setOnFactoryResetConfirmListener(OnFactoryResetConfirmListener listener) {
        this.mOnFactoryResetConfirmListener = listener;
    }
}
