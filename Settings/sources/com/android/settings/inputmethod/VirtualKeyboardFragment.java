package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.inputmethod.InputMethodAndSubtypeUtil;
import com.android.settingslib.inputmethod.InputMethodPreference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VirtualKeyboardFragment extends SettingsPreferenceFragment implements Indexable {
    private static final String ADD_VIRTUAL_KEYBOARD_SCREEN = "add_virtual_keyboard_screen";
    private static final Drawable NO_ICON = new ColorDrawable(0);
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.virtual_keyboard_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(VirtualKeyboardFragment.ADD_VIRTUAL_KEYBOARD_SCREEN);
            return keys;
        }
    };
    private Preference mAddVirtualKeyboardScreen;
    private DevicePolicyManager mDpm;
    private InputMethodManager mImm;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList();

    public void onCreatePreferences(Bundle bundle, String s) {
        Activity activity = (Activity) Preconditions.checkNotNull(getActivity());
        addPreferencesFromResource(R.xml.virtual_keyboard_settings);
        this.mImm = (InputMethodManager) Preconditions.checkNotNull((InputMethodManager) activity.getSystemService(InputMethodManager.class));
        this.mDpm = (DevicePolicyManager) Preconditions.checkNotNull((DevicePolicyManager) activity.getSystemService(DevicePolicyManager.class));
        this.mAddVirtualKeyboardScreen = (Preference) Preconditions.checkNotNull(findPreference(ADD_VIRTUAL_KEYBOARD_SCREEN));
    }

    public void onResume() {
        super.onResume();
        updateInputMethodPreferenceViews();
    }

    public int getMetricsCategory() {
        return 345;
    }

    private void updateInputMethodPreferenceViews() {
        this.mInputMethodPreferenceList.clear();
        List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
        Context context = getPrefContext();
        List<InputMethodInfo> imis = this.mImm.getEnabledInputMethodList();
        int i = 0;
        int N = imis == null ? 0 : imis.size();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= N) {
                break;
            }
            Drawable icon;
            InputMethodInfo imi = (InputMethodInfo) imis.get(i3);
            boolean isAllowedByOrganization = permittedList == null || permittedList.contains(imi.getPackageName());
            try {
                icon = getActivity().getPackageManager().getApplicationIcon(imi.getPackageName());
            } catch (Exception e) {
                icon = NO_ICON;
            }
            Drawable icon2 = icon;
            InputMethodPreference pref = new InputMethodPreference(context, imi, false, isAllowedByOrganization, null);
            pref.setIcon(icon2);
            this.mInputMethodPreferenceList.add(pref);
            i2 = i3 + 1;
        }
        this.mInputMethodPreferenceList.sort(new -$$Lambda$VirtualKeyboardFragment$3eczHKaadmVH3sZXf9rlrdYqLjw(Collator.getInstance()));
        getPreferenceScreen().removeAll();
        while (true) {
            int i4 = i;
            if (i4 < N) {
                InputMethodPreference pref2 = (InputMethodPreference) this.mInputMethodPreferenceList.get(i4);
                pref2.setOrder(i4);
                getPreferenceScreen().addPreference(pref2);
                InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref2);
                pref2.updatePreferenceViews();
                i = i4 + 1;
            } else {
                this.mAddVirtualKeyboardScreen.setIcon((int) R.drawable.ic_add_24dp);
                this.mAddVirtualKeyboardScreen.setOrder(N);
                getPreferenceScreen().addPreference(this.mAddVirtualKeyboardScreen);
                return;
            }
        }
    }
}
