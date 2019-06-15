package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.inputmethod.InputMethodAndSubtypeUtil;
import com.android.settingslib.inputmethod.InputMethodPreference;
import com.android.settingslib.inputmethod.InputMethodPreference.OnSavePreferenceListener;
import com.android.settingslib.inputmethod.InputMethodSettingValuesWrapper;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AvailableVirtualKeyboardFragment extends SettingsPreferenceFragment implements OnSavePreferenceListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> res = new ArrayList();
            SearchIndexableResource index = new SearchIndexableResource(context);
            index.xmlResId = R.xml.available_virtual_keyboard;
            res.add(index);
            return res;
        }
    };
    private DevicePolicyManager mDpm;
    private InputMethodManager mImm;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList();
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;

    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.available_virtual_keyboard);
        Activity activity = getActivity();
        this.mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(activity);
        this.mImm = (InputMethodManager) activity.getSystemService(InputMethodManager.class);
        this.mDpm = (DevicePolicyManager) activity.getSystemService(DevicePolicyManager.class);
    }

    public void onResume() {
        super.onResume();
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        updateInputMethodPreferenceViews();
    }

    public void onSaveInputMethodPreference(InputMethodPreference pref) {
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mImm.getInputMethodList(), getResources().getConfiguration().keyboard == 2);
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        Iterator it = this.mInputMethodPreferenceList.iterator();
        while (it.hasNext()) {
            ((InputMethodPreference) it.next()).updatePreferenceViews();
        }
    }

    public int getMetricsCategory() {
        return 347;
    }

    private static Drawable loadDrawable(PackageManager packageManager, String packageName, int resId, ApplicationInfo applicationInfo) {
        if (resId == 0) {
            return null;
        }
        try {
            return packageManager.getDrawable(packageName, resId, applicationInfo);
        } catch (Exception e) {
            return null;
        }
    }

    private static Drawable getInputMethodIcon(PackageManager packageManager, InputMethodInfo imi) {
        ServiceInfo si = imi.getServiceInfo();
        ApplicationInfo ai = si != null ? si.applicationInfo : null;
        String packageName = imi.getPackageName();
        if (si == null || ai == null || packageName == null) {
            return new ColorDrawable(0);
        }
        Drawable drawable = loadDrawable(packageManager, packageName, si.logo, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, si.icon, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, ai.logo, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, ai.icon, ai);
        if (drawable != null) {
            return drawable;
        }
        return new ColorDrawable(0);
    }

    private void updateInputMethodPreferenceViews() {
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        this.mInputMethodPreferenceList.clear();
        List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
        Context context = getPrefContext();
        PackageManager packageManager = getActivity().getPackageManager();
        List<InputMethodInfo> imis = this.mInputMethodSettingValues.getInputMethodList();
        int i = 0;
        int numImis = imis == null ? 0 : imis.size();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= numImis) {
                break;
            }
            InputMethodInfo imi = (InputMethodInfo) imis.get(i3);
            boolean isAllowedByOrganization = permittedList == null || permittedList.contains(imi.getPackageName());
            InputMethodPreference pref = new InputMethodPreference(context, imi, true, isAllowedByOrganization, (OnSavePreferenceListener) this);
            pref.setIcon(getInputMethodIcon(packageManager, imi));
            this.mInputMethodPreferenceList.add(pref);
            i2 = i3 + 1;
        }
        this.mInputMethodPreferenceList.sort(new -$$Lambda$AvailableVirtualKeyboardFragment$jwIjaxSxVSRnK0I3ZX1KVHtd2wk(Collator.getInstance()));
        getPreferenceScreen().removeAll();
        while (true) {
            int i4 = i;
            if (i4 < numImis) {
                InputMethodPreference pref2 = (InputMethodPreference) this.mInputMethodPreferenceList.get(i4);
                pref2.setOrder(i4);
                getPreferenceScreen().addPreference(pref2);
                InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref2);
                pref2.updatePreferenceViews();
                i = i4 + 1;
            } else {
                return;
            }
        }
    }
}
