package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v14.preference.ListPreferenceDialogFragment;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class CustomListPreference extends ListPreference {

    public static class CustomListPreferenceDialogFragment extends ListPreferenceDialogFragment {
        private static final String KEY_CLICKED_ENTRY_INDEX = "settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX";
        private int mClickedDialogEntryIndex;

        public static ListPreferenceDialogFragment newInstance(String key) {
            ListPreferenceDialogFragment fragment = new CustomListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private CustomListPreference getCustomizablePreference() {
            return (CustomListPreference) getPreference();
        }

        /* Access modifiers changed, original: protected */
        public void onPrepareDialogBuilder(Builder builder) {
            super.onPrepareDialogBuilder(builder);
            this.mClickedDialogEntryIndex = getCustomizablePreference().findIndexOfValue(getCustomizablePreference().getValue());
            getCustomizablePreference().onPrepareDialogBuilder(builder, getOnItemClickListener());
            if (!getCustomizablePreference().isAutoClosePreference()) {
                builder.setPositiveButton(R.string.okay, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CustomListPreferenceDialogFragment.this.onItemChosen();
                    }
                });
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (savedInstanceState != null) {
                this.mClickedDialogEntryIndex = savedInstanceState.getInt(KEY_CLICKED_ENTRY_INDEX, this.mClickedDialogEntryIndex);
            }
            getCustomizablePreference().onDialogCreated(dialog);
            return dialog;
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(KEY_CLICKED_ENTRY_INDEX, this.mClickedDialogEntryIndex);
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getCustomizablePreference().onDialogStateRestored(getDialog(), savedInstanceState);
        }

        /* Access modifiers changed, original: protected */
        public OnClickListener getOnItemClickListener() {
            return new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    CustomListPreferenceDialogFragment.this.setClickedDialogEntryIndex(which);
                    if (CustomListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                        CustomListPreferenceDialogFragment.this.onItemChosen();
                    }
                }
            };
        }

        /* Access modifiers changed, original: protected */
        public void setClickedDialogEntryIndex(int which) {
            this.mClickedDialogEntryIndex = which;
        }

        private String getValue() {
            ListPreference preference = getCustomizablePreference();
            if (this.mClickedDialogEntryIndex < 0 || preference.getEntryValues() == null) {
                return null;
            }
            return preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
        }

        /* Access modifiers changed, original: protected */
        public void onItemChosen() {
            CharSequence message = getCustomizablePreference().getConfirmationMessage(getValue());
            if (message != null) {
                Fragment f = new ConfirmDialogFragment();
                Bundle args = new Bundle();
                args.putCharSequence("android.intent.extra.TEXT", message);
                f.setArguments(args);
                f.setTargetFragment(this, 0);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getTag());
                stringBuilder.append("-Confirm");
                ft.add(f, stringBuilder.toString());
                ft.commitAllowingStateLoss();
                return;
            }
            onItemConfirmed();
        }

        /* Access modifiers changed, original: protected */
        public void onItemConfirmed() {
            onClick(getDialog(), -1);
            getDialog().dismiss();
        }

        public void onDialogClosed(boolean positiveResult) {
            getCustomizablePreference().onDialogClosed(positiveResult);
            ListPreference preference = getCustomizablePreference();
            String value = getValue();
            if (positiveResult && value != null && preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }

    public static class ConfirmDialogFragment extends InstrumentedDialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setMessage(getArguments().getCharSequence("android.intent.extra.TEXT")).setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Fragment f = ConfirmDialogFragment.this.getTargetFragment();
                    if (f != null) {
                        ((CustomListPreferenceDialogFragment) f).onItemConfirmed();
                    }
                }
            }).setNegativeButton(17039360, null).create();
        }

        public int getMetricsCategory() {
            return 529;
        }
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
    }

    /* Access modifiers changed, original: protected */
    public void onDialogClosed(boolean positiveResult) {
    }

    /* Access modifiers changed, original: protected */
    public void onDialogCreated(Dialog dialog) {
    }

    /* Access modifiers changed, original: protected */
    public boolean isAutoClosePreference() {
        return true;
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getConfirmationMessage(String value) {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void onDialogStateRestored(Dialog dialog, Bundle savedInstanceState) {
    }
}
