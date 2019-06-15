package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.ControlBar.OnChildFocusedListener;
import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

class ControlBarPresenter extends Presenter {
    static final int MAX_CONTROLS = 7;
    private static int sChildMarginDefault;
    private static int sControlIconWidth;
    boolean mDefaultFocusToMiddle = true;
    private int mLayoutResourceId;
    OnControlClickedListener mOnControlClickedListener;
    OnControlSelectedListener mOnControlSelectedListener;

    static class BoundData {
        ObjectAdapter adapter;
        Presenter presenter;

        BoundData() {
        }
    }

    interface OnControlClickedListener {
        void onControlClicked(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder, Object obj, BoundData boundData);
    }

    interface OnControlSelectedListener {
        void onControlSelected(android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder, Object obj, BoundData boundData);
    }

    class ViewHolder extends android.support.v17.leanback.widget.Presenter.ViewHolder {
        ObjectAdapter mAdapter;
        ControlBar mControlBar;
        View mControlsContainer;
        BoundData mData;
        DataObserver mDataObserver;
        Presenter mPresenter;
        SparseArray<android.support.v17.leanback.widget.Presenter.ViewHolder> mViewHolders = new SparseArray();

        ViewHolder(View rootView) {
            super(rootView);
            this.mControlsContainer = rootView.findViewById(R.id.controls_container);
            this.mControlBar = (ControlBar) rootView.findViewById(R.id.control_bar);
            if (this.mControlBar != null) {
                this.mControlBar.setDefaultFocusToMiddle(ControlBarPresenter.this.mDefaultFocusToMiddle);
                this.mControlBar.setOnChildFocusedListener(new OnChildFocusedListener(ControlBarPresenter.this) {
                    public void onChildFocusedListener(View child, View focused) {
                        if (ControlBarPresenter.this.mOnControlSelectedListener != null) {
                            for (int position = 0; position < ViewHolder.this.mViewHolders.size(); position++) {
                                if (((android.support.v17.leanback.widget.Presenter.ViewHolder) ViewHolder.this.mViewHolders.get(position)).view == child) {
                                    ControlBarPresenter.this.mOnControlSelectedListener.onControlSelected((android.support.v17.leanback.widget.Presenter.ViewHolder) ViewHolder.this.mViewHolders.get(position), ViewHolder.this.getDisplayedAdapter().get(position), ViewHolder.this.mData);
                                    break;
                                }
                            }
                        }
                    }
                });
                this.mDataObserver = new DataObserver(ControlBarPresenter.this) {
                    public void onChanged() {
                        if (ViewHolder.this.mAdapter == ViewHolder.this.getDisplayedAdapter()) {
                            ViewHolder.this.showControls(ViewHolder.this.mPresenter);
                        }
                    }

                    public void onItemRangeChanged(int positionStart, int itemCount) {
                        if (ViewHolder.this.mAdapter == ViewHolder.this.getDisplayedAdapter()) {
                            for (int i = 0; i < itemCount; i++) {
                                ViewHolder.this.bindControlToAction(positionStart + i, ViewHolder.this.mPresenter);
                            }
                        }
                    }
                };
                return;
            }
            throw new IllegalStateException("Couldn't find control_bar");
        }

        /* Access modifiers changed, original: 0000 */
        public int getChildMarginFromCenter(Context context, int numControls) {
            return ControlBarPresenter.this.getChildMarginDefault(context) + ControlBarPresenter.this.getControlIconWidth(context);
        }

        /* Access modifiers changed, original: 0000 */
        public void showControls(Presenter presenter) {
            ObjectAdapter adapter = getDisplayedAdapter();
            int position = 0;
            int adapterSize = adapter == null ? 0 : adapter.size();
            View focusedView = this.mControlBar.getFocusedChild();
            if (focusedView != null && adapterSize > 0 && this.mControlBar.indexOfChild(focusedView) >= adapterSize) {
                this.mControlBar.getChildAt(adapter.size() - 1).requestFocus();
            }
            for (int i = this.mControlBar.getChildCount() - 1; i >= adapterSize; i--) {
                this.mControlBar.removeViewAt(i);
            }
            while (position < adapterSize && position < 7) {
                bindControlToAction(position, adapter, presenter);
                position++;
            }
            this.mControlBar.setChildMarginFromCenter(getChildMarginFromCenter(this.mControlBar.getContext(), adapterSize));
        }

