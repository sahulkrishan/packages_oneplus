package android.support.v17.leanback.app;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v17.leanback.R;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.util.StateMachine.Event;
import android.support.v17.leanback.util.StateMachine.State;
import android.support.v17.leanback.widget.BaseOnItemViewClickedListener;
import android.support.v17.leanback.widget.BaseOnItemViewSelectedListener;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.BrowseFrameLayout.OnChildFocusListener;
import android.support.v17.leanback.widget.BrowseFrameLayout.OnFocusSearchListener;
import android.support.v17.leanback.widget.DetailsParallax;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ItemBridgeAdapter.AdapterListener;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import java.lang.ref.WeakReference;

public class DetailsSupportFragment extends BaseSupportFragment {
    static final boolean DEBUG = false;
    static final String TAG = "DetailsSupportFragment";
    final Event EVT_DETAILS_ROW_LOADED = new Event("onFirstRowLoaded");
    final Event EVT_ENTER_TRANSIITON_DONE = new Event("onEnterTransitionDone");
    final Event EVT_NO_ENTER_TRANSITION = new Event("EVT_NO_ENTER_TRANSITION");
    final Event EVT_ONSTART = new Event("onStart");
    final Event EVT_SWITCH_TO_VIDEO = new Event("switchToVideo");
    final State STATE_ENTER_TRANSITION_ADDLISTENER = new State("STATE_ENTER_TRANSITION_PENDING") {
        public void run() {
            TransitionHelper.addTransitionListener(TransitionHelper.getEnterTransition(DetailsSupportFragment.this.getActivity().getWindow()), DetailsSupportFragment.this.mEnterTransitionListener);
        }
    };
    final State STATE_ENTER_TRANSITION_CANCEL = new State("STATE_ENTER_TRANSITION_CANCEL", false, false) {
        public void run() {
            if (DetailsSupportFragment.this.mWaitEnterTransitionTimeout != null) {
                DetailsSupportFragment.this.mWaitEnterTransitionTimeout.mRef.clear();
            }
            if (DetailsSupportFragment.this.getActivity() != null) {
                Window window = DetailsSupportFragment.this.getActivity().getWindow();
                Object returnTransition = TransitionHelper.getReturnTransition(window);
                Object sharedReturnTransition = TransitionHelper.getSharedElementReturnTransition(window);
                TransitionHelper.setEnterTransition(window, null);
                TransitionHelper.setSharedElementEnterTransition(window, null);
                TransitionHelper.setReturnTransition(window, returnTransition);
                TransitionHelper.setSharedElementReturnTransition(window, sharedReturnTransition);
            }
        }
    };
    final State STATE_ENTER_TRANSITION_COMPLETE = new State("STATE_ENTER_TRANSIITON_COMPLETE", true, false);
    final State STATE_ENTER_TRANSITION_INIT = new State("STATE_ENTER_TRANSIITON_INIT");
    final State STATE_ENTER_TRANSITION_PENDING = new State("STATE_ENTER_TRANSITION_PENDING") {
        public void run() {
            if (DetailsSupportFragment.this.mWaitEnterTransitionTimeout == null) {
                WaitEnterTransitionTimeout waitEnterTransitionTimeout = new WaitEnterTransitionTimeout(DetailsSupportFragment.this);
            }
        }
    };
    final State STATE_ON_SAFE_START = new State("STATE_ON_SAFE_START") {
        public void run() {
            DetailsSupportFragment.this.onSafeStart();
        }
    };
    final State STATE_SET_ENTRANCE_START_STATE = new State("STATE_SET_ENTRANCE_START_STATE") {
        public void run() {
            DetailsSupportFragment.this.mRowsSupportFragment.setEntranceTransitionState(false);
        }
    };
    final State STATE_SWITCH_TO_VIDEO_IN_ON_CREATE = new State("STATE_SWITCH_TO_VIDEO_IN_ON_CREATE", false, false) {
        public void run() {
            DetailsSupportFragment.this.switchToVideoBeforeVideoSupportFragmentCreated();
        }
    };
    ObjectAdapter mAdapter;
    Drawable mBackgroundDrawable;
    View mBackgroundView;
    int mContainerListAlignTop;
    DetailsSupportFragmentBackgroundController mDetailsBackgroundController;
    DetailsParallax mDetailsParallax;
    TransitionListener mEnterTransitionListener = new TransitionListener() {
        public void onTransitionStart(Object transition) {
            if (DetailsSupportFragment.this.mWaitEnterTransitionTimeout != null) {
                DetailsSupportFragment.this.mWaitEnterTransitionTimeout.mRef.clear();
            }
        }

        public void onTransitionCancel(Object transition) {
            DetailsSupportFragment.this.mStateMachine.fireEvent(DetailsSupportFragment.this.EVT_ENTER_TRANSIITON_DONE);
        }

        public void onTransitionEnd(Object transition) {
            DetailsSupportFragment.this.mStateMachine.fireEvent(DetailsSupportFragment.this.EVT_ENTER_TRANSIITON_DONE);
        }
    };
    BaseOnItemViewSelectedListener mExternalOnItemViewSelectedListener;
    BaseOnItemViewClickedListener mOnItemViewClickedListener;
    final BaseOnItemViewSelectedListener<Object> mOnItemViewSelectedListener = new BaseOnItemViewSelectedListener<Object>() {
        public void onItemSelected(ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Object row) {
            DetailsSupportFragment.this.onRowSelected(DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView().getSelectedPosition(), DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView().getSelectedSubPosition());
            if (DetailsSupportFragment.this.mExternalOnItemViewSelectedListener != null) {
                DetailsSupportFragment.this.mExternalOnItemViewSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder, row);
            }
        }
    };
    boolean mPendingFocusOnVideo = false;
    TransitionListener mReturnTransitionListener = new TransitionListener() {
        public void onTransitionStart(Object transition) {
            DetailsSupportFragment.this.onReturnTransitionStart();
        }
    };
    BrowseFrameLayout mRootView;
    RowsSupportFragment mRowsSupportFragment;
    Object mSceneAfterEntranceTransition;
    final SetSelectionRunnable mSetSelectionRunnable = new SetSelectionRunnable();
    Fragment mVideoSupportFragment;
    WaitEnterTransitionTimeout mWaitEnterTransitionTimeout;

    private class SetSelectionRunnable implements Runnable {
        int mPosition;
        boolean mSmooth = true;

        SetSelectionRunnable() {
        }

        public void run() {
            if (DetailsSupportFragment.this.mRowsSupportFragment != null) {
                DetailsSupportFragment.this.mRowsSupportFragment.setSelectedPosition(this.mPosition, this.mSmooth);
            }
        }
    }

    static class WaitEnterTransitionTimeout implements Runnable {
        static final long WAIT_ENTERTRANSITION_START = 200;
        final WeakReference<DetailsSupportFragment> mRef;

        WaitEnterTransitionTimeout(DetailsSupportFragment f) {
            this.mRef = new WeakReference(f);
            f.getView().postDelayed(this, WAIT_ENTERTRANSITION_START);
        }

        public void run() {
            DetailsSupportFragment f = (DetailsSupportFragment) this.mRef.get();
            if (f != null) {
                f.mStateMachine.fireEvent(f.EVT_ENTER_TRANSIITON_DONE);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void switchToVideoBeforeVideoSupportFragmentCreated() {
        this.mDetailsBackgroundController.switchToVideoBeforeCreate();
        showTitle(false);
        this.mPendingFocusOnVideo = true;
        slideOutGridView();
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineStates() {
        super.createStateMachineStates();
        this.mStateMachine.addState(this.STATE_SET_ENTRANCE_START_STATE);
        this.mStateMachine.addState(this.STATE_ON_SAFE_START);
        this.mStateMachine.addState(this.STATE_SWITCH_TO_VIDEO_IN_ON_CREATE);
        this.mStateMachine.addState(this.STATE_ENTER_TRANSITION_INIT);
        this.mStateMachine.addState(this.STATE_ENTER_TRANSITION_ADDLISTENER);
        this.mStateMachine.addState(this.STATE_ENTER_TRANSITION_CANCEL);
        this.mStateMachine.addState(this.STATE_ENTER_TRANSITION_PENDING);
        this.mStateMachine.addState(this.STATE_ENTER_TRANSITION_COMPLETE);
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineTransitions() {
        super.createStateMachineTransitions();
        this.mStateMachine.addTransition(this.STATE_START, this.STATE_ENTER_TRANSITION_INIT, this.EVT_ON_CREATE);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_INIT, this.STATE_ENTER_TRANSITION_COMPLETE, this.COND_TRANSITION_NOT_SUPPORTED);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_INIT, this.STATE_ENTER_TRANSITION_COMPLETE, this.EVT_NO_ENTER_TRANSITION);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_INIT, this.STATE_ENTER_TRANSITION_CANCEL, this.EVT_SWITCH_TO_VIDEO);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_CANCEL, this.STATE_ENTER_TRANSITION_COMPLETE);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_INIT, this.STATE_ENTER_TRANSITION_ADDLISTENER, this.EVT_ON_CREATEVIEW);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_ADDLISTENER, this.STATE_ENTER_TRANSITION_COMPLETE, this.EVT_ENTER_TRANSIITON_DONE);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_ADDLISTENER, this.STATE_ENTER_TRANSITION_PENDING, this.EVT_DETAILS_ROW_LOADED);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_PENDING, this.STATE_ENTER_TRANSITION_COMPLETE, this.EVT_ENTER_TRANSIITON_DONE);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_COMPLETE, this.STATE_ENTRANCE_PERFORM);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_INIT, this.STATE_SWITCH_TO_VIDEO_IN_ON_CREATE, this.EVT_SWITCH_TO_VIDEO);
        this.mStateMachine.addTransition(this.STATE_SWITCH_TO_VIDEO_IN_ON_CREATE, this.STATE_ENTRANCE_COMPLETE);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_COMPLETE, this.STATE_SWITCH_TO_VIDEO_IN_ON_CREATE, this.EVT_SWITCH_TO_VIDEO);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_SET_ENTRANCE_START_STATE, this.EVT_ONSTART);
        this.mStateMachine.addTransition(this.STATE_START, this.STATE_ON_SAFE_START, this.EVT_ONSTART);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_COMPLETE, this.STATE_ON_SAFE_START);
        this.mStateMachine.addTransition(this.STATE_ENTER_TRANSITION_COMPLETE, this.STATE_ON_SAFE_START);
    }

    public void setAdapter(ObjectAdapter adapter) {
        this.mAdapter = adapter;
        Presenter[] presenters = adapter.getPresenterSelector().getPresenters();
        if (presenters != null) {
            for (Presenter presenter : presenters) {
                setupPresenter(presenter);
            }
        } else {
            Log.e(TAG, "PresenterSelector.getPresenters() not implemented");
        }
        if (this.mRowsSupportFragment != null) {
            this.mRowsSupportFragment.setAdapter(adapter);
        }
    }

    public ObjectAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setOnItemViewSelectedListener(BaseOnItemViewSelectedListener listener) {
        this.mExternalOnItemViewSelectedListener = listener;
    }

    public void setOnItemViewClickedListener(BaseOnItemViewClickedListener listener) {
        if (this.mOnItemViewClickedListener != listener) {
            this.mOnItemViewClickedListener = listener;
            if (this.mRowsSupportFragment != null) {
                this.mRowsSupportFragment.setOnItemViewClickedListener(listener);
            }
        }
    }

    public BaseOnItemViewClickedListener getOnItemViewClickedListener() {
        return this.mOnItemViewClickedListener;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContainerListAlignTop = getResources().getDimensionPixelSize(R.dimen.lb_details_rows_align_top);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (TransitionHelper.getEnterTransition(activity.getWindow()) == null) {
                this.mStateMachine.fireEvent(this.EVT_NO_ENTER_TRANSITION);
            }
            Object transition = TransitionHelper.getReturnTransition(activity.getWindow());
            if (transition != null) {
                TransitionHelper.addTransitionListener(transition, this.mReturnTransitionListener);
                return;
            }
            return;
        }
        this.mStateMachine.fireEvent(this.EVT_NO_ENTER_TRANSITION);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = (BrowseFrameLayout) inflater.inflate(R.layout.lb_details_fragment, container, false);
        this.mBackgroundView = this.mRootView.findViewById(R.id.details_background_view);
        if (this.mBackgroundView != null) {
            this.mBackgroundView.setBackground(this.mBackgroundDrawable);
        }
        this.mRowsSupportFragment = (RowsSupportFragment) getChildFragmentManager().findFragmentById(R.id.details_rows_dock);
        if (this.mRowsSupportFragment == null) {
            this.mRowsSupportFragment = new RowsSupportFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.details_rows_dock, this.mRowsSupportFragment).commit();
        }
        installTitleView(inflater, this.mRootView, savedInstanceState);
        this.mRowsSupportFragment.setAdapter(this.mAdapter);
        this.mRowsSupportFragment.setOnItemViewSelectedListener(this.mOnItemViewSelectedListener);
        this.mRowsSupportFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
        this.mSceneAfterEntranceTransition = TransitionHelper.createScene(this.mRootView, new Runnable() {
            public void run() {
                DetailsSupportFragment.this.mRowsSupportFragment.setEntranceTransitionState(true);
            }
        });
        setupDpadNavigation();
        if (VERSION.SDK_INT >= 21) {
            this.mRowsSupportFragment.setExternalAdapterListener(new AdapterListener() {
                public void onCreate(ItemBridgeAdapter.ViewHolder vh) {
                    if (DetailsSupportFragment.this.mDetailsParallax != null && (vh.getViewHolder() instanceof FullWidthDetailsOverviewRowPresenter.ViewHolder)) {
                        ((FullWidthDetailsOverviewRowPresenter.ViewHolder) vh.getViewHolder()).getOverviewView().setTag(R.id.lb_parallax_source, DetailsSupportFragment.this.mDetailsParallax);
                    }
                }
            });
        }
        return this.mRootView;
    }

    /* Access modifiers changed, original: protected */
    @Deprecated
    public View inflateTitle(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return super.onInflateTitleView(inflater, parent, savedInstanceState);
    }

    public View onInflateTitleView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflateTitle(inflater, parent, savedInstanceState);
    }

    /* Access modifiers changed, original: 0000 */
    public void setVerticalGridViewLayout(VerticalGridView listview) {
        listview.setItemAlignmentOffset(-this.mContainerListAlignTop);
        listview.setItemAlignmentOffsetPercent(-1.0f);
        listview.setWindowAlignmentOffset(0);
        listview.setWindowAlignmentOffsetPercent(-1.0f);
        listview.setWindowAlignment(0);
    }

    /* Access modifiers changed, original: protected */
    public void setupPresenter(Presenter rowPresenter) {
        if (rowPresenter instanceof FullWidthDetailsOverviewRowPresenter) {
            setupDetailsOverviewRowPresenter((FullWidthDetailsOverviewRowPresenter) rowPresenter);
        }
    }

    /* Access modifiers changed, original: protected */
    public void setupDetailsOverviewRowPresenter(FullWidthDetailsOverviewRowPresenter presenter) {
        ItemAlignmentFacet facet = new ItemAlignmentFacet();
        ItemAlignmentDef alignDef1 = new ItemAlignmentDef();
        alignDef1.setItemAlignmentViewId(R.id.details_frame);
        alignDef1.setItemAlignmentOffset(-getResources().getDimensionPixelSize(R.dimen.lb_details_v2_align_pos_for_actions));
        alignDef1.setItemAlignmentOffsetPercent(0.0f);
        ItemAlignmentDef alignDef2 = new ItemAlignmentDef();
        alignDef2.setItemAlignmentViewId(R.id.details_frame);
        alignDef2.setItemAlignmentFocusViewId(R.id.details_overview_description);
        alignDef2.setItemAlignmentOffset(-getResources().getDimensionPixelSize(R.dimen.lb_details_v2_align_pos_for_description));
        alignDef2.setItemAlignmentOffsetPercent(0.0f);
        facet.setAlignmentDefs(new ItemAlignmentDef[]{alignDef1, alignDef2});
        presenter.setFacet(ItemAlignmentFacet.class, facet);
    }

    /* Access modifiers changed, original: 0000 */
    public VerticalGridView getVerticalGridView() {
        return this.mRowsSupportFragment == null ? null : this.mRowsSupportFragment.getVerticalGridView();
    }

    public RowsSupportFragment getRowsSupportFragment() {
        return this.mRowsSupportFragment;
    }

    private void setupChildFragmentLayout() {
        setVerticalGridViewLayout(this.mRowsSupportFragment.getVerticalGridView());
    }

    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    public void setSelectedPosition(int position, boolean smooth) {
        this.mSetSelectionRunnable.mPosition = position;
        this.mSetSelectionRunnable.mSmooth = smooth;
        if (getView() != null && getView().getHandler() != null) {
            getView().getHandler().post(this.mSetSelectionRunnable);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void switchToVideo() {
        if (this.mVideoSupportFragment == null || this.mVideoSupportFragment.getView() == null) {
            this.mStateMachine.fireEvent(this.EVT_SWITCH_TO_VIDEO);
        } else {
            this.mVideoSupportFragment.getView().requestFocus();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void switchToRows() {
        this.mPendingFocusOnVideo = false;
        VerticalGridView verticalGridView = getVerticalGridView();
        if (verticalGridView != null && verticalGridView.getChildCount() > 0) {
            verticalGridView.requestFocus();
        }
    }

    /* Access modifiers changed, original: final */
    public final Fragment findOrCreateVideoSupportFragment() {
        if (this.mVideoSupportFragment != null) {
            return this.mVideoSupportFragment;
        }
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.video_surface_container);
        if (fragment == null && this.mDetailsBackgroundController != null) {
            FragmentTransaction ft2 = getChildFragmentManager().beginTransaction();
            int i = R.id.video_surface_container;
            Fragment onCreateVideoSupportFragment = this.mDetailsBackgroundController.onCreateVideoSupportFragment();
            fragment = onCreateVideoSupportFragment;
            ft2.add(i, onCreateVideoSupportFragment);
            ft2.commit();
            if (this.mPendingFocusOnVideo) {
                getView().post(new Runnable() {
                    public void run() {
                        if (DetailsSupportFragment.this.getView() != null) {
                            DetailsSupportFragment.this.switchToVideo();
                        }
                        DetailsSupportFragment.this.mPendingFocusOnVideo = false;
                    }
                });
            }
        }
        this.mVideoSupportFragment = fragment;
        return this.mVideoSupportFragment;
    }

    /* Access modifiers changed, original: 0000 */
    public void onRowSelected(int selectedPosition, int selectedSubPosition) {
        ObjectAdapter adapter = getAdapter();
        int i = 0;
        if (this.mRowsSupportFragment == null || this.mRowsSupportFragment.getView() == null || !this.mRowsSupportFragment.getView().hasFocus() || this.mPendingFocusOnVideo || !(adapter == null || adapter.size() == 0 || (getVerticalGridView().getSelectedPosition() == 0 && getVerticalGridView().getSelectedSubPosition() == 0))) {
            showTitle(false);
        } else {
            showTitle(true);
        }
        if (adapter != null && adapter.size() > selectedPosition) {
            VerticalGridView gridView = getVerticalGridView();
            int count = gridView.getChildCount();
            if (count > 0) {
                this.mStateMachine.fireEvent(this.EVT_DETAILS_ROW_LOADED);
            }
            while (i < count) {
                ItemBridgeAdapter.ViewHolder bridgeViewHolder = (ItemBridgeAdapter.ViewHolder) gridView.getChildViewHolder(gridView.getChildAt(i));
                RowPresenter rowPresenter = (RowPresenter) bridgeViewHolder.getPresenter();
                onSetRowStatus(rowPresenter, rowPresenter.getRowViewHolder(bridgeViewHolder.getViewHolder()), bridgeViewHolder.getAdapterPosition(), selectedPosition, selectedSubPosition);
                i++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @CallSuper
    public void onSafeStart() {
        if (this.mDetailsBackgroundController != null) {
            this.mDetailsBackgroundController.onStart();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @CallSuper
    public void onReturnTransitionStart() {
        if (this.mDetailsBackgroundController != null && !this.mDetailsBackgroundController.disableVideoParallax() && this.mVideoSupportFragment != null) {
            FragmentTransaction ft2 = getChildFragmentManager().beginTransaction();
            ft2.remove(this.mVideoSupportFragment);
            ft2.commit();
            this.mVideoSupportFragment = null;
        }
    }

    public void onStop() {
        if (this.mDetailsBackgroundController != null) {
            this.mDetailsBackgroundController.onStop();
        }
        super.onStop();
    }

    /* Access modifiers changed, original: protected */
    public void onSetRowStatus(RowPresenter presenter, RowPresenter.ViewHolder viewHolder, int adapterPosition, int selectedPosition, int selectedSubPosition) {
        if (presenter instanceof FullWidthDetailsOverviewRowPresenter) {
            onSetDetailsOverviewRowStatus((FullWidthDetailsOverviewRowPresenter) presenter, (FullWidthDetailsOverviewRowPresenter.ViewHolder) viewHolder, adapterPosition, selectedPosition, selectedSubPosition);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSetDetailsOverviewRowStatus(FullWidthDetailsOverviewRowPresenter presenter, FullWidthDetailsOverviewRowPresenter.ViewHolder viewHolder, int adapterPosition, int selectedPosition, int selectedSubPosition) {
        if (selectedPosition > adapterPosition) {
            presenter.setState(viewHolder, 0);
        } else if (selectedPosition == adapterPosition && selectedSubPosition == 1) {
            presenter.setState(viewHolder, 0);
        } else if (selectedPosition == adapterPosition && selectedSubPosition == 0) {
            presenter.setState(viewHolder, 1);
        } else {
            presenter.setState(viewHolder, 2);
        }
    }

    public void onStart() {
        super.onStart();
        setupChildFragmentLayout();
        this.mStateMachine.fireEvent(this.EVT_ONSTART);
        if (this.mDetailsParallax != null) {
            this.mDetailsParallax.setRecyclerView(this.mRowsSupportFragment.getVerticalGridView());
        }
        if (this.mPendingFocusOnVideo) {
            slideOutGridView();
        } else if (!getView().hasFocus()) {
            this.mRowsSupportFragment.getVerticalGridView().requestFocus();
        }
    }

    /* Access modifiers changed, original: protected */
    public Object createEntranceTransition() {
        return TransitionHelper.loadTransition(getContext(), R.transition.lb_details_enter_transition);
    }

    /* Access modifiers changed, original: protected */
    public void runEntranceTransition(Object entranceTransition) {
        TransitionHelper.runTransition(this.mSceneAfterEntranceTransition, entranceTransition);
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionEnd() {
        this.mRowsSupportFragment.onTransitionEnd();
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionPrepare() {
        this.mRowsSupportFragment.onTransitionPrepare();
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionStart() {
        this.mRowsSupportFragment.onTransitionStart();
    }

    public DetailsParallax getParallax() {
        if (this.mDetailsParallax == null) {
            this.mDetailsParallax = new DetailsParallax();
            if (!(this.mRowsSupportFragment == null || this.mRowsSupportFragment.getView() == null)) {
                this.mDetailsParallax.setRecyclerView(this.mRowsSupportFragment.getVerticalGridView());
            }
        }
        return this.mDetailsParallax;
    }

    /* Access modifiers changed, original: 0000 */
    public void setBackgroundDrawable(Drawable drawable) {
        if (this.mBackgroundView != null) {
            this.mBackgroundView.setBackground(drawable);
        }
        this.mBackgroundDrawable = drawable;
    }

    /* Access modifiers changed, original: 0000 */
    public void setupDpadNavigation() {
        this.mRootView.setOnChildFocusListener(new OnChildFocusListener() {
            public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                return false;
            }

            public void onRequestChildFocus(View child, View focused) {
                if (child == DetailsSupportFragment.this.mRootView.getFocusedChild()) {
                    return;
                }
                if (child.getId() == R.id.details_fragment_root) {
                    if (!DetailsSupportFragment.this.mPendingFocusOnVideo) {
                        DetailsSupportFragment.this.slideInGridView();
                        DetailsSupportFragment.this.showTitle(true);
                    }
                } else if (child.getId() == R.id.video_surface_container) {
                    DetailsSupportFragment.this.slideOutGridView();
                    DetailsSupportFragment.this.showTitle(false);
                } else {
                    DetailsSupportFragment.this.showTitle(true);
                }
            }
        });
        this.mRootView.setOnFocusSearchListener(new OnFocusSearchListener() {
            public View onFocusSearch(View focused, int direction) {
                if (DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView() == null || !DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView().hasFocus()) {
                    if (DetailsSupportFragment.this.getTitleView() != null && DetailsSupportFragment.this.getTitleView().hasFocus() && direction == 130 && DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView() != null) {
                        return DetailsSupportFragment.this.mRowsSupportFragment.getVerticalGridView();
                    }
                } else if (direction == 33) {
                    if (DetailsSupportFragment.this.mDetailsBackgroundController != null && DetailsSupportFragment.this.mDetailsBackgroundController.canNavigateToVideoSupportFragment() && DetailsSupportFragment.this.mVideoSupportFragment != null && DetailsSupportFragment.this.mVideoSupportFragment.getView() != null) {
                        return DetailsSupportFragment.this.mVideoSupportFragment.getView();
                    }
                    if (DetailsSupportFragment.this.getTitleView() != null && DetailsSupportFragment.this.getTitleView().hasFocusable()) {
                        return DetailsSupportFragment.this.getTitleView();
                    }
                }
                return focused;
            }
        });
        this.mRootView.setOnDispatchKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (DetailsSupportFragment.this.mVideoSupportFragment == null || DetailsSupportFragment.this.mVideoSupportFragment.getView() == null || !DetailsSupportFragment.this.mVideoSupportFragment.getView().hasFocus() || ((keyCode != 4 && keyCode != 111) || DetailsSupportFragment.this.getVerticalGridView().getChildCount() <= 0)) {
                    return false;
                }
                DetailsSupportFragment.this.getVerticalGridView().requestFocus();
                return true;
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void slideOutGridView() {
        if (getVerticalGridView() != null) {
            getVerticalGridView().animateOut();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void slideInGridView() {
        if (getVerticalGridView() != null) {
            getVerticalGridView().animateIn();
        }
    }
}
