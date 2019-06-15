package android.support.v17.leanback.widget;

import android.database.Cursor;
import android.support.v17.leanback.database.CursorMapper;
import android.util.LruCache;

public class CursorObjectAdapter extends ObjectAdapter {
    private static final int CACHE_SIZE = 100;
    private Cursor mCursor;
    private final LruCache<Integer, Object> mItemCache = new LruCache(100);
    private CursorMapper mMapper;

    public CursorObjectAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
    }

    public CursorObjectAdapter(Presenter presenter) {
        super(presenter);
    }

    public void changeCursor(Cursor cursor) {
        if (cursor != this.mCursor) {
            if (this.mCursor != null) {
                this.mCursor.close();
            }
            this.mCursor = cursor;
            this.mItemCache.trimToSize(0);
            onCursorChanged();
        }
    }

    public Cursor swapCursor(Cursor cursor) {
        if (cursor == this.mCursor) {
            return this.mCursor;
        }
        Cursor oldCursor = this.mCursor;
        this.mCursor = cursor;
        this.mItemCache.trimToSize(0);
        onCursorChanged();
        return oldCursor;
    }

    /* Access modifiers changed, original: protected */
    public void onCursorChanged() {
        notifyChanged();
    }

    public final Cursor getCursor() {
        return this.mCursor;
    }

    public final void setMapper(CursorMapper mapper) {
        boolean changed = this.mMapper != mapper;
        this.mMapper = mapper;
        if (changed) {
            onMapperChanged();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMapperChanged() {
    }

    public final CursorMapper getMapper() {
        return this.mMapper;
    }

    public int size() {
        if (this.mCursor == null) {
            return 0;
        }
        return this.mCursor.getCount();
    }

    public Object get(int index) {
        if (this.mCursor == null) {
            return null;
        }
        if (this.mCursor.moveToPosition(index)) {
            Object item = this.mItemCache.get(Integer.valueOf(index));
            if (item != null) {
                return item;
            }
            item = this.mMapper.convert(this.mCursor);
            this.mItemCache.put(Integer.valueOf(index), item);
            return item;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public void close() {
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
    }

    public boolean isClosed() {
        return this.mCursor == null || this.mCursor.isClosed();
    }

    /* Access modifiers changed, original: protected|final */
    public final void invalidateCache(int index) {
        this.mItemCache.remove(Integer.valueOf(index));
    }

    /* Access modifiers changed, original: protected|final */
    public final void invalidateCache(int index, int count) {
        int limit = count + index;
        while (index < limit) {
            invalidateCache(index);
            index++;
        }
    }

    public boolean isImmediateNotifySupported() {
        return true;
    }
}
