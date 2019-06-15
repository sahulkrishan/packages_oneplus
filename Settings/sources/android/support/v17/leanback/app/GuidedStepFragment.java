package android.support.v17.leanback.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.widget.DiffCallback;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidanceStylist.Guidance;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionAdapter;
import android.support.v17.leanback.widget.GuidedActionAdapter.ClickListener;
import android.support.v17.leanback.widget.GuidedActionAdapter.EditListener;
import android.support.v17.leanback.widget.GuidedActionAdapter.FocusListener;
import android.support.v17.leanback.widget.GuidedActionAdapterGroup;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.support.v17.leanback.widget.NonOverlappingLinearLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class GuidedStepFragment extends Fragment implements FocusListener {
    private static final boolean DEBUG = false;
    private static final String ENTRY_NAME_ENTRANCE = "GuidedStepEntrance";
    private static final String ENTRY_NAME_REPLACE = "GuidedStepDefault";
    private static final String EXTRA_ACTION_PREFIX = "action_";
    private static final String EXTRA_BUTTON_ACTION_PREFIX = "buttonaction_";
    public static final String EXTRA_UI_STYLE = "uiStyle";
    private static final boolean IS_FRAMEWORK_FRAGMENT = true;
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static final int SLIDE_FROM_BOTTOM = 1;
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static final int SLIDE_FROM_SIDE = 0;
    private static final String TAG = "GuidedStepF";
    private static final String TAG_LEAN_BACK_ACTIONS_FRAGMENT = "leanBackGuidedStepFragment";
    public static final int UI_STYLE_ACTIVITY_ROOT = 2;
    @Deprecated
    public static final int UI_STYLE_DEFAULT = 0;
    public static final int UI_STYLE_ENTRANCE = 1;
    public static final int UI_STYLE_REPLACE = 0;
    private int entranceTransitionType = 0;
    private List<GuidedAction> mActions = new ArrayList();
    GuidedActionsStylist mActionsStylist = onCreateActionsStylist();
    private GuidedActionAdapter mAdapter;
    private GuidedActionAdapterGroup mAdapterGroup;
    private List<GuidedAction> mButtonActions = new ArrayList();
    private GuidedActionsStylist mButtonActionsStylist = onCreateButtonActionsStylist();
    private GuidedActionAdapter mButtonAdapter;
    private GuidanceStylist mGuidanceStylist = onCreateGuidanceStylist();
    private GuidedActionAdapter mSubAdapter;
    private ContextThemeWrapper mThemeWrapper;

    @RestrictTo({Scope.LIBRARY_GROUP})
    public static class DummyFragment extends Fragment {
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = new View(inflater.getContext());
            v.setVisibility(8);
            return v;
        }
    }

    public GuidedStepFragment() {
        onProvideFragmentTransitions();
    }

    public GuidanceStylist onCreateGuidanceStylist() {
        return new GuidanceStylist();
    }

    public GuidedActionsStylist onCreateActionsStylist() {
        return new GuidedActionsStylist();
    }

    public GuidedActionsStylist onCreateButtonActionsStylist() {
        GuidedActionsStylist stylist = new GuidedActionsStylist();
        stylist.setAsButtonActions();
        return stylist;
    }

    public int onProvideTheme() {
        return -1;
    }

    @NonNull
    public Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new Guidance("", "", "", null);
    }

    public void onCreateActions(@NonNull List<GuidedAction> list, Bundle savedInstanceState) {
    }

    public void onCreateButtonActions(@NonNull List<GuidedAction> list, Bundle savedInstanceState) {
    }

    public void onGuidedActionClicked(GuidedAction action) {
    }

    public boolean onSubGuidedActionClicked(GuidedAction action) {
        return true;
    }

    public boolean isExpanded() {
        return this.mActionsStylist.isExpanded();
    }

    public boolean isSubActionsExpanded() {
        return this.mActionsStylist.isSubActionsExpanded();
    }

    public void expandSubActions(GuidedAction action) {
        if (action.hasSubActions()) {
            expandAction(action, true);
        }
    }

    public void expandAction(GuidedAction action, boolean withTransition) {
        this.mActionsStylist.expandAction(action, withTransition);
    }

    public void collapseSubActions() {
        collapseAction(true);
    }

    public void collapseAction(boolean withTransition) {
        if (this.mActionsStylist != null && this.mActionsStylist.getActionsGridView() != null) {
            this.mActionsStylist.collapseAction(withTransition);
        }
    }

    public void onGuidedActionFocused(GuidedAction action) {
    }

    @Deprecated
    public void onGuidedActionEdited(GuidedAction action) {
    }

    public void onGuidedActionEditCanceled(GuidedAction action) {
        onGuidedActionEdited(action);
    }

    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        onGuidedActionEdited(action);
        return -2;
    }

    public static int add(FragmentManager fragmentManager, GuidedStepFragment fragment) {
        return add(fragmentManager, fragment, 16908290);
    }

    public static int add(FragmentManager fragmentManager, GuidedStepFragment fragment, int id) {
        GuidedStepFragment current = getCurrentGuidedStepFragment(fragmentManager);
        int i = 0;
        boolean inGuidedStep = current != null;
        if (VERSION.SDK_INT >= 21 && VERSION.SDK_INT < 23 && !inGuidedStep) {
            fragmentManager.beginTransaction().replace(id, new DummyFragment(), TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!inGuidedStep) {
            i = 1;
        }
        fragment.setUiStyle(i);
        ft.addToBackStack(fragment.generateStackEntryName());
        if (current != null) {
            fragment.onAddSharedElementTransition(ft, current);
        }
        return ft.replace(id, fragment, TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit();
    }

    /* Access modifiers changed, original: protected */
    public void onAddSharedElementTransition(FragmentTransaction ft, GuidedStepFragment disappearing) {
        View fragmentView = disappearing.getView();
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.action_fragment_root), "action_fragment_root");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.action_fragment_background), "action_fragment_background");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.action_fragment), "action_fragment");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_root), "guidedactions_root");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_content), "guidedactions_content");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_list_background), "guidedactions_list_background");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_root2), "guidedactions_root2");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_content2), "guidedactions_content2");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(R.id.guidedactions_list_background2), "guidedactions_list_background2");
    }

    private static void addNonNullSharedElementTransition(FragmentTransaction ft, View subView, String transitionName) {
        if (subView != null) {
            TransitionHelper.addSharedElement(ft, subView, transitionName);
        }
    }

    /* Access modifiers changed, original: final */
    public final String generateStackEntryName() {
        return generateStackEntryName(getUiStyle(), getClass());
    }

    static String generateStackEntryName(int uiStyle, Class guidedStepFragmentClass) {
        StringBuilder stringBuilder;
        switch (uiStyle) {
            case 0:
                stringBuilder = new StringBuilder();
                stringBuilder.append(ENTRY_NAME_REPLACE);
                stringBuilder.append(guidedStepFragmentClass.getName());
                return stringBuilder.toString();
            case 1:
                stringBuilder = new StringBuilder();
                stringBuilder.append(ENTRY_NAME_ENTRANCE);
                stringBuilder.append(guidedStepFragmentClass.getName());
                return stringBuilder.toString();
            default:
                return "";
        }
    }

    static boolean isStackEntryUiStyleEntrance(String backStackEntryName) {
        return backStackEntryName != null && backStackEntryName.startsWith(ENTRY_NAME_ENTRANCE);
    }

    static String getGuidedStepFragmentClassName(String backStackEntryName) {
        if (backStackEntryName.startsWith(ENTRY_NAME_REPLACE)) {
            return backStackEntryName.substring(ENTRY_NAME_REPLACE.length());
        }
        if (backStackEntryName.startsWith(ENTRY_NAME_ENTRANCE)) {
            return backStackEntryName.substring(ENTRY_NAME_ENTRANCE.length());
        }
        return "";
    }

    public static int addAsRoot(Activity activity, GuidedStepFragment fragment, int id) {
        activity.getWindow().getDecorView();
        FragmentManager fragmentManager = activity.getFragmentManager();
        if (fragmentManager.findFragmentByTag(TAG_LEAN_BACK_ACTIONS_FRAGMENT) != null) {
            Log.w(TAG, "Fragment is already exists, likely calling addAsRoot() when savedInstanceState is not null in Activity.onCreate().");
            return -1;
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragment.setUiStyle(2);
        return ft.replace(id, fragment, TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit();
    }

    public static GuidedStepFragment getCurrentGuidedStepFragment(FragmentManager fm) {
        Fragment f = fm.findFragmentByTag(TAG_LEAN_BACK_ACTIONS_FRAGMENT);
        if (f instanceof GuidedStepFragment) {
            return (GuidedStepFragment) f;
        }
        return null;
    }

    public GuidanceStylist getGuidanceStylist() {
        return this.mGuidanceStylist;
    }

    public GuidedActionsStylist getGuidedActionsStylist() {
        return this.mActionsStylist;
    }

    public List<GuidedAction> getButtonActions() {
        return this.mButtonActions;
    }

    public GuidedAction findButtonActionById(long id) {
        int index = findButtonActionPositionById(id);
        return index >= 0 ? (GuidedAction) this.mButtonActions.get(index) : null;
    }

    public int findButtonActionPositionById(long id) {
        if (this.mButtonActions != null) {
            for (int i = 0; i < this.mButtonActions.size(); i++) {
                this.mButtonActions.get(i);
                if (((GuidedAction) this.mButtonActions.get(i)).getId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public GuidedActionsStylist getGuidedButtonActionsStylist() {
        return this.mButtonActionsStylist;
    }

    public void setButtonActions(List<GuidedAction> actions) {
        this.mButtonActions = actions;
        if (this.mButtonAdapter != null) {
            this.mButtonAdapter.setActions(this.mButtonActions);
        }
    }

    public void notifyButtonActionChanged(int position) {
        if (this.mButtonAdapter != null) {
            this.mButtonAdapter.notifyItemChanged(position);
        }
    }

    public View getButtonActionItemView(int position) {
        ViewHolder holder = this.mButtonActionsStylist.getActionsGridView().findViewHolderForPosition(position);
        return holder == null ? null : holder.itemView;
    }

    public void setSelectedButtonActionPosition(int position) {
        this.mButtonActionsStylist.getActionsGridView().setSelectedPosition(position);
    }

    public int getSelectedButtonActionPosition() {
        return this.mButtonActionsStylist.getActionsGridView().getSelectedPosition();
    }

    public List<GuidedAction> getActions() {
        return this.mActions;
    }

    public GuidedAction findActionById(long id) {
        int index = findActionPositionById(id);
        return index >= 0 ? (GuidedAction) this.mActions.get(index) : null;
    }

    public int findActionPositionById(long id) {
        if (this.mActions != null) {
            for (int i = 0; i < this.mActions.size(); i++) {
                this.mActions.get(i);
                if (((GuidedAction) this.mActions.get(i)).getId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setActions(List<GuidedAction> actions) {
        this.mActions = actions;
        if (this.mAdapter != null) {
            this.mAdapter.setActions(this.mActions);
        }
    }

    public void setActionsDiffCallback(DiffCallback<GuidedAction> diffCallback) {
        this.mAdapter.setDiffCallback(diffCallback);
    }

    public void notifyActionChanged(int position) {
        if (this.mAdapter != null) {
            this.mAdapter.notifyItemChanged(position);
        }
    }

    public View getActionItemView(int position) {
        ViewHolder holder = this.mActionsStylist.getActionsGridView().findViewHolderForPosition(position);
        return holder == null ? null : holder.itemView;
    }

    public void setSelectedActionPosition(int position) {
        this.mActionsStylist.getActionsGridView().setSelectedPosition(position);
    }

    public int getSelectedActionPosition() {
        return this.mActionsStylist.getActionsGridView().getSelectedPosition();
    }

    /* Access modifiers changed, original: protected */
    public void onProvideFragmentTransitions() {
        if (VERSION.SDK_INT >= 21) {
            Object fade;
            int uiStyle = getUiStyle();
            Object changeBounds;
            Object sharedElementTransition;
            if (uiStyle == 0) {
                Object enterTransition = TransitionHelper.createFadeAndShortSlide(GravityCompat.END);
                TransitionHelper.exclude(enterTransition, R.id.guidedstep_background, true);
                TransitionHelper.exclude(enterTransition, R.id.guidedactions_sub_list_background, true);
                TransitionHelper.setEnterTransition((Fragment) this, enterTransition);
                fade = TransitionHelper.createFadeTransition(3);
                TransitionHelper.include(fade, R.id.guidedactions_sub_list_background);
                changeBounds = TransitionHelper.createChangeBounds(false);
                sharedElementTransition = TransitionHelper.createTransitionSet(false);
                TransitionHelper.addTransition(sharedElementTransition, fade);
                TransitionHelper.addTransition(sharedElementTransition, changeBounds);
                TransitionHelper.setSharedElementEnterTransition((Fragment) this, sharedElementTransition);
            } else if (uiStyle == 1) {
                if (this.entranceTransitionType == 0) {
                    fade = TransitionHelper.createFadeTransition(3);
                    TransitionHelper.include(fade, R.id.guidedstep_background);
                    changeBounds = TransitionHelper.createFadeAndShortSlide(GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK);
                    TransitionHelper.include(changeBounds, R.id.content_fragment);
                    TransitionHelper.include(changeBounds, R.id.action_fragment_root);
                    sharedElementTransition = TransitionHelper.createTransitionSet(false);
                    TransitionHelper.addTransition(sharedElementTransition, fade);
                    TransitionHelper.addTransition(sharedElementTransition, changeBounds);
                    TransitionHelper.setEnterTransition((Fragment) this, sharedElementTransition);
                } else {
                    fade = TransitionHelper.createFadeAndShortSlide(80);
                    TransitionHelper.include(fade, R.id.guidedstep_background_view_root);
                    sharedElementTransition = TransitionHelper.createTransitionSet(false);
                    TransitionHelper.addTransition(sharedElementTransition, fade);
                    TransitionHelper.setEnterTransition((Fragment) this, sharedElementTransition);
                }
                TransitionHelper.setSharedElementEnterTransition((Fragment) this, null);
            } else if (uiStyle == 2) {
                TransitionHelper.setEnterTransition((Fragment) this, null);
                TransitionHelper.setSharedElementEnterTransition((Fragment) this, null);
            }
            fade = TransitionHelper.createFadeAndShortSlide(GravityCompat.START);
            TransitionHelper.exclude(fade, R.id.guidedstep_background, true);
            TransitionHelper.exclude(fade, R.id.guidedactions_sub_list_background, true);
            TransitionHelper.setExitTransition(this, fade);
        }
    }

    public View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lb_guidedstep_background, container, false);
    }

    public void setUiStyle(int style) {
        int oldStyle = getUiStyle();
        Bundle arguments = getArguments();
        boolean isNew = false;
        if (arguments == null) {
            arguments = new Bundle();
            isNew = true;
        }
        arguments.putInt("uiStyle", style);
        if (isNew) {
            setArguments(arguments);
        }
        if (style != oldStyle) {
            onProvideFragmentTransitions();
        }
    }

    public int getUiStyle() {
        Bundle b = getArguments();
        if (b == null) {
            return 1;
        }
        return b.getInt("uiStyle", 1);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onProvideFragmentTransitions();
        ArrayList<GuidedAction> actions = new ArrayList();
        onCreateActions(actions, savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreActions(actions, savedInstanceState);
        }
        setActions(actions);
        ArrayList<GuidedAction> buttonActions = new ArrayList();
        onCreateButtonActions(buttonActions, savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreButtonActions(buttonActions, savedInstanceState);
        }
        setButtonActions(buttonActions);
    }

    public void onDestroyView() {
        this.mGuidanceStylist.onDestroyView();
        this.mActionsStylist.onDestroyView();
        this.mButtonActionsStylist.onDestroyView();
        this.mAdapter = null;
        this.mSubAdapter = null;
        this.mButtonAdapter = null;
        this.mAdapterGroup = null;
        super.onDestroyView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState;
        resolveTheme();
        LayoutInflater inflater2 = getThemeInflater(inflater);
        GuidedStepRootLayout root = (GuidedStepRootLayout) inflater2.inflate(R.layout.lb_guidedstep_fragment, container, false);
        root.setFocusOutStart(isFocusOutStartAllowed());
        root.setFocusOutEnd(isFocusOutEndAllowed());
        ViewGroup guidanceContainer = (ViewGroup) root.findViewById(R.id.content_fragment);
        ViewGroup actionContainer = (ViewGroup) root.findViewById(R.id.action_fragment);
        ((NonOverlappingLinearLayout) actionContainer).setFocusableViewAvailableFixEnabled(true);
        View guidanceView = this.mGuidanceStylist.onCreateView(inflater2, guidanceContainer, onCreateGuidance(bundle));
        guidanceContainer.addView(guidanceView);
        View actionsView = this.mActionsStylist.onCreateView(inflater2, actionContainer);
        actionContainer.addView(actionsView);
        View buttonActionsView = this.mButtonActionsStylist.onCreateView(inflater2, actionContainer);
        actionContainer.addView(buttonActionsView);
        EditListener editListener = new EditListener() {
            public void onImeOpen() {
                GuidedStepFragment.this.runImeAnimations(true);
            }

            public void onImeClose() {
                GuidedStepFragment.this.runImeAnimations(false);
            }

            public long onGuidedActionEditedAndProceed(GuidedAction action) {
                return GuidedStepFragment.this.onGuidedActionEditedAndProceed(action);
            }

            public void onGuidedActionEditCanceled(GuidedAction action) {
                GuidedStepFragment.this.onGuidedActionEditCanceled(action);
            }
        };
        List list = this.mActions;
        GuidedActionAdapter guidedActionAdapter = r0;
        EditListener editListener2 = editListener;
        AnonymousClass2 anonymousClass2 = new ClickListener() {
            public void onGuidedActionClicked(GuidedAction action) {
                GuidedStepFragment.this.onGuidedActionClicked(action);
                if (GuidedStepFragment.this.isExpanded()) {
                    GuidedStepFragment.this.collapseAction(true);
                } else if (action.hasSubActions() || action.hasEditableActivatorView()) {
                    GuidedStepFragment.this.expandAction(action, true);
                }
            }
        };
        View buttonActionsView2 = buttonActionsView;
        GuidedActionAdapter guidedActionAdapter2 = new GuidedActionAdapter(list, anonymousClass2, this, this.mActionsStylist, null);
        this.mAdapter = guidedActionAdapter;
        GuidedActionAdapter guidedActionAdapter3 = guidedActionAdapter2;
        guidedActionAdapter2 = new GuidedActionAdapter(this.mButtonActions, new ClickListener() {
            public void onGuidedActionClicked(GuidedAction action) {
                GuidedStepFragment.this.onGuidedActionClicked(action);
            }
        }, this, this.mButtonActionsStylist, false);
        this.mButtonAdapter = guidedActionAdapter3;
        this.mSubAdapter = new GuidedActionAdapter(null, new ClickListener() {
            public void onGuidedActionClicked(GuidedAction action) {
                if (!GuidedStepFragment.this.mActionsStylist.isInExpandTransition() && GuidedStepFragment.this.onSubGuidedActionClicked(action)) {
                    GuidedStepFragment.this.collapseSubActions();
                }
            }
        }, this, this.mActionsStylist, true);
        this.mAdapterGroup = new GuidedActionAdapterGroup();
        this.mAdapterGroup.addAdpter(this.mAdapter, this.mButtonAdapter);
        this.mAdapterGroup.addAdpter(this.mSubAdapter, null);
        EditListener editListener3 = editListener2;
        this.mAdapterGroup.setEditListener(editListener3);
        this.mActionsStylist.setEditListener(editListener3);
        this.mActionsStylist.getActionsGridView().setAdapter(this.mAdapter);
        if (this.mActionsStylist.getSubActionsGridView() != null) {
            this.mActionsStylist.getSubActionsGridView().setAdapter(this.mSubAdapter);
        }
        this.mButtonActionsStylist.getActionsGridView().setAdapter(this.mButtonAdapter);
        if (this.mButtonActions.size() == 0) {
            LayoutParams lp = (LayoutParams) buttonActionsView2.getLayoutParams();
            lp.weight = 0.0f;
            buttonActionsView2.setLayoutParams(lp);
        } else {
            Context ctx = this.mThemeWrapper != null ? this.mThemeWrapper : FragmentUtil.getContext(this);
            TypedValue typedValue = new TypedValue();
            if (ctx.getTheme().resolveAttribute(R.attr.guidedActionContentWidthWeightTwoPanels, typedValue, true)) {
                buttonActionsView = root.findViewById(R.id.action_fragment_root);
                LayoutParams lp2 = (LayoutParams) buttonActionsView.getLayoutParams();
                lp2.weight = typedValue.getFloat();
                buttonActionsView.setLayoutParams(lp2);
            }
        }
        View backgroundView = onCreateBackgroundView(inflater2, root, bundle);
        if (backgroundView != null) {
            ((FrameLayout) root.findViewById(R.id.guidedstep_background_view_root)).addView(backgroundView, 0);
        }
        return root;
    }

    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.action_fragment).requestFocus();
    }

    /* Access modifiers changed, original: final */
    public final String getAutoRestoreKey(GuidedAction action) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(EXTRA_ACTION_PREFIX);
        stringBuilder.append(action.getId());
        return stringBuilder.toString();
    }

    /* Access modifiers changed, original: final */
    public final String getButtonAutoRestoreKey(GuidedAction action) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(EXTRA_BUTTON_ACTION_PREFIX);
        stringBuilder.append(action.getId());
        return stringBuilder.toString();
    }

    static boolean isSaveEnabled(GuidedAction action) {
        return action.isAutoSaveRestoreEnabled() && action.getId() != -1;
    }

    /* Access modifiers changed, original: final */
    public final void onRestoreActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            GuidedAction action = (GuidedAction) actions.get(i);
            if (isSaveEnabled(action)) {
                action.onRestoreInstanceState(savedInstanceState, getAutoRestoreKey(action));
            }
        }
    }

    /* Access modifiers changed, original: final */
    public final void onRestoreButtonActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            GuidedAction action = (GuidedAction) actions.get(i);
            if (isSaveEnabled(action)) {
                action.onRestoreInstanceState(savedInstanceState, getButtonAutoRestoreKey(action));
            }
        }
    }

    /* Access modifiers changed, original: final */
    public final void onSaveActions(List<GuidedAction> actions, Bundle outState) {
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            GuidedAction action = (GuidedAction) actions.get(i);
            if (isSaveEnabled(action)) {
                action.onSaveInstanceState(outState, getAutoRestoreKey(action));
            }
        }
    }

    /* Access modifiers changed, original: final */
    public final void onSaveButtonActions(List<GuidedAction> actions, Bundle outState) {
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            GuidedAction action = (GuidedAction) actions.get(i);
            if (isSaveEnabled(action)) {
                action.onSaveInstanceState(outState, getButtonAutoRestoreKey(action));
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveActions(this.mActions, outState);
        onSaveButtonActions(this.mButtonActions, outState);
    }

    private static boolean isGuidedStepTheme(Context context) {
        int resId = R.attr.guidedStepThemeFlag;
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(resId, typedValue, true) && typedValue.type == 18 && typedValue.data != 0) {
            return true;
        }
        return false;
    }

    public void finishGuidedStepFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        int entryCount = fragmentManager.getBackStackEntryCount();
        if (entryCount > 0) {
            for (int i = entryCount - 1; i >= 0; i--) {
                BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
                if (isStackEntryUiStyleEntrance(entry.getName())) {
                    GuidedStepFragment top = getCurrentGuidedStepFragment(fragmentManager);
                    if (top != null) {
                        top.setUiStyle(1);
                    }
                    fragmentManager.popBackStackImmediate(entry.getId(), 1);
                    return;
                }
            }
        }
        ActivityCompat.finishAfterTransition(getActivity());
    }

    public void popBackStackToGuidedStepFragment(Class guidedStepFragmentClass, int flags) {
        if (GuidedStepFragment.class.isAssignableFrom(guidedStepFragmentClass)) {
            FragmentManager fragmentManager = getFragmentManager();
            int entryCount = fragmentManager.getBackStackEntryCount();
            String className = guidedStepFragmentClass.getName();
            if (entryCount > 0) {
                for (int i = entryCount - 1; i >= 0; i--) {
                    BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
                    if (className.equals(getGuidedStepFragmentClassName(entry.getName()))) {
                        fragmentManager.popBackStackImmediate(entry.getId(), flags);
                        return;
                    }
                }
            }
        }
    }

    public boolean isFocusOutStartAllowed() {
        return false;
    }

    public boolean isFocusOutEndAllowed() {
        return false;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void setEntranceTransitionType(int transitionType) {
        this.entranceTransitionType = transitionType;
    }

    public void openInEditMode(GuidedAction action) {
        this.mActionsStylist.openInEditMode(action);
    }

    private void resolveTheme() {
        Context context = FragmentUtil.getContext(this);
        int theme = onProvideTheme();
        if (theme == -1 && !isGuidedStepTheme(context)) {
            int resId = R.attr.guidedStepTheme;
            TypedValue typedValue = new TypedValue();
            boolean found = context.getTheme().resolveAttribute(resId, typedValue, true);
            if (found) {
                ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, typedValue.resourceId);
                if (isGuidedStepTheme(themeWrapper)) {
                    this.mThemeWrapper = themeWrapper;
                } else {
                    found = false;
                    this.mThemeWrapper = null;
                }
            }
            if (!found) {
                Log.e(TAG, "GuidedStepFragment does not have an appropriate theme set.");
            }
        } else if (theme != -1) {
            this.mThemeWrapper = new ContextThemeWrapper(context, theme);
        }
    }

    private LayoutInflater getThemeInflater(LayoutInflater inflater) {
        if (this.mThemeWrapper == null) {
            return inflater;
        }
        return inflater.cloneInContext(this.mThemeWrapper);
    }

    private int getFirstCheckedAction() {
        int size = this.mActions.size();
        for (int i = 0; i < size; i++) {
            if (((GuidedAction) this.mActions.get(i)).isChecked()) {
                return i;
            }
        }
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void runImeAnimations(boolean entering) {
        ArrayList<Animator> animators = new ArrayList();
        if (entering) {
            this.mGuidanceStylist.onImeAppearing(animators);
            this.mActionsStylist.onImeAppearing(animators);
            this.mButtonActionsStylist.onImeAppearing(animators);
        } else {
            this.mGuidanceStylist.onImeDisappearing(animators);
            this.mActionsStylist.onImeDisappearing(animators);
            this.mButtonActionsStylist.onImeDisappearing(animators);
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.start();
    }
}
