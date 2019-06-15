package com.android.settings.dream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.dream.DreamBackend;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;

public class WhenToDreamPicker extends RadioButtonPickerFragment {
    private static final String TAG = "WhenToDreamPicker";
    private DreamBackend mBackend;

    private final class WhenToDreamCandidateInfo extends CandidateInfo {
        private final String key;
        private final String name;

        WhenToDreamCandidateInfo(String title, String value) {
            super(true);
            this.name = title;
            this.key = value;
        }

        public CharSequence loadLabel() {
            return this.name;
        }

        public Drawable loadIcon() {
            return null;
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
        return R.xml.when_to_dream_settings;
    }

    public int getMetricsCategory() {
        return 47;
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        String[] entries = entries();
        String[] values = keys();
        List<WhenToDreamCandidateInfo> candidates = new ArrayList();
        if (entries == null || entries.length <= 0) {
            return null;
        }
        if (values == null || values.length != entries.length) {
            throw new IllegalArgumentException("Entries and values must be of the same length.");
        }
        for (int i = 0; i < entries.length; i++) {
            candidates.add(new WhenToDreamCandidateInfo(entries[i], values[i]));
        }
        return candidates;
    }

    private String[] entries() {
        return getResources().getStringArray(R.array.when_to_start_screensaver_entries);
    }

    private String[] keys() {
        return getResources().getStringArray(R.array.when_to_start_screensaver_values);
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return DreamSettings.getKeyFromSetting(this.mBackend.getWhenToDreamSetting());
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        this.mBackend.setWhenToDream(DreamSettings.getSettingFromPrefKey(key));
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onSelectionPerformed(boolean success) {
        super.onSelectionPerformed(success);
        getActivity().finish();
    }
}
