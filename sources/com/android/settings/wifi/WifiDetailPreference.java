package com.android.settings.wifi;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.R;

public class WifiDetailPreference extends Preference {
    private String mDetailText;

    public WifiDetailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_summary);
    }

    public void setDetailText(String text) {
        if (!TextUtils.equals(this.mDetailText, text)) {
            this.mDetailText = text;
            notifyChanged();
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView textView = (TextView) view.findViewById(R.id.widget_summary);
        textView.setText(this.mDetailText);
        textView.setPadding(0, 0, 10, 0);
    }
}
