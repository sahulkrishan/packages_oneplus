package android.support.v17.leanback.widget;

import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.Parallax.IntProperty;
import android.support.v17.leanback.widget.RecyclerViewParallax.ChildPositionProperty;

public class DetailsParallax extends RecyclerViewParallax {
    final IntProperty mFrameBottom = ((ChildPositionProperty) addProperty("overviewRowBottom")).adapterPosition(0).viewId(R.id.details_frame).fraction(1.0f);
    final IntProperty mFrameTop = ((ChildPositionProperty) addProperty("overviewRowTop")).adapterPosition(0).viewId(R.id.details_frame);

    public IntProperty getOverviewRowTop() {
        return this.mFrameTop;
    }

    public IntProperty getOverviewRowBottom() {
        return this.mFrameBottom;
    }
}
