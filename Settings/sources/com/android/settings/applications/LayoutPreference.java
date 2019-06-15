package com.android.settings.applications;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.settings.R;
import com.android.settings.Utils;

public class LayoutPreference extends Preference {
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private final OnClickListener mClickListener;
    @VisibleForTesting
    View mRootView;

    public LayoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mClickListener = new -$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q(this);
        init(context, attrs, 0);
    }

    public LayoutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mClickListener = new -$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q(this);
        init(context, attrs, defStyleAttr);
    }

    public LayoutPreference(Context context, int resource) {
        this(context, LayoutInflater.from(context).inflate(resource, null, false));
    }

    public LayoutPreference(Context context, View view) {
        super(context);
        this.mClickListener = new -$$Lambda$LayoutPreference$-oL9WiG-H2u60jStTsEh5xi7a7Q(this);
        setView(view);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        this.mAllowDividerAbove = TypedArrayUtils.getBoolean(a, 16, 16, false);
        this.mAllowDividerBelow = TypedArrayUtils.getBoolean(a, 17, 17, false);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Preference, defStyleAttr, 0);
        int layoutResource = a.getResourceId(3, 0);
        if (layoutResource != 0) {
            a.recycle();
            setView(LayoutInflater.from(getContext()).inflate(layoutResource, null, false));
            return;
        }
        throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
    }

    private void setView(View view) {
        setLayoutResource(R.layout.layout_preference_frame);
        ViewGroup allDetails = (ViewGroup) view.findViewById(R.id.all_details);
        if (allDetails != null) {
            Utils.forceCustomPadding(allDetails, true);
        }
        this.mRootView = view;
        setShouldDisableView(false);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(this.mClickListener);
        boolean selectable = isSelectable();
        holder.itemView.setFocusable(selectable);
        holder.itemView.setClickable(selectable);
        holder.setDividerAllowedAbove(this.mAllowDividerAbove);
        holder.setDividerAllowedBelow(this.mAllowDividerBelow);
        FrameLayout layout = holder.itemView;
        layout.removeAllViews();
        ViewGroup parent = (ViewGroup) this.mRootView.getParent();
        if (parent != null) {
            parent.removeView(this.mRootView);
        }
        layout.addView(this.mRootView);
    }

    public <T extends View> T findViewById(int id) {
        return this.mRootView.findViewById(id);
    }
}
