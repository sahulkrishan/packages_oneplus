package com.android.settings.dream;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.dream.DreamBackend.DreamInfo;
import com.android.settingslib.widget.CandidateInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CurrentDreamPicker extends RadioButtonPickerFragment {
    private DreamBackend mBackend;

    private static final class DreamCandidateInfo extends CandidateInfo {
        private final Drawable icon;
        private final String key;
        private final CharSequence name;

        DreamCandidateInfo(DreamInfo info) {
            super(true);
            this.name = info.caption;
            this.icon = info.icon;
            this.key = info.componentName.flattenToString();
        }

        public CharSequence loadLabel() {
            return this.name;
        }

        public Drawable loadIcon() {
            return this.icon;
        }

        public String getKey() {
            return this.key;
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mBackend = DreamBackend.getInstance(context);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.current_dream_settings;
    }

    public int getMetricsCategory() {
        return 47;
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        Map<String, ComponentName> componentNameMap = getDreamComponentsMap();
        if (componentNameMap.get(key) == null) {
            return false;
        }
        this.mBackend.setActiveDream((ComponentName) componentNameMap.get(key));
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return this.mBackend.getActiveDream().flattenToString();
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        return (List) this.mBackend.getDreamInfos().stream().map(-$$Lambda$hBSizG3ais67bSjAeIqNEa6sDBo.INSTANCE).collect(Collectors.toList());
    }

    /* Access modifiers changed, original: protected */
    public void onSelectionPerformed(boolean success) {
        super.onSelectionPerformed(success);
        getActivity().finish();
    }

    private Map<String, ComponentName> getDreamComponentsMap() {
        Map<String, ComponentName> comps = new HashMap();
        this.mBackend.getDreamInfos().forEach(new -$$Lambda$CurrentDreamPicker$t4o3LQXIuoDz_RsLdUZZYlwB3bA(comps));
        return comps;
    }
}
