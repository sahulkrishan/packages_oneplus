package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.widget.AppPreference;
import com.android.settingslib.Utils;
import com.oneplus.settings.utils.OPUtils;

public class PowerGaugePreference extends AppPreference {
    private View appendix;
    private CharSequence mContentDescription;
    private Context mContext;
    private BatteryEntry mInfo;
    private OnClickListener mOnClickListener;
    private int mPowerState;
    private CharSequence mProgress;
    private boolean mShowAnomalyIcon;
    private View mSwitch;

    public PowerGaugePreference(Context context, Drawable icon, CharSequence contentDescription, BatteryEntry info) {
        this(context, null, icon, contentDescription, info);
    }

    public PowerGaugePreference(Context context) {
        this(context, null, null, null, null);
    }

    public PowerGaugePreference(Context context, AttributeSet attrs) {
        this(context, attrs, null, null, null);
    }

    private PowerGaugePreference(Context context, AttributeSet attrs, Drawable icon, CharSequence contentDescription, BatteryEntry info) {
        super(context, attrs);
        this.mPowerState = -1;
        setIcon(icon != null ? icon : new ColorDrawable(0));
        setWidgetLayoutResource(R.layout.preference_widget_summary);
        this.mInfo = info;
        this.mContentDescription = contentDescription == null ? context.getResources().getString(R.string.op_app_already_uninstalled) : contentDescription;
        this.mContext = context;
        this.mShowAnomalyIcon = false;
    }

    public void setContentDescription(String name) {
        this.mContentDescription = name;
        notifyChanged();
    }

    public void setPercent(double percentOfTotal) {
        this.mProgress = Utils.formatPercentage(percentOfTotal, true);
        updatePowerState();
        notifyChanged();
    }

    public String getPercent() {
        return this.mProgress.toString();
    }

    public void setState(int state) {
        this.mPowerState = state;
        updatePowerState();
    }

    public void setOnButtonClickListener(OnClickListener onClickListener) {
        setOnClickListener(this.mSwitch, onClickListener);
    }

    private void setOnClickListener(View v, OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
        if (v != null) {
            v.setOnClickListener(this.mOnClickListener);
            if (this.mInfo != null && this.mInfo.defaultPackageName != null) {
                v.setTag(this.mInfo.defaultPackageName);
            }
        }
    }

    private void shadowView() {
        if (this.appendix != null) {
            this.appendix.setVisibility(8);
        }
    }

    private void updatePowerState() {
        if (1 == this.mPowerState) {
            setSummary((CharSequence) convertToSpanned(this.mContext.getResources().getString(R.string.high_power_app_text_red, new Object[]{this.mProgress}), "D94B41"));
        } else if (this.mPowerState == 0) {
            setSummary((CharSequence) this.mContext.getResources().getString(R.string.high_power_app_text_nomal, new Object[]{this.mProgress}));
            shadowView();
        } else {
            setSummary((CharSequence) this.mProgress.toString());
        }
        setButtonVisible();
    }

    private void setButtonVisible() {
        if (this.mSwitch == null) {
            return;
        }
        if (this.mPowerState == 0 || 1 == this.mPowerState) {
            this.mSwitch.setVisibility(0);
            shadowView();
            return;
        }
        this.mSwitch.setVisibility(8);
    }

    private Spanned convertToSpanned(String txt, String color) {
        String colorfulTxt = new StringBuilder();
        colorfulTxt.append("<font color=\"#");
        colorfulTxt.append(color);
        colorfulTxt.append("\">");
        colorfulTxt.append(txt);
        colorfulTxt.append("</font>");
        colorfulTxt = colorfulTxt.toString();
        if (VERSION.SDK_INT >= 24) {
            return Html.fromHtml(colorfulTxt, null);
        }
        return Html.fromHtml(colorfulTxt);
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mProgress = subtitle;
        setSummary(this.mProgress);
        notifyChanged();
    }

    public CharSequence getSubtitle() {
        return this.mProgress;
    }

    public void shouldShowAnomalyIcon(boolean showAnomalyIcon) {
        this.mShowAnomalyIcon = showAnomalyIcon;
        notifyChanged();
    }

    public boolean showAnomalyIcon() {
        return this.mShowAnomalyIcon;
    }

    /* Access modifiers changed, original: 0000 */
    public BatteryEntry getInfo() {
        return this.mInfo;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.appendix = view.findViewById(R.id.appendix);
        TextView subtitle = (TextView) view.findViewById(R.id.widget_summary);
        subtitle.setText(this.mProgress);
        if (this.mShowAnomalyIcon) {
            subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning_24dp, 0, 0, 0);
        } else {
            subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        }
        this.mSwitch = view.findViewById(R.id.widget_summary);
        ColorStateList mTextButtonColor = OPUtils.creatOneplusPrimaryColorStateList(this.mContext);
        ((TextView) this.mSwitch).setText(R.string.oneplus_stop_run);
        ((TextView) this.mSwitch).setTextColor(this.mContext.getResources().getColor(R.color.oneplus_accent_color));
        setOnClickListener(this.mSwitch, this.mOnClickListener);
        setButtonVisible();
        if (this.mContentDescription != null) {
            ((TextView) view.findViewById(16908310)).setContentDescription(this.mContentDescription);
        }
    }
}
