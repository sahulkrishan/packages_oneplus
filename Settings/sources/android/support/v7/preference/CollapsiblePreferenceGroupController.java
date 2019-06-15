package android.support.v7.preference;

import android.content.Context;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import com.android.settings.utils.FileSizeFormatter;
import java.util.ArrayList;
import java.util.List;

final class CollapsiblePreferenceGroupController {
    private final Context mContext;
    private boolean mHasExpandablePreference = false;
    private final PreferenceGroupAdapter mPreferenceGroupAdapter;

    static class ExpandButton extends Preference {
        private long mId;

        ExpandButton(Context context, List<Preference> collapsedPreferences, long parentId) {
            super(context);
            initLayout();
            setSummary(collapsedPreferences);
            this.mId = FileSizeFormatter.MEGABYTE_IN_BYTES + parentId;
        }

        private void initLayout() {
            setLayoutResource(R.layout.expand_button);
            setIcon(R.drawable.ic_arrow_down_24dp);
            setTitle(R.string.expand_button_title);
            setOrder(999);
        }

        private void setSummary(List<Preference> collapsedPreferences) {
            CharSequence summary = null;
            List<PreferenceGroup> parents = new ArrayList();
            for (Preference preference : collapsedPreferences) {
                CharSequence title = preference.getTitle();
                if ((preference instanceof PreferenceGroup) && !TextUtils.isEmpty(title)) {
                    parents.add((PreferenceGroup) preference);
                }
                if (parents.contains(preference.getParent())) {
                    if (preference instanceof PreferenceGroup) {
                        parents.add((PreferenceGroup) preference);
                    }
                } else if (!TextUtils.isEmpty(title)) {
                    if (summary == null) {
                        summary = title;
                    } else {
                        summary = getContext().getString(R.string.summary_collapsed_preference_list, new Object[]{summary, title});
                    }
                }
            }
            setSummary(summary);
        }

        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            holder.setDividerAllowedAbove(false);
        }

        public long getId() {
            return this.mId;
        }
    }

    CollapsiblePreferenceGroupController(PreferenceGroup preferenceGroup, PreferenceGroupAdapter preferenceGroupAdapter) {
        this.mPreferenceGroupAdapter = preferenceGroupAdapter;
        this.mContext = preferenceGroup.getContext();
    }

    public List<Preference> createVisiblePreferencesList(PreferenceGroup group) {
        return createInnerVisiblePreferencesList(group);
    }

    private List<Preference> createInnerVisiblePreferencesList(PreferenceGroup group) {
        int i = 0;
        this.mHasExpandablePreference = false;
        int visiblePreferenceCount = 0;
        boolean hasExpandablePreference = group.getInitialExpandedChildrenCount() != Integer.MAX_VALUE;
        List<Preference> visiblePreferences = new ArrayList();
        List<Preference> collapsedPreferences = new ArrayList();
        int groupSize = group.getPreferenceCount();
        while (i < groupSize) {
            Preference preference = group.getPreference(i);
            if (preference.isVisible()) {
                if (!hasExpandablePreference || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                    visiblePreferences.add(preference);
                } else {
                    collapsedPreferences.add(preference);
                }
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup innerGroup = (PreferenceGroup) preference;
                    if (innerGroup.isOnSameScreenAsChildren()) {
                        List<Preference> innerList = createInnerVisiblePreferencesList(innerGroup);
                        if (hasExpandablePreference && this.mHasExpandablePreference) {
                            throw new IllegalArgumentException("Nested expand buttons are not supported!");
                        }
                        for (Preference inner : innerList) {
                            if (!hasExpandablePreference || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                                visiblePreferences.add(inner);
                            } else {
                                collapsedPreferences.add(inner);
                            }
                            visiblePreferenceCount++;
                        }
                    } else {
                        continue;
                    }
                } else {
                    visiblePreferenceCount++;
                }
            }
            i++;
        }
        if (hasExpandablePreference && visiblePreferenceCount > group.getInitialExpandedChildrenCount()) {
            visiblePreferences.add(createExpandButton(group, collapsedPreferences));
        }
        this.mHasExpandablePreference |= hasExpandablePreference;
        return visiblePreferences;
    }

    public boolean onPreferenceVisibilityChange(Preference preference) {
        if (!this.mHasExpandablePreference) {
            return false;
        }
        this.mPreferenceGroupAdapter.onPreferenceHierarchyChange(preference);
        return true;
    }

    private ExpandButton createExpandButton(final PreferenceGroup group, List<Preference> collapsedPreferences) {
        ExpandButton preference = new ExpandButton(this.mContext, collapsedPreferences, group.getId());
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                group.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
                CollapsiblePreferenceGroupController.this.mPreferenceGroupAdapter.onPreferenceHierarchyChange(preference);
                return true;
            }
        });
        return preference;
    }
}
