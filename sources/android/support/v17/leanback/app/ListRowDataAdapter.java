package android.support.v17.leanback.app;

import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.support.v17.leanback.widget.Row;

class ListRowDataAdapter extends ObjectAdapter {
    public static final int ON_CHANGED = 16;
    public static final int ON_ITEM_RANGE_CHANGED = 2;
    public static final int ON_ITEM_RANGE_INSERTED = 4;
    public static final int ON_ITEM_RANGE_REMOVED = 8;
    private final ObjectAdapter mAdapter;
    final DataObserver mDataObserver;
    int mLastVisibleRowIndex;

    private class QueueBasedDataObserver extends DataObserver {
        QueueBasedDataObserver() {
        }

        public void onChanged() {
            ListRowDataAdapter.this.initialize();
            ListRowDataAdapter.this.notifyChanged();
        }
    }

    private class SimpleDataObserver extends DataObserver {
        SimpleDataObserver() {
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (positionStart <= ListRowDataAdapter.this.mLastVisibleRowIndex) {
                onEventFired(2, positionStart, Math.min(itemCount, (ListRowDataAdapter.this.mLastVisibleRowIndex - positionStart) + 1));
            }
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (positionStart <= ListRowDataAdapter.this.mLastVisibleRowIndex) {
                ListRowDataAdapter listRowDataAdapter = ListRowDataAdapter.this;
                listRowDataAdapter.mLastVisibleRowIndex += itemCount;
                onEventFired(4, positionStart, itemCount);
                return;
            }
            int lastVisibleRowIndex = ListRowDataAdapter.this.mLastVisibleRowIndex;
            ListRowDataAdapter.this.initialize();
            if (ListRowDataAdapter.this.mLastVisibleRowIndex > lastVisibleRowIndex) {
                onEventFired(4, lastVisibleRowIndex + 1, ListRowDataAdapter.this.mLastVisibleRowIndex - lastVisibleRowIndex);
            }
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if ((positionStart + itemCount) - 1 < ListRowDataAdapter.this.mLastVisibleRowIndex) {
                ListRowDataAdapter listRowDataAdapter = ListRowDataAdapter.this;
                listRowDataAdapter.mLastVisibleRowIndex -= itemCount;
                onEventFired(8, positionStart, itemCount);
                return;
            }
            int lastVisibleRowIndex = ListRowDataAdapter.this.mLastVisibleRowIndex;
            ListRowDataAdapter.this.initialize();
            int totalItems = lastVisibleRowIndex - ListRowDataAdapter.this.mLastVisibleRowIndex;
            if (totalItems > 0) {
                onEventFired(8, Math.min(ListRowDataAdapter.this.mLastVisibleRowIndex + 1, positionStart), totalItems);
            }
        }

        public void onChanged() {
            ListRowDataAdapter.this.initialize();
            onEventFired(16, -1, -1);
        }

        /* Access modifiers changed, original: protected */
        public void onEventFired(int eventType, int positionStart, int itemCount) {
            ListRowDataAdapter.this.doNotify(eventType, positionStart, itemCount);
        }
    }

    public ListRowDataAdapter(ObjectAdapter adapter) {
        super(adapter.getPresenterSelector());
        this.mAdapter = adapter;
        initialize();
        if (adapter.isImmediateNotifySupported()) {
            this.mDataObserver = new SimpleDataObserver();
        } else {
            this.mDataObserver = new QueueBasedDataObserver();
        }
        attach();
    }

    /* Access modifiers changed, original: 0000 */
    public void detach() {
        this.mAdapter.unregisterObserver(this.mDataObserver);
    }

    /* Access modifiers changed, original: 0000 */
    public void attach() {
        initialize();
        this.mAdapter.registerObserver(this.mDataObserver);
    }

    /* Access modifiers changed, original: 0000 */
    public void initialize() {
        this.mLastVisibleRowIndex = -1;
        for (int i = this.mAdapter.size() - 1; i >= 0; i--) {
            if (((Row) this.mAdapter.get(i)).isRenderedAsRowView()) {
                this.mLastVisibleRowIndex = i;
                return;
            }
        }
    }

    public int size() {
        return this.mLastVisibleRowIndex + 1;
    }

    public Object get(int index) {
        return this.mAdapter.get(index);
    }

    /* Access modifiers changed, original: 0000 */
    public void doNotify(int eventType, int positionStart, int itemCount) {
        if (eventType == 2) {
            notifyItemRangeChanged(positionStart, itemCount);
        } else if (eventType == 4) {
            notifyItemRangeInserted(positionStart, itemCount);
        } else if (eventType == 8) {
            notifyItemRangeRemoved(positionStart, itemCount);
        } else if (eventType == 16) {
            notifyChanged();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid event type ");
            stringBuilder.append(eventType);
            throw new IllegalArgumentException(stringBuilder.toString());
        }
    }
}
