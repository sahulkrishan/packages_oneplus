package android.support.v17.leanback.app;

import android.os.Bundle;
import android.support.v17.leanback.R;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.util.StateMachine.State;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnChildLaidOutListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VerticalGridSupportFragment extends BaseSupportFragment {
    static final boolean DEBUG = false;
    static final String TAG = "VerticalGF";
    final State STATE_SET_ENTRANCE_START_STATE = new State("SET_ENTRANCE_START_STATE") {
        public void run() {
            VerticalGridSupportFragment.this.setEntranceTransitionState(false);
        }
    };
    private ObjectAdapter mAdapter;
    private final OnChildLaidOutListener mChildLaidOutListener = new OnChildLaidOutListener() {
        public void onChildLaidOut(ViewGroup parent, View view, int position, long id) {
            if (position == 0) {
                VerticalGridSupportFragment.this.showOrHideTitle();
            }
        }
    };
    private VerticalGridPresenter mGridPresenter;
    ViewHolder mGridViewHolder;
    private OnItemViewClickedListener mOnItemViewClickedListener;
    OnItemViewSelectedListener mOnItemViewSelectedListener;
    private Object mSceneAfterEntranceTransition;
    private int mSelectedPosition = -1;
    private final OnItemViewSelectedListener mViewSelectedListener = new OnItemViewSelectedListener() {
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            VerticalGridSupportFragment.this.gridOnItemSelected(VerticalGridSupportFragment.this.mGridViewHolder.getGridView().getSelectedPosition());
            if (VerticalGridSupportFragment.this.mOnItemViewSelectedListener != null) {
                VerticalGridSupportFragment.this.mOnItemViewSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder, row);
            }
        }
    };

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineStates() {
        super.createStateMachineStates();
        this.mStateMachine.addState(this.STATE_SET_ENTRANCE_START_STATE);
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineTransitions() {
        super.createStateMachineTransitions();
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_SET_ENTRANCE_START_STATE, this.EVT_ON_CREATEVIEW);
    }

    public void setGridPresenter(VerticalGridPresenter gridPresenter) {
        if (gridPresenter != null) {
            this.mGridPresenter = gridPresenter;
            this.mGridPresenter.setOnItemViewSelectedListener(this.mViewSelectedListener);
            if (this.mOnItemViewClickedListener != null) {
                this.mGridPresenter.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Grid presenter may not be null");
    }

    public VerticalGridPresenter getGridPresenter() {
        return this.mGridPresenter;
    }

    public void setAdapter(ObjectAdapter adapter) {
        this.mAdapter = adapter;
        updateAdapter();
    }

    public ObjectAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        this.mOnItemViewSelectedListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public void gridOnItemSelected(int position) {
        if (position != this.mSelectedPosition) {
            this.mSelectedPosition = position;
            showOrHideTitle();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void showOrHideTitle() {
        if (this.mGridViewHolder.getGridView().findViewHolderForAdapterPosition(this.mSelectedPosition) != null) {
            if (this.mGridViewHolder.getGridView().hasPreviousViewInSameRow(this.mSelectedPosition)) {
                showTitle(false);
            } else {
                showTitle(true);
            }
        }
    }

    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        this.mOnItemViewClickedListener = listener;
        if (this.mGridPresenter != null) {
            this.mGridPresenter.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
        }
    }

    public OnItemViewClickedListener getOnItemViewClickedListener() {
        return this.mOnItemViewClickedListener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.lb_vertical_grid_fragment, container, false);
        installTitleView(inflater, (ViewGroup) root.findViewById(R.id.grid_frame), savedInstanceState);
        getProgressBarManager().setRootView(root);
        ViewGroup gridDock = (ViewGroup) root.findViewById(R.id.browse_grid_dock);
        this.mGridViewHolder = this.mGridPresenter.onCreateViewHolder(gridDock);
        gridDock.addView(this.mGridViewHolder.view);
        this.mGridViewHolder.getGridView().setOnChildLaidOutListener(this.mChildLaidOutListener);
        this.mSceneAfterEntranceTransition = TransitionHelper.createScene(gridDock, new Runnable() {
            public void run() {
                VerticalGridSupportFragment.this.setEntranceTransitionState(true);
            }
        });
        updateAdapter();
        return root;
    }

    private void setupFocusSearchListener() {
        ((BrowseFrameLayout) getView().findViewById(R.id.grid_frame)).setOnFocusSearchListener(getTitleHelper().getOnFocusSearchListener());
    }

    public void onStart() {
        super.onStart();
        setupFocusSearchListener();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.mGridViewHolder = null;
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        if (this.mGridViewHolder != null && this.mGridViewHolder.getGridView().getAdapter() != null) {
            this.mGridViewHolder.getGridView().setSelectedPositionSmooth(position);
        }
    }

    private void updateAdapter() {
        if (this.mGridViewHolder != null) {
            this.mGridPresenter.onBindViewHolder(this.mGridViewHolder, this.mAdapter);
            if (this.mSelectedPosition != -1) {
                this.mGridViewHolder.getGridView().setSelectedPosition(this.mSelectedPosition);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Object createEntranceTransition() {
        return TransitionHelper.loadTransition(getContext(), R.transition.lb_vertical_grid_entrance_transition);
    }

    /* Access modifiers changed, original: protected */
    public void runEntranceTransition(Object entranceTransition) {
        TransitionHelper.runTransition(this.mSceneAfterEntranceTransition, entranceTransition);
    }

    /* Access modifiers changed, original: 0000 */
    public void setEntranceTransitionState(boolean afterTransition) {
        this.mGridPresenter.setEntranceTransitionState(this.mGridViewHolder, afterTransition);
    }
}
