package com.android.settingslib;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v14.preference.EditTextPreferenceDialogFragment;
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class CustomEditTextPreference extends EditTextPreference {
    private CustomPreferenceDialogFragment mFragment;

    public static class CustomPreferenceDialogFragment extends EditTextPreferenceDialogFragment {
        public static CustomPreferenceDialogFragment newInstance(String key) {
            CustomPreferenceDialogFragment fragment = new CustomPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomEditTextPreference getCustomizablePreference() {
            return (CustomEditTextPreference) getPreference();
        }

        /* Access modifiers changed, original: protected */
        public void onBindDialogView(View view) {
            super.onBindDialogView(view);
            getCustomizablePreference().onBindDialogView(view);
        }

        /* Access modifiers changed, original: protected */
        public void onPrepareDialogBuilder(Builder builder) {
            super.onPrepareDialogBuilder(builder);
            getCustomizablePreference().setFragment(this);
            getCustomizablePreference().onPrepareDialogBuilder(builder, this);
        }

        public void onDialogClosed(boolean positiveResult) {
            super.onDialogClosed(positiveResult);
            getCustomizablePreference().onDialogClosed(positiveResult);
        }

        public void onClick(DialogInterface dialog, int which) {
            super.onClick(dialog, which);
            getCustomizablePreference().onClick(dialog, which);
        }
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

    public EditText getEditText() {
        if (this.mFragment != null) {
            Dialog dialog = this.mFragment.getDialog();
            if (dialog != null) {
                return (EditText) dialog.findViewById(16908291);
            }
        }
        return null;
    }

    public boolean isDialogOpen() {
        return getDialog() != null && getDialog().isShowing();
    }

    public Dialog getDialog() {
        return this.mFragment != null ? this.mFragment.getDialog() : null;
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
    @CallSuper
    public void onBindDialogView(View view) {
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText != null) {
            editText.setInputType(16385);
            editText.requestFocus();
        }
    }

    private void setFragment(CustomPreferenceDialogFragment fragment) {
        this.mFragment = fragment;
    }
}
