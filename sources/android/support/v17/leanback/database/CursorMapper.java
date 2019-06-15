package android.support.v17.leanback.database;

import android.database.Cursor;

public abstract class CursorMapper {
    private Cursor mCursor;

    public abstract Object bind(Cursor cursor);

    public abstract void bindColumns(Cursor cursor);

    public Object convert(Cursor cursor) {
        if (cursor != this.mCursor) {
            this.mCursor = cursor;
            bindColumns(this.mCursor);
        }
        return bind(this.mCursor);
    }
}
