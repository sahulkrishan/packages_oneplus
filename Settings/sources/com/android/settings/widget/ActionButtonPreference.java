package com.android.settings.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;

public class ActionButtonPreference extends Preference {
    private final ButtonInfo mButton1Info = new ButtonInfo();
    private final ButtonInfo mButton2Info = new ButtonInfo();

    static class ButtonInfo {
        private boolean mIsEnabled = true;
        private boolean mIsPositive = true;
        private boolean mIsVisible = true;
        private OnClickListener mListener;
        private Button mNegativeButton;
        private Button mPositiveButton;
        private CharSequence mText;

        ButtonInfo() {
        }

        /* Access modifiers changed, original: 0000 */
        public void setUpButton() {
            setUpButton(this.mPositiveButton);
            setUpButton(this.mNegativeButton);
            if (!this.mIsVisible) {
                this.mPositiveButton.setVisibility(4);
                this.mNegativeButton.setVisibility(4);
            } else if (this.mIsPositive) {
                this.mPositiveButton.setVisibility(0);
                this.mNegativeButton.setVisibility(4);
            } else {
                this.mPositiveButton.setVisibility(4);
                this.mNegativeButton.setVisibility(0);
            }
        }

        private void setUpButton(Button button) {
            button.setText(this.mText);
            button.setOnClickListener(this.mListener);
            button.setEnabled(this.mIsEnabled);
        }
    }

    public ActionButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ActionButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ActionButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionButtonPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.two_action_buttons);
        setSelectable(false);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
        this.mButton1Info.mPositiveButton = (Button) holder.findViewById(R.id.button1_positive);
        this.mButton1Info.mNegativeButton = (Button) holder.findViewById(R.id.button1_negative);
        this.mButton2Info.mPositiveButton = (Button) holder.findViewById(R.id.button2_positive);
        this.mButton2Info.mNegativeButton = (Button) holder.findViewById(R.id.button2_negative);
        this.mButton1Info.setUpButton();
        this.mButton2Info.setUpButton();
    }

    public ActionButtonPreference setButton1Text(@StringRes int textResId) {
        String newText = getContext().getString(textResId);
        if (!TextUtils.equals(newText, this.mButton1Info.mText)) {
            this.mButton1Info.mText = newText;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton1Enabled(boolean isEnabled) {
        if (isEnabled != this.mButton1Info.mIsEnabled) {
            this.mButton1Info.mIsEnabled = isEnabled;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton2Text(@StringRes int textResId) {
        String newText = getContext().getString(textResId);
        if (!TextUtils.equals(newText, this.mButton2Info.mText)) {
            this.mButton2Info.mText = newText;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton2Enabled(boolean isEnabled) {
        if (isEnabled != this.mButton2Info.mIsEnabled) {
            this.mButton2Info.mIsEnabled = isEnabled;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton1OnClickListener(OnClickListener listener) {
        if (listener != this.mButton1Info.mListener) {
            this.mButton1Info.mListener = listener;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton2OnClickListener(OnClickListener listener) {
        if (listener != this.mButton2Info.mListener) {
            this.mButton2Info.mListener = listener;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton1Positive(boolean isPositive) {
        if (isPositive != this.mButton1Info.mIsPositive) {
            this.mButton1Info.mIsPositive = isPositive;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton2Positive(boolean isPositive) {
        if (isPositive != this.mButton2Info.mIsPositive) {
            this.mButton2Info.mIsPositive = isPositive;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton1Visible(boolean isPositive) {
        if (isPositive != this.mButton1Info.mIsVisible) {
            this.mButton1Info.mIsVisible = isPositive;
            notifyChanged();
        }
        return this;
    }

    public ActionButtonPreference setButton2Visible(boolean isPositive) {
        if (isPositive != this.mButton2Info.mIsVisible) {
            this.mButton2Info.mIsVisible = isPositive;
            notifyChanged();
        }
        return this;
    }
}
