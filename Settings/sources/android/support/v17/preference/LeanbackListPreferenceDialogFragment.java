package android.support.v17.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.util.ArraySet;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LeanbackListPreferenceDialogFragment extends LeanbackPreferenceDialogFragment {
    private static final String SAVE_STATE_ENTRIES = "LeanbackListPreferenceDialogFragment.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "LeanbackListPreferenceDialogFragment.entryValues";
    private static final String SAVE_STATE_INITIAL_SELECTION = "LeanbackListPreferenceDialogFragment.initialSelection";
    private static final String SAVE_STATE_INITIAL_SELECTIONS = "LeanbackListPreferenceDialogFragment.initialSelections";
    private static final String SAVE_STATE_IS_MULTI = "LeanbackListPreferenceDialogFragment.isMulti";
    private static final String SAVE_STATE_MESSAGE = "LeanbackListPreferenceDialogFragment.message";
    private static final String SAVE_STATE_TITLE = "LeanbackListPreferenceDialogFragment.title";
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mInitialSelection;
    private Set<String> mInitialSelections;
    private boolean mMulti;

    public class AdapterMulti extends Adapter<ViewHolder> implements OnItemClickListener {
        private final CharSequence[] mEntries;
        private final CharSequence[] mEntryValues;
        private final Set<String> mSelections;

        public AdapterMulti(CharSequence[] entries, CharSequence[] entryValues, Set<String> initialSelections) {
            this.mEntries = entries;
            this.mEntryValues = entryValues;
            this.mSelections = new HashSet(initialSelections);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.leanback_list_preference_item_multi, parent, false), this);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.getWidgetView().setChecked(this.mSelections.contains(this.mEntryValues[position].toString()));
            holder.getTitleView().setText(this.mEntries[position]);
        }

        public int getItemCount() {
            return this.mEntries.length;
        }

        public void onItemClick(ViewHolder viewHolder) {
            int index = viewHolder.getAdapterPosition();
            if (index != -1) {
                String entry = this.mEntryValues[index].toString();
                if (this.mSelections.contains(entry)) {
                    this.mSelections.remove(entry);
                } else {
                    this.mSelections.add(entry);
                }
                MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) LeanbackListPreferenceDialogFragment.this.getPreference();
                if (multiSelectListPreference.callChangeListener(new HashSet(this.mSelections))) {
                    multiSelectListPreference.setValues(new HashSet(this.mSelections));
                    LeanbackListPreferenceDialogFragment.this.mInitialSelections = this.mSelections;
                } else if (this.mSelections.contains(entry)) {
                    this.mSelections.remove(entry);
                } else {
                    this.mSelections.add(entry);
                }
                notifyDataSetChanged();
            }
        }
    }

    public class AdapterSingle extends Adapter<ViewHolder> implements OnItemClickListener {
        private final CharSequence[] mEntries;
        private final CharSequence[] mEntryValues;
        private CharSequence mSelectedValue;

        public AdapterSingle(CharSequence[] entries, CharSequence[] entryValues, CharSequence selectedValue) {
            this.mEntries = entries;
            this.mEntryValues = entryValues;
            this.mSelectedValue = selectedValue;
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.leanback_list_preference_item_single, parent, false), this);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.getWidgetView().setChecked(this.mEntryValues[position].equals(this.mSelectedValue));
            holder.getTitleView().setText(this.mEntries[position]);
        }

        public int getItemCount() {
            return this.mEntries.length;
        }

        public void onItemClick(ViewHolder viewHolder) {
            int index = viewHolder.getAdapterPosition();
            if (index != -1) {
                CharSequence entry = this.mEntryValues[index];
                ListPreference preference = (ListPreference) LeanbackListPreferenceDialogFragment.this.getPreference();
                if (index >= 0) {
                    String value = this.mEntryValues[index].toString();
                    if (preference.callChangeListener(value)) {
                        preference.setValue(value);
                        this.mSelectedValue = entry;
                    }
                }
                LeanbackListPreferenceDialogFragment.this.getFragmentManager().popBackStack();
                notifyDataSetChanged();
            }
        }
    }

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements OnClickListener {
        private final ViewGroup mContainer;
        private final OnItemClickListener mListener;
        private final TextView mTitleView;
        private final Checkable mWidgetView;

        public interface OnItemClickListener {
            void onItemClick(ViewHolder viewHolder);
        }

        public ViewHolder(@NonNull View view, @NonNull OnItemClickListener listener) {
            super(view);
            this.mWidgetView = (Checkable) view.findViewById(R.id.button);
            this.mContainer = (ViewGroup) view.findViewById(R.id.container);
            this.mTitleView = (TextView) view.findViewById(16908310);
            this.mContainer.setOnClickListener(this);
            this.mListener = listener;
        }

        public Checkable getWidgetView() {
            return this.mWidgetView;
        }

        public TextView getTitleView() {
            return this.mTitleView;
        }

        public ViewGroup getContainer() {
            return this.mContainer;
        }

        public void onClick(View v) {
            this.mListener.onItemClick(this);
        }
    }

    public static LeanbackListPreferenceDialogFragment newInstanceSingle(String key) {
        Bundle args = new Bundle(1);
        args.putString("key", key);
        LeanbackListPreferenceDialogFragment fragment = new LeanbackListPreferenceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static LeanbackListPreferenceDialogFragment newInstanceMulti(String key) {
        Bundle args = new Bundle(1);
        args.putString("key", key);
        LeanbackListPreferenceDialogFragment fragment = new LeanbackListPreferenceDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = 0;
        if (savedInstanceState == null) {
            DialogPreference preference = getPreference();
            this.mDialogTitle = preference.getDialogTitle();
            this.mDialogMessage = preference.getDialogMessage();
            if (preference instanceof ListPreference) {
                this.mMulti = false;
                this.mEntries = ((ListPreference) preference).getEntries();
                this.mEntryValues = ((ListPreference) preference).getEntryValues();
                this.mInitialSelection = ((ListPreference) preference).getValue();
                return;
            } else if (preference instanceof MultiSelectListPreference) {
                this.mMulti = true;
                this.mEntries = ((MultiSelectListPreference) preference).getEntries();
                this.mEntryValues = ((MultiSelectListPreference) preference).getEntryValues();
                this.mInitialSelections = ((MultiSelectListPreference) preference).getValues();
                return;
            } else {
                throw new IllegalArgumentException("Preference must be a ListPreference or MultiSelectListPreference");
            }
        }
        this.mDialogTitle = savedInstanceState.getCharSequence(SAVE_STATE_TITLE);
        this.mDialogMessage = savedInstanceState.getCharSequence(SAVE_STATE_MESSAGE);
        this.mMulti = savedInstanceState.getBoolean(SAVE_STATE_IS_MULTI);
        this.mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES);
        this.mEntryValues = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
        if (this.mMulti) {
            String[] initialSelections = savedInstanceState.getStringArray(SAVE_STATE_INITIAL_SELECTIONS);
            if (initialSelections != null) {
                i = initialSelections.length;
            }
            this.mInitialSelections = new ArraySet(i);
            if (initialSelections != null) {
                Collections.addAll(this.mInitialSelections, initialSelections);
                return;
            }
            return;
        }
        this.mInitialSelection = savedInstanceState.getString(SAVE_STATE_INITIAL_SELECTION);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(SAVE_STATE_TITLE, this.mDialogTitle);
        outState.putCharSequence(SAVE_STATE_MESSAGE, this.mDialogMessage);
        outState.putBoolean(SAVE_STATE_IS_MULTI, this.mMulti);
        outState.putCharSequenceArray(SAVE_STATE_ENTRIES, this.mEntries);
        outState.putCharSequenceArray(SAVE_STATE_ENTRY_VALUES, this.mEntryValues);
        if (this.mMulti) {
            outState.putStringArray(SAVE_STATE_INITIAL_SELECTIONS, (String[]) this.mInitialSelections.toArray(new String[this.mInitialSelections.size()]));
        } else {
            outState.putString(SAVE_STATE_INITIAL_SELECTION, this.mInitialSelection);
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leanback_list_preference_fragment, container, false);
        VerticalGridView verticalGridView = (VerticalGridView) view.findViewById(16908298);
        verticalGridView.setWindowAlignment(3);
        verticalGridView.setFocusScrollStrategy(0);
        verticalGridView.setAdapter(onCreateAdapter());
        verticalGridView.requestFocus();
        CharSequence title = this.mDialogTitle;
        if (!TextUtils.isEmpty(title)) {
            ((TextView) view.findViewById(R.id.decor_title)).setText(title);
        }
        CharSequence message = this.mDialogMessage;
        if (!TextUtils.isEmpty(message)) {
            TextView messageView = (TextView) view.findViewById(16908299);
            messageView.setVisibility(0);
            messageView.setText(message);
        }
        return view;
    }

    public Adapter onCreateAdapter() {
        if (this.mMulti) {
            return new AdapterMulti(this.mEntries, this.mEntryValues, this.mInitialSelections);
        }
        return new AdapterSingle(this.mEntries, this.mEntryValues, this.mInitialSelection);
    }
}
