package com.android.settings.notification;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.text.BidiFormatter;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;

public class HeaderPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, LifecycleObserver {
    private final PreferenceFragment mFragment;
    private EntityHeaderController mHeaderController;
    private boolean mStarted = false;

    public HeaderPreferenceController(Context context, PreferenceFragment fragment) {
        super(context, null);
        this.mFragment = fragment;
    }

    public String getPreferenceKey() {
        return EntityHeaderController.PREF_KEY_APP_HEADER;
    }

    public boolean isAvailable() {
        return this.mAppRow != null;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null && this.mFragment != null) {
            Activity activity = null;
            if (this.mStarted) {
                activity = this.mFragment.getActivity();
            }
            this.mHeaderController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, ((LayoutPreference) preference).findViewById(R.id.entity_header));
            this.mHeaderController.setIcon(this.mAppRow.icon).setLabel(getLabel()).setSummary(getSummary()).setPackageName(this.mAppRow.pkg).setUid(this.mAppRow.uid).setButtonActions(1, 0).setHasAppInfoLink(true).done(activity, this.mContext).findViewById(R.id.entity_header).setVisibility(0);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public CharSequence getLabel() {
        if (this.mChannel != null && !isDefaultChannel()) {
            return this.mChannel.getName();
        }
        if (this.mChannelGroup != null) {
            return this.mChannelGroup.getName();
        }
        return this.mAppRow.label;
    }

    public CharSequence getSummary() {
        if (this.mChannel == null || isDefaultChannel()) {
            if (this.mChannelGroup != null) {
                return this.mAppRow.label.toString();
            }
            return "";
        } else if (this.mChannelGroup == null || TextUtils.isEmpty(this.mChannelGroup.getName())) {
            return this.mAppRow.label.toString();
        } else {
            SpannableStringBuilder summary = new SpannableStringBuilder();
            BidiFormatter bidi = BidiFormatter.getInstance();
            summary.append(bidi.unicodeWrap(this.mAppRow.label.toString()));
            summary.append(bidi.unicodeWrap(this.mContext.getText(R.string.notification_header_divider_symbol_with_spaces)));
            summary.append(bidi.unicodeWrap(this.mChannelGroup.getName().toString()));
            return summary.toString();
        }
    }

    @OnLifecycleEvent(Event.ON_START)
    public void onStart() {
        this.mStarted = true;
        if (this.mHeaderController != null) {
            this.mHeaderController.styleActionBar(this.mFragment.getActivity());
        }
    }
}
