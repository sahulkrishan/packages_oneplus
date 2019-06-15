package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import android.view.View;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;

public class TimeoutListPreference extends RestrictedListPreference {
    private EnforcedAdmin mAdmin;
    private final CharSequence[] mInitialEntries = getEntries();
    private final CharSequence[] mInitialValues = getEntryValues();

    public TimeoutListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        if (this.mAdmin != null) {
            builder.setView(R.layout.admin_disabled_other_options_footer);
        } else {
            builder.setView(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        if (this.mAdmin != null) {
            dialog.findViewById(R.id.admin_disabled_other_options).findViewById(R.id.admin_more_details_link).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(TimeoutListPreference.this.getContext(), TimeoutListPreference.this.mAdmin);
                }
            });
        }
    }

    public void removeUnusableTimeouts(long maxTimeout, EnforcedAdmin admin) {
        if (((DevicePolicyManager) getContext().getSystemService("device_policy")) != null) {
            if (admin != null || this.mAdmin != null || isDisabledByAdmin()) {
                int i;
                if (admin == null) {
                    maxTimeout = Long.MAX_VALUE;
                }
                ArrayList<CharSequence> revisedEntries = new ArrayList();
                ArrayList<CharSequence> revisedValues = new ArrayList();
                for (i = 0; i < this.mInitialValues.length; i++) {
                    if (Long.parseLong(this.mInitialValues[i].toString()) <= maxTimeout) {
                        revisedEntries.add(this.mInitialEntries[i]);
                        revisedValues.add(this.mInitialValues[i]);
                    }
                }
                if (revisedValues.size() == 0) {
                    setDisabledByAdmin(admin);
                    return;
                }
                setDisabledByAdmin(null);
                if (revisedEntries.size() != getEntries().length) {
                    i = Integer.parseInt(getValue());
                    setEntries((CharSequence[]) revisedEntries.toArray(new CharSequence[0]));
                    setEntryValues((CharSequence[]) revisedValues.toArray(new CharSequence[0]));
                    this.mAdmin = admin;
                    if (((long) i) <= maxTimeout) {
                        setValue(String.valueOf(i));
                    } else if (revisedValues.size() > 0 && Long.parseLong(((CharSequence) revisedValues.get(revisedValues.size() - 1)).toString()) == maxTimeout) {
                        setValue(String.valueOf(maxTimeout));
                    }
                }
            }
        }
    }
}
