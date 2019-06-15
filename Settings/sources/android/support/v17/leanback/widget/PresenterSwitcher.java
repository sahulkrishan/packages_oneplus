package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

public abstract class PresenterSwitcher {
    private Presenter mCurrentPresenter;
    private ViewHolder mCurrentViewHolder;
    private ViewGroup mParent;
    private PresenterSelector mPresenterSelector;

    public abstract void insertView(View view);

    public void init(ViewGroup parent, PresenterSelector presenterSelector) {
        clear();
        this.mParent = parent;
        this.mPresenterSelector = presenterSelector;
    }

    public void select(Object object) {
        switchView(object);
        showView(true);
    }

    public void unselect() {
        showView(false);
    }

    public final ViewGroup getParentViewGroup() {
        return this.mParent;
    }

    private void showView(boolean show) {
        if (this.mCurrentViewHolder != null) {
            showView(this.mCurrentViewHolder.view, show);
        }
    }

    private void switchView(Object object) {
        Presenter presenter = this.mPresenterSelector.getPresenter(object);
        if (presenter != this.mCurrentPresenter) {
            showView(false);
            clear();
            this.mCurrentPresenter = presenter;
            if (this.mCurrentPresenter != null) {
                this.mCurrentViewHolder = this.mCurrentPresenter.onCreateViewHolder(this.mParent);
                insertView(this.mCurrentViewHolder.view);
            } else {
                return;
            }
        } else if (this.mCurrentPresenter != null) {
            this.mCurrentPresenter.onUnbindViewHolder(this.mCurrentViewHolder);
        } else {
            return;
        }
        this.mCurrentPresenter.onBindViewHolder(this.mCurrentViewHolder, object);
        onViewSelected(this.mCurrentViewHolder.view);
    }

    /* Access modifiers changed, original: protected */
    public void onViewSelected(View view) {
    }

    /* Access modifiers changed, original: protected */
    public void showView(View view, boolean visible) {
        view.setVisibility(visible ? 0 : 8);
    }

    public void clear() {
        if (this.mCurrentPresenter != null) {
            this.mCurrentPresenter.onUnbindViewHolder(this.mCurrentViewHolder);
            this.mParent.removeView(this.mCurrentViewHolder.view);
            this.mCurrentViewHolder = null;
            this.mCurrentPresenter = null;
        }
    }
}
