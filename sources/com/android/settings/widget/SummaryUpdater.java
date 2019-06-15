package com.android.settings.widget;

import android.content.Context;
import android.text.TextUtils;

public abstract class SummaryUpdater {
    protected final Context mContext;
    private final OnSummaryChangeListener mListener;
    private String mSummary;

    public interface OnSummaryChangeListener {
        void onSummaryChanged(String str);
    }

    public abstract String getSummary();

    public abstract void register(boolean z);

    public SummaryUpdater(Context context, OnSummaryChangeListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    /* Access modifiers changed, original: protected */
    public void notifyChangeIfNeeded() {
        String summary = getSummary();
        if (!TextUtils.equals(this.mSummary, summary)) {
            this.mSummary = summary;
            if (this.mListener != null) {
                this.mListener.onSummaryChanged(summary);
            }
        }
    }
}
