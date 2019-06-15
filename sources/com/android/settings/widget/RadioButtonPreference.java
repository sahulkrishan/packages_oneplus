package com.android.settings.widget;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.settings.R;

public class RadioButtonPreference extends CheckBoxPreference {
    private OnClickListener mListener;

    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mListener = null;
        setWidgetLayoutResource(R.layout.preference_widget_radiobutton);
        setLayoutResource(R.layout.preference_radio);
        setIconSpaceReserved(false);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, 16842894));
    }

    public RadioButtonPreference(Context context) {
        this(context, null);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public void onClick() {
        if (this.mListener != null) {
            this.mListener.onRadioButtonClicked(this);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View summaryContainer = view.findViewById(R.id.summary_container);
        if (summaryContainer != null) {
            int i;
            if (TextUtils.isEmpty(getSummary())) {
                i = 8;
            } else {
                i = 0;
            }
            summaryContainer.setVisibility(i);
        }
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }
    }
}
