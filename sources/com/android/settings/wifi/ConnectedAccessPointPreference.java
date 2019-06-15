package com.android.settings.wifi;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.R;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPointPreference;
import com.android.settingslib.wifi.AccessPointPreference.UserBadgeCache;

public class ConnectedAccessPointPreference extends AccessPointPreference implements OnClickListener {
    private final Fragment mFragment;
    private boolean mIsCaptivePortal;
    private OnGearClickListener mOnGearClickListener;

    public interface OnGearClickListener {
        void onGearClick(ConnectedAccessPointPreference connectedAccessPointPreference);
    }

    public ConnectedAccessPointPreference(AccessPoint accessPoint, Context context, UserBadgeCache cache, @DrawableRes int iconResId, boolean forSavedNetworks, Fragment fragment) {
        super(accessPoint, context, cache, iconResId, forSavedNetworks);
        this.mFragment = fragment;
    }

    /* Access modifiers changed, original: protected */
    public int getWidgetLayoutResourceId() {
        return R.layout.preference_widget_gear_optional_background;
    }

    public void refresh() {
        super.refresh();
        setShowDivider(this.mIsCaptivePortal);
        if (this.mIsCaptivePortal) {
            setSummary((int) R.string.wifi_tap_to_sign_in);
        }
    }

    public void setOnGearClickListener(OnGearClickListener l) {
        this.mOnGearClickListener = l;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View gear = holder.findViewById(R.id.settings_button);
        gear.setOnClickListener(this);
        int i = 0;
        holder.findViewById(R.id.settings_button_no_background).setVisibility(this.mIsCaptivePortal ? 4 : 0);
        if (!this.mIsCaptivePortal) {
            i = 4;
        }
        gear.setVisibility(i);
        if (this.mFragment != null) {
            holder.itemView.setOnCreateContextMenuListener(this.mFragment);
            holder.itemView.setTag(this);
            holder.itemView.setLongClickable(true);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.settings_button && this.mOnGearClickListener != null) {
            this.mOnGearClickListener.onGearClick(this);
        }
    }

    public void setCaptivePortal(boolean isCaptivePortal) {
        if (this.mIsCaptivePortal != isCaptivePortal) {
            this.mIsCaptivePortal = isCaptivePortal;
            refresh();
        }
    }
}
