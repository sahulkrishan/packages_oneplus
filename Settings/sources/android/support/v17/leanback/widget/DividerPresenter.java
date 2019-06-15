package android.support.v17.leanback.widget;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class DividerPresenter extends Presenter {
    private final int mLayoutResourceId;

    public DividerPresenter() {
        this(R.layout.lb_divider);
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public DividerPresenter(int layoutResourceId) {
        this.mLayoutResourceId = layoutResourceId;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(this.mLayoutResourceId, parent, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
    }

    public void onUnbindViewHolder(ViewHolder viewHolder) {
    }
}
