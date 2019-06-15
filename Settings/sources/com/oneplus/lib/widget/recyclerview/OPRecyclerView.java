package com.oneplus.lib.widget.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class OPRecyclerView extends RecyclerView {
    private final Rect mContentPadding = new Rect();

    public OPRecyclerView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OPRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OPRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setClipToPadding(false);
    }

    public void addOPItemDecoration(OPItemDecoration decor) {
        addItemDecoration(decor);
        setPadding(0, decor.getSpace(), decor.getSpace(), 0);
    }
}
