package com.android.settings.network;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Carriers;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import com.android.settings.R;

public class ApnPreference extends Preference implements OnCheckedChangeListener, OnClickListener {
    static final String TAG = "ApnPreference";
    private static CompoundButton mCurrentChecked = null;
    private static String mSelectedKey = null;
    private boolean mProtectFromCheckedChange;
    private boolean mSelectable;
    private int mSubId;

    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSubId = -1;
        this.mProtectFromCheckedChange = false;
        this.mSelectable = true;
    }

    public ApnPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.apnPreferenceStyle);
    }

    public ApnPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View widget = view.findViewById(R.id.apn_radiobutton);
        if (widget != null && (widget instanceof RadioButton)) {
            RadioButton rb = (RadioButton) widget;
            if (this.mSelectable) {
                rb.setOnCheckedChangeListener(this);
                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = rb;
                    mSelectedKey = getKey();
                }
                this.mProtectFromCheckedChange = true;
                rb.setChecked(isChecked);
                this.mProtectFromCheckedChange = false;
                rb.setVisibility(0);
            } else {
                rb.setVisibility(8);
            }
        }
        View textLayout = view.findViewById(R.id.text_layout);
        if (textLayout != null && (textLayout instanceof RelativeLayout)) {
            textLayout.setOnClickListener(this);
        }
    }

    public boolean isChecked() {
        return getKey().equals(mSelectedKey);
    }

    public void setChecked() {
        mSelectedKey = getKey();
    }

    public void unsetChecked() {
        if (mCurrentChecked != null && getKey() == mSelectedKey) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("unsetChecked mCurrentChecked.setChecked(false) getKey()  = ");
            stringBuilder.append(getKey());
            Log.d(str, stringBuilder.toString());
            mCurrentChecked.setChecked(false);
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ID: ");
        stringBuilder.append(getKey());
        stringBuilder.append(" :");
        stringBuilder.append(isChecked);
        Log.i(str, stringBuilder.toString());
        if (!this.mProtectFromCheckedChange) {
            if (isChecked) {
                if (mCurrentChecked != null) {
                    mCurrentChecked.setChecked(false);
                }
                mCurrentChecked = buttonView;
                mSelectedKey = getKey();
                callChangeListener(mSelectedKey);
            } else {
                mCurrentChecked = null;
                mSelectedKey = null;
            }
        }
    }

    public void onClick(View v) {
        if (v != null && R.id.text_layout == v.getId()) {
            Context context = getContext();
            if (context != null) {
                String str = "android.intent.action.EDIT";
                Intent editIntent = new Intent(str, ContentUris.withAppendedId(Carriers.CONTENT_URI, (long) Integer.parseInt(getKey())));
                editIntent.putExtra("sub_id", this.mSubId);
                context.startActivity(editIntent);
            }
        }
    }

    public void setSelectable(boolean selectable) {
        this.mSelectable = selectable;
    }

    public boolean getSelectable() {
        return this.mSelectable;
    }

    public void setSubId(int subId) {
        this.mSubId = subId;
    }
}
