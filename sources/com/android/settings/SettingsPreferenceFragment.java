package com.android.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.XmlRes;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.CustomListPreference.CustomListPreferenceDialogFragment;
import com.android.settings.RestrictedListPreference.RestrictedListPreferenceDialogFragment;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import com.android.settings.widget.HighlightablePreferenceGroupAdapter;
import com.android.settings.widget.LoadingViewController;
import com.android.settingslib.CustomDialogPreference;
import com.android.settingslib.CustomDialogPreference.CustomPreferenceDialogFragment;
import com.android.settingslib.CustomEditTextPreference;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.widget.FooterPreferenceMixin;
import java.util.UUID;

public abstract class SettingsPreferenceFragment extends InstrumentedPreferenceFragment implements DialogCreatable, HelpResourceProvider {
    private static final int ORDER_FIRST = -1;
    private static final String SAVE_HIGHLIGHTED_KEY = "android:preference_highlighted";
    private static final String TAG = "SettingsPreference";
    @VisibleForTesting
    public HighlightablePreferenceGroupAdapter mAdapter;
    private boolean mAnimationAllowed;
    private ViewGroup mButtonBar;
    private ContentResolver mContentResolver;
    private Adapter mCurrentRootAdapter;
    private AdapterDataObserver mDataSetObserver = new AdapterDataObserver() {
        public void onChanged() {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            SettingsPreferenceFragment.this.onDataSetChanged();
        }
    };
    private SettingsDialogFragment mDialogFragment;
    private View mEmptyView;
    protected final FooterPreferenceMixin mFooterPreferenceMixin = new FooterPreferenceMixin(this, getLifecycle());
    private LayoutPreference mHeader;
    private boolean mIsDataSetObserverRegistered = false;
    private LinearLayoutManager mLayoutManager;
    private ViewGroup mPinnedHeaderFrameLayout;
    private ArrayMap<String, Preference> mPreferenceCache;
    @VisibleForTesting
    public boolean mPreferenceHighlighted = false;

    public static class SettingsDialogFragment extends InstrumentedDialogFragment {
        private static final String KEY_DIALOG_ID = "key_dialog_id";
        private static final String KEY_PARENT_FRAGMENT_ID = "key_parent_fragment_id";
        private OnCancelListener mOnCancelListener;
        private OnDismissListener mOnDismissListener;
        private Fragment mParentFragment;

        public SettingsDialogFragment(DialogCreatable fragment, int dialogId) {
            super(fragment, dialogId);
            if (fragment instanceof Fragment) {
                this.mParentFragment = (Fragment) fragment;
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("fragment argument must be an instance of ");
            stringBuilder.append(Fragment.class.getName());
            throw new IllegalArgumentException(stringBuilder.toString());
        }

        public int getMetricsCategory() {
            if (this.mDialogCreatable == null) {
                return 0;
            }
            int metricsCategory = this.mDialogCreatable.getDialogMetricsCategory(this.mDialogId);
            if (metricsCategory > 0) {
                return metricsCategory;
            }
            throw new IllegalStateException("Dialog must provide a metrics category");
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (this.mParentFragment != null) {
                outState.putInt(KEY_DIALOG_ID, this.mDialogId);
                outState.putInt(KEY_PARENT_FRAGMENT_ID, this.mParentFragment.getId());
            }
        }

        public void onStart() {
            super.onStart();
            if (this.mParentFragment != null && (this.mParentFragment instanceof SettingsPreferenceFragment)) {
                ((SettingsPreferenceFragment) this.mParentFragment).onDialogShowing();
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                this.mDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 0);
                this.mParentFragment = getParentFragment();
                int mParentFragmentId = savedInstanceState.getInt(KEY_PARENT_FRAGMENT_ID, -1);
                if (this.mParentFragment == null) {
                    this.mParentFragment = getFragmentManager().findFragmentById(mParentFragmentId);
                }
                if (!(this.mParentFragment instanceof DialogCreatable)) {
                    Object name;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (this.mParentFragment != null) {
                        name = this.mParentFragment.getClass().getName();
                    } else {
                        name = Integer.valueOf(mParentFragmentId);
                    }
                    stringBuilder.append(name);
                    stringBuilder.append(" must implement ");
                    stringBuilder.append(DialogCreatable.class.getName());
                    throw new IllegalArgumentException(stringBuilder.toString());
                } else if (this.mParentFragment instanceof SettingsPreferenceFragment) {
                    ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = this;
                }
            }
            return ((DialogCreatable) this.mParentFragment).onCreateDialog(this.mDialogId);
        }

        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (this.mOnCancelListener != null) {
                this.mOnCancelListener.onCancel(dialog);
            }
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (this.mOnDismissListener != null) {
                this.mOnDismissListener.onDismiss(dialog);
            }
        }

