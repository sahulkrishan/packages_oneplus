package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
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
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkBoxPreferenceStyle);
    }

    public RadioButtonPreference(Context context) {
        this(context, null);
    }

    /* Access modifiers changed, original: 0000 */
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
        view.setDividerAllowedBelow(false);
        view.setDividerAllowedAbove(false);
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }
    }
}
