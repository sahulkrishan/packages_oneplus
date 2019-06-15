package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.android.settings.R;

public class OPGestureAnswerCallInStructionsCategory extends Preference {
    private LayoutInflater inflater;
    private Context mContext;
    private int mLayoutResId = R.layout.op_gesture_answer_call_instructions_category;

    public OPGestureAnswerCallInStructionsCategory(Context context) {
        super(context);
        initViews(context);
    }

    public OPGestureAnswerCallInStructionsCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public OPGestureAnswerCallInStructionsCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mContext = context;
        setLayoutResource(this.mLayoutResId);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.setDividerAllowedBelow(true);
    }
}
