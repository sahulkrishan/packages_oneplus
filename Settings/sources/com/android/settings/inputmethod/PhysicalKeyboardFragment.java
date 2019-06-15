package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.Settings.KeyboardLayoutPickerActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.inputmethod.KeyboardLayoutDialogFragment.OnSetupKeyboardLayoutsListener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.utils.ThreadUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class PhysicalKeyboardFragment extends SettingsPreferenceFragment implements InputDeviceListener, OnSetupKeyboardLayoutsListener, Indexable {
    private static final String KEYBOARD_ASSISTANCE_CATEGORY = "keyboard_assistance_category";
    private static final String KEYBOARD_SHORTCUTS_HELPER = "keyboard_shortcuts_helper";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.physical_keyboard_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String SHOW_VIRTUAL_KEYBOARD_SWITCH = "show_virtual_keyboard_switch";
    private final ContentObserver mContentObserver = new ContentObserver(new Handler(true)) {
        public void onChange(boolean selfChange) {
            PhysicalKeyboardFragment.this.updateShowVirtualKeyboardSwitch();
        }
    };
    private InputManager mIm;
    private Intent mIntentWaitingForResult;
    private PreferenceCategory mKeyboardAssistanceCategory;
    private final ArrayList<HardKeyboardDeviceInfo> mLastHardKeyboards = new ArrayList();
    private InputMethodSettings mSettings;
    private SwitchPreference mShowVirtualKeyboardSwitch;
    private final OnPreferenceChangeListener mShowVirtualKeyboardSwitchPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            PhysicalKeyboardFragment.this.mSettings.setShowImeWithHardKeyboard(((Boolean) newValue).booleanValue());
            return true;
        }
    };

    public static final class HardKeyboardDeviceInfo {
        public final InputDeviceIdentifier mDeviceIdentifier;
        public final String mDeviceName;
        public final String mLayoutLabel;

        public HardKeyboardDeviceInfo(String deviceName, InputDeviceIdentifier deviceIdentifier, String layoutLabel) {
            this.mDeviceName = TextUtils.emptyIfNull(deviceName);
            this.mDeviceIdentifier = deviceIdentifier;
            this.mLayoutLabel = layoutLabel;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof HardKeyboardDeviceInfo)) {
                return false;
            }
            HardKeyboardDeviceInfo that = (HardKeyboardDeviceInfo) o;
            if (TextUtils.equals(this.mDeviceName, that.mDeviceName) && Objects.equals(this.mDeviceIdentifier, that.mDeviceIdentifier) && TextUtils.equals(this.mLayoutLabel, that.mLayoutLabel)) {
                return true;
            }
            return false;
        }
    }

    public void onCreatePreferences(Bundle bundle, String s) {
        Activity activity = (Activity) Preconditions.checkNotNull(getActivity());
        addPreferencesFromResource(R.xml.physical_keyboard_settings);
        this.mIm = (InputManager) Preconditions.checkNotNull((InputManager) activity.getSystemService(InputManager.class));
        this.mSettings = new InputMethodSettings(activity.getResources(), getContentResolver(), new HashMap(), new ArrayList(), UserHandle.myUserId(), false);
        this.mKeyboardAssistanceCategory = (PreferenceCategory) Preconditions.checkNotNull((PreferenceCategory) findPreference(KEYBOARD_ASSISTANCE_CATEGORY));
        this.mShowVirtualKeyboardSwitch = (SwitchPreference) Preconditions.checkNotNull((SwitchPreference) this.mKeyboardAssistanceCategory.findPreference(SHOW_VIRTUAL_KEYBOARD_SWITCH));
        findPreference(KEYBOARD_SHORTCUTS_HELPER).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                PhysicalKeyboardFragment.this.toggleKeyboardShortcutsMenu();
                return true;
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mLastHardKeyboards.clear();
        scheduleUpdateHardKeyboards();
        this.mIm.registerInputDeviceListener(this, null);
        this.mShowVirtualKeyboardSwitch.setOnPreferenceChangeListener(this.mShowVirtualKeyboardSwitchPreferenceChangeListener);
        registerShowVirtualKeyboardSettingsObserver();
    }

    public void onPause() {
        super.onPause();
        this.mLastHardKeyboards.clear();
        this.mIm.unregisterInputDeviceListener(this);
        this.mShowVirtualKeyboardSwitch.setOnPreferenceChangeListener(null);
        unregisterShowVirtualKeyboardSettingsObserver();
    }

    public void onInputDeviceAdded(int deviceId) {
        scheduleUpdateHardKeyboards();
    }

    public void onInputDeviceRemoved(int deviceId) {
        scheduleUpdateHardKeyboards();
    }

    public void onInputDeviceChanged(int deviceId) {
        scheduleUpdateHardKeyboards();
    }

    public int getMetricsCategory() {
        return 346;
    }

    private void scheduleUpdateHardKeyboards() {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$PhysicalKeyboardFragment$j2wn_SRBsrC7ziAxKgN6he5fFRk(this, getContext()));
    }

    public static /* synthetic */ void lambda$scheduleUpdateHardKeyboards$1(PhysicalKeyboardFragment physicalKeyboardFragment, Context context) {
        Log.d("PhysicalKeyboardFgt", "postOnBackgroundThread scheduleUpdateHardKeyboards start");
        ThreadUtils.postOnMainThread(new -$$Lambda$PhysicalKeyboardFragment$TSW09XXjPDm85D9gNcQRBrAyYps(physicalKeyboardFragment, getHardKeyboards(context)));
        Log.d("PhysicalKeyboardFgt", "postOnBackgroundThread scheduleUpdateHardKeyboards end");
    }

    private void updateHardKeyboards(List<HardKeyboardDeviceInfo> newHardKeyboards) {
        if (!Objects.equals(this.mLastHardKeyboards, newHardKeyboards)) {
            this.mLastHardKeyboards.clear();
            this.mLastHardKeyboards.addAll(newHardKeyboards);
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();
            PreferenceCategory category = new PreferenceCategory(getPrefContext());
            category.setTitle((int) R.string.builtin_keyboard_settings_title);
            category.setOrder(0);
            preferenceScreen.addPreference(category);
            for (HardKeyboardDeviceInfo hardKeyboardDeviceInfo : newHardKeyboards) {
                Preference pref = new Preference(getPrefContext());
                pref.setTitle(hardKeyboardDeviceInfo.mDeviceName);
                pref.setSummary(hardKeyboardDeviceInfo.mLayoutLabel);
                pref.setOnPreferenceClickListener(new -$$Lambda$PhysicalKeyboardFragment$GzAuWQoIrNRWOGdhye1KALY7EFw(this, hardKeyboardDeviceInfo));
                category.addPreference(pref);
            }
            this.mKeyboardAssistanceCategory.setOrder(1);
            preferenceScreen.addPreference(this.mKeyboardAssistanceCategory);
            updateShowVirtualKeyboardSwitch();
        }
    }

    private void showKeyboardLayoutDialog(InputDeviceIdentifier inputDeviceIdentifier) {
        KeyboardLayoutDialogFragment fragment = new KeyboardLayoutDialogFragment(inputDeviceIdentifier);
        fragment.setTargetFragment(this, 0);
        fragment.show(getActivity().getFragmentManager(), "keyboardLayout");
    }

    private void registerShowVirtualKeyboardSettingsObserver() {
        unregisterShowVirtualKeyboardSettingsObserver();
        getActivity().getContentResolver().registerContentObserver(Secure.getUriFor("show_ime_with_hard_keyboard"), false, this.mContentObserver, UserHandle.myUserId());
        updateShowVirtualKeyboardSwitch();
    }

    private void unregisterShowVirtualKeyboardSettingsObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mContentObserver);
    }

    private void updateShowVirtualKeyboardSwitch() {
        this.mShowVirtualKeyboardSwitch.setChecked(this.mSettings.isShowImeWithHardKeyboardEnabled());
    }

    private void toggleKeyboardShortcutsMenu() {
        getActivity().requestShowKeyboardShortcuts();
    }

    public void onSetupKeyboardLayouts(InputDeviceIdentifier inputDeviceIdentifier) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(getActivity(), KeyboardLayoutPickerActivity.class);
        intent.putExtra(KeyboardLayoutPickerFragment.EXTRA_INPUT_DEVICE_IDENTIFIER, inputDeviceIdentifier);
        this.mIntentWaitingForResult = intent;
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mIntentWaitingForResult != null) {
            InputDeviceIdentifier inputDeviceIdentifier = (InputDeviceIdentifier) this.mIntentWaitingForResult.getParcelableExtra(KeyboardLayoutPickerFragment.EXTRA_INPUT_DEVICE_IDENTIFIER);
            this.mIntentWaitingForResult = null;
            showKeyboardLayoutDialog(inputDeviceIdentifier);
        }
    }

    private static String getLayoutLabel(InputDevice device, Context context, InputManager im) {
        String currentLayoutDesc = im.getCurrentKeyboardLayoutForInputDevice(device.getIdentifier());
        if (currentLayoutDesc == null) {
            return context.getString(R.string.keyboard_layout_default_label);
        }
        KeyboardLayout currentLayout = im.getKeyboardLayout(currentLayoutDesc);
        if (currentLayout == null) {
            return context.getString(R.string.keyboard_layout_default_label);
        }
        return TextUtils.emptyIfNull(currentLayout.getLabel());
    }

    static List<HardKeyboardDeviceInfo> getHardKeyboards(Context context) {
        List<HardKeyboardDeviceInfo> keyboards = new ArrayList();
        InputManager im = (InputManager) context.getSystemService(InputManager.class);
        if (im == null) {
            return new ArrayList();
        }
        for (int deviceId : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (!(device == null || device.isVirtual() || !device.isFullKeyboard())) {
                keyboards.add(new HardKeyboardDeviceInfo(device.getName(), device.getIdentifier(), getLayoutLabel(device, context, im)));
            }
        }
        keyboards.sort(new -$$Lambda$PhysicalKeyboardFragment$E1Pa9yi7mSTmfiefFBHYeSOZEJQ(Collator.getInstance()));
        return keyboards;
    }

    static /* synthetic */ int lambda$getHardKeyboards$3(Collator collator, HardKeyboardDeviceInfo a, HardKeyboardDeviceInfo b) {
        int result = collator.compare(a.mDeviceName, b.mDeviceName);
        if (result != 0) {
            return result;
        }
        result = a.mDeviceIdentifier.getDescriptor().compareTo(b.mDeviceIdentifier.getDescriptor());
        if (result != 0) {
            return result;
        }
        return collator.compare(a.mLayoutLabel, b.mLayoutLabel);
    }
}
