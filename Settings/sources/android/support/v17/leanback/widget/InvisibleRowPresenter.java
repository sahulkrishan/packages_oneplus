package android.support.v17.leanback.widget;

import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.RowPresenter.ViewHolder;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@RestrictTo({Scope.LIBRARY_GROUP})
public class InvisibleRowPresenter extends RowPresenter {
    public InvisibleRowPresenter() {
        setHeaderPresenter(null);
    }

    /* Access modifiers changed, original: protected */
    public ViewHolder createRowViewHolder(ViewGroup parent) {
        RelativeLayout root = new RelativeLayout(parent.getContext());
        root.setLayoutParams(new LayoutParams(0, 0));
        return new ViewHolder(root);
    }
}
