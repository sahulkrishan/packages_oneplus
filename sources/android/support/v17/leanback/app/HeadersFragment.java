package android.support.v17.leanback.app;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DividerPresenter;
import android.support.v17.leanback.widget.DividerRow;
import android.support.v17.leanback.widget.FocusHighlightHelper;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ItemBridgeAdapter.AdapterListener;
import android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder;
import android.support.v17.leanback.widget.ItemBridgeAdapter.Wrapper;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v17.leanback.widget.SectionRow;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

@Deprecated
public class HeadersFragment extends BaseRowFragment {
    private static final PresenterSelector sHeaderPresenter = new ClassPresenterSelector().addClassPresenter(DividerRow.class, new DividerPresenter()).addClassPresenter(SectionRow.class, new RowHeaderPresenter(R.layout.lb_section_header, false)).addClassPresenter(Row.class, new RowHeaderPresenter(R.layout.lb_header));
    static OnLayoutChangeListener sLayoutChangeListener = new OnLayoutChangeListener() {
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.setPivotX(v.getLayoutDirection() == 1 ? (float) v.getWidth() : 0.0f);
            v.setPivotY((float) (v.getMeasuredHeight() / 2));
        }
    };
    private final AdapterListener mAdapterListener = new AdapterListener() {
        public void onCreate(final ViewHolder viewHolder) {
            View headerView = viewHolder.getViewHolder().view;
            headerView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (HeadersFragment.this.mOnHeaderClickedListener != null) {
                        HeadersFragment.this.mOnHeaderClickedListener.onHeaderClicked((RowHeaderPresenter.ViewHolder) viewHolder.getViewHolder(), (Row) viewHolder.getItem());
                    }
                }
            });
            if (HeadersFragment.this.mWrapper != null) {
                viewHolder.itemView.addOnLayoutChangeListener(HeadersFragment.sLayoutChangeListener);
            } else {
                headerView.addOnLayoutChangeListener(HeadersFragment.sLayoutChangeListener);
            }
        }
    };
    private int mBackgroundColor;
    private boolean mBackgroundColorSet;
    private boolean mHeadersEnabled = true;
    private boolean mHeadersGone = false;
    OnHeaderClickedListener mOnHeaderClickedListener;
    private OnHeaderViewSelectedListener mOnHeaderViewSelectedListener;
    final Wrapper mWrapper = new Wrapper() {
        public void wrap(View wrapper, View wrapped) {
            ((FrameLayout) wrapper).addView(wrapped);
        }

        public View createWrapper(View root) {
            return new NoOverlappingFrameLayout(root.getContext());
        }
    };

    static class NoOverlappingFrameLayout extends FrameLayout {
        public NoOverlappingFrameLayout(Context context) {
            super(context);
        }

        public boolean hasOverlappingRendering() {
            return false;
        }
    }

    @Deprecated
    public interface OnHeaderClickedListener {
        void onHeaderClicked(RowHeaderPresenter.ViewHolder viewHolder, Row row);
    }

    @Deprecated
    public interface OnHeaderViewSelectedListener {
        void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row);
    }

    public /* bridge */ /* synthetic */ int getSelectedPosition() {
        return super.getSelectedPosition();
    }

    public /* bridge */ /* synthetic */ View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public /* bridge */ /* synthetic */ void onDestroyView() {
        super.onDestroyView();
    }

    public /* bridge */ /* synthetic */ void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    public /* bridge */ /* synthetic */ boolean onTransitionPrepare() {
        return super.onTransitionPrepare();
    }

    public /* bridge */ /* synthetic */ void setAlignment(int i) {
        super.setAlignment(i);
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int i) {
        super.setSelectedPosition(i);
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int i, boolean z) {
        super.setSelectedPosition(i, z);
    }

    public HeadersFragment() {
        setPresenterSelector(sHeaderPresenter);
        FocusHighlightHelper.setupHeaderItemFocusHighlight(getBridgeAdapter());
    }

    public void setOnHeaderClickedListener(OnHeaderClickedListener listener) {
        this.mOnHeaderClickedListener = listener;
    }

    public void setOnHeaderViewSelectedListener(OnHeaderViewSelectedListener listener) {
        this.mOnHeaderViewSelectedListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public VerticalGridView findGridViewFromRoot(View view) {
        return (VerticalGridView) view.findViewById(R.id.browse_headers);
    }

    /* Access modifiers changed, original: 0000 */
    public void onRowSelected(RecyclerView parent, RecyclerView.ViewHolder viewHolder, int position, int subposition) {
        if (this.mOnHeaderViewSelectedListener == null) {
            return;
        }
        if (viewHolder == null || position < 0) {
            this.mOnHeaderViewSelectedListener.onHeaderSelected(null, null);
            return;
        }
        ViewHolder vh = (ViewHolder) viewHolder;
        this.mOnHeaderViewSelectedListener.onHeaderSelected((RowHeaderPresenter.ViewHolder) vh.getViewHolder(), (Row) vh.getItem());
    }

    /* Access modifiers changed, original: 0000 */
    public int getLayoutResourceId() {
        return R.layout.lb_headers_fragment;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            if (this.mBackgroundColorSet) {
                listView.setBackgroundColor(this.mBackgroundColor);
                updateFadingEdgeToBrandColor(this.mBackgroundColor);
            } else {
                Drawable d = listView.getBackground();
                if (d instanceof ColorDrawable) {
                    updateFadingEdgeToBrandColor(((ColorDrawable) d).getColor());
                }
            }
            updateListViewVisibility();
        }
    }

    private void updateListViewVisibility() {
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            getView().setVisibility(this.mHeadersGone ? 8 : 0);
            if (!this.mHeadersGone) {
                if (this.mHeadersEnabled) {
                    listView.setChildrenVisibility(0);
                } else {
                    listView.setChildrenVisibility(4);
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setHeadersEnabled(boolean enabled) {
        this.mHeadersEnabled = enabled;
        updateListViewVisibility();
    }

    /* Access modifiers changed, original: 0000 */
    public void setHeadersGone(boolean gone) {
        this.mHeadersGone = gone;
        updateListViewVisibility();
    }

    /* Access modifiers changed, original: 0000 */
    public void updateAdapter() {
        super.updateAdapter();
        ItemBridgeAdapter adapter = getBridgeAdapter();
        adapter.setAdapterListener(this.mAdapterListener);
        adapter.setWrapper(this.mWrapper);
    }

    /* Access modifiers changed, original: 0000 */
    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        this.mBackgroundColorSet = true;
        if (getVerticalGridView() != null) {
            getVerticalGridView().setBackgroundColor(this.mBackgroundColor);
            updateFadingEdgeToBrandColor(this.mBackgroundColor);
        }
    }

    private void updateFadingEdgeToBrandColor(int backgroundColor) {
        Drawable background = getView().findViewById(R.id.fade_out_edge).getBackground();
        if (background instanceof GradientDrawable) {
            background.mutate();
            ((GradientDrawable) background).setColors(new int[]{0, backgroundColor});
        }
    }

    public void onTransitionStart() {
        super.onTransitionStart();
        if (!this.mHeadersEnabled) {
            VerticalGridView listView = getVerticalGridView();
            if (listView != null) {
                listView.setDescendantFocusability(131072);
                if (listView.hasFocus()) {
                    listView.requestFocus();
                }
            }
        }
    }

    public void onTransitionEnd() {
        if (this.mHeadersEnabled) {
            VerticalGridView listView = getVerticalGridView();
            if (listView != null) {
                listView.setDescendantFocusability(262144);
                if (listView.hasFocus()) {
                    listView.requestFocus();
                }
            }
        }
        super.onTransitionEnd();
    }

    public boolean isScrolling() {
        return getVerticalGridView().getScrollState() != 0;
    }
}
