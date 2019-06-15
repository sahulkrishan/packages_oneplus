package com.oneplus.lib.widget.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.widget.recyclerview.RecyclerView.State;

public class OPCardItemDecoration extends OPItemDecoration {
    Context mContext;

    public OPCardItemDecoration(Context context) {
        super(0);
        setSpace(context.getResources().getDimensionPixelSize(R.dimen.opcardview_margin));
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.set(this.mSpace, 0, 0, this.mSpace);
    }
}
