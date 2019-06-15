package com.android.settings.inputmethod;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.preference.Preference;
import android.text.BidiFormatter;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualKeyboardPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private final DevicePolicyManager mDpm;
    private final InputMethodManager mImm;
    private final PackageManager mPm = this.mContext.getPackageManager();

    public VirtualKeyboardPreferenceController(Context context) {
        super(context);
        this.mDpm = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mImm = (InputMethodManager) this.mContext.getSystemService("input_method");
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_virtual_keyboard_pref);
    }

    public String getPreferenceKey() {
        return "virtual_keyboard_pref";
    }

    public void updateState(Preference preference) {
        List<InputMethodInfo> imis = this.mImm.getEnabledInputMethodList();
        if (imis == null) {
            preference.setSummary((int) R.string.summary_empty);
            return;
        }
        List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
        List<String> labels = new ArrayList();
        Iterator it = imis.iterator();
        while (true) {
            boolean isAllowedByOrganization = true;
            if (!it.hasNext()) {
                break;
            }
            InputMethodInfo imi = (InputMethodInfo) it.next();
            if (!(permittedList == null || permittedList.contains(imi.getPackageName()))) {
                isAllowedByOrganization = false;
            }
            if (isAllowedByOrganization) {
                labels.add(imi.loadLabel(this.mPm).toString());
            }
        }
        if (labels.isEmpty()) {
            preference.setSummary((int) R.string.summary_empty);
            return;
        }
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        CharSequence summary = null;
        for (String label : labels) {
            if (summary == null) {
                summary = bidiFormatter.unicodeWrap(label);
            } else {
                summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, bidiFormatter.unicodeWrap(label)});
            }
        }
        preference.setSummary(summary);
    }
}
