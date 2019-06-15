package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CancellablePreference extends Preference implements OnClickListener {
    private boolean mCancellable;
    private OnCancelListener mListener;

    public interface OnCancelListener {
        void onCancel(CancellablePreference cancellablePreference);
    }

    public CancellablePreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.cancel_pref_widget);
    }

    public CancellablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.cancel_pref_widget);
    }

    public void setCancellable(boolean isCancellable) {
        this.mCancellable = isCancellable;
        notifyChanged();
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mListener = listener;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        ImageView cancel = (ImageView) view.findViewById(R.id.cancel);
        cancel.setVisibility(this.mCancellable ? 0 : 4);
        cancel.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (this.mListener != null) {
            this.mListener.onCancel(this);
        }
    }
}
