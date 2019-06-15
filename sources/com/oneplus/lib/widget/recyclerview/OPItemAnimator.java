package com.oneplus.lib.widget.recyclerview;

import android.view.MotionEvent;
import com.oneplus.lib.widget.recyclerview.RecyclerView.OnItemTouchListener;
import com.oneplus.lib.widget.recyclerview.RecyclerView.ViewHolder;

public class OPItemAnimator extends DefaultItemAnimator {
    private static final int ONEPLUS_DURATION_MOVE = 200;
    private static final int ONEPLUS_DURATION_REMOVE = 200;
    private RecyclerView mRecyclerView;
    private OnItemTouchListener recyclerViewDisabler = new RecyclerViewDisabler();

    private class RecyclerViewDisabler implements OnItemTouchListener {
        private RecyclerViewDisabler() {
        }

        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    public OPItemAnimator(RecyclerView rv) {
        this.mRecyclerView = rv;
        setRemoveDuration(200);
        setMoveDuration(200);
    }

    public void onRemoveStarting(ViewHolder item) {
        this.mRecyclerView.addOnItemTouchListener(this.recyclerViewDisabler);
    }

    public void onMoveFinished(ViewHolder viewHolder) {
        this.mRecyclerView.removeOnItemTouchListener(this.recyclerViewDisabler);
    }
}
