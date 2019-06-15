package com.android.settings.applications.manageapplications;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ApplicationViewHolder extends ViewHolder {
    private final ImageView mAppIcon;
    private final TextView mAppName;
    @VisibleForTesting
    final TextView mDisabled;
    private final boolean mKeepStableHeight;
    @VisibleForTesting
    final TextView mSummary;
    @VisibleForTesting
    View mSummaryContainer;
    @VisibleForTesting
    final Switch mSwitch;
    @VisibleForTesting
    final ViewGroup mWidgetContainer;

    ApplicationViewHolder(View itemView, boolean keepStableHeight) {
        super(itemView);
        this.mAppName = (TextView) itemView.findViewById(16908310);
        this.mAppIcon = (ImageView) itemView.findViewById(16908294);
        this.mSummaryContainer = itemView.findViewById(R.id.summary_container);
        this.mSummary = (TextView) itemView.findViewById(16908304);
        this.mDisabled = (TextView) itemView.findViewById(R.id.appendix);
        this.mKeepStableHeight = keepStableHeight;
        this.mSwitch = (Switch) itemView.findViewById(R.id.switchWidget);
        this.mWidgetContainer = (ViewGroup) itemView.findViewById(16908312);
    }

    static View newView(ViewGroup parent) {
        return newView(parent, false);
    }

    static View newView(ViewGroup parent, boolean twoTarget) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_app, parent, false);
        if (twoTarget) {
            ViewGroup widgetFrame = (ViewGroup) view.findViewById(16908312);
            if (widgetFrame != null) {
                LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_widget_master_switch, widgetFrame, true);
                view.addView(LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_two_target_divider, view, false), view.getChildCount() - 1);
            }
        }
        return view;
    }

    /* Access modifiers changed, original: 0000 */
    public void setSummary(CharSequence summary) {
        this.mSummary.setText(summary);
        updateSummaryContainer();
    }

    /* Access modifiers changed, original: 0000 */
    public void setSummary(@StringRes int summary) {
        this.mSummary.setText(summary);
        updateSummaryContainer();
    }

    /* Access modifiers changed, original: 0000 */
    public void setEnabled(boolean isEnabled) {
        this.itemView.setEnabled(isEnabled);
    }

    /* Access modifiers changed, original: 0000 */
    public void setTitle(CharSequence title) {
        if (title != null) {
            this.mAppName.setText(title);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setIcon(int drawableRes) {
        this.mAppIcon.setImageResource(drawableRes);
    }

    /* Access modifiers changed, original: 0000 */
    public void setIcon(Drawable icon) {
        if (icon != null) {
            this.mAppIcon.setImageDrawable(icon);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateDisableView(ApplicationInfo info) {
        if ((info.flags & 8388608) == 0) {
            this.mDisabled.setVisibility(0);
            this.mDisabled.setText(R.string.not_installed);
        } else if (!info.enabled || info.enabledSetting == 4) {
            this.mDisabled.setVisibility(0);
            this.mDisabled.setText(R.string.disabled);
        } else {
            this.mDisabled.setVisibility(8);
        }
        updateSummaryContainer();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSummaryContainer() {
        int i = 0;
        if (this.mKeepStableHeight) {
            this.mSummaryContainer.setVisibility(0);
            return;
        }
        boolean hasContent = (TextUtils.isEmpty(this.mDisabled.getText()) && TextUtils.isEmpty(this.mSummary.getText())) ? false : true;
        View view = this.mSummaryContainer;
        if (!hasContent) {
            i = 8;
        }
        view.setVisibility(i);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSizeText(AppEntry entry, CharSequence invalidSizeStr, int whichSize) {
        if (ManageApplications.DEBUG) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateSizeText of ");
            stringBuilder.append(entry.label);
            stringBuilder.append(" ");
            stringBuilder.append(entry);
            stringBuilder.append(": ");
            stringBuilder.append(entry.sizeStr);
            Log.d("ManageApplications", stringBuilder.toString());
        }
        if (entry.sizeStr != null) {
            switch (whichSize) {
                case 1:
                    setSummary(entry.internalSizeStr);
                    return;
                case 2:
                    setSummary(entry.externalSizeStr);
                    return;
                default:
                    setSummary(entry.sizeStr);
                    return;
            }
        } else if (entry.size == -2) {
            setSummary(invalidSizeStr);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateSwitch(OnClickListener listener, boolean enabled, boolean checked) {
        if (this.mSwitch != null && this.mWidgetContainer != null) {
            this.mWidgetContainer.setOnClickListener(listener);
            this.mSwitch.setChecked(checked);
            this.mSwitch.setEnabled(enabled);
        }
    }
}