        /* Access modifiers changed, original: 0000 */
        public void bindControlToAction(int position, Presenter presenter) {
            bindControlToAction(position, getDisplayedAdapter(), presenter);
        }

        private void bindControlToAction(final int position, ObjectAdapter adapter, Presenter presenter) {
            android.support.v17.leanback.widget.Presenter.ViewHolder vh = (android.support.v17.leanback.widget.Presenter.ViewHolder) this.mViewHolders.get(position);
            Object item = adapter.get(position);
            if (vh == null) {
                vh = presenter.onCreateViewHolder(this.mControlBar);
                this.mViewHolders.put(position, vh);
                final android.support.v17.leanback.widget.Presenter.ViewHolder itemViewHolder = vh;
                presenter.setOnClickListener(vh, new OnClickListener() {
                    public void onClick(View v) {
                        Object item = ViewHolder.this.getDisplayedAdapter().get(position);
                        if (ControlBarPresenter.this.mOnControlClickedListener != null) {
                            ControlBarPresenter.this.mOnControlClickedListener.onControlClicked(itemViewHolder, item, ViewHolder.this.mData);
                        }
                    }
                });
            }
            if (vh.view.getParent() == null) {
                this.mControlBar.addView(vh.view);
            }
            presenter.onBindViewHolder(vh, item);
        }

        /* Access modifiers changed, original: 0000 */
        public ObjectAdapter getDisplayedAdapter() {
            return this.mAdapter;
        }
    }

    public ControlBarPresenter(int layoutResourceId) {
        this.mLayoutResourceId = layoutResourceId;
    }

    public int getLayoutResourceId() {
        return this.mLayoutResourceId;
    }

    public void setOnControlClickedListener(OnControlClickedListener listener) {
        this.mOnControlClickedListener = listener;
    }

    public OnControlClickedListener getOnItemViewClickedListener() {
        return this.mOnControlClickedListener;
    }

    public void setOnControlSelectedListener(OnControlSelectedListener listener) {
        this.mOnControlSelectedListener = listener;
    }

    public OnControlSelectedListener getOnItemControlListener() {
        return this.mOnControlSelectedListener;
    }

    public void setBackgroundColor(ViewHolder vh, int color) {
        vh.mControlsContainer.setBackgroundColor(color);
    }

    public android.support.v17.leanback.widget.Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutResourceId(), parent, false));
    }

    public void onBindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder holder, Object item) {
        ViewHolder vh = (ViewHolder) holder;
        BoundData data = (BoundData) item;
        if (vh.mAdapter != data.adapter) {
            vh.mAdapter = data.adapter;
            if (vh.mAdapter != null) {
                vh.mAdapter.registerObserver(vh.mDataObserver);
            }
        }
        vh.mPresenter = data.presenter;
        vh.mData = data;
        vh.showControls(vh.mPresenter);
    }

    public void onUnbindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder holder) {
        ViewHolder vh = (ViewHolder) holder;
        if (vh.mAdapter != null) {
            vh.mAdapter.unregisterObserver(vh.mDataObserver);
            vh.mAdapter = null;
        }
        vh.mData = null;
    }

    /* Access modifiers changed, original: 0000 */
    public int getChildMarginDefault(Context context) {
        if (sChildMarginDefault == 0) {
            sChildMarginDefault = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_child_margin_default);
        }
        return sChildMarginDefault;
    }

    /* Access modifiers changed, original: 0000 */
    public int getControlIconWidth(Context context) {
        if (sControlIconWidth == 0) {
            sControlIconWidth = context.getResources().getDimensionPixelSize(R.dimen.lb_control_icon_width);
        }
        return sControlIconWidth;
    }

    /* Access modifiers changed, original: 0000 */
    public void setDefaultFocusToMiddle(boolean defaultFocusToMiddle) {
        this.mDefaultFocusToMiddle = defaultFocusToMiddle;
    }
}
