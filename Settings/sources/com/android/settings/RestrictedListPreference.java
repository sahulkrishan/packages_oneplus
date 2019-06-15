package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreferenceHelper;
import java.util.ArrayList;
import java.util.List;

public class RestrictedListPreference extends CustomListPreference {
    private final RestrictedPreferenceHelper mHelper;
    private int mProfileUserId;
    private boolean mRequiresActiveUnlockedProfile = false;
    private final List<RestrictedItem> mRestrictedItems = new ArrayList();

    public class RestrictedArrayAdapter extends ArrayAdapter<CharSequence> {
        private final int mSelectedIndex;

        public RestrictedArrayAdapter(Context context, CharSequence[] objects, int selectedIndex) {
            super(context, R.layout.restricted_dialog_singlechoice, R.id.text1, objects);
            this.mSelectedIndex = selectedIndex;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View root = super.getView(position, convertView, parent);
            CheckedTextView text = (CheckedTextView) root.findViewById(R.id.text1);
            ImageView padlock = (ImageView) root.findViewById(R.id.restricted_lock_icon);
            boolean z = false;
            if (RestrictedListPreference.this.isRestrictedForEntry((CharSequence) getItem(position))) {
                text.setEnabled(false);
                text.setChecked(false);
                padlock.setVisibility(0);
            } else {
                if (this.mSelectedIndex != -1) {
                    if (position == this.mSelectedIndex) {
                        z = true;
                    }
                    text.setChecked(z);
                }
                if (!text.isEnabled()) {
                    text.setEnabled(true);
                }
                padlock.setVisibility(8);
            }
            return root;
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class RestrictedItem {
        public final EnforcedAdmin enforcedAdmin;
        public final CharSequence entry;
        public final CharSequence entryValue;

        public RestrictedItem(CharSequence entry, CharSequence entryValue, EnforcedAdmin enforcedAdmin) {
            this.entry = entry;
            this.entryValue = entryValue;
            this.enforcedAdmin = enforcedAdmin;
        }
    }

    public static class RestrictedListPreferenceDialogFragment extends CustomListPreferenceDialogFragment {
        private int mLastCheckedPosition = -1;

        public static ListPreferenceDialogFragment newInstance(String key) {
            ListPreferenceDialogFragment fragment = new RestrictedListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private RestrictedListPreference getCustomizablePreference() {
            return (RestrictedListPreference) getPreference();
        }

        /* Access modifiers changed, original: protected */
        public OnClickListener getOnItemClickListener() {
            return new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RestrictedListPreference preference = RestrictedListPreferenceDialogFragment.this.getCustomizablePreference();
                    if (which >= 0 && which < preference.getEntryValues().length) {
                        RestrictedItem item = preference.getRestrictedItemForEntryValue(preference.getEntryValues()[which].toString());
                        if (item != null) {
                            ((AlertDialog) dialog).getListView().setItemChecked(RestrictedListPreferenceDialogFragment.this.getLastCheckedPosition(), true);
                            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(RestrictedListPreferenceDialogFragment.this.getContext(), item.enforcedAdmin);
                        } else {
                            RestrictedListPreferenceDialogFragment.this.setClickedDialogEntryIndex(which);
                        }
                        if (RestrictedListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                            RestrictedListPreferenceDialogFragment.this.onClick(dialog, -1);
                            dialog.dismiss();
                        }
                    }
                }
            };
        }

        private int getLastCheckedPosition() {
            if (this.mLastCheckedPosition == -1) {
                this.mLastCheckedPosition = getCustomizablePreference().getSelectedValuePos();
            }
            return this.mLastCheckedPosition;
        }

        private void setCheckedPosition(int checkedPosition) {
            this.mLastCheckedPosition = checkedPosition;
        }

        /* Access modifiers changed, original: protected */
        public void setClickedDialogEntryIndex(int which) {
            super.setClickedDialogEntryIndex(which);
            this.mLastCheckedPosition = which;
        }
    }

    public RestrictedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.restricted_icon);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
    }

    public RestrictedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHelper = new RestrictedPreferenceHelper(context, this, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        this.mHelper.onBindViewHolder(holder);
        View restrictedIcon = holder.findViewById(R.id.restricted_icon);
        if (restrictedIcon != null) {
            restrictedIcon.setVisibility(isDisabledByAdmin() ? 0 : 8);
        }
    }

    public void performClick() {
        if (this.mRequiresActiveUnlockedProfile) {
            if (!Utils.startQuietModeDialogIfNecessary(getContext(), UserManager.get(getContext()), this.mProfileUserId)) {
                KeyguardManager manager = (KeyguardManager) getContext().getSystemService("keyguard");
                if (manager.isDeviceLocked(this.mProfileUserId)) {
                    getContext().startActivity(manager.createConfirmDeviceCredentialIntent(null, null, this.mProfileUserId));
                    return;
                }
            }
            return;
        }
        if (!this.mHelper.performClick()) {
            super.performClick();
        }
    }

    public void setEnabled(boolean enabled) {
        if (enabled && isDisabledByAdmin()) {
            this.mHelper.setDisabledByAdmin(null);
        } else {
            super.setEnabled(enabled);
        }
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        if (this.mHelper.setDisabledByAdmin(admin)) {
            notifyChanged();
        }
    }

    public boolean isDisabledByAdmin() {
        return this.mHelper.isDisabledByAdmin();
    }

    public void setRequiresActiveUnlockedProfile(boolean reqState) {
        this.mRequiresActiveUnlockedProfile = reqState;
    }

    public void setProfileUserId(int profileUserId) {
        this.mProfileUserId = profileUserId;
    }

    public boolean isRestrictedForEntry(CharSequence entry) {
        if (entry == null) {
            return false;
        }
        for (RestrictedItem item : this.mRestrictedItems) {
            if (entry.equals(item.entry)) {
                return true;
            }
        }
        return false;
    }

    public void addRestrictedItem(RestrictedItem item) {
        this.mRestrictedItems.add(item);
    }

    public void clearRestrictedItems() {
        this.mRestrictedItems.clear();
    }

    private RestrictedItem getRestrictedItemForEntryValue(CharSequence entryValue) {
        if (entryValue == null) {
            return null;
        }
        for (RestrictedItem item : this.mRestrictedItems) {
            if (entryValue.equals(item.entryValue)) {
                return item;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public ListAdapter createListAdapter() {
        return new RestrictedArrayAdapter(getContext(), getEntries(), getSelectedValuePos());
    }

    public int getSelectedValuePos() {
        String selectedValue = getValue();
        return selectedValue == null ? -1 : findIndexOfValue(selectedValue);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        builder.setAdapter(createListAdapter(), listener);
    }
}
