package com.oneplus.settings.laboratory;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.settings.R;

public class OPRadioButtinGroup extends LinearLayout implements OnClickListener {
    private Context mContext;
    public OnRadioGroupClickListener mOnRadioGroupClickListener;

    public interface OnRadioGroupClickListener {
        void OnRadioGroupClick(int i);
    }

    public OPRadioButtinGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
    }

    public OPRadioButtinGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public OPRadioButtinGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public OPRadioButtinGroup(Context context) {
        super(context);
        this.mContext = context;
    }

    public void addChild(int childCount, String[] featureToggleNames) {
        for (int i = 0; i < childCount; i++) {
            View view = LayoutInflater.from(this.mContext).inflate(R.layout.op_radio_button_item, null);
            ((TextView) view.findViewById(R.id.title)).setText(featureToggleNames[i]);
            view.setId(i);
            view.setOnClickListener(this);
            addView(view, i);
        }
    }

    public void onClick(View v) {
        setSelect(v);
        if (this.mOnRadioGroupClickListener != null) {
            this.mOnRadioGroupClickListener.OnRadioGroupClick(v.getId());
        }
    }

    public void setSelect(View view) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            RadioButton rb = (RadioButton) getChildAt(i).findViewById(R.id.op_lab_feature_radio_button);
            if (view.getId() == i) {
                rb.setChecked(true);
            } else {
                rb.setChecked(false);
            }
        }
    }

    public void setSelect(int index) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            RadioButton rb = (RadioButton) getChildAt(i).findViewById(R.id.op_lab_feature_radio_button);
            if (index == i) {
                rb.setChecked(true);
            } else {
                rb.setChecked(false);
            }
        }
    }

    public void setOnRadioGroupClickListener(OnRadioGroupClickListener listener) {
        this.mOnRadioGroupClickListener = listener;
    }
}
