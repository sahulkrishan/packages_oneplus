package com.android.settingslib;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

public class CustomDialogPreference extends DialogPreference {
    private CustomPreferenceDialogFragment mFragment;
    private OnShowListener mOnShowListener;

    public static class CustomPreferenceDialogFragment extends PreferenceDialogFragment {
        public static CustomPreferenceDialogFragment newInstance(String key) {
            CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomDialogPreference getCustomizablePreference() {
            return (CustomDialogPreference) getPreference();
        }

        /* Access modifiers changed, original: protected */
        public void onPrepareDialogBuilder(Builder builder) {
            super.onPrepareDialogBuilder(builder);
            getCustomizablePreference().setFragment(this);
            getCustomizablePreference().onPrepareDialogBuilder(builder, this);
        }

        public void onDialogClosed(boolean positiveResult) {
            getCustomizablePreference().onDialogClosed(positiveResult);
        }

        /* Access modifiers changed, original: protected */
        public void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getCustomizablePreference().onBindDialogView(view);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setOnShowListener(getCustomizablePreference().getOnShowListener());
            return dialog;
        }

        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            getCustomizablePreference().onClick(dialog, which);
        }
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDialogPreference(Context context) {
        super(context);
    }

    public boolean isDialogOpen() {
        return getDialog() != null && getDialog().isShowing();
    }

    public Dialog getDialog() {
        return this.mFragment != null ? this.mFragment.getDialog() : null;
    }

    public void setOnShowListener(OnShowListener listner) {
        this.mOnShowListener = listner;
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(boolean positiveResult) {
    }

    /* Access modifiers changed, original: protected */
    public void onClick(DialogInterface dialog, int which) {
    }

    /* Access modifiers changed, original: protected */
    public void onBindDialogView(View view) {
    }

    private void setFragment(CustomPreferenceDialogFragment fragment) {
        this.mFragment = fragment;
    }

    private OnShowListener getOnShowListener() {
        return this.mOnShowListener;
    }
}
