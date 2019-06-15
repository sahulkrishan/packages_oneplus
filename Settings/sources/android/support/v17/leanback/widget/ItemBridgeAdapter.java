package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ItemBridgeAdapter extends Adapter implements FacetProviderAdapter {
    static final boolean DEBUG = false;
    static final String TAG = "ItemBridgeAdapter";
    private ObjectAdapter mAdapter;
    private AdapterListener mAdapterListener;
    private DataObserver mDataObserver;
    FocusHighlightHandler mFocusHighlight;
    private PresenterSelector mPresenterSelector;
    private ArrayList<Presenter> mPresenters;
    Wrapper mWrapper;

    public static class AdapterListener {
        public void onAddPresenter(Presenter presenter, int type) {
        }

        public void onCreate(ViewHolder viewHolder) {
        }

        public void onBind(ViewHolder viewHolder) {
        }

        public void onBind(ViewHolder viewHolder, List payloads) {
            onBind(viewHolder);
        }

        public void onUnbind(ViewHolder viewHolder) {
        }

        public void onAttachedToWindow(ViewHolder viewHolder) {
        }

        public void onDetachedFromWindow(ViewHolder viewHolder) {
        }
    }

    final class OnFocusChangeListener implements android.view.View.OnFocusChangeListener {
        android.view.View.OnFocusChangeListener mChainedListener;

        OnFocusChangeListener() {
        }

        public void onFocusChange(View view, boolean hasFocus) {
            if (ItemBridgeAdapter.this.mWrapper != null) {
                view = (View) view.getParent();
            }
            if (ItemBridgeAdapter.this.mFocusHighlight != null) {
                ItemBridgeAdapter.this.mFocusHighlight.onItemFocused(view, hasFocus);
            }
            if (this.mChainedListener != null) {
                this.mChainedListener.onFocusChange(view, hasFocus);
            }
        }
    }

    public static abstract class Wrapper {
        public abstract View createWrapper(View view);

        public abstract void wrap(View view, View view2);
    }

    public class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements FacetProvider {
        Object mExtraObject;
        final OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener();
        final android.support.v17.leanback.widget.Presenter.ViewHolder mHolder;
        Object mItem;
        final Presenter mPresenter;

        public final Presenter getPresenter() {
            return this.mPresenter;
        }

        public final android.support.v17.leanback.widget.Presenter.ViewHolder getViewHolder() {
            return this.mHolder;
        }

        public final Object getItem() {
            return this.mItem;
        }

        public final Object getExtraObject() {
            return this.mExtraObject;
        }

        public void setExtraObject(Object object) {
            this.mExtraObject = object;
        }

        public Object getFacet(Class<?> facetClass) {
            return this.mHolder.getFacet(facetClass);
        }

        ViewHolder(Presenter presenter, View view, android.support.v17.leanback.widget.Presenter.ViewHolder holder) {
            super(view);
            this.mPresenter = presenter;
            this.mHolder = holder;
        }
    }

    public ItemBridgeAdapter(ObjectAdapter adapter, PresenterSelector presenterSelector) {
        this.mPresenters = new ArrayList();
        this.mDataObserver = new DataObserver() {
            public void onChanged() {
                ItemBridgeAdapter.this.notifyDataSetChanged();
            }

            public void onItemRangeChanged(int positionStart, int itemCount) {
                ItemBridgeAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                ItemBridgeAdapter.this.notifyItemRangeChanged(positionStart, itemCount, payload);
            }

            public void onItemRangeInserted(int positionStart, int itemCount) {
                ItemBridgeAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                ItemBridgeAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
            }

            public void onItemMoved(int fromPosition, int toPosition) {
                ItemBridgeAdapter.this.notifyItemMoved(fromPosition, toPosition);
            }
        };
        setAdapter(adapter);
        this.mPresenterSelector = presenterSelector;
    }

    public ItemBridgeAdapter(ObjectAdapter adapter) {
        this(adapter, null);
    }

    public ItemBridgeAdapter() {
        this.mPresenters = new ArrayList();
        this.mDataObserver = /* anonymous class already generated */;
    }

    public void setAdapter(ObjectAdapter adapter) {
        if (adapter != this.mAdapter) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterObserver(this.mDataObserver);
            }
            this.mAdapter = adapter;
            if (this.mAdapter == null) {
                notifyDataSetChanged();
                return;
            }
            this.mAdapter.registerObserver(this.mDataObserver);
            if (hasStableIds() != this.mAdapter.hasStableIds()) {
                setHasStableIds(this.mAdapter.hasStableIds());
            }
            notifyDataSetChanged();
        }
    }

    public void setPresenter(PresenterSelector presenterSelector) {
        this.mPresenterSelector = presenterSelector;
        notifyDataSetChanged();
    }

    public void setWrapper(Wrapper wrapper) {
        this.mWrapper = wrapper;
    }

    public Wrapper getWrapper() {
        return this.mWrapper;
    }

    /* Access modifiers changed, original: 0000 */
    public void setFocusHighlight(FocusHighlightHandler listener) {
        this.mFocusHighlight = listener;
    }

    public void clear() {
        setAdapter(null);
    }

    public void setPresenterMapper(ArrayList<Presenter> presenters) {
        this.mPresenters = presenters;
    }

    public ArrayList<Presenter> getPresenterMapper() {
        return this.mPresenters;
    }

    public int getItemCount() {
        return this.mAdapter != null ? this.mAdapter.size() : 0;
    }

    public int getItemViewType(int position) {
        PresenterSelector presenterSelector;
        if (this.mPresenterSelector != null) {
            presenterSelector = this.mPresenterSelector;
        } else {
            presenterSelector = this.mAdapter.getPresenterSelector();
        }
        Presenter presenter = presenterSelector.getPresenter(this.mAdapter.get(position));
        int type = this.mPresenters.indexOf(presenter);
        if (type < 0) {
            this.mPresenters.add(presenter);
            type = this.mPresenters.indexOf(presenter);
            onAddPresenter(presenter, type);
            if (this.mAdapterListener != null) {
                this.mAdapterListener.onAddPresenter(presenter, type);
            }
        }
        return type;
    }

    /* Access modifiers changed, original: protected */
    public void onAddPresenter(Presenter presenter, int type) {
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(ViewHolder viewHolder) {
    }

    /* Access modifiers changed, original: protected */
    public void onBind(ViewHolder viewHolder) {
    }

    /* Access modifiers changed, original: protected */
    public void onUnbind(ViewHolder viewHolder) {
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow(ViewHolder viewHolder) {
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow(ViewHolder viewHolder) {
    }

    public final android.support.v7.widget.RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        android.support.v17.leanback.widget.Presenter.ViewHolder presenterVh;
        Presenter presenter = (Presenter) this.mPresenters.get(viewType);
        if (this.mWrapper != null) {
            view = this.mWrapper.createWrapper(parent);
            presenterVh = presenter.onCreateViewHolder(parent);
            this.mWrapper.wrap(view, presenterVh.view);
        } else {
            presenterVh = presenter.onCreateViewHolder(parent);
            view = presenterVh.view;
        }
        ViewHolder viewHolder = new ViewHolder(presenter, view, presenterVh);
        onCreate(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onCreate(viewHolder);
        }
        View presenterView = viewHolder.mHolder.view;
        if (presenterView != null) {
            viewHolder.mFocusChangeListener.mChainedListener = presenterView.getOnFocusChangeListener();
            presenterView.setOnFocusChangeListener(viewHolder.mFocusChangeListener);
        }
        if (this.mFocusHighlight != null) {
            this.mFocusHighlight.onInitializeView(view);
        }
        return viewHolder;
    }

    public void setAdapterListener(AdapterListener listener) {
        this.mAdapterListener = listener;
    }

    public final void onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mItem = this.mAdapter.get(position);
        viewHolder.mPresenter.onBindViewHolder(viewHolder.mHolder, viewHolder.mItem);
        onBind(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onBind(viewHolder);
        }
    }

    public final void onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder holder, int position, List payloads) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mItem = this.mAdapter.get(position);
        viewHolder.mPresenter.onBindViewHolder(viewHolder.mHolder, viewHolder.mItem, payloads);
        onBind(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onBind(viewHolder, payloads);
        }
    }

    public final void onViewRecycled(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mPresenter.onUnbindViewHolder(viewHolder.mHolder);
        onUnbind(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onUnbind(viewHolder);
        }
        viewHolder.mItem = null;
    }

    public final boolean onFailedToRecycleView(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        onViewRecycled(holder);
        return false;
    }

    public final void onViewAttachedToWindow(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        onAttachedToWindow(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onAttachedToWindow(viewHolder);
        }
        viewHolder.mPresenter.onViewAttachedToWindow(viewHolder.mHolder);
    }

    public final void onViewDetachedFromWindow(android.support.v7.widget.RecyclerView.ViewHolder holder) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.mPresenter.onViewDetachedFromWindow(viewHolder.mHolder);
        onDetachedFromWindow(viewHolder);
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onDetachedFromWindow(viewHolder);
        }
    }

    public long getItemId(int position) {
        return this.mAdapter.getId(position);
    }

    public FacetProvider getFacetProvider(int type) {
        return (FacetProvider) this.mPresenters.get(type);
    }
}