        public int getDialogId() {
            return this.mDialogId;
        }

        public void onDetach() {
            super.onDetach();
            if ((this.mParentFragment instanceof SettingsPreferenceFragment) && ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment == this) {
                ((SettingsPreferenceFragment) this.mParentFragment).mDialogFragment = null;
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        SearchMenuController.init((ObservablePreferenceFragment) this);
        HelpMenuController.init((ObservablePreferenceFragment) this);
        if (icicle != null) {
            this.mPreferenceHighlighted = icicle.getBoolean(SAVE_HIGHLIGHTED_KEY);
        }
        HighlightablePreferenceGroupAdapter.adjustInitialExpandedChildCount(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        this.mPinnedHeaderFrameLayout = (ViewGroup) root.findViewById(R.id.pinned_header);
        this.mButtonBar = (ViewGroup) root.findViewById(R.id.button_bar);
        return root;
    }

    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        checkAvailablePrefs(getPreferenceScreen());
    }

    private void checkAvailablePrefs(PreferenceGroup preferenceGroup) {
        if (preferenceGroup != null) {
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                Preference pref = preferenceGroup.getPreference(i);
                if ((pref instanceof SelfAvailablePreference) && !((SelfAvailablePreference) pref).isAvailable(getContext())) {
                    preferenceGroup.removePreference(pref);
                } else if (pref instanceof PreferenceGroup) {
                    checkAvailablePrefs((PreferenceGroup) pref);
                }
            }
        }
    }

    public ViewGroup getButtonBar() {
        return this.mButtonBar;
    }

    public View setPinnedHeaderView(int layoutResId) {
        View pinnedHeader = getActivity().getLayoutInflater().inflate(layoutResId, this.mPinnedHeaderFrameLayout, false);
        setPinnedHeaderView(pinnedHeader);
        return pinnedHeader;
    }

    public void setPinnedHeaderView(View pinnedHeader) {
        this.mPinnedHeaderFrameLayout.addView(pinnedHeader);
        this.mPinnedHeaderFrameLayout.setVisibility(0);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mAdapter != null) {
            outState.putBoolean(SAVE_HIGHLIGHTED_KEY, this.mAdapter.isHighlightRequested());
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        highlightPreferenceIfNeeded();
    }

    /* Access modifiers changed, original: protected */
    public void onBindPreferences() {
        registerObserverIfNeeded();
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindPreferences() {
        unregisterObserverIfNeeded();
    }

    public void setLoading(boolean loading, boolean animate) {
        LoadingViewController.handleLoadingContainer(getView().findViewById(R.id.loading_container), getListView(), loading ^ 1, animate);
    }

    public void registerObserverIfNeeded() {
        if (!this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
            }
            this.mCurrentRootAdapter = getListView().getAdapter();
            this.mCurrentRootAdapter.registerAdapterDataObserver(this.mDataSetObserver);
            this.mIsDataSetObserverRegistered = true;
            onDataSetChanged();
        }
    }

