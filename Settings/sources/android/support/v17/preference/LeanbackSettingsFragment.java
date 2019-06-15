package android.support.v17.preference;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.PreferenceFragment.OnPreferenceDisplayDialogCallback;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartFragmentCallback;
import android.support.v14.preference.PreferenceFragment.OnPreferenceStartScreenCallback;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Space;

public abstract class LeanbackSettingsFragment extends Fragment implements OnPreferenceStartFragmentCallback, OnPreferenceStartScreenCallback, OnPreferenceDisplayDialogCallback {
    private static final String PREFERENCE_FRAGMENT_TAG = "android.support.v17.preference.LeanbackSettingsFragment.PREFERENCE_FRAGMENT";
    private final RootViewOnKeyListener mRootViewOnKeyListener = new RootViewOnKeyListener();

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static class DummyFragment extends Fragment {
        @Nullable
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = new Space(inflater.getContext());
            v.setVisibility(8);
            return v;
        }
    }

    private class RootViewOnKeyListener implements OnKeyListener {
        private RootViewOnKeyListener() {
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                return LeanbackSettingsFragment.this.getChildFragmentManager().popBackStackImmediate();
            }
            return false;
        }
    }

    public abstract void onPreferenceStartInitialScreen();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.leanback_settings_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            onPreferenceStartInitialScreen();
        }
    }

    public void onResume() {
        super.onResume();
        LeanbackSettingsRootView rootView = (LeanbackSettingsRootView) getView();
        if (rootView != null) {
            rootView.setOnBackKeyListener(this.mRootViewOnKeyListener);
        }
    }

    public void onPause() {
        super.onPause();
        LeanbackSettingsRootView rootView = (LeanbackSettingsRootView) getView();
        if (rootView != null) {
            rootView.setOnBackKeyListener(null);
        }
    }

    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragment caller, Preference pref) {
        if (caller != null) {
            Fragment f;
            if (pref instanceof ListPreference) {
                f = LeanbackListPreferenceDialogFragment.newInstanceSingle(((ListPreference) pref).getKey());
                f.setTargetFragment(caller, 0);
                startPreferenceFragment(f);
            } else if (!(pref instanceof MultiSelectListPreference)) {
                return false;
            } else {
                f = LeanbackListPreferenceDialogFragment.newInstanceMulti(((MultiSelectListPreference) pref).getKey());
                f.setTargetFragment(caller, 0);
                startPreferenceFragment(f);
            }
            return true;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Cannot display dialog for preference ");
        stringBuilder.append(pref);
        stringBuilder.append(", Caller must not be null!");
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void startPreferenceFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (getChildFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG) != null) {
            transaction.addToBackStack(null).replace(R.id.settings_preference_fragment_container, fragment, PREFERENCE_FRAGMENT_TAG);
        } else {
            transaction.add(R.id.settings_preference_fragment_container, fragment, PREFERENCE_FRAGMENT_TAG);
        }
        transaction.commit();
    }

    public void startImmersiveFragment(@NonNull Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment preferenceFragment = getChildFragmentManager().findFragmentByTag(PREFERENCE_FRAGMENT_TAG);
        if (!(preferenceFragment == null || preferenceFragment.isHidden())) {
            if (VERSION.SDK_INT < 23) {
                transaction.add(R.id.settings_preference_fragment_container, new DummyFragment());
            }
            transaction.remove(preferenceFragment);
        }
        transaction.add(R.id.settings_dialog_container, fragment).addToBackStack(null).commit();
    }
}
