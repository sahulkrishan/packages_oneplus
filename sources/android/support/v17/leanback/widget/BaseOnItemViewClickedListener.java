package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.Presenter.ViewHolder;

public interface BaseOnItemViewClickedListener<T> {
    void onItemClicked(ViewHolder viewHolder, Object obj, RowPresenter.ViewHolder viewHolder2, T t);
}
