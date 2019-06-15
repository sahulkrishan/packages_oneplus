package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.RestrictedListPreference.RestrictedArrayAdapter;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class NotificationLockscreenPreference extends RestrictedListPreference {
    private EnforcedAdmin mAdminRestrictingRemoteInput;
    private boolean mAllowRemoteInput;
    private Listener mListener;
    private boolean mRemoteInputCheckBoxEnabled = true;
    private boolean mShowRemoteInput;
    private int mUserId = UserHandle.myUserId();

    private class Listener implements OnClickListener, OnCheckedChangeListener, View.OnClickListener {
        private final OnClickListener mInner;
        private View mView;

        public Listener(OnClickListener inner) {
            this.mInner = inner;
        }

        public void onClick(DialogInterface dialog, int which) {
            this.mInner.onClick(dialog, which);
            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
            if (this.mView != null) {
                this.mView.setVisibility(NotificationLockscreenPreference.this.checkboxVisibilityForSelectedIndex(selectedPosition, NotificationLockscreenPreference.this.mShowRemoteInput));
            }
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            NotificationLockscreenPreference.this.mAllowRemoteInput = isChecked ^ 1;
        }

        public void setView(View view) {
            this.mView = view;
        }

        public void onClick(View v) {
            if (v.getId() == 16908820) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(NotificationLockscreenPreference.this.getContext(), NotificationLockscreenPreference.this.mAdminRestrictingRemoteInput);
            }
        }
    }

    public NotificationLockscreenPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRemoteInputCheckBoxEnabled(boolean enabled) {
        this.mRemoteInputCheckBoxEnabled = enabled;
    }

    public void setRemoteInputRestricted(EnforcedAdmin admin) {
        this.mAdminRestrictingRemoteInput = admin;
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        Context context = getContext();
        if (!Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), this.mUserId)) {
            super.onClick();
        }
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener innerListener) {
        this.mListener = new Listener(innerListener);
        builder.setSingleChoiceItems(createListAdapter(), getSelectedValuePos(), this.mListener);
        boolean z = true;
        this.mShowRemoteInput = getEntryValues().length == 3;
        if (Secure.getInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", 0) == 0) {
            z = false;
        }
        this.mAllowRemoteInput = z;
        builder.setView(R.layout.lockscreen_remote_input);
    }

    /* Access modifiers changed, original: protected */
    public void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        CheckBox checkbox = (CheckBox) dialog.findViewById(R.id.lockscreen_remote_input);
        boolean z = true;
        checkbox.setChecked(this.mAllowRemoteInput ^ 1);
        checkbox.setOnCheckedChangeListener(this.mListener);
        if (this.mAdminRestrictingRemoteInput != null) {
            z = false;
        }
        checkbox.setEnabled(z);
        dialog.findViewById(R.id.restricted_lock_icon_remote_input).setVisibility(this.mAdminRestrictingRemoteInput == null ? 8 : 0);
        if (this.mAdminRestrictingRemoteInput != null) {
            checkbox.setClickable(false);
            dialog.findViewById(16908820).setOnClickListener(this.mListener);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
        super.onDialogStateRestored(dialog, savedInstanceState);
        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        View panel = dialog.findViewById(16908820);
        panel.setVisibility(checkboxVisibilityForSelectedIndex(selectedPosition, this.mShowRemoteInput));
        this.mListener.setView(panel);
    }

    /* Access modifiers changed, original: protected */
    public ListAdapter createListAdapter() {
        return new RestrictedArrayAdapter(getContext(), getEntries(), -1);
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Secure.putInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", this.mAllowRemoteInput);
    }

    /* Access modifiers changed, original: protected */
    public boolean isAutoClosePreference() {
        return false;
    }

    private int checkboxVisibilityForSelectedIndex(int selected, boolean showRemoteAtAll) {
        if (selected == 1 && showRemoteAtAll && this.mRemoteInputCheckBoxEnabled) {
            return 0;
        }
        return 8;
    }
}
