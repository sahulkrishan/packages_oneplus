package com.oneplus.settings.opfinger;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.android.settings.R;

public class OPFingerPrintEditCategory extends Preference {
    private LayoutInflater inflater;
    private Context mContext;
    private CharSequence mFingerprintName;
    private TextView mFingerprintNameView;
    private int mLayoutResId = R.layout.op_fingerprint_edit_category;

    public OPFingerPrintEditCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPFingerPrintEditCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPFingerPrintEditCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mFingerprintNameView = (TextView) view.findViewById(R.id.op_fingerprint_name);
        this.mFingerprintNameView.setText(this.mFingerprintName);
        view.setDividerAllowedBelow(false);
    }

    public void setFingerprintName(CharSequence name) {
        this.mFingerprintName = name;
        if (this.mFingerprintNameView != null) {
            this.mFingerprintNameView.setText(name);
        }
    }
}
