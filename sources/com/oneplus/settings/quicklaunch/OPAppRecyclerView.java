package com.oneplus.settings.quicklaunch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

class OPAppRecyclerView extends RecyclerView {
    public OPAppRecyclerView(Context context) {
        super(context);
    }

    public OPAppRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OPAppRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == 1) {
            OPAppDragAndDropAdapter adapter = (OPAppDragAndDropAdapter) getAdapter();
            if (adapter != null) {
                adapter.doTheUpdate();
            }
        }
        return super.onTouchEvent(e);
    }
}
