package com.android.settings.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.List;

public class VrDisplayPreferencePicker extends RadioButtonPickerFragment {
    static final String PREF_KEY_PREFIX = "vr_display_pref_";

    static class VrCandidateInfo extends CandidateInfo {
        public final String label;
        public final int value;

        public VrCandidateInfo(Context context, int value, int resId) {
            super(true);
            this.value = value;
            this.label = context.getString(resId);
        }

        public CharSequence loadLabel() {
            return this.label;
        }

        public Drawable loadIcon() {
            return null;
        }

        public String getKey() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(VrDisplayPreferencePicker.PREF_KEY_PREFIX);
            stringBuilder.append(this.value);
            return stringBuilder.toString();
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.vr_display_settings;
    }

    public int getMetricsCategory() {
        return 921;
    }

    /* Access modifiers changed, original: protected */
    public List<VrCandidateInfo> getCandidates() {
        List<VrCandidateInfo> candidates = new ArrayList();
        Context context = getContext();
        candidates.add(new VrCandidateInfo(context, 0, R.string.display_vr_pref_low_persistence));
        candidates.add(new VrCandidateInfo(context, 1, R.string.display_vr_pref_off));
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        int current = Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, this.mUserId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREF_KEY_PREFIX);
        stringBuilder.append(current);
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        int i = -1;
        switch (key.hashCode()) {
            case 1581655828:
                if (key.equals("vr_display_pref_0")) {
                    i = false;
                    break;
                }
                break;
            case 1581655829:
                if (key.equals("vr_display_pref_1")) {
                    i = 1;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                return Secure.putIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, this.mUserId);
            case 1:
                return Secure.putIntForUser(getContext().getContentResolver(), "vr_display_mode", 1, this.mUserId);
            default:
                return false;
        }
    }
}
