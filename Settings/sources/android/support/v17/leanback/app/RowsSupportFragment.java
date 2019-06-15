package android.support.v17.leanback.app;

import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.app.BrowseSupportFragment.FragmentHost;
import android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentAdapterProvider;
import android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentRowsAdapterProvider;
import android.support.v17.leanback.widget.BaseOnItemViewClickedListener;
import android.support.v17.leanback.widget.BaseOnItemViewSelectedListener;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ItemBridgeAdapter.AdapterListener;
import android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Presenter.ViewHolderTask;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.RecycledViewPool;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import java.util.ArrayList;

public class RowsSupportFragment extends BaseRowSupportFragment implements MainFragmentRowsAdapterProvider, MainFragmentAdapterProvider {
    static final int ALIGN_TOP_NOT_SET = Integer.MIN_VALUE;
    static final boolean DEBUG = false;
    static final String TAG = "RowsSupportFragment";
    boolean mAfterEntranceTransition = true;
    private int mAlignedTop = Integer.MIN_VALUE;
    private final AdapterListener mBridgeAdapterListener = new AdapterListener() {
        public void onAddPresenter(Presenter presenter, int type) {
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onAddPresenter(presenter, type);
            }
        }

        public void onCreate(ViewHolder vh) {
            VerticalGridView listView = RowsSupportFragment.this.getVerticalGridView();
            if (listView != null) {
                listView.setClipChildren(false);
            }
            RowsSupportFragment.this.setupSharedViewPool(vh);
            RowsSupportFragment.this.mViewsCreated = true;
            vh.setExtraObject(new RowViewHolderExtra(vh));
            RowsSupportFragment.setRowViewSelected(vh, false, true);
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onCreate(vh);
            }
            RowPresenter.ViewHolder rowVh = ((RowPresenter) vh.getPresenter()).getRowViewHolder(vh.getViewHolder());
            rowVh.setOnItemViewSelectedListener(RowsSupportFragment.this.mOnItemViewSelectedListener);
            rowVh.setOnItemViewClickedListener(RowsSupportFragment.this.mOnItemViewClickedListener);
        }

        public void onAttachedToWindow(ViewHolder vh) {
            RowsSupportFragment.setRowViewExpanded(vh, RowsSupportFragment.this.mExpand);
            RowPresenter rowPresenter = (RowPresenter) vh.getPresenter();
            RowPresenter.ViewHolder rowVh = rowPresenter.getRowViewHolder(vh.getViewHolder());
            rowPresenter.setEntranceTransitionState(rowVh, RowsSupportFragment.this.mAfterEntranceTransition);
            rowPresenter.freeze(rowVh, RowsSupportFragment.this.mFreezeRows);
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onAttachedToWindow(vh);
            }
        }

        public void onDetachedFromWindow(ViewHolder vh) {
            if (RowsSupportFragment.this.mSelectedViewHolder == vh) {
                RowsSupportFragment.setRowViewSelected(RowsSupportFragment.this.mSelectedViewHolder, false, true);
                RowsSupportFragment.this.mSelectedViewHolder = null;
            }
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onDetachedFromWindow(vh);
            }
        }

        public void onBind(ViewHolder vh) {
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onBind(vh);
            }
        }

        public void onUnbind(ViewHolder vh) {
            RowsSupportFragment.setRowViewSelected(vh, false, true);
            if (RowsSupportFragment.this.mExternalAdapterListener != null) {
                RowsSupportFragment.this.mExternalAdapterListener.onUnbind(vh);
            }
        }
    };
    boolean mExpand = true;
    AdapterListener mExternalAdapterListener;
    boolean mFreezeRows;
    private MainFragmentAdapter mMainFragmentAdapter;
    private MainFragmentRowsAdapter mMainFragmentRowsAdapter;
    BaseOnItemViewClickedListener mOnItemViewClickedListener;
    BaseOnItemViewSelectedListener mOnItemViewSelectedListener;
    private ArrayList<Presenter> mPresenterMapper;
    private RecycledViewPool mRecycledViewPool;
    int mSelectAnimatorDuration;
    Interpolator mSelectAnimatorInterpolator = new DecelerateInterpolator(2.0f);
    ViewHolder mSelectedViewHolder;
    private int mSubPosition;
    boolean mViewsCreated;

    final class RowViewHolderExtra implements TimeListener {
        final RowPresenter mRowPresenter;
        final Presenter.ViewHolder mRowViewHolder;
        final TimeAnimator mSelectAnimator = new TimeAnimator();
        int mSelectAnimatorDurationInUse;
        Interpolator mSelectAnimatorInterpolatorInUse;
        float mSelectLevelAnimDelta;
        float mSelectLevelAnimStart;

        RowViewHolderExtra(ViewHolder ibvh) {
            this.mRowPresenter = (RowPresenter) ibvh.getPresenter();
            this.mRowViewHolder = ibvh.getViewHolder();
            this.mSelectAnimator.setTimeListener(this);
        }

        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            if (this.mSelectAnimator.isRunning()) {
                updateSelect(totalTime, deltaTime);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void updateSelect(long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= ((long) this.mSelectAnimatorDurationInUse)) {
                fraction = 1.0f;
                this.mSelectAnimator.end();
            } else {
                fraction = (float) (((double) totalTime) / ((double) this.mSelectAnimatorDurationInUse));
            }
            if (this.mSelectAnimatorInterpolatorInUse != null) {
                fraction = this.mSelectAnimatorInterpolatorInUse.getInterpolation(fraction);
            }
            this.mRowPresenter.setSelectLevel(this.mRowViewHolder, this.mSelectLevelAnimStart + (this.mSelectLevelAnimDelta * fraction));
        }

        /* Access modifiers changed, original: 0000 */
        public void animateSelect(boolean select, boolean immediate) {
            this.mSelectAnimator.end();
            float end = select ? 1.0f : 0.0f;
            if (immediate) {
                this.mRowPresenter.setSelectLevel(this.mRowViewHolder, end);
            } else if (this.mRowPresenter.getSelectLevel(this.mRowViewHolder) != end) {
                this.mSelectAnimatorDurationInUse = RowsSupportFragment.this.mSelectAnimatorDuration;
                this.mSelectAnimatorInterpolatorInUse = RowsSupportFragment.this.mSelectAnimatorInterpolator;
                this.mSelectLevelAnimStart = this.mRowPresenter.getSelectLevel(this.mRowViewHolder);
                this.mSelectLevelAnimDelta = end - this.mSelectLevelAnimStart;
                this.mSelectAnimator.start();
            }
        }
    }

    public static class MainFragmentAdapter extends android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentAdapter<RowsSupportFragment> {
        public MainFragmentAdapter(RowsSupportFragment fragment) {
            super(fragment);
            setScalingEnabled(true);
        }

        public boolean isScrolling() {
            return ((RowsSupportFragment) getFragment()).isScrolling();
        }

        public void setExpand(boolean expand) {
            ((RowsSupportFragment) getFragment()).setExpand(expand);
        }

        public void setEntranceTransitionState(boolean state) {
            ((RowsSupportFragment) getFragment()).setEntranceTransitionState(state);
        }

        public void setAlignment(int windowAlignOffsetFromTop) {
            ((RowsSupportFragment) getFragment()).setAlignment(windowAlignOffsetFromTop);
        }

        public boolean onTransitionPrepare() {
            return ((RowsSupportFragment) getFragment()).onTransitionPrepare();
        }

        public void onTransitionStart() {
            ((RowsSupportFragment) getFragment()).onTransitionStart();
        }

        public void onTransitionEnd() {
            ((RowsSupportFragment) getFragment()).onTransitionEnd();
        }
    }

    public static class MainFragmentRowsAdapter extends android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentRowsAdapter<RowsSupportFragment> {
        public MainFragmentRowsAdapter(RowsSupportFragment fragment) {
            super(fragment);
        }

        public void setAdapter(ObjectAdapter adapter) {
            ((RowsSupportFragment) getFragment()).setAdapter(adapter);
        }

        public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
            ((RowsSupportFragment) getFragment()).setOnItemViewClickedListener(listener);
        }

        public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
            ((RowsSupportFragment) getFragment()).setOnItemViewSelectedListener(listener);
        }

        public void setSelectedPosition(int rowPosition, boolean smooth, ViewHolderTask rowHolderTask) {
            ((RowsSupportFragment) getFragment()).setSelectedPosition(rowPosition, smooth, rowHolderTask);
        }

        public void setSelectedPosition(int rowPosition, boolean smooth) {
            ((RowsSupportFragment) getFragment()).setSelectedPosition(rowPosition, smooth);
        }

        public int getSelectedPosition() {
            return ((RowsSupportFragment) getFragment()).getSelectedPosition();
        }

        public RowPresenter.ViewHolder findRowViewHolderByPosition(int position) {
            return ((RowsSupportFragment) getFragment()).findRowViewHolderByPosition(position);
        }
    }

    public /* bridge */ /* synthetic */ int getSelectedPosition() {
        return super.getSelectedPosition();
    }

    public /* bridge */ /* synthetic */ View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public /* bridge */ /* synthetic */ void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    public /* bridge */ /* synthetic */ void onTransitionStart() {
        super.onTransitionStart();
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int i) {
        super.setSelectedPosition(i);
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int i, boolean z) {
        super.setSelectedPosition(i, z);
    }

    public android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentAdapter getMainFragmentAdapter() {
        if (this.mMainFragmentAdapter == null) {
            this.mMainFragmentAdapter = new MainFragmentAdapter(this);
        }
        return this.mMainFragmentAdapter;
    }

    public android.support.v17.leanback.app.BrowseSupportFragment.MainFragmentRowsAdapter getMainFragmentRowsAdapter() {
        if (this.mMainFragmentRowsAdapter == null) {
            this.mMainFragmentRowsAdapter = new MainFragmentRowsAdapter(this);
        }
        return this.mMainFragmentRowsAdapter;
    }

    /* Access modifiers changed, original: protected */
    public VerticalGridView findGridViewFromRoot(View view) {
        return (VerticalGridView) view.findViewById(R.id.container_list);
    }

    public void setOnItemViewClickedListener(BaseOnItemViewClickedListener listener) {
        this.mOnItemViewClickedListener = listener;
        if (this.mViewsCreated) {
            throw new IllegalStateException("Item clicked listener must be set before views are created");
        }
    }

    public BaseOnItemViewClickedListener getOnItemViewClickedListener() {
        return this.mOnItemViewClickedListener;
    }

    @Deprecated
    public void enableRowScaling(boolean enable) {
    }

    public void setExpand(boolean expand) {
        this.mExpand = expand;
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            int count = listView.getChildCount();
            for (int i = 0; i < count; i++) {
                setRowViewExpanded((ViewHolder) listView.getChildViewHolder(listView.getChildAt(i)), this.mExpand);
            }
        }
    }

    public void setOnItemViewSelectedListener(BaseOnItemViewSelectedListener listener) {
        this.mOnItemViewSelectedListener = listener;
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            int count = listView.getChildCount();
            for (int i = 0; i < count; i++) {
                getRowViewHolder((ViewHolder) listView.getChildViewHolder(listView.getChildAt(i))).setOnItemViewSelectedListener(this.mOnItemViewSelectedListener);
            }
        }
    }

    public BaseOnItemViewSelectedListener getOnItemViewSelectedListener() {
        return this.mOnItemViewSelectedListener;
    }

    /* Access modifiers changed, original: 0000 */
    public void onRowSelected(RecyclerView parent, RecyclerView.ViewHolder viewHolder, int position, int subposition) {
        boolean z = true;
        if (!(this.mSelectedViewHolder == viewHolder && this.mSubPosition == subposition)) {
            this.mSubPosition = subposition;
            if (this.mSelectedViewHolder != null) {
                setRowViewSelected(this.mSelectedViewHolder, false, false);
            }
            this.mSelectedViewHolder = (ViewHolder) viewHolder;
            if (this.mSelectedViewHolder != null) {
                setRowViewSelected(this.mSelectedViewHolder, true, false);
            }
        }
        if (this.mMainFragmentAdapter != null) {
            FragmentHost fragmentHost = this.mMainFragmentAdapter.getFragmentHost();
            if (position > 0) {
                z = false;
            }
            fragmentHost.showTitleView(z);
        }
    }

    public RowPresenter.ViewHolder getRowViewHolder(int position) {
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView == null) {
            return null;
        }
        return getRowViewHolder((ViewHolder) verticalView.findViewHolderForAdapterPosition(position));
    }

    /* Access modifiers changed, original: 0000 */
    public int getLayoutResourceId() {
        return R.layout.lb_rows_fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSelectAnimatorDuration = getResources().getInteger(R.integer.lb_browse_rows_anim_duration);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getVerticalGridView().setItemAlignmentViewId(R.id.row_content);
        getVerticalGridView().setSaveChildrenPolicy(2);
        setAlignment(this.mAlignedTop);
        this.mRecycledViewPool = null;
        this.mPresenterMapper = null;
        if (this.mMainFragmentAdapter != null) {
            this.mMainFragmentAdapter.getFragmentHost().notifyViewCreated(this.mMainFragmentAdapter);
        }
    }

    public void onDestroyView() {
        this.mViewsCreated = false;
        super.onDestroyView();
    }

    /* Access modifiers changed, original: 0000 */
    public void setExternalAdapterListener(AdapterListener listener) {
        this.mExternalAdapterListener = listener;
    }

    static void setRowViewExpanded(ViewHolder vh, boolean expanded) {
        ((RowPresenter) vh.getPresenter()).setRowViewExpanded(vh.getViewHolder(), expanded);
    }

    static void setRowViewSelected(ViewHolder vh, boolean selected, boolean immediate) {
        ((RowViewHolderExtra) vh.getExtraObject()).animateSelect(selected, immediate);
        ((RowPresenter) vh.getPresenter()).setRowViewSelected(vh.getViewHolder(), selected);
    }

    /* Access modifiers changed, original: 0000 */
    public void setupSharedViewPool(ViewHolder bridgeVh) {
        RowPresenter.ViewHolder rowVh = ((RowPresenter) bridgeVh.getPresenter()).getRowViewHolder(bridgeVh.getViewHolder());
        if (rowVh instanceof ListRowPresenter.ViewHolder) {
            HorizontalGridView view = ((ListRowPresenter.ViewHolder) rowVh).getGridView();
            if (this.mRecycledViewPool == null) {
                this.mRecycledViewPool = view.getRecycledViewPool();
            } else {
                view.setRecycledViewPool(this.mRecycledViewPool);
            }
            ItemBridgeAdapter bridgeAdapter = ((ListRowPresenter.ViewHolder) rowVh).getBridgeAdapter();
            if (this.mPresenterMapper == null) {
                this.mPresenterMapper = bridgeAdapter.getPresenterMapper();
            } else {
                bridgeAdapter.setPresenterMapper(this.mPresenterMapper);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateAdapter() {
        super.updateAdapter();
        this.mSelectedViewHolder = null;
        this.mViewsCreated = false;
        ItemBridgeAdapter adapter = getBridgeAdapter();
        if (adapter != null) {
            adapter.setAdapterListener(this.mBridgeAdapterListener);
        }
    }

    public boolean onTransitionPrepare() {
        boolean prepared = super.onTransitionPrepare();
        if (prepared) {
            freezeRows(true);
        }
        return prepared;
    }

    public void onTransitionEnd() {
        super.onTransitionEnd();
        freezeRows(false);
    }

    private void freezeRows(boolean freeze) {
        this.mFreezeRows = freeze;
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null) {
            int count = verticalView.getChildCount();
            for (int i = 0; i < count; i++) {
                ViewHolder ibvh = (ViewHolder) verticalView.getChildViewHolder(verticalView.getChildAt(i));
                RowPresenter rowPresenter = (RowPresenter) ibvh.getPresenter();
                rowPresenter.freeze(rowPresenter.getRowViewHolder(ibvh.getViewHolder()), freeze);
            }
        }
    }

    public void setEntranceTransitionState(boolean afterTransition) {
        this.mAfterEntranceTransition = afterTransition;
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null) {
            int count = verticalView.getChildCount();
            for (int i = 0; i < count; i++) {
                ViewHolder ibvh = (ViewHolder) verticalView.getChildViewHolder(verticalView.getChildAt(i));
                RowPresenter rowPresenter = (RowPresenter) ibvh.getPresenter();
                rowPresenter.setEntranceTransitionState(rowPresenter.getRowViewHolder(ibvh.getViewHolder()), this.mAfterEntranceTransition);
            }
        }
    }

    public void setSelectedPosition(int rowPosition, boolean smooth, final ViewHolderTask rowHolderTask) {
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null) {
            android.support.v17.leanback.widget.ViewHolderTask task = null;
            if (rowHolderTask != null) {
                task = new android.support.v17.leanback.widget.ViewHolderTask() {
                    public void run(final RecyclerView.ViewHolder rvh) {
                        rvh.itemView.post(new Runnable() {
                            public void run() {
                                rowHolderTask.run(RowsSupportFragment.getRowViewHolder((ViewHolder) rvh));
                            }
                        });
                    }
                };
            }
            if (smooth) {
                verticalView.setSelectedPositionSmooth(rowPosition, task);
            } else {
                verticalView.setSelectedPosition(rowPosition, task);
            }
        }
    }

    static RowPresenter.ViewHolder getRowViewHolder(ViewHolder ibvh) {
        if (ibvh == null) {
            return null;
        }
        return ((RowPresenter) ibvh.getPresenter()).getRowViewHolder(ibvh.getViewHolder());
    }

    public boolean isScrolling() {
        boolean z = false;
        if (getVerticalGridView() == null) {
            return false;
        }
        if (getVerticalGridView().getScrollState() != 0) {
            z = true;
        }
        return z;
    }

    public void setAlignment(int windowAlignOffsetFromTop) {
        if (windowAlignOffsetFromTop != Integer.MIN_VALUE) {
            this.mAlignedTop = windowAlignOffsetFromTop;
            VerticalGridView gridView = getVerticalGridView();
            if (gridView != null) {
                gridView.setItemAlignmentOffset(0);
                gridView.setItemAlignmentOffsetPercent(-1.0f);
                gridView.setItemAlignmentOffsetWithPadding(true);
                gridView.setWindowAlignmentOffset(this.mAlignedTop);
                gridView.setWindowAlignmentOffsetPercent(-1.0f);
                gridView.setWindowAlignment(0);
            }
        }
    }

    public RowPresenter.ViewHolder findRowViewHolderByPosition(int position) {
        if (this.mVerticalGridView == null) {
            return null;
        }
        return getRowViewHolder((ViewHolder) this.mVerticalGridView.findViewHolderForAdapterPosition(position));
    }
}
