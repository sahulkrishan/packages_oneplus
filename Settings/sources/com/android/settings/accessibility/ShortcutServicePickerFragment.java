package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.AccessibilityShortcutController;
import com.android.internal.accessibility.AccessibilityShortcutController.ToggleableFrameworkFeatureInfo;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settingslib.accessibility.AccessibilityUtils;
import com.android.settingslib.widget.CandidateInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShortcutServicePickerFragment extends RadioButtonPickerFragment {

    private class FrameworkCandidateInfo extends CandidateInfo {
        final int mIconResId;
        final String mKey;
        final ToggleableFrameworkFeatureInfo mToggleableFrameworkFeatureInfo;

        public FrameworkCandidateInfo(ToggleableFrameworkFeatureInfo frameworkFeatureInfo, int iconResId, String key) {
            super(true);
            this.mToggleableFrameworkFeatureInfo = frameworkFeatureInfo;
            this.mIconResId = iconResId;
            this.mKey = key;
        }

        public CharSequence loadLabel() {
            return this.mToggleableFrameworkFeatureInfo.getLabel(ShortcutServicePickerFragment.this.getContext());
        }

        public Drawable loadIcon() {
            return ShortcutServicePickerFragment.this.getContext().getDrawable(this.mIconResId);
        }

        public String getKey() {
            return this.mKey;
        }
    }

    private class ServiceCandidateInfo extends CandidateInfo {
        final AccessibilityServiceInfo mServiceInfo;

        public ServiceCandidateInfo(AccessibilityServiceInfo serviceInfo) {
            super(true);
            this.mServiceInfo = serviceInfo;
        }

        public CharSequence loadLabel() {
            PackageManagerWrapper pmw = new PackageManagerWrapper(ShortcutServicePickerFragment.this.getContext().getPackageManager());
            CharSequence label = this.mServiceInfo.getResolveInfo().serviceInfo.loadLabel(pmw.getPackageManager());
            if (label != null) {
                return label;
            }
            ComponentName componentName = this.mServiceInfo.getComponentName();
            if (componentName == null) {
                return null;
            }
            try {
                return pmw.getApplicationInfoAsUser(componentName.getPackageName(), 0, UserHandle.myUserId()).loadLabel(pmw.getPackageManager());
            } catch (NameNotFoundException e) {
                return null;
            }
        }

        public Drawable loadIcon() {
            ResolveInfo resolveInfo = this.mServiceInfo.getResolveInfo();
            if (resolveInfo.getIconResource() == 0) {
                return ShortcutServicePickerFragment.this.getContext().getDrawable(R.mipmap.ic_accessibility_generic);
            }
            return resolveInfo.loadIcon(ShortcutServicePickerFragment.this.getContext().getPackageManager());
        }

        public String getKey() {
            return this.mServiceInfo.getComponentName().flattenToString();
        }
    }

    public static class ConfirmationDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
        private static final String EXTRA_KEY = "extra_key";
        private static final String TAG = "ConfirmationDialogFragment";
        private IBinder mToken;

        public static ConfirmationDialogFragment newInstance(ShortcutServicePickerFragment parent, String key) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle argument = new Bundle();
            argument.putString("extra_key", key);
            fragment.setArguments(argument);
            fragment.setTargetFragment(parent, 0);
            fragment.mToken = new Binder();
            return fragment;
        }

        public int getMetricsCategory() {
            return 6;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return AccessibilityServiceWarning.createCapabilitiesDialog(getActivity(), ((AccessibilityManager) getActivity().getSystemService(AccessibilityManager.class)).getInstalledServiceInfoWithComponentName(ComponentName.unflattenFromString(getArguments().getString("extra_key"))), this);
        }

        public void onClick(DialogInterface dialog, int which) {
            Fragment fragment = getTargetFragment();
            if (which == -1 && (fragment instanceof ShortcutServicePickerFragment)) {
                ((ShortcutServicePickerFragment) fragment).onServiceConfirmed(getArguments().getString("extra_key"));
            }
        }
    }

    public int getMetricsCategory() {
        return 6;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_shortcut_service_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<? extends CandidateInfo> getCandidates() {
        List<AccessibilityServiceInfo> installedServices = ((AccessibilityManager) getContext().getSystemService(AccessibilityManager.class)).getInstalledAccessibilityServiceList();
        int numInstalledServices = installedServices.size();
        List<CandidateInfo> candidates = new ArrayList(numInstalledServices);
        Map<ComponentName, ToggleableFrameworkFeatureInfo> frameworkFeatureInfoMap = AccessibilityShortcutController.getFrameworkShortcutFeaturesMap();
        for (ComponentName componentName : frameworkFeatureInfoMap.keySet()) {
            int iconId;
            if (componentName.equals(AccessibilityShortcutController.COLOR_INVERSION_COMPONENT_NAME)) {
                iconId = R.drawable.ic_color_inversion;
            } else if (componentName.equals(AccessibilityShortcutController.DALTONIZER_COMPONENT_NAME)) {
                iconId = R.drawable.ic_daltonizer;
            } else {
                iconId = R.drawable.empty_icon;
            }
            candidates.add(new FrameworkCandidateInfo((ToggleableFrameworkFeatureInfo) frameworkFeatureInfoMap.get(componentName), iconId, componentName.flattenToString()));
        }
        for (int i = 0; i < numInstalledServices; i++) {
            candidates.add(new ServiceCandidateInfo((AccessibilityServiceInfo) installedServices.get(i)));
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        String shortcutServiceString = AccessibilityUtils.getShortcutTargetServiceComponentNameString(getContext(), UserHandle.myUserId());
        if (shortcutServiceString != null) {
            ComponentName shortcutName = ComponentName.unflattenFromString(shortcutServiceString);
            if (shortcutName != null) {
                return shortcutName.flattenToString();
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        Secure.putString(getContext().getContentResolver(), "accessibility_shortcut_target_service", key);
        return true;
    }

    public void onRadioButtonClicked(RadioButtonPreference selected) {
        String selectedKey = selected.getKey();
        if (TextUtils.isEmpty(selectedKey)) {
            super.onRadioButtonClicked(selected);
            return;
        }
        if (AccessibilityShortcutController.getFrameworkShortcutFeaturesMap().containsKey(ComponentName.unflattenFromString(selectedKey))) {
            onRadioButtonConfirmed(selectedKey);
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            ConfirmationDialogFragment.newInstance(this, selectedKey).show(activity.getFragmentManager(), "ConfirmationDialogFragment");
        }
    }

    private void onServiceConfirmed(String serviceKey) {
        onRadioButtonConfirmed(serviceKey);
    }
}
