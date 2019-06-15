package android.support.v17.leanback.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.app.HeadersFragment.OnHeaderClickedListener;
import android.support.v17.leanback.app.HeadersFragment.OnHeaderViewSelectedListener;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v17.leanback.util.StateMachine.Event;
import android.support.v17.leanback.util.StateMachine.State;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.BrowseFrameLayout.OnChildFocusListener;
import android.support.v17.leanback.widget.BrowseFrameLayout.OnFocusSearchListener;
import android.support.v17.leanback.widget.InvisibleRowPresenter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Presenter.ViewHolderTask;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter.ViewHolder;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.ScaleFrameLayout;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class BrowseFragment extends BaseFragment {
    private static final String ARG_HEADERS_STATE;
    private static final String ARG_TITLE;
    private static final String CURRENT_SELECTED_POSITION = "currentSelectedPosition";
    static final boolean DEBUG = false;
    public static final int HEADERS_DISABLED = 3;
    public static final int HEADERS_ENABLED = 1;
    public static final int HEADERS_HIDDEN = 2;
    static final String HEADER_SHOW = "headerShow";
    static final String HEADER_STACK_INDEX = "headerStackIndex";
    private static final String IS_PAGE_ROW = "isPageRow";
    private static final String LB_HEADERS_BACKSTACK = "lbHeadersBackStack_";
    static final String TAG = "BrowseFragment";
    final Event EVT_HEADER_VIEW_CREATED = new Event("headerFragmentViewCreated");
    final Event EVT_MAIN_FRAGMENT_VIEW_CREATED = new Event("mainFragmentViewCreated");
    final Event EVT_SCREEN_DATA_READY = new Event("screenDataReady");
    final State STATE_SET_ENTRANCE_START_STATE = new State("SET_ENTRANCE_START_STATE") {
        public void run() {
            BrowseFragment.this.setEntranceTransitionStartState();
        }
    };
    private ObjectAdapter mAdapter;
    private PresenterSelector mAdapterPresenter;
    BackStackListener mBackStackChangedListener;
    private int mBrandColor = 0;
    private boolean mBrandColorSet;
    BrowseFrameLayout mBrowseFrame;
    BrowseTransitionListener mBrowseTransitionListener;
    boolean mCanShowHeaders = true;
    private int mContainerListAlignTop;
    private int mContainerListMarginStart;
    OnItemViewSelectedListener mExternalOnItemViewSelectedListener;
    private OnHeaderClickedListener mHeaderClickedListener = new OnHeaderClickedListener() {
        /* JADX WARNING: Missing block: B:13:0x0039, code skipped:
            return;
     */
        public void onHeaderClicked(android.support.v17.leanback.widget.RowHeaderPresenter.ViewHolder r3, android.support.v17.leanback.widget.Row r4) {
            /*
            r2 = this;
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mCanShowHeaders;
            if (r0 == 0) goto L_0x0039;
        L_0x0006:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mShowingHeaders;
            if (r0 == 0) goto L_0x0039;
        L_0x000c:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.isInHeadersTransition();
            if (r0 == 0) goto L_0x0015;
        L_0x0014:
            goto L_0x0039;
        L_0x0015:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragment;
            if (r0 == 0) goto L_0x0038;
        L_0x001b:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragment;
            r0 = r0.getView();
            if (r0 != 0) goto L_0x0026;
        L_0x0025:
            goto L_0x0038;
        L_0x0026:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r1 = 0;
            r0.startHeadersTransitionInternal(r1);
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragment;
            r0 = r0.getView();
            r0.requestFocus();
            return;
        L_0x0038:
            return;
        L_0x0039:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.BrowseFragment$AnonymousClass10.onHeaderClicked(android.support.v17.leanback.widget.RowHeaderPresenter$ViewHolder, android.support.v17.leanback.widget.Row):void");
        }
    };
    private PresenterSelector mHeaderPresenterSelector;
    private OnHeaderViewSelectedListener mHeaderViewSelectedListener = new OnHeaderViewSelectedListener() {
        public void onHeaderSelected(ViewHolder viewHolder, Row row) {
            BrowseFragment.this.onRowSelected(BrowseFragment.this.mHeadersFragment.getSelectedPosition());
        }
    };
    boolean mHeadersBackStackEnabled = true;
    HeadersFragment mHeadersFragment;
    private int mHeadersState = 1;
    Object mHeadersTransition;
    boolean mIsPageRow;
    Fragment mMainFragment;
    MainFragmentAdapter mMainFragmentAdapter;
    private MainFragmentAdapterRegistry mMainFragmentAdapterRegistry = new MainFragmentAdapterRegistry();
    ListRowDataAdapter mMainFragmentListRowDataAdapter;
    MainFragmentRowsAdapter mMainFragmentRowsAdapter;
    private boolean mMainFragmentScaleEnabled = true;
    private final OnChildFocusListener mOnChildFocusListener = new OnChildFocusListener() {
        public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
            boolean z = true;
            if (BrowseFragment.this.getChildFragmentManager().isDestroyed()) {
                return true;
            }
            if (BrowseFragment.this.mCanShowHeaders && BrowseFragment.this.mShowingHeaders && BrowseFragment.this.mHeadersFragment != null && BrowseFragment.this.mHeadersFragment.getView() != null && BrowseFragment.this.mHeadersFragment.getView().requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
            if (BrowseFragment.this.mMainFragment != null && BrowseFragment.this.mMainFragment.getView() != null && BrowseFragment.this.mMainFragment.getView().requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
            if (BrowseFragment.this.getTitleView() == null || !BrowseFragment.this.getTitleView().requestFocus(direction, previouslyFocusedRect)) {
                z = false;
            }
            return z;
        }

        public void onRequestChildFocus(View child, View focused) {
            if (!BrowseFragment.this.getChildFragmentManager().isDestroyed() && BrowseFragment.this.mCanShowHeaders && !BrowseFragment.this.isInHeadersTransition()) {
                int childId = child.getId();
                if (childId == R.id.browse_container_dock && BrowseFragment.this.mShowingHeaders) {
                    BrowseFragment.this.startHeadersTransitionInternal(false);
                } else if (childId == R.id.browse_headers_dock && !BrowseFragment.this.mShowingHeaders) {
                    BrowseFragment.this.startHeadersTransitionInternal(true);
                }
            }
        }
    };
    private final OnFocusSearchListener mOnFocusSearchListener = new OnFocusSearchListener() {
        public View onFocusSearch(View focused, int direction) {
            if (BrowseFragment.this.mCanShowHeaders && BrowseFragment.this.isInHeadersTransition()) {
                return focused;
            }
            if (BrowseFragment.this.getTitleView() != null && focused != BrowseFragment.this.getTitleView() && direction == 33) {
                return BrowseFragment.this.getTitleView();
            }
            if (BrowseFragment.this.getTitleView() != null && BrowseFragment.this.getTitleView().hasFocus() && direction == 130) {
                View verticalGridView = (BrowseFragment.this.mCanShowHeaders && BrowseFragment.this.mShowingHeaders) ? BrowseFragment.this.mHeadersFragment.getVerticalGridView() : BrowseFragment.this.mMainFragment.getView();
                return verticalGridView;
            }
            boolean z = true;
            if (ViewCompat.getLayoutDirection(focused) != 1) {
                z = false;
            }
            boolean isRtl = z;
            int towardEnd = 17;
            int towardStart = isRtl ? 66 : 17;
            if (!isRtl) {
                towardEnd = 66;
            }
            if (BrowseFragment.this.mCanShowHeaders && direction == towardStart) {
                if (BrowseFragment.this.isVerticalScrolling() || BrowseFragment.this.mShowingHeaders || !BrowseFragment.this.isHeadersDataReady()) {
                    return focused;
                }
                return BrowseFragment.this.mHeadersFragment.getVerticalGridView();
            } else if (direction == towardEnd) {
                if (BrowseFragment.this.isVerticalScrolling() || BrowseFragment.this.mMainFragment == null || BrowseFragment.this.mMainFragment.getView() == null) {
                    return focused;
                }
                return BrowseFragment.this.mMainFragment.getView();
            } else if (direction == 130 && BrowseFragment.this.mShowingHeaders) {
                return focused;
            } else {
                return null;
            }
        }
    };
    private OnItemViewClickedListener mOnItemViewClickedListener;
    Object mPageRow;
    private float mScaleFactor;
    private ScaleFrameLayout mScaleFrameLayout;
    private Object mSceneAfterEntranceTransition;
    Object mSceneWithHeaders;
    Object mSceneWithoutHeaders;
    private int mSelectedPosition = -1;
    private final SetSelectionRunnable mSetSelectionRunnable = new SetSelectionRunnable();
    boolean mShowingHeaders = true;
    String mWithHeadersBackStackName;

    final class BackStackListener implements OnBackStackChangedListener {
        int mIndexOfHeadersBackStack = -1;
        int mLastEntryCount;

        BackStackListener() {
            this.mLastEntryCount = BrowseFragment.this.getFragmentManager().getBackStackEntryCount();
        }

        /* Access modifiers changed, original: 0000 */
        public void load(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                this.mIndexOfHeadersBackStack = savedInstanceState.getInt(BrowseFragment.HEADER_STACK_INDEX, -1);
                BrowseFragment.this.mShowingHeaders = this.mIndexOfHeadersBackStack == -1;
            } else if (!BrowseFragment.this.mShowingHeaders) {
                BrowseFragment.this.getFragmentManager().beginTransaction().addToBackStack(BrowseFragment.this.mWithHeadersBackStackName).commit();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void save(Bundle outState) {
            outState.putInt(BrowseFragment.HEADER_STACK_INDEX, this.mIndexOfHeadersBackStack);
        }

        public void onBackStackChanged() {
            if (BrowseFragment.this.getFragmentManager() == null) {
                Log.w(BrowseFragment.TAG, "getFragmentManager() is null, stack:", new Exception());
                return;
            }
            int count = BrowseFragment.this.getFragmentManager().getBackStackEntryCount();
            if (count > this.mLastEntryCount) {
                if (BrowseFragment.this.mWithHeadersBackStackName.equals(BrowseFragment.this.getFragmentManager().getBackStackEntryAt(count - 1).getName())) {
                    this.mIndexOfHeadersBackStack = count - 1;
                }
            } else if (count < this.mLastEntryCount && this.mIndexOfHeadersBackStack >= count) {
                if (BrowseFragment.this.isHeadersDataReady()) {
                    this.mIndexOfHeadersBackStack = -1;
                    if (!BrowseFragment.this.mShowingHeaders) {
                        BrowseFragment.this.startHeadersTransitionInternal(true);
                    }
                } else {
                    BrowseFragment.this.getFragmentManager().beginTransaction().addToBackStack(BrowseFragment.this.mWithHeadersBackStackName).commit();
                    return;
                }
            }
            this.mLastEntryCount = count;
        }
    }

    @Deprecated
    public static class BrowseTransitionListener {
        public void onHeadersTransitionStart(boolean withHeaders) {
        }

        public void onHeadersTransitionStop(boolean withHeaders) {
        }
    }

    private class ExpandPreLayout implements OnPreDrawListener {
        static final int STATE_FIRST_DRAW = 1;
        static final int STATE_INIT = 0;
        static final int STATE_SECOND_DRAW = 2;
        private final Runnable mCallback;
        private int mState;
        private final View mView;
        private MainFragmentAdapter mainFragmentAdapter;

        ExpandPreLayout(Runnable callback, MainFragmentAdapter adapter, View view) {
            this.mView = view;
            this.mCallback = callback;
            this.mainFragmentAdapter = adapter;
        }

        /* Access modifiers changed, original: 0000 */
        public void execute() {
            this.mView.getViewTreeObserver().addOnPreDrawListener(this);
            this.mainFragmentAdapter.setExpand(false);
            this.mView.invalidate();
            this.mState = 0;
        }

        public boolean onPreDraw() {
            if (BrowseFragment.this.getView() == null || FragmentUtil.getContext(BrowseFragment.this) == null) {
                this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
            if (this.mState == 0) {
                this.mainFragmentAdapter.setExpand(true);
                this.mView.invalidate();
                this.mState = 1;
            } else if (this.mState == 1) {
                this.mCallback.run();
                this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
                this.mState = 2;
            }
            return false;
        }
    }

    @Deprecated
    public static abstract class FragmentFactory<T extends Fragment> {
        public abstract T createFragment(Object obj);
    }

    @Deprecated
    public interface FragmentHost {
        void notifyDataReady(MainFragmentAdapter mainFragmentAdapter);

        void notifyViewCreated(MainFragmentAdapter mainFragmentAdapter);

        void showTitleView(boolean z);
    }

    @Deprecated
    public static class MainFragmentAdapter<T extends Fragment> {
        private final T mFragment;
        FragmentHostImpl mFragmentHost;
        private boolean mScalingEnabled;

        public MainFragmentAdapter(T fragment) {
            this.mFragment = fragment;
        }

        public final T getFragment() {
            return this.mFragment;
        }

        public boolean isScrolling() {
            return false;
        }

        public void setExpand(boolean expand) {
        }

        public void setEntranceTransitionState(boolean state) {
        }

        public void setAlignment(int windowAlignOffsetFromTop) {
        }

        public boolean onTransitionPrepare() {
            return false;
        }

        public void onTransitionStart() {
        }

        public void onTransitionEnd() {
        }

        public boolean isScalingEnabled() {
            return this.mScalingEnabled;
        }

        public void setScalingEnabled(boolean scalingEnabled) {
            this.mScalingEnabled = scalingEnabled;
        }

        public final FragmentHost getFragmentHost() {
            return this.mFragmentHost;
        }

        /* Access modifiers changed, original: 0000 */
        public void setFragmentHost(FragmentHostImpl fragmentHost) {
            this.mFragmentHost = fragmentHost;
        }
    }

    @Deprecated
    public interface MainFragmentAdapterProvider {
        MainFragmentAdapter getMainFragmentAdapter();
    }

    @Deprecated
    public static final class MainFragmentAdapterRegistry {
        private static final FragmentFactory sDefaultFragmentFactory = new ListRowFragmentFactory();
        private final Map<Class, FragmentFactory> mItemToFragmentFactoryMapping = new HashMap();

        public MainFragmentAdapterRegistry() {
            registerFragment(ListRow.class, sDefaultFragmentFactory);
        }

        public void registerFragment(Class rowClass, FragmentFactory factory) {
            this.mItemToFragmentFactoryMapping.put(rowClass, factory);
        }

        public Fragment createFragment(Object item) {
            FragmentFactory fragmentFactory;
            if (item == null) {
                fragmentFactory = sDefaultFragmentFactory;
            } else {
                fragmentFactory = (FragmentFactory) this.mItemToFragmentFactoryMapping.get(item.getClass());
            }
            if (fragmentFactory == null && !(item instanceof PageRow)) {
                fragmentFactory = sDefaultFragmentFactory;
            }
            return fragmentFactory.createFragment(item);
        }
    }

    @Deprecated
    public static class MainFragmentRowsAdapter<T extends Fragment> {
        private final T mFragment;

        public MainFragmentRowsAdapter(T fragment) {
            if (fragment != null) {
                this.mFragment = fragment;
                return;
            }
            throw new IllegalArgumentException("Fragment can't be null");
        }

        public final T getFragment() {
            return this.mFragment;
        }

        public void setAdapter(ObjectAdapter adapter) {
        }

        public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        }

        public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        }

        public void setSelectedPosition(int rowPosition, boolean smooth, ViewHolderTask rowHolderTask) {
        }

        public void setSelectedPosition(int rowPosition, boolean smooth) {
        }

        public int getSelectedPosition() {
            return 0;
        }

        public RowPresenter.ViewHolder findRowViewHolderByPosition(int position) {
            return null;
        }
    }

    @Deprecated
    public interface MainFragmentRowsAdapterProvider {
        MainFragmentRowsAdapter getMainFragmentRowsAdapter();
    }

    private class SetSelectionRunnable implements Runnable {
        static final int TYPE_INTERNAL_SYNC = 0;
        static final int TYPE_INVALID = -1;
        static final int TYPE_USER_REQUEST = 1;
        private int mPosition;
        private boolean mSmooth;
        private int mType;

        SetSelectionRunnable() {
            reset();
        }

        /* Access modifiers changed, original: 0000 */
        public void post(int position, int type, boolean smooth) {
            if (type >= this.mType) {
                this.mPosition = position;
                this.mType = type;
                this.mSmooth = smooth;
                BrowseFragment.this.mBrowseFrame.removeCallbacks(this);
                BrowseFragment.this.mBrowseFrame.post(this);
            }
        }

        public void run() {
            BrowseFragment.this.setSelection(this.mPosition, this.mSmooth);
            reset();
        }

        private void reset() {
            this.mPosition = -1;
            this.mType = -1;
            this.mSmooth = false;
        }
    }

    private final class FragmentHostImpl implements FragmentHost {
        boolean mShowTitleView = true;

        FragmentHostImpl() {
        }

        public void notifyViewCreated(MainFragmentAdapter fragmentAdapter) {
            BrowseFragment.this.mStateMachine.fireEvent(BrowseFragment.this.EVT_MAIN_FRAGMENT_VIEW_CREATED);
            if (!BrowseFragment.this.mIsPageRow) {
                BrowseFragment.this.mStateMachine.fireEvent(BrowseFragment.this.EVT_SCREEN_DATA_READY);
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0024, code skipped:
            return;
     */
        public void notifyDataReady(android.support.v17.leanback.app.BrowseFragment.MainFragmentAdapter r3) {
            /*
            r2 = this;
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragmentAdapter;
            if (r0 == 0) goto L_0x0024;
        L_0x0006:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragmentAdapter;
            r0 = r0.getFragmentHost();
            if (r0 == r2) goto L_0x0011;
        L_0x0010:
            goto L_0x0024;
        L_0x0011:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mIsPageRow;
            if (r0 != 0) goto L_0x0018;
        L_0x0017:
            return;
        L_0x0018:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mStateMachine;
            r1 = android.support.v17.leanback.app.BrowseFragment.this;
            r1 = r1.EVT_SCREEN_DATA_READY;
            r0.fireEvent(r1);
            return;
        L_0x0024:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.BrowseFragment$FragmentHostImpl.notifyDataReady(android.support.v17.leanback.app.BrowseFragment$MainFragmentAdapter):void");
        }

        /* JADX WARNING: Missing block: B:9:0x0020, code skipped:
            return;
     */
        public void showTitleView(boolean r2) {
            /*
            r1 = this;
            r1.mShowTitleView = r2;
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragmentAdapter;
            if (r0 == 0) goto L_0x0020;
        L_0x0008:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mMainFragmentAdapter;
            r0 = r0.getFragmentHost();
            if (r0 == r1) goto L_0x0013;
        L_0x0012:
            goto L_0x0020;
        L_0x0013:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0 = r0.mIsPageRow;
            if (r0 != 0) goto L_0x001a;
        L_0x0019:
            return;
        L_0x001a:
            r0 = android.support.v17.leanback.app.BrowseFragment.this;
            r0.updateTitleViewVisibility();
            return;
        L_0x0020:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.BrowseFragment$FragmentHostImpl.showTitleView(boolean):void");
        }
    }

    @Deprecated
    public static class ListRowFragmentFactory extends FragmentFactory<RowsFragment> {
        public RowsFragment createFragment(Object row) {
            return new RowsFragment();
        }
    }

    class MainFragmentItemViewSelectedListener implements OnItemViewSelectedListener {
        MainFragmentRowsAdapter mMainFragmentRowsAdapter;

        public MainFragmentItemViewSelectedListener(MainFragmentRowsAdapter fragmentRowsAdapter) {
            this.mMainFragmentRowsAdapter = fragmentRowsAdapter;
        }

        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            BrowseFragment.this.onRowSelected(this.mMainFragmentRowsAdapter.getSelectedPosition());
            if (BrowseFragment.this.mExternalOnItemViewSelectedListener != null) {
                BrowseFragment.this.mExternalOnItemViewSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder, row);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineStates() {
        super.createStateMachineStates();
        this.mStateMachine.addState(this.STATE_SET_ENTRANCE_START_STATE);
    }

    /* Access modifiers changed, original: 0000 */
    public void createStateMachineTransitions() {
        super.createStateMachineTransitions();
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_SET_ENTRANCE_START_STATE, this.EVT_HEADER_VIEW_CREATED);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_ENTRANCE_ON_PREPARED_ON_CREATEVIEW, this.EVT_MAIN_FRAGMENT_VIEW_CREATED);
        this.mStateMachine.addTransition(this.STATE_ENTRANCE_ON_PREPARED, this.STATE_ENTRANCE_PERFORM, this.EVT_SCREEN_DATA_READY);
    }

    private boolean createMainFragment(ObjectAdapter adapter, int position) {
        Object item = null;
        boolean swap = true;
        if (this.mCanShowHeaders) {
            if (adapter == null || adapter.size() == 0) {
                return false;
            }
            if (position < 0) {
                position = 0;
            } else if (position >= adapter.size()) {
                throw new IllegalArgumentException(String.format("Invalid position %d requested", new Object[]{Integer.valueOf(position)}));
            }
            item = adapter.get(position);
        }
        boolean oldIsPageRow = this.mIsPageRow;
        Object oldPageRow = this.mPageRow;
        boolean z = this.mCanShowHeaders && (item instanceof PageRow);
        this.mIsPageRow = z;
        this.mPageRow = this.mIsPageRow ? item : null;
        if (this.mMainFragment == null) {
            swap = true;
        } else if (!oldIsPageRow) {
            swap = this.mIsPageRow;
        } else if (!this.mIsPageRow) {
            swap = true;
        } else if (oldPageRow == null) {
            swap = false;
        } else if (oldPageRow == this.mPageRow) {
            swap = false;
        }
        if (swap) {
            this.mMainFragment = this.mMainFragmentAdapterRegistry.createFragment(item);
            if (this.mMainFragment instanceof MainFragmentAdapterProvider) {
                setMainFragmentAdapter();
            } else {
                throw new IllegalArgumentException("Fragment must implement MainFragmentAdapterProvider");
            }
        }
        return swap;
    }

    /* Access modifiers changed, original: 0000 */
    public void setMainFragmentAdapter() {
        this.mMainFragmentAdapter = ((MainFragmentAdapterProvider) this.mMainFragment).getMainFragmentAdapter();
        this.mMainFragmentAdapter.setFragmentHost(new FragmentHostImpl());
        if (this.mIsPageRow) {
            setMainFragmentRowsAdapter(null);
            return;
        }
        if (this.mMainFragment instanceof MainFragmentRowsAdapterProvider) {
            setMainFragmentRowsAdapter(((MainFragmentRowsAdapterProvider) this.mMainFragment).getMainFragmentRowsAdapter());
        } else {
            setMainFragmentRowsAdapter(null);
        }
        this.mIsPageRow = this.mMainFragmentRowsAdapter == null;
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BrowseFragment.class.getCanonicalName());
        stringBuilder.append(".title");
        ARG_TITLE = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(BrowseFragment.class.getCanonicalName());
        stringBuilder.append(".headersState");
        ARG_HEADERS_STATE = stringBuilder.toString();
    }

    public static Bundle createArgs(Bundle args, String title, int headersState) {
        if (args == null) {
            args = new Bundle();
        }
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_HEADERS_STATE, headersState);
        return args;
    }

    public void setBrandColor(@ColorInt int color) {
        this.mBrandColor = color;
        this.mBrandColorSet = true;
        if (this.mHeadersFragment != null) {
            this.mHeadersFragment.setBackgroundColor(this.mBrandColor);
        }
    }

    @ColorInt
    public int getBrandColor() {
        return this.mBrandColor;
    }

    private void updateWrapperPresenter() {
        if (this.mAdapter == null) {
            this.mAdapterPresenter = null;
            return;
        }
        final PresenterSelector adapterPresenter = this.mAdapter.getPresenterSelector();
        if (adapterPresenter == null) {
            throw new IllegalArgumentException("Adapter.getPresenterSelector() is null");
        } else if (adapterPresenter != this.mAdapterPresenter) {
            this.mAdapterPresenter = adapterPresenter;
            Presenter[] presenters = adapterPresenter.getPresenters();
            final Presenter invisibleRowPresenter = new InvisibleRowPresenter();
            final Presenter[] allPresenters = new Presenter[(presenters.length + 1)];
            System.arraycopy(allPresenters, 0, presenters, 0, presenters.length);
            allPresenters[allPresenters.length - 1] = invisibleRowPresenter;
            this.mAdapter.setPresenterSelector(new PresenterSelector() {
                public Presenter getPresenter(Object item) {
                    if (((Row) item).isRenderedAsRowView()) {
                        return adapterPresenter.getPresenter(item);
                    }
                    return invisibleRowPresenter;
                }

                public Presenter[] getPresenters() {
                    return allPresenters;
                }
            });
        }
    }

    public void setAdapter(ObjectAdapter adapter) {
        this.mAdapter = adapter;
        updateWrapperPresenter();
        if (getView() != null) {
            updateMainFragmentRowsAdapter();
            this.mHeadersFragment.setAdapter(this.mAdapter);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setMainFragmentRowsAdapter(MainFragmentRowsAdapter mainFragmentRowsAdapter) {
        if (mainFragmentRowsAdapter != this.mMainFragmentRowsAdapter) {
            if (this.mMainFragmentRowsAdapter != null) {
                this.mMainFragmentRowsAdapter.setAdapter(null);
            }
            this.mMainFragmentRowsAdapter = mainFragmentRowsAdapter;
            if (this.mMainFragmentRowsAdapter != null) {
                this.mMainFragmentRowsAdapter.setOnItemViewSelectedListener(new MainFragmentItemViewSelectedListener(this.mMainFragmentRowsAdapter));
                this.mMainFragmentRowsAdapter.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
            }
            updateMainFragmentRowsAdapter();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateMainFragmentRowsAdapter() {
        ListRowDataAdapter listRowDataAdapter = null;
        if (this.mMainFragmentListRowDataAdapter != null) {
            this.mMainFragmentListRowDataAdapter.detach();
            this.mMainFragmentListRowDataAdapter = null;
        }
        if (this.mMainFragmentRowsAdapter != null) {
            if (this.mAdapter != null) {
                listRowDataAdapter = new ListRowDataAdapter(this.mAdapter);
            }
            this.mMainFragmentListRowDataAdapter = listRowDataAdapter;
            this.mMainFragmentRowsAdapter.setAdapter(this.mMainFragmentListRowDataAdapter);
        }
    }

    public final MainFragmentAdapterRegistry getMainFragmentRegistry() {
        return this.mMainFragmentAdapterRegistry;
    }

    public ObjectAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setOnItemViewSelectedListener(OnItemViewSelectedListener listener) {
        this.mExternalOnItemViewSelectedListener = listener;
    }

    public OnItemViewSelectedListener getOnItemViewSelectedListener() {
        return this.mExternalOnItemViewSelectedListener;
    }

    public RowsFragment getRowsFragment() {
        if (this.mMainFragment instanceof RowsFragment) {
            return (RowsFragment) this.mMainFragment;
        }
        return null;
    }

    public Fragment getMainFragment() {
        return this.mMainFragment;
    }

    public HeadersFragment getHeadersFragment() {
        return this.mHeadersFragment;
    }

    public void setOnItemViewClickedListener(OnItemViewClickedListener listener) {
        this.mOnItemViewClickedListener = listener;
        if (this.mMainFragmentRowsAdapter != null) {
            this.mMainFragmentRowsAdapter.setOnItemViewClickedListener(listener);
        }
    }

    public OnItemViewClickedListener getOnItemViewClickedListener() {
        return this.mOnItemViewClickedListener;
    }

    public void startHeadersTransition(boolean withHeaders) {
        if (!this.mCanShowHeaders) {
            throw new IllegalStateException("Cannot start headers transition");
        } else if (!isInHeadersTransition() && this.mShowingHeaders != withHeaders) {
            startHeadersTransitionInternal(withHeaders);
        }
    }

    public boolean isInHeadersTransition() {
        return this.mHeadersTransition != null;
    }

    public boolean isShowingHeaders() {
        return this.mShowingHeaders;
    }

    public void setBrowseTransitionListener(BrowseTransitionListener listener) {
        this.mBrowseTransitionListener = listener;
    }

    @Deprecated
    public void enableRowScaling(boolean enable) {
        enableMainFragmentScaling(enable);
    }

    public void enableMainFragmentScaling(boolean enable) {
        this.mMainFragmentScaleEnabled = enable;
    }

    /* Access modifiers changed, original: 0000 */
    public void startHeadersTransitionInternal(final boolean withHeaders) {
        if (!getFragmentManager().isDestroyed() && isHeadersDataReady()) {
            this.mShowingHeaders = withHeaders;
            this.mMainFragmentAdapter.onTransitionPrepare();
            this.mMainFragmentAdapter.onTransitionStart();
            onExpandTransitionStart(withHeaders ^ 1, new Runnable() {
                public void run() {
                    BrowseFragment.this.mHeadersFragment.onTransitionPrepare();
                    BrowseFragment.this.mHeadersFragment.onTransitionStart();
                    BrowseFragment.this.createHeadersTransition();
                    if (BrowseFragment.this.mBrowseTransitionListener != null) {
                        BrowseFragment.this.mBrowseTransitionListener.onHeadersTransitionStart(withHeaders);
                    }
                    TransitionHelper.runTransition(withHeaders ? BrowseFragment.this.mSceneWithHeaders : BrowseFragment.this.mSceneWithoutHeaders, BrowseFragment.this.mHeadersTransition);
                    if (!BrowseFragment.this.mHeadersBackStackEnabled) {
                        return;
                    }
                    if (withHeaders) {
                        int index = BrowseFragment.this.mBackStackChangedListener.mIndexOfHeadersBackStack;
                        if (index >= 0) {
                            BrowseFragment.this.getFragmentManager().popBackStackImmediate(BrowseFragment.this.getFragmentManager().getBackStackEntryAt(index).getId(), 1);
                            return;
                        }
                        return;
                    }
                    BrowseFragment.this.getFragmentManager().beginTransaction().addToBackStack(BrowseFragment.this.mWithHeadersBackStackName).commit();
                }
            });
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isVerticalScrolling() {
        return this.mHeadersFragment.isScrolling() || this.mMainFragmentAdapter.isScrolling();
    }

    /* Access modifiers changed, original: final */
    public final boolean isHeadersDataReady() {
        return (this.mAdapter == null || this.mAdapter.size() == 0) ? false : true;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_SELECTED_POSITION, this.mSelectedPosition);
        outState.putBoolean(IS_PAGE_ROW, this.mIsPageRow);
        if (this.mBackStackChangedListener != null) {
            this.mBackStackChangedListener.save(outState);
        } else {
            outState.putBoolean(HEADER_SHOW, this.mShowingHeaders);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = FragmentUtil.getContext(this);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.LeanbackTheme);
        this.mContainerListMarginStart = (int) ta.getDimension(R.styleable.LeanbackTheme_browseRowsMarginStart, (float) context.getResources().getDimensionPixelSize(R.dimen.lb_browse_rows_margin_start));
        this.mContainerListAlignTop = (int) ta.getDimension(R.styleable.LeanbackTheme_browseRowsMarginTop, (float) context.getResources().getDimensionPixelSize(R.dimen.lb_browse_rows_margin_top));
        ta.recycle();
        readArguments(getArguments());
        if (this.mCanShowHeaders) {
            if (this.mHeadersBackStackEnabled) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(LB_HEADERS_BACKSTACK);
                stringBuilder.append(this);
                this.mWithHeadersBackStackName = stringBuilder.toString();
                this.mBackStackChangedListener = new BackStackListener();
                getFragmentManager().addOnBackStackChangedListener(this.mBackStackChangedListener);
                this.mBackStackChangedListener.load(savedInstanceState);
            } else if (savedInstanceState != null) {
                this.mShowingHeaders = savedInstanceState.getBoolean(HEADER_SHOW);
            }
        }
        this.mScaleFactor = getResources().getFraction(R.fraction.lb_browse_rows_scale, 1, 1);
    }

    public void onDestroyView() {
        setMainFragmentRowsAdapter(null);
        this.mPageRow = null;
        this.mMainFragmentAdapter = null;
        this.mMainFragment = null;
        this.mHeadersFragment = null;
        super.onDestroyView();
    }

    public void onDestroy() {
        if (this.mBackStackChangedListener != null) {
            getFragmentManager().removeOnBackStackChangedListener(this.mBackStackChangedListener);
        }
        super.onDestroy();
    }

    public HeadersFragment onCreateHeadersFragment() {
        return new HeadersFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getChildFragmentManager().findFragmentById(R.id.scale_frame) == null) {
            this.mHeadersFragment = onCreateHeadersFragment();
            createMainFragment(this.mAdapter, this.mSelectedPosition);
            FragmentTransaction ft = getChildFragmentManager().beginTransaction().replace(R.id.browse_headers_dock, this.mHeadersFragment);
            if (this.mMainFragment != null) {
                ft.replace(R.id.scale_frame, this.mMainFragment);
            } else {
                this.mMainFragmentAdapter = new MainFragmentAdapter(null);
                this.mMainFragmentAdapter.setFragmentHost(new FragmentHostImpl());
            }
            ft.commit();
        } else {
            this.mHeadersFragment = (HeadersFragment) getChildFragmentManager().findFragmentById(R.id.browse_headers_dock);
            this.mMainFragment = getChildFragmentManager().findFragmentById(R.id.scale_frame);
            boolean z = savedInstanceState != null && savedInstanceState.getBoolean(IS_PAGE_ROW, false);
            this.mIsPageRow = z;
            this.mSelectedPosition = savedInstanceState != null ? savedInstanceState.getInt(CURRENT_SELECTED_POSITION, 0) : 0;
            setMainFragmentAdapter();
        }
        this.mHeadersFragment.setHeadersGone(1 ^ this.mCanShowHeaders);
        if (this.mHeaderPresenterSelector != null) {
            this.mHeadersFragment.setPresenterSelector(this.mHeaderPresenterSelector);
        }
        this.mHeadersFragment.setAdapter(this.mAdapter);
        this.mHeadersFragment.setOnHeaderViewSelectedListener(this.mHeaderViewSelectedListener);
        this.mHeadersFragment.setOnHeaderClickedListener(this.mHeaderClickedListener);
        View root = inflater.inflate(R.layout.lb_browse_fragment, container, false);
        getProgressBarManager().setRootView((ViewGroup) root);
        this.mBrowseFrame = (BrowseFrameLayout) root.findViewById(R.id.browse_frame);
        this.mBrowseFrame.setOnChildFocusListener(this.mOnChildFocusListener);
        this.mBrowseFrame.setOnFocusSearchListener(this.mOnFocusSearchListener);
        installTitleView(inflater, this.mBrowseFrame, savedInstanceState);
        this.mScaleFrameLayout = (ScaleFrameLayout) root.findViewById(R.id.scale_frame);
        this.mScaleFrameLayout.setPivotX(0.0f);
        this.mScaleFrameLayout.setPivotY((float) this.mContainerListAlignTop);
        if (this.mBrandColorSet) {
            this.mHeadersFragment.setBackgroundColor(this.mBrandColor);
        }
        this.mSceneWithHeaders = TransitionHelper.createScene(this.mBrowseFrame, new Runnable() {
            public void run() {
                BrowseFragment.this.showHeaders(true);
            }
        });
        this.mSceneWithoutHeaders = TransitionHelper.createScene(this.mBrowseFrame, new Runnable() {
            public void run() {
                BrowseFragment.this.showHeaders(false);
            }
        });
        this.mSceneAfterEntranceTransition = TransitionHelper.createScene(this.mBrowseFrame, new Runnable() {
            public void run() {
                BrowseFragment.this.setEntranceTransitionEndState();
            }
        });
        return root;
    }

    /* Access modifiers changed, original: 0000 */
    public void createHeadersTransition() {
        this.mHeadersTransition = TransitionHelper.loadTransition(FragmentUtil.getContext(this), this.mShowingHeaders ? R.transition.lb_browse_headers_in : R.transition.lb_browse_headers_out);
        TransitionHelper.addTransitionListener(this.mHeadersTransition, new TransitionListener() {
            public void onTransitionStart(Object transition) {
            }

            public void onTransitionEnd(Object transition) {
                BrowseFragment.this.mHeadersTransition = null;
                if (BrowseFragment.this.mMainFragmentAdapter != null) {
                    BrowseFragment.this.mMainFragmentAdapter.onTransitionEnd();
                    if (!(BrowseFragment.this.mShowingHeaders || BrowseFragment.this.mMainFragment == null)) {
                        View mainFragmentView = BrowseFragment.this.mMainFragment.getView();
                        if (!(mainFragmentView == null || mainFragmentView.hasFocus())) {
                            mainFragmentView.requestFocus();
                        }
                    }
                }
                if (BrowseFragment.this.mHeadersFragment != null) {
                    BrowseFragment.this.mHeadersFragment.onTransitionEnd();
                    if (BrowseFragment.this.mShowingHeaders) {
                        VerticalGridView headerGridView = BrowseFragment.this.mHeadersFragment.getVerticalGridView();
                        if (!(headerGridView == null || headerGridView.hasFocus())) {
                            headerGridView.requestFocus();
                        }
                    }
                }
                BrowseFragment.this.updateTitleViewVisibility();
                if (BrowseFragment.this.mBrowseTransitionListener != null) {
                    BrowseFragment.this.mBrowseTransitionListener.onHeadersTransitionStop(BrowseFragment.this.mShowingHeaders);
                }
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void updateTitleViewVisibility() {
        boolean showBranding;
        if (this.mShowingHeaders) {
            if (!this.mIsPageRow || this.mMainFragmentAdapter == null) {
                showBranding = isFirstRowWithContent(this.mSelectedPosition);
            } else {
                showBranding = this.mMainFragmentAdapter.mFragmentHost.mShowTitleView;
            }
            boolean showSearch = isFirstRowWithContentOrPageRow(this.mSelectedPosition);
            int flags = 0;
            if (showBranding) {
                flags = 0 | 2;
            }
            if (showSearch) {
                flags |= 4;
            }
            if (flags != 0) {
                showTitle(flags);
                return;
            } else {
                showTitle(false);
                return;
            }
        }
        if (!this.mIsPageRow || this.mMainFragmentAdapter == null) {
            showBranding = isFirstRowWithContent(this.mSelectedPosition);
        } else {
            showBranding = this.mMainFragmentAdapter.mFragmentHost.mShowTitleView;
        }
        if (showBranding) {
            showTitle(6);
        } else {
            showTitle(false);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isFirstRowWithContentOrPageRow(int rowPosition) {
        if (this.mAdapter == null || this.mAdapter.size() == 0) {
            return true;
        }
        boolean z = false;
        for (int i = 0; i < this.mAdapter.size(); i++) {
            Row row = (Row) this.mAdapter.get(i);
            if (row.isRenderedAsRowView() || (row instanceof PageRow)) {
                if (rowPosition == i) {
                    z = true;
                }
                return z;
            }
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isFirstRowWithContent(int rowPosition) {
        if (this.mAdapter == null || this.mAdapter.size() == 0) {
            return true;
        }
        boolean z = false;
        for (int i = 0; i < this.mAdapter.size(); i++) {
            if (((Row) this.mAdapter.get(i)).isRenderedAsRowView()) {
                if (rowPosition == i) {
                    z = true;
                }
                return z;
            }
        }
        return true;
    }

    public void setHeaderPresenterSelector(PresenterSelector headerPresenterSelector) {
        this.mHeaderPresenterSelector = headerPresenterSelector;
        if (this.mHeadersFragment != null) {
            this.mHeadersFragment.setPresenterSelector(this.mHeaderPresenterSelector);
        }
    }

    private void setHeadersOnScreen(boolean onScreen) {
        View containerList = this.mHeadersFragment.getView();
        MarginLayoutParams lp = (MarginLayoutParams) containerList.getLayoutParams();
        lp.setMarginStart(onScreen ? 0 : -this.mContainerListMarginStart);
        containerList.setLayoutParams(lp);
    }

    /* Access modifiers changed, original: 0000 */
    public void showHeaders(boolean show) {
        this.mHeadersFragment.setHeadersEnabled(show);
        setHeadersOnScreen(show);
        expandMainFragment(show ^ 1);
    }

    private void expandMainFragment(boolean expand) {
        MarginLayoutParams params = (MarginLayoutParams) this.mScaleFrameLayout.getLayoutParams();
        params.setMarginStart(!expand ? this.mContainerListMarginStart : 0);
        this.mScaleFrameLayout.setLayoutParams(params);
        this.mMainFragmentAdapter.setExpand(expand);
        setMainFragmentAlignment();
        float scaleFactor = (!expand && this.mMainFragmentScaleEnabled && this.mMainFragmentAdapter.isScalingEnabled()) ? this.mScaleFactor : 1.0f;
        this.mScaleFrameLayout.setLayoutScaleY(scaleFactor);
        this.mScaleFrameLayout.setChildScale(scaleFactor);
    }

    /* Access modifiers changed, original: 0000 */
    public void onRowSelected(int position) {
        this.mSetSelectionRunnable.post(position, 0, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void setSelection(int position, boolean smooth) {
        if (position != -1) {
            this.mSelectedPosition = position;
            if (this.mHeadersFragment != null && this.mMainFragmentAdapter != null) {
                this.mHeadersFragment.setSelectedPosition(position, smooth);
                replaceMainFragment(position);
                if (this.mMainFragmentRowsAdapter != null) {
                    this.mMainFragmentRowsAdapter.setSelectedPosition(position, smooth);
                }
                updateTitleViewVisibility();
            }
        }
    }

    private void replaceMainFragment(int position) {
        if (createMainFragment(this.mAdapter, position)) {
            swapToMainFragment();
            boolean z = (this.mCanShowHeaders && this.mShowingHeaders) ? false : true;
            expandMainFragment(z);
        }
    }

    private void swapToMainFragment() {
        final VerticalGridView gridView = this.mHeadersFragment.getVerticalGridView();
        if (!isShowingHeaders() || gridView == null || gridView.getScrollState() == 0) {
            getChildFragmentManager().beginTransaction().replace(R.id.scale_frame, this.mMainFragment).commit();
            return;
        }
        getChildFragmentManager().beginTransaction().replace(R.id.scale_frame, new Fragment()).commit();
        gridView.addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == 0) {
                    gridView.removeOnScrollListener(this);
                    FragmentManager fm = BrowseFragment.this.getChildFragmentManager();
                    if (fm.findFragmentById(R.id.scale_frame) != BrowseFragment.this.mMainFragment) {
                        fm.beginTransaction().replace(R.id.scale_frame, BrowseFragment.this.mMainFragment).commit();
                    }
                }
            }
        });
    }

    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    public int getSelectedPosition() {
        return this.mSelectedPosition;
    }

    public RowPresenter.ViewHolder getSelectedRowViewHolder() {
        if (this.mMainFragmentRowsAdapter == null) {
            return null;
        }
        return this.mMainFragmentRowsAdapter.findRowViewHolderByPosition(this.mMainFragmentRowsAdapter.getSelectedPosition());
    }

    public void setSelectedPosition(int position, boolean smooth) {
        this.mSetSelectionRunnable.post(position, 1, smooth);
    }

    public void setSelectedPosition(int rowPosition, boolean smooth, ViewHolderTask rowHolderTask) {
        if (this.mMainFragmentAdapterRegistry != null) {
            if (rowHolderTask != null) {
                startHeadersTransition(false);
            }
            if (this.mMainFragmentRowsAdapter != null) {
                this.mMainFragmentRowsAdapter.setSelectedPosition(rowPosition, smooth, rowHolderTask);
            }
        }
    }

    public void onStart() {
        super.onStart();
        this.mHeadersFragment.setAlignment(this.mContainerListAlignTop);
        setMainFragmentAlignment();
        if (this.mCanShowHeaders && this.mShowingHeaders && this.mHeadersFragment != null && this.mHeadersFragment.getView() != null) {
            this.mHeadersFragment.getView().requestFocus();
        } else if (!((this.mCanShowHeaders && this.mShowingHeaders) || this.mMainFragment == null || this.mMainFragment.getView() == null)) {
            this.mMainFragment.getView().requestFocus();
        }
        if (this.mCanShowHeaders) {
            showHeaders(this.mShowingHeaders);
        }
        this.mStateMachine.fireEvent(this.EVT_HEADER_VIEW_CREATED);
    }

    private void onExpandTransitionStart(boolean expand, Runnable callback) {
        if (expand) {
            callback.run();
        } else {
            new ExpandPreLayout(callback, this.mMainFragmentAdapter, getView()).execute();
        }
    }

    private void setMainFragmentAlignment() {
        int alignOffset = this.mContainerListAlignTop;
        if (this.mMainFragmentScaleEnabled && this.mMainFragmentAdapter.isScalingEnabled() && this.mShowingHeaders) {
            alignOffset = (int) ((((float) alignOffset) / this.mScaleFactor) + 0.5f);
        }
        this.mMainFragmentAdapter.setAlignment(alignOffset);
    }

    public final void setHeadersTransitionOnBackEnabled(boolean headersBackStackEnabled) {
        this.mHeadersBackStackEnabled = headersBackStackEnabled;
    }

    public final boolean isHeadersTransitionOnBackEnabled() {
        return this.mHeadersBackStackEnabled;
    }

    private void readArguments(Bundle args) {
        if (args != null) {
            if (args.containsKey(ARG_TITLE)) {
                setTitle(args.getString(ARG_TITLE));
            }
            if (args.containsKey(ARG_HEADERS_STATE)) {
                setHeadersState(args.getInt(ARG_HEADERS_STATE));
            }
        }
    }

    public void setHeadersState(int headersState) {
        if (headersState < 1 || headersState > 3) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid headers state: ");
            stringBuilder.append(headersState);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (headersState != this.mHeadersState) {
            this.mHeadersState = headersState;
            switch (headersState) {
                case 1:
                    this.mCanShowHeaders = true;
                    this.mShowingHeaders = true;
                    break;
                case 2:
                    this.mCanShowHeaders = true;
                    this.mShowingHeaders = false;
                    break;
                case 3:
                    this.mCanShowHeaders = false;
                    this.mShowingHeaders = false;
                    break;
                default:
                    String str = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Unknown headers state: ");
                    stringBuilder2.append(headersState);
                    Log.w(str, stringBuilder2.toString());
                    break;
            }
            if (this.mHeadersFragment != null) {
                this.mHeadersFragment.setHeadersGone(1 ^ this.mCanShowHeaders);
            }
        }
    }

    public int getHeadersState() {
        return this.mHeadersState;
    }

    /* Access modifiers changed, original: protected */
    public Object createEntranceTransition() {
        return TransitionHelper.loadTransition(FragmentUtil.getContext(this), R.transition.lb_browse_entrance_transition);
    }

    /* Access modifiers changed, original: protected */
    public void runEntranceTransition(Object entranceTransition) {
        TransitionHelper.runTransition(this.mSceneAfterEntranceTransition, entranceTransition);
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionPrepare() {
        this.mHeadersFragment.onTransitionPrepare();
        this.mMainFragmentAdapter.setEntranceTransitionState(false);
        this.mMainFragmentAdapter.onTransitionPrepare();
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionStart() {
        this.mHeadersFragment.onTransitionStart();
        this.mMainFragmentAdapter.onTransitionStart();
    }

    /* Access modifiers changed, original: protected */
    public void onEntranceTransitionEnd() {
        if (this.mMainFragmentAdapter != null) {
            this.mMainFragmentAdapter.onTransitionEnd();
        }
        if (this.mHeadersFragment != null) {
            this.mHeadersFragment.onTransitionEnd();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setSearchOrbViewOnScreen(boolean onScreen) {
        View searchOrbView = getTitleViewAdapter().getSearchAffordanceView();
        if (searchOrbView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) searchOrbView.getLayoutParams();
            lp.setMarginStart(onScreen ? 0 : -this.mContainerListMarginStart);
            searchOrbView.setLayoutParams(lp);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setEntranceTransitionStartState() {
        setHeadersOnScreen(false);
        setSearchOrbViewOnScreen(false);
    }

    /* Access modifiers changed, original: 0000 */
    public void setEntranceTransitionEndState() {
        setHeadersOnScreen(this.mShowingHeaders);
        setSearchOrbViewOnScreen(true);
        this.mMainFragmentAdapter.setEntranceTransitionState(true);
    }
}