    public void unregisterObserverIfNeeded() {
        if (this.mIsDataSetObserverRegistered) {
            if (this.mCurrentRootAdapter != null) {
                this.mCurrentRootAdapter.unregisterAdapterDataObserver(this.mDataSetObserver);
                this.mCurrentRootAdapter = null;
            }
            this.mIsDataSetObserverRegistered = false;
        }
    }

    public void highlightPreferenceIfNeeded() {
        if (isAdded() && this.mAdapter != null) {
            this.mAdapter.requestHighlight(getView(), getListView());
        }
    }

    public int getInitialExpandedChildCount() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        updateEmptyView();
    }

    public LayoutPreference getHeaderView() {
        return this.mHeader;
    }

    /* Access modifiers changed, original: protected */
    public void setHeaderView(int resource) {
        this.mHeader = new LayoutPreference(getPrefContext(), resource);
        addPreferenceToTop(this.mHeader);
    }

    /* Access modifiers changed, original: protected */
    public void setHeaderView(View view) {
        this.mHeader = new LayoutPreference(getPrefContext(), view);
        addPreferenceToTop(this.mHeader);
    }

    private void addPreferenceToTop(LayoutPreference preference) {
        preference.setOrder(-1);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(preference);
        }
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (!(preferenceScreen == null || preferenceScreen.isAttached())) {
            preferenceScreen.setShouldUseGeneratedIds(this.mAnimationAllowed);
        }
        super.setPreferenceScreen(preferenceScreen);
        if (preferenceScreen != null && this.mHeader != null) {
            preferenceScreen.addPreference(this.mHeader);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateEmptyView() {
        if (this.mEmptyView != null) {
            int i = 0;
            if (getPreferenceScreen() != null) {
                View listContainer = getActivity().findViewById(AndroidResources.ANDROID_R_LIST_CONTAINER);
                boolean z = true;
                if ((getPreferenceScreen().getPreferenceCount() - (this.mHeader != null ? 1 : 0)) - this.mFooterPreferenceMixin.hasFooter() > 0 && (listContainer == null || listContainer.getVisibility() == 0)) {
                    z = false;
                }
                boolean show = z;
                View view = this.mEmptyView;
                if (!show) {
                    i = 8;
                }
                view.setVisibility(i);
            } else {
                this.mEmptyView.setVisibility(0);
            }
        }
    }

    public void setEmptyView(View v) {
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        this.mEmptyView = v;
        updateEmptyView();
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    public LayoutManager onCreateLayoutManager() {
        this.mLayoutManager = new LinearLayoutManager(getContext());
        return this.mLayoutManager;
    }

    /* Access modifiers changed, original: protected */
    public Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        Bundle arguments = getArguments();
        this.mAdapter = new HighlightablePreferenceGroupAdapter(preferenceScreen, arguments == null ? null : arguments.getString(SettingsActivity.EXTRA_FRAGMENT_ARG_KEY), this.mPreferenceHighlighted);
        return this.mAdapter;
    }

    /* Access modifiers changed, original: protected */
    public void setAnimationAllowed(boolean animationAllowed) {
        this.mAnimationAllowed = animationAllowed;
    }

    /* Access modifiers changed, original: protected */
    public void cacheRemoveAllPrefs(PreferenceGroup group) {
        this.mPreferenceCache = new ArrayMap();
        int N = group.getPreferenceCount();
        for (int i = 0; i < N; i++) {
            Preference p = group.getPreference(i);
            if (!TextUtils.isEmpty(p.getKey())) {
                this.mPreferenceCache.put(p.getKey(), p);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Preference getCachedPreference(String key) {
        return this.mPreferenceCache != null ? (Preference) this.mPreferenceCache.remove(key) : null;
    }

    /* Access modifiers changed, original: protected */
    public void removeCachedPrefs(PreferenceGroup group) {
        for (Preference p : this.mPreferenceCache.values()) {
            group.removePreference(p);
        }
        this.mPreferenceCache = null;
    }

    /* Access modifiers changed, original: protected */
    public int getCachedCount() {
        return this.mPreferenceCache != null ? this.mPreferenceCache.size() : 0;
    }

    @VisibleForTesting(otherwise = 4)
    public boolean removePreference(String key) {
        return removePreference(getPreferenceScreen(), key);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean removePreference(PreferenceGroup group, String key) {
        int preferenceCount = group.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = group.getPreference(i);
            if (TextUtils.equals(preference.getKey(), key)) {
                return group.removePreference(preference);
            }
            if ((preference instanceof PreferenceGroup) && removePreference((PreferenceGroup) preference, key)) {
                return true;
            }
        }
        return false;
    }

    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    /* Access modifiers changed, original: protected */
    public ContentResolver getContentResolver() {
        Context context = getActivity();
        if (context != null) {
            this.mContentResolver = context.getContentResolver();
        }
        return this.mContentResolver;
    }

    /* Access modifiers changed, original: protected */
    public Object getSystemService(String name) {
        return getActivity().getSystemService(name);
    }

    /* Access modifiers changed, original: protected */
    public PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    public void onDetach() {
        if (isRemoving() && this.mDialogFragment != null) {
            this.mDialogFragment.dismiss();
            this.mDialogFragment = null;
        }
        super.onDetach();
    }

    /* Access modifiers changed, original: protected */
    public void showDialog(int dialogId) {
        if (this.mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        this.mDialogFragment = new SettingsDialogFragment(this, dialogId);
        this.mDialogFragment.show(getChildFragmentManager(), Integer.toString(dialogId));
    }

    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    public int getDialogMetricsCategory(int dialogId) {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void removeDialog(int dialogId) {
        if (this.mDialogFragment != null && this.mDialogFragment.getDialogId() == dialogId) {
            this.mDialogFragment.dismissAllowingStateLoss();
        }
        this.mDialogFragment = null;
    }

    /* Access modifiers changed, original: protected */
    public void setOnCancelListener(OnCancelListener listener) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.mOnCancelListener = listener;
        }
    }

    /* Access modifiers changed, original: protected */
    public void setOnDismissListener(OnDismissListener listener) {
        if (this.mDialogFragment != null) {
            this.mDialogFragment.mOnDismissListener = listener;
        }
    }

    public void onDialogShowing() {
    }

    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment f;
        if (preference.getKey() == null) {
            preference.setKey(UUID.randomUUID().toString());
        }
        if (preference instanceof RestrictedListPreference) {
            f = RestrictedListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomListPreference) {
            f = CustomListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomDialogPreference) {
            f = CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof CustomEditTextPreference) {
            f = CustomEditTextPreference.CustomPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
    }

    /* Access modifiers changed, original: protected */
    public boolean hasNextButton() {
        return ((ButtonBarHandler) getActivity()).hasNextButton();
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return ((ButtonBarHandler) getActivity()).getNextButton();
    }

    public void finish() {
        Activity activity = getActivity();
        if (activity != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                activity.finish();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Intent getIntent() {
        if (getActivity() == null) {
            return null;
        }
        return getActivity().getIntent();
    }

    /* Access modifiers changed, original: protected */
    public void setResult(int result, Intent intent) {
        if (getActivity() != null) {
            getActivity().setResult(result, intent);
        }
    }

    /* Access modifiers changed, original: protected */
    public void setResult(int result) {
        if (getActivity() != null) {
            getActivity().setResult(result);
        }
    }

    public boolean startFragment(Fragment caller, String fragmentClass, int titleRes, int requestCode, Bundle extras) {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).startPreferencePanel(fragmentClass, extras, titleRes, null, caller, requestCode);
            return true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Parent isn't SettingsActivity nor PreferenceActivity, thus there's no way to launch the given Fragment (name: ");
        stringBuilder.append(fragmentClass);
        stringBuilder.append(", requestCode: ");
        stringBuilder.append(requestCode);
        stringBuilder.append(")");
        Log.w(str, stringBuilder.toString());
        return false;
    }
}
