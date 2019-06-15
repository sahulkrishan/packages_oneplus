package com.android.settings.widget;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.ListPreference;
import android.widget.ArrayAdapter;
import com.android.internal.R;
import com.android.settingslib.core.instrumentation.Instrumentable;
import java.util.ArrayList;

public class UpdatableListPreferenceDialogFragment extends PreferenceDialogFragment implements Instrumentable {
    private static final String METRICS_CATEGORY_KEY = "metrics_category_key";
    private static final String SAVE_STATE_ENTRIES = "UpdatableListPreferenceDialogFragment.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "UpdatableListPreferenceDialogFragment.entryValues";
    private static final String SAVE_STATE_INDEX = "UpdatableListPreferenceDialogFragment.index";
    private ArrayAdapter mAdapter;
    private int mClickedDialogEntryIndex;
    private ArrayList<CharSequence> mEntries;
    private CharSequence[] mEntryValues;
    private int mMetricsCategory = 0;

    public static UpdatableListPreferenceDialogFragment newInstance(String key, int metricsCategory) {
        UpdatableListPreferenceDialogFragment fragment = new UpdatableListPreferenceDialogFragment();
        Bundle args = new Bundle(1);
        args.putString("key", key);
        args.putInt(METRICS_CATEGORY_KEY, metricsCategory);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMetricsCategory = getArguments().getInt(METRICS_CATEGORY_KEY, 0);
        if (savedInstanceState == null) {
            this.mEntries = new ArrayList();
            setPreferenceData(getListPreference());
            return;
        }
        this.mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, 0);
        this.mEntries = savedInstanceState.getCharSequenceArrayList(SAVE_STATE_ENTRIES);
        this.mEntryValues = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_INDEX, this.mClickedDialogEntryIndex);
        outState.putCharSequenceArrayList(SAVE_STATE_ENTRIES, this.mEntries);
        outState.putCharSequenceArray(SAVE_STATE_ENTRY_VALUES, this.mEntryValues);
    }

    public void onDialogClosed(boolean positiveResult) {
        ListPreference preference = getListPreference();
        if (positiveResult && this.mClickedDialogEntryIndex >= 0) {
            String value = this.mEntryValues[this.mClickedDialogEntryIndex].toString();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setAdapter(ArrayAdapter adapter) {
        this.mAdapter = adapter;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setEntries(ArrayList<CharSequence> entries) {
        this.mEntries = entries;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public ArrayAdapter getAdapter() {
        return this.mAdapter;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setMetricsCategory(Bundle bundle) {
        this.mMetricsCategory = bundle.getInt(METRICS_CATEGORY_KEY, 0);
    }

    /* Access modifiers changed, original: protected */
    public void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.AlertDialog, 16842845, 0);
        this.mAdapter = new ArrayAdapter(getContext(), a.getResourceId(21, 17367058), this.mEntries);
        builder.setSingleChoiceItems(this.mAdapter, this.mClickedDialogEntryIndex, new -$$Lambda$UpdatableListPreferenceDialogFragment$yZRmvmWflT3ytJ4m-nzXQtpejcQ(this));
        builder.setPositiveButton(null, null);
        a.recycle();
    }

    public static /* synthetic */ void lambda$onPrepareDialogBuilder$0(UpdatableListPreferenceDialogFragment updatableListPreferenceDialogFragment, DialogInterface dialog, int which) {
        updatableListPreferenceDialogFragment.mClickedDialogEntryIndex = which;
        updatableListPreferenceDialogFragment.onClick(dialog, -1);
        dialog.dismiss();
    }

    public int getMetricsCategory() {
        return this.mMetricsCategory;
    }

    private ListPreference getListPreference() {
        return (ListPreference) getPreference();
    }

    private void setPreferenceData(ListPreference preference) {
        this.mEntries.clear();
        this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
        for (CharSequence entry : preference.getEntries()) {
            this.mEntries.add(entry);
        }
        this.mEntryValues = preference.getEntryValues();
    }

    public void onListPreferenceUpdated(ListPreference preference) {
        if (this.mAdapter != null) {
            setPreferenceData(preference);
            this.mAdapter.notifyDataSetChanged();
        }
    }
}
